package juego;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;


import objetos.*;
import objetos.Mapa.*;
import objetos.Pelea.Fighter;
import objetos.Gremio.GuildMember;
import objetos.Mercadillo.HdvEntry;
import objetos.Oficio.StatsMetier;
import objetos.NPCModelo.*;
import objetos.Objeto.ObjTemplate;
import objetos.Personaje.Grupo;
import objetos.casas.Casas;
import objetos.casas.Cofres;
import objetos.hechizos.EfectoHechizo;
import objetos.hechizos.Hechizos.SortStats;
import comunes.*;

public class JuegoThread implements Runnable {
	private BufferedReader _in;
	private Thread _t;
	private PrintWriter _out;
	private Socket _s;
	private Cuenta _compte;
	private Personaje _perso;
	private final Map<Integer,GameAction> _actions = new TreeMap<>();
	private long _timeLastTradeMsg = 0, _timeLastRecrutmentMsg = 0, _timeLastsave = 0, _timeLastAlignMsg = 0;
	private long _timeLastIncarnamMsg = 0;
	private Comandos command;
	
	public static class GameAction {
		public final int _id;
		public final int _actionID;
		public final String _packet;
		public String _args;
		
		public GameAction(int aId, int aActionId,String aPacket) {
			_id = aId;
			_actionID = aActionId;
			_packet = aPacket;
		}
	}
	
	public JuegoThread(Socket sock) {
		try {
			_s = sock;
			_in = new BufferedReader(new InputStreamReader(_s.getInputStream()));
			_out = new PrintWriter(_s.getOutputStream());
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		} catch(IOException e) {
			try {
				JuegoServidor.agregar_a_los_logs(e.getMessage());
				if(!_s.isClosed())_s.close();
			} catch (IOException e1) {e1.printStackTrace();}
		}
	}
	
	public void run() {
		try {
			String packet = "";
			char[] charCur = new char[1];
			GestorSalida.GAME_SEND_HELLOGAME_PACKET(_out);
	    	while(_in.read(charCur, 0, 1)!=-1 && MainServidor.isRunning) {
	    		if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
	    			packet += charCur[0];
		    	}else if(!packet.isEmpty()) {
		    		packet = GestorEncriptador.toUnicode(packet);
					if(MainServidor.MOSTRAR_RECIBIDOS){
						JuegoServidor.addToSockLog("Juego: Recibido << "+packet);
					}
		    		parsePacket(packet);
		    		//System.out.println(packet);
		    		packet = "";
		    	}
	    	}
    	}catch(IOException e) {
    		try {
    			JuegoServidor.agregar_a_los_logs(e.getMessage());
	    		_in.close();
	    		_out.close();
	    		if(_compte != null) {
	    			_compte.setCurPerso(null);
	    			_compte.setGameThread(null);
	    			_compte.setRealmThread(null);
	    		}
	    		if(!_s.isClosed())_s.close();
	    	}catch(IOException e1){e1.printStackTrace();}
		}catch(Exception e) {
    		e.printStackTrace();
    		JuegoServidor.agregar_a_los_logs(e.getMessage());
    	} finally {
    		kick();
    	}
	}

	private void parsePacket(String packet) {
		if(_perso != null) {
			_perso.refreshLastPacketTime();
		}
		
		if(packet.length()>3 && packet.substring(0,4).equalsIgnoreCase("ping")) {
			GestorSalida.GAME_SEND_PONG(_out);
			return;
		}
		if(packet.length()>4 && packet.substring(0,5).equalsIgnoreCase("qping")) {
			GestorSalida.GAME_SEND_QPONG(_out);
			return;
		}
		
		switch(packet.charAt(0)) {
			case 'p': // 'p'
				if(packet.equals("ping")) {
					GestorSalida.GAME_SEND_PONG(_out);
				}
				break;				
			case 'q': // 'q'
				if(!packet.equals("qping")) {
					return;
				}
				if(_perso == null) {
					return;
				}
				if(_perso.getPelea() == null) {
					return;
				}
				_perso.getPelea().ticMyTimer();
				GestorSalida.GAME_SEND_QPONG(_out);
				break;
			case 'A':
				parseAccountPacket(packet);
			break;
			case 'B':
				parseBasicsPacket(packet);
			break;
			case 'c':
				parseChanelPacket(packet);
			break;
			case 'D':
				parseDialogPacket(packet);
			break;
			case 'E':
				parseExchangePacket(packet);
			break;
			case 'e':
				parse_environementPacket(packet);
			break;
			case 'F':
				parse_friendPacket(packet);
			break;
			case 'f':
				parseFightPacket(packet);
			break;
			case 'G':
				parseGamePacket(packet);
			break;
			case 'g':
				parseGuildPacket(packet);
			break;
			case 'h':
				parseHousePacket(packet);
			break;
			case 'i':
				parse_enemyPacket(packet);
			break;
			case 'K':
				parseHouseKodePacket(packet);
			break;
			case 'O':
				parseObjectPacket(packet);
			break;
			case 'P':
				parseGroupPacket(packet);
			break;
			case 'R':
				parseMountPacket(packet);
			break;
			case 'S':
				parseSpellPacket(packet);
			break;
			case 'W':
				parseWaypointPacket(packet);
			break;
		}
	}
	
	private void parseHousePacket(String packet)
	{
		switch (packet.charAt(1)) {
//Acheter la maison
			case 'B' -> {
				packet = packet.substring(2);
				Casas.HouseAchat(_perso);
			}
//Maison de guilde
			case 'G' -> {
				packet = packet.substring(2);
				if (packet.isEmpty()) packet = null;
				Casas.parseHG(_perso, packet);
			}
//Quitter/Expulser de la maison
			case 'Q' -> {
				packet = packet.substring(2);
				Casas.Leave(_perso, packet);
			}
//Modification du prix de vente
			case 'S' -> {
				packet = packet.substring(2);
				Casas.SellPrice(_perso, packet);
			}
//Fermer fenetre d'achat
			case 'V' -> Casas.closeBuy(_perso);
		}
	}
	
	private void parseHouseKodePacket(String packet)
	{
		switch (packet.charAt(1)) {
//Fermer fenetre du code
			case 'V' -> Casas.closeCode(_perso);
//Envoi du code
			case 'K' -> House_code(packet);
		}
	}
	
	private void House_code(String packet)
	{
		switch (packet.charAt(2)) {
//Envoi du code
			case '0' -> {
				packet = packet.substring(4);
				if (_perso.getInTrunk() != null)
					Cofres.OpenTrunk(_perso, packet, false);
				else
					Casas.OpenHouse(_perso, packet, false);
			}
//Changement du code
			case '1' -> {
				packet = packet.substring(4);
				if (_perso.getInTrunk() != null)
					Cofres.LockTrunk(_perso, packet);
				else
					Casas.LockHouse(_perso, packet);
			}
		}
	}
	
	private void parse_enemyPacket(String packet)
	{
		switch (packet.charAt(1)) {
//Ajouter
			case 'A' -> Enemy_add(packet);
//Delete
			case 'D' -> Enemy_delete(packet);
//Liste
			case 'L' -> GestorSalida.GAME_SEND_ENEMY_LIST(_perso);
		}
	}
	
	private void Enemy_add(String packet)
	{
		if(_perso == null)return;
		int guid = -1;
		switch (packet.charAt(2)) {
//Nom de perso
			case '%' -> {
				packet = packet.substring(3);
				Personaje P = Mundo.getPersonajePorNombre(packet);
				if (P == null) {
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = P.getAccID();
			}
//Pseudo
			case '*' -> {
				packet = packet.substring(3);
				Cuenta C = Mundo.getCompteByPseudo(packet);
				if (C == null) {
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = C.get_GUID();
			}
			default -> {
				packet = packet.substring(2);
				Personaje Pr = Mundo.getPersonajePorNombre(packet);
				if (Pr == null ? true : !Pr.isConectado()) {
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = Pr.getCuenta().get_GUID();
			}
		}
		if(guid == -1)
		{
			GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
			return;
		}
		_compte.addEnemy(packet, guid);
	}

	private void Enemy_delete(String packet)
	{
		if(_perso == null)return;
		int guid = -1;
		switch (packet.charAt(2)) {
//Nom de perso
			case '%' -> {
				packet = packet.substring(3);
				Personaje P = Mundo.getPersonajePorNombre(packet);
				if (P == null) {
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = P.getAccID();
			}
//Pseudo
			case '*' -> {
				packet = packet.substring(3);
				Cuenta C = Mundo.getCompteByPseudo(packet);
				if (C == null) {
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = C.get_GUID();
			}
			default -> {
				packet = packet.substring(2);
				Personaje Pr = Mundo.getPersonajePorNombre(packet);
				if (Pr == null ? true : !Pr.isConectado()) {
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = Pr.getCuenta().get_GUID();
			}
		}
		if(guid == -1 || !_compte.isEnemyWith(guid))
		{
			GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
			return;
		}
		_compte.removeEnemy(guid);
	}
	
	private void parseWaypointPacket(String packet)
	{
		switch (packet.charAt(1)) {
//Use
			case 'U' -> Waypoint_use(packet);
//use zaapi
			case 'u' -> Zaapi_use(packet);
//quitter zaapi
			case 'v' -> Zaapi_close();
//Quitter
			case 'V' -> Waypoint_quit();
		}
	}

	private void Zaapi_close()
	{
		_perso.Zaapi_close();
	}
	
	private void Zaapi_use(String packet)
	{
		if(_perso.getDeshonor() >= 2) 
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "183");
			return;
		}
		_perso.Zaapi_use(packet);
	}
	
	private void Waypoint_quit()
	{
		_perso.stopZaaping();
	}

	private void Waypoint_use(String packet)
	{
		short id = -1;
		try
		{
			id = Short.parseShort(packet.substring(2));
		}catch(Exception ignored){}
		if( id == -1)return;
		_perso.useZaap(id);
	}
	private void parseGuildPacket(String packet)
	{
		switch (packet.charAt(1)) {
//Stats
			case 'B' -> {
				if (_perso.get_guild() == null) return;
				Gremio G = _perso.get_guild();
				if (!_perso.getMiembroGremio().canDo(Constantes.G_BOOST)) return;
				switch (packet.charAt(2)) {
//Prospec
					case 'p' -> {
						if (G.get_Capital() < 1) return;
						if (G.get_Stats(176) >= 500) return;
						G.set_Capital(G.get_Capital() - 1);
						G.upgrade_Stats(176, 1);
					}
//Sagesse
					case 'x' -> {
						if (G.get_Capital() < 1) return;
						if (G.get_Stats(124) >= 400) return;
						G.set_Capital(G.get_Capital() - 1);
						G.upgrade_Stats(124, 1);
					}
//Pod
					case 'o' -> {
						if (G.get_Capital() < 1) return;
						if (G.get_Stats(158) >= 5000) return;
						G.set_Capital(G.get_Capital() - 1);
						G.upgrade_Stats(158, 20);
					}
//Nb Perco
					case 'k' -> {
						if (G.get_Capital() < 10) return;
						if (G.get_nbrPerco() >= 50) return;
						G.set_Capital(G.get_Capital() - 10);
						G.set_nbrPerco(G.get_nbrPerco() + 1);
					}
				}
				GestorSQL.actualizar_gremio(G);
				GestorSalida.GAME_SEND_gIB_PACKET(_perso, _perso.get_guild().parsePercotoGuild());
			}
//Sorts
			case 'b' -> {
				if (_perso.get_guild() == null) return;
				Gremio G2 = _perso.get_guild();
				if (!_perso.getMiembroGremio().canDo(Constantes.G_BOOST)) return;
				int spellID = Integer.parseInt(packet.substring(2));
				if (G2.getSpells().containsKey(spellID)) {
					if (G2.get_Capital() < 5) return;
					G2.set_Capital(G2.get_Capital() - 5);
					G2.boostSpell(spellID);
					GestorSQL.actualizar_gremio(G2);
					GestorSalida.GAME_SEND_gIB_PACKET(_perso, _perso.get_guild().parsePercotoGuild());
				} else {
					JuegoServidor.agregar_a_los_logs("[ERROR]Sort " + spellID + " non trouve.");
				}
			}
//Creation
			case 'C' -> guild_create(packet);
//Tï¿½lï¿½portation enclo de guilde
			case 'f' -> guild_enclo(packet.substring(2));
//Retirer percepteur
			case 'F' -> guild_remove_perco(packet.substring(2));
//Tï¿½lï¿½portation maison de guilde
			case 'h' -> guild_house(packet.substring(2));
//Poser un percepteur
			case 'H' -> guild_add_perco();
//Infos
			case 'I' -> guild_infos(packet.charAt(2));
//Join
			case 'J' -> guild_join(packet.substring(2));
//Kick
			case 'K' -> guild_kick(packet.substring(2));
//Promote
			case 'P' -> guild_promote(packet.substring(2));
//attaque sur percepteur
			case 'T' -> guild_perco_join_fight(packet.substring(2));
//Ferme le panneau de crï¿½ation de guilde
			case 'V' -> guild_CancelCreate();
		}
	}
	
	private void guild_perco_join_fight(String packet) 
	{
		if (packet.charAt(0) == 'J') {//Rejoindre
			String PercoID = Integer.toString(Integer.parseInt(packet.substring(1)), 36);

			int TiD = -1;
			try {
				TiD = Integer.parseInt(PercoID);
			} catch (Exception e) {
			}

			Recaudador perco = Mundo.getPerco(TiD);
			if (perco == null) return;

			int FightID = -1;
			try {
				FightID = perco.get_inFightID();
			} catch (Exception e) {
			}

			short MapID = -1;
			try {
				MapID = Mundo.getCarte((short) perco.get_mapID()).getFight(FightID).get_map().getID();
			} catch (Exception e) {
			}

			int CellID = -1;
			try {
				CellID = perco.get_cellID();
			} catch (Exception e) {
			}

			if (MainServidor.MOSTRAR_ENVIADOS)
				JuegoServidor.agregar_a_los_logs("[DEBUG] Percepteur INFORMATIONS : TiD:" + TiD + ", FightID:" + FightID + ", MapID:" + MapID + ", CellID" + CellID);
			if (TiD == -1 || FightID == -1 || MapID == -1 || CellID == -1) return;
			if (_perso.getPelea() == null && !_perso.is_away()) {
				if (_perso.getActualMapa().getID() != MapID) {
					_perso.teletransportar(MapID, CellID);
				}
				Mundo.getCarte(MapID).getFight(FightID).joinPercepteurFight(_perso, _perso.get_GUID(), TiD);
			}
		}
	}

	private void guild_remove_perco(String packet) 
	{
		if(_perso.get_guild() == null || _perso.getPelea() != null || _perso.is_away())return;
		if(!_perso.getMiembroGremio().canDo(Constantes.G_POSPERCO))return;//On peut le retirer si on a le droit de le poser
		byte IDPerco = Byte.parseByte(packet);
		Recaudador perco = Mundo.getPerco(IDPerco);
		if(perco == null || perco.get_inFight() > 0) return;
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_perso.getActualMapa(), IDPerco);
		GestorSQL.eliminar_recaudador(perco.getGuid());
		perco.DelPerco(perco.getGuid());
		for(Personaje z : _perso.get_guild().getMembers())
		{
			if(z.isConectado())
			{
				GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parsetoGuild(z.get_guild().get_id()));
				String str = "";
				str += "R"+perco.get_N1()+","+perco.get_N2()+"|";
				str += perco.get_mapID()+"|";
				str += Mundo.getCarte(perco.get_mapID()).getX()+"|"+ Mundo.getCarte(perco.get_mapID()).getY()+"|"+_perso.getNombre();
				GestorSalida.GAME_SEND_gT_PACKET(z, str);
			}
		}
	}

	private void guild_add_perco() 
	{
		if(_perso.get_guild() == null || _perso.getPelea() != null || _perso.is_away())return;
		if(!_perso.getMiembroGremio().canDo(Constantes.G_POSPERCO))return;//Pas le droit de le poser
		if(_perso.get_guild().getMembers().size() < MainServidor.MEMBRE_MINI_GUILDE_VALIDE)return;//Guilde invalide
		short price = (short)(1000+10*_perso.get_guild().get_lvl());//Calcul du prix du percepteur
		if(_perso.getKamas() < price)//Kamas insuffisants
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "182");
			return;
		}
		if(Recaudador.GetPercoGuildID(_perso.getActualMapa().getID()) > 0)//La carte possï¿½de un perco
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1168;1");
			return;
		}
		if(_perso.getActualMapa().getEsquemaPelea().length() < 5)//La map ne possï¿½de pas de "places"
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "113");
			return;
		}
		if(Recaudador.CountPercoGuild(_perso.get_guild().get_id()) >= _perso.get_guild().get_nbrPerco()) return;//Limite de percepteur
		short random1 = (short) (Formulas.getRandomValue(1, 39));
		short random2 = (short) (Formulas.getRandomValue(1, 71));
		//Ajout du Perco.
		int id = GestorSQL.nueva_id_recaudador();
		Recaudador perco = new Recaudador(id, _perso.getActualMapa().getID(), _perso.getActualCelda().getID(), (byte)3, _perso.get_guild().get_id(), random1, random2, "", 0, 0);
		Mundo.addPerco(perco);
		GestorSalida.ENVIAR_AGREGAR_RECAUDADOR_EN_MAPA(_perso.getActualMapa());
		GestorSQL.agregar_recaudador_en_mapa(id, _perso.getActualMapa().getID(), _perso.get_guild().get_id(), _perso.getActualCelda().getID(), 3, random1, random2);
		for(Personaje z : _perso.get_guild().getMembers())
		{
			if(z != null && z.isConectado())
			{
				GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parsetoGuild(z.get_guild().get_id()));
				String str = "";
				str += "S"+perco.get_N1()+","+perco.get_N2()+"|";
				str += perco.get_mapID()+"|";
				str += Mundo.getCarte(perco.get_mapID()).getX()+"|"+ Mundo.getCarte(perco.get_mapID()).getY()+"|"+_perso.getNombre();
				GestorSalida.GAME_SEND_gT_PACKET(z, str);
			}
		}
	}

	private void guild_enclo(String packet)
	{
		if(_perso.get_guild() == null)
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1135");
			return;
		}
		
		if(_perso.getPelea() != null || _perso.is_away())return;
		short MapID = Short.parseShort(packet);
		MountPark MP = Mundo.getCarte(MapID).getMountPark();
		if(MP.get_guild().get_id() != _perso.get_guild().get_id())
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1135");
			return;
		}
		int CellID = Mundo.getEncloCellIdByMapId(MapID);
		if (_perso.hasItemTemplate(9035, 1))
		{
			_perso.removeByTemplateID(9035,1);
			_perso.teletransportar(MapID, CellID);
		}else
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1159");
			return;
		}
	}
	
	private void guild_house(String packet)
	{
		if(_perso.get_guild() == null)
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1135");
			return;
		}
		
		if(_perso.getPelea() != null || _perso.is_away())return;
		int HouseID = Integer.parseInt(packet);
		Casas h = Mundo.getHouses().get(HouseID);
		if(h == null) return;
		if(_perso.get_guild().get_id() != h.get_guild_id()) 
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1135");
			return;
		}
		if(!h.canDo(Constantes.H_GTELE))
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1136");
			return;
		}
		if (_perso.hasItemTemplate(8883, 1))
		{
			_perso.removeByTemplateID(8883,1);
			_perso.teletransportar((short)h.get_mapid(), h.get_caseid());
		}else
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1137");
			return;
		}
	}
	
	private void guild_promote(String packet)
	{
		if(_perso.get_guild() == null)return;	//Si le personnage envoyeur n'a mï¿½me pas de guilde
		
		String[] infos = packet.split("\\|");
		
		int guid = Integer.parseInt(infos[0]);
		int rank = Integer.parseInt(infos[1]);
		byte xpGive = Byte.parseByte(infos[2]);
		int right = Integer.parseInt(infos[3]);
		
		Personaje p = Mundo.getPersonnage(guid);	//Cherche le personnage a qui l'on change les droits dans la mï¿½moire
		GuildMember toChange;
		GuildMember changer = _perso.getMiembroGremio();
		
		//Rï¿½cupï¿½ration du personnage ï¿½ changer, et verification de quelques conditions de base
		if(p == null)	//Arrive lorsque le personnage n'est pas chargï¿½ dans la mï¿½moire
		{
			int guildId = GestorSQL.personaje_esta_en_gremio(guid);	//Rï¿½cupï¿½re l'id de la guilde du personnage qui n'est pas dans la mï¿½moire
			
			if(guildId < 0)return;	//Si le personnage ï¿½ qui les droits doivent ï¿½tre modifiï¿½ n'existe pas ou n'a pas de guilde
			
			
			if(guildId != _perso.get_guild().get_id())					//Si ils ne sont pas dans la mï¿½me guilde
			{
				GestorSalida.GAME_SEND_gK_PACKET(_perso, "Ed");
				return;
			}
			toChange = Mundo.getGuild(guildId).getMember(guid);
		}
		else
		{
			if(p.get_guild() == null)return;	//Si la personne ï¿½ qui changer les droits n'a pas de guilde
			if(_perso.get_guild().get_id() != p.get_guild().get_id())	//Si ils ne sont pas de la meme guilde
			{
				GestorSalida.GAME_SEND_gK_PACKET(_perso, "Ea");
				return;
			}
			
			toChange = p.getMiembroGremio();
		}
		
		//Vï¿½rifie ce que le personnage changeur ï¿½ le droit de faire
		
		if(changer.getRank() == 1)	//Si c'est le meneur
		{
			if(changer.getGuid() == toChange.getGuid())	//Si il se modifie lui mï¿½me, reset tout sauf l'XP
			{
				rank = -1;
				right = -1;
			}
			else //Si il modifie un autre membre
			{
				if(rank == 1) //Si il met un autre membre "Meneur"
				{
					changer.setAllRights(2, (byte) -1, 29694);	//Met le meneur "Bras droit" avec tout les droits
					
					//Dï¿½fini les droits ï¿½ mettre au nouveau meneur
					rank = 1;
					xpGive = -1;
					right = 1;
				}
			}
		}
		else	//Sinon, c'est un membre normal
		{
			if(toChange.getRank() == 1)	//S'il veut changer le meneur, reset tout sauf l'XP
			{
				rank = -1;
				right = -1;
			}
			else	//Sinon il veut changer un membre normal
			{
				if(!changer.canDo(Constantes.G_RANK) || rank == 1)	//S'il ne peut changer les rang ou qu'il veut mettre meneur
					rank = -1; 	//"Reset" le rang
				
				if(!changer.canDo(Constantes.G_RIGHT) || right == 1)	//S'il ne peut changer les droits ou qu'il veut mettre les droits de meneur
					right = -1;	//"Reset" les droits
				
				if(!changer.canDo(Constantes.G_HISXP) && !changer.canDo(Constantes.G_ALLXP) && changer.getGuid() == toChange.getGuid())	//S'il ne peut changer l'XP de personne et qu'il est la cible
					xpGive = -1; //"Reset" l'XP
			}
			
			if(!changer.canDo(Constantes.G_ALLXP) && !changer.equals(toChange))	//S'il n'a pas le droit de changer l'XP des autres et qu'il n'est pas la cible
				xpGive = -1; //"Reset" L'XP
		}

		toChange.setAllRights(rank,xpGive,right);
		
		GestorSalida.GAME_SEND_gS_PACKET(_perso,_perso.getMiembroGremio());
		
		if(p != null && p.get_GUID() != _perso.get_GUID())
			GestorSalida.GAME_SEND_gS_PACKET(p,p.getMiembroGremio());
	}
	
	private void guild_CancelCreate()
	{
		GestorSalida.GAME_SEND_gV_PACKET(_perso);
	}

	private void guild_kick(String name)
	{
		if(_perso.get_guild() == null)return;
		Personaje P = Mundo.getPersonajePorNombre(name);
		int guid = -1,guildId = -1;
		Gremio toRemGuild;
		GuildMember toRemMember;
		if(P == null)
		{
			int[] infos = GestorSQL.personaje_esta_en_gremio(name);
			guid = infos[0];
			guildId = infos[1];
			if(guildId < 0 || guid < 0)return;
			toRemGuild = Mundo.getGuild(guildId);
			toRemMember = toRemGuild.getMember(guid);
		}
		else
		{
			toRemGuild = P.get_guild();
			if(toRemGuild == null)//La guilde du personnage n'est pas charger ?
			{
					toRemGuild = Mundo.getGuild(_perso.get_guild().get_id());//On prend la guilde du perso qui l'ï¿½jecte
			}
			toRemMember = toRemGuild.getMember(P.get_GUID());
			if(toRemMember == null) return;//Si le membre n'est pas dans la guilde.
			if(toRemMember.getGuild().get_id() != _perso.get_guild().get_id()) return;//Si guilde diffï¿½rente
		}
		//si pas la meme guilde
		if(toRemGuild.get_id() != _perso.get_guild().get_id())
		{
			GestorSalida.GAME_SEND_gK_PACKET(_perso, "Ea");
			return;
		}
		//S'il n'a pas le droit de kick, et que ce n'est pas lui mï¿½me la cible
		if(!_perso.getMiembroGremio().canDo(Constantes.G_BAN) && _perso.getMiembroGremio().getGuid() != toRemMember.getGuid())
		{
			GestorSalida.GAME_SEND_gK_PACKET(_perso, "Ed");
			return;
		}
		//Si diffï¿½rent : Kick
		if(_perso.getMiembroGremio().getGuid() != toRemMember.getGuid())
		{
			if(toRemMember.getRank() == 1) //S'il veut kicker le meneur
				return;
			
			toRemGuild.removeMember(toRemMember.getPerso());
			if(P != null)
				P.setGuildMember(null);
			
			GestorSalida.GAME_SEND_gK_PACKET(_perso, "K"+_perso.getNombre()+"|"+name);
			if(P != null)
				GestorSalida.GAME_SEND_gK_PACKET(P, "K"+_perso.getNombre());
		}else//si quitter
		{
			Gremio G = _perso.get_guild();
			if(_perso.getMiembroGremio().getRank() == 1 && G.getMembers().size() > 1)	//Si le meneur veut quitter la guilde mais qu'il reste d'autre joueurs
			{
				//TODO : Envoyer le message qu'il doit mettre un autre membre meneur (Pas vraiment....)
				return;
			}
			G.removeMember(_perso);
			_perso.setGuildMember(null);
			//S'il n'y a plus personne
			if(G.getMembers().isEmpty()) Mundo.removeGuild(G.get_id());
			GestorSalida.GAME_SEND_gK_PACKET(_perso, "K"+name+"|"+name);
		}
	}
	
	private void guild_join(String packet)
	{
		switch(packet.charAt(0))
		{
		case 'R'://Nom perso
			Personaje P = Mundo.getPersonajePorNombre(packet.substring(1));
			if(P == null || _perso.get_guild() == null)
			{
				GestorSalida.GAME_SEND_gJ_PACKET(_perso, "Eu");
				return;
			}
			if(!P.isConectado())
			{
				GestorSalida.GAME_SEND_gJ_PACKET(_perso, "Eu");
				return;
			}
			if(P.is_away())
			{
				GestorSalida.GAME_SEND_gJ_PACKET(_perso, "Eo");
				return;
			}
			if(P.get_guild() != null)
			{
				GestorSalida.GAME_SEND_gJ_PACKET(_perso, "Ea");
				return;
			}
			if(!_perso.getMiembroGremio().canDo(Constantes.G_INVITE))
			{
				GestorSalida.GAME_SEND_gJ_PACKET(_perso, "Ed");
				return;
			}
			if(_perso.get_guild().getMembers().size() >= (40+_perso.get_guild().get_lvl()))//Limite membres max
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "155;"+(40+_perso.get_guild().get_lvl()));
				return;
			}
			
			_perso.setInvitation(P.get_GUID());
			P.setInvitation(_perso.get_GUID());

			GestorSalida.GAME_SEND_gJ_PACKET(_perso,"R"+packet.substring(1));
			GestorSalida.GAME_SEND_gJ_PACKET(P,"r"+_perso.get_GUID()+"|"+_perso.getNombre()+"|"+_perso.get_guild().get_name());
		break;
		case 'E'://ou Refus
			if(packet.substring(1).equalsIgnoreCase(_perso.getInvitation()+""))
			{
				Personaje p = Mundo.getPersonnage(_perso.getInvitation());
				if(p == null)return;//Pas censï¿½ arriver
				GestorSalida.GAME_SEND_gJ_PACKET(p,"Ec");
			}
		break;
		case 'K'://Accepte
			if(packet.substring(1).equalsIgnoreCase(_perso.getInvitation()+""))
			{
				Personaje p = Mundo.getPersonnage(_perso.getInvitation());
				if(p == null)return;//Pas censï¿½ arriver
				Gremio G = p.get_guild();
				GuildMember GM = G.addNewMember(_perso);
				GestorSQL.actualizar_miembro_del_gremio(GM);
				_perso.setGuildMember(GM);
				_perso.setInvitation(-1);
				p.setInvitation(-1);
				//Packet
				GestorSalida.GAME_SEND_gJ_PACKET(p,"Ka"+_perso.getNombre());
				GestorSalida.GAME_SEND_gS_PACKET(_perso, GM);
				GestorSalida.GAME_SEND_gJ_PACKET(_perso,"Kj");
			}
		break;
		}
	}

	private void guild_infos(char c)
	{
		switch (c) {
//Perco
			case 'B' -> GestorSalida.GAME_SEND_gIB_PACKET(_perso, _perso.get_guild().parsePercotoGuild());
//Enclos
			case 'F' -> GestorSalida.GAME_SEND_gIF_PACKET(_perso, Mundo.parseMPtoGuild(_perso.get_guild().get_id()));
//General
			case 'G' -> GestorSalida.GAME_SEND_gIG_PACKET(_perso, _perso.get_guild());
//House
			case 'H' -> GestorSalida.GAME_SEND_gIH_PACKET(_perso, Casas.parseHouseToGuild(_perso));
//Members
			case 'M' -> GestorSalida.GAME_SEND_gIM_PACKET(_perso, _perso.get_guild(), '+');
//Perco
			case 'T' -> {
				GestorSalida.GAME_SEND_gITM_PACKET(_perso, Recaudador.parsetoGuild(_perso.get_guild().get_id()));
				Recaudador.parseAttaque(_perso, _perso.get_guild().get_id());
				Recaudador.parseDefense(_perso, _perso.get_guild().get_id());
			}
		}
	}

	private void guild_create(String packet)
	{
		if(_perso == null)return;
		if(_perso.get_guild() != null || _perso.getMiembroGremio() != null)
		{
			GestorSalida.GAME_SEND_gC_PACKET(_perso, "Ea");
			return;
		}
		if(_perso.getPelea() != null || _perso.is_away())return;
		try
		{
			String[] infos = packet.substring(2).split("\\|");
			//base 10 => 36
			String bgID = Integer.toString(Integer.parseInt(infos[0]),36);
			String bgCol = Integer.toString(Integer.parseInt(infos[1]),36);
			String embID =  Integer.toString(Integer.parseInt(infos[2]),36);
			String embCol =  Integer.toString(Integer.parseInt(infos[3]),36);
			String name = infos[4];
			if(Mundo.guildNameIsUsed(name))
			{
				GestorSalida.GAME_SEND_gC_PACKET(_perso, "Ean");
				return;
			}
			
			//Validation du nom de la guilde
			String tempName = name.toLowerCase();
			boolean isValid = true;
			//Vï¿½rifie d'abord si il contient des termes dï¿½finit
			if(tempName.length() > 20
					|| tempName.contains("mj")
					|| tempName.contains("modo")
					|| tempName.contains("admin"))
			{
				isValid = false;
			}
			//Si le nom passe le test, on vï¿½rifie que les caractï¿½re entrï¿½ sont correct.
			if(isValid)
			{
				int tiretCount = 0;
				for(char curLetter : tempName.toCharArray())
				{
					if(!(	(curLetter >= 'a' && curLetter <= 'z')
							|| curLetter == '-'))
					{
						isValid = false;
						break;
					}
					if(curLetter == '-')
					{
						if(tiretCount >= 2)
						{
							isValid = false;
							break;
						}
						else
						{
							tiretCount++;
						}
					}
				}
			}
			//Si le nom est invalide
			if(!isValid)
			{
				GestorSalida.GAME_SEND_gC_PACKET(_perso, "Ean");
				return;
			}
			//FIN de la validation
			String emblem = bgID+","+bgCol+","+embID+","+embCol;//9,6o5nc,2c,0;
			if(Mundo.guildEmblemIsUsed(emblem))
			{
				GestorSalida.GAME_SEND_gC_PACKET(_perso, "Eae");
				return;
			}
			if(_perso.getActualMapa().getID() == 2196)//Temple de crï¿½ation de guilde
			{
				if(!_perso.hasItemTemplate(1575,1))//Guildalogemme
				{
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "14");
					return;
				}
				_perso.removeByTemplateID(1575, 1);
			}
			Gremio G = new Gremio(_perso,name,emblem);
			GuildMember gm = G.addNewMember(_perso);
			gm.setAllRights(1,(byte) 0,1);//1 => Meneur (Tous droits)
			_perso.setGuildMember(gm);//On ajoute le meneur
			Mundo.addGuild(G, true);
			GestorSQL.actualizar_miembro_del_gremio(gm);
			//Packets
			GestorSalida.GAME_SEND_gS_PACKET(_perso, gm);
			GestorSalida.GAME_SEND_gC_PACKET(_perso,"K");
			GestorSalida.GAME_SEND_gV_PACKET(_perso);
		}catch(Exception e){return;}
	}

	private void parseChanelPacket(String packet)
	{
		if (packet.charAt(1) == 'C') {//Changement des Canaux
			Chanels_change(packet);
		}
	}

	private void Chanels_change(String packet)
	{
		String chan = packet.charAt(3)+"";
		switch (packet.charAt(2)) {
//Ajout du Canal
			case '+' -> _perso.addChanel(chan);
//Desactivation du canal
			case '-' -> _perso.removeChanel(chan);
		}
		GestorSQL.guardar_personaje(_perso, false);
	}

	private void parseMountPacket(String packet)
	{
		//On rafraichit l'enclo
		//On rafraichit l'enclo
		switch (packet.charAt(1)) {
//Achat d'un enclos
			case 'b' -> {
				GestorSalida.GAME_SEND_R_PACKET(_perso, "v");//Fermeture du panneau
				MountPark MP = _perso.getActualMapa().getMountPark();
				Personaje Seller = Mundo.getPersonnage(MP.get_owner());
				if (MP.get_owner() == -1) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "196");
					return;
				}
				if (MP.get_price() == 0) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "197");
					return;
				}
				if (_perso.get_guild() == null) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1135");
					return;
				}
				if (_perso.getMiembroGremio().getRank() != 1) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "198");
					return;
				}
				byte enclosMax = (byte) Math.floor(_perso.get_guild().get_lvl() / 10);
				byte TotalEncloGuild = (byte) Mundo.totalMPGuild(_perso.get_guild().get_id());
				if (TotalEncloGuild >= enclosMax) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1103");
					return;
				}
				if (_perso.getKamas() < MP.get_price()) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "182");
					return;
				}
				long NewKamas = _perso.getKamas() - MP.get_price();
				_perso.setKamas(NewKamas);
				if (Seller != null) {
					long NewSellerBankKamas = Seller.getBankKamas() + MP.get_price();
					Seller.setBankKamas(NewSellerBankKamas);
					if (Seller.isConectado()) {
						GestorSalida.GAME_SEND_MESSAGE(_perso, "Un enclo a ete vendu a " + MP.get_price() + ".", MainServidor.CONFIG_MOTD_COLOR);
					}
				}
				MP.set_price(0);//On vide le prix
				MP.set_owner(_perso.get_GUID());
				MP.set_guild(_perso.get_guild());
				GestorSQL.guardar_cercados(MP);
				GestorSQL.guardar_personaje(_perso, true);
				for (Personaje z : _perso.getActualMapa().getPersos()) {
					GestorSalida.GAME_SEND_Rp_PACKET(z, MP);
				}
			}
