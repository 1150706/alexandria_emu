package objetos;

import java.util.ArrayList;

//import common.ConditionParser;
import comunes.Mundo;

import objetos.Objeto.ObjTemplate;

public class NPCModelo {
	private final int _id;
	private final int _bonusValue;
	private final int _gfxID;
	private final int _scaleX;
	private final int _scaleY;
	private final int _sex;
	private final int _color1;
	private final int _color2;
	private final int _color3;
	private final String _acces;
	private final int _extraClip;
	private final int _customArtWork;
	private int _initQuestionID;
	private ArrayList<ObjTemplate> _ventes = new ArrayList<>();
	private Misiones _quest;

	public static class NPC_question
	{
		private final int _id;
		private String _reponses;
		private final String _args;
		
		//private String _cond;
		//private int falseQuestion;
		
		public NPC_question(int _id, String _reponses, String _args, String _cond, int falseQuestion) {
			this._id = _id;
			this._reponses = _reponses;
			this._args = _args;
			//_cond = _cond;
			//falseQuestion = falseQuestion;
		}
		
		public int get_id()
		{
			return _id;
		}
		
		public String parseToDQPacket(Personaje perso)
		{
			boolean mariage = false;
			boolean maried = false;
			if(_id == 50030 && //Si prêtre
					(perso.getActualCelda().getID() == 282 || perso.getActualCelda().getID() == 297)) // Si un des deux à marier
				mariage = Mundo.mariageok();
			if(_id == 50030 && perso.getWife() !=0)//Si prêtre et marrié
				maried = true;
			StringBuilder str = new StringBuilder();
			str.append(_id);
			if(!_args.equals(""))
				str.append(";").append(parseArguments(_args,perso));
			str.append("|").append(_reponses);
			if(mariage)str.append(";518");
			if(maried)str.append(";2582");
			return str.toString();
		}
		
		public String getReponses()
		{
			return _reponses;
		}
		
		private String parseArguments(String args, Personaje perso)
		{
			String arg = args;
			arg = arg.replace("[name]", perso.getStringVar("name"));
			arg = arg.replace("[bankCost]", perso.getStringVar("bankCost"));
			/*TODO*/
			return arg;
		}

		public void setReponses(String reps)
		{
			_reponses = reps;
		}
	}
	
	public static class NPC
	{
		private final NPCModelo _template;
		private int _cellID;
		private final int _guid;
		private byte _orientation;
		
		public NPC (NPCModelo temp, int guid, int cell, byte o)
		{
			_template = temp;
			_guid = guid;
			_cellID = cell;
			_orientation  = o;
		}

		public NPCModelo getModelo() {
			return _template;
		}

		public int getCeldaID() {
			return _cellID;
		}

		public int get_guid() {
			return _guid;
		}

		public int get_orientation() {
			return _orientation;
		}

		public String parseGM(Personaje p)
		{
			StringBuilder sock = new StringBuilder();
			sock.append("+");
			sock.append(_cellID).append(";");
			sock.append(_orientation).append(";");
			sock.append("0").append(";");
			sock.append(_guid).append(";");
			sock.append(_template.getID()).append(";");
			sock.append("-4").append(";");//type = NPC
			
			StringBuilder taille = new StringBuilder();
			if(_template.get_scaleX() == _template.get_scaleY())
			{
				taille.append(_template.get_scaleY());
			}else
			{
				taille.append(_template.get_scaleX()).append("x").append(_template.get_scaleY());
			}
			sock.append(_template.get_gfxID()).append("^").append(taille.toString()).append(";");
			sock.append(_template.get_sex()).append(";");
			sock.append(( _template.get_color1() != -1?Integer.toHexString( _template.get_color1()):"-1")).append(";");
			sock.append(( _template.get_color2() != -1?Integer.toHexString( _template.get_color2()):"-1")).append(";");
			sock.append(( _template.get_color3() != -1?Integer.toHexString( _template.get_color3()):"-1")).append(";");
			sock.append(_template.get_acces()).append(";");
			sock.append(_template.get_extraClip(p)).append(";");
			sock.append(_template.get_customArtWork());
			return sock.toString();
		}
		
