package comunes

import comunes.GestorSQL.actualizar_datos_cuenta
import comunes.GestorSQL.actualizar_pregunta_npc
import comunes.GestorSQL.actualizar_respuesta_de_npc
import comunes.GestorSQL.agregar_fin_pelea_accion
import comunes.GestorSQL.agregar_ip_baneada
import comunes.GestorSQL.agregar_npc_en_mapa
import comunes.GestorSQL.agregar_publicidad
import comunes.GestorSQL.agregar_respuesta_npc
import comunes.GestorSQL.cargar_cuenta_por_id
import comunes.GestorSQL.cargar_maximo_de_objetos
import comunes.GestorSQL.cargar_npc_modelo
import comunes.GestorSQL.cargar_preguntas_npc
import comunes.GestorSQL.cargar_publicidades_automaticas
import comunes.GestorSQL.cargar_respuestas_npc
import comunes.GestorSQL.eliminar_celdas
import comunes.GestorSQL.eliminar_npc_en_mapa
import comunes.GestorSQL.guardar_celdas
import comunes.GestorSQL.guardar_cercados
import comunes.GestorSQL.guardar_mapa
import comunes.GestorSQL.guardar_nuevo_grupo_monstruos
import comunes.GestorSQL.guardar_personaje
import comunes.GestorSalida.ENVIAR_AGREGAR_NPC_EN_MAPA
import comunes.GestorSalida.ENVIAR_AGREGAR_PERSONAJE_EN_MAPA
import comunes.GestorSalida.ENVIAR_MENSAJE_A_TODOS
import comunes.GestorSalida.ENVIAR_MENSAJE_DESDE_LANG
import comunes.GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS
import comunes.GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS
import comunes.GestorSalida.ENVIAR_TEXTO_EN_CONSOLA
import comunes.GestorSalida.GAME_SEND_ALTER_GM_PACKET
import comunes.GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP
import comunes.GestorSalida.GAME_SEND_NEW_LVL_PACKET
import comunes.GestorSalida.GAME_SEND_Ow_PACKET
import comunes.GestorSalida.GAME_SEND_SPELL_LIST
import comunes.GestorSalida.GAME_SEND_gn_PACKET
import comunes.MainServidor.CargarConfiguracion
import comunes.MainServidor.addToMjLog
import comunes.MainServidor.cabecerapersonalizada
import juego.JuegoServidor.SaveThread
import juego.JuegoServidor.todoslosturnospelea
import objetos.Accion
import objetos.Cuenta
import objetos.Mapa.MountPark
import objetos.Mercadillo.HdvEntry
import objetos.Objeto
import objetos.Personaje
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.PrintWriter
import javax.swing.Timer
import kotlin.math.abs
import kotlin.math.pow
import kotlin.system.exitProcess

class Comandos(personaje: Personaje) {
    lateinit var _cuenta: Cuenta
    lateinit var _personaje: Personaje
    lateinit var _imprimir: PrintWriter

    //Guardado
    private var _TimerStart = false
    var _timer: Timer? = null
    private fun createTimer(time: Int): Timer {
        val action: ActionListener = object : ActionListener {
            var Time = time
            override fun actionPerformed(event: ActionEvent) {
                Time -= 1
                if (Time == 1) {
                    ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;$Time minuto")
                } else {
                    ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;$Time minutos")
                }
                if (Time <= 0) {
                    for (perso in Mundo.getOnlinePersos()) {
                        perso.cuenta.juegoThread.kick()
                    }
                    exitProcess(0)
                }
            }
        }
        // Génération du repeat toutes les minutes.
        return Timer(60000, action) //60000
    }

    fun consoleCommand(packet: String) {
        if (_cuenta.gmlvl < 1) {
            _cuenta.juegoThread.closeSocket()
            return
        }
        val mensaje = packet.substring(2)
        val infos = mensaje.split(" ".toRegex()).toTypedArray()
        if (infos.isEmpty()) return
        val comando = infos[0]
        if (MainServidor.canLog) {
            addToMjLog(mensaje + " <=" + _cuenta.actualIP + " : " + _cuenta.nombre + " / " + _personaje.nombre)
        }
        when {
            _cuenta.gmlvl == 2 -> {
                ComandosGmNivelUno(comando, infos, mensaje)
            }
            _cuenta.gmlvl == 3 -> {
                ComandosGmNivelDos(comando, infos, mensaje)
            }
            _cuenta.gmlvl == 4 -> {
                ComandosGmNivelTres(comando, infos, mensaje)
            }
            _cuenta.gmlvl >= 5 -> {
                ComandosGmNivelCuatro(comando, infos, mensaje)
            }
        }
    }

