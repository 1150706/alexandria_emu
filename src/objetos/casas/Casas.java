package objetos.casas;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import comunes.MainServidor;
import comunes.Constantes;
import comunes.GestorSQL;
import comunes.GestorSalida;
import comunes.Mundo;
import objetos.Cuenta;
import objetos.Gremio;
import objetos.Personaje;

public class Casas
{
	private final int _id;
	private final short _mapa;
	private final int _celda;
	private int _dueño;
	private int _venta;
	private int _gremio;
	private int _derechosgremio;
	private int _acceso;
	private String _llave;
	private final int _mapainterior;
	private final int _celdainterior;
	
	//Droits de chaques maisons
	private final Map<Integer,Boolean> haveRight = new TreeMap<>();

	
	public Casas(int id, short mapa, int celda, int dueño, int venta, int gremio, int acceso, String llave, int derechosgremio, int mapainterior, int celdainterior) {
		_id = id;
		_mapa = mapa;
		_celda = celda;
		_dueño = dueño;
		_venta = venta;
		_gremio = gremio;
		_acceso = acceso;
		_llave = llave;
		_derechosgremio = derechosgremio;
		parseIntToRight(derechosgremio);
		_mapainterior = mapainterior;
		_celdainterior = celdainterior;
	}
	
	public int get_id() { return _id; }
	
	public short get_mapa()
	{
		return _mapa;
	}
	
	public int get_celda()
	{
		return _celda;
	}
	
	public int get_dueño() { return _dueño; }
	
	public void set_dueño(int id)
	{
		_dueño = id;
	}
	
	public int get_venta()
	{
		return _venta;
	}
	
	public void set_venta(int price)
	{
		_venta = price;
	}
	
	public int get_gremio()
	{
		return _gremio;
	}
	
	public void set_gremio(int GuildID)
	{
		_gremio = GuildID;
	}
	
	public int get_guild_rights()
	{
		return _derechosgremio;
	}
	
	public void set_guild_rights(int GuildRights)
	{
		_derechosgremio = GuildRights;
	}
	
	public int get_acceso()
	{
		return _acceso;
	}
	
	public void set_acceso(int access)
	{
		_acceso = access;
	}
	
	public String get_llave()
	{
		return _llave;
	}
	
	public void set_llave(String key)
	{
		_llave = key;
	}
	
	public int get_mapainterior()
	{
		return _mapainterior;
	}
	
	public int get_celdainterior()
	{
		return _celdainterior;
	}
	
	public static Casas get_house_id_by_coord(int map_id, int cell_id) {
		for(Entry<Integer, Casas> house : Mundo.getHouses().entrySet()) {
			if(house.getValue().get_mapa() == map_id && house.getValue().get_celda() == cell_id) {
				return house.getValue();
			}
		}
		return null;
	}
	
	public static void LoadHouse(Personaje P, int newMapID)//Affichage des maison + Blason
	{
		for(Entry<Integer, Casas> house : Mundo.getHouses().entrySet()) {
			if(house.getValue().get_mapa() == newMapID) {
				StringBuilder packet = new StringBuilder();
				packet.append("P").append(house.getValue().get_id()).append("|");
				if(house.getValue().get_dueño() > 0) {
					Cuenta C = Mundo.getCompte(house.getValue().get_dueño());
					if(C == null)//Ne devrait pas arriver
					{
						packet.append("undefined;");
					}else {
						packet.append(Mundo.getCompte(house.getValue().get_dueño()).getApodo()).append(";");
					}
				}else {
					packet.append(";");
				}
				if(house.getValue().get_venta() > 0)//Si prix > 0
				{
					packet.append("1");//Achetable
				}else {
					packet.append("0");//Non achetable
				}
				if(house.getValue().get_gremio() > 0) //Maison de guilde
				{
					Gremio G = Mundo.getGuild(house.getValue().get_gremio());
					if(G != null) {
						String Gname = G.get_name();
						String Gemblem = G.get_emblem();
						if(G.getMembers().size() < MainServidor.MEMBRE_MINI_GUILDE_VALIDE)//Ce n'est plus une maison de guilde
						{
							GestorSQL.casa_gremio(house.getValue(), 0, 0) ;
						}else {
							//Affiche le blason pour les membre de guilde OU Affiche le blason pour les non membre de guilde
							if(P.get_guild() != null && P.get_guild().get_id() == house.getValue().get_gremio() && house.getValue().canDo(Constantes.H_GBLASON))//meme guilde
							{
								packet.append(";").append(Gname).append(";").append(Gemblem);
							}
							else if(house.getValue().canDo(Constantes.H_OBLASON))//Pas de guilde/guilde-différente
							{
								packet.append(";").append(Gname).append(";").append(Gemblem);
							}
						}
					}
				}
				GestorSalida.GAME_SEND_hOUSE(P, packet.toString());

				if(house.getValue().get_dueño() == P.getAccID()) {
					StringBuilder packet1 = new StringBuilder();
					packet1.append("L+|").append(house.getValue().get_id()).append(";").append(house.getValue().get_acceso()).append(";");
					
					if(house.getValue().get_venta() <= 0) {
						packet1.append("0;").append(house.getValue().get_venta());
					}
					else if(house.getValue().get_venta() > 0) {
						packet1.append("1;").append(house.getValue().get_venta());
					}
					GestorSalida.GAME_SEND_hOUSE(P, packet1.toString());
				}
			}
		}
	}

