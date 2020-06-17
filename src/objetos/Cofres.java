package objetos;

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


/** Adlesne **/

public class Cofres {
	
	private int _id;
	private int _house_id;
	private short _mapid;
	private int _cellid;
	private Map<Integer, Objeto> _object = new TreeMap<Integer, Objeto>();
	private long _kamas;
	private String _key;
	private int _owner_id;
	
	public Cofres(int id, int house_id, short mapid, int cellid, String object, long kamas, String key, int owner_id)
	{
		_id = id;
		_house_id = house_id;
		_mapid = mapid;
		_cellid = cellid;
		
		for(String item : object.split("\\|"))
		{
			if(item.equals(""))continue;
			String[] infos = item.split(":");
			int guid = Integer.parseInt(infos[0]);

			Objeto obj = Mundo.getObjet(guid);
			if( obj == null)continue;
			_object.put(obj.getGuid(), obj);
		}

		_kamas = kamas;
		_key = key;
		_owner_id = owner_id;
	}
	
	public int get_id()
	{
		return _id;
	}
	
    public int get_house_id()
    {
            return _house_id;
    }
	
	public int get_mapid()
	{
		return _mapid;
	}
	
	public int get_cellid()
	{
		return _cellid;
	}
	
	public Map<Integer, Objeto> get_object()
	{
		return _object;
	}
	
	public long get_kamas()
	{
		return _kamas;
	}
	
	public void set_kamas(long kamas)
	{
		_kamas = kamas;
	}
	
	public String get_key()
	{
		return _key;
	}
	
	public void set_key(String key)
	{
		_key = key;
	}
	
	public int get_owner_id()
	{
		return _owner_id;
	}
	
	public void set_owner_id(int owner_id)
	{
		_owner_id = owner_id;
	}
	
	public void Lock(Jugador P)
	{
		GestorSalida.GAME_SEND_KODE(P, "CK1|8");
	}
	
	public static Cofres get_trunk_id_by_coord(int map_id, int cell_id)
	{
		for(Entry<Integer, Cofres> trunk : Mundo.getTrunks().entrySet())
		{
			if(trunk.getValue().get_mapid() == map_id && trunk.getValue().get_cellid() == cell_id)
			{
				return trunk.getValue();
			}
		}
		return null;
	}
	
	public static void LockTrunk(Jugador P, String packet)
	{
		Cofres t = P.getInTrunk();
		if(t == null) return;
		if(t.isTrunk(P, t))
		{
			GestorSQL.cofre_codigo(P, t, packet);//Change le code
			t.set_key(packet);
			closeCode(P);
		}else
		{
			closeCode(P);
		}
		P.setInTrunk(null);
		return;
	}
	
	public void HopIn(Jugador P)//Ouvrir coffre
	{
		// En gros si il fait quelque chose :)
		if(P.get_fight() != null ||
		   P.get_isTalkingWith() != 0 ||
		   P.get_isTradingWith() != 0 ||
		   P.getCurJobAction() != null ||
		   P.get_curExchange() != null)
		{
			return;
		}
		
		Cofres t = P.getInTrunk();
		House h = Mundo.getHouse(_house_id);
		
		if(t == null) return;
		if(t.get_owner_id() == P.getAccID() || (P.get_guild() == null ? false : P.get_guild().get_id() == h.get_guild_id() && h.canDo(Constantes.C_GNOCODE)))
		{
			OpenTrunk(P, "-", true);
		}
		else if(P.get_guild() == null && h.canDo(Constantes.C_OCANTOPEN))//si on compare par id �a bug (guild null)
		{
			GestorSalida.GAME_SEND_MESSAGE(P, "Ce coffre ne peut �tre ouvert que par les membres de la guilde !", MainServidor.CONFIG_MOTD_COLOR);
		return;
		}
		else if(t.get_owner_id() > 0)//Une personne autre le poss�de, il faut le code pour rentrer
		{
			GestorSalida.GAME_SEND_KODE(P, "CK0|8");//8 �tant le nombre de chiffre du code
		}
		else if(t.get_owner_id() == 0)//Coffre a personne
		{
			return;
		}else
		{
			return;
		}
	}
	