    fun ComandosGmNivelUno(comando: String, infos: Array<String>, mensaje: String) {
        var infos = infos
        if (_cuenta.gmlvl < 1) {
            _cuenta.juegoThread.closeSocket()
            return
        }
        if (comando.equals("INFORMACION", ignoreCase = true)) {
            var tiempo = System.currentTimeMillis() - MainServidor.gameServer!!.startTime
            val dias = (tiempo / (1000 * 3600 * 24)).toInt()
            tiempo %= (1000 * 3600 * 24).toLong()
            val horas = (tiempo / (1000 * 3600)).toInt()
            tiempo %= (1000 * 3600).toLong()
            val minutos = (tiempo / (1000 * 60)).toInt()
            tiempo %= (1000 * 60).toLong()
            val segundos = (tiempo / 1000).toInt()
            val mess = """
                ===========
                ${cabecerapersonalizada()}
                Tiempo online: ${dias}D ${horas}H ${minutos}M ${segundos}s
                Jugadores online: ${MainServidor.gameServer!!.playerNumber}
                Maximos conectados: ${MainServidor.gameServer!!.maxPlayer}
                ===========
                """.trimIndent()
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            return
        } else if (comando.equals("REFRESCAR_MONSTRUOS", ignoreCase = true)) {
            _personaje.actualMapa.refreshSpawns()
            val mess = "Monstruos del mapa refrescados con exito."
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            return
        } else if (comando.equals("ACTUALIZAR_SERVIDOR", ignoreCase = true)) {
            try {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Iniciando actualizacion de datos:")
                CargarConfiguracion()
                cargar_maximo_de_objetos()
                cargar_npc_modelo()
                cargar_preguntas_npc()
                cargar_respuestas_npc()
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Actualizacion terminada.")
            } catch (ignored: Exception) {
            }
            return
        } else if (comando.equals("RECARGAR_CONFIGURACION", ignoreCase = true)) {
            try {
                CargarConfiguracion()
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Configuracion recargada con exito.")
            } catch (ignored: Exception) {
            }
            return
        } else if (comando.equals("INFORMACION_DEL_MAPA", ignoreCase = true)) {
            var mess = """
                =========================================================
                Lista de NPC en el mapa:
                """.trimIndent()
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            val map = _personaje.actualMapa
            for ((key, value) in map.npcs) {
                mess = "ID Eliminar: " + key + "| ID: " + value.modelo.id + "| Celda: " + value.celdaID + "| Pregunta inicial: " + value.modelo.preguntaInicial
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            }
            mess = "Lista de los monstruos en el mapa:"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            for ((key, value) in map.mobGroups) {
                mess = "ID Eliminar: " + key + "| Celda: " + value.celdaID + "| Alineacion: " + value.alineacion + "| Tamaño: " + value.tamaño
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            }
            mess = "========================================================="
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            return
        } else if (comando.equals("QUIEN_ONLINE", ignoreCase = true)) {
            var mess = """
                =========================================================
                Lista de jugadores online:
                """.trimIndent()
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            val diff = MainServidor.gameServer!!.clientes.size - 30
            for (b in 0..29) {
                if (b == MainServidor.gameServer!!.clientes.size) break
                val GT = MainServidor.gameServer!!.clientes[b]
                val P = GT.perso ?: continue
                mess = P.nombre + "(" + P.id + ") "
                mess += when (P.clase) {
                    Constantes.CLASS_FECA -> "Fec"
                    Constantes.CLASS_OSAMODAS -> "Osa"
                    Constantes.CLASS_ENUTROF -> "Enu"
                    Constantes.CLASS_SRAM -> "Sra"
                    Constantes.CLASS_XELOR -> "Xel"
                    Constantes.CLASS_ECAFLIP -> "Eca"
                    Constantes.CLASS_ENIRIPSA -> "Eni"
                    Constantes.CLASS_IOP -> "Iop"
                    Constantes.CLASS_CRA -> "Cra"
                    Constantes.CLASS_SADIDA -> "Sad"
                    Constantes.CLASS_SACRIEUR -> "Sac"
                    Constantes.CLASS_PANDAWA -> "Pan"
                    else -> "Unk"
                }
                mess += " "
                mess += (if (P.sexo == 0) "M" else "F") + " "
                mess += P._lvl.toString() + " "
                mess += P.actualMapa.id.toString() + "(" + P.actualMapa.x + "/" + P.actualMapa.y + ") "
                mess += if (P.pelea == null) "" else "Combate "
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            }
            if (diff > 0) {
                mess = "Y $diff otros personajes..."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            }
            mess = "=========================================================\n"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            return
        } else if (comando.equals("VER_CELDAS_PELEA", ignoreCase = true)) {
            var mess = StringBuilder("Lista de las celdas de pelea [ID del team][ID de la celda]:")
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess.toString())
            val places = _personaje.actualMapa.esquemaPelea
            if (places.indexOf('|') == -1 || places.length < 2) {
                mess = StringBuilder("Las celdas de pelea en este mapa no se han definido.")
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess.toString())
                return
            }
            var team0 = ""
            var team1 = ""
            val p = places.split("\\|".toRegex()).toTypedArray()
            try {
                team0 = p[0]
            } catch (ignored: Exception) {
            }
            try {
                team1 = p[1]
            } catch (ignored: Exception) {
            }
            mess = StringBuilder("Team 0:\n")
            run {
                var a = 0
                while (a <= team0.length - 2) {
                    val code = team0.substring(a, a + 2)
                    mess.append(GestorEncriptador.cellCode_To_ID(code))
                    a += 2
                }
            }
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess.toString())
            mess = StringBuilder("Team 1:\n")
            var a = 0
            while (a <= team1.length - 2) {
                val code = team1.substring(a, a + 2)
                mess.append(GestorEncriptador.cellCode_To_ID(code)).append(" , ")
                a += 2
            }
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess.toString())
            return
        } else if (comando.equals("CREAR_GREMIO", ignoreCase = true)) {
            var perso: Personaje? = _personaje
            if (infos.size > 1) {
                perso = Mundo.getPersonajePorNombre(infos[1])
            }
            if (perso == null) {
                val mess = "El personaje no existe."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            if (!perso.isConectado) {
                val mess = "El personaje " + perso.nombre + " no esta conectado."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            if (perso._guild != null || perso.miembroGremio != null) {
                val mess = "El personaje " + perso.nombre + " ya tiene un gremio."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            GAME_SEND_gn_PACKET(perso)
            val mess = perso.nombre + ": Abrio panel de creacion de gremio"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            return
        } else if (comando.equals("CAMBIAR_AGRESION", ignoreCase = true)) {
            var personaje: Personaje? = _personaje
            var nombre: String? = null
            try {
                nombre = infos[1]
            } catch (ignored: Exception) {
            }
            personaje = Mundo.getPersonajePorNombre(nombre)
            if (personaje == null) {
                val mess = "El personaje no existe."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            personaje.setPuedeSerAgredido(!personaje.PuedeSerAgredido())
            var mess = personaje.nombre
            mess += if (personaje.PuedeSerAgredido()) " puede ser agredido." else " ya no puede ser agredido."
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess!!)
            if (!personaje.isConectado) {
                mess = "(El personaje " + personaje.nombre + " no esta conectado)"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            }
        } else if (comando.equals("ANUNCIO", ignoreCase = true)) {
            infos = mensaje.split(" ".toRegex(), 2).toTypedArray()
            ENVIAR_MENSAJE_A_TODOS(infos[1], MainServidor.CONFIG_MOTD_COLOR!!)
            return
        } else if (comando.equals("DESTRANSFORMAR", ignoreCase = true)) {
            var target: Personaje? = _personaje
            if (infos.size > 1) { //Si el nombre del personaje no esta espesificado
                target = Mundo.getPersonajePorNombre(infos[1])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            val morphID = target!!.clase * 10 + target.sexo
            target.setGFX(morphID)
            GAME_SEND_ERASE_ON_MAP_TO_MAP(target.actualMapa, target.id)
            ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(target.actualMapa, target)
            val str = "El personaje ha sido destransformado."
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("IR_NOMBRE", ignoreCase = true)) {
            val P = Mundo.getPersonajePorNombre(infos[1])
            if (P == null) {
                val str = "El personaje no existe."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            val mapID = P.actualMapa.id
            val cellID = P.actualCelda.id
            var target: Personaje? = _personaje
            if (infos.size > 2) { //Si el nombre del personaje no esta espesificado
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe."
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
                if (target.pelea != null) {
                    val str = "El objetivo esta en combate"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.teletransportar(mapID, cellID)
            val str = "Te has teletransportado al jugador objetivo"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("TRAER_HACIA_MI", ignoreCase = true)) {
            val target = Mundo.getPersonajePorNombre(infos[1])
            if (target == null) {
                val str = "El personaje no existe"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            if (target.pelea != null) {
                val str = "El personaje esta en pelea"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var P: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                P = Mundo.getPersonajePorNombre(infos[2])
                if (P == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            if (P!!.isConectado) {
                val mapID = P.actualMapa.id
                val cellID = P.actualCelda.id
                target.teletransportar(mapID, cellID)
                val str = "El personaje fue traido hacia ti"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
            } else {
                val str = "El personaje no esta en linea"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
            }
        } else if (comando.equals("NOMBRE_ANUNCIO", ignoreCase = true)) {
            infos = mensaje.split(" ".toRegex(), 2).toTypedArray()
            val prefix = _personaje.nombre + ": "
            ENVIAR_MENSAJE_A_TODOS(prefix + infos[1], MainServidor.CONFIG_MOTD_COLOR!!)
            return
        } else if (comando.equals("TELETRANSPORTAR", ignoreCase = true)) {
            var mapID: Short = -1
            var cellID = -1
            try {
                mapID = infos[1].toShort()
                cellID = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            if (mapID.toInt() == -1 || cellID == -1 || Mundo.getCarte(mapID) == null) {
                val str = "Mapa o celda invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            if (Mundo.getCarte(mapID).getMapa(cellID) == null) {
                val str = "Mapa o celda invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 3) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[3])
                if (target == null || target.pelea != null) {
                    val str = "El personaje esta en combate"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.teletransportar(mapID, cellID)
            val str = "El personaje se ha teletransportado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("IR_MAPA", ignoreCase = true)) {
            var mapX = 0
            var mapY = 0
            var cellID = 311
            var contID = 0 //Par défaut Amakna
            try {
                mapX = infos[1].toInt()
                mapY = infos[2].toInt()
                cellID = infos[3].toInt()
                contID = infos[4].toInt()
            } catch (ignored: Exception) {
            }
            val map = Mundo.getCarteByPosAndCont(mapX, mapY, contID)
            if (map == null) {
                val str = "Posicion del continente invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            if (map.getMapa(cellID) == null) {
                val str = "Celda invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 5) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[5])
                if (target == null || target.pelea != null) {
                    val str = "El personaje esta en combate"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
                if (target.pelea != null) {
                    val str = "El personaje esta en combate"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.teletransportar(map.id, cellID)
            val str = "El personaje se ha teletransportado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("ACCION", ignoreCase = true)) {
            //ACCION nombre tipo argumento condicion
            if (infos.size < 4) {
                val mess = "Argumento del comando incorrecto"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            var type = -100
            var args = ""
            var cond = ""
            var perso: Personaje? = _personaje
            try {
                perso = Mundo.getPersonajePorNombre(infos[1])
                if (perso == null) perso = _personaje
                type = infos[2].toInt()
                args = infos[3]
                if (infos.size > 4) cond = infos[4]
            } catch (e: Exception) {
                val mess = "Argumento del comando incorrecto"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            Accion(type, args, cond).apply(perso, null, -1, -1)
            val mess = "Accion efectuada"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
        } else {
            val mess = "Comando invalido"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
        }
    }

    fun ComandosGmNivelDos(comando: String, infos: Array<String>, mensaje: String) {
        if (_cuenta.gmlvl < 2) {
            _cuenta.juegoThread.closeSocket()
            return
        }
        if (comando.equals("SILENCIAR", ignoreCase = true)) {
            var perso: Personaje? = _personaje
            var name: String? = null
            try {
                name = infos[1]
            } catch (ignored: Exception) {
            }
            var time = 0
            try {
                time = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            perso = Mundo.getPersonajePorNombre(name)
            if (perso == null || time < 0) {
                val mess = "El personaje no existe o la duracion es invalida."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            var mess = "Usted silencio a " + perso.nombre + " por " + time + " segundos"
            if (perso.cuenta == null) {
                mess = "El personaje " + perso.nombre + " no esta conectado"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            perso.cuenta.mute(true, time)
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            if (!perso.isConectado) {
                mess = "El personaje " + perso.nombre + " no esta conectado"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            } else {
                ENVIAR_MENSAJE_DESDE_LANG(perso, "1124;$time")
            }
            return
        } else if (comando.equals("DEJAR_DE_SILENCIAR", ignoreCase = true)) {
            var perso: Personaje? = _personaje
            var name: String? = null
            try {
                name = infos[1]
            } catch (ignored: Exception) {
            }
            perso = Mundo.getPersonajePorNombre(name)
            if (perso == null) {
                val mess = "El personaje no existe"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            perso.cuenta.mute(false, 0)
            var mess = "Usted ha dejado que " + perso.nombre + " hable nuevamente"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            if (!perso.isConectado) {
                mess = "El personaje " + perso.nombre + " no esta conectado"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            }
        } else if (comando.equals("EXPULSAR", ignoreCase = true)) {
            var perso: Personaje? = _personaje
            var name: String? = null
            try {
                name = infos[1]
            } catch (ignored: Exception) {
            }
            perso = Mundo.getPersonajePorNombre(name)
            if (perso == null) {
                val mess = "El personaje no existe."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            if (perso.isConectado) {
                perso.cuenta.juegoThread.kick()
                val mess = "Usted ha expulsado a " + perso.nombre
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            } else {
                val mess = "El personaje " + perso.nombre + " no esta conectado"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            }
        } else if (comando.equals("DAR_PUNTOS_DE_HECHIZO", ignoreCase = true)) {
            var pts = -1
            try {
                pts = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (pts == -1) {
                val str = "Valor invalido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.addAgregarPuntosDeHechizo(pts)
            ENVIAR_PAQUETE_CARACTERISTICAS(target)
            val str = "La cantidad de puntos de hechizo del personaje " + _personaje.nombre + " se han modificado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("APRENDER_HECHIZO", ignoreCase = true)) {
            var spell = -1
            try {
                spell = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (spell == -1) {
                val str = "Valor invalido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var objetivo: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                objetivo = Mundo.getPersonajePorNombre(infos[2])
                if (objetivo == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            objetivo!!.AprenderHechizo(spell, 1, true, true)
            val str = "El personaje " + _personaje.nombre + " ha aprendido el hechizo"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("DAR_ALINEACION", ignoreCase = true)) {
            var align: Byte = -1
            try {
                align = infos[1].toByte()
            } catch (ignored: Exception) {
            }
            if (align < Constantes.ALIGNEMENT_NEUTRE || align > Constantes.ALIGNEMENT_MERCENAIRE) {
                val str = "Valor invalido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.modifAlignement(align)
            val str = "El personaje " + _personaje.nombre + " ha cambiado de alineacion"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("AGREGAR_RESPUESTA", ignoreCase = true)) {
            if (infos.size < 3) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Faltan datos")
                return
            }
            var id = 0
            try {
                id = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            val reps = infos[2]
            val Q = Mundo.getNPCQuestion(id)
            var str = ""
            if (id == 0 || Q == null) {
                str = "ID de la pregunta invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            Q.reponses = reps
            val a = actualizar_pregunta_npc(id, reps)
            str = "Lista de respuestas para la pregunta " + id + ": " + Q.reponses
            if (a) str += "Base de datos actualizada con exito"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
            return
        } else if (comando.equals("VER_RESPUESTAS", ignoreCase = true)) {
            var id = 0
            try {
                id = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            val Q = Mundo.getNPCQuestion(id)
            var str = ""
            if (id == 0 || Q == null) {
                str = "ID de la pregunta invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            str = "Lista de respuestas de la pregunta " + id + ": " + Q.reponses
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
            return
        } else if (comando.equals("DAR_HONOR", ignoreCase = true)) {
            var honor = 0
            try {
                honor = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            var target: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            var str = "Usted ha agregado " + honor + " de honor al personaje " + target!!.nombre
            if (target._align.toInt() == Constantes.ALIGNEMENT_NEUTRE) {
                str = "El jugador es neutral"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            target.addHonor(honor)
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("DAR_EXPERIENCIA_OFICIO", ignoreCase = true)) {
            var job = -1
            var xp = -1
            try {
                job = infos[1].toInt()
                xp = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            if (job == -1 || xp < 0) {
                val str = "Valor invalido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 3) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[3])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            val SM = target!!.getOficioPorID(job)
            if (SM == null) {
                val str = "El jugador no tiene el oficio indicado"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            SM.AgregarExperiencia(target, xp.toLong())
            val str = "La experiencia se ha agregado al oficio"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("APRENDER_OFICIO", ignoreCase = true)) {
            var job = -1
            try {
                job = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (job == -1 || Mundo.getMetier(job) == null) {
                val str = "Valor invalido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.learnJob(Mundo.getMetier(job))
            val str = "El oficio ha sido aprendido con exito"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("DAR_CAPITAL", ignoreCase = true)) {
            var pts = -1
            try {
                pts = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (pts == -1) {
                val str = "Valor invalido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.addPuntosDeCapital(pts)
            ENVIAR_PAQUETE_CARACTERISTICAS(target)
            val str = "El capital fue modificado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        }
        if (comando.equals("TAMAÑO", ignoreCase = true)) {
            var size = -1
            try {
                size = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (size == -1) {
                val str = "Medida invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.setTamaño(size)
            GAME_SEND_ERASE_ON_MAP_TO_MAP(target.actualMapa, target.id)
            ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(target.actualMapa, target)
            val str = "El tamaño del personaje " + _personaje.nombre + " se ha modificado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("TRANSFORMAR", ignoreCase = true)) {
            var morphID = -1
            try {
                morphID = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (morphID == -1) {
                val str = "ID de la transformacion invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 2) //Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.setGFX(morphID)
            GAME_SEND_ERASE_ON_MAP_TO_MAP(target.actualMapa, target.id)
            ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(target.actualMapa, target)
            val str = "El personaje " + _personaje.nombre + " se ha transformado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        }
        if (comando.equals("MOVER_NPC", ignoreCase = true)) {
            var id = 0
            try {
                id = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            val npc = _personaje.actualMapa.getNPC(id)
            if (id == 0 || npc == null) {
                val str = "ID negativa del NPC invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            val exC = npc.celdaID
            //on l'efface de la map
            GAME_SEND_ERASE_ON_MAP_TO_MAP(_personaje.actualMapa, id)
            //on change sa position/orientation
            npc.setCellID(_personaje.actualCelda.id)
            npc.setOrientation(_personaje.orientacion.toByte())
            //on envoie la modif
            ENVIAR_AGREGAR_NPC_EN_MAPA(_personaje.actualMapa, npc)
            var str = "El personaje se ha desplazado"
            if (_personaje.orientacion == 0 || _personaje.orientacion == 2 || _personaje.orientacion == 4 || _personaje.orientacion == 6) str += " pero se ha vuelto invisible, la orientacion no es valida."
            if (eliminar_npc_en_mapa(_personaje.actualMapa.id.toInt(), exC)
                    && agregar_npc_en_mapa(_personaje.actualMapa.id.toInt(), npc.modelo.id, _personaje.actualCelda.id, _personaje.orientacion)) ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str) else ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Error al momento de guardar la posicion")
        } else if (comando.equals("AGREGAR_SET", ignoreCase = true)) {
            var tID = 0
            var nom: String? = null
            try {
                if (infos.size > 3) nom = infos[3] else if (infos.size > 1) tID = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            val IS = Mundo.getItemSet(tID)
            if (tID == 0 || IS == null) {
                val mess = "El set $tID no existe"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            var useMax = false
            if (infos.size > 2) useMax = infos[2] == "MAXIMO" //Si un jet est spécifié
            var perso: Personaje? = _personaje
            if (nom != null) try {
                perso = Mundo.getPersonajePorNombre(nom)
            } catch (ignored: Exception) {
            }
            for (t in IS.itemTemplates) {
                val obj = t.createNewItem(1, useMax)
                if (perso != null) {
                    if (perso.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
                        Mundo.addObjet(obj, true)
                } else if (_personaje.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
                    Mundo.addObjet(obj, true)
            }
            var str = "Se ha creado el set $tID con exito"
            if (useMax) str += " en sus maximas caracteristicas"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comando.equals("DAR_NIVEL", ignoreCase = true)) {
            var count = 0
            try {
                count = infos[1].toInt()
                if (count < 1) count = 1
                if (count > Mundo.getExpLevelSize()) count = Mundo.getExpLevelSize()
                var perso: Personaje? = _personaje
                if (infos.size == 3) //Si le nom du perso est spécifié
                {
                    val name = infos[2]
                    perso = Mundo.getPersonajePorNombre(name)
                    if (perso == null) perso = _personaje
                }
                if (perso!!._lvl < count) {
                    while (perso._lvl < count) {
                        perso.levelUp(false, true)
                    }
                    if (perso.isConectado) {
                        GAME_SEND_SPELL_LIST(perso)
                        GAME_SEND_NEW_LVL_PACKET(perso.cuenta.juegoThread._out, perso._lvl)
                        ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                    }
                }
                val mess = "Cambiaste el nivel actual de " + perso.nombre + " a " + count
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            } catch (e: Exception) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Valor incorrecto.")
                return
            }
        } else if (comando.equals("CURAR", ignoreCase = true)) {
            var count = 0
            try {
                count = infos[1].toInt()
                if (count < 0) count = 0
                if (count > 100) count = 100
                var perso: Personaje? = _personaje
                if (infos.size == 3) //Si le nom du perso est spécifié
                {
                    val name = infos[2]
                    perso = Mundo.getPersonajePorNombre(name)
                    if (perso == null) perso = _personaje
                }
                val newPDV = perso!!._PDVMAX * count / 100
                perso._PDV = newPDV
                if (perso.isConectado) ENVIAR_PAQUETE_CARACTERISTICAS(perso)
                val mess = "Usted ha curado a " + perso.nombre + " en la cantidad de puntos de vida " + count
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            } catch (e: Exception) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Valor incorrecto")
                return
            }
        } else if (comando.equals("DAR_KAMAS", ignoreCase = true)) {
            var count = 0
            count = try {
                infos[1].toInt()
            } catch (e: Exception) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Valor incorrecto")
                return
            }
            if (count == 0) return
            var perso: Personaje? = _personaje
            if (infos.size == 3) //Si le nom du perso est spécifié
            {
                val name = infos[2]
                perso = Mundo.getPersonajePorNombre(name)
                if (perso == null) perso = _personaje
            }
            val curKamas = perso!!.kamas
            var newKamas = curKamas + count
            if (newKamas < 0) newKamas = 0
            if (newKamas > 1000000000) newKamas = 1000000000
            perso.kamas = newKamas
            if (perso.isConectado) ENVIAR_PAQUETE_CARACTERISTICAS(perso)
            var mess = "Usted ha "
            mess += (if (count < 0) "retirado" else "agregado") + " "
            mess += abs(count).toString() + " kamas a " + perso.nombre
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
        } else if (comando.equals("AGREGAR_ITEM", ignoreCase = true) || comando.equals("!getitem", ignoreCase = true)) {
            val isOffiCmd = comando.equals("!getitem", ignoreCase = true)
            if (_cuenta.gmlvl < 2) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "No tienes el nivel de GM necesario")
                return
            }
            var tID = 0
            try {
                tID = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (tID == 0) {
                val mess = "El objeto modelo $tID no existe"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            var qua = 1
            if (infos.size == 3) //Si une quantité est spécifiée
            {
                try {
                    qua = infos[2].toInt()
                } catch (ignored: Exception) {
                }
            }
            var useMax = false
            if (infos.size == 4 && !isOffiCmd) //Si un jet est spécifié
            {
                if (infos[3].equals("MAXIMO", ignoreCase = true)) useMax = true
            }
            val t = Mundo.getObjTemplate(tID)
            if (t == null) {
                val mess = "El objeto modelo $tID no existe"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
                return
            }
            if (qua < 1) qua = 1
            val obj = t.createNewItem(qua, useMax)
            if (_personaje.addObjet(obj, true)) //Si le joueur n'avait pas d'item similaire
                Mundo.addObjet(obj, true)
            var str = "Se ha creado un objeto $tID con exito"
            if (useMax) str += " en sus maximas caracteristicas"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
            GAME_SEND_Ow_PACKET(_personaje)
        } else if (comando.equals("REFRESCAR", ignoreCase = true)) {
            var Mob: String? = null
            try {
                Mob = infos[1]
            } catch (ignored: Exception) {
            }
            if (Mob == null) return
            _personaje.actualMapa.spawnGroupOnCommand(_personaje.actualCelda.id, Mob)
        } else if (comando.equals("DAR_TITULO", ignoreCase = true)) {
            var target: Personaje? = null
            var TitleID: Byte = 0
            try {
                target = Mundo.getPersonajePorNombre(infos[1])
                TitleID = infos[2].toByte()
            } catch (ignored: Exception) {
            }
            if (target == null) {
                val str = "El personaje no existe"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            target._title = TitleID
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "El titulo se ha agregado.")
            guardar_personaje(target, false)
            if (target.pelea == null) GAME_SEND_ALTER_GM_PACKET(target.actualMapa, target)
        } else {
            ComandosGmNivelUno(comando, infos, mensaje)
        }
    }

    fun ComandosGmNivelTres(command: String, infos: Array<String>, msg: String) {
        var infos = infos
        if (_cuenta.gmlvl < 3) {
            _cuenta.juegoThread.closeSocket()
            return
        }
        if (command.equals("REINICIAR", ignoreCase = true)) {
            System.exit(0)
        } else if (command.equals("DESCONGELAR_TURNOS", ignoreCase = true)) {
            Mundo.ticAllFightersTurns()
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "isAlive= " + MainServidor._passerTours!!.isAlive + ", SDATA= " + MainServidor._passerTours.toString())
        } else if (command.equals("EXPULSAR_A_TODOS", ignoreCase = true)) {
            MainServidor.gameServer!!.expulsaratodos()
        } else if (command.equals("FINALIZAR_TURNOS", ignoreCase = true)) {
            MainServidor._passerTours = Thread(todoslosturnospelea())
            MainServidor._passerTours!!.start()
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Finalizados")
        } else if (command.equals("GUARDAR", ignoreCase = true) && !MainServidor.isSaving) {
            val t = Thread(SaveThread())
            t.start()
            val mess = "Guardado lanzado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            return
        } else if (command.equals("IR_COORDENADAS", ignoreCase = true)) {
            val cell = _personaje.actualCelda.id
            val mess = "[" + Camino.getCellXCoord(_personaje.actualMapa, cell) + "," + Camino.getCellYCoord(_personaje.actualMapa, cell) + "]"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            return
        } else if (command.equals("ELIMINAR_CELDA_PELEA", ignoreCase = true)) {
            var cell = -1
            try {
                cell = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            if (cell < 0 || _personaje.actualMapa.getMapa(cell) == null) {
                cell = _personaje.actualCelda.id
            }
            val places = _personaje.actualMapa.esquemaPelea
            val p = places.split("\\|".toRegex()).toTypedArray()
            val newPlaces = StringBuilder()
            var team0 = ""
            var team1 = ""
            try {
                team0 = p[0]
            } catch (ignored: Exception) {
            }
            try {
                team1 = p[1]
            } catch (ignored: Exception) {
            }
            run {
                var a = 0
                while (a <= team0.length - 2) {
                    val c = p[0].substring(a, a + 2)
                    if (cell == GestorEncriptador.cellCode_To_ID(c)) {
                        a += 2
                        continue
                    }
                    newPlaces.append(c)
                    a += 2
                }
            }
            newPlaces.append("|")
            var a = 0
            while (a <= team1.length - 2) {
                val c = p[1].substring(a, a + 2)
                if (cell == GestorEncriptador.cellCode_To_ID(c)) {
                    a += 2
                    continue
                }
                newPlaces.append(c)
                a += 2
            }
            _personaje.actualMapa.setPlaces(newPlaces.toString())
            if (!guardar_mapa(_personaje.actualMapa)) return
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Las celdas se han modificado ($newPlaces)")
            return
        } else if (command.equals("BANEAR_PERSONAJE", ignoreCase = true)) {
            val P = Mundo.getPersonajePorNombre(infos[1])
            if (P == null) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "El personaje no existe")
                return
            }
            if (P.cuenta == null) cargar_cuenta_por_id(P.accID)
            if (P.cuenta == null) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Error")
                return
            }
            P.cuenta.isBanned = true
            actualizar_datos_cuenta(P.cuenta)
            if (P.cuenta.juegoThread != null) P.cuenta.juegoThread.kick()
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Usted ha baneado a " + P.nombre)
            return
        } else if (command.equals("DESBANEAR_PERSONAJE", ignoreCase = true)) {
            val P = Mundo.getPersonajePorNombre(infos[1])
            if (P == null) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "El personaje no existe")
                return
            }
            if (P.cuenta == null) cargar_cuenta_por_id(P.accID)
            if (P.cuenta == null) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Error")
                return
            }
            P.cuenta.isBanned = false
            actualizar_datos_cuenta(P.cuenta)
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Usted ha desbaneado a " + P.nombre)
            return
        } else if (command.equals("AGREGAR_ESQUEMA_DE_PELEA", ignoreCase = true)) {
            var team = -1
            var cell = -1
            try {
                team = infos[1].toInt()
                cell = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            if (team < 0 || team > 1) {
                val str = "Equipo o celda incorrecta"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            if (cell < 0 || _personaje.actualMapa.getMapa(cell) == null || !_personaje.actualMapa.getMapa(cell).isCaminable(true)) {
                cell = _personaje.actualCelda.id
            }
            val places = _personaje.actualMapa.esquemaPelea
            val p = places.split("\\|".toRegex()).toTypedArray()
            var already = false
            var team0 = ""
            var team1 = ""
            try {
                team0 = p[0]
            } catch (ignored: Exception) {
            }
            try {
                team1 = p[1]
            } catch (ignored: Exception) {
            }

            //Si case déjà utilisée
            println("""
    0 => $team0
    1 =>$team1
    Cell: ${GestorEncriptador.cellID_To_Code(cell)}
    """.trimIndent())
            run {
                var a = 0
                while (a <= team0.length - 2) {
                    if (cell == GestorEncriptador.cellCode_To_ID(team0.substring(a, a + 2))) already = true
                    a += 2
                }
            }
            var a = 0
            while (a <= team1.length - 2) {
                if (cell == GestorEncriptador.cellCode_To_ID(team1.substring(a, a + 2))) already = true
                a += 2
            }
            if (already) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "La celda ya existe en el esquema")
                return
            }
            if (team == 0) team0 += GestorEncriptador.cellID_To_Code(cell) else if (team == 1) team1 += GestorEncriptador.cellID_To_Code(cell)
            val newPlaces = "$team0|$team1"
            _personaje.actualMapa.setPlaces(newPlaces)
            if (!guardar_mapa(_personaje.actualMapa)) return
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Las celdas del esquema se han modificado ($newPlaces)")
            return
        } else if (command.equals("MODIFICAR_GRUPOS_MAXIMO_MOOBS", ignoreCase = true)) {
            infos = msg.split(" ".toRegex(), 4).toTypedArray()
            var id: Byte = -1
            try {
                id = infos[1].toByte()
            } catch (ignored: Exception) {
            }
            if (id.toInt() == -1) {
                val str = "Valor invalido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var mess = "El numero de grupo de moobs ha sido arreglado"
            _personaje.actualMapa.setMaxGroup(id)
            val ok = guardar_mapa(_personaje.actualMapa)
            if (ok) mess += " se ha guardado en la base de datos"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
        } else if (command.equals("AGREGAR_ACCION_RESPUESTA", ignoreCase = true)) {
            infos = msg.split(" ".toRegex(), 4).toTypedArray()
            var id = -30
            var repID = 0
            val args = infos[3]
            try {
                repID = infos[1].toInt()
                id = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            val rep = Mundo.getNPCreponse(repID)
            if (id == -30 || rep == null) {
                val str = "Al menos uno de los valores no es valido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var mess = "La accion ha sido agregada"
            rep.addAction(Accion(id, args, ""))
            val ok = agregar_respuesta_npc(repID, id, args)
            if (ok) mess += " se ha actualizado en la base de datos"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
        } else if (command.equals("AGREGAR_PREGUNTA_PRINCIPAL", ignoreCase = true)) {
            infos = msg.split(" ".toRegex(), 4).toTypedArray()
            var id = -30
            var q = 0
            try {
                q = infos[2].toInt()
                id = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (id == -30) {
                val str = "ID del NPC es invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var mess = "La pregunta inicial ha sido agregada"
            val npc = Mundo.getNPCTemplate(id)
            npc.setInitQuestion(q)
            val ok = actualizar_respuesta_de_npc(id, q)
            if (ok) mess += " se ha actualizado la base de datos"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
        } else if (command.equals("AGREGAR_ACCION_FIN_PELEA", ignoreCase = true)) {
            infos = msg.split(" ".toRegex(), 4).toTypedArray()
            var id = -30
            var type = 0
            val args = infos[3]
            val cond = infos[4]
            try {
                type = infos[1].toInt()
                id = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            if (id == -30) {
                val str = "Alguno de los datos es invalido"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var mess = "La accion se ha cambiado"
            _personaje.actualMapa.addEndFightAction(type, Accion(id, args, cond))
            val ok = agregar_fin_pelea_accion(_personaje.actualMapa.id.toInt(), type, id, args, cond)
            if (ok) mess += " se ha actualizado la base de datos"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess)
            return
        } else if (command.equals("ACTUALIZAR_GRUPO_FIX", ignoreCase = true)) {
            val groupData = infos[1]
            _personaje.actualMapa.addStaticGroup(_personaje.actualCelda.id, groupData)
            var str = "El grupo fue arreglado"
            //Sauvegarde DB de la modif
            if (guardar_nuevo_grupo_monstruos(_personaje.actualMapa.id.toInt(), _personaje.actualCelda.id, groupData)) str += " se ha actualizado la base de datos"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
            return
        } else if (command.equals("AGREGAR_NPC", ignoreCase = true)) {
            var id = 0
            try {
                id = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (id == 0 || Mundo.getNPCTemplate(id) == null) {
                val str = "ID del NPC invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            val npc = _personaje.actualMapa.addNpc(id, _personaje.actualCelda.id, _personaje.orientacion)
            ENVIAR_AGREGAR_NPC_EN_MAPA(_personaje.actualMapa, npc)
            var str = "El NPC se ha agregado"
            if (_personaje.orientacion == 0 || _personaje.orientacion == 2 || _personaje.orientacion == 4 || _personaje.orientacion == 6) str += " pero esta invisible, ya que la orientacion no es valida."
            if (agregar_npc_en_mapa(_personaje.actualMapa.id.toInt(), id, _personaje.actualCelda.id, _personaje.orientacion)) ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str) else ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Error al momento de guardar la posicion del npc")
        } else if (command.equals("ELIMINAR_NPC", ignoreCase = true)) {
            var id = 0
            try {
                id = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            val npc = _personaje.actualMapa.getNPC(id)
            if (id == 0 || npc == null) {
                val str = "ID negativa del npc es invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            val exC = npc.celdaID
            //on l'efface de la map
            GAME_SEND_ERASE_ON_MAP_TO_MAP(_personaje.actualMapa, id)
            _personaje.actualMapa.removeNpcOrMobGroup(id)
            val str = "El npc se ha suprimido"
            if (eliminar_npc_en_mapa(_personaje.actualMapa.id.toInt(), exC)) ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str) else ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "error al guardar la posicion")
        } else if (command.equals("ELIMINAR_ACCION_DE_CELDA", ignoreCase = true)) {
            var cellID = -1
            try {
                cellID = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (cellID == -1 || _personaje.actualMapa.getMapa(cellID) == null) {
                val str = "Celda invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            _personaje.actualMapa.getMapa(cellID).EliminarAccionDeCelda()
            val success = eliminar_celdas(_personaje.actualMapa.id.toInt(), cellID)
            var str = ""
            str = if (success) "La accion de la celda ha sido eliminada" else "La accion de la celda no puede ser eliminada"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (command.equals("AGREGAR_ACCION_DE_CELDA", ignoreCase = true)) {
            var actionID = -1
            var args = ""
            var cond = ""
            try {
                actionID = infos[1].toInt()
                args = infos[2]
                cond = infos[3]
            } catch (ignored: Exception) {
            }
            if (args == "" || actionID <= -3) {
                val str = "Celda invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            _personaje.actualCelda.addOnCellStopAction(actionID, args, cond)
            val success = guardar_celdas(_personaje.actualMapa.id.toInt(), _personaje.actualCelda.id, actionID, 1, args, cond)
            var str = ""
            str = if (success) "La accion de celda se ha agregado" else "La accion de celda no puede ser agregada"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (command.equals("ELIMINAR_OBJETO_NPC_VENTA", ignoreCase = true)) {
            if (_cuenta.gmlvl < 3) return
            var npcGUID = 0
            var itmID = -1
            try {
                npcGUID = infos[1].toInt()
                itmID = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            val npc = _personaje.actualMapa.getNPC(npcGUID).modelo
            if (npcGUID == 0 || itmID == -1 || npc == null) {
                val str = "Id negativa del NPC o ID del item invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var str = ""
            str = if (npc.delItemVendor(itmID)) "El objeto se ha eliminado" else "El objeto no puede ser retirado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (command.equals("AGREGAR_OBJETO_NPC_VENTA", ignoreCase = true)) {
            if (_cuenta.gmlvl < 3) return
            var npcGUID = 0
            var itmID = -1
            try {
                npcGUID = infos[1].toInt()
                itmID = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            val npc = _personaje.actualMapa.getNPC(npcGUID).modelo
            val item = Mundo.getObjTemplate(itmID)
            if (npcGUID == 0 || itmID == -1 || npc == null || item == null) {
                val str = "ID negativa del NPC o ID del item invalida"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var str = ""
            str = if (npc.addItemVendor(item)) "El objeto se ha agregado con exito" else "El objeto no puede ser agregado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (command.equals("AGREGAR_CERCADO", ignoreCase = true)) {
            var size = -1
            var owner = -2
            var price = -1
            try {
                size = infos[1].toInt()
                owner = infos[2].toInt()
                price = infos[3].toInt()
                if (price > 20000000) price = 20000000
                if (price < 0) price = 0
            } catch (ignored: Exception) {
            }
            if (size == -1 || owner == -2 || price == -1 || _personaje.actualMapa.mountPark != null) {
                val str = "Informacion invalida, no se puede configurar"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            val MP = MountPark(owner, _personaje.actualMapa, _personaje.actualCelda.id, size, "", -1, price)
            _personaje.actualMapa.mountPark = MP
            guardar_cercados(MP)
            val str = "La configuracion del cercado ha sido un exito"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (command.equals("PROGRAMAR_REINICIO", ignoreCase = true)) {
            var time = 30
            var OffOn = 0
            try {
                OffOn = infos[1].toInt()
                time = infos[2].toInt()
            } catch (ignored: Exception) {
            }
            if (OffOn == 1 && _TimerStart) // demande de démarer le reboot
            {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Un reinicio se ha programado.")
            } else if (OffOn == 1 && !_TimerStart) {
                _timer = createTimer(time)
                _timer!!.start()
                _TimerStart = true
                var timeMSG = "minutos"
                if (time <= 1) {
                    timeMSG = "minuto"
                }
                ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;$time $timeMSG")
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Reinicio lanzado.")
            } else if (OffOn == 0 && _TimerStart) {
                _timer!!.stop()
                _TimerStart = false
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Reinicio se ha detenido.")
            } else if (OffOn == 0 && !_TimerStart) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "No se puede reiniciar.")
            }
        } else {
            ComandosGmNivelDos(command, infos, msg)
        }
    }

    fun ComandosGmNivelCuatro(comandos: String, infos: Array<String>, mensaje: String) {
        var infos = infos
        if (_cuenta.gmlvl < 4) {
            _cuenta.juegoThread.closeSocket()
            return
        }
        if (comandos.equals("DAR_ADMIN", ignoreCase = true)) {
            var gmLvl = -100
            try {
                gmLvl = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            if (gmLvl == -100) {
                val str = "Valor incorrecto"
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            var target: Personaje? = _personaje
            if (infos.size > 2) { //Si un nom de perso est spécifié
                target = Mundo.getPersonajePorNombre(infos[2])
                if (target == null) {
                    val str = "El personaje no existe"
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                    return
                }
            }
            target!!.cuenta.gmlvl = gmLvl
            actualizar_datos_cuenta(target.cuenta)
            val str = "El nivel de admin de " + _personaje.nombre + " se ha modificado"
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
        } else if (comandos.equals("BLOQUEAR_SERVIDOR", ignoreCase = true)) {
            var LockValue: Byte = 1 //Accessible
            try {
                LockValue = infos[1].toByte()
            } catch (ignored: Exception) {
            }
            if (LockValue > 2) LockValue = 2
            if (LockValue < 0) LockValue = 0
            Mundo.set_state(LockValue.toShort())
            when {
                LockValue.toInt() == 1 -> {
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Servidor accesible.")
                }
                LockValue.toInt() == 0 -> {
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Servidor inaccesible.")
                }
                LockValue.toInt() == 2 -> {
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Servidor en modo guardado.")
                }
            }
        } else if (comandos.equals("AGREGAR_PUBLICIDAD", ignoreCase = true)) {
            infos = mensaje.split(" ".toRegex(), 2).toTypedArray()
            val nuevapublicidad: String = try {
                infos[1]
            } catch (e: Exception) {
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "No puedes cargar una publicidad en blanco")
                return
            }
            agregar_publicidad(nuevapublicidad)
            cargar_publicidades_automaticas()
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Publicidad cargada con exito")
        } else if (comandos.equals("SOLO_ADMIN", ignoreCase = true)) {
            var GmAccess: Byte = 0
            var KickPlayer: Byte = 0
            try {
                GmAccess = infos[1].toByte()
                KickPlayer = infos[2].toByte()
            } catch (ignored: Exception) {
            }
            Mundo.setGmAccess(GmAccess)
            ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Server solo para admin nivel: $GmAccess")
            if (KickPlayer > 0) {
                for (z in Mundo.getOnlinePersos()) {
                    if (z.cuenta.gmlvl < GmAccess) z.cuenta.juegoThread.closeSocket()
                }
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Lo jugadores con nivel de admin inferior a $GmAccess seran expulsados.")
            }
        } else if (comandos.equals("BANEAR_IP", ignoreCase = true)) {
            var P: Personaje? = null
            try {
                P = Mundo.getPersonajePorNombre(infos[1])
            } catch (ignored: Exception) {
            }
            if (P == null || !P.isConectado) {
                val str = "El personaje no existe."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
            if (!Constantes.IPcompareToBanIP(P.cuenta.actualIP)) {
                Constantes.BAN_IP += "," + P.cuenta.actualIP
                if (agregar_ip_baneada(P.cuenta.actualIP)) {
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "La IP ha sido baneada.")
                }
                if (P.isConectado) {
                    P.cuenta.juegoThread.kick()
                    ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "El jugador ha sido expulsado.")
                }
            } else {
                val str = "La IP no existe."
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, str)
                return
            }
        } else if (comandos.equals("VER_MERCADILLO_TOTAL", ignoreCase = true)) {
            var numb = 1
            try {
                numb = infos[1].toInt()
            } catch (ignored: Exception) {
            }
            fullHdv(numb)
        } else {
            ComandosGmNivelTres(comandos, infos, mensaje)
        }
    }

    private fun fullHdv(ofEachTemplate: Int) {
        ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Démarrage du remplissage!")
        var objet: Objeto? = null
        var entry: HdvEntry? = null
        var amount: Byte = 0
        var hdv = 0
        var lastSend = 0
        val time1 = System.currentTimeMillis() //TIME
        for (curTemp in Mundo.getObjetosModelos()) { //Boucler dans les template
            try {
                if (MainServidor.NOTINHDV.contains(curTemp.id)) continue
                for (j in 0 until ofEachTemplate) { //Ajouter plusieur fois le template
                    if (curTemp.type == 85) break
                    objet = curTemp.createNewItem(1, false)
                    hdv = getHdv(objet.template.type)
                    if (hdv < 0) break
                    amount = Formulas.getRandomValue(1, 3).toByte()
                    entry = HdvEntry(CalcularPrecio(objet, amount.toInt()), amount, -1, objet)
                    objet.quantity = entry.getAmount(true).toInt()
                    Mundo.getHdv(hdv).addEntry(entry)
                    Mundo.addObjet(objet, false)
                }
            } catch (e: Exception) {
                continue
            }
            if ((System.currentTimeMillis() - time1) / 1000 != lastSend.toLong() && (System.currentTimeMillis() - time1) / 1000 % 3 == 0L) {
                lastSend = ((System.currentTimeMillis() - time1) / 1000).toInt()
                ENVIAR_TEXTO_EN_CONSOLA(_imprimir, ((System.currentTimeMillis() - time1) / 1000).toString() + "sec Template: " + curTemp.id)
            }
        }
        ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Remplissage fini en " + (System.currentTimeMillis() - time1) + "ms")
        Mundo.saveAll(null)
        ENVIAR_MENSAJE_A_TODOS("HDV remplis!", MainServidor.CONFIG_MOTD_COLOR!!)
    }

    private fun getHdv(type: Int): Int {
        val rand = Formulas.getRandomValue(1, 4)
        var map = -1
        return when (type) {
            12, 14, 26, 43, 44, 45, 66, 70, 71, 86 -> {
                map = when (rand) {
                    1 -> {
                        4271
                    }
                    2 -> {
                        4607
                    }
                    else -> {
                        7516
                    }
                }
                map
            }
            1, 9 -> {
                map = when (rand) {
                    1 -> {
                        4216
                    }
                    2 -> {
                        4622
                    }
                    else -> {
                        7514
                    }
                }
                map
            }
            18, 72, 77, 90, 97, 113, 116 -> {
                map = if (rand == 1) {
                    8759
                } else {
                    8753
                }
                map
            }
            63, 64, 69 -> {
                map = when (rand) {
                    1 -> {
                        4287
                    }
                    2 -> {
                        4595
                    }
                    3 -> {
                        7515
                    }
                    else -> {
                        7350
                    }
                }
                map
            }
            33, 42 -> {
                map = when (rand) {
                    1 -> {
                        2221
                    }
                    2 -> {
                        4630
                    }
                    else -> {
                        7510
                    }
                }
                map
            }
            84, 93, 112, 114 -> {
                map = when (rand) {
                    1 -> {
                        4232
                    }
                    2 -> {
                        4627
                    }
                    else -> {
                        12262
                    }
                }
                map
            }
            38, 95, 96, 98, 108 -> {
                map = when (rand) {
                    1 -> {
                        4178
                    }
                    2 -> {
                        5112
                    }
                    else -> {
                        7289
                    }
                }
                map
            }
            10, 11 -> {
                map = when (rand) {
                    1 -> {
                        4183
                    }
                    2 -> {
                        4562
                    }
                    else -> {
                        7602
                    }
                }
                map
            }
            13, 25, 73, 75, 76 -> {
                map = if (rand == 1) {
                    8760
                } else {
                    8754
                }
                map
            }
            5, 6, 7, 8, 19, 20, 21, 22 -> {
                map = when (rand) {
                    1 -> {
                        4098
                    }
                    2 -> {
                        5317
                    }
                    else -> {
                        7511
                    }
                }
                map
            }
            39, 40, 50, 51, 88 -> {
                map = when (rand) {
                    1 -> {
                        4179
                    }
                    2 -> {
                        5311
                    }
                    else -> {
                        7443
                    }
                }
                map
            }
            87 -> {
                map = if (rand == 1) {
                    6159
                } else {
                    6167
                }
                map
            }
            34, 52, 60 -> {
                map = when (rand) {
                    1 -> {
                        4299
                    }
                    2 -> {
                        4629
                    }
                    else -> {
                        7397
                    }
                }
                map
            }
            41, 49, 62 -> {
                map = when (rand) {
                    1 -> {
                        4247
                    }
                    2 -> {
                        4615
                    }
                    3 -> {
                        7501
                    }
                    else -> {
                        7348
                    }
                }
                map
            }
            15, 35, 36, 46, 47, 48, 53, 54, 55, 56, 57, 58, 59, 65, 68, 103, 104, 105, 106, 107, 109, 110, 111 -> {
                map = when (rand) {
                    1 -> {
                        4262
                    }
                    2 -> {
                        4646
                    }
                    else -> {
                        7413
                    }
                }
                map
            }
            78 -> {
                map = if (rand == 1) {
                    8757
                } else {
                    8756
                }
                map
            }
            2, 3, 4 -> {
                map = when (rand) {
                    1 -> {
                        4174
                    }
                    2 -> {
                        4618
                    }
                    else -> {
                        7512
                    }
                }
                map
            }
            16, 17, 81 -> {
                map = when (rand) {
                    1 -> {
                        4172
                    }
                    2 -> {
                        4588
                    }
                    else -> {
                        7513
                    }
                }
                map
            }
            83 -> {
                map = if (rand == 1) {
                    10129
                } else {
                    8482
                }
                map
            }
            82 -> 8039
            else -> -1
        }
    }

    private fun CalcularPrecio(obj: Objeto?, logAmount: Int): Int {
        val amount: Int = ((10.0.pow(logAmount.toDouble()) / 10).toByte()).toInt()
        var stats = 0
        for (curStat in obj!!.stats.map.values) {
            stats += curStat
        }
        return if (stats > 0) ((Math.cbrt(stats.toDouble()) * obj.template.level.toDouble().pow(2.0) * 10 + Formulas.getRandomValue(1, obj.template.level * 100)) * amount).toInt() else ((Math.pow(obj.template.level.toDouble(), 2.0) * 10 + Formulas.getRandomValue(1, obj.template.level * 100)) * amount).toInt()
    }
}