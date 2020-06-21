package objetos;

import juego.JuegoThread;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Timer;

import objetos.Mercadillo.HdvEntry;

import realm.RealmThread;

import comunes.*;

public class Cuenta {

	private int _GUID;
	private String _name;
	private String _pass;
	private String _pseudo;
	private String _key;
	private String _lastIP = "";
	private String _question;
	private String _reponse;
	private boolean _banned = false;
	private int _gmLvl = 0;
	private int _vip = 0;
	private String _curIP = "";
	private String _lastConnectionDate = "";
	private JuegoThread _gameThread;
	private RealmThread _realmThread;
	private Personaje _curPerso;
	private long _bankKamas = 0;
	private final Map<Integer, Objeto> _bank = new TreeMap<>();
	private final ArrayList<Integer> _friendGuids = new ArrayList<>();
	private final ArrayList<Integer> _EnemyGuids = new ArrayList<>();
	private boolean _mute = false;
	public Timer _muteTimer;
	public int _position = -1;//Position du joueur
	private final Map<Integer,ArrayList<HdvEntry>> _hdvsItems;// Contient les items des HDV format : <hdvID,<cheapestID>>
	
	private final Map<Integer, Personaje> _persos = new TreeMap<>();
	
	public Cuenta(int aGUID, String aName, String aPass, String aPseudo, String aQuestion, String aReponse, int aGmLvl, int vip, boolean aBanned, String aLastIp, String aLastConnectionDate, String bank, int bankKamas, String friends, String enemy) {
		this._GUID 		= aGUID;
		this._name 		= aName;
		this._pass		= aPass;
		this._pseudo 	= aPseudo;
		this._question	= aQuestion;
		this._reponse	= aReponse;
		this._gmLvl		= aGmLvl;
		this._vip 		= vip;
		this._banned	= aBanned;
		this._lastIP	= aLastIp;
		this._lastConnectionDate = aLastConnectionDate;
		this._bankKamas = bankKamas;
		this._hdvsItems = Mundo.getMyItems(_GUID);
		//Cargando los bancos
		for(String item : bank.split("\\|")) {
			if(item.equals(""))continue;
			String[] infos = item.split(":");
			int guid = Integer.parseInt(infos[0]);

			Objeto obj = Mundo.getObjet(guid);
			if( obj == null)continue;
			_bank.put(obj.getGuid(), obj);
		}
		//Chargement de la liste d'amie
		for(String f : friends.split(";")) {
			try {
				_friendGuids.add(Integer.parseInt(f));
			}catch(Exception ignored){}
		}
		//Chargement de la liste d'Enemy
		for(String f : enemy.split(";")) {
			try {
				_EnemyGuids.add(Integer.parseInt(f));
			}catch(Exception ignored){}
		}
	}
	
	public void setBankKamas(long i) {
		_bankKamas = i;
		GestorSQL.actualizar_datos_cuenta(this);
	}

	public boolean isMuted()
	{
		return _mute;
	}

	public void mute(boolean b, int time) {
		_mute = b;
		String msg = "";
		if(_mute)msg = "Vous avez ete mute";
		else msg = "Vous n'etes plus mute";
		GestorSalida.GAME_SEND_MESSAGE(_curPerso, msg, MainServidor.CONFIG_MOTD_COLOR);
		if(time == 0)return;
		if(_muteTimer == null && time >0) {
			_muteTimer = new Timer(time*1000, arg0 -> {
                mute(false,0);
                _muteTimer.stop();
            });
			_muteTimer.start();
		}else if(time ==0) {
			//SI 0 on désactive le Timer (Infinie)
			_muteTimer = null;
		}else {
			if (_muteTimer.isRunning()) _muteTimer.stop(); 
			_muteTimer.setInitialDelay(time*1000); 
			_muteTimer.start(); 
		}
	}
	
	public String parseBankObjetsToDB() {
		StringBuilder str = new StringBuilder();
		if(_bank.isEmpty())
			return "";
		for(Entry<Integer, Objeto> entry : _bank.entrySet()) {
			Objeto obj = entry.getValue();
			str.append(obj.getGuid()).append("|");
		}
		return str.toString();
	}
	
	public Map<Integer, Objeto> getBank() { return _bank; }

	public long getBankKamas()
	{
		return _bankKamas;
	}

	public void setGameThread(JuegoThread t)
	{
		_gameThread = t;
	}
	
	public void setActualIP(String ip) {
		_curIP = ip;
	}
	
	public String getLastConnectionDate() {
		return _lastConnectionDate;
	}
	
	public void setLastIP(String _lastip) {
		_lastIP = _lastip;
	}

	public void setLastConnectionDate(String connectionDate) {
		_lastConnectionDate = connectionDate;
	}

	public JuegoThread getJuegoThread()
	{
		return _gameThread;
	}
	
	public RealmThread getRealmThread()
	{
		return _realmThread;
	}
	
	public int getID() {
		return _GUID;
	}
	
