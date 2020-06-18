package objetos;

import juego.JuegoServidor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import objetos.Personaje.Stats;
import comunes.MainServidor;
import comunes.Constantes;
import comunes.Formulas;
import comunes.GestorSQL;
import comunes.Mundo;

public class Objeto {

	public static class ObjTemplate {
		private int ID;
		private String StrTemplate;
		private String name;
		private	int type;
		private int level;
		private int pod;
		private int prix;
		private int panopID;
		private String conditions;
		private int PACost,POmin,POmax,TauxCC,TauxEC,BonusCC;
		private boolean isTwoHanded;
		private ArrayList<Accion> onUseActions = new ArrayList<>();
		private long sold;
		private int avgPrice;
		
		public ObjTemplate(int id, String strTemplate, String name, int type,int level, int pod, int prix, int panopID, String conditions,String armesInfos, int sold, int avgPrice)
		{
			this.ID = id;
			this.StrTemplate = strTemplate;
			this.name = name;
			this.type = type;
			this.level = level;
			this.pod = pod;
			this.prix = prix;
			this.panopID = panopID;
			this.conditions = conditions;
			this.PACost = -1;
			this.POmin = 1;
			this.POmax = 1;
			this.TauxCC = 100;
			this.TauxEC = 2;
			this.BonusCC = 0;
			this.sold = sold;
			this.avgPrice = avgPrice;
			
			try
			{
				String[] infos = armesInfos.split(";");
				PACost = Integer.parseInt(infos[0]);
				POmin = Integer.parseInt(infos[1]);
				POmax = Integer.parseInt(infos[2]);
				TauxCC = Integer.parseInt(infos[3]);
				TauxEC = Integer.parseInt(infos[4]);
				BonusCC = Integer.parseInt(infos[5]);
				isTwoHanded = infos[6].equals("1");
			}catch(Exception ignored){};
	
		}
		
		public int get_obviType() {
			try {
				for (String sts : StrTemplate.split(",")) {
					String[] stats = sts.split("#");
					int statID = Integer.parseInt(stats[0], 16);
					if (statID == 973) {
						return Integer.parseInt(stats[3], 16);
					}
				}
			} catch (Exception e) {
				JuegoServidor.addToLog(e.getMessage());
				return Constantes.ITEM_TYPE_OBJET_VIVANT;
			}
			return Constantes.ITEM_TYPE_OBJET_VIVANT; //Si erreur on retourne le type de base
		}

		public void addAction(Accion A)
		{
			onUseActions.add(A);
		}
		
		public boolean isTwoHanded()
		{
			return isTwoHanded;
		}
		
		public int getBonusCC()
		{
			return BonusCC;
		}
		
		public int getPOmin() {
			return POmin;
		}
		
		public int getPOmax() {
			return POmax;
		}

		public int getTauxCC() {
			return TauxCC;
		}

		public int getTauxEC() {
			return TauxEC;
		}

		public int getPACost()
		{
			return PACost;
		}
		public int getID() {
			return ID;
		}

		public String getStrTemplate() {
			return StrTemplate;
		}

		public String getName() {
			return name;
		}

		public int getType() {
			return type;
		}

		public int getLevel() {
			return level;
		}

		public int getPod() {
			return pod;
		}

		public int getPrix() {
			return prix;
		}

		public int getPanopID() {
			return panopID;
		}

		public String getConditions() {
			return conditions;
		}
		
		public Objeto createNewItem(int qua, boolean useMax) {
			Objeto item = new Objeto(Mundo.getNewItemGuid(), ID, qua, Constantes.ITEM_POS_NO_EQUIPED, generateNewStatsFromTemplate(StrTemplate,useMax), getEffectTemplate(StrTemplate));
			return item;
		}

