package objetos;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import objetos.Personaje.Stats;
import objetos.casas.House;
import objetos.hechizos.Hechizos.SortStats;

import org.joda.time.LocalDate;
import org.joda.time.Days;
import comunes.GestorSQL;
import comunes.Mundo;
import comunes.Constantes;

public class Gremio {
	private final int _id;
	private String _name = "";
	private String _emblem = "";
	private final Map<Integer,GuildMember> _members = new TreeMap<>();
	private int _lvl;
	private long _xp;
	
	//Percepteur
	private int _capital = 0;
	private int _nbrPerco = 0;
	private final Map<Integer, SortStats> Spells = new TreeMap<>();	//<ID, Level>
	private final Map<Integer, Integer> stats = new TreeMap<>(); //<Effet, Quantité>
	//Stats en combat
	private final Map<Integer,Integer> statsFight = new TreeMap<>();
	
	public static class GuildMember {
		private final int _guid;
		private final Gremio _guild;
		private int _rank = 0;
		private byte _pXpGive = 0;
		private long _xpGave = 0;
		private int _rights = 0;
		
		//Droit
		private final Map<Integer,Boolean> haveRight = new TreeMap<>();

		public GuildMember(int gu, Gremio g, int r, long x, byte pXp, int ri) {
			_guid = gu;
			_guild = g;
			_rank = r;
			_xpGave = x;
			_pXpGive = pXp;
			_rights = ri;
			parseIntToRight(_rights);
		}
		
		public int getGuid()
		{
			return _guid;
		}
		public int getRank()
		{
			return _rank;
		}
		
		public Gremio getGuild()
		{
			return _guild;
		}

		public String parseRights()
		{
			return Integer.toString(_rights,36);
		}

		public int getRights()
		{
			return _rights;
		}

		public long getXpGave() {
			return _xpGave;
		}

		public int getPXpGive()
		{
			return _pXpGive;
		}
		
		public int getHoursFromLastCo() {
			String[] strDate = getPerso().get_compte().getLastConnectionDate().split("~");
			LocalDate lastCo = new LocalDate(Integer.parseInt(strDate[0]),Integer.parseInt(strDate[1]),Integer.parseInt(strDate[2]));
			LocalDate now = new LocalDate();
			
			return Days.daysBetween(lastCo,now).getDays()*24;
		}

		public Personaje getPerso()
		{
			return Mundo.getPersonnage(_guid);
		}

		public boolean canDo(int rightValue) {
			if(this._rights == 1)
				return true;
			
			return haveRight.get(rightValue);
		}

		public void setRank(int i)
		{
			_rank = i;
		}
		
		public void setAllRights(int rank,byte xp,int right) {
			if(rank == -1)
				rank = this._rank;
			
			if(xp < 0)
				xp = this._pXpGive;
			if(xp > 90)
				xp = 90;
			
			if(right == -1)
				right = this._rights;
			
			this._rank = rank;
			this._pXpGive = xp;
			
			if(right != this._rights && right != 1)	//Vérifie si les droits sont pareils ou si des droits de meneur; pour ne pas faire la conversion pour rien
				parseIntToRight(right);
			this._rights = right;
			
			GestorSQL.actualizar_miembro_del_gremio(this);
		}
		
		public void giveXpToGuild(long xp) {
			this._xpGave+=xp;
			this._guild.addXp(xp);
		}
		
		public void initRight() {
			haveRight.put(Constantes.G_BOOST,false);
			haveRight.put(Constantes.G_RIGHT,false);
			haveRight.put(Constantes.G_INVITE,false);
			haveRight.put(Constantes.G_BAN,false);
			haveRight.put(Constantes.G_ALLXP,false);
			haveRight.put(Constantes.G_HISXP,false);
			haveRight.put(Constantes.G_RANK,false);
			haveRight.put(Constantes.G_POSPERCO,false);
			haveRight.put(Constantes.G_COLLPERCO,false);
			haveRight.put(Constantes.G_USEENCLOS,false);
			haveRight.put(Constantes.G_AMENCLOS,false);
			haveRight.put(Constantes.G_OTHDINDE,false);
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
			
			Integer[] mapKey = haveRight.keySet().toArray(new Integer[haveRight.size()]);	//Récupère les clef de map dans un tableau d'Integer
			
			while(total > 0) {
				for (int i = haveRight.size()-1; i < haveRight.size(); i--) {
					if(mapKey[i] <= total)
					{
						total ^= mapKey[i];
						haveRight.put(mapKey[i],true);
						break;
					}
				}
			}
		}
	}

