package objetos.hechizos;

import juego.JuegoServidor;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import comunes.MainServidor;
import comunes.Constantes;
import comunes.Formulas;
import comunes.Camino;
import comunes.Mundo;
import objetos.Mapa.Case;
import objetos.Pelea;
import objetos.Pelea.Fighter;

public class Hechizos {
	private final int spellID;
	private final int spriteID;
	private final String spriteInfos;
	private final Map<Integer,SortStats> sortStats = new TreeMap<>();
	private final ArrayList<Integer> effectTargets = new ArrayList<>();

    public static class SortStats
	{
		private final int spellID;
		private final int level;
		private final int PACost;
		private final int minPO;
		private final int maxPO;
		private final int TauxCC;
		private final int TauxEC;
		private final boolean isLineLaunch;
		private final boolean hasLDV;
		private final boolean isEmptyCell;
		private final boolean isModifPO;
		private final int maxLaunchbyTurn;
		private final int maxLaunchbyByTarget;
		private final int coolDown;
		private final int reqLevel;
		private final boolean isEcEndTurn;
		private final ArrayList<EfectoHechizo> effects;
		private final ArrayList<EfectoHechizo> CCeffects;
		private final String porteeType;
		
		public SortStats(int AspellID,int Alevel,int cost, int minPO, int maxPO, int tauxCC,int tauxEC, boolean isLineLaunch, boolean hasLDV,
				boolean isEmptyCell, boolean isModifPO, int maxLaunchbyTurn,int maxLaunchbyByTarget, int coolDown,
				int reqLevel,boolean isEcEndTurn, String effects,String ceffects,String typePortee)
		{
			this.spellID = AspellID;
			this.level = Alevel;
			this.PACost = cost;
			this.minPO = minPO;
			this.maxPO = maxPO;
			this.TauxCC = tauxCC;
			this.TauxEC = tauxEC;
			this.isLineLaunch = isLineLaunch;
			this.hasLDV = hasLDV;
			this.isEmptyCell = isEmptyCell;
			this.isModifPO = isModifPO;
			this.maxLaunchbyTurn = maxLaunchbyTurn;
			this.maxLaunchbyByTarget = maxLaunchbyByTarget;
			this.coolDown = coolDown;
			this.reqLevel = reqLevel;
			this.isEcEndTurn = isEcEndTurn;
			this.effects = parseEffect(effects);
			this.CCeffects = parseEffect(ceffects);
			this.porteeType = typePortee;
		}
		
		private ArrayList<EfectoHechizo> parseEffect(String e)
		{
			ArrayList<EfectoHechizo> effets = new ArrayList<>();
			String[] splt = e.split("\\|");
			for(String a : splt)
			{
				try
				{
					if(e.equals("-1"))continue;
					int id = Integer.parseInt(a.split(";",2)[0]);
					String args = a.split(";",2)[1];
					effets.add(new EfectoHechizo(id, args,spellID,level));
				}catch(Exception f){f.printStackTrace();System.out.println(a);System.exit(1);}
			}
			return effets;
		}


		public int getSpellID() {
			return spellID;
		}
		
		public Hechizos getSpell()
		{
			return Mundo.getSort(spellID);
		}
		public int getSpriteID()
		{
			return getSpell().getSpriteID();
		}
		
		public String getSpriteInfos()
		{
			return getSpell().getSpriteInfos();
		}
		
		public int getLevel() {
			return level;
		}

		public int getPACost() {
			return PACost;
		}

		public int getMinPO() {
			return minPO;
		}

		public int getMaxPO() {
			return maxPO;
		}

		public int getTauxCC() {
			return TauxCC;
		}

		public int getTauxEC() {
			return TauxEC;
		}

		public boolean isLineLaunch() {
			return isLineLaunch;
		}

		public boolean hasLDV() {
			return hasLDV;
		}

		public boolean isEmptyCell() {
			return isEmptyCell;
		}

		public boolean isModifPO() {
			return isModifPO;
		}

		public int getMaxLaunchbyTurn() {
			return maxLaunchbyTurn;
		}

		public int getMaxLaunchbyByTarget() {
			return maxLaunchbyByTarget;
		}

		public int getCoolDown() {
			return coolDown;
		}

		public int getReqLevel() {
			return reqLevel;
		}

		public boolean isEcEndTurn() {
			return isEcEndTurn;
		}

		public ArrayList<EfectoHechizo> getEffects() {
			return effects;
		}

		public ArrayList<EfectoHechizo> getCCeffects() {
			return CCeffects;
		}

