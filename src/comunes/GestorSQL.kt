package comunes;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import juego.JuegoServidor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;

import java.sql.PreparedStatement;

import comunes.Mundo.*;
import objetos.*;
import objetos.NPCModelo.*;
import objetos.Objeto.*;
import objetos.casas.Casas;
import objetos.casas.Cofres;
import objetos.hechizos.Hechizos;
import objetos.hechizos.Hechizos.*;
import objetos.Mapa.*;
import objetos.Gremio.GuildMember;
import objetos.Mercadillo.HdvEntry;
import realm.RealmServer;

public class GestorSQL {

	private static Connection _dinamicos;
	private static Connection _estaticos;
	
	public synchronized static ResultSet EjecutarConsulta(String query, String DBNAME) throws SQLException {
		if(!MainServidor.isInit)
			return null;
		Connection DB;
		if(DBNAME.equals(MainServidor.DB_DINAMICOS))
			DB = _dinamicos;
		else
			DB = _estaticos;
		Statement stat = DB.createStatement();
		ResultSet RS = stat.executeQuery(query);
		stat.setQueryTimeout(300);
		return RS;
	}

	public synchronized static PreparedStatement NuevaConsulta(String baseQuery, Connection dbCon) throws SQLException {
		return dbCon.prepareStatement(baseQuery);
	}

	public synchronized static void CerrarConsulta() {
		try {
			_dinamicos.close();
			_estaticos.close();
		}catch (Exception e) {
			System.out.println("Erreur a la fermeture des connexions SQL:"+e.getMessage());
			e.printStackTrace();
		}
	}

