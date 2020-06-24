package juego;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import objetos.Cuenta;
import objetos.Mapa;
import objetos.Personaje;

import comunes.*;

public class JuegoServidor implements Runnable{

	private ServerSocket _SS;
	private Thread _thread;
	private final ArrayList<JuegoThread> _clientes = new ArrayList<>();
	private final ArrayList<Cuenta> _esperando = new ArrayList<>();
	private long _tiempoinicio;
	private int _jugadoresmaximos = 0;
	private String _publicidad;
	private static int _nropublicidad = 0;
	
	public JuegoServidor(String Ip) {
		try {

			//Timer de guardado automatico
			Timer _tiempoguardado = new Timer();
			_tiempoguardado.schedule(new TimerTask() {
				public void run() {
					if(!MainServidor.isSaving) {
						Thread t = new Thread(new SaveThread());
						t.start();
					}
				}
			}, MainServidor.TIEMPO_DE_GUARDADO, MainServidor.TIEMPO_DE_GUARDADO);

			//Timer envio de publicidad automatica
			Timer _publicidadautomatica = new Timer();
			_publicidadautomatica.schedule(new TimerTask() {
				public void run() {
					//Si no tenemos publicidad agregada, no ejecutamos el script
					if (Mundo.Publicidad.isEmpty()){
						return;
					}
					//Listamos la cantidad de id desde la base de datos
					_publicidad = Mundo.Publicidad.get(_nropublicidad);
					//Aumentamos +1 las id
					_nropublicidad++;
					//Imprimimos la publicidad desde el lang
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("1241;" + _publicidad);
					if (MainServidor.MOSTRAR_ENVIADOS){
					JuegoServidor.agregar_a_los_logs("Enviando publicidad automatica");
					}
					//En caso de que la lista ya termine la retomamos
					if (_nropublicidad >= Mundo.Publicidad.size()) {
						_nropublicidad = 0;
					}
				}
			}, MainServidor.TIEMPO_ENVIO_PUBLICIDAD_AUTOMATICA, MainServidor.TIEMPO_ENVIO_PUBLICIDAD_AUTOMATICA);

			//Timer movimiento de moobs en mapa
			Timer _movimientodemonsters = new Timer();
			_movimientodemonsters.schedule(new TimerTask() {
				public void run() {
					//Tomamos la lista de mapas del juego
					ArrayList<Short> mapas = new ArrayList<>();
					//Listamos los personajes online
					for(Personaje player: Mundo.getOnlinePersos()){
						Mapa map = player.getActualMapa();
						//Si el personaje esta en un mapa donde tenemos moobs los movemos
						if(!mapas.contains(map.getID())){
							map.MovimientoDeMonstruosEnMapas();
							mapas.add(map.getID());
							JuegoServidor.agregar_a_los_logs("Moviendo los monstruos en mapas donde se encuentran personajes");
						}
					}
				}
			}, MainServidor.TIEMPO_MOVIMIENTO_MONSTRUOS, MainServidor.TIEMPO_MOVIMIENTO_MONSTRUOS);

			//Timer ejecucion de acciones en tiempo real
			Timer _loadActionTimer = new Timer();
			_loadActionTimer.schedule(new TimerTask() {
				public void run() {
					GestorSQL.cargar_acciones();
					JuegoServidor.agregar_a_los_logs("Las acciones en tiempo real se han realizado!");
				}
			}, MainServidor.ACTIVAR_ACCIONES_TIEMPO_REAL, MainServidor.ACTIVAR_ACCIONES_TIEMPO_REAL);

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
			Timer _lastPacketTimer = new Timer();
			_lastPacketTimer.schedule(new TimerTask() {
				public void run() {
					for(Personaje perso : Mundo.getOnlinePersos()) {
						if (perso.getLastPacketTime() + MainServidor.TIEMPO_DESCONECTAR_POR_AFK < System.currentTimeMillis()) {
							
							if(perso.getCuenta().getJuegoThread() != null && perso.isConectado()) {
								JuegoServidor.agregar_a_los_logs("Se ha desconectado a "+perso.getNombre()+" por inactividad");
								GestorSalida.REALM_SEND_MESSAGE(perso.getCuenta().getJuegoThread().get_out(),"01|");
								perso.getCuenta().getJuegoThread().closeSocket();
							}
						}
					}
				}
			}, 60000,60000);
			
			_SS = new ServerSocket(MainServidor.PUERTO_DE_JUEGO);
			if(MainServidor.USAR_IP)
				MainServidor.GAMESERVER_IP = GestorEncriptador.CryptIP(Ip)+ GestorEncriptador.CryptPort(MainServidor.PUERTO_DE_JUEGO);
			_tiempoinicio = System.currentTimeMillis();
			_thread = new Thread(this);
			_thread.start();
		} catch (IOException e) {
			agregar_a_los_logs("IOException: "+e.getMessage());
			MainServidor.cerrarservidor();
		}
	}
	
