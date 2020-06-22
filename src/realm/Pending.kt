package realm

import comunes.GestorSalida
import objetos.Cuenta

object Pending {

    //TODO : Gestion du paquet Af + position dans la file d'attente.

	@JvmStatic
	fun sistema_de_pendientes(C: Cuenta?) {
        if (C == null) return
        if (C._position <= 1) {
            try {
                Thread.sleep(750)
                if (C == null || C.realmThread._imprimir == null) return
                GestorSalida.MULTI_SEND_Af_PACKET(C.realmThread._imprimir, 1, RealmServer.totalabonado, RealmServer.totalnoabonado, "" + 1, RealmServer.idcola)
                RealmServer.totalabonado--
            } catch (e: InterruptedException) {
                GestorSalida.ENVIAR_ESTA_CONECTADO(C.realmThread._imprimir)
                RealmServer.agregar_a_los_logs("Erreur : " + e.message)
            }
        } else {
            try {
                Thread.sleep(750 * C._position.toLong())
                if (C == null || C.realmThread._imprimir == null) return
                GestorSalida.MULTI_SEND_Af_PACKET(C.realmThread._imprimir, 1, RealmServer.totalabonado, RealmServer.totalnoabonado, "" + 1, RealmServer.idcola)
                RealmServer.totalabonado--
            } catch (e: InterruptedException) {
                GestorSalida.ENVIAR_ESTA_CONECTADO(C.realmThread._imprimir)
                RealmServer.agregar_a_los_logs("Erreur : " + e.message)
            }
        }
    }
}