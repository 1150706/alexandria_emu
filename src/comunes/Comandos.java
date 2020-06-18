package comunes;

import juego.JuegoServidor;
import juego.JuegoThread;
import juego.JuegoServidor.SaveThread;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Map.Entry;

import javax.swing.Timer;

import comunes.Mundo.ItemSet;

import objetos.Accion;
import objetos.Mapa;
import objetos.Cuenta;
import objetos.NPCModelo;
import objetos.Objeto;
import objetos.Personaje;
import objetos.Mapa.MountPark;
import objetos.Mercadillo.HdvEntry;
import objetos.Oficio.StatsMetier;
import objetos.Monstruo.MobGroup;
import objetos.NPCModelo.NPC;
import objetos.NPCModelo.NPC_question;
import objetos.NPCModelo.NPC_reponse;
import objetos.Objeto.ObjTemplate;


public class Comandos {
	final Cuenta _compte;
	final Personaje _perso;
	final PrintWriter _out;
	//Guardado
	private boolean _TimerStart = false;
	Timer _timer;
	
	private Timer createTimer(final int time)
	{
	    ActionListener action = new ActionListener ()
	      {
	    	int Time = time;
	        public void actionPerformed (ActionEvent event)
	        {
	        	Time = Time-1;
	        	if(Time == 1)
	        	{
	        		GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;"+Time+" minuto");
	        	}else
	        	{
		        	GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;"+Time+" minutos");
	        	}
	        	if(Time <= 0)
	        	{
	        		for(Personaje perso : Mundo.getOnlinePersos())
	        		{
	        			perso.get_compte().getGameThread().kick();
	        		}
	    			System.exit(0);
	        	}
	        }
	      };
	    // Génération du repeat toutes les minutes.
	    return new Timer (60000, action);//60000
	}
	
	public Comandos(Personaje perso)
	{
		this._compte = perso.get_compte();
		this._perso = perso;
		this._out = _compte.getGameThread().get_out();
	}
	
	public void consoleCommand(String packet)
	{
		
		if(_compte.get_gmLvl() < 1)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
		
		String msg = packet.substring(2);
		String[] infos = msg.split(" ");
		if(infos.length == 0)return;
		String command = infos[0];
		
		if(MainServidor.canLog)
		{
			MainServidor.addToMjLog(msg+" <="+_compte.get_curIP()+" : "+_compte.get_name()+" / "+_perso.get_name());
		}
		
		if(_compte.get_gmLvl() == 2)
		{
			commandGmOne(command, infos, msg);
		}else
		if(_compte.get_gmLvl() == 3)
		{
			commandGmTwo(command, infos, msg);
		}
		else
		if(_compte.get_gmLvl() == 4)
		{
			commandGmThree(command, infos, msg);
		}
		else
		if(_compte.get_gmLvl() >= 5)
		{
			commandGmFour(command, infos, msg);
		}
	}
	
