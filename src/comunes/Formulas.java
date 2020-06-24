package comunes;


import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import comunes.Mundo.Doble;

import objetos.*;
import objetos.Pelea.*;
import objetos.Gremio.GuildMember;
import objetos.hechizos.EfectoHechizo;

public class Formulas {


	public static int getRandomValue(int i1,int i2)
	{
		Random rand = new Random();
		return (rand.nextInt((i2-i1)+1))+i1;
	}
	
	public static int getRandomJet(String jet)//1d5+6
	{
		try
		{
			int num = 0;
			int des = Integer.parseInt(jet.split("d")[0]);
			int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
			int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
			for(int a=0;a<des;a++)
			{
				num += getRandomValue(1,faces);
			}
			num += add;
			return num;
		}catch(NumberFormatException e){return -1;}
	}
	public static int getMiddleJet(String jet)//1d5+6
	{
		try
		{
			int num = 0;
			int des = Integer.parseInt(jet.split("d")[0]);
			int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
			int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
			num += ((1+faces)/2)*des;//on calcule moyenne
			num += add;
			return num;
		}catch(NumberFormatException e){return 0;}
	}
	public static int getTacleChance(Peleador tacleur, ArrayList<Peleador> tacle)
	{
		int agiTR = tacleur.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
		int agiT = 0;
		for(Peleador T : tacle)
		{
			agiT += T.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
		}
		int a = agiTR+25;
		int b = agiTR+agiT+50;
		int chance = (int)((long)(300*a/b)-100);
		if(chance <10)chance = 10;
		if(chance >90)chance = 90;
		return chance;
	}
	
	  public static long XPdefie(Peleador perso, ArrayList<Peleador> winners, ArrayList<Peleador> looser)      {
          if(perso.getPersonnage()== null)return 0;
          if(winners.contains(perso.getID()))//Si winner
          {
                  int lvlLoosers = 0; // on initialise la variable lvlLoosers
                  for(Peleador entry : looser)
                          lvlLoosers += entry.get_lvl(); // on r�cupere le level du perdant
          
                  int lvlWinners = 0; // pareil pour les gagnants
                  for(Peleador entry : winners)
                          lvlWinners += entry.get_lvl();

//Ici on calcule l'xp gagn� en fonction du quotient des deux levels et du taux d�finit dans votre config pour l'xp Pvp ainsi que l'xp qu'il reste � gagner avant le prochain level. (En gros l'xp gagn�e = lvl du perdant/lvl du gagnant * l'xp qu'il reste avant de up / 100 * la rate xp pvp)
                  int taux = MainServidor.XP_PVP;
                  float rapport = (float)lvlLoosers/(float)lvlWinners;
                  long xpWin = (long)(
                                          (
                                                  rapport
                                          *       getXpNeededAtLevel(perso.getPersonnage().get_lvl())
                                          /       100
                                          )
                                          *       taux
                                  );
                  //DEBUG
                  System.out.println("Taux: "+taux);
                  System.out.println("Rapport: "+rapport);
                  System.out.println("XpNeeded: "+getXpNeededAtLevel(perso.getPersonnage().get_lvl()));
                  System.out.println("xpWin: "+xpWin);
                  //*/
                  return xpWin; // et on r�cup�re l'xp gagn�e pour le gagnant apr�s notre calcul judicieux :P
          }
          return 0; //Si perdant pas de xp gagn�
  }

	public static int calculFinalHeal(Personaje caster, int jet)
	{
		int statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
		int soins = caster.getTotalStats().getEffect(Constantes.STATS_ADD_SOIN);
		if(statC<0)statC=0;
		return jet * (100 + statC) / 100 + soins;
	}
	
	public static int calculFinalDommage(Pelea fight, Peleador caster, Peleador target, int statID, int jet, boolean isHeal, boolean isCaC, int spellid)
	{
		float i = 0;//Bonus maitrise
		float j = 100; //Bonus de Classe
		float a = 1;//Calcul
		float num = 0;
		float statC = 0, domC = 0, perdomC = 0, resfT = 0, respT = 0;
		int multiplier = 0;
		if(!isHeal)
		{
			domC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_DOMA);
			perdomC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_PERDOM);
			multiplier = caster.getTotalStats().getEffect(Constantes.STATS_MULTIPLY_DOMMAGE);
		}else
		{
			domC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_SOIN);
		}

		//on ajoute les dom Physique
		//Ajout de la resist Physique
		//on ajout les dom Physique
		//Ajout de la resist Physique
		//Ajout de la resist Magique
		//Ajout de la resist Magique
		//Ajout de la resist Magique
		switch (statID) {
//Fixe
			case Constantes.ELEMENT_NULL -> {
				statC = 0;
				resfT = 0;
				respT = 0;
				respT = 0;
			}
//neutre
			case Constantes.ELEMENT_NEUTRE -> {
				statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
				resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_NEU);
				respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU);
				if (caster.getPersonnage() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_NEU);
					resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_NEU);
				}
				domC += caster.getTotalStats().getEffect(142);
				resfT = target.getTotalStats().getEffect(184);
			}
