package juego

import comunes.GestorEncriptador
import comunes.GestorSQL.cargar_acciones
import comunes.GestorSalida
import comunes.MainServidor
import comunes.MainServidor.cerrarservidor
import comunes.Mundo
import objetos.Cuenta
import java.io.IOException
import java.net.ServerSocket
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class JuegoServidor(Ip: String?) : Runnable {
    private var serversocket: ServerSocket? = null
    var thread: Thread? = null
    val clientes = ArrayList<JuegoThread>()
    private val _esperando = ArrayList<Cuenta>()
    var startTime: Long = 0
    var maxPlayer = 0
        private set
    private var _publicidad: String? = null

    class SaveThread : Runnable {
        override fun run() {
            if (!MainServidor.isSaving) {
                GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("1164")
                Mundo.saveAll(null)
                GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("1165")
            }
        }
    }

    val playerNumber: Int
        get() = clientes.size

    override fun run() {
        while (MainServidor.isRunning) //bloque sur _SS.accept()
        {
            try {
                clientes.add(JuegoThread(serversocket!!.accept()))
                if (clientes.size > maxPlayer) maxPlayer = clientes.size
            } catch (e: IOException) {
                agregar_a_los_logs("IOException: " + e.message)
                try {
                    if (!serversocket!!.isClosed) serversocket!!.close()
                    cerrarservidor()
                } catch (ignored: IOException) {
                }
            }
        }
    }

    fun expulsaratodos() {
        try {
            serversocket!!.close()
        } catch (ignored: IOException) {
        }
        //Copie
        val c = ArrayList(clientes)
        for (GT in c) {
            try {
                GT.closeSocket()
            } catch (ignored: Exception) {
            }
        }
    }

    fun delClient(gameThread: JuegoThread?) {
        clientes.remove(gameThread)
        if (clientes.size > maxPlayer) maxPlayer = clientes.size
    }

    @Synchronized
    fun getWaitingCompte(guid: Int): Cuenta? {
        for (waiting in _esperando) {
            if (waiting.id == guid) return waiting
        }
        return null
    }

    @Synchronized
    fun delWaitingCompte(_compte: Cuenta?) {
        _esperando.remove(_compte)
    }

    @Synchronized
    fun addWaitingCompte(_compte: Cuenta) {
        _esperando.add(_compte)
    }

    class todoslosturnospelea : Runnable {
        var todoslosturnospelea: Timer? = null
        var ultimosturnospelea: Long = 0
        override fun run() {
            try {
                todoslosturnospelea = Timer()
                todoslosturnospelea!!.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        try {
                            try {
                                Mundo.ticAllFightersTurns()
                            } catch (e: Exception) {
                                GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "ERREUR FATAL ------ No2 ---- (rar) Dans: ticAllFightersTurns(); " + e.message)
                            }
                        } catch (e: Exception) {
                            println("--------------- ERROR! " + e.message)
                            return
                        }
                    }
                }
                        , 1500L, 1500L)
            } catch (ignored: Exception) {
            }
        }
    }

    companion object {
        private var nropublicidad = 0

        @JvmStatic
		@Synchronized
        fun agregar_a_los_logs(str: String?) {
            println(str)
            if (MainServidor.canLog) {
                try {
                    val date = Calendar.getInstance()[Calendar.HOUR_OF_DAY].toString() + ":" + Calendar.getInstance()[Calendar.MINUTE] + ":" + Calendar.getInstance()[Calendar.SECOND]
                    MainServidor.Log_Game!!.write("$date: $str")
                    MainServidor.Log_Game!!.newLine()
                    MainServidor.Log_Game!!.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                } //ne devrait pas avoir lieu
            }
        }

        @JvmStatic
		@Synchronized
        fun addToSockLog(str: String) {
            if (MainServidor.MOSTRAR_ENVIADOS) println(str)
            if (MainServidor.canLog) {
                try {
                    val date = Calendar.getInstance()[Calendar.HOUR_OF_DAY].toString() + ":" + Calendar.getInstance()[Calendar.MINUTE] + ":" + Calendar.getInstance()[Calendar.SECOND]
                    MainServidor.Log_GameSock!!.write("$date: $str")
                    MainServidor.Log_GameSock!!.newLine()
                    MainServidor.Log_GameSock!!.flush()
                } catch (ignored: IOException) {
                } //ne devrait pas avoir lieu
            }
        }

        @JvmStatic
		val serverTime: String
            get() {
                val actDate = Date()
                return "BT" + (actDate.time + 3600000)
            }

        @JvmStatic
		val serverDate: String
            get() {
                val actDate = Date()
                var dateFormat: DateFormat = SimpleDateFormat("dd")
                val jour = StringBuilder(dateFormat.format(actDate).toInt().toString() + "")
                while (jour.length < 2) {
                    jour.insert(0, "0")
                }
                dateFormat = SimpleDateFormat("MM")
                val mois = StringBuilder((dateFormat.format(actDate).toInt() - 1).toString() + "")
                while (mois.length < 2) {
                    mois.insert(0, "0")
                }
                dateFormat = SimpleDateFormat("yyyy")
                val annee = (dateFormat.format(actDate).toInt() - 1370).toString() + ""
                return "BD$annee|$mois|$jour"
            }
    }

    init {
        try {

            //Timer de guardado automatico
            val _tiempoguardado = Timer()
            _tiempoguardado.schedule(object : TimerTask() {
                override fun run() {
                    if (!MainServidor.isSaving) {
                        val t = Thread(SaveThread())
                        t.start()
                    }
                }
            }, MainServidor.TIEMPO_DE_GUARDADO.toLong(), MainServidor.TIEMPO_DE_GUARDADO.toLong())

            //Timer envio de publicidad automatica
            val _publicidadautomatica = Timer()
            _publicidadautomatica.schedule(object : TimerTask() {
                override fun run() {
                    //Si no tenemos publicidad agregada, no ejecutamos el script
                    if (Mundo.Publicidad.isEmpty()) {
                        return
                    }
                    //Listamos la cantidad de id desde la base de datos
                    _publicidad = Mundo.Publicidad[nropublicidad]
                    //Aumentamos +1 las id
                    nropublicidad++
                    //Imprimimos la publicidad desde el lang
                    GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("1241;$_publicidad")
                    if (MainServidor.MOSTRAR_ENVIADOS) {
                        agregar_a_los_logs("Enviando publicidad automatica")
                    }
                    //En caso de que la lista ya termine la retomamos
                    if (nropublicidad >= Mundo.Publicidad.size) {
                        nropublicidad = 0
                    }
                }
            }, MainServidor.TIEMPO_ENVIO_PUBLICIDAD_AUTOMATICA.toLong(), MainServidor.TIEMPO_ENVIO_PUBLICIDAD_AUTOMATICA.toLong())

            //Timer movimiento de moobs en mapa
            val _movimientodemonsters = Timer()
            _movimientodemonsters.schedule(object : TimerTask() {
                override fun run() {
                    //Tomamos la lista de mapas del juego
                    val mapas = ArrayList<Short>()
                    //Listamos los personajes online
                    for (player in Mundo.getOnlinePersos()) {
                        val map = player.actualMapa
                        //Si el personaje esta en un mapa donde tenemos moobs los movemos
                        if (!mapas.contains(map.id)) {
                            map.MovimientoDeMonstruosEnMapas()
                            mapas.add(map.id)
                            agregar_a_los_logs("Moviendo los monstruos en mapas donde se encuentran personajes")
                        }
                    }
                }
            }, MainServidor.TIEMPO_MOVIMIENTO_MONSTRUOS.toLong(), MainServidor.TIEMPO_MOVIMIENTO_MONSTRUOS.toLong())

            //Timer ejecucion de acciones en tiempo real
            val _loadActionTimer = Timer()
            _loadActionTimer.schedule(object : TimerTask() {
                override fun run() {
                    cargar_acciones()
                    agregar_a_los_logs("Las acciones en tiempo real se han realizado!")
                }
            }, MainServidor.ACTIVAR_ACCIONES_TIEMPO_REAL.toLong(), MainServidor.ACTIVAR_ACCIONES_TIEMPO_REAL.toLong())

            /*
			//Timer recargar moobs global
			Timer _reloadMobTimer = new Timer();
			_reloadMobTimer.schedule(new TimerTask() {
				public void run() {
					Mundo.RefreshAllMob();
					JuegoServidor.addToLog("La recharge des mobs est finie");
				}
			}, MainServidor.CONFIG_RELOAD_MOB_DELAY, MainServidor.CONFIG_RELOAD_MOB_DELAY);*/

            //Timer desconectar por AFK
            val _lastPacketTimer = Timer()
            _lastPacketTimer.schedule(object : TimerTask() {
                override fun run() {
                    for (perso in Mundo.getOnlinePersos()) {
                        if (perso.lastPacketTime + MainServidor.TIEMPO_DESCONECTAR_POR_AFK < System.currentTimeMillis()) {
                            if (perso.cuenta.juegoThread != null && perso.isConectado) {
                                agregar_a_los_logs("Se ha desconectado a " + perso.nombre + " por inactividad")
                                GestorSalida.REALM_SEND_MESSAGE(perso.cuenta.juegoThread._out, "01|")
                                perso.cuenta.juegoThread.closeSocket()
                            }
                        }
                    }
                }
            }, 60000, 60000)
            serversocket = ServerSocket(MainServidor.PUERTO_DE_JUEGO)
            if (MainServidor.USAR_IP) MainServidor.GAMESERVER_IP = GestorEncriptador.CryptIP(Ip) + GestorEncriptador.CryptPort(MainServidor.PUERTO_DE_JUEGO)
            startTime = System.currentTimeMillis()
            thread = Thread(this)
            thread!!.start()
        } catch (e: IOException) {
            agregar_a_los_logs("IOException: " + e.message)
            cerrarservidor()
        }
    }
}