	public static class SaveThread implements Runnable {
		public void run() {
			if(!MainServidor.isSaving) {
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("1164");
				Mundo.saveAll(null);
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("1165");
			}
		}
	}
	
	public ArrayList<JuegoThread> getClients() {
		return _clientes;
	}

	public long getStartTime()
	{
		return _tiempoinicio;
	}
	
	public int getMaxPlayer()
	{
		return _jugadoresmaximos;
	}
	
	public int getPlayerNumber()
	{
		return _clientes.size();
	}

	public void run() {
		while(MainServidor.isRunning)//bloque sur _SS.accept()
		{
			try {
				_clientes.add(new JuegoThread(_SS.accept()));
				if(_clientes.size() > _jugadoresmaximos) _jugadoresmaximos = _clientes.size();
			}catch(IOException e) {
				agregar_a_los_logs("IOException: "+e.getMessage());
				try {
					if(!_SS.isClosed())_SS.close();
					MainServidor.cerrarservidor();
				} catch(IOException ignored){}
			}
		}
	}
	
	public void expulsar_a_todos() {
		try {
			_SS.close();
		} catch (IOException ignored) {}
		//Copie
		ArrayList<JuegoThread> c = new ArrayList<>(_clientes);
		for(JuegoThread GT : c) {
			try {
				GT.closeSocket();
			}catch(Exception ignored){}
		}
	}
	
	public synchronized static void agregar_a_los_logs(String str) {
		System.out.println(str);
		if(MainServidor.canLog) {
			try {
				String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
				MainServidor.Log_Game.write(date+": "+str);
				MainServidor.Log_Game.newLine();
				MainServidor.Log_Game.flush();
			} catch (IOException e) {e.printStackTrace();}//ne devrait pas avoir lieu
		}
	}
	
	public synchronized static void addToSockLog(String str) {
		if(MainServidor.MOSTRAR_ENVIADOS)System.out.println(str);
		if(MainServidor.canLog) {
			try {
				String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
				MainServidor.Log_GameSock.write(date+": "+str);
				MainServidor.Log_GameSock.newLine();
				MainServidor.Log_GameSock.flush();
			} catch (IOException ignored) {}//ne devrait pas avoir lieu
		}
	}
	
	public void delClient(JuegoThread gameThread) {
		_clientes.remove(gameThread);
		if(_clientes.size() > _jugadoresmaximos) _jugadoresmaximos = _clientes.size();
	}

	public synchronized Cuenta getWaitingCompte(int guid) {
		for (Cuenta waiting : _esperando) {
			if (waiting.getID() == guid)
				return waiting;
		}
		return null;
	}
	
	public synchronized void delWaitingCompte(Cuenta _compte)
	{
		_esperando.remove(_compte);
	}
	
	public synchronized void addWaitingCompte(Cuenta _compte)
	{
		_esperando.add(_compte);
	}
	
	public static String getServerTime() {
		Date actDate = new Date();
		return "BT"+(actDate.getTime()+3600000);
	}

	public static String getServerDate() {
		Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		StringBuilder jour = new StringBuilder(Integer.parseInt(dateFormat.format(actDate)) + "");
		while(jour.length() <2) {
			jour.insert(0, "0");
		}
		dateFormat = new SimpleDateFormat("MM");
		StringBuilder mois = new StringBuilder((Integer.parseInt(dateFormat.format(actDate)) - 1) + "");
		while(mois.length() <2) {
			mois.insert(0, "0");
		}
		dateFormat = new SimpleDateFormat("yyyy");
		String annee = (Integer.parseInt(dateFormat.format(actDate))-1370)+"";
		return "BD"+annee+"|"+mois+"|"+jour;
	}

	public Thread getThread()
	{
		return _thread;
	}
	public static class AllFightsTurns
    implements Runnable {
    Timer _allFightsTurns;
    long _lastFightsTurns;

    public void run() {
      try {
        _allFightsTurns = new Timer();
        _allFightsTurns.scheduleAtFixedRate(new TimerTask() {
          public void run() {
            try {
              /*if (System.currentTimeMillis() - _lastFightsTurns > 3500L) {
                SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "ERREUR TIMER-LAG Dans: _allFightsTurns; " + (System.currentTimeMillis() - _lastFightsTurns));
              }*/
              //long t = System.currentTimeMillis();
              
              try {
                Mundo.ticAllFightersTurns();
              } catch (Exception e) {
                GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "ERREUR FATAL ------ No2 ---- (rar) Dans: ticAllFightersTurns(); " + e.getMessage());
              }

              /*if (System.currentTimeMillis() - t > 5000L) {
                SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "LAG: ticAllFightersTurns(); " + (System.currentTimeMillis() - t));
              }*/
              
             // System.out.println("---- Tic! " + (System.currentTimeMillis() - _lastFightsTurns));
              //_lastFightsTurns = System.currentTimeMillis();
            } catch (Exception e) {
              System.out.println("--------------- ERROR! " + e.getMessage());
              return;
            }
          }
        }
        , 1500L, 1500L);
      } catch (Exception ignored) {
      }
    }
  }
}