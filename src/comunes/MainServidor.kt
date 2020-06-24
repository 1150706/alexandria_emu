package comunes

import juego.JuegoServidor
import juego.JuegoServidor.AllFightsTurns
import realm.RealmServer
import java.io.*
import java.net.InetAddress
import java.util.*
import kotlin.system.exitProcess

object MainServidor {
    private const val ARCHIVO_DE_CONFIGURACION = "config.txt"
    @JvmField
    var CONFIG_URLVOTE = ""
    @JvmField
    var IP: String? = "127.0.0.1"
    @JvmField
    var isInit = false
    @JvmField
    var DB_HOST: String? = null
    @JvmField
    var DB_USUARIO: String? = null
    @JvmField
    var DB_PASS: String? = null
    @JvmField
    var DB_ESTATICOS: String? = null
    @JvmField
    var DB_DINAMICOS: String? = null
    @JvmField
    var FLOOD_TIME: Long = 60000
    @JvmField
    var GAMESERVER_IP: String? = null
    @JvmField
    var NOMBRE_DEL_SERVIDOR = ""
    @JvmField
    var WEB_DEL_SERVIDOR = ""
    @JvmField
    var CONFIG_MOTD_COLOR: String? = ""
    @JvmField
    var CONFIG_PUB_COLOR: String? = ""
    @JvmField
    var VERSION_DEL_CLIENTE: String? = ""
    @JvmField
    var IGNORAR_VERSION_DEL_CLIENTE = false
    @JvmField
    var MOSTRAR_ENVIADOS = false
    @JvmField
    var MOSTRAR_RECIBIDOS = false
    private var PS: PrintStream? = null
    @JvmField
    var ENVIAR_POLITICA_DE_PRIVACIDAD = false
    @JvmField
    var PUERTO_DE_CONEXION = 443
    @JvmField
    var PUERTO_DE_JUEGO = 5555
    @JvmField
    var MAXIMO_PERSONAJES_POR_CUENTA = 5
    @JvmField
    var MAPA_INICIO_PERSONALIZADO: Short = 10298
    @JvmField
    var CONFIG_DD_LVL_DEPART: Short = 100
    @JvmField
    var CALDA_INICIO_PERSONALIZADA = 314
    @JvmField
    var CONFIG_MAP_SHOP: Short = 10114
    @JvmField
    var CONFIG_CELL_SHOP: Short = 360
    @JvmField
    var CONFIG_ENCLOS_MAP: Short = 7411
    @JvmField
    var CONFIG_CELL_ENCLOS: Short = 369
    @JvmField
    var CONFIG_MAP_PVP: Short = 7411
    @JvmField
    var CONFIG_CELL_PVP: Short = 369
    @JvmField
    var CONFIG_MAP_PVM: Short = 7411
    @JvmField
    var CONFIG_CELL_PVM: Short = 368
    @JvmField
    var HABILITAR_MULTI_CUENTA = false
    @JvmField
    var NIVEL_DE_INICIO = 1
    @JvmField
    var DAR_KAMAS_AL_INICIO = 0
    @JvmField
    var LIMITE_DE_MAPAS = 20000
    @JvmField
    var CONFIG_KAMASMIN = 101
    @JvmField
    var CONFIG_KAMASMAX = 10000
    @JvmField
    var TIEMPO_DE_GUARDADO = 10 * 60 * 10000
    @JvmField
    var TIEMPO_ENVIO_PUBLICIDAD_AUTOMATICA = 10000
    @JvmField
    var CONFIG_DROP = 1
    @JvmField
    var CONFIG_ZAAP = false
    @JvmField
    var ACTIVAR_ACCIONES_TIEMPO_REAL = 60000
    @JvmField
    var TIEMPO_MOVIMIENTO_MONSTRUOS = 30000
    @JvmField
    var CONFIG_RELOAD_MOB_DELAY = 360000
    @JvmField
    var MAXIMO_DE_CONECTADOS = 30
    @JvmField
    var CONFIG_IP_LOOPBACK = true
    @JvmField
    var XP_PVP = 10
    @JvmField
    var LVL_PVP = 15
    @JvmField
    var ALLOW_MULE_PVP = false
    @JvmField
    var XP_PVM = 1
    @JvmField
    var KAMAS = 1
    @JvmField
    var HONOR = 1
    @JvmField
    var XP_OFICIOS = 1
    @JvmField
    var CONFIG_CUSTOM_STARTMAP = false
    @JvmField
    var USAR_MOOBS = false
    @JvmField
    var CONFIG_XP_DEFI = true
    @JvmField
    var USAR_IP = false
    @JvmField
    var CONFIG_NOM_DD = ""
    @JvmField
    var CONFIG_HELP = ""
    @JvmField
    var PERMITIR_COMANDOS_JUGADORES = true
    @JvmField
    var gameServer: JuegoServidor? = null
    @JvmField
    var realmServer: RealmServer? = null
    @JvmField
    var isRunning = false
    @JvmField
    var Log_GameSock: BufferedWriter? = null
    @JvmField
    var Log_Game: BufferedWriter? = null
    @JvmField
    var Log_Realm: BufferedWriter? = null
    @JvmField
    var Log_MJ: BufferedWriter? = null
    @JvmField
    var Log_RealmSock: BufferedWriter? = null
    @JvmField
    var Log_Shop: BufferedWriter? = null
    @JvmField
    var canLog = false
    @JvmField
    var isSaving = false
    @JvmField
    var MOSTRAR_AURAS = false
    // TIC des fights
    @JvmField
    var _passerTours: Thread? = null
    //Arene
    @JvmField
    var arenaMap = ArrayList<Int>(8)
    @JvmField
    var CONFIG_ARENA_TIMER = 10 * 60 * 1000 // 10 minutes
    @JvmField
    var TIEMPO_DESCONECTAR_POR_AFK = 10 * 60 * 1000 // 10 minutes
    //HDV
    @JvmField
    var NOTINHDV = ArrayList<Int>()
    //UseCompactDATA
    @JvmField
    var CONFIG_SOCKET_USE_COMPACT_DATA = false
    @JvmField
    var CONFIG_SOCKET_TIME_COMPACT_DATA = 200
    //Guilde
    @JvmField
    var MEMBRE_MINI_GUILDE_VALIDE = 1
    //Challenges et Etoiles
    @JvmField
    var CONFIG_CHALLENGE_NUMBER = 1
    @JvmField
    var CONFIG_INDUNGEON_CHALLENGE = 2
    @JvmField
    var CONFIG_SECONDS_FOR_BONUS = 60
    @JvmField
    var CONFIG_BONUS_MAX = 400
    // Temps en combat
    @JvmField
    var CONFIG_MS_PER_TURN: Long = 30000
    @JvmField
    var CONFIG_MS_FOR_START_FIGHT: Long = 45000
    // Taille Percepteur
    @JvmField
    var CONFIG_TAILLE_VAR = true
    // Qu�tes
    @JvmField
    var ari = "7695;1;1500;1500|"