	public static void OpenTrunk(Jugador P, String packet, boolean isTrunk)//Ouvrir un coffre
	{	
		Cofres t = P.getInTrunk();
		if(t == null) return;
		
		if(packet.compareTo(t.get_key()) == 0 || isTrunk)//Si c'est chez lui ou que le mot de passe est bon
		{
			GestorSalida.GAME_SEND_ECK_PACKET(P.get_compte().getGameThread().get_out(), 5, "");
			GestorSalida.GAME_SEND_EL_TRUNK_PACKET(P, t);
			closeCode(P);
		}
		
		else if(packet.compareTo(t.get_key()) != 0)//Mauvais code
		{
			GestorSalida.GAME_SEND_KODE(P, "KE");
			closeCode(P);
			P.setInTrunk(null);
		}
	}
	
	public static void closeCode(Jugador P)
	{
		GestorSalida.GAME_SEND_KODE(P, "V");
	}
	
	public boolean isTrunk(Jugador P, Cofres t)//Savoir si c'est son coffre
	{
		if(t.get_owner_id() == P.getAccID()) return true;
		else return false;
	}
	
    public static ArrayList<Cofres> getTrunksByHouse(House h)
    {
            ArrayList<Cofres> trunks = new ArrayList<Cofres>();
            for(Entry<Integer, Cofres> trunk : Mundo.getTrunks().entrySet())
            {
                    if(trunk.getValue().get_house_id() == h.get_id())
                    {
                            trunks.add(trunk.getValue());
                    }
            }
           
            return trunks;
    }
    
	public String parseToTrunkPacket()
	{
		StringBuilder packet = new StringBuilder();
		for(Objeto obj : _object.values())
			packet.append("O").append(obj.parseItem()).append(";");
		if(get_kamas() != 0)
			packet.append("G").append(get_kamas());
		return packet.toString();
	}
	
	public void addInTrunk(int guid, int qua, Jugador P)
	{
		if(P.getInTrunk().get_id() != get_id()) return;
		
		if(_object.size() >= 80) // Le plus grand c'est pour si un admin ajoute des objets via la bdd...
		{
			GestorSalida.GAME_SEND_MESSAGE(P, "Le nombre d'objets maximal de ce coffre � �t� atteint !", MainServidor.CONFIG_MOTD_COLOR);
			return;
		}
		
		Objeto PersoObj = Mundo.getObjet(guid);
		if(PersoObj == null) return;
		//Si le joueur n'a pas l'item dans son sac ...
		if(P.getItems().get(guid) == null)
		{
			JuegoServidor.addToLog("Le joueur "+P.get_name()+" a tenter d'ajouter un objet dans un coffre qu'il n'avait pas.");
			return;
		}
		
		String str = "";
		
		//Si c'est un item �quip� ...
		if(PersoObj.getPosition() != Constantes.ITEM_POS_NO_EQUIPED)return;
		
		Objeto TrunkObj = getSimilarTrunkItem(PersoObj);
		int newQua = PersoObj.getQuantity() - qua;
		if(TrunkObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				P.removeItem(PersoObj.getGuid());
				//On met l'objet du sac dans le coffre, avec la meme quantit�
				_object.put(PersoObj.getGuid() ,PersoObj);
				str = "O+"+PersoObj.getGuid()+"|"+PersoObj.getQuantity()+"|"+PersoObj.getTemplate().getID()+"|"+PersoObj.parseStatsString();
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
				
			}
			else//S'il reste des objets au joueur
			{
				//on modifie la quantit� d'item du sac
				PersoObj.setQuantity(newQua);
				//On ajoute l'objet au coffre et au monde
				TrunkObj = Objeto.getCloneObjet(PersoObj, qua);
				Mundo.addObjet(TrunkObj, true);
				_object.put(TrunkObj.getGuid() ,TrunkObj);
				
				//Envoie des packets
				str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				P.removeItem(PersoObj.getGuid());
				//On enleve l'objet du monde
				Mundo.removeItem(PersoObj.getGuid());
				//On ajoute la quantit� a l'objet dans le coffre
				TrunkObj.setQuantity(TrunkObj.getQuantity() + PersoObj.getQuantity());
				//on envoie l'ajout au coffre de l'objet
			    str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				//on envoie la supression de l'objet du sac au joueur
				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(P, guid);
				
			}else //S'il restait des objets
			{
				//on modifie la quantit� d'item du sac
				PersoObj.setQuantity(newQua);
				TrunkObj.setQuantity(TrunkObj.getQuantity() + qua);
				str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				
			}
		}
		
