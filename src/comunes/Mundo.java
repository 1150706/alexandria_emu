package comunes;

import juego.JuegoServidor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import objetos.*;
import objetos.Mercadillo.HdvEntry;
import objetos.NPCModelo.*;
import objetos.Objeto.ObjTemplate;
import objetos.Personaje.Stats;
import objetos.casas.Casas;
import objetos.casas.Cofres;
import objetos.hechizos.Hechizos;

public class Mundo {

	private static final Map<Integer, Cuenta> Cuentas = new TreeMap<>();
	private static final Map<String,Integer> CuentaPorNombre = new TreeMap<>();
	private static final StringBuilder Retos = new StringBuilder();
	private static final Map<Integer, Personaje> Personajes = new TreeMap<>();
	private static final Map<Short, Mapa> Mapas = new TreeMap<>();
	private static final Map<Integer, Objeto> Objetos = new TreeMap<>();
	private static final Map<Integer,ExpLevel> Experiencias = new TreeMap<>();
	private static final Map<Integer, Hechizos> Hechizos = new TreeMap<>();
	private static final Map<Integer,ObjTemplate> ObjTemplates = new TreeMap<>();
	private static final Map<Integer, Monstruo> MobTemplates = new TreeMap<>();
	private static final Map<Integer, NPCModelo> NPCTemplates = new TreeMap<>();
	private static final Map<Integer,NPC_question> NPCQuestions = new TreeMap<>();
	private static final Map<Integer,NPC_reponse> NPCReponses = new TreeMap<>();
	private static final Map<Integer,IOTemplate> IOTemplate = new TreeMap<>();
	private static final Map<Integer, Dragopavo> Dragodindes = new TreeMap<>();
	private static final Map<Integer,SuperArea> SuperAreas = new TreeMap<>();
	private static final Map<Integer,Area> Areas = new TreeMap<>();
	private static final Map<Integer,SubArea> SubAreas = new TreeMap<>();
	private static final Map<Integer, Oficio> Jobs = new TreeMap<>();
	private static final Map<Integer,ArrayList<Couple<Integer,Integer>>> Crafts = new TreeMap<>();
	private static final Map<Integer,ItemSet> ItemSets = new TreeMap<>();
	private static final Map<Integer, Gremio> Guildes = new TreeMap<>();
	private static final Map<Integer, Mercadillo> Hdvs = new TreeMap<>();
	private static final Map<Integer,Map<Integer,ArrayList<HdvEntry>>> _hdvsItems = new HashMap<>();	//Contient tout les items en ventes des comptes dans le format<compteID,<hdvID,items<>>>
	private static final Map<Integer, Personaje> Married = new TreeMap<>();
	private static final Map<Integer, Animaciones> Animations = new TreeMap<>();
	private static final Map<Short, Mapa.MountPark> MountPark = new TreeMap<>();
	private static final Map<Integer, Cofres> Trunks = new TreeMap<>();
	private static final Map<Integer, Recaudador> Percepteurs = new TreeMap<>();
	private static final Map<Integer, Casas> Houses = new TreeMap<>();
	private static final Map<Short,Collection<Integer>> Seller	= new TreeMap<>();
	public static final Map<Integer, Quest.Step> Steps = new TreeMap<>();
	public static final Map<Integer, Quest> Quests = new TreeMap<>();
	public static final ArrayList<String> Publicidad = new ArrayList<>();
	
	 
	private static int nextHdvID;	//Contient le derniere ID utilis? pour cr?e un HDV, pour obtenir un ID non utilis? il faut imp?rativement l'incr?menter
	private static int nextLigneID;	//Contient le derniere ID utilis? pour cr?e une ligne dans un HDV
	
	private static int saveTry = 1;
	//Statut du serveur 1: accesible; 0: inaccesible; 2: sauvegarde
	private static short _state = 1;
	
	private static byte _GmAccess = 0;
	
	private static int nextObjetID; //Contient le derniere ID utilis? pour cr?e un Objet
	
