package comunes

import juego.JuegoServidor
import juego.JuegoServidor.Companion.serverDate
import juego.JuegoServidor.Companion.serverTime
import objetos.*
import objetos.Gremio.GuildMember
import objetos.Mapa.MountPark
import objetos.Monstruo.MobGroup
import objetos.NPCModelo.NPC
import objetos.Oficio.StatsMetier
import objetos.Pelea.Peleador
import objetos.Personaje.Grupo
import objetos.casas.Cofres
import realm.RealmServer
import java.io.PrintWriter
import java.util.*

object GestorSalida {
    @JvmStatic
    fun enviar(personaje: Personaje?, paquete: String) {
        var paquete = paquete
        if (personaje == null || personaje.cuenta == null) return
        if (personaje.cuenta.juegoThread == null) return
        val out = personaje.cuenta.juegoThread._out
        if (out != null && paquete != "" && paquete != "" + 0x00.toChar()) {
            paquete = GestorEncriptador.toUtf(paquete)
            if (MainServidor.CONFIG_SOCKET_USE_COMPACT_DATA) {
                GestorEnvio.enviar(out, paquete)
            } else {
                out.print(paquete + 0x00.toChar())
                out.flush()
            }
        }
    }

    fun enviar(out: PrintWriter?, packet: String) {
        var packet = packet
        if (out != null && packet != "" && packet != "" + 0x00.toChar()) {
            packet = GestorEncriptador.toUtf(packet)
            if (MainServidor.CONFIG_SOCKET_USE_COMPACT_DATA) {
                GestorEnvio.enviar(out, packet)
            } else {
                out.print(packet + 0x00.toChar())
                out.flush()
            }
        }
    }

    @JvmStatic
    fun REALM_SEND_HC_PACKET(out: PrintWriter?): String {
        val alphabet = "abcdefghijklmnopqrstuvwxyz"
        val hashkey = StringBuilder()
        val rand = Random()
        for (i in 0..31) {
            hashkey.append(alphabet[rand.nextInt(alphabet.length)])
        }
        val packet = "HC$hashkey"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Realm: Send>>$packet")
        return hashkey.toString()
    }

