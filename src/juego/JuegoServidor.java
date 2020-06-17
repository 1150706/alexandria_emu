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
import objetos.Jugador;

import comunes.*;

public class JuegoServidor implements Runnable{

	private ServerSocket _SS;
	private Thread _t;
	private ArrayList<JuegoThread> _clients = new ArrayList<JuegoThread>();
	private ArrayList<Cuenta> _waitings = new ArrayList<Cuenta>();
	private Timer _saveTimer;
	private Timer _loadActionTimer;
	private Timer _reloadMobTimer;
	private Timer _reloadPubTimer;
	private Timer _lastPacketTimer;
	private long _startTime;
	private int _maxPlayer = 0;
	
	public JuegoServidor(String Ip)
	{
		try {
			_saveTimer = new Timer();
			_saveTimer.schedule(new TimerTask()
			{
				public void run()
				{
					if(!MainServidor.isSaving)
					{
						Thread t = new Thread(new SaveThread());
						t.start();
					}
				}
			}, MainServidor.CONFIG_SAVE_TIME, MainServidor.CONFIG_SAVE_TIME);
			
			_loadActionTimer = new Timer();
			_loadActionTimer.schedule(new TimerTask()
			{
				public void run()
				{
					GestorSQL.cargar_acciones();
					JuegoServidor.addToLog("Les live actions ont ete appliquees");
				}
			}, MainServidor.CONFIG_LOAD_DELAY, MainServidor.CONFIG_LOAD_DELAY);
			
			_reloadMobTimer = new Timer();
			_reloadMobTimer.schedule(new TimerTask()
			{
				public void run()
				{
					Mundo.RefreshAllMob();
					JuegoServidor.addToLog("La recharge des mobs est finie");
				}
			}, MainServidor.CONFIG_RELOAD_MOB_DELAY, MainServidor.CONFIG_RELOAD_MOB_DELAY);
			
			_lastPacketTimer = new Timer();
			_lastPacketTimer.schedule(new TimerTask()
			{
				public void run()
				{
					for(Jugador perso : Mundo.getOnlinePersos())
					{ 
						if (perso.getLastPacketTime() + MainServidor.CONFIG_MAX_IDLE_TIME < System.currentTimeMillis())
						{
							
							if(perso != null && perso.get_compte().getGameThread() != null && perso.isOnline())
							{
								JuegoServidor.addToLog("Kick pour inactiviter de : "+perso.get_name());
								GestorSalida.REALM_SEND_MESSAGE(perso.get_compte().getGameThread().get_out(),"01|");
								perso.get_compte().getGameThread().closeSocket();
							}
						}
						
					}
				}
			}, 60000,60000);
			
			_SS = new ServerSocket(MainServidor.CONFIG_GAME_PORT);
			if(MainServidor.CONFIG_USE_IP)
				MainServidor.GAMESERVER_IP = GestorEncriptador.CryptIP(Ip)+ GestorEncriptador.CryptPort(MainServidor.CONFIG_GAME_PORT);
			_startTime = System.currentTimeMillis();
			_t = new Thread(this);
			_t.start();
		} catch (IOException e) {
			addToLog("IOException: "+e.getMessage());
			MainServidor.closeServers();
		}
	}
	
	public static class SaveThread implements Runnable
	{
		public void run()
		{
			if(!MainServidor.isSaving)
			{
				GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("1164");
				Mundo.saveAll(null);
				GestorSalida.GAME_SEND_Im_PACKET_TO_ALL("1165");
			}
		}
	}
	
	public ArrayList<JuegoThread> getClients() {
		return _clients;
	}

	public long getStartTime()
	{
		return _startTime;
	}
	
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	public int getPlayerNumber()
	{
		return _clients.size();
	}
	public void run()
	{	
		while(MainServidor.isRunning)//bloque sur _SS.accept()
		{
			try
			{
				_clients.add(new JuegoThread(_SS.accept()));
				if(_clients.size() > _maxPlayer)_maxPlayer = _clients.size();
			}catch(IOException e)
			{
				addToLog("IOException: "+e.getMessage());
				try
				{
					if(!_SS.isClosed())_SS.close();
					MainServidor.closeServers();
				}
				catch(IOException e1){}
			}
		}
	}
	
	public void kickAll()
	{
		try {
			_SS.close();
		} catch (IOException e) {}
		//Copie
		ArrayList<JuegoThread> c = new ArrayList<JuegoThread>();
		c.addAll(_clients);
		for(JuegoThread GT : c)
		{
			try
			{
				GT.closeSocket();
			}catch(Exception e){};	
		}
	}
	
	public synchronized static void addToLog(String str)
	{
		System.out.println(str);
		if(MainServidor.canLog)
		{
			try {
				String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
				MainServidor.Log_Game.write(date+": "+str);
				MainServidor.Log_Game.newLine();
				MainServidor.Log_Game.flush();
			} catch (IOException e) {e.printStackTrace();}//ne devrait pas avoir lieu
		}
	}
	
	public synchronized static void addToSockLog(String str)
	{
		if(MainServidor.CONFIG_DEBUG)System.out.println(str);
		if(MainServidor.canLog)
		{
			try {
				String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+":"+Calendar.getInstance().get(+Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND);
				MainServidor.Log_GameSock.write(date+": "+str);
				MainServidor.Log_GameSock.newLine();
				MainServidor.Log_GameSock.flush();
			} catch (IOException e) {}//ne devrait pas avoir lieu
		}
	}
	
	public void delClient(JuegoThread gameThread)
	{
		_clients.remove(gameThread);
		if(_clients.size() > _maxPlayer)_maxPlayer = _clients.size();
	}

	public synchronized Cuenta getWaitingCompte(int guid)
	{
		for (int i = 0; i < _waitings.size(); i++)
		{
			if(_waitings.get(i).get_GUID() == guid)
				return _waitings.get(i);
		}
		return null;
	}
	
	public synchronized void delWaitingCompte(Cuenta _compte)
	{
		_waitings.remove(_compte);
	}
	
	public synchronized void addWaitingCompte(Cuenta _compte)
	{
		_waitings.add(_compte);
	}
	
	public static String getServerTime()
	{
		Date actDate = new Date();
		return "BT"+(actDate.getTime()+3600000);
	}
	public static String getServerDate()
	{
		Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		String jour = Integer.parseInt(dateFormat.format(actDate))+"";
		while(jour.length() <2)
		{
			jour = "0"+jour;
		}
		dateFormat = new SimpleDateFormat("MM");
		String mois = (Integer.parseInt(dateFormat.format(actDate))-1)+"";
		while(mois.length() <2)
		{
			mois = "0"+mois;
		}
		dateFormat = new SimpleDateFormat("yyyy");
		String annee = (Integer.parseInt(dateFormat.format(actDate))-1370)+"";
		return "BD"+annee+"|"+mois+"|"+jour;
	}

	public Thread getThread()
	{
		return _t;
	}
	public static class AllFightsTurns
    implements Runnable
  {
    Timer _allFightsTurns;
    long _lastFightsTurns;

    public void run()
    {
      try
      {
        _allFightsTurns = new Timer();
        _allFightsTurns.scheduleAtFixedRate(new TimerTask()
        {
          public void run()
          {
            try {
              /*if (System.currentTimeMillis() - _lastFightsTurns > 3500L) {
                SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-TIC", "ERREUR TIMER-LAG Dans: _allFightsTurns; " + (System.currentTimeMillis() - _lastFightsTurns));
              }*/
              //long t = System.currentTimeMillis();
              
              try
              {
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
      }
      catch (Exception localException)
      {
      }
    }
  }
}