		private Stats generateNewStatsFromTemplate(String statsTemplate,boolean useMax) {
			Stats itemStats = new Stats(false, null);
			//Si stats Vides
			if(statsTemplate.equals("") || statsTemplate == null) return itemStats;
			
			String[] splitted = statsTemplate.split(",");
			for(String s : splitted)
			{	
				String[] stats = s.split("#");
				int statID = Integer.parseInt(stats[0],16);
				boolean follow = true;
				
				for(int a : Constantes.ARMES_EFFECT_IDS)//Si c'est un Effet Actif
					if(a == statID)
						follow = false;
				if(!follow)continue;//Si c'était un effet Actif d'arme
				
				String jet = "";
				int value  = 1;
				try
				{
					jet = stats[4];
					value = Formulas.getRandomJet(jet);
					if(useMax)
					{
						try
						{
							//on prend le jet max
							int min = Integer.parseInt(stats[1],16);
							int max = Integer.parseInt(stats[2],16);
							value = min;
							if(max != 0)value = max;
						}catch(Exception e){value = Formulas.getRandomJet(jet);};			
					}
				}catch(Exception e){};
				itemStats.addOneStat(statID, value);
			}
			return itemStats;
		}
		
		private ArrayList<EfectoHechizo> getEffectTemplate(String statsTemplate)
		{
			ArrayList<EfectoHechizo> Effets = new ArrayList<>();
			if(statsTemplate.equals("") || statsTemplate == null) return Effets;
			
			String[] splitted = statsTemplate.split(",");
			for(String s : splitted)
			{	
				String[] stats = s.split("#");
				int statID = Integer.parseInt(stats[0],16);
				for(int a : Constantes.ARMES_EFFECT_IDS)
				{
					if(a == statID)
					{
						int id = statID;
						String min = stats[1];
						String max = stats[2];
						String jet = stats[4];
						String args = min+";"+max+";-1;-1;0;"+jet;
						Effets.add(new EfectoHechizo(id, args,0,-1));
					}
				}
			}
			return Effets;
		}
		
		public String parseItemTemplateStats()
		{
			return (this.ID+";"+StrTemplate);
		}

		public void applyAction(Personaje perso, Personaje target, int objID, short cellid)
		{
			for(Accion a : onUseActions)a.apply(perso, target, objID, cellid);
		}
		
		public int getAvgPrice()
		{
			return avgPrice;
		}
		
		public long getSold()
		{
			return this.sold;
		}
		
		public synchronized void newSold(int amount, int price)
		{
			long oldSold = sold;
			sold += amount;
			avgPrice = (int)((avgPrice * oldSold + price) / sold);
		}
	}

	protected ObjTemplate template;
	protected int quantity = 1;
	protected int position = Constantes.ITEM_POS_NO_EQUIPED;
	protected int guid;
	protected int obvijevan;
	protected int obvijevanLook;
	private Personaje.Stats Stats = new Stats();
	protected int dueño;
	private ArrayList<EfectoHechizo> Effects = new ArrayList<>();
	private Map<Integer,String> txtStats = new TreeMap<>();
	//Speaking Item
	//private boolean isExchangeable = true;
	protected boolean isSpeaking = false;
	protected boolean isPet = false;
	//private Speaking linkedItem = null;
	//private int linkedItem_id = -1;
	//private Speaking linkedItem = null;
	//private boolean isLinked = false;

	public Objeto(int Guid, int template, int qua, int pos, String strStats) {
		this.guid = Guid;
		this.template = Mundo.getObjTemplate(template);
		this.quantity = qua;
		this.position = pos;
		Stats = new Stats();
		parseStringToStats(strStats);
	}

	public Objeto() { }
	
	public int getObvijevanPos() {
		return obvijevan;
	}
	
	public void setObvijevanPos(int pos) {
		obvijevan = pos;
		
	}
	public int getObvijevanLook() {
		return obvijevanLook;
	}
	
	public void setObvijevanLook(int look) {
		obvijevanLook = look;
	}

	  
	
	public void parseStringToStats(String strStats)
	{
		String[] split = strStats.split(",");
		for(String s : split)
		{	
			try
			{
				String[] stats = s.split("#");
				int statID = Integer.parseInt(stats[0],16);
				
				//Stats spécials
				if(statID == 997 || statID == 996)
				{
					txtStats.put(statID, stats[4]);
					continue;
				}
				//Si stats avec Texte (Signature, apartenance, etc)
				if((!stats[3].equals("") && !stats[3].equals("0")))
				{
					txtStats.put(statID, stats[3]);
					continue;
				}
				
				String jet = stats[4];
				boolean follow = true;
				for(int a : Constantes.ARMES_EFFECT_IDS)
				{
					if(a == statID)
					{
						int id = statID;
						String min = stats[1];
						String max = stats[2];
						String args = min+";"+max+";-1;-1;0;"+jet;
						Effects.add(new EfectoHechizo(id, args,0,-1));
						follow = false;
					}
				}
				if(!follow)continue;//Si c'était un effet Actif d'arme ou une signature
				int value = Integer.parseInt(stats[1],16);
				Stats.addOneStat(statID, value);
			}catch(Exception e){continue;};
		}
	}

