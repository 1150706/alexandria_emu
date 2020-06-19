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
import objetos.casas.House;

public class Accion {

	private final int ID;
	private final String args;
	private final String cond;
	
	public Accion(int id, String args, String cond)
	{
		this.ID = id;
		this.args = args;
		this.cond = cond;
	}


	public void apply(Personaje perso, Personaje target, int itemID, int cellid) {
		if(perso == null)return;
		if(!cond.equalsIgnoreCase("") && !cond.equalsIgnoreCase("-1")&& !Condiciones.ValidarCondicion(perso,cond)) {
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "119");
			return;
		}
		if(perso.get_compte().getGameThread() == null) return;
		PrintWriter out = perso.get_compte().getGameThread().get_out();	
		switch(ID) {

			case -2://Crear un gremio
				if(perso.is_away())return;
				if(perso.get_guild() != null || perso.getGuildMember() != null) {
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
					long nKamas = perso.get_kamas() - cost;
					//Si el jugador no tiene las suficientes kamas para abrir el banco
					if(nKamas <0){
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1128;"+cost);
						return;
					}
					perso.set_kamas(nKamas);
					GestorSalida.GAME_SEND_STATS_PACKET(perso);
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "020;"+cost);
				}
				GestorSalida.GAME_SEND_ECK_PACKET(perso.get_compte().getGameThread().get_out(), 5, "");
				GestorSalida.GAME_SEND_EL_BANK_PACKET(perso);
				perso.set_away(true);
				perso.setInBank(true);
			break;
			
			case 0://Teletransportacion
				try {
					short nuevomapa = Short.parseShort(args.split(",",2)[0]);
					int nuevacelda = Integer.parseInt(args.split(",",2)[1]);
					perso.teletransportar(nuevomapa,nuevacelda);
				}catch(Exception e ){return;}
				break;
			
			case 1://Discusion con un NPC
				out = perso.get_compte().getGameThread().get_out();
				if(args.equalsIgnoreCase("DV")) {
					GestorSalida.GAME_SEND_END_DIALOG_PACKET(out);
					perso.set_isTalkingWith(0);
				}else {
					int qID = -1;
					try {
						qID = Integer.parseInt(args);
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
					int count = Integer.parseInt(args);
					long curKamas = perso.get_kamas();
					long newKamas = curKamas + count;
					if(newKamas <0) newKamas = 0;
					perso.set_kamas(newKamas);
					//Si en ligne (normalement oui)
					if(perso.isOnline())
						GestorSalida.GAME_SEND_STATS_PACKET(perso);
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;

			case 5://Objeto
				try {
					int tID = Integer.parseInt(args.split(",")[0]);
					int count = Integer.parseInt(args.split(",")[1]);
					boolean send = true;
					if(args.split(",").length >2)send = args.split(",")[2].equals("1");
					
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
					if(perso.isOnline())//on envoie le packet qui indique l'ajout//retrait d'un item
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
					JuegoServidor.addToLog(e.getMessage());}
				break;

			case 6://Aprender un oficio
				try {
					int oficioid = Integer.parseInt(args);
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
						if(perso.getMetierByID(oficioid) != null)//Métier déjà appris
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
						if(perso.getMetierByID(17) != null && perso.getMetierByID(17).get_lvl() >= 65 && oficioid == 43
						|| perso.getMetierByID(11) != null && perso.getMetierByID(11).get_lvl() >= 65 && oficioid == 44
						|| perso.getMetierByID(14) != null && perso.getMetierByID(14).get_lvl() >= 65 && oficioid == 45
						|| perso.getMetierByID(20) != null && perso.getMetierByID(20).get_lvl() >= 65 && oficioid == 46
						|| perso.getMetierByID(31) != null && perso.getMetierByID(31).get_lvl() >= 65 && oficioid == 47
						|| perso.getMetierByID(13) != null && perso.getMetierByID(13).get_lvl() >= 65 && oficioid == 48
						|| perso.getMetierByID(19) != null && perso.getMetierByID(19).get_lvl() >= 65 && oficioid == 49
						|| perso.getMetierByID(18) != null && perso.getMetierByID(18).get_lvl() >= 65 && oficioid == 50
						|| perso.getMetierByID(15) != null && perso.getMetierByID(15).get_lvl() >= 65 && oficioid == 62
						|| perso.getMetierByID(16) != null && perso.getMetierByID(16).get_lvl() >= 65 && oficioid == 63
						|| perso.getMetierByID(27) != null && perso.getMetierByID(27).get_lvl() >= 65 && oficioid == 64) {
							//On compte les specialisations déja acquis si c'est supérieur a 2 on ignore
							if(perso.getMetierByID(oficioid) != null)//Métier déjà appris
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
								perso.getMetierByID(oficioid).addXp(perso, 582000);//Level 100 direct
							}	
						}else {
							GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "12");
						}
					}
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;

