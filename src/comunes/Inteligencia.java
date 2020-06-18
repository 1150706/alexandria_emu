package comunes;

import juego.JuegoServidor;
import juego.JuegoThread.GameAction;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import objetos.*;
import objetos.Hechizos.SortStats;
import objetos.Mapa.Case;
import objetos.Pelea.*;

public class Inteligencia {

	public static class IAThread implements Runnable
	{
		private Pelea _fight;
		private Fighter _fighter;
		private static boolean stop = false;
		private Thread _t;
		
		public IAThread(Fighter fighter, Pelea fight)
		{
			_fighter = fighter;
			_fight = fight;
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		}
		public void run()
		{
			stop = false;
			if(_fighter.getMob() == null)
			{
                if(_fighter.isDouble())
                {
                	apply_type5(_fighter,_fight);
    				try {
    					Thread.sleep(2000);
    				} catch (InterruptedException e) {};
    				_fight.endTurn();
                }
                else if(_fighter.isPerco())
				{
					apply_typePerco(_fighter,_fight);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {};
					_fight.endTurn();
				}
				else
				{
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {};
					_fight.endTurn();
				}
			}else 
			if(_fighter.getMob().getTemplate() == null)
			{
				_fight.endTurn();
			}else
			{
				switch(_fighter.getMob().getTemplate().getIAType())
				{
					case 0://Ne rien faire
						apply_type0(_fighter,_fight);
					break;
					case 1://Attaque, Buff soi-même, Buff Alliés, Avancer vers ennemis. 
						apply_type1(_fighter,_fight);//DV < 15% : Auto-Soin, Attaque, soin allié, buff allié, fuite
					break;
					case 2://Soutien
						apply_type2(_fighter,_fight);
					break;
					case 3://Avancer vers Alliés, Buff Alliés, Buff sois même
						apply_type3(_fighter,_fight);
					break;
					case 4://Attaque, Fuite, Buff Alliés, Buff sois même
						apply_type4(_fighter,_fight);
					break;
					case 5://Avancer vers ennemis
						apply_type5(_fighter,_fight);
					break;
					case 6://IA type invocations
						apply_type6(_fighter,_fight);
					break;
				}
				try {
					Thread.sleep(2000); // C'est si lent dofus =O
				} catch (InterruptedException e) {};
				
				if(!_fighter.isDead())//Mort d'une invocation pendant son tour de jeu : empeche de passer le tour du joueur suivant
				{
					_fight.endTurn();
				}
			}
		}
		
		private static void apply_type0(Fighter F, Pelea fight)
		{
			stop = true;
		}

		private static void apply_type1(Fighter F, Pelea fight)
		{
			while(!stop && F.canPlay())
			{
				int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
				Fighter T = getNearestEnnemy(fight, F); // Ennemis
				Fighter T2 = getNearestFriend(fight,F); // Amis
				if(T == null)
					return;
				if(PDVPER > 15)
				{
					int attack = attackIfPossible(fight,F);
					if(attack != 0)//Attaque
					{
						if(attack == 5) stop = true;//EC
						if(!moveToAttackIfPossible(fight,F))
						{
							if(!buffIfPossible(fight,F,F))//auto-buff
							{
								if(!HealIfPossible(fight,F, false))//soin allié
								{
									if(!buffIfPossible(fight,F,T2))//buff allié
									{
										if(!moveNearIfPossible(fight,F,T))//avancer
										{
											if(!invocIfPossible(fight,F))//invoquer
											{
												stop = true;
											}
										}
									}
								}
							}
						}
					}
				}
				else
				{
					if(!HealIfPossible(fight,F,true))//auto-soin
					{
						int attack = attackIfPossible(fight,F);
						if(attack != 0)//Attaque
						{
							if(attack == 5) stop = true;//EC
							if(!buffIfPossible(fight,F,F))//auto-buff
							{
								if(!HealIfPossible(fight,F,false))//soin allié
								{
									if(!buffIfPossible(fight,F,T2))//buff allié
									{
										if(!invocIfPossible(fight,F))
										{
											if(!moveFarIfPossible(fight, F))//fuite
											{
												stop = true;
											}
										}
									}
								}
							}
						}
					}				
				}
			}
		}

