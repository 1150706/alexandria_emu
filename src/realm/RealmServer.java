package realm;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Calendar;

import comunes.MainServidor;

public class RealmServer implements Runnable{

	private ServerSocket _SS;
	private Thread _t;
	
	public static int _totalNonAbo = 0;//Total de connections non abo
	public static int _totalAbo = 0;//Total de connections abo
	public static int _queueID = -1;//Numéro de la queue
	public static int _subscribe = 1;//File des non abonnées (0) ou abonnées (1)
	
	public RealmServer()
	{
		try {
			_SS = new ServerSocket(MainServidor.CONFIG_REALM_PORT);
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		} catch (IOException e) {
			addToLog("IOException: "+e.getMessage());
			MainServidor.closeServers();
		}
		
	}

	public void run()
	{	
		while(MainServidor.isRunning)//bloque sur _SS.accept()
		{
			try
			{
				new RealmThread(_SS.accept());
			}catch(IOException e)
			{
				addToLog("IOException: "+e.getMessage());
				try
				{
					addToLog("Fermeture du serveur de connexion");	
					if(!_SS.isClosed())_SS.close();
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
	}
	public synchronized static void addToLog(String str)
	{
		System.out.println(str);
		if(MainServidor.canLog)
		{
			try {
				String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
				MainServidor.Log_Realm.write(date+": "+str);
				MainServidor.Log_Realm.newLine();
				MainServidor.Log_Realm.flush();
			} catch (IOException e) {}//ne devrait pas avoir lieu
		}
	}
	
	public synchronized static void addToSockLog(String str)
	{
		if(MainServidor.CONFIG_DEBUG)System.out.println(str);
		if(MainServidor.canLog)
		{
			try {
				String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
				MainServidor.Log_RealmSock.write(date+": "+str);
				MainServidor.Log_RealmSock.newLine();
				MainServidor.Log_RealmSock.flush();
			} catch (IOException e) {}//ne devrait pas avoir lieu
		}
	}

	public Thread getThread()
	{
		return _t;
	}
}
