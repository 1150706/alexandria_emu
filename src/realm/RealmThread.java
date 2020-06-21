package realm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import objetos.Cuenta;

import comunes.MainServidor;
import comunes.Constantes;
import comunes.GestorSQL;
import comunes.GestorSalida;
import comunes.Mundo;

public class RealmThread implements Runnable{
    private BufferedReader _in;
    private Thread _thread;
    public PrintWriter _imprimir;
    private Socket _s;
    private String _hashKey;
    private int _numerodepaquete = 0;
    private String _nombredecuenta;
    private Cuenta _cuenta;

    public RealmThread(Socket sock) {
        try {
            _s = sock;
            _in = new BufferedReader(new InputStreamReader(_s.getInputStream()));
            _imprimir = new PrintWriter(_s.getOutputStream());
            _thread = new Thread(this);
            _thread.setDaemon(true);
            _thread.start();
        } catch(IOException e) {
            try {
                if(!_s.isClosed())_s.close();
            } catch (IOException ignored) {}
        } finally {
            if(_cuenta != null) {
                _cuenta.setRealmThread(null);
                _cuenta.setGameThread(null);
                _cuenta.setActualIP("");
            }
        }
    }

    public void run() {
        try {
            StringBuilder packet = new StringBuilder();
            char[] charCur = new char[1];
            if(MainServidor.ENVIAR_POLITICA_DE_PRIVACIDAD)
                GestorSalida.REALM_SEND_POLICY_FILE(_imprimir);

            _hashKey = GestorSalida.REALM_SEND_HC_PACKET(_imprimir);

            while(_in.read(charCur, 0, 1)!=-1 && MainServidor.isRunning) {
                if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
                {
                    packet.append(charCur[0]);
                }else if(packet.length() > 0) {

                    if(MainServidor.MOSTRAR_RECIBIDOS){
                        RealmServer.addToSockLog("Realm: Recibido << "+packet);
                    }
                    _numerodepaquete++;
                    parsePacket(packet.toString());
                    packet = new StringBuilder();
                }
            }
        }catch(IOException e) {
            try {
                _in.close();
                _imprimir.close();
                if(_cuenta != null) {
                    _cuenta.setCurPerso(null);
                    _cuenta.setGameThread(null);
                    _cuenta.setRealmThread(null);
                    _cuenta.setActualIP("");
                }
                if(!_s.isClosed())_s.close();
                _thread.interrupt();
            }catch(IOException ignored){}
        } finally {
            try {
                _in.close();
                _imprimir.close();
                if(_cuenta != null) {
                    _cuenta.setCurPerso(null);
                    _cuenta.setGameThread(null);
                    _cuenta.setRealmThread(null);
                    _cuenta.setActualIP("");
                }
                if(!_s.isClosed())_s.close();
                _thread.interrupt();
            }catch(IOException ignored){}
        }
    }

