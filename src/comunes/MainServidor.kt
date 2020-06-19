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
    var DB_USER: String? = null
    @JvmField
    var DB_PASS: String? = null
    @JvmField
    var STATIC_DB_NAME: String? = null
    @JvmField
    var OTHER_DB_NAME: String? = null
    @JvmField
    var FLOOD_TIME: Long = 60000
    @JvmField
    var GAMESERVER_IP: String? = null
    @JvmField
    var CONFIG_MOTD = ""
    @JvmField
    var CONFIG_MOTD_COLOR: String? = ""
    @JvmField
    var CONFIG_PUB_COLOR: String? = ""
    @JvmField
    var CONFIG_DEBUG = false
    private var PS: PrintStream? = null
    @JvmField
    var CONFIG_POLICY = false
    @JvmField
    var PUERTO_DE_CONEXION = 443
    @JvmField
    var PUERTO_DE_JUEGO = 5555
    @JvmField
    var CONFIG_MAX_PERSOS = 5
    @JvmField
    var CONFIG_START_MAP: Short = 10298
    @JvmField
    var CONFIG_DD_LVL_DEPART: Short = 100
    @JvmField
    var CONFIG_START_CELL = 314
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
    var CONFIG_ALLOW_MULTI = false
    @JvmField
    var CONFIG_START_LEVEL = 1
    @JvmField
    var CONFIG_START_KAMAS = 0
    @JvmField
    var CONFIG_KAMASMIN = 101
    @JvmField
    var CONFIG_KAMASMAX = 10000
    @JvmField
    var CONFIG_SAVE_TIME = 10 * 60 * 10000
    @JvmField
    var CONFIG_DROP = 1
    @JvmField
    var CONFIG_ZAAP = false
    @JvmField
    var CONFIG_LOAD_DELAY = 60000
    @JvmField
    var TIEMPO_MOVIMIENTO_MONSTRUOS = 30000
    @JvmField
    var CONFIG_RELOAD_MOB_DELAY = 360000
    @JvmField
    var CONFIG_PUB_DELAY = 50000
    @JvmField
    var CONFIG_PLAYER_LIMIT = 30
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
    var XP_METIER = 1
    @JvmField
    var CONFIG_CUSTOM_STARTMAP = false
    @JvmField
    var CONFIG_USE_MOBS = false
    @JvmField
    var CONFIG_XP_DEFI = true
    @JvmField
    var USAR_IP = false
    @JvmField
    var CONFIG_NOM_DD = ""
    @JvmField
    var CONFIG_HELP = ""
    @JvmField
    var CONFIG_ALLOW_PLAYER_COMMANDS = true
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
    var AURA_SYSTEM = false

    // TIC des fights
    @JvmField
    var _passerTours: Thread? = null

    //Arene
    @JvmField
    var arenaMap = ArrayList<Int>(8)
    @JvmField
    var CONFIG_ARENA_TIMER = 10 * 60 * 1000 // 10 minutes

    //BDD
    @JvmField
    var CONFIG_DB_COMMIT = 30 * 1000

    //Inactivit�
    @JvmField
    var CONFIG_MAX_IDLE_TIME = 1800000 //En millisecondes

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
                closeServers()
            }
        }
        )
        println("==============================================================")
        println(makeHeader())
        println("==============================================================\n")
        println("Chargement de la configuration :")
        loadConfiguration()
        isInit = true
        println("Configuration Ok !")
        println("Connexion a la base de donnee :")
        if (GestorSQL.InicarConexion()) println("Connexion Ok !") else {
            println("Connexion invalide")
            closeServers()
            exitProcess(0)
        }
        println("Creation du Monde :")
        val startTime = System.currentTimeMillis()
        Mundo.createWorld()
        val endTime = System.currentTimeMillis()
        val differenceTime = (endTime - startTime) / 1000
        println("Monde Ok ! en : $differenceTime s")
        isRunning = true
        print("Lancement du Timer global : ")
        _passerTours = Thread(AllFightsTurns())
        _passerTours!!.start()
        println(" Reussi !")
        println("Lancement du serveur de Jeu sur le port $PUERTO_DE_JUEGO")
        var Ip: String? = ""
        try {
            Ip = InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            println(e.message)
            try {
                Thread.sleep(10000)
            } catch (e1: InterruptedException) {
            }
            exitProcess(1)
        }
        Ip = IP
        gameServer = JuegoServidor(Ip)
        println("Lancement du serveur de Connexion sur le port : $PUERTO_DE_CONEXION")
        realmServer = RealmServer()
        if (USAR_IP) println("Ip du serveur $IP crypt $GAMESERVER_IP")
        println("Nerf'Emu est en marche.\nEn attente de connexions")
        if (CONFIG_SOCKET_USE_COMPACT_DATA) {
            println("Lancement du FlushTimer")
            GestorEnvio.FlushTimer().start()
            println("FlushTimer : Ok !")
        }
    }

    @JvmStatic
    fun loadConfiguration() {
        var log = false
        try {
            val config = BufferedReader(FileReader(ARCHIVO_DE_CONFIGURACION))
            var line = config.readLine()
            while (config.readLine().also { line = it } != null) {
                if (line.split("=".toRegex()).toTypedArray().size == 1) continue
                val param = line.split("=".toRegex()).toTypedArray()[0].trim { it <= ' ' }
                var value = line.split("=".toRegex()).toTypedArray()[1].trim { it <= ' ' }
                if (param.equals("DEBUG", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_DEBUG = true
                        println("Mode Debug: On")
                    }
                } else if (param.equals("SEND_POLICY", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_POLICY = true
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
                } else if (param.equals("LOG", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        log = true
                    }
                } else if (param.equals("PERCO_TAILLE_VAR", ignoreCase = true)) {
                    if (value.equals("false", ignoreCase = true)) {
                        CONFIG_TAILLE_VAR = false
                    }
                } else if (param.equals("USE_CUSTOM_START", ignoreCase = true)) {
                    if (value.equals("true", ignoreCase = true)) {
                        CONFIG_CUSTOM_STARTMAP = true
                    }
                } else if (param.equals("START_KAMAS", ignoreCase = true)) {
                    CONFIG_START_KAMAS = value.toInt()
                    if (CONFIG_START_KAMAS < 0) CONFIG_START_KAMAS = 0
                    if (CONFIG_START_KAMAS > 1000000000) CONFIG_START_KAMAS = 1000000000
                } else if (param.equals("KAMASMAX", ignoreCase = true)) {
                    CONFIG_KAMASMAX = value.toInt()
                    if (CONFIG_KAMASMAX < 0) CONFIG_KAMASMAX = 0
                    if (CONFIG_KAMASMAX > 1000000000) CONFIG_KAMASMAX = 1000000000
                } else if (param.equals("KAMASMIN", ignoreCase = true)) {
                    CONFIG_KAMASMIN = value.toInt()
                    if (CONFIG_KAMASMIN < 0) CONFIG_KAMASMIN = 0
                    if (CONFIG_KAMASMIN > 1000000000) CONFIG_KAMASMIN = 1000000000
                } else if (param.equals("START_LEVEL", ignoreCase = true)) {
                    CONFIG_START_LEVEL = value.toInt()
                    if (CONFIG_START_LEVEL < 1) CONFIG_START_LEVEL = 1
                    if (CONFIG_START_LEVEL > 200) CONFIG_START_LEVEL = 200
                } else if (param.equals("START_MAP", ignoreCase = true)) {
                    CONFIG_START_MAP = value.toShort()
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
                } else if (param.equals("START_CELL", ignoreCase = true)) {
                    CONFIG_START_CELL = value.toInt()
                } else if (param.equals("KAMAS", ignoreCase = true)) {
                    KAMAS = value.toInt()
                } else if (param.equals("HONOR", ignoreCase = true)) {
                    HONOR = value.toInt()
                } else if (param.equals("SAVE_TIME", ignoreCase = true)) {
                    CONFIG_SAVE_TIME = value.toInt() * 60 * 1000000000
                } else if (param.equals("XP_PVM", ignoreCase = true)) {
                    XP_PVM = value.toInt()
                } else if (param.equals("XP_PVP", ignoreCase = true)) {
                    XP_PVP = value.toInt()
                } else if (param.equals("LVL_PVP", ignoreCase = true)) {
                    LVL_PVP = value.toInt()
                } else if (param.equals("DROP", ignoreCase = true)) {
                    CONFIG_DROP = value.toInt()
                } else if (param.equals("LOCALIP_LOOPBACK", ignoreCase = true)) {
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
                } else if (param.equals("MOTD", ignoreCase = true)) {
                    CONFIG_MOTD = line.split("=".toRegex(), 2).toTypedArray()[1]
                } else if (param.equals("URLVOTE", ignoreCase = true)) {
                    CONFIG_URLVOTE = line.split("=".toRegex(), 2).toTypedArray()[1]
                } else if (param.equals("MOTD_COLOR", ignoreCase = true)) {
                    CONFIG_MOTD_COLOR = value
                } else if (param.equals("PUB_COLOR", ignoreCase = true)) {
                    CONFIG_PUB_COLOR = value
                } else if (param.equals("XP_METIER", ignoreCase = true)) {
                    XP_METIER = value.toInt()
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
                } else if (param.equals("CONFIG_PUB_DELAY", ignoreCase = true)) {
                    CONFIG_PUB_DELAY = value.toInt()
                } else if (param.equals("HOST_IP", ignoreCase = true)) {
                    IP = value
                } else if (param.equals("DB_HOST", ignoreCase = true)) {
                    DB_HOST = value
                } else if (param.equals("DB_USER", ignoreCase = true)) {
                    DB_USER = value
                } else if (param.equals("DB_PASS", ignoreCase = true)) {
                    if (value == null) value = ""
                    DB_PASS = value
                } else if (param.equals("STATIC_DB_NAME", ignoreCase = true)) {
                    STATIC_DB_NAME = value
                } else if (param.equals("OTHER_DB_NAME", ignoreCase = true)) {
                    OTHER_DB_NAME = value
                } else if (param.equals("MAX_PERSO_PAR_COMPTE", ignoreCase = true)) {
                    CONFIG_MAX_PERSOS = value.toInt()
                } else if (param.equals("USE_MOBS", ignoreCase = true)) {
                    CONFIG_USE_MOBS = value.equals("true", ignoreCase = true)
                } else if (param.equals("ALLOW_MULTI_ACCOUNT", ignoreCase = true)) {
                    CONFIG_ALLOW_MULTI = value.equals("true", ignoreCase = true)
                } else if (param.equals("LOAD_ACTION_DELAY", ignoreCase = true)) {
                    CONFIG_LOAD_DELAY = value.toInt() * 1000
                } else if (param.equals("PLAYER_LIMIT", ignoreCase = true)) {
                    CONFIG_PLAYER_LIMIT = value.toInt()
                } else if (param.equals("ARENA_MAP", ignoreCase = true)) {
                    for (curID in value.split(",".toRegex()).toTypedArray()) {
                        arenaMap.add(curID.toInt())
                    }
                } else if (param.equals("ARENA_TIMER", ignoreCase = true)) {
                    CONFIG_ARENA_TIMER = value.toInt()
                } else if (param.equals("AURA_SYSTEM", ignoreCase = true)) {
                    AURA_SYSTEM = value.equals("true", ignoreCase = true)
                } else if (param.equals("ALLOW_MULE_PVP", ignoreCase = true)) {
                    ALLOW_MULE_PVP = value.equals("true", ignoreCase = true)
                } else if (param.equals("MAX_IDLE_TIME", ignoreCase = true)) {
                    CONFIG_MAX_IDLE_TIME = value.toInt() * 60000
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
                } else if (param.equals("ALLOW_PLAYER_COMMANDS", ignoreCase = true)) {
                    CONFIG_ALLOW_PLAYER_COMMANDS = value.equals("true", ignoreCase = true)
                }
            }
            if (STATIC_DB_NAME == null || OTHER_DB_NAME == null || DB_HOST == null || DB_PASS == null || DB_USER == null) {
                throw Exception()
            }
        } catch (e: Exception) {
            println(e.message)
            println("Fichier de configuration non existant ou illisible")
            println("Fermeture du serveur")
            exitProcess(1)
        }
        if (CONFIG_DEBUG)
        try {
            val date =
                Calendar.getInstance()[Calendar.DAY_OF_MONTH].toString() + "-" + (Calendar.getInstance()[Calendar.MONTH] + 1) + "-" + Calendar.getInstance()[Calendar.YEAR]
            if (log) {
                Log_GameSock = BufferedWriter(FileWriter("Game_logs/" + date + "_packets.txt", true))
                Log_Game = BufferedWriter(FileWriter("Game_logs/$date.txt", true))
                Log_Realm = BufferedWriter(FileWriter("Realm_logs/$date.txt", true))
                Log_RealmSock = BufferedWriter(FileWriter("Realm_logs/" + date + "_packets.txt", true))
                Log_Shop = BufferedWriter(FileWriter("Shop_logs/$date.txt", true))
                PS = PrintStream(File("Error_logs/" + date + "_error.txt"))
                PS!!.append("Lancement du serveur..\n")
                PS!!.flush()
                System.setErr(PS)
                Log_MJ = BufferedWriter(FileWriter("Gms_logs/" + date + "_GM.txt", true))
                canLog = true
                val str = "Lancement du serveur...\n"
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
            /*On cr�er les dossiers*/
            println("Les fichiers de logs n'ont pas pu etre creer")
            println("Creation des dossiers")
            File("Shop_logs").mkdir()
            File("Game_logs").mkdir()
            File("Realm_logs").mkdir()
            File("Gms_logs").mkdir()
            File("Error_logs").mkdir()
            println(e.message)
            exitProcess(1)
        }
    }

    @JvmStatic
    fun closeServers() {
        println("Arret du serveur demande ...")
        if (isRunning) {
            isRunning = false
            gameServer!!.kickAll()
            Mundo.saveAll(null)
            GestorSQL.closeCons()
        }
        println("Arret du serveur: OK")
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
    fun makeHeader(): String {
        return "Alexandria EMU - BY Player-xD - http://privatedofus.net\n" +
                "Basado en Ancestra Remake rev47"
    }
}