	public String getNombre() {
		return _name;
	}

	public String get_pass() {
		return _pass;
	}

	public String getApodo() {
		return _pseudo;
	}

	public String get_key() {
		return _key;
	}

	public void setClientKey(String aKey)
	{
		_key = aKey;
	}
	
	public Map<Integer, Personaje> get_persos() {
		return _persos;
	}

	public String get_lastIP() {
		return _lastIP;
	}

	public String get_question() {
		return _question;
	}

	public Personaje get_curPerso() {
		return _curPerso;
	}

	public String get_reponse() {
		return _reponse;
	}

	public boolean isBanned() {
		return _banned;
	}

	public void setBanned(boolean banned) {
		_banned = banned;
	}

	public boolean isConectado() {
		if(_gameThread != null)return true;
		if(_realmThread != null)return true;
		return false;
	}

	public int getGMLVL() { return _gmLvl; }

	public String getActualIP() {
		return _curIP;
	}
	
	public boolean isValidPass(String pass,String hash) {
		return pass.equals(GestorEncriptador.CryptPassword(hash, _pass));
	}
	
	public int getNumeroDePersonajes() {
		return _persos.size();
	}

	public static boolean COMPTE_LOGIN(String name, String pass, String key) {
		return Mundo.getCompteByName(name) != null && Objects.requireNonNull(Mundo.getCompteByName(name)).isValidPass(pass, key);
	}

	public void addPerso(Personaje perso)
	{
		_persos.put(perso.get_GUID(),perso);
	}
	
	public boolean createPerso(String name, int sexe, int classe,int color1, int color2, int color3) {
		Personaje perso = Personaje.CREATE_PERSONNAGE(name, sexe, classe, color1, color2, color3, this);
		if(perso==null) {
			return false;
		}
		_persos.put(perso.get_GUID(), perso);
		return true;
	}

	public void deletePerso(int guid) {
		if(!_persos.containsKey(guid))return;
		Mundo.deletePerso(_persos.get(guid));
		_persos.remove(guid);
	}

	public void setRealmThread(RealmThread thread)
	{
		_realmThread = thread;
	}

	public void setCurPerso(Personaje perso)
	{
		_curPerso = perso;
	}

	public void updateInfos(int aGUID,String aName,String aPass, String aPseudo,String aQuestion,String aReponse,int aGmLvl, boolean aBanned) {
		this._GUID 		= aGUID;
		this._name 		= aName;
		this._pass		= aPass;
		this._pseudo 	= aPseudo;
		this._question	= aQuestion;
		this._reponse	= aReponse;
		this._gmLvl		= aGmLvl;
		this._banned	= aBanned;
	}

	public void deconnexion() {
		_curPerso = null;
		_gameThread = null;
		_realmThread = null;
		_curIP = "";
		GestorSQL.salir_del_juego(getID(), 0);
		resetAllChars(true);
		GestorSQL.actualizar_datos_cuenta(this);
	}

	public void resetAllChars(boolean save) {
		for(Personaje P : _persos.values()) {
			//Si Echange avec un joueur
			if(P.get_curExchange() != null)P.get_curExchange().cancel();
			//Si en groupe
			if(P.getActualGrupo() != null)P.getActualGrupo().leave(P);
			
			//Si en combat
			if(P.getPelea() != null)P.getPelea().leftFight(P, null);
			else//Si hors combat
			{
				P.getActualCelda().removePlayer(P.get_GUID());
				if(P.getActualMapa() != null && P.isConectado()) GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(P.getActualMapa(), P.get_GUID());
			}
			P.setConectado(false);
			//Reset des vars du perso
			P.resetVars();
			if(save) GestorSQL.guardar_personaje(P,true);
			Mundo.unloadPerso(P.get_GUID());
		}
		_persos.clear();
	}

	public String parseFriendList() {
		StringBuilder str = new StringBuilder();
		if(_friendGuids.isEmpty())return "";
		for(int i : _friendGuids) {
			Cuenta C = Mundo.getCompte(i);
			if(C == null)continue;
			str.append("|").append(C.getApodo());
			//on s'arrete la si aucun perso n'est connecté
			if(!C.isConectado())continue;
			Personaje P = C.get_curPerso();
			if(P == null)continue;
			str.append(P.parseToFriendList(_GUID));
		}
		return str.toString();
	}
	
	public void SendOnline() {
		for (int i : _friendGuids) {
			if (this.isFriendWith(i)) {
				Personaje perso = Mundo.getPersonnage(i);
				if (perso != null && perso.is_showFriendConnection() && perso.isConectado())
				GestorSalida.GAME_SEND_FRIEND_ONLINE(this._curPerso, perso);
			}
		}
	}

