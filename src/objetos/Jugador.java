package objetos;

import juego.JuegoServidor;
import juego.JuegoThread.GameAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Timer;

import objetos.Mapa.*;
import objetos.Gremio.GuildMember;
import objetos.Oficio.*;
import objetos.Hechizos.SortStats;

import comunes.MainServidor;
import comunes.Constantes;
import comunes.Formulas;
import comunes.GestorSQL;
import comunes.GestorSalida;
import comunes.Mundo;
import comunes.Mundo.Exchange;
import comunes.Mundo.ItemSet;

public class Jugador {
	
	private int _GUID;
	private String _name;
	private int _sexe;
	private int _classe;
	private int _color1;
	private int _color2;
	private int _color3;
	private long _kamas;
	private int _spellPts;
	private int _capital;
	private int _energy;
	private int _lvl;
	private long _curExp;
	private int _size;
	private int _gfxID;
	private int _orientation = 1;
	private Cuenta _compte;
	private int _accID;
	private boolean _canAggro = true;
	private String _emotes = "7667711";
	
	//Variables d'ali
	private byte _align = 0;
	private int _deshonor = 0;
	private int _honor = 0;
	private boolean _showWings = false;
	private int _aLvl = 0;
	//Fin ali
	
	private GuildMember _guildMember;
	private boolean _showFriendConnection;
	private String _canaux;
	Stats _baseStats;
	private Pelea _fight;
	private boolean _away;
	private Mapa _curCarte;
	private Case _curCell;
	private boolean _sitted;
	private boolean _ready = false;
	private boolean _isOnline  = false;
	private Group _group;
	private int _duelID = -1;
	private Map<Integer, EfectoHechizo> _buffs = new TreeMap<>();
	private Map<Integer, Objeto> _items = new TreeMap<>();
	private Timer _sitTimer;
	private String _savePos;
	private int _emoteActive = 0;
	//PDV
	private int _PDV;
	private int _PDVMAX;
	private int _exPdv;
	//Echanges
	private int _isTradingWith = 0;
	private Exchange _curExchange;
	//Dialogue
	private int _isTalkingWith = 0;
	//Invitation
	private int _inviting = 0;
	//Job
	private JobAction _curJobAction;
	private Map<Integer,StatsMetier> _metiers = new TreeMap<>();
	//Enclos
	private MountPark _inMountPark;
	//Monture
	private Dragopavo _mount;
	private int _mountXpGive = 0;
	private boolean _onMount = false;
	//Banque
	private boolean _isInBank;
	//Zaap
	private boolean _isZaaping = false;
	private ArrayList<Short> _zaaps = new ArrayList<>();
	//Disponibilité
	public boolean _isAbsent = false;
	public boolean _isInvisible = false;
	//Sort
	public boolean _seeSpell = false;
	private boolean _isForgetingSpell = false;
	private Map<Integer,SortStats> _sorts = new TreeMap<>();
	private Map<Integer,Character> _sortsPlaces = new TreeMap<>();
	//Double
	public boolean _isClone = false;
	//Percepteurs
	private int _isOnPercepteurID = 0;
	//Traque
	private traque _traqued = null;
	//Titre
	private byte _title = 0;
	//Inactivité
	protected long _lastPacketTime;
	//Mariage
	private int _wife = 0;
	private int _isOK = 0;
	//Suiveur - Suivi
	public Map<Integer, Jugador> _Follower = new TreeMap<>();
	public Jugador _Follows = null;
	//Fantome
	public boolean _isGhosts = false;
	private int _Speed = 0;
	//Coffre
	private Cofres _curTrunk;
	//Maison
	private House _curHouse;
	//Marchand
	public boolean _seeSeller = false;
	private Map<Integer , Integer> _storeItems = new TreeMap<>();//<ObjID, Prix>
	//Quêtes
    private int savestat;
	
	public static class traque 
	{
		private long _time;
		private Jugador _traqued;
		
		public traque(long time, Jugador p)
		{
			this._time = time;
			this._traqued = p;
		}
		
		public void set_traqued(Jugador tempP)
		{
			_traqued = tempP;
		}
		
		public Jugador get_traqued()
		{
			return _traqued;
		}
		
		public long get_time()
		{
			return _time;
		}
	
		public void set_time(long time)
		{
			_time = time;
		}
	}
	public static class Group
	{
		private ArrayList<Jugador> _persos = new ArrayList<>();
		private Jugador _chief;
		
		public Group(Jugador p1, Jugador p2)
		{
			_chief = p1;
			_persos.add(p1);
			_persos.add(p2);
		}
		
		public boolean isChief(int guid)
		{
			return _chief.get_GUID() == guid;
		}
		
		public void addPerso(Jugador p)
		{
			_persos.add(p);
		}
		
		public int getPersosNumber()
		{
			return _persos.size();
		}
		
		public int getGroupLevel()
		{
			int lvls = 0;
			for(Jugador p : _persos)
			{
				lvls += p.get_lvl();
			}
			return lvls;
		}
		
		public ArrayList<Jugador> getPersos()
		{
			return _persos;
		}

		public Jugador getChief()
		{
			return _chief;
		}

		public void leave(Jugador p)
		{
			if(!_persos.contains(p))return;
			p.setGroup(null);
			_persos.remove(p);
			if(_persos.size() == 1)
			{
				_persos.get(0).setGroup(null);
				if(_persos.get(0).get_compte() == null || _persos.get(0).get_compte().getGameThread() == null)return;
				GestorSalida.GAME_SEND_PV_PACKET(_persos.get(0).get_compte().getGameThread().get_out(),"");
			}
			else
				GestorSalida.GAME_SEND_PM_DEL_PACKET_TO_GROUP(this,p.get_GUID());
		}
	}
	public static class Stats
	{
		private Map<Integer,Integer> Effects = new TreeMap<>();
		
		public Stats(boolean addBases, Jugador perso)
		{
			Effects = new TreeMap<>();
			if(!addBases)return;
			Effects.put(Constantes.STATS_ADD_PA,  perso.get_lvl()<100?6:7);
			Effects.put(Constantes.STATS_ADD_PM, 3);
			Effects.put(Constantes.STATS_ADD_PROS, perso.get_classe()== Constantes.CLASS_ENUTROF?120:100);
			Effects.put(Constantes.STATS_ADD_PODS, 1000);
			Effects.put(Constantes.STATS_CREATURE, 1);
			Effects.put(Constantes.STATS_ADD_INIT, 1);
		}
		public Stats(Map<Integer, Integer> stats, boolean addBases, Jugador perso)
		{
			Effects = stats;
			if(!addBases)return;
			Effects.put(Constantes.STATS_ADD_PA,  perso.get_lvl()<100?6:7);
			Effects.put(Constantes.STATS_ADD_PM, 3);
			Effects.put(Constantes.STATS_ADD_PROS, perso.get_classe()== Constantes.CLASS_ENUTROF?120:100);
			Effects.put(Constantes.STATS_ADD_PODS, 1000);
			Effects.put(Constantes.STATS_CREATURE, 1);
			Effects.put(Constantes.STATS_ADD_INIT, 1);
		}
		
		public Stats(Map<Integer, Integer> stats)
		{
			Effects = stats;
		}
		
		public Stats()
		{
			Effects = new TreeMap<>();
		}
		
		public int addOneStat(int id, int val)
		{
			if(Effects.get(id) == null || Effects.get(id) == 0)
				Effects.put(id,val);
			else
			{
				int newVal = (Effects.get(id)+val);
				Effects.put(id, newVal);
			}
			return Effects.get(id);
		}
		
		public boolean isSameStats(Stats other)
		{
			for(Entry<Integer,Integer> entry : Effects.entrySet())
			{
				//Si la stat n'existe pas dans l'autre map
				if(other.getMap().get(entry.getKey()) == null)return false;
				//Si la stat existe mais n'a pas la même valeur
				if(other.getMap().get(entry.getKey()) != entry.getValue())return false;	
			}
			for(Entry<Integer,Integer> entry : other.getMap().entrySet())
			{
				//Si la stat n'existe pas dans l'autre map
				if(Effects.get(entry.getKey()) == null)return false;
				//Si la stat existe mais n'a pas la même valeur
				if(Effects.get(entry.getKey()) != entry.getValue())return false;	
			}
			return true;
		}
		
		public int getEffect(int id)
		{
			int val;
			if(Effects.get(id) == null)
				 val=0;
			else
				val = Effects.get(id);
			
			switch(id)//Bonus/Malus TODO
			{
				case Constantes.STATS_ADD_AFLEE:
					if(Effects.get(Constantes.STATS_REM_AFLEE)!= null)
						val -= (int)(getEffect(Constantes.STATS_REM_AFLEE));
					if(Effects.get(Constantes.STATS_ADD_SAGE) != null)
						val += (int)(getEffect(Constantes.STATS_ADD_SAGE)/4);
				break;
				case Constantes.STATS_ADD_MFLEE:
					if(Effects.get(Constantes.STATS_REM_MFLEE)!= null)
						val -= (int)(getEffect(Constantes.STATS_REM_MFLEE));
					if(Effects.get(Constantes.STATS_ADD_SAGE) != null)
						val += (int)(getEffect(Constantes.STATS_ADD_SAGE)/4);
				break;
				case Constantes.STATS_ADD_INIT:
					if(Effects.get(Constantes.STATS_REM_INIT)!= null)
						val -= Effects.get(Constantes.STATS_REM_INIT);
				break;
				case Constantes.STATS_ADD_AGIL:
					if(Effects.get(Constantes.STATS_REM_AGIL)!= null)
						val -= Effects.get(Constantes.STATS_REM_AGIL);
				break;
				case Constantes.STATS_ADD_FORC:
					if(Effects.get(Constantes.STATS_REM_FORC)!= null)
						val -= Effects.get(Constantes.STATS_REM_FORC);
				break;
				case Constantes.STATS_ADD_CHAN:
					if(Effects.get(Constantes.STATS_REM_CHAN)!= null)
						val -= Effects.get(Constantes.STATS_REM_CHAN);
				break;
				case Constantes.STATS_ADD_INTE:
					if(Effects.get(Constantes.STATS_REM_INTE)!= null)
					val -= Effects.get(Constantes.STATS_REM_INTE);
				break;
				case Constantes.STATS_ADD_PA:
					if(Effects.get(Constantes.STATS_ADD_PA2)!= null)
						val += Effects.get(Constantes.STATS_ADD_PA2);
					if(Effects.get(Constantes.STATS_REM_PA)!= null)
						val -= Effects.get(Constantes.STATS_REM_PA);
					if(Effects.get(Constantes.STATS_REM_PA2)!= null)//Non esquivable
						val -= Effects.get(Constantes.STATS_REM_PA2);
				break;
				case Constantes.STATS_ADD_PM:
					if(Effects.get(Constantes.STATS_ADD_PM2)!= null)
						val += Effects.get(Constantes.STATS_ADD_PM2);
					if(Effects.get(Constantes.STATS_REM_PM)!= null)
						val -= Effects.get(Constantes.STATS_REM_PM);
					if(Effects.get(Constantes.STATS_REM_PM2)!= null)//Non esquivable
						val -= Effects.get(Constantes.STATS_REM_PM2);
				break;
				case Constantes.STATS_ADD_PO:
					if(Effects.get(Constantes.STATS_REM_PO)!= null)
						val -= Effects.get(Constantes.STATS_REM_PO);
				break;
				case Constantes.STATS_ADD_VITA:
					if(Effects.get(Constantes.STATS_REM_VITA)!= null)
						val -= Effects.get(Constantes.STATS_REM_VITA);
				break;
				case Constantes.STATS_ADD_DOMA:
					if(Effects.get(Constantes.STATS_REM_DOMA)!= null)
						val -= Effects.get(Constantes.STATS_REM_DOMA);
				break;
				case Constantes.STATS_ADD_PODS:
					if(Effects.get(Constantes.STATS_REM_PODS)!= null)
						val -= Effects.get(Constantes.STATS_REM_PODS);
				break;
				case Constantes.STATS_ADD_PROS:
					if(Effects.get(Constantes.STATS_REM_PROS)!= null)
						val -= Effects.get(Constantes.STATS_REM_PROS);
				break;
				case Constantes.STATS_ADD_R_TER:
					if(Effects.get(Constantes.STATS_REM_R_TER)!= null)
						val -= Effects.get(Constantes.STATS_REM_R_TER);
				break;
				case Constantes.STATS_ADD_R_EAU:
					if(Effects.get(Constantes.STATS_REM_R_EAU)!= null)
						val -= Effects.get(Constantes.STATS_REM_R_EAU);
				break;
				case Constantes.STATS_ADD_R_AIR:
					if(Effects.get(Constantes.STATS_REM_R_AIR)!= null)
						val -= Effects.get(Constantes.STATS_REM_R_AIR);
				break;
				case Constantes.STATS_ADD_R_FEU:
					if(Effects.get(Constantes.STATS_REM_R_FEU)!= null)
						val -= Effects.get(Constantes.STATS_REM_R_FEU);
				break;
				case Constantes.STATS_ADD_R_NEU:
					if(Effects.get(Constantes.STATS_REM_R_NEU)!= null)
						val -= Effects.get(Constantes.STATS_REM_R_NEU);
				break;
				case Constantes.STATS_ADD_RP_TER:
					if(Effects.get(Constantes.STATS_REM_RP_TER)!= null)
						val -= Effects.get(Constantes.STATS_REM_RP_TER);
				break;
				case Constantes.STATS_ADD_RP_EAU:
					if(Effects.get(Constantes.STATS_REM_RP_EAU)!= null)
						val -= Effects.get(Constantes.STATS_REM_RP_EAU);
				break;
				case Constantes.STATS_ADD_RP_AIR:
					if(Effects.get(Constantes.STATS_REM_RP_AIR)!= null)
						val -= Effects.get(Constantes.STATS_REM_RP_AIR);
				break;
				case Constantes.STATS_ADD_RP_FEU:
					if(Effects.get(Constantes.STATS_REM_RP_FEU)!= null)
						val -= Effects.get(Constantes.STATS_REM_RP_FEU);
				break;
				case Constantes.STATS_ADD_RP_NEU:
					if(Effects.get(Constantes.STATS_REM_RP_NEU)!= null)
						val -= Effects.get(Constantes.STATS_REM_RP_NEU);
				break;
				case Constantes.STATS_ADD_MAITRISE:
					if(Effects.get(Constantes.STATS_ADD_MAITRISE)!= null)
						val = Effects.get(Constantes.STATS_ADD_MAITRISE);
				break;
			}
			return val;
		}
		
		public void clear() {
			Effects.clear();
		}