	public void commandGmOne(String command, String[] infos, String msg)
	{
		if(_compte.get_gmLvl() < 1)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
		if(command.equalsIgnoreCase("INFOS"))
		{
			long uptime = System.currentTimeMillis() - MainServidor.gameServer.getStartTime();
			int jour = (int) (uptime/(1000*3600*24));
			uptime %= (1000*3600*24);
			int hour = (int) (uptime/(1000*3600));
			uptime %= (1000*3600);
			int min = (int) (uptime/(1000*60));
			uptime %= (1000*60);
			int sec = (int) (uptime/(1000));
			
			String mess =	"===========\n"+ MainServidor.makeHeader()
				+			"Tiempo online: "+jour+"D "+hour+"H "+min+"M "+sec+"s\n"
				+			"Jugadores online: "+ MainServidor.gameServer.getPlayerNumber()+"\n"
				+			"Maximos conectados: "+ MainServidor.gameServer.getMaxPlayer()+"\n"
				+			"===========";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("REFRESHMOBS"))
		{
			_perso.getActualMapa().refreshSpawns();
			String mess = "Mob Spawn refreshed!";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else if(command.equalsIgnoreCase("RELOAD"))
        {
            try
            {
                    GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Début du chargement :");
                    MainServidor.loadConfiguration();
                    GestorSQL.cargar_maximo_de_objetos();
                    GestorSQL.cargar_npc_modelo();
                    GestorSQL.cargar_preguntas_npc();
                    GestorSQL.cargar_respuestas_npc();
                    GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Chargement terminé");

            }
            catch(Exception ignored) { }
            return;
    }else if(command.equalsIgnoreCase("MAPINFO"))
		{
			String mess = 	"==========\n"
						+	"Liste des Npcs de la carte:";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			Mapa map = _perso.getActualMapa();
			for(Entry<Integer,NPC> entry : map.get_npcs().entrySet())
			{
				mess = entry.getKey()+" "+entry.getValue().get_template().get_id()+" "+entry.getValue().get_cellID()+" "+entry.getValue().get_template().get_initQuestionID();
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			}
			mess = "Liste des groupes de monstres:";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			for(Entry<Integer,MobGroup> entry : map.getMobGroups().entrySet())
			{
				mess = entry.getKey()+" "+entry.getValue().getCellID()+" "+entry.getValue().getAlignement()+" "+entry.getValue().getSize();
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			}
			mess = "==========";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("WHO")) {
			String mess = 	"==========\n"
				+			"Liste des joueurs en ligne:";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			int diff = MainServidor.gameServer.getClients().size() -  30;
			for(byte b = 0; b < 30; b++) {
				if(b == MainServidor.gameServer.getClients().size())break;
				JuegoThread GT = MainServidor.gameServer.getClients().get(b);
				Personaje P = GT.getPerso();
				if(P == null)continue;
				mess = P.get_name()+"("+P.get_GUID()+") ";

				switch (P.get_classe()) {
					case Constantes.CLASS_FECA -> mess += "Fec";
					case Constantes.CLASS_OSAMODAS -> mess += "Osa";
					case Constantes.CLASS_ENUTROF -> mess += "Enu";
					case Constantes.CLASS_SRAM -> mess += "Sra";
					case Constantes.CLASS_XELOR -> mess += "Xel";
					case Constantes.CLASS_ECAFLIP -> mess += "Eca";
					case Constantes.CLASS_ENIRIPSA -> mess += "Eni";
					case Constantes.CLASS_IOP -> mess += "Iop";
					case Constantes.CLASS_CRA -> mess += "Cra";
					case Constantes.CLASS_SADIDA -> mess += "Sad";
					case Constantes.CLASS_SACRIEUR -> mess += "Sac";
					case Constantes.CLASS_PANDAWA -> mess += "Pan";
					default -> mess += "Unk";
				}
				mess += " ";
				mess += (P.get_sexe()==0?"M":"F")+" ";
				mess += P.get_lvl()+" ";
				mess += P.getActualMapa().get_id()+"("+P.getActualMapa().getX()+"/"+P.getActualMapa().getY()+") ";
				mess += P.get_fight()==null?"":"Combat ";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			}
			if(diff >0)
			{
				mess = 	"Et "+diff+" autres personnages";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			}
			mess = 	"==========\n";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("SHOWFIGHTPOS"))
		{
			String mess = "Liste des StartCell [teamID][cellID]:";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			String places = _perso.getActualMapa().get_placesStr();
			if(places.indexOf('|') == -1 || places.length() <2)
			{
				mess = "Les places n'ont pas ete definies";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
				return;
			}
			String team0 = "",team1 = "";
			String[] p = places.split("\\|");
			try
			{
				team0 = p[0];
			}catch(Exception ignored){}
			try
			{
				team1 = p[1];
			}catch(Exception ignored){}
			mess = "Team 0:\n";
			for(int a = 0;a <= team0.length()-2; a+=2)
			{
				String code = team0.substring(a,a+2);
				mess += GestorEncriptador.cellCode_To_ID(code);
			}
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			mess = "Team 1:\n";
			for(int a = 0;a <= team1.length()-2; a+=2)
			{
				String code = team1.substring(a,a+2);
				mess += GestorEncriptador.cellCode_To_ID(code)+" , ";
			}
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("CREATEGUILD"))
		{
			Personaje perso = _perso;
			if(infos.length >1)
			{
				perso = Mundo.getPersoByName(infos[1]);
			}
			if(perso == null)
			{
				String mess = "Le personnage n'existe pas.";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			
			if(!perso.isOnline())
			{
				String mess = "Le personnage "+perso.get_name()+" n'etait pas connecte";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			if(perso.get_guild() != null || perso.getGuildMember() != null)
			{
				String mess = "Le personnage "+perso.get_name()+" a deja une guilde";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			GestorSalida.GAME_SEND_gn_PACKET(perso);
			String mess = perso.get_name()+": Panneau de creation de guilde ouvert";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			return;
		}else
		if(command.equalsIgnoreCase("TOOGLEAGGRO"))
		{
			Personaje perso = _perso;
			
			String name = null;
			try
			{
				name = infos[1];
			}catch(Exception ignored){}

			perso = Mundo.getPersoByName(name);
			
			if(perso == null)
			{
				String mess = "Le personnage n'existe pas.";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			
			perso.set_canAggro(!perso.canAggro());
			String mess = perso.get_name();
			if(perso.canAggro()) mess += " peut maintenant etre aggresser";
			else mess += " ne peut plus etre agresser";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			
			if(!perso.isOnline())
			{
				mess = "(Le personnage "+perso.get_name()+" n'etait pas connecte)";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			}
		}else
		if(command.equalsIgnoreCase("ANNOUNCE"))
		{
			infos = msg.split(" ",2);
			GestorSalida.GAME_SEND_MESSAGE_TO_ALL(infos[1], MainServidor.CONFIG_MOTD_COLOR);
			return;
		}else
		if(command.equalsIgnoreCase("DEMORPH"))
		{
			Personaje target = _perso;
			if(infos.length > 1)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[1]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			int morphID = target.get_classe()*10 + target.get_sexe();
			target.set_gfxID(morphID);
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getActualMapa(), target.get_GUID());
			GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(target.getActualMapa(), target);
			String str = "Le joueur a ete transforme";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}
		else
		if(command.equalsIgnoreCase("GONAME") || command.equalsIgnoreCase("JOIN"))
		{
			Personaje P = Mundo.getPersoByName(infos[1]);
			if(P == null)
			{
				String str = "Le personnage n'existe pas";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			short mapID = P.getActualMapa().get_id();
			int cellID = P.getActualCelda().getID();
			
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				if(target.get_fight() != null)
				{
					String str = "La cible est en combat";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.teletransportar(mapID, cellID);
			String str = "Le joueur a ete teleporte";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("NAMEGO"))
		{
			Personaje target = Mundo.getPersoByName(infos[1]);
			if(target == null)
			{
				String str = "Le personnage n'existe pas";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			if(target.get_fight() != null)
			{
				String str = "La cible est en combat";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje P = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				P = Mundo.getPersoByName(infos[2]);
				if(P == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			if(P.isOnline())
			{
				short mapID = P.getActualMapa().get_id();
				int cellID = P.getActualCelda().getID();
				target.teletransportar(mapID, cellID);
				String str = "Le joueur a ete teleporte";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}else
			{
				String str = "Le joueur n'est pas en ligne";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			}
		}else
		if(command.equalsIgnoreCase("NAMEANNOUNCE"))
		{
			infos = msg.split(" ",2);
			String prefix = "["+_perso.get_name()+"]";
			GestorSalida.GAME_SEND_MESSAGE_TO_ALL(prefix+infos[1], MainServidor.CONFIG_MOTD_COLOR);
			return;
		}else
		if(command.equalsIgnoreCase("TELEPORT"))
		{
			short mapID = -1;
			int cellID = -1;
			try
			{
				mapID = Short.parseShort(infos[1]);
				cellID = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}
			if(mapID == -1 || cellID == -1 || Mundo.getCarte(mapID) == null)
			{
				String str = "MapID ou cellID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			if(Mundo.getCarte(mapID).getCase(cellID) == null)
			{
				String str = "MapID ou cellID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 3)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[3]);
				if(target == null  || target.get_fight() != null)
				{
					String str = "Le personnage n'a pas ete trouve ou est en combat";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.teletransportar(mapID, cellID);
			String str = "Le joueur a ete teleporte";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("GOMAP"))
		{
			int mapX = 0;
			int mapY = 0;
			int cellID = 311;
			int contID = 0;//Par défaut Amakna
			try
			{
				mapX = Integer.parseInt(infos[1]);
				mapY = Integer.parseInt(infos[2]);
				cellID = Integer.parseInt(infos[3]);
				contID = Integer.parseInt(infos[4]);
			}catch(Exception ignored){}
			Mapa map = Mundo.getCarteByPosAndCont(mapX,mapY,contID);
			if(map == null)
			{
				String str = "Position ou continent invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			if(map.getCase(cellID) == null)
			{
				String str = "CellID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 5)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[5]);
				if(target == null || target.get_fight() != null)
				{
					String str = "Le personnage n'a pas ete trouve ou est en combat";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
				if(target.get_fight() != null)
				{
					String str = "La cible est en combat";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.teletransportar(map.get_id(), cellID);
			String str = "Le joueur a ete teleporte";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("DOACTION"))
		{
			//DOACTION NAME TYPE ARGS COND
			if(infos.length < 4)
			{
				String mess = "Nombre d'argument de la commande incorect !";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			int type = -100;
			String args = "",cond = "";
			Personaje perso = _perso;
			try
			{
				perso = Mundo.getPersoByName(infos[1]);
				if(perso == null)perso = _perso;
				type = Integer.parseInt(infos[2]);
				args = infos[3];
				if(infos.length >4)
				cond = infos[4];
			}catch(Exception e)
			{
				String mess = "Arguments de la commande incorect !";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			(new Accion(type,args,cond)).apply(perso, null, -1, -1);
			String mess = "Action effectuee !";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
		}else
		{
			String mess = "Commande non reconnue";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
		}
	}
	
	public void commandGmTwo(String command, String[] infos, String msg)
	{
		if(_compte.get_gmLvl() < 2)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
		
		if(command.equalsIgnoreCase("MUTE"))
		{
			Personaje perso = _perso;
			String name = null;
			try
			{
				name = infos[1];
			}catch(Exception ignored){}
			int time = 0;
			try
			{
				time = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}

			perso = Mundo.getPersoByName(name);
			if(perso == null || time < 0)
			{
				String mess = "Le personnage n'existe pas ou la duree est invalide.";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			String mess = "Vous avez mute "+perso.get_name()+" pour "+time+" secondes";
			if(perso.get_compte() == null)
			{
				mess = "(Le personnage "+perso.get_name()+" n'etait pas connecte)";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			perso.get_compte().mute(true,time);
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			
			if(!perso.isOnline())
			{
				mess = "(Le personnage "+perso.get_name()+" n'etait pas connecte)";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			}else
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1124;"+time);
			}
			return;
		}else
		if(command.equalsIgnoreCase("UNMUTE"))
		{
			Personaje perso = _perso;
			
			String name = null;
			try
			{
				name = infos[1];
			}catch(Exception ignored){}

			perso = Mundo.getPersoByName(name);
			if(perso == null)
			{
				String mess = "Le personnage n'existe pas.";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			
			perso.get_compte().mute(false,0);
			String mess = "Vous avez unmute "+perso.get_name();
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			
			if(!perso.isOnline())
			{
				mess = "(Le personnage "+perso.get_name()+" n'etait pas connecte)";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			}
		}else
		if(command.equalsIgnoreCase("KICK"))
		{
			Personaje perso = _perso;
			String name = null;
			try
			{
				name = infos[1];
			}catch(Exception ignored){}
			perso = Mundo.getPersoByName(name);
			if(perso == null)
			{
				String mess = "Le personnage n'existe pas.";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			if(perso.isOnline())
			{
				perso.get_compte().getGameThread().kick();
				String mess = "Vous avez kick "+perso.get_name();
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			}
			else
			{
				String mess = "Le personnage "+perso.get_name()+" n'est pas connecte";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			}
		}else
		if(command.equalsIgnoreCase("SPELLPOINT"))
		{
			int pts = -1;
			try
			{
				pts = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(pts == -1)
			{
				String str = "Valeur invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.addSpellPoint(pts);
			GestorSalida.GAME_SEND_STATS_PACKET(target);
			String str = "Le nombre de point de sort a ete modifiee";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("LEARNSPELL"))
		{
			int spell = -1;
			try
			{
				spell = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(spell == -1)
			{
				String str = "Valeur invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			
			target.learnSpell(spell, 1, true,true);
			
			String str = "Le sort a ete appris";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("SETALIGN"))
		{
			byte align = -1;
			try
			{
				align = Byte.parseByte(infos[1]);
			}catch(Exception ignored){}
			if(align < Constantes.ALIGNEMENT_NEUTRE || align > Constantes.ALIGNEMENT_MERCENAIRE)
			{
				String str = "Valeur invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			
			target.modifAlignement(align);
			
			String str = "L'alignement du joueur a ete modifie";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("SETREPONSES"))
		{
			if(infos.length <3)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Il manque un/des arguments");
				return;
			}
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			String reps = infos[2];
			NPC_question Q = Mundo.getNPCQuestion(id);
			String str = "";
			if(id == 0 || Q == null)
			{
				str = "QuestionID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Q.setReponses(reps);
			boolean a= GestorSQL.actualizar_pregunta_npc(id,reps);
			str = "Liste des reponses pour la question "+id+": "+Q.getReponses();
			if(a)str += "(sauvegarde dans la BDD)";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			return;
		}else
		if(command.equalsIgnoreCase("SHOWREPONSES"))
		{
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			NPC_question Q = Mundo.getNPCQuestion(id);
			String str = "";
			if(id == 0 || Q == null)
			{
				str = "QuestionID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			str = "Liste des reponses pour la question "+id+": "+Q.getReponses();
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			return;
		}else
		if(command.equalsIgnoreCase("HONOR"))
		{
			int honor = 0;
			try
			{
				honor = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			String str = "Vous avez ajouter "+honor+" honneur a "+target.get_name();
			if(target.get_align() == Constantes.ALIGNEMENT_NEUTRE)
			{
				str = "Le joueur est neutre ...";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			target.addHonor(honor);
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			
		}else
		if(command.equalsIgnoreCase("ADDJOBXP"))
		{
			int job = -1;
			int xp = -1;
			try
			{
				job = Integer.parseInt(infos[1]);
				xp = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}
			if(job == -1 || xp < 0)
			{
				String str = "Valeurs invalides";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
				Personaje target = _perso;
			if(infos.length > 3)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[3]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			StatsMetier SM = target.getMetierByID(job);
			if(SM== null)
			{
				String str = "Le joueur ne connais pas le metier demande";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
				
			SM.addXp(target, xp);
			
			String str = "Le metier a ete experimenter";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("LEARNJOB"))
		{
			int job = -1;
			try
			{
				job = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(job == -1 || Mundo.getMetier(job) == null)
			{
				String str = "Valeur invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			
			target.learnJob(Mundo.getMetier(job));
			
			String str = "Le metier a ete appris";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("CAPITAL"))
		{
			int pts = -1;
			try
			{
				pts = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(pts == -1)
			{
				String str = "Valeur invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.addCapital(pts);
			GestorSalida.GAME_SEND_STATS_PACKET(target);
			String str = "Le capital a ete modifiee";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}
		if(command.equalsIgnoreCase("SIZE"))
		{
			int size = -1;
			try
			{
				size = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(size == -1)
			{
				String str = "Taille invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.set_size(size);
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getActualMapa(), target.get_GUID());
			GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(target.getActualMapa(), target);
			String str = "La taille du joueur a ete modifiee";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("MORPH"))
		{
			int morphID = -1;
			try
			{
				morphID = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(morphID == -1)
			{
				String str = "MorphID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = Mundo.getPersoByName(infos[2]);
				if(target == null)
				{
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.set_gfxID(morphID);
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getActualMapa(), target.get_GUID());
			GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(target.getActualMapa(), target);
			String str = "Le joueur a ete transforme";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}if(command.equalsIgnoreCase("MOVENPC"))
		{
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			NPC npc = _perso.getActualMapa().getNPC(id);
			if(id == 0 || npc == null)
			{
				String str = "Npc GUID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			int exC = npc.get_cellID();
			//on l'efface de la map
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_perso.getActualMapa(), id);
			//on change sa position/orientation
			npc.setCellID(_perso.getActualCelda().getID());
			npc.setOrientation((byte)_perso.get_orientation());
			//on envoie la modif
			GestorSalida.GAME_SEND_ADD_NPC_TO_MAP(_perso.getActualMapa(),npc);
			String str = "Le PNJ a ete deplace";
			if(_perso.get_orientation() == 0
			|| _perso.get_orientation() == 2
			|| _perso.get_orientation() == 4
			|| _perso.get_orientation() == 6)
				str += " mais est devenu invisible (orientation diagonale invalide).";
			if(GestorSQL.eliminar_npc_en_mapa(_perso.getActualMapa().get_id(),exC)
			&& GestorSQL.agregar_npc_en_mapa(_perso.getActualMapa().get_id(),npc.get_template().get_id(),_perso.getActualCelda().getID(),_perso.get_orientation()))
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			else
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Erreur au moment de sauvegarder la position");
		}else	
		if(command.equalsIgnoreCase("ITEMSET"))
		{
			int tID = 0;
			String nom = null;
			try
			{
				if(infos.length > 3)
					nom = infos[3];
				else if(infos.length > 1)
					tID = Integer.parseInt(infos[1]);
				
			}catch(Exception ignored){}
			ItemSet IS = Mundo.getItemSet(tID);
			if(tID == 0 || IS == null)
			{
				String mess = "La panoplie "+tID+" n'existe pas ";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			boolean useMax = false;
			if(infos.length > 2)
				useMax = infos[2].equals("MAX");//Si un jet est spécifié

			Personaje perso = _perso;
			if(nom != null)
				try {
					perso = Mundo.getPersoByName(nom);
				} catch(Exception ignored) {}
			for(ObjTemplate t : IS.getItemTemplates())
			{
				Objeto obj = t.createNewItem(1,useMax);
				if(perso != null) {
					if(perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
						Mundo.addObjet(obj,true);
				} else if(_perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
					Mundo.addObjet(obj,true);
			}
			String str = "Creation de la panoplie "+tID+" reussie";
			if(useMax) str += " avec des stats maximums";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("DAR_NIVEL"))
		{
			int count = 0;
			try
			{
				count = Integer.parseInt(infos[1]);
				if(count < 1)	count = 1;
				if(count > Mundo.getExpLevelSize())	count = Mundo.getExpLevelSize();
				Personaje perso = _perso;
				if(infos.length == 3)//Si le nom du perso est spécifié
				{
					String name = infos[2];
					perso = Mundo.getPersoByName(name);
					if(perso == null)
						perso = _perso;
				}
				if(perso.get_lvl() < count)
				{
					while(perso.get_lvl() < count)
					{
						perso.levelUp(false,true);
					}
					if(perso.isOnline())
					{
						GestorSalida.GAME_SEND_SPELL_LIST(perso);
						GestorSalida.GAME_SEND_NEW_LVL_PACKET(perso.get_compte().getGameThread().get_out(),perso.get_lvl());
						GestorSalida.GAME_SEND_STATS_PACKET(perso);
					}
				}
				String mess = "Cambiaste el nivel actual de "+perso.get_name()+" a "+count;
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			}catch(Exception e)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Valor incorrecto.");
				return;
			}
		}else
		if(command.equalsIgnoreCase("PDVPER"))
		{
			int count = 0;
			try
			{
				count = Integer.parseInt(infos[1]);
				if(count < 0)	count = 0;
				if(count > 100)	count = 100;
				Personaje perso = _perso;
				if(infos.length == 3)//Si le nom du perso est spécifié
				{
					String name = infos[2];
					perso = Mundo.getPersoByName(name);
					if(perso == null)
						perso = _perso;
				}
				int newPDV = perso.get_PDVMAX() * count / 100;
				perso.set_PDV(newPDV);
				if(perso.isOnline())
					GestorSalida.GAME_SEND_STATS_PACKET(perso);
				String mess = "Vous avez fixer le pourcentage de pdv de "+perso.get_name()+" a "+count;
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			}catch(Exception e)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Valeur incorecte");
				return;
			}
		}else
		if(command.equalsIgnoreCase("KAMAS"))
		{
			int count = 0;
			try
			{
				count = Integer.parseInt(infos[1]);
			}catch(Exception e)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Valeur incorecte");
				return;
			}
			if(count == 0)return;
			
			Personaje perso = _perso;
			if(infos.length == 3)//Si le nom du perso est spécifié
			{
				String name = infos[2];
				perso = Mundo.getPersoByName(name);
				if(perso == null)
					perso = _perso;
			}
			long curKamas = perso.get_kamas();
			long newKamas = curKamas + count;
			if(newKamas <0) newKamas = 0;
			if(newKamas > 1000000000) newKamas = 1000000000;
			perso.set_kamas(newKamas);
			if(perso.isOnline())
				GestorSalida.GAME_SEND_STATS_PACKET(perso);
			String mess = "Vous avez ";
			mess += (count<0?"retirer":"ajouter")+" ";
			mess += Math.abs(count)+" kamas a "+perso.get_name();
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
		}else
		if(command.equalsIgnoreCase("ITEM") || command.equalsIgnoreCase("!getitem"))
		{
			boolean isOffiCmd = command.equalsIgnoreCase("!getitem");
			if(_compte.get_gmLvl() < 2)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int tID = 0;
			try
			{
				tID = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(tID == 0)
			{
				String mess = "Le template "+tID+" n'existe pas ";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			int qua = 1;
			if(infos.length == 3)//Si une quantité est spécifiée
			{
				try
				{
					qua = Integer.parseInt(infos[2]);
				}catch(Exception ignored){}
			}
			boolean useMax = false;
			if(infos.length == 4 && !isOffiCmd)//Si un jet est spécifié
			{
				if(infos[3].equalsIgnoreCase("MAX"))useMax = true;
			}
			ObjTemplate t = Mundo.getObjTemplate(tID);
			if(t == null)
			{
				String mess = "Le template "+tID+" n'existe pas ";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
				return;
			}
			if(qua <1)qua =1;
			Objeto obj = t.createNewItem(qua,useMax);
			if(_perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
				Mundo.addObjet(obj,true);
			String str = "Creation de l'item "+tID+" reussie";
			if(useMax) str += " avec des stats maximums";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			GestorSalida.GAME_SEND_Ow_PACKET(_perso);
		}else 
		if (command.equalsIgnoreCase("SPAWN"))
		{			
			String Mob = null;
			try
			{
				Mob = infos[1];
			}catch(Exception ignored){}
			if(Mob == null) return;
			_perso.getActualMapa().spawnGroupOnCommand(_perso.getActualCelda().getID(), Mob);
		}else
		if (command.equalsIgnoreCase("TITLE"))
		{
			Personaje target = null;
			byte TitleID = 0;
			try
			{
				target = Mundo.getPersoByName(infos[1]);
				TitleID = Byte.parseByte(infos[2]);
			}catch(Exception ignored){}

			if(target == null)
			{
				String str = "Le personnage n'a pas ete trouve";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			target.set_title(TitleID);
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Titre mis en place.");
			GestorSQL.guardar_personaje(target, false);
			if(target.get_fight() == null) GestorSalida.GAME_SEND_ALTER_GM_PACKET(target.getActualMapa(), target);
		}else
		{
			this.commandGmOne(command, infos, msg);
		}
	}
	
	public void commandGmThree(String command, String[] infos, String msg)
	{
		if(_compte.get_gmLvl() < 3)
		{
			_compte.getGameThread().closeSocket();
			return;
		}
		
		if(command.equalsIgnoreCase("EXIT"))
		{
			System.exit(0);
		} else
		if (command.equalsIgnoreCase("DEBUGTURNS"))
		{
	        Mundo.ticAllFightersTurns();
	        GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "isAlive= " + MainServidor._passerTours.isAlive() + ", SDATA= " + MainServidor._passerTours.toString());
		} else 
			if (command.equalsIgnoreCase("KICKALL"))
			{
				MainServidor.gameServer.kickAll();
			}else
		if (command.equalsIgnoreCase("ENDTURNS"))
		{
			MainServidor._passerTours = new Thread(new JuegoServidor.AllFightsTurns());
			MainServidor._passerTours.start();
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Ok.");
		} else    
		if(command.equalsIgnoreCase("SAVE") && !MainServidor.isSaving)
		{
			Thread t = new Thread(new SaveThread());
			t.start();
			String mess = "Sauvegarde lancee!";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("GETCOORD"))
		{
			int cell = _perso.getActualCelda().getID();
			String mess = "["+ Camino.getCellXCoord(_perso.getActualMapa(), cell)+","+ Camino.getCellYCoord(_perso.getActualMapa(), cell)+"]";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("DELFIGHTPOS"))
		{
			int cell = -1;
			try
			{
				cell = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}
			if(cell < 0 || _perso.getActualMapa().getCase(cell) == null)
			{
				cell = _perso.getActualCelda().getID();
			}
			String places = _perso.getActualMapa().get_placesStr();
			String[] p = places.split("\\|");
			String newPlaces = "";
			String team0 = "",team1 = "";
			try
			{
				team0 = p[0];
			}catch(Exception ignored){}
			try
			{
				team1 = p[1];
			}catch(Exception ignored){}

			for(int a = 0;a<=team0.length()-2;a+=2)
			{
				String c = p[0].substring(a,a+2);
				if(cell == GestorEncriptador.cellCode_To_ID(c))continue;
				newPlaces += c;
			}
			newPlaces += "|";
			for(int a = 0;a<=team1.length()-2;a+=2)
			{
				String c = p[1].substring(a,a+2);
				if(cell == GestorEncriptador.cellCode_To_ID(c))continue;
				newPlaces += c;
			}
			_perso.getActualMapa().setPlaces(newPlaces);
			if(!GestorSQL.guardar_mapa(_perso.getActualMapa()))return;
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Les places ont ete modifiees ("+newPlaces+")");
			return;
		}else
		if(command.equalsIgnoreCase("BAN"))
		{
			Personaje P = Mundo.getPersoByName(infos[1]);
			if(P == null)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
				return;
			}
			if(P.get_compte() == null) GestorSQL.cargar_cuenta_por_id(P.getAccID());
			if(P.get_compte() == null)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur");
				return;
			}
			P.get_compte().setBanned(true);
			GestorSQL.actualizar_datos_cuenta(P.get_compte());
			if(P.get_compte().getGameThread() != null)P.get_compte().getGameThread().kick();
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous avez banni "+P.get_name());
			return;
		}else
		if(command.equalsIgnoreCase("UNBAN"))
		{
			Personaje P = Mundo.getPersoByName(infos[1]);
			if(P == null)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Personnage non trouve");
				return;
			}
			if(P.get_compte() == null) GestorSQL.cargar_cuenta_por_id(P.getAccID());
			if(P.get_compte() == null)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Erreur");
				return;
			}
			P.get_compte().setBanned(false);
			GestorSQL.actualizar_datos_cuenta(P.get_compte());
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Vous avez debanni "+P.get_name());
			return;
		}else
		if(command.equalsIgnoreCase("ADDFIGHTPOS"))
		{
			int team = -1;
			int cell = -1;
			try
			{
				team = Integer.parseInt(infos[1]);
				cell = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}
			if( team < 0 || team>1)
			{
				String str = "Team ou cellID incorects";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			if(cell <0 || _perso.getActualMapa().getCase(cell) == null || !_perso.getActualMapa().getCase(cell).isWalkable(true))
			{
				cell = _perso.getActualCelda().getID();
			}
			String places = _perso.getActualMapa().get_placesStr();
			String[] p = places.split("\\|");
			boolean already = false;
			String team0 = "",team1 = "";
			try
			{
				team0 = p[0];
			}catch(Exception ignored){}
			try
			{
				team1 = p[1];
			}catch(Exception ignored){}

			//Si case déjà utilisée
			System.out.println("0 => "+team0+"\n1 =>"+team1+"\nCell: "+ GestorEncriptador.cellID_To_Code(cell));
			for(int a = 0; a <= team0.length()-2;a+=2)if(cell == GestorEncriptador.cellCode_To_ID(team0.substring(a,a+2)))already = true;
			for(int a = 0; a <= team1.length()-2;a+=2)if(cell == GestorEncriptador.cellCode_To_ID(team1.substring(a,a+2)))already = true;
			if(already)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"La case est deja dans la liste");
				return;
			}
			if(team == 0)team0 += GestorEncriptador.cellID_To_Code(cell);
			else if(team == 1)team1 += GestorEncriptador.cellID_To_Code(cell);
			
			String newPlaces = team0+"|"+team1;
			
			_perso.getActualMapa().setPlaces(newPlaces);
			if(!GestorSQL.guardar_mapa(_perso.getActualMapa()))return;
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Les places ont ete modifiees ("+newPlaces+")");
			return;
		}else
		if(command.equalsIgnoreCase("SETMAXGROUP"))
		{
			infos = msg.split(" ",4);
			byte id = -1;
			try
			{
				id = Byte.parseByte(infos[1]);
			}catch(Exception ignored){}
			if(id == -1)
			{
				String str = "Valeur invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			String mess = "Le nombre de groupe a ete fixe";
			_perso.getActualMapa().setMaxGroup(id);
			boolean ok = GestorSQL.guardar_mapa(_perso.getActualMapa());
			if(ok)mess += " et a ete sauvegarder a la BDD";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
		}else
		if(command.equalsIgnoreCase("ADDREPONSEACTION"))
		{
			infos = msg.split(" ",4);
			int id = -30;
			int repID = 0;
			String args = infos[3];
			try
			{
				repID = Integer.parseInt(infos[1]);
				id = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}
			NPC_reponse rep = Mundo.getNPCreponse(repID);
			if(id == -30 || rep == null)
			{
				String str = "Au moins une des valeur est invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			
			rep.addAction(new Accion(id,args,""));
			boolean ok = GestorSQL.agregar_respuesta_npc(repID,id,args);
			if(ok)mess += " et ajoute a la BDD";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
		}else
		if(command.equalsIgnoreCase("SETINITQUESTION"))
		{
			infos = msg.split(" ",4);
			int id = -30;
			int q = 0;
			try
			{
				q = Integer.parseInt(infos[2]);
				id = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(id == -30)
			{
				String str = "NpcID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			NPCModelo npc = Mundo.getNPCTemplate(id);
			
			npc.setInitQuestion(q);
			boolean ok = GestorSQL.actualizar_respuesta_de_npc(id,q);
			if(ok)mess += " et ajoute a la BDD";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
		}else
		if(command.equalsIgnoreCase("ADDENDFIGHTACTION"))
		{
			infos = msg.split(" ",4);
			int id = -30;
			int type = 0;
			String args = infos[3];
			String cond = infos[4];
			try
			{
				type = Integer.parseInt(infos[1]);
				id = Integer.parseInt(infos[2]);
				
			}catch(Exception ignored){}
			if(id == -30)
			{
				String str = "Au moins une des valeur est invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			_perso.getActualMapa().addEndFightAction(type, new Accion(id,args,cond));
			boolean ok = GestorSQL.agregar_fin_pelea_accion(_perso.getActualMapa().get_id(),type,id,args,cond);
			if(ok)mess += " et ajoute a la BDD";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,mess);
			return;
		}else
		if(command.equalsIgnoreCase("SPAWNFIX"))
		{
			String groupData = infos[1];

			_perso.getActualMapa().addStaticGroup(_perso.getActualCelda().getID(), groupData);
			String str = "Le grouppe a ete fixe";
			//Sauvegarde DB de la modif
			if(GestorSQL.guardar_nuevo_grupo_monstruos(_perso.getActualMapa().get_id(),_perso.getActualCelda().getID(), groupData))
				str += " et a ete sauvegarde dans la BDD";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			return;
		}else
		if(command.equalsIgnoreCase("ADDNPC"))
		{
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(id == 0 || Mundo.getNPCTemplate(id) == null)
			{
				String str = "NpcID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			NPC npc = _perso.getActualMapa().addNpc(id, _perso.getActualCelda().getID(), _perso.get_orientation());
			GestorSalida.GAME_SEND_ADD_NPC_TO_MAP(_perso.getActualMapa(), npc);
			String str = "Le PNJ a ete ajoute";
			if(_perso.get_orientation() == 0
					|| _perso.get_orientation() == 2
					|| _perso.get_orientation() == 4
					|| _perso.get_orientation() == 6)
						str += " mais est invisible (orientation diagonale invalide).";
			
			if(GestorSQL.agregar_npc_en_mapa(_perso.getActualMapa().get_id(), id, _perso.getActualCelda().getID(), _perso.get_orientation()))
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			else
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Erreur au moment de sauvegarder la position");
		}else
		if(command.equalsIgnoreCase("DELNPC"))
		{
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			NPC npc = _perso.getActualMapa().getNPC(id);
			if(id == 0 || npc == null)
			{
				String str = "Npc GUID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			int exC = npc.get_cellID();
			//on l'efface de la map
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_perso.getActualMapa(), id);
			_perso.getActualMapa().removeNpcOrMobGroup(id);
			
			String str = "Le PNJ a ete supprime";
			if(GestorSQL.eliminar_npc_en_mapa(_perso.getActualMapa().get_id(),exC))
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
			else
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Erreur au moment de sauvegarder la position");
		}else
		if(command.equalsIgnoreCase("DELTRIGGER"))
		{
			int cellID = -1;
			try
			{
				cellID = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(cellID == -1 || _perso.getActualMapa().getCase(cellID) == null)
			{
				String str = "CellID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			_perso.getActualMapa().getCase(cellID).clearOnCellAction();
			boolean success = GestorSQL.eliminar_celdas(_perso.getActualMapa().get_id(),cellID);
			String str = "";
			if(success)	str = "Le trigger a ete retire";
			else 		str = "Le trigger n'a pas ete retire";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("ADDTRIGGER"))
		{
			int actionID = -1;
			String args = "",cond = "";
			try
			{
				actionID = Integer.parseInt(infos[1]);
				args = infos[2];
				cond = infos[3];
			}catch(Exception ignored){}
			if(args.equals("") || actionID <= -3)
			{
				String str = "Valeur invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			_perso.getActualCelda().addOnCellStopAction(actionID,args, cond);
			boolean success = GestorSQL.guardar_celdas(_perso.getActualMapa().get_id(),_perso.getActualCelda().getID(),actionID,1,args,cond);
			String str = "";
			if(success)	str = "Le trigger a ete ajoute";
			else 		str = "Le trigger n'a pas ete ajoute";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("DELNPCITEM"))
		{
			if(_compte.get_gmLvl() <3)return;
			int npcGUID = 0;
			int itmID = -1;
			try
			{
				npcGUID = Integer.parseInt(infos[1]);
				itmID = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}
			NPCModelo npc =  _perso.getActualMapa().getNPC(npcGUID).get_template();
			if(npcGUID == 0 || itmID == -1 || npc == null)
			{
				String str = "NpcGUID ou itmID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			
			String str = "";
			if(npc.delItemVendor(itmID))str = "L'objet a ete retire";
			else str = "L'objet n'a pas ete retire";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("ADDNPCITEM"))
		{
			if(_compte.get_gmLvl() <3)return;
			int npcGUID = 0;
			int itmID = -1;
			try
			{
				npcGUID = Integer.parseInt(infos[1]);
				itmID = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}
			NPCModelo npc =  _perso.getActualMapa().getNPC(npcGUID).get_template();
			ObjTemplate item =  Mundo.getObjTemplate(itmID);
			if(npcGUID == 0 || itmID == -1 || npc == null || item == null)
			{
				String str = "NpcGUID ou itmID invalide";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			
			String str = "";
			if(npc.addItemVendor(item))str = "L'objet a ete rajoute";
			else str = "L'objet n'a pas ete rajoute";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("ADDMOUNTPARK"))
		{
			int size = -1;
			int owner = -2;
			int price = -1;
			try
			{
				size = Integer.parseInt(infos[1]);
				owner = Integer.parseInt(infos[2]);
				price = Integer.parseInt(infos[3]);
				if(price > 20000000)price = 20000000;
				if(price <0)price = 0;
			}catch(Exception ignored){}
			if(size == -1 || owner == -2 || price == -1 || _perso.getActualMapa().getMountPark() != null)
			{
				String str = "Infos invalides ou map deja config.";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			MountPark MP = new MountPark(owner, _perso.getActualMapa(), _perso.getActualCelda().getID(), size, "", -1, price);
			_perso.getActualMapa().setMountPark(MP);
			GestorSQL.guardar_cercados(MP);
			String str = "L'enclos a ete config. avec succes";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else 
		if (command.equalsIgnoreCase("SHUTDOWN"))
		{
			int time = 30, OffOn = 0;
			try
			{
				OffOn = Integer.parseInt(infos[1]);
				time = Integer.parseInt(infos[2]);
			}catch(Exception ignored){}

			if(OffOn == 1 && _TimerStart)// demande de démarer le reboot
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Un shutdown est deja programmer.");
			}else if(OffOn == 1 && !_TimerStart)
			{
				_timer = createTimer(time);
				_timer.start();
				_TimerStart = true;
				String timeMSG = "minutes";
				if(time <= 1)
				{
					timeMSG = "minute";
				}
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;"+time+" "+timeMSG);
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Shutdown lance.");
			}else if(OffOn == 0 && _TimerStart)
			{
				_timer.stop();
				_TimerStart = false;
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Shutdown arrete.");
			}else if(OffOn == 0 && !_TimerStart)
			{
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Aucun shutdown n'est lance.");
			}
		}else
		{
			this.commandGmTwo(command, infos, msg);
		}
	}
	
	public void commandGmFour(String command, String[] infos, String msg) {
		if(_compte.get_gmLvl() < 4) {
			_compte.getGameThread().closeSocket();
			return;
		}
		
		if(command.equalsIgnoreCase("SETADMIN")) {
			int gmLvl = -100;
			try {
				gmLvl = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			if(gmLvl == -100) {
				String str = "Valeur incorrecte";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			Personaje target = _perso;
			if(infos.length > 2){ //Si un nom de perso est spécifié
				target = Mundo.getPersoByName(infos[2]);
				if(target == null) {
					String str = "Le personnage n'a pas ete trouve";
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
					return;
				}
			}
			target.get_compte().setGmLvl(gmLvl);
			GestorSQL.actualizar_datos_cuenta(target.get_compte());
			String str = "Le niveau GM du joueur a ete modifie";
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
		}else
		if(command.equalsIgnoreCase("LOCK")) {
			byte LockValue = 1;//Accessible
			try {
				LockValue = Byte.parseByte(infos[1]);
			}catch(Exception ignored){}

			if(LockValue > 2) LockValue = 2;
			if(LockValue < 0) LockValue = 0;
			Mundo.set_state(LockValue);
			if(LockValue == 1) {
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Serveur accessible.");
			}else if(LockValue == 0) {
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Serveur inaccessible.");
			}else if(LockValue == 2) {
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Serveur en sauvegarde.");
			}
		}else
		if(command.equalsIgnoreCase("BLOCK")) {
			byte GmAccess = 0;
			byte KickPlayer = 0;
			try
			{
				GmAccess = Byte.parseByte(infos[1]);
				KickPlayer = Byte.parseByte(infos[2]);
			}catch(Exception ignored){}

			Mundo.setGmAccess(GmAccess);
			GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Serveur bloque au GmLevel : "+GmAccess);
			if(KickPlayer > 0) {
				for(Personaje z : Mundo.getOnlinePersos()) {
					if(z.get_compte().get_gmLvl() < GmAccess)
						z.get_compte().getGameThread().closeSocket();
				}
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Les joueurs de GmLevel inferieur a "+GmAccess+" ont ete kicks.");
			}
		}else
		if(command.equalsIgnoreCase("BANIP")) {
			Personaje P = null;
			try {
				P = Mundo.getPersoByName(infos[1]);
			}catch(Exception ignored){}
			if(P == null || !P.isOnline()) {
				String str = "Le personnage n'a pas ete trouve.";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
			if(!Constantes.IPcompareToBanIP(P.get_compte().get_curIP())) {
				Constantes.BAN_IP += ","+P.get_compte().get_curIP();
				if(GestorSQL.agregar_ip_baneada(P.get_compte().get_curIP())) {
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "L'IP a ete banni.");
				}
				if(P.isOnline()){
					P.get_compte().getGameThread().kick();
					GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Le joueur a ete kick.");
				}
			}else {
				String str = "L'IP existe deja.";
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,str);
				return;
			}
			
		}else
		if(command.equalsIgnoreCase("FULLHDV")) {
			int numb = 1;
			try {
				numb = Integer.parseInt(infos[1]);
			}catch(Exception ignored){}
			fullHdv(numb);
		}else {
			this.commandGmThree(command, infos, msg);
		}
	}
	
	private void fullHdv(int ofEachTemplate) {
		GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Démarrage du remplissage!");
		
		Objeto objet = null;
		HdvEntry entry = null;
		byte amount = 0;
		int hdv = 0;
		
		int lastSend = 0;
		long time1 = System.currentTimeMillis();//TIME
		for (ObjTemplate curTemp : Mundo.getObjTemplates()){ //Boucler dans les template
			try {
				if(MainServidor.NOTINHDV.contains(curTemp.getID())) continue;
				for (int j = 0; j < ofEachTemplate; j++) { //Ajouter plusieur fois le template
					if(curTemp.getType() == 85) break;
					
					objet = curTemp.createNewItem(1, false);
					hdv = getHdv(objet.getTemplate().getType());
					
					if(hdv < 0) break;
						
					amount = (byte) Formulas.getRandomValue(1, 3);
					
					
					entry = new HdvEntry(calculPrice(objet,amount), amount, -1, objet);
					objet.setQuantity(entry.getAmount(true));
					
					
					Mundo.getHdv(hdv).addEntry(entry);
					Mundo.addObjet(objet, false);
				}
			}catch (Exception e) {
				continue;
			}
			
			if((System.currentTimeMillis() - time1)/1000 != lastSend
				&& (System.currentTimeMillis() - time1)/1000 % 3 == 0) {
				lastSend = (int) ((System.currentTimeMillis() - time1)/1000);
				GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,(System.currentTimeMillis() - time1)/1000 + "sec Template: "+curTemp.getID());
			}
		}
		GestorSalida.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out,"Remplissage fini en "+(System.currentTimeMillis() - time1) + "ms");
		Mundo.saveAll(null);
		GestorSalida.GAME_SEND_MESSAGE_TO_ALL("HDV remplis!", MainServidor.CONFIG_MOTD_COLOR);
	}
	private int getHdv(int type) {
		int rand = Formulas.getRandomValue(1, 4);
		int map = -1;
		
		switch(type) {
			case 12:
			case 14: 
			case 26: 
			case 43: 
			case 44: 
			case 45: 
			case 66: 
			case 70: 
			case 71: 
			case 86:
				if(rand == 1)
				{
					map = 4271;
				}else
				if(rand == 2)
				{
					map = 4607;
				}else
				{
					map = 7516;
				}
				return map;
			case 1:
			case 9:
				if(rand == 1)
				{
					map = 4216;
				}else
				if(rand == 2)
				{
					map = 4622;
				}else
				{
					map = 7514;
				}
				return map;
			case 18: 
			case 72: 
			case 77: 
			case 90: 
			case 97: 
			case 113: 
			case 116:
				if(rand == 1)
				{
					map = 8759;
				}else
				{
					map = 8753;
				}
				return map;
			case 63:
			case 64:
			case 69:
				if(rand == 1)
				{
					map = 4287;
				}else
				if(rand == 2)
				{
					map = 4595;
				}else
				if(rand == 3)
				{
					map = 7515;
				}else
				{
					map = 7350;
				}
				return map;
			case 33:
			case 42:
				if(rand == 1)
				{
					map = 2221;
				}else
				if(rand == 2)
				{
					map = 4630;
				}else
				{
					map = 7510;
				}
				return map;
			case 84: 
			case 93: 
			case 112: 
			case 114:
				if(rand == 1)
				{
					map = 4232;
				}else
				if(rand == 2)
				{
					map = 4627;
				}else
				{
					map = 12262;
				}
				return map;
			case 38: 
			case 95: 
			case 96: 
			case 98: 
			case 108:
				if(rand == 1)
				{
					map = 4178;
				}else
				if(rand == 2)
				{
					map = 5112;
				}else
				{
					map = 7289;
				}
				return map;
			case 10:
			case 11:
				if(rand == 1)
				{
					map = 4183;
				}else
				if(rand == 2)
				{
					map = 4562;
				}else
				{
					map = 7602;
				}
				return map;
			case 13: 
			case 25: 
			case 73: 
			case 75: 
			case 76:
				if(rand == 1)
				{
					map = 8760;
				}else
				{
					map = 8754;
				}
				return map;
			case 5: 
			case 6: 
			case 7: 
			case 8: 
			case 19: 
			case 20: 
			case 21: 
			case 22:
				if(rand == 1)
				{
					map = 4098;
				}else
				if(rand == 2)
				{
					map = 5317;
				}else
				{
					map = 7511;
				}
				return map;
			case 39: 
			case 40: 
			case 50: 
			case 51: 
			case 88:
				if(rand == 1)
				{
					map = 4179;
				}else
				if(rand == 2)
				{
					map = 5311;
				}else
				{
					map = 7443;
				}
				return map;
			case 87:
				if(rand == 1)
				{
					map = 6159;
				}else
				{
					map = 6167;
				}
				return map;
			case 34:
			case 52:
			case 60:
				if(rand == 1)
				{
					map = 4299;
				}else
				if(rand == 2)
				{
					map = 4629;
				}else
				{
					map = 7397;
				}
				return map;
			case 41:
			case 49:
			case 62:
				if(rand == 1)
				{
					map = 4247;
				}else
				if(rand == 2)
				{
					map = 4615;
				}else
				if(rand == 3)
				{
					map = 7501;
				}else
				{
					map = 7348;
				}
				return map;
			case 15: 
			case 35: 
			case 36: 
			case 46: 
			case 47: 
			case 48: 
			case 53: 
			case 54: 
			case 55: 
			case 56: 
			case 57: 
			case 58: 
			case 59: 
			case 65: 
			case 68: 
			case 103: 
			case 104: 
			case 105: 
			case 106: 
			case 107: 
			case 109: 
			case 110: 
			case 111:
				if(rand == 1)
				{
					map = 4262;
				}else
				if(rand == 2)
				{
					map = 4646;
				}else
				{
					map = 7413;
				}
				return map;
			case 78:
				if(rand == 1)
				{
					map = 8757;
				}else
				{
					map = 8756;
				}
				return map;
			case 2:
			case 3:
			case 4:
				if(rand == 1)
				{
					map = 4174;
				}else
				if(rand == 2)
				{
					map = 4618;
				}else
				{
					map = 7512;
				}
				return map;
			case 16:
			case 17:
			case 81:
				if(rand == 1)
				{
					map = 4172;
				}else
				if(rand == 2)
				{
					map = 4588;
				}else
				{
					map = 7513;
				}
				return map;
			case 83:
				if(rand == 1)
				{
					map = 10129;
				}else
				{
					map = 8482;
				}
				return map;
			case 82:
				return 8039;
			default:
				return -1;
		}
	}
	private int calculPrice(Objeto obj, int logAmount)
	{
		int amount = (byte)(Math.pow(10, logAmount) / 10);
		int stats = 0;
		
		for(int curStat : obj.getStats().getMap().values())
		{
			stats += curStat;
		}
		if(stats > 0)
			return (int) (((Math.cbrt(stats) * Math.pow(obj.getTemplate().getLevel(), 2)) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100)) * amount);
		else
			return (int) ((Math.pow(obj.getTemplate().getLevel(),2) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100))*amount);
	}
}