//Demande Description
			case 'd' -> Mount_description(packet);
//Change le nom
			case 'n' -> Mount_name(packet.substring(2));
//Monter sur la dinde
			case 'r' -> Mount_ride();
//Vendre l'enclo
			case 's' -> {
				GestorSalida.GAME_SEND_R_PACKET(_perso, "v");//Fermeture du panneau
				int price = Integer.parseInt(packet.substring(2));
				MountPark MP1 = _perso.getActualMapa().getMountPark();
				if (!MP1.getData().isEmpty()) {
					GestorSalida.GAME_SEND_MESSAGE(_perso, "[ENCLO] Impossible de vendre un enclo plein.", MainServidor.CONFIG_MOTD_COLOR);
					return;
				}
				if (MP1.get_owner() == -1) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "194");
					return;
				}
				if (MP1.get_owner() != _perso.get_GUID()) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "195");
					return;
				}
				MP1.set_price(price);
				GestorSQL.guardar_cercados(MP1);
				GestorSQL.guardar_personaje(_perso, true);
				for (Personaje z : _perso.getActualMapa().getPersos()) {
					GestorSalida.GAME_SEND_Rp_PACKET(z, MP1);
				}
			}
//Fermeture panneau d'achat
			case 'v' -> GestorSalida.GAME_SEND_R_PACKET(_perso, "v");