		public static Stats cumulStat(Stats s1,Stats s2)
		{
			TreeMap<Integer,Integer> effets = new TreeMap<>();
			for(int a = 0; a <= Constantes.MAX_EFFECTS_ID; a++)
			{
				if((s1.Effects.get(a) == null  || s1.Effects.get(a) == 0) && (s2.Effects.get(a) == null || s2.Effects.get(a) == 0))
					continue;
				int som = 0;
				if(s1.Effects.get(a) != null)
					som += s1.Effects.get(a);
				
				if(s2.Effects.get(a) != null)
					som += s2.Effects.get(a);
				
				effets.put(a, som);
			}
			return new Stats(effets,false,null);
		}
		
		public Map<Integer, Integer> getMap()
		{
			return Effects;
		}
		public String parseToItemSetStats()
		{
			StringBuilder str = new StringBuilder();
			if(Effects.isEmpty())return "";
			for(Entry<Integer,Integer> entry : Effects.entrySet())
			{
				if(str.length() >0)str.append(",");
				str.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0");
			}
			return str.toString();
		}
	}
	
	public Jugador(int _guid, String _name, int _sexe, int _classe,
				   int _color1, int _color2, int _color3, long _kamas, int pts, int _capital, int _energy, int _lvl, long exp,
				   int _size, int _gfxid, byte alignement, int _compte, Map<Integer,Integer> stats,
				   byte seeFriend, byte seeAlign, byte seeSeller, String canaux, short map, int cell, String stuff, String storeObjets, int pdvPer, String spells, String savePos, String jobs,
				   int mountXp, int mount, int honor, int deshonor, int alvl, String z, byte title, int wifeGuid)
	{
		this._GUID = _guid;
		this._name = _name;
		this._sexe = _sexe;
		this._classe = _classe;
		this._color1 = _color1;
		this._color2 = _color2;
		this._color3 = _color3;
        savestat = 0;
		this._kamas = _kamas;
		this._spellPts = pts;
		this._capital = _capital;
		this._align = alignement;
		this._honor = honor;
		this._deshonor = deshonor;
		this._aLvl = alvl;
		this._energy = _energy;
		this._lvl = _lvl;
		this._curExp = exp;
		if(mount != -1)this._mount = Mundo.getDragoByID(mount);
		this._size = _size;
		this._gfxID = _gfxid;
		this._mountXpGive = mountXp;
		this._baseStats = new Stats(stats,true,this);
		this._accID = _compte;
		this._compte = Mundo.getCompte(_compte);
		this._showFriendConnection = seeFriend==1;
		this._wife = wifeGuid; 
		if(this.get_align() != 0)
		{
			this._showWings = seeAlign==1;
		}else
		{
			this._showWings = false;
		}
		this._canaux = canaux;
		this._curCarte = Mundo.getCarte(map);
		this._savePos = savePos;
		if(_curCarte == null && Mundo.getCarte(MainServidor.CONFIG_START_MAP) != null)
		{
			this._curCarte = Mundo.getCarte(MainServidor.CONFIG_START_MAP);
			this._curCell = _curCarte.getCase(MainServidor.CONFIG_START_CELL);
		}else if (_curCarte == null && Mundo.getCarte(MainServidor.CONFIG_START_MAP) == null)
		{
			JuegoServidor.addToLog("Personnage mal positione, et position de départ non valide. Fermeture du serveur.");
			MainServidor.closeServers();
		}
		else if(_curCarte != null)
		{
			this._curCell = _curCarte.getCase(cell);
			if(_curCell == null)
			{
				this._curCarte = Mundo.getCarte(MainServidor.CONFIG_START_MAP);
				this._curCell = _curCarte.getCase(MainServidor.CONFIG_START_CELL);
			}
		}
		for(String str : z.split(","))
		{
			try
			{
				_zaaps.add(Short.parseShort(str));
			}catch(Exception e){};
		}
		if(_curCarte == null || _curCell == null)
		{
			JuegoServidor.addToLog("Map ou case de départ du personnage "+_name+" invalide");
			JuegoServidor.addToLog("Map ou case par défaut invalide");
			JuegoServidor.addToLog("Le serveur ne peut se lancer");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
			MainServidor.closeServers();
		}

		if(!stuff.equals(""))
		{
			if(stuff.charAt(stuff.length()-1) == '|')
				stuff = stuff.substring(0,stuff.length()-1);
			GestorSQL.cargando_objetos(stuff.replace("|",","));
		}
		for(String item : stuff.split("\\|"))
		{
			if(item.equals(""))continue;
			String[] infos = item.split(":");
			
			int guid = 0;
			try
			{
				guid = Integer.parseInt(infos[0]);
			}catch(Exception e ){continue;};
			
			Objeto obj = Mundo.getObjet(guid);
			if(obj == null)continue;
			_items.put(obj.getGuid(), obj);
		}
		if(!storeObjets.equals(""))
		{
			for(String _storeObjets : storeObjets.split("\\|"))
			{
				String[] infos = _storeObjets.split(",");
				int guid = 0;
				int price = 0;
				try
				{
					guid = Integer.parseInt(infos[0]);
					price = Integer.parseInt(infos[1]);
				}catch(Exception e ){continue;};
				
				Objeto obj = Mundo.getObjet(guid);
				if(obj == null)continue;
				
				_storeItems.put(obj.getGuid(), price);
			}
		}
		this._PDVMAX = (_lvl-1)*5+ Constantes.getBasePdv(_classe)+getTotalStats().getEffect(Constantes.STATS_ADD_VITA);
		this._PDV = (_PDVMAX*pdvPer)/100;
		parseSpells(spells);
		
		_sitTimer = new Timer(2000, e -> regenLife());
		
		_exPdv = _PDV;
		
		//Chargement des métiers
		if(!jobs.equals(""))
		{
			for(String aJobData : jobs.split(";"))
			{
				String[] infos = aJobData.split(",");
				try
				{
					int jobID = Integer.parseInt(infos[0]);
					long xp = Long.parseLong(infos[1]);
					Oficio m = Mundo.getMetier(jobID);
					StatsMetier SM = _metiers.get(learnJob(m));
					SM.addXp(this, xp);
				}catch(Exception e){e.getStackTrace();}
			}
		}
		
		this._title = title;
		if(_energy == 0) set_Ghosts();
	}
	
	//Clone double
	public Jugador(int _guid, String _name, int _sexe, int _classe,
				   int _color1, int _color2, int _color3, int _lvl,
				   int _size, int _gfxid, Map<Integer,Integer> stats,
				   String stuff, int pdvPer, byte seeAlign, int mount, int alvl, byte alignement)
	{
		this._GUID = _guid;
		this._name = _name;
		this._sexe = _sexe;
		this._classe = _classe;
		this._color1 = _color1;
		this._color2 = _color2;
		this._color3 = _color3;
		this._lvl = _lvl;
		this._aLvl = alvl;
		this._size = _size;
		this._gfxID = _gfxid;
		this._baseStats = new Stats(stats,true,this);
		if(!stuff.equals(""))
		{
			if(stuff.charAt(stuff.length()-1) == '|')
				stuff = stuff.substring(0,stuff.length()-1);
			GestorSQL.cargando_objetos(stuff.replace("|",","));
		}
		for(String item : stuff.split("\\|"))
		{
			if(item.equals(""))continue;
			String[] infos = item.split(":");
			int guid = Integer.parseInt(infos[0]);
			Objeto obj = Mundo.getObjet(guid);
			if( obj == null)continue;
			_items.put(obj.getGuid(), obj);
		}
		
		this._PDVMAX = (_lvl-1)*5+ Constantes.getBasePdv(_classe)+getTotalStats().getEffect(Constantes.STATS_ADD_VITA);
		this._PDV = (_PDVMAX*pdvPer)/100;
		
		_exPdv = _PDV;
		
		this._align = alignement;
		if(this.get_align() != 0)
		{
			this._showWings = seeAlign==1;
		}else
		{
			this._showWings = false;
		}
		if(mount != -1)this._mount = Mundo.getDragoByID(mount);
	}

	public void regenLife()
	{
		//Joueur pas en jeu
		if(_curCarte == null)return;
		//Pas de regen en combat
		if(_fight != null)return;
		//Déjà Full PDV
		if(_PDV == _PDVMAX)return;
		_PDV++;
	}
	
	public static Jugador CREATE_PERSONNAGE(String name, int sexe, int classe, int color1, int color2, int color3, Cuenta compte)
	{
		String z = "";
		if(MainServidor.CONFIG_ZAAP)
		{
			for(Entry<Integer, Integer> i : Constantes.ZAAPS.entrySet())
			{
				if(z.length() != 0)z+=",";
				z += i.getKey();
			}
		}
		Jugador perso = new Jugador(
				GestorSQL.getSiguienteIDPersonaje(),
				name,
				sexe,
				classe,
				color1,
				color2,
				color3,
				MainServidor.CONFIG_START_KAMAS,
				((MainServidor.CONFIG_START_LEVEL-1)*1),
				((MainServidor.CONFIG_START_LEVEL-1)*5),
				10000,
				MainServidor.CONFIG_START_LEVEL,
				Mundo.getPersoXpMin(MainServidor.CONFIG_START_LEVEL),
				100,
				classe * 10 + sexe,
				(byte)0,
				compte.get_GUID(),
				new TreeMap<>(),
				(byte)1,
				(byte)0,
				(byte)0,
				"*#%!pi$:?",
				Constantes.getStartMap(classe),
				Constantes.getStartCell(classe),
				"",
				"",
				100,
				"",
				MainServidor.CONFIG_START_MAP + "," + MainServidor.CONFIG_START_CELL,
				"",
				0,
				-1,
				0,
				0,
				0,
				z,
				(byte)0,
				0
				);
		perso._sorts = Constantes.getStartSorts(classe);
		for(int a = 1; a <= perso.get_lvl();a++)
		{
			Constantes.onLevelUpSpells(perso, a);
		}
		perso._sortsPlaces = Constantes.getStartSortsPlaces(classe);
		if(!GestorSQL.agregar_personaje_db(perso))
			return null;
		
		Mundo.addPersonnage(perso);
	
		return perso;
	}

	public void set_Online(boolean d)
	{
		_isOnline = d;
	}
	
	public boolean isOnline()
	{
		return _isOnline;
	}
	
	public void setGroup(Group g)
	{
		_group = g;
	}

	public Group getGroup()
	{
		return _group;
	}
	
	public String parseSpellToDB()
	{
		StringBuilder sorts = new StringBuilder();
		if(_sorts.isEmpty())return "";
		for(int key : _sorts.keySet())
		{
			//3;1;a,4;3;b
			SortStats SS = _sorts.get(key);
			sorts.append(SS.getSpellID()).append(";").append(SS.getLevel()).append(";");
			if(_sortsPlaces.get(key)!=null)
				sorts.append(_sortsPlaces.get(key));
			else
				sorts.append("_");
			sorts.append(",");
		}
		return sorts.substring(0, sorts.length()-1).toString();
	}
	
	private void parseSpells(String str)
	{
		String[] spells = str.split(",");
		for(String e : spells)
		{
			try
			{
				int id = Integer.parseInt(e.split(";")[0]);
				int lvl = Integer.parseInt(e.split(";")[1]);
				char place = e.split(";")[2].charAt(0);
				learnSpell(id,lvl,false,false);
				_sortsPlaces.put(id, place);
			}catch(NumberFormatException e1){continue;};
		}
	}
	
	public String get_savePos() {
		return _savePos;
	}

	public void set_savePos(String savePos) {
		_savePos = savePos;
	}

	public int get_isTradingWith() {
		return _isTradingWith;
	}

	public void set_isTradingWith(int tradingWith) {
		_isTradingWith = tradingWith;
	}
	
	public int get_isTalkingWith() {
		return _isTalkingWith;
	}

	public void set_isTalkingWith(int talkingWith) {
		_isTalkingWith = talkingWith;
	}
	
	public long get_kamas() {
		return _kamas;
	}

	public Map<Integer, EfectoHechizo> get_buff() {
		return _buffs;
	}

	public void set_kamas(long l) {
		this._kamas = l;
	}
	

	public Cuenta get_compte() {
		return _compte;
	}

	public int get_spellPts() {
		return _spellPts;
	}

	public void set_spellPts(int pts) {
		_spellPts = pts;
	}

	public Gremio get_guild()
	{
		if(_guildMember == null)return null;
		return _guildMember.getGuild();
	}

	public void setGuildMember(GuildMember _guild) {
		this._guildMember = _guild;
	}
	
	public boolean is_ready() {
		return _ready;
	}

	public void set_ready(boolean _ready) {
		this._ready = _ready;
	}

	public int get_duelID() {
		return _duelID;
	}

	public Pelea get_fight() {
		return _fight;
	}

	public void set_duelID(int _duelid) {
		_duelID = _duelid;
	}

	public int get_energy() {
		return _energy;
	}

	public boolean is_showFriendConnection() {
		return _showFriendConnection;
	}
	
	public boolean is_showSpells() {
		return _seeSpell;
	}
	
	public boolean is_showWings() {
		return _showWings;
	}
	
	public boolean is_showSeller() {
		return _seeSeller;
	}
	
	public void set_showSeller(boolean is) {
		_seeSeller = is;
	}
	
	public String get_canaux() {
		return _canaux;
	}

	public void set_energy(int _energy) {
		this._energy = _energy;
	}

	public int get_lvl() {
		return _lvl;
	}

	public void set_lvl(int _lvl) {
		this._lvl = _lvl;
	}

	public long get_curExp() {
		return _curExp;
	}

	public Mapa.Case get_curCell() {
		return _curCell;
	}

	public void set_curCell(Mapa.Case cell) {
		_curCell = cell;
	}

	public void set_curExp(long exp) {
		_curExp = exp;
	}

	public int get_size() {
		return _size;
	}

	public void set_size(int _size) {
		this._size = _size;
	}

	public void set_fight(Pelea _fight) {
		this._fight = _fight;
	}

	public int get_gfxID() {
		return _gfxID;
	}

	public void set_gfxID(int _gfxid) {
		_gfxID = _gfxid;
	}

	public int get_GUID() {
		return _GUID;
	}

	public Mapa get_curCarte() {
		return _curCarte;
	}

	public String get_name() {
		return _name;
	}

	public boolean is_away() {
		return _away;
	}

	public void set_away(boolean _away) {
		this._away = _away;
	}

	public boolean isSitted() {
		return _sitted;
	}

	public int get_sexe() {
		return _sexe;
	}

	public int get_classe() {
		return _classe;
	}

	public int get_color1() {
		return _color1;
	}

	public int get_color2() {
		return _color2;
	}

	public Stats get_baseStats() {
		return _baseStats;
	}

	public int get_color3() {
		return _color3;
	}

	public int get_capital() {
		return _capital;
	}
	