		for(Jugador perso : P.get_curCarte().getPersos())
		{
			if(perso.getInTrunk() != null && get_id() == perso.getInTrunk().get_id())
			{
				GestorSalida.GAME_SEND_EsK_PACKET(perso, str);
			}
		}
		
		GestorSalida.GAME_SEND_Ow_PACKET(P);
		GestorSQL.actualizar_cofre(this);
	}
	
	public void removeFromTrunk(int guid, int qua, Jugador P)
	{
		if(P.getInTrunk().get_id() != get_id()) return;
		
		Objeto TrunkObj = Mundo.getObjet(guid);
		if(TrunkObj == null) return;
		//Si le joueur n'a pas l'item dans son coffre
		if(_object.get(guid) == null)
		{
			JuegoServidor.addToLog("Le joueur "+P.get_name()+" a tenter de retirer un objet dans un coffre qu'il n'avait pas.");
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
				_object.remove(guid);
				//On l'ajoute au joueur
				P.getItems().put(guid, TrunkObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(P,TrunkObj);
				str = "O-"+guid;
				
			}else //S'il reste des objets dans le coffre
			{
				//On cr�e une copy de l'item dans le coffre
				PersoObj = Objeto.getCloneObjet(TrunkObj, qua);
				//On l'ajoute au monde
				Mundo.addObjet(PersoObj, true);
				//On retire X objet du coffre
				TrunkObj.setQuantity(newQua);
				//On l'ajoute au joueur
				P.getItems().put(PersoObj.getGuid(), PersoObj);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OAKO_PACKET(P,PersoObj);
				str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
				
			}
		}
		else
		{
			//S'il ne reste rien dans le coffre
			if(newQua <= 0)
			{
				//On retire l'item du coffre
				_object.remove(TrunkObj.getGuid());
				Mundo.removeItem(TrunkObj.getGuid());
				//On Modifie la quantit� de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + TrunkObj.getQuantity());
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P, PersoObj);
				str = "O-"+guid;
				
			}
			else//S'il reste des objets dans le coffre
			{
				//On retire X objet du coffre
				TrunkObj.setQuantity(newQua);
				//On ajoute X objets au joueurs
				PersoObj.setQuantity(PersoObj.getQuantity() + qua);
				
				//On envoie les packets
				GestorSalida.GAME_SEND_OBJECT_QUANTITY_PACKET(P,PersoObj);
				str = "O+"+TrunkObj.getGuid()+"|"+TrunkObj.getQuantity()+"|"+TrunkObj.getTemplate().getID()+"|"+TrunkObj.parseStatsString();
			}
		}
		
		for(Jugador perso : P.get_curCarte().getPersos())
		{
			if(perso.getInTrunk() != null && get_id() == perso.getInTrunk().get_id())
			{
				GestorSalida.GAME_SEND_EsK_PACKET(perso, str);
			}
		}
		
		GestorSalida.GAME_SEND_Ow_PACKET(P);
		GestorSQL.actualizar_cofre(this);
	}
	
	private Objeto getSimilarTrunkItem(Objeto obj)
	{
		for(Objeto value : _object.values())
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
		for(Entry<Integer, Objeto> entry : _object.entrySet())
		{
			Objeto obj = entry.getValue();
			str.append(obj.getGuid()).append("|");
		}
		return str.toString();
	}
	
	public void purgeTrunk()
	{
		for(Entry<Integer, Objeto> obj : get_object().entrySet())
		{
			Mundo.removeItem(obj.getKey());
		}
		get_object().clear();
	}
	
	public void moveTrunktoBank(Cuenta Cbank)
	{
		for(Entry<Integer, Objeto> obj : get_object().entrySet())
		{
			Cbank.getBank().put(obj.getKey(), obj.getValue());
		}
		get_object().clear();
	}
}