//Change l'xp donner a la dinde
			case 'x' -> Mount_changeXpGive(packet);
		}
	}

	private void Mount_changeXpGive(String packet)
	{
		try
		{
			int xp = Integer.parseInt(packet.substring(2));
			if(xp <0)xp = 0;
			if(xp >90)xp = 90;
			_perso.setMountGiveXp(xp);
			GestorSalida.GAME_SEND_Rx_PACKET(_perso);
		}catch(Exception ignored){}
	}

	private void Mount_name(String name)
	{
		if(_perso.getMount() == null)return;
		_perso.getMount().setName(name);
		GestorSalida.GAME_SEND_Rn_PACKET(_perso, name);
	}
	
	private void Mount_ride()
	{
		if(_perso.get_lvl()<60 || _perso.getMount() == null || !_perso.getMount().isMountable() || _perso._isGhosts)
		{
			GestorSalida.GAME_SEND_Re_PACKET(_perso,"Er", null);
			return;
		}
		_perso.toogleOnMount();
	}
	
	private void Mount_description(String packet) {
		int DDid = -1;
		try {
			DDid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			//on ignore le temps?
		}catch(Exception ignored){}
		if(DDid == -1)return;
		Dragopavo dragopavo = Mundo.getDragopavoPorID(DDid);
		if(dragopavo == null)return;
		GestorSalida.ENVIAR_PAQUETE_DESCRIPCION_DE_MONTURA(_perso,dragopavo);
	}

	private void parse_friendPacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A'://Ajouter
				Friend_add(packet);
			break;
			case 'D'://Effacer un ami
				Friend_delete(packet);
			break;
			case 'L'://Liste
				GestorSalida.GAME_SEND_FRIENDLIST_PACKET(_perso);
			break;
			case 'O':
				switch (packet.charAt(2)) {
					case '-' -> {
						_perso.SetSeeFriendOnline(false);
						GestorSalida.GAME_SEND_BN(_perso);
					}
					case '+' -> {
						_perso.SetSeeFriendOnline(true);
						GestorSalida.GAME_SEND_BN(_perso);
					}
				}
			break;
			case 'J': //Wife
				FriendLove(packet);
			break;
		}
	}

	private void FriendLove(String packet)
	{
		Personaje Wife = Mundo.getPersonnage(_perso.getWife());
		if(Wife == null) return;
		if(!Wife.isConectado())
		{
			if(Wife.getSexo() == 0) GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "140");
			else GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "139");
			
			GestorSalida.GAME_SEND_FRIENDLIST_PACKET(_perso);
			return;
		}
		switch(packet.charAt(2))
		{
			case 'S'://Teleportation
				if(_perso.getPelea() != null)
					return;
				else
					_perso.meetWife(Wife);
			break;
			case 'C'://Suivre le deplacement
				if(packet.charAt(3) == '+'){//Si lancement de la traque
					if(_perso._Follows != null)
					{
						_perso._Follows._Follower.remove(_perso.get_GUID());
					}
					GestorSalida.GAME_SEND_FLAG_PACKET(_perso, Wife);
					_perso._Follows = Wife;
					Wife._Follower.put(_perso.get_GUID(), _perso);
				}else{//On arrete de suivre
					GestorSalida.GAME_SEND_DELETE_FLAG_PACKET(_perso);
					_perso._Follows = null;
					Wife._Follower.remove(_perso.get_GUID());
				}
			break;
		}
	} 
	
	private void Friend_delete(String packet) {
		if(_perso == null)return;
		int guid = -1;
		switch (packet.charAt(2)) {
//Nom de perso
			case '%' -> {
				packet = packet.substring(3);
				Personaje P = Mundo.getPersonajePorNombre(packet);
				if (P == null)//Si P est nul, ou si P est nonNul et P offline
				{
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = P.getAccID();
			}
//Pseudo
			case '*' -> {
				packet = packet.substring(3);
				Cuenta C = Mundo.getCompteByPseudo(packet);
				if (C == null) {
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = C.get_GUID();
			}
			default -> {
				packet = packet.substring(2);
				Personaje Pr = Mundo.getPersonajePorNombre(packet);
				if (Pr == null ? true : !Pr.isConectado())//Si P est nul, ou si P est nonNul et P offline
				{
					GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
					return;
				}
				guid = Pr.getCuenta().get_GUID();
			}
		}
		if(guid == -1 || !_compte.isFriendWith(guid))
		{
			GestorSalida.GAME_SEND_FD_PACKET(_perso, "Ef");
			return;
		}
		_compte.removeFriend(guid);
	}

	private void Friend_add(String packet)
	{
		if(_perso == null)return;
		int guid = -1;
		switch (packet.charAt(2)) {
//Nom de perso
			case '%' -> {
				packet = packet.substring(3);
				Personaje P = Mundo.getPersonajePorNombre(packet);
				if (P == null ? true : !P.isConectado())//Si P est nul, ou si P est nonNul et P offline
				{
					GestorSalida.GAME_SEND_FA_PACKET(_perso, "Ef");
					return;
				}
				guid = P.getAccID();
			}
//Pseudo
			case '*' -> {
				packet = packet.substring(3);
				Cuenta C = Mundo.getCompteByPseudo(packet);
				if (C == null ? true : !C.isOnline()) {
					GestorSalida.GAME_SEND_FA_PACKET(_perso, "Ef");
					return;
				}
				guid = C.get_GUID();
			}
			default -> {
				packet = packet.substring(2);
				Personaje Pr = Mundo.getPersonajePorNombre(packet);
				if (Pr == null ? true : !Pr.isConectado())//Si P est nul, ou si P est nonNul et P offline
				{
					GestorSalida.GAME_SEND_FA_PACKET(_perso, "Ef");
					return;
				}
				guid = Pr.getCuenta().get_GUID();
			}
		}
		if(guid == -1)
		{
			GestorSalida.GAME_SEND_FA_PACKET(_perso, "Ef");
			return;
		}
		_compte.addFriend(guid);
	}

	private void parseGroupPacket(String packet)
	{
		switch (packet.charAt(1)) {
//Accepter invitation
			case 'A' -> group_accept(packet);
//Suivre membre du groupe PF+GUID
			case 'F' -> {
				Grupo g = _perso.getActualGrupo();
				if (g == null) return;
				int pGuid = -1;
				try {
					pGuid = Integer.parseInt(packet.substring(3));
				} catch (NumberFormatException e) {
					return;
				}
				if (pGuid == -1) return;
				Personaje P = Mundo.getPersonnage(pGuid);
				if (P == null || !P.isConectado()) return;
				if (packet.charAt(2) == '+')//Suivre
				{
					if (_perso._Follows != null) {
						_perso._Follows._Follower.remove(_perso.get_GUID());
					}
					GestorSalida.GAME_SEND_FLAG_PACKET(_perso, P);
					GestorSalida.GAME_SEND_PF(_perso, "+" + P.get_GUID());
					_perso._Follows = P;
					P._Follower.put(_perso.get_GUID(), _perso);
				} else if (packet.charAt(2) == '-')//Ne plus suivre
				{
					GestorSalida.GAME_SEND_DELETE_FLAG_PACKET(_perso);
					GestorSalida.GAME_SEND_PF(_perso, "-");
					_perso._Follows = null;
					P._Follower.remove(_perso.get_GUID());
				}
			}
//Suivez le tous PG+GUID
			case 'G' -> {
				Grupo g2 = _perso.getActualGrupo();
				if (g2 == null) return;
				int pGuid2 = -1;
				try {
					pGuid2 = Integer.parseInt(packet.substring(3));
				} catch (NumberFormatException e) {
					return;
				}
				if (pGuid2 == -1) return;
				Personaje P2 = Mundo.getPersonnage(pGuid2);
				if (P2 == null || !P2.isConectado()) return;
				if (packet.charAt(2) == '+')//Suivre
				{
					for (Personaje T : g2.getMiembrosGrupo()) {
						if (T.get_GUID() == P2.get_GUID()) continue;
						if (T._Follows != null) {
							T._Follows._Follower.remove(_perso.get_GUID());
						}
						GestorSalida.GAME_SEND_FLAG_PACKET(T, P2);
						GestorSalida.GAME_SEND_PF(T, "+" + P2.get_GUID());
						T._Follows = P2;
						P2._Follower.put(T.get_GUID(), T);
					}
				} else if (packet.charAt(2) == '-')//Ne plus suivre
				{
					for (Personaje T : g2.getMiembrosGrupo()) {
						if (T.get_GUID() == P2.get_GUID()) continue;
						GestorSalida.GAME_SEND_DELETE_FLAG_PACKET(T);
						GestorSalida.GAME_SEND_PF(T, "-");
						T._Follows = null;
						P2._Follower.remove(T.get_GUID());
					}
				}
			}
//inviation
			case 'I' -> group_invite(packet);
//Refuse
			case 'R' -> group_refuse();
//Quitter
			case 'V' -> group_quit(packet);
//Localisation du groupe
			case 'W' -> group_locate();
		}
	}
	
	private void group_locate()
	{
		if(_perso == null)return;
		Grupo g = _perso.getActualGrupo();
		if(g == null)return;
		String str = "";
		boolean isFirst = true;
		for(Personaje GroupP : _perso.getActualGrupo().getMiembrosGrupo())
		{
			if(!isFirst) str += "|";
			str += GroupP.getActualMapa().getX()+";"+GroupP.getActualMapa().getY()+";"+GroupP.getActualMapa().getID()+";2;"+GroupP.get_GUID()+";"+GroupP.getNombre();
			isFirst = false;
		}
		GestorSalida.GAME_SEND_IH_PACKET(_perso, str);
	}
	
	private void group_quit(String packet)
	{
		if(_perso == null)return;
		Grupo g = _perso.getActualGrupo();
		if(g == null)return;
		if(packet.length() == 2)//Si aucun guid est spï¿½cifiï¿½, alors c'est que le joueur quitte
		{
			 g.leave(_perso);
			 GestorSalida.GAME_SEND_PV_PACKET(_out,"");
			GestorSalida.GAME_SEND_IH_PACKET(_perso, "");
		}else if(g.isChief(_perso.get_GUID()))//Sinon, c'est qu'il kick un joueur du groupe
		{
			int guid = -1;
			try
			{
				guid = Integer.parseInt(packet.substring(2));
			}catch(NumberFormatException e){return;}
			if(guid == -1)return;
			Personaje t = Mundo.getPersonnage(guid);
			g.leave(t);
			GestorSalida.GAME_SEND_PV_PACKET(t.getCuenta().getGameThread().get_out(),""+_perso.get_GUID());
			GestorSalida.GAME_SEND_IH_PACKET(t, "");
		}
	}

	private void group_invite(String packet)
	{
		if(_perso == null)return;
		String name = packet.substring(2);
		Personaje target = Mundo.getPersonajePorNombre(name);
		if(target == null)return;
		if(!target.isConectado())
		{
			GestorSalida.GAME_SEND_GROUP_INVITATION_ERROR(_out,"n"+name);
			return;
		}
		if(target.getActualGrupo() != null)
		{
			GestorSalida.GAME_SEND_GROUP_INVITATION_ERROR(_out, "a"+name);
			return;
		}
		if(_perso.getActualGrupo() != null && _perso.getActualGrupo().getPersosNumber() == 8)
		{
			GestorSalida.GAME_SEND_GROUP_INVITATION_ERROR(_out, "f");
			return;
		}
		target.setInvitation(_perso.get_GUID());	
		_perso.setInvitation(target.get_GUID());
		GestorSalida.GAME_SEND_GROUP_INVITATION(_out,_perso.getNombre(),name);
		GestorSalida.GAME_SEND_GROUP_INVITATION(target.getCuenta().getGameThread().get_out(),_perso.getNombre(),name);
	}

	private void group_refuse()
	{
		if(_perso == null)return;
		if(_perso.getInvitation() == 0)return;
		_perso.setInvitation(0);
		GestorSalida.GAME_SEND_BN(_out);
		Personaje t = Mundo.getPersonnage(_perso.getInvitation());
		if(t == null) return;
		t.setInvitation(0);
		GestorSalida.GAME_SEND_PR_PACKET(t);
	}

	private void group_accept(String packet)
	{
		if(_perso == null)return;
		if(_perso.getInvitation() == 0)return;
		Personaje t = Mundo.getPersonnage(_perso.getInvitation());
		if(t == null) return;
		Grupo g = t.getActualGrupo();
		if(g == null)
		{
			g = new Grupo(t,_perso);
			GestorSalida.GAME_SEND_GROUP_CREATE(_out,g);
			GestorSalida.GAME_SEND_PL_PACKET(_out,g);
			GestorSalida.GAME_SEND_GROUP_CREATE(t.getCuenta().getGameThread().get_out(),g);
			GestorSalida.GAME_SEND_PL_PACKET(t.getCuenta().getGameThread().get_out(),g);
			t.setGroup(g);
			GestorSalida.GAME_SEND_ALL_PM_ADD_PACKET(t.getCuenta().getGameThread().get_out(),g);
		}
		else
		{
			GestorSalida.GAME_SEND_GROUP_CREATE(_out,g);
			GestorSalida.GAME_SEND_PL_PACKET(_out,g);
			GestorSalida.GAME_SEND_PM_ADD_PACKET_TO_GROUP(g, _perso);
			g.addPerso(_perso);
		}
		_perso.setGroup(g);
		GestorSalida.GAME_SEND_ALL_PM_ADD_PACKET(_out,g);
		GestorSalida.GAME_SEND_PR_PACKET(t);
	}

	private void parseObjectPacket(String packet)
	{
		switch (packet.charAt(1)) {
//Supression d'un objet
			case 'd' -> Object_delete(packet);
//Depose l'objet au sol
			case 'D' -> Object_drop(packet);
//Bouger un objet (Equiper/dï¿½sï¿½quiper) // Associer obvijevan
			case 'M' -> Object_move(packet);
//Utiliser un objet (potions)
			case 'U' -> Object_use(packet);
			case 'x' -> Object_obvijevan_desassocier(packet);
			case 'f' -> Object_obvijevan_feed(packet);
			case 's' -> Object_obvijevan_changeApparence(packet);
		}
	}

	private void Object_drop(String packet)
	{
		int guid = -1;
		int qua = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			qua = Integer.parseInt(packet.split("\\|")[1]);
		}catch(Exception ignored){}
		if(guid == -1 || qua <= 0 || !_perso.hasItemGuid(guid) || _perso.getPelea() != null || _perso.is_away())return;
		Objeto obj = Mundo.getObjet(guid);
		
		_perso.set_curCell(_perso.getActualCelda());
		int cellPosition = Constantes.getNearCellidUnused(_perso);
		if(cellPosition < 0)
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1145");
			return;
		}
		if(obj.getPosition() != Constantes.ITEM_POS_NO_EQUIPED)
		{
			obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
			GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(_perso,obj);
			if(obj.getPosition() == Constantes.ITEM_POS_ARME 		||
				obj.getPosition() == Constantes.ITEM_POS_COIFFE 		||
				obj.getPosition() == Constantes.ITEM_POS_FAMILIER 	||
				obj.getPosition() == Constantes.ITEM_POS_CAPE		||
				obj.getPosition() == Constantes.ITEM_POS_BOUCLIER	||
				obj.getPosition() == Constantes.ITEM_POS_NO_EQUIPED)
					GestorSalida.GAME_SEND_ON_EQUIP_ITEM(_perso.getActualMapa(), _perso);
		}
		if(qua >= obj.getQuantity())
		{
			_perso.removeItem(guid);
			_perso.getActualMapa().getMapa(_perso.getActualCelda().getID()+cellPosition).addDroppedItem(obj);
			obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
			GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(_perso, guid);
		}else
		{
			obj.setQuantity(obj.getQuantity() - qua);
			Objeto obj2 = Objeto.getCloneObjet(obj, qua);
			obj2.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
			_perso.getActualMapa().getMapa(_perso.getActualCelda().getID()+cellPosition).addDroppedItem(obj2);
			GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
		}
		GestorSalida.GAME_SEND_Ow_PACKET(_perso);
		GestorSalida.GAME_SEND_GDO_PACKET_TO_MAP(_perso.getActualMapa(),'+',_perso.getActualMapa().getMapa(_perso.getActualCelda().getID()+cellPosition).getID(),obj.getTemplate().getID(),0);
		GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
	}

	private void Object_use(String packet)
	{
		int guid = -1;
		int targetGuid = -1;
		short cellID = -1;
		Personaje Target = null;
		try
		{
			String[] infos = packet.substring(2).split("\\|");
			guid = Integer.parseInt(infos[0]);
			try
			{
				targetGuid = Integer.parseInt(infos[1]);
			}catch(Exception e){targetGuid = -1;}
			try
			{
				cellID = Short.parseShort(infos[2]);
			}catch(Exception e){cellID = -1;}
		}catch(Exception e){return;}
		//Si le joueur n'a pas l'objet
		if(Mundo.getPersonnage(targetGuid) != null)
		{
			Target = Mundo.getPersonnage(targetGuid);
		}
		if(!_perso.hasItemGuid(guid) || _perso.getPelea() != null || _perso.is_away())return;
		if(Target != null && (Target.getPelea() != null || Target.is_away()))return;
		Objeto obj = Mundo.getObjet(guid);
		if(obj == null) return;
		ObjTemplate T = obj.getTemplate();
		if(!obj.getTemplate().getConditions().equalsIgnoreCase("") && !Condiciones.ValidarCondicion(_perso,obj.getTemplate().getConditions()))
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "119|43");
			return;
		}
		T.applyAction(_perso, Target, guid, cellID);
	}

	private synchronized void Object_move(String packet)
	{
		String[] infos = packet.substring(2).split(""+(char)0x0A)[0].split("\\|");
		try
		{
			int qua;
			int guid = Integer.parseInt(infos[0]);
			int pos = Integer.parseInt(infos[1]);
			try
			{
				qua = Integer.parseInt(infos[2]);
			}catch(Exception e)
			{
				qua = 1;
			}
			Objeto obj = Mundo.getObjet(guid);
			// LES VERIFS
			if(!_perso.hasItemGuid(guid) || obj == null) // item n'existe pas ou perso n'a pas l'item
				return;
			if(_perso.getPelea() != null) // si en combat dï¿½marrï¿½
				if(_perso.getPelea().get_state() > Constantes.FIGHT_STATE_ACTIVE)
					return;
			if(!Constantes.isValidPlaceForItem(obj.getTemplate(),pos) && pos != Constantes.ITEM_POS_NO_EQUIPED) // si mauvaise place
				return;
			if(!obj.getTemplate().getConditions().equalsIgnoreCase("") && !Condiciones.ValidarCondicion(_perso,obj.getTemplate().getConditions())) {
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "119|43"); // si le perso ne vï¿½rifie pas les conditions diverses
				return;
			}
			if(obj.getTemplate().getLevel() > _perso.get_lvl())  {// si le perso n'a pas le level
				GestorSalida.GAME_SEND_OAEL_PACKET(_out);
				return;
			}
			//On ne peut ï¿½quiper 2 items de panoplies identiques, ou 2 Dofus identiques
			if(pos != Constantes.ITEM_POS_NO_EQUIPED && (obj.getTemplate().getPanopID() != -1 || obj.getTemplate().getType() == Constantes.ITEM_TYPE_DOFUS )&& _perso.hasEquiped(obj.getTemplate().getID()))
				return;
			// FIN DES VERIFS
			
			
			Objeto exObj = _perso.getObjetByPos(pos);//Objet a l'ancienne position
		    int objGUID = obj.getTemplate().getID();
		    // CODE OBVI
			if ((objGUID == 9234) || (objGUID == 9233) || (objGUID == 9255) || (objGUID == 9256))
			{
				// LES VERFIS
				if (exObj == null) 	{// si on place l'obvi sur un emplacement vide
					GestorSalida.send(_perso, "Im1161");
					return;	
				}
				if (exObj.getObvijevanPos() != 0) {// si il y a dï¿½jï¿½ un obvi
					GestorSalida.GAME_SEND_BN(_perso);
					return;
				}
				// FIN DES VERIFS
		        		
				exObj.setObvijevanPos(obj.getObvijevanPos()); // L'objet qui ï¿½tait en place a maintenant un obvi
					
				_perso.removeItem(obj.getGuid(), 1, false, false); // on enlï¿½ve l'existance de l'obvi en lui-mï¿½me
				GestorSalida.send(_perso, "OR" + obj.getGuid()); // on le prï¿½cise au client

				exObj.clearStats();
				String cibleNewStats = obj.parseStatsStringSansUserObvi() + "," + exObj.parseStatsStringSansUserObvi() +
						",3ca#" + Integer.toHexString(objGUID) + "#0#0#0d0+" + objGUID;
				exObj.parseStringToStats(cibleNewStats);
					
				GestorSalida.send(_perso, exObj.obvijevanOCO_Packet(pos));
					
				if ((objGUID == 9233) || (objGUID == 9234)) 
					GestorSalida.GAME_SEND_ON_EQUIP_ITEM(_perso.getActualMapa(), _perso); // Si l'obvi ï¿½tait cape ou coiffe : packet au client
				// S'il y avait plusieurs objets
				if(obj.getQuantity() > 1)
				{
					if(qua > obj.getQuantity())
						qua = obj.getQuantity();
					
					if(obj.getQuantity() - qua > 0)//Si il en reste
					{
						int newItemQua = obj.getQuantity()-qua;
						Objeto newItem = Objeto.getCloneObjet(obj,newItemQua);
						_perso.addObjet(newItem,false);
						Mundo.addObjet(newItem,true);
						obj.setQuantity(qua);
						GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
					}
				}
				
				return; // on s'arrï¿½te lï¿½ pour l'obvi
			} // FIN DU CODE OBVI
			
			if(exObj != null)//S'il y avait dï¿½ja un objet sur cette place on dï¿½sï¿½quipe
			{
				Objeto obj2;
				if((obj2 = _perso.getSimilarItem(exObj)) != null)//On le possï¿½de deja
				{
					obj2.setQuantity(obj2.getQuantity()+exObj.getQuantity());
					GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj2);
					Mundo.removeItem(exObj.getGuid());
					_perso.removeItem(exObj.getGuid());
					GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(_perso, exObj.getGuid());
				}
				else//On ne le possï¿½de pas
				{
					exObj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
					GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(_perso,exObj);
				}
				if(_perso.getObjetByPos(Constantes.ITEM_POS_ARME) == null)
					GestorSalida.GAME_SEND_OT_PACKET(_out, -1);
				
				//Si objet de panoplie
				if(exObj.getTemplate().getPanopID() > 0)
					GestorSalida.GAME_SEND_OS_PACKET(_perso,exObj.getTemplate().getPanopID());
			}else//getNumbEquipedItemOfPanoplie(exObj.getTemplate().getPanopID()
			{
				Objeto obj2;
				//On a un objet similaire
				if((obj2 = _perso.getSimilarItem(obj)) != null)
				{
					if(qua > obj.getQuantity()) qua = 
							obj.getQuantity();
					
					obj2.setQuantity(obj2.getQuantity()+qua);
					GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj2);
					
					if(obj.getQuantity() - qua > 0)//Si il en reste
					{
						obj.setQuantity(obj.getQuantity()-qua);
						GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
					}else//Sinon on supprime
					{
						Mundo.removeItem(obj.getGuid());
						_perso.removeItem(obj.getGuid());
						GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(_perso, obj.getGuid());
					}
				}
				else//Pas d'objets similaires
				{
					obj.setPosition(pos);
					GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(_perso,obj);
					if(obj.getQuantity() > 1)
					{
						if(qua > obj.getQuantity()) qua = obj.getQuantity();
						
						if(obj.getQuantity() - qua > 0)//Si il en reste
						{
							int newItemQua = obj.getQuantity()-qua;
							Objeto newItem = Objeto.getCloneObjet(obj,newItemQua);
							_perso.addObjet(newItem,false);
							Mundo.addObjet(newItem,true);
							obj.setQuantity(qua);
							GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
						}
					}
				}
			}
			GestorSalida.GAME_SEND_Ow_PACKET(_perso);
			_perso.refreshStats();
			if(_perso.getActualGrupo() != null)
			{
				GestorSalida.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_perso.getActualGrupo(),_perso);
			}
			GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
			if( pos == Constantes.ITEM_POS_ARME 		||
				pos == Constantes.ITEM_POS_COIFFE 	||
				pos == Constantes.ITEM_POS_FAMILIER 	||
				pos == Constantes.ITEM_POS_CAPE		||
				pos == Constantes.ITEM_POS_BOUCLIER	||
				pos == Constantes.ITEM_POS_NO_EQUIPED)
				GestorSalida.GAME_SEND_ON_EQUIP_ITEM(_perso.getActualMapa(), _perso);
		
			//Si familier
			if(pos == Constantes.ITEM_POS_FAMILIER && _perso.isOnMount())_perso.toogleOnMount();
			//Verif pour les outils de mï¿½tier
			if(pos == Constantes.ITEM_POS_NO_EQUIPED && _perso.getObjetByPos(Constantes.ITEM_POS_ARME) == null)
				GestorSalida.GAME_SEND_OT_PACKET(_out, -1);
			
			if(pos == Constantes.ITEM_POS_ARME && _perso.getObjetByPos(Constantes.ITEM_POS_ARME) != null)
			{
				int ID = _perso.getObjetByPos(Constantes.ITEM_POS_ARME).getTemplate().getID();
				for(Entry<Integer,StatsMetier> e : _perso.getMetiers().entrySet())
				{
					if(e.getValue().getTemplate().isValidTool(ID))
						GestorSalida.GAME_SEND_OT_PACKET(_out,e.getValue().getTemplate().getId());
				}
			}
			//Si objet de panoplie
			if(obj.getTemplate().getPanopID() > 0) GestorSalida.GAME_SEND_OS_PACKET(_perso,obj.getTemplate().getPanopID());
			//Si en combat
			if(_perso.getPelea() != null)
			{
				GestorSalida.GAME_SEND_ON_EQUIP_ITEM_FIGHT(_perso, _perso.getPelea().getFighterByPerso(_perso), _perso.getPelea());
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			GestorSalida.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
		}
	}

	private void Object_delete(String packet)
	{
		String[] infos = packet.substring(2).split("\\|");
		try
		{
			int guid = Integer.parseInt(infos[0]);
			int qua = 1;
			try
			{
				qua = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			Objeto obj = Mundo.getObjet(guid);
			if(obj == null || !_perso.hasItemGuid(guid) || qua <= 0 || _perso.getPelea() != null || _perso.is_away())
			{
				GestorSalida.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
				return;
			}
			int newQua = obj.getQuantity()-qua;
			if(newQua <=0)
			{
				_perso.removeItem(guid);
				Mundo.removeItem(guid);
				GestorSQL.eliminar_objeto(guid);
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(_perso, guid);
			}else
			{
				obj.setQuantity(newQua);
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);
			}
			GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
			GestorSalida.GAME_SEND_Ow_PACKET(_perso);
		}catch(Exception e)
		{
			GestorSalida.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
		}
	}

	private void parseDialogPacket(String packet)
	{
		switch (packet.charAt(1)) {
//Demande de l'initQuestion
			case 'C' -> Dialog_start(packet);
//Rï¿½ponse du joueur
			case 'R' -> Dialog_response(packet);
//Fin du dialog
			case 'V' -> Dialog_end();
		}
	}

	private void Dialog_response(String packet)
	{
		String[] infos = packet.substring(2).split("\\|");
		try
		{
			int qID = Integer.parseInt(infos[0]);
			int rID = Integer.parseInt(infos[1]);
			NPC_question quest = Mundo.getNPCQuestion(qID);
			NPC_reponse rep = Mundo.getNPCreponse(rID);
			if(quest == null || rep == null || !rep.isAnotherDialog())
			{
				GestorSalida.GAME_SEND_END_DIALOG_PACKET(_out);
				_perso.set_isTalkingWith(0);
			}
			rep.apply(_perso);
		}catch(Exception e)
		{
			GestorSalida.GAME_SEND_END_DIALOG_PACKET(_out);
		}
	}

	private void Dialog_end()
	{
		GestorSalida.GAME_SEND_END_DIALOG_PACKET(_out);
		if(_perso.get_isTalkingWith() != 0)
			_perso.set_isTalkingWith(0);
	}

	private void Dialog_start(String packet)
	{
		try
		{
			int npcID = Integer.parseInt(packet.substring(2).split((char)0x0A+"")[0]);
			NPC npc = _perso.getActualMapa().getNPC(npcID);
			if( npc == null)return;
			GestorSalida.GAME_SEND_DCK_PACKET(_out,npcID);
			int qID = npc.getModelo().getPreguntaInicial();
			NPC_question quest = Mundo.getNPCQuestion(qID);
			if(quest == null)
			{
				GestorSalida.GAME_SEND_END_DIALOG_PACKET(_out);
				return;
			}
			GestorSalida.GAME_SEND_QUESTION_PACKET(_out,quest.parseToDQPacket(_perso));
			_perso.set_isTalkingWith(npcID);
		}catch(NumberFormatException ignored){}
	}

	private void parseExchangePacket(String packet)
	{
		switch (packet.charAt(1)) {
//Accepter demande d'ï¿½change
			case 'A' -> Exchange_accept();
//Achat
			case 'B' -> Exchange_onBuyItem(packet);
//Demande prix moyen + catï¿½gorie
			case 'H' -> Exchange_HDV(packet);
//Ok
			case 'K' -> Exchange_isOK();
//jobAction : Refaire le craft prï¿½cedent
			case 'L' -> Exchange_doAgain();
//Move (Ajouter//retirer un objet a l'ï¿½change)
			case 'M' -> Exchange_onMoveItem(packet);
//Mode marchand
			case 'q' -> {
				if (_perso.get_isTradingWith() > 0 || _perso.getPelea() != null || _perso.is_away()) return;
				if (_perso.getActualMapa().getStoreCount() == 5) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "125;5");
					return;
				}
				if (_perso.parseStoreItemsList().isEmpty()) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "123");
					return;
				}
				int orientation = Formulas.getRandomValue(1, 3);
				_perso.set_orientation(orientation);
				Mapa map = _perso.getActualMapa();
				_perso.set_showSeller(true);
				Mundo.addSeller(_perso);
				kick();
				for (Personaje z : map.getPersos()) {
					if (z != null && z.isConectado())
						GestorSalida.GAME_SEND_MERCHANT_LIST(z, z.getActualMapa().getID());
				}
			}
