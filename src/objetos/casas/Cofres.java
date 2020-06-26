package objetos.casas;

import juego.JuegoServidor;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import comunes.MainServidor;
import comunes.Constantes;
import comunes.GestorSQL;
import comunes.GestorSalida;
import comunes.Mundo;
import objetos.Accion;
import objetos.Cuenta;
import objetos.Objeto;
import objetos.Personaje;

public class Cofres {
	
	private final int _id;
	private final int _casa;
	private final short _mapa;
	private final int _celda;
	private final Map<Integer, Objeto> _objetos = new TreeMap<>();
	private long _kamas;
	private String _llave;
	private int _dueño;
	private Personaje _personaje = null;
	
	public Cofres(int id, int casa, short mapa, int celda, String objeto, long kamas, String llave, int dueño) {
		_id = id;
		_casa = casa;
		_mapa = mapa;
		_celda = celda;
		
		for(String objetos : objeto.split("\\|")) {
			if(objetos.equals(""))continue;
			String[] infos = objetos.split(":");
			int guid = Integer.parseInt(infos[0]);

			Objeto obj = Mundo.getObjet(guid);
			if( obj == null)continue;
			_objetos.put(obj.getID(), obj);
		}

		_kamas = kamas;
		_llave = llave;
		_dueño = dueño;
	}
	
	public int getID()
	{
		return _id;
	}
	
    public int getCasa()
    {
            return _casa;
    }
	
	public int getMapa()
	{
		return _mapa;
	}
	
	public int getCelda()
	{
		return _celda;
	}
	
	public Map<Integer, Objeto> getObjetos()
	{
		return _objetos;
	}
	
	public long getKamas()
	{
		return _kamas;
	}
	
	public void setKamas(long kamas)
	{
		_kamas = kamas;
	}
	
	public String getLlave()
	{
		return _llave;
	}
	
	public void setLlave(String key)
	{
		_llave = key;
	}
	
	public int getDueño()
	{
		return _dueño;
	}
	
	public void setDueño(int owner_id)
	{
		_dueño = owner_id;
	}

	public Personaje getPersonaje() { return _personaje; }

	public void setPersonaje(Personaje personaje) {
		this._personaje = personaje;
	}
	
	public void cerradura(Personaje personaje)
	{
		GestorSalida.EVIAR_CODIGO(personaje, "CK1|8");
	}
	
	public static Cofres getCofrePorCoordenadas(int map_id, int cell_id) {
		for(Entry<Integer, Cofres> trunk : Mundo.getTrunks().entrySet())
			if(trunk.getValue().getMapa() == map_id && trunk.getValue().getCelda() == cell_id)
				return trunk.getValue();
		return null;
	}
	
	public static void BloquearCofre(Personaje personaje, String packet) {
		Cofres cofre = personaje.getEnCofre();
		if(cofre == null) return;
		if(cofre.isCofre(personaje, cofre)) {
			GestorSQL.cofre_codigo(personaje, cofre, packet); //Cambiar el codigo
			cofre.setLlave(packet);
			closeCode(personaje);
		} else {
			closeCode(personaje);
		}
		personaje.setEnCofre(null);
	}

	public void EntrarEnCofre(Personaje player) {
		if (player.getPelea() != null || player.getExchangeAction() != null)
			return;

		Casas house = Mundo.getHouse(getCasa());

		if(house.get_dueño() == player.getAccID() && this.getDueño() != player.getAccID())
			this.setDueño(player.getAccID());
		if (this.getDueño() == player.getAccID() ||(player.getActualGrupo() != null)|| (player.get_guild() != null && player.get_guild().get_id() == house.get_gremio() && house.canDo(Constantes.C_GNOCODE))) {
			player.setExchangeAction(new Accion.AccionIntercambiar<>(Accion.AccionIntercambiar.IN_TRUNK, this));
			AbrirCofre(player, "-", true);
		} else if (player.get_guild() == null && house.canDo(Constantes.C_OCANTOPEN))
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(player, "1244;"); //Enviar ERROR_244 - LANG
		else if (this.getDueño() > 0)
			GestorSalida.EVIAR_CODIGO(player, "CK0|8");
	}

