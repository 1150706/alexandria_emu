package objetos;

import java.io.PrintWriter;
import java.util.ArrayList;

import objetos.Oficio.StatsMetier;
import objetos.Monstruo.MobGroup;
import objetos.NPCModelo.NPC_question;
import objetos.Objeto.ObjTemplate;
import objetos.Personaje.traque;

import comunes.MainServidor;
import comunes.Condiciones;
import comunes.Constantes;
import comunes.Formulas;
import comunes.GestorSQL;
import comunes.GestorSalida;
import comunes.Mundo;

import juego.JuegoServidor;
import juego.JuegoThread;
import objetos.casas.Casas;

public class Accion {

	private final int _id;
	private final String _argumento;
	private final String _condicion;
	
	public Accion(int id, String argumento, String condicion) {
		this._id = id;
		this._argumento = argumento;
		this._condicion = condicion;
	}

	public void apply(Personaje perso, Personaje target, int itemID, int cellid) {
		if(perso == null)return;
		if(!_condicion.equalsIgnoreCase("") && !_condicion.equalsIgnoreCase("-1")&& !Condiciones.ValidarCondicion(perso, _condicion)) {
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "119");
			return;
		}
		if(perso.getCuenta().getGameThread() == null) return;
		PrintWriter out = perso.getCuenta().getGameThread().get_out();
		switch(_id) {

			case -2://Crear un gremio
				if(perso.is_away())return;
				if(perso.get_guild() != null || perso.getMiembroGremio() != null) {
					GestorSalida.GAME_SEND_gC_PACKET(perso, "Ea");
					return;
				}
				GestorSalida.GAME_SEND_gn_PACKET(perso);
			break;

			case -1://Abrir el banco
				//Guardamos el personaje y los elementos antes de entrar al banco
				GestorSQL.guardar_personaje(perso,true);
				//Si tiene deshonor no dejamos que abra el banco
				if(perso.getDeshonor() >= 1) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "183");
					return;
				}
				//Sacamos la cantidad de kamas necesarias para abrir el banco
				int cost = perso.getCostoAbrirBanco();
				if(cost > 0) {
					long nKamas = perso.getKamas() - cost;
					//Si el jugador no tiene las suficientes kamas para abrir el banco
					if(nKamas <0){
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1128;"+cost);
						return;
					}
					perso.setKamas(nKamas);
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "020;"+cost);
				}
				GestorSalida.GAME_SEND_ECK_PACKET(perso.getCuenta().getGameThread().get_out(), 5, "");
				GestorSalida.GAME_SEND_EL_BANK_PACKET(perso);
				perso.set_away(true);
				perso.setInBank(true);
			break;
			
			case 0://Teletransportacion
				try {
					short nuevomapa = Short.parseShort(_argumento.split(",",2)[0]);
					int nuevacelda = Integer.parseInt(_argumento.split(",",2)[1]);
					perso.teletransportar(nuevomapa,nuevacelda);
				}catch(Exception e ){return;}
				break;
			
			case 1://Discusion con un NPC
				out = perso.getCuenta().getGameThread().get_out();
				if(_argumento.equalsIgnoreCase("DV")) {
					GestorSalida.GAME_SEND_END_DIALOG_PACKET(out);
					perso.set_isTalkingWith(0);
				}else {
					int qID = -1;
					try {
						qID = Integer.parseInt(_argumento);
					}catch(NumberFormatException ignored){}

					NPC_question  quest = Mundo.getNPCQuestion(qID);
					if(quest == null) {
						GestorSalida.GAME_SEND_END_DIALOG_PACKET(out);
						perso.set_isTalkingWith(0);
						return;
					}
					GestorSalida.GAME_SEND_QUESTION_PACKET(out, quest.parseToDQPacket(perso));
				}
			break;
			
			case 4://Kamas
				try {
					int count = Integer.parseInt(_argumento);
					long curKamas = perso.getKamas();
					long newKamas = curKamas + count;
					if(newKamas <0) newKamas = 0;
					perso.setKamas(newKamas);
					//Si en ligne (normalement oui)
					if(perso.isConectado())
						GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

			case 5://Objeto
				try {
					int tID = Integer.parseInt(_argumento.split(",")[0]);
					int count = Integer.parseInt(_argumento.split(",")[1]);
					boolean send = true;
					if(_argumento.split(",").length >2)send = _argumento.split(",")[2].equals("1");
					
					//Si on ajoute
					if(count > 0) {
						ObjTemplate T = Mundo.getObjTemplate(tID);
						if(T == null)return;
						Objeto O = T.createNewItem(count, false);
						//Si retourne true, on l'ajoute au monde
						if(perso.addObjet(O, true))
							Mundo.addObjet(O, true);
					}else {
						perso.removeByTemplateID(tID,-count);
					}
					//Si en ligne (normalement oui)
					if(perso.isConectado())//on envoie le packet qui indique l'ajout//retrait d'un item
					{
						GestorSalida.GAME_SEND_Ow_PACKET(perso);
						if(send) {
							if(count >= 0){
								GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "021;"+count+"~"+tID);
							}
							else if(count < 0){
								GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "022;"+-count+"~"+tID);
							}
						}
					}
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

			case 6://Aprender un oficio
				try {
					int oficioid = Integer.parseInt(_argumento);
					if(Mundo.getMetier(oficioid) == null)return;
					//Si es un oficio basico
					if(oficioid == 	2 || oficioid == 11 ||
					   oficioid == 13 || oficioid == 14 ||
					   oficioid == 15 || oficioid == 16 ||
					   oficioid == 17 || oficioid == 18 ||
					   oficioid == 19 || oficioid == 20 ||
					   oficioid == 24 || oficioid == 25 ||
					   oficioid == 26 || oficioid == 27 ||
					   oficioid == 28 || oficioid == 31 ||
					   oficioid == 36 || oficioid == 41 ||
					   oficioid == 56 || oficioid == 58 ||
					   oficioid == 60 || oficioid == 65) {
						if(perso.getOficioPorID(oficioid) != null)//Métier déjà appris
						{
							GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "111");
						}
						
						if(perso.totalJobBasic() > 2)//On compte les métiers déja acquis si c'est supérieur a 2 on ignore
						{
							GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "19");
						}else//Si c'est < ou = à 2 on apprend
						{
							perso.learnJob(Mundo.getMetier(oficioid));
						}
					}
					//Si es un oficio espesial de ForjaMagia
					if(oficioid == 	43 || oficioid == 44 ||
					   oficioid == 45 || oficioid == 46 ||
					   oficioid == 47 || oficioid == 48 ||
					   oficioid == 49 || oficioid == 50 ||
					   oficioid == 62 || oficioid == 63 ||
					   oficioid == 64) {
						//Requiere nivel 65 de un oficio simple
						if(perso.getOficioPorID(17) != null && perso.getOficioPorID(17).get_lvl() >= 65 && oficioid == 43
						|| perso.getOficioPorID(11) != null && perso.getOficioPorID(11).get_lvl() >= 65 && oficioid == 44
						|| perso.getOficioPorID(14) != null && perso.getOficioPorID(14).get_lvl() >= 65 && oficioid == 45
						|| perso.getOficioPorID(20) != null && perso.getOficioPorID(20).get_lvl() >= 65 && oficioid == 46
						|| perso.getOficioPorID(31) != null && perso.getOficioPorID(31).get_lvl() >= 65 && oficioid == 47
						|| perso.getOficioPorID(13) != null && perso.getOficioPorID(13).get_lvl() >= 65 && oficioid == 48
						|| perso.getOficioPorID(19) != null && perso.getOficioPorID(19).get_lvl() >= 65 && oficioid == 49
						|| perso.getOficioPorID(18) != null && perso.getOficioPorID(18).get_lvl() >= 65 && oficioid == 50
						|| perso.getOficioPorID(15) != null && perso.getOficioPorID(15).get_lvl() >= 65 && oficioid == 62
						|| perso.getOficioPorID(16) != null && perso.getOficioPorID(16).get_lvl() >= 65 && oficioid == 63
						|| perso.getOficioPorID(27) != null && perso.getOficioPorID(27).get_lvl() >= 65 && oficioid == 64) {
							//On compte les specialisations déja acquis si c'est supérieur a 2 on ignore
							if(perso.getOficioPorID(oficioid) != null)//Métier déjà appris
							{
								GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "111");
							}
							
							if(perso.totalJobFM() > 2)//On compte les métiers déja acquis si c'est supérieur a 2 on ignore
							{
								GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "19");
							}
							else//Si c'est < ou = à 2 on apprend
							{
								perso.learnJob(Mundo.getMetier(oficioid));
								perso.getOficioPorID(oficioid).AgregarExperiencia(perso, 582000);//Level 100 direct
							}	
						}else {
							GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "12");
						}
					}
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

			case 7://Devolver al punto de guardado
				perso.warpToSavePos();
			break;

			case 8://Ajustar las estadisticas
		            int statID = Integer.parseInt(_argumento.split(",", 2)[0]);
		            int number = Integer.parseInt(_argumento.split(",", 2)[1]);
		            perso.get_baseStats().addOneStat(statID, number);
		            GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
		            int messID = 0;
				if (statID == 126) { // '~'
					messID = 14;
				}
		            if(messID > 0)
		                GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "0" + messID + ";" + number);
		            return;

			case 9://Aprender un hechizo
				try {
					int sID = Integer.parseInt(_argumento);
					if(Mundo.getSort(sID) == null)return;
					perso.AprenderHechizo(sID,1, true,true);
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

			case 10://Pain/potion/viande/poisson
				try {
					int min = Integer.parseInt(_argumento.split(",",2)[0]);
					int max = Integer.parseInt(_argumento.split(",",2)[1]);
					if(max == 0) max = min;
					int val = Formulas.getRandomValue(min, max);
					if(target != null) {
						if(target.get_PDV() + val > target.get_PDVMAX())val = target.get_PDVMAX()-target.get_PDV();
						target.set_PDV(target.get_PDV()+val);
						GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(target);
					} else {
						if(perso.get_PDV() + val > perso.get_PDVMAX())val = perso.get_PDVMAX()-perso.get_PDV();
						perso.set_PDV(perso.get_PDV()+val);
						GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
					}
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

			case 11://Definir la alineacion
				try {
					byte newAlign = Byte.parseByte(_argumento.split(",",2)[0]);
					boolean replace = Integer.parseInt(_argumento.split(",",2)[1]) == 1;
					//Si le perso n'est pas neutre, et qu'on doit pas remplacer, on passe
					if(perso.get_align() != Constantes.ALIGNEMENT_NEUTRE && !replace)return;
					perso.modifAlignement(newAlign);
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

			case 12://Refrescar un grupo de monstruos
				try {
					boolean delObj = _argumento.split(",")[0].equals("true");
					boolean inArena = _argumento.split(",")[1].equals("true");
					if(inArena && !Mundo.isArenaMap(perso.getActualMapa().getID()))return;	//Si la map du personnage n'est pas classé comme étant dans l'arène
					PiedraAlma pierrePleine = (PiedraAlma) Mundo.getObjet(itemID);
					String groupData = pierrePleine.parseGroupData();
					String condition = "MiS = "+perso.get_GUID();	//Condition pour que le groupe ne soit lançable que par le personnage qui à utiliser l'objet
					perso.getActualMapa().spawnNewGroup(true, perso.getActualCelda().getID(), groupData,condition);
					if(delObj) {
						perso.removeItem(itemID, 1, true, true);
					}
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

		    case 13://Reiniciar caracteristicas
		        try {
		          perso.get_baseStats().addOneStat(125, -perso._baseStats.getEffect(125));
		          perso.get_baseStats().addOneStat(124, -perso._baseStats.getEffect(124));
		          perso.get_baseStats().addOneStat(118, -perso._baseStats.getEffect(118));
		          perso.get_baseStats().addOneStat(123, -perso._baseStats.getEffect(123));
		          perso.get_baseStats().addOneStat(119, -perso._baseStats.getEffect(119));
		          perso.get_baseStats().addOneStat(126, -perso._baseStats.getEffect(126));
		          perso.addPuntosDeCapital((perso.get_lvl() - 1) * 5 - perso.get_capital());
		          GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
		        }catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

		    case 14://Ouvrir l'interface d'oublie de sort
		    	perso.setisForgetingSpell(true);
				GestorSalida.GAME_SEND_FORGETSPELL_INTERFACE('+', perso);
			break;
			case 15://Téléportation donjon
				try
				{
					short newMapID = Short.parseShort(_argumento.split(",")[0]);
					int newCellID = Integer.parseInt(_argumento.split(",")[1]);
					int ObjetNeed = Integer.parseInt(_argumento.split(",")[2]);
					int MapNeed = Integer.parseInt(_argumento.split(",")[3]);
					if(ObjetNeed == 0)
					{
						//Téléportation sans objets
						perso.teletransportar(newMapID,newCellID);
					}else if(ObjetNeed > 0)
					{
					if(MapNeed == 0)
					{
						//Téléportation sans map
						perso.teletransportar(newMapID,newCellID);
					}else if(MapNeed > 0)
					{
					if (perso.hasItemTemplate(ObjetNeed, 1) && perso.getActualMapa().getID() == MapNeed)
					{
						//Le perso a l'item
						//Le perso est sur la bonne map
						//On téléporte, on supprime après
						perso.teletransportar(newMapID,newCellID);
						perso.removeByTemplateID(ObjetNeed, 1);
						GestorSalida.GAME_SEND_Ow_PACKET(perso);
					}
					else if(perso.getActualMapa().getID() != MapNeed)
					{
						//Le perso n'est pas sur la bonne map
						GestorSalida.GAME_SEND_MESSAGE(perso, "Vous n'etes pas sur la bonne map du donjon pour etre teleporter.", "009900");
					}
					else
					{
						//Le perso ne possède pas l'item
						GestorSalida.GAME_SEND_MESSAGE(perso, "Vous ne possedez pas la clef necessaire.", "009900");
					}
					}
					}
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 16://Ajout d'honneur HonorValue
				try
				{
					if(perso.get_align() != 0)
					{
						int AddHonor = Integer.parseInt(_argumento);
						int ActualHonor = perso.get_honor();
						perso.set_honor(ActualHonor+AddHonor);
					}
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 17://Xp métier JobID,XpValue
				try
				{
					int JobID = Integer.parseInt(_argumento.split(",")[0]);
					int XpValue = Integer.parseInt(_argumento.split(",")[1]);
					if(perso.getOficioPorID(JobID) != null)
					{
						perso.getOficioPorID(JobID).AgregarExperiencia(perso, XpValue);
					}
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 18://Téléportation chez sois
				if(Casas.AlreadyHaveHouse(perso))//Si il a une maison
				{
					Objeto obj = Mundo.getObjet(itemID);
					if (perso.hasItemTemplate(obj.getTemplate().getID(), 1))
					{
						perso.removeByTemplateID(obj.getTemplate().getID(),1);
						Casas h = Casas.get_HouseByPerso(perso);
						if(h == null) return;
						perso.teletransportar((short)h.get_mapid(), h.get_caseid());
					}
				}
			break;
			case 19://Téléportation maison de guilde (ouverture du panneau de guilde)
				GestorSalida.GAME_SEND_GUILDHOUSE_PACKET(perso);
			break;
			case 20://+Points de sorts
				try
				{
					int pts = Integer.parseInt(_argumento);
					if(pts < 1) return;
					perso.addAgregarPuntosDeHechizo(pts);
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 21://+Energie
				try
				{
					int Energy = Integer.parseInt(_argumento);
					if(Energy < 1) return;
					
					int EnergyTotal = perso.get_energy()+Energy;
					if(EnergyTotal > 10000) EnergyTotal = 10000;
					
					perso.set_energy(EnergyTotal);
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 22://+Xp
				try
				{
					long XpAdd = Integer.parseInt(_argumento);
					if(XpAdd < 1) return;
					
					long TotalXp = perso.get_curExp()+XpAdd;
					perso.set_curExp(TotalXp);
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 23://UnlearnJob
				try
				{
					int Job = Integer.parseInt(_argumento);
					if(Job < 1) return;
					StatsMetier m = perso.getOficioPorID(Job);
					if(m == null) return;
					perso.unlearnJob(m.getID());
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
					GestorSQL.guardar_personaje(perso, false);
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 24://SimpleMorph
				try
				{
					int morphID = Integer.parseInt(_argumento);
					if(morphID < 0)return;
					perso.setGFX(morphID);
					GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getActualMapa(), perso.get_GUID());
					GestorSalida.ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(perso.getActualMapa(), perso);
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 25://SimpleUnMorph
				int UnMorphID = perso.getClase()*10 + perso.getSexo();
				perso.setGFX(UnMorphID);
				GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getActualMapa(), perso.get_GUID());
				GestorSalida.ENVIAR_AGREGAR_PERSONAJE_EN_MAPA(perso.getActualMapa(), perso);
			break;
			case 26://Téléportation enclo de guilde (ouverture du panneau de guilde)
				GestorSalida.GAME_SEND_GUILDENCLO_PACKET(perso);
			break;
			case 27://startFigthVersusMonstres args : monsterID,monsterLevel| ...
				String ValidMobGroup = "";
				try
		        {
					for(String MobAndLevel : _argumento.split("\\|"))
					{
						int monsterID = -1;
						int monsterLevel = -1;
						String[] MobOrLevel = MobAndLevel.split(",");
						monsterID = Integer.parseInt(MobOrLevel[0]);
						monsterLevel = Integer.parseInt(MobOrLevel[1]);
						
						if(Mundo.getMonstre(monsterID) == null || Mundo.getMonstre(monsterID).getGradeByLevel(monsterLevel) == null)
						{
							if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Monstre invalide : monsterID:"+monsterID+" monsterLevel:"+monsterLevel);
							continue;
						}
						ValidMobGroup += monsterID+","+monsterLevel+","+monsterLevel+";";
					}
					if(ValidMobGroup.isEmpty()) return;
					MobGroup group  = new MobGroup(perso.getActualMapa()._nextObjectID,perso.getActualCelda().getID(),ValidMobGroup);
					perso.getActualMapa().startFigthVersusMonstres(perso, group);
		        }catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			case 50://Traque
				if(perso.get_traque() == null)
				{
					traque traq = new traque(0, null);
					perso.set_traque(traq);
				}
				if(perso.get_traque().get_time() < System.currentTimeMillis() - 600000 || perso.get_traque().get_time() == 0)
				{
					Personaje tempP = null;
					int tmp = 15;
					int diff = 0;
					for(byte b = 0; b < 100; b++)
					{
					if(b == MainServidor.gameServer.getClients().size())break;
					JuegoThread GT = MainServidor.gameServer.getClients().get(b);
					Personaje P = GT.getPerso();
					if(P == null || P == perso)continue;
					if(P.getCuenta().getActualIP().compareTo(perso.getCuenta().getActualIP()) == 0)continue;
					//SI pas sériane ni neutre et si alignement opposé
					if(P.get_align() == perso.get_align() || P.get_align() == 0 || P.get_align() == 3)continue;
					
					if(P.get_lvl()>perso.get_lvl())diff = P.get_lvl() - perso.get_lvl();
					if(perso.get_lvl()>P.get_lvl())diff = perso.get_lvl() - P.get_lvl();
					if(diff<tmp)tempP = P; tmp = diff;
					}
					if(tempP == null)
					{
						GestorSalida.GAME_SEND_MESSAGE(perso, "Nous n'avons pas trouve de cible a ta hauteur. Reviens plus tard." , "000000");
						break;
					}
					
					
					GestorSalida.GAME_SEND_MESSAGE(perso, "Vous etes desormais en chasse de "+tempP.getNombre()+"." , "000000");
					
					perso.get_traque().set_traqued(tempP);
					perso.get_traque().set_time(System.currentTimeMillis());
					
					
					ObjTemplate T = Mundo.getObjTemplate(10085);
					if(T == null)return;
					perso.removeByTemplateID(T.getID(),100);
					
					Objeto newObj = T.createNewItem(20, false);
					//On ajoute le nom du type à recherché
					
					newObj.addTxtStat(962, Integer.toString(tempP.get_lvl()));
					newObj.addTxtStat(961, Integer.toString(tempP.getGrade()));
					
					int alignid = tempP.get_align();
					String align = "";
					switch(alignid)
					{
					case 0:
					align = "Neutre";
					case 1:
					align = "Bontarien";
					break;
					case 2:
					align = "Brakmarien";
					break;
					case 3:
					align = "Sériane";
					break;
					}
					newObj.addTxtStat(960, align);
					
					newObj.addTxtStat(989, tempP.getNombre());
					
					//Si retourne true, on l'ajoute au monde
					if(perso.addObjet(newObj, true)){
						Mundo.addObjet(newObj, true);
			}else
			{
				perso.removeByTemplateID(T.getID(),20);
			}
			}
			else{
			GestorSalida.GAME_SEND_MESSAGE(perso, "Thomas Sacre : Vous venez juste de signer un contrat, vous devez vous reposer." , "000000");
				}

			break;
			case 51://Cible sur la géoposition
				String perr = "";
				
				perr = Mundo.getObjet(itemID).getTraquedName();
				if(perr == null)
				{
					break;	
				}
				Personaje cible = Mundo.getPersonajePorNombre(perr);
				if(cible==null)break;
				if(!cible.isConectado())
				{
					GestorSalida.GAME_SEND_MESSAGE(perso, "Ce joueur n'est pas connecte." , "000000");
					break;
				}
				GestorSalida.GAME_SEND_FLAG_PACKET(perso, cible);
			break;
			case 52://recompenser pour traque
				if(perso.get_traque() != null && perso.get_traque().get_time() == -2)
				{
					int xp = Formulas.getTraqueXP(perso.get_lvl());
					perso.addXp(xp);
					perso.set_traque(null);//On supprime la traque
					GestorSalida.GAME_SEND_MESSAGE(perso, "Vous venez de recevoir "+xp+" points d'experiences." , "000000");
				}
				else
				{
					GestorSalida.GAME_SEND_MESSAGE(perso, "Thomas Sacre : Reviens me voir quand tu aura abatu un ennemi." , "000000");
				}

			break;
			case 100://Donner l'abilité 'args' à une dragodinde
                Dragopavo dragopavo = perso.getMount();
                Mundo.addDragopavo(new Dragopavo(
                 dragopavo.getID(),
                 dragopavo.getColor(),
                 dragopavo.getSexo(),
                 dragopavo.getAmor(),
                 dragopavo.get_endurance(),
                 dragopavo.get_level(),
                 dragopavo.get_exp(),
                 dragopavo.get_nom(),
                 dragopavo.get_fatigue(),
                 dragopavo.get_energie(),
                 dragopavo.get_reprod(),
                 dragopavo.get_maturite(),
                 dragopavo.get_serenite(),
                 dragopavo.getItemsId(),
                 dragopavo.getAncestros(), _argumento));
                 perso.setMount(Mundo.getDragopavoPorID(dragopavo.getID()));
                 GestorSalida.GAME_SEND_Re_PACKET(perso, "+", Mundo.getDragopavoPorID(dragopavo.getID()));
                 GestorSQL.actualizar_informacion_monturas(dragopavo);
                 break;

			case 101://Arriver sur case de mariage
				if((perso.getSexo() == 0 && perso.getActualCelda().getID() == 282) || (perso.getSexo() == 1 && perso.getActualCelda().getID() == 297)) {
					Mundo.AddMarried(perso.getSexo(), perso);
				}else {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1102");
				}
			break;

			case 102://Casamiento de 2 personajes
				Mundo.PriestRequest(perso, perso.getActualMapa(), perso.get_isTalkingWith());
			break;

			case 103://Divorsiarse
				if(perso.getKamas() < 50000) {
					return;
				}else {
					perso.setKamas(perso.getKamas()-50000);
					Personaje wife = Mundo.getPersonnage(perso.getWife());
					wife.Divorce();
					perso.Divorce();
				}
			break;

			case 104://Cliqueador
				try {
					int caracteristica = Integer.parseInt(_argumento);
					int valor = 0;
					int cantidad = 0;
					while(cantidad <= perso.get_capital()){
						switch (caracteristica) {
							//Fuerza
							case 10 -> valor = perso._baseStats.getEffect(Constantes.STATS_ADD_FORC);
							//Suerte
							case 13 -> valor = perso._baseStats.getEffect(Constantes.STATS_ADD_CHAN);
							//Agilidad
							case 14 -> valor = perso._baseStats.getEffect(Constantes.STATS_ADD_AGIL);
							//Inteligencia
							case 15 -> valor = perso._baseStats.getEffect(Constantes.STATS_ADD_INTE);
						}
						cantidad = Constantes.getReqPtsToBoostStatsByClass(perso.getClase(), caracteristica, valor);
						switch(caracteristica) {
							case 11://Vitalidad
								//Si es sacrogrito se modifica
								if(perso.getClase() != Constantes.CLASS_SACRIEUR)
									perso._baseStats.addOneStat(Constantes.STATS_ADD_VITA, 1);
								else
									perso._baseStats.addOneStat(Constantes.STATS_ADD_VITA, 2);
								break;
							case 12://Sabiduria
								perso._baseStats.addOneStat(Constantes.STATS_ADD_SAGE, 1);
								break;
							case 10://Fuerza
								perso._baseStats.addOneStat(Constantes.STATS_ADD_FORC, 1);
								break;
							case 13://Suerte
								perso._baseStats.addOneStat(Constantes.STATS_ADD_CHAN, 1);
								break;
							case 14://Agilidad
								perso._baseStats.addOneStat(Constantes.STATS_ADD_AGIL, 1);
								break;
							case 15://Inteligencia
								perso._baseStats.addOneStat(Constantes.STATS_ADD_INTE, 1);
								break;
							default:
								return;
						}
						perso.addPuntosDeCapital(-cantidad);
					}
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(perso);
					GestorSQL.guardar_personaje(perso, false);
				}catch(Exception e){JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;

			case 105://Teletransportar a todos los miembros del grupo
				Personaje.Grupo grupo = perso.getActualGrupo();
				//Verificamos que el jugador este en un grupo
				if (grupo == null) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1251;");
					return;
				}
				//Vemos la ID del mapa y celda del jugador a crear la accion
				short idmapa = perso.getActualMapa().getID();
				int idcelda = perso.getActualCelda().getID();
				//Listamos los miembros del grupo
				ArrayList<Personaje> miembros = perso.getActualGrupo().getMiembrosGrupo();
				for (Personaje personaje : miembros) {
					personaje.teletransportar(idmapa, idcelda);
				}
				break;

			case 228://Faire animation Hors Combat
				try
				{
					int AnimationId = Integer.parseInt(_argumento);
					Animaciones animation = Mundo.getAnimation(AnimationId);
					if(perso.getPelea() != null) return;
					perso.changeOrientation(1);
					GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(perso.getActualMapa(), "0", 228, perso.get_GUID()+";"+cellid+","+ Animaciones.PrepareToGA(animation), "");
				}catch(Exception e){
					JuegoServidor.agregar_a_los_logs(e.getMessage());}
				break;
			default:
				JuegoServidor.agregar_a_los_logs("Action ID="+ _id +" non implantee");
			break;
		}
	}


	public int get_id()
	{
		return _id;
	}
}