//Rides => Monture
			case 'r' -> Exchange_mountPark(packet);
//liste d'achat NPC
			case 'R' -> Exchange_start(packet);
//Vente
			case 'S' -> Exchange_onSellItem(packet);
//Fin de l'ï¿½change
			case 'V' -> Exchange_finish_buy();
		}
	}
	
	private void Exchange_HDV(String packet)
	{
		if(_perso.get_isTradingWith() > 0 || _perso.getPelea() != null || _perso.is_away())return;
		int templateID;
		switch (packet.charAt(2)) {
//Confirmation d'achat
			case 'B' -> {
				String[] info = packet.substring(3).split("\\|");//ligneID|amount|price
				Mercadillo curHdv = Mundo.getHdv(Math.abs(_perso.get_isTradingWith()));
				int ligneID = Integer.parseInt(info[0]);
				byte amount = Byte.parseByte(info[1]);
				if (curHdv.buyItem(ligneID, amount, Integer.parseInt(info[2]), _perso)) {
					GestorSalida.GAME_SEND_EHm_PACKET(_perso, "-", ligneID + "");//Enleve la ligne
					if (curHdv.getLigne(ligneID) != null && !curHdv.getLigne(ligneID).isEmpty())
						GestorSalida.GAME_SEND_EHm_PACKET(_perso, "+", curHdv.getLigne(ligneID).parseToEHm());//Rï¿½ajoute la ligne si elle n'est pas vide

					/*if(curHdv.getLigne(ligneID) != null)
					{
						String str = curHdv.getLigne(ligneID).parseToEHm();
						SocketManager.GAME_SEND_EHm_PACKET(_perso,"+",str);
					}*/


					_perso.refreshStats();
					GestorSalida.GAME_SEND_Ow_PACKET(_perso);
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "068");//Envoie le message "Lot achetï¿½"
				} else {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "172");//Envoie un message d'erreur d'achat
				}
			}
//Demande listage d'un template (les prix)
			case 'l' -> {
				templateID = Integer.parseInt(packet.substring(3));
				try {
					GestorSalida.GAME_SEND_EHl(_perso, Mundo.getHdv(Math.abs(_perso.get_isTradingWith())), templateID);
				} catch (NullPointerException e)//Si erreur il y a, retire le template de la liste chez le client
				{
					GestorSalida.GAME_SEND_EHM_PACKET(_perso, "-", templateID + "");
				}
			}
//Demande des prix moyen
			case 'P' -> {
				templateID = Integer.parseInt(packet.substring(3));
				GestorSalida.GAME_SEND_EHP_PACKET(_perso, templateID);
			}
