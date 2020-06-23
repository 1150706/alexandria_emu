package realm

import comunes.Constantes
import comunes.GestorSQL.Cargar_cuenta_por_usuario
import comunes.GestorSalida
import comunes.MainServidor
import comunes.Mundo
import objetos.Cuenta
import realm.Pending.sistema_de_pendientes
import realm.RealmServer.Companion.addToSockLog
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class RealmThread(sock: Socket?) : Runnable {
    private var _in: BufferedReader? = null
    private var _thread: Thread? = null
    var _imprimir: PrintWriter? = null
    private var _s: Socket? = null
    private var _hashKey: String? = null
    private var _numerodepaquete = 0
    private var _nombredecuenta: String? = null
    private var _cuenta: Cuenta? = null
    override fun run() {
        try {
            var packet = StringBuilder()
            val charCur = CharArray(1)
            if (MainServidor.ENVIAR_POLITICA_DE_PRIVACIDAD) GestorSalida.REALM_SEND_POLICY_FILE(_imprimir)
            _hashKey = GestorSalida.REALM_SEND_HC_PACKET(_imprimir)
            while (_in!!.read(charCur, 0, 1) != -1 && MainServidor.isRunning) {
                if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
                    packet.append(charCur[0])
                } else if (packet.isNotEmpty()) {
                    if (MainServidor.MOSTRAR_RECIBIDOS) {
                        addToSockLog("Realm: Recibido << $packet")
                    }
                    _numerodepaquete++
                    parsePacket(packet.toString())
                    packet = StringBuilder()
                }
            }
        } catch (e: IOException) {
            try {
                _in!!.close()
                _imprimir!!.close()
                if (_cuenta != null) {
                    _cuenta!!.setCurPerso(null)
                    _cuenta!!.setGameThread(null)
                    _cuenta!!.realmThread = null
                    _cuenta!!.actualIP = ""
                }
                if (!_s!!.isClosed) _s!!.close()
                _thread!!.interrupt()
            } catch (ignored: IOException) {
            }
        } finally {
            try {
                _in!!.close()
                _imprimir!!.close()
                if (_cuenta != null) {
                    _cuenta!!.setCurPerso(null)
                    _cuenta!!.setGameThread(null)
                    _cuenta!!.realmThread = null
                    _cuenta!!.actualIP = ""
                }
                if (!_s!!.isClosed) _s!!.close()
                _thread!!.interrupt()
            } catch (ignored: IOException) {
            }
        }
    }

    private fun parsePacket(packet: String) {
        when (_numerodepaquete) {
            1 -> if (!packet.equals(MainServidor.VERSION_DEL_CLIENTE, ignoreCase = true) && !MainServidor.IGNORAR_VERSION_DEL_CLIENTE) {
                GestorSalida.REALM_SEND_REQUIRED_VERSION(_imprimir)
                try {
                    _s!!.close()
                } catch (ignored: IOException) {
                }
            }
            2 -> _nombredecuenta = packet.toLowerCase()
            3 -> {
                if (!packet.substring(0, 2).equals("#1", ignoreCase = true)) {
                    try {
                        _s!!.close()
                    } catch (ignored: IOException) {
                    }
                }
                if (Cuenta.COMPTE_LOGIN(_nombredecuenta, packet, _hashKey)) {
                    _cuenta = Mundo.getCompteByName(_nombredecuenta)
                    if (_cuenta!!.isConectado && _cuenta!!.juegoThread != null) {
                        _cuenta!!.juegoThread.closeSocket()
                    } else if (_cuenta!!.isConectado && _cuenta!!.juegoThread == null) {
                        GestorSalida.ENVIAR_ESTA_CONECTADO(_imprimir)
                        GestorSalida.ENVIAR_ESTA_CONECTADO(_cuenta!!.realmThread._imprimir)
                        return
                    }
                    if (_cuenta!!.isBanned) {
                        GestorSalida.REALM_SEND_BANNED(_imprimir)
                        try {
                            _s!!.close()
                        } catch (ignored: IOException) {
                        }
                        return
                    }
                    if (MainServidor.MAXIMO_DE_CONECTADOS != -1 && MainServidor.MAXIMO_DE_CONECTADOS <= MainServidor.gameServer!!.playerNumber) {
                        //Seulement si joueur
                        if (_cuenta!!.gmlvl == 0 && _cuenta!!.vip == 0) {
                            GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir)
                            try {
                                _s!!.close()
                            } catch (ignored: IOException) {
                            }
                            return
                        }
                    }
                    if (Mundo.getGmAccess() > _cuenta!!.gmlvl) {
                        GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir)
                        return
                    }
                    val ip = _s!!.inetAddress.hostAddress
                    if (Constantes.IPcompareToBanIP(ip)) {
                        GestorSalida.REALM_SEND_BANNED(_imprimir)
                        return
                    }
                    //Verification Multi compte
                    if (!MainServidor.HABILITAR_MULTI_CUENTA) {
                        if (Mundo.IpEstaUsada(ip)) {
                            GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir)
                            try {
                                _s!!.close()
                            } catch (ignored: IOException) {
                            }
                            return
                        }
                    }
                    _cuenta!!.realmThread = this
                    _cuenta!!.actualIP = ip
                    RealmServer.totalabonado++ //On incrémente le total
                    //_cuenta!!._position = RealmServer.totalabonado //On lui donne une position
                    GestorSalida.REALM_SEND_Ad_Ac_AH_AlK_AQ_PACKETS(_imprimir, _cuenta!!.apodo, if (_cuenta!!.gmlvl > 0) 1 else 0, _cuenta!!.pregunta)
                } else  //Si le compte n'a pas été reconnu
                {
                    Cargar_cuenta_por_usuario(_nombredecuenta!!)
                    if (Cuenta.COMPTE_LOGIN(_nombredecuenta, packet, _hashKey)) {
                        _cuenta = Mundo.getCompteByName(_nombredecuenta)
                        if (_cuenta!!.isConectado && _cuenta!!.juegoThread != null) {
                            _cuenta!!.juegoThread.closeSocket()
                        } else if (_cuenta!!.isConectado && _cuenta!!.juegoThread == null) {
                            GestorSalida.ENVIAR_ESTA_CONECTADO(_imprimir)
                            GestorSalida.ENVIAR_ESTA_CONECTADO(_cuenta!!.realmThread._imprimir)
                            return
                        }
                        if (_cuenta!!.isBanned) {
                            GestorSalida.REALM_SEND_BANNED(_imprimir)
                            try {
                                _s!!.close()
                            } catch (ignored: IOException) {
                            }
                            return
                        }
                        if (MainServidor.MAXIMO_DE_CONECTADOS != -1 && MainServidor.MAXIMO_DE_CONECTADOS <= MainServidor.gameServer!!.playerNumber) {
                            //Seulement si joueur
                            if (_cuenta!!.gmlvl == 0 && _cuenta!!.vip == 0) {
                                GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir)
                                try {
                                    _s!!.close()
                                } catch (ignored: IOException) {
                                }
                                return
                            }
                        }
                        if (Mundo.getGmAccess() > _cuenta!!.gmlvl) {
                            GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir)
                            return
                        }
                        val ip = _s!!.inetAddress.hostAddress
                        if (Constantes.IPcompareToBanIP(ip)) {
                            GestorSalida.REALM_SEND_BANNED(_imprimir)
                            return
                        }
                        //Verification Multi compte
                        if (!MainServidor.HABILITAR_MULTI_CUENTA) {
                            if (Mundo.IpEstaUsada(ip)) {
                                GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir)
                                try {
                                    _s!!.close()
                                } catch (ignored: IOException) {
                                }
                                return
                            }
                        }
                        _cuenta!!.actualIP = ip
                        _cuenta!!.realmThread = this
                        RealmServer.totalabonado++ //On incrémente le total
                        //_cuenta!!._position = RealmServer.totalabonado //On lui donne une position
                        GestorSalida.REALM_SEND_Ad_Ac_AH_AlK_AQ_PACKETS(_imprimir, _cuenta!!.apodo, if (_cuenta!!.gmlvl > 0) 1 else 0, _cuenta!!.pregunta)
                    } else { //Si le compte n'a pas été reconnu
                        GestorSalida.REALM_SEND_LOGIN_ERROR(_imprimir)
                        try {
                            _s!!.close()
                        } catch (ignored: IOException) {
                        }
                    }
                }
            }
            else -> when {
                packet.startsWith("Af") -> {
                    _numerodepaquete--
                    sistema_de_pendientes(_cuenta)
                    Mundo.getPersonajePorCuenta(_cuenta!!.id)
                }
                packet.startsWith("Ax") -> {
                    if (_cuenta == null) return
                    //cargar_personaje_por_cuenta(_cuenta!!.id)
                    //Mundo.getPersonajePorCuenta(_cuenta!!.id)
                    GestorSalida.REALM_SEND_PERSO_LIST(_imprimir, _cuenta!!.numeroDePersonajes)
                }
                packet == "AX1" -> {
                    MainServidor.gameServer!!.addWaitingCompte(_cuenta)
                    val ip = _cuenta!!.actualIP
                    Mundo.getPersonajePorCuenta(_cuenta!!.id)
                    GestorSalida.REALM_SEND_GAME_SERVER_IP(_imprimir, _cuenta!!.id, ip == "127.0.0.1")
                }
            }
        }
    }

    init {
        try {
            _s = sock
            _in = BufferedReader(InputStreamReader(_s!!.getInputStream()))
            _imprimir = PrintWriter(_s!!.getOutputStream())
            _thread = Thread(this)
            _thread!!.isDaemon = true
            _thread!!.start()
        } catch (e: IOException) {
            try {
                if (!_s!!.isClosed) _s!!.close()
            } catch (ignored: IOException) {
            }
        } finally {
            if (_cuenta != null) {
                _cuenta!!.realmThread = null
                _cuenta!!.setGameThread(null)
                _cuenta!!.actualIP = ""
            }
        }
    }
}