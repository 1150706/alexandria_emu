package objetos;

import juego.*;
import juego.JuegoThread.*;

import java.util.ArrayList;
import java.util.Map;
import javax.swing.Timer;
import objetos.Pelea.*;
import objetos.Monstruo.*;
import objetos.NPCModelo.*;

import java.util.TreeMap;
import java.util.Map.Entry;
import comunes.*;
import comunes.Mundo.*;
import objetos.casas.Casas;
import objetos.casas.Cofres;

public class Mapa {
	private final short _id;
	private final String _date;
	private final byte _w;
	private final byte _h;
	private final String _key;
	private String _placesStr;
	private Map<Integer,Case> 		_cases 			= new TreeMap<>();
	private final Map<Integer, Pelea> 		_fights 		= new TreeMap<>();
	private final ArrayList<MobGrade> 	_mobPossibles 	= new ArrayList<>();
	private final Map<Integer,MobGroup> 	_mobGroups 		= new TreeMap<>();
	private final Map<Integer,MobGroup> 	_fixMobGroups 	= new TreeMap<>();
	private final Map<Integer,NPC>		_npcs	 		= new TreeMap<>();
	int _nextObjectID = -1;
	private byte _X = 0;
	private byte _Y = 0;
	private SubArea _subArea;
	private MountPark _mountPark;
	private byte _maxGroup = 3;
	private final Map<Integer,ArrayList<Accion>> _endFightAction = new TreeMap<>();
	private byte _maxSize;
	
	public static class MountPark {
		private int _owner;
		private final InteractiveObject _door;
		private final int _size;
		private final ArrayList<Case> _cases = new ArrayList<>();
		private Gremio _guild;
		private final Mapa _map;
		private int _cellid = -1;
		private int _price;
		private final Map<Integer,Integer> MountParkDATA = new TreeMap<>();//DragoID, IDperso
		
		public MountPark(int owner, Mapa map, int cellid, int size, String data, int guild, int price) {
			_owner = owner;
			_door = map.getMountParkDoor();
			_size = size;
			_guild = Mundo.getGuild(guild);
			_map = map;
			_cellid = cellid;
			_price = price;
			if(_map != null)_map.setMountPark(this);
			for(String firstCut : data.split(";"))//PosseseurID,DragoID;PosseseurID2,DragoID2;PosseseurID,DragoID3
			{
				try {
					String[] secondCut = firstCut.split(",");
					Dragopavo DD = Mundo.getDragopavoPorID(Integer.parseInt(secondCut[1]));
					if(DD == null) continue;
					MountParkDATA.put(Integer.parseInt(secondCut[1]), Integer.parseInt(secondCut[0]));
				}catch(Exception ignored){}
			}
		}

		public int get_owner() {
			return _owner;
		}
		
		public void set_owner(int AccID) {
			_owner = AccID;
		}
		
		public InteractiveObject get_door() {
			return _door;
		}

		public int get_size() {
			return _size;
		}

		public Gremio getGremio() {
			return _guild;
		}
		
		public void set_guild(Gremio guild) {
			_guild = guild;
		}

		public Mapa get_map() {
			return _map;
		}
		
		public int get_cellid() {
			return _cellid;
		}

		public int get_price() {
			return _price;
		}
		
		public void set_price(int price) {
			_price = price;
		}

		public int getObjectNumb() {
			int n = 0;
			for(Case C : _cases)if(C.getObject() != null)n++;
			return n;
		}

		public String parseData(int PID, boolean isPublic) {
			if(MountParkDATA.isEmpty())return "~";
			
			StringBuilder packet = new StringBuilder();
			for(Entry<Integer, Integer> MPdata : MountParkDATA.entrySet()) {
				if(MPdata.getValue() == PID && isPublic)//Montrer que ses montures uniquement en public
				{
					if(packet.length() > 0)packet.append(";");
					packet.append(Mundo.getDragopavoPorID(MPdata.getKey()).parse());
				}else {
					if(packet.length() > 0)packet.append(";");
					packet.append(Mundo.getDragopavoPorID(MPdata.getKey()).parse());
				}
			}
			return packet.toString();
		}
		
		public String parseDBData() {
			StringBuilder str = new StringBuilder();
			if(MountParkDATA.isEmpty())return "";
			
			for(Entry<Integer, Integer> MPdata : MountParkDATA.entrySet()) {
				if(str.length() > 0)str.append(";");
				str.append(MPdata.getValue()).append(",").append(Mundo.getDragopavoPorID(MPdata.getKey()).getID());
			}
			return str.toString();
		}
		
		public void addData(int DID, int PID)
		{
			MountParkDATA.put(DID, PID);
		}
		
		public void removeData(int DID)
		{
			MountParkDATA.remove(DID);
		}
		
		public Map<Integer, Integer> getData()
		{
			return MountParkDATA;
		}
		
		public int MountParkDATASize()
		{
			return MountParkDATA.size();
		}
		
		public static void removeMountPark(int GuildID) {
			for(Entry<Short, Mapa.MountPark> mp : Mundo.getMountPark().entrySet())//Pour chaque enclo si ils en ont plusieurs
			{
				if(mp.getValue().getGremio().get_id() == GuildID) {
					if(!mp.getValue().getData().isEmpty()) {
						for(Entry<Integer, Integer> MPdata : mp.getValue().getData().entrySet()) {
							Mundo.removeDragodinde(MPdata.getKey());//Suppression des dindes dans le world
							GestorSQL.eliminar_montura(MPdata.getKey());//Suppression des dindes dans chaque enclo
						}
					}
					mp.getValue().getData().clear();
					mp.getValue().set_owner(0);
					mp.getValue().set_guild(null);
					mp.getValue().set_price(3000000);
					GestorSQL.guardar_cercados(mp.getValue());
					for(Personaje p : mp.getValue().get_map().getPersos())
					{
						GestorSalida.GAME_SEND_Rp_PACKET(p, mp.getValue());
					}
				}
			}
		}
	}
	
	public static class InteractiveObject {
		private final int _id;
		private int _state;
		private final Mapa _map;
		private final Case _cell;
		private boolean _interactive = true;
		private Timer _respawnTimer;
		private final ObjetosInteractivos _template;
		
		public InteractiveObject(Mapa a_map, Case a_cell, int a_id) {
			_id = a_id;
			_map = a_map;
			_cell = a_cell;
			_state = Constantes.IOBJECT_STATE_FULL;
			int respawnTime = 10000;
			_template = Mundo.getIOTemplate(_id);
			if(_template != null)respawnTime = _template.getRespawnTime();
			//définition du timer
			_respawnTimer = new Timer(respawnTime,
                    e -> {
                        _respawnTimer.stop();
                        _state = Constantes.IOBJECT_STATE_FULLING;
                        _interactive = true;
                        GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(_map, _cell);
                        _state = Constantes.IOBJECT_STATE_FULL;
                    }
            );
		}
		