	public static boolean InicarConexion() {
		try {
			HikariConfig configDinamica = new HikariConfig();
			configDinamica.setDataSourceClassName("org.mariadb.jdbc.MySQLDataSource");
			configDinamica.addDataSourceProperty("serverName", MainServidor.DB_HOST);
			configDinamica.addDataSourceProperty("port", 3306);
			configDinamica.addDataSourceProperty("databaseName", MainServidor.DB_DINAMICOS);
			configDinamica.addDataSourceProperty("user", MainServidor.DB_USUARIO);
			configDinamica.addDataSourceProperty("password", MainServidor.DB_PASS);
			configDinamica.setAutoCommit(true);
			configDinamica.setMaximumPoolSize(50);
			configDinamica.setMinimumIdle(1);
			configDinamica.setPoolName("Dinamicos");

			HikariConfig configEstatica = new HikariConfig();
			configEstatica.setDataSourceClassName("org.mariadb.jdbc.MySQLDataSource");
			configEstatica.addDataSourceProperty("serverName", MainServidor.DB_HOST);
			configEstatica.addDataSourceProperty("port", 3306);
			configEstatica.addDataSourceProperty("databaseName", MainServidor.DB_ESTATICOS);
			configEstatica.addDataSourceProperty("user", MainServidor.DB_USUARIO);
			configEstatica.addDataSourceProperty("password", MainServidor.DB_PASS);
			configEstatica.setAutoCommit(true);
			configEstatica.setMaximumPoolSize(10);
			configEstatica.setMinimumIdle(1);
			configEstatica.setPoolName("Estaticos");

			HikariDataSource dinamicos = new HikariDataSource(configDinamica);
			HikariDataSource estaticos = new HikariDataSource(configEstatica);

			_dinamicos = dinamicos.getConnection();
			_estaticos = estaticos.getConnection();
			if(!_estaticos.isValid(1000) || !_dinamicos.isValid(1000)) {
				JuegoServidor.addToLog("SQLError : Connexion a la BD invalide!");
				return false;
			}
			return true;
		}catch(SQLException e) {
			System.out.println("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private static void CerrarResultado(ResultSet resultado) {
		try {
			resultado.getStatement().close();
			resultado.close();
		} catch (SQLException e) {e.printStackTrace();}
	}

	private static void CerrarNuevaConsulta(PreparedStatement p) {
		try {
			p.clearParameters();
			p.close();
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	public static void actualizar_datos_cuenta(Cuenta cuenta) {
		try {
			String consulta = "UPDATE `datos_cuenta` SET `kamasbanco` = ?, `banco` = ?, `nivel` = ?, `baneado` = ?, `amigos` = ?, `enemigos` = ? WHERE `id` = ?;";
			PreparedStatement p = NuevaConsulta(consulta, _dinamicos);
			p.setLong(1, cuenta.getBankKamas());
			p.setString(2, cuenta.parseBankObjetsToDB());
			p.setInt(3, cuenta.getGMLVL());
			p.setInt(4, (cuenta.isBanned()?1:0));
			p.setString(5, cuenta.parseFriendListToDB());
			p.setString(6, cuenta.parseEnemyListToDB());
			p.setInt(7, cuenta.get_GUID());
			p.executeUpdate();
			CerrarNuevaConsulta(p);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_recetas() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM `datos_recetas`;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				ArrayList<Couple<Integer,Integer>> m = new ArrayList<>();
				boolean cont = true;
				for(String str : RS.getString("recetas").split(";")) {
					try {
							int tID = Integer.parseInt(str.split("\\*")[0]);
							int qua =  Integer.parseInt(str.split("\\*")[1]);
							m.add(new Couple<>(tID, qua));
					}catch(Exception e){e.printStackTrace();cont = false;}
				}
				//s'il y a eu une erreur de parsing, on ignore cette recette
				if(!cont)continue;
				Mundo.addCraft(RS.getInt("id"), m);
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_publicidades_automaticas() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM `datos_publicidad`;", MainServidor.DB_ESTATICOS);
				while (RS.next())
					Mundo.Publicidad.add(RS.getString("texto"));
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_retos() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM `datos_retos`;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				String chal = RS.getInt("id") + "," +
						RS.getInt("gananciaxp") + "," +
						RS.getInt("gananciadrop") + "," +
						RS.getInt("gananciapormob") + "," +
						RS.getInt("condiciones");
				Mundo.addChallenge(chal);
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_gremios() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_gremio;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				Mundo.addGuild
				(new Gremio(
						RS.getInt("id"),
						RS.getString("nombre"),
						RS.getString("emblema"),
						RS.getInt("nivel"),
						RS.getLong("experiencia"),
						RS.getInt("capital"),
						RS.getInt("recaudadoresmaximos"),
						RS.getString("hechizos"),
						RS.getString("caracteristicas")),false);
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_miembros_gremio() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_miembros_gremio;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				Gremio G = Mundo.getGuild(RS.getInt("gremio"));
				if(G == null)continue;
				G.addMember(RS.getInt("id"), RS.getInt("rango"), RS.getByte("xpdonada"), RS.getLong("porcentajexp"), RS.getInt("derechos"));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_montura() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_montura;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				Mundo.addDragopavo(new Dragopavo(
						RS.getInt("id"),
						RS.getInt("color"),
						RS.getInt("sexo"),
						RS.getInt("amor"),
						RS.getInt("resistencia"),
						RS.getInt("nivel"),
						RS.getLong("experiencia"),
						RS.getString("nombre"),
						RS.getInt("fatiga"),
						RS.getInt("energia"),
						RS.getInt("reproducciones"),
						RS.getInt("madurez"),
						RS.getInt("serenidad"),
						RS.getString("objetos"),
						RS.getString("ancestros"),
						RS.getString("habilidad")));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_drops() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_drops;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				Monstruo MT = Mundo.getMonstre(RS.getInt("monstruo"));
				MT.addDrop(new Drop(
						RS.getInt("objeto"),
						RS.getInt("limite"),
						RS.getFloat("maximo"),
						RS.getInt("porcentaje")));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_sets() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_objetos_sets;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				Mundo.addItemSet(new ItemSet(
								RS.getInt("id"),
								RS.getString("objetos"),
								RS.getString("bonus")
							));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void LOAD_IOTEMPLATE() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_objetos_interactivos;", MainServidor.DB_ESTATICOS);
			while(RS.next())
			{
				Mundo.addIOTemplate(new IOTemplate(
								RS.getInt("id"),
								RS.getInt("actualizar"),
								RS.getInt("duracion"),
								RS.getInt("desconocido"),
								RS.getInt("caminable")==1));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static int cargar_cercados() {
		int nbr = 0;
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_cercados;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				Mapa map = Mundo.getCarte(RS.getShort("mapa"));
				if(map == null)continue;
					Mundo.addMountPark(new MountPark(
						RS.getInt("dueño"), map,
						RS.getInt("celda"),
						RS.getInt("tamaño"),
						RS.getString("monturas"),
						RS.getInt("gremio"),
						RS.getInt("precio")));
					nbr++;
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			nbr = 0;
		}
		return nbr;
	}

	public static void cargar_oficios() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_oficios;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				Mundo.addJob(new Oficio(
							RS.getInt("id"),
							RS.getString("herramientas"),
							RS.getString("recetas")
							));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void cargar_area() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_area;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				Area A = new Area(
						RS.getInt("id"),
						RS.getInt("superarea"),
						RS.getString("nombre"));
				Mundo.addArea(A);
				//on ajoute la zone au continent
				A.get_superArea().addArea(A);
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void cargar_subareas() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_subareas;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				SubArea SA = new SubArea(
						RS.getInt("id"),
						RS.getInt("area"),
						RS.getInt("alineacion"),
						RS.getString("nombre"));
				Mundo.addSubArea(SA);
				//on ajoute la sous zone a la zone
				if(SA.get_area() != null)
					SA.get_area().addSubArea(SA);
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static int cargar_npc() {
		int nbr = 0;
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_npc;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				Mapa map = Mundo.getCarte(RS.getShort("mapa"));
				if(map == null)continue;
				map.addNpc(RS.getInt("npc"), RS.getInt("celda"), RS.getInt("orientacion"));
				nbr ++;
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			nbr = 0;
		}
		return nbr;
	}

	public static int cargar_recaudadores() {
		int nbr = 0;
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_recaudadores;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				Mapa map = Mundo.getCarte(RS.getShort("mapid"));
				if(map == null)continue;
				
				Mundo.addPerco(new Recaudador(
						RS.getInt("id"),
						RS.getShort("mapa"),
						RS.getInt("celda"),
						RS.getByte("orientacion"),
						RS.getInt("gremio"),
						RS.getShort("N1"),
						RS.getShort("N2"),
						RS.getString("objetos"),
						RS.getLong("kamas"),
						RS.getLong("experiencia")
						));
				nbr ++;
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			nbr = 0;
		}
		return nbr;
	}

	public static int cargar_casas() {
		int nbr = 0;
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_casas;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				Mapa map = Mundo.getCarte(RS.getShort("mapa"));
				if(map == null)continue;
				
				Mundo.addHouse(new Casas(
						RS.getInt("id"),
						RS.getShort("mapa"),
						RS.getInt("celda"),
						RS.getInt("dueño"),
						RS.getInt("venta"),
						RS.getInt("gremio"),
						RS.getInt("acceso"),
						RS.getString("llave"),
						RS.getInt("derechosgremio"),
						RS.getInt("mapa_interior"),
						RS.getInt("celda_interior")
						));
				nbr ++;
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			nbr = 0;
		}
		return nbr;
	}

	public static void cargar_cuentas() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_cuenta;", MainServidor.DB_DINAMICOS);
			String baseQuery = "UPDATE datos_cuenta SET `actualizarnecesita` = 0 WHERE id = ?;";
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			while(RS.next()) {
				Cuenta C = new Cuenta(
						RS.getInt("id"),
						RS.getString("cuenta").toLowerCase(),
						RS.getString("contraseña"),
						RS.getString("apodo"),
						RS.getString("pregunta"),
						RS.getString("respuesta"),
						RS.getInt("nivel"),
						RS.getInt("vip"),
						(RS.getInt("baneado") == 1),
						RS.getString("ultimaip"),
						RS.getString("ultimafechaconexion"),
						RS.getString("banco"),
						RS.getInt("kamasbanco"),
						RS.getString("amigos"),
						RS.getString("enemigos"));
				Mundo.addAccount(C);
				Mundo.addAccountbyName(C);
				
				p.setInt(1, RS.getInt("id"));
				p.executeUpdate();
				
			}
			CerrarNuevaConsulta(p);
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static int getSiguienteIDPersonaje() {
		try {
			ResultSet RS = EjecutarConsulta("SELECT id FROM datos_personajes ORDER BY id DESC LIMIT 1;", MainServidor.DB_DINAMICOS);
			if(!RS.first())return 1;
			int guid = RS.getInt("id");
			guid++;
			CerrarResultado(RS);
			return guid;
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			MainServidor.closeServers();
		}
		return 0;
	}

	public static void cargar_personaje_por_cuenta(int accID) {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_personajes WHERE cuenta = '"+accID+"';", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				TreeMap<Integer,Integer> stats = new TreeMap<>();
				stats.put(Constantes.STATS_ADD_VITA, RS.getInt("vitalidad"));
				stats.put(Constantes.STATS_ADD_FORC, RS.getInt("fuerza"));
				stats.put(Constantes.STATS_ADD_SAGE, RS.getInt("sabiduria"));
				stats.put(Constantes.STATS_ADD_INTE, RS.getInt("inteligencia"));
				stats.put(Constantes.STATS_ADD_CHAN, RS.getInt("suerte"));
				stats.put(Constantes.STATS_ADD_AGIL, RS.getInt("agilidad"));
				
				Personaje perso = new Personaje(
						RS.getInt("id"),
						RS.getString("nombre"),
						RS.getInt("sexo"),
						RS.getInt("clase"),
						RS.getInt("color1"),
						RS.getInt("color2"),
						RS.getInt("color3"),
						RS.getLong("kamas"),
						RS.getInt("puntoshechizo"),
						RS.getInt("capital"),
						RS.getInt("energia"),
						RS.getInt("nivel"),
						RS.getLong("experiencia"),
						RS.getInt("tamaño"),
						RS.getInt("gfx"),
						RS.getByte("alineacion"),
						RS.getInt("cuenta"), stats,
						RS.getByte("veramigos"),
						RS.getByte("veralineacion"),
						RS.getByte("vervendedor"),
						RS.getString("canales"),
						RS.getShort("mapa"),
						RS.getInt("celda"),
						RS.getString("objetos"),
						RS.getString("objetosmercante"),
						RS.getInt("puntosdevida"),
						RS.getString("hechizos"),
						RS.getString("puntoguardado"),
						RS.getString("oficios"),
						RS.getInt("xpmontura"),
						RS.getInt("montura"),
						RS.getInt("honor"),
						RS.getInt("deshonor"),
						RS.getInt("nivelalineacion"),
						RS.getString("zaaps"),
						RS.getByte("titulo"),
						RS.getInt("esposo"));
				//Vérifications pré-connexion
				perso.VerifAndChangeItemPlace();
				Mundo.addPersonnage(perso);
				int guildId = isPersoInGuild(RS.getInt("id"));
				if(guildId >= 0) {
					perso.setGuildMember(Mundo.getGuild(guildId).getMember(RS.getInt("id")));
				}
				if(Mundo.getCompte(accID) != null)
					Mundo.getCompte(accID).addPerso(perso);
			}
			
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			MainServidor.closeServers();
		}
	}

	public static void cargar_personaje() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_personajes;", MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				TreeMap<Integer,Integer> stats = new TreeMap<>();
				stats.put(Constantes.STATS_ADD_VITA, RS.getInt("vitalidad"));
				stats.put(Constantes.STATS_ADD_FORC, RS.getInt("fuerza"));
				stats.put(Constantes.STATS_ADD_SAGE, RS.getInt("sabiduria"));
				stats.put(Constantes.STATS_ADD_INTE, RS.getInt("inteligencia"));
				stats.put(Constantes.STATS_ADD_CHAN, RS.getInt("suerte"));
				stats.put(Constantes.STATS_ADD_AGIL, RS.getInt("agilidad"));
				
				Personaje perso = new Personaje(
						RS.getInt("id"),
						RS.getString("nombre"),
						RS.getInt("sexo"),
						RS.getInt("clase"),
						RS.getInt("color1"),
						RS.getInt("color2"),
						RS.getInt("color3"),
						RS.getLong("kamas"),
						RS.getInt("puntoshechizo"),
						RS.getInt("capital"),
						RS.getInt("energia"),
						RS.getInt("nivel"),
						RS.getLong("experiencia"),
						RS.getInt("tamaño"),
						RS.getInt("gfx"),
						RS.getByte("alineacion"),
						RS.getInt("cuenta"), stats,
						RS.getByte("veramigos"),
						RS.getByte("veralineacion"),
						RS.getByte("vervendedor"),
						RS.getString("canales"),
						RS.getShort("mapa"),
						RS.getInt("celda"),
						RS.getString("objetos"),
						RS.getString("objetosmercante"),
						RS.getInt("puntosdevida"),
						RS.getString("hechizos"),
						RS.getString("puntoguardado"),
						RS.getString("oficios"),
						RS.getInt("xpmontura"),
						RS.getInt("montura"),
						RS.getInt("honor"),
						RS.getInt("deshonor"),
						RS.getInt("nivelalineacion"),
						RS.getString("zaaps"),
						RS.getByte("titulo"),
						RS.getInt("esposo"));
				//Vérifications pré-connexion
				perso.VerifAndChangeItemPlace();
				Mundo.addPersonnage(perso);
				if(Mundo.getCompte(RS.getInt("cuenta")) != null)
					Mundo.getCompte(RS.getInt("cuenta")).addPerso(perso);
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			MainServidor.closeServers();
		}
	}

	public static boolean eliminar_personaje_db(Personaje perso) {
		int guid = perso.get_GUID();
		String baseQuery = "DELETE FROM datos_personajes WHERE id = ?;";
		
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, guid);
			
			p.execute();
			
			if(!perso.getItemsIDSplitByChar(",").equals("")) {
				baseQuery = "DELETE FROM datos_objetos WHERE id IN (?);";
				p = NuevaConsulta(baseQuery, _dinamicos);
				p.setString(1, perso.getItemsIDSplitByChar(","));
				
				p.execute();
			}
			if(!perso.getStoreItemsIDSplitByChar(",").equals("")) {
				baseQuery = "DELETE FROM datos_objetos WHERE id IN (?);";
				p = NuevaConsulta(baseQuery, _dinamicos);
				p.setString(1, perso.getStoreItemsIDSplitByChar(","));
				
				p.execute();
			}
			if(perso.getMount() != null) {
				baseQuery = "DELETE FROM datos_montura WHERE id = ?";
				p = NuevaConsulta(baseQuery, _dinamicos);
				p.setInt(1, perso.getMount().getID());
				
				p.execute();
				Mundo.delDragoByID(perso.getMount().getID());
			}
			
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
			JuegoServidor.addToLog("Game: Supression du personnage echouee");
			return false;
		}
	}

	public static boolean agregar_personaje_db(Personaje perso) {
		String baseQuery = "INSERT INTO datos_personajes( `id` , `nombre` , `sexo` , `clase` , `color1` , `color2` , `color3` , `kamas` , `puntoshechizo` , `capital` , `energia` , `nivel` , `experiencia`, `tamaño`, `gfx`, `cuenta`, `celda`,`mapa`,`hechizos`,`objetos`, `objetosmercante`)" +
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'', '');";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1,perso.get_GUID());
			p.setString(2, perso.getNombre());
			p.setInt(3,perso.getSexo());
			p.setInt(4,perso.getClase());
			p.setInt(5,perso.get_color1());
			p.setInt(6,perso.get_color2());
			p.setInt(7,perso.get_color3());
			p.setLong(8,perso.getKamas());
			p.setInt(9,perso.get_spellPts());
			p.setInt(10,perso.get_capital());
			p.setInt(11,perso.get_energy());
			p.setInt(12,perso.get_lvl());
			p.setLong(13,perso.get_curExp());
			p.setInt(14,perso.get_size());
			p.setInt(15,perso.get_gfxID());
			p.setInt(16,perso.getAccID());
			p.setInt(17,perso.getActualCelda().getID());
			p.setInt(18,perso.getActualMapa().getID());
			p.setString(19, perso.parseSpellToDB());
			
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
			JuegoServidor.addToLog("Game: Creation du personnage echouee");
			return false;
		}
	}

	public static void cargar_experiencias() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_experiencia;", MainServidor.DB_ESTATICOS);
			while(RS.next()) Mundo.addExpLevel(RS.getInt("nivel"),new Mundo.ExpLevel(RS.getLong("personaje"),RS.getInt("oficio"),RS.getInt("dragopavo"),RS.getInt("alineacion")));
			CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
	}

	public static int cargar_celdas() {
		try {
			int nbr = 0;
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM `datos_celdas_accion`", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				if(Mundo.getCarte(RS.getShort("mapa")) == null) continue;
				if(Mundo.getCarte(RS.getShort("mapa")).getMapa(RS.getInt("celda")) == null) continue;

				if (RS.getInt("tipo") == 1) {//Stop sur la case(triggers)
					Mundo.getCarte(RS.getShort("mapa")).getMapa(RS.getInt("celda")).addOnCellStopAction(RS.getInt("accion"), RS.getString("argumento"), RS.getString("condicion"));
				} else {
					JuegoServidor.addToLog("Action Event " + RS.getInt("tipo") + " non implante");
				}
				nbr++;
			}
			CerrarResultado(RS);
			return nbr;
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
		return 0;
	}

	public static void cargar_mapas() {
		try {
			ResultSet RS;
			RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_mapas LIMIT "+ Constantes.LIMITE_MAPAS +";", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
					Mundo.addCarte(new Mapa(
							RS.getShort("id"),
							RS.getString("datos"),
							RS.getByte("ancho"),
							RS.getByte("largo"),
							RS.getString("llave"),
							RS.getString("esquemapelea"),
							RS.getString("datosmapa"),
							RS.getString("celdas"),
							RS.getString("monstruos"),
							RS.getString("pos"),
							RS.getByte("numerogrupos"),
							RS.getByte("tamañogrupo")));
			}
			GestorSQL.CerrarResultado(RS);
			RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_grupo_mobs;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
					Mapa c = Mundo.getCarte(RS.getShort("mapa"));
					if(c == null)continue;
					if(c.getMapa(RS.getInt("celda")) == null)continue;
					c.addStaticGroup(RS.getInt("celda"), RS.getString("grupo"));
			}
			GestorSQL.CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
	}

	public static void guardar_personaje(Personaje _perso, boolean saveItem) {
		String baseQuery = "UPDATE `datos_personajes` SET "+
						"`kamas`= ?,"+
						"`puntoshechizo`= ?,"+
						"`capital`= ?,"+
						"`energia`= ?,"+
						"`nivel`= ?,"+
						"`experiencia`= ?,"+
						"`tamaño` = ?," +
						"`gfx`= ?,"+
						"`alineacion`= ?,"+
						"`honor`= ?,"+
						"`deshonor`= ?,"+
						"`nivelalineacion`= ?,"+
						"`vitalidad`= ?,"+
						"`fuerza`= ?,"+
						"`sabiduria`= ?,"+
						"`inteligencia`= ?,"+
						"`suerte`= ?,"+
						"`agilidad`= ?,"+
						"`verhechizo`= ?,"+
						"`veramigos`= ?,"+
						"`veralineacion`= ?,"+
						"`vervendedor`= ?,"+
						"`canales`= ?,"+
						"`mapa`= ?,"+
						"`celda`= ?,"+
						"`puntosdevida`= ?,"+
						"`hechizos`= ?," +
						"`objetos`= ?,"+
						"`objetosmercante`= ?,"+
						"`puntoguardado`= ?,"+
						"`zaaps`= ?,"+
						"`oficios`= ?,"+
						"`xpmontura`= ?,"+
						"`montura`= ?,"+
						"`titulo`= ?,"+
						"`esposo`= ?"+
						" WHERE `datos_personajes`.`id` = ? LIMIT 1 ;";
		
		PreparedStatement p = null;
		try {
			p = NuevaConsulta(baseQuery, _dinamicos);
			
			p.setLong(1,_perso.getKamas());
			p.setInt(2,_perso.get_spellPts());
			p.setInt(3,_perso.get_capital());
			p.setInt(4,_perso.get_energy());
			p.setInt(5,_perso.get_lvl());
			p.setLong(6,_perso.get_curExp());
			p.setInt(7,_perso.get_size());
			p.setInt(8,_perso.get_gfxID());
			p.setInt(9,_perso.get_align());
			p.setInt(10,_perso.get_honor());
			p.setInt(11,_perso.getDeshonor());
			p.setInt(12,_perso.getALvl());
			p.setInt(13,_perso.get_baseStats().getEffect(Constantes.STATS_ADD_VITA));
			p.setInt(14,_perso.get_baseStats().getEffect(Constantes.STATS_ADD_FORC));
			p.setInt(15,_perso.get_baseStats().getEffect(Constantes.STATS_ADD_SAGE));
			p.setInt(16,_perso.get_baseStats().getEffect(Constantes.STATS_ADD_INTE));
			p.setInt(17,_perso.get_baseStats().getEffect(Constantes.STATS_ADD_CHAN));
			p.setInt(18,_perso.get_baseStats().getEffect(Constantes.STATS_ADD_AGIL));
			p.setInt(19,(_perso.is_showSpells()?1:0));
			p.setInt(20,(_perso.is_showFriendConnection()?1:0));
			p.setInt(21,(_perso.is_showWings()?1:0));
			p.setInt(22,(_perso.is_showSeller()?1:0));
			p.setString(23,_perso.get_canaux());
			p.setInt(24,_perso.getActualMapa().getID());
			p.setInt(25,_perso.getActualCelda().getID());
			p.setInt(26,_perso.get_pdvper());
			p.setString(27,_perso.parseSpellToDB());
			p.setString(28,_perso.parseObjetsToDB());
			p.setString(29, _perso.parseStoreItemstoBD());
			p.setString(30,_perso.get_savePos());
			p.setString(31,_perso.parseZaaps());
			p.setString(32,_perso.parseJobData());
			p.setInt(33,_perso.getMountXpGive());
			p.setInt(34, (_perso.getMount()!=null?_perso.getMount().getID():-1));
			p.setByte(35,(_perso.get_title()));
			p.setInt(36,_perso.getWife());
			p.setInt(37,_perso.get_GUID());
			
			p.executeUpdate();
			
			if(_perso.getMiembroGremio() != null)
				actualizar_miembro_del_gremio(_perso.getMiembroGremio());
			if(_perso.getMount() != null)
				actualizar_informacion_monturas(_perso.getMount());
			JuegoServidor.addToLog("Personaje "+_perso.getNombre()+" guardado");
		}catch(Exception e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.out.println("Requete: "+baseQuery);
			System.out.println("Le personnage n'a pas ete sauvegarde");
			System.exit(1);
		}
		if(saveItem) {
			baseQuery = "UPDATE `datos_objetos` SET cantidad = ?, ubicacion = ?, caracteristicas = ? WHERE id = ?;";
			try {
				p = NuevaConsulta(baseQuery, _dinamicos);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			for(String idStr : _perso.getItemsIDSplitByChar(":").split(":")) {
				try {
					int guid = Integer.parseInt(idStr);
					Objeto obj = Mundo.getObjet(guid);
					if(obj == null)continue;
					
					p.setInt(1, obj.getQuantity());
					p.setInt(2, obj.getPosition());
					p.setString(3, obj.parseToSave());
					p.setInt(4, Integer.parseInt(idStr));
					
					p.execute();
				}catch(Exception e){continue;}
			}
			
			if(_perso.getCuenta() == null)
				return;
			for(String idStr : _perso.getBankItemsIDSplitByChar(":").split(":")) {
				try {
					int guid = Integer.parseInt(idStr);
					Objeto obj = Mundo.getObjet(guid);
					if(obj == null)continue;
					
					p.setInt(1, obj.getQuantity());
					p.setInt(2, obj.getPosition());
					p.setString(3, obj.parseToSave());
					p.setInt(4, Integer.parseInt(idStr));
					
					p.execute();
				}catch(Exception e){continue;}
			}
		}
		CerrarNuevaConsulta(p);
	}

	public static void cargar_hechizos() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_hechizos;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				int id = RS.getInt("id");
				Hechizos sort = new Hechizos(id,RS.getInt("sprite"),RS.getString("infosprite"),RS.getString("objetivoefecto"));
				SortStats l1 = parseSortStats(id,1,RS.getString("lvl1"));
				SortStats l2 = parseSortStats(id,2,RS.getString("lvl2"));
				SortStats l3 = parseSortStats(id,3,RS.getString("lvl3"));
				SortStats l4 = parseSortStats(id,4,RS.getString("lvl4"));
				SortStats l5 = null;
				if(!RS.getString("lvl5").equalsIgnoreCase("-1"))
					l5 = parseSortStats(id,5,RS.getString("lvl5"));
				SortStats l6 = null;
				if(!RS.getString("lvl6").equalsIgnoreCase("-1"))
						l6 = parseSortStats(id,6,RS.getString("lvl6"));
				sort.addSortStats(1,l1);
				sort.addSortStats(2,l2);
				sort.addSortStats(3,l3);
				sort.addSortStats(4,l4);
				sort.addSortStats(5,l5);
				sort.addSortStats(6,l6);
				Mundo.addSort(sort);
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
	}

	public static void cargar_objetos_modelo() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_objeto_modelo;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
					Mundo.addObjTemplate(new ObjTemplate(
							RS.getInt("id"),
							RS.getString("caracteristicas"),
							RS.getString("nombre"),
							RS.getInt("tipo"),
							RS.getInt("nivel"),
							RS.getInt("pod"),
							RS.getInt("precio"),
							RS.getInt("set"),
							RS.getString("condicion"),
							RS.getString("infoarma"),
							RS.getInt("vendido"),
							RS.getInt("preciomedio")));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
	}

	private static SortStats parseSortStats(int id,int lvl,String str) {
		try {
			SortStats stats = null;
			String[] stat = str.split(",");
			String effets = stat[0];
			String CCeffets = stat[1];
			int PACOST = 6;
			try {
				PACOST = Integer.parseInt(stat[2].trim());
			}catch(NumberFormatException ignored){}

			int POm = Integer.parseInt(stat[3].trim());
			int POM = Integer.parseInt(stat[4].trim());
			int TCC = Integer.parseInt(stat[5].trim());
			int TEC = Integer.parseInt(stat[6].trim());
			boolean line = stat[7].trim().equalsIgnoreCase("true");
			boolean LDV = stat[8].trim().equalsIgnoreCase("true");
			boolean emptyCell = stat[9].trim().equalsIgnoreCase("true");
			boolean MODPO = stat[10].trim().equalsIgnoreCase("true");
			//int unk = Integer.parseInt(stat[11]);//All 0
			int MaxByTurn = Integer.parseInt(stat[12].trim());
			int MaxByTarget = Integer.parseInt(stat[13].trim());
			int CoolDown = Integer.parseInt(stat[14].trim());
			String type = stat[15].trim();
			int level = Integer.parseInt(stat[stat.length-2].trim());
			boolean endTurn = stat[19].trim().equalsIgnoreCase("true");
			stats = new SortStats(id,lvl,PACOST,POm, POM, TCC, TEC, line, LDV, emptyCell, MODPO, MaxByTurn, MaxByTarget, CoolDown, level, endTurn, effets, CCeffets,type);
			return stats;
		}catch(Exception e) {
			e.printStackTrace();
			int nbr = 0;
			System.out.println("[DEBUG]Sort "+id+" lvl "+lvl);
			for(String z:str.split(",")) {
				System.out.println("[DEBUG]"+nbr+" "+z);
				nbr++;
			}
			System.exit(1);
			return null;
		}
	}

	public static void cargar_monstruo_modelo() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_monstruos;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				int id = RS.getInt("id");
				int gfxID = RS.getInt("gfx");
				int align = RS.getInt("alineacion");
				String colors = RS.getString("color");
				String grades = RS.getString("grado");
				String spells = RS.getString("hechizo");
				String stats = RS.getString("caracteristicas");
				String pdvs = RS.getString("puntosdevida");
				String pts = RS.getString("puntos");
				String inits = RS.getString("iniciativa");
				int mK = RS.getInt("kamasminimas");
				int MK = RS.getInt("kamasmaximas");
				int IAType = RS.getInt("tipoia");
				String xp = RS.getString("experiencia");
				boolean capturable;
				capturable = RS.getInt("capturable") == 1;
				Mundo.addMobTemplate(id, new Monstruo(id, gfxID, align, colors, grades, spells, stats, pdvs, pts, inits, mK, MK, xp, IAType, capturable));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
	}

	public static void cargar_npc_modelo() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_npc_modelo;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				int id = RS.getInt("id");
				int bonusValue = RS.getInt("bonificacion");
				int gfxID = RS.getInt("gfx");
				int scaleX = RS.getInt("escalax");
				int scaleY = RS.getInt("escalay");
				int sex = RS.getInt("sexo");
				int color1 = RS.getInt("color1");
				int color2 = RS.getInt("color2");
				int color3 = RS.getInt("color3");
				String access = RS.getString("accesorios");
				int extraClip = RS.getInt("clipextra");
				int customArtWork = RS.getInt("personalizacion");
				int initQId = RS.getInt("pregunta");
				String ventes = RS.getString("ventas");
				Mundo.addNpcTemplate(new NPCModelo(id, bonusValue, gfxID, scaleX, scaleY, sex, color1, color2, color3, access, extraClip, customArtWork, initQId, ventes));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
	}

	public static void guardar_nuevo_objeto(Objeto item) {
		try {
		String baseQuery = "REPLACE INTO `datos_objetos` VALUES(?,?,?,?,?);";
		PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
		p.setInt(1,item.getGuid());
		p.setInt(2,item.getTemplate().getID());
		p.setInt(3,item.getQuantity());
		p.setInt(4,item.getPosition());
		p.setString(5,item.parseToSave());
		p.execute();
		CerrarNuevaConsulta(p);
		} catch (SQLException e) {e.printStackTrace();}
	}

	public static boolean guardar_nuevo_grupo_monstruos(int mapID, int cellID, String groupData) {
		try {
		String baseQuery = "REPLACE INTO `datos_grupo_mobs` VALUES(?,?,?)";
		PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
		
		p.setInt(1, mapID);
		p.setInt(2, cellID);
		p.setString(3, groupData);
		
		p.execute();
		CerrarNuevaConsulta(p);
		
		return true;
		} catch (SQLException e) {e.printStackTrace();}
		return false;
	}

	public static void cargar_preguntas_npc() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_npc_pregunta;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				Mundo.addNPCQuestion(new NPC_question(
						RS.getInt("id"),
						RS.getString("respuesta"),
						RS.getString("parametro"),
						RS.getString("condicion"),
						RS.getInt("esfalso")));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
	}

	public static void cargar_respuestas_npc() {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_npc_respuesta;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				int id = RS.getInt("id");
				int type = RS.getInt("tipo");
				String args = RS.getString("argumento");
				if(Mundo.getNPCreponse(id) == null)
					Mundo.addNPCreponse(new NPC_reponse(id));
				Mundo.getNPCreponse(id).addAction(new Accion(type,args,""));
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
	}

	public static int cargar_acciones_fin_pelea() {
		int nbr = 0;
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_fin_pelea_accion;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				Mapa map = Mundo.getCarte(RS.getShort("mapa"));
				if(map == null)continue;
				map.addEndFightAction(RS.getInt("tipopelea"),
						new Accion(RS.getInt("accion"),RS.getString("argumento"),RS.getString("condicion")));
				nbr++;
			}
			CerrarResultado(RS);
			return nbr;
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
		return nbr;
	}

	public static int cargar_accion_objetos() {
		int nbr = 0;
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_objetos_accion;", MainServidor.DB_ESTATICOS);
			while(RS.next()) {
				int id = RS.getInt("modelo");
				int type = RS.getInt("tipo");
				String args = RS.getString("argumento");
				if(Mundo.getObjTemplate(id) == null)continue;
				Mundo.getObjTemplate(id).addAction(new Accion(type,args,""));
				nbr++;
			}
			CerrarResultado(RS);
			return nbr;
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.exit(1);
		}
		return nbr;
	}

	public static void cargando_objetos(String ids) {
		String req = "SELECT * FROM datos_objetos WHERE id IN ("+ids+");";
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta(req, MainServidor.DB_DINAMICOS);
			while(RS.next()) {
				int guid 	= RS.getInt("id");
				int tempID 	= RS.getInt("modelo");
				int qua 	= RS.getInt("cantidad");
				int pos		= RS.getInt("ubicacion");
				String stats= RS.getString("caracteristicas");
				Mundo.addObjet(Mundo.newObjet(guid, tempID, qua, pos, stats), false);
			}
			CerrarResultado(RS);
		}catch(SQLException e) {
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.out.println("Requete: \n"+req);
			System.exit(1);
		}
	}

	public static void eliminar_objeto(int guid) {
		String baseQuery = "DELETE FROM datos_objetos WHERE id = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, guid);
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void guardar_objeto(Objeto item) {
		String baseQuery = "REPLACE INTO `datos_objetos` VALUES (?,?,?,?,?);";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, item.getGuid());
			p.setInt(2, item.getTemplate().getID());
			p.setInt(3, item.getQuantity());
			p.setInt(4, item.getPosition());
			p.setString(5,item.parseToSave());
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}	
	}

	public static void crear_montura(Dragopavo DD) {
		String baseQuery = "REPLACE INTO `datos_montura`(`id`,`color`,`sexo`,`nombre`,`experiencia`,`nivel`," +
				"`resistencia`,`amor`,`madurez`,`serenidad`,`reproducciones`,`fatiga`,`objetos`," +
				"`ancestros`,`energia`, `habilidad`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1,DD.getID());
			p.setInt(2,DD.getColor());
			p.setInt(3,DD.getSexo());
			p.setString(4,DD.get_nom());
			p.setLong(5,DD.get_exp());
			p.setInt(6,DD.get_level());
			p.setInt(7,DD.get_endurance());
			p.setInt(8,DD.getAmor());
			p.setInt(9,DD.get_maturite());
			p.setInt(10,DD.get_serenite());
			p.setInt(11,DD.get_reprod());
			p.setInt(12,DD.get_fatigue());
			p.setString(13,DD.getItemsId());
			p.setString(14,DD.getAncestros());
			p.setInt(15,DD.get_energie());
			p.setString(16, DD.get_ability());
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void eliminar_montura(int DID) {
		String baseQuery = "DELETE FROM `datos_montura` WHERE `id` = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, DID);
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void cargar_cuenta_por_id(int user) {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_cuenta WHERE `id` = '"+user+"';", MainServidor.DB_DINAMICOS);
			String baseQuery = "UPDATE datos_cuenta SET `actualizarnecesita` = 0 WHERE id = ?;";
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			while(RS.next()) {
				//Si le compte est déjà connecté, on zap
				if(Mundo.getCompte(RS.getInt("id")) != null)if(Mundo.getCompte(RS.getInt("id")).isOnline())continue;
				
				Cuenta C = new Cuenta(
						RS.getInt("id"),
						RS.getString("cuenta").toLowerCase(),
						RS.getString("contraseña"),
						RS.getString("apodo"),
						RS.getString("pregunta"),
						RS.getString("respuesta"),
						RS.getInt("nivel"),
						RS.getInt("vip"),
						(RS.getInt("baneado") == 1),
						RS.getString("ultimaip"),
						RS.getString("ultimafechaconexion"),
						RS.getString("banco"),
						RS.getInt("kamasbanco"),
						RS.getString("amigos"),
						RS.getString("enemigos"));
				Mundo.addAccount(C);
				Mundo.ReassignAccountToChar(C);
				p.setInt(1, RS.getInt("guid"));
				p.executeUpdate();
			}
			
			CerrarNuevaConsulta(p);
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void Cargar_cuenta_por_usuario(String user) {
		try {
			ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_cuenta WHERE `cuenta` LIKE '"+user+"';", MainServidor.DB_DINAMICOS);
			String baseQuery = "UPDATE datos_cuenta SET `actualizarnecesita` = 0 WHERE id = ?;";
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			
			while(RS.next()) {
				//Si le compte est déjà connecté, on zap
				if(Mundo.getCompte(RS.getInt("id")) != null)
					if(Mundo.getCompte(RS.getInt("id")).isOnline())
						continue;
				
				Cuenta C = new Cuenta(
						RS.getInt("id"),
						RS.getString("cuenta").toLowerCase(),
						RS.getString("contraseña"),
						RS.getString("apodo"),
						RS.getString("pregunta"),
						RS.getString("respuesta"),
						RS.getInt("nivel"),
						RS.getInt("vip"),
						(RS.getInt("baneado") == 1),
						RS.getString("ultimaip"),
						RS.getString("ultimafechaconexion"),
						RS.getString("banco"),
						RS.getInt("kamasbanco"),
						RS.getString("amigos"),
						RS.getString("enemigos"));
				Mundo.addAccount(C);
				Mundo.ReassignAccountToChar(C);
				
				p.setInt(1, RS.getInt("id"));
				p.executeUpdate();
			}
			CerrarNuevaConsulta(p);
			CerrarResultado(RS);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void actualizar_ultima_fecha_conexion(Cuenta compte) {
		String baseQuery = "UPDATE datos_cuenta SET `ultimaip` = ?, `ultimafechaconexion` = ? WHERE `id` = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			
			p.setString(1, compte.getActualIP());
			p.setString(2, compte.getLastConnectionDate());
			p.setInt(3, compte.get_GUID());
			
			p.executeUpdate();
			CerrarNuevaConsulta(p);
		}catch(SQLException e) {
			RealmServer.addToLog("SQL ERROR: "+e.getMessage());
			RealmServer.addToLog("Query: "+baseQuery);
			e.printStackTrace();
		}
	}

	public static void actualizar_informacion_monturas(Dragopavo DD) {
		String baseQuery = "UPDATE datos_montura SET " +
		"`nombre` = ?," +
		"`experiencia` = ?," +
		"`nivel` = ?," +
		"`resistencia` = ?," +
		"`amor` = ?," +
		"`madurez` = ?," +
		"`serenidad` = ?," +
		"`reproducciones` = ?," +
		"`fatiga` = ?," +
		"`energia` = ?," +
		"`ancestros` = ?," +
		"`objetos` = ?," +
		"`habilidad` = ?" +
		" WHERE `id` = ?;";
		
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setString(1,DD.get_nom());
			p.setLong(2,DD.get_exp());
			p.setInt(3,DD.get_level());
			p.setInt(4,DD.get_endurance());
			p.setInt(5,DD.getAmor());
			p.setInt(6,DD.get_maturite());
			p.setInt(7,DD.get_serenite());
			p.setInt(8,DD.get_reprod());
			p.setInt(9,DD.get_fatigue());
			p.setInt(10,DD.get_energie());
			p.setString(11,DD.getAncestros());
			p.setString(12,DD.getItemsId());
			p.setString(13,DD.get_ability());
			p.setInt(14, DD.getID());
			
			p.execute();
			CerrarNuevaConsulta(p);

		}catch(SQLException e) {
			JuegoServidor.addToLog("SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Query: "+baseQuery);
			e.printStackTrace();
		}
	}

	public static void guardar_cercados(MountPark MP) {
		String baseQuery = "REPLACE INTO `datos_cercados`( `mapa` , `celda`, `tamaño` , `dueño` , `gremio` , `precio` , `monturas` ) VALUES (?,?,?,?,?,?,?);";
				
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1,MP.get_map().getID());
			p.setInt(2,MP.get_cellid());
			p.setInt(3,MP.get_size());
			p.setInt(4,MP.get_owner());
			p.setInt(5,(MP.get_guild()==null?-1:MP.get_guild().get_id()));
			p.setInt(6,MP.get_price());
			p.setString(7,MP.parseDBData());
			
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void actualizar_cercado(MountPark MP) {
		String baseQuery = "UPDATE `datos_cercados` SET `monturas` = ? WHERE mapa = ?;";
		
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setString(1, MP.parseDBData());
			p.setShort(2, MP.get_map().getID());
			
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static boolean guardar_celdas(int mapID1, int cellID1, int action, int event, String args, String cond) {
		String baseQuery = "REPLACE INTO `datos_celdas_accion` VALUES (?,?,?,?,?,?);";
		
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1,mapID1);
			p.setInt(2,cellID1);
			p.setInt(3,action);
			p.setInt(4,event);
			p.setString(5,args);
			p.setString(6,cond);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean eliminar_celdas(int mapID, int cellID) {
		String baseQuery = "DELETE FROM `datos_celdas_accion` WHERE `mapa` = ? AND `celda` = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, mapID);
			p.setInt(2, cellID);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean guardar_mapa(Mapa map) {
		String baseQuery = "UPDATE `datos_mapas` SET `esquemapelea` = ?, `numerogrupos` = ? WHERE id = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setString(1,map.getEsquemaPelea());
			p.setInt(2, map.getMaxGroupNumb());
			p.setInt(3, map.getID());
			p.executeUpdate();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean eliminar_npc_en_mapa(int m, int c) {
		String baseQuery = "DELETE FROM datos_npc WHERE mapa = ? AND celda = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, m);
			p.setInt(2, c);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean eliminar_recaudador(int id) {
		String baseQuery = "DELETE FROM datos_recaudadores WHERE id = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, id);
			
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean agregar_publicidad(String publicidad) {
		String baseQuery = "INSERT INTO `datos_publicidad` VALUES (?, ?);";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, 0);
			p.setString(2, publicidad);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean agregar_npc_en_mapa(int m, int id, int c, int o) {
		String baseQuery = "INSERT INTO `datos_npc` VALUES (?,?,?,?);";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, m);
			p.setInt(2, id);
			p.setInt(3, c);
			p.setInt(4, o);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean agregar_recaudador_en_mapa(int guid, int mapid, int guildID, int cellid, int o, short N1, short N2) {
		String baseQuery = "INSERT INTO `datos_recaudadores` VALUES (?,?,?,?,?,?,?,?,?,?);";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, guid);
			p.setInt(2, mapid);
			p.setInt(3, cellid);
			p.setInt(4, o);
			p.setInt(5, guildID);
			p.setShort(6, N1);
			p.setShort(7, N2);
			p.setString(8, "");
			p.setLong(9, 0);
			p.setLong(10, 0);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static void actualizar_recaudador(Recaudador P) {
		String baseQuery = "UPDATE `datos_recaudadores` SET `objetos` = ?, `kamas` = ?, `experiencia` = ? WHERE id = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setString(1, P.parseItemPercepteur());
			p.setLong(2, P.getKamas());
			p.setLong(3, P.getXp());
			p.setInt(4, P.getGuid());
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static boolean agregar_fin_pelea_accion(int mapID, int type, int Aid, String args, String cond) {
		if(!eliminar_fin_pelea_accion(mapID,type,Aid))return false;
		String baseQuery = "INSERT INTO `datos_fin_pelea_accion` VALUES (?,?,?,?,?);";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, mapID);
			p.setInt(2, type);
			p.setInt(3, Aid);
			p.setString(4,args);
			p.setString(5, cond);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean eliminar_fin_pelea_accion(int mapID, int type, int aid) {
		String baseQuery = "DELETE FROM `datos_fin_pelea_accion` WHERE mapa = ? AND tipopelea = ? AND accion = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, mapID);
			p.setInt(2, type);
			p.setInt(3, aid);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
			return false;
		}
	}

	public static void guardar_nuevo_gremio(Gremio g) {
		String baseQuery = "INSERT INTO `datos_gremio` VALUES (?,?,?,1,0,0,0,?,?);";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, g.get_id());
			p.setString(2, g.get_name());
			p.setString(3, g.get_emblem());
			p.setString(4, "462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0");
			p.setString(5, "176;100|158;1000|124;100");
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void eliminar_gremio(int id) {
		String baseQuery = "DELETE FROM `datos_gremio` WHERE `id` = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, id);
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void eliminar_todos_los_miembros_del_gremio(int guildid) {
		String baseQuery = "DELETE FROM `datos_miembros_gremio` WHERE `gremio` = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, guildid);
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void eliminar_miembro_del_gremio(int id) {
		String baseQuery = "DELETE FROM `datos_miembros_gremio` WHERE `id` = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, id);
			
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void actualizar_gremio(Gremio g) {
		String baseQuery = "UPDATE `datos_gremio` SET `nivel` = ?, `experiencia` = ?,`capital` = ?, `recaudadoresmaximos` = ?, `hechizos` = ?, `caracteristicas` = ? WHERE id = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1, g.get_lvl());
			p.setLong(2, g.get_xp());
			p.setInt(3, g.get_Capital());
			p.setInt(4, g.get_nbrPerco());
			p.setString(5, g.compileSpell());
			p.setString(6, g.compileStats());
			p.setInt(7, g.get_id());
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static void actualizar_miembro_del_gremio(GuildMember gm) {
		String baseQuery = "REPLACE INTO `datos_miembros_gremio` VALUES(?,?,?,?,?,?);";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
			p.setInt(1,gm.getGuid());
			p.setInt(2,gm.getGuild().get_id());
			p.setInt(3,gm.getRank());
			p.setLong(4,gm.getXpGave());
			p.setInt(5,gm.getPXpGive());
			p.setInt(6,gm.getRights());
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
	}

	public static int isPersoInGuild(int guid) {
		int guildId = -1;
		try {
			ResultSet GuildQuery = GestorSQL.EjecutarConsulta("SELECT gremio FROM `datos_miembros_gremio` WHERE id ="+guid+";", MainServidor.DB_DINAMICOS);
			boolean found = GuildQuery.first();
			if(found)
				guildId = GuildQuery.getInt("gremio");
			CerrarResultado(GuildQuery);
		}catch(SQLException e) {
			JuegoServidor.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
		return guildId;
	}

	public static int[] isPersoInGuild(String name) {
		int guildId = -1;
		int guid = -1;
		try {
			ResultSet GuildQuery = GestorSQL.EjecutarConsulta("SELECT gremio,id FROM `datos_miembros_gremio` WHERE nombre ='"+name+"';", MainServidor.DB_DINAMICOS);
			boolean found = GuildQuery.first();
			if(found) {
				guildId = GuildQuery.getInt("gremio");
				guid = GuildQuery.getInt("id");
			}
			
			CerrarResultado(GuildQuery);
		}catch(SQLException e) {
			JuegoServidor.addToLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
		int[] toReturn = {guid,guildId};
		return toReturn;
	}

	public static boolean agregar_respuesta_npc(int repID, int type, String args) {
		String baseQuery = "DELETE FROM `datos_npc_respuesta` WHERE `id` = ? AND `tipo` = ?;";
		PreparedStatement p; 
		try {
			p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, repID);
			p.setInt(2, type);
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		baseQuery = "INSERT INTO `datos_npc_respuesta` VALUES (?,?,?);";
		try {
			p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, repID);
			p.setInt(2, type);
			p.setString(3, args);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean actualizar_respuesta_de_npc(int id, int q) {
		String baseQuery = "UPDATE `datos_npc_modelo` SET `pregunta` = ? WHERE `id` = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setInt(1, q);
			p.setInt(2, id);
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static boolean actualizar_pregunta_npc(int id, String reps) {
		String baseQuery = "UPDATE `datos_npc_pregunta` SET `respuestas` = ? WHERE `id` = ?;";
		try {
			PreparedStatement p = NuevaConsulta(baseQuery, _estaticos);
			p.setString(1, reps);
			p.setInt(2, id);
			
			p.execute();
			CerrarNuevaConsulta(p);
			return true;
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+baseQuery);
		}
		return false;
	}

	public static void cargar_acciones() {
			/*Variables représentant les champs de la base*/
			Personaje perso;
			int action;
			int nombre;
			int id;
			MainServidor.addToShopLog("Lancement de l'application des Lives Actions ...");
			String sortie;
			String couleur = "DF0101"; //La couleur du message envoyer a l'utilisateur (couleur en code HTML)
			ObjTemplate t;
			Objeto obj;
			PreparedStatement p;
			/*FIN*/
			try {
				ResultSet RS = EjecutarConsulta("SELECT * FROM datos_acciones_tiempo_real;", MainServidor.DB_DINAMICOS);
				while(RS.next()) {
					perso = Mundo.getPersonnage(RS.getInt("personaje"));
					if(perso == null) {
						MainServidor.addToShopLog("Personnage "+RS.getInt("personaje")+" non trouve, personnage non charge ?");
						continue;
					}
					if(!perso.isConectado()) {
						MainServidor.addToShopLog("Personnage "+RS.getInt("personaje")+" hors ligne");
						continue;
					}
					if(perso.getCuenta() == null) {
						MainServidor.addToShopLog("Le Personnage "+RS.getInt("personaje")+" n'est attribue a aucun compte charge");
						continue;
					}
					if(perso.getCuenta().getGameThread() == null) {
						MainServidor.addToShopLog("Le Personnage "+RS.getInt("personaje")+" n'a pas thread associe, le personnage est il hors ligne ?");
						continue;
					}
					if(perso.getPelea() != null) continue; // Perso en combat  @ Nami-Doc
					action = RS.getInt("accion");
					nombre = RS.getInt("nombre");
					id = RS.getInt("id");
					sortie = "+";

					switch (action) {
						//Monter d'un level
						case 1 -> {
							if (perso.get_lvl() == Mundo.getExpLevelSize()) continue;
							for (int n = nombre; n > 1; n--) perso.levelUp(false, true);
							perso.levelUp(true, true);
							sortie += nombre + " Niveau(x)";
						}
						//Ajouter X point d'experience
						case 2 -> {
							if (perso.get_lvl() == Mundo.getExpLevelSize()) continue;
							perso.addXp(nombre);
							sortie += nombre + " Xp";
						}
						//Ajouter X kamas
						case 3 -> {
							perso.addKamas(nombre);
							sortie += nombre + " Kamas";
						}
						//Ajouter X point de capital
						case 4 -> {
							perso.addPuntosDeCapital(nombre);
							sortie += nombre + " Point(s) de capital";
						}
						//Ajouter X point de sort
						case 5 -> {
							perso.addAgregarPuntosDeHechizo(nombre);
							sortie += nombre + " Point(s) de sort";
						}
						//Ajouter un item avec des jets aléatoire
						case 20 -> {
							t = Mundo.getObjTemplate(nombre);
							if (t == null) continue;
							obj = t.createNewItem(1, false); //Si mis à "true" l'objet à des jets max. Sinon ce sont des jets aléatoire
							if (obj == null) continue;
							if (perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
								Mundo.addObjet(obj, true);
							JuegoServidor.addToSockLog("Objet " + nombre + " ajouter a " + perso.getNombre() + " avec des stats aleatoire");
							GestorSalida.GAME_SEND_MESSAGE(perso, "L'objet \"" + t.getName() + "\" viens d'etre ajouter a votre personnage", couleur);
						}
						//Ajouter un item avec des jets MAX
						case 21 -> {
							t = Mundo.getObjTemplate(nombre);
							if (t == null) continue;
							obj = t.createNewItem(1, true); //Si mis à "true" l'objet à des jets max. Sinon ce sont des jets aléatoire
							if (obj == null) continue;
							if (perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
								Mundo.addObjet(obj, true);
							JuegoServidor.addToSockLog("Objet " + nombre + " ajoute a " + perso.getNombre() + " avec des stats MAX");
							GestorSalida.GAME_SEND_MESSAGE(perso, "L'objet \"" + t.getName() + "\" avec des stats maximum, viens d'etre ajoute a votre personnage", couleur);
						}
						//Force
						case 118 -> {
							perso.get_baseStats().addOneStat(action, nombre);
							GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
							sortie += nombre + " force";
						}
						//Agilite
						case 119 -> {
							perso.get_baseStats().addOneStat(action, nombre);
							GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
							sortie += nombre + " agilite";
						}
						//Chance
						case 123 -> {
							perso.get_baseStats().addOneStat(action, nombre);
							GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
							sortie += nombre + " chance";
						}
						//Sagesse
						case 124 -> {
							perso.get_baseStats().addOneStat(action, nombre);
							GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
							sortie += nombre + " sagesse";
						}
						//Vita
						case 125 -> {
							perso.get_baseStats().addOneStat(action, nombre);
							GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
							sortie += nombre + " vita";
						}
						//Intelligence
						case 126 -> {
							int statID = action;
							perso.get_baseStats().addOneStat(statID, nombre);
							GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
							sortie += nombre + " intelligence";
						}
					}
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
					if(action < 20 || action >100) GestorSalida.GAME_SEND_MESSAGE(perso,sortie+" a votre personnage",couleur); //Si l'action n'est pas un ajout d'objet on envoye un message a l'utilisateur
					MainServidor.addToShopLog("(Commande "+id+")Action "+action+" Nombre: "+nombre+" appliquee sur le personnage "+RS.getInt("jugador")+"("+perso.getNombre()+")");
				try {
					String query = "DELETE FROM datos_acciones_tiempo_real WHERE id="+id+";";
					p = NuevaConsulta(query, _dinamicos);
					p.execute();
					CerrarNuevaConsulta(p);
					MainServidor.addToShopLog("Commande "+id+" supprimee.");
				}catch(SQLException e) {
					JuegoServidor.addToLog("SQL ERROR: "+e.getMessage());
					MainServidor.addToShopLog("Error Delete From: "+e.getMessage());
					e.printStackTrace();
				}
				GestorSQL.guardar_personaje(perso,true);
			}
				CerrarResultado(RS);
		}catch(Exception e) {
			JuegoServidor.addToLog("ERROR: "+e.getMessage());
			MainServidor.addToShopLog("Error: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static void salir_del_juego(int accID, int logged) {
		PreparedStatement p;
		String query = "UPDATE `datos_cuenta` SET conectado = ? WHERE `id`=?;";
		try {
			p = NuevaConsulta(query, _dinamicos);
			p.setInt(1, logged);
			p.setInt(2, accID);
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+query);
		}
	}

	public static void conectado_a_0() {
		PreparedStatement p;
		String query = "UPDATE `datos_cuenta` SET conectado = 0;";
		try {
			p = NuevaConsulta(query, _dinamicos);
			p.execute();
			CerrarNuevaConsulta(p);
		} catch (SQLException e) {
			JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
			JuegoServidor.addToLog("Game: Query: "+query);
		}
	}

	public static void cargar_maximo_de_objetos() {
		    try {
		      ResultSet RS = EjecutarConsulta("SELECT * FROM datos_objetos;", MainServidor.DB_DINAMICOS);
		      while (RS.next()) {
		        int guid = RS.getInt("id");
		        int tempID = RS.getInt("modelo");
		        int qua = RS.getInt("cantidad");
		        int pos = RS.getInt("ubicacion");
		        String stats = RS.getString("caracteristicas");
		        Mundo.addObjet(new Objeto(guid, tempID, qua, pos, stats), false);
		      }
		      CerrarResultado(RS);
		    } catch (SQLException e) {
		      JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
		      System.exit(1);
		    }
	}

		public static boolean persoExist(String name) {
			boolean exist = false;
			PreparedStatement p;
			String query = "SELECT COUNT(*) AS exist FROM datos_personajes WHERE nombre LIKE ?;";
			try {
				p = NuevaConsulta(query, _dinamicos);
				p.setString(1, name);
				ResultSet RS =  p.executeQuery();
				
				boolean found = RS.first();
				
				if(found) {
					if(RS.getInt("exist") != 0)
						exist = true;
				}
				
				CerrarResultado(RS);
				CerrarNuevaConsulta(p);
			}catch(SQLException e) {
				RealmServer.addToLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
			return exist;
		}

		public static void comprar_casa(Personaje P, Casas h) {
			PreparedStatement p;
			String query = "UPDATE `datos_casas` SET `venta`='0', `dueño`=?, `gremio`='0', `acceso`='0', `llave`='-', `derechosgremio`='0' WHERE `id`=?;";
			try {
				p = NuevaConsulta(query, _dinamicos);
				p.setInt(1, P.getAccID());
				p.setInt(2, h.get_id());
				p.execute();
				CerrarNuevaConsulta(p);
				h.set_sale(0);
				h.set_owner_id(P.getAccID());
				h.set_guild_id(0);
				h.set_access(0);
				h.set_key("-");
				h.set_guild_rights(0);
			} catch (SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				JuegoServidor.addToLog("Game: Query: "+query);
			}
			
			ArrayList<Cofres> trunks = Cofres.getTrunksByHouse(h);
			for(Cofres trunk : trunks) {
				trunk.set_owner_id(P.getAccID());
				trunk.set_key("-");
			}
			
			query = "UPDATE `datos_cofres` SET `dueño`=?, `llave`='-' WHERE `casa`=?;";
			try {
				p = NuevaConsulta(query, _dinamicos);
				p.setInt(1, P.getAccID());
				p.setInt(2, h.get_id());
				p.execute();
				CerrarNuevaConsulta(p);
			} catch (SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				JuegoServidor.addToLog("Game: Query: "+query);
			}
		}

		public static void vender_casa(Casas h, int price) {
			h.set_sale(price);
			PreparedStatement p;
			String query = "UPDATE `datos_casas` SET `venta`=? WHERE `id`=?;";
			try {
				p = NuevaConsulta(query, _dinamicos);
				p.setInt(1, price);
				p.setInt(2, h.get_id());
				p.execute();
				CerrarNuevaConsulta(p);
			} catch (SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				JuegoServidor.addToLog("Game: Query: "+query);
			}
		}

		public static void codigo_casa(Personaje P, Casas h, String packet) {
			PreparedStatement p;
			String query = "UPDATE `datos_casas` SET `llave`=? WHERE `id`=? AND dueño=?;";
			try {
				p = NuevaConsulta(query, _dinamicos);
				p.setString(1, packet);
				p.setInt(2, h.get_id());
				p.setInt(3, P.getAccID());
				p.execute();
				CerrarNuevaConsulta(p);
				h.set_key(packet);
			} catch (SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				JuegoServidor.addToLog("Game: Query: "+query);
			}
		}

		public static void casa_gremio(Casas h, int GuildID, int GuildRights) {
			PreparedStatement p;
			String query = "UPDATE `datos_casas` SET `gremio`=?, `derechosgremio`=? WHERE `id`=?;";
			try {
				p = NuevaConsulta(query, _dinamicos);
				p.setInt(1, GuildID);
				p.setInt(2, GuildRights);
				p.setInt(3, h.get_id());
				p.execute();
				CerrarNuevaConsulta(p);
				h.set_guild_id(GuildID);
				h.set_guild_rights(GuildRights);
			} catch (SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				JuegoServidor.addToLog("Game: Query: "+query);
			}
		}

		public static void eliminar_casa_gremio(int GuildID) {
			PreparedStatement p;
			String query = "UPDATE `datos_casas` SET `derechosgremio`='0', `gremio`='0' WHERE `gremio`=?;";
			try {
				p = NuevaConsulta(query, _dinamicos);
				p.setInt(1, GuildID);
				p.execute();
				CerrarNuevaConsulta(p);
			} catch (SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				JuegoServidor.addToLog("Game: Query: "+query);
			}
		}

		public static void actualizar_casa(Casas h) {
			String baseQuery = "UPDATE `datos_casas` SET `dueño` = ?, `venta` = ?, `gremio` = ?, `acceso` = ?, `llave` = ?, `derechosgremio` = ? WHERE id = ?;";
			try {
				PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
				p.setInt(1, h.get_owner_id());
				p.setInt(2, h.get_sale());
				p.setInt(3, h.get_guild_id());
				p.setInt(4, h.get_access());
				p.setString(5, h.get_key());
				p.setInt(6, h.get_guild_rights());
				p.setInt(7, h.get_id());
				p.execute();
				CerrarNuevaConsulta(p);
			} catch (SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				JuegoServidor.addToLog("Game: Query: "+baseQuery);
			}
		}

		public static int nueva_id_recaudador() {
			int i = -50;//Pour éviter les conflits avec touts autre NPC
			try {
				String query = "SELECT `id` FROM `datos_recaudadores` ORDER BY `id` ASC LIMIT 0 , 1;";
				ResultSet RS = EjecutarConsulta(query, MainServidor.DB_DINAMICOS);
				while (RS.next()) {
					i = RS.getInt("guid")-1; 
				}
				CerrarResultado(RS);
			}catch(SQLException e) {
				RealmServer.addToLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
			return i;
		}

		public static int cargar_zaapis() {
			int i = 0;
			StringBuilder bonta = new StringBuilder();
			StringBuilder brak = new StringBuilder();
			StringBuilder neutral = new StringBuilder();
			try {
				ResultSet RS = GestorSQL.EjecutarConsulta("SELECT mapa, alineacion FROM datos_zappis;", MainServidor.DB_ESTATICOS);
				while (RS.next()) {
					if(RS.getInt("alineacion") == Constantes.ALIGNEMENT_BONTARIEN) {
						bonta.append(RS.getString("mapa"));
						if(!RS.isLast()) bonta.append(",");
					}
					else if(RS.getInt("alineacion") == Constantes.ALIGNEMENT_BRAKMARIEN) {
						brak.append(RS.getString("mapa"));
						if(!RS.isLast()) brak.append(",");
					}
					else {
						neutral.append(RS.getString("mapa"));
						if(!RS.isLast()) neutral.append(",");
					}
					i++;
				}
				Constantes.ZAAPI.put(Constantes.ALIGNEMENT_BONTARIEN, bonta.toString());
				Constantes.ZAAPI.put(Constantes.ALIGNEMENT_BRAKMARIEN, brak.toString());
				Constantes.ZAAPI.put(Constantes.ALIGNEMENT_NEUTRE, neutral.toString());
				CerrarResultado(RS);
			}catch(SQLException e) {
				RealmServer.addToLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
			return i;
		}

		public static int cargar_zaaps() {
			int i = 0;
			try {
				ResultSet RS = GestorSQL.EjecutarConsulta("SELECT mapa, celda FROM datos_zaaps;", MainServidor.DB_ESTATICOS);
				while (RS.next()) {
					Constantes.ZAAPS.put(RS.getInt("mapa"), RS.getInt("celda"));
					i++;
				}
				CerrarResultado(RS);
			}catch(SQLException e) {
				RealmServer.addToLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
			return i;
		}

		public static int siguiente_id_objeto() {
			try {
				ResultSet RS = EjecutarConsulta("SELECT MAX(id) AS max FROM datos_objetos;", MainServidor.DB_DINAMICOS);
				int guid = 0;
				boolean found = RS.first();
				if(found)
					guid = RS.getInt("max");
				CerrarResultado(RS);
				return guid;
			}catch(SQLException e) {
				RealmServer.addToLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
				MainServidor.closeServers();
			}
			return 0;
		}

		public static int cargar_ip_baneadas() {
			int i = 0;
			try {
				ResultSet RS = EjecutarConsulta("SELECT ip FROM datos_ipbaneadas;", MainServidor.DB_DINAMICOS);
				while (RS.next()) {
					Constantes.BAN_IP += RS.getString("ip");
					if(!RS.isLast()) Constantes.BAN_IP += ",";
					i++;
			    }
				CerrarResultado(RS);
			}catch(SQLException e) {
				RealmServer.addToLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
			return i;
		}

		public static boolean agregar_ip_baneada(String ip) {
			String baseQuery = "INSERT INTO `datos_ipbaneadas` VALUES (?);";
			try {
				PreparedStatement p = NuevaConsulta(baseQuery, _dinamicos);
				p.setString(1, ip);
				p.execute();
				CerrarNuevaConsulta(p);
				return true;
			} catch (SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				JuegoServidor.addToLog("Game: Query: "+baseQuery);
			}
			return false;
		}

		public static void cargar_mercadillos() {
			try {
				ResultSet RS = EjecutarConsulta("SELECT * FROM `datos_mercadillos` ORDER BY id ASC", MainServidor.DB_ESTATICOS);
				while(RS.next()) {
					Mundo.addHdv(new Mercadillo(
									RS.getInt("mapa"),
									RS.getFloat("tazaventa"),
									RS.getShort("tiempoventa"),
									RS.getShort("cuenta"),
									RS.getShort("nivelmaximo"),
									RS.getString("categoria")));
					
				}
				RS = EjecutarConsulta("SELECT id MAX FROM `datos_mercadillos`", MainServidor.DB_ESTATICOS);
				RS.first();
				Mundo.setNextHdvID(RS.getInt("MAX"));
				CerrarResultado(RS);
			}catch(SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}

		public static void cargar_objetos_mercadillos() {
			try {
				long time1 = System.currentTimeMillis();	//TIME
				ResultSet RS = EjecutarConsulta("SELECT i.* FROM `datos_objetos` AS i,`datos_objetos_mercadillo` AS h WHERE i.id = h.objeto", MainServidor.DB_DINAMICOS);
				//Load items
				while(RS.next()) {
					int guid 	= RS.getInt("id");
					int tempID 	= RS.getInt("modelo");
					int qua 	= RS.getInt("cantidad");
					int pos		= RS.getInt("ubicacion");
					String stats= RS.getString("caracteristicas");
					Mundo.addObjet(Mundo.newObjet(guid, tempID, qua, pos, stats), false);
				}
				
				//Load HDV entry
				RS = EjecutarConsulta("SELECT * FROM `datos_objetos_mercadillo`", MainServidor.DB_DINAMICOS);
				while(RS.next()) {
					Mercadillo tempHdv = Mundo.getHdv(RS.getInt("mapa"));
					if(tempHdv == null)continue;
					tempHdv.addEntry(new Mercadillo.HdvEntry(
											RS.getInt("precio"),
											RS.getByte("cantidad"),
											RS.getInt("dueño"),
											Mundo.getObjet(RS.getInt("objeto"))));
				}
				System.out.println (System.currentTimeMillis() - time1 + "ms pour loader les HDVS items");	//TIME
				CerrarResultado(RS);
			}catch(SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}

		public static void guardar_objetos_mercadillo(ArrayList<HdvEntry> liste) {
			PreparedStatement queries = null;
			try {
				String emptyQuery = "TRUNCATE TABLE `datos_objetos_mercadillo`";
				PreparedStatement emptyTable = NuevaConsulta(emptyQuery, _dinamicos);
				emptyTable.execute();
				CerrarNuevaConsulta(emptyTable);
				
				String baseQuery = "INSERT INTO `datos_objetos_mercadillo` (`mapa`,`dueño`,`precio`,`cantidad`,`objeto`) VALUES (?,?,?,?,?);";
				queries = NuevaConsulta(baseQuery, _dinamicos);
				for(HdvEntry curEntry : liste) {
					if(curEntry.getOwner() == -1)continue;
					queries.setInt(1, curEntry.getHdvID());
					queries.setInt(2, curEntry.getOwner());
					queries.setInt(3, curEntry.getPrice());
					queries.setInt(4, curEntry.getAmount(false));
					queries.setInt(5, curEntry.getObjet().getGuid());
					queries.execute();
				}
				CerrarNuevaConsulta(queries);
				guardar_mercadillo_precio_medio();
				}catch(SQLException e) {
					JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
					e.printStackTrace();
			}
		}

		public static void guardar_mercadillo_precio_medio() {
			String baseQuery = "UPDATE `datos_objeto_modelo` SET vendido = ?, preciomedio = ? WHERE id = ?;";
			PreparedStatement queries = null;
			try {
				queries = NuevaConsulta(baseQuery, _estaticos);
				for(ObjTemplate curTemp : Mundo.getObjTemplates()) {
					if(curTemp.getSold() == 0)
						continue;
					queries.setLong(1, curTemp.getSold());
					queries.setInt(2, curTemp.getAvgPrice());
					queries.setInt(3, curTemp.getID());
					queries.executeUpdate();
				}
				CerrarNuevaConsulta(queries);
			}catch(SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}

		public static void cargar_animaciones() {
			try {
				ResultSet RS = EjecutarConsulta("SELECT * FROM datos_animaciones;", MainServidor.DB_ESTATICOS);
				while(RS.next()) {
					Mundo.addAnimation(new Animaciones(
							RS.getInt("id"),
							RS.getInt("id2"),
							RS.getString("nombre"),
							RS.getInt("area"),
							RS.getInt("accion"),
							RS.getInt("tamaño")));
				}
				CerrarResultado(RS);
			}catch(SQLException e) {
				JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}

	    public static int cargar_cofre() {
                int nbr = 0;
                try {
                        ResultSet RS = GestorSQL.EjecutarConsulta("SELECT * FROM datos_cofres;", MainServidor.DB_DINAMICOS);
                        while(RS.next()) {
                                Mundo.addTrunk(new Cofres(
                                                RS.getInt("id"),
                                                RS.getInt("casa"),
                                                RS.getShort("mapa"),
                                                RS.getInt("celda"),
                                                RS.getString("objeto"),
                                                RS.getInt("kamas"),
                                                RS.getString("llave"),
                                                RS.getInt("dueño")));
                                nbr ++;
                        }
                        CerrarResultado(RS);
                }catch(SQLException e){
                        RealmServer.addToLog("SQL ERROR: "+e.getMessage());
                        e.printStackTrace();
                        nbr = 0;
                }
                return nbr;
        }
       
        public static void cofre_codigo(Personaje P, Cofres t, String packet) {
                PreparedStatement p;
                String query = "UPDATE `datos_cofres` SET `llave`=? WHERE `id`=? AND dueño=?;";
                try {
                        p = NuevaConsulta(query, _dinamicos);
                        p.setString(1, packet);
                        p.setInt(2, t.get_id());
                        p.setInt(3, P.getAccID());
                        p.execute();
                        CerrarNuevaConsulta(p);
                } catch (SQLException e) {
                        JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
                        JuegoServidor.addToLog("Game: Query: "+query);
                }
        }
       
        public static void actualizar_cofre(Cofres t) {
                PreparedStatement p;
                String query = "UPDATE `datos_cofres` SET `kamas`=?, `objeto`=? WHERE `id`=?";
                try {
                        p = NuevaConsulta(query, _dinamicos);
                        p.setLong(1, t.get_kamas());
                        p.setString(2, t.parseTrunkObjetsToDB());
                        p.setInt(3, t.get_id());
                        p.execute();
                        CerrarNuevaConsulta(p);
                } catch (SQLException e) {
                        JuegoServidor.addToLog("Game: SQL ERROR: "+e.getMessage());
                        JuegoServidor.addToLog("Game: Query: "+query);
                }
        }
	}