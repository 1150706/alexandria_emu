package comunes;


import juego.JuegoServidor;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;

import objetos.*;

public class Condiciones {

	public static boolean ValidarCondicion(Personaje personaje, String requiere) {
		if(requiere == null || requiere.equals(""))return true;
		if(requiere.contains("BI"))return false;
		Jep jep = new Jep();
		requiere = requiere.replace("&", "&&").replace("=", "==").replace("|", "||").replace("!", "!=").replace("~", "==");
		
		if(requiere.contains("PO"))
			requiere = havePO(requiere, personaje);
		if(requiere.contains("PN"))
			requiere = canPN(requiere, personaje);
	 	//TODO : Gérer PJ Pj
		try {
				//Stats stuff compris
				jep.addVariable("CI", personaje.getTotalStats().getEffect(Constantes.STATS_ADD_INTE));
			 	jep.addVariable("CV", personaje.getTotalStats().getEffect(Constantes.STATS_ADD_VITA));
			 	jep.addVariable("CA", personaje.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL));
			 	jep.addVariable("CW", personaje.getTotalStats().getEffect(Constantes.STATS_ADD_SAGE));
			 	jep.addVariable("CC", personaje.getTotalStats().getEffect(Constantes.STATS_ADD_CHAN));
			 	jep.addVariable("CS", personaje.getTotalStats().getEffect(Constantes.STATS_ADD_FORC));
			 	//Stats de bases
			 	jep.addVariable("Ci", personaje.get_baseStats().getEffect(Constantes.STATS_ADD_INTE));
			 	jep.addVariable("Cs", personaje.get_baseStats().getEffect(Constantes.STATS_ADD_FORC));
			 	jep.addVariable("Cv", personaje.get_baseStats().getEffect(Constantes.STATS_ADD_VITA));
			 	jep.addVariable("Ca", personaje.get_baseStats().getEffect(Constantes.STATS_ADD_AGIL));
			 	jep.addVariable("Cw", personaje.get_baseStats().getEffect(Constantes.STATS_ADD_SAGE));
			 	jep.addVariable("Cc", personaje.get_baseStats().getEffect(Constantes.STATS_ADD_CHAN));
			 	//Autre
			 	jep.addVariable("Ps", personaje.get_align());
			 	jep.addVariable("Pa", personaje.getALvl());
			 	jep.addVariable("PP", personaje.getGrade());
			 	jep.addVariable("PL", personaje.get_lvl());
			 	jep.addVariable("PK", personaje.getKamas());
			 	jep.addVariable("PG", personaje.getClase());
			 	jep.addVariable("PS", personaje.getSexo());
			 	jep.addVariable("PZ", 1);//Abonado
			 	jep.addVariable("PX", personaje.getCuenta().getGMLVL());
			 	jep.addVariable("PW", personaje.getMaxPod());
			 	jep.addVariable("PB", personaje.getActualMapa().getSubArea().get_id());
			 	jep.addVariable("PR", (personaje.getWife()>0?1:0));
			 	jep.addVariable("SI", personaje.getActualMapa().getID());
			 	//Les pierres d'ames sont lancables uniquement par le lanceur.
			 	jep.addVariable("MiS",personaje.get_GUID());
			 	
			 	jep.parse(requiere);
			 	Object result = jep.evaluate();
			 	boolean ok = false;
			 	if(result != null)ok = Boolean.parseBoolean(result.toString());
			 	return ok;
		} catch (JepException e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
		return true;
	}
	