	public Gremio(Personaje owner, String name, String emblem) {
		_id = Mundo.getNextHighestGuildID();
		_name = name;
		_emblem = emblem;
		_lvl = 1;
		_xp= 0;
		decompileSpell("462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0|"); 
		decompileStats("176;100|158;1000|124;100|"); 
	}

	public Gremio(int id, String name, String emblem, int lvl, long xp, int capital, int nbrmax, String sorts, String stats) {
		_id = id;
		_name = name;
		_emblem = emblem;
		_xp = xp;
		_lvl = lvl;
		_capital = capital;
		_nbrPerco = nbrmax;
		decompileSpell(sorts);
		decompileStats(stats);
		//Mise en place des stats
		statsFight.clear();
		statsFight.put(Constantes.STATS_ADD_FORC, _lvl);
		statsFight.put(Constantes.STATS_ADD_SAGE, get_Stats(Constantes.STATS_ADD_SAGE));
		statsFight.put(Constantes.STATS_ADD_INTE, _lvl);
		statsFight.put(Constantes.STATS_ADD_CHAN, _lvl);
		statsFight.put(Constantes.STATS_ADD_AGIL, _lvl);
		statsFight.put(Constantes.STATS_ADD_RP_NEU, (int)Math.floor(get_lvl()/2));
		statsFight.put(Constantes.STATS_ADD_RP_FEU, (int)Math.floor(get_lvl()/2));
		statsFight.put(Constantes.STATS_ADD_RP_EAU, (int)Math.floor(get_lvl()/2));
		statsFight.put(Constantes.STATS_ADD_RP_AIR, (int)Math.floor(get_lvl()/2));
		statsFight.put(Constantes.STATS_ADD_RP_TER, (int)Math.floor(get_lvl()/2));
		statsFight.put(Constantes.STATS_ADD_AFLEE, (int)Math.floor(get_lvl()/2));
		statsFight.put(Constantes.STATS_ADD_MFLEE, (int)Math.floor(get_lvl()/2));
	}

	public GuildMember addMember(int guid,int r,byte pXp,long x,int ri) {
		GuildMember GM = new GuildMember(guid,this,r,x,pXp,ri);
		_members.put(guid,GM);
		return GM;
	}

	public GuildMember addNewMember(Personaje p) {
		GuildMember GM = new GuildMember(p.get_GUID(),this,0,0,(byte) 0,0);
		_members.put(p.get_GUID(),GM);
		return GM;
	}

	public int get_id()
	{
		return _id;
	}
	
	public int get_nbrPerco()
	{
		return _nbrPerco;
	}
	public void set_nbrPerco(int nbr)
	{
		_nbrPerco = nbr;
	}
	
	public int get_Capital()
	{
		return _capital;
	}
	public void set_Capital(int nbr)
	{
		_capital = nbr;
	}
	
	public Map<Integer,SortStats> getSpells() {
		return Spells;
	}
	public Map<Integer, Integer> getStats() {
		return stats;
	}

	public void addStat(int stat, int qte) {
		int old = stats.get(stat);
		
		stats.put(stat, old + qte);
	}

	public void boostSpell(int ID) {
		SortStats SS = Spells.get(ID);
		if(SS != null && SS.getLevel() == 5)return;
		Spells.put(ID, ((SS == null)? Mundo.getSort(ID).getStatsByLevel(1): Mundo.getSort(ID).getStatsByLevel(SS.getLevel()+1)));
	}

	public Stats getStatsFight()
	{
		return new Stats(statsFight);
	}
	
	public String get_name() {
		return _name;
	}
	public String get_emblem()
	{
		return _emblem;
	}
	public long get_xp()
	{
		return _xp;
	}
	public int get_lvl()
	{
		return _lvl;
	}
	public int getSize()
	{
		return _members.size();
	}

