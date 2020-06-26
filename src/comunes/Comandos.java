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
    final Cuenta _cuenta;
    final Personaje _personaje;
    final PrintWriter _imprimir;
    //Guardado
    private boolean _TimerStart = false;
    Timer _timer;

    private Timer createTimer(final int time) {
        ActionListener action = new ActionListener () {
            int Time = time;
            public void actionPerformed (ActionEvent event) {
                Time = Time-1;
                if(Time == 1) {
                    GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;"+Time+" minuto");
                }else {
                    GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;"+Time+" minutos");
                }
                if(Time <= 0) {
                    for(Personaje perso : Mundo.getOnlinePersos()) {
                        perso.getCuenta().getJuegoThread().kick();
                    }
                    System.exit(0);
                }
            }
        };
        // Génération du repeat toutes les minutes.
        return new Timer (60000, action);//60000
    }

    public Comandos(Personaje personaje) {
        this._cuenta = personaje.getCuenta();
        this._personaje = personaje;
        this._imprimir = _cuenta.getJuegoThread().get_out();
    }

    public void consoleCommand(String packet) {
        if(_cuenta.getGMLVL() < 1) {
            _cuenta.getJuegoThread().closeSocket();
            return;
        }

        String mensaje = packet.substring(2);
        String[] infos = mensaje.split(" ");
        if(infos.length == 0)return;
        String comando = infos[0];

        if(MainServidor.canLog) {
            MainServidor.addToMjLog(mensaje+" <="+ _cuenta.getActualIP()+" : "+ _cuenta.getNombre()+" / "+ _personaje.getNombre());
        }

        if(_cuenta.getGMLVL() == 2) {
            ComandosGmNivelUno(comando, infos, mensaje);
        }else if(_cuenta.getGMLVL() == 3) {
            ComandosGmNivelDos(comando, infos, mensaje);
        }else if(_cuenta.getGMLVL() == 4) {
            ComandosGmNivelTres(comando, infos, mensaje);
        }else if(_cuenta.getGMLVL() >= 5) {
            ComandosGmNivelCuatro(comando, infos, mensaje);
        }
    }

    public void ComandosGmNivelUno(String comando, String[] infos, String mensaje) {
        if(_cuenta.getGMLVL() < 1) {
            _cuenta.getJuegoThread().closeSocket();
            return;
        } if(comando.equalsIgnoreCase("INFORMACION")) {
            long tiempo = System.currentTimeMillis() - MainServidor.gameServer.getStartTime();
            int dias = (int) (tiempo/(1000*3600*24));
            tiempo %= (1000*3600*24);
            int horas = (int) (tiempo/(1000*3600));
            tiempo %= (1000*3600);
            int minutos = (int) (tiempo/(1000*60));
            tiempo %= (1000*60);
            int segundos = (int) (tiempo/(1000));

            String mess =	"===========\n"+ MainServidor.cabecerapersonalizada()
                    +			"\nTiempo online: "+dias+"D "+horas+"H "+minutos+"M "+segundos+"s\n"
                    +			"Jugadores online: "+ MainServidor.gameServer.getPlayerNumber()+"\n"
                    +			"Maximos conectados: "+ MainServidor.gameServer.getMaxPlayer()+"\n"
                    +			"===========";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            return;
        }else if(comando.equalsIgnoreCase("REFRESCAR_MONSTRUOS")) {
            _personaje.getActualMapa().refreshSpawns();
            String mess = "Monstruos del mapa refrescados con exito.";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            return;
        }else if(comando.equalsIgnoreCase("ACTUALIZAR_SERVIDOR")) {
            try {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Iniciando actualizacion de datos:");
                MainServidor.CargarConfiguracion();
                GestorSQL.cargar_maximo_de_objetos();
                GestorSQL.cargar_npc_modelo();
                GestorSQL.cargar_preguntas_npc();
                GestorSQL.cargar_respuestas_npc();
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Actualizacion terminada.");
            }
            catch(Exception ignored) { }
            return;
        }else if(comando.equalsIgnoreCase("RECARGAR_CONFIGURACION")) {
            try {
                MainServidor.CargarConfiguracion();
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Configuracion recargada con exito.");
            }
            catch(Exception ignored) { }
            return;
        }else if(comando.equalsIgnoreCase("INFORMACION_DEL_MAPA")) {
            String mess = 	"=========================================================\n"
                    +	"Lista de NPC en el mapa:";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            Mapa map = _personaje.getActualMapa();
            for(Entry<Integer,NPC> entry : map.getNPCS().entrySet()) {
                mess = "ID Eliminar: "+entry.getKey()+"| ID: "+entry.getValue().getModelo().getID()+"| Celda: "+entry.getValue().getCeldaID()+"| Pregunta inicial: "+entry.getValue().getModelo().getPreguntaInicial();
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            }
            mess = "Lista de los monstruos en el mapa:";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            for(Entry<Integer,MobGroup> entry : map.getMobGroups().entrySet()) {
                mess = "ID Eliminar: "+entry.getKey()+"| Celda: "+entry.getValue().getCeldaID()+"| Alineacion: "+entry.getValue().getAlineacion()+"| Tamaño: "+entry.getValue().getTamaño();
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            }
            mess = "=========================================================";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            return;
        }else if(comando.equalsIgnoreCase("QUIEN_ONLINE")) {
            String mess = 	"=========================================================\n"
                    +			"Lista de jugadores online:";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            int diff = MainServidor.gameServer.getClientes().size() -  30;
            for(byte b = 0; b < 30; b++) {
                if(b == MainServidor.gameServer.getClientes().size())break;
                JuegoThread GT = MainServidor.gameServer.getClientes().get(b);
                Personaje P = GT.getPerso();
                if(P == null)continue;
                mess = P.getNombre()+"("+P.getID()+") ";

                switch (P.getClase()) {
                    case Constantes.CLASS_FECA -> mess += "Feca";
                    case Constantes.CLASS_OSAMODAS -> mess += "Osamodas";
                    case Constantes.CLASS_ENUTROF -> mess += "Anutrof";
                    case Constantes.CLASS_SRAM -> mess += "Sram";
                    case Constantes.CLASS_XELOR -> mess += "Xelor";
                    case Constantes.CLASS_ECAFLIP -> mess += "Zurkarak";
                    case Constantes.CLASS_ENIRIPSA -> mess += "Aniripsa";
                    case Constantes.CLASS_IOP -> mess += "Yopuka";
                    case Constantes.CLASS_CRA -> mess += "Ocra";
                    case Constantes.CLASS_SADIDA -> mess += "Sadida";
                    case Constantes.CLASS_SACRIEUR -> mess += "Sacrogrito";
                    case Constantes.CLASS_PANDAWA -> mess += "Pandawa";
                    default -> mess += "Undefined";
                }
                mess += " ";
                mess += (P.getSexo()==0?"M":"F")+" ";
                mess += P.get_lvl()+" ";
                mess += P.getActualMapa().getID()+"("+P.getActualMapa().getX()+"/"+P.getActualMapa().getY()+") ";
                mess += P.getPelea()==null?"":"Combate ";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            }
            if(diff >0) {
                mess = 	"Y "+diff+" otros personajes...";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            }
            mess = 	"=========================================================\n";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            return;
        }else if(comando.equalsIgnoreCase("VER_CELDAS_PELEA")) {
            StringBuilder mess = new StringBuilder("Lista de las celdas de pelea [ID del team][ID de la celda]:");
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess.toString());
            String places = _personaje.getActualMapa().getEsquemaPelea();
            if(places.indexOf('|') == -1 || places.length() <2) {
                mess = new StringBuilder("Las celdas de pelea en este mapa no se han definido.");
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess.toString());
                return;
            }
            String team0 = "",team1 = "";
            String[] p = places.split("\\|");
            try {
                team0 = p[0];
            }catch(Exception ignored){}
            try {
                team1 = p[1];
            }catch(Exception ignored){}
            mess = new StringBuilder("Team 0:\n");
            for(int a = 0;a <= team0.length()-2; a+=2) {
                String code = team0.substring(a,a+2);
                mess.append(GestorEncriptador.cellCode_To_ID(code));
            }
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess.toString());
            mess = new StringBuilder("Team 1:\n");
            for(int a = 0;a <= team1.length()-2; a+=2) {
                String code = team1.substring(a,a+2);
                mess.append(GestorEncriptador.cellCode_To_ID(code)).append(" , ");
            }
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess.toString());
            return;
        }else if(comando.equalsIgnoreCase("CREAR_GREMIO")) {
            Personaje perso = _personaje;
            if(infos.length >1) {
                perso = Mundo.getPersonajePorNombre(infos[1]);
            }
            if(perso == null) {
                String mess = "El personaje no existe.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }

            if(!perso.isConectado()) {
                String mess = "El personaje "+perso.getNombre()+" no esta conectado.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            if(perso.get_guild() != null || perso.getMiembroGremio() != null) {
                String mess = "El personaje "+perso.getNombre()+" ya tiene un gremio.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            GestorSalida.GAME_SEND_gn_PACKET(perso);
            String mess = perso.getNombre()+": Abrio panel de creacion de gremio";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            return;
        }else if(comando.equalsIgnoreCase("CAMBIAR_AGRESION")) {
            Personaje personaje = _personaje;
            String nombre = null;
            try {
                nombre = infos[1];
            }catch(Exception ignored){}
            personaje = Mundo.getPersonajePorNombre(nombre);
            if(personaje == null) {
                String mess = "El personaje no existe.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }

            personaje.setPuedeSerAgredido(!personaje.PuedeSerAgredido());
            String mess = personaje.getNombre();
            if(personaje.PuedeSerAgredido()) mess += " puede ser agredido.";
            else mess += " ya no puede ser agredido.";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);

            if(!personaje.isConectado()) {
                mess = "(El personaje "+personaje.getNombre()+" no esta conectado)";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            }
        }else if(comando.equalsIgnoreCase("ANUNCIO")) {
            infos = mensaje.split(" ",2);
            GestorSalida.ENVIAR_MENSAJE_A_TODOS(infos[1], MainServidor.CONFIG_MOTD_COLOR);
            return;
        }else if(comando.equalsIgnoreCase("DESTRANSFORMAR")) {
            Personaje target = _personaje;
            if(infos.length > 1) { //Si el nombre del personaje no esta espesificado
                target = Mundo.getPersonajePorNombre(infos[1]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            int morphID = target.getClase() *10 + target.getSexo();
            target.setGFX(morphID);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getActualMapa(), target.getID());
            GestorSalida.ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(target.getActualMapa(), target);
            String str = "El personaje ha sido destransformado.";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        } else if(comando.equalsIgnoreCase("IR_NOMBRE")) {
            Personaje P = Mundo.getPersonajePorNombre(infos[1]);
            if(P == null) {
                String str = "El personaje no existe.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            short mapID = P.getActualMapa().getID();
            int cellID = P.getActualCelda().getID();

            Personaje target = _personaje;
            if(infos.length > 2) { //Si el nombre del personaje no esta espesificado
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe.";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
                if(target.getPelea() != null) {
                    String str = "El objetivo esta en combate";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.teletransportar(mapID, cellID);
            String str = "Te has teletransportado al jugador objetivo";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else if(comando.equalsIgnoreCase("TRAER_HACIA_MI")) {
            Personaje target = Mundo.getPersonajePorNombre(infos[1]);
            if(target == null) {
                String str = "El personaje no existe";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            if(target.getPelea() != null) {
                String str = "El personaje esta en pelea";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje P = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                P = Mundo.getPersonajePorNombre(infos[2]);
                if(P == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            if(P.isConectado()) {
                short mapID = P.getActualMapa().getID();
                int cellID = P.getActualCelda().getID();
                target.teletransportar(mapID, cellID);
                String str = "El personaje fue traido hacia ti";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            }else {
                String str = "El personaje no esta en linea";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            }
        }else if(comando.equalsIgnoreCase("NOMBRE_ANUNCIO")) {
            infos = mensaje.split(" ",2);
            String prefix = _personaje.getNombre() + ": ";
            GestorSalida.ENVIAR_MENSAJE_A_TODOS(prefix+infos[1], MainServidor.CONFIG_MOTD_COLOR);
            return;
        }else if(comando.equalsIgnoreCase("TELETRANSPORTAR")) {
            short mapID = -1;
            int cellID = -1;
            try {
                mapID = Short.parseShort(infos[1]);
                cellID = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}
            if(mapID == -1 || cellID == -1 || Mundo.getCarte(mapID) == null) {
                String str = "Mapa o celda invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            if(Mundo.getCarte(mapID).getMapa(cellID) == null) {
                String str = "Mapa o celda invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 3)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[3]);
                if(target == null  || target.getPelea() != null) {
                    String str = "El personaje esta en combate";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.teletransportar(mapID, cellID);
            String str = "El personaje se ha teletransportado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("IR_MAPA")) {
            int mapX = 0;
            int mapY = 0;
            int cellID = 311;
            int contID = 0;//Par défaut Amakna
            try {
                mapX = Integer.parseInt(infos[1]);
                mapY = Integer.parseInt(infos[2]);
                cellID = Integer.parseInt(infos[3]);
                contID = Integer.parseInt(infos[4]);
            }catch(Exception ignored){}
            Mapa map = Mundo.getCarteByPosAndCont(mapX,mapY,contID);
            if(map == null) {
                String str = "Posicion del continente invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            if(map.getMapa(cellID) == null) {
                String str = "Celda invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 5)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[5]);
                if(target == null || target.getPelea() != null) {
                    String str = "El personaje esta en combate";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
                if(target.getPelea() != null) {
                    String str = "El personaje esta en combate";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.teletransportar(map.getID(), cellID);
            String str = "El personaje se ha teletransportado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("ACCION")) {
            //ACCION nombre tipo argumento condicion
            if(infos.length < 4) {
                String mess = "Argumento del comando incorrecto";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            int type = -100;
            String args = "",cond = "";
            Personaje perso = _personaje;
            try {
                perso = Mundo.getPersonajePorNombre(infos[1]);
                if(perso == null)perso = _personaje;
                type = Integer.parseInt(infos[2]);
                args = infos[3];
                if(infos.length >4)
                    cond = infos[4];
            }catch(Exception e) {
                String mess = "Argumento del comando incorrecto";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            (new Accion(type,args,cond)).apply(perso, null, -1, -1);
            String mess = "Accion efectuada";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
        }else {
            String mess = "Comando invalido";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
        }
    }

    public void ComandosGmNivelDos(String comando, String[] infos, String mensaje) {
        if(_cuenta.getGMLVL() < 2) {
            _cuenta.getJuegoThread().closeSocket();
            return;
        }

        if(comando.equalsIgnoreCase("SILENCIAR")) {
            Personaje perso = _personaje;
            String name = null;
            try {
                name = infos[1];
            }catch(Exception ignored){}
            int time = 0;
            try {
                time = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}

            perso = Mundo.getPersonajePorNombre(name);
            if(perso == null || time < 0) {
                String mess = "El personaje no existe o la duracion es invalida.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            String mess = "Usted silencio a "+perso.getNombre()+" por "+time+" segundos";
            if(perso.getCuenta() == null) {
                mess = "El personaje "+perso.getNombre()+" no esta conectado";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            perso.getCuenta().mute(true,time);
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);

            if(!perso.isConectado()) {
                mess = "El personaje "+perso.getNombre()+" no esta conectado";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            }else {
                GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1124;"+time);
            }
            return;
        }else
        if(comando.equalsIgnoreCase("DEJAR_DE_SILENCIAR")) {
            Personaje perso = _personaje;
            String name = null;
            try {
                name = infos[1];
            }catch(Exception ignored){}

            perso = Mundo.getPersonajePorNombre(name);
            if(perso == null) {
                String mess = "El personaje no existe";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }

            perso.getCuenta().mute(false,0);
            String mess = "Usted ha dejado que "+perso.getNombre()+" hable nuevamente";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);

            if(!perso.isConectado()) {
                mess = "El personaje "+perso.getNombre()+" no esta conectado";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            }
        }else
        if(comando.equalsIgnoreCase("EXPULSAR")) {
            Personaje perso = _personaje;
            String name = null;
            try {
                name = infos[1];
            }catch(Exception ignored){}
            perso = Mundo.getPersonajePorNombre(name);
            if(perso == null) {
                String mess = "El personaje no existe.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            if(perso.isConectado()) {
                perso.getCuenta().getJuegoThread().kick();
                String mess = "Usted ha expulsado a "+perso.getNombre();
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            } else {
                String mess = "El personaje "+perso.getNombre()+" no esta conectado";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            }
        }else
        if(comando.equalsIgnoreCase("DAR_PUNTOS_DE_HECHIZO")) {
            int pts = -1;
            try {
                pts = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(pts == -1) {
                String str = "Valor invalido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.addAgregarPuntosDeHechizo(pts);
            GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(target);
            String str = "La cantidad de puntos de hechizo del personaje "+_personaje.getNombre()+" se han modificado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("APRENDER_HECHIZO")) {
            int spell = -1;
            try {
                spell = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(spell == -1) {
                String str = "Valor invalido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje objetivo = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                objetivo = Mundo.getPersonajePorNombre(infos[2]);
                if(objetivo == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }

            objetivo.AprenderHechizo(spell, 1, true,true);

            String str = "El personaje "+_personaje.getNombre()+" ha aprendido el hechizo";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("DAR_ALINEACION")) {
            byte align = -1;
            try {
                align = Byte.parseByte(infos[1]);
            }catch(Exception ignored){}
            if(align < Constantes.ALIGNEMENT_NEUTRE || align > Constantes.ALIGNEMENT_MERCENAIRE) {
                String str = "Valor invalido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }

            target.modifAlignement(align);

            String str = "El personaje "+_personaje.getNombre()+" ha cambiado de alineacion";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("AGREGAR_RESPUESTA")) {
            if(infos.length <3) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"Faltan datos");
                return;
            }
            int id = 0;
            try {
                id = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            String reps = infos[2];
            NPC_question Q = Mundo.getNPCQuestion(id);
            String str = "";
            if(id == 0 || Q == null) {
                str = "ID de la pregunta invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Q.setReponses(reps);
            boolean a= GestorSQL.actualizar_pregunta_npc(id,reps);
            str = "Lista de respuestas para la pregunta "+id+": "+Q.getReponses();
            if(a)str += "Base de datos actualizada con exito";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            return;
        }else
        if(comando.equalsIgnoreCase("VER_RESPUESTAS")) {
            int id = 0;
            try {
                id = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            NPC_question Q = Mundo.getNPCQuestion(id);
            String str = "";
            if(id == 0 || Q == null) {
                str = "ID de la pregunta invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            str = "Lista de respuestas de la pregunta "+id+": "+Q.getReponses();
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            return;
        }else
        if(comando.equalsIgnoreCase("DAR_HONOR")) {
            int honor = 0;
            try {
                honor = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            Personaje target = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            String str = "Usted ha agregado "+honor+" de honor al personaje "+target.getNombre();
            if(target.get_align() == Constantes.ALIGNEMENT_NEUTRE) {
                str = "El jugador es neutral";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            target.addHonor(honor);
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);

        }else
        if(comando.equalsIgnoreCase("DAR_EXPERIENCIA_OFICIO")) {
            int job = -1;
            int xp = -1;
            try {
                job = Integer.parseInt(infos[1]);
                xp = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}
            if(job == -1 || xp < 0) {
                String str = "Valor invalido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 3)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[3]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            StatsMetier SM = target.getOficioPorID(job);
            if(SM== null) {
                String str = "El jugador no tiene el oficio indicado";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            SM.AgregarExperiencia(target, xp);
            String str = "La experiencia se ha agregado al oficio";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("APRENDER_OFICIO")) {
            int job = -1;
            try {
                job = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(job == -1 || Mundo.getMetier(job) == null) {
                String str = "Valor invalido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.learnJob(Mundo.getMetier(job));
            String str = "El oficio ha sido aprendido con exito";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("DAR_CAPITAL")) {
            int pts = -1;
            try {
                pts = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(pts == -1) {
                String str = "Valor invalido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.addPuntosDeCapital(pts);
            GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(target);
            String str = "El capital fue modificado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }
        if(comando.equalsIgnoreCase("TAMAÑO")) {
            int size = -1;
            try {
                size = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(size == -1) {
                String str = "Medida invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.setTamaño(size);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getActualMapa(), target.getID());
            GestorSalida.ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(target.getActualMapa(), target);
            String str = "El tamaño del personaje "+_personaje.getNombre()+" se ha modificado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("TRANSFORMAR")) {
            int morphID = -1;
            try {
                morphID = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(morphID == -1) {
                String str = "ID de la transformacion invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 2)//Si un nom de perso est spécifié
            {
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.setGFX(morphID);
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getActualMapa(), target.getID());
            GestorSalida.ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(target.getActualMapa(), target);
            String str = "El personaje "+_personaje.getNombre()+" se ha transformado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }if(comando.equalsIgnoreCase("MOVER_NPC")) {
            int id = 0;
            try {
                id = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            NPC npc = _personaje.getActualMapa().getNPC(id);
            if(id == 0 || npc == null) {
                String str = "ID negativa del NPC invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            int exC = npc.getCeldaID();
            //on l'efface de la map
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_personaje.getActualMapa(), id);
            //on change sa position/orientation
            npc.setCellID(_personaje.getActualCelda().getID());
            npc.setOrientation((byte) _personaje.getOrientacion());
            //on envoie la modif
            GestorSalida.ENVIAR_AGREGAR_NPC_EN_MAPA(_personaje.getActualMapa(),npc);
            String str = "El personaje se ha desplazado";
            if(_personaje.getOrientacion() == 0
                    || _personaje.getOrientacion() == 2
                    || _personaje.getOrientacion() == 4
                    || _personaje.getOrientacion() == 6)
                str += " pero se ha vuelto invisible, la orientacion no es valida.";
            if(GestorSQL.eliminar_npc_en_mapa(_personaje.getActualMapa().getID(),exC)
                    && GestorSQL.agregar_npc_en_mapa(_personaje.getActualMapa().getID(),npc.getModelo().getID(), _personaje.getActualCelda().getID(), _personaje.getOrientacion()))
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            else
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"Error al momento de guardar la posicion");
        }else
        if(comando.equalsIgnoreCase("AGREGAR_SET")) {
            int tID = 0;
            String nom = null;
            try {
                if(infos.length > 3)
                    nom = infos[3];
                else if(infos.length > 1)
                    tID = Integer.parseInt(infos[1]);

            }catch(Exception ignored){}
            ItemSet IS = Mundo.getItemSet(tID);
            if(tID == 0 || IS == null) {
                String mess = "El set "+tID+" no existe";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            boolean useMax = false;
            if(infos.length > 2)
                useMax = infos[2].equals("MAXIMO");//Si un jet est spécifié

            Personaje perso = _personaje;
            if(nom != null)
                try {
                    perso = Mundo.getPersonajePorNombre(nom);
                } catch(Exception ignored) {}
            for(ObjTemplate t : IS.getItemTemplates()) {
                Objeto obj = t.createNewItem(1,useMax);
                if(perso != null) {
                    if(perso.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
                        Mundo.addObjet(obj,true);
                } else if(_personaje.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
                    Mundo.addObjet(obj,true);
            }
            String str = "Se ha creado el set "+tID+" con exito";
            if(useMax) str += " en sus maximas caracteristicas";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comando.equalsIgnoreCase("DAR_NIVEL")) {
            int count = 0;
            try {
                count = Integer.parseInt(infos[1]);
                if(count < 1)	count = 1;
                if(count > Mundo.getExpLevelSize())	count = Mundo.getExpLevelSize();
                Personaje perso = _personaje;
                if(infos.length == 3)//Si le nom du perso est spécifié
                {
                    String name = infos[2];
                    perso = Mundo.getPersonajePorNombre(name);
                    if(perso == null)
                        perso = _personaje;
                }
                if(perso.get_lvl() < count) {
                    while(perso.get_lvl() < count) {
                        perso.levelUp(false,true);
                    }
                    if(perso.isConectado()) {
                        GestorSalida.GAME_SEND_SPELL_LIST(perso);
                        GestorSalida.GAME_SEND_NEW_LVL_PACKET(perso.getCuenta().getJuegoThread().get_out(),perso.get_lvl());
                        GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
                    }
                }
                String mess = "Cambiaste el nivel actual de "+perso.getNombre()+" a "+count;
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            }catch(Exception e) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Valor incorrecto.");
                return;
            }
        }else
        if(comando.equalsIgnoreCase("CURAR")) {
            int count = 0;
            try {
                count = Integer.parseInt(infos[1]);
                if(count < 0)	count = 0;
                if(count > 100)	count = 100;
                Personaje perso = _personaje;
                if(infos.length == 3)//Si le nom du perso est spécifié
                {
                    String name = infos[2];
                    perso = Mundo.getPersonajePorNombre(name);
                    if(perso == null)
                        perso = _personaje;
                }
                int newPDV = perso.get_PDVMAX() * count / 100;
                perso.set_PDV(newPDV);
                if(perso.isConectado())
                    GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
                String mess = "Usted ha curado a "+perso.getNombre()+" en la cantidad de puntos de vida "+count;
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            }catch(Exception e) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Valor incorrecto");
                return;
            }
        }else
        if(comando.equalsIgnoreCase("DAR_KAMAS")) {
            int count = 0;
            try {
                count = Integer.parseInt(infos[1]);
            }catch(Exception e) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Valor incorrecto");
                return;
            }
            if(count == 0)return;

            Personaje perso = _personaje;
            if(infos.length == 3)//Si le nom du perso est spécifié
            {
                String name = infos[2];
                perso = Mundo.getPersonajePorNombre(name);
                if(perso == null)
                    perso = _personaje;
            }
            long curKamas = perso.getKamas();
            long newKamas = curKamas + count;
            if(newKamas <0) newKamas = 0;
            if(newKamas > 1000000000) newKamas = 1000000000;
            perso.setKamas(newKamas);
            if(perso.isConectado())
                GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
            String mess = "Usted ha ";
            mess += (count<0?"retirado":"agregado")+" ";
            mess += Math.abs(count)+" kamas a "+perso.getNombre();
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
        }else
        if(comando.equalsIgnoreCase("AGREGAR_ITEM") || comando.equalsIgnoreCase("!getitem")) {
            boolean isOffiCmd = comando.equalsIgnoreCase("!getitem");
            if(_cuenta.getGMLVL() < 2) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "No tienes el nivel de GM necesario");
                return;
            }
            int tID = 0;
            try {
                tID = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(tID == 0) {
                String mess = "El objeto modelo "+tID+" no existe";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            int qua = 1;
            if(infos.length == 3)//Si une quantité est spécifiée
            {
                try {
                    qua = Integer.parseInt(infos[2]);
                }catch(Exception ignored){}
            }
            boolean useMax = false;
            if(infos.length == 4 && !isOffiCmd)//Si un jet est spécifié
            {
                if(infos[3].equalsIgnoreCase("MAXIMO"))useMax = true;
            }
            ObjTemplate t = Mundo.getObjTemplate(tID);
            if(t == null) {
                String mess = "El objeto modelo "+tID+" no existe";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
                return;
            }
            if(qua <1)qua =1;
            Objeto obj = t.createNewItem(qua,useMax);
            if(_personaje.addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
                Mundo.addObjet(obj,true);
            String str = "Se ha creado un objeto "+tID+" con exito";
            if(useMax) str += " en sus maximas caracteristicas";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            GestorSalida.GAME_SEND_Ow_PACKET(_personaje);
        }else
        if (comando.equalsIgnoreCase("REFRESCAR")) {
            String Mob = null;
            try {
                Mob = infos[1];
            }catch(Exception ignored){}
            if(Mob == null) return;
            _personaje.getActualMapa().spawnGroupOnCommand(_personaje.getActualCelda().getID(), Mob);
        }else
        if (comando.equalsIgnoreCase("DAR_TITULO")) {
            Personaje target = null;
            byte TitleID = 0;
            try {
                target = Mundo.getPersonajePorNombre(infos[1]);
                TitleID = Byte.parseByte(infos[2]);
            }catch(Exception ignored){}

            if(target == null) {
                String str = "El personaje no existe";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }

            target.set_title(TitleID);
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "El titulo se ha agregado.");
            GestorSQL.guardar_personaje(target, false);
            if(target.getPelea() == null) GestorSalida.GAME_SEND_ALTER_GM_PACKET(target.getActualMapa(), target);
        }else {
            this.ComandosGmNivelUno(comando, infos, mensaje);
        }
    }

    public void ComandosGmNivelTres(String command, String[] infos, String msg) {
        if(_cuenta.getGMLVL() < 3) {
            _cuenta.getJuegoThread().closeSocket();
            return;
        }

        if(command.equalsIgnoreCase("REINICIAR")) {
            System.exit(0);
        } else
        if (command.equalsIgnoreCase("DESCONGELAR_TURNOS")) {
            Mundo.ticAllFightersTurns();
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "isAlive= " + MainServidor._passerTours.isAlive() + ", SDATA= " + MainServidor._passerTours.toString());
        } else
        if (command.equalsIgnoreCase("EXPULSAR_A_TODOS")) {
            MainServidor.gameServer.expulsaratodos();
        }else
        if (command.equalsIgnoreCase("FINALIZAR_TURNOS")) {
            MainServidor._passerTours = new Thread(new JuegoServidor.todoslosturnospelea());
            MainServidor._passerTours.start();
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Finalizados");
        } else
        if(command.equalsIgnoreCase("GUARDAR") && !MainServidor.isSaving) {
            Thread t = new Thread(new SaveThread());
            t.start();
            String mess = "Guardado lanzado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            return;
        }else
        if(command.equalsIgnoreCase("IR_COORDENADAS")) {
            int cell = _personaje.getActualCelda().getID();
            String mess = "["+ Camino.getCellXCoord(_personaje.getActualMapa(), cell)+","+ Camino.getCellYCoord(_personaje.getActualMapa(), cell)+"]";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, mess);
            return;
        }else
        if(command.equalsIgnoreCase("ELIMINAR_CELDA_PELEA")) {
            int cell = -1;
            try {
                cell = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}
            if(cell < 0 || _personaje.getActualMapa().getMapa(cell) == null) {
                cell = _personaje.getActualCelda().getID();
            }
            String places = _personaje.getActualMapa().getEsquemaPelea();
            String[] p = places.split("\\|");
            StringBuilder newPlaces = new StringBuilder();
            String team0 = "",team1 = "";
            try {
                team0 = p[0];
            }catch(Exception ignored){}
            try{
                team1 = p[1];
            }catch(Exception ignored){}

            for(int a = 0;a<=team0.length()-2;a+=2) {
                String c = p[0].substring(a,a+2);
                if(cell == GestorEncriptador.cellCode_To_ID(c))continue;
                newPlaces.append(c);
            }
            newPlaces.append("|");
            for(int a = 0;a<=team1.length()-2;a+=2) {
                String c = p[1].substring(a,a+2);
                if(cell == GestorEncriptador.cellCode_To_ID(c))continue;
                newPlaces.append(c);
            }
            _personaje.getActualMapa().setPlaces(newPlaces.toString());
            if(!GestorSQL.guardar_mapa(_personaje.getActualMapa()))return;
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"Las celdas se han modificado ("+newPlaces+")");
            return;
        }else
        if(command.equalsIgnoreCase("BANEAR_PERSONAJE")) {
            Personaje P = Mundo.getPersonajePorNombre(infos[1]);
            if(P == null) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "El personaje no existe");
                return;
            }
            if(P.getCuenta() == null) GestorSQL.cargar_cuenta_por_id(P.getAccID());
            if(P.getCuenta() == null) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Error");
                return;
            }
            P.getCuenta().setBanned(true);
            GestorSQL.actualizar_datos_cuenta(P.getCuenta());
            if(P.getCuenta().getJuegoThread() != null)P.getCuenta().getJuegoThread().kick();
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Usted ha baneado a "+P.getNombre());
            return;
        }else
        if(command.equalsIgnoreCase("DESBANEAR_PERSONAJE")) {
            Personaje P = Mundo.getPersonajePorNombre(infos[1]);
            if(P == null) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "El personaje no existe");
                return;
            }
            if(P.getCuenta() == null) GestorSQL.cargar_cuenta_por_id(P.getAccID());
            if(P.getCuenta() == null) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Error");
                return;
            }
            P.getCuenta().setBanned(false);
            GestorSQL.actualizar_datos_cuenta(P.getCuenta());
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Usted ha desbaneado a "+P.getNombre());
            return;
        }else
        if(command.equalsIgnoreCase("AGREGAR_ESQUEMA_DE_PELEA")) {
            int team = -1;
            int cell = -1;
            try {
                team = Integer.parseInt(infos[1]);
                cell = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}
            if( team < 0 || team>1) {
                String str = "Equipo o celda incorrecta";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            if(cell <0 || _personaje.getActualMapa().getMapa(cell) == null || !_personaje.getActualMapa().getMapa(cell).isCaminable(true)) {
                cell = _personaje.getActualCelda().getID();
            }
            String places = _personaje.getActualMapa().getEsquemaPelea();
            String[] p = places.split("\\|");
            boolean already = false;
            String team0 = "",team1 = "";
            try {
                team0 = p[0];
            }catch(Exception ignored){}
            try {
                team1 = p[1];
            }catch(Exception ignored){}

            //Si case déjà utilisée
            System.out.println("0 => "+team0+"\n1 =>"+team1+"\nCell: "+ GestorEncriptador.cellID_To_Code(cell));
            for(int a = 0; a <= team0.length()-2;a+=2)if(cell == GestorEncriptador.cellCode_To_ID(team0.substring(a,a+2)))already = true;
            for(int a = 0; a <= team1.length()-2;a+=2)if(cell == GestorEncriptador.cellCode_To_ID(team1.substring(a,a+2)))already = true;
            if(already) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"La celda ya existe en el esquema");
                return;
            }
            if(team == 0)team0 += GestorEncriptador.cellID_To_Code(cell);
            else if(team == 1)team1 += GestorEncriptador.cellID_To_Code(cell);

            String newPlaces = team0+"|"+team1;

            _personaje.getActualMapa().setPlaces(newPlaces);
            if(!GestorSQL.guardar_mapa(_personaje.getActualMapa()))return;
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"Las celdas del esquema se han modificado ("+newPlaces+")");
            return;
        }else
        if(command.equalsIgnoreCase("MODIFICAR_GRUPOS_MAXIMO_MOOBS")) {
            infos = msg.split(" ",4);
            byte id = -1;
            try {
                id = Byte.parseByte(infos[1]);
            }catch(Exception ignored){}
            if(id == -1) {
                String str = "Valor invalido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            String mess = "El numero de grupo de moobs ha sido arreglado";
            _personaje.getActualMapa().setMaxGroup(id);
            boolean ok = GestorSQL.guardar_mapa(_personaje.getActualMapa());
            if(ok)mess += " se ha guardado en la base de datos";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
        }else
        if(command.equalsIgnoreCase("AGREGAR_ACCION_RESPUESTA")) {
            infos = msg.split(" ",4);
            int id = -30;
            int repID = 0;
            String args = infos[3];
            try {
                repID = Integer.parseInt(infos[1]);
                id = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}
            NPC_reponse rep = Mundo.getNPCreponse(repID);
            if(id == -30 || rep == null) {
                String str = "Al menos uno de los valores no es valido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            String mess = "La accion ha sido agregada";

            rep.addAction(new Accion(id,args,""));
            boolean ok = GestorSQL.agregar_respuesta_npc(repID,id,args);
            if(ok)mess += " se ha actualizado en la base de datos";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
        }else
        if(command.equalsIgnoreCase("AGREGAR_PREGUNTA_PRINCIPAL")) {
            infos = msg.split(" ",4);
            int id = -30;
            int q = 0;
            try {
                q = Integer.parseInt(infos[2]);
                id = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(id == -30) {
                String str = "ID del NPC es invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            String mess = "La pregunta inicial ha sido agregada";
            NPCModelo npc = Mundo.getNPCTemplate(id);

            npc.setInitQuestion(q);
            boolean ok = GestorSQL.actualizar_respuesta_de_npc(id,q);
            if(ok)mess += " se ha actualizado la base de datos";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
        }else
        if(command.equalsIgnoreCase("AGREGAR_ACCION_FIN_PELEA")) {
            infos = msg.split(" ",4);
            int id = -30;
            int type = 0;
            String args = infos[3];
            String cond = infos[4];
            try {
                type = Integer.parseInt(infos[1]);
                id = Integer.parseInt(infos[2]);

            }catch(Exception ignored){}
            if(id == -30) {
                String str = "Alguno de los datos es invalido";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            String mess = "La accion se ha cambiado";
            _personaje.getActualMapa().addEndFightAction(type, new Accion(id,args,cond));
            boolean ok = GestorSQL.agregar_fin_pelea_accion(_personaje.getActualMapa().getID(),type,id,args,cond);
            if(ok)mess += " se ha actualizado la base de datos";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,mess);
            return;
        }else
        if(command.equalsIgnoreCase("ACTUALIZAR_GRUPO_FIX")) {
            String groupData = infos[1];

            _personaje.getActualMapa().addStaticGroup(_personaje.getActualCelda().getID(), groupData);
            String str = "El grupo fue arreglado";
            //Sauvegarde DB de la modif
            if(GestorSQL.guardar_nuevo_grupo_monstruos(_personaje.getActualMapa().getID(), _personaje.getActualCelda().getID(), groupData))
                str += " se ha actualizado la base de datos";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            return;
        }else
        if(command.equalsIgnoreCase("AGREGAR_NPC")) {
            int id = 0;
            try
            {
                id = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(id == 0 || Mundo.getNPCTemplate(id) == null)
            {
                String str = "ID del NPC invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            NPC npc = _personaje.getActualMapa().addNpc(id, _personaje.getActualCelda().getID(), _personaje.getOrientacion());
            GestorSalida.ENVIAR_AGREGAR_NPC_EN_MAPA(_personaje.getActualMapa(), npc);
            String str = "El NPC se ha agregado";
            if(_personaje.getOrientacion() == 0
                    || _personaje.getOrientacion() == 2
                    || _personaje.getOrientacion() == 4
                    || _personaje.getOrientacion() == 6)
                str += " pero esta invisible, ya que la orientacion no es valida.";

            if(GestorSQL.agregar_npc_en_mapa(_personaje.getActualMapa().getID(), id, _personaje.getActualCelda().getID(), _personaje.getOrientacion()))
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            else
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"Error al momento de guardar la posicion del npc");
        }else
        if(command.equalsIgnoreCase("ELIMINAR_NPC"))
        {
            int id = 0;
            try
            {
                id = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            NPC npc = _personaje.getActualMapa().getNPC(id);
            if(id == 0 || npc == null)
            {
                String str = "ID negativa del npc es invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            int exC = npc.getCeldaID();
            //on l'efface de la map
            GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_personaje.getActualMapa(), id);
            _personaje.getActualMapa().removeNpcOrMobGroup(id);

            String str = "El npc se ha suprimido";
            if(GestorSQL.eliminar_npc_en_mapa(_personaje.getActualMapa().getID(),exC))
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
            else
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"error al guardar la posicion");
        }else
        if(command.equalsIgnoreCase("ELIMINAR_ACCION_DE_CELDA")) {
            int cellID = -1;
            try {
                cellID = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(cellID == -1 || _personaje.getActualMapa().getMapa(cellID) == null) {
                String str = "Celda invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }

            _personaje.getActualMapa().getMapa(cellID).EliminarAccionDeCelda();
            boolean success = GestorSQL.eliminar_celdas(_personaje.getActualMapa().getID(),cellID);
            String str = "";
            if(success)	str = "La accion de la celda ha sido eliminada";
            else 		str = "La accion de la celda no puede ser eliminada";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(command.equalsIgnoreCase("AGREGAR_ACCION_DE_CELDA")) {
            int actionID = -1;
            String args = "",cond = "";
            try
            {
                actionID = Integer.parseInt(infos[1]);
                args = infos[2];
                cond = infos[3];
            }catch(Exception ignored){}
            if(args.equals("") || actionID <= -3) {
                String str = "Celda invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }

            _personaje.getActualCelda().addOnCellStopAction(actionID,args, cond);
            boolean success = GestorSQL.guardar_celdas(_personaje.getActualMapa().getID(), _personaje.getActualCelda().getID(),actionID,1,args,cond);
            String str = "";
            if(success)	str = "La accion de celda se ha agregado";
            else 		str = "La accion de celda no puede ser agregada";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(command.equalsIgnoreCase("ELIMINAR_OBJETO_NPC_VENTA")) {
            if(_cuenta.getGMLVL() <3)return;
            int npcGUID = 0;
            int itmID = -1;
            try {
                npcGUID = Integer.parseInt(infos[1]);
                itmID = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}
            NPCModelo npc =  _personaje.getActualMapa().getNPC(npcGUID).getModelo();
            if(npcGUID == 0 || itmID == -1 || npc == null) {
                String str = "Id negativa del NPC o ID del item invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            String str = "";
            if(npc.delItemVendor(itmID))str = "El objeto se ha eliminado";
            else str = "El objeto no puede ser retirado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(command.equalsIgnoreCase("AGREGAR_OBJETO_NPC_VENTA")) {
            if(_cuenta.getGMLVL() <3)return;
            int npcGUID = 0;
            int itmID = -1;
            try {
                npcGUID = Integer.parseInt(infos[1]);
                itmID = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}
            NPCModelo npc =  _personaje.getActualMapa().getNPC(npcGUID).getModelo();
            ObjTemplate item =  Mundo.getObjTemplate(itmID);
            if(npcGUID == 0 || itmID == -1 || npc == null || item == null) {
                String str = "ID negativa del NPC o ID del item invalida";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            String str = "";
            if(npc.addItemVendor(item))str = "El objeto se ha agregado con exito";
            else str = "El objeto no puede ser agregado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(command.equalsIgnoreCase("AGREGAR_CERCADO")) {
            int size = -1;
            int owner = -2;
            int price = -1;
            try {
                size = Integer.parseInt(infos[1]);
                owner = Integer.parseInt(infos[2]);
                price = Integer.parseInt(infos[3]);
                if(price > 20000000)price = 20000000;
                if(price <0)price = 0;
            }catch(Exception ignored){}
            if(size == -1 || owner == -2 || price == -1 || _personaje.getActualMapa().getMountPark() != null) {
                String str = "Informacion invalida, no se puede configurar";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            MountPark MP = new MountPark(owner, _personaje.getActualMapa(), _personaje.getActualCelda().getID(), size, "", -1, price);
            _personaje.getActualMapa().setMountPark(MP);
            GestorSQL.guardar_cercados(MP);
            String str = "La configuracion del cercado ha sido un exito";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if (command.equalsIgnoreCase("PROGRAMAR_REINICIO")) {
            int time = 30, OffOn = 0;
            try {
                OffOn = Integer.parseInt(infos[1]);
                time = Integer.parseInt(infos[2]);
            }catch(Exception ignored){}

            if(OffOn == 1 && _TimerStart)// demande de démarer le reboot
            {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Un reinicio se ha programado.");
            }else if(OffOn == 1 && !_TimerStart) {
                _timer = createTimer(time);
                _timer.start();
                _TimerStart = true;
                String timeMSG = "minutos";
                if(time <= 1) {
                    timeMSG = "minuto";
                }
                GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_A_TODOS("115;"+time+" "+timeMSG);
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Reinicio lanzado.");
            }else if(OffOn == 0 && _TimerStart) {
                _timer.stop();
                _TimerStart = false;
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Reinicio se ha detenido.");
            }else if(OffOn == 0 && !_TimerStart) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "No se puede reiniciar.");
            }
        }else {
            this.ComandosGmNivelDos(command, infos, msg);
        }
    }

    public void ComandosGmNivelCuatro(String comandos, String[] infos, String mensaje) {
        if(_cuenta.getGMLVL() < 4) {
            _cuenta.getJuegoThread().closeSocket();
            return;
        }

        if(comandos.equalsIgnoreCase("DAR_ADMIN")) {
            int gmLvl = -100;
            try {
                gmLvl = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            if(gmLvl == -100) {
                String str = "Valor incorrecto";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }
            Personaje target = _personaje;
            if(infos.length > 2){ //Si un nom de perso est spécifié
                target = Mundo.getPersonajePorNombre(infos[2]);
                if(target == null) {
                    String str = "El personaje no existe";
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                    return;
                }
            }
            target.getCuenta().setGMLVL(gmLvl);
            GestorSQL.actualizar_datos_cuenta(target.getCuenta());
            String str = "El nivel de admin de "+_personaje.getNombre()+" se ha modificado";
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
        }else
        if(comandos.equalsIgnoreCase("BLOQUEAR_SERVIDOR")) {
            byte LockValue = 1;//Accessible
            try {
                LockValue = Byte.parseByte(infos[1]);
            }catch(Exception ignored){}

            if(LockValue > 2) LockValue = 2;
            if(LockValue < 0) LockValue = 0;
            Mundo.set_state(LockValue);
            if(LockValue == 1) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Servidor accesible.");
            }else if(LockValue == 0) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Servidor inaccesible.");
            }else if(LockValue == 2) {
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Servidor en modo guardado.");
            }
        }else if(comandos.equalsIgnoreCase("AGREGAR_PUBLICIDAD")) {
            infos = mensaje.split(" ",2);
            String nuevapublicidad;
            try {
                nuevapublicidad = String.valueOf(infos[1]);
            }catch(Exception e){
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "No puedes cargar una publicidad en blanco");
                return;
            }
            GestorSQL.agregar_publicidad(nuevapublicidad);
            GestorSQL.cargar_publicidades_automaticas();
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Publicidad cargada con exito");
        }else if(comandos.equalsIgnoreCase("SOLO_ADMIN")) {
            byte GmAccess = 0;
            byte KickPlayer = 0;
            try {
                GmAccess = Byte.parseByte(infos[1]);
                KickPlayer = Byte.parseByte(infos[2]);
            }catch(Exception ignored){}

            Mundo.setGmAccess(GmAccess);
            GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Server solo para admin nivel: "+GmAccess);
            if(KickPlayer > 0) {
                for(Personaje z : Mundo.getOnlinePersos()) {
                    if(z.getCuenta().getGMLVL() < GmAccess)
                        z.getCuenta().getJuegoThread().closeSocket();
                }
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "Lo jugadores con nivel de admin inferior a "+GmAccess+" seran expulsados.");
            }
        }else
        if(comandos.equalsIgnoreCase("BANEAR_IP")) {
            Personaje P = null;
            try {
                P = Mundo.getPersonajePorNombre(infos[1]);
            }catch(Exception ignored){}
            if(P == null || !P.isConectado()) {
                String str = "El personaje no existe.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }

            if(!Constantes.IPcompareToBanIP(P.getCuenta().getActualIP())) {
                Constantes.BAN_IP += ","+P.getCuenta().getActualIP();
                if(GestorSQL.agregar_ip_baneada(P.getCuenta().getActualIP())) {
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "La IP ha sido baneada.");
                }
                if(P.isConectado()){
                    P.getCuenta().getJuegoThread().kick();
                    GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir, "El jugador ha sido expulsado.");
                }
            }else {
                String str = "La IP no existe.";
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,str);
                return;
            }

        }else
        if(comandos.equalsIgnoreCase("VER_MERCADILLO_TOTAL")) {
            int numb = 1;
            try {
                numb = Integer.parseInt(infos[1]);
            }catch(Exception ignored){}
            fullHdv(numb);
        }else {
            this.ComandosGmNivelTres(comandos, infos, mensaje);
        }
    }

    private void fullHdv(int ofEachTemplate) {
        GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"Démarrage du remplissage!");

        Objeto objet = null;
        HdvEntry entry = null;
        byte amount = 0;
        int hdv = 0;

        int lastSend = 0;
        long time1 = System.currentTimeMillis();//TIME
        for (ObjTemplate curTemp : Mundo.getObjetosModelos()){ //Boucler dans les template
            try {
                if(MainServidor.NOTINHDV.contains(curTemp.getID())) continue;
                for (int j = 0; j < ofEachTemplate; j++) { //Ajouter plusieur fois le template
                    if(curTemp.getType() == 85) break;

                    objet = curTemp.createNewItem(1, false);
                    hdv = getHdv(objet.getTemplate().getType());

                    if(hdv < 0) break;

                    amount = (byte) Formulas.getRandomValue(1, 3);


                    entry = new HdvEntry(CalcularPrecio(objet,amount), amount, -1, objet);
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
                GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,(System.currentTimeMillis() - time1)/1000 + "sec Template: "+curTemp.getID());
            }
        }
        GestorSalida.ENVIAR_TEXTO_EN_CONSOLA(_imprimir,"Remplissage fini en "+(System.currentTimeMillis() - time1) + "ms");
        Mundo.saveAll(null);
        GestorSalida.ENVIAR_MENSAJE_A_TODOS("HDV remplis!", MainServidor.CONFIG_MOTD_COLOR);
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
                if(rand == 1) {
                    map = 4271;
                }else
                if(rand == 2) {
                    map = 4607;
                }else {
                    map = 7516;
                }
                return map;
            case 1:
            case 9:
                if(rand == 1) {
                    map = 4216;
                }else
                if(rand == 2) {
                    map = 4622;
                }else {
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
                if(rand == 1) {
                    map = 8759;
                }else {
                    map = 8753;
                }
                return map;
            case 63:
            case 64:
            case 69:
                if(rand == 1) {
                    map = 4287;
                }else
                if(rand == 2) {
                    map = 4595;
                }else
                if(rand == 3) {
                    map = 7515;
                }else {
                    map = 7350;
                }
                return map;
            case 33:
            case 42:
                if(rand == 1) {
                    map = 2221;
                }else
                if(rand == 2) {
                    map = 4630;
                }else {
                    map = 7510;
                }
                return map;
            case 84:
            case 93:
            case 112:
            case 114:
                if(rand == 1) {
                    map = 4232;
                }else
                if(rand == 2) {
                    map = 4627;
                }else {
                    map = 12262;
                }
                return map;
            case 38:
            case 95:
            case 96:
            case 98:
            case 108:
                if(rand == 1) {
                    map = 4178;
                }else
                if(rand == 2) {
                    map = 5112;
                }else {
                    map = 7289;
                }
                return map;
            case 10:
            case 11:
                if(rand == 1) {
                    map = 4183;
                }else
                if(rand == 2) {
                    map = 4562;
                }else {
                    map = 7602;
                }
                return map;
            case 13:
            case 25:
            case 73:
            case 75:
            case 76:
                if(rand == 1) {
                    map = 8760;
                }else {
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
                if(rand == 1) {
                    map = 4098;
                }else
                if(rand == 2) {
                    map = 5317;
                }else {
                    map = 7511;
                }
                return map;
            case 39:
            case 40:
            case 50:
            case 51:
            case 88:
                if(rand == 1) {
                    map = 4179;
                }else
                if(rand == 2) {
                    map = 5311;
                }else {
                    map = 7443;
                }
                return map;
            case 87:
                if(rand == 1) {
                    map = 6159;
                }else {
                    map = 6167;
                }
                return map;
            case 34:
            case 52:
            case 60:
                if(rand == 1) {
                    map = 4299;
                }else
                if(rand == 2) {
                    map = 4629;
                }else {
                    map = 7397;
                }
                return map;
            case 41:
            case 49:
            case 62:
                if(rand == 1) {
                    map = 4247;
                }else
                if(rand == 2) {
                    map = 4615;
                }else
                if(rand == 3) {
                    map = 7501;
                }else {
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
                if(rand == 1) {
                    map = 4262;
                }else
                if(rand == 2) {
                    map = 4646;
                }else {
                    map = 7413;
                }
                return map;
            case 78:
                if(rand == 1) {
                    map = 8757;
                }else {
                    map = 8756;
                }
                return map;
            case 2:
            case 3:
            case 4:
                if(rand == 1) {
                    map = 4174;
                }else
                if(rand == 2) {
                    map = 4618;
                }else {
                    map = 7512;
                }
                return map;
            case 16:
            case 17:
            case 81:
                if(rand == 1) {
                    map = 4172;
                }else
                if(rand == 2) {
                    map = 4588;
                }else {
                    map = 7513;
                }
                return map;
            case 83:
                if(rand == 1) {
                    map = 10129;
                }else {
                    map = 8482;
                }
                return map;
            case 82:
                return 8039;
            default:
                return -1;
        }
    }

    private int CalcularPrecio(Objeto obj, int logAmount) {
        int amount = (byte)(Math.pow(10, logAmount) / 10);
        int stats = 0;

        for(int curStat : obj.getStats().getMap().values()) {
            stats += curStat;
        }
        if(stats > 0)
            return (int) (((Math.cbrt(stats) * Math.pow(obj.getTemplate().getLevel(), 2)) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100)) * amount);
        else
            return (int) ((Math.pow(obj.getTemplate().getLevel(),2) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100))*amount);
    }
}