    // Montilier
    @JvmField
    var CONFIG_MONTILIER_ID = 30000

    @JvmStatic
    fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                cerrarservidor()
            }
        })
        println("==============================================================")
        println(cabecerapersonalizada())
        println("==============================================================\n")
        println("Cargando el archivo de configuracion:")
        CargarConfiguracion()
        isInit = true
        println("Cargado!")
        println("Conectando a la base de datos:")
        if (GestorSQL.iniciar_conexion()) println("Conectado!") else {
            println("Conexion invalida")
            cerrarservidor()
            exitProcess(0)
        }
        println("Creando el Mundo:")
        val startTime = System.currentTimeMillis()
        Mundo.crear_el_mundo()
        val endTime = System.currentTimeMillis()
        val differenceTime = (endTime - startTime) / 1000
        println("Creado en: $differenceTime segundos")
        isRunning = true
        print("Lanzando el Timer global: ")
        _passerTours = Thread(AllFightsTurns())
        _passerTours!!.start()
        println("Lanzado!")
        println("Lanzamiento del server de juego con el puerto: $PUERTO_DE_JUEGO")
        var ip: String? = ""
        try {
            ip = InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            println(e.message)
            try {
                Thread.sleep(10000)
            } catch (e1: InterruptedException) {
            }
            exitProcess(1)
        }
        ip = IP
        gameServer = JuegoServidor(ip)
        println("Lanzamiento del servidor de conexion por el puerto: $PUERTO_DE_CONEXION")
        realmServer = RealmServer()
        if (USAR_IP) println("IP del servidor $IP encriptada $GAMESERVER_IP")
        println("Atento a las conexiones de nuevos usuarios")
        if (CONFIG_SOCKET_USE_COMPACT_DATA) {
            println("Lanzando FlushTimer")
            GestorEnvio.flush_timer().start()
            println("Lanzado!")
        }
    }

    @JvmStatic
    fun CargarConfiguracion() {
        var log = false
        try {
            val config = BufferedReader(FileReader(ARCHIVO_DE_CONFIGURACION))
            var line = config.readLine()
            while (config.readLine().also { line = it } != null) {
                if (line.split("=".toRegex()).toTypedArray().size == 1) continue
                val param = line.split("=".toRegex()).toTypedArray()[0].trim { it <= ' ' }
                var value = line.split("=".toRegex()).toTypedArray()[1].trim { it <= ' ' }
                if (param.equals("MOSTRAR_ENVIADOS", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        MOSTRAR_ENVIADOS = true
                        println("Mode Debug: On")
                    }
                } else if (param.equals("ENVIAR_POLITICA_DE_PRIVACIDAD", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        ENVIAR_POLITICA_DE_PRIVACIDAD = true
                    }
                } else if (param.equals("CHALLENGE_NUMBER", ignoreCase = true)) {
                    CONFIG_CHALLENGE_NUMBER = value.toInt()
                    if (CONFIG_CHALLENGE_NUMBER < 0) CONFIG_CHALLENGE_NUMBER = 0
                    if (CONFIG_CHALLENGE_NUMBER > 4) CONFIG_CHALLENGE_NUMBER = 4
                } else if (param.equals("INDUNGEON_CHALLENGE", ignoreCase = true)) {
                    CONFIG_INDUNGEON_CHALLENGE = value.toInt()
                    if (CONFIG_INDUNGEON_CHALLENGE < 0) CONFIG_INDUNGEON_CHALLENGE = 0
                    if (CONFIG_INDUNGEON_CHALLENGE > 5) CONFIG_INDUNGEON_CHALLENGE = 5
                } else if (param.equals("BONUS_MAX", ignoreCase = true)) {
                    CONFIG_BONUS_MAX = value.toInt()
                    if (CONFIG_BONUS_MAX < 0) CONFIG_BONUS_MAX = 0
                    if (CONFIG_BONUS_MAX > 1000) CONFIG_BONUS_MAX = 1000
                } else if (param.equals("SECONDS_PER_TURN", ignoreCase = true)) {
                    CONFIG_MS_PER_TURN = value.toInt().toLong()
                    if (CONFIG_MS_PER_TURN < 1) CONFIG_MS_PER_TURN = 1
                    if (CONFIG_MS_PER_TURN > 300) CONFIG_MS_PER_TURN = 300
                    CONFIG_MS_PER_TURN *= 1000
                } else if (param.equals("INDUNGEON_CHALLENGE", ignoreCase = true)) {
                    CONFIG_MS_FOR_START_FIGHT = value.toInt().toLong()
                    if (CONFIG_MS_FOR_START_FIGHT < 1) CONFIG_MS_FOR_START_FIGHT = 1
                    if (CONFIG_MS_FOR_START_FIGHT > 300) CONFIG_MS_FOR_START_FIGHT = 300
                    CONFIG_MS_FOR_START_FIGHT *= 1000
                } else if (param.equals("SECONDS_FOR_BONUS", ignoreCase = true)) {
                    CONFIG_SECONDS_FOR_BONUS = value.toInt()
                    if (CONFIG_SECONDS_FOR_BONUS < 1) CONFIG_SECONDS_FOR_BONUS = 1
                    if (CONFIG_SECONDS_FOR_BONUS > 3600) CONFIG_SECONDS_FOR_BONUS = 3600
                } else if (param.equals("MONTILIER_ID", ignoreCase = true)) {
                    CONFIG_MONTILIER_ID = value.toInt()
                    if (CONFIG_MONTILIER_ID < 1) CONFIG_MONTILIER_ID = 1
                } else if (param.equals("HABILITAR_LOGS", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        log = true
                    }
                } else if (param.equals("PERCO_TAILLE_VAR", ignoreCase = true)) {
                    if (value.equals("false", ignoreCase = true)) {
                        CONFIG_TAILLE_VAR = false
                    }
                } else if (param.equals("USAR_MAPA_INICIO_PERSONALIZADO", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_CUSTOM_STARTMAP = true
                    }
                } else if (param.equals("IGNORAR_VERSION_DEL_CLIENTE", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        IGNORAR_VERSION_DEL_CLIENTE = true
                    }
                } else if (param.equals("DAR_KAMAS_AL_INICIO", ignoreCase = true)) {
                    DAR_KAMAS_AL_INICIO = value.toInt()
                    if (DAR_KAMAS_AL_INICIO < 0) DAR_KAMAS_AL_INICIO = 0
                    if (DAR_KAMAS_AL_INICIO > 1000000000) DAR_KAMAS_AL_INICIO = 1000000000
                } else if (param.equals("LIMITE_DE_MAPAS", ignoreCase = true)) {
                    LIMITE_DE_MAPAS = value.toInt()
                } else if (param.equals("KAMASMAX", ignoreCase = true)) {
                    CONFIG_KAMASMAX = value.toInt()
                    if (CONFIG_KAMASMAX < 0) CONFIG_KAMASMAX = 0
                    if (CONFIG_KAMASMAX > 1000000000) CONFIG_KAMASMAX = 1000000000
                } else if (param.equals("KAMASMIN", ignoreCase = true)) {
                    CONFIG_KAMASMIN = value.toInt()
                    if (CONFIG_KAMASMIN < 0) CONFIG_KAMASMIN = 0
                    if (CONFIG_KAMASMIN > 1000000000) CONFIG_KAMASMIN = 1000000000
                } else if (param.equals("NVEL_DE_INICIO", ignoreCase = true)) {
                    NIVEL_DE_INICIO = value.toInt()
                    if (NIVEL_DE_INICIO < 1) NIVEL_DE_INICIO = 1
                    if (NIVEL_DE_INICIO > 200) NIVEL_DE_INICIO = 200
                } else if (param.equals("MAPA_INICIO_PERSONALIZADO", ignoreCase = true)) {
                    MAPA_INICIO_PERSONALIZADO = value.toShort()
                } else if (param.equals("DD_LVL_DEPART", ignoreCase = true)) {
                    CONFIG_DD_LVL_DEPART = value.toShort()
                } else if (param.equals("MAP_PVP", ignoreCase = true)) {
                    CONFIG_MAP_PVP = value.toShort()
                } else if (param.equals("MAP_PVM", ignoreCase = true)) {
                    CONFIG_MAP_PVM = value.toShort()
                } else if (param.equals("CELL_PVM", ignoreCase = true)) {
                    CONFIG_CELL_PVM = value.toShort()
                } else if (param.equals("CELL_PVP", ignoreCase = true)) {
                    CONFIG_CELL_PVP = value.toShort()
                } else if (param.equals("ENCLOS_MAP", ignoreCase = true)) {
                    CONFIG_ENCLOS_MAP = value.toShort()
                } else if (param.equals("CELL_ENCLOS", ignoreCase = true)) {
                    CONFIG_CELL_ENCLOS = value.toShort()
                } else if (param.equals("MAP_SHOP", ignoreCase = true)) {
                    CONFIG_MAP_SHOP = value.toShort()
                } else if (param.equals("CELL_SHOP", ignoreCase = true)) {
                    CONFIG_CELL_SHOP = value.toShort()
                } else if (param.equals("CELDA_INICIO_PERSONALIZADA", ignoreCase = true)) {
                    CALDA_INICIO_PERSONALIZADA = value.toInt()
                } else if (param.equals("KAMAS", ignoreCase = true)) {
                    KAMAS = value.toInt()
                } else if (param.equals("HONOR", ignoreCase = true)) {
                    HONOR = value.toInt()
                } else if (param.equals("TIEMPO_DE_GUARDADO", ignoreCase = true)) {
                    TIEMPO_ENVIO_PUBLICIDAD_AUTOMATICA = value.toInt() * 60 * 1000000000
                } else if (param.equals("XP_PVM", ignoreCase = true)) {
                    XP_PVM = value.toInt()
                } else if (param.equals("XP_PVP", ignoreCase = true)) {
                    XP_PVP = value.toInt()
                } else if (param.equals("LVL_PVP", ignoreCase = true)) {
                    LVL_PVP = value.toInt()
                } else if (param.equals("DROP", ignoreCase = true)) {
                    CONFIG_DROP = value.toInt()
                } else if (param.equals("MOSTRAR_IP_LOCAL", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_IP_LOOPBACK = true
                    }
                } else if (param.equals("ZAAP", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_ZAAP = true
                    }
                } else if (param.equals("USAR_IP", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        USAR_IP = true
                    }
                } else if (param.equals("NOMBRE_DEL_SERVIDOR", ignoreCase = true)) {
                    NOMBRE_DEL_SERVIDOR = line.split("=".toRegex(), 2).toTypedArray()[1]
                } else if (param.equals("WEB_DEL_SERVIDOR", ignoreCase = true)) {
                    WEB_DEL_SERVIDOR = line.split("=".toRegex(), 2).toTypedArray()[1]
                } else if (param.equals("URLVOTE", ignoreCase = true)) {
                    CONFIG_URLVOTE = line.split("=".toRegex(), 2).toTypedArray()[1]
                } else if (param.equals("MOTD_COLOR", ignoreCase = true)) {
                    CONFIG_MOTD_COLOR = value
                } else if (param.equals("VERSION_DEL_CLIENTE", ignoreCase = true)) {
                    VERSION_DEL_CLIENTE = value
                } else if (param.equals("PUB_COLOR", ignoreCase = true)) {
                    CONFIG_PUB_COLOR = value
                } else if (param.equals("XP_OFICIOS", ignoreCase = true)) {
                    XP_OFICIOS = value.toInt()
                } else if (param.equals("PUERTO_DE_JUEGO", ignoreCase = true)) {
                    PUERTO_DE_JUEGO = value.toInt()
                } else if (param.equals("HELP", ignoreCase = true)) {
                    CONFIG_HELP = line.split("=".toRegex(), 2).toTypedArray()[1]
                } else if (param.equals("NOM_DD", ignoreCase = true)) {
                    CONFIG_NOM_DD = line.split("=".toRegex(), 2).toTypedArray()[1]
                } else if (param.equals("PUERTO_DE_CONEXION", ignoreCase = true)) {
                    PUERTO_DE_CONEXION = value.toInt()
                } else if (param.equals("FLOODER_TIME", ignoreCase = true)) {
                    FLOOD_TIME = value.toInt().toLong()
                } else if (param.equals("HOST_IP", ignoreCase = true)) {
                    IP = value
                } else if (param.equals("DB_HOST", ignoreCase = true)) {
                    DB_HOST = value
                } else if (param.equals("DB_USUARIO", ignoreCase = true)) {
                    DB_USUARIO = value
                } else if (param.equals("DB_PASS", ignoreCase = true)) {
                    if (value == null) value = ""
                    DB_PASS = value
                } else if (param.equals("DB_ESTATICOS", ignoreCase = true)) {
                    DB_ESTATICOS = value
                } else if (param.equals("DB_DINAMICOS", ignoreCase = true)) {
                    DB_DINAMICOS = value
                } else if (param.equals("MAXIMO_PERSONAJES_POR_CUENTA", ignoreCase = true)) {
                    MAXIMO_PERSONAJES_POR_CUENTA = value.toInt()
                } else if (param.equals("USAR_MOOBS", ignoreCase = true)) {
                    USAR_MOOBS = value.equals("true", ignoreCase = true)
                } else if (param.equals("HABILITAR_MULTI_CUENTA", ignoreCase = true)) {
                    HABILITAR_MULTI_CUENTA = value.equals("true", ignoreCase = true)
                } else if (param.equals("ACTIVAR_ACCIONES_TIEMPO_REAL", ignoreCase = true)) {
                    ACTIVAR_ACCIONES_TIEMPO_REAL = value.toInt() * 1000
                } else if (param.equals("TIEMPO_ENVIO_PUBLICIDAD_AUTOMATICA", ignoreCase = true)) {
                    TIEMPO_ENVIO_PUBLICIDAD_AUTOMATICA = value.toInt() * 1000
                } else if (param.equals("MAXIMO_DE_CONECTADOS", ignoreCase = true)) {
                    MAXIMO_DE_CONECTADOS = value.toInt()
                } else if (param.equals("ARENA_MAP", ignoreCase = true)) {
                    for (curID in value.split(",".toRegex()).toTypedArray()) {
                        arenaMap.add(curID.toInt())
                    }
                } else if (param.equals("ARENA_TIMER", ignoreCase = true)) {
                    CONFIG_ARENA_TIMER = value.toInt()
                } else if (param.equals("MOSTRAR_AURAS", ignoreCase = true)) {
                    MOSTRAR_AURAS = value.equals("true", ignoreCase = true)
                } else if (param.equals("ALLOW_MULE_PVP", ignoreCase = true)) {
                    ALLOW_MULE_PVP = value.equals("true", ignoreCase = true)
                } else if (param.equals("TIEMPO_DESCONECTAR_POR_AFK", ignoreCase = true)) {
                    TIEMPO_DESCONECTAR_POR_AFK = value.toInt()
                } else if (param.equals("NOT_IN_HDV", ignoreCase = true)) {
                    for (curID in value.split(",".toRegex()).toTypedArray()) {
                        NOTINHDV.add(curID.toInt())
                    }
                } else if (param.equals("USE_COMPACT_DATA", ignoreCase = true)) {
                    CONFIG_SOCKET_USE_COMPACT_DATA = value.equals("true", ignoreCase = true)
                } else if (param.equals("TIME_COMPACT_DATA", ignoreCase = true)) {
                    CONFIG_SOCKET_TIME_COMPACT_DATA = value.toInt()
                } else if (param.equals("RELOAD_MOB_DELAY", ignoreCase = true)) {
                    CONFIG_RELOAD_MOB_DELAY = value.toInt()
                } else if (param.equals("PERMITIR_COMANDOS_JUGADORES", ignoreCase = true)) {
                    PERMITIR_COMANDOS_JUGADORES = value.equals("true", ignoreCase = true)
                }
            }
            if (DB_ESTATICOS == null || DB_DINAMICOS == null || DB_HOST == null || DB_PASS == null || DB_USUARIO == null) {
                throw Exception()
            }
        } catch (e: Exception) {
            println(e.message)
            println("Fichero de configuracion config.txt inexistente o no puede leerse")
            println("Cerrando el servidor")
            exitProcess(1)
        }
        if (MOSTRAR_ENVIADOS)
        try {
            val date =
                Calendar.getInstance()[Calendar.DAY_OF_MONTH].toString() + "-" + (Calendar.getInstance()[Calendar.MONTH] + 1) + "-" + Calendar.getInstance()[Calendar.YEAR]
            if (log) {
                Log_GameSock = BufferedWriter(FileWriter("Logs/Juego_logs/" + date + "_packets.txt", true))
                Log_Game = BufferedWriter(FileWriter("Logs/Juego_logs/$date.txt", true))
                Log_Realm = BufferedWriter(FileWriter("Logs/Realm_logs/$date.txt", true))
                Log_RealmSock = BufferedWriter(FileWriter("Logs/Realm_logs/" + date + "_packets.txt", true))
                Log_Shop = BufferedWriter(FileWriter("Logs/Ventas_logs/$date.txt", true))
                PS = PrintStream(File("Logs/Error_logs/" + date + "_error.txt"))
                PS!!.append("Lanzando el servidor\n")
                PS!!.flush()
                System.setErr(PS)
                Log_MJ = BufferedWriter(FileWriter("Logs/Mod_logs/" + date + "_GM.txt", true))
                canLog = true
                val str = "Lanzando el servidor\n"
                Log_GameSock!!.write(str)
                Log_Game!!.write(str)
                Log_MJ!!.write(str)
                Log_Realm!!.write(str)
                Log_RealmSock!!.write(str)
                Log_Shop!!.write(str)
                Log_GameSock!!.flush()
                Log_Game!!.flush()
                Log_MJ!!.flush()
                Log_Realm!!.flush()
                Log_RealmSock!!.flush()
                Log_Shop!!.flush()
            }
        } catch (e: IOException) {
            //Si los ficheros no existen se crean en Logs/carpetas
            println("Los ficheros de logs no pueden ser creados")
            println("Creacion de las carpetas y ficheros")
            File("Logs").mkdir()
            File("Logs/Ventas_logs").mkdir()
            File("Logs/Juego_logs").mkdir()
            File("Logs/Realm_logs").mkdir()
            File("Logs/Mod_logs").mkdir()
            File("Logs/Error_logs").mkdir()
            println(e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
    fun cerrarservidor() {
        println("Cerrando el servidor")
        if (isRunning) {
            isRunning = false
            gameServer!!.expulsar_a_todos()
            Mundo.saveAll(null)
            GestorSQL.cerrar_consulta()
        }
        println("Servidor cerrado")
        isRunning = false
    }

    @JvmStatic
    fun addToMjLog(str: String) {
        if (!canLog) return
        val date =
            Calendar.getInstance()[Calendar.HOUR_OF_DAY].toString() + ":" + Calendar.getInstance()[+Calendar.MINUTE] + ":" + Calendar.getInstance()[Calendar.SECOND]
        try {
            Log_MJ!!.write("$str  [$date]")
            Log_MJ!!.newLine()
            Log_MJ!!.flush()
        } catch (e: IOException) {
        }
    }

    @JvmStatic
    fun addToShopLog(str: String) {
        if (!canLog) return
        val date =
            Calendar.getInstance()[Calendar.HOUR_OF_DAY].toString() + ":" + Calendar.getInstance()[+Calendar.MINUTE] + ":" + Calendar.getInstance()[Calendar.SECOND]
        try {
            Log_Shop!!.write("[$date]$str")
            Log_Shop!!.newLine()
            Log_Shop!!.flush()
        } catch (e: IOException) {
        }
    }

    @JvmStatic
    fun cabecerapersonalizada(): String {
        return "Alexandria EMU - BY Player-xD - http://privatedofus.net\n" +
                "Basado en Ancestra Remake rev47"
    }
}