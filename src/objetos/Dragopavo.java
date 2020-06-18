package objetos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import comunes.MainServidor;
import comunes.Constantes;
import comunes.GestorSQL;
import comunes.Mundo;

import objetos.Personaje.Stats;

public class Dragopavo {

	private final int _id;
	private final int _color;
	private int _sexe;
	private int _amour;
	private int _endurance;
	private int _level;
	private long _exp;
	private String _nom;
	private final int _fatigue;
	private final int _energie;
	private final int _reprod;
	private final int _maturite;
	private final int _serenite;
	private Stats _stats = new Stats();
	private String _ancetres = ",,,,,,,,,,,,,";
	private final ArrayList<Objeto> _items = new ArrayList<>();
	private final List<Integer> capacite = new ArrayList<>();
	private String _ability = ",";
	
	public Dragopavo(int color)
	{
		_id = Mundo.getNextIdForMount();
		_color = color;
		_level = MainServidor.CONFIG_DD_LVL_DEPART;
		_exp = 0;
		_nom = MainServidor.CONFIG_NOM_DD;
		_fatigue = 0;
		_energie = getMaxEnergie();
		_reprod = 0;
		_maturite = getMaxMatu();
		_serenite = 0;
		_stats = Constantes.getMountStats(_color,_level);
		_ancetres = ",,,,,,,,,,,,,";
		_ability = "0";
		
		Mundo.addDragodinde(this);
		GestorSQL.crear_montura(this);
	}
	
	public Dragopavo(int id, int color, int sexe, int amour, int endurance,
					 int level, long exp, String nom, int fatigue,
					 int energie, int reprod, int maturite, int serenite, String items, String anc, String ability)
	{
		_id = id;
		_color = color;
		_sexe = sexe;
		_amour = amour;
		_endurance = endurance;
		_level = level;
		_exp = exp;
		_nom = nom;
		_fatigue = fatigue;
		_energie = energie;
		_reprod = reprod;
		_maturite = maturite;
		_serenite = serenite;
		_ancetres = anc;
		_stats = Constantes.getMountStats(_color,_level);
		_ability = ability;
		for (String s : ability.split(",", 2))
			if (s != null) {
				int a = Integer.parseInt(s);
				try {
					this.capacite.add(Integer.valueOf(a));
				} catch (Exception ignored) {}
			}
		for(String str : items.split(";"))
		{
			try
			{
				Objeto obj = Mundo.getObjet(Integer.parseInt(str));
				if(obj != null)_items.add(obj);
			}catch(Exception e){continue;}
		}
	}

	public int get_id() {
		return _id;
	}

	public int get_color() {
		return _color;
	}
	
	public String get_color(String a)
	{
		String b = "";
		if (capacite.contains(Integer.valueOf(9))) 
			b = b + "," + a;
		return _color + b;
	}

	public int get_sexe() {
		return _sexe;
	}

	public int get_amour() {
		return _amour;
	}

	public String get_ancetres() {
		return _ancetres;
	}

	public int get_endurance() {
		return _endurance;
	}
	public int get_level() {
		return _level;
	}

	public long get_exp() {
		return _exp;
	}

	public String get_nom() {
		return _nom;
	}

	public int get_fatigue() {
		return _fatigue;
	}

	public int get_energie() {
		return _energie;
	}

	public int get_reprod() {
		return _reprod;
	}

	public int get_maturite() {
		return _maturite;
	}

	public int get_serenite() {
		return _serenite;
	}

	public Stats get_stats() {
		return _stats;
	}

	public ArrayList<Objeto> get_items() {
		return _items;
	}
	
	public String parse()
	{
		String str = _id + ":" +
				_color + ":" +
				_ancetres + ":" +
				",," + _ability + ":" +//FIXME capacités
				_nom + ":" +
				_sexe + ":" +
				parseXpString() + ":" +
				_level + ":" +
				"1" + ":" +//FIXME
				getTotalPod() + ":" +
				"0" + ":" +//FIXME podActuel?
				_endurance + ",10000:" +
				_maturite + "," + getMaxMatu() + ":" +
				_energie + "," + getMaxEnergie() + ":" +
				_serenite + ",-10000,10000:" +
				_amour + ",10000:" +
				"-1" + ":" +//FIXME
				"0" + ":" +//FIXME
				parseStats() + ":" +
				_fatigue + ",240:" +
				_reprod + ",20:";
		return str;
	}

	private String parseStats()
	{
		String stats = "";
		for(Entry<Integer,Integer> entry : _stats.getMap().entrySet())
		{
			if(entry.getValue() <= 0)continue;
			if(stats.length() >0)stats += ",";
			stats += Integer.toHexString(entry.getKey())+"#"+Integer.toHexString(entry.getValue())+"#0#0";
		}
		return stats;
	}

	private int getMaxEnergie()
	{
		int energie = 10000;
		return energie;
	}

	private int getMaxMatu()
	{
		int matu = 1000;
		return matu;
	}

	private int getTotalPod()
	{
		int pod = 1000;
		
		return pod;
	}

	private String parseXpString()
	{
		return _exp+","+ Mundo.getExpLevel(_level).dinde+","+ Mundo.getExpLevel(_level+1).dinde;
	}

	public boolean isMountable()
	{
		if(_energie <10
		|| _maturite < getMaxMatu()
		|| _fatigue == 240)return false;
		return true;
	}

	public String getItemsId()
	{
		String str = "";
		for(Objeto obj : _items)str += (str.length()>0?";":"")+obj.getGuid();
		return str;
	}

	public void setName(String packet)
	{
		_nom = packet;
		GestorSQL.actualizar_informacion_monturas(this);
	}
	
	public void addXp(long amount)
	{
		_exp += amount;

		while(_exp >= Mundo.getExpLevel(_level+1).dinde && _level<100)
			levelUp();
		
	}
	
	public void levelUp()
	{
		_level++;
		_stats = Constantes.getMountStats(_color,_level);
	}
	
	public boolean isCameleone() {
		return capacite.contains(Integer.valueOf(9));
	}
	
	public String get_ability() {
		return _ability;
	}
	
	public boolean addCapacity(String capacites) {
		int c = 0;
		for (String s : capacites.split(",", 2)) {
			if (capacite.size() >= 2) 
				return false; 
			try
			{
				c = Integer.parseInt(s); 
			} catch (Exception ignored) {}
			
			if (c != 0)
				capacite.add(Integer.valueOf(c));
			
			if (capacite.size() == 1)
				_ability = (capacite.get(0) + ",");
			else
				_ability = (capacite.get(0) + "," + this.capacite.get(1));
		}
		return true;
	}
}