	public String parseMembersToGM() {
		StringBuilder str = new StringBuilder();
		for(GuildMember GM : _members.values()) {
			String online = "0";
			if(GM.getPerso() != null)if(GM.getPerso().isOnline())online = "1";
			if(str.length() != 0)str.append("|");
			str.append(GM.getGuid()).append(";");
			str.append(GM.getPerso().get_name()).append(";");
			str.append(GM.getPerso().get_lvl()).append(";");
			str.append(GM.getPerso().get_gfxID()).append(";");
			str.append(GM.getRank()).append(";");
			str.append(GM.getXpGave()).append(";");
			str.append(GM.getPXpGive()).append(";");
			str.append(GM.getRights()).append(";");
			str.append(online).append(";");
			str.append(GM.getPerso().get_align()).append(";");
			str.append(GM.getHoursFromLastCo());
		}
		return str.toString();
	}

	public ArrayList<Personaje> getMembers() {
		ArrayList<Personaje> a = new ArrayList<>();
		for(GuildMember GM : _members.values())a.add(GM.getPerso());
		return a;
	}

	public GuildMember getMember(int guid)
	{
		return _members.get(guid);
	}

	public void removeMember(Personaje perso) {
		House h = House.get_HouseByPerso(perso);//On prend ça maison
		if(h != null) {
			if(House.HouseOnGuild(_id) > 0) {
				GestorSQL.casa_gremio(h, 0, 0);//On retire de la guilde
			}
		}
		_members.remove(perso.get_GUID());
		GestorSQL.eliminar_miembro_del_gremio(perso.get_GUID());
	}
	
	public void addXp(long xp) {
		_xp += xp;		
		while(_xp >= Mundo.getGuildXpMax(_lvl) && _lvl<200)
			levelUp();
	}
	
	public void levelUp() {
		_lvl++;
		_capital += 5;
	}
	
	public void decompileSpell(String spellStr){ //ID;lvl|ID;lvl|...
		int id;
		int lvl;
		
		for(String split : spellStr.split("\\|")) {
			id = Integer.parseInt(split.split(";")[0]);
			lvl = Integer.parseInt(split.split(";")[1]);
			
			Spells.put(id, Mundo.getSort(id).getStatsByLevel(lvl));
		}
	}
	
	public void decompileStats(String statsStr){ //ID;lvl|ID;lvl|...
		int id;
		int value;
		
		for(String split : statsStr.split("\\|")) { //pp pod sagesse
			id = Integer.parseInt(split.split(";")[0]);
			value = Integer.parseInt(split.split(";")[1]);
			
			stats.put(id, value);
		}
	}
	
	public String compileSpell() {
		if(Spells.isEmpty())return "";
		
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true;
		
		for(Entry<Integer, SortStats> curSpell : Spells.entrySet()) {
			if(!isFirst)
				toReturn.append("|");
			
			toReturn.append(curSpell.getKey()).append(";").append(((curSpell.getValue() == null)?0:curSpell.getValue().getLevel()));
			
			isFirst = false;
		}
		
		return toReturn.toString();
	}

	public String compileStats() {
		if(stats.isEmpty())return "";

		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true;
		
		for(Entry<Integer, Integer> curStats : stats.entrySet()) {
			if(!isFirst)
				toReturn.append("|");
			
			toReturn.append(curStats.getKey()).append(";").append(curStats.getValue());
			
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public void upgrade_Stats(int statsid, int add) {
		int actual = stats.get(statsid);
		stats.put(statsid, (actual+add));
	}
	
	public int get_Stats(int statsid) {
		int value = 0;
		for(Entry<Integer, Integer> curStats : stats.entrySet()) {
			if(curStats.getKey() == statsid)
			{
				value = curStats.getValue();
			}
		}
		return value;
	}
	
	public String parsePercotoGuild() {
		//Percomax|NbPerco|100*level|level|perco_add_pods|perco_prospection|perco_sagesse|perco_max|perco_boost|1000+10*level|perco_spells

		String packet = get_nbrPerco() + "|" +
				Recaudador.CountPercoGuild(get_id()) + "|" +
				100 * get_lvl() + "|" + get_lvl() + "|" +
				get_Stats(158) + "|" + get_Stats(176) + "|" +
				get_Stats(124) + "|" + get_nbrPerco() + "|" +
				get_Capital() + "|" + (1000 + (10 * get_lvl())) + "|" + compileSpell();
		return packet;
	}
}