			case 7://Devolver al punto de guardado
				perso.warpToSavePos();
			break;

			case 8://Ajustar las estadisticas
		            int statID = Integer.parseInt(args.split(",", 2)[0]);
		            int number = Integer.parseInt(args.split(",", 2)[1]);
		            perso.get_baseStats().addOneStat(statID, number);
		            GestorSalida.GAME_SEND_STATS_PACKET(perso);
		            int messID = 0;
				if (statID == 126) { // '~'
					messID = 14;
				}
		            if(messID > 0)
		                GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "0" + messID + ";" + number);
		            return;

			case 9://Aprender un hechizo
				try {
					int sID = Integer.parseInt(args);
					if(Mundo.getSort(sID) == null)return;
					perso.learnSpell(sID,1, true,true);
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;

			case 10://Pain/potion/viande/poisson
				try {
					int min = Integer.parseInt(args.split(",",2)[0]);
					int max = Integer.parseInt(args.split(",",2)[1]);
					if(max == 0) max = min;
					int val = Formulas.getRandomValue(min, max);
					if(target != null) {
						if(target.get_PDV() + val > target.get_PDVMAX())val = target.get_PDVMAX()-target.get_PDV();
						target.set_PDV(target.get_PDV()+val);
						GestorSalida.GAME_SEND_STATS_PACKET(target);
					} else {
						if(perso.get_PDV() + val > perso.get_PDVMAX())val = perso.get_PDVMAX()-perso.get_PDV();
						perso.set_PDV(perso.get_PDV()+val);
						GestorSalida.GAME_SEND_STATS_PACKET(perso);
					}
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;

			case 11://Definir la alineacion
				try {
					byte newAlign = Byte.parseByte(args.split(",",2)[0]);
					boolean replace = Integer.parseInt(args.split(",",2)[1]) == 1;
					//Si le perso n'est pas neutre, et qu'on doit pas remplacer, on passe
					if(perso.get_align() != Constantes.ALIGNEMENT_NEUTRE && !replace)return;
					perso.modifAlignement(newAlign);
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;

			case 12://Refrescar un grupo de monstruos
				try {
					boolean delObj = args.split(",")[0].equals("true");
					boolean inArena = args.split(",")[1].equals("true");
					if(inArena && !Mundo.isArenaMap(perso.getActualMapa().get_id()))return;	//Si la map du personnage n'est pas classé comme étant dans l'arène
					PiedraAlma pierrePleine = (PiedraAlma) Mundo.getObjet(itemID);
					String groupData = pierrePleine.parseGroupData();
					String condition = "MiS = "+perso.get_GUID();	//Condition pour que le groupe ne soit lançable que par le personnage qui à utiliser l'objet
					perso.getActualMapa().spawnNewGroup(true, perso.getActualCelda().getID(), groupData,condition);
					if(delObj) {
						perso.removeItem(itemID, 1, true, true);
					}
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;

		    case 13://Reiniciar caracteristicas
		        try {
		          perso.get_baseStats().addOneStat(125, -perso._baseStats.getEffect(125));
		          perso.get_baseStats().addOneStat(124, -perso._baseStats.getEffect(124));
		          perso.get_baseStats().addOneStat(118, -perso._baseStats.getEffect(118));
		          perso.get_baseStats().addOneStat(123, -perso._baseStats.getEffect(123));
		          perso.get_baseStats().addOneStat(119, -perso._baseStats.getEffect(119));
		          perso.get_baseStats().addOneStat(126, -perso._baseStats.getEffect(126));
		          perso.addCapital((perso.get_lvl() - 1) * 5 - perso.get_capital());
		          GestorSalida.GAME_SEND_STATS_PACKET(perso);
		        }catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;

		    case 14://Ouvrir l'interface d'oublie de sort
		    	perso.setisForgetingSpell(true);
				GestorSalida.GAME_SEND_FORGETSPELL_INTERFACE('+', perso);
			break;
			case 15://Téléportation donjon
				try
				{
					short newMapID = Short.parseShort(args.split(",")[0]);
					int newCellID = Integer.parseInt(args.split(",")[1]);
					int ObjetNeed = Integer.parseInt(args.split(",")[2]);
					int MapNeed = Integer.parseInt(args.split(",")[3]);
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
					if (perso.hasItemTemplate(ObjetNeed, 1) && perso.getActualMapa().get_id() == MapNeed)
					{
						//Le perso a l'item
						//Le perso est sur la bonne map
						//On téléporte, on supprime après
						perso.teletransportar(newMapID,newCellID);
						perso.removeByTemplateID(ObjetNeed, 1);
						GestorSalida.GAME_SEND_Ow_PACKET(perso);
					}
					else if(perso.getActualMapa().get_id() != MapNeed)
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
					JuegoServidor.addToLog(e.getMessage());}
				break;
			case 16://Ajout d'honneur HonorValue
				try
				{
					if(perso.get_align() != 0)
					{
						int AddHonor = Integer.parseInt(args);
						int ActualHonor = perso.get_honor();
						perso.set_honor(ActualHonor+AddHonor);
					}
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;
			case 17://Xp métier JobID,XpValue
				try
				{
					int JobID = Integer.parseInt(args.split(",")[0]);
					int XpValue = Integer.parseInt(args.split(",")[1]);
					if(perso.getMetierByID(JobID) != null)
					{
						perso.getMetierByID(JobID).addXp(perso, XpValue);
					}
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;
			case 18://Téléportation chez sois
				if(House.AlreadyHaveHouse(perso))//Si il a une maison
				{
					Objeto obj = Mundo.getObjet(itemID);
					if (perso.hasItemTemplate(obj.getTemplate().getID(), 1))
					{
						perso.removeByTemplateID(obj.getTemplate().getID(),1);
						House h = House.get_HouseByPerso(perso);
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
					int pts = Integer.parseInt(args);
					if(pts < 1) return;
					perso.addSpellPoint(pts);
					GestorSalida.GAME_SEND_STATS_PACKET(perso);
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;
			case 21://+Energie
				try
				{
					int Energy = Integer.parseInt(args);
					if(Energy < 1) return;
					
					int EnergyTotal = perso.get_energy()+Energy;
					if(EnergyTotal > 10000) EnergyTotal = 10000;
					
					perso.set_energy(EnergyTotal);
					GestorSalida.GAME_SEND_STATS_PACKET(perso);
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;
			case 22://+Xp
				try
				{
					long XpAdd = Integer.parseInt(args);
					if(XpAdd < 1) return;
					
					long TotalXp = perso.get_curExp()+XpAdd;
					perso.set_curExp(TotalXp);
					GestorSalida.GAME_SEND_STATS_PACKET(perso);
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;
			case 23://UnlearnJob
				try
				{
					int Job = Integer.parseInt(args);
					if(Job < 1) return;
					StatsMetier m = perso.getMetierByID(Job);
					if(m == null) return;
					perso.unlearnJob(m.getID());
					GestorSalida.GAME_SEND_STATS_PACKET(perso);
					GestorSQL.guardar_personaje(perso, false);
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;
			case 24://SimpleMorph
				try
				{
					int morphID = Integer.parseInt(args);
					if(morphID < 0)return;
					perso.set_gfxID(morphID);
					GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getActualMapa(), perso.get_GUID());
					GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getActualMapa(), perso);
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;
			case 25://SimpleUnMorph
				int UnMorphID = perso.get_classe()*10 + perso.get_sexe();
				perso.set_gfxID(UnMorphID);
				GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getActualMapa(), perso.get_GUID());
				GestorSalida.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getActualMapa(), perso);
			break;
			case 26://Téléportation enclo de guilde (ouverture du panneau de guilde)
				GestorSalida.GAME_SEND_GUILDENCLO_PACKET(perso);
			break;
			case 27://startFigthVersusMonstres args : monsterID,monsterLevel| ...
				String ValidMobGroup = "";
				try
		        {
					for(String MobAndLevel : args.split("\\|"))
					{
						int monsterID = -1;
						int monsterLevel = -1;
						String[] MobOrLevel = MobAndLevel.split(",");
						monsterID = Integer.parseInt(MobOrLevel[0]);
						monsterLevel = Integer.parseInt(MobOrLevel[1]);
						
						if(Mundo.getMonstre(monsterID) == null || Mundo.getMonstre(monsterID).getGradeByLevel(monsterLevel) == null)
						{
							if(MainServidor.CONFIG_DEBUG) JuegoServidor.addToLog("Monstre invalide : monsterID:"+monsterID+" monsterLevel:"+monsterLevel);
							continue;
						}
						ValidMobGroup += monsterID+","+monsterLevel+","+monsterLevel+";";
					}
					if(ValidMobGroup.isEmpty()) return;
					MobGroup group  = new MobGroup(perso.getActualMapa()._nextObjectID,perso.getActualCelda().getID(),ValidMobGroup);
					perso.getActualMapa().startFigthVersusMonstres(perso, group);
		        }catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
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
					if(P.get_compte().get_curIP().compareTo(perso.get_compte().get_curIP()) == 0)continue;
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
					
					
					GestorSalida.GAME_SEND_MESSAGE(perso, "Vous etes desormais en chasse de "+tempP.get_name()+"." , "000000");
					
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
					
					newObj.addTxtStat(989, tempP.get_name());
					
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
				Personaje cible = Mundo.getPersoByName(perr);
				if(cible==null)break;
				if(!cible.isOnline())
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
                 dragopavo.getAncestros(), args));
                 perso.setMount(Mundo.getDragoByID(dragopavo.getID()));
                 GestorSalida.GAME_SEND_Re_PACKET(perso, "+", Mundo.getDragoByID(dragopavo.getID()));
                 GestorSQL.actualizar_informacion_monturas(dragopavo);
                 break;

			case 101://Arriver sur case de mariage
				if((perso.get_sexe() == 0 && perso.getActualCelda().getID() == 282) || (perso.get_sexe() == 1 && perso.getActualCelda().getID() == 297)) {
					Mundo.AddMarried(perso.get_sexe(), perso);
				}else {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1102");
				}
			break;

			case 102://Casamiento de 2 personajes
				Mundo.PriestRequest(perso, perso.getActualMapa(), perso.get_isTalkingWith());
			break;

			case 103://Divorsiarse
				if(perso.get_kamas() < 50000) {
					return;
				}else {
					perso.set_kamas(perso.get_kamas()-50000);
					Personaje wife = Mundo.getPersonnage(perso.getWife());
					wife.Divorce();
					perso.Divorce();
				}
			break;

			case 104://Cliqueador
				try {
					int caracteristica = Integer.parseInt(args);
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
						cantidad = Constantes.getReqPtsToBoostStatsByClass(perso.get_classe(), caracteristica, valor);
						switch(caracteristica) {
							case 11://Vitalidad
								//Si es sacrogrito se modifica
								if(perso.get_classe() != Constantes.CLASS_SACRIEUR)
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
						perso.addCapital(-cantidad);
					}
					GestorSalida.GAME_SEND_STATS_PACKET(perso);
					GestorSQL.guardar_personaje(perso, false);
				}catch(Exception e){JuegoServidor.addToLog(e.getMessage());}
				break;

			case 105://Teletransportar a todos los miembros del grupo
				Personaje.Grupo grupo = perso.getActualGrupo();
				//Verificamos que el jugador este en un grupo
				if (grupo == null) {
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1251;");
					return;
				}
				//Vemos la ID del mapa y celda del jugador a crear la accion
				short idmapa = perso.getActualMapa().get_id();
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
					int AnimationId = Integer.parseInt(args);
					Animaciones animation = Mundo.getAnimation(AnimationId);
					if(perso.get_fight() != null) return;
					perso.changeOrientation(1);
					GestorSalida.GAME_SEND_GA_PACKET_TO_MAP(perso.getActualMapa(), "0", 228, perso.get_GUID()+";"+cellid+","+ Animaciones.PrepareToGA(animation), "");
				}catch(Exception e){
					JuegoServidor.addToLog(e.getMessage());}
				break;
			default:
				JuegoServidor.addToLog("Action ID="+ID+" non implantee");
			break;
		}
	}


	public int getID()
	{
		return ID;
	}
}