	public boolean learnSpell(int spellID,int level,boolean save,boolean send)
	{
		if(Mundo.getSort(spellID).getStatsByLevel(level)==null)
		{
			JuegoServidor.addToLog("[ERROR]Sort "+spellID+" lvl "+level+" non trouve.");
			return false;
		}
		_sorts.put(spellID, Mundo.getSort(spellID).getStatsByLevel(level));
		
		if(send)
		{
			GestorSalida.GAME_SEND_SPELL_LIST(this);
			GestorSalida.GAME_SEND_Im_PACKET(this, "03;"+spellID);
		}
		if(save) GestorSQL.guardar_personaje(this,false);
		return true;
	}
	
	public boolean boostSpell(int spellID)
	{
		if(getSortStatBySortIfHas(spellID)== null)
		{
			JuegoServidor.addToLog(_name+" n'a pas le sort "+spellID);
			return false;
		}
		int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
		if(AncLevel == 6)return false;
		if(_spellPts>=AncLevel && Mundo.getSort(spellID).getStatsByLevel(AncLevel+1).getReqLevel() <= _lvl)
		{
			if(learnSpell(spellID,AncLevel+1,true,false))
			{
				_spellPts -= AncLevel;
				GestorSQL.guardar_personaje(this,false);
				return true;
			}else
			{
				JuegoServidor.addToLog(_name+" : Echec LearnSpell "+spellID);
				return false;
			}
		}
		else//Pas le niveau ou pas les Points
		{
			if(_spellPts<AncLevel)
				JuegoServidor.addToLog(_name+" n'a pas les points requis pour booster le sort "+spellID+" "+_spellPts+"/"+AncLevel);
			if(Mundo.getSort(spellID).getStatsByLevel(AncLevel+1).getReqLevel() > _lvl)
				JuegoServidor.addToLog(_name+" n'a pas le niveau pour booster le sort "+spellID+" "+_lvl+"/"+ Mundo.getSort(spellID).getStatsByLevel(AncLevel+1).getReqLevel());
			return false;
		}
	}
	
	public boolean forgetSpell(int spellID)
	{
		if(getSortStatBySortIfHas(spellID)== null)
		{
			if(MainServidor.CONFIG_DEBUG) JuegoServidor.addToLog(_name+" n'a pas le sort "+spellID);
			return false;
		}
		int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
		if(AncLevel <= 1)return false;
		
		if(learnSpell(spellID,1,true,false))
		{
			_spellPts += Formulas.spellCost(AncLevel);
			
			GestorSQL.guardar_personaje(this,false);
			return true;
		}else
		{
			if(MainServidor.CONFIG_DEBUG) JuegoServidor.addToLog(_name+" : Echec LearnSpell "+spellID);
			return false;
		}
		
	}
	
	public String parseSpellList()
	{
		StringBuilder packet = new StringBuilder();
		packet.append("SL");
		for (SortStats SS : _sorts.values()) {
			packet.append(SS.getSpellID()).append("~").append(SS.getLevel()).append("~").append(_sortsPlaces.get(SS.getSpellID())).append(";");
		}
		return packet.toString();
	}

	public void set_SpellPlace(int SpellID, char Place)
	{
			replace_SpellInBook(Place);
			_sortsPlaces.remove(SpellID);	
			_sortsPlaces.put(SpellID, Place);
			GestorSQL.guardar_personaje(this,false);//On sauvegarde les changements
	}

	private void replace_SpellInBook(char Place)
	{
		for(int key : _sorts.keySet())
		{
			if(_sortsPlaces.get(key)!=null)
			{
				if (_sortsPlaces.get(key).equals(Place))
				{
					_sortsPlaces.remove(key);
				}
			}
		}
	}
	
	public SortStats getSortStatBySortIfHas(int spellID)
	{
		return _sorts.get(spellID);
	}
	
	public boolean hasSpell(int spellID)
	{
		return (getSortStatBySortIfHas(spellID) == null ? false : true);
	}
	
	public String parseALK()
	{
		StringBuilder perso = new StringBuilder();
		perso.append("|");
		perso.append(this._GUID).append(";");
		perso.append(this._name).append(";");
		perso.append(this._lvl).append(";");
		perso.append(this._gfxID).append(";");
		perso.append((this._color1!= -1?Integer.toHexString(this._color1):"-1")).append(";");
		perso.append((this._color2!= -1?Integer.toHexString(this._color2):"-1")).append(";");
		perso.append((this._color3!= -1?Integer.toHexString(this._color3):"-1")).append(";");
		perso.append(getGMStuffString()).append(";");
		perso.append((this.is_showSeller()?1:0)).append(";");
		perso.append("1;");
		perso.append(";");//DeathCount	this.deathCount;
		perso.append(";");//LevelMax
		return perso.toString();
	}
	
	public void remove()
	{
		GestorSQL.eliminar_personaje_db(this);
	}
	
	public void OnJoinGame()
	{
		if(_compte.getGameThread() == null)return;
		PrintWriter out = _compte.getGameThread().get_out();
		_compte.setCurPerso(this);
		_isOnline = true;
		
		if(_mount != null) GestorSalida.GAME_SEND_Re_PACKET(this,"+",_mount);
		GestorSalida.GAME_SEND_Rx_PACKET(this);
		
		GestorSalida.GAME_SEND_ASK(out, this);
		//Envoie des bonus pano si besoin
		for(int a = 1; a< Mundo.getItemSetNumber(); a++)
		{
			int num =getNumbEquipedItemOfPanoplie(a);
			if(num == 0)continue;
			GestorSalida.GAME_SEND_OS_PACKET(this, a);
		}
		
		//envoie des données de métier
		if(_metiers.size() >0)
		{
			ArrayList<StatsMetier> list = new ArrayList<>();
			list.addAll(_metiers.values());
			//packet JS
			GestorSalida.GAME_SEND_JS_PACKET(this, list);
			//packet JX
			GestorSalida.GAME_SEND_JX_PACKET(this, list);
			//Packet JO (Job Option)
			GestorSalida.GAME_SEND_JO_PACKET(this, list);
			Objeto obj = getObjetByPos(Constantes.ITEM_POS_ARME);
			if(obj != null)
			{
				for(StatsMetier sm : list)
					if(sm.getTemplate().isValidTool(obj.getTemplate().getID()))
						GestorSalida.GAME_SEND_OT_PACKET(_compte.getGameThread().get_out(),sm.getTemplate().getId());
			}
		}
		//Fin métier
		GestorSalida.GAME_SEND_ALIGNEMENT(out, _align);
		GestorSalida.GAME_SEND_ADD_CANAL(out,_canaux+"^"+(_compte.get_gmLvl()>0?"@¤":""));
		if(_guildMember != null) GestorSalida.GAME_SEND_gS_PACKET(this,_guildMember);
		GestorSalida.GAME_SEND_ZONE_ALLIGN_STATUT(out);
		GestorSalida.GAME_SEND_SPELL_LIST(this);
		GestorSalida.GAME_SEND_EMOTE_LIST(this,_emotes,"0");
		GestorSalida.GAME_SEND_RESTRICTIONS(out);
		GestorSalida.GAME_SEND_Ow_PACKET(this);
		GestorSalida.GAME_SEND_SEE_FRIEND_CONNEXION(out,_showFriendConnection);
		this._compte.SendOnline();
		
		//Messages de bienvenue
		GestorSalida.GAME_SEND_Im_PACKET(this, "189");
		if(!_compte.getLastConnectionDate().equals("") && !_compte.get_lastIP().equals(""))
			GestorSalida.GAME_SEND_Im_PACKET(this, "0152;"+_compte.getLastConnectionDate()+"~"+_compte.get_lastIP());
		GestorSalida.GAME_SEND_Im_PACKET(this, "0153;"+_compte.get_curIP());
		//Fin messages
		//Actualisation de l'ip
		_compte.setLastIP(_compte.get_curIP());
		
		//Mise a jour du lastConnectionDate
		Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		String jour = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("MM");
		String mois = dateFormat.format(actDate);	
		dateFormat = new SimpleDateFormat("yyyy");
		String annee = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("HH");
		String heure = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("mm");
		String min = dateFormat.format(actDate);
		_compte.setLastConnectionDate(annee+"~"+mois+"~"+jour+"~"+heure+"~"+min);
		if(_guildMember != null)
			_compte.setLastConnectionDate(annee+"~"+mois+"~"+jour+"~"+heure+"~"+min);
		
		//Actualisation dans la DB
		GestorSQL.actualizar_ultima_fecha_conexion(_compte);
		
		if(!MainServidor.CONFIG_MOTD.equals(""))//Si le motd est notifié
		{
			String color = MainServidor.CONFIG_MOTD_COLOR;
			if(color.equals(""))color = "000000";//Noir
			
			GestorSalida.GAME_SEND_MESSAGE(this, MainServidor.CONFIG_MOTD, color);
		}
		//on démarre le Timer pour la Regen de Pdv
		_sitTimer.start();
		//on le demarre coté client
		GestorSalida.GAME_SEND_ILS_PACKET(this, 2000);
	}
	
	public void SetSeeFriendOnline(boolean bool)
	{
		_showFriendConnection = bool;
	}
	
	public void sendGameCreate()
	{
		if(_compte.getGameThread() == null) return;
		PrintWriter out = _compte.getGameThread().get_out();
		
		if(is_showSeller() == true && Mundo.getSeller(get_curCarte().get_id()) != null && Mundo.getSeller(get_curCarte().get_id()).contains(get_GUID()))
		{
			Mundo.removeSeller(get_GUID(), get_curCarte().get_id());
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(get_curCarte(), get_GUID());
			set_showSeller(false);
		}
		
		GestorSalida.GAME_SEND_GAME_CREATE(out,_name);
		GestorSalida.GAME_SEND_STATS_PACKET(this);
		GestorSQL.salir_del_juego(_compte.get_GUID(), 1);
		GestorSalida.GAME_SEND_MAPDATA(out,_curCarte.get_id(),_curCarte.get_date(),_curCarte.get_key());
		GestorSalida.GAME_SEND_MAP_FIGHT_COUNT(out,this.get_curCarte());
		_curCarte.addPlayer(this);
		GestorSQL.guardar_personaje(this, true);
	}
	
	public String parseToOa()
	{
		StringBuilder packetOa = new StringBuilder();
		packetOa.append("Oa").append(_GUID).append("|").append(getGMStuffString());
		return packetOa.toString();
	}
	
	public String parseToGM()
	{
		StringBuilder str = new StringBuilder();
		if(_fight == null)// Hors combat
		{
			str.append(_curCell.getID()).append(";").append(_orientation).append(";");
			str.append("0").append(";");//FIXME:?
			str.append(_GUID).append(";").append(_name).append(";").append(_classe);
			str.append((this.get_title()>0?(","+this.get_title()+";"):(";")));
			str.append(_gfxID).append("^").append(_size).append(";");//gfxID^size
			str.append(_sexe).append(";").append(_align).append(",");//1,0,0,4055064
			str.append("0,");//FIXME:?
			str.append((_showWings?getGrade():"0")).append(",");
			str.append(_lvl).append(";");
			str.append((_color1==-1?"-1":Integer.toHexString(_color1))).append(";");
			str.append((_color2==-1?"-1":Integer.toHexString(_color2))).append(";");
			str.append((_color3==-1?"-1":Integer.toHexString(_color3))).append(";");
			str.append(getGMStuffString()).append(";");
			if(MainServidor.AURA_SYSTEM)
			{
				str.append((_lvl>99?(_lvl>199?(2):(1)):(0))).append(";");
			}else
			{
				str.append("0;");
			}
			str.append(";");//Emote
			str.append(";");//Emote timer
			if(this._guildMember!=null && this._guildMember.getGuild().getMembers().size()>= MainServidor.MEMBRE_MINI_GUILDE_VALIDE)
			{
				str.append(this._guildMember.getGuild().get_name()).append(";").append(this._guildMember.getGuild().get_emblem()).append(";");
			}
			else str.append(";;");
			str.append(get_Speed()).append(";");//Restriction
			str.append((_onMount&&_mount!=null?_mount.get_color(parsecolortomount()):"")).append(";");
			str.append(";");
		}
		return str.toString();
	}
	
	public String parsecolortomount() {
		return (this._color1 == -1 ? "" : Integer.toHexString(this._color1)) + "," + (this._color2 == -1 ? "" : Integer.toHexString(this._color2)) + "," + (this._color3 == -1 ? "" : Integer.toHexString(this._color3));
	}
	
    public String parseToMerchant() 
    {
    	StringBuilder str = new StringBuilder();
    	str.append(_curCell.getID()).append(";");
    	str.append(_orientation).append(";");
    	str.append("0").append(";");
    	str.append(_GUID).append(";");
    	str.append(_name).append(";");
    	str.append("-5").append(";");//Merchant identifier
    	str.append(_gfxID).append("^").append(_size).append(";");
		str.append((_color1==-1?"-1":Integer.toHexString(_color1))).append(";");
		str.append((_color2==-1?"-1":Integer.toHexString(_color2))).append(";");
		str.append((_color3==-1?"-1":Integer.toHexString(_color3))).append(";");
    	str.append(getGMStuffString()).append(";");//acessories
    	str.append((_guildMember != null ? _guildMember.getGuild().get_name() : "")).append(";");//guildName
    	str.append((_guildMember != null ? _guildMember.getGuild().get_emblem() : "")).append(";");//emblem
    	str.append("0;");//offlineType

        return str.toString();
    }
	
	public String getGMStuffString()
	{
		StringBuilder str = new StringBuilder();
		// pour chaque place d'objet apparent, en Hexa, l'ID de l'objet
		// 2411 pour la Cape Objivejan (9233)
		// 2412 pour la Coiffe Objivejan (9234)
		if(getObjetByPos(Constantes.ITEM_POS_ARME) != null)
		 	str.append(Integer.toHexString(getObjetByPos(Constantes.ITEM_POS_ARME).getTemplate().getID()));
		str.append(",");
		if(getObjetByPos(Constantes.ITEM_POS_COIFFE) != null) {
			if(getObjetByPos(Constantes.ITEM_POS_COIFFE).getObvijevanPos() != 10)
				str.append(Integer.toHexString(getObjetByPos(Constantes.ITEM_POS_COIFFE).getTemplate().getID()));
			else str.append("2412~16~").append(getObjetByPos(Constantes.ITEM_POS_COIFFE).getObvijevanLook());
			}	
		str.append(",");
		if(getObjetByPos(Constantes.ITEM_POS_CAPE) != null) {
			if(getObjetByPos(Constantes.ITEM_POS_CAPE).getObvijevanPos() != 11)
				str.append(Integer.toHexString(getObjetByPos(Constantes.ITEM_POS_CAPE).getTemplate().getID()));
			else str.append("2411~17~").append(getObjetByPos(Constantes.ITEM_POS_CAPE).getObvijevanLook());
			}	
		str.append(",");
		if(getObjetByPos(Constantes.ITEM_POS_FAMILIER) != null)
			str.append(Integer.toHexString(getObjetByPos(Constantes.ITEM_POS_FAMILIER).getTemplate().getID()));
		str.append(",");
		if(getObjetByPos(Constantes.ITEM_POS_BOUCLIER) != null)
			str.append(Integer.toHexString(getObjetByPos(Constantes.ITEM_POS_BOUCLIER).getTemplate().getID()));
		return str.toString();
	}