    public void HopIn(Personaje P) {
        if(P.getPelea() != null || P.get_isTalkingWith() != 0 || P.get_isTradingWith() != 0 || P.getCurJobAction() != null || P.get_curExchange() != null)
            return;
        Casas h = P.getInHouse();
        if(h == null)
            return;
        if(h.get_dueño() == P.getAccID() || P.get_guild() != null && P.get_guild().get_id() == h.get_gremio() && canDo(Constantes.H_GNOCODE))
            OpenHouse(P, "-", true);
        else
        if(h.get_dueño() > 0)
            GestorSalida.EVIAR_CODIGO(P, "CK0|8");
        else
        if(h.get_dueño() == 0)
            OpenHouse(P, "-", false);
        else
            return;
    }

    public static void OpenHouse(Personaje P, String packet, boolean isHome) {
        if(P.get_savestat() == 0) {
            Casas h = P.getInHouse();
            GestorSQL.guardar_personaje(P, true);
            if(!h.canDo(Constantes.H_OCANTOPEN) && packet.compareTo(h.get_llave()) == 0 || isHome) {
                P.teletransportar((short)h.get_mapainterior(), h.get_celdainterior());
                closeCode(P);
            } else if(packet.compareTo(h.get_llave()) != 0 || h.canDo(Constantes.H_OCANTOPEN)) {
                GestorSalida.EVIAR_CODIGO(P, "KE");
                GestorSalida.EVIAR_CODIGO(P, "V");
            }
        } else {
            int code = Integer.parseInt(packet);
            if(P.get_capital() >= code) {
                for(int i = 0; i < code; i++)
                    P.boostStat(P.get_savestat());

                GestorSalida.EVIAR_CODIGO(P, "V");
                P.set_savestat(0);
            }
        }
    }
	
	public void BuyIt(Personaje P)//Acheter une maison
	{
		Casas h = P.getInHouse();
		String str = "CK"+h.get_id()+"|"+h.get_venta();//ID + Prix
		GestorSalida.GAME_SEND_hOUSE(P, str);
	}

	public static void HouseAchat(Personaje P)//Acheter une maison
	{
		Casas h = P.getInHouse();
		if(AlreadyHaveHouse(P)) {
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(P, "132;1");
			return;
		}
		//On enleve les kamas
		if(P.getKamas() < h.get_venta()) return;
		long newkamas = P.getKamas()-h.get_venta();
		P.setKamas(newkamas);
		
		int tKamas = 0;
		for(Cofres t : Cofres.getTrunksByHouse(h)) {
			if(h.get_dueño() > 0) {
				t.moveTrunktoBank(Mundo.getCompte(h.get_dueño()));//Déplacement des items vers la banque
			}
			tKamas += t.getKamas();
			t.setKamas(0);//Retrait kamas
			t.setLlave("-");//ResetPass
			t.setDueño(0);//ResetOwner
			GestorSQL.actualizar_cofre(t);
		}
		
		//Ajoute des kamas dans la banque du vendeur
		if(h.get_dueño() > 0) {
			Cuenta Seller = Mundo.getCompte(h.get_dueño());
			long newbankkamas = Seller.getBankKamas()+h.get_venta()+tKamas;
			Seller.setBankKamas(newbankkamas);
			//Petit message pour le prévenir si il est on?
			if(Seller.get_curPerso() != null) {
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(Seller.get_curPerso(), "1240;" + h.get_venta());
				GestorSQL.guardar_personaje(Seller.get_curPerso(), true);
			}
			GestorSQL.actualizar_datos_cuenta(Seller);
		}
		
		//On save l'acheteur
		GestorSQL.guardar_personaje(P, true);
		GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(P);
		closeBuy(P);

		//Achat de la maison
		GestorSQL.comprar_casa(P, h);

		//Rafraichir la map après l'achat
		for(Personaje z:P.getActualMapa().getPersos()) {
			LoadHouse(z, z.getActualMapa().getID());
		}
	}
	
