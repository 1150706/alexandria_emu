	package objetos;
      
	import comunes.Mundo;
	import java.util.ArrayList;
	import java.util.Map;
import java.util.TreeMap;
//import objects.Quest.Step;
      
	public class Misiones
	{
		private final int _ID;
		private final int _lvl;
		private final long _xp;
		private final int _ali;
		private final int _kamas;
		private final String _name;
		//private int _lastquestrequired;
      
		public Misiones(int ID, String step, int lvl, long xp, int ali, String gainob, String gainbb, int kamas, String name, int lastquestrequired)
		{
			this._ID = ID;
			this._lvl = lvl;
			this._xp = xp;
			this._ali = ali;
			this._kamas = kamas;
			this._name = name;
			//this._lastquestrequired = lastquestrequired;
			String[] ie;
			try
			{
				ie = step.split("\\|");
				int a = 0;
				while (a != ie.length) {
					a++;
					Map<Integer, Step> _step = new TreeMap<>();
					_step.put(Integer.valueOf(a), Mundo.getStep(Integer.parseInt(ie[(a - 1)])));
				}
			} catch (Exception e) {
				ie = null;
			}
			try
			{
				ie = gainob.split("\\|");
				int a = 0;
				while (a != ie.length) {
					a++;
					ArrayList<Objeto.ObjTemplate> _gainob = new ArrayList<>();
					_gainob.add(Mundo.getObjTemplate(Integer.parseInt(ie[(a - 1)])));
				}
			} catch (Exception e) {
				ie = null;
			}
			try
			{
				ie = gainbb.split("\\|");
				int a = 0;
				while (a != ie.length) {
					a++;
					String[] avz = gainbb.split("-");
					Map<Objeto.ObjTemplate, Integer> _gain = new TreeMap<>();
					_gain.put(Mundo.getObjTemplate(Integer.parseInt(avz[0])),
							Integer.valueOf(Integer.parseInt(avz[1])));
				}
			} catch (Exception ignored) {
			}
		}
		
		public int get_id() {
			return this._ID;
		}
		
		public boolean can_do(Personaje p)
		{
			return ((this._ali == 0) || (this._ali == p.get_align())) && 
					(this._lvl <= p.get_lvl());
		}
		
		public int get_lvl()
		{
			return this._lvl;
		}
		public long get_xp() {
			return this._xp;
		}
		public int get_ali() {
			return this._ali;
		}
		public int get_kamas() {
			return this._kamas;
		}
		public String get_name() {
			return this._name;
		}
		
		public static class Step {
			private final int _id;
			private final int _type;
			private final int _objectif;
			
			public Step(int id, int type, int objectif) { 
				this._id = id;
				this._type = type;
				this._objectif = objectif;
			}
			
			public int get_id() {
				return this._id;
			}
			public int get_type() {
				return this._type;
			}
			public int get_objectif() {
				return this._objectif;
			}
		}
	}
	
/* Location:           C:\Documents and Settings\dell\Desktop\ancestra.jar
 * Qualified Name:     objects.Quest
 * JD-Core Version:    0.6.0
 */