		public int getID()
		{
			return _id;
		}
		
		public boolean isInteractive()
		{
			return _interactive;
		}
		
		public void setInteractive(boolean b)
		{
			_interactive = b;
		}
		
		public int getState()
		{
			return _state;
		}
		
		public void setState(int state)
		{
			_state = state;
		}

		public int getUseDuration() {
			int duration = 1500;
			if(_template != null) {
				duration = _template.getDuration();
			}
			return duration;
		}

		public void startTimer() {
			if(_respawnTimer == null)return;
			_state = Constantes.IOBJECT_STATE_EMPTY2;
			_respawnTimer.restart();
		}

		public int getUnknowValue() {
			int unk = 4;
			if(_template != null) {
				unk = _template.getUnk();
			}
			return unk;
		}

		public boolean isWalkable() {
			if(_template == null)return false;
			return _template.isWalkable() && _state == Constantes.IOBJECT_STATE_FULL;
		}
	}
	
	public static class Case {
		private final int _id;
		private Map<Integer, Personaje>	_persos;		//= new TreeMap<Integer, Personnage>();
		private Map<Integer, Peleador> 		_fighters;	//= new TreeMap<Integer, Fighter>();
		private boolean _Walkable = true;
		private boolean _LoS = true;
		private final short _map;
		//private ArrayList<Action> _onCellPass;
		//private ArrayList<Action> _onItemOnCell;
		private ArrayList<Accion> _onCellStop;// = new ArrayList<Action>();
		private InteractiveObject _object;
		private Objeto _droppedItem;
		
		public Case(Mapa a_map, int id, boolean _walk, boolean LoS, int objID) {
			_map = a_map.getID();
			_id = id;
			_Walkable = _walk;
			_LoS = LoS;
			if(objID == -1)return;
			_object = new InteractiveObject(a_map,this,objID);
		}

		public boolean getOnCellStopAction() {
			return this._onCellStop != null;
		}

		public InteractiveObject getObject() {
			return _object;
		}

		public Objeto getDroppedItem() {
			return _droppedItem;
		}