	public void SellIt(Personaje P)//Vendre une maison
	{
		Casas h = P.getInHouse();
		if(isHouse(P, h)) {
			String str = "CK"+h.get_id()+"|"+h.get_venta();//ID + Prix
			GestorSalida.GAME_SEND_hOUSE(P, str);
				return;
		}else {
			return;
		}
	}
	
	public static void SellPrice(Personaje P, String packet)//Vendre une maison
	{
		Casas h = P.getInHouse();
		int price = Integer.parseInt(packet);	
		if(h.isHouse(P, h)) {
			GestorSalida.GAME_SEND_hOUSE(P, "V");
			GestorSalida.GAME_SEND_hOUSE(P, "SK"+h.get_id()+"|"+price);
				
			//Vente de la maison
			GestorSQL.vender_casa(h, price);

			//Rafraichir la map après la mise en vente
			for(Personaje z:P.getActualMapa().getPersos())
			{
				LoadHouse(z, z.getActualMapa().getID());
			}
				
			return;
		}else {
			return;
		}
	}

	public boolean isHouse(Personaje P, Casas h)//Savoir si c'est sa maison
	{
		if(h.get_dueño() == P.getAccID()) return true;
		else return false;
	}
	
	public static void closeCode(Personaje P)
	{
		GestorSalida.EVIAR_CODIGO(P, "V");
	}
	
	public static void closeBuy(Personaje P)
	{
		GestorSalida.GAME_SEND_hOUSE(P, "V");
	}
	
	public void Lock(Personaje P)
	{
		GestorSalida.EVIAR_CODIGO(P, "CK1|8");
	}
	
	public static void LockHouse(Personaje P, String packet)
	{
		Casas h = P.getInHouse();
		if(h.isHouse(P, h)) {
			GestorSQL.codigo_casa(P, h, packet);//Change le code
			closeCode(P);
			return;
		}else {
			closeCode(P);
			return;
		}
	}
	
	public static String parseHouseToGuild(Personaje P) {
		boolean isFirst = true;
		StringBuilder packet = new StringBuilder();
		for(Entry<Integer, Casas> house : Mundo.getHouses().entrySet()) {
			if(house.getValue().get_gremio() == P.get_guild().get_id() && house.getValue().get_guild_rights() > 0) {
				if(isFirst) packet.append("+");
				if(!isFirst) packet.append("|");
				
				packet.append(house.getKey()).append(";");
				packet.append(Mundo.getPersonnage(house.getValue().get_dueño()).getCuenta().getApodo()).append(";");
				packet.append(Mundo.getCarte((short)house.getValue().get_mapainterior()).getX()).append(",").append(Mundo.getCarte((short)house.getValue().get_mapainterior()).getY()).append(";");
				packet.append("0;");//TODO : Compétences ...
				packet.append(house.getValue().get_guild_rights());	
				isFirst = false;
			}
		}
			return packet.toString();
	}
	
	public static boolean AlreadyHaveHouse(Personaje P) {
		for(Entry<Integer, Casas> house : Mundo.getHouses().entrySet()) {
			if(house.getValue().get_dueño() == P.getAccID()) {
				return true;
			}
		}
		return false;
	}
	