	public static void AbrirCofre(Personaje P, String packet, boolean isTrunk) {//Ouvrir un coffre
		Cofres t = (Cofres) P.getExchangeAction().getValue();
		if (t == null)
			return;
		if (packet.compareTo(t.getLlave()) == 0 || isTrunk)//Si c'est chez lui ou que le mot de passe est bon
		{
			t._personaje = P;
			GestorSalida.GAME_SEND_ECK_PACKET(P.getCuenta().getJuegoThread().get_out(), 5, "");
			GestorSalida.ENVIAR_PAQUETE_COFRE(P, t);
			closeCode(P);
			P.setExchangeAction(new Accion.AccionIntercambiar<>(Accion.AccionIntercambiar.IN_TRUNK, t));
		} else if (packet.compareTo(t.getLlave()) != 0)//Mauvais code
		{
			GestorSalida.EVIAR_CODIGO(P, "KE");
			closeCode(P);
			P.setExchangeAction(null);
		}
	}
	
	public static void closeCode(Personaje P) {
		GestorSalida.EVIAR_CODIGO(P, "V");
	}
	
	public boolean isCofre(Personaje P, Cofres t) { //Saber si es un cofre
		if(t.getDueño() == P.getAccID()) return true;
		else return false;
	}
	
    public static ArrayList<Cofres> getTrunksByHouse(Casas h) {
            ArrayList<Cofres> trunks = new ArrayList<>();
            for(Entry<Integer, Cofres> trunk : Mundo.getTrunks().entrySet()) {
                    if(trunk.getValue().getCasa() == h.getID()) {
                            trunks.add(trunk.getValue());
                    }
            }
            return trunks;
    }
    
	public String parseToTrunkPacket() {
		StringBuilder paquete = new StringBuilder();
		for(Objeto objetos : _objetos.values())
			paquete.append("O").append(objetos.parseItem()).append(";");
		if(getKamas() != 0)
			paquete.append("G").append(getKamas());
		return paquete.toString();
	}
	
