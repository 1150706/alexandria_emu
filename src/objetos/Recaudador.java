package objetos;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import objetos.Pelea.Fighter;

import comunes.MainServidor;
import comunes.GestorSQL;
import comunes.GestorSalida;
import comunes.Mundo;
import comunes.Mundo.Drop;

public class Recaudador
{
	private final int _guid;
	private final short _MapID;
	private final int _cellID;
	private final byte _orientation;
	private int _GuildID = 0;
	private short _N1 = 0;
	private short _N2 = 0;
	private byte _inFight = 0;
	private int _inFightID = -1;
	private final Map<Integer, Objeto> _objets = new TreeMap<>();
	private long _kamas = 0;
	private long _xp = 0;
	private boolean _inExchange = false;
	//Timer
	private long _timeTurn = 45000;
	//Les logs
	private final Map<Integer, Objeto> _LogObjets = new TreeMap<>();
	private long _LogXP = 0;
	
	public Recaudador(int guid, short map, int cellID, byte orientation, int GuildID,
					  short N1, short N2, String items, long kamas, long xp)
	{
		_guid = guid;
		_MapID = map;
		_cellID = cellID;
		_orientation = orientation;
		_GuildID = GuildID;
		_N1 = N1;
		_N2 = N2;
		//Mise en place de son inventaire
		for(String item : items.split("\\|"))
		{
			if(item.equals(""))continue;
			String[] infos = item.split(":");
			int id = Integer.parseInt(infos[0]);
			Objeto obj = Mundo.getObjet(id);
			if(obj == null)continue;
			_objets.put(obj.getGuid(), obj);
		}
		_xp = xp;
		_kamas = kamas;
	}
	
	public ArrayList<Drop> getDrops() {
		ArrayList<Drop> toReturn = new ArrayList<>();
		for(Objeto obj : _objets.values()) {
			toReturn.add(new Drop(obj.getTemplate().getID(),0, 100, obj.getQuantity()));
		}
		return toReturn;
	}
	
	public long getKamas() 
	{
		return _kamas;
	}
	
	public void setKamas(long kamas) 
	{
		this._kamas = kamas;
	}
	
	public long getXp() 
	{
		return _xp;
	}
	
	public void setXp(long xp) 
	{
		this._xp = xp;
	}
	
	public Map<Integer, Objeto> getObjets()
	{
		return _objets;
	}
	
	public void removeObjet(int guid)
	{
		_objets.remove(guid);
	}
	