		public String getPorteeType() {
			return porteeType;
		}

		
		public void applySpellEffectToFight(Pelea fight, Fighter perso, Case cell, ArrayList<Case> cells, boolean isCC)
		{
			//Seulement appellé par les pieges, or les sorts de piege
			ArrayList<EfectoHechizo> effets;
			
			if(isCC)
				effets = CCeffects;
			else
				effets = effects;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Nombre d'effets: "+effets.size());
			int jetChance = Formulas.getRandomValue(0, 99);
			int curMin = 0;
			//int num = 0;
			for(EfectoHechizo SE : effets)
			{
				if(SE.getChance() != 0 && SE.getChance() != 100)//Si pas 100% lancement
				{
					if(jetChance <= curMin || jetChance >= (SE.getChance() + curMin))
					{
						curMin += SE.getChance();
						continue;
					}
					curMin += SE.getChance();
				}
				
				ArrayList<Fighter> cibles = EfectoHechizo.getTargets(SE,fight,cells);
				SE.applyToFight(fight, perso, cell,cibles);

				//num++;
			}
		}
		
		public void applySpellEffectToFight(Pelea fight, Fighter perso, Case cell, boolean isCC)
		{
			ArrayList<EfectoHechizo> effets;
			
			if(isCC)
				effets = CCeffects;
			else
				effets = effects;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.agregar_a_los_logs("Nombre d'effets: "+effets.size());
			int jetChance = Formulas.getRandomValue(0, 99);
			int curMin = 0;
			int num = 0;
			for(EfectoHechizo SE : effets)
			{
				if(fight.get_state()>= Constantes.FIGHT_STATE_FINISHED)return;
				if(SE.getChance() != 0 && SE.getChance() != 100)//Si pas 100% lancement
				{
					if(jetChance <= curMin || jetChance >= (SE.getChance() + curMin))
					{
						curMin += SE.getChance();
						continue;
					}
					curMin += SE.getChance();
				}
				
				int POnum = num*2;
				if(isCC)
				{
					POnum += effects.size()*2;//On zaap la partie du String des effets hors CC
				} 
				ArrayList<Case> cells = Camino.getCellListFromAreaString(fight.get_map(),cell.getID(),perso.get_fightCell().getID(),porteeType,POnum,isCC);
				
				ArrayList<Case> finalCells = new ArrayList<>();
				
				int TE = 0;
				Hechizos S = Mundo.getSort(spellID);
				//on prend le targetFlag corespondant au num de l'effet
				if(S!= null?S.getEffectTargets().size()>num:false)TE = S.getEffectTargets().get(num);
				
				for(Case C : cells)
				{
					if(C == null)continue;
					Fighter F = C.getFirstFighter();
					if(F == null)continue;
					//Ne touche pas les alliés
					if(((TE & 1) == 1) && (F.getTeam() == perso.getTeam()))continue;
					//Ne touche pas le lanceur
					if((((TE>>1) & 1) == 1) && (F.getGUID() == perso.getGUID()))continue;
					//Ne touche pas les ennemies
					if((((TE>>2) & 1) == 1) && (F.getTeam() != perso.getTeam()))continue;
					//Ne touche pas les combatants (seulement invocations)
					if((((TE>>3) & 1) == 1) && (!F.isInvocation()))continue;
					//Ne touche pas les invocations
					if((((TE>>4) & 1) == 1) && (F.isInvocation()))continue;
					//N'affecte que le lanceur
					if((((TE>>5) & 1) == 1) && (F.getGUID() != perso.getGUID()))continue;
					//Si pas encore eu de continue, on ajoute la case
					finalCells.add(C);
				}
				//Si le sort n'affecte que le lanceur et que le lanceur n'est pas dans la zone
				if(((TE>>5) & 1) == 1)if(!finalCells.contains(perso.get_fightCell()))finalCells.add(perso.get_fightCell());
				ArrayList<Fighter> cibles = EfectoHechizo.getTargets(SE,fight,finalCells);
				SE.applyToFight(fight, perso, cell,cibles);
				num++;
			}
		}
		
		
	}
	
	public Hechizos(int aspellID, int aspriteID, String aspriteInfos, String ET)
	{
		spellID = aspellID;
		spriteID = aspriteID;
		spriteInfos = aspriteInfos;
		String nET = ET.split(":")[0];
		String ccET = "";
		if(ET.split(":").length>1)ccET = ET.split(":")[1];
		for(String num : nET.split(";"))
		{
			try
			{
				effectTargets.add(Integer.parseInt(num));
			}catch(Exception e)
			{
				effectTargets.add(0);
				continue;
			}
		}
		for(String num : ccET.split(";"))
		{
            ArrayList<Integer> CCeffectTargets = new ArrayList<>();
            try
			{
				CCeffectTargets.add(Integer.parseInt(num));
			}catch(Exception e)
			{
				CCeffectTargets.add(0);
				continue;
			}
		}
	}
	
	
	public ArrayList<Integer> getEffectTargets()
	{
		return effectTargets;
	}


	public int getSpriteID() {
		return spriteID;
	}

	public String getSpriteInfos() {
		return spriteInfos;
	}

	public int getSpellID() {
		return spellID;
	}
	
	public SortStats getStatsByLevel(int lvl)
	{
		return sortStats.get(lvl);
	}
	
	public void addSortStats(Integer lvl,SortStats stats)
	{
		if(sortStats.get(lvl) != null)return;
		sortStats.put(lvl,stats);
	}
	
}
