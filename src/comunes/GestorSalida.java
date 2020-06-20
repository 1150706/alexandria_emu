package comunes;

import juego.JuegoServidor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import comunes.Mundo.ItemSet;

import objetos.Mapa;
import objetos.Dragopavo;
import objetos.Gremio;
import objetos.Mercadillo;
import objetos.Objeto;
import objetos.Recaudador;
import objetos.Personaje;
import objetos.Pelea;
import objetos.Mapa.Case;
import objetos.Mapa.InteractiveObject;
import objetos.Mapa.MountPark;
import objetos.Pelea.Fighter;
import objetos.Gremio.GuildMember;
import objetos.Mercadillo.HdvEntry;
import objetos.Oficio.StatsMetier;
import objetos.Monstruo.MobGroup;
import objetos.NPCModelo.NPC;
import objetos.Objeto.ObjTemplate;
import objetos.Personaje.Grupo;
import objetos.casas.Cofres;

import realm.RealmServer;

public class GestorSalida {
	
	public static void send(Personaje p, String packet) {
		if(p == null || p.getCuenta() == null)return;
		if(p.getCuenta().getGameThread() == null)return;
		PrintWriter out = p.getCuenta().getGameThread().get_out();
		if(out != null && !packet.equals("") && !packet.equals(""+(char)0x00)) {
			packet = GestorEncriptador.toUtf(packet);
			if(MainServidor.CONFIG_SOCKET_USE_COMPACT_DATA) {
				GestorEnvio.send(out, packet);
			}else {
				out.print((packet)+(char)0x00);
				out.flush();
			}
		}
	}
	
	public static void send(PrintWriter out, String packet) {
		if(out != null && !packet.equals("") && !packet.equals(""+(char)0x00)) {
			packet = GestorEncriptador.toUtf(packet);
			if(MainServidor.CONFIG_SOCKET_USE_COMPACT_DATA) {
				GestorEnvio.send(out, packet);
			}else {
				out.print((packet)+(char)0x00);
				out.flush();
			}
		}
	}
	
	public static String REALM_SEND_HC_PACKET(PrintWriter out) {
		
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		StringBuilder hashkey = new StringBuilder();
		
        Random rand = new Random();
        
        for (int i=0; i<32; i++) {
               hashkey.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        }
        String packet = "HC"+hashkey;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Realm: Send>>"+packet);
		return hashkey.toString();
	}
	
	public static void REALM_SEND_REQUIRED_VERSION(PrintWriter out) {
		String packet = "AlEv" + Constantes.CLIENT_VERSION;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Conn: Send>>"+packet);
	}
	
	public static void REALM_SEND_LOGIN_ERROR(PrintWriter out) {
		String packet = "AlEf";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Conn: Send>>"+packet);
	}