		public String parseGMperco(String GuildName)
		{
			String sock = "+";
			sock += _cellID+";";
			sock += "1;";//Orientation
			sock += "0;";
			sock += "-6;";//guid
			sock += "1f,2m;";
			sock += "-6"+";";//type = NPC
			sock += "6000^125;";
			sock += "91;";
			sock += GuildName + ";9,qjtz,q,6y7ke";
			/*sock += _template.get_acces()+";";
			sock += (_template.get_extraClip()!=-1?(_template.get_extraClip()):(""))+";";
			sock += _template.get_customArtWork();*/
			return sock;
		}

		public void setCellID(int id)
		{
			_cellID = id;
		}

		public void setOrientation(byte o)
		{
			_orientation = o;
		}
		
	}
	
	public static class NPC_reponse
	{
		private final int _id;
		private final ArrayList<Accion> _actions = new ArrayList<>();
		
		public NPC_reponse(int id)
		{
			_id = id;
		}
		
		public int get_id()
		{
			return _id;
		}
		
		public void addAction(Accion act) {
            ArrayList<Accion> c = new ArrayList<>(_actions);
			for(Accion a : c)if(a.getID() == act.getID())_actions.remove(a);
			_actions.add(act);
		}
		
		public void apply(Personaje perso) {
			for(Accion act : _actions)
			act.apply(perso, null, -1, -1);
		}
		
		public boolean isAnotherDialog() {
			for(Accion curAct : _actions) {
				if(curAct.getID() == 1) //1 = Discours NPC
					return true;
			}
			return false;
		}
	}
	
	public NPCModelo(int _id, int value, int _gfxid, int _scalex, int _scaley,
					 int _sex, int _color1, int _color2, int _color3, String _acces,
					 int clip, int artWork, int questionID, String ventes) {
		super();
		this._id = _id;
		_bonusValue = value;
		_gfxID = _gfxid;
		_scaleX = _scalex;
		_scaleY = _scaley;
		this._sex = _sex;
		this._color1 = _color1;
		this._color2 = _color2;
		this._color3 = _color3;
		this._acces = _acces;
		_extraClip = clip;
		_customArtWork = artWork;
		_initQuestionID = questionID;
		if(ventes.equals(""))return;
		for(String obj : ventes.split(",")) {
			try {
				int tempID = Integer.parseInt(obj);
				ObjTemplate temp = Mundo.getObjTemplate(tempID);
				if(temp == null)continue;
				_ventes.add(temp);
			}catch(NumberFormatException e){continue;}
		}
	}

	public int getID() {
		return _id;
	}

	public int get_bonusValue() {
		return _bonusValue;
	}

	public int get_gfxID() {
		return _gfxID;
	}

	public int get_scaleX() {
		return _scaleX;
	}

	public int get_scaleY() {
		return _scaleY;
	}

	public int get_sex() {
		return _sex;
	}

	public int get_color1() {
		return _color1;
	}

	public int get_color2() {
		return _color2;
	}

	public int get_color3() {
		return _color3;
	}

	public String get_acces() {
		return _acces;
	}

	public String get_extraClip(Personaje p) {
		System.out.println("Quest test " + _quest);
		// Si on ne trouve pas le extraClip.
		if (_extraClip == -1) { // Personnage NPC banal.
			return "";
		}
		return Integer.toString(_extraClip);
	}

	public int get_customArtWork() {
		return _customArtWork;
	}

	public int getPreguntaInicial() {
		return _initQuestionID;
	}
	
	public String getItemVendorList()
	{
		StringBuilder items = new StringBuilder();
		if(_ventes.isEmpty())return "";
		for(ObjTemplate obj : _ventes)
		{
			items.append(obj.parseItemTemplateStats()).append("|");
		}
		return items.toString();
	}

	public boolean addItemVendor(ObjTemplate T)
	{
		if(_ventes.contains(T))return false;
		_ventes.add(T);
		return true;
	}
	public boolean delItemVendor(int tID)
	{
		ArrayList<ObjTemplate> newVentes = new ArrayList<>();
		boolean remove = false;
		for(ObjTemplate T : _ventes)
		{
			if(T.getID() == tID)
			{
				remove = true;
				continue;
			}
			newVentes.add(T);
		}
		_ventes = newVentes;
		return remove;
	}

	public void setInitQuestion(int q)
	{
		_initQuestionID = q;
	}
	
	public boolean haveItem(int templateID)
	{
		for(ObjTemplate curTemp : _ventes)
		{
			if(curTemp.getID() == templateID)
				return true;
		}
		
		return false;
	}
}