	public static String havePO(String cond, Personaje perso)//On remplace les PO par leurs valeurs si possession de l'item
	{
		boolean Jump = false;
		boolean ContainsPO = false;
		boolean CutFinalLenght = true;
		StringBuilder copyCond = new StringBuilder();
		int finalLength = 0;
		
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Entered Cond : "+cond);
		
		if(cond.contains("&&")) {
			for(String cur : cond.split("&&")) {
				if(cond.contains("==")) {
					for(String cur2 : cur.split("==")) {
						if(cur2.contains("PO")) {
							ContainsPO = true;
							continue;
						}
						if(Jump) {
							copyCond.append(cur2);
							Jump = false;
							continue;
						}
						if(!cur2.contains("PO") && !ContainsPO) {
							copyCond.append(cur2).append("==");
							Jump = true;
							continue;
						}
						if(cur2.contains("!=")) continue;
						ContainsPO = false;
						if(perso.hasItemTemplate(Integer.parseInt(cur2), 1)) {
							copyCond.append(Integer.parseInt(cur2)).append("==").append(Integer.parseInt(cur2));
						}else {
							copyCond.append(Integer.parseInt(cur2)).append("==").append(0);
						}
					}
				}
				if(cond.contains("!=")) {
					for(String cur2 : cur.split("!=")) {
						if(cur2.contains("PO")) {
							ContainsPO = true;
							continue;
						}
						if(Jump) {
							copyCond.append(cur2);
							Jump = false;
							continue;
						}
						if(!cur2.contains("PO") && !ContainsPO) {
							copyCond.append(cur2).append("!=");
							Jump = true;
							continue;
						}
						if(cur2.contains("==")) continue;
						ContainsPO = false;
						if(perso.hasItemTemplate(Integer.parseInt(cur2), 1)) {
							copyCond.append(Integer.parseInt(cur2)).append("!=").append(Integer.parseInt(cur2));
						}else {
							copyCond.append(Integer.parseInt(cur2)).append("!=").append(0);
						}
					}
				}
				copyCond.append("&&");
			}
		}else if(cond.contains("||")) {
			for(String cur : cond.split("\\|\\|")) {
				if(cond.contains("==")) {
					for(String cur2 : cur.split("==")) {
						if(cur2.contains("PO")) {
							ContainsPO = true;
							continue;
						}
						if(Jump) {
							copyCond.append(cur2);
							Jump = false;
							continue;
						}
						if(!cur2.contains("PO") && !ContainsPO) {
							copyCond.append(cur2).append("==");
							Jump = true;
							continue;
						}
						if(cur2.contains("!=")) continue;
						ContainsPO = false;
						if(perso.hasItemTemplate(Integer.parseInt(cur2), 1)) {
							copyCond.append(Integer.parseInt(cur2)).append("==").append(Integer.parseInt(cur2));
						}else {
							copyCond.append(Integer.parseInt(cur2)).append("==").append(0);
						}
					}
				}
				if(cond.contains("!=")) {
					for(String cur2 : cur.split("!=")) {
						if(cur2.contains("PO")) {
							ContainsPO = true;
							continue;
						}
						if(Jump) {
							copyCond.append(cur2);
							Jump = false;
							continue;
						}
						if(!cur2.contains("PO") && !ContainsPO) {
							copyCond.append(cur2).append("!=");
							Jump = true;
							continue;
						}
						if(cur2.contains("==")) continue;
						ContainsPO = false;
						if(perso.hasItemTemplate(Integer.parseInt(cur2), 1)) {
							copyCond.append(Integer.parseInt(cur2)).append("!=").append(Integer.parseInt(cur2));
						}else {
							copyCond.append(Integer.parseInt(cur2)).append("!=").append(0);
						}
					}
				}
					copyCond.append("||");
			}
		}else {
			CutFinalLenght = false;
			if(cond.contains("==")) {
				for(String cur : cond.split("==")) {
					if(cur.contains("PO")) {
						continue;
					}
					if(cur.contains("!=")) continue;
					if(perso.hasItemTemplate(Integer.parseInt(cur), 1)) {
						copyCond.append(Integer.parseInt(cur)).append("==").append(Integer.parseInt(cur));
					}else {
						copyCond.append(Integer.parseInt(cur)).append("==").append(0);
					}
				}
			}
			if(cond.contains("!=")) {
				for(String cur : cond.split("!=")) {
					if(cur.contains("PO")) {
						continue;
					}
					if(cur.contains("==")) continue;
					if(perso.hasItemTemplate(Integer.parseInt(cur), 1)) {
						copyCond.append(Integer.parseInt(cur)).append("!=").append(Integer.parseInt(cur));
					}else {
						copyCond.append(Integer.parseInt(cur)).append("!=").append(0);
					}
				}
			}
		}
		if(CutFinalLenght) {
			finalLength = (copyCond.length()-2);//On retire les deux derniers carractères (|| ou &&)
			copyCond = new StringBuilder(copyCond.substring(0, finalLength));
		}
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Returned Cond : "+copyCond);
		return copyCond.toString();
	}
	
	public static String canPN(String cond, Personaje perso)//On remplace le PN par 1 et si le nom correspond == 1 sinon == 0
	{
		StringBuilder copyCond = new StringBuilder();
		for(String cur : cond.split("==")) {
			if(cur.contains("PN")) {
				copyCond.append("1==");
				continue;
			}
			if(perso.getNombre().toLowerCase().compareTo(cur) == 0) {
				copyCond.append("1");
			}else {
				copyCond.append("0");
			}
		}
		return copyCond.toString();
	}
}