	public void addTxtStat(int i,String s)
	{
		txtStats.put(i, s);
	}
	
	public String getTraquedName()
	{
		for(Entry<Integer,String> entry : txtStats.entrySet())
		{
			if(Integer.toHexString(entry.getKey()).compareTo("3dd") == 0)
			{
				
				return entry.getValue();	
			}
		}
		return null;
	}
	
	public Objeto(int Guid, int template, int qua, int pos, Stats stats, ArrayList<EfectoHechizo> effects) {
		this.guid = Guid;
		this.template = Mundo.getObjTemplate(template);
		this.quantity = qua;
		this.position = pos;
		this.Stats = stats;
		this.Effects = effects;
		this.obvijevan = 0;
	    this.obvijevanLook = 0;
	}
	
	public Personaje.Stats getStats() {
		return Stats;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public ObjTemplate getTemplate() {
		return template;
	}

	public int getGuid() {
		return guid;
	}
	
	public String parseItem()
	{
		StringBuilder str = new StringBuilder();
		String posi = position== Constantes.ITEM_POS_NO_EQUIPED?"":Integer.toHexString(position);
		str.append(Integer.toHexString(guid)).append("~").append(Integer.toHexString(template.getID())).append("~").append(Integer.toHexString(quantity)).append("~").append(posi).append("~").append(parseStatsString()).append(";");
		return str.toString();
	}

	public String parseStatsString()
	{
		if(getTemplate().getType() == 83)	//Si c'est une pierre d'âme vide
			return getTemplate().getStrTemplate();
		
		StringBuilder stats = new StringBuilder();
		boolean isFirst = true;
		for(EfectoHechizo SE : Effects)
		{
			if(!isFirst)
				stats.append(",");
			
			String[] infos = SE.getArgs().split(";");
			try
			{
				stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
			}catch(Exception e)
			{
				e.printStackTrace();
				continue;
			};
			
			isFirst = false;
		}
		
		for(Entry<Integer,Integer> entry : Stats.getMap().entrySet())
		{
			if(!isFirst)
				stats.append(",");
			int statID = ((Integer)entry.getKey()).intValue();

			if ((statID == 970) || (statID == 971) || (statID == 972) || (statID == 973) || (statID == 974))
			{
				int jet = ((Integer)entry.getValue()).intValue();
				if ((statID == 974) || (statID == 972) || (statID == 970))
					stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(jet));
				else {
					stats.append(Integer.toHexString(statID)).append("#0#0#").append(jet);
				}
				if (statID == 973) setObvijevanPos(jet);
				if (statID == 972) setObvijevanLook(jet); 
			}
			else {
				String jet = "0d0+" + entry.getValue();
				stats.append(Integer.toHexString(statID)).append("#");
				stats.append(Integer.toHexString(((Integer)entry.getValue()).intValue())).append("#0#0#").append(jet);
			}
			//String jet = "0d0+"+entry.getValue();
			//stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue()));
			//stats.append("#0#0#").append(jet);
			isFirst = false;
		}
		
		for(Entry<Integer,String> entry : txtStats.entrySet())
		{
			if(!isFirst)
				stats.append(",");
			
			if(entry.getKey() == Constantes.CAPTURE_MONSTRE)
			{
				stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());	
			}
			else
			{
				stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
			}
			isFirst = false;
		}
		return stats.toString();
	}
	
	public String parseStatsStringSansUserObvi()
	{
		if(getTemplate().getType() == 83)	//Si c'est une pierre d'âme vide
			return getTemplate().getStrTemplate();
		
		StringBuilder stats = new StringBuilder();
		boolean isFirst = true;
		for(EfectoHechizo SE : Effects)
		{
			if(!isFirst)
				stats.append(",");
			
			String[] infos = SE.getArgs().split(";");
			try
			{
				stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
			}catch(Exception e)
			{
				e.printStackTrace();
				continue;
			};
			
			isFirst = false;
		}
		
		for(Entry<Integer,Integer> entry : Stats.getMap().entrySet())
		{
			if(!isFirst)
				stats.append(",");
			String jet = "0d0+"+entry.getValue();
			stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue()));
			stats.append("#0#0#").append(jet);
			isFirst = false;
		}
		
		for(Entry<Integer,String> entry : txtStats.entrySet())
		{
			if(!isFirst)
				stats.append(",");
			
			if(entry.getKey() == Constantes.CAPTURE_MONSTRE)
			{
				stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());	
			}
			else
			{
				stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
			}
			isFirst = false;
		}
		return stats.toString();
	}
	
	public String parseToSave()
	{
	    	return parseStatsStringSansUserObvi();
	  	}
	
	public String obvijevanOCO_Packet(int pos)
	{
		String strPos = String.valueOf(pos);
		if (pos == -1) strPos = "";
		String upPacket = "OCO";
		upPacket = upPacket + Integer.toHexString(getGuid()) + "~";
		upPacket = upPacket + Integer.toHexString(getTemplate().getID()) + "~";
		upPacket = upPacket + Integer.toHexString(getQuantity()) + "~";
		upPacket = upPacket + strPos + "~";
		upPacket = upPacket + parseStatsString();
		return upPacket;
	}
	
	public void obvijevanNourir(Objeto obj) {
		if (obj == null)
			return;
		for (Map.Entry<Integer, Integer> entry : Stats.getMap().entrySet())
		{
			if (entry.getKey().intValue() != 974) // on ne boost que la stat de l'expérience de l'obvi
				continue;
			if (entry.getValue().intValue() > 500) // si le boost a une valeur supérieure à 500 (irréaliste)
				return;
			entry.setValue(Integer.valueOf(entry.getValue().intValue() + obj.getTemplate().getLevel() / 32)); // valeur d'origine + ObjLvl / 32
			// s'il mange un obvi, on récupère son expérience
			/*if (obj.getTemplate().getID() == getTemplate().getID()) {
				for(Map.Entry<Integer, Integer> ent : obj.getStats().getMap().entrySet()) {
					if (entry.getKey().intValue() != 974) // on ne considère que la stat de l'expérience de l'obvi
						continue; 
					entry.setValue(Integer.valueOf(entry.getValue().intValue() + Integer.valueOf(ent.getValue().intValue())));
				}
			}*/
		}
	}
	
	public void obvijevanChangeStat(int statID, int val)
	{
		for (Map.Entry<Integer, Integer> entry : Stats.getMap().entrySet())
		{
			if (((Integer)entry.getKey()).intValue() != statID) continue; entry.setValue(Integer.valueOf(val));
		}
	}
	
	public void removeAllObvijevanStats() {
		setObvijevanPos(0);
		Personaje.Stats StatsSansObvi = new Personaje.Stats();
		for (Map.Entry<Integer, Integer> entry : Stats.getMap().entrySet())
		{
			int statID = ((Integer)entry.getKey()).intValue();
			if ((statID == 970) || (statID == 971) || (statID == 972) || (statID == 973) || (statID == 974))
				continue;
			StatsSansObvi.addOneStat(statID, ((Integer)entry.getValue()).intValue());
		}
		Stats = StatsSansObvi;
	}
	
	public void removeAll_ExepteObvijevanStats()
	{
		setObvijevanPos(0);
		Personaje.Stats StatsSansObvi = new Personaje.Stats();
		for (Map.Entry<Integer, Integer> entry : Stats.getMap().entrySet())
		{
			int statID = ((Integer)entry.getKey()).intValue();
			if ((statID != 971) && (statID != 972) && (statID != 973) && (statID != 974))
				continue;
			StatsSansObvi.addOneStat(statID, ((Integer)entry.getValue()).intValue());
		}
		Stats = StatsSansObvi;
	}
	
	public String getObvijevanStatsOnly()
	{
		Objeto obj = getCloneObjet(this, 1);
		obj.removeAll_ExepteObvijevanStats();
		return obj.parseStatsStringSansUserObvi();
	}
	
	/*public String parseToSave()
	{
		return parseStatsString();
	}*/
	
	/* *********FM SYSTEM********* */
	public String parseFMStatsString(String statsstr, Objeto obj, int add, boolean negatif)
	{
		StringBuilder stats = new StringBuilder();
		boolean isFirst = true;
		for(EfectoHechizo SE : obj.Effects)
		{
			if(!isFirst)
				stats.append(",");
			
			String[] infos = SE.getArgs().split(";");
			try
			{
				stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
			}catch(Exception e)
			{
				e.printStackTrace();
				continue;
			};
			
			isFirst = false;
		}
		
		for(Entry<Integer,Integer> entry : obj.Stats.getMap().entrySet())
		{
			if(!isFirst)stats.append(",");
			if(Integer.toHexString(entry.getKey()).compareTo(statsstr) == 0)
			{
				int newstats = 0;
				if(negatif)
				{
					newstats = entry.getValue()-add;
					if(newstats < 1) continue;
				}else
				{
					newstats = entry.getValue()+add;
				}
				String jet = "0d0+"+newstats;
				stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append(add).append("#0#0#").append(jet);
			}
			else
			{
				String jet = "0d0+"+entry.getValue();
				stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0#").append(jet);
			}
			isFirst = false;
		}
		
		for(Entry<Integer,String> entry : obj.txtStats.entrySet())
		{
			if(!isFirst)stats.append(",");
			stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
			isFirst = false;
		}
		
		return stats.toString();
	}
	
	public String parseFMEchecStatsString(Objeto obj, double poid)
	{
		StringBuilder stats = new StringBuilder();
		boolean isFirst = true;
		for(EfectoHechizo SE : obj.Effects)
		{
			if(!isFirst)
				stats.append(",");
			
			String[] infos = SE.getArgs().split(";");
			try
			{
				stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
			}catch(Exception e)
			{
				e.printStackTrace();
				continue;
			};
			
			isFirst = false;
		}
		
		for(Entry<Integer,Integer> entry : obj.Stats.getMap().entrySet())
		{
				//En cas d'echec les stats négatives Chance,Agi,Intel,Force,Portee,Vita augmentes
				int newstats = 0;
				
				if(entry.getKey() == 152 ||
				   entry.getKey() == 154 ||
				   entry.getKey() == 155 ||
				   entry.getKey() == 157 ||
				   entry.getKey() == 116 ||
				   entry.getKey() == 153)
				{
					float a = (float)((entry.getValue()*poid)/100);
					if(a < 1) a = 1;
					float chute = (float)(entry.getValue()+a);
					newstats = (int)Math.floor(chute);
					//On limite la chute du négatif a sont maximum
					if(newstats > Oficio.getBaseMaxJet(obj.getTemplate().getID(), Integer.toHexString(entry.getKey())))
					{
						newstats = Oficio.getBaseMaxJet(obj.getTemplate().getID(), Integer.toHexString(entry.getKey()));
					}
				}else
				{
				if(entry.getKey() == 127 || entry.getKey() == 101) continue;//PM, pas de négatif ainsi que PA
				
					float chute = (float)(entry.getValue()-((entry.getValue()*poid)/100));
					newstats = (int)Math.floor(chute);
				}
				if(newstats < 1) continue;
				String jet = "0d0+"+newstats;
				if(!isFirst)stats.append(",");
				stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(newstats)).append("#0#0#").append(jet);
				isFirst = false;
		}
		
		for(Entry<Integer,String> entry : obj.txtStats.entrySet())
		{
			if(!isFirst)stats.append(",");
			stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
			isFirst = false;
		}
		return stats.toString();
	}
	
	public Stats generateNewStatsFromTemplate(String statsTemplate,boolean useMax)
	{
		Stats itemStats = new Stats(false, null);
		//Si stats Vides
		if(statsTemplate.equals("") || statsTemplate == null) return itemStats;

		String[] splitted = statsTemplate.split(",");
		for(String s : splitted)
		{	
			String[] stats = s.split("#");
			int statID = Integer.parseInt(stats[0],16);
			boolean follow = true;
			
			for(int a : Constantes.ARMES_EFFECT_IDS)//Si c'est un Effet Actif
				if(a == statID)
					follow = false;
			if(!follow)continue;//Si c'était un effet Actif d'arme
			
			String jet = "";
			int value  = 1;
			try
			{
				jet = stats[4];
				value = Formulas.getRandomJet(jet);
				if(useMax)
				{
					try
					{
						//on prend le jet max
						int min = Integer.parseInt(stats[1],16);
						int max = Integer.parseInt(stats[2],16);
						value = min;
						if(max != 0)value = max;
					}catch(Exception e){value = Formulas.getRandomJet(jet);};			
				}
			}catch(Exception e){};
			itemStats.addOneStat(statID, value);
		}
		return itemStats;
	}
	
	public void setStats (Stats SS)
	{
		Stats = SS;
	}
	
	public static int getPoidOfActualItem(String statsTemplate)//Donne le poid de l'item actuel
	{
		int poid = 0;
		int somme = 0;
		String[] splitted = statsTemplate.split(",");
		for(String s : splitted)
		{
			String[] stats = s.split("#");
			int statID = Integer.parseInt(stats[0],16);
			boolean follow = true;
			
			for(int a : Constantes.ARMES_EFFECT_IDS)//Si c'est un Effet Actif
				if(a == statID)
					follow = false;
			if(!follow)continue;//Si c'était un effet Actif d'arme
			
			String jet = "";
			int value  = 1;
			try
			{
				jet = stats[4];
				value = Formulas.getRandomJet(jet);
					try
					{
						//on prend le jet max
						int min = Integer.parseInt(stats[1],16);
						int max = Integer.parseInt(stats[2],16);
						value = min;
						if(max != 0)value = max;
					}catch(Exception e){value = Formulas.getRandomJet(jet);};			
			}catch(Exception e){};
			
			int multi = 1;
			if(statID == 118 || statID == 126 || statID == 125 || statID == 119 || statID == 123 || statID == 158 || statID == 174)//Force,Intel,Vita,Agi,Chance,Pod,Initiative
			{
				multi = 1;
			}
			else if(statID == 138 || statID == 666 || statID == 226 || statID == 220)//Domages %,Domage renvoyé,Piège %
			{
				multi = 2;
			}	
			else if(statID == 124 || statID == 176)//Sagesse,Prospec
			{
				multi = 3;
			}
			else if(statID == 240 || statID == 241 || statID == 242 || statID == 243 || statID == 244)//Ré Feu, Air, Eau, Terre, Neutre
			{
				multi = 4;
			}
			else if(statID == 210 || statID == 211 || statID == 212 || statID == 213 || statID == 214)//Ré % Feu, Air, Eau, Terre, Neutre
			{
				multi = 5;
			}
			else if(statID == 225)//Piège
			{
				multi = 15;
			}
			else if(statID == 178 || statID == 112)//Soins,Dommage
			{
				multi = 20;
			}
			else if(statID == 115 || statID == 182)//Cri,Invoc
			{
				multi = 30;
			}
			else if(statID == 117)//PO
			{
				multi = 50;
			}
			else if(statID == 128)//PM
			{
				multi = 90;
			}
			else if(statID == 111)//PA
			{
				multi = 100;
			}
				poid = value*multi; //poid de la carac
				somme += poid;
		}
		return somme;
	}

	public static int getPoidOfBaseItem(int i)//Donne le poid de l'item actuel
	{
		int poid = 0;
		int somme = 0;
		String NaturalStatsItem = "";
		ResultSet RS;
		try {
			RS = GestorSQL.executeQuery("SELECT statsTemplate from `item_template` WHERE `id`='"+i+"';", MainServidor.STATIC_DB_NAME);
			RS.next();
				NaturalStatsItem = RS.getString("statsTemplate");
		} catch (SQLException e) {
			System.out.println("Erreur SQL : "+e.getMessage());
			e.printStackTrace();
		}

		if(NaturalStatsItem == null || NaturalStatsItem.isEmpty()) return 0;
		String[] splitted = NaturalStatsItem.split(",");
		for(String s : splitted)
		{
			String[] stats = s.split("#");
			int statID = Integer.parseInt(stats[0],16);
			boolean follow = true;
			
			for(int a : Constantes.ARMES_EFFECT_IDS)//Si c'est un Effet Actif
				if(a == statID)
					follow = false;
			if(!follow)continue;//Si c'était un effet Actif d'arme
			
			String jet = "";
			int value  = 1;
			try
			{
				jet = stats[4];
				value = Formulas.getRandomJet(jet);
					try
					{
						//on prend le jet max
						int min = Integer.parseInt(stats[1],16);
						int max = Integer.parseInt(stats[2],16);
						value = min;
						if(max != 0)value = max;
					}catch(Exception e){value = Formulas.getRandomJet(jet);};			
			}catch(Exception e){};
			
			int multi = 1;
			if(statID == 118 || statID == 126 || statID == 125 || statID == 119 || statID == 123 || statID == 158 || statID == 174)//Force,Intel,Vita,Agi,Chance,Pod,Initiative
			{
				multi = 1;
			}
			else if(statID == 138 || statID == 666 || statID == 226 || statID == 220)//Domages %,Domage renvoyé,Piège %
			{
				multi = 2;
			}	
			else if(statID == 124 || statID == 176)//Sagesse,Prospec
			{
				multi = 3;
			}
			else if(statID == 240 || statID == 241 || statID == 242 || statID == 243 || statID == 244)//Ré Feu, Air, Eau, Terre, Neutre
			{
				multi = 4;
			}
			else if(statID == 210 || statID == 211 || statID == 212 || statID == 213 || statID == 214)//Ré % Feu, Air, Eau, Terre, Neutre
			{
				multi = 5;
			}
			else if(statID == 225)//Piège
			{
				multi = 15;
			}
			else if(statID == 178 || statID == 112)//Soins,Dommage
			{
				multi = 20;
			}
			else if(statID == 115 || statID == 182)//Cri,Invoc
			{
				multi = 30;
			}
			else if(statID == 117)//PO
			{
				multi = 50;
			}
			else if(statID == 128)//PM
			{
				multi = 90;
			}
			else if(statID == 111)//PA
			{
				multi = 100;
			}
			poid = value*multi; //poid de la carac
			somme +=poid;
		}
		return somme;
	}
	/* *********FM SYSTEM********* */

	public ArrayList<EfectoHechizo> getEffects()
	{
		return Effects;
	}

	public ArrayList<EfectoHechizo> getCritEffects() {
		ArrayList<EfectoHechizo> effets = new ArrayList<>();
		for(EfectoHechizo SE : Effects) {
			try {
				boolean boost = true;
				for(int i : Constantes.NO_BOOST_CC_IDS)if(i == SE.getEffectID())boost = false;
				String[] infos = SE.getArgs().split(";");
				if(!boost)
				{
					effets.add(SE);
					continue;
				}
				int min = Integer.parseInt(infos[0],16)+ (boost?template.getBonusCC():0);
				int max = Integer.parseInt(infos[1],16)+ (boost?template.getBonusCC():0);
				String jet = "1d"+(max-min+1)+"+"+(min-1);
				//exCode: String newArgs = Integer.toHexString(min)+";"+Integer.toHexString(max)+";-1;-1;0;"+jet;
				//osef du minMax, vu qu'on se sert du jet pour calculer les dégats
				String newArgs = "0;0;0;-1;0;"+jet;
				effets.add(new EfectoHechizo(SE.getEffectID(),newArgs,0,-1));
			}catch(Exception e){continue;};
		}
		return effets;
	}

	public static Objeto getCloneObjet(Objeto obj, int qua) {
		Objeto ob = new Objeto(Mundo.getNewItemGuid(), obj.getTemplate().getID(), qua, Constantes.ITEM_POS_NO_EQUIPED, obj.getStats(), obj.getEffects());
		return ob;
	}

	public void clearStats() {
		//On vide l'item de tous ces effets
		Stats = new Stats();
		Effects.clear();
		txtStats.clear();
	}
	
}
