package realm

import comunes.GestorSalida
import objetos.Cuenta

object Pending {

    //TODO : Gestion du paquet Af + position dans la file d'attente.

	@JvmStatic
	fun PendingSystem(C: Cuenta?) {
        if (C == null) return
        if (C._position <= 1) {
            try {
                Thread.sleep(750)
                if (C == null || C.realmThread._out == null) return
                GestorSalida.MULTI_SEND_Af_PACKET(C.realmThread._out, 1, RealmServer.totalabonado, RealmServer.totalnoabonado, "" + 1, RealmServer.idcola)
                C._position = -1
                RealmServer.totalabonado--
            } catch (e: InterruptedException) {
                GestorSalida.REALM_SEND_ALREADY_CONNECTED(C.realmThread._out)
                RealmServer.addToLog("Erreur : " + e.message)
            }
        } else {
            try {
                Thread.sleep(750 * C._position.toLong())
                if (C == null || C.realmThread._out == null) return
                GestorSalida.MULTI_SEND_Af_PACKET(C.realmThread._out, 1, RealmServer.totalabonado, RealmServer.totalnoabonado, "" + 1, RealmServer.idcola)
                C._position = -1
                RealmServer.totalabonado--
            } catch (e: InterruptedException) {
                GestorSalida.REALM_SEND_ALREADY_CONNECTED(C.realmThread._out)
                RealmServer.addToLog("Erreur : " + e.message)
            }
        }
    }
}