	public boolean HaveObjet(int guid)
	{
		if(_objets.get(guid) != null)
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	public void remove_timeTurn(long time)
	{
		_timeTurn -= time;
	}
	
	public void set_timeTurn(long time)
	{
		_timeTurn = time;
	}
	
	public long get_turnTimer()
	{
		return _timeTurn;
	}
	
	public static String parseGM(Mapa map)
	{
		StringBuilder sock = new StringBuilder();
		sock.append("GM|");
		boolean isFirst = true;
		for(Entry<Integer, Recaudador> perco :  Mundo.getPercos().entrySet())
		{
			if(perco.getValue()._inFight > 0) continue;//On affiche pas le perco si il est en combat
			if(perco.getValue()._MapID == map.getID())
			{
				if(!isFirst) sock.append("|");
				sock.append("+");
				sock.append(perco.getValue()._cellID).append(";");
				sock.append(perco.getValue()._orientation).append(";");
				sock.append("0").append(";");
				sock.append(perco.getValue()._guid).append(";");
				sock.append(perco.getValue()._N1).append(",").append(perco.getValue()._N2).append(";");
				sock.append("-6").append(";");
				sock.append("6000^");
				Gremio G = Mundo.getGuild(perco.getValue()._GuildID);
				sock.append(50+G.get_lvl()).append(";");
				sock.append(G.get_lvl()).append(";");
				sock.append(G.get_name()).append(";"+G.get_emblem());
				isFirst = false;
			}else
			{
				continue;
			}
		}
		return sock.toString();
	}
	
	public int get_guildID() {
		return _GuildID;
	}
	
	public void DelPerco(int percoGuid)
	{
		for(Objeto obj : _objets.values())
		{
			//On supprime les objets non ramasser/drop
			Mundo.removeItem(obj.guid);
		}
		Mundo.getPercos().remove(percoGuid);
	}
	
	public int get_inFight()
	{
		return _inFight;
	}
	
	public void set_inFight(byte fight)
	{
		_inFight = fight;
	}
	
	public int getGuid()
	{
		return _guid;
	}
	
	public int get_cellID()
	{
		return _cellID;
	}
	
	public void set_inFightID(int ID)
	{
		_inFightID = ID;
	}
	
	public int get_inFightID()
	{
		return _inFightID;
	}
	
	public short get_mapID()
	{
		return _MapID;
	}
	
	public int get_N1()
	{
		return _N1;
	}
	
	public int get_N2()
	{
		return _N2;
	}
	
	public static String parsetoGuild(int GuildID)
	{
		StringBuilder packet = new StringBuilder();
		boolean isFirst = true;
		for(Entry<Integer, Recaudador> perco : Mundo.getPercos().entrySet())
		{
			 if(perco.getValue().get_guildID() == GuildID)
    		 {
				 	Mapa map = Mundo.getCarte(perco.getValue().get_mapID());
				 	if(isFirst) 
				 		packet.append("+");
	    			if(!isFirst) packet.append("|");
	    			packet.append(perco.getValue().getGuid()).append(";").append(perco.getValue().get_N1()).append(",").append(perco.getValue().get_N2()).append(";");
	    			
	    			packet.append(Integer.toString(map.getID(), 36)).append(",").append(map.getX()).append(",").append(map.getY()).append(";");
	    			packet.append(perco.getValue().get_inFight()).append(";");
	    			if(perco.getValue().get_inFight() == 1)
	    			{
	    				if(map.getFight(perco.getValue().get_inFightID()) == null)
	    				{
	    					packet.append(MainServidor.CONFIG_MS_FOR_START_FIGHT).append(";");//TimerActuel
	    				}else
	    				{
	    					packet.append(perco.getValue().get_turnTimer()).append(";");//TimerActuel
	    				}
	    				packet.append(MainServidor.CONFIG_MS_FOR_START_FIGHT).append(";");//TimerInit
	    				packet.append("7;");//Nombre de place maximum FIXME : En fonction de la map
	    				packet.append("?,?,");//?
	    			}else
	    			{
	    				packet.append("0;");
	    				packet.append(MainServidor.CONFIG_MS_FOR_START_FIGHT).append(";");
	    				packet.append("7;");
	    				packet.append("?,?,");
	    			}
	    			packet.append("1,2,3,4,5");
	    			
	    			//	?,?,callername,startdate(Base 10),lastHarvesterName,lastHarvestDate(Base 10),nextHarvestDate(Base 10)
	    			isFirst = false;
    		 }else
    		 {
    			 continue;
    		 }
   	 	}
		if(packet.length() == 0) packet = new StringBuilder("null");
		return packet.toString();
	}
	
	public static int GetPercoGuildID(int _id) {
		
		for(Entry<Integer, Recaudador> perco :  Mundo.getPercos().entrySet())
		{
			if(perco.getValue().get_mapID() == _id)
			{
				return perco.getValue().get_guildID();
			}
		}
		return 0;
	}
	
	public int GetPercoGuildID() {
		
		return get_guildID();
	}
	
	public static Recaudador GetPercoByMapID(short _id) {
		
		for(Entry<Integer, Recaudador> perco :  Mundo.getPercos().entrySet())
		{
			if(perco.getValue().get_mapID() == _id)
			{
				return  Mundo.getPercos().get(perco.getValue().getGuid());
			}
		}
		return null;
	}
	
	public static int CountPercoGuild(int GuildID) {
		int i = 0;
		for(Entry<Integer, Recaudador> perco :  Mundo.getPercos().entrySet())
		{
			if(perco.getValue().get_guildID() == GuildID)
			{
				i++;
			}
		}
		return i;
	}
	
	public static void parseAttaque(Personaje perso, int guildID)
	{
		for(Entry<Integer, Recaudador> perco :  Mundo.getPercos().entrySet())
		{
			if(perco.getValue().get_inFight() > 0 && perco.getValue().get_guildID() == guildID)
			{
				GestorSalida.GAME_SEND_gITp_PACKET(perso, parseAttaqueToGuild(perco.getValue().getGuid(), perco.getValue().get_mapID(), perco.getValue().get_inFightID()));
			}
		}
	}
	
	public static void parseDefense(Personaje perso, int guildID)
	{
		for(Entry<Integer, Recaudador> perco :  Mundo.getPercos().entrySet())
		{
			if(perco.getValue().get_inFight() > 0 && perco.getValue().get_guildID() == guildID)
			{
				GestorSalida.GAME_SEND_gITP_PACKET(perso, parseDefenseToGuild(perco.getValue().getGuid(), perco.getValue().get_mapID(), perco.getValue().get_inFightID()));
			}
		}
	}
	
	public static String parseAttaqueToGuild(int guid, short mapid, int fightid)
	{
		StringBuilder str = new StringBuilder();
		str.append("+").append(guid);
			
		for(Entry<Integer, Pelea> F : Mundo.getCarte(mapid).get_fights().entrySet())
		{
			//Je boucle les combats de la map bien qu'inutile :/
			//Mais cela évite le bug F.getValue().getFighters(1) == null
				if(F.getValue().get_id() == fightid)
				{
					for(Fighter f : F.getValue().getFighters(1))//Attaquants
					{
						str.append("|");
						str.append(Integer.toString(f.getPersonnage().get_GUID(), 36)).append(";");
						str.append(f.getPersonnage().getNombre()).append(";");
						str.append(f.getPersonnage().get_lvl()).append(";");
						str.append("0;");
					}
				}
		}
		return str.toString();
	}
	
	public static String parseDefenseToGuild(int guid, short mapid, int fightid)
	{
		StringBuilder str = new StringBuilder();
		str.append("+").append(guid);
			
		for(Entry<Integer, Pelea> F : Mundo.getCarte(mapid).get_fights().entrySet())
		{
			//Je boucle les combats de la map bien qu'inutile :/
			//Mais cela évite le bug F.getValue().getFighters(2) == null
				if(F.getValue().get_id() == fightid)
				{
					for(Fighter f : F.getValue().getFighters(2))//Defenseurs
					{
						if(f.getPersonnage() == null) continue;//On sort le percepteur
						str.append("|");
						str.append(Integer.toString(f.getPersonnage().get_GUID(), 36)).append(";");
						str.append(f.getPersonnage().getNombre()).append(";");
						str.append(f.getPersonnage().get_gfxID()).append(";");
						str.append(f.getPersonnage().get_lvl()).append(";");
						str.append(Integer.toString(f.getPersonnage().get_color1(), 36)).append(";");
						str.append(Integer.toString(f.getPersonnage().get_color2(), 36)).append(";");
						str.append(Integer.toString(f.getPersonnage().get_color3(), 36)).append(";");
						str.append("0;");
					}
				}
		}
		return str.toString();
	}
	
	public String getItemPercepteurList()
	{
		StringBuilder items = new StringBuilder();
		if(!_objets.isEmpty())
		{
			for(Objeto obj : _objets.values())
			{
				items.append("O").append(obj.parseItem()).append(";");
			}
		}
		if(_kamas != 0) items.append("G").append(_kamas);
		return items.toString();
	}
	
	public String parseItemPercepteur()
	{
		String items = "";
		for(Objeto obj : _objets.values())
		{
			items+= obj.guid+"|";
		}
		return items;
	}
	
	
	public void removeFromPercepteur(Personaje P, int guid, int qua)
	{
		Objeto PercoObj = Mundo.getObjet(guid);
		Objeto PersoObj = P.getSimilarItem(PercoObj);
		
		int newQua = PercoObj.getQuantity() - qua;
		
		if(PersoObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien
			if(newQua <= 0)
			{
				//On retire l'item
				removeObjet(guid);
				//On l'ajoute au joueur
				P.addObjet(PercoObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(P,PercoObj);
				String str = "O-"+guid;
				GestorSalida.GAME_SEND_EsK_PACKET(P, str);
				
			}else //S'il reste des objets
			{
				//On crée une copy de l'item
				PersoObj = Objeto.getCloneObjet(PercoObj, qua);
				//On l'ajoute au monde
				Mundo.addObjet(PersoObj, true);
				//On retire X objet
				PercoObj.setQuantity(newQua);
				//On l'ajoute au joueur
				P.addObjet(PersoObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(P,PersoObj);
				String str = "O+"+PercoObj.getGuid()+"|"+PercoObj.getQuantity()+"|"+PercoObj.getTemplate().getID()+"|"+PercoObj.parseStatsString();
				GestorSalida.GAME_SEND_EsK_PACKET(P, str);
				
			}
		}
		else
		{
			//S'il ne reste rien
			if(newQua <= 0)
			{
				//On retire l'item
				this.removeObjet(guid);
				Mundo.removeItem(PercoObj.getGuid());
				//On Modifie la quantité de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + PercoObj.getQuantity());
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				String str = "O-"+guid;
				GestorSalida.GAME_SEND_EsK_PACKET(P, str);
				
			}
			else//S'il reste des objets
			{
				//On retire X objet
				PercoObj.setQuantity(newQua);
				//On ajoute X objets
				PersoObj.setQuantity(PersoObj.getQuantity() + qua);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P,PersoObj);
				String str = "O+"+PercoObj.getGuid()+"|"+PercoObj.getQuantity()+"|"+PercoObj.getTemplate().getID()+"|"+PercoObj.parseStatsString();
				GestorSalida.GAME_SEND_EsK_PACKET(P, str);
				
			}
		}
		GestorSalida.GAME_SEND_Ow_PACKET(P);
		GestorSQL.guardar_personaje(P, true);
	}
	
	public void LogXpDrop(long Xp)
	{
		_LogXP += Xp;
	}
	
	public void LogObjetDrop(int guid, Objeto obj)
	{
		_LogObjets.put(guid, obj);
	}
	
	public long get_LogXp()
	{
		return _LogXP;
	}
	
	public String get_LogItems()
	{
		StringBuilder str = new StringBuilder();
		if(_LogObjets.isEmpty()) return "";
		for(Objeto obj : _LogObjets.values())
			str.append(";").append(obj.getTemplate().getID()).append(",").append(obj.getQuantity());
		return str.toString();
	}
	
	public void addObjet(Objeto newObj)
	{
		_objets.put(newObj.getGuid(), newObj);
	}
	
	public void set_Exchange(boolean Exchange)
	{
		_inExchange = Exchange;
	}
	
	public boolean get_Exchange()
	{
		return _inExchange;
	}
	
	public static void removePercepteur(int GuildID)
	{
		for(Entry<Integer, Recaudador> perco : Mundo.getPercos().entrySet())
		{
			if(perco.getValue().get_guildID() == GuildID)
			{
				Mundo.getPercos().remove(perco.getKey());
				for(Personaje p : Mundo.getCarte(perco.getValue().get_mapID()).getPersos())
				{
					GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(p.getActualMapa(), perco.getValue().getGuid());//Suppression visuelle
				}
				GestorSQL.eliminar_recaudador(perco.getKey());//Supprime les percepteurs
			}else
			{
				continue;
			}
		}
	}
}