		private static void apply_type2(Fighter F, Pelea fight)
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestFriend(fight,F);
				if(!HealIfPossible(fight,F,false))//soin allié
				{
					if(!buffIfPossible(fight,F,T))//buff allié
					{
						if(!moveNearIfPossible(fight,F,T))//Avancer vers allié
						{
							if(!HealIfPossible(fight,F,true))//auto-soin
							{
								if(!buffIfPossible(fight,F,F))//auto-buff
								{
									if(!invocIfPossible(fight,F))
									{
										T = getNearestEnnemy(fight, F);
										int attack = attackIfPossible(fight,F);
										if(attack != 0)//Attaque
										{
											if(attack == 5) stop = true;//EC
											if(!moveFarIfPossible(fight, F))//fuite
												stop = true;
										}
									}
								}
							}
						}
					}
				}			
			}
		}
		
		private static void apply_type3(Fighter F, Pelea fight)
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestFriend(fight,F);
					if(!moveNearIfPossible(fight,F,T))//Avancer vers allié
					{
						if(!HealIfPossible(fight,F,false))//soin allié
						{
							if(!buffIfPossible(fight,F,T))//buff allié
							{
								if(!HealIfPossible(fight,F,true))//auto-soin
								{
									if(!invocIfPossible(fight,F))
									{
										if(!buffIfPossible(fight,F,F))//auto-buff
										{
												stop = true;
										}
									}
							}
						}
					}
				}
			}		
		}
		
		private static void apply_type4(Fighter F, Pelea fight) //IA propre La Folle
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestEnnemy(fight, F);
				if(T == null) return;
				int attack = attackIfPossible(fight,F);
				if(attack != 0)//Attaque
				{
					if(attack == 5) stop = true;//EC
					if(!moveFarIfPossible(fight, F))//fuite
					{
							if(!HealIfPossible(fight,F,false))//soin allié
							{
								if(!buffIfPossible(fight,F,T))//buff allié
								{
									if(!HealIfPossible(fight,F,true))//auto-soin
									{
										if(!invocIfPossible(fight,F))
										{
											if(!buffIfPossible(fight,F,F))//auto-buff
											{
													stop = true;
											}
										}
									}
								}
							}
					}
				}
			}
		}
		
		private static void apply_type5(Fighter F, Pelea fight) //IA propre aux énus
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestEnnemy(fight, F);
				if(T == null) return;
				
				if(!moveNearIfPossible(fight,F,T))//Avancer vers enemis
				{
					stop = true;
				}
			}
		}
		
		private static void apply_type6(Fighter F, Pelea fight)
		{
			while(!stop && F.canPlay())
			{
				if(!invocIfPossible(fight,F))
				{
					Fighter T = getNearestFriend(fight,F);
					if(!HealIfPossible(fight,F,false))//soin allié
					{
						if(!buffIfPossible(fight,F,T))//buff allié
						{
							if(!buffIfPossible(fight,F,F))//buff allié
							{
								if(!HealIfPossible(fight,F,true))
								{
									int attack = attackIfPossible(fight,F);
									if(attack != 0)//Attaque
									{
										if(attack == 5) stop = true;//EC
										if(!moveFarIfPossible(fight, F))//fuite
											stop = true;
									}
								}
							}
						}
					}	
				}
			}
		}
		
		private static void apply_typePerco(Fighter F, Pelea fight)
		{
			while(!stop && F.canPlay())
			{
				Fighter T = getNearestEnnemy(fight, F);
				if(T == null) return;
				int attack = attackIfPossiblePerco(fight,F);
				if(attack != 0)//Attaque
				{
					if(attack == 5) stop = true;//EC
					if(!moveFarIfPossible(fight, F))//fuite
					{
							if(!HealIfPossiblePerco(fight,F,false))//soin allié
							{
								if(!buffIfPossiblePerco(fight,F,T))//buff allié
								{
									if(!HealIfPossiblePerco(fight,F,true))//auto-soin
									{
										if(!buffIfPossiblePerco(fight,F,F))//auto-buff
										{
												stop = true;
										}
									}
								}
							}
					}
				}
			}
		}
		
		private static boolean moveFarIfPossible(Pelea fight, Fighter F)
		{
			int dist[] = {1000,1000,1000,1000,1000,1000,1000,1000,1000,1000}, cell[] = {0,0,0,0,0,0,0,0,0,0};
			for(int i = 0; i < 10 ; i++)
			{
				for(Fighter f : fight.getFighters(3))
				{
					
					if(f.isDead())continue;
					if(f == F || f.getTeam() == F.getTeam())continue;
					int cellf = f.get_fightCell().getID();
					if(cellf == cell[0] || cellf == cell[1] || cellf == cell[2] || cellf == cell[3] || cellf == cell[4] || cellf == cell[5] || cellf == cell[6] || cellf == cell[7] || cellf == cell[8] || cellf == cell[9])continue;					
					int d = 0;
					d = Camino.getDistanceBetween(fight.get_map(), F.get_fightCell().getID(), f.get_fightCell().getID());
					if(d == 0)continue;
					if(d < dist[i])
					{
						dist[i] = d;
						cell[i] = cellf;
					}
					if(dist[i] == 1000)
					{
						dist[i] = 0;
						cell[i] = F.get_fightCell().getID();
					}
				}
			}
			if(dist[0] == 0)return false;
			int dist2[] = {0,0,0,0,0,0,0,0,0,0};
			int PM = F.getCurPM(fight), caseDepart = F.get_fightCell().getID(), destCase = F.get_fightCell().getID();
			for(int i = 0; i <= PM;i++)
			{
				if(destCase > 0)
					caseDepart = destCase;
				int curCase = caseDepart;
				curCase += 15;
				int infl = 0, inflF = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Camino.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart + 14;
				infl = 0;
				
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Camino.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart -15;
				infl = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Camino.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
				
				curCase = caseDepart - 14;
				infl = 0;
				for(int a = 0; a < 10 && dist[a] != 0; a++)
				{
					dist2[a] = Camino.getDistanceBetween(fight.get_map(), curCase, cell[a]);
					if(dist2[a] > dist[a])
						infl++;
				}
				
				if(infl > inflF && curCase > 0 && curCase < 478 && testCotes(destCase, curCase))
				{
					inflF = infl;
					destCase = curCase;
				}
			}
			System.out.println("Test MOVEFAR : cell = " + destCase);
			if(destCase < 0 || destCase > 478 || destCase == F.get_fightCell().getID() || !fight.get_map().getCase(destCase).isWalkable(false))return false;
			if(F.getPM() <= 0)return false;
			ArrayList<Case> path = Camino.getShortestPathBetween(fight.get_map(),F.get_fightCell().getID(),destCase, 0);
			if(path == null)return false;
			
			// DEBUG PATHFINDING
			/*System.out.println("DEBUG PATHFINDING:");
			System.out.println("startCell: "+F.get_fightCell().getID());
			System.out.println("destinationCell: "+cellID);
			
			for(Case c : path)
			{
				System.out.println("Passage par cellID: "+c.getID()+" walk: "+c.isWalkable(true));
			}*/
			
			ArrayList<Case> finalPath = new ArrayList<>();
			for(int a = 0; a<F.getPM();a++)
			{
				if(path.size() == a)break;
				finalPath.add(path.get(a));
			}
			String pathstr = "";
			try{
			int curCaseID = F.get_fightCell().getID();
			int curDir = 0;
			for(Case c : finalPath)
			{
				char d = Camino.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
				if(d == 0)return false;//Ne devrait pas arriver :O
				if(curDir != d)
				{
					if(finalPath.indexOf(c) != 0)
						pathstr += GestorEncriptador.cellID_To_Code(curCaseID);
					pathstr += d;
				}
				curCaseID = c.getID();
			}
			if(curCaseID != F.get_fightCell().getID())
				pathstr += GestorEncriptador.cellID_To_Code(curCaseID);
			}catch(Exception e){e.printStackTrace();};
			//Création d'une GameAction
			GameAction GA = new GameAction(0,1, "");
			GA._args = pathstr;
			boolean result = fight.fighterDeplace(F, GA);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			return result;

		}

		private static boolean testCotes(int cell1, int cell2)
		{
			if ( cell1 == 15 || cell1 == 44 || cell1 == 73 || cell1 == 102 || cell1 == 131 || cell1 == 160 || cell1 == 189 || cell1 == 218 || cell1 == 247 || cell1 == 276 || cell1 == 305 || cell1 == 334 || cell1 == 363 || cell1 == 392 || cell1 == 421 || cell1 == 450 )
			{
				if( cell2 == cell1 + 14 || cell2 == cell1 - 15 )
					return false;			
			}
			if ( cell1 == 28 || cell1 == 57 || cell1 == 86 || cell1 == 115 || cell1 == 144 || cell1 == 173 || cell1 == 202 || cell1 == 231 || cell1 == 260 || cell1 == 289 || cell1 == 318 || cell1 == 347 || cell1 == 376 || cell1 == 405 || cell1 == 434 || cell1 == 463 )
			{
				if( cell2 == cell1 + 15 || cell2 == cell1 - 14 )
					return false;
			}
			return true;
		}
		
		private static boolean invocIfPossible(Pelea fight, Fighter fighter)
		{
			Fighter nearest = getNearestEnnemy(fight, fighter);
			if(nearest == null)
				return false;
			int nearestCell = Camino.getNearestCellAround(fight.get_map(),fighter.get_fightCell().getID(),nearest.get_fightCell().getID(),null);
			if(nearestCell == -1)
				return false;
			SortStats spell = getInvocSpell(fight,fighter,nearestCell);
			if(spell == null)
				return false;
			int invoc = fight.tryCastSpell(fighter, spell, nearestCell);
			if(invoc != 0)return false;
			
			return true;
		}
		
		private static SortStats getInvocSpell(Pelea fight, Fighter fighter, int nearestCell)
		{
			if(fighter.getMob() == null)return null;
			for(Entry<Integer, SortStats> SS : fighter.getMob().getSpells().entrySet())
			{
				if(!fight.CanCastSpell(fighter, SS.getValue(), fight.get_map().getCase(nearestCell), -1))
					continue;
				for(EfectoHechizo SE : SS.getValue().getEffects())
				{
					if(SE.getEffectID() == 181)
						return SS.getValue();		
				}
			}
			return null;
		}
		
		private static boolean HealIfPossible(Pelea fight, Fighter f, boolean autoSoin)//boolean pour choisir entre auto-soin ou soin allié
		{
			if(autoSoin && (f.getPDV()*100)/f.getPDVMAX() > 95 )return false;
			Fighter target = null;
			SortStats SS = null;
			if(autoSoin)
			{
				target = f;			
				SS = getHealSpell(fight,f,target);
			}
			else//sélection joueur ayant le moins de pv
			{
				Fighter curF = null;
				int PDVPERmin = 100;
				SortStats curSS = null;
				for(Fighter F : fight.getFighters(3))
				{					
					if(f.isDead())continue;
					if(F == f)continue;
					if(F.getTeam() == f.getTeam())
					{
						int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
						if( PDVPER < PDVPERmin && PDVPER < 95)
						{
							int infl = 0;
							for(Entry<Integer, SortStats> ss : f.getMob().getSpells().entrySet())
							{
								if(infl < calculInfluenceHeal(ss.getValue()) && calculInfluenceHeal(ss.getValue()) != 0 && fight.CanCastSpell(f, ss.getValue(), F.get_fightCell(), -1))//Si le sort est plus interessant
								{
									infl = calculInfluenceHeal(ss.getValue());
									curSS = ss.getValue();
								}
							}
							if(curSS != SS && curSS != null)
							{
								curF = F;
								SS = curSS;
								PDVPERmin = PDVPER;
							}
						}
					}
				}
				target = curF;			
			}
			if(target == null)return false;
			if(SS == null)return false;
			int heal = fight.tryCastSpell(f, SS, target.get_fightCell().getID());
			if(heal != 0)
				return false;
			
			return true;
		}
		
		private static boolean HealIfPossiblePerco(Pelea fight, Fighter f, boolean autoSoin)//boolean pour choisir entre auto-soin ou soin allié
		{
			if(autoSoin && (f.getPDV()*100)/f.getPDVMAX() > 95 )return false;
			Fighter target = null;
			SortStats SS = null;
			if(autoSoin)
			{
				target = f;			
				SS = getHealSpell(fight,f,target);
			}
			else//sélection joueur ayant le moins de pv
			{
				Fighter curF = null;
				int PDVPERmin = 100;
				SortStats curSS = null;
				for(Fighter F : fight.getFighters(3))
				{					
					if(f.isDead())continue;
					if(F == f)continue;
					if(F.getTeam() == f.getTeam())
					{
						int PDVPER = (F.getPDV()*100)/F.getPDVMAX();
						if( PDVPER < PDVPERmin && PDVPER < 95)
						{
							int infl = 0;
							for(Entry<Integer, SortStats> ss : Mundo.getGuild(f.getPerco().GetPercoGuildID()).getSpells().entrySet())
							{
								if(ss.getValue() == null) continue;
								if(infl < calculInfluenceHeal(ss.getValue()) && calculInfluenceHeal(ss.getValue()) != 0 && fight.CanCastSpell(f, ss.getValue(), F.get_fightCell(), -1))//Si le sort est plus interessant
								{
									infl = calculInfluenceHeal(ss.getValue());
									curSS = ss.getValue();
								}
							}
							if(curSS != SS && curSS != null)
							{
								curF = F;
								SS = curSS;
								PDVPERmin = PDVPER;
							}
						}
					}
				}
				target = curF;			
			}
			if(target == null)return false;
			if(SS == null)return false;
			int heal = fight.tryCastSpell(f, SS, target.get_fightCell().getID());
			if(heal != 0)
				return false;
			
			return true;
		}
		
		private static boolean buffIfPossible(Pelea fight, Fighter fighter, Fighter target)
		{		
			if(target == null)return false;
			SortStats SS = getBuffSpell(fight,fighter,target);
			if(SS == null)return false;
			int buff = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
			if(buff != 0)return false;			
			
			return true;	
		}
		
		private static boolean buffIfPossiblePerco(Pelea fight, Fighter fighter, Fighter target)
		{		
			if(target == null)return false;
			SortStats SS = getBuffSpellPerco(fight,fighter,target);
			if(SS == null)return false;
			int buff = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
			if(buff != 0)return false;			
			
			return true;	
		}

		private static SortStats getBuffSpell(Pelea fight, Fighter F, Fighter T)
		{
			int infl = 0;	
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				if(infl < calculInfluence(fight, SS.getValue(),F,T) && calculInfluence(fight, SS.getValue(),F,T) > 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))//Si le sort est plus interessant
				{
					infl = calculInfluence(fight, SS.getValue(),F,T);
					ss = SS.getValue();
				}
			}
			return ss;				
		}
		
		private static SortStats getBuffSpellPerco(Pelea fight, Fighter F, Fighter T)
		{
			int infl = 0;	
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : Mundo.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
			{
				if(SS.getValue() == null) continue;
				if(infl < calculInfluence(fight, SS.getValue(),F,T) && calculInfluence(fight, SS.getValue(),F,T) > 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))//Si le sort est plus interessant
				{
					infl = calculInfluence(fight, SS.getValue(),F,T);
					ss = SS.getValue();
				}
			}
			return ss;				
		}
		
		private static SortStats getHealSpell(Pelea fight, Fighter F, Fighter T)
		{
			int infl = 0;	
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				if(infl < calculInfluenceHeal(SS.getValue()) && calculInfluenceHeal(SS.getValue()) != 0 && fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))//Si le sort est plus interessant
				{
					infl = calculInfluenceHeal(SS.getValue());
					ss = SS.getValue();
				}
			}
			return ss;
		}
		
		private static boolean moveNearIfPossible(Pelea fight, Fighter F, Fighter T)
		{
			if(F.getCurPM(fight) <= 0)
				return false;
			if(Camino.isNextTo(F.get_fightCell().getID(), T.get_fightCell().getID()))
				return false;
			
			if(MainServidor.CONFIG_DEBUG) JuegoServidor.addToLog("Tentative d'approche par "+F.getPacketsName()+" de "+T.getPacketsName());
			
			int cellID = Camino.getNearestCellAround(fight.get_map(),T.get_fightCell().getID(),F.get_fightCell().getID(),null);
			//On demande le chemin plus court
			if(cellID == -1)
			{
				Map<Integer,Fighter> ennemys = getLowHpEnnemyList(fight,F);
				for(Entry<Integer, Fighter> target : ennemys.entrySet())
				{
					int cellID2 = Camino.getNearestCellAround(fight.get_map(),target.getValue().get_fightCell().getID(),F.get_fightCell().getID(),null);
					if(cellID2 != -1)
					{
						cellID = cellID2;
						break;
					}
				}
			}
			ArrayList<Case> path = Camino.getShortestPathBetween(fight.get_map(),F.get_fightCell().getID(),cellID,0);
			if(path == null || path.isEmpty())return false;
			// DEBUG PATHFINDING
			/*System.out.println("DEBUG PATHFINDING:");
			System.out.println("startCell: "+F.get_fightCell().getID());
			System.out.println("destinationCell: "+cellID);
			
			for(Case c : path)
			{
				System.out.println("Passage par cellID: "+c.getID()+" walk: "+c.isWalkable(true));
			}*/
			
			ArrayList<Case> finalPath = new ArrayList<>();
			for(int a = 0; a<F.getCurPM(fight);a++)
			{
				if(path.size() == a)break;
				finalPath.add(path.get(a));
			}
			String pathstr = "";
			try{
			int curCaseID = F.get_fightCell().getID();
			int curDir = 0;
			for(Case c : finalPath)
			{
				char d = Camino.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
				if(d == 0)return false;//Ne devrait pas arriver :O
				if(curDir != d)
				{
					if(finalPath.indexOf(c) != 0)
						pathstr += GestorEncriptador.cellID_To_Code(curCaseID);
					pathstr += d;
				}
				curCaseID = c.getID();
			}
			if(curCaseID != F.get_fightCell().getID())
				pathstr += GestorEncriptador.cellID_To_Code(curCaseID);
			}catch(Exception e){e.printStackTrace();};
			//Création d'une GameAction
			GameAction GA = new GameAction(0,1, "");
			GA._args = pathstr;
			boolean result = fight.fighterDeplace(F, GA);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			return result;
		}

		private static Fighter getNearestFriend(Pelea fight, Fighter fighter)
		{
			int dist = 1000;
			Fighter curF = null;
			for(Fighter f : fight.getFighters(3))
			{
				if(f.isDead())continue;
				if(f == fighter)continue;
				if(f.getTeam2() == fighter.getTeam2())//Si c'est un ami
				{
					int d = Camino.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
					if( d < dist)
					{
						dist = d;
						curF = f;
					}
				}
			}
			return curF;
		}
		
		private static Fighter getNearestEnnemy(Pelea fight, Fighter fighter)
		{
			int dist = 1000;
			Fighter curF = null;
			for(Fighter f : fight.getFighters(3))
			{
				if(f.isDead())continue;
				if(f.getTeam2() != fighter.getTeam2())//Si c'est un ennemis
				{
					int d = Camino.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), f.get_fightCell().getID());
					if( d < dist)
					{
						dist = d;
						curF = f;
					}
				}
			}
			return curF;
		}
		
		private static Map<Integer,Fighter> getLowHpEnnemyList(Pelea fight, Fighter fighter)
		{
			Map<Integer,Fighter> list = new TreeMap<>();
			Map<Integer,Fighter> ennemy = new TreeMap<>();
			for(Fighter f : fight.getFighters(3))
			{
				if(f.isDead())continue;
				if(f == fighter)continue;
				if(f.getTeam2() != fighter.getTeam2())
				{
					ennemy.put(f.getPDV(), f);
				}
			}
			int i = 0, i2 = ennemy.size();
			int curHP = 10000;
			
			while ( i < i2)
			{
				curHP = 200000;
				for(Entry<Integer, Fighter> t : ennemy.entrySet())
				{
					if (t.getValue().getPDV() < curHP)
						curHP = t.getValue().getPDV();
				}
				Fighter test = ennemy.get(curHP);
				list.put(test.getPDV(), test);
				ennemy.remove(curHP);
				i++;
			}
			return list;
		}
		
		
		private static int attackIfPossible(Pelea fight, Fighter fighter)// 0 = Rien, 5 = EC, 666 = NULL, 10 = SpellNull ou ActionEnCour ou Can'tCastSpell, 0 = AttaqueOK
		{	
			Map<Integer,Fighter> ennemyList = getLowHpEnnemyList(fight,fighter);
			SortStats SS = null;
			Fighter target = null;
			for(Entry<Integer, Fighter> t : ennemyList.entrySet()) // pour chaque ennemi on cherche le meilleur sort
			{
				SS = getBestSpellForTarget(fight,fighter,t.getValue());
				if(SS != null) // s'il existe un sort pour un ennemi, on le prend pour cible
				{
					target = t.getValue();
					break;
				}
			}
			int curTarget = 0,cell = 0;
			SortStats SS2 = null;
			for(Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet()) // pour chaque sort du mob
			{
				int targetVal = getBestTargetZone(fight,fighter,S.getValue(),fighter.get_fightCell().getID()); // on détermine le meilleur
				if(targetVal == -1 || targetVal == 0) // endroit pour lancer le sort de zone (ou non)
					continue;
				int nbTarget = targetVal / 1000;
				int cellID = targetVal - nbTarget * 1000;
				if(nbTarget > curTarget)
				{
					curTarget = nbTarget;
					cell = cellID;
					SS2 = S.getValue();
				}
			}
			if(curTarget > 0 && cell > 0 && cell < 480 && SS2 != null) // si la case sélectionnée est valide et qu'il y a au moins une cible
			{
				int attack = fight.tryCastSpell(fighter, SS2, cell);
				if(attack != 0)
					return attack;
			}
			else
			{
				if(target == null || SS == null)
					return 666;
				int attack = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
				if(attack != 0)
					return attack;			
			}
			return 0;
		}
		
		private static int attackIfPossiblePerco(Pelea fight, Fighter fighter)
		{	
			Map<Integer,Fighter> ennemyList = getLowHpEnnemyList(fight,fighter);
			SortStats SS = null;
			Fighter target = null;
			for(Entry<Integer, Fighter> t : ennemyList.entrySet())
			{
				SS = getBestSpellForTargetPerco(fight,fighter,t.getValue());
				if(SS != null)
				{
					target = t.getValue();
					break;
				}
			}
			int curTarget = 0,cell = 0;
			SortStats SS2 = null;
			for(Entry<Integer, SortStats> S : Mundo.getGuild(fighter.getPerco().GetPercoGuildID()).getSpells().entrySet())
			{
				if(S.getValue() == null) continue;
				int targetVal = getBestTargetZone(fight,fighter,S.getValue(),fighter.get_fightCell().getID());
				if(targetVal == -1 || targetVal == 0)
					continue;
				int nbTarget = targetVal / 1000;
				int cellID = targetVal - nbTarget * 1000;
				if(nbTarget > curTarget)
				{
					curTarget = nbTarget;
					cell = cellID;
					SS2 = S.getValue();
				}
			}
			if(curTarget > 0 && cell > 0 && cell < 480 && SS2 != null)
			{
				int attack = fight.tryCastSpell(fighter, SS2, cell);
				if(attack != 0)
					return attack;
			}
			else
			{
				if(target == null || SS == null)
					return 666;
				int attack = fight.tryCastSpell(fighter, SS, target.get_fightCell().getID());
				if(attack != 0)
					return attack;			
			}		
			return 0;
			
		}
		
		
		private static boolean moveToAttackIfPossible(Pelea fight, Fighter fighter)
		{
			ArrayList<Integer> cells = Camino.getListCaseFromFighter(fight,fighter);
			if(cells == null)
				return false;
			int distMin = Camino.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), getNearestEnnemy(fight,fighter).get_fightCell().getID());
			ArrayList <SortStats> sorts = getLaunchableSort(fighter,fight,distMin);
			if(sorts == null)
				return false;
			ArrayList <Fighter> targets = getPotentialTarget(fight,fighter,sorts);
			if(targets == null)
				return false;
			
			int CellDest = 0;
			boolean found = false;
			for(int i : cells)
			{
				for(SortStats S : sorts)
				{
					for(Fighter T : targets)
					{
						if(fight.CanCastSpell(fighter,S,T.get_fightCell(),i))
						{
							CellDest = i;
							found = true;
						}
						int targetVal = getBestTargetZone(fight,fighter,S,i);
						if(targetVal > 0)
						{
							int nbTarget = targetVal / 1000;
							int cellID = targetVal - nbTarget * 1000;
							if(fight.get_map().getCase(cellID) != null)
							{
								if(fight.CanCastSpell(fighter,S,fight.get_map().getCase(cellID),i))
								{
									CellDest = i;
									found = true;
								}
							}
						}
						if(found)
							break;
					}
					if(found)
						break;
				}
				if(found)
					break;
			}
			if(CellDest == 0)
				return false;
			ArrayList<Case> path = Camino.getShortestPathBetween(fight.get_map(),fighter.get_fightCell().getID(),CellDest, 0);
			if(path == null)return false;
			String pathstr = "";
			try{
			int curCaseID = fighter.get_fightCell().getID();
			int curDir = 0;
			for(Case c : path)
			{
				char d = Camino.getDirBetweenTwoCase(curCaseID, c.getID(), fight.get_map(), true);
				if(d == 0)return false;//Ne devrait pas arriver :O
				if(curDir != d)
				{
					if(path.indexOf(c) != 0)
						pathstr += GestorEncriptador.cellID_To_Code(curCaseID);
					pathstr += d;
				}
				curCaseID = c.getID();
			}
			if(curCaseID != fighter.get_fightCell().getID())
				pathstr += GestorEncriptador.cellID_To_Code(curCaseID);
			}catch(Exception e){e.printStackTrace();};
			//Création d'une GameAction
			GameAction GA = new GameAction(0,1, "");
			GA._args = pathstr;
			boolean result = fight.fighterDeplace(fighter, GA);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			return result;
			
		}
		
		private static ArrayList <SortStats> getLaunchableSort(Fighter fighter, Pelea fight, int distMin)
		{
			ArrayList <SortStats> sorts = new ArrayList<>();
			if(fighter.getMob() == null)
				return null;
			for(Entry<Integer, SortStats> S : fighter.getMob().getSpells().entrySet())
			{
				if(S.getValue().getPACost() > fighter.getCurPA(fight))//si PA insuffisant
					continue;
				//if(S.getValue().getMaxPO() + fighter.getCurPM(fight) < distMin && S.getValue().getMaxPO() != 0)// si po max trop petite
					//continue;
				if(!LaunchedSort.coolDownGood(fighter, S.getValue().getSpellID()))// si cooldown ok
					continue;
				if(S.getValue().getMaxLaunchbyTurn() - LaunchedSort.getNbLaunch(fighter, S.getValue().getSpellID()) <= 0 && S.getValue().getMaxLaunchbyTurn() > 0)// si nb tours ok
					continue;
				if(calculInfluence(fight, S.getValue(),fighter,fighter) >= 0)// si sort pas d'attaque
					continue;
				sorts.add(S.getValue());
			}
			ArrayList <SortStats> finalS = TriInfluenceSorts(fight, fighter,sorts);
			
			return finalS;
		}
		
		private static ArrayList <SortStats> TriInfluenceSorts(Pelea fight, Fighter fighter, ArrayList <SortStats> sorts)
		{
			if(sorts == null)
				return null;
			
			ArrayList <SortStats> finalSorts = new ArrayList<>();
			Map <Integer,SortStats> copie = new TreeMap<>();
			for(SortStats S : sorts)
			{
				copie.put(S.getSpellID(), S);
			}
			
			int curInfl = 0;
			int curID = 0;
			
			while ( copie.size() > 0)
			{
				curInfl = 0;
				curID = 0;
				for(Entry<Integer, SortStats> S : copie.entrySet())
				{
					int infl = -calculInfluence(fight, S.getValue(),fighter,fighter);
					if (infl > curInfl)
					{
						curID = S.getValue().getSpellID();
						curInfl = infl;
					}
				}
				if(curID == 0 || curInfl == 0)
					break;
				finalSorts.add(copie.get(curID));
				copie.remove(curID);
			}
			
			return finalSorts;
		}
		
		private static ArrayList <Fighter> getPotentialTarget(Pelea fight, Fighter fighter, ArrayList<SortStats> sorts)
		{
			ArrayList <Fighter> targets = new ArrayList<>();
			int distMax = 0;
			for(SortStats S : sorts)
			{
				if(S.getMaxPO() > distMax)
					distMax = S.getMaxPO();
			}
			distMax += fighter.getCurPM(fight);
			Map<Integer,Fighter> potentialsT = getLowHpEnnemyList(fight,fighter);
			for(Entry<Integer,Fighter> T : potentialsT.entrySet())
			{
				int dist = Camino.getDistanceBetween(fight.get_map(), fighter.get_fightCell().getID(), T.getValue().get_fightCell().getID());
				if(dist < distMax)
					targets.add(T.getValue());
			}
			
			return targets;
		}
		
		private static SortStats getBestSpellForTarget(Pelea fight, Fighter F, Fighter T)
		{
			int inflMax = 0;
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : F.getMob().getSpells().entrySet())
			{
				int curInfl = 0, Infl1 = 0, Infl2 = 0;
				int PA = F.getMob().getPA();
				int usedPA[] = {0,0};
				if(!fight.CanCastSpell(F, SS.getValue(), T.get_fightCell(), -1))continue;
				curInfl = calculInfluence(fight, SS.getValue(),F,T);
				if(curInfl == 0)continue;
				if(curInfl > inflMax)
				{
					ss = SS.getValue();
					usedPA[0] = ss.getPACost();
					Infl1 = curInfl;
					inflMax = Infl1;
				}
				
				for(Entry<Integer, SortStats> SS2 : F.getMob().getSpells().entrySet())
				{
					if( (PA - usedPA[0]) < SS2.getValue().getPACost())continue;
					if(!fight.CanCastSpell(F, SS2.getValue(), T.get_fightCell(), -1))continue;
					curInfl = calculInfluence(fight, SS2.getValue(),F,T);
					if(curInfl == 0)continue;
					if((Infl1 + curInfl) > inflMax)
					{
						ss = SS.getValue();
						usedPA[1] = SS2.getValue().getPACost();
						Infl2 = curInfl;
						inflMax = Infl1 + Infl2;
					}
					for(Entry<Integer, SortStats> SS3 : F.getMob().getSpells().entrySet())
					{
						if( (PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())continue;
						if(!fight.CanCastSpell(F, SS3.getValue(), T.get_fightCell(), -1))continue;
						curInfl = calculInfluence(fight, SS3.getValue(),F,T);
						if(curInfl == 0)continue;
						if((curInfl+Infl1+Infl2) > inflMax)
						{
							ss = SS.getValue();
							inflMax = curInfl + Infl1 + Infl2;
						}
					}				
				}			
			}
			return ss;
		}
		
		private static SortStats getBestSpellForTargetPerco(Pelea fight, Fighter F, Fighter T)
		{
			int inflMax = 0;
			SortStats ss = null;
			for(Entry<Integer, SortStats> SS : Mundo.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
			{
				if(SS.getValue() == null) continue;
				int curInfl = 0, Infl1 = 0, Infl2 = 0;
				int PA = 6;
				int usedPA[] = {0,0};
				if(!fight.CanCastSpell(F, SS.getValue(), F.get_fightCell(), T.get_fightCell().getID()))continue;
				curInfl = calculInfluence(fight, SS.getValue(),F,T);
				if(curInfl == 0)continue;
				if(curInfl > inflMax)
				{
					ss = SS.getValue();
					usedPA[0] = ss.getPACost();
					Infl1 = curInfl;
					inflMax = Infl1;
				}
				
				for(Entry<Integer, SortStats> SS2 : Mundo.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
				{
					if( (PA - usedPA[0]) < SS2.getValue().getPACost())continue;
					if(!fight.CanCastSpell(F, SS2.getValue(), F.get_fightCell(), T.get_fightCell().getID()))continue;
					curInfl = calculInfluence(fight, SS2.getValue(),F,T);
					if(curInfl == 0)continue;
					if((Infl1 + curInfl) > inflMax)
					{
						ss = SS.getValue();
						usedPA[1] = SS2.getValue().getPACost();
						Infl2 = curInfl;
						inflMax = Infl1 + Infl2;
					}
					for(Entry<Integer, SortStats> SS3 : Mundo.getGuild(F.getPerco().GetPercoGuildID()).getSpells().entrySet())
					{
						if( (PA - usedPA[0] - usedPA[1]) < SS3.getValue().getPACost())continue;
						if(!fight.CanCastSpell(F, SS3.getValue(), F.get_fightCell(), T.get_fightCell().getID()))continue;
						curInfl = calculInfluence(fight, SS3.getValue(),F,T);
						if(curInfl == 0)continue;
						if((curInfl+Infl1+Infl2) > inflMax)
						{
							ss = SS.getValue();
							inflMax = curInfl + Infl1 + Infl2;
						}
					}				
				}			
			}
			return ss;
		}

		private static int getBestTargetZone(Pelea fight, Fighter fighter, SortStats spell, int launchCell)
		{
			if(spell.getPorteeType().isEmpty() || (spell.getPorteeType().charAt(0) == 'P' && spell.getPorteeType().charAt(1) == 'a'))
			{
				return 0;
			}
			ArrayList<Case> possibleLaunch = new ArrayList<>();
			int CellF = -1;
			if(spell.getMaxPO() != 0)
			{
				char arg1 = 'a';
				if(spell.isLineLaunch())
				{	
					arg1 = 'X';
				}
				else
				{
					arg1 = 'C';
				}
				char[] table = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v'};
				char arg2 = 'a';
				if(spell.getMaxPO() > 20)
				{
					arg2 = 'u';
				}
				else
				{
					arg2 = table[spell.getMaxPO()];
				}
				String args = Character.toString(arg1) + Character.toString(arg2);
				possibleLaunch = Camino.getCellListFromAreaString(fight.get_map(),launchCell,launchCell,args,0,false);
			}
			else
			{
				possibleLaunch.add(fight.get_map().getCase(launchCell));
			}
			
			if(possibleLaunch == null)
			{
				return -1;
			}
			int nbTarget = 0;	
			for(Case cell : possibleLaunch)
			{
				try{
					if(!fight.CanCastSpell(fighter, spell, cell, launchCell))
						continue;
					int num = 0;
					int curTarget = 0;
					ArrayList<EfectoHechizo> test = new ArrayList<>();
					test.addAll(spell.getEffects());
					
					for(EfectoHechizo SE : test)
					{
						try{
							if(SE == null)
								continue;
							if(SE.getValue() == -1)
								continue;
							int POnum = num *2;
							ArrayList<Case> cells = Camino.getCellListFromAreaString(fight.get_map(),cell.getID(),launchCell,spell.getPorteeType(),POnum,false);
							for(Case c : cells)
							{
								if(c.getFirstFighter() == null)
									continue;
								if(c.getFirstFighter().getTeam2() != fighter.getTeam2())
									curTarget++;
							}
						}catch(Exception e){};
						num++;
					}
					if(curTarget > nbTarget)
					{
						nbTarget = curTarget;
						CellF = cell.getID();
					}
				}
				catch(Exception E){}
			}
			if(nbTarget > 0 && CellF != -1)	
				return CellF + nbTarget * 1000;
			else
				return 0;
		}
		
		private static int calculInfluenceHeal(SortStats ss)
		{
			int inf = 0;
			for(EfectoHechizo SE : ss.getEffects())
			{
				if(SE.getEffectID() != 108)return 0;			
				inf += 100 * Formulas.getMiddleJet(SE.getJet());
			}
			
			return inf;
		}
		
		private static int calculInfluence(Pelea fight, SortStats ss, Fighter C, Fighter T)
		{		
			//FIXME TODO
			int infTot = 0;
			double fact = 1;
			int num = 0, POnum = 0;
			int allies = 0, ennemies = 0;
			for(EfectoHechizo SE : ss.getEffects())
			{
				allies = 0;
				ennemies = 0;
				POnum = 2*num;
				/**Détermine à qui s'applique l'effet**/
				ArrayList<Case> cells = Camino.getCellListFromAreaString(fight.get_map(),T.get_fightCell().getID(),C.get_fightCell().getID(),ss.getPorteeType(),POnum,false);
				ArrayList<Case> finalCells = new ArrayList<>();
				int TE = 0;
				Hechizos S = ss.getSpell();
				//on prend le targetFlag corespondant au num de l'effet
				//si on peut
				if(S!= null?S.getEffectTargets().size()>num:false)
					TE = S.getEffectTargets().get(num);
				
				for(Case C1 : cells)
				{
					if(C1 == null)continue;
					Fighter F = C1.getFirstFighter();
					if(F == null)continue;
					//Ne touche pas les alliés
					if(((TE & 1) == 1) && (F.getTeam() == C.getTeam()))continue;
					//Ne touche pas le lanceur
					if((((TE>>1) & 1) == 1) && (F.getGUID() == C.getGUID()))continue;
					//Ne touche pas les ennemies
					if((((TE>>2) & 1) == 1) && (F.getTeam() != C.getTeam()))continue;
					//Ne touche pas les combatants (seulement invocations)
					if((((TE>>3) & 1) == 1) && (!F.isInvocation()))continue;
					//Ne touche pas les invocations
					if((((TE>>4) & 1) == 1) && (F.isInvocation()))continue;
					//N'affecte que le lanceur
					if((((TE>>5) & 1) == 1) && (F.getGUID() != C.getGUID()))continue;
					//Si pas encore eu de continue, on ajoute la case
					finalCells.add(C1);
				}
				//Si le sort n'affecte que le lanceur et que le lanceur n'est pas dans la zone
				if(((TE>>5) & 1) == 1)
					if(!finalCells.contains(C.get_fightCell()))
						finalCells.add(C.get_fightCell());
				ArrayList<Fighter> cibles = EfectoHechizo.getTargets(SE,fight,finalCells);
				for (Fighter fighter : cibles)
				{
					if (fighter.getTeam() == C.getTeam())
						allies++;
					else ennemies++;
				}
				num++;
				/**Fin de la détermination**/
				//System.out.println("SpellEffect : "+SE.getEffectID()+"   Nbr ennemis : "+ennemies+"   Nbr alliés : "+allies);
				int inf = 0;
				switch(SE.getEffectID())
				{
					case 5 ://repousse de X cases
						inf = 100;
						fact += 0.3 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 89://dommages % vie neutre
						inf = 500 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 91://Vol de Vie Eau
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 92://Vol de Vie Terre
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 93://Vol de Vie Air
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 94://Vol de Vie feu
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 95://Vol de Vie neutre
						inf = 200 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 96://Dommage Eau
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 97://Dommage Terre
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 98://Dommage Air
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 99://Dommage feu
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 100://Dommage neutre
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 101://retrait PA
						inf = 5000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 127://retrait PM
						inf = 3000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 84://vol PA
						inf = 8000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 77://vol PM
						inf = 4000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 108:// soin
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 111://+ PA
						inf = -5000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 128://+ PM
						inf = -3000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 121://+ Dom
						inf = -500 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 131://poison X pdv par PA
						inf = 300 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 132://désenvoute
						inf = 6000;
					break;
					case 138://+ %Dom
						inf = -150 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 150://invisibilité
						inf = -5000;
					break;
					case 168://retrait PA non esquivable
						inf = 10000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 169://retrait PM non esquivable
						inf = 8000 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 210://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 211://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 212://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 213://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 214://résistance
						inf = -100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 215://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 216://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 217://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 218://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 219://faiblesse
						inf = 100 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 265://réduction dommage
						inf = -250 * Formulas.getMiddleJet(SE.getJet());
					break;
					case 765://sacrifice
						inf = -50000;
					break;
						
					
				}
				if(C.getTeam() == T.getTeam())//Si Amis
					infTot -= inf*(allies-ennemies);
				else//Si ennemis
					infTot += inf*(ennemies-allies);
			}
			return (int) (((double)infTot)*fact);
		}
	}
}
