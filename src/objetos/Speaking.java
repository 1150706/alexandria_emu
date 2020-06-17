package objetos;

/*public class Speaking extends Objet {

	private int _xp = 0; //Experience
	private int _appearance = 1; //skin (=level)
	private int _state = 0; //humeur
	private String _lastEat = ""; //Date du repas
	private int _linked_id = -1;
	private Objet _linked = null;
	private boolean hasLinked = false;
	private int _lvl = 1;
	private int _type = Constants.ITEM_TYPE_OBJET_VIVANT;
	private int _winXp = 0; //Xp gagne lors des derniers repas (remis à zéro lors du reload)

	public Speaking(int Guid, int template, int qua, int pos, Stats stats, ArrayList<SpellEffect> effects) //New Item
	{
		super(Guid, template, qua, pos, stats, effects); //Item Stats

		//Speaking stats
		this._lvl = 1;
		this._xp = 0;
		_type = this.template.get_obviType();
		this._appearance = 1;

		this.set_state(0);

		this._winXp = 0;
		this._lastEat = "";
		set_unlinkedItem();

		addTxtStat(805, Formulas.newReceivedDate("#"));

		this.isSpeaking = true;
	}

	public int get_type() {
		return _type;
	}

	public Speaking(Objet obj, int skin, int lvl, int xp, int state,
			int winXp, String lastEat, int linked, int type) //Loaded Item
	{
		super(obj.getGuid(), obj.getTemplate().getID(), obj.getQuantity(), obj.getPosition(), obj.parseToSave()); //Item stats

		//Speaking stats
		this._lvl = lvl;
		this._xp = xp;
		this._type = type;
		this._appearance = skin;

		this._state = state;
		this._winXp = winXp;
		this._lastEat = lastEat;

		//this.set_linkedItemID(linked); => On link une fois tout charge
		this._linked_id = linked;

		this.isSpeaking = true;
	}

	public int get_level() {
		int i = 1;
		for (;;) {
			if (this._xp > World.getExpLevel(i).obvijevan) {
				return i;
			}
		}
	}

	public int get_mealsXp() {
		return _winXp;
	}

	public void set_mealsXp(int XP) {
		this._winXp = XP;
	}

	public void add_mealsXp(int toAdd) {
		this._winXp += toAdd;
	}

	public void setlvl(int lvl) {
		this._lvl = lvl;
	}

	public void levelUp(boolean addXp) {
		if (_lvl > 20) {
			return;
		}
		this._lvl += 1;
		if (addXp) {
			_xp = (int) World.getExpLevel(_lvl).obvijevan;
		}
	}

	public void addXp(long winxp) {
		if (winxp < 1) {
			winxp = 1;
		}
		if (_lvl >= 20) {
			eatSkinny(winxp);
			return;
		}
		this._xp += winxp;
		this._winXp += winxp;
		if (_winXp > Math.round(World.getExpLevel(_lvl + 1).obvijevan / 10)) {
			this._winXp = 0;
			if (_state == 0) //S'il etait maigrichon on ajoute, sinon on laisse Rassasie (= Nourrit normalement)
			{
				addState();
			}
		}
		while (_xp >= World.getObviXpMax(_lvl) && _lvl < 20) {
			levelUp(false);
		}
	}

	public void eatSkinny(long winxp) {
		if (winxp < 1) {
			winxp = 1;
		}
		this._winXp += winxp;
		if (_winXp > Math.round(World.getExpLevel(_lvl + 1).obvijevan / 10)) {
			this._winXp = 0;
			if (_state == 0) //S'il etait maigrichon on ajoute, sinon on laisse Rassasie (= Nourrit normalement)
			{
				addState();
			}
		}
	}

	public void setXp(int Xp) {
		this._xp = Xp;
	}

	public int get_lvl() {
		return _lvl;
	}

	public boolean has_linkedItem() {
		return hasLinked;
	}

	public static Speaking create_SpeakingItem(int Guid, int template, int qua, int pos, Stats stats, ArrayList<SpellEffect> effects) {
		return new Speaking(Guid, template, qua, pos, stats, effects);
	}

	public static Speaking load_SpeakingItem(Objet itm, int skin, int lvl,
			int xp, int meals, int winXp, String lastEat, int linked, int type) {
		return new Speaking(itm, skin, lvl, xp, meals, winXp, lastEat, linked, type);
		//int skin, int lvl, int xp, int meals,
		//int winXp, String lastEat, int linked)
	}

	public void parseStats(String Stats) {
		for (String SSats : Stats.split(",")) {
			String[] stats = SSats.split("#");
			int statID = Integer.parseInt(stats[0], 16);
			switch (statID) {
				case 808: //A mange
				{
					this._lastEat = Formulas.getDate(stats);
					int nbr = Formulas.get_missedMeals(_lastEat);
					if (nbr > 0) {
						add_mealsXp(-nbr);
					}
					break;
				}
				case 972: //Skin
				{
					this._appearance = Integer.parseInt(stats[3], 16);
					break;
				}
				case 974: //Exp
				{
					this._xp = Integer.parseInt(stats[3], 16);
					break;
				}
				case 970: //templateID
				{
					this.template = World.getObjTemplate(Integer.parseInt(stats[3], 16));
					break;
				}
			}
			//this.StaticStates.put(statID, SSats);
		}
	}

	public int get_xp() {
		return _xp;
	}

	public void add_xp(int xp) {
		this._xp += xp;
	}

	public int get_selectedLevel() {
		return _appearance;
	}

	public void set_selectedLevel(int lvl) {
		if (lvl > get_lvl()) {
			return;
		} else {
			this._appearance = lvl;
		}
	}

	public boolean eatItem(Personnage p, Objet item) {
		if (!canEat() && _state == 1) {
			addState();
			item.decreaseQuantity(p, 1);
			int XP = Formulas.getXpItem(item, 40);
			if (XP < 1) {
				XP = 1;
			}
			addXp(XP);
			this._lastEat = Formulas.lastEat_newDate("-");
			return true;
		} else if (canEat() && _state == 1) {
			item.decreaseQuantity(p, 1);
			int XP = Formulas.getXpItem(item, 10);
			if (XP < 1) {
				XP = 1;
			}
			addXp(XP);
			this._lastEat = Formulas.lastEat_newDate("-");
			return true;
		} else if (_state >= 2) {
			return false;
		} else {
			//Sinon
			item.decreaseQuantity(p, 1);
			int XP = Formulas.getXpItem(item, 10);
			if (XP < 1) {
				XP = 1;
			}
			addXp(XP);
			this._lastEat = Formulas.lastEat_newDate("-");
			return true;
		}
	}

	public boolean canEat() {
		boolean can = false;
		try {
			String Date = this.get_lastEat();
			if (Date.contains("-")) {
				if (!Formulas.CompareTime(Date, Constants.ITEM_TIME_FEED_MIN)) //S'il a attendu Assez
				{
					can = true;
				}
			} else {
				Date = Formulas.getDate(("325#" + this.getTxtStat().get(Constants.EFFECT_RECEIVED_DATE)).split("#"));
				if (!Formulas.CompareTime(Date, Constants.ITEM_TIME_FEED_MIN)) //S'il a attendu Assez
				{
					can = true;
				}
			}
		} catch (Exception e) {
			GameServer.addToLog("Erreur Speaking: " + e.getMessage());
			return false;
		}
		return can;
	}

	public void addState() {
		if (_state >= 2) {
			_state = 2;
		}
		_state++;
	}

	public void decrState() {
		if (_state <= 0) {
			_state = 0;
		}
		_state--;
	}

	public void set_state(int _state) {
		this._state = _state;
	}

	public int get_state() {
		return _state;
	}

	public void set_lastEat(String _lastEat) {
		this._lastEat = _lastEat;
	}

	public String get_lastEat() {
		return _lastEat;
	}

	public void set_hasLinked(Objet obj) {
		this._linked_id = obj.getGuid();
		this._linked = obj;
		this.hasLinked = true;
	}

	public void set_linkedItem(Objet linkedItem) {
		this._linked = linkedItem;
	}

	public void set_unlinkedItem() {
		this._linked_id = -1;
		this.hasLinked = false;
		this._linked = null;
	}

	public int get_linked_ID() {
		return this._linked_id;
	}

	public Objet get_linked() {
		return this._linked;
	}

	public String lastEat_toPacket() {
		String[] infos = this._lastEat.split("-");
		String split = "#";
		return (new StringBuilder(Integer.toHexString(Integer.parseInt(infos[0]))).append(split).append(Integer.toHexString(Integer.parseInt(infos[1] + "" + infos[2]))).append(split).append(Integer.toHexString(Integer.parseInt(infos[3] + "" + infos[4])))).toString();
	}

	public String parse_speakingStates() {
		StringBuilder states = new StringBuilder("");
		// 808: //A mange a...
		try {
			if (this._lastEat.contains("-")) {
				states.append(Integer.toHexString(Constants.EFFECT_OBVI_LAST_EAT)).append("#").append(lastEat_toPacket()).append(",");
			}
		} catch (Exception e) {
		}
		// 972: //Skin
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_SKIN)).append("#0#0#").append(Integer.toHexString(_appearance)).append(",");
		// 974: //Exp
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_XP)).append("#0#0#").append(Integer.toHexString(_xp)).append(",");
		// 970: //itemID
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_ITEMID)).append("#0#0#").append(Integer.toHexString(template.getID())).append(",");
		// 971: //State
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_STATE)).append("#0#0#").append(this.get_state()).append(",");
		// 973: //TypeID
		states.append(Integer.toHexString(Constants.EFFECT_OBVI_TYPE)).append("#0#0#").append(Integer.toHexString(_type));

		return states.toString();
	}

	public boolean isSimilar(Speaking obj) {
		return false;
	}

	public static Speaking toSpeaking(Objet item) {
		return (Speaking) item;
	}

	public static Objet toItem(Speaking item) {
		return (Objet) item;
	}
}
*/