//Demande des template de la catï¿½gorie
			case 'T' -> {
				int categ = Integer.parseInt(packet.substring(3));
				String allTemplate = Mundo.getHdv(Math.abs(_perso.get_isTradingWith())).parseTemplate(categ);
				GestorSalida.GAME_SEND_EHL_PACKET(_perso, categ, allTemplate);
			}
		}
	}
	
	private void Exchange_mountPark(String packet)
	{
		//Si dans un enclos
		if(_perso.getInMountPark() != null)
		{
			MountPark MP = _perso.getInMountPark();
			if(_perso.get_isTradingWith() > 0 || _perso.getPelea() != null)return;
			char c = packet.charAt(2);
			packet = packet.substring(3);
			int guid = -1;
			try
			{
				guid = Integer.parseInt(packet);
			}catch(Exception ignored){}
			switch(c)
			{
				case 'C'://Parcho => Etable (Stocker)
					if(guid == -1 || !_perso.hasItemGuid(guid))return;
					if(MP.get_size() <= MP.MountParkDATASize())
					{
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1145");
						return;
					}
					Objeto obj = Mundo.getObjet(guid);
					//on prend la DD demandï¿½e
					int DDid = obj.getStats().getEffect(995);
					Dragopavo DD = Mundo.getDragopavoPorID(DDid);
					//FIXME mettre return au if pour ne pas crï¿½er des nouvelles dindes
					if(DD == null)
					{
						int color = Constantes.getMountColorByParchoTemplate(obj.getTemplate().getID());
						if(color <1)return;
						DD = new Dragopavo(color);
					}
					//On enleve l'objet du Monde et du Perso
					_perso.removeItem(guid);
					Mundo.removeItem(guid);
					//on ajoute la dinde a l'ï¿½table
					MP.addData(DD.getID(), _perso.get_GUID());
					GestorSQL.actualizar_cercado(MP);
					//On envoie les packet
					GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(_perso,obj.getGuid());
					GestorSalida.GAME_SEND_Ee_PACKET(_perso, '+', DD.parse());
				break;
				case 'c'://Etable => Parcho(Echanger)
					Dragopavo DD1 = Mundo.getDragopavoPorID(guid);
					//S'il n'a pas la dinde
					if(DD1 == null || !MP.getData().containsKey(DD1.getID()))return;
					if(MP.getData().get(DD1.getID()) != _perso.get_GUID() &&
						Mundo.getPersonnage(MP.getData().get(DD1.getID())).get_guild() != _perso.get_guild())
					{
						//Pas la mï¿½me guilde, pas le mï¿½me perso
						return;
					}
					if(MP.getData().get(DD1.getID()) != _perso.get_GUID() &&
							Mundo.getPersonnage(MP.getData().get(DD1.getID())).get_guild() == _perso.get_guild() &&
							!_perso.getMiembroGremio().canDo(Constantes.G_OTHDINDE))
					{
						//Mï¿½me guilde, pas le droit
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1101");
						return;
					}
					//on retire la dinde de l'ï¿½table
					MP.removeData(DD1.getID());
					GestorSQL.actualizar_cercado(MP);
					//On crï¿½er le parcho
					ObjTemplate T = Constantes.getParchoTemplateByMountColor(DD1.getColor());
					Objeto obj1 = T.createNewItem(1, false);
					//On efface les stats
					obj1.clearStats();
					//on ajoute la possibilitï¿½ de voir la dinde
					obj1.getStats().addOneStat(995, DD1.getID());
					obj1.addTxtStat(996, _perso.getNombre());
					obj1.addTxtStat(997, DD1.get_nom());
					
					//On ajoute l'objet au joueur
					Mundo.addObjet(obj1, true);
					_perso.addObjet(obj1, false);//Ne seras jamais identique de toute
					
					//Packets
					GestorSalida.GAME_SEND_Ow_PACKET(_perso);
					GestorSalida.GAME_SEND_Ee_PACKET(_perso,'-',DD1.getID()+"");
				break;
				case 'g'://Equiper
					Dragopavo DD3 = Mundo.getDragopavoPorID(guid);
					//S'il n'a pas la dinde
					if(DD3 == null || !MP.getData().containsKey(DD3.getID()) || _perso.getMount() != null)return;
					
					if(MP.getData().get(DD3.getID()) != _perso.get_GUID() &&
							Mundo.getPersonnage(MP.getData().get(DD3.getID())).get_guild() != _perso.get_guild())
					{
						//Pas la mï¿½me guilde, pas le mï¿½me perso
						return;
					}
					if(MP.getData().get(DD3.getID()) != _perso.get_GUID() &&
							Mundo.getPersonnage(MP.getData().get(DD3.getID())).get_guild() == _perso.get_guild() &&
							!_perso.getMiembroGremio().canDo(Constantes.G_OTHDINDE))
					{
						//Mï¿½me guilde, pas le droit
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1101");
						return;
					}
					
					MP.removeData(DD3.getID());
					GestorSQL.actualizar_cercado(MP);
					_perso.setMount(DD3);
					
					//Packets
					GestorSalida.GAME_SEND_Re_PACKET(_perso, "+", DD3);
					GestorSalida.GAME_SEND_Ee_PACKET(_perso,'-',DD3.getID()+"");
					GestorSalida.GAME_SEND_Rx_PACKET(_perso);
				break;
				case 'p'://Equipï¿½ => Stocker
					//Si c'est la dinde ï¿½quipï¿½
					if(_perso.getMount()!=null?_perso.getMount().getID() == guid:false)
					{
						//Si le perso est sur la monture on le fait descendre
						if(_perso.isOnMount())_perso.toogleOnMount();
						//Si ca n'a pas rï¿½ussie, on s'arrete lï¿½ (Items dans le sac ?)
						if(_perso.isOnMount())return;
						
						Dragopavo DD2 = _perso.getMount();
						MP.addData(DD2.getID(), _perso.get_GUID());
						GestorSQL.actualizar_cercado(MP);
						_perso.setMount(null);
						
						//Packets
						GestorSalida.GAME_SEND_Ee_PACKET(_perso,'+',DD2.parse());
						GestorSalida.GAME_SEND_Re_PACKET(_perso, "-", null);
						GestorSalida.GAME_SEND_Rx_PACKET(_perso);
					}else//Sinon...
					{
						
					}
				break;
			}
		}
	}

	private void Exchange_doAgain()
	{
		if(_perso.getCurJobAction() != null)
			_perso.getCurJobAction().putLastCraftIngredients();
	}

	private void Exchange_isOK()
	{
		if(_perso.getCurJobAction() != null)
		{
			//Si pas action de craft, on s'arrete la
			if(!_perso.getCurJobAction().isCraft())return;
			_perso.getCurJobAction().startCraft(_perso);
		}
		if(_perso.get_curExchange() == null)return;
		_perso.get_curExchange().toogleOK(_perso.get_GUID());
	}

	private void Exchange_onMoveItem(String packet)
	{
		//Store
		if(_perso.get_isTradingWith() == _perso.get_GUID())
		{
			if (packet.charAt(2) == 'O') {//Objets
				if (packet.charAt(3) == '+') {
					String[] infos = packet.substring(4).split("\\|");
					try {

						int guid = Integer.parseInt(infos[0]);
						int qua = Integer.parseInt(infos[1]);
						int price = Integer.parseInt(infos[2]);

						Objeto obj = Mundo.getObjet(guid);
						if (obj == null) return;

						if (qua > obj.getQuantity())
							qua = obj.getQuantity();

						_perso.addinStore(obj.getGuid(), price, qua);

					} catch (NumberFormatException e) {
					}
				} else {
					String[] infos = packet.substring(4).split("\\|");
					try {
						int guid = Integer.parseInt(infos[0]);
						int qua = Integer.parseInt(infos[1]);

						if (qua <= 0) return;

						Objeto obj = Mundo.getObjet(guid);
						if (obj == null) return;
						if (qua > obj.getQuantity()) return;
						if (qua < obj.getQuantity()) qua = obj.getQuantity();

						_perso.removeFromStore(obj.getGuid(), qua);
					} catch (NumberFormatException e) {
					}
				}
			}
			return;
		}
		//Percepteur
		if(_perso.get_isOnPercepteurID() != 0)
		{
			Recaudador perco = Mundo.getPerco(_perso.get_isOnPercepteurID());
			if(perco == null || perco.get_inFight() > 0)return;
			switch(packet.charAt(2))
			{
			case 'G'://Kamas
				if(packet.charAt(3) == '-') //On retire
				{
					long P_Kamas = Integer.parseInt(packet.substring(4));
					long P_Retrait = perco.getKamas()-P_Kamas;
					if(P_Retrait < 0)
					{
						P_Retrait = 0;
						P_Kamas = perco.getKamas();
					}
					perco.setKamas(P_Retrait);
					_perso.addKamas(P_Kamas);
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
					GestorSalida.GAME_SEND_EsK_PACKET(_perso,"G"+perco.getKamas());
				}
			break;
			case 'O'://Objets
				if(packet.charAt(3) == '-') //On retire
				{
					String[] infos = packet.substring(4).split("\\|");
					int guid = 0;
					int qua = 0;
					try
					{
						guid = Integer.parseInt(infos[0]);
						qua  = Integer.parseInt(infos[1]);
					}catch(NumberFormatException ignored){}

					if(guid <= 0 || qua <= 0) return;
					
					Objeto obj = Mundo.getObjet(guid);
					if(obj == null)return;

					if(perco.HaveObjet(guid))
					{
						perco.removeFromPercepteur(_perso, guid, qua);
					}
					perco.LogObjetDrop(guid, obj);
				}
			break;
			}
			_perso.get_guild().addXp(perco.getXp());
			perco.LogXpDrop(perco.getXp());
			perco.setXp(0);
			GestorSQL.actualizar_gremio(_perso.get_guild());
			return;
		}
		//HDV
		if(_perso.get_isTradingWith() < 0)
		{
			switch (packet.charAt(3)) {
//Retirer un objet de l'HDV
				case '-' -> {
					int cheapestID = Integer.parseInt(packet.substring(4).split("\\|")[0]);
					int count = Integer.parseInt(packet.substring(4).split("\\|")[1]);
					if (count <= 0) return;
					_perso.getCuenta().recoverItem(cheapestID, count);//Retire l'objet de la liste de vente du compte
					GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(_out, '-', "", cheapestID + "");
				}
//Mettre un objet en vente
				case '+' -> {
					int itmID = Integer.parseInt(packet.substring(4).split("\\|")[0]);
					byte amount = Byte.parseByte(packet.substring(4).split("\\|")[1]);
					int price = Integer.parseInt(packet.substring(4).split("\\|")[2]);
					if (amount <= 0 || price <= 0) return;
					Mercadillo curHdv = Mundo.getHdv(Math.abs(_perso.get_isTradingWith()));
					int taxe = (int) (price * (curHdv.getTaxe() / 100));
					if (!_perso.hasItemGuid(itmID))//Vï¿½rifie si le personnage a bien l'item spï¿½cifiï¿½ et l'argent pour payer la taxe
						return;
					if (_perso.getCuenta().countHdvItems(curHdv.getHdvID()) >= curHdv.getMaxItemCompte()) {
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "058");
						return;
					}
					if (_perso.getKamas() < taxe) {
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "176");
						return;
					}
					_perso.addKamas(taxe * -1);//Retire le montant de la taxe au personnage
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);//Met a jour les kamas du client
					Objeto obj = Mundo.getObjet(itmID);//Rï¿½cupï¿½re l'item
					if (amount > obj.getQuantity())//S'il veut mettre plus de cette objet en vente que ce qu'il possï¿½de
						return;
					int rAmount = (int) (Math.pow(10, amount) / 10);
					int newQua = (obj.getQuantity() - rAmount);
					if (newQua <= 0)//Si c'est plusieurs objets ensemble enleve seulement la quantitï¿½ de mise en vente
					{
						_perso.removeItem(itmID);//Enlï¿½ve l'item de l'inventaire du personnage
						GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(_perso, itmID);//Envoie un packet au client pour retirer l'item de son inventaire
					} else {
						obj.setQuantity(obj.getQuantity() - rAmount);
						GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, obj);

						Objeto newObj = Objeto.getCloneObjet(obj, rAmount);
						Mundo.addObjet(newObj, true);
						obj = newObj;
					}
					HdvEntry toAdd = new HdvEntry(price, amount, _perso.getCuenta().get_GUID(), obj);
					curHdv.addEntry(toAdd);    //Ajoute l'entry dans l'HDV
					GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(_out, '+', "", toAdd.parseToEmK());    //Envoie un packet pour ajouter l'item dans la fenetre de l'HDV du client
				}
			}
			return;
		}
		//Metier
		if(_perso.getCurJobAction() != null)
		{
			//Si pas action de craft, on s'arrete la
			if(!_perso.getCurJobAction().isCraft())return;
			if(packet.charAt(2) == 'O')//Ajout d'objet
			{
				if(packet.charAt(3) == '+')
				{
					//FIXME gerer les packets du genre  EMO+173|5+171|5+172|5 (split sur '+' ?:/)
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						if(qua <= 0)return;
						if(!_perso.hasItemGuid(guid))return;
						Objeto obj = Mundo.getObjet(guid);
						if(obj == null)return;
						if(obj.getQuantity()<qua)
							qua = obj.getQuantity();
							_perso.getCurJobAction().modifIngredient(_perso,guid,qua);
					}catch(NumberFormatException ignored){}
				}else
				{
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						if(qua <= 0)return;
						Objeto obj = Mundo.getObjet(guid);
						if(obj == null)return;
						_perso.getCurJobAction().modifIngredient(_perso,guid,-qua);
					}catch(NumberFormatException ignored){}
				}
				
			}else
			if(packet.charAt(2) == 'R')
			{
				try
				{
					int c = Integer.parseInt(packet.substring(3));
					_perso.getCurJobAction().repeat(c,_perso);
				}catch(Exception ignored){}
			}
			return;
		}
		//Banque
		if(_perso.isInBank())
		{
			if(_perso.get_curExchange() != null)return;
			switch (packet.charAt(2)) {
//Kamas
				case 'G' -> {
					long kamas = 0;
					try {
						kamas = Integer.parseInt(packet.substring(3));
					} catch (Exception ignored) {
					}
					if (kamas == 0) return;
					if (kamas > 0)//Si On ajoute des kamas a la banque
					{
						if (_perso.getKamas() < kamas) kamas = _perso.getKamas();
						_perso.setBankKamas(_perso.getBankKamas() + kamas);//On ajoute les kamas a la banque
						_perso.setKamas(_perso.getKamas() - kamas);//On retire les kamas du personnage
						GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
						GestorSalida.GAME_SEND_EsK_PACKET(_perso, "G" + _perso.getBankKamas());
					} else {
						kamas = -kamas;//On repasse en positif
						if (_perso.getBankKamas() < kamas) kamas = _perso.getBankKamas();
						_perso.setBankKamas(_perso.getBankKamas() - kamas);//On retire les kamas de la banque
						_perso.setKamas(_perso.getKamas() + kamas);//On ajoute les kamas du personnage
						GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
						GestorSalida.GAME_SEND_EsK_PACKET(_perso, "G" + _perso.getBankKamas());
					}
				}
//Objet
				case 'O' -> {
					int guid = 0;
					int qua = 0;
					try {
						guid = Integer.parseInt(packet.substring(4).split("\\|")[0]);
						qua = Integer.parseInt(packet.substring(4).split("\\|")[1]);
					} catch (Exception ignored) {
					}
					if (guid == 0 || qua <= 0) return;
					switch (packet.charAt(3)) {
//Ajouter a la banque
						case '+' -> _perso.addInBank(guid, qua);
//Retirer de la banque
						case '-' -> _perso.removeFromBank(guid, qua);
					}
				}
			}
			return;
		}
		//Coffre
	    if(_perso.getInTrunk() != null)
        {
                if(_perso.get_curExchange() != null)return;
                Cofres t = _perso.getInTrunk();
                if(t == null) return;

			switch (packet.charAt(2)) {
//Kamas
				case 'G' -> {
					long kamas = 0;
					try {
						kamas = Integer.parseInt(packet.substring(3));
					} catch (Exception ignored) {
					}
					if (kamas == 0) return;
					if (kamas > 0)//Si On ajoute des kamas au coffre
					{
						if (_perso.getKamas() < kamas) kamas = _perso.getKamas();
						t.set_kamas(t.get_kamas() + kamas);//On ajoute les kamas au coffre
						_perso.setKamas(_perso.getKamas() - kamas);//On retire les kamas du personnage
						GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
					} else // On retire des kamas au coffre
					{
						kamas = -kamas;//On repasse en positif
						if (t.get_kamas() < kamas) kamas = t.get_kamas();
						t.set_kamas(t.get_kamas() - kamas);//On retire les kamas de la banque
						_perso.setKamas(_perso.getKamas() + kamas);//On ajoute les kamas du personnage
						GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
					}
					for (Personaje P : Mundo.getOnlinePersos()) {
						if (P.getInTrunk() != null && _perso.getInTrunk().get_id() == P.getInTrunk().get_id()) {
							GestorSalida.GAME_SEND_EsK_PACKET(P, "G" + t.get_kamas());
						}
					}
					GestorSQL.actualizar_cofre(t);
				}
				//Objet
				case 'O' -> {
					int guid = 0;
					int qua = 0;
					try {
						guid = Integer.parseInt(packet.substring(4).split("\\|")[0]);
						qua = Integer.parseInt(packet.substring(4).split("\\|")[1]);
					} catch (Exception ignored) {
					}
					if (guid == 0 || qua <= 0) return;
					switch (packet.charAt(3)) {
						//Ajouter a la banque
						case '+' -> t.addInTrunk(guid, qua, _perso);
						//Retirer de la banque
						case '-' -> t.removeFromTrunk(guid, qua, _perso);
					}
				}
			}
                return;
        }
		if(_perso.get_curExchange() == null)return;
		switch(packet.charAt(2))
		{
			case 'O'://Objet ?
				if(packet.charAt(3) == '+')
				{
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						int quaInExch = _perso.get_curExchange().getQuaItem(guid, _perso.get_GUID());
						
						if(!_perso.hasItemGuid(guid))return;
						Objeto obj = Mundo.getObjet(guid);
						if(obj == null)return;
						
						if(qua > obj.getQuantity()-quaInExch)

							qua = obj.getQuantity()-quaInExch;
						if(qua <= 0)return;
						
						_perso.get_curExchange().addItem(guid,qua,_perso.get_GUID());
					}catch(NumberFormatException ignored){}
				}else
				{
					String[] infos = packet.substring(4).split("\\|");
					try
					{
						int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						
						if(qua <= 0)return;
						if(!_perso.hasItemGuid(guid))return;
						
						Objeto obj = Mundo.getObjet(guid);
						if(obj == null)return;
						if(qua > _perso.get_curExchange().getQuaItem(guid, _perso.get_GUID()))return;
						
						_perso.get_curExchange().removeItem(guid,qua,_perso.get_GUID());
					}catch(NumberFormatException ignored){}
				}
			break;
			case 'G'://Kamas
				try
				{
					long numb = Integer.parseInt(packet.substring(3));
					if(_perso.getKamas() < numb)
						numb = _perso.getKamas();
					_perso.get_curExchange().setKamas(_perso.get_GUID(), numb);
				}catch(NumberFormatException ignored){}
				break;
		}
	}

	private void Exchange_accept()
	{
		if(_perso.get_isTradingWith() == 0)return;
		Personaje target = Mundo.getPersonnage(_perso.get_isTradingWith());
		if(target == null)return;
		GestorSalida.GAME_SEND_EXCHANGE_CONFIRM_OK(_out,1);
		GestorSalida.GAME_SEND_EXCHANGE_CONFIRM_OK(target.getCuenta().getGameThread().get_out(),1);
		Mundo.Exchange echg = new Mundo.Exchange(target,_perso);
		_perso.setCurExchange(echg);
		_perso.set_isTradingWith(target.get_GUID());
		target.setCurExchange(echg);
		target.set_isTradingWith(_perso.get_GUID());
	}

	private void Exchange_onSellItem(String packet)
	{
		try
		{
			String[] infos = packet.substring(2).split("\\|");
			int guid = Integer.parseInt(infos[0]);
			int qua = Integer.parseInt(infos[1]);
			if(!_perso.hasItemGuid(guid))
			{
				GestorSalida.GAME_SEND_SELL_ERROR_PACKET(_out);
				return;
			}
			_perso.sellItem(guid, qua);
		}catch(Exception e)
		{
			GestorSalida.GAME_SEND_SELL_ERROR_PACKET(_out);
		}
	}

	private void Exchange_onBuyItem(String packet)
	{
		String[] infos = packet.substring(2).split("\\|");
		
        if (_perso.get_isTradingWith() > 0) 
        {
            Personaje seller = Mundo.getPersonnage(_perso.get_isTradingWith());
            if (seller != null) 
            {
            	int itemID = 0;
            	int qua = 0;
            	int price = 0;
            	try
        		{
            		itemID = Integer.valueOf(infos[0]);
            		qua = Integer.valueOf(infos[1]);
        		}catch(Exception e){return;}
        		
                if (!seller.getStoreItems().containsKey(itemID) || qua <= 0) 
                {
                    GestorSalida.GAME_SEND_BUY_ERROR_PACKET(_out);
                    return;
                }
                price = seller.getStoreItems().get(itemID);
                Objeto itemStore = Mundo.getObjet(itemID);
                if(itemStore == null) return;
                
                if(qua > itemStore.getQuantity()) qua = itemStore.getQuantity();
                if(qua == itemStore.getQuantity())
                {
                	seller.getStoreItems().remove(itemStore.getGuid());
                	_perso.addObjet(itemStore, true);
                }else // si l'ï¿½change peut se faire
                {
                	seller.getStoreItems().remove(itemStore.getGuid()); // on enlï¿½ve entiï¿½rement l'objet en vente
                	itemStore.setQuantity(itemStore.getQuantity()-qua); // on modifie la quantitï¿½ dans le magasin
                	GestorSQL.guardar_objeto(itemStore);					// on sauvegarde le magasin
                	seller.addStoreItem(itemStore.getGuid(), price);	// on remet dans le magasin
                	
                	Objeto clone = Objeto.getCloneObjet(itemStore, qua);	// on clone l'objet achetï¿½
                    GestorSQL.guardar_nuevo_objeto(clone);					// on sauvegarde celui-ci
                    _perso.addObjet(clone, true);						// et on le donne au joueur
                }
	            //remove kamas
	            _perso.addKamas(-price * qua);
	            //add seller kamas
	            seller.addKamas(price * qua);
	            GestorSQL.guardar_personaje(seller, true);
	            GestorSQL.guardar_personaje(this._perso, true);
	            //send packets
	            GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
	            GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller, _perso);
	            GestorSalida.GAME_SEND_BUY_OK_PACKET(_out);
	            if(seller.getStoreItems().isEmpty())
	            {
	            	if(Mundo.getSeller(seller.getActualMapa().getID()) != null && Mundo.getSeller(seller.getActualMapa().getID()).contains(seller.get_GUID()))
	        		{
	        			Mundo.removeSeller(seller.get_GUID(), seller.getActualMapa().getID());
	        			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(seller.getActualMapa(), seller.get_GUID());
	        			Exchange_finish_buy();
	        		}
	            }
            }
            return;
        }
        
		try
		{
			int tempID = Integer.parseInt(infos[0]);
			int qua = Integer.parseInt(infos[1]);
			
			if(qua <= 0) return;
			
			ObjTemplate template = Mundo.getObjTemplate(tempID);
			if(template == null)//Si l'objet demandï¿½ n'existe pas(ne devrait pas arrivï¿½)
			{
				JuegoServidor.agregar_a_los_logs(_perso.getNombre()+" tente d'acheter l'itemTemplate "+tempID+" qui est inexistant");
				GestorSalida.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			if(!_perso.getActualMapa().getNPC(_perso.get_isTradingWith()).getModelo().haveItem(tempID))//Si le PNJ ne vend pas l'objet voulue
			{
				JuegoServidor.agregar_a_los_logs(_perso.getNombre()+" tente d'acheter l'itemTemplate "+tempID+" que le present PNJ ne vend pas");
				GestorSalida.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			int prix = template.getPrix() * qua;
			if(_perso.getKamas()<prix)//Si le joueur n'a pas assez de kamas
			{
				JuegoServidor.agregar_a_los_logs(_perso.getNombre()+" tente d'acheter l'itemTemplate "+tempID+" mais n'a pas l'argent necessaire");
				GestorSalida.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			Objeto newObj = template.createNewItem(qua,false);
			long newKamas = _perso.getKamas() - prix;
			_perso.setKamas(newKamas);
			if(_perso.addObjet(newObj,true))//Return TRUE si c'est un nouvel item
				Mundo.addObjet(newObj,true);
			GestorSalida.GAME_SEND_BUY_OK_PACKET(_out);
			GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
			GestorSalida.GAME_SEND_Ow_PACKET(_perso);
		}catch(Exception e)
		{
			e.printStackTrace();
			GestorSalida.GAME_SEND_BUY_ERROR_PACKET(_out);
			return;
		}
	}

	private void Exchange_finish_buy()
	{
		if(_perso.get_isTradingWith() == 0 &&
		   _perso.get_curExchange() == null &&
		   _perso.getCurJobAction() == null &&
		   _perso.getInMountPark() == null &&
		   !_perso.isInBank() &&
		   _perso.get_isOnPercepteurID() == 0 &&
		   _perso.getInTrunk() == null)
			return;
		
		//Si ï¿½change avec un personnage
		if(_perso.get_curExchange() != null)
		{
			_perso.get_curExchange().cancel();
			_perso.set_isTradingWith(0);
			_perso.set_away(false);
			return;
		}
		//Si mï¿½tier
		if(_perso.getCurJobAction() != null)
		{
			_perso.getCurJobAction().resetCraft();
		}
		//Si dans un enclos
		if(_perso.getInMountPark() != null)_perso.leftMountPark();
		//prop d'echange avec un joueur
		if(_perso.get_isTradingWith() > 0)
		{
			Personaje p = Mundo.getPersonnage(_perso.get_isTradingWith());
			if(p != null)
			{
				if(p.isConectado())
				{
					PrintWriter out = p.getCuenta().getGameThread().get_out();
					GestorSalida.GAME_SEND_EV_PACKET(out);
					p.set_isTradingWith(0);
				}
			}
		}
		//Si perco
		if(_perso.get_isOnPercepteurID() != 0)
		{
			Recaudador perco = Mundo.getPerco(_perso.get_isOnPercepteurID());
			if(perco == null) return;
			for(Personaje z : Mundo.getGuild(perco.get_guildID()).getMembers())
			{
				if(z.isConectado())
				{
					GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parsetoGuild(z.get_guild().get_id()));
					String str = "";
					str += "G"+perco.get_N1()+","+perco.get_N2();
					str += "|.|"+ Mundo.getCarte(perco.get_mapID()).getX()+"|"+ Mundo.getCarte(perco.get_mapID()).getY()+"|";
					str += _perso.getNombre()+"|";
					str += perco.get_LogXp();
					str += perco.get_LogItems();
					GestorSalida.GAME_SEND_gT_PACKET(z, str);
				}
			}
			_perso.getActualMapa().RemoveNPC(perco.getGuid());
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_perso.getActualMapa(), perco.getGuid());
			perco.DelPerco(perco.getGuid());
			GestorSQL.eliminar_recaudador(perco.getGuid());
			_perso.set_isOnPercepteurID(0);
		}
		
		GestorSQL.guardar_personaje(_perso,true);
		GestorSalida.GAME_SEND_EV_PACKET(_out);
		_perso.set_isTradingWith(0);
		_perso.set_away(false);
		_perso.setInBank(false);
		_perso.setInTrunk(null);
	}

	private void Exchange_start(String packet)
	{
		if(packet.substring(2,4).equals("11"))//Ouverture HDV achat
		{
			if(_perso.get_isTradingWith() < 0)//Si dï¿½jï¿½ ouvert
				GestorSalida.GAME_SEND_EV_PACKET(_out);
			
			if(_perso.getDeshonor() >= 5) 
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "183");
				return;
			}
			
			Mercadillo toOpen = Mundo.getHdv(_perso.getActualMapa().getID());
			
			if(toOpen == null) return;
			
			String info = "1,10,100;"+
						toOpen.getStrCategories()+
						";"+toOpen.parseTaxe()+
						";"+toOpen.getLvlMax()+
						";"+toOpen.getMaxItemCompte()+
						";-1;"+
						toOpen.getSellTime();
			GestorSalida.GAME_SEND_ECK_PACKET(_perso,11,info);
			_perso.set_isTradingWith(0 - _perso.getActualMapa().getID());	//Rï¿½cupï¿½re l'ID de la map et rend cette valeur nï¿½gative
			return;
		}
		else if(packet.substring(2,4).equals("10"))//Ouverture HDV vente
		{
			if(_perso.get_isTradingWith() < 0)//Si dï¿½jï¿½ ouvert
				GestorSalida.GAME_SEND_EV_PACKET(_out);
			
			if(_perso.getDeshonor() >= 5) 
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "183");
				return;
			}
			
			Mercadillo toOpen = Mundo.getHdv(_perso.getActualMapa().getID());
			
			if(toOpen == null) return;
			
			String info = "1,10,100;"+
						toOpen.getStrCategories()+
						";"+toOpen.parseTaxe()+
						";"+toOpen.getLvlMax()+
						";"+toOpen.getMaxItemCompte()+
						";-1;"+
						toOpen.getSellTime();
			GestorSalida.GAME_SEND_ECK_PACKET(_perso,10,info);
			_perso.set_isTradingWith(0 - _perso.getActualMapa().getID());	//Rï¿½cupï¿½re l'ID de la map et rend cette valeur nï¿½gative
			
			GestorSalida.GAME_SEND_HDVITEM_SELLING(_perso);
			return;
		}
		switch(packet.charAt(2))
		{
			case '0'://Si NPC
				try
				{
					int npcID = Integer.parseInt(packet.substring(4));
					NPCModelo.NPC npc = _perso.getActualMapa().getNPC(npcID);
					if(npc == null)return;
					GestorSalida.GAME_SEND_ECK_PACKET(_out, 0, npcID+"");
					GestorSalida.GAME_SEND_ITEM_VENDOR_LIST_PACKET(_out,npc);
					_perso.set_isTradingWith(npcID);
				}catch(NumberFormatException ignored){}
				break;
			case '1'://Si joueur
				try
				{
				int guidTarget = Integer.parseInt(packet.substring(4));
				Personaje target = Mundo.getPersonnage(guidTarget);
				if(target == null )
				{
					GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'E');
					return;
				}
				if(target.getActualMapa()!= _perso.getActualMapa() || !target.isConectado())//Si les persos ne sont pas sur la meme map
				{
					GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'E');
					return;
				}
				if(target.is_away() || _perso.is_away() || target.get_isTradingWith() != 0)
				{
					GestorSalida.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'O');
					return;
				}
				GestorSalida.GAME_SEND_EXCHANGE_REQUEST_OK(_out, _perso.get_GUID(), guidTarget,1);
				GestorSalida.GAME_SEND_EXCHANGE_REQUEST_OK(target.getCuenta().getGameThread().get_out(),_perso.get_GUID(), guidTarget,1);
				_perso.set_isTradingWith(guidTarget);
				target.set_isTradingWith(_perso.get_GUID());
			}catch(NumberFormatException ignored){}
			break;
            case '4'://StorePlayer
            	int pID = 0;
            	//int cellID = 0;//Inutile
            	try
				{
            		pID = Integer.valueOf(packet.split("\\|")[1]);
            		//cellID = Integer.valueOf(packet.split("\\|")[2]);
				}catch(NumberFormatException e){return;}
				if(_perso.get_isTradingWith() > 0 || _perso.getPelea() != null || _perso.is_away())return;
				Personaje seller = Mundo.getPersonnage(pID);
				if(seller == null) return;
				_perso.set_isTradingWith(pID);
				GestorSalida.GAME_SEND_ECK_PACKET(_perso, 4, seller.get_GUID()+"");
				GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller, _perso);
            break;
			case '6'://StoreItems
				if(_perso.get_isTradingWith() > 0 || _perso.getPelea() != null || _perso.is_away())return;
                _perso.set_isTradingWith(_perso.get_GUID());
                GestorSalida.GAME_SEND_ECK_PACKET(_perso, 6, "");
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(_perso, _perso);
			break;
			case '8'://Si Percepteur
				try
				{
					int PercepteurID = Integer.parseInt(packet.substring(4));
					Recaudador perco = Mundo.getPerco(PercepteurID);
					if(perco == null || perco.get_inFight() > 0 || perco.get_Exchange())return;
					perco.set_Exchange(true);
					GestorSalida.GAME_SEND_ECK_PACKET(_out, 8, perco.getGuid()+"");
					GestorSalida.GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(_out, perco);
					_perso.set_isTradingWith(perco.getGuid());
					_perso.set_isOnPercepteurID(perco.getGuid());
				}catch(NumberFormatException ignored){}
				break;
		}
	}

	private void parse_environementPacket(String packet)
	{
		switch (packet.charAt(1)) {
//Change direction
			case 'D' -> Environement_change_direction(packet);
//Emote
			case 'U' -> Environement_emote(packet);
		}
	}

	private void Environement_emote(String packet)
	{
		int emote = -1;
		try
		{
			emote = Integer.parseInt(packet.substring(2));
		}catch(Exception ignored){}
		if(emote == -1)return;
		if(_perso == null)return;
		if(_perso.getPelea() != null)return;//Pas d'ï¿½mote en combat

		//effets spï¿½ciaux des ï¿½motes
		switch (emote) {
// s'asseoir
			case 19, 1 -> _perso.setSitted(!_perso.isSitted());
		}
		if(_perso.emoteActive() == emote)_perso.setEmoteActive(0);
		else _perso.setEmoteActive(emote);
		
		System.out.println("Set Emote "+_perso.emoteActive());
		System.out.println("Is sitted "+_perso.isSitted());
		
		GestorSalida.GAME_SEND_eUK_PACKET_TO_MAP(_perso.getActualMapa(), _perso.get_GUID(), _perso.emoteActive());
	}

	private void Environement_change_direction(String packet)
	{
		try
		{
			if(_perso.getPelea() != null)return;
			int dir = Integer.parseInt(packet.substring(2));
			_perso.set_orientation(dir);
			GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(_perso.getActualMapa(),_perso.get_GUID(),dir);
		}catch(NumberFormatException e){return;}
	}

	private void parseSpellPacket(String packet)
	{
		switch (packet.charAt(1)) {
			case 'B' -> boostSort(packet);
//Oublie de sort
			case 'F' -> forgetSpell(packet);
			case 'M' -> addToSpellBook(packet);
		}
	}

	private void addToSpellBook(String packet)
	{
		try
		{
			int SpellID = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			int Position = Integer.parseInt(packet.substring(2).split("\\|")[1]);
			SortStats Spell = _perso.getSortStatBySortIfHas(SpellID);
			
			if(Spell != null)
			{
				_perso.set_SpellPlace(SpellID, GestorEncriptador.getHashedValueByInt(Position));
			}
				
			GestorSalida.GAME_SEND_BN(_out);
		}catch(Exception ignored){}
	}

	private void boostSort(String packet)
	{
		try
		{
			int id = Integer.parseInt(packet.substring(2));
			JuegoServidor.agregar_a_los_logs("Info: "+_perso.getNombre()+": Tente BOOST sort id="+id);
			if(_perso.boostSpell(id))
			{
				JuegoServidor.agregar_a_los_logs("Info: "+_perso.getNombre()+": OK pour BOOST sort id="+id);
				GestorSalida.GAME_SEND_SPELL_UPGRADE_SUCCED(_out, id, _perso.getSortStatBySortIfHas(id).getLevel());
				GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
			}else
			{
				JuegoServidor.agregar_a_los_logs("Info: "+_perso.getNombre()+": Echec BOOST sort id="+id);
				GestorSalida.GAME_SEND_SPELL_UPGRADE_FAILED(_out);
				return;
			}
		}catch(NumberFormatException e){
			GestorSalida.GAME_SEND_SPELL_UPGRADE_FAILED(_out);return;}
	}

	private void forgetSpell(String packet)
	{
		if(!_perso.isForgetingSpell())return;
		
		int id = Integer.parseInt(packet.substring(2));
		
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Info: "+_perso.getNombre()+": Tente Oublie sort id="+id);
		
		if(_perso.forgetSpell(id))
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Info: "+_perso.getNombre()+": OK pour Oublie sort id="+id);
			GestorSalida.GAME_SEND_SPELL_UPGRADE_SUCCED(_out, id, _perso.getSortStatBySortIfHas(id).getLevel());
			GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
			_perso.setisForgetingSpell(false);
		}
	}

	private void parseFightPacket(String packet)
	{
		try
		{
			switch (packet.charAt(1)) {
//Dï¿½tails d'un combat (liste des combats)
				case 'D' -> {
					int key = -1;
					try {
						key = Integer.parseInt(packet.substring(2).replace(((int) 0x0) + "", ""));
					} catch (Exception ignored) {
					}
					if (key == -1) return;
					GestorSalida.GAME_SEND_FIGHT_DETAILS(_out, _perso.getActualMapa().get_fights().get(key));
				}
//Aide
				case 'H' -> {
					if (_perso.getPelea() == null) return;
					_perso.getPelea().toggleHelp(_perso.get_GUID());
				}
//Lister les combats
				case 'L' -> GestorSalida.GAME_SEND_FIGHT_LIST_PACKET(_out, _perso.getActualMapa());
//Bloquer le combat
				case 'N' -> {
					if (_perso.getPelea() == null) return;
					_perso.getPelea().toggleLockTeam(_perso.get_GUID());
				}
//Seulement le groupe
				case 'P' -> {
					if (_perso.getPelea() == null || _perso.getActualGrupo() == null) return;
					_perso.getPelea().toggleOnlyGroup(_perso.get_GUID());
				}
//Bloquer les specs
				case 'S' -> {
					if (_perso.getPelea() == null) return;
					_perso.getPelea().toggleLockSpec(_perso.get_GUID());
				}
			}
		}catch(Exception e){e.printStackTrace();}
	}

	private void parseBasicsPacket(String packet)
	{
		switch (packet.charAt(1)) {
//Console
			case 'A' -> Basic_console(packet);
			case 'D' -> Basic_send_Date_Hour();
			case 'M' -> Basic_chatMessage(packet);
			case 'W' -> Basic_infosmessage(packet);
			case 'S' -> _perso.emoticone(packet.substring(2));
			case 'Y' -> Basic_state(packet);
		}
	}
	public void Basic_state(String packet)
	{
		switch(packet.charAt(2))
		{
			case 'A': //Absent
				if(_perso._isAbsent)
				{

					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "038");

					_perso._isAbsent = false;
				}
				else

				{
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "037");
					_perso._isAbsent = true;
				}
			break;
			case 'I': //Invisible
				if(_perso._isInvisible)
				{
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "051");
					_perso._isInvisible = false;
				}
				else
				{
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "050");
					_perso._isInvisible = true;
				}
			break;
		}
	}
	
	public Personaje getPerso()
	{
		return _perso;
	}
	  
	private void Basic_console(String packet)
	{
		if(command == null) command = new Comandos(_perso);
		command.consoleCommand(packet);
	}

	public void closeSocket()
	{
		try {
			this._s.close();
		} catch (IOException ignored) {}
	}

	private void Basic_chatMessage(String packet) {
		String msg = "";
		if(_perso.isMuted()) {
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1124;"+_perso.getCuenta()._muteTimer.getInitialDelay());//FIXME
			return;
		}
		packet = packet.replace("<", "");
		packet = packet.replace(">", "");
		if(packet.length() == 3)return;
		switch(packet.charAt(2)) {
			case '*'://Canal noir
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				msg = packet.split("\\|",2)[1];
				
				//Comandos de los jugadores
				if(msg.charAt(0) == '.') {
					if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("start")) {
						if(_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES)return;
						_perso.warpToSavePos();
						return;
					
							
					}else if(msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("vie")) {
							if (_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES) {
								String str = "Tu ne peux pa utiliser cette commande !";
							GestorSalida.GAME_SEND_MESSAGE(_perso, str, MainServidor.CONFIG_MOTD_COLOR);
							}else {
								int count = 100;
								Personaje perso = _perso;
								int newPDV = (perso.get_PDVMAX() * count) / 100;
								perso.set_PDV(newPDV);
								if(perso.isConectado()) {
								GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
								}
							return;
							}
					}else if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("astrub")) {
						if (_perso.getPelea() != null|| !MainServidor.PERMITIR_COMANDOS_JUGADORES) {
							GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu ne peux pas utiliser cette commande !", MainServidor.CONFIG_MOTD_COLOR);
						}else {
							_perso.teletransportar((short)7411, 369);
							}
						}
					if (msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("shop")) {
						if(_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES) {
							GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu ne peux pas utiliser cette commande !", MainServidor.CONFIG_MOTD_COLOR);
							return;
						}else
						_perso.teletransportar(MainServidor.CONFIG_MAP_SHOP, MainServidor.CONFIG_CELL_SHOP);
						return;
					}else if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("enclos")) {
					    	if (_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES) {
					    GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu ne peux pas utiliser cette commande !", MainServidor.CONFIG_MOTD_COLOR);
					    		return;
					    	}else
					    		_perso.teletransportar(MainServidor.CONFIG_ENCLOS_MAP, MainServidor.CONFIG_CELL_ENCLOS);
					    	return;
					    }else if(msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("pvm")) {
						if (_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES)
						{
						GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu ne peux pas utiliser cette commande !", MainServidor.CONFIG_MOTD_COLOR);
						return;
						}else
						_perso.teletransportar(MainServidor.CONFIG_MAP_PVM, MainServidor.CONFIG_CELL_PVM);
						return;
					}
					if(msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("pvp"))
					{
						if (_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES)
						{
						GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu es en combat ou la commande est désactivée !", MainServidor.CONFIG_MOTD_COLOR);
						return;
						}else
						_perso.teletransportar(MainServidor.CONFIG_MAP_PVP, MainServidor.CONFIG_CELL_PVP);
						return;
					}
						 if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("staff")) // Commande .staffonline
						 	{
						        StringBuilder staff = new StringBuilder("Membres du staff connectés :\n");
						        boolean allOffline = true;
						                                                    
						        for(int i = 0; i < Mundo.getOnlinePersos().size(); i++) {
						            if(Mundo.getOnlinePersos().get(i).getCuenta().getGMLVL() > 0) {
						                staff.append("- ").append(Mundo.getOnlinePersos().get(i).getNombre()).append(" (");
						    
						                if(Mundo.getOnlinePersos().get(i).getCuenta().getGMLVL() == 1)
						                    staff.append("Animateur)");
						                else if(Mundo.getOnlinePersos().get(i).getCuenta().getGMLVL() == 2)
						                    staff.append("Modérateur)");
						                else if(Mundo.getOnlinePersos().get(i).getCuenta().getGMLVL() == 3)
						                    staff.append("MJ)");
						                else if(Mundo.getOnlinePersos().get(i).getCuenta().getGMLVL() == 4)
						                    staff.append("Administrateur)");
						                else if(Mundo.getOnlinePersos().get(i).getCuenta().getGMLVL() == 5)
						                    staff.append("Créateur)");
						                else
						                    staff.append("Unknown");
						                                                            
						                staff.append("\n");
						                                                                    
						                allOffline = false;
						            }
						        }
						        if((staff.length() > 0) && !allOffline) {
						            GestorSalida.GAME_SEND_MESSAGE(_perso, staff.toString(), MainServidor.CONFIG_MOTD_COLOR);
						        } else if (allOffline) {
						            GestorSalida.GAME_SEND_MESSAGE(_perso, "Aucun membre du staff est présent !", MainServidor.CONFIG_MOTD_COLOR);
						        }
						        return;
						     					    }
						if(msg.length() > 9 && msg.substring(1, 10).equalsIgnoreCase("bontarien")) //Commande bontarien
					if (_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES) {
						GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu es en combat ou la commande est désactivée !", MainServidor.CONFIG_MOTD_COLOR);
					}else {
						byte align = 1;
						Personaje target = _perso;
						target.modifAlignement(align);
						if(target.isConectado())
						GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(target);
						GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu es désormais Bontarien", MainServidor.CONFIG_MOTD_COLOR);
						return;
				}else if(msg.length() > 10 && msg.substring(1, 11).equalsIgnoreCase("brakmarien")) //Commande Brakmarien
						if (_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES) {
							GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu es en combat ou la commande est désactivée !", MainServidor.CONFIG_MOTD_COLOR);
						}else
					{
					byte align = 2;
					Personaje target = _perso;
					target.modifAlignement(align);
					if(target.isConectado())
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(target);
					GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu es désormais Brakmarien", MainServidor.CONFIG_MOTD_COLOR);
					return;
					}

					if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("neutre")) //Commande neutre
						if (_perso.getPelea() != null || !MainServidor.PERMITIR_COMANDOS_JUGADORES) {
							GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu es en combat ou la commande est désactivée !", MainServidor.CONFIG_MOTD_COLOR);
						}else {
					byte align = 0;
					Personaje target = _perso;
					target.modifAlignement(align);
					if(target.isConectado())
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(target);
					GestorSalida.GAME_SEND_MESSAGE(_perso, "Tu es désormais Neutre", MainServidor.CONFIG_MOTD_COLOR);
					return;
					}
						if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("fmcac")) //Adlesne Commande
						{
	                            Objeto obj = _perso.getObjetByPos(Constantes.ITEM_POS_ARME);
	                            
	                            if(_perso.getKamas() < 500000) {
	                                    GestorSalida.GAME_SEND_MESSAGE(_perso,  "Action impossible : vous avez moins de 500.000 k", MainServidor.CONFIG_MOTD_COLOR);
	                                    return;
	                            }else if(_perso.getPelea() != null) {
	                                    GestorSalida.GAME_SEND_MESSAGE(_perso,  "Action impossible : vous ne devez pas être en combat", MainServidor.CONFIG_MOTD_COLOR);
	                                    return;
	                            }else if(obj == null) {
	                                    GestorSalida.GAME_SEND_MESSAGE(_perso,  "Action impossible : vous ne portez pas d'arme", MainServidor.CONFIG_MOTD_COLOR);
	                                    return;
	                            }
	                            
	                            boolean containNeutre = false;
	                            for(EfectoHechizo effect :  obj.getEffects()) {
									if (effect.getEffectID() == 100 || effect.getEffectID() == 95) {
										containNeutre = true;
										break;
									}
	                            }
	                            if(!containNeutre)
	                            {
	                                    GestorSalida.GAME_SEND_MESSAGE(_perso,  "Action impossible : votre arme n'a pas de dégats neutre", MainServidor.CONFIG_MOTD_COLOR);
	                                    return;
	                            }
	                            
	                            String answer;
	                            
	                            try
	                            {
	                                answer = msg.substring(7, msg.length() - 1);
	                            }
	                            catch(Exception e)
	                            {
	                                    GestorSalida.GAME_SEND_MESSAGE(_perso,  "Action impossible : vous n'avez pas spécifié l'élément (air, feu, terre, eau) qui remplacera les dégats/vols de vies neutres", MainServidor.CONFIG_MOTD_COLOR);
	                                    return;
	                            }
	                            
	                            if(!answer.equalsIgnoreCase("air") && !answer.equalsIgnoreCase("terre") && !answer.equalsIgnoreCase("feu") && !answer.equalsIgnoreCase("eau"))
	                            {
	                                    GestorSalida.GAME_SEND_MESSAGE(_perso,  "Action impossible : l'élément " + answer + " n'existe pas ! (dispo : air, feu, terre, eau)", MainServidor.CONFIG_MOTD_COLOR);
	                                    return;
	                            }
	                            
	                            for(int i = 0; i < obj.getEffects().size(); i++)
	                            {
	                                    if(obj.getEffects().get(i).getEffectID() == 100)
	                                    {
	                                            if(answer.equalsIgnoreCase("air"))
	                                            {
	                                                    obj.getEffects().get(i).setEffectID(98);
	                                            }
	                                            if(answer.equalsIgnoreCase("feu"))
	                                            {
	                                                    obj.getEffects().get(i).setEffectID(99);
	                                            }
	                                            if(answer.equalsIgnoreCase("terre"))
	                                            {
	                                                    obj.getEffects().get(i).setEffectID(97);
	                                            }
	                                            if(answer.equalsIgnoreCase("eau"))
	                                            {
	                                                    obj.getEffects().get(i).setEffectID(96);
	                                            }
	                                    }
	                                    
	                                    if(obj.getEffects().get(i).getEffectID() == 95)
	                                    {
	                                            if(answer.equalsIgnoreCase("air"))
	                                            {
	                                                    obj.getEffects().get(i).setEffectID(93);
	                                            }
	                                            if(answer.equalsIgnoreCase("feu"))
	                                            {
	                                                    obj.getEffects().get(i).setEffectID(94);
	                                            }
	                                            if(answer.equalsIgnoreCase("terre"))
	                                            {
	                                                    obj.getEffects().get(i).setEffectID(92);
	                                            }
	                                            if(answer.equalsIgnoreCase("eau"))
	                                            {
	                                                    obj.getEffects().get(i).setEffectID(91);
	                                            }
	                                    }
	                            }
	                            
	                            long new_kamas = _perso.getKamas() - 500000;
	                            if(new_kamas < 0) //Ne devrait pas arriver...
	                                    new_kamas = 0;
	                            _perso.setKamas(new_kamas);
	                            
	                            GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
	                            
	                            GestorSalida.GAME_SEND_MESSAGE(_perso,  "Votre objet : " + obj.getTemplate().getName() + " a été FM avec succès en " + answer, MainServidor.CONFIG_MOTD_COLOR);
	                            GestorSalida.GAME_SEND_MESSAGE(_perso,  " Penser à vous deco/reco pour voir les changement !", MainServidor.CONFIG_MOTD_COLOR);
	                            return;
	                    }
						
						else
						if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("parcho"))
					      {
					        if(_perso.getPelea() != null)
					            return;  
					                                              
					        String element = "";
					        int nbreElement = 0;
					        if(_perso.get_baseStats().getEffect(125) < 101)
					        {
					            _perso.get_baseStats().addOneStat(125, 101 - _perso.get_baseStats().getEffect(125));
					            element += "vitalité";
					            nbreElement++;
					        }

					        if(_perso.get_baseStats().getEffect(124) < 101)
					        {
					            _perso.get_baseStats().addOneStat(124, 101 - _perso.get_baseStats().getEffect(124));
					        if(nbreElement == 0)
					            element += "sagesse";
					        else
					            element += ", sagesse";
					        nbreElement++;
					        }

					        if(_perso.get_baseStats().getEffect(118) < 101)
					        {
					            _perso.get_baseStats().addOneStat(118, 101 - _perso.get_baseStats().getEffect(118));
					        if(nbreElement == 0)
					            element += "force";
					        else
					            element += ", force";
					            nbreElement++;
					        }

					        if(_perso.get_baseStats().getEffect(126) < 101)
					        {
					            _perso.get_baseStats().addOneStat(126, 101 - _perso.get_baseStats().getEffect(126));
					        if(nbreElement == 0)
					            element += "intelligence";
					        else
					            element += ", intelligence";
					            nbreElement++;
					        }

					        if(_perso.get_baseStats().getEffect(119) < 101)
					        {
					            _perso.get_baseStats().addOneStat(119, 101 - _perso.get_baseStats().getEffect(119));
					        if(nbreElement == 0)
					            element += "agilité";
					        else
					            element += ", agilité";
					            nbreElement++;
					        }

					        if(_perso.get_baseStats().getEffect(123) < 101)
					        {
					            _perso.get_baseStats().addOneStat(123, 101 - _perso.get_baseStats().getEffect(123));
					        if(nbreElement == 0)
					            element += "chance";
					        else
					            element += ", chance";
					            nbreElement++;
					        }
					                                                
					        if(nbreElement == 0)
					        {
					            GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "116;<i>System</i>~Vous avez déjà plus de 100 partout !");
					        }
					        else
					        {
					            GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
					            GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "116;<i>System</i>~Vous êtes parcho 101 en " + element + " !");
					        }
					        return;
					      }else if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("vote")) {
                        GestorSalida.GAME_SEND_MESSAGE(_perso, "<b><a href=\""+ MainServidor.CONFIG_URLVOTE+"\">Votez pour nous en cliquant sur ce lien !</a></b>", MainServidor.CONFIG_PUB_COLOR);
                        return;
                    } else if(msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("infos"))
                    {
                    	
                        long uptime = System.currentTimeMillis() - MainServidor.gameServer.getStartTime();
                        int jour = (int) (uptime/(1000*3600*24));
                        uptime %= (1000*3600*24);
                        int hour = (int) (uptime/(1000*3600));
                        uptime %= (1000*3600);
                        int min = (int) (uptime/(1000*60));
                        uptime %= (1000*60);
                        int sec = (int) (uptime/(1000));

                        String mess =	"===========\n"+ MainServidor.makeHeader()
                            +			"Tiempo online: "+jour+"j "+hour+"h "+min+"m "+sec+"s\n"
                            +			"Jugadores en linea: "+ MainServidor.gameServer.getPlayerNumber()+"\n"
                            +			"Maximos conectados: "+ MainServidor.gameServer.getMaxPlayer()+"\n"
                            +			"===========";
                        GestorSalida.GAME_SEND_MESSAGE(_perso, mess, MainServidor.CONFIG_MOTD_COLOR);
                        return;
                    }else if(msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("help")){
                        GestorSalida.GAME_SEND_MESSAGE(_perso, "Comandos disponibles : \n<b>.start</b> - Téléporte  a la map de départ\n<b>.fmcac</b> - FM le CAC equipé selon votre choix. (ex: .fmcac air)\n<b>.shop</b> - Téléporte a la map shop\n<b>.pvp</b> - Téléporte a la map pvp\n<b>.enclos</b> - Téléporte a l'enclos\n<b>.vie</b> - Régénère votre vie.\n<b>.parcho</b> - Vous parchotte 101 partout.\n<b>.astrub</b> - Vous téléporte à Astrub\n<b>.pvm</b> - Vous téléporte a la map PVM\n<b>.staff</b> - Fournis les membres du staff en ligne\n<b>.bontarien/brakmarien/neutre - Change votre alignement\n<b>.help</b> - Affiche ce message\n", MainServidor.CONFIG_PUB_COLOR);
                        return;
                    }
				}
				if(_perso.getPelea() == null)
					GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(_perso.getActualMapa(), "", _perso.get_GUID(), _perso.getNombre(), msg);
				else
					GestorSalida.GAME_SEND_cMK_PACKET_TO_FIGHT(_perso.getPelea(), 7, "", _perso.get_GUID(), _perso.getNombre(), msg);
			break;
			case '#'://Canal Equipe
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				if(_perso.getPelea() != null)
				{
					msg = packet.split("\\|",2)[1];
					int team = _perso.getPelea().getTeamID(_perso.get_GUID());
					if(team == -1)return;
					GestorSalida.GAME_SEND_cMK_PACKET_TO_FIGHT(_perso.getPelea(), team, "#", _perso.get_GUID(), _perso.getNombre(), msg);
				}
			break;
			case '$'://Canal groupe
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				if(_perso.getActualGrupo() == null)break;
				msg = packet.split("\\|",2)[1];
				GestorSalida.GAME_SEND_cMK_PACKET_TO_GROUP(_perso.getActualGrupo(), "$", _perso.get_GUID(), _perso.getNombre(), msg);
			break;
			
			case ':'://Canal commerce
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				long l;
				if((l = System.currentTimeMillis() - _timeLastTradeMsg) < MainServidor.FLOOD_TIME)
				{
					l = (MainServidor.FLOOD_TIME  - l)/1000;//On calcul la diffï¿½rence en secondes
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "0115;"+((int)Math.ceil(l)+1));
					return;
				}
				_timeLastTradeMsg = System.currentTimeMillis();
				msg = packet.split("\\|",2)[1];
				GestorSalida.GAME_SEND_cMK_PACKET_TO_ALL(":", _perso.get_GUID(), _perso.getNombre(), msg);
			break;
			case '@'://Canal Admin
				if(_perso.getCuenta().getGMLVL() ==0)return;
				msg = packet.split("\\|",2)[1];
				GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", _perso.get_GUID(), _perso.getNombre(), msg);
			break;
			case '?'://Canal recrutement
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				long j;
				if((j = System.currentTimeMillis() - _timeLastRecrutmentMsg) < MainServidor.FLOOD_TIME)
				{
					j = (MainServidor.FLOOD_TIME  - j)/1000;//On calcul la diffï¿½rence en secondes
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "0115;"+((int)Math.ceil(j)+1));
					return;
				}
				_timeLastRecrutmentMsg = System.currentTimeMillis();
				msg = packet.split("\\|",2)[1];
				GestorSalida.GAME_SEND_cMK_PACKET_TO_ALL("?", _perso.get_GUID(), _perso.getNombre(), msg);
			break;
			case '%'://Canal guilde
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				if(_perso.get_guild() == null)return;
				msg = packet.split("\\|",2)[1];
				GestorSalida.GAME_SEND_cMK_PACKET_TO_GUILD(_perso.get_guild(), "%", _perso.get_GUID(), _perso.getNombre(), msg);
			break;
			case 0xC2://Canal 
			break;
			case '!'://Alignement
				if(!_perso.get_canaux().contains(packet.charAt(2)+""))return;
				if(_perso.get_align() == 0) return;
				if(_perso.getDeshonor() >= 1) 
				{
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "183");
					return;
				}
				long k;
				if((k = System.currentTimeMillis() - _timeLastAlignMsg) < MainServidor.FLOOD_TIME)
				{
					k = (MainServidor.FLOOD_TIME  - k)/1000;//On calcul la diffï¿½rence en secondes
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "0115;"+((int)Math.ceil(k)+1));
					return;
				}
				_timeLastAlignMsg = System.currentTimeMillis();
				msg = packet.split("\\|",2)[1];
				GestorSalida.GAME_SEND_cMK_PACKET_TO_ALIGN("!", _perso.get_GUID(), _perso.getNombre(), msg, _perso);
			break;
			case '^':// Canal Incarnam 
				msg = packet.split("\\|", 2)[1]; 
				long x; 
				if((x = System.currentTimeMillis() - _timeLastIncarnamMsg) < MainServidor.FLOOD_TIME)
				{
					x = (MainServidor.FLOOD_TIME - x)/1000;//Calculamos a diferenï¿½a em segundos
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "0115;"+((int)Math.ceil(x)+1));
					return; 
				} 
				_timeLastIncarnamMsg = System.currentTimeMillis();
				msg = packet.split("\\|",2)[1];
				GestorSalida.GAME_SEND_cMK_PACKET_INCARNAM_CHAT(_perso, "^", _perso.get_GUID(), _perso.getNombre(), msg);
			break;
			default:
				String nom = packet.substring(2).split("\\|")[0];
				msg = packet.split("\\|",2)[1];
				if(nom.length() <= 1)
					JuegoServidor.agregar_a_los_logs("ChatHandler: Chanel non gere : "+nom);
				else
				{
					Personaje target = Mundo.getPersonajePorNombre(nom);
					if(target == null)//si le personnage n'existe pas
					{
						GestorSalida.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(target.getCuenta() == null)
					{
						GestorSalida.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(target.getCuenta().getGameThread() == null)//si le perso n'est pas co
					{
						GestorSalida.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(target.getCuenta().isEnemyWith(_perso.getCuenta().get_GUID()) == true || !target.isDispo(_perso))
					{
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "114;"+target.getNombre());
						return;
					}
					GestorSalida.GAME_SEND_cMK_PACKET(target, "F", _perso.get_GUID(), _perso.getNombre(), msg);
					GestorSalida.GAME_SEND_cMK_PACKET(_perso, "T", target.get_GUID(), target.getNombre(), msg);
				}
			break;
		}
	}

	private void Basic_send_Date_Hour()
	{
		GestorSalida.GAME_SEND_SERVER_DATE(_out);
		GestorSalida.GAME_SEND_SERVER_HOUR(_out);
	}
	
	private void Basic_infosmessage(String packet)
	{
			packet = packet.substring(2);
			Personaje T = Mundo.getPersonajePorNombre(packet);
			if(T == null) return;
			GestorSalida.GAME_SEND_BWK(_perso, T.getCuenta().get_pseudo()+"|1|"+T.getNombre()+"|-1");
	}

	private void parseGamePacket(String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A':
				if(_perso == null)
					return;
				parseGameActionPacket(packet);
			break;
			case 'C':
				if(_perso == null)
					return;
				_perso.sendGameCreate();
			break;
			case 'd': // demande de reciblage challenge
				Game_on_Gdi_packet(packet);
			case 'f':
				Game_on_showCase(packet);
			break;
			case 'I':
				Game_on_GI_packet();
			break;
			case 'K':
				Game_on_GK_packet(packet);
			break;
			case 'P'://PvP Toogle
				_perso.toggleWings(packet.charAt(2));
			break;
			case 'p':
				Game_on_ChangePlace_packet(packet);
			break;
			case 'Q':
				Game_onLeftFight(packet);
			break;
			case 'R':
				Game_on_Ready(packet);
			break;
			case 't':
				if(_perso.getPelea() == null)return;
				_perso.getPelea().playerPass(_perso);
			break;
		}
	}

	
	private void Game_onLeftFight(String packet)
	{
		int targetID = -1;
		if(!packet.substring(2).isEmpty())
		{
			try
			{
				targetID = Integer.parseInt(packet.substring(2));
			}catch(Exception ignored){}
		}
		if(_perso.getPelea() == null)return;
		if(targetID > 0)//Expulsion d'un joueurs autre que soi-meme
		{
			Personaje target = Mundo.getPersonnage(targetID);
			//On ne quitte pas un joueur qui : est null, ne combat pas, n'est pas de ï¿½a team.
			if(target == null || target.getPelea() == null || target.getPelea().getTeamID(target.get_GUID()) != _perso.getPelea().getTeamID(_perso.get_GUID()))return;
			_perso.getPelea().leftFight(_perso, target);
			
		}else
		{
			_perso.getPelea().leftFight(_perso, null);
		}
	}

	private void Game_on_showCase(String packet)
	{
		if(_perso == null)return;
		if(_perso.getPelea() == null)return;
		if(_perso.getPelea().get_state() != Constantes.FIGHT_STATE_ACTIVE)return;
		int cellID = -1;
		try
		{
			cellID = Integer.parseInt(packet.substring(2));
		}catch(Exception ignored){}
		if(cellID == -1)return;
		_perso.getPelea().showCaseToTeam(_perso.get_GUID(),cellID);
	}

	private void Game_on_Ready(String packet)
	{
		if(_perso.getPelea() == null)return;
		if(_perso.getPelea().get_state() != Constantes.FIGHT_STATE_PLACE)return;
		_perso.set_ready(packet.substring(2).equalsIgnoreCase("1"));
		_perso.getPelea().verifIfAllReady();
		GestorSalida.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(_perso.getPelea(),3,_perso.get_GUID(),packet.substring(2).equalsIgnoreCase("1"));
	}

	private void Game_on_ChangePlace_packet(String packet)
	{
		if(_perso.getPelea() == null)return;
		try
		{
			int cell = Integer.parseInt(packet.substring(2));
			_perso.getPelea().changePlace( _perso, cell);
		}catch(NumberFormatException e){return;}
	}
	
	private void Game_on_Gdi_packet(String packet)
	{
		int chalID = 0;
		chalID = Integer.parseInt(packet.split("i")[1]);
		if(chalID != 0 && _perso.getPelea() != null) {
			 Pelea fight = _perso.getPelea();
			 if(fight.get_challenges().containsKey(chalID))
				 fight.get_challenges().get(chalID).show_cibleToPerso(_perso);
		}
			
	}

	private void Game_on_GK_packet(String packet)
	{	
		int GameActionId = -1;
		String[] infos = packet.substring(3).split("\\|");
		try
		{
			GameActionId = Integer.parseInt(infos[0]);
		}catch(Exception e){return;}
		if(GameActionId == -1)return;
		GameAction GA = _actions.get(GameActionId);
		if(GA == null)return;
		boolean isOk = packet.charAt(2) == 'K';
		
		switch(GA._actionID)
		{
			case 1://Deplacement
				if(isOk)
				{
					//Hors Combat
					if(_perso.getPelea() == null)
					{
						_perso.getActualCelda().removePlayer(_perso.get_GUID());
						GestorSalida.GAME_SEND_BN(_out);
						String path = GA._args;
						//On prend la case ciblï¿½e
						Case nextCell = _perso.getActualMapa().getMapa(GestorEncriptador.cellCode_To_ID(path.substring(path.length()-2)));
						Case targetCell = _perso.getActualMapa().getMapa(GestorEncriptador.cellCode_To_ID(GA._packet.substring(GA._packet.length()-2)));
						
						//On dï¿½finie la case et on ajoute le personnage sur la case
						_perso.set_curCell(nextCell);
						_perso.set_orientation(GestorEncriptador.getIntByHashedValue(path.charAt(path.length()-3)));
						_perso.getActualCelda().addPerso(_perso);
						if(!_perso._isGhosts) _perso.set_away(false);
						
						if(targetCell.getObject() != null)
						{
							//Si c'est une "borne" comme Emotes, ou Crï¿½ation guilde
							if(targetCell.getObject().getID() == 1324)
							{
								Constantes.applyPlotIOAction(_perso,_perso.getActualMapa().getID(),targetCell.getID());
							}
							//Statues phoenix
							else if(targetCell.getObject().getID() == 542)
							{
								if(_perso._isGhosts) _perso.set_Alive();
							}
						}
						_perso.getActualMapa().onPlayerArriveOnCell(_perso,_perso.getActualCelda().getID());
					}
					else//En combat
					{
						_perso.getPelea().onGK(_perso);
						return;
					}
					
				}
				else
				{
					//Si le joueur s'arrete sur une case
					int newCellID = -1;
					try
					{
						newCellID = Integer.parseInt(infos[1]);
					}catch(Exception e){return;}
					if(newCellID == -1)return;
					String path = GA._args;
					_perso.getActualCelda().removePlayer(_perso.get_GUID());
					_perso.set_curCell(_perso.getActualMapa().getMapa(newCellID));
					_perso.set_orientation(GestorEncriptador.getIntByHashedValue(path.charAt(path.length()-3)));
					_perso.getActualCelda().addPerso(_perso);
					GestorSalida.GAME_SEND_BN(_out);
				}
			break;
			
			case 500://Action Sur Map
				_perso.finishActionOnCell(GA);
			break;

		}
		removeAction(GA);
	}

	private void Game_on_GI_packet() 
	{
		if(_perso.getPelea() != null)
		{
			//Only percepteur
			GestorSalida.GAME_SEND_MAP_GMS_PACKETS(_perso.getActualMapa(), _perso);
			GestorSalida.GAME_SEND_GDK_PACKET(_out);
			return;
		}
		//Enclos
		GestorSalida.GAME_SEND_Rp_PACKET(_perso, _perso.getActualMapa().getMountPark());
		//Maisons
		Casas.LoadHouse(_perso, _perso.getActualMapa().getID());
		//Objets sur la carte
		GestorSalida.GAME_SEND_MAP_GMS_PACKETS(_perso.getActualMapa(), _perso);
		GestorSalida.GAME_SEND_MAP_MOBS_GMS_PACKETS(_perso.getCuenta().getGameThread().get_out(), _perso.getActualMapa());
		GestorSalida.GAME_SEND_MAP_NPCS_GMS_PACKETS(_perso,_perso.getActualMapa());
		GestorSalida.GAME_SEND_MAP_PERCO_GMS_PACKETS(_out,_perso.getActualMapa());
		GestorSalida.GAME_SEND_MAP_OBJECTS_GDS_PACKETS(_out,_perso.getActualMapa());
		GestorSalida.GAME_SEND_GDK_PACKET(_out);
		GestorSalida.GAME_SEND_MAP_FIGHT_COUNT(_out, _perso.getActualMapa());
		GestorSalida.GAME_SEND_MERCHANT_LIST(_perso, _perso.getActualMapa().getID());
		//Les drapeau de combats
		Pelea.FightStateAddFlag(_perso.getActualMapa(), _perso);
		//items au sol
		_perso.getActualMapa().sendFloorItems(_perso);
	}

	private void parseGameActionPacket(String packet)
	{
		int actionID;
		try
		{
			actionID = Integer.parseInt(packet.substring(2,5));
		}catch(NumberFormatException e){return;}

		int nextGameActionID = 0;
		if(_actions.size() > 0)
		{
			//On prend le plus haut GameActionID + 1
			nextGameActionID = (Integer)(_actions.keySet().toArray()[_actions.size()-1])+1;
		}
		GameAction GA = new GameAction(nextGameActionID,actionID,packet);

		switch (actionID) {
//Deplacement
			case 1 -> game_parseDeplacementPacket(GA);
//Sort
			case 300 -> game_tryCastSpell(packet);
//Attaque CaC
			case 303 -> game_tryCac(packet);
//Action Sur Map
			case 500 -> game_action(GA);
//Panneau intï¿½rieur de la maison
			case 507 -> house_action(packet);
//Mariage oui
			case 618 -> {
				_perso.setisOK(Integer.parseInt(packet.substring(5, 6)));
				GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(_perso.getActualMapa(), "", _perso.get_GUID(), _perso.getNombre(), "Oui");
				if (Mundo.getMarried(0).getisOK() > 0 && Mundo.getMarried(1).getisOK() > 0) {
					Mundo.Wedding(Mundo.getMarried(0), Mundo.getMarried(1), 1);
				}
				if (Mundo.getMarried(0) != null && Mundo.getMarried(1) != null) {
					Mundo.PriestRequest((Mundo.getMarried(0) == _perso ? Mundo.getMarried(1) : Mundo.getMarried(0)), (Mundo.getMarried(0) == _perso ? Mundo.getMarried(1).getActualMapa() : Mundo.getMarried(0).getActualMapa()), _perso.get_isTalkingWith());
				}
			}
//Mariage non
			case 619 -> {
				_perso.setisOK(0);
				GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(_perso.getActualMapa(), "", _perso.get_GUID(), _perso.getNombre(), "Non");
				Mundo.Wedding(Mundo.getMarried(0), Mundo.getMarried(1), 0);
			}
//Demande Defie
			case 900 -> game_ask_duel(packet);
//Accepter Defie
			case 901 -> game_accept_duel(packet);
//Refus/Anuler Defie
			case 902 -> game_cancel_duel(packet);
//Rejoindre combat
			case 903 -> game_join_fight(packet);
//Agresser
			case 906 -> game_aggro(packet);
//Perco
			case 909 -> game_perco(packet);
		}
	}

	private void house_action(String packet)
	{
		int actionID = Integer.parseInt(packet.substring(5));
		Casas h = _perso.getInHouse();
		if(h == null) return;
		switch (actionID) {
//Vï¿½rouiller maison
			case 81 -> h.Lock(_perso);
//Acheter maison
			case 97 -> h.BuyIt(_perso);
//Modifier prix de vente
			case 98, 108 -> h.SellIt(_perso);
		}
	}
	
	
	private void game_perco(String packet)
	{
		try
		{
			if(_perso == null)return;
			if(_perso.getPelea() != null)return;
			if(_perso.get_isTalkingWith() != 0 ||
			   _perso.get_isTradingWith() != 0 ||
			   _perso.getCurJobAction() != null ||
			   _perso.get_curExchange() != null ||
			   _perso.is_away())
					{
						return;
					}
			int id = Integer.parseInt(packet.substring(5));
			Recaudador target = Mundo.getPerco(id);
			if(target == null || target.get_inFight() > 0) return;
			if(target.get_Exchange())
			{
				
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "1180");
				return;
			}
			GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(_perso.getActualMapa(),"", 909, _perso.get_GUID()+"", id+"");
			_perso.getActualMapa().startFigthVersusPercepteur(_perso, target);
		}catch(Exception ignored){}
	}
	
	private void game_aggro(String packet)
	{
		try
		{
			if(_perso == null)return;
			if(_perso.getPelea() != null)return;
			int id = Integer.parseInt(packet.substring(5));
			Personaje target = Mundo.getPersonnage(id);
			if(target == null || !target.isConectado() || target.getPelea() != null
			|| target.getActualMapa().getID() != _perso.getActualMapa().getID()
			|| target.get_align() == _perso.get_align()
			|| _perso.getActualMapa().getEsquemaPelea().equalsIgnoreCase("|")
			|| !target.PuedeSerAgredido())
				return;
			
			if(target.get_align() == 0) 
			{
				_perso.setDeshonor(_perso.getDeshonor()+1);
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "084;1");
			}

			_perso.toggleWings('+');
			GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(_perso.getActualMapa(),"", 906, _perso.get_GUID()+"", id+"");
			_perso.getActualMapa().newFight(_perso, target, Constantes.FIGHT_TYPE_AGRESSION);
		}catch(Exception ignored){}
	}

	private void game_action(GameAction GA)
	{
		String packet = GA._packet.substring(5);
		int cellID = -1;
		int actionID = -1;
		
		try
		{
			cellID = Integer.parseInt(packet.split(";")[0]);
			actionID = Integer.parseInt(packet.split(";")[1]);
		}catch(Exception ignored){}
		//Si packet invalide, ou cellule introuvable
		if(cellID == -1 || actionID == -1 || _perso == null || _perso.getActualMapa() == null ||
				_perso.getActualMapa().getMapa(cellID) == null)
			return;
		GA._args = cellID+";"+actionID;
		_perso.getCuenta().getGameThread().addAction(GA);
		_perso.startActionOnCell(GA);
	}

	private void game_tryCac(String packet)
	{
		try
		{
			if(_perso.getPelea() ==null)return;
			int cellID = -1;
			try
			{
				cellID = Integer.parseInt(packet.substring(5));
			}catch(Exception e){return;}

			_perso.getPelea().tryCaC(_perso,cellID);
		}catch(Exception ignored){}
	}

	private void game_tryCastSpell(String packet)
	{
		try
		{
			String[] splt = packet.split(";");
			int spellID = Integer.parseInt(splt[0].substring(5));
			int caseID = Integer.parseInt(splt[1]);
			if(_perso.getPelea() != null)
			{
				SortStats SS = _perso.getSortStatBySortIfHas(spellID);
				if(SS == null)return;
				_perso.getPelea().tryCastSpell(_perso.getPelea().getFighterByPerso(_perso),SS,caseID);
			}
		}catch(NumberFormatException e){return;}
	}

	private void game_join_fight(String packet)
	{
		String[] infos = packet.substring(5).split(";");
		if(infos.length == 1)
		{
			try
			{
				Pelea F = _perso.getActualMapa().getFight(Integer.parseInt(infos[0]));
				F.joinAsSpect(_perso);
			}catch(Exception e){return;}
		}else
		{
			try
			{
				int guid = Integer.parseInt(infos[1]);
				if(_perso.is_away())
				{
					GestorSalida.GAME_SEND_GA903_ERROR_PACKET(_out,'o',guid);
					return;
				}
				if(Mundo.getPersonnage(guid) == null)return;
				Mundo.getPersonnage(guid).getPelea().joinFight(_perso,guid);
			}catch(Exception e){return;}
		}
	}

	private void game_accept_duel(String packet)
	{
		int guid = -1;
		try{guid = Integer.parseInt(packet.substring(5));}catch(NumberFormatException e){return;}
		if(_perso.get_duelID() != guid || _perso.get_duelID() == -1)return;
		GestorSalida.GAME_SEND_MAP_START_DUEL_TO_MAP(_perso.getActualMapa(),_perso.get_duelID(),_perso.get_GUID());
		Pelea fight = _perso.getActualMapa().newFight(Mundo.getPersonnage(_perso.get_duelID()),_perso, Constantes.FIGHT_TYPE_CHALLENGE);
		_perso.set_fight(fight);
		Mundo.getPersonnage(_perso.get_duelID()).set_fight(fight);
		
	}

	private void game_cancel_duel(String packet)
	{
		try
		{
			if(_perso.get_duelID() == -1)return;
			GestorSalida.GAME_SEND_CANCEL_DUEL_TO_MAP(_perso.getActualMapa(),_perso.get_duelID(),_perso.get_GUID());
			Mundo.getPersonnage(_perso.get_duelID()).set_away(false);
			Mundo.getPersonnage(_perso.get_duelID()).set_duelID(-1);
			_perso.set_away(false);
			_perso.set_duelID(-1);	
		}catch(NumberFormatException e){return;}
	}

	private void game_ask_duel(String packet)
	{
		if(_perso.getActualMapa().getEsquemaPelea().equalsIgnoreCase("|"))
		{
			GestorSalida.GAME_SEND_DUEL_Y_AWAY(_out, _perso.get_GUID());
			return;
		}
		try
		{
			int guid = Integer.parseInt(packet.substring(5));
			if(_perso.is_away() || _perso.getPelea() != null){
				GestorSalida.GAME_SEND_DUEL_Y_AWAY(_out, _perso.get_GUID());return;}
			Personaje Target = Mundo.getPersonnage(guid);
			if(Target == null) return;
			if(Target.is_away() || Target.getPelea() != null || Target.getActualMapa().getID() != _perso.getActualMapa().getID()){
				GestorSalida.GAME_SEND_DUEL_E_AWAY(_out, _perso.get_GUID());return;}
			_perso.set_duelID(guid);
			_perso.set_away(true);
			Mundo.getPersonnage(guid).set_duelID(_perso.get_GUID());
			Mundo.getPersonnage(guid).set_away(true);
			GestorSalida.GAME_SEND_MAP_NEW_DUEL_TO_MAP(_perso.getActualMapa(),_perso.get_GUID(),guid);
		}catch(NumberFormatException e){return;}
	}

	private void game_parseDeplacementPacket(GameAction GA)
	{
		String path = GA._packet.substring(5);
		if(_perso.getPelea() == null)
		{
			if(_perso.getPodUsed() > _perso.getMaxPod())
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(_perso, "112");
				GestorSalida.GAME_SEND_GA_PACKET(_out, "", "0", "", "");
				removeAction(GA);
				return;
			}
			AtomicReference<String> pathRef = new AtomicReference<>(path);
			int result = Camino.isValidPath(_perso.getActualMapa(),_perso.getActualCelda().getID(),pathRef, null);
			
			//Si dï¿½placement inutile
			if(result == 0)
			{
				GestorSalida.GAME_SEND_GA_PACKET(_out, "", "0", "", "");
				removeAction(GA);
				return;
			}
			if(result != -1000 && result < 0)result = -result;
			
			//On prend en compte le nouveau path
			path = pathRef.get();
			//Si le path est invalide
			if(result == -1000)
			{
				JuegoServidor.agregar_a_los_logs(_perso.getNombre()+"("+_perso.get_GUID()+") Tentative de  deplacement avec un path invalide");
				path = GestorEncriptador.getHashedValueByInt(_perso.getOrientacion())+ GestorEncriptador.cellID_To_Code(_perso.getActualCelda().getID());
			}
			//On sauvegarde le path dans la variable
			GA._args = path;
			
			GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(_perso.getActualMapa(), ""+GA._id, 1, _perso.get_GUID()+"", "a"+ GestorEncriptador.cellID_To_Code(_perso.getActualCelda().getID())+path);
			addAction(GA);
			if(_perso.isSitted())_perso.setSitted(false);
			_perso.set_away(true);
		}else
		{
			Fighter F = _perso.getPelea().getFighterByPerso(_perso);
			if(F == null)return;
			GA._args = path;
			_perso.getPelea().fighterDeplace(F,GA);
		}
	}

	public PrintWriter get_out() {
		return _out;
	}
	
	public void kick()
	{
		try
		{
			MainServidor.gameServer.delClient(this);
			
    		if(_compte != null)
    		{
    			_compte.deconnexion();
    		}
    		if(!_s.isClosed())
    		_s.close();
    		_in.close();
    		_out.close();
    		_t.interrupt();
		}catch(IOException e1){e1.printStackTrace();}
	}

	private void parseAccountPacket(String packet) {
		//Validation du nom du personnage
		//Vï¿½rifie d'abord si il contient des termes dï¿½finit
		//Si le nom passe le test, on vï¿½rifie que les caractï¿½re entrï¿½ sont correct.
		//Si le nom est invalide
		//SocketManager.GAME_SEND_HIDE_GENERATE_NAME(_out);
		switch (packet.charAt(1)) {
			case 'A' -> {
				String[] infos = packet.substring(2).split("\\|");
				if (GestorSQL.persoExist(infos[0])) {
					GestorSalida.GAME_SEND_NAME_ALREADY_EXIST(_out);
					return;
				}
				boolean isValid = true;
				String name = infos[0].toLowerCase();
				if (name.length() > 20
						|| name.contains("mj")
						|| name.contains("modo")
						|| name.contains("admin")) {
					isValid = false;
				}
				if (isValid) {
					int tiretCount = 0;
					char exLetterA = ' ';
					char exLetterB = ' ';
					for (char curLetter : name.toCharArray()) {
						if (!((curLetter >= 'a' && curLetter <= 'z') || curLetter == '-')) {
							isValid = false;
							break;
						}
						if (curLetter == exLetterA && curLetter == exLetterB) {
							isValid = false;
							break;
						}
						if (curLetter >= 'a') {
							exLetterA = exLetterB;
							exLetterB = curLetter;
						}
						if (curLetter == '-') {
							if (tiretCount >= 1) {
								isValid = false;
								break;
							} else {
								tiretCount++;
							}
						}
					}
				}
				if (!isValid) {
					GestorSalida.GAME_SEND_NAME_ALREADY_EXIST(_out);
					return;
				}
				if (_compte.GET_PERSO_NUMBER() >= MainServidor.MAXIMO_PERSONAJES_POR_CUENTA) {
					GestorSalida.GAME_SEND_CREATE_PERSO_FULL(_out);
					return;
				}
				if (_compte.createPerso(infos[0], Integer.parseInt(infos[2]), Integer.parseInt(infos[1]), Integer.parseInt(infos[3]), Integer.parseInt(infos[4]), Integer.parseInt(infos[5]))) {
					GestorSalida.GAME_SEND_CREATE_OK(_out);
					GestorSalida.GAME_SEND_PERSO_LIST(_out, _compte.get_persos());
				} else {
					GestorSalida.GAME_SEND_CREATE_FAILED(_out);
				}
			}
			case 'B' -> {
				int stat = -1;
				try {
					if (packet.substring(2).contains(";")) {
						stat = Integer.parseInt(packet.substring(2).split(";")[0]);
						if (stat > 0) {
							int code = 0;
							code = Integer.parseInt(packet.substring(2).split(";")[1]);
							if (code < 0)
								return;
							if (this._perso.get_capital() < code)
								code = this._perso.get_capital();
							_perso.boostStatFixedCount(stat, code);
						}
					} else {
						stat = Integer.parseInt(packet.substring(2).split("/u000A")[0]);
						this._perso.boostStat(stat);
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			case 'D' -> {
				String[] split = packet.substring(2).split("\\|");
				int GUID = Integer.parseInt(split[0]);
				String reponse = split.length > 1 ? split[1] : "";
				if (_compte.get_persos().containsKey(GUID)) {
					if (_compte.get_persos().get(GUID).get_lvl() < 20 || (_compte.get_persos().get(GUID).get_lvl() >= 20 && reponse.equals(_compte.get_reponse()))) {
						_compte.deletePerso(GUID);
						GestorSalida.GAME_SEND_PERSO_LIST(_out, _compte.get_persos());
					} else
						GestorSalida.GAME_SEND_DELETE_PERSO_FAILED(_out);
				} else
					GestorSalida.GAME_SEND_DELETE_PERSO_FAILED(_out);
			}
			case 'f' -> {
				int queueID = 1;
				int position = 1;
				GestorSalida.MULTI_SEND_Af_PACKET(_out, position, 1, 1, "" + 1, queueID);
			}
			case 'i' -> _compte.setClientKey(packet.substring(2));
			case 'L' -> GestorSalida.GAME_SEND_PERSO_LIST(_out, _compte.get_persos());
			case 'S' -> {
				int charID = Integer.parseInt(packet.substring(2));
				if (_compte.get_persos().get(charID) != null) {
					_compte.setGameThread(this);
					_perso = _compte.get_persos().get(charID);
					if (_perso != null) {
						_perso.OnJoinGame();
						return;
					}
				}
				GestorSalida.GAME_SEND_PERSO_SELECTION_FAILED(_out);
			}
			case 'T' -> {
				int guid = Integer.parseInt(packet.substring(2));
				_compte = MainServidor.gameServer.getWaitingCompte(guid);
				if (_compte != null) {
					String ip = _s.getInetAddress().getHostAddress();

					_compte.setGameThread(this);
					_compte.setCurIP(ip);
					MainServidor.gameServer.delWaitingCompte(_compte);
					GestorSalida.GAME_SEND_ATTRIBUTE_SUCCESS(_out);
				} else {
					GestorSalida.GAME_SEND_ATTRIBUTE_FAILED(_out);
				}
			}
			case 'V' -> GestorSalida.GAME_SEND_AV0(_out);
			case 'P' -> GestorSalida.REALM_SEND_REQUIRED_APK(_out);
		}
	}

	public Thread getThread()
	{
		return _t;
	}

	public void removeAction(GameAction GA)
	{
		//* DEBUG
		System.out.println("Supression de la GameAction id = "+GA._id);
		//*/
		_actions.remove(GA._id);
	}
	
	public void addAction(GameAction GA)
	{
		_actions.put(GA._id, GA);
		//* DEBUG
		System.out.println("Ajout de la GameAction id = "+GA._id);
		System.out.println("Packet: "+GA._packet);
		//*/
	}
	
	private void Object_obvijevan_changeApparence(String packet)
	{
		int guid = -1;
		int pos = -1;
		int val = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			pos = Integer.parseInt(packet.split("\\|")[1]);
			val = Integer.parseInt(packet.split("\\|")[2]); } catch (Exception e) {
				return;
			}if ((guid == -1) || (!_perso.hasItemGuid(guid))) return;
			Objeto obj = Mundo.getObjet(guid);
			if ((val >= 21) || (val <= 0)) return;
			
			obj.obvijevanChangeStat(972, val);
			GestorSalida.send(_perso, obj.obvijevanOCO_Packet(pos));
			if (pos != -1) GestorSalida.GAME_SEND_ON_EQUIP_ITEM(_perso.getActualMapa(), _perso);
	}
	private void Object_obvijevan_feed(String packet)
	{
		int guid = -1;
		int pos = -1;
		int victime = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			pos = Integer.parseInt(packet.split("\\|")[1]);
			victime = Integer.parseInt(packet.split("\\|")[2]);
		} catch (Exception e) {return;}
		
		if ((guid == -1) || (!_perso.hasItemGuid(guid)))
			return;
		Objeto obj = Mundo.getObjet(guid);
		Objeto objVictime = Mundo.getObjet(victime);
		obj.obvijevanNourir(objVictime);
		
		int qua = objVictime.getQuantity();
		if (qua <= 1)
		{
			_perso.removeItem(objVictime.getGuid());
			GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(_perso, objVictime.getGuid());
		} else {
			objVictime.setQuantity(qua - 1);
			GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(_perso, objVictime);
		}
		GestorSalida.send(_perso, obj.obvijevanOCO_Packet(pos));
	}
	
	private void Object_obvijevan_desassocier(String packet)
	{
		int guid = -1;
		int pos = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			pos = Integer.parseInt(packet.split("\\|")[1]); } catch (Exception e) {
				return;
			}if ((guid == -1) || (!_perso.hasItemGuid(guid))) return;
			Objeto obj = Mundo.getObjet(guid);
			int idOBVI = 0;

		switch (obj.getTemplate().getType()) {
			case 1 -> idOBVI = 9255;
			case 9 -> idOBVI = 9256;
			case 16 -> idOBVI = 9234;
			case 17 -> idOBVI = 9233;
			default -> {
				GestorSalida.GAME_SEND_MESSAGE(_perso, "Erreur d'obvijevan numero: 4. Merci de nous le signaler si le probleme est grave.", "000000");
				return;
			}
		}
			Objeto.ObjTemplate t = Mundo.getObjTemplate(idOBVI);
			Objeto obV = t.createNewItem(1, true);
			String obviStats = obj.getObvijevanStatsOnly();
			if (obviStats.equals("")) {
				GestorSalida.GAME_SEND_MESSAGE(_perso, "Erreur d'obvijevan numero: 3. Merci de nous le signaler si le probleme est grave.", "000000");
				return;
			}
			obV.clearStats();
			obV.parseStringToStats(obviStats);
			if (_perso.addObjet(obV, true)) {
				Mundo.addObjet(obV, true);
			}
			obj.removeAllObvijevanStats();
			GestorSalida.send(_perso, obj.obvijevanOCO_Packet(pos));
			GestorSalida.GAME_SEND_ON_EQUIP_ITEM(_perso.getActualMapa(), _perso);
	}
	/*
	private void Object_dissociate(String packet) {
		String[] infos = packet.substring(2).split("" + (char) 0x0A)[0].split("\\|");
		try {
			int guid = Integer.parseInt(infos[0]);
			Objet Obj = World.getObjet(guid);

			if (Obj.is_linked()) {
				Speaking Obv = Obj.get_linkedItem();
				Obj.set_unlinkedItem();
				Obv.set_unlinkedItem();
				_perso.addObjet(Obv);

				SQLManager.SAVE_PERSONNAGE(_perso, false);
				SQLManager.UPDATE_SPEAKING(Obv);

				SocketManager.GAME_SEND_OAKO_PACKET(_perso, Obv);
				SocketManager.GAME_SEND_OCO_PACKET(_perso, Obj);
				SocketManager.GAME_SEND_Ow_PACKET(_perso);

				_perso.refreshStats();
				if (_perso.getGroup() != null) {
					SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_perso.getGroup(), _perso);
				}
                                 _perso.resetAS();
				SocketManager.GAME_SEND_STATS_PACKET(_perso);
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);

				//Si objet de panoplie
				if (Obj.getTemplate().getPanopID() > 0) {
					SocketManager.GAME_SEND_OS_PACKET(_perso, Obj.getTemplate().getPanopID());
				}
			}
		} catch (Exception e) {
			SocketManager.GAME_SEND_BN(_perso);
			return;
		}
	}

	private void Object_eat(String packet) {
		String[] infos = packet.substring(2).split("" + (char) 0x0A)[0].split("\\|");
		try {
			int guid = Integer.parseInt(infos[0]);
			int foodID = Integer.parseInt(infos[2]);
			Item Obj = World.getObjet(guid);
			Item Food = World.getObjet(foodID);

			Speaking Obv = Obj.get_linkedItem();

			if (Obv == null) {
				if (Ancestrar.CONFIG_DEBUG) {
					Ancestrar.printIn("Target Object null", true);
				}
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}
			if (Food == null) {
				if (Ancestrar.CONFIG_DEBUG) {
					Ancestrar.printIn("Nourriture Object null", true);
				}
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}
			if (Obj.getTemplate().getType() != Obv.getTemplate().get_obviType()) {
				if (Ancestrar.CONFIG_DEBUG) {
					Ancestrar.printIn("Mauvaise nourriture", true);
				}
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}
			if (Obv.eatItem(_perso, Food)) {
				SQLManager.UPDATE_SPEAKING(Obv);//on save

				SocketManager.GAME_SEND_OCO_PACKET(_perso, Obj);//Update affichage (liï¿½ obligatoirement pour nourrir)

				//On envoit le reste (apparence)
				SocketManager.GAME_SEND_Ow_PACKET(_perso);
				_perso.refreshStats();
				if (_perso.getGroup() != null) {
					SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_perso.getGroup(), _perso);
				}
                                 _perso.resetAS();
				SocketManager.GAME_SEND_STATS_PACKET(_perso);
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);

				//Si objet de panoplie
				if (Obj.getTemplate().getPanopID() > 0) {
					SocketManager.GAME_SEND_OS_PACKET(_perso, Obj.getTemplate().getPanopID());
				}
				SocketManager.GAME_SEND_BN(_perso);
			} else {
				if (Ancestrar.CONFIG_DEBUG) {
					Ancestrar.printIn("Ne peut pas ï¿½tre nourri", true);
				}
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}
		} catch (Exception e) {
			SocketManager.GAME_SEND_BN(_perso);
			if (Ancestrar.CONFIG_DEBUG) {
				Ancestrar.printIn("Erreur globale: " + e.getMessage() + "& \n" + e.getCause(), true);
			}
			return;
		}
	}

	private void Object_ChangeSkin(String packet) {
		String[] infos = packet.substring(2).split("" + (char) 0x0A)[0].split("\\|");
		try {
			int guid = Integer.parseInt(infos[0]);
			int skinTarget = Integer.parseInt(infos[2]);
			Item Obj = World.getObjet(guid);
			Speaking Obv = null;
			if (Obj.isSpeaking()) {
				Obv = Speaking.toSpeaking(Obj);
			} else if (Obj.is_linked()) {
				Obv = Obj.get_linkedItem();
			}
			if (skinTarget < 0 || skinTarget > 20 || Obv == null || skinTarget > Obv.get_lvl() || skinTarget == Obv.get_selectedLevel()) {
				SocketManager.GAME_SEND_BN(_perso);
				return;
			}

			Obv.set_selectedLevel(skinTarget);
			SQLManager.UPDATE_SPEAKING(Obv);//on save

			if (Obj.is_linked()) {
				SocketManager.GAME_SEND_OCO_PACKET(_perso, Obj);//Update affichage si liï¿½
			} else {
				SocketManager.GAME_SEND_OCO_PACKET(_perso, Obv);//Update affichage si seul
			}
			//On envoit le reste (apparence)
			SocketManager.GAME_SEND_Ow_PACKET(_perso);
			_perso.refreshStats();
			if (_perso.getGroup() != null) {
				SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_perso.getGroup(), _perso);
			}
                         _perso.resetAS();
			SocketManager.GAME_SEND_STATS_PACKET(_perso);
			SocketManager.GAME_SEND_ON_EQUIP_ITEM(_perso.get_curCarte(), _perso);

			//Si objet de panoplie
			if (Obj.getTemplate().getPanopID() > 0) {
				SocketManager.GAME_SEND_OS_PACKET(_perso, Obj.getTemplate().getPanopID());
			}
			SocketManager.GAME_SEND_BN(_perso);
		} catch (Exception e) {
			SocketManager.GAME_SEND_BN(_perso);
			return;
		}
	}*/


	
}
	