	public static void MULTI_SEND_Af_PACKET(PrintWriter out,int position, int totalAbo, int totalNonAbo, String subscribe, int queueID) {
		StringBuilder packet = new StringBuilder();
		packet.append("Af").append(position).append("|").append(totalAbo).append("|").append(totalNonAbo).append("|").append(subscribe).append("|").append(queueID);
		send(out,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Serv: Send>>"+packet.toString());
	}

	public static void REALM_SEND_Ad_Ac_AH_AlK_AQ_PACKETS(PrintWriter out, String pseudo, int level, String question) {
		StringBuilder packet = new StringBuilder();
		packet.append("Ad").append(pseudo).append((char)0x00);
		packet.append("Ac0").append((char)0x00);
		//AH[ID];[State];[Completion];[CanLog]
		packet.append("AH1;").append(Mundo.get_state()).append(";110;1").append((char)0x00);
		packet.append("AlK").append(level).append((char)0x00);
		packet.append("AQ").append(question.replace(" ", "+"));
		
		send(out,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Conn: Send>>"+packet.toString());
	}

	public static void REALM_SEND_BANNED(PrintWriter out) {
		String packet = "AlEb";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Conn: Send>>"+packet);
	}

	public static void REALM_SEND_ALREADY_CONNECTED(PrintWriter out) {
		String packet = "AlEc";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Conn: Send>>"+packet);	
	}

	public static void REALM_SEND_POLICY_FILE(PrintWriter out) {
		String packet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +   
    		"<cross-domain-policy>"+  
    	    "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\" />"+  
    	    "<site-control permitted-cross-domain-policies=\"master-only\" />"+  
    	    "</cross-domain-policy>";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void REALM_SEND_PERSO_LIST(PrintWriter out, int number) {
		String packet = "AxK31536000000";//Temps d'abonnement
		if(number>0)
			packet+= "|1," + number;//ServeurID
		
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Conn: Send>>"+packet);	
	}
	
	public static void REALM_SEND_GAME_SERVER_IP(PrintWriter out,int guid,boolean isHost) {
		String packet = "A";
		if(MainServidor.USAR_IP) {
			String ip = MainServidor.CONFIG_IP_LOOPBACK && isHost? GestorEncriptador.CryptIP("127.0.0.1")+ GestorEncriptador.CryptPort(MainServidor.PUERTO_DE_JUEGO): MainServidor.GAMESERVER_IP;
			packet += "XK"+ip+guid;
		}else {
			String ip = MainServidor.CONFIG_IP_LOOPBACK && isHost?"127.0.0.1": MainServidor.IP;
			packet += "YK"+ip+":"+ MainServidor.PUERTO_DE_JUEGO +";"+guid;
		}
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			RealmServer.addToSockLog("Conn: Send>>"+packet);
	}

	public static void GAME_SEND_HELLOGAME_PACKET(PrintWriter out) {
		String packet = "HG";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ATTRIBUTE_FAILED(PrintWriter out)
	{
		String packet = "ATE";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ATTRIBUTE_SUCCESS(PrintWriter out) {
		String packet = "ATK0";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_AV0(PrintWriter out) {
		String packet = "AV0";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_HIDE_GENERATE_NAME(PrintWriter out) {
		String packet = "APE2";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_PERSO_LIST(PrintWriter out, Map<Integer, Personaje> persos) {
		StringBuilder packet = new StringBuilder();
		packet.append("ALK31536000000|").append(persos.size());
		for(Entry<Integer, Personaje> entry : persos.entrySet()) {
			packet.append(entry.getValue().parseALK());
		}
		send(out,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}

	public static void GAME_SEND_NAME_ALREADY_EXIST(PrintWriter out) {
		String packet = "AAEa";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
		JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_CREATE_PERSO_FULL(PrintWriter out) {
		String packet = "AAEf";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_CREATE_OK(PrintWriter out) {
		String packet = "AAK";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_DELETE_PERSO_FAILED(PrintWriter out) {
		String packet = "ADE";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_CREATE_FAILED(PrintWriter out) {
		String packet = "AAEF";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);

	}

	public static void GAME_SEND_PERSO_SELECTION_FAILED(PrintWriter out) {
		String packet = "ASE";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void ENVIAR_PAQUETE_CARACTERISTICAS(Personaje perso) {
        String packet = perso.getAsPacket();
        send(perso, packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_Rx_PACKET(Personaje out) {
		String packet = "Rx"+out.getMountXpGive();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_Rn_PACKET(Personaje out, String name) {
		String packet = "Rn"+name;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_Re_PACKET(Personaje out, String sign, Dragopavo DD) {
		String packet = "Re"+sign;
		if(sign.equals("+"))packet += DD.parse();
		
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ASK(PrintWriter out, Personaje perso) {
		StringBuilder packet = new StringBuilder();
		packet.append("ASK|").append(perso.get_GUID()).append("|").append(perso.getNombre()).append("|");
		packet.append(perso.get_lvl()).append("|").append(perso.getClase()).append("|").append(perso.getSexo());
		packet.append("|").append(perso.get_gfxID()).append("|").append((perso.get_color1()==-1?"-1":Integer.toHexString(perso.get_color1())));
		packet.append("|").append((perso.get_color2()==-1?"-1":Integer.toHexString(perso.get_color2()))).append("|");
		packet.append((perso.get_color3()==-1?"-1":Integer.toHexString(perso.get_color3()))).append("|");
		packet.append(perso.parseItemToASK());
		
		send(out,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ALIGNEMENT(PrintWriter out,int alliID) {
		String packet = "ZS"+alliID;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ADD_CANAL(PrintWriter out, String chans) {
		String packet = "cC+"+chans;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ZONE_ALLIGN_STATUT(PrintWriter out) {
		String packet = "al|"+ Mundo.getSousZoneStateString();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_SEESPELL_OPTION(PrintWriter out, boolean spells) {
		String packet = "SLo"+(spells?"+":"-");
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_RESTRICTIONS(PrintWriter out) {
		String packet =  "AR6bk";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_Ow_PACKET(Personaje perso) {
		String packet =  "Ow"+perso.getPodUsed()+"|"+perso.getMaxPod();
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_OT_PACKET(PrintWriter out, int id) {
		String packet =  "OT";
		if(id > 0) packet += id;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_SEE_FRIEND_CONNEXION(PrintWriter out,boolean see) {
		String packet = "FO"+(see?"+":"-");
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_GAME_CREATE(PrintWriter out, String _name) {
		String packet = "GCK|1|"+_name;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_SERVER_HOUR(PrintWriter out) {
		String packet = JuegoServidor.getServerTime();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_SERVER_DATE(PrintWriter out) {
		String packet = JuegoServidor.getServerDate();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_MAPDATA(PrintWriter out, int id, String date,String key) {
		String packet = "GDM|"+id+"|"+date+"|"+key;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_GDK_PACKET(PrintWriter out) {
		String packet = "GDK";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_MAP_MOBS_GMS_PACKETS(PrintWriter out, Mapa carte) {
		String packet = carte.getMobGroupGMsPackets();
		if(packet.equals(""))return;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_MAP_OBJECTS_GDS_PACKETS(PrintWriter out, Mapa carte) {
		String packet = carte.getObjectsGDsPackets();
		if(packet.equals(""))return;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_MAP_NPCS_GMS_PACKETS(Personaje p, Mapa carte) {
		String packet = carte.getNpcsGMsPackets(p);
		if(packet.equals(""))return;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_MAP_PERCO_GMS_PACKETS(PrintWriter out, Mapa carte) {
		String packet = Recaudador.parseGM(carte);
		if(packet.length() < 5)return;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_MAP_GMS_PACKETS(PrintWriter out, Mapa carte) {
		String packet = carte.getGMsPackets();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ERASE_ON_MAP_TO_MAP(Mapa map, int guid) {
		String packet = "GM|-"+guid;
		for(Personaje z : map.getPersos()) {
			if(z.getCuenta().getGameThread() == null)continue;
			send(z.getCuenta().getGameThread().get_out(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map "+map.getID()+": Send>>"+packet);
	}
	
	public static void GAME_SEND_ERASE_ON_MAP_TO_FIGHT(Pelea f, int guid) {
		String packet = "GM|-"+guid;
		for(int z=0;z < f.getFighters(1).size();z++) {
			if(f.getFighters(1).get(z).getPersonnage().getCuenta().getGameThread() == null)continue;
			send(f.getFighters(1).get(z).getPersonnage().getCuenta().getGameThread().get_out(),packet);
		}
		for(int z=0;z < f.getFighters(2).size();z++) {
			if(f.getFighters(2).get(z).getPersonnage().getCuenta().getGameThread() == null)continue;
			send(f.getFighters(2).get(z).getPersonnage().getCuenta().getGameThread().get_out(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fighter ID "+f.get_id()+": Send>>"+packet);
	}
	
	public static void GAME_SEND_ON_FIGHTER_KICK(Pelea f, int guid, int team) {
		String packet = "GM|-"+guid;
		for(Fighter F : f.getFighters(team)) {
			if(F.getPersonnage() == null || F.getPersonnage().getCuenta().getGameThread() == null || F.getPersonnage().get_GUID() == guid)continue;
			send(F.getPersonnage().getCuenta().getGameThread().get_out(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fighter ID "+f.get_id()+": Send>>"+packet);
	}
	
	public static void GAME_SEND_ALTER_FIGHTER_MOUNT(Pelea fight, Fighter fighter, int guid, int team, int otherteam) {
		StringBuilder packet = new StringBuilder();
		packet.append("GM|-").append(guid).append((char)0x00).append(fighter.getGmPacket('~'));
		for(Fighter F : fight.getFighters(team)) {
			if(F.getPersonnage() == null || F.getPersonnage().getCuenta().getGameThread() == null || !F.getPersonnage().isConectado())continue;
			send(F.getPersonnage().getCuenta().getGameThread().get_out(),packet.toString());
		}
		if(otherteam > -1) {
			for(Fighter F : fight.getFighters(otherteam)) {
				if(F.getPersonnage() == null || F.getPersonnage().getCuenta().getGameThread() == null || !F.getPersonnage().isConectado())continue;
				send(F.getPersonnage().getCuenta().getGameThread().get_out(),packet.toString());
			}
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight ID "+fight.get_id()+": Send>>"+packet);
	}

	public static void ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(Mapa map, Personaje perso) {
		String packet = "GM|+"+perso.parseToGM();
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map "+map.getID()+": Send>>"+packet);
	}

	public static void GAME_SEND_DUEL_Y_AWAY(PrintWriter out, int guid) {
		String packet = "GA;903;"+guid+";o";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_DUEL_E_AWAY(PrintWriter out, int guid) {
		String packet = "GA;903;"+guid+";z";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_MAP_NEW_DUEL_TO_MAP(Mapa map, int guid, int guid2) {
		String packet = "GA;900;"+guid+";"+guid2;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map "+map.getID()+": Send>>"+packet);
	}
	
	public static void GAME_SEND_CANCEL_DUEL_TO_MAP(Mapa map, int guid, int guid2) {
		String packet = "GA;902;"+guid+";"+guid2;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_MAP_START_DUEL_TO_MAP(Mapa map, int guid, int guid2) {
		String packet = "GA;901;"+guid+";"+guid2;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_MAP_FIGHT_COUNT(PrintWriter out, Mapa map) {
		String packet = "fC"+map.getNbrFight();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(Pelea fight, int teams, int state, int cancelBtn, int duel, int spec, long time, int type) {
		StringBuilder packet = new StringBuilder();
		packet.append("GJK").append(state).append("|");
		packet.append(cancelBtn).append("|").append(duel).append("|");
		packet.append(spec).append("|").append(time).append("|").append(type);
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())
				continue;
			send(f.getPersonnage(),packet.toString());
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(Pelea fight, int teams, String places, int team) {
		String packet = "GP"+places+"|"+team;
		for(Fighter f : fight.getFighters(teams)) {
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(Mapa map) {
		String packet = "fC"+map.getNbrFight();
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(Mapa map, int arg1, int guid1, int guid2, int cell1, String str1, int cell2, String str2) {
		StringBuilder packet = new StringBuilder();
		packet.append("Gc+").append(guid1).append(";").append(arg1).append("|").append(guid1).append(";").append(cell1).append(";").append(str1).append("|").append(guid2).append(";").append(cell2).append(";").append(str2);
		for(Personaje z : map.getPersos()) send(z,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(Personaje p, Mapa map, int arg1, int guid1, int guid2, int cell1, String str1, int cell2, String str2) {
		StringBuilder packet = new StringBuilder();
		packet.append("Gc+").append(guid1).append(";").append(arg1).append("|").append(guid1).append(";").append(cell1).append(";").append(str1).append("|").append(guid2).append(";").append(cell2).append(";").append(str2);
		send(p,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(Mapa map, int guid) {
		String packet = "Gc-"+guid;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(Mapa map, int teamID, Fighter perso) {
		StringBuilder packet = new StringBuilder();
		packet.append("Gt").append(teamID).append("|+").append(perso.getGUID()).append(";").append(perso.getPacketsName()).append(";").append(perso.get_lvl());
		for(Personaje z : map.getPersos()) send(z,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(Personaje p, Mapa map, int teamID, Fighter perso) {
		StringBuilder packet = new StringBuilder();
		packet.append("Gt").append(teamID).append("|+").append(perso.getGUID()).append(";").append(perso.getPacketsName()).append(";").append(perso.get_lvl());
		send(p,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_REMOVE_IN_TEAM_PACKET_TO_MAP(Mapa map, int teamID, Fighter perso) {
		StringBuilder packet = new StringBuilder();
		packet.append("Gt").append(teamID).append("|-").append(perso.getGUID()).append(";").append(perso.getPacketsName()).append(";").append(perso.get_lvl());
		for(Personaje z : map.getPersos()) send(z,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(Mapa map) {
		String packet = map.getMobGroupGMsPackets(); // Un par un comme sa lors du respawn :)
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_MAP_MOBS_GM_PACKET(Mapa map, MobGroup current_Mobs) {
		String packet = "GM|";
		packet += current_Mobs.parseGM();// Un par un comme sa lors du respawn :)
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_MAP_GMS_PACKETS(Mapa map, Personaje _perso) {
		String packet = map.getGMsPackets();
		send(_perso, packet);
		
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ON_EQUIP_ITEM(Mapa map, Personaje _perso) {
		String packet = _perso.parseToOa();
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_ON_EQUIP_ITEM_FIGHT(Personaje _perso, Fighter f, Pelea F) {
		String packet = _perso.parseToOa();
		for(Fighter z : F.getFighters(f.getTeam2())) {
			if(z.getPersonnage() == null) continue;
			send(z.getPersonnage(),packet);
		}
		for(Fighter z : F.getFighters(f.getOtherTeam())) {
			if(z.getPersonnage() == null) continue;
			send(z.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(Pelea fight, int teams, Mapa map, int guid, int cell)
	{
		String packet = "GIC|"+guid+";"+cell+";1";
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
				send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(Mapa map, char s, char option, int guid)
	{
		String packet = "Go"+s+option+guid;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(Pelea fight, int teams, int guid, boolean b)
	{
		String packet = "GR"+(b?"1":"0")+guid;
		if(fight.get_state() != 2)return;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			if(f.hasLeft())continue;
				send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}

	public static void GAME_SEND_GJK_PACKET(Personaje out, int state, int cancelBtn, int duel, int spec, long time, int unknown)
	{
		StringBuilder packet = new StringBuilder();
		packet.append("GJK").append(state).append("|").append(cancelBtn).append("|").append(duel).append("|").append(spec).append("|").append(time).append("|").append(unknown);
		send(out,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}

	public static void GAME_SEND_FIGHT_PLACES_PACKET(PrintWriter out,String places, int team) {
		String packet = "GP"+places+"|"+team;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void ENVIAR_MENSAJE_DESDE_LANG_A_TODOS(String str) {
		String packet = "Im"+str; 
		for(Personaje perso : Mundo.getOnlinePersos())
			send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void ENVIAR_MENSAJE_DESDE_LANG(Personaje out, String str) {
		String packet = "Im"+str;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ILS_PACKET(Personaje out, int i) {
		String packet = "ILS"+i;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ILF_PACKET(Personaje P, int i) {
		String packet = "ILF"+i;
		send(P,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void ENVIAR_MENSAJE_DESDE_LANG_AL_MAPA(Mapa map, String id) {
		String packet = "Im"+id;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_eUK_PACKET_TO_MAP(Mapa map, int guid, int emote) {
		String packet = "eUK"+guid+"|"+emote;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(Pelea fight, int teams, String id) {
		String packet = "Im"+id;
		for(Fighter f : fight.getFighters(teams)) {
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_MESSAGE(Personaje out, String mess, String color) {
		String packet = "cs<font color='#"+color+"'>"+mess+"</font>";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_MESSAGE_TO_MAP(Mapa map, String mess, String color)
	{
		String packet = "cs<font color='#"+color+"'>"+mess+"</font>";
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_GA903_ERROR_PACKET(PrintWriter out, char c,int guid)
	{
		String packet = "GA;903;"+guid+";"+c;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	public static void GAME_SEND_GIC_PACKETS_TO_FIGHT(Pelea fight, int teams)
	{
		StringBuilder packet = new StringBuilder();
		packet.append("GIC|");
		for(Fighter p : fight.getFighters(3))
		{
			if(p.get_fightCell() == null)continue;
			packet.append(p.getGUID()).append(";").append(p.get_fightCell().getID()).append(";1|");
		}
		for(Fighter perso:fight.getFighters(teams))
		{
			if(perso.hasLeft())continue;
			if(perso.getPersonnage() == null || !perso.getPersonnage().isConectado())continue;
			send(perso.getPersonnage(),packet.toString());
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet.toString());
	}
	public static void GAME_SEND_GIC_PACKET_TO_FIGHT(Pelea fight, int teams, Fighter f)
	{
		StringBuilder packet = new StringBuilder();
		packet.append("GIC|").append(f.getGUID()).append(";").append(f.get_fightCell().getID()).append(";1|");

		for(Fighter perso:fight.getFighters(teams))
		{
			if(perso.hasLeft())continue;
			if(perso.getPersonnage() == null || !perso.getPersonnage().isConectado())continue;
			send(perso.getPersonnage(),packet.toString());
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet.toString());
	}
	public static void GAME_SEND_GS_PACKET_TO_FIGHT(Pelea fight, int teams)
	{
		String packet = "GS";
		for(Fighter f:fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			f.initBuffStats();
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	public static void GAME_SEND_GS_PACKET(Personaje out)
	{
		String packet = "GS";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	public static void GAME_SEND_GTL_PACKET_TO_FIGHT(Pelea fight, int teams)
	{
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),fight.getGTL());
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+fight.getGTL());
	}
	public static void GAME_SEND_GTL_PACKET(Personaje out, Pelea fight)
	{
		String packet = fight.getGTL();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	public static void GAME_SEND_GTM_PACKET_TO_FIGHT(Pelea fight, int teams)
	{
		StringBuilder packet = new StringBuilder();
		packet.append("GTM");
		for(Fighter f : fight.getFighters(3))
		{
			packet.append("|").append(f.getGUID()).append(";");
			if(f.isDead())
			{
				packet.append("1");
				continue;
			}else
			packet.append("0;").append(f.getPDV()).append(";").append(f.getPA()).append(";").append(f.getPM()).append(";");
			packet.append((f.isHide()?"-1":f.get_fightCell().getID())).append(";");//On envoie pas la cell d'un invisible :p
			packet.append(";");//??
			packet.append(f.getPDVMAX());
		}
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet.toString());
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet.toString());
	}

	public static void GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(Pelea fight, int teams, int guid, int time)
	{
		String packet = "GTS"+guid+"|"+time;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	public static void GAME_SEND_GAMETURNSTART_PACKET(Personaje P, int guid, int time)
	{
		String packet = "GTS"+guid+"|"+time;
		send(P,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	public static void GAME_SEND_GV_PACKET(Personaje P)
	{
		String packet = "GV";
		send(P,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	public static void GAME_SEND_PONG(PrintWriter out)
	{
		String packet = "pong";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	public static void GAME_SEND_QPONG(PrintWriter out)
	{
		String packet = "qpong";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	public static void GAME_SEND_GAS_PACKET_TO_FIGHT(Pelea fight, int teams, int guid)
	{
		String packet = "GAS"+guid;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	
	public static void GAME_SEND_GA_PACKET_TO_FIGHT(Pelea fight, int teams, int actionID, String s1, String s2)
	{
		String packet = "GA;"+actionID+";"+s1;
		if(!s2.equals(""))
			packet+=";"+s2;
		
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight("+fight.getFighters(teams).size()+") : Send>>"+packet);
	}
	
	public static void GAME_SEND_GA_PACKET(PrintWriter out, String actionID,String s0,String s1, String s2)
	{
		String packet = "GA"+actionID+";"+s0;
		if(!s1.equals(""))
			packet += ";"+s1;
		if(!s2.equals(""))
			packet+=";"+s2;
		
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_GA_PACKET_TO_FIGHT(Pelea fight, int teams, int gameActionID, String s1, String s2, String s3)
	{
		String packet = "GA"+gameActionID+";"+s1+";"+s2+";"+s3;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	
	public static void GAME_SEND_GAMEACTION_TO_FIGHT(Pelea fight, int teams, String packet)
	{
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}

	public static void GAME_SEND_GAF_PACKET_TO_FIGHT(Pelea fight, int teams, int i1, int guid)
	{
		String packet = "GAF"+i1+"|"+guid;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}

	public static void GAME_SEND_BN(Personaje out)
	{
		String packet = "BN";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_BN(PrintWriter out)
	{
		String packet = "BN";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(Pelea fight, int teams, int guid)
	{
		String packet = "GTF"+guid;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}

	public static void GAME_SEND_GTR_PACKET_TO_FIGHT(Pelea fight, int teams, int guid)
	{
		String packet = "GTR"+guid;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}

	public static void GAME_SEND_EMOTICONE_TO_MAP(Mapa map, int guid, int id)
	{
		String packet = "cS"+guid+"|"+id;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_SPELL_UPGRADE_FAILED(PrintWriter _out)
	{
		String packet = "SUE";
		send(_out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_SPELL_UPGRADE_SUCCED(PrintWriter _out,int spellID,int level)
	{
		String packet = "SUK"+spellID+"~"+level;
		send(_out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_SPELL_LIST(Personaje perso)
	{
		String packet = perso.parseSpellList();
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(Pelea fight, int teams, int guid)
	{
		String packet = "GA;103;"+guid+";"+guid;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft() || f.getPersonnage() == null)continue;
			if(f.getPersonnage().isConectado())
				send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}

	public static void GAME_SEND_FIGHT_GE_PACKET_TO_FIGHT(Pelea fight, int teams, int win)
	{
		String packet = fight.GetGE(win);
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft() || f.getPersonnage() == null)continue;
			if(f.getPersonnage().isConectado())
				send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	
	public static void GAME_SEND_FIGHT_GE_PACKET(PrintWriter out, Pelea fight, int win)
	{
		String packet = fight.GetGE(win);
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
	
	public static void GAME_SEND_FIGHT_GIE_TO_FIGHT(Pelea fight, int teams, int mType, int cible, int value, String mParam2, String mParam3, String mParam4, int turn, int spellID)
	{
		StringBuilder packet = new StringBuilder();
		packet.append("GIE").append(mType).append(";").append(cible).append(";").append(value).append(";").append(mParam2).append(";").append(mParam3).append(";").append(mParam4).append(";").append(turn).append(";").append(spellID);
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft() || f.getPersonnage() == null)continue;
			if(f.getPersonnage().isConectado())
			send(f.getPersonnage(),packet.toString());
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(Pelea fight, int teams, Mapa map)
	{
		String packet = map.getFightersGMsPackets();
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}

	public static void GAME_SEND_MAP_FIGHT_GMS_PACKETS(Pelea fight, Mapa map, Personaje _perso)
	{
		String packet = map.getFightersGMsPackets();
		send(_perso, packet);
		
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}
	
	public static void GAME_SEND_FIGHT_PLAYER_JOIN(Pelea fight, int teams, Fighter _fighter)
	{
		String packet = _fighter.getGmPacket('+');
		
		for(Fighter f : fight.getFighters(teams))
		{
			if (f != _fighter)
			{
				if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
				if(f.getPersonnage() != null && f.getPersonnage().getCuenta().getGameThread() != null)
					send(f.getPersonnage(),packet);
			}
		}
		
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}
	
	public static void GAME_SEND_cMK_PACKET(Personaje perso, String suffix, int guid, String name, String msg)
	{
		String packet = "cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_FIGHT_LIST_PACKET(PrintWriter out, Mapa map)
	{
		StringBuilder packet = new StringBuilder();
		packet.append("fL");
		for(Entry<Integer, Pelea> entry : map.get_fights().entrySet())
		{
			if(packet.length()>2)
			{
				packet.append("|");
			}
			packet.append(entry.getValue().parseFightInfos());
		}
		send(out,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_cMK_PACKET_TO_MAP(Mapa map, String suffix, int guid, String name, String msg)
	{
		String packet = "cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	public static void GAME_SEND_cMK_PACKET_TO_GUILD(Gremio g, String suffix, int guid, String name, String msg)
	{
		String packet = "cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
		for(Personaje perso : g.getMembers())
		{
			if(perso == null || !perso.isConectado())continue;
					send(perso,packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Guild: Send>>"+packet);
	}
	public static void GAME_SEND_cMK_PACKET_TO_ALL(String suffix,int guid,String name,String msg)
	{
		String packet = "cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
		for(Personaje perso : Mundo.getOnlinePersos())
			send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: ALL("+ Mundo.getOnlinePersos().size()+"): Send>>"+packet);
	}

	public static void GAME_SEND_cMK_PACKET_TO_ALIGN(String suffix,int guid,String name,String msg, Personaje _perso) {
		String packet = "cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
		for(Personaje perso : Mundo.getOnlinePersos()) {
			if(perso.get_align() == _perso.get_align()) {
				send(perso,packet);
			}
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: ALL("+ Mundo.getOnlinePersos().size()+"): Send>>"+packet);
	}

	public static void GAME_SEND_cMK_PACKET_TO_ADMIN(String suffix,int guid,String name,String msg) {
		String packet = "cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
		for(Personaje perso : Mundo.getOnlinePersos())if(perso.isConectado())if(perso.getCuenta() != null)if(perso.getCuenta().getGMLVL()>0)send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: ALL("+ Mundo.getOnlinePersos().size()+"): Send>>"+packet);
	}

	public static void GAME_SEND_cMK_PACKET_TO_FIGHT(Pelea fight, int teams, String suffix, int guid, String name, String msg) {
		if(fight != null) fight.ticMyTimer();
		
        String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg;
		assert fight != null;
		for(Fighter f : fight.getFighters(teams)) {
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}
	
	public static void GAME_SEND_GDZ_PACKET_TO_FIGHT(Pelea fight, int teams, String suffix, int cell, int size, int unk) {
		String packet = "GDZ"+suffix+cell+";"+size+";"+unk;
		for(Fighter f : fight.getFighters(teams)) {
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}
	
	public static void GAME_SEND_GDC_PACKET_TO_FIGHT(Pelea fight, int teams, int cell) {
		String packet = "GDC"+cell;
		for(Fighter f : fight.getFighters(teams)) {
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}
	
	public static void GAME_SEND_GA2_PACKET(PrintWriter out, int guid) {
		String packet = "GA;2;"+guid+";";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_CHAT_ERROR_PACKET(PrintWriter out,String name) {
		String packet = "cMEf"+name;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_eD_PACKET_TO_MAP(Mapa map, int guid, int dir) {
		String packet = "eD"+guid+"|"+dir;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_ECK_PACKET(Personaje out, int type, String str) {
		String packet = "ECK"+type;
		if(!str.equals(""))packet += "|"+str;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ECK_PACKET(PrintWriter out, int type,String str) {
		String packet = "ECK"+type;
		if(!str.equals(""))packet += "|"+str;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ITEM_VENDOR_LIST_PACKET(PrintWriter out, NPC npc) {
		String packet = "EL"+npc.getModelo().getItemVendorList();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(PrintWriter out, Recaudador perco) {
		String packet = "EL"+perco.getItemPercepteurList();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ITEM_LIST_PACKET_SELLER(Personaje p, Personaje out) {
		String packet = "EL"+p.parseStoreItemsList();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_EV_PACKET(PrintWriter out) {
		String packet = "EV";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_DCK_PACKET(PrintWriter out, int id) {
		String packet = "DCK"+id;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_QUESTION_PACKET(PrintWriter out,String str) {
		String packet = "DQ"+str;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_END_DIALOG_PACKET(PrintWriter out) {
		String packet = "DV";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void ENVIAR_TEXTO_EN_CONSOLA(PrintWriter out, String mess) {
		String packet = "BAT2"+mess;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_BUY_ERROR_PACKET(PrintWriter out) {
		String packet = "EBE";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_SELL_ERROR_PACKET(PrintWriter out) {
		String packet = "ESE";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_BUY_OK_PACKET(PrintWriter out)
	{
		String packet = "EBK";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_OBJECT_QUANTITY_PACKET(Personaje out, Objeto obj)
	{
		String packet = "OQ"+obj.getGuid()+"|"+obj.getQuantity();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_OAKO_PACKET(Personaje out, Objeto obj)
	{
		String packet = "OAKO"+obj.parseItem();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ESK_PACKEt(Personaje out)
	{
		String packet = "ESK";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_REMOVE_ITEM_PACKET(Personaje out, int guid)
	{
		String packet = "OR"+guid;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_DELETE_OBJECT_FAILED_PACKET(PrintWriter out)
	{
		String packet = "OdE";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_OBJET_MOVE_PACKET(Personaje out, Objeto obj)
	{
		String packet = "OM"+obj.getGuid()+"|";
		if(obj.getPosition() != Constantes.ITEM_POS_NO_EQUIPED)
			packet += obj.getPosition();
		
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_EMOTICONE_TO_FIGHT(Pelea fight, int teams, int guid, int id)
	{
		if(fight != null) fight.ticMyTimer();
        
		String packet = "cS" + guid + "|" + id;
		assert fight != null;
		for(Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
				send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}

	public static void GAME_SEND_OAEL_PACKET(PrintWriter out)
	{
		String packet = "OAEL";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_NEW_LVL_PACKET(PrintWriter out, int lvl)
	{
		String packet = "AN"+lvl;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void ENVIAR_MENSAJE_A_TODOS(String msg, String color) {
		String packet = "cs<font color='#"+color+"'>"+msg+"</font>";
		for(Personaje P : Mundo.getOnlinePersos()) {
			send(P,packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: ALL: Send>>"+packet);
	}

	public static void GAME_SEND_EXCHANGE_REQUEST_OK(PrintWriter out, int guid, int guidT, int msgID)
	{
		String packet = "ERK"+guid+"|"+guidT+"|"+msgID;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_EXCHANGE_REQUEST_ERROR(PrintWriter out, char c)
	{
		String packet = "ERE"+c;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_EXCHANGE_CONFIRM_OK(PrintWriter out, int type)
	{
		String packet = "ECK"+type;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_EXCHANGE_MOVE_OK(Personaje out, char type, String signe, String s1)
	{
		String packet = "EMK"+type+signe;
		if(!s1.equals(""))
			packet += s1;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_EXCHANGE_OTHER_MOVE_OK(PrintWriter out,char type,String signe,String s1)
	{
		String packet = "EmK"+type+signe;
		if(!s1.equals(""))
			packet += s1;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_EXCHANGE_OK(PrintWriter out,boolean ok, int guid)
	{
		String packet = "EK"+(ok?"1":"0")+guid;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_EXCHANGE_VALID(PrintWriter out, char c)
	{
		String packet = "EV"+c;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_GROUP_INVITATION_ERROR(PrintWriter out, String s) {
		String packet = "PIE"+s;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_GROUP_INVITATION(PrintWriter out,String n1, String n2)
	{
		String packet = "PIK"+n1+"|"+n2;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_GROUP_CREATE(PrintWriter out, Grupo g)
	{
		String packet = "PCK"+g.getChief().getNombre();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Groupe: Send>>"+packet);
	}

	public static void GAME_SEND_PL_PACKET(PrintWriter out, Grupo g)
	{
		String packet = "PL"+g.getChief().get_GUID();
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Groupe: Send>>"+packet);
	}
	
	public static void GAME_SEND_PR_PACKET(Personaje out)
	{
		String packet = "PR";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_PV_PACKET(PrintWriter out,String s)
	{
		String packet = "PV"+s;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ALL_PM_ADD_PACKET(PrintWriter out, Grupo g)
	{
		StringBuilder packet = new StringBuilder();
		packet.append("PM+");
		boolean first = true;
		for(Personaje p : g.getMiembrosGrupo())
		{
			if(!first) packet.append("|");
			packet.append(p.parseToPM());
			first = false;
		}
		send(out,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_PM_ADD_PACKET_TO_GROUP(Grupo g, Personaje p)
	{
		String packet = "PM+"+p.parseToPM();
		for(Personaje P : g.getMiembrosGrupo())send(P,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Groupe: Send>>"+packet);
	}
	
	public static void GAME_SEND_PM_MOD_PACKET_TO_GROUP(Grupo g, Personaje p)
	{
		String packet = "PM~"+p.parseToPM();
		for(Personaje P : g.getMiembrosGrupo())send(P,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Groupe: Send>>"+packet);
	}

	public static void GAME_SEND_PM_DEL_PACKET_TO_GROUP(Grupo g, int guid)
	{
		String packet = "PM-"+guid;
		for(Personaje P : g.getMiembrosGrupo())send(P,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Groupe: Send>>"+packet);
	}

	public static void GAME_SEND_cMK_PACKET_TO_GROUP(Grupo g, String s, int guid, String name, String msg)
	{
		String packet = "cMK"+s+"|"+guid+"|"+name+"|"+msg+"|";
		for(Personaje P : g.getMiembrosGrupo())send(P,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Groupe: Send>>"+packet);
	}

	public static void GAME_SEND_FIGHT_DETAILS(PrintWriter out, Pelea fight)
	{
		if(fight == null)return;
		StringBuilder packet = new StringBuilder();
		packet.append("fD").append(fight.get_id()).append("|");
		for(Fighter f : fight.getFighters(1))packet.append(f.getPacketsName()).append("~").append(f.get_lvl()).append(";");
		packet.append("|");
		for(Fighter f : fight.getFighters(2))packet.append(f.getPacketsName()).append("~").append(f.get_lvl()).append(";");
		send(out,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}

	public static void GAME_SEND_IQ_PACKET(Personaje perso, int guid, int qua)
	{
		String packet = "IQ"+guid+"|"+qua;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	public static void GAME_SEND_JN_PACKET(Personaje perso, int jobID, int lvl)
	{
		String packet = "JN"+jobID+"|"+lvl;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	public static void GAME_SEND_GDF_PACKET_TO_MAP(Mapa map, Case cell)
	{
		int cellID = cell.getID();
		InteractiveObject object = cell.getObject();
		String packet = "GDF|"+cellID+";"+object.getState()+";"+(object.isInteractive()?"1":"0");
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_GA_PACKET_TO_MAP(Mapa map, String gameActionID, int actionID, String s1, String s2)
	{
		String packet = "GA"+gameActionID+";"+actionID+";"+s1;
		if(!s2.equals(""))packet += ";"+s2;
		
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_EL_BANK_PACKET(Personaje perso)
	{
		String packet = "EL"+perso.parseBankPacket();
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_EL_TRUNK_PACKET(Personaje perso, Cofres t)
	{
		String packet = "EL"+t.parseToTrunkPacket();
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_JX_PACKET(Personaje perso, ArrayList<StatsMetier> SMs)
	{
		StringBuilder packet = new StringBuilder();
		packet.append("JX");
		for(StatsMetier sm : SMs)
		{
			packet.append("|").append(sm.getTemplate().getId()).append(";").append(sm.get_lvl()).append(";").append(sm.getXpString(";")).append(";");
		}
		send(perso,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}
	public static void GAME_SEND_JO_PACKET(Personaje perso, ArrayList<StatsMetier> SMs)
	{
		for(StatsMetier sm : SMs)
		{
			String packet = "JO"+sm.getID()+"|"+sm.getOptBinValue()+"|2";//FIXME 2=?
			send(perso,packet);
			if(MainServidor.MOSTRAR_ENVIADOS)
				JuegoServidor.addToSockLog("Game: Send>>"+packet);
		}
	}
	public static void GAME_SEND_JS_PACKET(Personaje perso, ArrayList<StatsMetier> SMs)
	{
		StringBuilder packet = new StringBuilder("JS");
		for(StatsMetier sm : SMs)
		{
			packet.append(sm.parseJS());
		}
		send(perso, packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_EsK_PACKET(Personaje perso, String str)
	{
		String packet = "EsK"+str;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_FIGHT_SHOW_CASE(ArrayList<PrintWriter> PWs, int guid, int cellID)
	{
		String packet = "Gf"+guid+"|"+cellID;
		for(PrintWriter PW : PWs)
		{
			send(PW,packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight: Send>>"+packet);
	}
	
	public static void GAME_SEND_Ea_PACKET(Personaje perso, String str)
	{
		String packet = "Ea"+str;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_EA_PACKET(Personaje perso, String str)
	{
		String packet = "EA"+str;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_Ec_PACKET(Personaje perso, String str)
	{
		String packet = "Ec"+str;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_Em_PACKET(Personaje perso, String str)
	{
		String packet = "Em"+str;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_IO_PACKET_TO_MAP(Mapa map, int guid, String str)
	{
		String packet = "IO"+guid+"|"+str;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_FRIENDLIST_PACKET(Personaje perso)
	{
		String packet = "FL"+perso.getCuenta().parseFriendList();
		send(perso,packet);
		if(perso.getWife() != 0)
		{
			String packet2 = "FS" + perso.get_wife_friendlist();
			send(perso,packet2);
			if(MainServidor.MOSTRAR_ENVIADOS)
				JuegoServidor.addToSockLog("Game: Send>>"+packet2);
		} 
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_FRIEND_ONLINE(Personaje logando, Personaje amigo)
	{
		String packet = "Im0143;"+logando.getCuenta().get_pseudo()+" (<b><a href='asfunction:onHref,ShowPlayerPopupMenu,"+logando.getNombre()+"'>"+logando.getNombre()+"</a></b>)";
		send(amigo, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
		JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_FA_PACKET(Personaje perso, String str)
	{
		String packet = "FA"+str;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	public static void GAME_SEND_FD_PACKET(Personaje perso, String str)
	{
		String packet = "FD"+str;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	public static void GAME_SEND_Rp_PACKET(Personaje perso, MountPark MP)
	{
		StringBuilder packet = new StringBuilder();
		if(MP == null)return;
		
		packet.append("Rp").append(MP.get_owner()).append(";").append(MP.get_price()).append(";").append(MP.get_size()).append(";").append(MP.getObjectNumb()).append(";");
			
		Gremio G = MP.get_guild();
		//Si une guilde est definie
		if(G != null)
		{
			packet.append(G.get_name()).append(";").append(G.get_emblem());
		}
		else
		{
			packet.append(";");
		}
		
		send(perso,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}

	public static void GAME_SEND_OS_PACKET(Personaje perso, int pano) {
		StringBuilder packet = new StringBuilder();
		packet.append("OS");
		int num = perso.getNumbEquipedItemOfPanoplie(pano);
		if(num <= 0) packet.append("-").append(pano);
		else {
			packet.append("+").append(pano).append("|");
			ItemSet IS = Mundo.getItemSet(pano);
			if(IS != null) {
				StringBuilder items = new StringBuilder();
				//Pour chaque objet de la pano
				for(ObjTemplate OT : IS.getItemTemplates()) {
					//Si le joueur l'a équipé
					if(perso.hasEquiped(OT.getID())) {
						//On l'ajoute au packet
						if(items.length() >0)items.append(";");
						items.append(OT.getID());
					}
				}
				packet.append(items.toString()).append("|").append(IS.getBonusStatByItemNumb(num).parseToItemSetStats());
			}
		}	
		send(perso,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}

	public static void ENVIAR_PAQUETE_DESCRIPCION_DE_MONTURA(Personaje personaje, Dragopavo dragopavo) {
		String packet = "Rd"+dragopavo.parse();
		send(personaje,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_Rr_PACKET(Personaje perso, String str) {
		String packet = "Rr"+str;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_ALTER_GM_PACKET(Mapa map, Personaje perso) {
		String packet = "GM|~"+perso.parseToGM();
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_Ee_PACKET(Personaje perso, char c, String s) {
		String packet = "Ee"+c+s;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_cC_PACKET(Personaje perso, char c, String s) {
		String packet = "cC"+c+s;
		send(perso,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void ENVIAR_AGREGAR_NPC_EN_MAPA(Mapa map, NPC npc) {
		for(Personaje z : map.getPersos()) {
			String packet = "GM|"+npc.parseGM(z);
			send(z,packet);
			if(MainServidor.MOSTRAR_ENVIADOS)
				JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
		}
	}
	
	public static void ENVIAR_AGREGAR_RECAUDADOR_EN_MAPA(Mapa map) {
		String packet = "GM|"+ Recaudador.parseGM(map);
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}

	public static void GAME_SEND_GDO_PACKET_TO_MAP(Mapa map, char c, int cell, int itm, int i) {
		String packet = "GDO"+c+cell+";"+itm+";"+i;
		for(Personaje z : map.getPersos()) send(z,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Map: Send>>"+packet);
	}
	
	public static void GAME_SEND_GDO_PACKET(Personaje p, char c, int cell, int itm, int i) {
		String packet = "GDO"+c+cell+";"+itm+";"+i;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ZC_PACKET(Personaje p, int a) {
		String packet = "ZC"+a;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_GIP_PACKET(Personaje p, int a) {
		String packet = "GIP"+a;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_gn_PACKET(Personaje p) {
		String packet = "gn";
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gC_PACKET(Personaje p, String s) {
		String packet = "gC"+s;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gV_PACKET(Personaje p) {
		String packet = "gV";
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gIM_PACKET(Personaje p, Gremio g, char c) {
		String packet = "gIM"+c;
		if (c == '+') {
			packet += g.parseMembersToGM();
		}
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gIB_PACKET(Personaje p, String infos) {
		String packet = "gIB"+infos;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gIH_PACKET(Personaje p, String infos) {
		String packet = "gIH"+infos;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gS_PACKET(Personaje p, GuildMember gm) {
		StringBuilder packet = new StringBuilder();
		packet.append("gS").append(gm.getGuild().get_name()).append("|").append(gm.getGuild().get_emblem().replace(',', '|')).append("|").append(gm.parseRights());
		send(p,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}
	
	public static void GAME_SEND_gJ_PACKET(Personaje p, String str) {
		String packet = "gJ"+str;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gK_PACKET(Personaje p, String str)
	{
		String packet = "gK"+str;
		send(p,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gIG_PACKET(Personaje p, Gremio g) {
		long xpMin = Mundo.getExpLevel(g.get_lvl()).guilde;
		long xpMax;
		if(Mundo.getExpLevel(g.get_lvl()+1) == null) {
			xpMax = -1;
		}else {
			xpMax = Mundo.getExpLevel(g.get_lvl()+1).guilde;
		}
		StringBuilder packet = new StringBuilder();
		packet.append("gIG").append((g.getSize()>9?1:0)).append("|").append(g.get_lvl()).append("|").append(xpMin).append("|").append(g.get_xp()).append("|").append(xpMax);
		send(p,packet.toString());
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet.toString());
	}
	
	public static void REALM_SEND_MESSAGE(PrintWriter out, String args) {
		String packet = "M"+args;
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_WC_PACKET(Personaje perso) {
		String packet = "WC"+perso.parseZaapList();
		send(perso.getCuenta().getGameThread().get_out(),packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}

	public static void GAME_SEND_WV_PACKET(Personaje out) {
		String packet = "WV";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_ZAAPI_PACKET(Personaje perso, String list) {
		String packet = "Wc" + perso.getActualMapa().getID()+ "|"+list;
		send(perso, packet);
		JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_CLOSE_ZAAPI_PACKET(Personaje out) {
		String packet = "Wv";
		send(out, packet);
		JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_WUE_PACKET(Personaje out) {
		String packet = "WUE";
		send(out,packet);
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_EMOTE_LIST(Personaje perso, String s, String s1) {
		String packet = "eL"+s+"|"+s1;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_NO_EMOTE(Personaje out) {
		String packet = "eUE";
		send(out, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void REALM_SEND_TOO_MANY_PLAYER_ERROR(PrintWriter out) {
		String packet = "AlEw";
		send(out, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void REALM_SEND_REQUIRED_APK(PrintWriter out) {
	    String chars = "abcdefghijklmnopqrstuvwxyz"; // Tu supprimes les lettres dont tu ne veux pas
	    StringBuilder pass = new StringBuilder();
	    for(int x=0;x<5;x++)
	    {
	       int i = (int)Math.floor(Math.random() * 26); // Si tu supprimes des lettres tu diminues ce nb
	       pass.append(chars.charAt(i));
	    }
	    System.out.println(pass);
	    
		String packet = "APK"+pass;
				send(out, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_ADD_ENEMY(Personaje out, Personaje pr) {
		String packet = "iAK"+pr.getCuenta().getNombre()+";2;"+pr.getNombre()+";36;10;0;100.FL.";
		send(out, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_iAEA_PACKET(Personaje out) {
		String packet = "iAEA.";
		send(out, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_ENEMY_LIST(Personaje perso) {
		String packet = "iL"+perso.getCuenta().parseEnemyList();
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_iD_COMMANDE(Personaje perso, String str) {
		String packet = "iD"+str;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_BWK(Personaje perso, String str) {
		String packet = "BWK"+str;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
    public static void GAME_SEND_KODE(Personaje perso, String str) {
        String packet = "K" + str;
        send(perso, packet);
        if(MainServidor.MOSTRAR_ENVIADOS)
            JuegoServidor.addToSockLog("Game: Send>>" + packet);
    }

    public static void GAME_SEND_hOUSE(Personaje perso, String str) {
        String packet = "h" + str;
        send(perso, packet);
        if(MainServidor.MOSTRAR_ENVIADOS)
            JuegoServidor.addToSockLog("Game: Send>>" + packet);
    }
	
	public static void GAME_SEND_FORGETSPELL_INTERFACE(char sign, Personaje perso) {
		String packet = "SF"+sign;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_R_PACKET(Personaje perso, String str) {
		String packet = "R"+str;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_gIF_PACKET(Personaje perso, String str) {
		String packet = "gIF"+str;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_gITM_PACKET(Personaje perso, String str) {
		String packet = "gITM"+str;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_gITp_PACKET(Personaje perso, String str) {
		String packet = "gITp"+str;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_gITP_PACKET(Personaje perso, String str) {
		String packet = "gITP"+str;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_IH_PACKET(Personaje perso, String str) {
		String packet = "IH"+str;
		send(perso, packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}
	
	public static void GAME_SEND_FLAG_PACKET(Personaje perso, Personaje cible) {
		String packet = "IC"+cible.getActualMapa().getX()+"|"+cible.getActualMapa().getY();
		send(perso,packet); 
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_DELETE_FLAG_PACKET(Personaje perso) {
		String packet = "IC|"; 
		send(perso,packet); 
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_gT_PACKET(Personaje perso, String str) {
		String packet = "gT"+str; 
		send(perso,packet); 
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_GUILDHOUSE_PACKET(Personaje perso) {
		String packet = "gUT"; 
		send(perso,packet); 
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	public static void GAME_SEND_GUILDENCLO_PACKET(Personaje perso) {
		String packet = "gUF"; 
		send(perso,packet); 
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>"+packet);
	}
	
	//Mercadillos
	public static void GAME_SEND_EHm_PACKET(Personaje out, String sign, String str) {
		String packet = "EHm"+sign + str;
		send(out,packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_EHM_PACKET(Personaje out, String sign, String str) {
		String packet = "EHM"+sign + str;
		send(out,packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_EHP_PACKET(Personaje out, int templateID)	//Packet d'envoie du prix moyen du template (En réponse a un packet EHP)
	{
		String packet = "EHP"+templateID+"|"+ Mundo.getObjTemplate(templateID).getAvgPrice();
		send(out,packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_EHl(Personaje out, Mercadillo seller, int templateID) {
		String packet = "EHl" + seller.parseToEHl(templateID);
		send(out,packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_EHL_PACKET(Personaje out, int categ, String templates)	//Packet de listage des templates dans une catégorie (En réponse au packet EHT)
	{
		String packet = "EHL"+categ+"|"+templates;
		
		send(out,packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_EHL_PACKET(Personaje out, String items)	//Packet de listage des objets en vente
	{
		String packet = "EHL"+items;
		
		send(out,packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_HDVITEM_SELLING(Personaje perso) {
		StringBuilder packet = new StringBuilder("EL");
		HdvEntry[] entries = perso.getCuenta().getHdvItems(Math.abs(perso.get_isTradingWith()));	//Récupère un tableau de tout les items que le personnage à en vente dans l'HDV où il est
		boolean isFirst = true;
		for(HdvEntry curEntry : entries) {
			if(curEntry == null)
				break;
			if(!isFirst)
				packet.append("|");
			packet.append(curEntry.parseToEL());
		isFirst = false;
		}
		send(perso, packet.toString());
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_WEDDING(Mapa c, int action, int homme, int femme, int parlant) {
		String packet = "GA;"+action+";"+homme+";"+homme+","+femme+","+parlant;
		Personaje Homme = Mundo.getPersonnage(homme);
		send(Homme,packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

	public static void GAME_SEND_PF(Personaje perso, String str) {
		String packet = "PF"+str;
		send(perso,packet);
		if (MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Send>>" + packet);
	}

    public static void GAME_SEND_MERCHANT_LIST(Personaje P, short mapID) {
    	StringBuilder packet = new StringBuilder();
    	packet.append("GM|~");
    	if(Mundo.getSeller(P.getActualMapa().getID()) == null) return;
        for (Integer pID : Mundo.getSeller(P.getActualMapa().getID())) {
        	if(!Mundo.getPersonnage(pID).isConectado() && Mundo.getPersonnage(pID).is_showSeller()) {
        		packet.append(Mundo.getPersonnage(pID).parseToMerchant()).append("|");
            }
        }
        if(packet.length() < 5) return;
        send(P, packet.toString());
        if (MainServidor.MOSTRAR_ENVIADOS)
        	JuegoServidor.addToSockLog("Game: Send>>" + packet.toString());
    }
    
    public static void GAME_SEND_cMK_PACKET_INCARNAM_CHAT(Personaje perso, String suffix, int guid, String name, String msg) {
    	String packet = "cMK" + suffix + "|" + guid + "|" + name + "|" + msg; 
    	if (perso.get_lvl() > 15) {
    		GAME_SEND_BN(perso); 
    		return; 
    	} 
    	for(Personaje perso1 : Mundo.getOnlinePersos()) {
    		send(perso1, packet); 
    	} 
    	if(MainServidor.MOSTRAR_ENVIADOS)
    		JuegoServidor.addToSockLog("Game: ALL("+ Mundo.getOnlinePersos().size()+"): Send>>"+packet);
    }

	public static void GAME_SEND_PACKET_TO_FIGHT(Pelea fight, int i, String packet) {
		for(Fighter f : fight.getFighters(i)) {
			if(f.hasLeft())continue;
			if(f.getPersonnage() == null || !f.getPersonnage().isConectado())continue;
			send(f.getPersonnage(),packet);
		}
		if(MainServidor.MOSTRAR_ENVIADOS)
			JuegoServidor.addToSockLog("Game: Fight : Send>>"+packet);
	}
}
