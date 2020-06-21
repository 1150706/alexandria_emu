package realm

import comunes.MainServidor
import comunes.MainServidor.closeServers
import java.io.IOException
import java.net.ServerSocket
import java.util.*

class RealmServer : Runnable {

    private var serversocket: ServerSocket? = null
    var thread: Thread? = null

    override fun run() {
        while (MainServidor.isRunning) {//bloque sur _SS.accept()
            try {
                RealmThread(serversocket!!.accept())
            } catch (e: IOException) {
                agregar_a_los_logs("IOException: " + e.message)
                try {
                    agregar_a_los_logs("Fermeture du serveur de connexion")
                    if (!serversocket!!.isClosed) serversocket!!.close()
                } catch (e1: IOException) {
                }
            }
        }
    }

    fun kickAll() {
        try {
            serversocket!!.close()
        } catch (e: IOException) {
        }
    }

    companion object {
        var totalnoabonado = 0 //Total de no abonados conectados
        @JvmField
		var totalabonado = 0 //Total de abonados conectados
        var idcola = -1 //Numero de id en cola
        var abonado = 1 //Fila de no abonados (0) - Fila de abonados (1)

        @JvmStatic
		@Synchronized
        fun agregar_a_los_logs(str: String) {
            println(str)
            if (MainServidor.canLog) {
                try {
                    val date = Calendar.HOUR_OF_DAY.toString() + ":" + Calendar.MINUTE + ":" + Calendar.SECOND
                    MainServidor.Log_Realm!!.write("$date: $str")
                    MainServidor.Log_Realm!!.newLine()
                    MainServidor.Log_Realm!!.flush()
                } catch (e: IOException) {
                } //ne devrait pas avoir lieu
            }
        }

        @JvmStatic
		@Synchronized
        fun addToSockLog(str: String) {
            if (MainServidor.MOSTRAR_ENVIADOS) println(str)
            if (MainServidor.canLog) {
                try {
                    val date = Calendar.HOUR_OF_DAY.toString() + ":" + Calendar.MINUTE + ":" + Calendar.SECOND
                    MainServidor.Log_RealmSock!!.write("$date: $str")
                    MainServidor.Log_RealmSock!!.newLine()
                    MainServidor.Log_RealmSock!!.flush()
                } catch (e: IOException) {
                } //ne devrait pas avoir lieu
            }
        }
    }

    init {
        try {
            serversocket = ServerSocket(MainServidor.PUERTO_DE_CONEXION)
            thread = Thread(this)
            thread!!.isDaemon = true
            thread!!.start()
        } catch (e: IOException) {
            agregar_a_los_logs("IOException: " + e.message)
            closeServers()
        }
    }
}