	public static void parseHG(Personaje P, String packet) {
		Casas h = P.getInHouse();
		
		if(P.get_guild() == null) return;
		
		if(packet != null) {
			if(packet.charAt(0) == '+') {
				//Ajoute en guilde
				byte HouseMaxOnGuild = (byte) Math.floor(P.get_guild().get_lvl()/10);
				if(HouseOnGuild(P.get_guild().get_id()) >= HouseMaxOnGuild) return;
				if(P.get_guild().getMembers().size() < 10) return;
				GestorSQL.casa_gremio(h, P.get_guild().get_id(), 0);
				parseHG(P, null);
			} else if(packet.charAt(0) == '-') {
				//Retire de la guilde
				GestorSQL.casa_gremio(h, 0, 0);
				parseHG(P, null);
			} else {
				GestorSQL.casa_gremio(h, h.get_gremio(), Integer.parseInt(packet));
				h.parseIntToRight(Integer.parseInt(packet));
			}
		} else if(packet == null) {
		if(h.get_gremio() <= 0) {
			GestorSalida.GAME_SEND_hOUSE(P, "G"+h.get_id());
		}else if(h.get_gremio() > 0) {
			GestorSalida.GAME_SEND_hOUSE(P, "G"+h.get_id()+";"+P.get_guild().get_name()+";"+P.get_guild().get_emblem()+";"+h.get_guild_rights());
		}
		}
	}
	
	public static byte HouseOnGuild(int GuildID) {
		byte i = 0;
		for(Entry<Integer, Casas> house : Mundo.getHouses().entrySet()) {
			if(house.getValue().get_gremio() == GuildID) {
				i++;
			}
		}
		return i;
	}

	public boolean canDo(int rightValue)
	{	
		return haveRight.get(rightValue);
	}
	
	public void initRight() {
		haveRight.put(Constantes.H_GBLASON, false);
		haveRight.put(Constantes.H_OBLASON,false);
		haveRight.put(Constantes.H_GNOCODE,false);
		haveRight.put(Constantes.H_OCANTOPEN,false);
		haveRight.put(Constantes.C_GNOCODE,false);
		haveRight.put(Constantes.C_OCANTOPEN,false);
		haveRight.put(Constantes.H_GREPOS,false);
		haveRight.put(Constantes.H_GTELE,false);
	}
	
	public void parseIntToRight(int total) {
		if(haveRight.isEmpty()) {
			initRight();
		}
		if(total == 1)
			return;

		if(haveRight.size() > 0)	//Si les droits contiennent quelque chose -> Vidage (Même si le TreeMap supprimerais les entrées doublon lors de l'ajout)
			haveRight.clear();

		initRight();	//Remplissage des droits

		Integer[] mapKey = haveRight.keySet().toArray(new Integer[0]);	//Récupère les clef de map dans un tableau d'Integer

		while(total > 0) {
			for (int i = haveRight.size()-1; i < haveRight.size(); i--) {
				if(mapKey[i] <= total) {
					total ^= mapKey[i];
					haveRight.put(mapKey[i],true);
					break;
				}
			}
		}
	}
	
	public static void Leave(Personaje P, String packet) {
		Casas h = P.getInHouse();
		if(!h.isHouse(P, h)) return;
		int Pguid = Integer.parseInt(packet);
		Personaje Target = Mundo.getPersonnage(Pguid);
		if(Target == null || !Target.isConectado() || Target.getPelea() != null || Target.getActualMapa().getID() != P.getActualMapa().getID()) return;
		Target.teletransportar(h.get_mapa(), h.get_celda());
		GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(Target, "018;"+P.getNombre());
	}
	
	
	public static Casas get_HouseByPerso(Personaje P)//Connaitre la MAPID + CELLID de sa maison
	{
		for(Entry<Integer, Casas> house : Mundo.getHouses().entrySet()) {
			if(house.getValue().get_dueño() == P.getAccID()) {
				return house.getValue();
			}
		}
		return null;
	}
	
	public static void removeHouseGuild(int GuildID) {
		for(Entry<Integer, Casas> h : Mundo.getHouses().entrySet()) {
			if(h.getValue().get_gremio() == GuildID) {
				h.getValue().set_guild_rights(0);
				h.getValue().set_gremio(0);
			}else {
				continue;
			}
		}
		GestorSQL.eliminar_casa_gremio(GuildID);//Supprime les maisons de guilde
	}
}