		public boolean canDoAction(int id) {
			switch(id) {
				//Moudre et egrenner - Paysan
				case 122:
				case 47:
					return _object.getID() == 7007;
				//Faucher Blé
				case 45:
					if (_object.getID() == 7511) {//Blé
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Faucher Orge
				case 53:
					if (_object.getID() == 7515) {//Orge
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				
				//Faucher Avoine
				case 57:
					if (_object.getID() == 7517) {//Avoine
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;	
				//Faucher Houblon
				case 46:
					if (_object.getID() == 7512) {//Houblon
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Faucher Lin
				case 50:
				case 68:
					if (_object.getID() == 7513) {//Lin
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Faucher Riz
				case 159:
					if (_object.getID() == 7550) {//Riz
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Faucher Seigle
				case 52:
					if (_object.getID() == 7516) {//Seigle
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Faucher Malt
				case 58:
					if (_object.getID() == 7518) {//Malt
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;			
				//Faucher Chanvre - Cueillir Chanvre
				case 69:
				case 54:
					if (_object.getID() == 7514) {//Chanvre
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Scier - Bucheron
				case 101:
					return _object.getID() == 7003;
				//Couper Frêne
				case 6:
					if (_object.getID() == 7500) {//Frêne
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Châtaignier
				case 39:
					if (_object.getID() == 7501) {//Châtaignier
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Noyer
				case 40:
					if (_object.getID() == 7502) {//Noyer
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Chêne
				case 10:
					if (_object.getID() == 7503) {//Chêne
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Oliviolet
				case 141:
					if (_object.getID() == 7542) {//Oliviolet
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Bombu
				case 139:
					if (_object.getID() == 7541) {//Bombu
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Erable
				case 37:
					if (_object.getID() == 7504) {//Erable
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Bambou
				case 154:
					if (_object.getID() == 7553) {//Bambou
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper If
				case 33:
					if (_object.getID() == 7505) {//If
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Merisier
				case 41:
					if (_object.getID() == 7506) {//Merisier
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Ebène
				case 34:
					if (_object.getID() == 7507) {//Ebène
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Kalyptus
				case 174:
					if (_object.getID() == 7557) {//Kalyptus
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Charme
				case 38:
					if (_object.getID() == 7508) {//Charme
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Orme
				case 35:
					if (_object.getID() == 7509) {//Orme
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Bambou Sombre
				case 155:
					if (_object.getID() == 7554) {//Bambou Sombre
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Couper Bambou Sacré
				case 158:
					if (_object.getID() == 7552) {//Bambou Sacré
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Puiser
				case 102:
					if (_object.getID() == 7519) {//Puits
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Polir
				case 48:
					return _object.getID() == 7005;//7510
				//Moule/Fondre - Mineur
				case 32:
					return _object.getID() == 7002;
				//Miner Fer
				case 24:
					if (_object.getID() == 7520) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Cuivre
				case 25:
					if (_object.getID() == 7522) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Bronze
				case 26:
					if (_object.getID() == 7523) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Kobalte
				case 28:
					if (_object.getID() == 7525) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Manga
				case 56:
					if (_object.getID() == 7524) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Sili
				case 162:
					if (_object.getID() == 7556) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Etain
				case 55:
					if (_object.getID() == 7521) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Argent
				case 29:
					if (_object.getID() == 7526) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Bauxite
				case 31:
					if (_object.getID() == 7528) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Or
				case 30:
					if (_object.getID() == 7527) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Miner Dolomite
				case 161:
					if (_object.getID() == 7555) {//Miner
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Fabriquer potion - Alchimiste
				case 23:
					return _object.getID() == 7019;
				//Cueillir Trèfle
				case 71:
					if (_object.getID() == 7533) {//Trèfle
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Cueillir Menthe
				case 72:
					if (_object.getID() == 7534) {//Menthe
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Cueillir Orchidée
				case 73:
					if (_object.getID() == 7535) {// Orchidée
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Cueillir Edelweiss
				case 74:
					if (_object.getID() == 7536) {//Edelweiss
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Cueillir Graine de Pandouille
				case 160:
					if (_object.getID() == 7551) {//Graine de Pandouille
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Vider - Pêcheur
				case 133:
					return _object.getID() == 7024;
				//Pêcher Petits poissons de mer
				case 128:
					if (_object.getID() == 7530) {//Petits poissons de mer
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Petits poissons de rivière
				case 124:
					if (_object.getID() == 7529) {//Petits poissons de rivière
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Pichon
				case 136:
					if (_object.getID() == 7544) {//Pichon
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Ombre Etrange
				case 140:
					if (_object.getID() == 7543) {//Ombre Etrange
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Poissons de rivière
				case 125:
					if (_object.getID() == 7532) {//Poissons de rivière
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Poissons de mer
				case 129:
					if (_object.getID() == 7531) {//Poissons de mer
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Gros poissons de rivière
				case 126:
					if (_object.getID() == 7537) {//Gros poissons de rivière
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Gros poissons de mers
				case 130:
					if (_object.getID() == 7538) {//Gros poissons de mers
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Poissons géants de rivière
				case 127:
					if (_object.getID() == 7539) {//Poissons géants de rivière
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Pêcher Poissons géants de mer
				case 131:
					if (_object.getID() == 7540) {//Poissons géants de mer
						return _object.getState() == Constantes.IOBJECT_STATE_FULL;
					}
				return false;
				//Boulanger
				case 109://Pain
				case 27://Bonbon
					return _object.getID() == 7001;
				//Poissonier
				case 135://Faire un poisson (mangeable)
					return _object.getID() == 7022;
				//Chasseur
				case 134:
					return _object.getID() == 7023;
				//Boucher
				case 132:
					return _object.getID() == 7025;
				case 157:
					return (_object.getID() == 7030 || _object.getID() == 7031);
				case 44://Sauvegarder le Zaap
				case 114://Utiliser le Zaap
					//Zaaps
					return switch (_object.getID()) {
						case 7000, 7026, 7029, 4287 -> true;
						default -> false;
					};

				case 175://Accéder
				case 176://Acheter
				case 177://Vendre
				case 178://Modifier le prix de vente
					//Enclos
					return switch (_object.getID()) {
						case 6763, 6766, 6767, 6772 -> true;
						default -> false;
					};

				//Se rendre à incarnam
				case 183:
					return switch (_object.getID()) {
						case 1845, 1853, 1854, 1855, 1856, 1857, 1858, 1859, 1860, 1861, 1862, 2319 -> true;
						default -> false;
					};

				//Enclume magique
				case  1:
				case 113:
				case 115:
				case 116:
				case 117:
				case 118:
				case 119:
				case 120:
					return _object.getID() == 7020;

				//Enclume
				case 19:
				case 143:
				case 145:
				case 144:
				case 142:
				case 146:
				case 67:
				case 21:
				case 65:
				case 66:
				case 20:
				case 18:
					return _object.getID() == 7012;

				//Costume Mage
				case 167:
				case 165:
				case 166:
					return _object.getID() == 7036;

				//Coordo Mage
				case 164:
				case 163:
					return _object.getID() == 7037;

				//Joai Mage
				case 168:
				case 169:
					return _object.getID() == 7038;

				//Bricoleur
				case 171:
				case 182:
					return _object.getID() == 7039;

				//Forgeur Bouclier
				case 156:
					return _object.getID() == 7027;

				//Coordonier
				case 13:
				case 14:
					return _object.getID() == 7011;

				//Tailleur (Dos)
				case 123:
				case 64:
					return _object.getID() == 7015;


				//Sculteur
				case 17:
				case 16:
				case 147:
				case 148:
				case 149:
				case 15:
					return _object.getID() == 7013;

				//Tailleur (Haut)
				case 63:
					return (_object.getID() == 7014 || _object.getID() == 7016);
				//Atelier : Créer Amu // Anneau
				case 11:
				case 12:
					return (_object.getID() >= 7008 && _object.getID() <= 7010);
				//Maison
				case 81://Vérouiller
				case 84://Acheter
				case 97://Entrer
				case 98://Vendre
				case 108://Modifier le prix de vente
					return (_object.getID() >= 6700 && _object.getID() <= 6776);
				//Coffre	
				case 104://Ouvrir
				case 105://Code
					return (_object.getID() == 7350 || _object.getID() == 7351 || _object.getID() == 7353);
				//Action ID non trouvé
				default:
					JuegoServidor.agregar_a_los_logs("MapActionID non existant dans Case.canDoAction: "+id);
					return false;
			}
		}
		
		public int getID()
		{
			return _id;
		}
		
		public void addOnCellStopAction(int id, String args, String cond)
		{
			if(_onCellStop == null) _onCellStop = new ArrayList<>();
			
			_onCellStop.add(new Accion(id,args,cond));
		}
		
		public void applyOnCellStopActions(Personaje perso)
		{
			if(_onCellStop == null) return;
			
			for(Accion act : _onCellStop)
			{
				act.apply(perso, null, -1, -1);
			}
		}
		public void addPerso(Personaje perso)
		{
			if(_persos == null) _persos = new TreeMap<>();
			_persos.put(perso.getID(),perso);
			
		}
		public void addFighter(Peleador fighter)
		{
			if(_fighters == null) _fighters = new TreeMap<>();
			_fighters.put(fighter.getID(),fighter);
		}
		public void removeFighter(Peleador fighter)
		{
			_fighters.remove(fighter.getID());
		}

		public boolean isCaminable(boolean useObject) {
			if(_object != null && useObject)
				return _Walkable && _object.isWalkable();
			return _Walkable;
		}

		public boolean blockLoS() {
			if(_fighters == null) return _LoS;
			boolean fighter = true;
			for(Entry<Integer, Peleador> f : _fighters.entrySet())
			{
				if(!f.getValue().isHide())fighter = false;
			}
			return _LoS && fighter;
		}
		public boolean isLoS()
		{
			return _LoS;
		}
		public void removePlayer(int _guid)
		{
			if(_persos == null) return;
			_persos.remove(_guid);
			if(_persos.isEmpty()) _persos = null;
		}
		public Map<Integer, Personaje> getPersos()
		{
			if(_persos == null) return new TreeMap<>();
			return _persos;
		}
		public Map<Integer, Peleador> getFighters()
		{
			if(_fighters == null) return new TreeMap<>();
			return _fighters;
		}
		public Peleador getFirstFighter()
		{
			if(_fighters == null) 
				return null;
			for(Entry<Integer, Peleador> entry : _fighters.entrySet())
				return entry.getValue();
			return null;
		}

		public void startAction(Personaje perso, JuegoAccion GA)
		{
			int actionID = -1;
			short CcellID = -1;
			try
			{
				actionID = Integer.parseInt(GA._args.split(";")[1]);
				CcellID = Short.parseShort(GA._args.split(";")[0]);
			}catch(Exception e){e.printStackTrace();}
			if(actionID == -1)return;
			if(Constantes.isJobAction(actionID))
			{
				perso.doJobAction(actionID,_object,GA,this);
				return;
			}
			//SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(perso.get_curCarte(),this);
			switch (actionID) {
//Sauvegarder pos
				case 44 -> {
					String str = _map + "," + _id;
					perso.set_savePos(str);
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "06");
				}
//Puiser
				case 102 -> {
					if (!_object.isInteractive()) return;//Si l'objet est utilisé
					if (_object.getState() != Constantes.IOBJECT_STATE_FULL) return;//Si le puits est vide
					_object.setState(Constantes.IOBJECT_STATE_EMPTYING);
					_object.setInteractive(false);
					GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(perso.getActualMapa(), "" + GA._id, 501, perso.getID() + "", _id + "," + _object.getUseDuration() + "," + _object.getUnknowValue());
					GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(perso.getActualMapa(), this);
				}
//Utiliser (zaap)
				case 114 -> {
					perso.openZaapMenu();
					perso.getCuenta().getJuegoThread().removeAction(GA);
				}
//Zaapis
				case 157 -> {
					StringBuilder ZaapiList = new StringBuilder();
					String[] Zaapis;
					int count = 0;
					int price = 20;
					if (perso.getActualMapa()._subArea.get_area().get_id() == 7 && (perso.get_align() == 1 || perso.get_align() == 0 || perso.get_align() == 3))//Ange, Neutre ou Sérianne
					{
						Zaapis = Constantes.ZAAPI.get(Constantes.ALIGNEMENT_BONTARIEN).split(",");
						if (perso.get_align() == 1) price = 10;
					} else if (perso.getActualMapa()._subArea.get_area().get_id() == 11 && (perso.get_align() == 2 || perso.get_align() == 0 || perso.get_align() == 3))//Démons, Neutre ou Sérianne
					{
						Zaapis = Constantes.ZAAPI.get(Constantes.ALIGNEMENT_BRAKMARIEN).split(",");
						if (perso.get_align() == 2) price = 10;
					} else {
						Zaapis = Constantes.ZAAPI.get(Constantes.ALIGNEMENT_NEUTRE).split(",");
					}
					if (Zaapis.length > 0) {
						for (String s : Zaapis) {
							if (count == Zaapis.length)
								ZaapiList.append(s).append(";").append(price);
							else
								ZaapiList.append(s).append(";").append(price).append("|");
							count++;
						}
						perso.SetZaaping(true);
						GestorSalida.GAME_SEND_ZAAPI_PACKET(perso, ZaapiList.toString());
					}
				}
//Acceder a un enclos
				case 175 -> {
					if (_object.getState() != Constantes.IOBJECT_STATE_EMPTY) ;
					perso.openMountPark();
				}
//Achat enclo
				case 176 -> {
					MountPark MP = perso.getActualMapa().getMountPark();
					if (MP.get_owner() == -1)//Public
					{
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "196");
						return;
					}
					if (MP.get_price() == 0)//Non en vente
					{
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "197");
						return;
					}
					if (perso.get_guild() == null)//Pas de guilde
					{
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1135");
						return;
					}
					if (perso.getMiembroGremio().getRank() != 1)//Non meneur
					{
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "198");
						return;
					}
					GestorSalida.GAME_SEND_R_PACKET(perso, "D" + MP.get_price() + "|" + MP.get_price());
				}
//Modifier prix de vente
				case 177, 178 -> {
					MountPark MP1 = perso.getActualMapa().getMountPark();
					if (MP1.get_owner() == -1) {
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "194");
						return;
					}
					if (MP1.get_owner() != perso.getID()) {
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "195");
						return;
					}
					GestorSalida.GAME_SEND_R_PACKET(perso, "D" + MP1.get_price() + "|" + MP1.get_price());
				}
//Retourner sur Incarnam
				case 183 -> {
					if (perso.get_lvl() > 15) {
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1127");
						perso.getCuenta().getJuegoThread().removeAction(GA);
						return;
					}
					short mapID = Constantes.getStartMap(perso.getClase());
					int cellID = Constantes.getStartCell(perso.getClase());
					perso.teletransportar(mapID, cellID);
					perso.getCuenta().getJuegoThread().removeAction(GA);
				}
//Vérouiller maison
				case 81 -> {
					Casas h = Casas.get_house_id_by_coord(perso.getActualMapa().getID(), CcellID);
					if (h == null) return;
					perso.setInHouse(h);
					h.Lock(perso);
				}
//Rentrer dans une maison
				case 84 -> {
					Casas h2 = Casas.get_house_id_by_coord(perso.getActualMapa().getID(), CcellID);
					if (h2 == null) return;
					perso.setInHouse(h2);
					h2.HopIn(perso);
				}
//Acheter maison
				case 97 -> {
					Casas h3 = Casas.get_house_id_by_coord(perso.getActualMapa().getID(), CcellID);
					if (h3 == null) return;
					perso.setInHouse(h3);
					h3.BuyIt(perso);
				}
//Ouvrir coffre privé
				case 104 -> {
					Cofres trunk = Cofres.get_trunk_id_by_coord(perso.getActualMapa().getID(), CcellID);
					if (trunk == null) {
						JuegoServidor.agregar_a_los_logs("Game: INVALID TRUNK ON MAP : " + perso.getActualMapa().getID() + " CELLID : " + CcellID);
						return;
					}
					perso.setInTrunk(trunk);
					trunk.HopIn(perso);
				}
//Vérouiller coffre
				case 105 -> {
					Cofres t = Cofres.get_trunk_id_by_coord(perso.getActualMapa().getID(), CcellID);
					if (t == null) {
						JuegoServidor.agregar_a_los_logs("Game: INVALID TRUNK ON MAP : " + perso.getActualMapa().getID() + " CELLID : " + CcellID);
						return;
					}
					perso.setInTrunk(t);
					t.Lock(perso);
				}
//Modifier prix de vente
				case 98, 108 -> {
					Casas h4 = Casas.get_house_id_by_coord(perso.getActualMapa().getID(), CcellID);
					if (h4 == null) return;
					perso.setInHouse(h4);
					h4.SellIt(perso);
				}
				default -> JuegoServidor.agregar_a_los_logs("Case.startAction non definie pour l'actionID = " + actionID);
			}
		}

		public void finishAction(Personaje perso, JuegoAccion GA) {
			int actionID = -1;
			try {
				actionID = Integer.parseInt(GA._args.split(";")[1]);
			}catch(Exception ignored){}
			if(actionID == -1)return;
			
			if(Constantes.isJobAction(actionID)) {
				perso.finishJobAction(actionID,_object,GA,this);
				return;
			}
			switch(actionID) {
				case 44://Sauvegarder a un zaap
				case 81://Vérouiller maison
				case 84://ouvrir maison
				case 97://Acheter maison.
				case 98://Vendre
				case 104://Ouvrir coffre
				case 105://Code coffre
				case 108://Modifier prix de vente
				case 157://Zaapi
				break;
				case 102://Puiser
					_object.setState(Constantes.IOBJECT_STATE_EMPTY);
					_object.setInteractive(false);
					_object.startTimer();
					GestorSalida.GAME_SEND_GDF_PACKET_TO_MAP(perso.getActualMapa(),this);
					int qua = Formulas.getRandomValue(1, 10);//On a entre 1 et 10 eaux
					Objeto obj = Mundo.getObjTemplate(311).createNewItem(qua, false);
					if(perso.addObjet(obj, true))
						Mundo.addObjet(obj,true);
					GestorSalida.GAME_SEND_IQ_PACKET(perso,perso.getID(),qua);
				break;
				
				case 183:
				break;
				
				default:
					JuegoServidor.agregar_a_los_logs("[FIXME]Case.finishAction non definie pour l'actionID = "+actionID);
				break;
			}
		}

		public void EliminarAccionDeCelda() {
			//_onCellStop.clear();
			_onCellStop = null;
		}

		public void addDroppedItem(Objeto obj)
		{
			_droppedItem = obj;
		}

		public void clearDroppedItem()
		{
			_droppedItem = null;
		}
	}

	public Mapa(short _id, String _date, byte _w, byte _h, String _key, String places, String dData, String cellsData, String monsters, String mapPos, byte maxGroup, byte maxSize) {
		this._id = _id;
		this._date = _date;
		this._w = _w;
		this._h = _h;
		this._key = _key;
		this._placesStr = places;
		this._maxGroup = maxGroup;
		this._maxSize = maxSize;
		String[] mapInfos = mapPos.split(",");
		try {
			this._X = Byte.parseByte(mapInfos[0]);
			this._Y = Byte.parseByte(mapInfos[1]);
			int subArea = Integer.parseInt(mapInfos[2]);
			_subArea = Mundo.getSubArea(subArea);
			if(_subArea != null)_subArea.addMap(this);
		}catch(Exception e) {
			JuegoServidor.agregar_a_los_logs("Erreur de chargement de la map "+_id+": Le champ MapPos est invalide");
			System.exit(0);
		}
		
		if(!dData.isEmpty()) {
		_cases = GestorEncriptador.DecompileMapData(this,dData);
		}else {
		String[] cellsDataArray = cellsData.split("\\|");
		
		for(String o : cellsDataArray) {
			
			boolean Walkable = true;
			boolean LineOfSight = true;
			int Number = -1;
			int obj = -1;
			String[] cellInfos = o.split(",");
			try {
				Walkable = cellInfos[2].equals("1");
				LineOfSight = cellInfos[1].equals("1");
				Number = Integer.parseInt(cellInfos[0]);
				if(!cellInfos[3].trim().equals("")) {
					obj = Integer.parseInt(cellInfos[3]);
				}
			}catch(Exception ignored){}
			if(Number == -1)continue;
			
            _cases.put(Number, new Case(this,Number,Walkable,LineOfSight,obj));	
			}
		}
		for(String mob : monsters.split("\\|")) {
			if(mob.equals(""))continue;
			int id = 0;
			int lvl = 0;
			
			try {
				id = Integer.parseInt(mob.split(",")[0]);
				lvl = Integer.parseInt(mob.split(",")[1]);
			}catch(NumberFormatException e){continue;}
			if(id == 0 || lvl == 0)continue;
			if(Mundo.getMonstre(id) == null)continue;
			if(Mundo.getMonstre(id).getGradeByLevel(lvl) == null)continue;
			_mobPossibles.add(Mundo.getMonstre(id).getGradeByLevel(lvl));
		}
		if(_cases.isEmpty())return;
		
		if (MainServidor.USAR_MOOBS) {
			if(_maxGroup == 0)return;
			spawnGroup(Constantes.ALIGNEMENT_NEUTRE,_maxGroup,false,-1);//Spawn des groupes d'alignement neutre
			spawnGroup(Constantes.ALIGNEMENT_BONTARIEN,1,false,-1);//Spawn du groupe de gardes bontarien s'il y a
			spawnGroup(Constantes.ALIGNEMENT_BRAKMARIEN,1,false,-1);//Spawn du groupe de gardes brakmarien s'il y a
		}
	}

	public void applyEndFightAction(int type, Personaje perso) {
		if(_endFightAction.get(type) == null)
			return;
		for(Accion A : _endFightAction.get(type))
			A.apply(perso, null, -1, -1);
	}
	
	public boolean hasEndFightAction(int type) {
		return (_endFightAction.get(type) != null);
	}

	public void addEndFightAction(int type, Accion A) {
		_endFightAction.computeIfAbsent(type, k -> new ArrayList<>());
		//On retire l'action si elle existait déjà
		delEndFightAction(type,A.getID());
		_endFightAction.get(type).add(A);
	}

	public void delEndFightAction(int type,int aType) {
		if(_endFightAction.get(type) == null)return;
		ArrayList<Accion> copy = new ArrayList<>(_endFightAction.get(type));
		for(Accion A : copy)if(A.getID() == aType)_endFightAction.get(type).remove(A);
	}

	public void MovimientoDeMonstruosEnMapas() {
		if (getMobGroups().size() == 0)
			return;
		int RandNumb = Formulas.getRandomValue(1, getMobGroups().size());
		int i = 0;
		for (Monstruo.MobGroup group : getMobGroups().values()) {
			if(group.isFix() && this._id != 8279)
				continue;
			if (this._id == 8279) {// W:15   H:17
				final int cell1 = group.getCeldaID();
				final Case cell2 = this.getMapa((cell1 - 15)), cell3 = this.getMapa((cell1 - 15 + 1));
				final Case cell4 = this.getMapa((cell1 + 15 - 1)), cell5 = this.getMapa((cell1 + 15));
				boolean case2 = (cell2 != null && (cell2.isCaminable(true) && (cell2.getPersos().isEmpty())));
				boolean case3 = (cell3 != null && (cell3.isCaminable(true) && (cell3.getPersos().isEmpty())));
				boolean case4 = (cell4 != null && (cell4.isCaminable(true) && (cell4.getPersos().isEmpty())));
				boolean case5 = (cell5 != null && (cell5.isCaminable(true) && (cell5.getPersos().isEmpty())));
				ArrayList<Boolean> array = new ArrayList<>();
				array.add(case2);
				array.add(case3);
				array.add(case4);
				array.add(case5);

				int count = 0;
				for (boolean bo : array)
					if (bo)
						count++;

				if (count == 0)
					return;
				if (count == 1) {
					Case newCell = (case2 ? cell2 : (case3 ? cell3 : (case4 ? cell4 : cell5)));
					Case nextCell = null;
					if (newCell == null)
						return;

					if (newCell.equals(cell2)) {
						if (checkCell(newCell.getID() - 15)) {
							nextCell = this.getMapa(newCell.getID() - 15);
							if (this.checkCell(nextCell.getID() - 15)) {
								nextCell = this.getMapa(nextCell.getID() - 15);
							}
						}
					} else if (newCell.equals(cell3)) {
						if (this.checkCell(newCell.getID() - 15 + 1)) {
							nextCell = this.getMapa(newCell.getID() - 15 + 1);
							if (this.getMapa(nextCell.getID() - 15 + 1) != null) {
								nextCell = this.getMapa(nextCell.getID() - 15 + 1);
							}
						}
					} else if (newCell.equals(cell4)) {
						if (this.checkCell(newCell.getID() + 15 - 1)) {
							nextCell = this.getMapa(newCell.getID() + 15 - 1);
							if (this.checkCell(nextCell.getID() + 15 - 1)) {
								nextCell = this.getMapa(nextCell.getID() + 15 - 1);
							}
						}
					} else if (newCell.equals(cell5)) {
						if (this.checkCell(newCell.getID() + 15)) {
							nextCell = this.getMapa(newCell.getID() + 15);
							if (this.checkCell(nextCell.getID() + 15)) {
								nextCell = this.getMapa(nextCell.getID() + 15);
							}
						}
					}

					String pathstr;
					try {
						assert nextCell != null;
						pathstr = Camino.getShortestStringPathBetween(this, group.getCeldaID(), nextCell.getID(), 0);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
					if (pathstr == null)
						return;
					group.setCellID(nextCell.getID());
					for (Personaje z : getPersos())
						GestorSalida.GAME_SEND_GA_PACKET(z.getCuenta().getJuegoThread().get_out(), "0", "1", group.getID()
								+ "", pathstr);
				} else {
					if (group.isFix())
						continue;
					i++;
					if (i != RandNumb)
						continue;

					int cell = -1;
					while (cell == -1 || cell == 383 || cell == 384
							|| cell == 398 || cell == 369)
						cell = getRandomNearFreeCellId(group.getCeldaID());
					String pathstr;
					try {
						pathstr = Camino.getShortestStringPathBetween(this, group.getCeldaID(), cell, 0);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
					if (pathstr == null)
						return;
					group.setCellID(cell);
					for (Personaje z : getPersos())
						GestorSalida.GAME_SEND_GA_PACKET(z.getCuenta().getJuegoThread().get_out(), "0", "1", group.getID() + "", pathstr);
				}
			} else {
				if (group.isFix())
					continue;
				i++;
				if (i != RandNumb)
					continue;
				int cell = getRandomNearFreeCellId(group.getCeldaID());
				String pathstr;
				try {
					pathstr = Camino.getShortestStringPathBetween(this, group.getCeldaID(), cell, 0);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				if (pathstr == null)
					return;
				group.setCellID(cell);
				for (Personaje z : getPersos())
					GestorSalida.GAME_SEND_GA_PACKET(z.getCuenta().getJuegoThread().get_out(), "0", "1", group.getID()
							+ "", pathstr);
			}

		}
	}

	public boolean checkCell(int id) {
		return this.getMapa(id - 15) != null && this.getMapa(id - 15).isCaminable(true);
	}

	public void setMountPark(MountPark mountPark)
	{
		_mountPark = mountPark;
	}

	public MountPark getMountPark()
	{
		return _mountPark;
	}

	public Mapa(short id, String date, byte w, byte h, String key, String places) {
		_id = id;
		_date = date;
		_w = w;
		_h = h;
		_key = key;
		_placesStr = places;
		_cases = new TreeMap<>();
	}
	
	public SubArea getSubArea()
	{
		return _subArea;
	}
	
	public int getX() {
		return _X;
	}

	public int getY() {
		return _Y;
	}
	
	public Map<Integer, NPC> getNPCS() { return _npcs; }

	public NPC addNpc(int npcID,int cellID, int dir) {
		NPCModelo temp = Mundo.getNPCTemplate(npcID);
		if(temp == null)return null;
		if(getMapa(cellID) == null)return null;
		NPC npc = new NPC(temp,_nextObjectID,cellID,(byte)dir);
		_npcs.put(_nextObjectID, npc);
		_nextObjectID--;
		return npc;
	}
	
	public void spawnGroup(int align, int nbr,boolean log,int cellID) {
		if(nbr<1)return;
		if(_mobGroups.size() - _fixMobGroups.size() >= _maxGroup)return;
		for(int a = 1; a<=nbr;a++) {
			MobGroup group  = new MobGroup(_nextObjectID,align,_mobPossibles,this,cellID,this._maxSize);
			if(group.getMobs().isEmpty())continue;
			_mobGroups.put(_nextObjectID, group);
			if(log) {
				JuegoServidor.agregar_a_los_logs("Groupe de monstres ajoutes sur la map: "+_id+" alignement: "+align+" ID: "+_nextObjectID);
				GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
			}
			_nextObjectID--;
		}
	}
	
	public void spawnNewGroup(boolean timer,int cellID,String groupData,String condition) {
		MobGroup group = new MobGroup(_nextObjectID, cellID, groupData);
		if(group.getMobs().isEmpty())return;
		_mobGroups.put(_nextObjectID, group);
		group.setCondition(condition);
		group.setIsFix(false);
		
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Groupe de monstres ajoutes sur la map: "+_id+" ID: "+_nextObjectID);
		
		GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
		_nextObjectID--;
		
		if(timer)
			group.startCondTimer();
	}
	
	public void spawnGroupOnCommand(int cellID,String groupData) {
		MobGroup group = new MobGroup(_nextObjectID, cellID, groupData);
		if(group.getMobs().isEmpty())return;
		_mobGroups.put(_nextObjectID, group);
		group.setIsFix(false);

		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Groupe de monstres ajoutes sur la map: "+_id+" ID: "+_nextObjectID);

		GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
		_nextObjectID--;
	}
	
	public void addStaticGroup(int cellID,String groupData) {
		MobGroup group = new MobGroup(_nextObjectID,cellID,groupData);
		if(group.getMobs().isEmpty())return;
		_mobGroups.put(_nextObjectID, group);
		_nextObjectID--;
		_fixMobGroups.put(-1000+_nextObjectID, group);
		GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
	}
	
	public void setPlaces(String place)
	{
		_placesStr = place;
	}

	public void removeFight(int id)
	{
		_fights.remove(id);
	}

	public NPC getNPC(int id)
	{
		return _npcs.get(id);
	}
	
	public NPC RemoveNPC(int id)
	{
		return _npcs.remove(id);
	}
	
	public Case getMapa(int id)
	{
		return _cases.get(id);
	}
	
	public ArrayList<Personaje> getPersos() {
		ArrayList<Personaje> persos = new ArrayList<>();
		for(Case c : _cases.values())
			persos.addAll(c.getPersos().values());
		return persos;
	}
	public short getID() {
		return _id;
	}

	public String get_date() {
		return _date;
	}

	public byte get_w() {
		return _w;
	}

	public byte get_h() {
		return _h;
	}

	public String get_key() {
		return _key;
	}

	public String getEsquemaPelea() { return _placesStr; }

	public void addPlayer(Personaje perso) {
		GestorSalida.ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(this,perso);
		perso.getActualCelda().addPerso(perso);
	}

	public String getGMsPackets() {
		StringBuilder packet = new StringBuilder();
		for(Case cell : _cases.values())for(Personaje perso : cell.getPersos().values())packet.append("GM|+").append(perso.parseToGM()).append('\u0000');
		return packet.toString();
	}

	public String getFightersGMsPackets() {
		StringBuilder packet = new StringBuilder();
		for(Entry<Integer,Case> cell : _cases.entrySet())
		{
			for(Entry<Integer, Peleador> f : cell.getValue().getFighters().entrySet())
			{
				packet.append(f.getValue().getGmPacket('+')).append('\u0000');
			}
		}
		return packet.toString();
	}

	public String getMobGroupGMsPackets() {
		if(_mobGroups.isEmpty())return "";
		
		StringBuilder packet = new StringBuilder();
		packet.append("GM|");
		boolean isFirst = true;
		for(MobGroup entry : _mobGroups.values()) {
			String GM = entry.parseGM();
			if(GM.equals(""))continue;
			
			if(!isFirst)
				packet.append("|");
			
			packet.append(GM);
			isFirst = false;
		}
		//System.out.println(packet.toString());
		return packet.toString();
	}
	
	public String getNpcsGMsPackets(Personaje p) {
		if(_npcs.isEmpty())return "";
		
		StringBuilder packet = new StringBuilder();
		packet.append("GM|");
		boolean isFirst = true;
		for(Entry<Integer,NPC> entry : _npcs.entrySet()) {
			String GM = entry.getValue().parseGM(p);
			if(GM.equals(""))continue;
			
			if(!isFirst)
				packet.append("|");
			
			packet.append(GM);
			isFirst = false;
		}
		return packet.toString();
	}
	
	public String getObjectsGDsPackets() {
		StringBuilder toreturn = new StringBuilder();
		boolean first = true;
		for(Entry<Integer,Case> entry : _cases.entrySet()) {
			if(entry.getValue().getObject() != null) {
				if(!first)toreturn.append((char)0x00);
				first = false;
				int cellID = entry.getValue().getID();
				InteractiveObject object = entry.getValue().getObject();
				toreturn.append("GDF|").append(cellID).append(";").append(object.getState()).append(";").append((object.isInteractive()?"1":"0"));
			}
		}
		return toreturn.toString();
	}
	
	public int getNbrFight()
	{
		return _fights.size();
	}
	
	public Map<Integer, Pelea> get_fights() {
		return _fights;
	}

	public Pelea newFight(Personaje init1, Personaje init2, int type) {
		int id = 1;
		if(!_fights.isEmpty())
			id = ((Integer)(_fights.keySet().toArray()[_fights.size()-1]))+1;
		
		Pelea f = new Pelea(type,id,this,init1,init2);
		_fights.put(id,f);
		GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
		return f;
	}
	
	public int getRandomFreeCellID() {
		ArrayList<Integer> freecell = new ArrayList<>();
		for(Entry<Integer,Case> entry : _cases.entrySet()) {
			//Si la case n'est pas marchable
			if(!entry.getValue().isCaminable(true))continue;
			//Si la case est prise par un groupe de monstre
			boolean ok = true;
			for(Entry<Integer,MobGroup> mgEntry : _mobGroups.entrySet()) {
				if (mgEntry.getValue().getCeldaID() == entry.getValue().getID()) {
					ok = false;
					break;
				}
			}
			if(!ok)continue;
			//Si la case est prise par un npc
			ok = true;
			for(Entry<Integer,NPC> npcEntry : _npcs.entrySet()) {
				if (npcEntry.getValue().getCeldaID() == entry.getValue().getID()) {
					ok = false;
					break;
				}
			}
			if(!ok)continue;
			//Si la case est prise par un joueur
			if(!entry.getValue().getPersos().isEmpty())continue;
			//Sinon
			freecell.add(entry.getValue().getID());
		}
		if(freecell.isEmpty()) {
			JuegoServidor.agregar_a_los_logs("Aucune cellulle libre n'a ete trouve sur la map "+_id+" : groupe non spawn");
			return -1;
		}
		int rand = Formulas.getRandomValue(0, freecell.size()-1);
		return freecell.get(rand);
		/*
		int max =  _cases.size()-_w;
		int rand = 0;
		int lim = 0;
		boolean isOccuped;
		
		do
		{
			isOccuped = false;
			rand = Formulas.getRandomValue(_w,max);
			if(lim >50)
				return 0;
			for(Entry<Integer,MobGroup> group : _mobGroups.entrySet())
			{
				if (group.getValue().getCellID() != 0)
				{
					if(group.getValue().getCellID() == _cases.get(_cases.keySet().toArray()[rand]).getID())
						isOccuped = true;
				}
			}
			for(Entry<Integer,NPC> npc : _npcs.entrySet())
			{
				if(npc.getValue().get_cellID() == _cases.get(_cases.keySet().toArray()[rand]).getID())
					isOccuped = true;
			}
			
			if (_cases.get(_cases.keySet().toArray()[rand]).isWalkable() && !isOccuped)
			{
				return _cases.get(_cases.keySet().toArray()[rand]).getID();
			}
			
			lim++;
		}while(!_cases.get(_cases.keySet().toArray()[rand]).isWalkable() && !isOccuped);
		
		return 0;
		//*/
	}

	public int getRandomNearFreeCellId(int cellid){ //obtenir une cell al?atoire et proche
		ArrayList<Integer> freecell = new ArrayList<>();
		ArrayList<Integer> cases = new ArrayList<>();
		cases.add((cellid + 1));
		cases.add((cellid - 1));
		cases.add((cellid + 2));
		cases.add((cellid - 2));
		cases.add((cellid + 14));
		cases.add((cellid - 14));
		cases.add((cellid + 15));
		cases.add((cellid - 15));
		cases.add((cellid + 16));
		cases.add((cellid - 16));
		cases.add((cellid + 27));
		cases.add((cellid - 27));
		cases.add((cellid + 28));
		cases.add((cellid - 28));
		cases.add((cellid + 29));
		cases.add((cellid - 29));
		cases.add((cellid + 30));
		cases.add((cellid - 30));
		cases.add((cellid + 31));
		cases.add((cellid - 31));
		cases.add((cellid + 42));
		cases.add((cellid - 42));
		cases.add((cellid + 43));
		cases.add((cellid - 43));
		cases.add((cellid + 44));
		cases.add((cellid - 44));
		cases.add((cellid + 45));
		cases.add((cellid - 45));
		cases.add((cellid + 57));
		cases.add((cellid - 57));
		cases.add((cellid + 58));
		cases.add((cellid - 58));
		cases.add((cellid + 59));
		cases.add((cellid - 59));

		for (int entry : cases) {
			Case gameCase = this.getMapa(entry);
			if (gameCase == null)
				continue;
			if(gameCase.getOnCellStopAction())
				continue;
			//Si la case n'est pas marchable
			if (!gameCase.isCaminable(true))
				continue;
			//Si la case est prise par un groupe de monstre
			boolean ok = true;
			for (Entry<Integer, Monstruo.MobGroup> mgEntry : _mobGroups.entrySet())
				if (mgEntry.getValue().getCeldaID() == gameCase.getID()) {
					ok = false;
					break;
				}
			if (!ok)
				continue;
			//Si la case est prise par un npc
			ok = true;
			for (Entry<Integer, NPC> npcEntry : _npcs.entrySet())
				if (npcEntry.getValue().getCeldaID() == gameCase.getID()) {
					ok = false;
					break;
				}
			if (!ok)
				continue;
			//Si la case est prise par un joueur
			if (!gameCase.getPersos().isEmpty())
				continue;
			//Sinon
			freecell.add(gameCase.getID());
		}
		if (freecell.isEmpty())
			return -1;
		int rand = Formulas.getRandomValue(0, freecell.size() - 1);
		return freecell.get(rand);
	}

	public void refreshSpawns() {
		for(int id : _mobGroups.keySet()) {
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(this, id);
		}
		_mobGroups.clear();
		_mobGroups.putAll(_fixMobGroups);
		for(MobGroup mg : _fixMobGroups.values()) GestorSalida.GAME_SEND_MAP_MOBS_GM_PACKET(this, mg);

		spawnGroup(Constantes.ALIGNEMENT_NEUTRE,_maxGroup,true,-1);//Spawn des groupes d'alignement neutre
		spawnGroup(Constantes.ALIGNEMENT_BONTARIEN,1,true,-1);//Spawn du groupe de gardes bontarien s'il y a
		spawnGroup(Constantes.ALIGNEMENT_BRAKMARIEN,1,true,-1);//Spawn du groupe de gardes brakmarien s'il y a
	}
	
	public void onPlayerArriveOnCell(Personaje perso, int caseID) {
		if(_cases.get(caseID) == null)return;
		Objeto obj = _cases.get(caseID).getDroppedItem();
		if(obj != null)
		{
			if(perso.addObjet(obj, true))
				Mundo.addObjet(obj, true);
			GestorSalida.GAME_SEND_GDO_PACKET_TO_MAP(this,'-',caseID,0,0);
			GestorSalida.GAME_SEND_Ow_PACKET(perso);
			_cases.get(caseID).clearDroppedItem();
		}
		_cases.get(caseID).applyOnCellStopActions(perso);
		
		if(_placesStr.equalsIgnoreCase("|")) return;
		//Si le joueur a changer de map ou ne peut etre aggro
		if(perso.getActualMapa().getID() != _id || !perso.PuedeSerAgredido())return;
		
		for(MobGroup group : _mobGroups.values())
		{
			if(Camino.getDistanceBetween(this,caseID,group.getCeldaID()) <= group.getAggroDistance())//S'il y aggro
			{
				if((group.getAlineacion() == -1 || ((perso.get_align() == 1 || perso.get_align() == 2) && (perso.get_align() != group.getAlineacion()))) && Condiciones.ValidarCondicion(perso, group.getCondition()))
				{
					JuegoServidor.agregar_a_los_logs(perso.getNombre()+" lance un combat contre le groupe "+group.getID()+" sur la map "+_id);
					startFigthVersusMonstres(perso,group);
					return;
				}
			}
		}
	}
	
	public void startFigthVersusMonstres(Personaje perso, MobGroup group) {
		int id = 1;
		if(!_fights.isEmpty())
			id = ((Integer)(_fights.keySet().toArray()[_fights.size()-1]))+1;
		
		if(!group.isFix())_mobGroups.remove(group.getID());
		else GestorSalida.GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(this);
		_fights.put(id, new Pelea(id,this,perso,group));
		GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
	}
	
	public void startFigthVersusPercepteur(Personaje perso, Recaudador perco) {
		int id = 1;
		if(!_fights.isEmpty())
			id = ((Integer)(_fights.keySet().toArray()[_fights.size()-1]))+1;

		_fights.put(id, new Pelea(id,this,perso,perco));
		GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
	}

	public Mapa getMapCopy() {
		Map<Integer,Case> cases = new TreeMap<>();
		
		Mapa map = new Mapa(_id,_date,_w,_h,_key,_placesStr);
		
		for(Entry<Integer,Case> entry : _cases.entrySet())
			cases.put(entry.getKey(),
					new Case(
							map,
							entry.getValue().getID(),
							entry.getValue().isCaminable(false),
							entry.getValue().isLoS(),
							(entry.getValue().getObject()==null?-1:entry.getValue().getObject().getID())
							)
						);
		map.setCases(cases);
		return map;
	}

	private void setCases(Map<Integer, Case> cases)
	{
		_cases = cases;
	}

	public InteractiveObject getMountParkDoor() {
		for(Case c : _cases.values())
		{
			if(c.getObject() == null)continue;
			//Si enclose
			if(c.getObject().getID() == 6763
			|| c.getObject().getID() == 6766
			|| c.getObject().getID() == 6767
			|| c.getObject().getID() == 6772)
				return c.getObject();

		}
		return null;
	}

	public Map<Integer, MobGroup> getMobGroups()
	{
		return _mobGroups;
	}

	public void removeNpcOrMobGroup(int id) {
		_npcs.remove(id);
		_mobGroups.remove(id);
	}

	public int getMaxGroupNumb()
	{
		return _maxGroup;
	}

	public void setMaxGroup(byte id)
	{
		_maxGroup = id;
	}

	public Pelea getFight(int id)
	{
		return _fights.get(id);
	}

	public void sendFloorItems(Personaje perso) {
		for(Case c : _cases.values())
		{
			if(c.getDroppedItem() != null)
			GestorSalida.GAME_SEND_GDO_PACKET(perso,'+',c.getID(),c.getDroppedItem().getTemplate().getID(),0);
		}
	}

	public Map<Integer, Case> GetCases() {
		 return _cases;
	}
	
	public int getStoreCount()
	{
		return (Mundo.getSeller(getID()) == null?0: Mundo.getSeller(getID()).size());
	}
}