    public String getAsPacket()
    {
        refreshStats();
        StringBuilder ASData = new StringBuilder();
        ASData.append("As").append(xpString(",")).append("|");
        ASData.append(_kamas).append("|").append(_capital).append("|").append(_spellPts).append("|");
        ASData.append(_align).append("~").append(_align).append(",").append(_aLvl).append(",").append(getGrade()).append(",").append(_honor).append(",").append((new StringBuilder(String.valueOf(_deshonor))).append(",").toString()).append(_showWings ? "1" : "0").append("|");
        int pdv = get_PDV();
        int pdvMax = get_PDVMAX();
        if(_fight != null)
        {
            Pelea.Fighter f = _fight.getFighterByPerso(this);
            if(f != null)
            {
                pdv = f.getPDV();
                pdvMax = f.getPDVMAX();
            }
        }
        ASData.append(pdv).append(",").append(pdvMax).append("|");
        ASData.append(_energy).append(",10000|");
        ASData.append(getInitiative()).append("|");
        ASData.append(_baseStats.getEffect(176) + getStuffStats().getEffect(176) + (int)Math.ceil(_baseStats.getEffect(123) / 10) + getBuffsStats().getEffect(176)).append("|");
        ASData.append(_baseStats.getEffect(111)).append(",").append(getStuffStats().getEffect(111)).append(",").append(getDonsStats().getEffect(111)).append(",").append(getBuffsStats().getEffect(111)).append(",").append(getTotalStats().getEffect(111)).append("|");
        ASData.append(_baseStats.getEffect(128)).append(",").append(getStuffStats().getEffect(128)).append(",").append(getDonsStats().getEffect(128)).append(",").append(getBuffsStats().getEffect(128)).append(",").append(getTotalStats().getEffect(128)).append("|");
        ASData.append(_baseStats.getEffect(118)).append(",").append(getStuffStats().getEffect(118)).append(",").append(getDonsStats().getEffect(118)).append(",").append(getBuffsStats().getEffect(118)).append("|");
        ASData.append(_baseStats.getEffect(125)).append(",").append(getStuffStats().getEffect(125)).append(",").append(getDonsStats().getEffect(125)).append(",").append(getBuffsStats().getEffect(125)).append("|");
        ASData.append(_baseStats.getEffect(124)).append(",").append(getStuffStats().getEffect(124)).append(",").append(getDonsStats().getEffect(124)).append(",").append(getBuffsStats().getEffect(124)).append("|");
        ASData.append(_baseStats.getEffect(123)).append(",").append(getStuffStats().getEffect(123)).append(",").append(getDonsStats().getEffect(123)).append(",").append(getBuffsStats().getEffect(123)).append("|");
        ASData.append(_baseStats.getEffect(119)).append(",").append(getStuffStats().getEffect(119)).append(",").append(getDonsStats().getEffect(119)).append(",").append(getBuffsStats().getEffect(119)).append("|");
        ASData.append(_baseStats.getEffect(126)).append(",").append(getStuffStats().getEffect(126)).append(",").append(getDonsStats().getEffect(126)).append(",").append(getBuffsStats().getEffect(126)).append("|");
        ASData.append(_baseStats.getEffect(117)).append(",").append(getStuffStats().getEffect(117)).append(",").append(getDonsStats().getEffect(117)).append(",").append(getBuffsStats().getEffect(117)).append("|");
        ASData.append(_baseStats.getEffect(182)).append(",").append(getStuffStats().getEffect(182)).append(",").append(getDonsStats().getEffect(182)).append(",").append(getBuffsStats().getEffect(182)).append("|");
        ASData.append(_baseStats.getEffect(112)).append(",").append(getStuffStats().getEffect(112)).append(",").append(getDonsStats().getEffect(112)).append(",").append(getBuffsStats().getEffect(112)).append("|");
        ASData.append(_baseStats.getEffect(142)).append(",").append(getStuffStats().getEffect(142)).append(",").append(getDonsStats().getEffect(142)).append(",").append(getBuffsStats().getEffect(142)).append("|");
        ASData.append("0,0,0,0|");
        ASData.append(_baseStats.getEffect(138)).append(",").append(getStuffStats().getEffect(138)).append((new StringBuilder(",")).append(getDonsStats().getEffect(138)).toString()).append(",").append(getBuffsStats().getEffect(138)).append("|");
        ASData.append(_baseStats.getEffect(178)).append(",").append(getStuffStats().getEffect(178)).append(",").append(getDonsStats().getEffect(178)).append(",").append(getBuffsStats().getEffect(178)).append("|");
        ASData.append(_baseStats.getEffect(225)).append(",").append(getStuffStats().getEffect(225)).append(",").append(getDonsStats().getEffect(225)).append(",").append(getBuffsStats().getEffect(225)).append("|");
        ASData.append(_baseStats.getEffect(226)).append(",").append(getStuffStats().getEffect(226)).append(",").append(getDonsStats().getEffect(226)).append(",").append(getBuffsStats().getEffect(226)).append("|");
        ASData.append(_baseStats.getEffect(220)).append(",").append(getStuffStats().getEffect(220)).append(",").append(getDonsStats().getEffect(220)).append(",").append(getBuffsStats().getEffect(220)).append("|");
        ASData.append(_baseStats.getEffect(115)).append(",").append(getStuffStats().getEffect(115)).append(",").append(getDonsStats().getEffect(115)).append(",").append(getBuffsStats().getEffect(115)).append("|");
        ASData.append(_baseStats.getEffect(122)).append(",").append(getStuffStats().getEffect(122)).append(",").append(getDonsStats().getEffect(122)).append(",").append(getBuffsStats().getEffect(122)).append("|");
        ASData.append(_baseStats.getEffect(160)).append(",").append(getStuffStats().getEffect(160)).append(",").append(0).append(",").append(getBuffsStats().getEffect(160)).append(",").append(getBuffsStats().getEffect(160)).append("|");
        ASData.append(_baseStats.getEffect(161)).append(",").append(getStuffStats().getEffect(161)).append(",").append(0).append(",").append(getBuffsStats().getEffect(161)).append(",").append(getBuffsStats().getEffect(161)).append("|");
        ASData.append(_baseStats.getEffect(241)).append(",").append(getStuffStats().getEffect(241)).append(",").append(0).append(",").append(getBuffsStats().getEffect(241)).append(",").append(getBuffsStats().getEffect(241)).append("|");
        ASData.append(_baseStats.getEffect(214)).append(",").append(getStuffStats().getEffect(214)).append(",").append(0).append(",").append(getBuffsStats().getEffect(214)).append(",").append(getBuffsStats().getEffect(214)).append("|");
        ASData.append(_baseStats.getEffect(264)).append(",").append(getStuffStats().getEffect(264)).append(",").append(0).append(",").append(getBuffsStats().getEffect(264)).append(",").append(getBuffsStats().getEffect(264)).append("|");
        ASData.append(_baseStats.getEffect(254)).append(",").append(getStuffStats().getEffect(254)).append(",").append(0).append(",").append(getBuffsStats().getEffect(254)).append(",").append(getBuffsStats().getEffect(254)).append("|");
        ASData.append(_baseStats.getEffect(242)).append(",").append(getStuffStats().getEffect(242)).append(",").append(0).append(",").append(getBuffsStats().getEffect(242)).append(",").append(getBuffsStats().getEffect(242)).append("|");
        ASData.append(_baseStats.getEffect(210)).append(",").append(getStuffStats().getEffect(210)).append(",").append(0).append(",").append(getBuffsStats().getEffect(210)).append(",").append(getBuffsStats().getEffect(210)).append("|");
        ASData.append(_baseStats.getEffect(260)).append(",").append(getStuffStats().getEffect(260)).append(",").append(0).append(",").append(getBuffsStats().getEffect(260)).append(",").append(getBuffsStats().getEffect(260)).append("|");
        ASData.append(_baseStats.getEffect(250)).append(",").append(getStuffStats().getEffect(250)).append(",").append(0).append(",").append(getBuffsStats().getEffect(250)).append(",").append(getBuffsStats().getEffect(250)).append("|");
        ASData.append(_baseStats.getEffect(243)).append(",").append(getStuffStats().getEffect(243)).append(",").append(0).append(",").append(getBuffsStats().getEffect(243)).append(",").append(getBuffsStats().getEffect(243)).append("|");
        ASData.append(_baseStats.getEffect(211)).append(",").append(getStuffStats().getEffect(211)).append(",").append(0).append(",").append(getBuffsStats().getEffect(211)).append(",").append(getBuffsStats().getEffect(211)).append("|");
        ASData.append(_baseStats.getEffect(261)).append(",").append(getStuffStats().getEffect(261)).append(",").append(0).append(",").append(getBuffsStats().getEffect(261)).append(",").append(getBuffsStats().getEffect(261)).append("|");
        ASData.append(_baseStats.getEffect(251)).append(",").append(getStuffStats().getEffect(251)).append(",").append(0).append(",").append(getBuffsStats().getEffect(251)).append(",").append(getBuffsStats().getEffect(251)).append("|");
        ASData.append(_baseStats.getEffect(244)).append(",").append(getStuffStats().getEffect(244)).append(",").append(0).append(",").append(getBuffsStats().getEffect(244)).append(",").append(getBuffsStats().getEffect(244)).append("|");
        ASData.append(_baseStats.getEffect(212)).append(",").append(getStuffStats().getEffect(212)).append(",").append(0).append(",").append(getBuffsStats().getEffect(212)).append(",").append(getBuffsStats().getEffect(212)).append("|");
        ASData.append(_baseStats.getEffect(262)).append(",").append(getStuffStats().getEffect(262)).append(",").append(0).append(",").append(getBuffsStats().getEffect(262)).append(",").append(getBuffsStats().getEffect(262)).append("|");
        ASData.append(_baseStats.getEffect(252)).append(",").append(getStuffStats().getEffect(252)).append(",").append(0).append(",").append(getBuffsStats().getEffect(252)).append(",").append(getBuffsStats().getEffect(252)).append("|");
        ASData.append(_baseStats.getEffect(240)).append(",").append(getStuffStats().getEffect(240)).append(",").append(0).append(",").append(getBuffsStats().getEffect(240)).append(",").append(getBuffsStats().getEffect(240)).append("|");
        ASData.append(_baseStats.getEffect(213)).append(",").append(getStuffStats().getEffect(213)).append(",").append(0).append(",").append(getBuffsStats().getEffect(213)).append(",").append(getBuffsStats().getEffect(213)).append("|");
        ASData.append(_baseStats.getEffect(263)).append(",").append(getStuffStats().getEffect(263)).append(",").append(0).append(",").append(getBuffsStats().getEffect(263)).append(",").append(getBuffsStats().getEffect(263)).append("|");
        ASData.append(_baseStats.getEffect(253)).append(",").append(getStuffStats().getEffect(253)).append(",").append(0).append(",").append(getBuffsStats().getEffect(253)).append(",").append(getBuffsStats().getEffect(253)).append("|");
        return ASData.toString();
    }
	
	public int getGrade()
	{
		if(_align == Constantes.ALIGNEMENT_NEUTRE)return 0;
		if(_honor >= 17500)return 10;
		for(int n = 1; n <=10; n++)
		{
			if(_honor < Mundo.getExpLevel(n).pvp)return n-1;
		}
		return 0;
	}
	
	public String xpString(String c)
	{
		return _curExp+c+ Mundo.getPersoXpMin(_lvl)+c+ Mundo.getPersoXpMax(_lvl);
	}
	
	public int emoteActive() {
		return _emoteActive;
	}

	public void setEmoteActive(int emoteActive) {
		this._emoteActive = emoteActive;
	}

	private Stats getStuffStats()
	{
		Stats stats = new Stats(false,null);
		ArrayList<Integer> itemSetApplied = new ArrayList<>();
		
		for(Entry<Integer, Objeto> entry : _items.entrySet())
		{
			if(entry.getValue().getPosition() != Constantes.ITEM_POS_NO_EQUIPED)
			{
				stats = Stats.cumulStat(stats,entry.getValue().getStats());
				int panID = entry.getValue().getTemplate().getPanopID();
				//Si panoplie, et si l'effet de pano n'a pas encore été ajouté
				if(panID>0 && !itemSetApplied.contains(panID))
				{
					itemSetApplied.add(panID);
					ItemSet IS = Mundo.getItemSet(panID);
					//Si la pano existe
					if(IS != null)
					{
						//on ajoute le bonus de pano en fonction du nombre d'item
						stats = Stats.cumulStat(stats,IS.getBonusStatByItemNumb(this.getNumbEquipedItemOfPanoplie(panID)));
					}
				}
			}
		}
		if(_onMount && _mount != null)
		{
			stats = Stats.cumulStat(stats, _mount.get_stats());
		}
		return stats;
	}

	private Stats getBuffsStats()
	{
		Stats stats = new Stats(false,null);
		for(Map.Entry<Integer, EfectoHechizo> entry : _buffs.entrySet())
		{
			stats.addOneStat(entry.getValue().getEffectID(), entry.getValue().getValue());
		}
		return stats;
	}

	public int get_orientation() {
		return _orientation;
	}

	public void set_orientation(int _orientation) {
		this._orientation = _orientation;
	}

	public int getInitiative()
	{
		int fact = 4;
		int pvmax = _PDVMAX - Constantes.getBasePdv(_classe);
		int pv = _PDV - Constantes.getBasePdv(_classe);
		if(_classe == Constantes.CLASS_SACRIEUR)fact = 8;
		double coef = pvmax/fact;
		
		coef += getStuffStats().getEffect(Constantes.STATS_ADD_INIT);
		
		coef += getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
		coef += getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
		coef += getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
		coef += getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
		
		int init = 1;
		if(pvmax != 0)
		 init = (int)(coef*((double)pv/(double)pvmax));
		if(init <0)
			init = 0;
		return init;
	}

	public Stats getTotalStats()
	{
		Stats total = new Stats(false,null);
		total = Stats.cumulStat(total,_baseStats);
		total = Stats.cumulStat(total,getStuffStats());
		total = Stats.cumulStat(total,getDonsStats());
		if(_fight == null)
			total = Stats.cumulStat(total,getBuffsStats());
		
		return total;
	}

	private Stats getDonsStats()
	{
		/* TODO*/
		Stats stats = new Stats(false,null);
		return stats;
	}

	public int getPodUsed()
	{
		int pod = 0;
		for(Entry<Integer, Objeto> entry : _items.entrySet())
		{
			pod += entry.getValue().getTemplate().getPod() * entry.getValue().getQuantity();
		}
		return pod;
	}