	public static void ticAllFightersTurns() {
	    for (Mapa map : Mapas.values())
	      try {
	        if (map != null)
	        	if(map.get_fights() != null)
	        		if(map.getNbrFight() > 0)
	        			for (Pelea f : map.get_fights().values()) {
	        				if (f == null) continue;
	        				try {
	        					f.ticMyTimer();
	        				} catch (Exception e2) {
	        					GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC-N2", "ERREUR FATALE !!! Inside ticAllFightersTurns().f.ticMyTimer(); " + e2.getMessage());
	        					GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC-N2", ", mapID: " + map.getID());
	        				}
	        			}
	      	} catch (Exception e) {
	      		GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "ERREUR FATALE !!! Inside ticAllFightersTurns(); " + e.getMessage());
	      		GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", ", mapID: " + map.getID());
	      	}
	  	}
	
	public static void addStep(Quest.Step s)
	{
		Steps.put(s.get_id(), s);
	}
	
	public static Quest.Step getStep(int guid) {
		return Steps.get(guid);
	}
	
	public static void addquest(Quest q) {
		Quests.put(q.get_id(), q);
		System.out.println("Quest ID : " + q.get_id());
		System.out.println("Ali ID : " + q.get_ali());
		System.out.println("Lvl : " + q.get_lvl());
	}
	
	public static Quest getQuest(int q) {
		return Quests.get(q);
	}
	
	public static class Drop
	{
		private final int _itemID;
		private final int _prosp;
		private final float _taux;
		private int _max;
		
		public Drop(int itm,int p,float t,int m)
		{
			_itemID = itm;
			_prosp = p;
			_taux = t;
			_max = m;
		}
		public void setMax(int m)
		{
			_max = m;
		}
		public int get_itemID() {
			return _itemID;
		}

		public int getMinProsp() {
			return _prosp;
		}

		public float get_taux() {
			return _taux;
		}

		public int get_max() {
			return _max;
		}
	}

	public static class ItemSet
	{
		private final int _id;
		private final ArrayList<ObjTemplate> _itemTemplates = new ArrayList<>();
		private final ArrayList<Stats> _bonuses = new ArrayList<>();
		
		public ItemSet (int id,String items, String bonuses)
		{
			_id = id;
			//parse items String
			for(String str : items.split(","))
			{
				try
				{
					ObjTemplate t = Mundo.getObjTemplate(Integer.parseInt(str.trim()));
					if(t == null)continue;
					_itemTemplates.add(t);
				}catch(Exception ignored){}
			}
			
			//on ajoute un bonus vide pour 1 item
			_bonuses.add(new Stats());
			//parse bonuses String
			for(String str : bonuses.split(";"))
			{
				Stats S = new Stats();
				//s?paration des bonus pour un m?me nombre d'item
				for(String str2 : str.split(","))
				{
					try
					{
						String[] infos = str2.split(":");
						int stat = Integer.parseInt(infos[0]);
						int value = Integer.parseInt(infos[1]);
						//on ajoute a la stat
						S.addOneStat(stat, value);
					}catch(Exception ignored){}
				}
				//on ajoute la stat a la liste des bonus
				_bonuses.add(S);
			}
		}

		public int getId()
		{
			return _id;
		}
		
		public Stats getBonusStatByItemNumb(int numb)
		{
			if(numb>_bonuses.size())return new Stats();
			return _bonuses.get(numb-1);
		}
		
		public ArrayList<ObjTemplate> getItemTemplates()
		{
			return _itemTemplates;
		}
	}
	
	public static class SuperArea
	{
		private final int _id;
		private final ArrayList<Area> _areas = new ArrayList<>();
		
		public SuperArea(int a_id)
		{
			_id = a_id;
		}
		
		public void addArea(Area A)
		{
			_areas.add(A);
		}
		
		public int get_id()
		{
			return _id;
		}
	}
	
	public static class Area
	{
		private final int _id;
		private SuperArea _superArea;
		private final String _name;
		private final ArrayList<SubArea> _subAreas = new ArrayList<>();
		
		public Area(int id, int superArea,String name)
		{
			_id = id;
			_name = name;
			_superArea = Mundo.getSuperArea(superArea);
			//Si le continent n'est pas encore cr?er, on le cr?er et on l'ajoute au monde
			if(_superArea == null)
			{
				_superArea = new SuperArea(superArea);
				Mundo.addSuperArea(_superArea);
			}
		}
		public String get_name()
		{
			return _name;
		}
		public int get_id()
		{
			return _id;
		}
		
		public SuperArea get_superArea()
		{
			return _superArea;
		}
		
		public void addSubArea(SubArea sa)
		{
			_subAreas.add(sa);
		}
		
		public ArrayList<Mapa> getMaps()
		{
			ArrayList<Mapa> maps = new ArrayList<>();
			for(SubArea SA : _subAreas)maps.addAll(SA.getMaps());
			return maps;
		}
	}
	
	public static class SubArea
	{
		private final int _id;
		private final Area _area;
		private final int _alignement;
		private final String _name;
		private final ArrayList<Mapa> _maps = new ArrayList<>();
		
		public SubArea(int id, int areaID, int alignement,String name)
		{
			_id = id;
			_name = name;
			_area =  Mundo.getArea(areaID);
			_alignement = alignement;
		}
		
		public String get_name()
		{
			return _name;
		}
		public int get_id() {
			return _id;
		}
		public Area get_area() {
			return _area;
		}
		public int get_alignement() {
			return _alignement;
		}
		public ArrayList<Mapa> getMaps() {
			return _maps;
		}

		public void addMap(Mapa carte)
		{
			_maps.add(carte);
		}
		
	}
	
	public static class Couple<L,R>
	{
	    public final L first;
	    public R second;

	    public Couple(L s, R i)
	    {
	         this.first = s;
	         this.second = i;
	    }
	}

	public static class IOTemplate
	{
		private final int _id;
		private final int _respawnTime;
		private final int _duration;
		private final int _unk;
		private final boolean _walkable;
		
		public IOTemplate(int a_i,int a_r,int a_d,int a_u, boolean a_w)
		{
			_id = a_i;
			_respawnTime = a_r;
			_duration = a_d;
			_unk = a_u;
			_walkable = a_w;
		}
		
		public int getId() {
			return _id;
		}	
		public boolean isWalkable() {
			return _walkable;
		}

		public int getRespawnTime() {
			return _respawnTime;
		}
		public int getDuration() {
			return _duration;
		}
		public int getUnk() {
			return _unk;
		}
	}
	
	public static class Exchange
	{
		private final Personaje perso1;
		private final Personaje perso2;
		private long kamas1 = 0;
		private long kamas2 = 0;
		private final ArrayList<Couple<Integer,Integer>> items1 = new ArrayList<>();
		private final ArrayList<Couple<Integer,Integer>> items2 = new ArrayList<>();
		private boolean ok1;
		private boolean ok2;
		
		public Exchange(Personaje p1, Personaje p2)
		{
			perso1 = p1;
			perso2 = p2;
		}
		
		synchronized public long getKamas(int guid)
		{
			int i = 0;
			if(perso1.get_GUID() == guid)
				i = 1;
			else if(perso2.get_GUID() == guid)
				i = 2;
			
			if(i == 1)
				return kamas1;
			else if (i == 2)
				return kamas2;
			return 0;
		}
		
		synchronized public void toogleOK(int guid)
		{
			int i = 0;
			if(perso1.get_GUID() == guid)
				i = 1;
			else if(perso2.get_GUID() == guid)
				i = 2;
			
			if(i == 1)
			{
				ok1 = !ok1;
				GestorSalida.GAME_SEND_EXCHANGE_OK(perso1.getCuenta().getGameThread().get_out(),ok1,guid);
				GestorSalida.GAME_SEND_EXCHANGE_OK(perso2.getCuenta().getGameThread().get_out(),ok1,guid);
			}
			else if (i == 2)
			{
				ok2 = !ok2;
				GestorSalida.GAME_SEND_EXCHANGE_OK(perso1.getCuenta().getGameThread().get_out(),ok2,guid);
				GestorSalida.GAME_SEND_EXCHANGE_OK(perso2.getCuenta().getGameThread().get_out(),ok2,guid);
			}
			else 
				return;
			
			
			if(ok1 && ok2)
				apply();
		}
		
		synchronized public void setKamas(int guid, long k)
		{
			ok1 = false;
			ok2 = false;
			
			int i = 0;
			if(perso1.get_GUID() == guid)
				i = 1;
			else if(perso2.get_GUID() == guid)
				i = 2;
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso1.getCuenta().getGameThread().get_out(),ok1,perso1.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso2.getCuenta().getGameThread().get_out(),ok1,perso1.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso1.getCuenta().getGameThread().get_out(),ok2,perso2.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso2.getCuenta().getGameThread().get_out(),ok2,perso2.get_GUID());
			
			if(i == 1)
			{
				kamas1 = k;
				GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'G', "", k+"");
				GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getCuenta().getGameThread().get_out(), 'G', "", k+"");
			}else if (i == 2)
			{
				kamas2 = k;
				GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getCuenta().getGameThread().get_out(), 'G', "", k+"");
				GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'G', "", k+"");
			}
		}
		
		synchronized public void cancel()
		{
			if(perso1.getCuenta() != null)if(perso1.getCuenta().getGameThread() != null) GestorSalida.GAME_SEND_EV_PACKET(perso1.getCuenta().getGameThread().get_out());
			if(perso2.getCuenta() != null)if(perso2.getCuenta().getGameThread() != null) GestorSalida.GAME_SEND_EV_PACKET(perso2.getCuenta().getGameThread().get_out());
			perso1.set_isTradingWith(0);
			perso2.set_isTradingWith(0);
			perso1.setCurExchange(null);
			perso2.setCurExchange(null);
		}
		
		synchronized public void apply()
		{
			//Gestion des Kamas
			perso1.addKamas((-kamas1+kamas2));
			perso2.addKamas((-kamas2+kamas1));
			for(Couple<Integer, Integer> couple : items1)
			{
				if(couple.second == 0)continue;
				if(!perso1.hasItemGuid(couple.first))//Si le perso n'a pas l'item (Ne devrait pas arriver)
				{
					couple.second = 0;//On met la quantit? a 0 pour ?viter les problemes
					continue;
				}	
				Objeto obj = Mundo.getObjet(couple.first);
				if((obj.getQuantity() - couple.second) <1)//S'il ne reste plus d'item apres l'?change
				{
					perso1.removeItem(couple.first);
					couple.second = obj.getQuantity();
					GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(perso1, couple.first);
					if(!perso2.addObjet(obj, true))//Si le joueur avait un item similaire
						Mundo.removeItem(couple.first);//On supprime l'item inutile
				}else
				{
					obj.setQuantity(obj.getQuantity()-couple.second);
					GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(perso1, obj);
					Objeto newObj = Objeto.getCloneObjet(obj, couple.second);
					if(perso2.addObjet(newObj, true))//Si le joueur n'avait pas d'item similaire
						Mundo.addObjet(newObj,true);//On ajoute l'item au World
				}
			}
			for(Couple<Integer, Integer> couple : items2)
			{
				if(couple.second == 0)continue;
				if(!perso2.hasItemGuid(couple.first))//Si le perso n'a pas l'item (Ne devrait pas arriver)
				{
					couple.second = 0;//On met la quantit? a 0 pour ?viter les problemes
					continue;
				}	
				Objeto obj = Mundo.getObjet(couple.first);
				if((obj.getQuantity() - couple.second) <1)//S'il ne reste plus d'item apres l'?change
				{
					perso2.removeItem(couple.first);
					couple.second = obj.getQuantity();
					GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(perso2, couple.first);
					if(!perso1.addObjet(obj, true))//Si le joueur avait un item similaire
						Mundo.removeItem(couple.first);//On supprime l'item inutile
				}else
				{
					obj.setQuantity(obj.getQuantity()-couple.second);
					GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(perso2, obj);
					Objeto newObj = Objeto.getCloneObjet(obj, couple.second);
					if(perso1.addObjet(newObj, true))//Si le joueur n'avait pas d'item similaire
						Mundo.addObjet(newObj,true);//On ajoute l'item au World
				}
			}
			//Fin
			perso1.set_isTradingWith(0);
			perso2.set_isTradingWith(0);
			perso1.setCurExchange(null);
			perso2.setCurExchange(null);
			GestorSalida.GAME_SEND_Ow_PACKET(perso1);
			GestorSalida.GAME_SEND_Ow_PACKET(perso2);
			GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso1);
			GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso2);
			GestorSalida.GAME_SEND_EXCHANGE_VALID(perso1.getCuenta().getGameThread().get_out(),'a');
			GestorSalida.GAME_SEND_EXCHANGE_VALID(perso2.getCuenta().getGameThread().get_out(),'a');
			GestorSQL.guardar_personaje(perso1,true);
			GestorSQL.guardar_personaje(perso2,true);
		}

		synchronized public void addItem(int guid, int qua, int pguid)
		{
			ok1 = false;
			ok2 = false;
			
			Objeto obj = Mundo.getObjet(guid);
			int i = 0;
			
			if(perso1.get_GUID() == pguid) i = 1;
			if(perso2.get_GUID() == pguid) i = 2;
			
			if(qua == 1) qua = 1;
			String str = guid+"|"+qua;
			if(obj == null)return;
			String add = "|"+obj.getTemplate().getID()+"|"+obj.parseStatsString();
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso1.getCuenta().getGameThread().get_out(),ok1,perso1.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso2.getCuenta().getGameThread().get_out(),ok1,perso1.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso1.getCuenta().getGameThread().get_out(),ok2,perso2.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso2.getCuenta().getGameThread().get_out(),ok2,perso2.get_GUID());
			if(i == 1)
			{
				Couple<Integer,Integer> couple = getCoupleInList(items1,guid);
				if(couple != null)
				{
					couple.second += qua;
					GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", ""+guid+"|"+couple.second);
					GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getCuenta().getGameThread().get_out(), 'O', "+", ""+guid+"|"+couple.second+add);
					return;
				}
				GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", str);
				GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getCuenta().getGameThread().get_out(), 'O', "+", str+add);
				items1.add(new Couple<>(guid, qua));
			}else if(i == 2)
			{
				Couple<Integer,Integer> couple = getCoupleInList(items2,guid);
				if(couple != null)
				{
					couple.second += qua;
					GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", ""+guid+"|"+couple.second);
					GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getCuenta().getGameThread().get_out(), 'O', "+", ""+guid+"|"+couple.second+add);
					return;
				}
				GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", str);
				GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getCuenta().getGameThread().get_out(), 'O', "+", str+add);
				items2.add(new Couple<>(guid, qua));
			}
		}

		
		synchronized public void removeItem(int guid, int qua, int pguid)
		{
			int i = 0;
			if(perso1.get_GUID() == pguid)
				i = 1;
			else if(perso2.get_GUID() == pguid)
				i = 2;
			ok1 = false;
			ok2 = false;
			
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso1.getCuenta().getGameThread().get_out(),ok1,perso1.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso2.getCuenta().getGameThread().get_out(),ok1,perso1.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso1.getCuenta().getGameThread().get_out(),ok2,perso2.get_GUID());
			GestorSalida.GAME_SEND_EXCHANGE_OK(perso2.getCuenta().getGameThread().get_out(),ok2,perso2.get_GUID());
			
			Objeto obj = Mundo.getObjet(guid);
			if(obj == null)return;
			String add = "|"+obj.getTemplate().getID()+"|"+obj.parseStatsString();
			if(i == 1)
			{
				Couple<Integer,Integer> couple = getCoupleInList(items1,guid);
				int newQua = couple.second - qua;
				if(newQua <1)//Si il n'y a pu d'item
				{
					items1.remove(couple);
					GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "-", ""+guid);
					GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getCuenta().getGameThread().get_out(), 'O', "-", ""+guid);
				}else
				{
					couple.second = newQua;
					GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", ""+guid+"|"+newQua);
					GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getCuenta().getGameThread().get_out(), 'O', "+", ""+guid+"|"+newQua+add);
				}
			}else if(i ==2)
			{
				Couple<Integer,Integer> couple = getCoupleInList(items2,guid);
				int newQua = couple.second - qua;
				
				if(newQua <1)//Si il n'y a pu d'item
				{
					items2.remove(couple);
					GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getCuenta().getGameThread().get_out(), 'O', "-", ""+guid);
					GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "-", ""+guid);
				}else
				{
					couple.second = newQua;
					GestorSalida.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getCuenta().getGameThread().get_out(), 'O', "+", ""+guid+"|"+newQua+add);
					GestorSalida.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", ""+guid+"|"+newQua);
				}
			}
		}

		synchronized private Couple<Integer, Integer> getCoupleInList(ArrayList<Couple<Integer, Integer>> items,int guid)
		{
			for(Couple<Integer, Integer> couple : items)
			{
				if(couple.first == guid)
					return couple;
			}
			return null;
		}
		
		public synchronized int getQuaItem(int itemID, int playerGuid)
		{
			ArrayList<Couple<Integer, Integer>> items;
			if(perso1.get_GUID() == playerGuid)
				items = items1;
			else
				items = items2;
			
			for(Couple<Integer, Integer> curCoupl : items)
			{
				if(curCoupl.first == itemID)
				{
					return curCoupl.second;
				}
			}
			
			return 0;
		}
		
	}

	public static class ExpLevel {
		public final long perso;
		public final int metier;
		public final int dinde;
		public final int pvp;
		public final long guilde;
		
		public ExpLevel(long c, int m, int d, int p) {
			perso = c;
			metier = m;
			dinde = d;
			pvp = p;
			guilde = perso*10;
		}
		
	}
	
	public static void createWorld() {
		System.out.println("====> Cargando datos estaticos <====");
		System.out.println("Cargando las experiencias:");
		GestorSQL.cargar_experiencias();
		System.out.println(Experiencias.size()+" niveles cargados");
		System.out.println("Chargement des sorts:");
		GestorSQL.cargar_hechizos();
		System.out.println(Hechizos.size()+" sorts ont ete charges");
		System.out.println("Chargement des templates de monstre:");
		GestorSQL.cargar_monstruo_modelo();
		System.out.println(MobTemplates.size()+" templates de monstre ont ete chargees");
		System.out.println("Chargement des templates d'objet:");
		GestorSQL.cargar_objetos_modelo();
		System.out.println(ObjTemplates.size()+" templates d'objet ont ete chargees");
		System.out.println("Chargement des templates de NPC:");
		GestorSQL.cargar_npc_modelo();
		System.out.println(NPCTemplates.size()+" templates de NPC ont ete chargees");
		System.out.println("Chargement des questions de NPC:");
		GestorSQL.cargar_preguntas_npc();
		System.out.println(NPCQuestions.size()+" questions de NPC ont ete charges");
		System.out.println("Chargement des reponses de NPC:");
		GestorSQL.cargar_respuestas_npc();
		System.out.println(NPCReponses.size()+" reponses de NPC ont ete chargees");
		System.out.println("Chargement des zones:");
		GestorSQL.cargar_area();
		System.out.println(Areas.size()+" zones ont ete chargees");
		System.out.println("Chargement des sous-zone:");
		GestorSQL.cargar_subareas();
		System.out.println(SubAreas.size()+" sous-zones ont ete chargees");
		System.out.println("Chargement des template d'objet interactifs:");
		GestorSQL.LOAD_IOTEMPLATE();
		System.out.println(IOTemplate.size()+" template d'IO ont ete charges");
		System.out.println("Chargement des recettes:");
		GestorSQL.cargar_recetas();
		System.out.println(Crafts.size()+" recettes ont ete chargses");
		System.out.println("Chargement des metiers:");
		GestorSQL.cargar_oficios();
		System.out.println(Jobs.size()+" metiers ont ete charges");
		System.out.println("Chargement des panolies:");
		GestorSQL.cargar_sets();
		System.out.println(ItemSets.size()+" panoplies ont ete chargees");
		System.out.println("Chargement des maps:");
		GestorSQL.cargar_mapas();
		System.out.println(Mapas.size()+" maps ont ete chargees");
		System.out.println("Chargement des Triggers:");
		int nbr = GestorSQL.cargar_celdas();
		System.out.println(nbr+" triggers ont ete charges");
		System.out.println("Chargement des actions de fin de combat:");
		nbr = GestorSQL.cargar_acciones_fin_pelea();
		System.out.println(nbr+" actions ont ete charges");
		System.out.println("Chargement des npcs:");
		nbr = GestorSQL.cargar_npc();
		System.out.println(nbr+" npcs ont ete chargees");
		System.out.println("Chargement des actions des objets:");
		nbr = GestorSQL.cargar_accion_objetos();
		System.out.println(nbr+" actions ont ete chargees");
		System.out.print("Chargement des Drops: ");
		GestorSQL.cargar_drops();
		System.out.println("Ok !");
		System.out.println("Chargement des Animations: ");
		GestorSQL.cargar_animaciones();
		System.out.println(Animations.size() + " ont ete chargees");
		
		System.out.println("====> Cargando los datos dinamicos <====");
		System.out.print("Mise a 0 des logged: ");
		GestorSQL.conectado_a_0();
		System.out.println("Ok !");
		System.out.print("Chargement des items: ");
		GestorSQL.cargar_maximo_de_objetos();
		System.out.println("Ok !");
		System.out.print("Chargement des comptes: ");
		GestorSQL.cargar_cuentas();
		System.out.println(Cuentas.size()+" comptes charges");
		System.out.print("Chargement des personnages: ");
		GestorSQL.cargar_personaje();
		System.out.println(Personajes.size()+" personnages charges");
	    System.out.print("Chargement des guildes: ");
		GestorSQL.cargar_gremios();
		System.out.println(Guildes.size()+" guildes chargees");
		System.out.print("Chargement des dragodindes: ");
		GestorSQL.cargar_montura();
		System.out.println(Dragodindes.size()+" dragodindes chargees");
		System.out.print("Chargement des challenges: ");
		GestorSQL.cargar_retos();
		System.out.println(Retos.toString().split(";").length+" challenges charges");
		System.out.print("Chargement des membres de guildes: ");
		GestorSQL.cargar_miembros_gremio();
		System.out.println("Ok !");
		System.out.print("Chargement des donnees d'enclos: ");
		nbr = GestorSQL.cargar_cercados();
		System.out.println(nbr+" enclos charges");
		System.out.print("Chargement des percepteurs: ");
		nbr = GestorSQL.cargar_recaudadores();
		System.out.println(nbr+" percepteurs charges");
		System.out.print("Chargement des maisons: ");
		nbr = GestorSQL.cargar_casas();
		System.out.println(nbr+" maisons chargees");
		System.out.print("Chargement des coffres: ");
		nbr = GestorSQL.cargar_cofre();
		System.out.println(nbr+" coffres chargees");
		System.out.print("Chargement des zaaps: ");
		nbr = GestorSQL.cargar_zaaps();
		System.out.println(nbr+" zaaps chargees");
		System.out.print("Chargement des zaapis: ");
		nbr = GestorSQL.cargar_zaapis();
		System.out.println(nbr+" zaapis chargees");
		System.out.print("Chargement des BAN_IP: ");
		nbr = GestorSQL.cargar_ip_baneadas();
		System.out.println(nbr+" BAN_IP chargees");
		System.out.print("Cargando los mercadillos: ");
		GestorSQL.cargar_mercadillos();
		System.out.print("\nCargando los objetos de los mercadillos: ");
		GestorSQL.cargar_objetos_mercadillos();
		nextObjetID = GestorSQL.siguiente_id_objeto();
		System.out.print("Cargando publicidades automaticas: ");
		GestorSQL.cargar_publicidades_automaticas();
		System.out.print(Publicidad.size()+" publicidades cargadas.\n");
	}
	
	public static Area getArea(int areaID)
	{
		return Areas.get(areaID);
	}

	public static SuperArea getSuperArea(int areaID)
	{
		return SuperAreas.get(areaID);
	}
	
	public static SubArea getSubArea(int areaID)
	{
		return SubAreas.get(areaID);
	}
	
	public static void addArea(Area area)
	{
		Areas.put(area.get_id(), area);
	}
	
	public static void addSuperArea(SuperArea SA)
	{
		SuperAreas.put(SA.get_id(), SA);
	}
	
	public static void addSubArea(SubArea SA)
	{
		SubAreas.put(SA.get_id(), SA);
	}
	
	public static void addNPCreponse(NPC_reponse rep)
	{
		NPCReponses.put(rep.get_id(), rep);
	}
	
	public static NPC_reponse getNPCreponse(int guid)
	{
		return NPCReponses.get(guid);
	}
	
	public static int getExpLevelSize()
	{
		return Experiencias.size();
	}
	
	public static void addExpLevel(int lvl,ExpLevel exp)
	{
		Experiencias.put(lvl, exp);
	}
	
	public static Cuenta getCompte(int guid)
	{
		return Cuentas.get(guid);
	}
	
	public static void addNPCQuestion(NPC_question quest)
	{
		NPCQuestions.put(quest.get_id(), quest);
	}
	
	public static NPC_question getNPCQuestion(int guid)
	{
		return NPCQuestions.get(guid);
	}
	public static NPCModelo getNPCTemplate(int guid)
	{
		return NPCTemplates.get(guid);
	}
	
	public static void addNpcTemplate(NPCModelo temp)
	{
		NPCTemplates.put(temp.getID(), temp);
	}
	
	public static Mapa getCarte(short id)
	{
		return Mapas.get(id);
	}
	
	public static  void addCarte(Mapa map) {
		if(!Mapas.containsKey(map.getID()))
			Mapas.put(map.getID(),map);
	}
	
	public static void delCarte(Mapa map) {
		Mapas.remove(map.getID());
	}
	
	public static Cuenta getCompteByName(String name) {
		return (CuentaPorNombre.get(name.toLowerCase())!=null? Cuentas.get(CuentaPorNombre.get(name.toLowerCase())):null);
	}
	
	public static Personaje getPersonnage(int guid)
	{
		return Personajes.get(guid);
	}
	
	public static void addAccount(Cuenta compte) {
		Cuentas.put(compte.get_GUID(), compte);
		CuentaPorNombre.put(compte.getNombre().toLowerCase(), compte.get_GUID());
	}
	
	public static void addChallenge(String chal) {
		//ChalID,gainXP,gainDrop,gainParMob,Conditions;...
		if(!Retos.toString().isEmpty())
			Retos.append(";");
		Retos.append(chal);
	}
	
	public static String getChallengeFromConditions(boolean sevEnn, boolean sevAll, boolean bothSex, boolean EvenEnn,boolean MoreEnn,boolean hasCaw,boolean hasChaf,boolean hasRoul,boolean hasArak, boolean isBoss) {
		String noBossChals = ";2;5;9;17;19;24;38;47;50;"; // ceux impossibles contre boss
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true, isGood = false;
		int cond = 0;
		for(String chal : Retos.toString().split(";")) {
			if(!isFirst && isGood)
				toReturn.append(";");
			isGood = true;
			cond = Integer.parseInt(chal.split(",")[4]);
			//N?cessite plusieurs ennemis
			if(((cond & 1) == 1) && !sevEnn)
				isGood = false;
			//N?cessite plusieurs alli?s
			if((((cond>>1) & 1) == 1) && !sevAll)
				isGood = false;
			//N?cessite les deux sexes
			if((((cond>>2) & 1) == 1) && !sevAll)
				isGood = false;
			// N?cessite un nombre pair d'ennemis
			if((((cond>>3) & 1) == 1) && !sevAll)
				isGood = false;
			// N?cessite plus d'ennemis que d'alli?s
			if((((cond>>4) & 1) == 1) && !sevAll)
				isGood = false;
			// Jardinier
			if(!hasCaw && (Integer.parseInt(chal.split(",")[0]) == 7))
				isGood = false;
			// Fossoyeur
			if(!hasChaf && (Integer.parseInt(chal.split(",")[0]) == 12))
				isGood = false;
			// Casino Royal
			if(!hasRoul && (Integer.parseInt(chal.split(",")[0]) == 14))
				isGood = false;
			// Araknophile
			if(!hasArak && (Integer.parseInt(chal.split(",")[0]) == 15))
				isGood = false;
			// Contre un boss de donjon
			if(noBossChals.contains(";"+chal.split(",")[0]+";"))
				isGood = false;
			if(isGood)
				toReturn.append(chal);
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public static ArrayList<String> getRandomChallenge(int nombreChal, String challenges) {
		String MovingChals = ";1;2;8;36;37;39;40;"; // Challenges de d?placements incompatibles
		boolean hasMovingChal = false;
		String TargetChals = ";3;4;10;25;31;32;34;35;38;42;"; // ceux qui ciblent
		boolean hasTargetChal = false;
		String SpellChals = ";5;6;9;11;19;20;24;41;"; // ceux qui obligent ? caster sp?cialement
		boolean hasSpellChal = false;
		String KillerChals = ";28;29;30;44;45;46;48;"; // ceux qui disent qui doit tuer
		boolean hasKillerChal = false;
		String HealChals = ";18;43;"; // ceux qui emp?chent de soigner
		boolean hasHealChal = false;
		
		int compteur = 0, i = 0;
		ArrayList<String> toReturn = new ArrayList<>();
		String chal;
		while(compteur < 100 && toReturn.size() < nombreChal) {
			
			compteur++;
			i = Formulas.getRandomValue(1, challenges.split(";").length);
			chal = challenges.split(";")[i-1]; // challenge au hasard dans la liste
			
			if(!toReturn.contains(chal)) {// si le challenge n'y ?tait pas encore
				if(MovingChals.contains(";"+chal.split(",")[0]+";")) // s'il appartient ? une liste 
					if(!hasMovingChal) { // et qu'aucun de la liste n'a ?t? choisi d?j?
						hasMovingChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if(TargetChals.contains(";"+chal.split(",")[0]+";")) 
					if(!hasTargetChal) {
						hasTargetChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if(SpellChals.contains(";"+chal.split(",")[0]+";")) 
					if(!hasSpellChal) {
						hasSpellChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if(KillerChals.contains(";"+chal.split(",")[0]+";"))
					if(!hasKillerChal) {
						hasKillerChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				if(HealChals.contains(";"+chal.split(",")[0]+";"))
					if(!hasHealChal) {
						hasHealChal = true;
						toReturn.add(chal);
						continue;
					} else continue;
				toReturn.add(chal); // s'il n'appartient ? aucune liste
					
			}
			compteur++;
		}
		//System.out.println(toReturn.toString());
		return toReturn;
	}

	public static void addAccountbyName(Cuenta compte)
	{
		CuentaPorNombre.put(compte.getNombre(), compte.get_GUID());
	}

	public static void addPersonnage(Personaje perso)
	{
		Personajes.put(perso.get_GUID(), perso);
	}

	public static Personaje getPersonajePorNombre(String nombre) {
		ArrayList<Personaje> personajes = new ArrayList<>(Personajes.values());
		for(Personaje personaje : personajes)
			if(personaje.getNombre().equalsIgnoreCase(nombre))
				return personaje;
		return null;
	}

	public static void deletePerso(Personaje perso) {
		if(perso.get_guild() != null) {
			if(perso.get_guild().getMembers().size() <= 1)//Il est tout seul dans la guilde : Supression
			{
				Mundo.removeGuild(perso.get_guild().get_id());
			}else if(perso.getMiembroGremio().getRank() == 1)//On passe les pouvoir a celui qui a le plus de droits si il est meneur
			{
				int curMaxRight = 0;
				Personaje Meneur = null;
				for(Personaje newMeneur : perso.get_guild().getMembers()) {
					if(newMeneur == perso) continue;
					if(newMeneur.getMiembroGremio().getRights() < curMaxRight) {
						Meneur = newMeneur;
					}
				}
				perso.get_guild().removeMember(perso);
				Meneur.getMiembroGremio().setRank(1);
			}else//Supression simple
			{
				perso.get_guild().removeMember(perso);
			}
		}
		perso.remove();//Supression BDD Perso, items, monture.
		Mundo.unloadPerso(perso.get_GUID());//UnLoad du perso+item
	}

	public static String getSousZoneStateString() {
		String data = "";
		/* TODO: Sous Zone Alignement */
		return data;
	}
	
	public static long getPersoXpMin(int _lvl) {
		if(_lvl > getExpLevelSize()) 	_lvl = getExpLevelSize();
		if(_lvl < 1) 	_lvl = 1;
		return Experiencias.get(_lvl).perso;
	}
	
	public static long getPersoXpMax(int _lvl) {
		if(_lvl >= getExpLevelSize()) 	_lvl = (getExpLevelSize()-1);
		if(_lvl <= 1)	 	_lvl = 1;
		return Experiencias.get(_lvl+1).perso;
	}
	
	public static void addSort(Hechizos sort)
	{
		Hechizos.put(sort.getSpellID(), sort);
	}

	public static void addObjTemplate(ObjTemplate obj)
	{
		ObjTemplates.put(obj.getID(), obj);
	}
	
	public static Hechizos getSort(int id)
	{
		return Hechizos.get(id);
	}

	public static ObjTemplate getObjTemplate(int id)
	{
		return ObjTemplates.get(id);
	}
	
	public synchronized static int getNewItemGuid() {
		return nextObjetID++;
	}

	public static void addMobTemplate(int id, Monstruo mob)
	{
		MobTemplates.put(id, mob);
	}

	public static Monstruo getMonstre(int id)
	{
		return MobTemplates.get(id);
	}

	public static List<Personaje> getOnlinePersos() {
		List<Personaje> online = new ArrayList<>();
		for(Entry<Integer, Personaje> perso : Personajes.entrySet()) {
			if(perso.getValue().isConectado() && perso.getValue().getCuenta().getGameThread() != null) {
				if(perso.getValue().getCuenta().getGameThread().get_out() != null) {
					online.add(perso.getValue());
				}
			}
		}
		return online;
	}

	public static void addObjet(Objeto item, boolean saveSQL) {
		Objetos.put(item.getGuid(), item);
		if(saveSQL)
			GestorSQL.guardar_nuevo_objeto(item);
	}

	public static Objeto getObjet(int guid)
	{
		return Objetos.get(guid);
	}

	public static void removeItem(int guid) {
		Objetos.remove(guid);
		GestorSQL.eliminar_objeto(guid);
	}

	public static void addIOTemplate(IOTemplate IOT)
	{
		IOTemplate.put(IOT.getId(), IOT);
	}
	
	public static Dragopavo getDragopavoPorID(int id)
	{
		return Dragodindes.get(id);
	}
	
	public static void addDragopavo(Dragopavo dragopavo)
	{
		Dragodindes.put(dragopavo.getID(), dragopavo);
	}

	public static void removeDragodinde(int DID)
	{
		Dragodindes.remove(DID);
	}

	public static void saveAll(Personaje saver) {
		PrintWriter _out = null;
		if(saver != null)
		_out = saver.getCuenta().getGameThread().get_out();
		
		set_state((short)2);

		try {
			JuegoServidor.addToLog("Lancement de la sauvegarde du Monde...");
			MainServidor.isSaving = true;
			
			//Thread.sleep(10000);
			
			JuegoServidor.addToLog("Sauvegarde des personnages...");
			for(Personaje perso : Personajes.values()) {
				if(!perso.isConectado())continue;
				Thread.sleep(100);//0.1 sec. pour 1 objets
				GestorSQL.guardar_personaje(perso,true);//sauvegarde des persos et de leurs items
			}
			
			//Thread.sleep(2500);
			
			JuegoServidor.addToLog("Sauvegarde des guildes...");
			for(Gremio guilde : Guildes.values()) {
				Thread.sleep(100);//0.1 sec. pour 1 guilde
				GestorSQL.actualizar_gremio(guilde);
			}
			
			//Thread.sleep(2500);
			
			JuegoServidor.addToLog("Sauvegarde des percepteurs...");
			for(Recaudador perco : Percepteurs.values()) {
				if(perco.get_inFight()>0)continue;
				Thread.sleep(100);//0.1 sec. pour 1 percepteur
				GestorSQL.actualizar_recaudador(perco);
			}
			
			//Thread.sleep(2500);
			
			JuegoServidor.addToLog("Sauvegarde des maisons...");
			for(Casas house : Houses.values()) {
				if(house.get_owner_id() > 0) {
					Thread.sleep(100);//0.1 sec. pour 1 maison
					GestorSQL.actualizar_casa(house);
				}
			}
			
			//Thread.sleep(2500);
			
			JuegoServidor.addToLog("Sauvegarde des coffres...");
			for(Cofres t : Trunks.values()) {
				if(t.get_owner_id() > 0) {
					Thread.sleep(100);//0.1 sec. pour 1 coffre
					GestorSQL.actualizar_cofre(t);
				}
			}
			
			//Thread.sleep(2500);
			
			JuegoServidor.addToLog("Sauvegarde des enclos...");
			for(Mapa.MountPark mp : MountPark.values()) {
				if(mp.get_owner() > 0 || mp.get_owner() == -1) {
					Thread.sleep(100);//0.1 sec. pour 1 enclo
					GestorSQL.actualizar_cercado(mp);
				}
			}
			
			//Thread.sleep(2500);
			
			JuegoServidor.addToLog("Sauvegarde des Hdvs...");
			ArrayList<HdvEntry> toSave = new ArrayList<>();
			for(Mercadillo curHdv : Hdvs.values()) {
				toSave.addAll(curHdv.getAllEntry());
			}
			GestorSQL.guardar_objetos_mercadillo(toSave);
			
			//Thread.sleep(10000);
			
			JuegoServidor.addToLog("Sauvegarde effectuee !");
			
			set_state((short)1);
			//TODO : Rafraichir
			
		}catch(ConcurrentModificationException e) {
			if(saveTry < 10) {
				JuegoServidor.addToLog("Nouvelle tentative de sauvegarde");
				if(saver != null && _out != null)
					GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_out, "Erreur. Nouvelle tentative de sauvegarde");
				saveTry++;
				saveAll(saver);
			} else {
				set_state((short)1);
				//TODO : Rafraichir 
				String mess = "Echec de la sauvegarde apres " + saveTry + " tentatives";
				if(saver != null && _out != null)
					GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_out, mess);
				JuegoServidor.addToLog(mess);
			}
				
		}catch(Exception e) {
			JuegoServidor.addToLog("Erreur lors de la sauvegarde : " + e.getMessage());
			e.printStackTrace();
		} finally {
			MainServidor.isSaving = false;
			saveTry = 1;
		}
	}

	public static void RefreshAllMob() {
		GestorSalida.ENVIAR_MENSAJE_A_TODOS("Recharge des Mobs en cours, des latences peuvent survenir.", MainServidor.CONFIG_MOTD_COLOR);
		for(Mapa map : Mapas.values()) {
			map.refreshSpawns();
		}
		GestorSalida.ENVIAR_MENSAJE_A_TODOS("Recharge des Mobs finie. La prochaine recharge aura lieu dans 5heures.", MainServidor.CONFIG_MOTD_COLOR);
	}

	public static ExpLevel getExpLevel(int lvl)
	{
		return Experiencias.get(lvl);
	}

	public static IOTemplate getIOTemplate(int id)
	{
		return IOTemplate.get(id);
	}

	public static Oficio getMetier(int id)
	{
		return Jobs.get(id);
	}

	public static void addJob(Oficio metier)
	{
		Jobs.put(metier.getId(), metier);
	}

	public static void addCraft(int id, ArrayList<Couple<Integer, Integer>> m) { Crafts.put(id,m); }
	
	public static ArrayList<Couple<Integer,Integer>> getCraft(int i)
	{
		return Crafts.get(i);
	}

	public static int getObjectByIngredientForJob( ArrayList<Integer> list, Map<Integer, Integer> ingredients) {
		if(list == null)return -1;
		for(int tID : list) {
			ArrayList<Couple<Integer,Integer>> craft = Mundo.getCraft(tID);
			if(craft == null) {
				JuegoServidor.addToLog("/!\\Recette pour l'objet "+tID+" non existante !");
				continue;
			}
			if(craft.size() != ingredients.size())continue;
			boolean ok = true;
			for(Couple<Integer,Integer> c : craft) {
				//si ingredient non pr?sent ou mauvaise quantit?
				if(!ingredients.get(c.first).equals(c.second))ok = false;
			}
			if(ok)return tID;
		}
		return -1;
	}

	public static Cuenta getCompteByPseudo(String p) {
		for(Cuenta C : Cuentas.values())if(C.get_pseudo().equals(p))return C;
		return null;
	}

	public static void addItemSet(ItemSet itemSet)
	{
		ItemSets.put(itemSet.getId(), itemSet);
	}

	public static ItemSet getItemSet(int tID)
	{
		return ItemSets.get(tID);
	}

	public static int getItemSetNumber()
	{
		return ItemSets.size();
	}

	public static int getNextIdForMount() {
		int max = 1;
		for(int a : Dragodindes.keySet())if(a > max)max = a;
		return max+1;
	}

	public static Mapa getCarteByPosAndCont(int mapX, int mapY, int contID) {
		for(Mapa map : Mapas.values()) {
			if( map.getX() == mapX
			&&	map.getY() == mapY
			&&	map.getSubArea().get_area().get_superArea().get_id() == contID)
				return map;
		}
		return null;
	}

	public static void addGuild(Gremio g, boolean save) {
		Guildes.put(g.get_id(), g);
		if(save) GestorSQL.guardar_nuevo_gremio(g);
	}

	public static int getNextHighestGuildID() {
		if(Guildes.isEmpty())return 1;
		int n = 0;
		for(int x : Guildes.keySet())if(n<x)n = x;
		return n+1;
	}

	public static boolean guildNameIsUsed(String name) {
		for(Gremio g : Guildes.values())if(g.get_name().equalsIgnoreCase(name))return true;
		return false;
	}

	public static boolean guildEmblemIsUsed(String emb) {
		for(Gremio g : Guildes.values()) {
			if(g.get_emblem().equals(emb))return true;
		}
		return false;
	}

	public static Gremio getGuild(int i)
	{
		return Guildes.get(i);
	}

	public static long getGuildXpMax(int _lvl) {
		if(_lvl >= 200) 	_lvl = 199;
		if(_lvl <= 1)	 	_lvl = 1;
		return Experiencias.get(_lvl+1).guilde;
	}

	public static void ReassignAccountToChar(Cuenta C) {
		C.get_persos().clear();
		GestorSQL.cargar_personaje_por_cuenta(C.get_GUID());
		for(Personaje P : Personajes.values()) {
			if(P.getAccID() == C.get_GUID()) {
				C.addPerso(P);
				P.setAccount(C);
			}
		}
	}

	public static int getZaapCellIdByMapId(short i) {
		for(Entry<Integer, Integer> zaap : Constantes.ZAAPS.entrySet()) {
			if(zaap.getKey() == i)return zaap.getValue();
		}
		return -1;
	}

	public static int getEncloCellIdByMapId(short i) {
		if(Mundo.getCarte(i).getMountPark() != null) {
			if(Mundo.getCarte(i).getMountPark().get_cellid() > 0) {
				return Mundo.getCarte(i).getMountPark().get_cellid();
			}
		}
		
		return -1;
	}

	public static void delDragoByID(int getId)
	{
		Dragodindes.remove(getId);
	}

	public static void removeGuild(int id) {
		//Maison de guilde+SQL
		Casas.removeHouseGuild(id);
		//Enclo+SQL
		Mapa.MountPark.removeMountPark(id);
		//Percepteur+SQL
		Recaudador.removePercepteur(id);
		//Guilde
		Guildes.remove(id);
		GestorSQL.eliminar_todos_los_miembros_del_gremio(id);//Supprime les membres
		GestorSQL.eliminar_gremio(id);//Supprime la guilde
	}

	public static boolean ipIsUsed(String ip) {
		for(Cuenta c : Cuentas.values())if(c.getActualIP().compareTo(ip) == 0)return true;
		return false;
	}

	public static void unloadPerso(int g) {
		Personaje toRem = Personajes.get(g);
		if(!toRem.getItems().isEmpty()) {
			for(Entry<Integer, Objeto> curObj : toRem.getItems().entrySet()) {
				Objetos.remove(curObj.getKey());
			}
		}
		toRem = null;
		//Persos.remove(g);
	}
	
	public static boolean isArenaMap(int mapID) {
		for(int curID : MainServidor.arenaMap) {
			if(curID == mapID)
				return true;
		}
		return false;
	}

	public static Objeto newObjet(int Guid, int template, int qua, int pos, String strStats) {
		if(Mundo.getObjTemplate(template) == null) {
			System.out.println("ItemTemplate "+template+" inexistant, GUID dans la table `items`:"+Guid);
			MainServidor.closeServers();
		} 
		
		if(Mundo.getObjTemplate(template).getType() == 85)
			return new PiedraAlma(Guid, qua, template, pos, strStats);
		else
			return new Objeto(Guid, template, qua, pos, strStats);
	}
	
	
	public static short get_state()
	{
		return _state;
	}
	
	public static void set_state(short state)
	{
		_state = state;
	}
	
	public static byte getGmAccess()
	{
		return _GmAccess;
	}
	
	public static void setGmAccess(byte GmAccess)
	{
		_GmAccess = GmAccess;
	}

	public static Mercadillo getHdv(int mapID)
	{
		return Hdvs.get(mapID);
	}
	
	public synchronized static int getNextHdvID()//ATTENTION A NE PAS EXECUTER POUR RIEN CETTE METHODE CHANGE LE PROCHAIN ID DE L'HDV LORS DE SON EXECUTION
	{
		nextHdvID++;
		return nextHdvID;
	}
	public synchronized static void setNextHdvID(int nextID)
	{
		nextHdvID = nextID;
	}
	public synchronized static int getNextLigneID()
	{
		nextLigneID++;
		return nextLigneID;
	}
	public synchronized static void setNextLigneID(int ligneID)
	{
		nextLigneID = ligneID;
	}
	
	public static void addHdvItem(int compteID, int hdvID, HdvEntry toAdd)
	{
        //Si le compte n'est pas dans la memoire
        _hdvsItems.computeIfAbsent(compteID, k -> new HashMap<>());    //Ajout du compte cl?:compteID et un nouveau map<hdvID,items<>>

        _hdvsItems.get(compteID).computeIfAbsent(hdvID, k -> new ArrayList<>());
			
		_hdvsItems.get(compteID).get(hdvID).add(toAdd);
	}
	
	public static void removeHdvItem(int compteID,int hdvID,HdvEntry toDel)
	{
		_hdvsItems.get(compteID).get(hdvID).remove(toDel);
	}
	
	public static int getHdvNumber()
	{
		return Hdvs.size();
	}
	public static int getHdvObjetsNumber()
	{
		int size = 0;
		
		for(Map<Integer,ArrayList<HdvEntry>> curCompte : _hdvsItems.values())
		{
			for(ArrayList<HdvEntry> curHdv : curCompte.values())
			{
				size += curHdv.size();
			}
		}
		return size;
	}
	public static void addHdv(Mercadillo toAdd)
	{
		Hdvs.put(toAdd.getHdvID(),toAdd);
	}
	public static Map<Integer, ArrayList<HdvEntry>> getMyItems(int compteID)
	{
        //Si le compte n'est pas dans la memoire
        _hdvsItems.computeIfAbsent(compteID, k -> new HashMap<>());//Ajout du compte cl?:compteID et un nouveau map<hdvID,items
			
		return _hdvsItems.get(compteID);
	}
	public static Collection<ObjTemplate> getObjTemplates()
	{
		return ObjTemplates.values();
	}
	
	public static boolean mariageok(){ // Le mariage est-il ok ?
		boolean a = false;
		boolean b = false;
		try{
			if(Married.get(1) != null) a = true;
			if(Married.get(2) != null) b = true;
		}catch(Exception ignored){
			
		}
		if(a == true && b == true)return true;
		return false;
	}
	
	public static Personaje getMarried(int ordre)
	{
		return Married.get(ordre);
	}
	
	public static void AddMarried(int ordre, Personaje perso)
	{
		Personaje Perso = Married.get(ordre);
		if(Perso != null)
		{
			if(perso.get_GUID() == Perso.get_GUID()) // Si c'est le meme joueur...
				return;
			if(Perso.isConectado())// Si perso en ligne...
			{
				Married.remove(ordre);
				Married.put(ordre, perso);
				return;
			}
			
			return;
		}else
		{
			Married.put(ordre, perso);
			return;
		}
	}
	
	public static void PriestRequest(Personaje perso, Mapa carte, int IdPretre)
	{
		Personaje Homme = Married.get(0);
		Personaje Femme = Married.get(1);
		if(Homme.getWife() != 0){
			GestorSalida.GAME_SEND_MESSAGE_TO_MAP(carte, Homme.getNombre()+" est deja marier!", MainServidor.CONFIG_MOTD_COLOR);
			return;
		}
		if(Femme.getWife() != 0){
			GestorSalida.GAME_SEND_MESSAGE_TO_MAP(carte, Femme.getNombre()+" est deja marier!", MainServidor.CONFIG_MOTD_COLOR);
			return;
		}
		GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(perso.getActualMapa(), "", -1, "Pr?tre", perso.getNombre()+" acceptez-vous d'?pouser "+getMarried((perso.getSexo()==1?0:1)).getNombre()+" ?");
		GestorSalida.GAME_SEND_WEDDING(carte, 617, (Homme==perso?Homme.get_GUID():Femme.get_GUID()), (Homme==perso?Femme.get_GUID():Homme.get_GUID()), IdPretre);
	}
	
	public static void Wedding(Personaje Homme, Personaje Femme, int isOK)
	{
		if(isOK > 0)
		{
			GestorSalida.GAME_SEND_cMK_PACKET_TO_MAP(Homme.getActualMapa(), "", -1, "Pr?tre", "Je d?clare "+Homme.getNombre()+" et "+Femme.getNombre()+" unis par les liens sacr?s du mariage.");
			Homme.MarryTo(Femme);
			Femme.MarryTo(Homme);
		}else
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_AL_MAPA(Homme.getActualMapa(), "048;"+Homme.getNombre()+"~"+Femme.getNombre());
		}
		Married.get(0).setisOK(0);
		Married.get(1).setisOK(0);
		Married.clear();
	}
	
	public static Animaciones getAnimation(int AnimationId)
	{
		return Animations.get(AnimationId);
	}
	
	public static void addAnimation(Animaciones animation)
	{
		Animations.put(animation.getId(), animation);
	}
	
	public static void addHouse(Casas house)
	{
		Houses.put(house.get_id(), house);
	}
	
	public static Map<Integer, Casas> getHouses()
	{
		return Houses;
	}
	
	public static Casas getHouse(int id)
	{
		return Houses.get(id);
	}
	
	public static void addPerco(Recaudador perco)
	{
		Percepteurs.put(perco.getGuid(), perco);
	}
	
	public static Recaudador getPerco(int percoID)
	{
		return Percepteurs.get(percoID);
	}
	
	public static Map<Integer, Recaudador> getPercos()
	{
		return Percepteurs;
	}
	
	public static void addTrunk(Cofres trunk)
	{
		Trunks.put(trunk.get_id(), trunk);
	}
	
	public static Cofres getTrunk(int id)
	{
		return Trunks.get(id);
	}
	
	public static Map<Integer, Cofres> getTrunks()
	{
		return Trunks;
	}
	
	public static void addMountPark(Mapa.MountPark mp)
	{
		MountPark.put(mp.get_map().getID(), mp);
	}
	
	public static Map<Short, Mapa.MountPark> getMountPark()
	{
		return MountPark;
	}
	
	public static String parseMPtoGuild(int GuildID)
	{
		Gremio G = Mundo.getGuild(GuildID);
		byte enclosMax = (byte)Math.floor(G.get_lvl()/10);
		StringBuilder packet = new StringBuilder();
		packet.append(enclosMax);
		
		for(Entry<Short, Mapa.MountPark> mp : MountPark.entrySet())
		{
			if(mp.getValue().get_guild() != null && mp.getValue().get_guild().get_id() == GuildID)
			{
				packet.append("|").append(mp.getValue().get_map().getID()).append(";").append(mp.getValue().get_size()).append(";").append(mp.getValue().getObjectNumb());// Nombre d'objets pour le dernier
			}else
			{
				continue;
			}
		}
		return packet.toString();
	}
	
	public static int totalMPGuild(int GuildID)
	{
		int i = 0;
		for(Entry<Short, Mapa.MountPark> mp : MountPark.entrySet())
		{
			if(mp.getValue().get_guild().get_id() == GuildID)
			{
				i++;
			}else
			{
				continue;
			}
		}
		return i;
	}
	
	public static void addSeller(Personaje p)
	{
		if(Seller.get(p.getActualMapa().getID()) == null)
		{
			ArrayList<Integer> PersoID = new ArrayList<>();
			PersoID.add(p.get_GUID());
			Seller.put(p.getActualMapa().getID(), PersoID);
		}else
		{
			ArrayList<Integer> PersoID = new ArrayList<>();
			PersoID.addAll(Seller.get(p.getActualMapa().getID()));
			PersoID.add(p.get_GUID());
			Seller.remove(p.getActualMapa().getID());
			Seller.put(p.getActualMapa().getID(), PersoID);
		}
	}
	
	public static Collection<Integer> getSeller(short mapID)
	{
		return Seller.get(mapID);
	}
	
	public static void removeSeller(int pID, short mapID)
	{
		Seller.get(mapID).remove(pID);
	}
}