//force
			case Constantes.ELEMENT_TERRE -> {
				statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
				resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_TER);
				respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_TER);
				if (caster.getPersonnage() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_TER);
					resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_TER);
				}
				domC += caster.getTotalStats().getEffect(142);
				resfT = target.getTotalStats().getEffect(184);
			}
//chance
			case Constantes.ELEMENT_EAU -> {
				statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
				resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_EAU);
				respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_EAU);
				if (caster.getPersonnage() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_EAU);
					resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_EAU);
				}
				resfT = target.getTotalStats().getEffect(183);
			}
//intell
			case Constantes.ELEMENT_FEU -> {
				statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
				resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_FEU);
				respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_FEU);
				if (caster.getPersonnage() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_FEU);
					resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_FEU);
				}
				resfT = target.getTotalStats().getEffect(183);
			}
//agilit�
			case Constantes.ELEMENT_AIR -> {
				statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
				resfT = target.getTotalStats().getEffect(Constantes.STATS_ADD_R_AIR);
				respT = target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_AIR);
				if (caster.getPersonnage() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constantes.STATS_ADD_RP_PVP_AIR);
					resfT += target.getTotalStats().getEffect(Constantes.STATS_ADD_R_PVP_AIR);
				}
				resfT = target.getTotalStats().getEffect(183);
			}
		}
		//On bride la resistance a 50% si c'est un joueur 
		if(target.getMob() == null && respT >50)respT = 50;
		
		if(statC<0)statC=0;
		/* DEBUG
		System.out.println("Jet: "+jet+" Stats: "+statC+" perdomC: "+perdomC+" multiplier: "+multiplier);
		System.out.println("(100 + statC + perdomC)= "+(100 + statC + perdomC));
		System.out.println("(jet * (100 + statC + perdomC + (multiplier*100) ) / 100)= "+(jet * ((100 + statC + perdomC) / 100 )));
		System.out.println("res Fix. T "+ resfT);
		System.out.println("res %age T "+respT);
		if(target.getMob() != null)
		{
			System.out.println("resmonstre: "+target.getMob().getStats().getEffect(Constants.STATS_ADD_RP_FEU));
			System.out.println("TotalStat: "+target.getTotalStats().getEffect(Constants.STATS_ADD_RP_FEU));
			System.out.println("FightStat: "+target.getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_RP_FEU));
			
		}
		//*/
			if(caster.getPersonnage() != null && isCaC)
			{
			int ArmeType = caster.getPersonnage().getObjetByPos(1).getTemplate().getType();
			
			if((caster.getSpellValueBool(392) == true) && ArmeType == 2)//ARC
			{
				i = caster.getMaitriseDmg(392);
			}
			if((caster.getSpellValueBool(390) == true) && ArmeType == 4)//BATON
			{
				i = caster.getMaitriseDmg(390);
			}
			if((caster.getSpellValueBool(391) == true) && ArmeType == 6)//EPEE
			{
				i = caster.getMaitriseDmg(391);
			}
			if((caster.getSpellValueBool(393) == true) && ArmeType == 7)//MARTEAUX
			{
				i = caster.getMaitriseDmg(393);
			}
			if((caster.getSpellValueBool(394) == true) && ArmeType == 3)//BAGUETTE
			{
				i = caster.getMaitriseDmg(394);
			}
			if((caster.getSpellValueBool(395) == true) && ArmeType == 5)//DAGUES
			{
				i = caster.getMaitriseDmg(395);
			}
			if((caster.getSpellValueBool(396) == true) && ArmeType == 8)//PELLE
			{
				i = caster.getMaitriseDmg(396);
			}
			if((caster.getSpellValueBool(397) == true) && ArmeType == 19)//HACHE
			{
				i = caster.getMaitriseDmg(397);
			}
				a = (((100+i)/100)*(j/100));
			}
			
			num = a*(jet * ((100 + statC + perdomC + (multiplier*100)) / 100 ))+ domC;//d�gats bruts
			
		//Poisons
		if(spellid != -1)
		{
			/*
			 * case [SPELLID]:
			 * statC = caster.getTotalStats().getEffect([EFFECT])
			 * num = (jet * ((100 + statC + perdomC + (multiplier*100)) / 100 ))+ domC;
			 * return (int) num;
			 */
			switch (spellid) {
				case 66 -> {
					statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
					num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
					if (target.hasBuff(105)) {
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getID() + "", target.getID() + "," + target.getBuff(105).getValue());
						return 0;
					}
					if (target.hasBuff(184)) {
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getID() + "", target.getID() + "," + target.getBuff(184).getValue());
						return 0;
					}
					return (int) num;
				}
				case 71, 196, 219 -> {
					statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
					num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
					if (target.hasBuff(105)) {
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getID() + "", target.getID() + "," + target.getBuff(105).getValue());
						return 0;
					}
					if (target.hasBuff(184)) {
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getID() + "", target.getID() + "," + target.getBuff(184).getValue());
						return 0;
					}
					return (int) num;
				}
				case 181, 200 -> {
					statC = caster.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
					num = (jet * ((100 + statC + perdomC + (multiplier * 100)) / 100)) + domC;
					if (target.hasBuff(105)) {
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getID() + "", target.getID() + "," + target.getBuff(105).getValue());
						return 0;
					}
					if (target.hasBuff(184)) {
						GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getID() + "", target.getID() + "," + target.getBuff(184).getValue());
						return 0;
					}
					return (int) num;
				}
			}
		}
		//Renvoie
		int renvoie = target.getTotalStatsLessBuff().getEffect(Constantes.STATS_RETDOM);
		if(renvoie >0 && !isHeal)
		{
			if(renvoie > num)renvoie = (int)num;
			num -= renvoie;
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 107, "-1", target.getID()+","+renvoie);
			if(renvoie>caster.getPDV())renvoie = caster.getPDV();
			if(num<1)num =0;
			caster.removePDV(renvoie);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getID()+"", caster.getID()+",-"+renvoie);
		}
		
		if(!isHeal)num -= resfT;//resis fixe
		int reduc =	(int)((num/(float)100)*respT);//Reduc %resis
		if(!isHeal)num -= reduc;
		
		int armor = getArmorResist(target,statID);
		if(!isHeal)num -= armor;
		if(!isHeal)if(armor > 0) GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getID()+"", target.getID()+","+armor);
		//d�gats finaux
		if(num < 1)num=0;
		
		// D�but Formule pour les MOBs
		if(caster.getPersonnage() == null && !caster.isPerco())
		{
			if(caster.getMob().getTemplate().getID() == 116)//Sacrifi� Dommage = PDV*2
			{
				return (int)((num/25)*caster.getPDVMAX());
			}else
			{
			int niveauMob = caster.get_lvl();
			double CalculCoef = ((niveauMob*0.5)/100);
			int Multiplicateur = (int) Math.ceil(CalculCoef);
			return (int)num*Multiplicateur;
			}
		}
		// Fin Formule pour les MOBs
		else
		{
			return (int)num;
		}
	}

	public static int calculZaapCost(Mapa map1, Mapa map2)
	{
		return 10*(Math.abs(map2.getX()-map1.getX())+Math.abs(map2.getY()-map1.getY())-1);
	}
	private static int getArmorResist(Peleador target, int statID)
	{
		int armor = 0;
		for(EfectoHechizo SE : target.getBuffsByEffectID(265))
		{
			Peleador fighter;

			//Si pas element feu, on ignore l'armure
			//Les stats du f�ca sont prises en compte
			//Si pas element terre/neutre, on ignore l'armure
			//Les stats du f�ca sont prises en compte
			//Si pas element air, on ignore l'armure
			//Les stats du f�ca sont prises en compte
			//Si pas element eau, on ignore l'armure
			//Les stats du f�ca sont prises en compte
			switch (SE.getHechizo()) {
//Armure incandescente
				case 1 -> {
					if (statID != Constantes.ELEMENT_FEU) continue;
					fighter = SE.getLanzador();
				}
//Armure Terrestre
				case 6 -> {
					if (statID != Constantes.ELEMENT_TERRE && statID != Constantes.ELEMENT_NEUTRE) continue;
					fighter = SE.getLanzador();
				}
//Armure Venteuse
				case 14 -> {
					if (statID != Constantes.ELEMENT_AIR) continue;
					fighter = SE.getLanzador();
				}
//Armure aqueuse
				case 18 -> {
					if (statID != Constantes.ELEMENT_EAU) continue;
					fighter = SE.getLanzador();
				}
//Dans les autres cas on prend les stats de la cible et on ignore l'element de l'attaque
				default -> fighter = target;
			}
			int intell = fighter.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
			int carac = switch (statID) {
				case Constantes.ELEMENT_AIR -> fighter.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
				case Constantes.ELEMENT_FEU -> fighter.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
				case Constantes.ELEMENT_EAU -> fighter.getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
				case Constantes.ELEMENT_NEUTRE, Constantes.ELEMENT_TERRE -> fighter.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
				default -> 0;
			};
			int value = SE.getValue();
			int a = value * (100 + (intell/2) + (carac/2))/100;
			armor += a;
		}
		for(EfectoHechizo SE : target.getBuffsByEffectID(105))
		{
			int intell = target.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
			int carac = switch (statID) {
				case Constantes.ELEMENT_AIR -> target.getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
				case Constantes.ELEMENT_FEU -> target.getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
				case Constantes.ELEMENT_EAU -> target.getTotalStats().getEffect(Constantes.STATS_ADD_CHAN);
				case Constantes.ELEMENT_NEUTRE, Constantes.ELEMENT_TERRE -> target.getTotalStats().getEffect(Constantes.STATS_ADD_FORC);
				default -> 0;
			};
			int value = SE.getValue();
			int a = value * (100 + (intell/2) + (carac/2))/100;
			armor += a;
		}
		return armor;
	}

	public static int getPointsLost(char z, int value, Peleador caster, Peleador target)
	{
		float esquiveC = z=='a'?caster.getTotalStats().getEffect(Constantes.STATS_ADD_AFLEE):caster.getTotalStats().getEffect(Constantes.STATS_ADD_MFLEE);
		float esquiveT = z=='a'?target.getTotalStats().getEffect(Constantes.STATS_ADD_AFLEE):target.getTotalStats().getEffect(Constantes.STATS_ADD_MFLEE);
		float ptsMax = z=='a'?target.getTotalStatsLessBuff().getEffect(Constantes.STATS_ADD_PA):target.getTotalStatsLessBuff().getEffect(Constantes.STATS_ADD_PM);
		
		int retrait = 0;

		for(int i = 0; i < value;i++)
		{
			if(ptsMax == 0 && target.getMob() != null)
			{
				ptsMax= z=='a'?target.getMob().getPA():target.getMob().getPM();
			}
			
			float pts = z =='a'?target.getPA():target.getPM();
			float ptsAct = pts - retrait;
			
			if(esquiveT == 0)esquiveT=1;
			if(esquiveC == 0)esquiveC=1;

			float a = esquiveC/esquiveT;
			float b = (ptsAct/ptsMax);

			float pourcentage = a*b*50;
			int chance = (int)Math.ceil(pourcentage);
			
			/*
			System.out.println("Esquive % : "+a+" Facteur PA/PM : "+b);
			System.out.println("ptsMax : "+ptsMax+" ptsAct : "+ptsAct);
			System.out.println("Chance d'esquiver le "+(i+1)+" eme PA/PM : "+chance);
			*/
			
			if(chance <0)chance = 0;
			if(chance >100)chance = 100;

			int jet = getRandomValue(0, 99);
			if(jet<chance)
			{
				retrait++;
			}
		}
		return retrait;
	}
	
	public static long getXpWinPerco(Recaudador perco, ArrayList<Peleador> winners, ArrayList<Peleador> loosers, long groupXP)
	{
			Gremio G = Mundo.getGuild(perco.get_guildID());
			float sag = G.get_Stats(Constantes.STATS_ADD_SAGE);
			float coef = (sag + 100)/100;
			int taux = MainServidor.XP_PVM;
			long xpWin = 0;
			int lvlmax = 0;
			for(Peleador entry : winners)
			{
				if(entry.get_lvl() > lvlmax)
					lvlmax = entry.get_lvl();
			}
			int nbbonus = 0;
			for(Peleador entry : winners)
			{
				if(entry.get_lvl() > (lvlmax / 3))
					nbbonus += 1;				
			}
			
			double bonus = 1;
			if(nbbonus == 2)
				bonus = 1.1;
			if(nbbonus == 3)
				bonus = 1.3;
			if(nbbonus == 4)
				bonus = 2.2;
			if(nbbonus == 5)
				bonus = 2.5;
			if(nbbonus == 6)
				bonus = 2.8;
			if(nbbonus == 7)
				bonus = 3.1;
			if(nbbonus >= 8)
				bonus = 3.5;
			
			int lvlLoosers = 0;
			for(Peleador entry : loosers)
				lvlLoosers += entry.get_lvl();
			int lvlWinners = 0;
			for(Peleador entry : winners)
				lvlWinners += entry.get_lvl();
			double rapport = 1+((double)lvlLoosers/(double)lvlWinners);
			if (rapport <= 1.3)
				rapport = 1.3;
			/*
			if (rapport > 5)
				rapport = 5;
			//*/
			int lvl = G.get_lvl();
			double rapport2 = 1 + ((double)lvl / (double)lvlWinners);

			xpWin = (long) (groupXP * rapport * bonus * taux *coef * rapport2);
			
			/*/ DEBUG XP
			System.out.println("=========");
			System.out.println("groupXP: "+groupXP);
			System.out.println("rapport1: "+rapport);
			System.out.println("bonus: "+bonus);
			System.out.println("taux: "+taux);
			System.out.println("coef: "+coef);
			System.out.println("rapport2: "+rapport2);
			System.out.println("xpWin: "+xpWin);
			System.out.println("=========");
			//*/
			return xpWin;	
	}
	
	public static long getXpWinPvm2(Peleador perso, ArrayList<Peleador> winners, ArrayList<Peleador> loosers, long groupXP)
	{
		if(perso.getPersonnage()== null)return 0;
		if(winners.contains(perso))//Si winner
		{
			float sag = perso.getTotalStats().getEffect(Constantes.STATS_ADD_SAGE);
			float coef = (sag + 100)/100;
			int taux = MainServidor.XP_PVM;
			long xpWin = 0;
			int lvlmax = 0;
			for(Peleador entry : winners)
			{
				if(entry.get_lvl() > lvlmax)
					lvlmax = entry.get_lvl();
			}
			int nbbonus = 0;
			for(Peleador entry : winners)
			{
				if(entry.get_lvl() > (lvlmax / 3))
					nbbonus += 1;				
			}
			
			double bonus = 1;
			if(nbbonus == 2)
				bonus = 1.1;
			if(nbbonus == 3)
				bonus = 1.3;
			if(nbbonus == 4)
				bonus = 2.2;
			if(nbbonus == 5)
				bonus = 2.5;
			if(nbbonus == 6)
				bonus = 2.8;
			if(nbbonus == 7)
				bonus = 3.1;
			if(nbbonus >= 8)
				bonus = 3.5;
			
			int lvlLoosers = 0;
			for(Peleador entry : loosers)
				lvlLoosers += entry.get_lvl();
			int lvlWinners = 0;
			for(Peleador entry : winners)
				lvlWinners += entry.get_lvl();
			double rapport = 1+((double)lvlLoosers/(double)lvlWinners);
			if (rapport <= 1.3)
				rapport = 1.3;
			/*
			if (rapport > 5)
				rapport = 5;
			//*/
			int lvl = perso.get_lvl();
			double rapport2 = 1 + ((double)lvl / (double)lvlWinners);

			xpWin = (long) (groupXP * rapport * bonus * taux *coef * rapport2);
			
			/*/ DEBUG XP
			System.out.println("=========");
			System.out.println("groupXP: "+groupXP);
			System.out.println("rapport1: "+rapport);
			System.out.println("bonus: "+bonus);
			System.out.println("taux: "+taux);
			System.out.println("coef: "+coef);
			System.out.println("rapport2: "+rapport2);
			System.out.println("xpWin: "+xpWin);
			System.out.println("=========");
			//*/
			return xpWin;	
		}
		return 0;
	}
	
     public static long getXpWinPvm(Peleador perso, ArrayList<Peleador> team, ArrayList<Peleador> loose, long groupXP)
     {
         int lvlwin = 0;
         for(Peleador entry : team)lvlwin += entry.get_lvl();
         int lvllos = 0;
         for(Peleador entry : loose)lvllos += entry.get_lvl();
         float bonusSage = (perso.getTotalStats().getEffect(Constantes.STATS_ADD_SAGE)+100)/100;
         //* Formule 1
         float taux = perso.get_lvl()/lvlwin;
         long xp = (long)(groupXP * taux * bonusSage * perso.get_lvl()/2);
         //*/
         /* Formule 2
         long sXp = groupXP*lvllos;
         long gXp = 2 * groupXP * perso.get_lvl();
         long xp = (long)((sXp + gXp)*bonusSage);
         /*/
         return xp* MainServidor.XP_PVM;
     }

    public static long getXpWinPvm3(Peleador perso, ArrayList<Peleador> winners, ArrayList<Peleador> loosers, long groupXP, int star) {
        int lvlWinners = 0, lvlLoosers = 0, lvlLoosersmax = 0, lvlmax = 0;
        for(Peleador entry : loosers){
            lvlLoosers += entry.get_lvl();
            if (lvlLoosersmax < entry.get_lvl())
                lvlLoosersmax = entry.get_lvl();

            if (lvlmax < entry.get_lvl())
                lvlmax = entry.get_lvl();
        }
        for(Peleador entry : winners){
            lvlWinners += entry.get_lvl();
            if (lvlmax < entry.get_lvl()){
                lvlmax = entry.get_lvl();
            }
        }

        if(winners.contains(perso))//Si winner
        {
            float sag = perso.getTotalStats().getEffect(Constantes.STATS_ADD_SAGE) + star;
            float coef = (sag + 100)/100;
            int taux = MainServidor.XP_PVM;
            long xpWin = 0;


            int nbbonus = 0;
            for(Peleador entry : winners) {
                if(entry.get_lvl() > (lvlmax / 3)) {
                    nbbonus += 1;
                }
            }

            int lvl = perso.get_lvl();
            double bonusgroupe = ((double)lvl / (double)lvlmax);

            double modif1 = 1;
            if (lvlLoosers + 5 > lvlWinners && lvlWinners > lvlLoosers - 10) {
                modif1 = 1;
            }
            if (lvlWinners < lvlLoosers - 10) {
                modif1 = (((double)lvlWinners + 10) / (double)lvlLoosers);
            }
            if (lvlLoosers + 5 < lvlWinners) {
                modif1 = (double)lvlLoosers / (double)lvlWinners;
            }
            if (lvlLoosers > lvlWinners + 10) {
                modif1 = ((double)lvlWinners + 10)/ (double)lvlLoosers;
            }
            if (lvlWinners > lvlLoosers + 5) {
                modif1 = (double)lvlLoosers / (double)lvlWinners;
            }

            double modif2 = 0;
            if ((lvlLoosersmax * 2.5) > lvlWinners) {
                modif2 = 1;
            }
            else {
                modif2 = Math.floor((2.5 * lvlLoosersmax)) / lvlWinners;
            }

            double bonus = 1.5;
            if(nbbonus == 2) {
                bonus = 2;
            }
            else if(nbbonus == 3) {
                bonus = 2.5;
            }
            else if(nbbonus == 4) {
                bonus = 3;
            }
            else if(nbbonus == 5) {
                bonus = 3.5;
            }
            else if(nbbonus == 6) {
                bonus = 4;
            }
            else if(nbbonus == 7) {
                bonus = 4.5;
            }
            else if(nbbonus >= 8) {
                bonus = 4.7;
            }
            else {
                bonus = 5;
            }

            double rapport = 1+((double)lvlLoosers/(double)lvlWinners);
            if (rapport <= 1.3) {
                rapport = 1.3;
            }

			double formula = (((groupXP * (modif1 * modif2)) * bonusgroupe) * (coef * bonus)) * taux;

            if (lvlWinners == lvlLoosers) {
                xpWin = (long) formula;
            }
            else {
                xpWin = (long) formula;
            }

            if(xpWin <= 1) {
                xpWin = 1;
            }

            for(Peleador entry : winners)
            {
                for(Peleador loos : loosers)
                {
                    if(perso.getPersonnage() == null) {
                        return 0;
                    }
                    if(entry.isInvocation()) {
                        continue;
                    }
                    if(entry.hasLeft()){continue;}//Abandon xp pas

                    if(entry.getPersonnage() == null){continue;}//Monstre xp pas

                    if(loos.getMob() == null){continue;}//Le perdant doit �tre un monstre

                    if(loos.getPersonnage() != null){continue;}//Le perdant ne doit pas �tre un joueur

                    return xpWin;
                }

                return 0;
            }

            return 0;
        }

        return 0;
    }

	  public static long XPDefie(Peleador perso, ArrayList winners, ArrayList looser)
	  {
	      int lvlLoosers = 0;
		  for (Object o : looser) {
			  Peleador entry = (Peleador) o;
			  lvlLoosers += entry.get_lvl();
		  }

	      int lvlWinners = 0;
		  for (Object winner : winners) {
			  Peleador entry = (Peleador) winner;
			  lvlWinners += entry.get_lvl();
		  }

	      int taux = MainServidor.XP_PVP;
	      float rapport = (float)lvlLoosers / (float)lvlWinners;
	      int malus = 1;
	      if((double)rapport < 0.84999999999999998D)
	          malus = 6;
	      if(rapport >= 1.0F)
	          malus = 1;
	      long xpWin = (long)(((rapport * (float)getXpNeededAtLevel(perso.getPersonnage().get_lvl())) / 10F) * (float)taux) / (long)malus;
	      return xpWin;
	  }
	
	private static long getXpNeededAtLevel(int lvl)
	{
		long xp = (Mundo.getPersoXpMax(lvl) - Mundo.getPersoXpMin(lvl));
		System.out.println("Xp Max => "+ Mundo.getPersoXpMax(lvl));
		System.out.println("Xp Min => "+ Mundo.getPersoXpMin(lvl));
		
		return xp;
	}

	public static long getGuildXpWin(Peleador perso, AtomicReference<Long> xpWin)
	{
		if(perso.getPersonnage()== null)return 0;
		if(perso.getPersonnage().getMiembroGremio() == null)return 0;
		

		GuildMember gm = perso.getPersonnage().getMiembroGremio();
		
		double xp = (double)xpWin.get(), Lvl = perso.get_lvl(),LvlGuild = perso.getPersonnage().get_guild().get_lvl(),pXpGive = (double)gm.getPXpGive()/100;
		
		double maxP = xp * pXpGive * 0.10;	//Le maximum donn� � la guilde est 10% du montant pr�lev� sur l'xp du combat
		double diff = Math.abs(Lvl - LvlGuild);	//Calcul l'�cart entre le niveau du personnage et le niveau de la guilde
		double toGuild;
		if(diff >= 70)
		{
			toGuild = maxP * 0.10;	//Si l'�cart entre les deux level est de 70 ou plus, l'experience donn�e a la guilde est de 10% la valeur maximum de don
		}
		else if(diff >= 31 && diff <= 69)
		{
			toGuild = maxP - ((maxP * 0.10) * (Math.floor((diff+30)/10)));
		}
		else if(diff >= 10 && diff <= 30)
		{
			toGuild = maxP - ((maxP * 0.20) * (Math.floor(diff/10))) ;
		}
		else	//Si la diff�rence est [0,9]
		{
			toGuild = maxP;
		}
		xpWin.set((long)(xp - xp*pXpGive));
		return Math.round(toGuild);
	}
	
	public static long getMountXpWin(Peleador perso, AtomicReference<Long> xpWin)
	{
		if(perso.getPersonnage()== null)return 0;
		if(perso.getPersonnage().getMount() == null)return 0;
		

		int diff = Math.abs(perso.get_lvl() - perso.getPersonnage().getMount().get_level());
		
		double coeff = 0;
		double xp = (double) xpWin.get();
		double pToMount = (double)perso.getPersonnage().getMountXpGive() / 100 + 0.2;
		
		if(diff >= 0 && diff <= 9)
			coeff = 0.1;
		else if(diff >= 10 && diff <= 19)
			coeff = 0.08;
		else if(diff >= 20 && diff <= 29)
			coeff = 0.06;
		else if(diff >= 30 && diff <= 39)
			coeff = 0.04;
		else if(diff >= 40 && diff <= 49)
			coeff = 0.03;
		else if(diff >= 50 && diff <= 59)
			coeff = 0.02;
		else if(diff >= 60 && diff <= 69)
			coeff = 0.015;
		else
			coeff = 0.01;
		
		if(pToMount > 0.2)
			xpWin.set((long)(xp - (xp*(pToMount-0.2))));
		
		return Math.round(xp * pToMount * coeff);
	}

	public static int getKamasWin(Peleador i, ArrayList<Peleador> winners, int maxk, int mink)
	{
		maxk++;
		int rkamas = (int)(Math.random() * (maxk-mink)) + mink;
		return rkamas* MainServidor.KAMAS;
	}
	public static int getKamasWinPVP(Peleador i, ArrayList<Peleador> winners, int maxk, int mink)
	{
		maxk++;
		int rkamas = (int)(Math.random() * (MainServidor.CONFIG_KAMASMAX- MainServidor.CONFIG_KAMASMIN)) + MainServidor.CONFIG_KAMASMIN;
		return rkamas* MainServidor.KAMAS;
	}
	
	public static int getKamasWinPerco(int maxk, int mink)
	{
		maxk++;
		int rkamas = (int)(Math.random() * (maxk-mink)) + mink;
		return rkamas* MainServidor.KAMAS;
	}
	
	public static int calculElementChangeChance(int lvlM,int lvlA,int lvlP)
	{
		int K = 350;
		if(lvlP == 1)K = 100;
		else if (lvlP == 25)K = 175;
		else if (lvlP == 50)K = 350;
		return (lvlM*100)/(K + lvlA);
	}

	public static int calculHonorWin(ArrayList<Peleador> winners, ArrayList<Peleador> loosers, Peleador F)
	{
		float totalGradeWin = 0;
		float totalLevelWin = 0;
		float totalGradeLoose = 0;
		float totalLevelLoose = 0;
		for(Peleador f : winners)
		{
			if(f.getPersonnage() == null )continue;
			totalLevelWin += f.get_lvl();
			totalGradeWin += f.getPersonnage().getGrade();

		}
		for(Peleador f : loosers)
		{
			if(f.getPersonnage() == null)continue;
			totalLevelLoose += f.get_lvl();
			totalGradeLoose += f.getPersonnage().getGrade();

		}
		
		if(totalLevelWin-totalLevelLoose > MainServidor.LVL_PVP) return 0;

		int base = (int)(100 * (totalGradeLoose/totalGradeWin))/winners.size();
		if(loosers.contains(F))base = -base;
		return base * MainServidor.HONOR;
	}
	
	public static Doble<Integer, Integer> decompPierreAme(Objeto toDecomp)
	{
		Doble<Integer, Integer> toReturn;
		String[] stats = toDecomp.parseStatsString().split("#");
		int lvlMax = Integer.parseInt(stats[3],16);
		int chance = Integer.parseInt(stats[1],16);
		toReturn = new Doble<>(chance, lvlMax);
		
		return toReturn;
	}
	
	public static int totalCaptChance(int pierreChance, Personaje p)
	{
		int sortChance = switch (p.getSortStatBySortIfHas(413).getLevel()) {
			case 1 -> 1;
			case 2 -> 3;
			case 3 -> 6;
			case 4 -> 10;
			case 5 -> 15;
			case 6 -> 25;
			default -> 0;
		};

		return sortChance + pierreChance;
	}
	
	public static String parseReponse(String reponse)
	{
		StringBuilder toReturn = new StringBuilder();
		
		String[] cut = reponse.split("[%]");
		
		if(cut.length == 1)return reponse;
		
		toReturn.append(cut[0]);
		
		char charact;
		for (int i = 1; i < cut.length; i++)
		{
			charact = (char) Integer.parseInt(cut[i].substring(0, 2),16);
			toReturn.append(charact).append(cut[i].substring(2));
		}
		
		return toReturn.toString();
	}
	
	public static int spellCost(int nb)
	{
		int total = 0;
		for (int i = 1; i < nb ; i++)
		{
			total += i;
		}
		
		return total;
	}
	
	public static int ChanceFM(int poidItemBase, int poidItemActual, int poidBaseJet, int poidActualJet, double poidRune, int Puis, double Coef)
	{
		int Chance = 0;
		int a = (poidItemBase+poidBaseJet+(Puis*2));
		int b = (int) (Math.sqrt(poidItemActual+poidActualJet+poidRune));
		if(b <= 0) b = 1;
		Chance = (int) Math.floor((a/b)*Coef);
		
		//DEBUG :
		System.out.println("A : "+a);
		System.out.println("B : "+b);
		return Chance;
	}
	
	public static int getTraqueXP(int lvl)
	{
		if(lvl < 50)return 10000 * MainServidor.XP_PVM;
		if(lvl < 60)return 65000 * MainServidor.XP_PVM;
		if(lvl < 70)return 90000 * MainServidor.XP_PVM;
		if(lvl < 80)return 120000 * MainServidor.XP_PVM;
		if(lvl < 90)return 160000 * MainServidor.XP_PVM;
		if(lvl < 100)return 210000 * MainServidor.XP_PVM;
		if(lvl < 110)return 270000 * MainServidor.XP_PVM;
		if(lvl < 120)return 350000 * MainServidor.XP_PVM;
		if(lvl < 130)return 440000 * MainServidor.XP_PVM;
		if(lvl < 140)return 540000 * MainServidor.XP_PVM;
		if(lvl < 150)return 650000 * MainServidor.XP_PVM;
		if(lvl < 155)return 760000 * MainServidor.XP_PVM;
		if(lvl < 160)return 880000 * MainServidor.XP_PVM;
		if(lvl < 165)return 1000000 * MainServidor.XP_PVM;
		if(lvl < 170)return 1130000 * MainServidor.XP_PVM;
		if(lvl < 175)return 1300000 * MainServidor.XP_PVM;
		if(lvl < 180)return 1500000 * MainServidor.XP_PVM;
		if(lvl < 185)return 1700000 * MainServidor.XP_PVM;
		if(lvl < 190)return 2000000 * MainServidor.XP_PVM;
		if(lvl < 195)return 2500000 * MainServidor.XP_PVM;
		if(lvl < 200)return 3000000 * MainServidor.XP_PVM;
		return 0;
	}
	
	public static int getLoosEnergy(int lvl, boolean isAgression, boolean isPerco)
	{
		int returned = 25*lvl;
		if(isAgression) returned *= (7.0 /4);
		if(isPerco) returned *= (3.0 /2);
		return returned;
	}
}