	public void addInTrunk(int guid, int qua, Personaje P) {
		if(P.getEnCofre().getID() != getID()) return;
		
		if(_objetos.size() >= 80) // Le plus grand c'est pour si un admin ajoute des objets via la bdd...
		{
			GestorSalida.GAME_SEND_MESSAGE(P, "Le nombre d'objets maximal de ce coffre à été atteint !", MainServidor.CONFIG_MOTD_COLOR);
			return;
		}
		
		Objeto PersoObj = Mundo.getObjet(guid);
		if(PersoObj == null) return;
		//Si le joueur n'a pas l'item dans son sac ...
		if(P.getItems().get(guid) == null)
		{
			JuegoServidor.agregar_a_los_logs("Le joueur "+P.getNombre()+" a tenter d'ajouter un objet dans un coffre qu'il n'avait pas.");
			return;
		}
		
		String str = "";
		
		//Si c'est un item équipé ...
		if(PersoObj.getPosition() != Constantes.ITEM_POS_NO_EQUIPED)return;
		
		Objeto TrunkObj = getSimilarTrunkItem(PersoObj);
		int newQua = PersoObj.getQuantity() - qua;
		if(TrunkObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0) {
				//On enleve l'objet du sac du joueur
				P.removeItem(PersoObj.getID());
				//On met l'objet du sac dans le coffre, avec la meme quantité
				_objetos.put(PersoObj.getID() ,PersoObj);
				str = "O+"+PersoObj.getID()+"|"+PersoObj.getQuantity()+"|"+PersoObj.getTemplate().getID()+"|"+PersoObj.parseStatsString();
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
				
			} else//S'il reste des objets au joueur
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				//On ajoute l'objet au coffre et au monde
				TrunkObj = Objeto.getCloneObjet(PersoObj, qua);
				Mundo.addObjet(TrunkObj, true);
				_objetos.put(TrunkObj.getID() ,TrunkObj);
				
				//Envoie des packets
				str = "O+"+TrunkObj.getID()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				P.removeItem(PersoObj.getID());
				//On enleve l'objet du monde
				Mundo.removeItem(PersoObj.getID());
				//On ajoute la quantité a l'objet dans le coffre
				TrunkObj.setQuantity(TrunkObj.getQuantity() + PersoObj.getQuantity());
				//on envoie l'ajout au coffre de l'objet
			    str = "O+"+TrunkObj.getID()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				//on envoie la supression de l'objet du sac au joueur
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
				
			}else //S'il restait des objets
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				TrunkObj.setQuantity(TrunkObj.getQuantity() + qua);
				str = "O+"+TrunkObj.getID()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				
			}
		}
		
		for(Personaje perso : P.getActualMapa().getPersos())
		{
			if(perso.getEnCofre() != null && getID() == perso.getEnCofre().getID())
			{
				GestorSalida.GAME_SEND_EsK_PACKET(perso, str);
			}
		}
		
		GestorSalida.GAME_SEND_Ow_PACKET(P);
		GestorSQL.actualizar_cofre(this);
	}
	
	public void removeFromTrunk(int guid, int qua, Personaje P)
	{
		if(P.getEnCofre().getID() != getID()) return;
		
		Objeto TrunkObj = Mundo.getObjet(guid);
		if(TrunkObj == null) return;
		//Si le joueur n'a pas l'item dans son coffre
		if(_objetos.get(guid) == null)
		{
			JuegoServidor.agregar_a_los_logs("Le joueur "+P.getNombre()+" a tenter de retirer un objet dans un coffre qu'il n'avait pas.");
			return;
		}
		
		Objeto PersoObj = P.getSimilarItem(TrunkObj);
		
		String str = "";
		
		int newQua = TrunkObj.getQuantity() - qua;
		
		if(PersoObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien dans le coffre
			if(newQua <= 0)
			{
				//On retire l'item du coffre
				_objetos.remove(guid);
				//On l'ajoute au joueur
				P.getItems().put(guid, TrunkObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(P,TrunkObj);
				str = "O-"+guid;
				
			}else //S'il reste des objets dans le coffre
			{
				//On crée une copy de l'item dans le coffre
				PersoObj = Objeto.getCloneObjet(TrunkObj, qua);
				//On l'ajoute au joueur
				P.getItems().put(PersoObj.getID(), PersoObj);
				//On l'ajoute au monde
				Mundo.addObjet(PersoObj, true);
				//On retire X objet du coffre
				TrunkObj.setQuantity(newQua);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(P,PersoObj);
				str = "O+"+TrunkObj.getID()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				
			}
		} else {
			//S'il ne reste rien dans le coffre
			if(newQua <= 0) {
				//On retire l'item du coffre
				_objetos.remove(TrunkObj.getID());
				Mundo.removeItem(TrunkObj.getID());
				//On Modifie la quantité de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + TrunkObj.getQuantity());
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				str = "O-"+guid;
				
			} else { //S'il reste des objets dans le coffre
				//On retire X objet du coffre
				TrunkObj.setQuantity(newQua);
				//On ajoute X objets au joueurs
				PersoObj.setQuantity(PersoObj.getQuantity() + qua);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P,PersoObj);
				str = "O+"+TrunkObj.getID()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
			}
		}
		
		for(Personaje perso : P.getActualMapa().getPersos()) {
			if(perso.getEnCofre() != null && getID() == perso.getEnCofre().getID()) {
				GestorSalida.GAME_SEND_EsK_PACKET(perso, str);
			}
		}
		
		GestorSalida.GAME_SEND_Ow_PACKET(P);
		GestorSQL.actualizar_cofre(this);
	}
	
	private Objeto getSimilarTrunkItem(Objeto obj)
	{
		for(Objeto value : _objetos.values())
		{
			if(value.getTemplate().getType() == 85)
				continue;
			if(value.getTemplate().getID() == obj.getTemplate().getID() && value.getStats().isSameStats(obj.getStats()))
				return value;
		}
		return null;
	}
	
	public String parseTrunkObjetsToDB()
	{
		StringBuilder str = new StringBuilder();
		for(Entry<Integer, Objeto> entry : _objetos.entrySet())
		{
			Objeto obj = entry.getValue();
			str.append(obj.getID()).append("|");
		}
		return str.toString();
	}
	
	public void purgeTrunk()
	{
		for(Entry<Integer, Objeto> obj : getObjetos().entrySet())
		{
			Mundo.removeItem(obj.getKey());
		}
		getObjetos().clear();
	}
	
	public void moveTrunktoBank(Cuenta Cbank)
	{
		for(Entry<Integer, Objeto> obj : getObjetos().entrySet())
		{
			Cbank.getBank().put(obj.getKey(), obj.getValue());
		}
		getObjetos().clear();
	}
}