	public int getMaxPod() {
		int pods = getTotalStats().getEffect(Constantes.STATS_ADD_PODS);
		pods += getTotalStats().getEffect(Constantes.STATS_ADD_FORC)*5;
		for(StatsMetier SM : _metiers.values())
		{
			pods += SM.get_lvl()*5;
			if(SM.get_lvl() == 100) pods += 1000;
		}
		return pods;
	}

	
	
	public int get_PDV() {
		return _PDV;
	}

	public void set_PDV(int _pdv) {
		_PDV = _pdv;
		if(_group != null)
		{
			GestorSalida.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_group,this);
		}
	}

	public int get_PDVMAX() {
		return _PDVMAX;
	}

	public void set_PDVMAX(int _pdvmax) {
		_PDVMAX = _pdvmax;
		if(_group != null)
		{
			GestorSalida.GAME_SEND_PM_MOD_PACKET_TO_GROUP(_group,this);
		}
	}

	public void setSitted(boolean b)
	{
		_sitted = b;
		int diff = _PDV - _exPdv;
		int time = (b?1000:2000);
		
		_exPdv = _PDV;
		if(_isOnline)
		{//On envoie le message "Vous avez recuperer X pdv"
		GestorSalida.GAME_SEND_ILF_PACKET(this, diff);
		//On envoie la modif du Timer de regenPdv coté client
		GestorSalida.GAME_SEND_ILS_PACKET(this, time);
		}
		//on modifie le delay coté Serveur du timer de regenPDV
		_sitTimer.setDelay(time);
		//Si on se leve, on desactive l'émote
		if((_emoteActive == 1 || _emoteActive == 19) && b == false)_emoteActive = 0;
	}

	public byte get_align()
	{
		return _align;
	}
	
	public int get_pdvper() {
		int pdvper = 100;
		pdvper = (100*_PDV)/_PDVMAX;
		return pdvper;
	}

	public void emoticone(String str) 
	{
		try
		{
			int id = Integer.parseInt(str);
			Mapa map = _curCarte;
			if(_fight == null)
				GestorSalida.GAME_SEND_EMOTICONE_TO_MAP(map,_GUID,id);
			else
				GestorSalida.GAME_SEND_EMOTICONE_TO_FIGHT(_fight,7,_GUID,id);
		}catch(NumberFormatException e){return;};
	}

	public void refreshMapAfterFight()
	{
		_curCarte.addPlayer(this);
		if(_compte.getGameThread() != null && _compte.getGameThread().get_out() != null)
		{
			GestorSalida.GAME_SEND_STATS_PACKET(this);
			GestorSalida.GAME_SEND_ILS_PACKET(this, 1000);
		}
		_fight = null;
		_away = false;
	}

    public void boostStat(int stat)
    {
        int value = 0;
        switch(stat)
        {
        case 10: // '\n'
            value = _baseStats.getEffect(118);
            break;

        case 13: // '\r'
            value = _baseStats.getEffect(123);
            break;

        case 14: // '\016'
            value = _baseStats.getEffect(119);
            break;

        case 15: // '\017'
            value = _baseStats.getEffect(126);
            break;
        }
        int cout = Constantes.getReqPtsToBoostStatsByClass(_classe, stat, value);
        if(cout <= _capital)
        {
            switch(stat)
            {
            case 11: // '\013'
                if(_classe != 11)
                    _baseStats.addOneStat(125, 1);
                else
                    _baseStats.addOneStat(125, 2);
                break;

            case 12: // '\f'
                _baseStats.addOneStat(124, 1);
                break;

            case 10: // '\n'
                _baseStats.addOneStat(118, 1);
                break;

            case 13: // '\r'
                _baseStats.addOneStat(123, 1);
                break;

            case 14: // '\016'
                _baseStats.addOneStat(119, 1);
                break;

            case 15: // '\017'
                _baseStats.addOneStat(126, 1);
                break;

            default:
                return;
            }
            _capital -= cout;
            GestorSalida.GAME_SEND_STATS_PACKET(this);
        }
        // SQLManager.SAVE_PERSONNAGE(this, true);
    }

	public boolean isMuted()
	{
		return _compte.isMuted();
	}
	public void set_curCarte(Mapa carte)
	{
		_curCarte = carte;
	}

	public String parseObjetsToDB()
	{
		StringBuilder str = new StringBuilder();
		if(_items.isEmpty())return "";
		for(Entry<Integer, Objeto> entry : _items.entrySet())
		{
			Objeto obj = entry.getValue();
			str.append(obj.getGuid()).append("|");
		}
		return str.toString();
	}
	
	public boolean addObjet(Objeto newObj, boolean stackIfSimilar)
	{
		for(Entry<Integer, Objeto> entry : _items.entrySet())
		{
			Objeto obj = entry.getValue();
			if(obj.getTemplate().getID() == newObj.getTemplate().getID()
				&& obj.getStats().isSameStats(newObj.getStats())
				&& stackIfSimilar
				&& newObj.getTemplate().getType() != 85
				&& obj.getPosition() == Constantes.ITEM_POS_NO_EQUIPED)//Si meme Template et Memes Stats et Objet non équipé
			{
				obj.setQuantity(obj.getQuantity()+newObj.getQuantity());//On ajoute QUA item a la quantité de l'objet existant
				GestorSQL.guardar_objeto(obj);
				if(_isOnline) GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this,obj);
				return false;
			}
		}
		_items.put(newObj.getGuid(), newObj);
		GestorSalida.GAME_SEND_OAKO_PACKET(this,newObj);
		return true;
	}
	public void addObjet(Objeto newObj)
	{
		_items.put(newObj.getGuid(), newObj);
	}
	public Map<Integer, Objeto> getItems()
	{
		return _items;
	}
	public String parseItemToASK()
	{
		StringBuilder str = new StringBuilder();
		if(_items.isEmpty())return "";
		for(Objeto obj : _items.values())
		{
			str.append(obj.parseItem());
		}
		return str.toString();
	}

	public String getBankItemsIDSplitByChar(String splitter)
	{
		StringBuilder str = new StringBuilder();
		if(_compte.getBank().isEmpty())return "";
		for(int entry : _compte.getBank().keySet())
		{
			str.append(entry).append(splitter);
		}
		return str.toString();
	}
	
	public String getItemsIDSplitByChar(String splitter)
	{
		StringBuilder str = new StringBuilder();
		if(_items.isEmpty())return "";
		for(int entry : _items.keySet())
		{
			if(str.length() != 0) str.append(splitter);
			str.append(entry);
		}
		return str.toString();
	}
	
	public String getStoreItemsIDSplitByChar(String splitter)
	{
		StringBuilder str = new StringBuilder();
		if(_storeItems.isEmpty())return "";
		for(int entry : _storeItems.keySet())
		{
			if(str.length() != 0) str.append(splitter);
			str.append(entry);
		}
		return str.toString();
	}

	public boolean hasItemGuid(int guid)
	{
		return _items.get(guid) != null?_items.get(guid).getQuantity()>0:false;
	}
	
	public void sellItem(int guid,int qua)
	{
		if(qua <= 0)
			return;
		if(_items.get(guid).getQuantity() < qua)//Si il a moins d'item que ce qu'on veut Del
			qua = _items.get(guid).getQuantity();
		
		int prix = qua * (_items.get(guid).getTemplate().getPrix()/10);//Calcul du prix de vente (prix d'achat/10)
		int newQua =  _items.get(guid).getQuantity() - qua;
		
		if(newQua <= 0)//Ne devrait pas etre <0, S'il n'y a plus d'item apres la vente 
		{
			_items.remove(guid);
			Mundo.removeItem(guid);
			GestorSQL.eliminar_objeto(guid);
			GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
		}else//S'il reste des items apres la vente
		{
			_items.get(guid).setQuantity(newQua);
			GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, _items.get(guid));
		}
		_kamas = _kamas + prix;
		GestorSalida.GAME_SEND_STATS_PACKET(this);
		GestorSalida.GAME_SEND_Ow_PACKET(this);
		GestorSalida.GAME_SEND_ESK_PACKEt(this);
	}

	public void removeItem(int guid)
	{
		_items.remove(guid);
	}
	public void removeItem(int guid, int nombre,boolean send,boolean deleteFromWorld)
	{
		Objeto obj = _items.get(guid);
		
		if(nombre > obj.getQuantity())
			nombre = obj.getQuantity();
		
		if(obj.getQuantity() >= nombre)
		{
			int newQua = obj.getQuantity() - nombre;
			if(newQua >0)
			{
				obj.setQuantity(newQua);
				if(send && _isOnline)
					GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
			}else
			{
				//on supprime de l'inventaire et du Monde
				_items.remove(obj.getGuid());
				if(deleteFromWorld)
					Mundo.removeItem(obj.getGuid());
				//on envoie le packet si connecté
				if(send && _isOnline)
					GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
			}
		}
	}
	public void deleteItem(int guid)
	{
		_items.remove(guid);
		Mundo.removeItem(guid);
	}
	public Objeto getObjetByPos(int pos)
	{
		if(pos == Constantes.ITEM_POS_NO_EQUIPED)return null;
		
		for(Entry<Integer, Objeto> entry : _items.entrySet())
		{
			Objeto obj = entry.getValue();
			if(obj.getPosition() == pos)
				return obj;
		}
		return null;
	}

	public void refreshStats()
	{
		double actPdvPer = (100*(double)_PDV)/(double)_PDVMAX;
		_PDVMAX = (_lvl-1)*5+ Constantes.getBasePdv(_classe)+getTotalStats().getEffect(Constantes.STATS_ADD_VITA);
		_PDV = (int) Math.round(_PDVMAX*actPdvPer/100);
	}

	public void levelUp(boolean send,boolean addXp) {
		if(_lvl == Mundo.getExpLevelSize())return;
		_lvl++;
		_capital+=5;
		_spellPts++;
		_PDVMAX += 5;
		_PDV = _PDVMAX;
		if(_lvl == 100)
			_baseStats.addOneStat(Constantes.STATS_ADD_PA, 1);
		if(_lvl == 200)
			_baseStats.addOneStat(Constantes.STATS_ADD_PM, 1);
		if(_lvl == 300)
			_baseStats.addOneStat(Constantes.STATS_ADD_PA, 1);
		 if(_lvl == 300)
	            GestorSalida.GAME_SEND_MESSAGE_TO_ALL("Bravo a (<b>" + get_name() + "</b>) il vient d'atteindre le niveau <b>300</b> !", MainServidor.CONFIG_PUB_COLOR);


		Constantes.onLevelUpSpells(this,_lvl);
		if(addXp)_curExp = Mundo.getExpLevel(_lvl).perso;
		if(get_guild() != null) {
			GestorSQL.actualizar_miembro_del_gremio(getGuildMember());
		}
		if(send && _isOnline) {
			GestorSalida.GAME_SEND_NEW_LVL_PACKET(_compte.getGameThread().get_out(),_lvl);
			GestorSalida.GAME_SEND_STATS_PACKET(this);
			GestorSalida.GAME_SEND_SPELL_LIST(this);
		}
		GestorSQL.guardar_personaje(this, true);
	}
	
	public void addXp(long winxp) {
		_curExp += winxp;
		int exLevel = _lvl;
		while(_curExp >= Mundo.getPersoXpMax(_lvl) && _lvl< Mundo.getExpLevelSize())
			levelUp(true,false);
		if(_isOnline) {
			if(exLevel < _lvl) GestorSalida.GAME_SEND_NEW_LVL_PACKET(_compte.getGameThread().get_out(),_lvl);
			GestorSalida.GAME_SEND_STATS_PACKET(this);
		}
	}
	
	public void addKamas (long l)
	{
		_kamas += l;
	}

	public Objeto getSimilarItem(Objeto exObj) {
		for(Entry<Integer, Objeto> entry : _items.entrySet()) {
			Objeto obj = entry.getValue();
			
			if(obj.getTemplate().getID() == exObj.getTemplate().getID()
				&& obj.getStats().isSameStats(exObj.getStats())
				&& obj.getGuid() != exObj.getGuid()
				&& obj.getPosition() == Constantes.ITEM_POS_NO_EQUIPED)
			return obj;
		}
		return null;
	}

	public void setCurExchange(Exchange echg)
	{
		_curExchange = echg;
	}
	
	public Exchange get_curExchange()
	{
		return _curExchange;
	}

	public int learnJob(Oficio m) {
		for(Entry<Integer,StatsMetier> entry : _metiers.entrySet()) {
			if(entry.getValue().getTemplate().getId() == m.getId())//Si le joueur a déjà le métier
				return -1;
		}
		int Msize = _metiers.size();
		if(Msize == 6)//Si le joueur a déjà 6 métiers
			return -1;
		int pos = 0;
		if(Constantes.isMageJob(m.getId())) {
			if(_metiers.get(5) == null) pos = 5;
			if(_metiers.get(4) == null) pos = 4;
			if(_metiers.get(3) == null) pos = 3;
		}else {
			if(_metiers.get(2) == null) pos = 2;
			if(_metiers.get(1) == null) pos = 1;
			if(_metiers.get(0) == null) pos = 0;
		}
		
		StatsMetier sm = new StatsMetier(pos,m,1,0);
		_metiers.put(pos, sm);//On apprend le métier lvl 1 avec 0 xp
		if(_isOnline) {
			//on créer la listes des statsMetier a envoyer (Seulement celle ci)
			ArrayList<StatsMetier> list = new ArrayList<>();
			list.add(sm);
			
			GestorSalida.GAME_SEND_Im_PACKET(this, "02;"+m.getId());
			//packet JS
			GestorSalida.GAME_SEND_JS_PACKET(this, list);
			//packet JX
			GestorSalida.GAME_SEND_JX_PACKET(this, list);
			//Packet JO (Job Option)
			GestorSalida.GAME_SEND_JO_PACKET(this,list);
			
			Objeto obj = getObjetByPos(Constantes.ITEM_POS_ARME);
			if(obj != null)
				if(sm.getTemplate().isValidTool(obj.getTemplate().getID()))
					GestorSalida.GAME_SEND_OT_PACKET(_compte.getGameThread().get_out(),m.getId());
		}
		return pos;
	}
	
	public void unlearnJob(int m)
	{
		_metiers.remove(m);
	}

	public boolean hasEquiped(int id) {
		for(Entry<Integer, Objeto> entry : _items.entrySet())
			if(entry.getValue().getTemplate().getID() == id && entry.getValue().getPosition() != Constantes.ITEM_POS_NO_EQUIPED)
				return true;
		return false;
	}

	public void setInvitation(int target)
	{
		_inviting = target;
	}
	
	public int getInvitation()
	{
		return _inviting;
	}
	
	public String parseToPM() {
		StringBuilder str = new StringBuilder();
		str.append(_GUID).append(";");
		str.append(_name).append(";");
		str.append(_gfxID).append(";");
		str.append(_color1).append(";");
		str.append(_color2).append(";");
		str.append(_color3).append(";");
		str.append(getGMStuffString()).append(";");
		str.append(_PDV).append(",").append(_PDVMAX).append(";");
		str.append(_lvl).append(";");
		str.append(getInitiative()).append(";");
		str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PROS)).append(";");
		str.append("0");//Side = ?
		return str.toString();
	}
	
	public int getNumbEquipedItemOfPanoplie(int panID) {
		int nb = 0;
		for(Entry<Integer, Objeto> i : _items.entrySet())
		{
			//On ignore les objets non équipés
			if(i.getValue().getPosition() == Constantes.ITEM_POS_NO_EQUIPED)continue;
			//On prend que les items de la pano demandée, puis on augmente le nombre si besoin
			if(i.getValue().getTemplate().getPanopID() == panID)nb++;
		}
		return nb;
	}

	public void startActionOnCell(GameAction GA) {
		int cellID = -1;
		int action = -1;
		try {
			cellID = Integer.parseInt(GA._args.split(";")[0]);
			action = Integer.parseInt(GA._args.split(";")[1]);
		}catch(Exception ignored){};
		if(cellID == -1 || action == -1)return;
		//Si case invalide
		if(!_curCarte.getCase(cellID).canDoAction(action))return;
		_curCarte.getCase(cellID).startAction(this,GA);
	}

	public void finishActionOnCell(GameAction GA) {
		int cellID = -1;
		try {
			cellID = Integer.parseInt(GA._args.split(";")[0]);
		}catch(Exception ignored){};
		if(cellID == -1)return;
		_curCarte.getCase(cellID).finishAction(this,GA);
	}
	
	public void teleport(short newMapID, int newCellID) {
		PrintWriter PW = null;
		if(_compte.getGameThread() != null) {
			PW = _compte.getGameThread().get_out();
		}
		if(Mundo.getCarte(newMapID) == null) {
			JuegoServidor.addToLog("Game: INVALID MAP : "+newMapID);
			return;
		}
		if(Mundo.getCarte(newMapID).getCase(newCellID) == null) {
			JuegoServidor.addToLog("Game: INVALID CELL : "+newCellID+" ON MAP : "+newMapID);
			return;
		}
		if(PW != null) {
			GestorSalida.GAME_SEND_GA2_PACKET(PW,_GUID);
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_curCarte, _GUID);
		}
		_curCell.removePlayer(_GUID);
		_curCarte = Mundo.getCarte(newMapID);
		_curCell = _curCarte.getCase(newCellID);
		
		//Verification de la carte
		//Verifier la validité du mountpark
		if(_curCarte.getMountPark() != null && _curCarte.getMountPark().get_owner() > 0 && _curCarte.getMountPark().get_guild().get_id() != -1) {
			if(Mundo.getGuild(_curCarte.getMountPark().get_guild().get_id()) == null){ //Ne devrait pas arriver
				JuegoServidor.addToLog("[MountPark] Suppression d'un MountPark a Guild invalide. GuildID : "+_curCarte.getMountPark().get_guild().get_id());
				Mapa.MountPark.removeMountPark(_curCarte.getMountPark().get_guild().get_id());
			}
		}
		//Verifier la validité du percepteur
		if(Recaudador.GetPercoByMapID(_curCarte.get_id()) != null) {
			if(Mundo.getGuild(Recaudador.GetPercoByMapID(_curCarte.get_id()).get_guildID()) == null)//Ne devrait pas arriver
			{
				JuegoServidor.addToLog("[Percepteur] Suppression d'un Percepteur a Guild invalide. GuildID : "+ Recaudador.GetPercoByMapID(_curCarte.get_id()).get_guildID());
				Recaudador.removePercepteur(Recaudador.GetPercoByMapID(_curCarte.get_id()).get_guildID());
			}
		}
		
		if(PW != null) {
		GestorSalida.GAME_SEND_MAPDATA(
				PW,
				newMapID,
				_curCarte.get_date(),
				_curCarte.get_key());
		_curCarte.addPlayer(this);
		}
		
		if(!_Follower.isEmpty())//On met a jour la carte des personnages qui nous suivent
		{
			for(Jugador t : _Follower.values()) {
				if(t.isOnline())
					GestorSalida.GAME_SEND_FLAG_PACKET(t, this);
				else
					_Follower.remove(t.get_GUID());
			}
		}

	}
	
	public int getBankCost()
	{
		return _compte.getBank().size();
	}
	
	public String getStringVar(String str)
	{
		//TODO completer
		if(str.equals("name"))return _name;
		if(str.equals("bankCost"))
		{
			return getBankCost()+"";
		}
		return "";
	}

	public void setBankKamas(long i)
	{
		_compte.setBankKamas(i);
		GestorSQL.actualizar_datos_cuenta(_compte);
	}
	
	public long getBankKamas()
	{
		return _compte.getBankKamas();
	}

	public void setInBank(boolean b)
	{
		_isInBank = b;
	}
	public boolean isInBank()
	{
		return _isInBank;
	}

	public String parseBankPacket()
	{
		StringBuilder packet = new StringBuilder();
		for(Entry<Integer, Objeto> entry : _compte.getBank().entrySet())
			packet.append("O").append(entry.getValue().parseItem()).append(";");
		if(getBankKamas() != 0)
			packet.append("G").append(getBankKamas());
		return packet.toString();
	}

	public void addCapital(int pts)
	{
		_capital += pts;
	}

	public void addSpellPoint(int pts)
	{
		_spellPts += pts;
	}

	public void addInBank(int guid, int qua)
	{
		Objeto PersoObj = Mundo.getObjet(guid);
		//Si le joueur n'a pas l'item dans son sac ...
		if(_items.get(guid) == null)
		{
			JuegoServidor.addToLog("Le joueur "+_name+" a tenter d'ajouter un objet en banque qu'il n'avait pas.");
			return;
		}
		//Si c'est un item équipé ...
		if(PersoObj.getPosition() != Constantes.ITEM_POS_NO_EQUIPED)return;
		
		Objeto BankObj = getSimilarBankItem(PersoObj);
		int newQua = PersoObj.getQuantity() - qua;
		if(BankObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On met l'objet du sac dans la banque, avec la meme quantité
				_compte.getBank().put(PersoObj.getGuid(), PersoObj);
				String str = "O+"+PersoObj.getGuid()+"|"+PersoObj.getQuantity()+"|"+PersoObj.getTemplate().getID()+"|"+PersoObj.parseStatsString();
				GestorSalida.GAME_SEND_EsK_PACKET(this, str);
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
				
			}
			else//S'il reste des objets au joueur
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				//On ajoute l'objet a la banque et au monde
				BankObj = Objeto.getCloneObjet(PersoObj, qua);
				Mundo.addObjet(BankObj, true);
				_compte.getBank().put(BankObj.getGuid(), BankObj);
				
				//Envoie des packets
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				GestorSalida.GAME_SEND_EsK_PACKET(this, str);
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On enleve l'objet du monde
				Mundo.removeItem(PersoObj.getGuid());
				//On ajoute la quantité a l'objet en banque
				BankObj.setQuantity(BankObj.getQuantity() + PersoObj.getQuantity());
				//on envoie l'ajout a la banque de l'objet
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				GestorSalida.GAME_SEND_EsK_PACKET(this, str);
				//on envoie la supression de l'objet du sac au joueur
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
				
			}else //S'il restait des objets
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				BankObj.setQuantity(BankObj.getQuantity() + qua);
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				GestorSalida.GAME_SEND_EsK_PACKET(this, str);
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}
		GestorSalida.GAME_SEND_Ow_PACKET(this);
		GestorSQL.actualizar_datos_cuenta(_compte);
	}

	private Objeto getSimilarBankItem(Objeto obj)
	{
		for(Objeto value : _compte.getBank().values())
		{
			if(value.getTemplate().getType() == 85)
				continue;
			if(value.getTemplate().getID() == obj.getTemplate().getID() && value.getStats().isSameStats(obj.getStats()))
				return value;
		}
		return null;
	}

	public void removeFromBank(int guid, int qua)
	{
		Objeto BankObj = Mundo.getObjet(guid);
		//Si le joueur n'a pas l'item dans sa banque ...
		if(_compte.getBank().get(guid) == null)
		{
			JuegoServidor.addToLog("Le joueur "+_name+" a tenter de retirer un objet en banque qu'il n'avait pas.");
			return;
		}
		
		Objeto PersoObj = getSimilarItem(BankObj);
		
		int newQua = BankObj.getQuantity() - qua;
		
		if(PersoObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien en banque
			if(newQua <= 0)
			{
				//On retire l'item de la banque
				_compte.getBank().remove(guid);
				//On l'ajoute au joueur
				_items.put(guid, BankObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(this,BankObj);
				String str = "O-"+guid;
				GestorSalida.GAME_SEND_EsK_PACKET(this, str);
				
			}else //S'il reste des objets en banque
			{
				//On crée une copy de l'item en banque
				PersoObj = Objeto.getCloneObjet(BankObj, qua);
				//On l'ajoute au monde
				Mundo.addObjet(PersoObj, true);
				//On retire X objet de la banque
				BankObj.setQuantity(newQua);
				//On l'ajoute au joueur
				_items.put(PersoObj.getGuid(), PersoObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(this,PersoObj);
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				GestorSalida.GAME_SEND_EsK_PACKET(this, str);
				
			}
		}
		else
		{
			//S'il ne reste rien en banque
			if(newQua <= 0)
			{
				//On retire l'item de la banque
				_compte.getBank().remove(BankObj.getGuid());
				Mundo.removeItem(BankObj.getGuid());
				//On Modifie la quantité de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + BankObj.getQuantity());
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				String str = "O-"+guid;
				GestorSalida.GAME_SEND_EsK_PACKET(this, str);
				
			}
			else//S'il reste des objets en banque
			{
				//On retire X objet de la banque
				BankObj.setQuantity(newQua);
				//On ajoute X objets au joueurs
				PersoObj.setQuantity(PersoObj.getQuantity() + qua);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				GestorSalida.GAME_SEND_EsK_PACKET(this, str);
				
			}
		}
		GestorSalida.GAME_SEND_Ow_PACKET(this);
		GestorSQL.actualizar_datos_cuenta(_compte);
	}

	public void openMountPark()
	{
		if(getDeshonor() >= 5) 
		{
			GestorSalida.GAME_SEND_Im_PACKET(this, "183");
			return;
		}
		
		_inMountPark = _curCarte.getMountPark();
		_away = true;
		String str = _inMountPark.parseData(get_GUID(), (_inMountPark.get_owner()==-1?true:false));
		
		if(_inMountPark.get_owner() == -1 || _inMountPark.get_owner() == this.get_GUID())//Public ou le proprio
		{
			GestorSalida.GAME_SEND_ECK_PACKET(this, 16, str);
		}else if(get_guild() != null && 
				Mundo.getPersonnage(_inMountPark.get_owner()).get_guild() != null &&
				Mundo.getPersonnage(_inMountPark.get_owner()).get_guild() == get_guild() &&
				getGuildMember().canDo(Constantes.G_USEENCLOS))//Meme guilde + droits
		{
			GestorSalida.GAME_SEND_ECK_PACKET(this, 16, str);
		}else
		{
			GestorSalida.GAME_SEND_Im_PACKET(this, "1101");
			_inMountPark = null;
			_away = false;
		}
	}
	
	public void leftMountPark()
	{
		if(_inMountPark == null)return;
		_inMountPark = null;
	}

	public MountPark getInMountPark()
	{
		return _inMountPark;
	}

	public void fullPDV()
	{
		_PDV = _PDVMAX;
	}

	public void warpToSavePos()
	{
		try
		{
			String[] infos = _savePos.split(",");
			teleport(Short.parseShort(infos[0]), Integer.parseInt(infos[1]));
		}catch(Exception e){};
	}
	
	public void removeByTemplateID(int tID, int count)
	{
		//Copie de la liste pour eviter les modif concurrentes
		ArrayList<Objeto> list = new ArrayList<>();
		list.addAll(_items.values());
		
		ArrayList<Objeto> remove = new ArrayList<>();
		int tempCount = count;
		
		//on verifie pour chaque objet
		for(Objeto obj : list)
		{
			//Si mauvais TemplateID, on passe
			if(obj.getTemplate().getID() != tID)continue;
			
			if(obj.getQuantity() >= count)
			{
				int newQua = obj.getQuantity() - count;
				if(newQua >0)
				{
					obj.setQuantity(newQua);
					if(_isOnline)
						GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
				}else
				{
					//on supprime de l'inventaire et du Monde
					_items.remove(obj.getGuid());
					Mundo.removeItem(obj.getGuid());
					//on envoie le packet si connecté
					if(_isOnline)
						GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
				}
				return;
			}
			else//Si pas assez d'objet
			{
				if(obj.getQuantity() >= tempCount)
				{
					int newQua = obj.getQuantity() - tempCount;
					if(newQua > 0)
					{
						obj.setQuantity(newQua);
						if(_isOnline)
							GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
					}
					else remove.add(obj);
					
					for(Objeto o : remove)
					{
						//on supprime de l'inventaire et du Monde
						_items.remove(o.getGuid());
						Mundo.removeItem(o.getGuid());
						//on envoie le packet si connecté
						if(_isOnline)
							GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, o.getGuid());
					}
				}else
				{
					// on réduit le compteur
					tempCount -= obj.getQuantity();
					remove.add(obj);
				}
			}
		}
	}

	public Map<Integer,StatsMetier> getMetiers()
	{
		return _metiers;
	}

	public void doJobAction(int actionID, InteractiveObject object, GameAction GA,Case cell)
	{
		StatsMetier SM = getMetierBySkill(actionID);
		if(SM == null)return;
		SM.startAction(actionID,this, object,GA,cell);
	}
	public void finishJobAction(int actionID, InteractiveObject object, GameAction GA,Case cell)
	{
		StatsMetier SM = getMetierBySkill(actionID);
		if(SM == null)return;
		SM.endAction(actionID,this, object,GA,cell);
	}

	public String parseJobData()
	{
		StringBuilder str = new StringBuilder();
		if(_metiers.isEmpty())return "";
		for(StatsMetier SM : _metiers.values())
		{
			if(str.length() >0)str.append(";");
			str.append(SM.getTemplate().getId()).append(",").append(SM.getXp());
		}
		return str.toString();
	}
	
	public int totalJobBasic()
	{
		int i=0;

		for(StatsMetier SM : _metiers.values())
		{
			// Si c'est un métier 'basic' :
			if(SM.getTemplate().getId() == 	2 || SM.getTemplate().getId() == 11 ||
			   SM.getTemplate().getId() == 13 || SM.getTemplate().getId() == 14 ||
			   SM.getTemplate().getId() == 15 || SM.getTemplate().getId() == 16 ||
			   SM.getTemplate().getId() == 17 || SM.getTemplate().getId() == 18 ||
			   SM.getTemplate().getId() == 19 || SM.getTemplate().getId() == 20 ||
			   SM.getTemplate().getId() == 24 || SM.getTemplate().getId() == 25 ||
			   SM.getTemplate().getId() == 26 || SM.getTemplate().getId() == 27 ||
			   SM.getTemplate().getId() == 28 || SM.getTemplate().getId() == 31 ||
			   SM.getTemplate().getId() == 36 || SM.getTemplate().getId() == 41 ||
			   SM.getTemplate().getId() == 56 || SM.getTemplate().getId() == 58 ||
			   SM.getTemplate().getId() == 60 || SM.getTemplate().getId() == 65)
			{
			i++;
			}
		}
		return i;
	}
	
	public int totalJobFM()
	{
		int i=0;

		for(StatsMetier SM : _metiers.values())
		{
			// Si c'est une spécialisation 'FM' :
			if(SM.getTemplate().getId() == 	43 || SM.getTemplate().getId() == 44 ||
			   SM.getTemplate().getId() == 45 || SM.getTemplate().getId() == 46 ||
			   SM.getTemplate().getId() == 47 || SM.getTemplate().getId() == 48 ||
			   SM.getTemplate().getId() == 49 || SM.getTemplate().getId() == 50 ||
			   SM.getTemplate().getId() == 62 || SM.getTemplate().getId() == 63 ||
			   SM.getTemplate().getId() == 64)
			{
			i++;
			}
		}
		return i;
	}
	
	public boolean canAggro() {
		return _canAggro;
	}

	public void set_canAggro(boolean canAggro) {
		_canAggro = canAggro;
	}

	public void setCurJobAction(JobAction JA)
	{
		_curJobAction = JA;
	}
	public JobAction getCurJobAction()
	{
		return _curJobAction;
	}

	public StatsMetier getMetierBySkill(int skID)
	{
		for(StatsMetier SM : _metiers.values())
			if(SM.isValidMapAction(skID))return SM;
		return null;
	}

	public String parseToFriendList(int guid)
	{
		StringBuilder str = new StringBuilder();
		str.append(";");
		str.append("?;");//FIXME
		str.append(_name).append(";");
		if(_compte.isFriendWith(guid))
		{
			str.append(_lvl).append(";");
			str.append(_align).append(";");
		}else
		{
			str.append("?;");
			str.append("-1;");
		}
		str.append(_classe).append(";");
		str.append(_sexe).append(";");
		str.append(_gfxID);
		return str.toString();
	}
	
	public String parseToEnemyList(int guid)
	{
		StringBuilder str = new StringBuilder();
		str.append(";");
		str.append("?;");//FIXME
		str.append(_name).append(";");
		if(_compte.isFriendWith(guid))
		{
			str.append(_lvl).append(";");
			str.append(_align).append(";");
		}else
		{
			str.append("?;");
			str.append("-1;");
		}
		str.append(_classe).append(";");
		str.append(_sexe).append(";");
		str.append(_gfxID);
		return str.toString();
	}

	public StatsMetier getMetierByID(int job)
	{
		for(StatsMetier SM : _metiers.values())if(SM.getTemplate().getId() == job)return SM;
		return null;
	}

	public boolean isOnMount()
	{
		return _onMount;
	}
	public void toogleOnMount()
	{
		_onMount = !_onMount;
		Objeto obj = getObjetByPos(Constantes.ITEM_POS_FAMILIER);
		if(_onMount && obj != null)
		{
			obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
			GestorSalida.GAME_SEND_OBJET_MOVE_PACKET(this, obj);
		}
		//on envoie les packets
		if(get_fight() != null && get_fight().get_state() == 2)
		{
			GestorSalida.GAME_SEND_ALTER_FIGHTER_MOUNT(get_fight(), get_fight().getFighterByPerso(this), get_GUID(), get_fight().getTeamID(get_GUID()), get_fight().getOtherTeamID(get_GUID()));
		}else
		{
			GestorSalida.GAME_SEND_ALTER_GM_PACKET(_curCarte,this);
		}
		GestorSalida.GAME_SEND_Re_PACKET(this, "+", _mount);
		GestorSalida.GAME_SEND_Rr_PACKET(this,_onMount?"+":"-");
		GestorSalida.GAME_SEND_STATS_PACKET(this);
	}
	public int getMountXpGive()
	{
		return _mountXpGive;
	}

	public Dragopavo getMount()
	{
		return _mount;
	}

	public void setMount(Dragopavo DD)
	{
		_mount = DD;
	}

	public void setMountGiveXp(int parseInt)
	{
		_mountXpGive = parseInt;
	}
	
	public void resetVars()
	{
		_isTradingWith = 0;
		_isTalkingWith = 0;
		_away = false;
		_emoteActive = 0;
		_fight = null;
		_duelID = 0;
		_ready = false;
		_curExchange = null;
		_group = null;
		_isInBank = false;
		_inviting = 0;
		_sitted = false;
		_curJobAction = null;
		_isZaaping = false;
		_inMountPark = null;
		_onMount = false;
		_isOnPercepteurID = 0;
		_isClone = false;
		_isForgetingSpell = false;
		_isAbsent = false;
		_isInvisible = false;
		_Follower.clear();
		_Follows = null;
		_curTrunk = null;
		_curHouse = null;
		_isGhosts = false;
	}
	
	public void addChanel(String chan)
	{
		if(_canaux.contains(chan))return;
		_canaux += chan;
		GestorSalida.GAME_SEND_cC_PACKET(this, '+', chan);
	}
	
	public void removeChanel(String chan)
	{
		_canaux = _canaux.replace(chan, "");
		GestorSalida.GAME_SEND_cC_PACKET(this, '-', chan);
	}

	public void modifAlignement(byte a)
	{
		//Reset Variables
		_honor = 0;
		_deshonor = 0;
		_align = a;
		_aLvl = 1;
		//envoies des packets
		//Im022;10~42 ?
		GestorSalida.GAME_SEND_ZC_PACKET(this, a);
		GestorSalida.GAME_SEND_STATS_PACKET(this);
		//Im045;50 ?
	}

	public void setDeshonor(int deshonor)
	{
		_deshonor = deshonor;
	}

	public int getDeshonor()
	{
		return _deshonor;
	}
	
	public void setShowWings(boolean showWings) {
		_showWings = showWings;
	}
	
	public int get_honor()
	{
		return _honor;
	}

	public void set_honor(int honor)
	{
		_honor = honor;
	}
	public void setALvl(int a)
	{
		_aLvl = a;
	}
	public int getALvl()
	{
		return _aLvl;
	}

	public void toggleWings(char c)
	{
		if(_align == Constantes.ALIGNEMENT_NEUTRE)return;
		int hloose = _honor*5/100;//FIXME: perte de X% honneur
		switch(c)
		{
		case '*':
			GestorSalida.GAME_SEND_GIP_PACKET(this,hloose);
		return;
		case '+':
			setShowWings(true);
			GestorSalida.GAME_SEND_STATS_PACKET(this);
			GestorSQL.guardar_personaje(this, false);
		break;
		case '-':
			setShowWings(false);
			_honor -= hloose;
			GestorSalida.GAME_SEND_STATS_PACKET(this);
			GestorSQL.guardar_personaje(this, false);
		break;
		}
		//SocketManager.GAME_SEND_ALTER_GM_PACKET(_curCarte, this);
	}

	public void addHonor(int winH)
	{
		int g = getGrade();
		_honor += winH;
		//Changement de grade
		if(getGrade() != g)
		{
			//TODO: Message IG
		}
	}

	public GuildMember getGuildMember()
	{
		return _guildMember;
	}

	public int getAccID()
	{
		return _accID;
	}

	public void setAccount(Cuenta c)
	{
		_compte = c;
	}
	public String parseZaapList()//Pour le packet WC
	{
		String map = _curCarte.get_id()+"";
		try
		{
			map = _savePos.split(",")[0];
		}catch(Exception e){};
		
		StringBuilder str = new StringBuilder();
		str.append(map);
		int SubAreaID = _curCarte.getSubArea().get_area().get_superArea().get_id();
		for(short i : _zaaps)
		{
			if(Mundo.getCarte(i) == null)continue;
			if(Mundo.getCarte(i).getSubArea().get_area().get_superArea().get_id() != SubAreaID)continue;
			int cost = Formulas.calculZaapCost(_curCarte, Mundo.getCarte(i));
			if(i == _curCarte.get_id()) cost = 0;
			str.append("|").append(i).append(";").append(cost);
		}
		return str.toString();
	}
	public boolean hasZaap(int mapID)
	{
		for(int i : _zaaps)if( i == mapID)return true;
		return false;
	}

	public void openZaapMenu()
	{
		if(this._fight == null)//On ouvre si il n'est pas en combat
		{
			if(getDeshonor() >= 3) 
			{
				GestorSalida.GAME_SEND_Im_PACKET(this, "183");
				return;
			}
			_isZaaping = true;
			if(!hasZaap(_curCarte.get_id()))//Si le joueur ne connaissait pas ce zaap
			{
				_zaaps.add(_curCarte.get_id());
				GestorSalida.GAME_SEND_Im_PACKET(this, "024");
				GestorSQL.guardar_personaje(this, false);
			}
			GestorSalida.GAME_SEND_WC_PACKET(this);
		}
	}
	public void useZaap(short id)
	{
		if(!_isZaaping)return;//S'il n'a pas ouvert l'interface Zaap(hack?)
		if(_fight != null) return;//Si il combat
		if(!hasZaap(id))return;//S'il n'a pas le zaap demandé(ne devrais pas arriver)
		int cost = Formulas.calculZaapCost(_curCarte, Mundo.getCarte(id));
		if(_kamas < cost)return;//S'il n'a pas les kamas (verif coté client)
		short mapID = id;
		int SubAreaID = _curCarte.getSubArea().get_area().get_superArea().get_id();
		int cellID = Mundo.getZaapCellIdByMapId(id);
		if(Mundo.getCarte(mapID) == null)
		{
			JuegoServidor.addToLog("La map "+id+" n'est pas implantee, Zaap refuse");
			GestorSalida.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(Mundo.getCarte(mapID).getCase(cellID) == null)
		{
			JuegoServidor.addToLog("La cellule associee au zaap "+id+" n'est pas implantee, Zaap refuse");
			GestorSalida.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(!Mundo.getCarte(mapID).getCase(cellID).isWalkable(true))
		{
			JuegoServidor.addToLog("La cellule associee au zaap "+id+" n'est pas 'walkable', Zaap refuse");
			GestorSalida.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(Mundo.getCarte(mapID).getSubArea().get_area().get_superArea().get_id() != SubAreaID)
		{
			GestorSalida.GAME_SEND_WUE_PACKET(this);
			return;
		}
		_kamas -= cost;
		teleport(mapID,cellID);
		GestorSalida.GAME_SEND_STATS_PACKET(this);//On envoie la perte de kamas
		GestorSalida.GAME_SEND_WV_PACKET(this);//On ferme l'interface Zaap
		_isZaaping = false;
	}
	public String parseZaaps()
	{
		StringBuilder str = new StringBuilder();
		boolean first = true;
		
		if(_zaaps.isEmpty())return "";
		for(int i : _zaaps)
		{
			if(!first) str.append(",");
			first = false;
			str.append(i);
		}
		return str.toString();
	}
	public void stopZaaping()
	{
		if(!_isZaaping)return;
		_isZaaping = false;
		GestorSalida.GAME_SEND_WV_PACKET(this);
	}
	
	public void Zaapi_close()
	{
		if(!_isZaaping)return;
		_isZaaping = false;
		GestorSalida.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
	}
	
	public void Zaapi_use(String packet)
	{
		Mapa map = Mundo.getCarte(Short.valueOf(packet.substring(2)));
	
		short idcelula = 100;
		if (map != null)
		{
			for (Entry<Integer, Case> entry  : map.GetCases().entrySet())
			{
			InteractiveObject obj = entry.getValue().getObject();
			if (obj != null)
				{
				if (obj.getID() == 7031 || obj.getID() == 7030)
					{
						idcelula = (short) (entry.getValue().getID() + 18);
					}
				}
			}
		}
		if (map.getSubArea().get_area().get_id() == 7 || map.getSubArea().get_area().get_id() == 11)
		{
		int price = 20;
		if (this.get_align() == 1 || this.get_align() == 2)
		price = 10;
		_kamas -= price;
		GestorSalida.GAME_SEND_STATS_PACKET(this);
		this.teleport(Short.valueOf(packet.substring(2)), idcelula);
		GestorSalida.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
		}
	}
		
	public boolean hasItemTemplate(int i, int q)
	{
		for(Objeto obj : _items.values())
		{
			if(obj.getPosition() != Constantes.ITEM_POS_NO_EQUIPED)continue;
			if(obj.getTemplate().getID() != i)continue;
			if(obj.getQuantity() >= q)return true;
		}
		return false;
	}

	public void SetZaaping(boolean zaaping) {
		_isZaaping = zaaping;
		
	}
	
	public void setisForgetingSpell(boolean isForgetingSpell) {
		_isForgetingSpell = isForgetingSpell;
	}
	
	public boolean isForgetingSpell() {
		return _isForgetingSpell;
	}
	
	public boolean isDispo(Jugador sender)
	{
		if(_isAbsent)
			return false;
		
		if(_isInvisible)
		{
			return _compte.isFriendWith(sender.get_compte().get_GUID());
		}
		
		return true;
	}
	
	public boolean get_isClone()
	{
		return _isClone;
	}
	
	public void set_isClone(boolean isClone)
	{
		_isClone = isClone;
	}
	
	public int get_isOnPercepteurID()
	{
		return _isOnPercepteurID;
	}
	
	public void set_isOnPercepteurID(int isOnPercepteurID)
	{
		_isOnPercepteurID = isOnPercepteurID;
	}
	
	public void set_title(byte title)
	{
		_title = title;
	}
	
	public byte get_title()
	{
		return _title;
	}
	
	public long getLastPacketTime()
	{
		return _lastPacketTime;
	}
	
	public void refreshLastPacketTime()
	{
		_lastPacketTime = System.currentTimeMillis();
	}
	
	public static Jugador ClonePerso(Jugador P, int id)
	{	
		TreeMap<Integer,Integer> stats = new TreeMap<>();
		stats.put(Constantes.STATS_ADD_VITA, P.get_baseStats().getEffect(Constantes.STATS_ADD_VITA));
		stats.put(Constantes.STATS_ADD_FORC, P.get_baseStats().getEffect(Constantes.STATS_ADD_FORC));
		stats.put(Constantes.STATS_ADD_SAGE, P.get_baseStats().getEffect(Constantes.STATS_ADD_SAGE));
		stats.put(Constantes.STATS_ADD_INTE, P.get_baseStats().getEffect(Constantes.STATS_ADD_INTE));
		stats.put(Constantes.STATS_ADD_CHAN, P.get_baseStats().getEffect(Constantes.STATS_ADD_CHAN));
		stats.put(Constantes.STATS_ADD_AGIL, P.get_baseStats().getEffect(Constantes.STATS_ADD_AGIL));
		stats.put(Constantes.STATS_ADD_PA, P.get_baseStats().getEffect(Constantes.STATS_ADD_PA));
		stats.put(Constantes.STATS_ADD_PM, P.get_baseStats().getEffect(Constantes.STATS_ADD_PM));
		stats.put(Constantes.STATS_ADD_RP_NEU, P.get_baseStats().getEffect(Constantes.STATS_ADD_RP_NEU));
		stats.put(Constantes.STATS_ADD_RP_TER, P.get_baseStats().getEffect(Constantes.STATS_ADD_RP_TER));
		stats.put(Constantes.STATS_ADD_RP_FEU, P.get_baseStats().getEffect(Constantes.STATS_ADD_RP_FEU));
		stats.put(Constantes.STATS_ADD_RP_EAU, P.get_baseStats().getEffect(Constantes.STATS_ADD_RP_EAU));
		stats.put(Constantes.STATS_ADD_RP_AIR, P.get_baseStats().getEffect(Constantes.STATS_ADD_RP_AIR));
		stats.put(Constantes.STATS_ADD_AFLEE, P.get_baseStats().getEffect(Constantes.STATS_ADD_AFLEE));
		stats.put(Constantes.STATS_ADD_MFLEE, P.get_baseStats().getEffect(Constantes.STATS_ADD_MFLEE));
		
		byte showWings = 0;
		int alvl = 0;
		if(P.get_align() != 0 && P._showWings)
		{
			showWings = 1;
			alvl = P.getGrade();
		}
		int mountID = -1;
		if(P.getMount() != null)
		{
			mountID = P.getMount().get_id();
		}
		
		Jugador Clone = new Jugador(
				id, 
				P.get_name(), 
				P.get_sexe(), 
				P.get_classe(), 
				P.get_color1(), 
				P.get_color2(), 
				P.get_color3(), 
				P.get_lvl(), 
				100, 
				P.get_gfxID(),
				stats,
				P.parseObjetsToDB(),
				100,
				showWings,
				mountID,
				alvl,
				P.get_align()
				);
		
		Clone.set_isClone(true);
		if(P._onMount)
		{
			Clone._onMount = true;
		}
		return Clone;
	}
	
	public void VerifAndChangeItemPlace()
	{
		boolean isFirstAM = true;
		boolean isFirstAN = true;
		boolean isFirstANb = true;
		boolean isFirstAR = true;
		boolean isFirstBO = true;
		boolean isFirstBOb = true;
		boolean isFirstCA = true;
		boolean isFirstCE = true;
		boolean isFirstCO = true;
		boolean isFirstDa = true;
		boolean isFirstDb = true;
		boolean isFirstDc = true;
		boolean isFirstDd = true;
		boolean isFirstDe = true;
		boolean isFirstDf = true;
		boolean isFirstFA = true;
		for(Objeto obj : _items.values())
		{
			if(obj.getPosition() == Constantes.ITEM_POS_NO_EQUIPED)continue;
			if(obj.getPosition() == Constantes.ITEM_POS_AMULETTE)
			{
				if(isFirstAM)
				{
					isFirstAM = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_ANNEAU1)
			{
				if(isFirstAN)
				{
					isFirstAN = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_ANNEAU2)
			{
				if(isFirstANb)
				{
					isFirstANb = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_ARME)
			{
				if(isFirstAR)
				{
					isFirstAR = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_BOTTES)
			{
				if(isFirstBO)
				{
					isFirstBO = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_BOUCLIER)
			{
				if(isFirstBOb)
				{
					isFirstBOb = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_CAPE)
			{
				if(isFirstCA)
				{
					isFirstCA = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_CEINTURE)
			{
				if(isFirstCE)
				{
					isFirstCE = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_COIFFE)
			{
				if(isFirstCO)
				{
					isFirstCO = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_DOFUS1)
			{
				if(isFirstDa)
				{
					isFirstDa = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_DOFUS2)
			{
				if(isFirstDb)
				{
					isFirstDb = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_DOFUS3)
			{
				if(isFirstDc)
				{
					isFirstDc = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_DOFUS4)
			{
				if(isFirstDd)
				{
					isFirstDd = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_DOFUS5)
			{
				if(isFirstDe)
				{
					isFirstDe = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_DOFUS6)
			{
				if(isFirstDf)
				{
					isFirstDf = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
			else if(obj.getPosition() == Constantes.ITEM_POS_FAMILIER)
			{
				if(isFirstFA)
				{
					isFirstFA = false;
				}else
				{
					obj.setPosition(Constantes.ITEM_POS_NO_EQUIPED);
				}
				continue;
			}
		}
		
	}
	
	public traque get_traque()
	{
		return _traqued;
	}
	
	public void set_traque(traque traq)
	{
		_traqued = traq;
	}
	
	//Mariage
	
	public void MarryTo(Jugador wife)
	{
		_wife = wife.get_GUID();
		GestorSQL.guardar_personaje(this,true);
	}
	
	public String get_wife_friendlist()
	{
		Jugador wife = Mundo.getPersonnage(_wife);
		StringBuilder str = new StringBuilder();
		if(wife != null)
		{
			str.append(wife.get_name()).append("|").append(wife.get_classe()+wife.get_sexe()).append("|").append(wife.get_color1()).append("|").append(wife.get_color2()).append("|").append(wife.get_color3()).append("|");
			if(!wife.isOnline()){
				str.append("|");
			}else{
			str.append(wife.parse_towife()).append("|");
			}
		}else{
			str.append("|");
		}
		return str.toString();
	}
	
	public String parse_towife()
	{
		int f = 0;
		if(_fight != null)
		{
			f = 1;
		}
		return _curCarte.get_id() + "|" + _lvl + "|" + f;
	}
	
	public void meetWife(Jugador p)// Se teleporter selon les sacro-saintes autorisations du mariage.
	{
		if(p == null)return; // Ne devrait theoriquement jamais se produire.
		
		int dist = (_curCarte.getX() - p.get_curCarte().getX())*(_curCarte.getX() - p.get_curCarte().getX())
					+ (_curCarte.getY() - p.get_curCarte().getY())*(_curCarte.getY() - p.get_curCarte().getY());
		if(dist > 100)// La distance est trop grande...
		{
			if(p.get_sexe() == 0)
			{
				GestorSalida.GAME_SEND_Im_PACKET(this, "178");
			}else
			{
				GestorSalida.GAME_SEND_Im_PACKET(this, "179");
			}
			return;
		}
		
		int cellPositiontoadd = Constantes.getNearCellidUnused(p);
		if(cellPositiontoadd == -1)
		{
			if(p.get_sexe() == 0)
			{
				GestorSalida.GAME_SEND_Im_PACKET(this, "141");
			}else
			{
				GestorSalida.GAME_SEND_Im_PACKET(this, "142");
			}
			return;
		}
		
		teleport(p.get_curCarte().get_id(), (p.get_curCell().getID()+cellPositiontoadd));
	}
	
	public void Divorce()
	{
		if(isOnline())
			GestorSalida.GAME_SEND_Im_PACKET(this, "047;"+ Mundo.getPersonnage(_wife).get_name());
		
		_wife = 0;
		GestorSQL.guardar_personaje(this, true);
	}
	
	public int getWife()
	{
		return _wife;
	}
	
	public int setisOK(int ok)
	{
		return _isOK = ok;
	}
	
	public int getisOK()
	{
		return _isOK;
	}
	
	public void changeOrientation(int toOrientation)
	{
		if(this.get_orientation() == 0 
				|| this.get_orientation() == 2 
				|| this.get_orientation() == 4 
				|| this.get_orientation() == 6)
		{
			this.set_orientation(toOrientation);
			GestorSalida.GAME_SEND_eD_PACKET_TO_MAP(get_curCarte(), this.get_GUID(), toOrientation);
		}
	}
	/*
	public void set_FuneralStone()
	{
		// Ce transformer en tombe TODO
		set_gfxID(Integer.parseInt(get_classe()+"3"));
	}
	*/
	public void set_Ghosts()
	{
		if(isOnMount()) toogleOnMount();
		_isGhosts = true;
		set_gfxID(8004);
		set_canAggro(false);
		set_away(true);
		set_Speed(-40);
		teleport((short)20020, 397);
		//Le teleporter aux zone de mort la plus proche
		/*for(Carte map : ) FIXME
		{
			map.
		}*/
	}
	
	public void set_Alive()
	{
		if(!_isGhosts) return;
		_isGhosts = false;
		set_energy(1000);
		set_gfxID(Integer.parseInt(get_classe()+""+get_sexe()));
		set_canAggro(true);
		set_away(false);
		set_Speed(0);
		GestorSalida.GAME_SEND_STATS_PACKET(this);
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(get_curCarte(), get_GUID());
		GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(get_curCarte(), this);
	}
   
    public void setInTrunk(Cofres t)
    {
            _curTrunk = t;
    }
   
    public Cofres getInTrunk()
    {
            return _curTrunk;
    }
    
    public void setInHouse(House h)
    {
            _curHouse = h;
    }
   
    public House getInHouse()
    {
            return _curHouse;
    }
    
	public Map<Integer, Integer> getStoreItems()
	{
		return _storeItems;
	}
	
    public int get_savestat()
    {
        return savestat;
    }

    public void set_savestat(int stat)
    {
        savestat = stat;
    }

	
    public String parseStoreItemsList() 
    {
    	StringBuilder list = new StringBuilder();
        if(_storeItems.isEmpty())return "";
        for(Entry<Integer,Integer> obj : _storeItems.entrySet()) 
        {
        	Objeto O = Mundo.getObjet(obj.getKey());
        	if(O == null) continue;
        	list.append(O.getGuid()).append(";").append(O.getQuantity()).append(";").append(O.getTemplate().getID()).append(";").append(O.parseStatsString()).append(";").append(obj.getValue()).append("|");
        }
        return (list.length()>0?list.toString().substring(0, list.length()-1):list.toString());
    }
    
    public String parseStoreItemstoBD()
    {
    	StringBuilder str = new StringBuilder();
		for(Entry<Integer, Integer> _storeObjets : _storeItems.entrySet())
		{
			str.append(_storeObjets.getKey()).append(",").append(_storeObjets.getValue()).append("|");
		}
		return str.toString();
    }
    
    public void addinStore(int ObjID, int price, int qua)
    {
		Objeto PersoObj = Mundo.getObjet(ObjID);
		//Si le joueur n'a pas l'item dans son sac ...
		if(_storeItems.get(ObjID) != null)
		{
			_storeItems.remove(ObjID);
			_storeItems.put(ObjID, price);
			GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
			return;
		}
		if(_items.get(ObjID) == null)
		{
			JuegoServidor.addToLog("Le joueur "+_name+" a tenter d'ajouter un objet au store qu'il n'avait pas.");
			return;
		}
		//Si c'est un item équipé ...
		if(PersoObj.getPosition() != Constantes.ITEM_POS_NO_EQUIPED)return;
		
		Objeto SimilarObj = getSimilarStoreItem(PersoObj);
		int newQua = PersoObj.getQuantity() - qua;
		if(SimilarObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On met l'objet du sac dans le store, avec la meme quantité
				_storeItems.put(PersoObj.getGuid(), price);
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getGuid());
                GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
			}
			else//S'il reste des objets au joueur
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				//On ajoute l'objet a la banque et au monde
				SimilarObj = Objeto.getCloneObjet(PersoObj, qua);
				Mundo.addObjet(SimilarObj, true);
				_storeItems.put(SimilarObj.getGuid(), price);
				
				//Envoie des packets
				GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On enleve l'objet du monde
				Mundo.removeItem(PersoObj.getGuid());
				//On ajoute la quantité a l'objet en banque
				SimilarObj.setQuantity(SimilarObj.getQuantity() + PersoObj.getQuantity());
				_storeItems.remove(SimilarObj.getGuid());
				_storeItems.put(SimilarObj.getGuid(), price);
				//on envoie l'ajout a la banque de l'objet
				GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				//on envoie la supression de l'objet du sac au joueur
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getGuid());
			}else //S'il restait des objets
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				SimilarObj.setQuantity(SimilarObj.getQuantity() + qua);
				_storeItems.remove(SimilarObj.getGuid());
				_storeItems.put(SimilarObj.getGuid(), price);
				GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}
		GestorSalida.GAME_SEND_Ow_PACKET(this);
		GestorSQL.guardar_personaje(this, true);
    }

	private Objeto getSimilarStoreItem(Objeto obj)
	{
		for(Entry<Integer, Integer> value : _storeItems.entrySet())
		{
			Objeto obj2 = Mundo.getObjet(value.getKey());
			if(obj2.getTemplate().getType() == 85)
				continue;
			if(obj2.getTemplate().getID() == obj.getTemplate().getID() && obj2.getStats().isSameStats(obj.getStats()))
				return obj2;
		}
		return null;
	}
	
	public void removeFromStore(int guid, int qua)
	{
		Objeto SimilarObj = Mundo.getObjet(guid);
		//Si le joueur n'a pas l'item dans son store ...
		if(_storeItems.get(guid) == null)
		{
			JuegoServidor.addToLog("Le joueur "+_name+" a tenter de retirer un objet du store qu'il n'avait pas.");
			return;
		}
		
		Objeto PersoObj = getSimilarItem(SimilarObj);
		
		int newQua = SimilarObj.getQuantity() - qua;
		
		if(PersoObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien en store
			if(newQua <= 0)
			{
				//On retire l'item du store
				_storeItems.remove(guid);
				//On l'ajoute au joueur
				_items.put(guid, SimilarObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(this,SimilarObj);
				GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				
			}
		}
		else
		{
			//S'il ne reste rien en store
			if(newQua <= 0)
			{
				//On retire l'item de la banque
				_storeItems.remove(SimilarObj.getGuid());
				Mundo.removeItem(SimilarObj.getGuid());
				//On Modifie la quantité de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + SimilarObj.getQuantity());
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				GestorSalida.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				
			}
		}
		GestorSalida.GAME_SEND_Ow_PACKET(this);
		GestorSQL.guardar_personaje(this, true);
	}
	
	public void removeStoreItem(int guid)
	{
		_storeItems.remove(guid);
	}
	
	public void addStoreItem(int guid, int price)
	{
		_storeItems.put(guid, price);
	}

	public void set_Speed(int _Speed) {
		this._Speed = _Speed;
	}

	public int get_Speed() {
		return _Speed;
	}

}