    @JvmStatic
    fun REALM_SEND_REQUIRED_VERSION(out: PrintWriter?) {
        val packet = "AlEv" + MainServidor.VERSION_DEL_CLIENTE
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Conn: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_LOGIN_ERROR(out: PrintWriter?) {
        val packet = "AlEf"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Conn: Send>>$packet")
    }

    @JvmStatic
	fun MULTI_SEND_Af_PACKET(out: PrintWriter?, position: Int, totalAbo: Int, totalNonAbo: Int, subscribe: String?, queueID: Int) {
        val packet = StringBuilder()
        packet.append("Af").append(position).append("|").append(totalAbo).append("|").append(totalNonAbo).append("|").append(subscribe).append("|").append(queueID)
        enviar(out, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Serv: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_Ad_Ac_AH_AlK_AQ_PACKETS(out: PrintWriter?, pseudo: String?, level: Int, question: String) {
        val packet = StringBuilder()
        packet.append("Ad").append(pseudo).append(0x00.toChar())
        packet.append("Ac0").append(0x00.toChar())
        //AH[ID];[State];[Completion];[CanLog]
        packet.append("AH1;").append(Mundo.get_state().toInt()).append(";110;1").append(0x00.toChar())
        packet.append("AlK").append(level).append(0x00.toChar())
        packet.append("AQ").append(question.replace(" ", "+"))
        enviar(out, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Conn: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_BANNED(out: PrintWriter?) {
        val packet = "AlEb"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Conn: Send>>$packet")
    }

    @JvmStatic
    fun ENVIAR_ESTA_CONECTADO(out: PrintWriter?) {
        val packet = "AlEc"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Conn: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_POLICY_FILE(out: PrintWriter?) {
        val packet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<cross-domain-policy>" +
                "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\" />" +
                "<site-control permitted-cross-domain-policies=\"master-only\" />" +
                "</cross-domain-policy>"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_PERSO_LIST(out: PrintWriter?, number: Int) {
        var packet = "AxK31536000000" //Temps d'abonnement
        if (number > 0) packet += "|1,$number" //ServeurID
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Conn: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_GAME_SERVER_IP(out: PrintWriter?, guid: Int, isHost: Boolean) {
        var packet = "A"
        packet += if (MainServidor.USAR_IP) {
            val ip = if (MainServidor.CONFIG_IP_LOOPBACK && isHost) GestorEncriptador.CryptIP("127.0.0.1") + GestorEncriptador.CryptPort(MainServidor.PUERTO_DE_JUEGO) else MainServidor.GAMESERVER_IP!!
            "XK$ip$guid"
        } else {
            val ip = if (MainServidor.CONFIG_IP_LOOPBACK && isHost) "127.0.0.1" else MainServidor.IP!!
            "YK" + ip + ":" + MainServidor.PUERTO_DE_JUEGO + ";" + guid
        }
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) RealmServer.addToSockLog("Conn: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_HELLOGAME_PACKET(out: PrintWriter?) {
        val packet = "HG"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ATTRIBUTE_FAILED(out: PrintWriter?) {
        val packet = "ATE"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ATTRIBUTE_SUCCESS(out: PrintWriter?) {
        val packet = "ATK0"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_AV0(out: PrintWriter?) {
        val packet = "AV0"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_HIDE_GENERATE_NAME(out: PrintWriter?) {
        val packet = "APE2"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PERSO_LIST(out: PrintWriter?, persos: Map<Int?, Personaje>) {
        val packet = StringBuilder()
        packet.append("ALK31536000000|").append(persos.size)
        for ((_, value) in persos) {
            packet.append(value.parseALK())
        }
        enviar(out, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_NAME_ALREADY_EXIST(out: PrintWriter?) {
        val packet = "AAEa"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_CREATE_PERSO_FULL(out: PrintWriter?) {
        val packet = "AAEf"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_CREATE_OK(out: PrintWriter?) {
        val packet = "AAK"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_DELETE_PERSO_FAILED(out: PrintWriter?) {
        val packet = "ADE"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_CREATE_FAILED(out: PrintWriter?) {
        val packet = "AAEF"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PERSO_SELECTION_FAILED(out: PrintWriter?) {
        val packet = "ASE"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_PAQUETE_CARACTERISTICAS(perso: Personaje) {
        val packet = perso.asPacket
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Rx_PACKET(out: Personaje) {
        val packet = "Rx" + out.mountXpGive
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Rn_PACKET(out: Personaje?, name: String) {
        val packet = "Rn$name"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Re_PACKET(out: Personaje?, sign: String, DD: Dragopavo) {
        var packet = "Re$sign"
        if (sign == "+") packet += DD.parse()
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ASK(out: PrintWriter?, perso: Personaje) {
        val packet = StringBuilder()
        packet.append("ASK|").append(perso.id).append("|").append(perso.nombre).append("|")
        packet.append(perso._lvl).append("|").append(perso.clase).append("|").append(perso.sexo)
        packet.append("|").append(perso._gfxID).append("|").append(if (perso._color1 == -1) "-1" else Integer.toHexString(perso._color1))
        packet.append("|").append(if (perso._color2 == -1) "-1" else Integer.toHexString(perso._color2)).append("|")
        packet.append(if (perso._color3 == -1) "-1" else Integer.toHexString(perso._color3)).append("|")
        packet.append(perso.parseItemToASK())
        enviar(out, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ALIGNEMENT(out: PrintWriter?, alliID: Int) {
        val packet = "ZS$alliID"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ADD_CANAL(out: PrintWriter?, chans: String) {
        val packet = "cC+$chans"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ZONE_ALLIGN_STATUT(out: PrintWriter?) {
        val packet = "al|" + Mundo.getSousZoneStateString()
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_SEESPELL_OPTION(out: PrintWriter?, spells: Boolean) {
        val packet = "SLo" + if (spells) "+" else "-"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_RESTRICTIONS(out: PrintWriter?) {
        val packet = "AR6bk"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Ow_PACKET(perso: Personaje) {
        val packet = "Ow" + perso.podUsed + "|" + perso.maxPod
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_OT_PACKET(out: PrintWriter?, id: Int) {
        var packet = "OT"
        if (id > 0) packet += id
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_SEE_FRIEND_CONNEXION(out: PrintWriter?, see: Boolean) {
        val packet = "FO" + if (see) "+" else "-"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAME_CREATE(out: PrintWriter?, _name: String) {
        val packet = "GCK|1|$_name"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_SERVER_HOUR(out: PrintWriter?) {
        val packet = serverTime
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_SERVER_DATE(out: PrintWriter?) {
        val packet = serverDate
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAPDATA(out: PrintWriter?, id: Int, date: String, key: String) {
        val packet = "GDM|$id|$date|$key"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GDK_PACKET(out: PrintWriter?) {
        val packet = "GDK"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_MOBS_GMS_PACKETS(out: PrintWriter?, carte: Mapa) {
        val packet = carte.mobGroupGMsPackets
        if (packet == "") return
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_OBJECTS_GDS_PACKETS(out: PrintWriter?, carte: Mapa) {
        val packet = carte.objectsGDsPackets
        if (packet == "") return
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_NPCS_GMS_PACKETS(p: Personaje?, carte: Mapa) {
        val packet = carte.getNpcsGMsPackets(p)
        if (packet == "") return
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_PERCO_GMS_PACKETS(out: PrintWriter?, carte: Mapa?) {
        val packet = Recaudador.parseGM(carte)
        if (packet.length < 5) return
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_MAP_GMS_PACKETS(out: PrintWriter?, carte: Mapa) {
        val packet = carte.gMsPackets
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ERASE_ON_MAP_TO_MAP(map: Mapa, guid: Int) {
        val packet = "GM|-$guid"
        for (z in map.persos) {
            if (z.cuenta.juegoThread == null) continue
            enviar(z.cuenta.juegoThread._out, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map " + map.id + ": Send>>" + packet)
    }

    @JvmStatic
    fun GAME_SEND_ERASE_ON_MAP_TO_FIGHT(f: Pelea, guid: Int) {
        val packet = "GM|-$guid"
        for (z in f.getFighters(1).indices) {
            if (f.getFighters(1)[z].personnage.cuenta.juegoThread == null) continue
            enviar(f.getFighters(1)[z].personnage.cuenta.juegoThread._out, packet)
        }
        for (z in f.getFighters(2).indices) {
            if (f.getFighters(2)[z].personnage.cuenta.juegoThread == null) continue
            enviar(f.getFighters(2)[z].personnage.cuenta.juegoThread._out, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fighter ID " + f._id + ": Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_ON_FIGHTER_KICK(f: Pelea, guid: Int, team: Int) {
        val packet = "GM|-$guid"
        for (F in f.getFighters(team)) {
            if (F.personnage == null || F.personnage.cuenta.juegoThread == null || F.personnage.id == guid) continue
            enviar(F.personnage.cuenta.juegoThread._out, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fighter ID " + f._id + ": Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_ALTER_FIGHTER_MOUNT(fight: Pelea, fighter: Peleador, guid: Int, team: Int, otherteam: Int) {
        val packet = StringBuilder()
        packet.append("GM|-").append(guid).append(0x00.toChar()).append(fighter.getGmPacket('~'))
        for (F in fight.getFighters(team)) {
            if (F.personnage == null || F.personnage.cuenta.juegoThread == null || !F.personnage.isConectado) continue
            enviar(F.personnage.cuenta.juegoThread._out, packet.toString())
        }
        if (otherteam > -1) {
            for (F in fight.getFighters(otherteam)) {
                if (F.personnage == null || F.personnage.cuenta.juegoThread == null || !F.personnage.isConectado) continue
                enviar(F.personnage.cuenta.juegoThread._out, packet.toString())
            }
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight ID " + fight._id + ": Send>>" + packet)
    }

    @JvmStatic
	fun ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(map: Mapa, perso: Personaje) {
        val packet = "GM|+" + perso.parseToGM()
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map " + map.id + ": Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_DUEL_Y_AWAY(out: PrintWriter?, guid: Int) {
        val packet = "GA;903;$guid;o"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_DUEL_E_AWAY(out: PrintWriter?, guid: Int) {
        val packet = "GA;903;$guid;z"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_NEW_DUEL_TO_MAP(map: Mapa, guid: Int, guid2: Int) {
        val packet = "GA;900;$guid;$guid2"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map " + map.id + ": Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_CANCEL_DUEL_TO_MAP(map: Mapa, guid: Int, guid2: Int) {
        val packet = "GA;902;$guid;$guid2"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_START_DUEL_TO_MAP(map: Mapa, guid: Int, guid2: Int) {
        val packet = "GA;901;$guid;$guid2"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_FIGHT_COUNT(out: PrintWriter?, map: Mapa) {
        val packet = "fC" + map.nbrFight
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(fight: Pelea, teams: Int, state: Int, cancelBtn: Int, duel: Int, spec: Int, time: Long, type: Int) {
        val packet = StringBuilder()
        packet.append("GJK").append(state).append("|")
        packet.append(cancelBtn).append("|").append(duel).append("|")
        packet.append(spec).append("|").append(time).append("|").append(type)
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            enviar(f.personnage, packet.toString())
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(fight: Pelea, teams: Int, places: String, team: Int) {
        val packet = "GP$places|$team"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(map: Mapa) {
        val packet = "fC" + map.nbrFight
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(map: Mapa, arg1: Int, guid1: Int, guid2: Int, cell1: Int, str1: String?, cell2: Int, str2: String?) {
        val packet = StringBuilder()
        packet.append("Gc+").append(guid1).append(";").append(arg1).append("|").append(guid1).append(";").append(cell1).append(";").append(str1).append("|").append(guid2).append(";").append(cell2).append(";").append(str2)
        for (z in map.persos) enviar(z, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(p: Personaje?, map: Mapa?, arg1: Int, guid1: Int, guid2: Int, cell1: Int, str1: String?, cell2: Int, str2: String?) {
        val packet = StringBuilder()
        packet.append("Gc+").append(guid1).append(";").append(arg1).append("|").append(guid1).append(";").append(cell1).append(";").append(str1).append("|").append(guid2).append(";").append(cell2).append(";").append(str2)
        enviar(p, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(map: Mapa, guid: Int) {
        val packet = "Gc-$guid"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(map: Mapa, teamID: Int, perso: Peleador) {
        val packet = StringBuilder()
        packet.append("Gt").append(teamID).append("|+").append(perso.id).append(";").append(perso.packetsName).append(";").append(perso._lvl)
        for (z in map.persos) enviar(z, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(p: Personaje?, map: Mapa?, teamID: Int, perso: Peleador) {
        val packet = StringBuilder()
        packet.append("Gt").append(teamID).append("|+").append(perso.id).append(";").append(perso.packetsName).append(";").append(perso._lvl)
        enviar(p, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_REMOVE_IN_TEAM_PACKET_TO_MAP(map: Mapa, teamID: Int, perso: Peleador) {
        val packet = StringBuilder()
        packet.append("Gt").append(teamID).append("|-").append(perso.id).append(";").append(perso.packetsName).append(";").append(perso._lvl)
        for (z in map.persos) enviar(z, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(map: Mapa) {
        val packet = map.mobGroupGMsPackets // Un par un comme sa lors du respawn :)
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_MOBS_GM_PACKET(map: Mapa, current_Mobs: MobGroup) {
        var packet = "GM|"
        packet += current_Mobs.parseGM() // Un par un comme sa lors du respawn :)
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_GMS_PACKETS(map: Mapa, _perso: Personaje?) {
        val packet = map.gMsPackets
        enviar(_perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ON_EQUIP_ITEM(map: Mapa, _perso: Personaje) {
        val packet = _perso.parseToOa()
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ON_EQUIP_ITEM_FIGHT(_perso: Personaje, f: Peleador, F: Pelea) {
        val packet = _perso.parseToOa()
        for (z in F.getFighters(f.team2)) {
            if (z.personnage == null) continue
            enviar(z.personnage, packet)
        }
        for (z in F.getFighters(f.otherTeam)) {
            if (z.personnage == null) continue
            enviar(z.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(fight: Pelea, teams: Int, map: Mapa?, guid: Int, cell: Int) {
        val packet = "GIC|$guid;$cell;1"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(map: Mapa, s: Char, option: Char, guid: Int) {
        val packet = "Go$s$option$guid"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(fight: Pelea, teams: Int, guid: Int, b: Boolean) {
        val packet = "GR" + (if (b) "1" else "0") + guid
        if (fight._state != 2) return
        for (f in fight.getFighters(teams)) {
            if (f.personnage == null || !f.personnage.isConectado) continue
            if (f.hasLeft()) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GJK_PACKET(out: Personaje?, state: Int, cancelBtn: Int, duel: Int, spec: Int, time: Long, unknown: Int) {
        val packet = StringBuilder()
        packet.append("GJK").append(state).append("|").append(cancelBtn).append("|").append(duel).append("|").append(spec).append("|").append(time).append("|").append(unknown)
        enviar(out, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_PLACES_PACKET(out: PrintWriter?, places: String, team: Int) {
        val packet = "GP$places|$team"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_MENSAJE_DESDE_LANG_A_TODOS(str: String) {
        val packet = "Im$str"
        for (perso in Mundo.getOnlinePersos()) enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_MENSAJE_DESDE_LANG(out: Personaje?, str: String) {
        val packet = "Im$str"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ILS_PACKET(out: Personaje?, i: Int) {
        val packet = "ILS$i"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ILF_PACKET(P: Personaje?, i: Int) {
        val packet = "ILF$i"
        enviar(P, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_MENSAJE_DESDE_LANG_AL_MAPA(map: Mapa, id: String) {
        val packet = "Im$id"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_eUK_PACKET_TO_MAP(map: Mapa, guid: Int, emote: Int) {
        val packet = "eUK$guid|$emote"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(fight: Pelea, teams: Int, id: String) {
        val packet = "Im$id"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MESSAGE(out: Personaje?, mess: String, color: String) {
        val packet = "cs<font color='#$color'>$mess</font>"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MESSAGE_TO_MAP(map: Mapa, mess: String, color: String) {
        val packet = "cs<font color='#$color'>$mess</font>"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GA903_ERROR_PACKET(out: PrintWriter?, c: Char, guid: Int) {
        val packet = "GA;903;$guid;$c"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GIC_PACKETS_TO_FIGHT(fight: Pelea, teams: Int) {
        val packet = StringBuilder()
        packet.append("GIC|")
        for (p in fight.getFighters(3)) {
            if (p._fightCell == null) continue
            packet.append(p.id).append(";").append(p._fightCell.id).append(";1|")
        }
        for (perso in fight.getFighters(teams)) {
            if (perso.hasLeft()) continue
            if (perso.personnage == null || !perso.personnage.isConectado) continue
            enviar(perso.personnage, packet.toString())
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GIC_PACKET_TO_FIGHT(fight: Pelea, teams: Int, f: Peleador) {
        val packet = StringBuilder()
        packet.append("GIC|").append(f.id).append(";").append(f._fightCell.id).append(";1|")
        for (perso in fight.getFighters(teams)) {
            if (perso.hasLeft()) continue
            if (perso.personnage == null || !perso.personnage.isConectado) continue
            enviar(perso.personnage, packet.toString())
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GS_PACKET_TO_FIGHT(fight: Pelea, teams: Int) {
        val packet = "GS"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            f.initBuffStats()
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GS_PACKET(out: Personaje?) {
        val packet = "GS"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GTL_PACKET_TO_FIGHT(fight: Pelea, teams: Int) {
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, fight.gtl)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>" + fight.gtl)
    }

    @JvmStatic
	fun GAME_SEND_GTL_PACKET(out: Personaje?, fight: Pelea) {
        val packet = fight.gtl
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GTM_PACKET_TO_FIGHT(fight: Pelea, teams: Int) {
        val packet = StringBuilder()
        packet.append("GTM")
        for (f in fight.getFighters(3)) {
            packet.append("|").append(f.id).append(";")
            if (f.isDead) {
                packet.append("1")
                continue
            } else packet.append("0;").append(f.pdv).append(";").append(f.pa).append(";").append(f.pm).append(";")
            packet.append(if (f.isHide) "-1" else f._fightCell.id).append(";") //On envoie pas la cell d'un invisible :p
            packet.append(";") //??
            packet.append(f.pdvmax)
        }
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet.toString())
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(fight: Pelea, teams: Int, guid: Int, time: Int) {
        val packet = "GTS$guid|$time"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAMETURNSTART_PACKET(P: Personaje?, guid: Int, time: Int) {
        val packet = "GTS$guid|$time"
        enviar(P, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GV_PACKET(P: Personaje?) {
        val packet = "GV"
        enviar(P, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PONG(out: PrintWriter?) {
        val packet = "pong"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_QPONG(out: PrintWriter?) {
        val packet = "qpong"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAS_PACKET_TO_FIGHT(fight: Pelea, teams: Int, guid: Int) {
        val packet = "GAS$guid"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_GA_PACKET_TO_FIGHT(fight: Pelea, teams: Int, actionID: Int, s1: String, s2: String) {
        var packet = "GA;$actionID;$s1"
        if (s2 != "") packet += ";$s2"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight(" + fight.getFighters(teams).size + ") : Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_GA_PACKET(out: PrintWriter?, actionID: String, s0: String, s1: String, s2: String) {
        var packet = "GA$actionID;$s0"
        if (s1 != "") packet += ";$s1"
        if (s2 != "") packet += ";$s2"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GA_PACKET_TO_FIGHT(fight: Pelea, teams: Int, gameActionID: Int, s1: String, s2: String, s3: String) {
        val packet = "GA$gameActionID;$s1;$s2;$s3"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAMEACTION_TO_FIGHT(fight: Pelea, teams: Int, packet: String) {
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAF_PACKET_TO_FIGHT(fight: Pelea, teams: Int, i1: Int, guid: Int) {
        val packet = "GAF$i1|$guid"
        for (f in fight.getFighters(teams)) {
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_BN(out: Personaje?) {
        val packet = "BN"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_BN(out: PrintWriter?) {
        val packet = "BN"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(fight: Pelea, teams: Int, guid: Int) {
        val packet = "GTF$guid"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GTR_PACKET_TO_FIGHT(fight: Pelea, teams: Int, guid: Int) {
        val packet = "GTR$guid"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EMOTICONE_TO_MAP(map: Mapa, guid: Int, id: Int) {
        val packet = "cS$guid|$id"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_SPELL_UPGRADE_FAILED(_out: PrintWriter?) {
        val packet = "SUE"
        enviar(_out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_SPELL_UPGRADE_SUCCED(_out: PrintWriter?, spellID: Int, level: Int) {
        val packet = "SUK$spellID~$level"
        enviar(_out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_SPELL_LIST(perso: Personaje) {
        val packet = perso.parseSpellList()
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(fight: Pelea, teams: Int, guid: Int) {
        val packet = "GA;103;$guid;$guid"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft() || f.personnage == null) continue
            if (f.personnage.isConectado) enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_GE_PACKET_TO_FIGHT(fight: Pelea, teams: Int, win: Int) {
        val packet = fight.GetGE(win)
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft() || f.personnage == null) continue
            if (f.personnage.isConectado) enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_FIGHT_GE_PACKET(out: PrintWriter?, fight: Pelea, win: Int) {
        val packet = fight.GetGE(win)
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_GIE_TO_FIGHT(fight: Pelea, teams: Int, mType: Int, cible: Int, value: Int, mParam2: String?, mParam3: String?, mParam4: String?, turn: Int, spellID: Int) {
        val packet = StringBuilder()
        packet.append("GIE").append(mType).append(";").append(cible).append(";").append(value).append(";").append(mParam2).append(";").append(mParam3).append(";").append(mParam4).append(";").append(turn).append(";").append(spellID)
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft() || f.personnage == null) continue
            if (f.personnage.isConectado) enviar(f.personnage, packet.toString())
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(fight: Pelea, teams: Int, map: Mapa) {
        val packet = map.fightersGMsPackets
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MAP_FIGHT_GMS_PACKETS(fight: Pelea?, map: Mapa, _perso: Personaje?) {
        val packet = map.fightersGMsPackets
        enviar(_perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_PLAYER_JOIN(fight: Pelea, teams: Int, _fighter: Peleador) {
        val packet = _fighter.getGmPacket('+')
        for (f in fight.getFighters(teams)) {
            if (f !== _fighter) {
                if (f.personnage == null || !f.personnage.isConectado) continue
                if (f.personnage != null && f.personnage.cuenta.juegoThread != null) enviar(f.personnage, packet)
            }
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET(perso: Personaje?, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_LIST_PACKET(out: PrintWriter?, map: Mapa) {
        val packet = StringBuilder()
        packet.append("fL")
        for ((_, value) in map._fights) {
            if (packet.length > 2) {
                packet.append("|")
            }
            packet.append(value.parseFightInfos())
        }
        enviar(out, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET_TO_MAP(map: Mapa, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET_TO_GUILD(g: Gremio, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (perso in g.members) {
            if (perso == null || !perso.isConectado) continue
            enviar(perso, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Guild: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET_TO_ALL(suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (perso in Mundo.getOnlinePersos()) enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: ALL(" + Mundo.getOnlinePersos().size + "): Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET_TO_ALIGN(suffix: String, guid: Int, name: String, msg: String, _perso: Personaje) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (perso in Mundo.getOnlinePersos()) {
            if (perso._align == _perso._align) {
                enviar(perso, packet)
            }
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: ALL(" + Mundo.getOnlinePersos().size + "): Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET_TO_ADMIN(suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        for (perso in Mundo.getOnlinePersos()) if (perso.isConectado) if (perso.cuenta != null) if (perso.cuenta.gmlvl > 0) enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: ALL(" + Mundo.getOnlinePersos().size + "): Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET_TO_FIGHT(fight: Pelea?, teams: Int, suffix: String, guid: Int, name: String, msg: String) {
        fight?.ticMyTimer()
        val packet = "cMK$suffix|$guid|$name|$msg"
        assert(fight != null)
        for (f in fight!!.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GDZ_PACKET_TO_FIGHT(fight: Pelea, teams: Int, suffix: String, cell: Int, size: Int, unk: Int) {
        val packet = "GDZ$suffix$cell;$size;$unk"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GDC_PACKET_TO_FIGHT(fight: Pelea, teams: Int, cell: Int) {
        val packet = "GDC$cell"
        for (f in fight.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GA2_PACKET(out: PrintWriter?, guid: Int) {
        val packet = "GA;2;$guid;"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_CHAT_ERROR_PACKET(out: PrintWriter?, name: String) {
        val packet = "cMEf$name"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_eD_PACKET_TO_MAP(map: Mapa, guid: Int, dir: Int) {
        val packet = "eD$guid|$dir"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_ECK_PACKET(out: Personaje?, type: Int, str: String) {
        var packet = "ECK$type"
        if (str != "") packet += "|$str"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ECK_PACKET(out: PrintWriter?, type: Int, str: String) {
        var packet = "ECK$type"
        if (str != "") packet += "|$str"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ITEM_VENDOR_LIST_PACKET(out: PrintWriter?, npc: NPC) {
        val packet = "EL" + npc.modelo.itemVendorList
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(out: PrintWriter?, perco: Recaudador) {
        val packet = "EL" + perco.itemPercepteurList
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ITEM_LIST_PACKET_SELLER(p: Personaje, out: Personaje?) {
        val packet = "EL" + p.parseStoreItemsList()
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EV_PACKET(out: PrintWriter?) {
        val packet = "EV"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_DCK_PACKET(out: PrintWriter?, id: Int) {
        val packet = "DCK$id"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_QUESTION_PACKET(out: PrintWriter?, str: String) {
        val packet = "DQ$str"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_END_DIALOG_PACKET(out: PrintWriter?) {
        val packet = "DV"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_TEXTO_EN_CONSOLA(out: PrintWriter?, mess: String) {
        val packet = "BAT2$mess"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_BUY_ERROR_PACKET(out: PrintWriter?) {
        val packet = "EBE"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_SELL_ERROR_PACKET(out: PrintWriter?) {
        val packet = "ESE"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_BUY_OK_PACKET(out: PrintWriter?) {
        val packet = "EBK"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_OBJECT_QUANTITY_PACKET(out: Personaje?, obj: Objeto) {
        val packet = "OQ" + obj.id + "|" + obj.quantity
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_OAKO_PACKET(out: Personaje?, obj: Objeto) {
        val packet = "OAKO" + obj.parseItem()
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ESK_PACKEt(out: Personaje?) {
        val packet = "ESK"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_REMOVE_ITEM_PACKET(out: Personaje?, guid: Int) {
        val packet = "OR$guid"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_DELETE_OBJECT_FAILED_PACKET(out: PrintWriter?) {
        val packet = "OdE"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_OBJET_MOVE_PACKET(out: Personaje?, obj: Objeto) {
        var packet = "OM" + obj.id + "|"
        if (obj.position != Constantes.ITEM_POS_NO_EQUIPED) packet += obj.position
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EMOTICONE_TO_FIGHT(fight: Pelea?, teams: Int, guid: Int, id: Int) {
        fight?.ticMyTimer()
        val packet = "cS$guid|$id"
        assert(fight != null)
        for (f in fight!!.getFighters(teams)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_OAEL_PACKET(out: PrintWriter?) {
        val packet = "OAEL"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_NEW_LVL_PACKET(out: PrintWriter?, lvl: Int) {
        val packet = "AN$lvl"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_MENSAJE_A_TODOS(msg: String, color: String) {
        val packet = "cs<font color='#$color'>$msg</font>"
        for (P in Mundo.getOnlinePersos()) {
            enviar(P, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: ALL: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EXCHANGE_REQUEST_OK(out: PrintWriter?, guid: Int, guidT: Int, msgID: Int) {
        val packet = "ERK$guid|$guidT|$msgID"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EXCHANGE_REQUEST_ERROR(out: PrintWriter?, c: Char) {
        val packet = "ERE$c"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EXCHANGE_CONFIRM_OK(out: PrintWriter?, type: Int) {
        val packet = "ECK$type"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EXCHANGE_MOVE_OK(out: Personaje?, type: Char, signe: String, s1: String) {
        var packet = "EMK$type$signe"
        if (s1 != "") packet += s1
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EXCHANGE_OTHER_MOVE_OK(out: PrintWriter?, type: Char, signe: String, s1: String) {
        var packet = "EmK$type$signe"
        if (s1 != "") packet += s1
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_INTERCAMBIO_EXITOSO(out: PrintWriter?, ok: Boolean, guid: Int) {
        val packet = "EK" + (if (ok) "1" else "0") + guid
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EXCHANGE_VALID(out: PrintWriter?, c: Char) {
        val packet = "EV$c"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GROUP_INVITATION_ERROR(out: PrintWriter?, s: String) {
        val packet = "PIE$s"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GROUP_INVITATION(out: PrintWriter?, n1: String, n2: String) {
        val packet = "PIK$n1|$n2"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GROUP_CREATE(out: PrintWriter?, g: Grupo) {
        val packet = "PCK" + g.chief.nombre
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PL_PACKET(out: PrintWriter?, g: Grupo) {
        val packet = "PL" + g.chief.id
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PR_PACKET(out: Personaje?) {
        val packet = "PR"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PV_PACKET(out: PrintWriter?, s: String) {
        val packet = "PV$s"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ALL_PM_ADD_PACKET(out: PrintWriter?, g: Grupo) {
        val packet = StringBuilder()
        packet.append("PM+")
        var first = true
        for (p in g.miembrosGrupo) {
            if (!first) packet.append("|")
            packet.append(p.parseToPM())
            first = false
        }
        enviar(out, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PM_ADD_PACKET_TO_GROUP(g: Grupo, p: Personaje) {
        val packet = "PM+" + p.parseToPM()
        for (P in g.miembrosGrupo) enviar(P, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PM_MOD_PACKET_TO_GROUP(g: Grupo, p: Personaje) {
        val packet = "PM~" + p.parseToPM()
        for (P in g.miembrosGrupo) enviar(P, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PM_DEL_PACKET_TO_GROUP(g: Grupo, guid: Int) {
        val packet = "PM-$guid"
        for (P in g.miembrosGrupo) enviar(P, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET_TO_GROUP(g: Grupo, s: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$s|$guid|$name|$msg|"
        for (P in g.miembrosGrupo) enviar(P, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Groupe: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_DETAILS(out: PrintWriter?, fight: Pelea?) {
        if (fight == null) return
        val packet = StringBuilder()
        packet.append("fD").append(fight._id).append("|")
        for (f in fight.getFighters(1)) packet.append(f.packetsName).append("~").append(f._lvl).append(";")
        packet.append("|")
        for (f in fight.getFighters(2)) packet.append(f.packetsName).append("~").append(f._lvl).append(";")
        enviar(out, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_IQ_PACKET(perso: Personaje?, guid: Int, qua: Int) {
        val packet = "IQ$guid|$qua"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_JN_PACKET(perso: Personaje?, jobID: Int, lvl: Int) {
        val packet = "JN$jobID|$lvl"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GDF_PACKET_TO_MAP(map: Mapa, cell: Mapa.Case) {
        val cellID = cell.id
        val `object` = cell.getObject()
        val packet = "GDF|" + cellID + ";" + `object`.state + ";" + if (`object`.isInteractive) "1" else "0"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GA_PACKET_TO_MAP(map: Mapa, gameActionID: String, actionID: Int, s1: String, s2: String) {
        var packet = "GA$gameActionID;$actionID;$s1"
        if (s2 != "") packet += ";$s2"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EL_BANK_PACKET(perso: Personaje) {
        val packet = "EL" + perso.parseBankPacket()
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_PAQUETE_COFRE(perso: Personaje?, t: Cofres) {
        val packet = "EL" + t.parseToTrunkPacket()
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_JX_PACKET(perso: Personaje?, SMs: ArrayList<StatsMetier>) {
        val packet = StringBuilder()
        packet.append("JX")
        for (sm in SMs) {
            packet.append("|").append(sm.template.id).append(";").append(sm._lvl).append(";").append(sm.getXpString(";")).append(";")
        }
        enviar(perso, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_JO_PACKET(perso: Personaje?, SMs: ArrayList<StatsMetier>) {
        for (sm in SMs) {
            val packet = "JO" + sm.id + "|" + sm.optBinValue + "|2" //FIXME 2=?
            enviar(perso, packet)
            if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
        }
    }

    @JvmStatic
	fun GAME_SEND_JS_PACKET(perso: Personaje?, SMs: ArrayList<StatsMetier>) {
        val packet = StringBuilder("JS")
        for (sm in SMs) {
            packet.append(sm.parseJS())
        }
        enviar(perso, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EsK_PACKET(perso: Personaje?, str: String) {
        val packet = "EsK$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FIGHT_SHOW_CASE(PWs: ArrayList<PrintWriter?>, guid: Int, cellID: Int) {
        val packet = "Gf$guid|$cellID"
        for (PW in PWs) {
            enviar(PW, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Ea_PACKET(perso: Personaje?, str: String) {
        val packet = "Ea$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EA_PACKET(perso: Personaje?, str: String) {
        val packet = "EA$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Ec_PACKET(perso: Personaje?, str: String) {
        val packet = "Ec$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Em_PACKET(perso: Personaje?, str: String) {
        val packet = "Em$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_IO_PACKET_TO_MAP(map: Mapa, guid: Int, str: String) {
        val packet = "IO$guid|$str"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FRIENDLIST_PACKET(perso: Personaje) {
        val packet = "FL" + perso.cuenta.parseFriendList()
        enviar(perso, packet)
        if (perso.wife != 0) {
            val packet2 = "FS" + perso._wife_friendlist
            enviar(perso, packet2)
            if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet2")
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FRIEND_ONLINE(logando: Personaje, amigo: Personaje?) {
        val packet = "Im0143;" + logando.cuenta.apodo + " (<b><a href='asfunction:onHref,ShowPlayerPopupMenu," + logando.nombre + "'>" + logando.nombre + "</a></b>)"
        enviar(amigo, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FA_PACKET(perso: Personaje?, str: String) {
        val packet = "FA$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FD_PACKET(perso: Personaje?, str: String) {
        val packet = "FD$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Rp_PACKET(perso: Personaje?, MP: MountPark?) {
        val packet = StringBuilder()
        if (MP == null) return
        packet.append("Rp").append(MP._owner).append(";").append(MP._price).append(";").append(MP._size).append(";").append(MP.objectNumb).append(";")
        val G = MP.gremio
        //Si une guilde est definie
        if (G != null) {
            packet.append(G._name).append(";").append(G._emblem)
        } else {
            packet.append(";")
        }
        enviar(perso, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_OS_PACKET(perso: Personaje, pano: Int) {
        val packet = StringBuilder()
        packet.append("OS")
        val num = perso.getNumbEquipedItemOfPanoplie(pano)
        if (num <= 0) packet.append("-").append(pano) else {
            packet.append("+").append(pano).append("|")
            val IS = Mundo.getItemSet(pano)
            if (IS != null) {
                val items = StringBuilder()
                //Pour chaque objet de la pano
                for (OT in IS.itemTemplates) {
                    //Si le joueur l'a équipé
                    if (perso.hasEquiped(OT.id)) {
                        //On l'ajoute au packet
                        if (items.length > 0) items.append(";")
                        items.append(OT.id)
                    }
                }
                packet.append(items.toString()).append("|").append(IS.getBonusStatByItemNumb(num).parseToItemSetStats())
            }
        }
        enviar(perso, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_PAQUETE_DESCRIPCION_DE_MONTURA(personaje: Personaje?, dragopavo: Dragopavo) {
        val packet = "Rd" + dragopavo.parse()
        enviar(personaje, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Rr_PACKET(perso: Personaje?, str: String) {
        val packet = "Rr$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ALTER_GM_PACKET(map: Mapa, perso: Personaje) {
        val packet = "GM|~" + perso.parseToGM()
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_Ee_PACKET(perso: Personaje?, c: Char, s: String) {
        val packet = "Ee$c$s"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_cC_PACKET(perso: Personaje?, c: Char, s: String) {
        val packet = "cC$c$s"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun ENVIAR_AGREGAR_NPC_EN_MAPA(map: Mapa, npc: NPC) {
        for (z in map.persos) {
            val packet = "GM|" + npc.parseGM(z)
            enviar(z, packet)
            if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
        }
    }

    @JvmStatic
	fun ENVIAR_AGREGAR_RECAUDADOR_EN_MAPA(map: Mapa) {
        val packet = "GM|" + Recaudador.parseGM(map)
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GDO_PACKET_TO_MAP(map: Mapa, c: Char, cell: Int, itm: Int, i: Int) {
        val packet = "GDO$c$cell;$itm;$i"
        for (z in map.persos) enviar(z, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Map: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GDO_PACKET(p: Personaje?, c: Char, cell: Int, itm: Int, i: Int) {
        val packet = "GDO$c$cell;$itm;$i"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ZC_PACKET(p: Personaje?, a: Int) {
        val packet = "ZC$a"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GIP_PACKET(p: Personaje?, a: Int) {
        val packet = "GIP$a"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gn_PACKET(p: Personaje?) {
        val packet = "gn"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gC_PACKET(p: Personaje?, s: String) {
        val packet = "gC$s"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gV_PACKET(p: Personaje?) {
        val packet = "gV"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gIM_PACKET(p: Personaje?, g: Gremio, c: Char) {
        var packet = "gIM$c"
        if (c == '+') {
            packet += g.parseMembersToGM()
        }
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gIB_PACKET(p: Personaje?, infos: String) {
        val packet = "gIB$infos"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gIH_PACKET(p: Personaje?, infos: String) {
        val packet = "gIH$infos"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gS_PACKET(p: Personaje?, gm: GuildMember) {
        val packet = StringBuilder()
        packet.append("gS").append(gm.guild._name).append("|").append(gm.guild._emblem.replace(',', '|')).append("|").append(gm.parseRights())
        enviar(p, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gJ_PACKET(p: Personaje?, str: String) {
        val packet = "gJ$str"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gK_PACKET(p: Personaje?, str: String) {
        val packet = "gK$str"
        enviar(p, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gIG_PACKET(p: Personaje?, g: Gremio) {
        val xpMin = Mundo.getExpLevel(g._lvl).guilde
        val xpMax: Long
        xpMax = if (Mundo.getExpLevel(g._lvl + 1) == null) {
            -1
        } else {
            Mundo.getExpLevel(g._lvl + 1).guilde
        }
        val packet = StringBuilder()
        packet.append("gIG").append(if (g.size > 9) 1 else 0).append("|").append(g._lvl).append("|").append(xpMin).append("|").append(g._xp).append("|").append(xpMax)
        enviar(p, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_MESSAGE(out: PrintWriter?, args: String) {
        val packet = "M$args"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_WC_PACKET(perso: Personaje) {
        val packet = "WC" + perso.parseZaapList()
        enviar(perso.cuenta.juegoThread._out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_WV_PACKET(out: Personaje?) {
        val packet = "WV"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ZAAPI_PACKET(perso: Personaje, list: String) {
        val packet = "Wc" + perso.actualMapa.id + "|" + list
        enviar(perso, packet)
        JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_CLOSE_ZAAPI_PACKET(out: Personaje?) {
        val packet = "Wv"
        enviar(out, packet)
        JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_WUE_PACKET(out: Personaje?) {
        val packet = "WUE"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EMOTE_LIST(perso: Personaje?, s: String, s1: String) {
        val packet = "eL$s|$s1"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_NO_EMOTE(out: Personaje?) {
        val packet = "eUE"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun REALM_SEND_TOO_MANY_PLAYER_ERROR(out: PrintWriter?) {
        val packet = "AlEw"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun REALM_SEND_REQUIRED_APK(out: PrintWriter?) {
        val chars = "abcdefghijklmnopqrstuvwxyz" // Tu supprimes les lettres dont tu ne veux pas
        val pass = StringBuilder()
        for (x in 0..4) {
            val i = Math.floor(Math.random() * 26).toInt() // Si tu supprimes des lettres tu diminues ce nb
            pass.append(chars[i])
        }
        println(pass)
        val packet = "APK$pass"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ADD_ENEMY(out: Personaje?, pr: Personaje) {
        val packet = "iAK" + pr.cuenta.nombre + ";2;" + pr.nombre + ";36;10;0;100.FL."
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_iAEA_PACKET(out: Personaje?) {
        val packet = "iAEA."
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_ENEMY_LIST(perso: Personaje) {
        val packet = "iL" + perso.cuenta.parseEnemyList()
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_iD_COMMANDE(perso: Personaje?, str: String) {
        val packet = "iD$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_BWK(perso: Personaje?, str: String) {
        val packet = "BWK$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun EVIAR_CODIGO(perso: Personaje?, str: String) {
        val packet = "K$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_hOUSE(perso: Personaje?, str: String) {
        val packet = "h$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FORGETSPELL_INTERFACE(sign: Char, perso: Personaje?) {
        val packet = "SF$sign"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_R_PACKET(perso: Personaje?, str: String) {
        val packet = "R$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gIF_PACKET(perso: Personaje?, str: String) {
        val packet = "gIF$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gITM_PACKET(perso: Personaje?, str: String) {
        val packet = "gITM$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gITp_PACKET(perso: Personaje?, str: String) {
        val packet = "gITp$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gITP_PACKET(perso: Personaje?, str: String) {
        val packet = "gITP$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_IH_PACKET(perso: Personaje?, str: String) {
        val packet = "IH$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_FLAG_PACKET(perso: Personaje?, cible: Personaje) {
        val packet = "IC" + cible.actualMapa.x + "|" + cible.actualMapa.y
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_DELETE_FLAG_PACKET(perso: Personaje?) {
        val packet = "IC|"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_gT_PACKET(perso: Personaje?, str: String) {
        val packet = "gT$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GUILDHOUSE_PACKET(perso: Personaje?) {
        val packet = "gUT"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_GUILDENCLO_PACKET(perso: Personaje?) {
        val packet = "gUF"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    //Mercadillos
	@JvmStatic
	fun GAME_SEND_EHm_PACKET(out: Personaje?, sign: String, str: String) {
        val packet = "EHm$sign$str"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EHM_PACKET(out: Personaje?, sign: String, str: String) {
        val packet = "EHM$sign$str"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EHP_PACKET(out: Personaje?, templateID: Int) //Packet d'envoie du prix moyen du template (En réponse a un packet EHP)
    {
        val packet = "EHP" + templateID + "|" + Mundo.getObjTemplate(templateID).avgPrice
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_EHl(out: Personaje?, seller: Mercadillo, templateID: Int) {
        val packet = "EHl" + seller.parseToEHl(templateID)
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EHL_PACKET(out: Personaje?, categ: Int, templates: String) //Packet de listage des templates dans une catégorie (En réponse au packet EHT)
    {
        val packet = "EHL$categ|$templates"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
    fun GAME_SEND_EHL_PACKET(out: Personaje?, items: String) //Packet de listage des objets en vente
    {
        val packet = "EHL$items"
        enviar(out, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_HDVITEM_SELLING(perso: Personaje) {
        val packet = StringBuilder("EL")
        val entries = perso.cuenta.getMercadilloObjetos(Math.abs(perso._isTradingWith)) //Récupère un tableau de tout les items que le personnage à en vente dans l'HDV où il est
        var isFirst = true
        for (curEntry in entries) {
            if (curEntry == null) break
            if (!isFirst) packet.append("|")
            packet.append(curEntry.parseToEL())
            isFirst = false
        }
        enviar(perso, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_WEDDING(c: Mapa?, action: Int, homme: Int, femme: Int, parlant: Int) {
        val packet = "GA;$action;$homme;$homme,$femme,$parlant"
        val Homme = Mundo.getPersonnage(homme)
        enviar(Homme, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_PF(perso: Personaje?, str: String) {
        val packet = "PF$str"
        enviar(perso, packet)
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_MERCHANT_LIST(P: Personaje, mapID: Short) {
        val packet = StringBuilder()
        packet.append("GM|~")
        if (Mundo.getSeller(P.actualMapa.id) == null) return
        for (pID in Mundo.getSeller(P.actualMapa.id)) {
            if (!Mundo.getPersonnage(pID!!).isConectado && Mundo.getPersonnage(pID).is_showSeller) {
                packet.append(Mundo.getPersonnage(pID).parseToMerchant()).append("|")
            }
        }
        if (packet.length < 5) return
        enviar(P, packet.toString())
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Send>>$packet")
    }

    @JvmStatic
	fun GAME_SEND_cMK_PACKET_INCARNAM_CHAT(perso: Personaje, suffix: String, guid: Int, name: String, msg: String) {
        val packet = "cMK$suffix|$guid|$name|$msg"
        if (perso._lvl > 15) {
            GAME_SEND_BN(perso)
            return
        }
        for (perso1 in Mundo.getOnlinePersos()) {
            enviar(perso1, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: ALL(" + Mundo.getOnlinePersos().size + "): Send>>" + packet)
    }

    @JvmStatic
	fun GAME_SEND_PACKET_TO_FIGHT(fight: Pelea, i: Int, packet: String) {
        for (f in fight.getFighters(i)) {
            if (f.hasLeft()) continue
            if (f.personnage == null || !f.personnage.isConectado) continue
            enviar(f.personnage, packet)
        }
        if (MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToSockLog("Game: Fight : Send>>$packet")
    }
}