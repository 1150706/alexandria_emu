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
	private Thread _t;
	public PrintWriter _out;
	private Socket _s;
	private String _hashKey;
	private int _packetNum = 0;
	private String _accountName;
    private Cuenta _compte;
	
	public RealmThread(Socket sock) {
		try {
			_s = sock;
			_in = new BufferedReader(new InputStreamReader(_s.getInputStream()));
			_out = new PrintWriter(_s.getOutputStream());
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		} catch(IOException e) {
			try {
				if(!_s.isClosed())_s.close();
			} catch (IOException ignored) {}
		} finally {
			if(_compte != null) {
				_compte.setRealmThread(null);
				_compte.setGameThread(null);
				_compte.setCurIP("");
			}
		}
	}

	public void run() {
		try {
			StringBuilder packet = new StringBuilder();
			char[] charCur = new char[1];
			if(MainServidor.ENVIAR_POLITICA_DE_PRIVACIDAD)
				GestorSalida.REALM_SEND_POLICY_FILE(_out);
	        
			_hashKey = GestorSalida.REALM_SEND_HC_PACKET(_out);
	        
	    	while(_in.read(charCur, 0, 1)!=-1 && MainServidor.isRunning) {
	    		if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
		    	{
	    			packet.append(charCur[0]);
		    	}else if(packet.length() > 0) {

					if(MainServidor.MOSTRAR_RECIBIDOS){
						RealmServer.addToSockLog("Realm: Recibido << "+packet);
					}
		    		_packetNum++;
		    		parsePacket(packet.toString());
		    		packet = new StringBuilder();
		    	}
	    	}
    	}catch(IOException e) {
    		try {
	    		_in.close();
	    		_out.close();
	    		if(_compte != null) {
	    			_compte.setCurPerso(null);
	    			_compte.setGameThread(null);
	    			_compte.setRealmThread(null);
	    			_compte.setCurIP("");
	    		}
	    		if(!_s.isClosed())_s.close();
	    		_t.interrupt();
	    	}catch(IOException ignored){}
		} finally {
    		try {
	    		_in.close();
	    		_out.close();
	    		if(_compte != null) {
	    			_compte.setCurPerso(null);
	    			_compte.setGameThread(null);
	    			_compte.setRealmThread(null);
	    			_compte.setCurIP("");
	    		}
	    		if(!_s.isClosed())_s.close();
	    		_t.interrupt();
	    	}catch(IOException ignored){}
		}
	}
	
	private void parsePacket(String packet) {
		switch(_packetNum) {
			case 1://Version
				if(!packet.equalsIgnoreCase(Constantes.CLIENT_VERSION) && !Constantes.IGNORE_VERSION) {
					GestorSalida.REALM_SEND_REQUIRED_VERSION(_out);
					try {
						this._s.close();
					} catch (IOException ignored) {}
				}
				break;
			case 2://Account Name
				_accountName = packet.toLowerCase();
				break;
			case 3://HashPass
				if(!packet.substring(0, 2).equalsIgnoreCase("#1")) {
					try {
						this._s.close();
					} catch (IOException ignored) {}
				}

                if(Cuenta.COMPTE_LOGIN(_accountName, packet,_hashKey))
				{
					_compte = Mundo.getCompteByName(_accountName);
					if(_compte.isOnline() && _compte.getGameThread() != null)
					{
						_compte.getGameThread().closeSocket();
					}else if(_compte.isOnline() && _compte.getGameThread() == null)
					{
						GestorSalida.REALM_SEND_ALREADY_CONNECTED(_out);
						GestorSalida.REALM_SEND_ALREADY_CONNECTED(_compte.getRealmThread()._out);
						return;
					}
					if(_compte.isBanned())
					{
						GestorSalida.REALM_SEND_BANNED(_out);
						try {
							_s.close();
						} catch (IOException ignored) {}
						return;
					}
					if(MainServidor.MAXIMO_DE_CONECTADOS != -1 && MainServidor.MAXIMO_DE_CONECTADOS <= MainServidor.gameServer.getPlayerNumber())
					{
						//Seulement si joueur
						if(_compte.get_gmLvl() == 0  && _compte.get_vip() == 0)
						{
							GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_out);
							try {
								_s.close();
							} catch (IOException ignored) {}
							return;
						}
					}
					if(Mundo.getGmAccess() > _compte.get_gmLvl())
					{
						GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_out);
						return;
					}
					String ip = _s.getInetAddress().getHostAddress();
					if(Constantes.IPcompareToBanIP(ip))
					{
						GestorSalida.REALM_SEND_BANNED(_out);
						return;
					}
					//Verification Multi compte
					if(!MainServidor.HABILITAR_MULTI_CUENTA)
					{
						if(Mundo.ipIsUsed(ip))
						{
							GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_out);
							try {
								_s.close();
							} catch (IOException ignored) {}
							return;
						}
					}
					_compte.setRealmThread(this);
					_compte.setCurIP(ip);
					RealmServer.totalabonado++;//On incrémente le total
					_compte._position = RealmServer.totalabonado;//On lui donne une position
					GestorSalida.REALM_SEND_Ad_Ac_AH_AlK_AQ_PACKETS(_out, _compte.get_pseudo(),(_compte.get_gmLvl()>0?(1):(0)), _compte.get_question() );
				}else//Si le compte n'a pas été reconnu
				{
					GestorSQL.Cargar_cuenta_por_usuario(_accountName);
					if(Cuenta.COMPTE_LOGIN(_accountName, packet,_hashKey)) {
						_compte = Mundo.getCompteByName(_accountName);
						if(_compte.isOnline() && _compte.getGameThread() != null) {
							_compte.getGameThread().closeSocket();
						}else if(_compte.isOnline() && _compte.getGameThread() == null) {
							GestorSalida.REALM_SEND_ALREADY_CONNECTED(_out);
							GestorSalida.REALM_SEND_ALREADY_CONNECTED(_compte.getRealmThread()._out);
							return;
						}
						if(_compte.isBanned()) {
							GestorSalida.REALM_SEND_BANNED(_out);
							try {
								this._s.close();
							} catch (IOException ignored) {}
							return;
						}
						if(MainServidor.MAXIMO_DE_CONECTADOS != -1 && MainServidor.MAXIMO_DE_CONECTADOS <= MainServidor.gameServer.getPlayerNumber()) {
							//Seulement si joueur
							if(_compte.get_gmLvl() == 0  && _compte.get_vip() == 0) {
								GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_out);
								try {
									_s.close();
								} catch (IOException ignored) {}
								return;
							}
						}
						if(Mundo.getGmAccess() > _compte.get_gmLvl()) {
							GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_out);
							return;
						}
						String ip = _s.getInetAddress().getHostAddress();
						if(Constantes.IPcompareToBanIP(ip)) {
							GestorSalida.REALM_SEND_BANNED(_out);
							return;
						}
						//Verification Multi compte
						if(!MainServidor.HABILITAR_MULTI_CUENTA) {
							if(Mundo.ipIsUsed(ip)) {
								GestorSalida.REALM_SEND_TOO_MANY_PLAYER_ERROR(_out);
								try {
									_s.close();
								} catch (IOException ignored) {}
								return;
							}
						}
						_compte.setCurIP(ip);
						_compte.setRealmThread(this);
						RealmServer.totalabonado++;//On incrémente le total
						_compte._position = RealmServer.totalabonado;//On lui donne une position
						GestorSalida.REALM_SEND_Ad_Ac_AH_AlK_AQ_PACKETS(_out, _compte.get_pseudo(),(_compte.get_gmLvl()>0?(1):(0)), _compte.get_question() );
					}else{ //Si le compte n'a pas été reconnu
						GestorSalida.REALM_SEND_LOGIN_ERROR(_out);
						try {
							this._s.close();
						} catch (IOException ignored) {}
					}
				}
				break;
			default:
				if(packet.startsWith("Af")) {
					_packetNum--;
					Pending.PendingSystem(_compte);
				}else
				if(packet.substring(0,2).equals("Ax")) {
					if(_compte == null)return;
					GestorSQL.cargar_personaje_por_cuenta(_compte.get_GUID());
					GestorSalida.REALM_SEND_PERSO_LIST(_out, _compte.GET_PERSO_NUMBER());
				}else
				if(packet.equals("AX1")) {
					MainServidor.gameServer.addWaitingCompte(_compte);
					String ip = _compte.get_curIP();
					GestorSalida.REALM_SEND_GAME_SERVER_IP(_out, _compte.get_GUID(),ip.equals("127.0.0.1"));
				}
				break;
		}
	}
}