    private void parsePacket(String packet) {
        switch(_numerodepaquete) {
            case 1://Version
                if(!packet.equalsIgnoreCase(MainServidor.VERSION_DEL_CLIENTE) && !MainServidor.IGNORAR_VERSION_DEL_CLIENTE) {
                    GestorSalida.REALM_SEND_REQUIRED_VERSION(_imprimir);
                    try {
                        this._s.close();
                    } catch (IOException ignored) {}
                }
                break;
            case 2://Nombre de cuenta
                _nombredecuenta = packet.toLowerCase();
                break;
            case 3://Contraseña
                if(!packet.substring(0, 2).equalsIgnoreCase("#1")) {
                    try {
                        this._s.close();
                    } catch (IOException ignored) {}
                }

                if(Cuenta.COMPTE_LOGIN(_nombredecuenta, packet,_hashKey))
                {
                    _cuenta = Mundo.getCompteByName(_nombredecuenta);
                    if(_cuenta.isConectado() && _cuenta.getJuegoThread() != null)
                    {
                        _cuenta.getJuegoThread().closeSocket();
                    }else if(_cuenta.isConectado() && _cuenta.getJuegoThread() == null)
                    {
                        GestorSalida.ENVIAR_ESTA_CONECTADO(_imprimir);
                        GestorSalida.ENVIAR_ESTA_CONECTADO(_cuenta.getRealmThread()._imprimir);
                        return;
                    }
                    if(_cuenta.isBanned())
                    {
                        GestorSalida.REALM_SEND_BANNED(_imprimir);
                        try {
                            _s.close();
                        } catch (IOException ignored) {}
                        return;
                    }
                    if(MainServidor.MAXIMO_DE_CONECTADOS != -1 && MainServidor.MAXIMO_DE_CONECTADOS <= MainServidor.gameServer.getPlayerNumber())
                    {
                        //Seulement si joueur
                        if(_cuenta.getGMLVL() == 0  && _cuenta.getVIP() == 0)
                        {
                            GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir);
                            try {
                                _s.close();
                            } catch (IOException ignored) {}
                            return;
                        }
                    }
                    if(Mundo.getGmAccess() > _cuenta.getGMLVL()) {
                        GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir);
                        return;
                    }
                    String ip = _s.getInetAddress().getHostAddress();
                    if(Constantes.IPcompareToBanIP(ip)) {
                        GestorSalida.REALM_SEND_BANNED(_imprimir);
                        return;
                    }
                    //Verification Multi compte
                    if(!MainServidor.HABILITAR_MULTI_CUENTA) {
                        if(Mundo.IpEstaUsada(ip)) {
                            GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir);
                            try {
                                _s.close();
                            } catch (IOException ignored) {}
                            return;
                        }
                    }
                    _cuenta.setRealmThread(this);
                    _cuenta.setActualIP(ip);
                    RealmServer.totalabonado++;//On incrémente le total
                    _cuenta._position = RealmServer.totalabonado;//On lui donne une position
                    GestorSalida.REALM_SEND_Ad_Ac_AH_AlK_AQ_PACKETS(_imprimir, _cuenta.getApodo(),(_cuenta.getGMLVL()>0?(1):(0)), _cuenta.get_question() );
                }else//Si le compte n'a pas été reconnu
                {
                    GestorSQL.Cargar_cuenta_por_usuario(_nombredecuenta);
                    if(Cuenta.COMPTE_LOGIN(_nombredecuenta, packet,_hashKey)) {
                        _cuenta = Mundo.getCompteByName(_nombredecuenta);
                        if(_cuenta.isConectado() && _cuenta.getJuegoThread() != null) {
                            _cuenta.getJuegoThread().closeSocket();
                        }else if(_cuenta.isConectado() && _cuenta.getJuegoThread() == null) {
                            GestorSalida.ENVIAR_ESTA_CONECTADO(_imprimir);
                            GestorSalida.ENVIAR_ESTA_CONECTADO(_cuenta.getRealmThread()._imprimir);
                            return;
                        }
                        if(_cuenta.isBanned()) {
                            GestorSalida.REALM_SEND_BANNED(_imprimir);
                            try {
                                this._s.close();
                            } catch (IOException ignored) {}
                            return;
                        }
                        if(MainServidor.MAXIMO_DE_CONECTADOS != -1 && MainServidor.MAXIMO_DE_CONECTADOS <= MainServidor.gameServer.getPlayerNumber()) {
                            //Seulement si joueur
                            if(_cuenta.getGMLVL() == 0  && _cuenta.getVIP() == 0) {
                                GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir);
                                try {
                                    _s.close();
                                } catch (IOException ignored) {}
                                return;
                            }
                        }
                        if(Mundo.getGmAccess() > _cuenta.getGMLVL()) {
                            GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir);
                            return;
                        }
                        String ip = _s.getInetAddress().getHostAddress();
                        if(Constantes.IPcompareToBanIP(ip)) {
                            GestorSalida.REALM_SEND_BANNED(_imprimir);
                            return;
                        }
                        //Verification Multi compte
                        if(!MainServidor.HABILITAR_MULTI_CUENTA) {
                            if(Mundo.IpEstaUsada(ip)) {
                                GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_imprimir);
                                try {
                                    _s.close();
                                } catch (IOException ignored) {}
                                return;
                            }
                        }
                        _cuenta.setActualIP(ip);
                        _cuenta.setRealmThread(this);
                        RealmServer.totalabonado++;//On incrémente le total
                        _cuenta._position = RealmServer.totalabonado;//On lui donne une position
                        GestorSalida.REALM_SEND_Ad_Ac_AH_AlK_AQ_PACKETS(_imprimir, _cuenta.getApodo(),(_cuenta.getGMLVL()>0?(1):(0)), _cuenta.get_question() );
                    }else{ //Si le compte n'a pas été reconnu
                        GestorSalida.REALM_SEND_LOGIN_ERROR(_imprimir);
                        try {
                            this._s.close();
                        } catch (IOException ignored) {}
                    }
                }
                break;
            default:
                if(packet.startsWith("Af")) {
                    _numerodepaquete--;
                    Pending.PendingSystem(_cuenta);
                }else
                if(packet.startsWith("Ax")) {
                    if(_cuenta == null)return;
                    GestorSQL.cargar_personaje_por_cuenta(_cuenta.getID());
                    GestorSalida.REALM_SEND_PERSO_LIST(_imprimir, _cuenta.getNumeroDePersonajes());
                }else
                if(packet.equals("AX1")) {
                    MainServidor.gameServer.addWaitingCompte(_cuenta);
                    String ip = _cuenta.getActualIP();
                    GestorSalida.REALM_SEND_GAME_SERVER_IP(_imprimir, _cuenta.getID(),ip.equals("127.0.0.1"));
                }
                break;
        }
    }
}