	public void addFriend(int guid) {
		if(_GUID == guid) {
			GestorSalida.GAME_SEND_FA_PACKET(_curPerso,"Ey");
			return;
		}
		if(!_friendGuids.contains(guid)) {
			_friendGuids.add(guid);
			GestorSalida.GAME_SEND_FA_PACKET(_curPerso,"K"+ Mundo.getCompte(guid).getApodo()+ Mundo.getCompte(guid).get_curPerso().parseToFriendList(_GUID));
			GestorSQL.actualizar_datos_cuenta(this);
		}
		else GestorSalida.GAME_SEND_FA_PACKET(_curPerso,"Ea");
	}
	
	public void removeFriend(int guid) {
		if(_friendGuids.remove((Object)guid)) GestorSQL.actualizar_datos_cuenta(this);
		GestorSalida.GAME_SEND_FD_PACKET(_curPerso,"K");
	}
	
	public boolean isFriendWith(int guid)
	{
		return _friendGuids.contains(guid);
	}
	
	public String parseFriendListToDB() {
		StringBuilder str = new StringBuilder();
		for(int i : _friendGuids) {
			if(!str.toString().equalsIgnoreCase("")) str.append(";");
			str.append(i);
		}
		return str.toString();
	}
	
	public void addEnemy(String packet, int guid) {
		if(_GUID == guid) {
			GestorSalida.GAME_SEND_FA_PACKET(_curPerso,"Ey");
			return;
		}
		if(!_EnemyGuids.contains(guid)) {
			_EnemyGuids.add(guid);
			Personaje Pr = Mundo.getPersonajePorNombre(packet);
			GestorSalida.GAME_SEND_ADD_ENEMY(_curPerso, Pr);
			GestorSQL.actualizar_datos_cuenta(this);
		}
		else GestorSalida.GAME_SEND_iAEA_PACKET(_curPerso);
	}
	
	public void removeEnemy(int guid) {
		if(_EnemyGuids.remove((Object)guid)) GestorSQL.actualizar_datos_cuenta(this);
		GestorSalida.GAME_SEND_iD_COMMANDE(_curPerso,"K");
	}
	
	public boolean isEnemyWith(int guid)
	{
		return _EnemyGuids.contains(guid);
	}
	
	public String parseEnemyListToDB() {
		StringBuilder str = new StringBuilder();
		for(int i : _EnemyGuids) {
			if(!str.toString().equalsIgnoreCase("")) str.append(";");
			str.append(i);
		}
		return str.toString();
	}
	
	public String parseEnemyList() {
		StringBuilder str = new StringBuilder();
		if(_EnemyGuids.isEmpty())return "";
		for(int i : _EnemyGuids) {
			Cuenta C = Mundo.getCompte(i);
			if(C == null)continue;
			str.append("|").append(C.getApodo());
			//on s'arrete la si aucun perso n'est connecté
			if(!C.isConectado())continue;
			Personaje P = C.get_curPerso();
			if(P == null)continue;
			str.append(P.parseToEnemyList(_GUID));
		}
		return str.toString();
	}
	
	public void setGMLVL(int gmLvl)
	{
		_gmLvl = gmLvl;
	}

	public int getVIP() {
		return _vip;
	}
	
	public boolean recoverItem(int ligneID, int amount) {
		if(_curPerso == null)
			return false;
		if(_curPerso.get_isTradingWith() >= 0)
			return false;
		
		int hdvID = Math.abs(_curPerso.get_isTradingWith());//Récupère l'ID de l'HDV
		
		HdvEntry entry = null;
		for(HdvEntry tempEntry : _hdvsItems.get(hdvID))//Boucle dans la liste d'entry de l'HDV pour trouver un entry avec le meme cheapestID que spécifié
		{
			if(tempEntry.getLigneID() == ligneID)//Si la boucle trouve un objet avec le meme cheapestID, arrete la boucle
			{
				entry = tempEntry;
				break;
			}
		}
		if(entry == null)//Si entry == null cela veut dire que la boucle s'est effectué sans trouver d'item avec le meme cheapestID
			return false;
		
		_hdvsItems.get(hdvID).remove(entry);//Retire l'item de la liste des objets a vendre du compte

		Objeto obj = entry.getObjet();
		
		boolean OBJ = _curPerso.addObjet(obj,true);//False = Meme item dans l'inventaire donc augmente la qua
		if(!OBJ) {
			Mundo.removeItem(obj.getGuid());
		}
		
		Mundo.getHdv(hdvID).delEntry(entry);//Retire l'item de l'HDV
		return true;
		//Hdv curHdv = World.getHdv(hdvID);
	}
	
	public HdvEntry[] getMercadilloObjetos(int hdvID) {
		if(_hdvsItems.get(hdvID) == null)
			return new HdvEntry[1];
		
		HdvEntry[] toReturn = new HdvEntry[20];
		for (int i = 0; i < _hdvsItems.get(hdvID).size(); i++) {
			toReturn[i] = _hdvsItems.get(hdvID).get(i);
		}
		return toReturn;
	}
	
	public int countHdvItems(int hdvID) {
		if(_hdvsItems.get(hdvID) == null)
			return 0;
		
		return _hdvsItems.get(hdvID).size();
	}
}
