package objetos;

import juego.JuegoServidor;
import juego.JuegoThread.GameAction;

import java.io.PrintWriter;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Timer;

import objetos.Mapa.Case;
import objetos.Monstruo.MobGrade;
import objetos.Monstruo.MobGroup;
import objetos.Objeto.ObjTemplate;
import objetos.Personaje.Grupo;
import objetos.Personaje.Stats;
import objetos.hechizos.EfectoHechizo;
import objetos.hechizos.Hechizos.SortStats;

import comunes.*;
import comunes.Mundo.*;

public class Pelea
{
	/*
	 * TODO:
	 * Effets de combat
	 */
	public static class Piege
	{
		private final Fighter _caster;
		private final Case _cell;
		private final byte _size;
		private final int _spell;
		private final SortStats _trapSpell;
		private final Pelea _fight;
		private final int _color;
		private boolean _isunHide = true;
		private int _teamUnHide = -1;
		//private Map<Integer, Challenge> _challenges = new TreeMap<Integer, Challenge>();
		
		public Piege(Pelea fight, Fighter caster, Case cell, byte size, SortStats trapSpell, int spell)
		{
			_fight = fight;
			_caster = caster;
			_cell =cell;
			_spell = spell;
			_size = size;
			_trapSpell = trapSpell;
			_color = Constantes.getTrapsColor(spell);
		}

		public Case get_cell() {
			return _cell;
		}

		public byte get_size() {
			return _size;
		}

		public Fighter get_caster() {
			return _caster;
		}
		
		public void set_isunHide(Fighter f)
		{
			_isunHide = true;
			_teamUnHide = f.getTeam();
		}
		
		public boolean get_isunHide()
		{
			return _isunHide;
		}
		
		public void desappear()
		{
			StringBuilder str = new StringBuilder();
			StringBuilder str2 = new StringBuilder();
			StringBuilder str3 = new StringBuilder();
			StringBuilder str4 = new StringBuilder();
			
			int team = _caster.getTeam()+1;
			str.append("GDZ-").append(_cell.getID()).append(";").append(_size).append(";").append(_color);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team, 999, _caster.getGUID()+"", str.toString());
			str2.append("GDC"+_cell.getID());
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team, 999, _caster.getGUID()+"", str2.toString());
			if(get_isunHide())
			{
				int team2 = _teamUnHide+1;
				str3.append("GDZ-").append(_cell.getID()).append(";").append(_size).append(";").append(_color);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team2, 999, _caster.getGUID()+"", str3.toString());
				str4.append("GDC").append(_cell.getID());
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team2, 999, _caster.getGUID()+"", str4.toString());
			}
		}
		
		public void appear(Fighter f)
		{
			StringBuilder str = new StringBuilder();
			StringBuilder str2 = new StringBuilder();
			
			int team = f.getTeam()+1;
			str.append("GDZ+").append(_cell.getID()).append(";").append(_size).append(";").append(_color);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team, 999, _caster.getGUID()+"", str.toString());
			str2.append("GDC").append(_cell.getID()).append(";Haaaaaaaaz3005;");
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, team, 999, _caster.getGUID()+"", str2.toString());
		}
		
		public void onTraped(Fighter target)
		{
			if(target.isDead())
				return;
			_fight.get_traps().remove(this); // on enl�ve le pi�ge sur lequel target a march�
			desappear(); //On efface le piege
			//On d�clenche ses effets
			String str = _spell+","+_cell.getID()+",0,1,1,"+_caster.getGUID();
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 307, target.getGUID()+"", str);
			
			ArrayList<Case> cells = new ArrayList<>();
			cells.add(_cell);
			//on ajoute les autres cases que couvre le pi�ge
			for(int a = 0; a < _size;a++)
			{
				char[] dirs = {'b','d','f','h'};
				ArrayList<Case> cases2 = new ArrayList<>();//on �vite les modifications concurrentes
				cases2.addAll(cells); 
				for(Case aCell : cases2)
				{
					for(char d : dirs)
					{
						Case cell = _fight.get_map().getMapa(Camino.GetCaseIDFromDirrection(aCell.getID(), d, _fight.get_map(), true));
						if(cell == null)continue;
						if(!cells.contains(cell))
						{
							cells.add(cell);
						}
					}
				}
			}
			Fighter fakeCaster;
			if(_caster.getPersonnage() == null)
					fakeCaster = new Fighter(_fight,_caster.getMob());
			else 	fakeCaster = new Fighter(_fight,_caster.getPersonnage());

			fakeCaster.set_fightCell(_cell);
			_trapSpell.applySpellEffectToFight(_fight,fakeCaster,target.get_fightCell(),cells,false);
			_fight.verifIfTeamAllDead();
		}
		
		public int get_color()
		{
			return _color;
		}
	}
	
	public static class Fighter
	{
		private int _id = 0;
		private boolean _canPlay = false;
		private final Pelea _fight;
		private int _type = 0; // 1 : Personnage, 2 : Mob, 5 : Perco
		private MobGrade _mob = null;
		private Personaje _perso = null;
		private Recaudador _Perco = null;
		private Personaje _double = null;
		private int _team = -2;
		private Case _cell;
		private final ArrayList<EfectoHechizo> _fightBuffs = new ArrayList<>();
		private final Map<Integer,Integer> _chatiValue = new TreeMap<>();
		private Fighter _invocator;
		public int _nbInvoc = 0;
		private int _PDVMAX;
		private int _PDV;
		private boolean _isDead;
		private boolean _hasLeft;
		private int _gfxID;
		private final Map<Integer,Integer> _state = new TreeMap<>();
		private Fighter _isHolding;
		private Fighter _holdedBy;
		private final ArrayList<LaunchedSort> _launchedSort = new ArrayList<>();
		private Fighter _oldCible = null;
		
		public Fighter get_oldCible() {
			return _oldCible;
		}
		public void set_oldCible(Fighter cible) {
			_oldCible = cible;
		}
		
		public Fighter(Pelea f, MobGrade mob)
		{
			_fight = f;
			_type = 2;
			_mob = mob;
			_id = mob.getInFightID();
			_PDVMAX = mob.getPDVMAX();
			_PDV = mob.getPDV();
			_gfxID = getDefaultGfx();
		}
		
		public Fighter(Pelea f, Personaje perso)
		{
			_fight = f;
			if(perso._isClone)
			{
				_type = 10;
				_double = perso;
			}else
			{
				_type = 1;
				_perso = perso;
			}
			_id = perso.get_GUID();
			_PDVMAX = perso.get_PDVMAX();
			_PDV = perso.get_PDV();
			_gfxID = getDefaultGfx();
		}

		public Fighter(Pelea f, Recaudador Perco) {
			_fight = f;
			_type = 5;
			_Perco = Perco;
			System.out.println("Perco:"+Perco);
			_id = -1;
			_PDVMAX = (Mundo.getGuild(Perco.get_guildID()).get_lvl()*100);
			_PDV = (Mundo.getGuild(Perco.get_guildID()).get_lvl()*100);
			_gfxID = 6000;
		}

		public ArrayList<LaunchedSort> getLaunchedSorts()
		{
			return _launchedSort;
		}
		
		public void ActualiseLaunchedSort()
		{
			ArrayList<LaunchedSort> copie = new ArrayList<>();
			copie.addAll(_launchedSort);
			int i = 0;
			for(LaunchedSort S : copie)
			{
				S.ActuCooldown();
				if(S.getCooldown() <= 0)
				{
					_launchedSort.remove(i);
					i--;
				}
				i++;
			}
		}
		
		public void addLaunchedSort(Fighter target,SortStats sort)
		{
			LaunchedSort launched = new LaunchedSort(target,sort);
			_launchedSort.add(launched);
		}
		
		public int getGUID()
		{
			return _id;
		}
		public Fighter get_isHolding() {
			return _isHolding;
		}

		public void set_isHolding(Fighter isHolding) {
			_isHolding = isHolding;
		}

		public Fighter get_holdedBy() {
			return _holdedBy;
		}

		public void set_holdedBy(Fighter holdedBy) {
			_holdedBy = holdedBy;
		}

		public int get_gfxID() {
			return _gfxID;
		}

		public void set_gfxID(int gfxID) {
			_gfxID = gfxID;
		}

		public ArrayList<EfectoHechizo> get_fightBuff()
		{
			return _fightBuffs;
		}
		public void set_fightCell(Case cell)
		{
			_cell = cell;
		}
		public boolean isHide()
		{
			return hasBuff(150);
		}
		public Case get_fightCell()
		{		
			return _cell;
		}
		public void setTeam(int i)
		{
			_team = i;
		}
		public boolean isDead() {
			return _isDead;
		}

		public void setDead(boolean isDead) {
			_isDead = isDead;
		}

		public boolean hasLeft() {
			return _hasLeft;
		}

		public void setLeft(boolean hasLeft) {
			_hasLeft = hasLeft;
		}

		public Personaje getPersonnage()
		{
			if(_type == 1)
				return _perso;
			return null;
		}
		
		public Recaudador getPerco()
		{
			if(_type == 5)
				return _Perco;
			return null;
		}
		public boolean testIfCC(int tauxCC)
		{
			if(tauxCC < 2)return false;
			int agi = getTotalStats().getEffect(Constantes.STATS_ADD_AGIL);
			if(agi <0)agi =0;
			tauxCC -= getTotalStats().getEffect(Constantes.STATS_ADD_CC);
			tauxCC = (int)((tauxCC * 2.9901) / Math.log(agi +12));//Influence de l'agi
			if(tauxCC<2)tauxCC = 2;
			int jet = Formulas.getRandomValue(1, tauxCC);
			return (jet == tauxCC);
		}
		
		public Stats getTotalStats()
		{
			Stats stats = new Stats(new TreeMap<>());
			if(_type == 1)//Personnage
				stats = _perso.getTotalStats();
			if(_type == 2)//Mob
				stats =_mob.getStats();
			if(_type == 5)//Percepteur
				stats = Mundo.getGuild(_Perco.get_guildID()).getStatsFight();
			if(_type == 10)//Double
				stats = _double.getTotalStats();
			
			stats = Stats.cumulStat(stats,getFightBuffStats());
			return stats;
		}
		
		
		public void initBuffStats()
		{
			if(_type == 1)
			{
				for(Map.Entry<Integer, EfectoHechizo> entry : _perso.get_buff().entrySet())
				{
					_fightBuffs.add(entry.getValue());
				}
			}
		}
		
		private Stats getFightBuffStats()
		{
			Stats stats = new Stats();
			for(EfectoHechizo entry : _fightBuffs)
			{
				stats.addOneStat(entry.getEffectID(), entry.getValue());
			}
			return stats;
		}
		
		public String getGmPacket(char c)
		{
			StringBuilder str = new StringBuilder();
			str.append("GM|").append(c);
			str.append(_cell.getID()).append(";");
			int _orientation = 1;
			str.append(_orientation).append(";");
			str.append("0;");
			str.append(getGUID()).append(";");
			str.append(getPacketsName()).append(";");

			switch (_type) {
//Perso
				case 1 -> {
					str.append(_perso.getClase()).append(";");
					str.append(_perso.get_gfxID()).append("^").append(_perso.get_size()).append(";");
					str.append(_perso.getSexo()).append(";");
					str.append(_perso.get_lvl()).append(";");
					str.append(_perso.get_align()).append(",");
					str.append("0,");//TODO
					str.append((_perso.is_showWings() ? _perso.getGrade() : "0")).append(",");
					str.append(_perso.get_GUID()).append(";");
					str.append((_perso.get_color1() == -1 ? "-1" : Integer.toHexString(_perso.get_color1()))).append(";");
					str.append((_perso.get_color2() == -1 ? "-1" : Integer.toHexString(_perso.get_color2()))).append(";");
					str.append((_perso.get_color3() == -1 ? "-1" : Integer.toHexString(_perso.get_color3()))).append(";");
					str.append(_perso.getGMStuffString()).append(";");
					str.append(getPDV()).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PA)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PM)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_TER)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_FEU)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_EAU)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_AIR)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_AFLEE)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_MFLEE)).append(";");
					str.append(_team).append(";");
					if (_perso.isOnMount() && _perso.getMount() != null)
						str.append(_perso.getMount().getColor(_perso.parsecolortomount()));
					str.append(";");
				}
//Mob
				case 2 -> {
					str.append("-2;");
					str.append(_mob.getTemplate().getGfxID()).append("^100;");
					str.append(_mob.getGrade()).append(";");
					str.append(_mob.getTemplate().getColors().replace(",", ";")).append(";");
					str.append("0,0,0,0;");
					str.append(this.getPDVMAX()).append(";");
					str.append(_mob.getPA()).append(";");
					str.append(_mob.getPM()).append(";");
					str.append(_team);
				}
//Perco
				case 5 -> {
					str.append("-6;");//Perco
					str.append("6000^");//GFXID^
					Gremio G = Mundo.getGuild(Recaudador.GetPercoGuildID(_fight._mapOld.getID()));
					str.append(50 + G.get_lvl()).append(";"); // Size
					str.append(G.get_lvl()).append(";");
					str.append("1;");//FIXME
					str.append("2;4;");//FIXME
					str.append((int) Math.floor(G.get_lvl() / 2)).append(";").append((int) Math.floor(G.get_lvl() / 2)).append(";").append((int) Math.floor(G.get_lvl() / 2)).append(";").append((int) Math.floor(G.get_lvl() / 2)).append(";").append((int) Math.floor(G.get_lvl() / 2)).append(";").append((int) Math.floor(G.get_lvl() / 2)).append(";").append((int) Math.floor(G.get_lvl() / 2)).append(";");//R�sistances
					str.append(_team);
				}
//Double
				case 10 -> {
					str.append(_double.getClase()).append(";");
					str.append(_double.get_gfxID()).append("^").append(_double.get_size()).append(";");
					str.append(_double.getSexo()).append(";");
					str.append(_double.get_lvl()).append(";");
					str.append(_double.get_align()).append(",");
					str.append("0,");//TODO
					str.append((_double.is_showWings() ? _double.getGrade() : "0")).append(",");
					str.append(_double.get_GUID()).append(";");
					str.append((_double.get_color1() == -1 ? "-1" : Integer.toHexString(_double.get_color1()))).append(";");
					str.append((_double.get_color2() == -1 ? "-1" : Integer.toHexString(_double.get_color2()))).append(";");
					str.append((_double.get_color3() == -1 ? "-1" : Integer.toHexString(_double.get_color3()))).append(";");
					str.append(_double.getGMStuffString()).append(";");
					str.append(getPDV()).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PA)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_PM)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_NEU)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_TER)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_FEU)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_EAU)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_RP_AIR)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_AFLEE)).append(";");
					str.append(getTotalStats().getEffect(Constantes.STATS_ADD_MFLEE)).append(";");
					str.append(_team).append(";");
					if (_double.isOnMount() && _double.getMount() != null)
						str.append(_double.getMount().getColor(_perso.parsecolortomount()));
					str.append(";");
				}
			}
			
			return str.toString();
		}
		
		public void setState(int id, int t)
		{
			_state.remove(id);
			if(t != 0)
			_state.put(id, t);
		}
		
		public boolean isState(int id)
		{
			if(_state.get(id) == null)return false;
			return _state.get(id) != 0;
		}
		
		public void decrementStates()
		{
			//Copie pour �vident les modif concurrentes
			ArrayList<Entry<Integer,Integer>> entries = new ArrayList<>();
			entries.addAll(_state.entrySet());
			for(Entry<Integer,Integer> e : entries)
			{
				//Si la valeur est n�gative, on y touche pas
				if(e.getKey() < 0)continue;
				
				_state.remove(e.getKey());
				int nVal = e.getValue()-1;
				//Si 0 on ne remet pas la valeur dans le tableau
				if(nVal == 0)//ne pas mettre plus petit, -1 = infinie
				{
					//on envoie au client la desactivation de l'�tat
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 950, getGUID()+"", getGUID()+","+e.getKey()+",0");
					continue;
				}
				//Sinon on remet avec la nouvelle valeur
				_state.put(e.getKey(), nVal);
			}
		}
		
		public int getPDV()
		{
			int pdv = _PDV + getBuffValue(Constantes.STATS_ADD_VITA);
			return pdv;
		}
		
		public void removePDV(int pdv)
		{
			_PDV -= pdv;
		}
		
		public void applyBeginningTurnBuff(Pelea fight)
		{
			synchronized(_fightBuffs)
			{
				for(int effectID : Constantes.BEGIN_TURN_BUFF)
				{
					//On �vite les modifications concurrentes
					ArrayList<EfectoHechizo> buffs = new ArrayList<>();
					buffs.addAll(_fightBuffs);
					for(EfectoHechizo entry : buffs)
					{
						if(entry.getEffectID() == effectID)
						{
							if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Effet de debut de tour : "+ effectID);
							entry.applyBeginingBuff(fight, this);
						}
					}
				}
			}
		}

		public EfectoHechizo getBuff(int id)
		{
			for(EfectoHechizo entry : _fightBuffs)
			{
				if(entry.getEffectID() == id && entry.getDuration() >0)
				{
					return entry;
				}
			}
			return null;
		}
		
		public boolean hasBuff(int id)
		{
			for(EfectoHechizo entry : _fightBuffs)
			{
				if(entry.getEffectID() == id && entry.getDuration() >0)
				{
					return true;
				}
			}
			return false;
		}
		
		public int getBuffValue(int id)
		{
			int value = 0;
			for(EfectoHechizo entry : _fightBuffs)
			{
				if(entry.getEffectID() == id)
					value += entry.getValue();
			}
			return value;
		}
		
		public int getMaitriseDmg(int id)
		{
			int value = 0;
			for(EfectoHechizo entry : _fightBuffs)
			{
				if(entry.getSpell() == id)
					value += entry.getValue();
			}
			return value;
		}

		
		public boolean getSpellValueBool(int id)
		{
			for(EfectoHechizo entry : _fightBuffs)
			{
				if(entry.getSpell() == id)
					return true;
			}
			return false;
		}
	
		public void refreshfightBuff()
		{
			//Copie pour contrer les modifications Concurentes
			ArrayList<EfectoHechizo> b = new ArrayList<>();
			for(EfectoHechizo entry : _fightBuffs)
			{
				if(entry.decrementDuration() > 0)//Si pas fin du buff
				{
					b.add(entry);
				}else
				{
					if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Suppression du buff "+entry.getEffectID()+" sur le joueur Fighter ID= "+getGUID());
					switch(entry.getEffectID())
					{
						case 108:
							if(entry.getSpell() == 441)
							{
								//Baisse des pdvs max
								_PDVMAX = (_PDVMAX-entry.getValue());
								
								//Baisse des pdvs actuel
								int pdv = 0;
								if(_PDV-entry.getValue() <= 0){
									pdv = 0;
									_fight.onFighterDie(this, this);
									_fight.verifIfTeamAllDead();
								}
								else pdv = (_PDV-entry.getValue());
								_PDV = pdv;
							}
						break;
					
						case 150://Invisibilit�
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 150, entry.getCaster().getGUID()+"",getGUID()+",0");
						break;
						
						case 950:
							String args = entry.getArgs();
							int id = -1;
							try
							{
								id = Integer.parseInt(args.split(";")[2]);
							}catch(Exception ignored){}
							if(id == -1)return;
							setState(id,0);
							GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 950, entry.getCaster().getGUID()+"", entry.getCaster().getGUID()+","+id+",0");
						break;
					}
				}
			}
			_fightBuffs.clear();
			_fightBuffs.addAll(b);
		}
		
		public void addBuff(int id,int val,int duration,int turns,boolean debuff,int spellID,String args,Fighter caster)
		{
			if(spellID == 99 || 
			   spellID == 5 || 
			   spellID == 20 || 
			   spellID == 127 ||
			   spellID == 89 ||
			   spellID == 126 ||
			   spellID == 115 ||
			   spellID == 192 ||
			   spellID == 4 ||
			   spellID == 1 ||
			   spellID == 6 ||
			   spellID == 14 ||
			   spellID == 18 ||
			   spellID == 7 ||
			   spellID == 284 ||
			   spellID == 197 ||
			   spellID == 704
			   )
			{
				//Tr�ve
				//Immu
				//Pr�vention
				//Momification
				//D�vouement
				//Mot stimulant
				//Odorat
				//Ronce Apaisante
				//Renvoi de sort
				//Armure Incandescente
				//Armure Terrestre
				//Armure Venteuse
				//Armure Aqueuse
				//Bouclier F�ca
				//Acc�l�ration Poupesque
				//Puissance Sylvestre
				//Pandanlku
				debuff = true;
			}
			//Si c'est le jouer actif qui s'autoBuff, on ajoute 1 a la dur�e
			_fightBuffs.add(new EfectoHechizo(id,val,(_canPlay?duration+1:duration),turns,debuff,caster,args,spellID));
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Ajout du Buff "+id+" sur le personnage Fighter ID = "+this.getGUID()+" val : "+val+" duration : "+duration+" turns : "+turns+" debuff : "+debuff+" spellid : "+spellID+" args : "+args);


			switch (id) {
//Renvoie de sort
				case 6 -> GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), -1, val + "", "10", "", duration, spellID);
//Chance �ca
				case 79 -> {
					val = Integer.parseInt(args.split(";")[0]);
					String valMax = args.split(";")[1];
					String chance = args.split(";")[2];
					GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, valMax, chance, "", duration, spellID);
				}
//Fait apparaitre message le temps de buff sacri Chatiment de X sur Y tours
				case 788 -> {
					val = Integer.parseInt(args.split(";")[1]);
					String valMax2 = args.split(";")[2];
					if (Integer.parseInt(args.split(";")[0]) == 108) return;
					GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, "" + val, "" + valMax2, "", duration, spellID);
				}
//Ma�trises
				case 98, 107, 100, 108, 165 -> {
					val = Integer.parseInt(args.split(";")[0]);
					String valMax1 = args.split(";")[1];
					if (valMax1.compareTo("-1") == 0 || spellID == 82 || spellID == 94) {
						GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, "", "", "", duration, spellID);
					} else if (valMax1.compareTo("-1") != 0) {
						GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, valMax1, "", "", duration, spellID);
					}
				}
				default -> GestorSalida.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, "", "", "", duration, spellID);
			}
		}
		
		public int getInitiative()
		{
			if(_type == 1)
				return _perso.getInitiative();
			if(_type == 2)
				return _mob.getInit();
			if(_type == 5)
				return Mundo.getGuild(_Perco.get_guildID()).get_lvl();
			if(_type == 10)
				return _double.getInitiative();
			
			return 0;
		}
		public int getPDVMAX()
		{
			return _PDVMAX + getBuffValue(Constantes.STATS_ADD_VITA);
		}
		
		public int get_lvl() {
			if(_type == 1)
				return _perso.get_lvl();
			if(_type == 2)
				return _mob.getLevel();
			if(_type == 5)
				return Mundo.getGuild(_Perco.get_guildID()).get_lvl();
			if(_type == 10)
				return _double.get_lvl();
	
			return 0;
		}
		public String xpString(String str)
		{
			if(_perso != null)
			{
				int max = _perso.get_lvl()+1;
				if(max> Mundo.getExpLevelSize())max = Mundo.getExpLevelSize();
				return Mundo.getExpLevel(_perso.get_lvl()).perso+str+_perso.get_curExp()+str+ Mundo.getExpLevel(max).perso;
			}
			return "0"+str+"0"+str+"0";
		}
		public String getPacketsName()
		{
			if(_type == 1)
				return _perso.getNombre();
			if(_type == 2)
				return _mob.getTemplate().getID()+"";
			if(_type == 5)
				return (_Perco.get_N1()+","+_Perco.get_N2());
			if(_type == 10)
				return _double.getNombre();
			
			return "";
		}
		public MobGrade getMob()
		{
			if(_type == 2)
				return _mob;
			
			return null;
		}
		public int getTeam()
		{
			return _team;
		}
		public int getTeam2()
		{
			return _fight.getTeamID(_id);
		}
		public int getOtherTeam()
		{
			return _fight.getOtherTeamID(_id);
		}
		public boolean canPlay()
		{
			return _canPlay;
		}
		public void setCanPlay(boolean b)
		{
			_canPlay = b;
		}
		public ArrayList<EfectoHechizo> getBuffsByEffectID(int effectID)
		{
			ArrayList<EfectoHechizo> buffs = new ArrayList<>();
			for(EfectoHechizo buff : _fightBuffs)
			{
				if(buff.getEffectID() == effectID)
					buffs.add(buff);
			}
			return buffs;
		}
		public Stats getTotalStatsLessBuff()
		{
			Stats stats = new Stats(new TreeMap<>());
			if(_type == 1)
				stats = _perso.getTotalStats();
			if(_type == 2)
				stats =_mob.getStats();
			if(_type == 5)
				stats = Mundo.getGuild(_Perco.get_guildID()).getStatsFight();
			if(_type == 10)
				stats = _double.getTotalStats();
			
			return stats;
		}
		public int getPA()
		{
			if(_type == 1)
				return getTotalStats().getEffect(Constantes.STATS_ADD_PA);
			if(_type == 2)
				return getTotalStats().getEffect(Constantes.STATS_ADD_PA) + _mob.getPA();
			if(_type == 5)
				return getTotalStats().getEffect(Constantes.STATS_ADD_PM) + 6;
			if(_type == 10)
				return getTotalStats().getEffect(Constantes.STATS_ADD_PA);
			
			return 0;
		}
		public int getPM()
		{
			if(_type == 1)
				return getTotalStats().getEffect(Constantes.STATS_ADD_PM);
			if(_type == 2)
				return getTotalStats().getEffect(Constantes.STATS_ADD_PM) + _mob.getPM();
			if(_type == 5)
				return getTotalStats().getEffect(Constantes.STATS_ADD_PM) + 3;
			if(_type == 10)
				return getTotalStats().getEffect(Constantes.STATS_ADD_PM);
			
			return 0;
		}
		public int getCurPA(Pelea fight)
		{
			return fight._curFighterPA;
		}
		
		public int getCurPM(Pelea fight)
		{
			return fight._curFighterPM;
		}
		
		public void setCurPM(Pelea fight, int pm)
		{
			fight._curFighterPM = pm;
		}
		
		public void setCurPA(Pelea fight, int pa)
		{
			fight._curFighterPA = pa;
		}
		
		public void setInvocator(Fighter caster)
		{
			_invocator = caster;
		}
		
		public Fighter getInvocator()
		{
			return _invocator;
		}
		
		public boolean isInvocation()
		{
			return (_invocator!=null);
		}
		
		public boolean isPerco()
		{
			return (_Perco!=null);
		}

        public boolean isDouble()
		{
			return (_double!=null);
		}

		public void debuff()
		{
			ArrayList<EfectoHechizo> newBuffs = new ArrayList<>();
			//on v�rifie chaque buff en cours, si pas d�buffable, on l'ajout a la nouvelle liste
			for(EfectoHechizo SE : _fightBuffs)
			{
				if(!SE.isDebuffabe())newBuffs.add(SE);
				//On envoie les Packets si besoin
				switch (SE.getEffectID()) {
					case Constantes.STATS_ADD_PA, Constantes.STATS_ADD_PA2 -> GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 101, getGUID() + "", getGUID() + ",-" + SE.getValue());
					case Constantes.STATS_ADD_PM, Constantes.STATS_ADD_PM2 -> GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 127, getGUID() + "", getGUID() + ",-" + SE.getValue());
				}
			}
			_fightBuffs.clear();
			_fightBuffs.addAll(newBuffs);
			if(_perso != null && !_hasLeft)
				GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_perso);
		}

		public void fullPDV()
		{
			_PDV = _PDVMAX;
		}

		public void setIsDead(boolean b)
		{
			_isDead = b;
		}

		public void unHide(int spellid)
		{
			//on retire le buff invi
			if(spellid != -1)// -1 : CAC
			{
				switch(spellid) 
				{ 
				case 66: 
				case 71:
				case 181: 
				case 196: 
				case 200: 
				case 219: 
				return; 
				}
			}
			ArrayList<EfectoHechizo> buffs = new ArrayList<>();
			buffs.addAll(get_fightBuff());
			for(EfectoHechizo SE : buffs)
			{
				if(SE.getEffectID() == 150)
					get_fightBuff().remove(SE);
			}
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 150,getGUID()+"",getGUID()+",0");
			//On actualise la position
			GestorSalida.GAME_SEND_GIC_PACKET_TO_FIGHT(_fight, 7,this);
		}

		public int getPdvMaxOutFight()
		{
			if(_perso != null)return _perso.get_PDVMAX();
			if(_mob != null)return _mob.getPDVMAX();
			return 0;
		}

		public Map<Integer, Integer> get_chatiValue() {
			return _chatiValue;
		}

		public int getDefaultGfx()
		{
			if(_perso != null)return _perso.get_gfxID();
			if(_mob != null)return _mob.getTemplate().getGfxID();
			return 0;
		}

		public long getXpGive()
		{
			if(_mob != null)return _mob.getBaseXp();
			return 0;
		}
		public void addPDV(int max) 
		{
			_PDVMAX = (_PDVMAX+max);
			_PDV = (_PDV+max);
		}
		public boolean canLaunchSpell(int spellID) {
			if(!this.getPersonnage().hasSpell(spellID))
				return false;
			else return LaunchedSort.coolDownGood(this,spellID);
		}

	}
	
	public static class Glyphe
	{
		private final Fighter _caster;
		private final Case _cell;
		private final byte _size;
		private final int _spell;
		private final SortStats _trapSpell;
		private byte _duration;
		private final Pelea _fight;
		private final int _color;
		
		public Glyphe(Pelea fight, Fighter caster, Case cell, byte size, SortStats trapSpell, byte duration, int spell)
		{
			_fight = fight;
			_caster = caster;
			_cell =cell;
			_spell = spell;
			_size = size;
			_trapSpell = trapSpell;
			_duration = duration;
			_color = Constantes.getGlyphColor(spell);
		}

		public Case get_cell() {
			return _cell;
		}

		public byte get_size() {
			return _size;
		}

		public Fighter get_caster() {
			return _caster;
		}
		
		public byte get_duration() {
			return _duration;
		}

		public int decrementDuration()
		{
			_duration--;
			return _duration;
		}
		
		public void onTraped(Fighter target)
		{
			String str = _spell+","+_cell.getID()+",0,1,1,"+_caster.getGUID();
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 307, target.getGUID()+"", str);
			_trapSpell.applySpellEffectToFight(_fight,_caster,target.get_fightCell(),false);
			_fight.verifIfTeamAllDead();
		}

		public void desapear()
		{
			GestorSalida.GAME_SEND_GDZ_PACKET_TO_FIGHT(_fight, 7, "-",_cell.getID(), _size, _color);
			GestorSalida.GAME_SEND_GDC_PACKET_TO_FIGHT(_fight, 7, _cell.getID());
		}
		
		public int get_color()
		{
			return _color;
		}
	}
	
	public static class LaunchedSort
	{
		private int _spellId = 0;
		private int _cooldown = 0;
		private Fighter _target = null;
		
		public LaunchedSort(Fighter t,SortStats SS)
		{
			_target = t;
			_spellId = SS.getSpellID();
			_cooldown = SS.getCoolDown();
		}
		
		public void ActuCooldown()
		{
			_cooldown--;
		}
		
		public int getCooldown()
		{
			return _cooldown;
		}
		
		public int getId()
		{
			return _spellId;
		}
		
		public Fighter getTarget()
		{
			return _target;
		}
		
		public static boolean coolDownGood(Fighter fighter,int id)
		{
			
			for(LaunchedSort S : fighter.getLaunchedSorts())
			{
				if(S._spellId == id && S.getCooldown() > 0)
					return false;
			}
			return true;
		}
		
		public static int getNbLaunch(Fighter fighter,int id)
		{
			int nb = 0;
			for(LaunchedSort S : fighter.getLaunchedSorts())
			{
				if(S._spellId == id)
					nb++;
			}
			return nb;
		}
		
		public static int getNbLaunchTarget(Fighter fighter,Fighter target,int id)
		{
			int nb = 0;
			for(LaunchedSort S : fighter.getLaunchedSorts())
			{
				if(S._target == null || target == null)
					continue;
				if(S._spellId == id && S._target.getGUID() == target.getGUID())
					nb++;
			}
			return nb;
		}
		
	}
	
	private final int _id;
	private final Map<Integer,Fighter> _team0 = new TreeMap<>();
	private final Map<Integer,Fighter> _team1 = new TreeMap<>();
	private final Map<Integer,Fighter> deadList = new TreeMap<>();
	private final Map<Integer, Personaje> _spec  = new TreeMap<>();
	private Mapa _map;
	private final Mapa _mapOld;
	private final Fighter _init0;
	private Fighter _init1;
	private ArrayList<Case> _start0 = new ArrayList<>();
	private ArrayList<Case> _start1 = new ArrayList<>();
	private int _state = 0;
	private int _guildID = -1;
	private int _type = -1;
	private boolean locked0 = false;
	private boolean onlyGroup0 = false;
	private boolean locked1 = false;
	private boolean onlyGroup1 = false;
	private boolean specOk = true;
	private boolean help1 = false;
	private boolean help2 = false;
	private final int _st2;
	private final int _st1;
	private int _curPlayer;
	private long _startTime = 0;
	private int _curFighterPA;
	private int _curFighterPM;
	private int _curFighterUsedPA;
	private int _curFighterUsedPM;
	private String _curAction = "";
	private List<Fighter> _ordreJeu = new ArrayList<>();
	private Timer _turnTimer;
	private final List<Glyphe> _glyphs = new ArrayList<>();
	private final List<Piege> _traps = new ArrayList<>();
	private MobGroup _mobGroup;
	private Recaudador _perco;
	
	private final ArrayList<Fighter> _captureur = new ArrayList<>(8);	//Cr�ation d'une liste de longueur 8. Les combats contiennent un max de 8 Attaquant
	private boolean isCapturable = false;
	private int captWinner = -1;
	private PiedraAlma pierrePleine;
	private final Map<Integer, Retos> _challenges = new TreeMap<>();
	private final Map<Integer, Case> _raulebaque = new TreeMap<>();
	private long _ticMyTimer_startTime = 0L;
	private boolean _ticMyTimer_endTurn = false;
	  
	public synchronized void endAction() 
	{
		notifyAll(); 
	}
	// Fin changement
	//TIMER d�compte toutes les secondes
	/*private Timer TurnTimer (final int timer, final Percepteur perco)
	{
	    ActionListener action = new ActionListener ()
	      {
	    	int Time = timer;
	        public void actionPerformed (ActionEvent event)
	        {
	        	Time = Time-1000;
	        	if(perco != null) perco.remove_timeTurn(1000);
	        	if(Time <= 0)
	        	{
	        		startFight();
					_turnTimer.stop();
					if(perco != null) perco.set_timeTurn(45000);
					return;
	        	}
	        }
	      };
	    return new Timer (1000, action);
	 }*/
	
	public void ticMyTimer()
	  {
	      if(_startTime == 0L) {// si le combat n'a pas commenc�
	    	  // temps qui reste � s'�couler en ms
	          long timeRestant = (MainServidor.CONFIG_MS_FOR_START_FIGHT - 1L) -
	          			(System.currentTimeMillis() - _ticMyTimer_startTime); 
	          if(timeRestant <= 0L) // si tout le temps est �coul�
	          {
	        	  if(_type != Constantes.FIGHT_TYPE_CHALLENGE)
	        		  try {// on essaye de d�marrer le combat si ce n'est pas un challenge
	        			  startFight(); }
	              catch(Exception e){
	                  GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-FIGHT", "startFight(); Dans ticMyTimer() a \351chou\351. MAPID: " + get_map().getID());
	              }
	              if(_perco != null)
	            	  _perco.set_timeTurn(MainServidor.CONFIG_MS_FOR_START_FIGHT);
	          } else {// si il reste du temps
	              if(_perco != null) // et qu'il y a un perco
	            	  _perco.set_timeTurn((int)timeRestant);
	              return;
	          }
	      } else {// si le combat a commenc�
	          if(!_ticMyTimer_endTurn) // et qu'on a endturn
	        	  return;
	          long timeRestant = (MainServidor.CONFIG_MS_PER_TURN - 3L) - (System.currentTimeMillis() - _ticMyTimer_startTime); // temps qui reste avant la fin du tour en ms
	          if(timeRestant <= 0L) // si le temps est �coul�
	          {
	              _ticMyTimer_endTurn = false; // on a pas fait la fin du tour
	              _ticMyTimer_startTime = 0L; // on r�initialise le d�but du tour � 0
	              try
	              {
	                  endTurn(); // on essaye de terminer le tour
	              }
	              catch(Exception e)
	              {
	                  GestorSalida.GAME_SEND_cMK_PACKET_TO_ADMIN("@", 0, "DEBUG-FIGHT", "endTurn(); Dans ticMyTimer() a \351chou\351. MAPID: " + get_map().getID());
	              }
	          }
	      }
	  }
	
	public Pelea(int type, int id, Mapa map, Personaje init1, Personaje init2)
	{
		_type = type; //0: D�fie (4: Pvm) 1:PVP (5:Perco)
		_id = id;
		_map = map.getMapCopy();
		_mapOld = map;
		_init0 = new Fighter(this,init1);
		_init1 = new Fighter(this,init2);
		_team0.put(init1.get_GUID(), _init0);
		_team1.put(init2.get_GUID(), _init1);
		//on desactive le timer de regen cot� client
		GestorSalida.GAME_SEND_ILF_PACKET(init1, 0);
		GestorSalida.GAME_SEND_ILF_PACKET(init2, 0);
		
		int cancelBtn = _type== Constantes.FIGHT_TYPE_CHALLENGE?1:0;
		long time = _type== Constantes.FIGHT_TYPE_CHALLENGE?0: MainServidor.CONFIG_MS_FOR_START_FIGHT;
		GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,7,2, cancelBtn,1,0, time,_type);
		
		if(_type== Constantes.FIGHT_TYPE_CHALLENGE)
			_ticMyTimer_startTime = 0L;
		else
			_ticMyTimer_startTime = 45000L;
	    _ticMyTimer_endTurn = false;
		Random teams = new Random();
		if(teams.nextBoolean())
		{
			_start0 = parsePlaces(0);
			_start1 = parsePlaces(1);
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,_map.getEsquemaPelea(),0);
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,2,_map.getEsquemaPelea(),1);
			_st1 = 0;
			_st2 = 1;
		}else
		{
			_start0 = parsePlaces(1);
			_start1 = parsePlaces(0);
			_st1 = 1;
			_st2 = 0;
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,_map.getEsquemaPelea(),1);
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,2,_map.getEsquemaPelea(),0);
		}
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init1.get_GUID()+"", init1.get_GUID()+","+ Constantes.ETAT_PORTE+",0");
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init1.get_GUID()+"", init1.get_GUID()+","+ Constantes.ETAT_PORTEUR+",0");
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init2.get_GUID()+"", init2.get_GUID()+","+ Constantes.ETAT_PORTE+",0");
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init2.get_GUID()+"", init2.get_GUID()+","+ Constantes.ETAT_PORTEUR+",0");
		
		_init0.set_fightCell(getRandomCell(_start0));
		_init1.set_fightCell(getRandomCell(_start1));
		
		_init0.getPersonnage().getActualCelda().removePlayer(_init0.getGUID());
		_init1.getPersonnage().getActualCelda().removePlayer(_init1.getGUID());
		
		_init0.get_fightCell().addFighter(_init0);
		_init1.get_fightCell().addFighter(_init1);
		_init0.getPersonnage().set_fight(this);
		_init0.setTeam(0);
		_init1.getPersonnage().set_fight(this);
		_init1.setTeam(1);
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_init0.getPersonnage().getActualMapa(), _init0.getGUID());
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_init1.getPersonnage().getActualMapa(), _init1.getGUID());
		if(_type == 1)
		{
			GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),0,_init0.getGUID(),_init1.getGUID(),_init0.getPersonnage().getActualCelda().getID(),"0;"+_init0.getPersonnage().get_align(), _init1.getPersonnage().getActualCelda().getID(), "0;"+_init1.getPersonnage().get_align());
		}else
		{
			GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),0,_init0.getGUID(),_init1.getGUID(),_init0.getPersonnage().getActualCelda().getID(),"0;-1", _init1.getPersonnage().getActualCelda().getID(), "0;-1");
		}
		GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),_init0.getGUID(), _init0);
		GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),_init1.getGUID(), _init1);
		
		GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,_map);
		
		set_state(Constantes.FIGHT_STATE_PLACE);
	}
	
	public Pelea(int id, Mapa map, Personaje init1, MobGroup group)
	{
		_mobGroup = group;
		_type = Constantes.FIGHT_TYPE_PVM; //(0: D�fie) 4: Pvm (1:PVP) (5:Perco)
		_id = id;
		_map = map.getMapCopy();
		_mapOld = map;
		_init0 = new Fighter(this,init1);
		
		_team0.put(init1.get_GUID(), _init0);
		for(Entry<Integer, MobGrade> entry : group.getMobs().entrySet())
		{
			entry.getValue().setInFightID(entry.getKey());
			Fighter mob = new Fighter(this,entry.getValue());
			_team1.put(entry.getKey(), mob);
		}
		//on desactive le timer de regen cot� client
		GestorSalida.GAME_SEND_ILF_PACKET(init1, 0);
		
		// on envoie le timer ?
		GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,1,2,0,1,0, MainServidor.CONFIG_MS_FOR_START_FIGHT,_type);
		
		
		//_turnTimer = TurnTimer(45000, null);
		//_turnTimer.start();
		_ticMyTimer_startTime = System.currentTimeMillis();
	    _ticMyTimer_endTurn = false;
	    
		Random teams = new Random();
		if(teams.nextBoolean())
		{
			_start0 = parsePlaces(0);
			_start1 = parsePlaces(1);
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,_map.getEsquemaPelea(),0);
			_st1 = 0;
			_st2 = 1;
		}else
		{
			_start0 = parsePlaces(1);
			_start1 = parsePlaces(0);
			_st1 = 1;
			_st2 = 0;
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,_map.getEsquemaPelea(),1);
		}
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init1.get_GUID()+"", init1.get_GUID()+","+ Constantes.ETAT_PORTE+",0");
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init1.get_GUID()+"", init1.get_GUID()+","+ Constantes.ETAT_PORTEUR+",0");
		
		List<Entry<Integer, Fighter>> e = new ArrayList<>();
		e.addAll(_team1.entrySet());
		for(Entry<Integer,Fighter> entry : e)
		{
			Fighter f = entry.getValue();
			Case cell = getRandomCell(_start1);
			if(cell == null)
			{
				_team1.remove(f.getGUID());
				continue;
			}
			
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, f.getGUID()+"", f.getGUID()+","+ Constantes.ETAT_PORTE+",0");
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, f.getGUID()+"", f.getGUID()+","+ Constantes.ETAT_PORTEUR+",0");
			f.set_fightCell(cell);
			f.get_fightCell().addFighter(f);
			f.setTeam(1);
			f.fullPDV();
		}
		_init0.set_fightCell(getRandomCell(_start0));
		
		_init0.getPersonnage().getActualCelda().removePlayer(_init0.getPersonnage().get_GUID());
		
		_init0.get_fightCell().addFighter(_init0);
		
		_init0.getPersonnage().set_fight(this);
		_init0.setTeam(0);
		
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_init0.getPersonnage().getActualMapa(), _init0.getGUID());
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_init0.getPersonnage().getActualMapa(), group.getID());
		
		GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),4,_init0.getGUID(),group.getID(),(_init0.getPersonnage().getActualCelda().getID()+1),"0;-1",group.getCeldaID(),"1;-1");
		GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),_init0.getGUID(), _init0);
		
		for(Fighter f : _team1.values())
		{
			GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),group.getID(), f);
		}
		
		GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,_map);
		
		set_state(Constantes.FIGHT_STATE_PLACE);
	}

	public Pelea(int id, Mapa map, Personaje perso, Recaudador perco)
	{	
		set_guildID(perco.get_guildID());
		perco.set_inFight((byte)1);
		perco.set_inFightID((byte)id);
		
		_type = Constantes.FIGHT_TYPE_PVT; //(0: D�fie) (4: Pvm) (1:PVP) 5:Perco
		_id = id;
		_map = map.getMapCopy();
		_mapOld = map;
		_init0 = new Fighter(this,perso);
		_ticMyTimer_startTime = 0L;
	    _ticMyTimer_endTurn = false;
	    _perco = perco;
	    //on desactive le timer de regen cot� client
	  	GestorSalida.GAME_SEND_ILF_PACKET(perso, 0);
	  		
		_team0.put(perso.get_GUID(), _init0);

		Fighter percoF = new Fighter(this,perco);
		_team1.put(-1, percoF);

		GestorSalida.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,1,2,0,1,0, MainServidor.CONFIG_MS_FOR_START_FIGHT,_type); //  timer de combat
		
		
		//_turnTimer = TurnTimer(45000, perco);
		//_turnTimer.start();
		_ticMyTimer_startTime = System.currentTimeMillis();
		Random teams = new Random();
		if(teams.nextBoolean())
		{
			_start0 = parsePlaces(0);
			_start1 = parsePlaces(1);
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,_map.getEsquemaPelea(),0);
			_st1 = 0;
			_st2 = 1;
		}else
		{
			_start0 = parsePlaces(1);
			_start1 = parsePlaces(0);
			_st1 = 1;
			_st2 = 0;
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,_map.getEsquemaPelea(),1);
		}
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_PORTE+",0");
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_PORTEUR+",0");
		
		List<Entry<Integer, Fighter>> e = new ArrayList<>();
		e.addAll(_team1.entrySet());
		for(Entry<Integer,Fighter> entry : e)
		{
			Fighter f = entry.getValue();
			Case cell = getRandomCell(_start1);
			if(cell == null)
			{
				_team1.remove(f.getGUID());
				continue;
			}
			
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, f.getGUID()+"", f.getGUID()+","+ Constantes.ETAT_PORTE+",0");
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, f.getGUID()+"", f.getGUID()+","+ Constantes.ETAT_PORTEUR+",0");
			f.set_fightCell(cell);
			f.get_fightCell().addFighter(f);
			f.setTeam(1);
			f.fullPDV();
		}
		_init0.set_fightCell(getRandomCell(_start0));
		
		_init0.getPersonnage().getActualCelda().removePlayer(_init0.getPersonnage().get_GUID());
		
		_init0.get_fightCell().addFighter(_init0);
		
		_init0.getPersonnage().set_fight(this);
		_init0.setTeam(0);
		
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_init0.getPersonnage().getActualMapa(), _init0.getGUID());
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_init0.getPersonnage().getActualMapa(), perco.getGuid());
		
		GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),5,_init0.getGUID(),perco.getGuid(),(_init0.getPersonnage().getActualCelda().getID()+1),"0;-1",perco.get_cellID(),"3;-1");
		GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),_init0.getGUID(), _init0);
		
		for(Fighter f : _team1.values())
		{
			GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),perco.getGuid(), f);
		}

		GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,_map);
		set_state(Constantes.FIGHT_STATE_PLACE);
		
		//On actualise la guilde+Message d'attaque FIXME
		for(Personaje z : Mundo.getGuild(_guildID).getMembers())
		{
			if(z == null) continue;
			if(z.isConectado())
			{
				GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parsetoGuild(z.get_guild().get_id()));
				Recaudador.parseAttaque(z, _guildID);
				Recaudador.parseDefense(z, _guildID);
				GestorSalida.GAME_SEND_MESSAGE(z, "Un de vos percepteurs est attaque !", MainServidor.CONFIG_MOTD_COLOR);
			}
		}
	}
	
	public Mapa get_map() {
		return _map;
	}

	public List<Piege> get_traps() {
		return _traps;
	}

	public List<Glyphe> get_glyphs() {
		return _glyphs;
	}

	private Case getRandomCell(List<Case> cells)
	{
		Random rand = new Random();
		Case cell;
		if(cells.isEmpty())return null;
		int limit = 0;
		do
		{
			int id = rand.nextInt(cells.size()-1);
			cell = cells.get(id);
			limit++;
		}while((cell == null || !cell.getFighters().isEmpty()) && limit < 80);
		if(limit == 80)
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Case non trouve dans la liste");
			return null;
		}
		return cell;		
	}
	
	private ArrayList<Case> parsePlaces(int num)
	{
		return GestorEncriptador.parseStartCell(_map, num);
	}
	
	public int get_id() {
		return _id;
	}

	public ArrayList<Fighter> getFighters(int teams)//teams entre 0 et 7, binaire([spec][t2][t1]);
	{
		ArrayList<Fighter> fighters = new ArrayList<>();
		
		if(teams - 4 >= 0)
		{
			for(Entry<Integer, Personaje> entry : _spec.entrySet())
			{
				fighters.add(new Fighter(this,entry.getValue()));
			}
			teams -= 4;
		}
		if(teams -2 >= 0)
		{
			for(Entry<Integer,Fighter> entry : _team1.entrySet())
			{
				fighters.add(entry.getValue());
			}
			teams -= 2;
		}
		if(teams -1 >=0)
		{	
			for(Entry<Integer,Fighter> entry : _team0.entrySet())
			{
				fighters.add(entry.getValue());
			}
		}
		return fighters;
	}
	
	public synchronized void changePlace(Personaje perso, int cell)
	{
		Fighter fighter = getFighterByPerso(perso);
		int team = getTeamID(perso.get_GUID()) -1;
		if(fighter == null)return;
		if(get_state() != 2 || isOccuped(cell) || perso.is_ready() || (team == 0 && !groupCellContains(_start0,cell)) || (team == 1 && !groupCellContains(_start1,cell)))return;

		fighter.get_fightCell().getFighters().clear();
		fighter.set_fightCell(_map.getMapa(cell));
		
		_map.getMapa(cell).addFighter(fighter);
		GestorSalida.GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(this,3,_map,perso.get_GUID(),cell);
	}

	public boolean isOccuped(int cell)
	{
		/* ex Code
		for(Entry<Integer,Fighter> entry : _team0.entrySet())
		{
			if(entry.getValue().getPDV() <= 0)continue;
			if(entry.getValue().get_fightCell().getID() == cell)
				return true;
		}
		for(Entry<Integer,Fighter> entry : _team1.entrySet())
		{
			if(entry.getValue().getPDV() <= 0)continue;
			if(entry.getValue().get_fightCell().getID() == cell)
				return true;
		}
		//*/
		return _map.getMapa(cell).getFighters().size() > 0;
	}

	private boolean groupCellContains(ArrayList<Case> cells, int cell)
	{
		for (Case aCase : cells) {
			if (aCase.getID() == cell)
				return true;
		}
		return false;
	}

	public void verifIfAllReady()
	{
		boolean val = true;
		for(int a=0;a<_team0.size();a++)
		{
			if(!_team0.get(_team0.keySet().toArray()[a]).getPersonnage().is_ready())
				val = false;
		}
		if(_type != Constantes.FIGHT_TYPE_PVM && _type != Constantes.FIGHT_TYPE_PVT)
		{
			for(int a=0;a<_team1.size();a++)
			{
				if(!_team1.get(_team1.keySet().toArray()[a]).getPersonnage().is_ready())
					val = false;
			}
		}
		if(_type == Constantes.FIGHT_TYPE_PVT)
			val = false;//Evite de lancer le combat trop vite
		if(val)
		{
			startFight();
		}
	}

	private void startFight()
	{
		if(_state >= Constantes.FIGHT_STATE_ACTIVE)
			return;
		if(_type == Constantes.FIGHT_TYPE_PVT)
		{
			_perco.set_inFight((byte)2);
			//On actualise la guilde+Message d'attaque FIXME
			String packet = Recaudador.parsetoGuild(_guildID);
			for(Personaje z : Mundo.getGuild(_guildID).getMembers())
			{
				if(z == null) continue;
				if(z.isConectado())
				{
					GestorSalida.GAME_SEND_gITM_PACKET(z, packet);
					Recaudador.parseAttaque(z, _guildID);
					Recaudador.parseDefense(z, _guildID);
					GestorSalida.GAME_SEND_MESSAGE(z, "Un de vos percepteurs est rentre en combat.", MainServidor.CONFIG_MOTD_COLOR);
				}
			}
		}
		_state = Constantes.FIGHT_STATE_ACTIVE;
		_startTime = System.currentTimeMillis();
		GestorSalida.GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(),_init0.getGUID());
		if(_type == Constantes.FIGHT_TYPE_PVM)
		{
			int align = -1;
			if(_team1.size() >0)
			{
				 _team1.get(_team1.keySet().toArray()[0]).getMob().getTemplate().getAlign();
			}
			//Si groupe non fixe
			if(!_mobGroup.isFix()) Mundo.getCarte(_map.getID()).spawnGroup(align, 1, true,_mobGroup.getCeldaID());//Respawn d'un groupe
		}
		GestorSalida.GAME_SEND_GIC_PACKETS_TO_FIGHT(this, 7);
		GestorSalida.GAME_SEND_GS_PACKET_TO_FIGHT(this, 7);
		InitOrdreJeu();
		_curPlayer = -1;
		GestorSalida.GAME_SEND_GTL_PACKET_TO_FIGHT(this,7);
		GestorSalida.GAME_SEND_GTM_PACKET_TO_FIGHT(this, 7);
		if(_turnTimer  != null)_turnTimer.stop();
		_turnTimer = null;
		_turnTimer = new Timer(Constantes.TIME_BY_TURN, e -> endTurn());
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Debut du combat");
		for(Fighter F : getFighters(3))
		{
			Personaje perso = F.getPersonnage();
			if(perso == null)continue;
			if(perso.isOnMount())
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_CHEVAUCHANT+",1");
			
		}
		
		try
		{
	      if (this._type == 4)
	      {
	    	  
	    	  boolean hasMale = false, hasFemale = false;
	    	  boolean  hasCawotte = false, hasChafer = false, hasRoulette = false, hasArakne = false;
	    	  boolean hasBoss = false, inDungeon = false;
	    	  for(Fighter f : _team0.values()) {
	    		  if(f.getPersonnage() != null) {
	    			  Personaje perso = f.getPersonnage();
	    			  if(perso.hasSpell(367))
	    				  hasCawotte = true;
	    			  if(perso.hasSpell(373))
	    				  hasChafer = true;
	    			  if(perso.hasSpell(101))
	    				  hasRoulette = true;
	    			  if(perso.hasSpell(370))
	    				  hasArakne = true;
	    			  if(perso.getSexo() == 0)
	    				  hasMale = true;
	    			  if(perso.getSexo() == 1)
	    				  hasFemale = true;
	    			  if(perso.getActualMapa().hasEndFightAction(_type))
	    				  inDungeon = true;
	    		  }
	    	  }
	    	  //BR,tournesol affam�, Mob l'�ponge, scara dor�, bworker, blops royaux, wa wab, 
	    	  //rat noir, rat blanc, spincter, skeunk, croca, toror, tot, meulou, DC, CM, AA
	    	  //Ougah, Krala
	    	  String IDisBoss = ";147;799;928;1001;797;478;1184;1185;1186;1187;1188;180;939;940;943;780;854;121;827;232;113;257;173;1159;423;";
	    	  for(Fighter f : _team1.values()) {
	    		  if(IDisBoss.contains(";"+f.getMob().getTemplate().getID()+";"))
	    			  hasBoss = true;
	    	  }
	    	  
	    	  boolean severalEnnemies, severalAllies, bothSexes, EvenEnnemies, MoreEnnemies;
	    	  severalEnnemies = (_team1.size() < 2 ? false : true);
	    	  severalAllies = (_team0.size() < 2 ? false : true);
	    	  bothSexes = (!hasMale || !hasFemale ? false : true);
	    	  EvenEnnemies = (_team1.size() % 2 == 0 ? true : false);
	    	  MoreEnnemies = (_team1.size() < _team0.size() ? false : true);
	    	 
	    	  String challenges = Mundo.getChallengeFromConditions(severalEnnemies,
	    			  severalAllies, bothSexes, EvenEnnemies, MoreEnnemies, 
	    			  hasCawotte, hasChafer, hasRoulette, hasArakne, hasBoss);
	    	  
	    	  String[] chalInfo;
	    	  int challengeID, challengeXP, challengeDP, bonusGroupe;
	    	  int challengeNumber = (inDungeon ? MainServidor.CONFIG_INDUNGEON_CHALLENGE : MainServidor.CONFIG_CHALLENGE_NUMBER);
	    	  
	    	  for(String chalInfos : Mundo.getRandomChallenge(challengeNumber, challenges)) {
	    		  chalInfo = chalInfos.split(",");
	    		  challengeID = Integer.parseInt(chalInfo[0]);
	    		  challengeXP = Integer.parseInt(chalInfo[1]);
	    		  challengeDP = Integer.parseInt(chalInfo[2]);
	    		  bonusGroupe = Integer.parseInt(chalInfo[3]);
	    		  bonusGroupe *= this._team1.size();
	    		  this._challenges.put(challengeID, new Retos(this, challengeID, challengeXP+bonusGroupe, challengeDP+bonusGroupe));
	    	  }	    	  
	    	  
	    	  for (Map.Entry<Integer, Retos> c : this._challenges.entrySet())
	    	  {
	    		  if (c.getValue() == null)
	    			  continue;
	    		  c.getValue().onFight_start();
	    		  GestorSalida.GAME_SEND_PACKET_TO_FIGHT(this, 7, c.getValue().parseToPacket());
	    	  }
	    	  
	       	}
	      
		}
		catch (Exception localException)
		{
			localException.printStackTrace(System.out);
		}
		startTurn();
	     	
		for(Fighter F : getFighters(3)){
			if (F == null) 
				continue;
			_raulebaque.put(F.getGUID(), F.get_fightCell());
		}
	}


	private void startTurn()
	{
		if(!verifyStillInFight()) verifIfTeamAllDead();
		
		if(_state >= Constantes.FIGHT_STATE_FINISHED)return;
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {e1.printStackTrace();}
		
		_curPlayer++;
		_curAction = "";
		if(_curPlayer >= _ordreJeu.size())_curPlayer = 0;
		
		_curFighterPA = _ordreJeu.get(_curPlayer).getPA();
		_curFighterPM = _ordreJeu.get(_curPlayer).getPM();
		_curFighterUsedPA = 0;
		_curFighterUsedPM = 0;
		_ticMyTimer_startTime = System.currentTimeMillis();
		_ticMyTimer_endTurn = true;
		
		if(_ordreJeu.get(_curPlayer).hasLeft() || _ordreJeu.get(_curPlayer).isDead())//Si joueur mort
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Fighter ID=  "+_ordreJeu.get(_curPlayer).getGUID()+" est mort");
			endTurn();
			return;
		}
		
		_ordreJeu.get(_curPlayer).applyBeginningTurnBuff(this);
		if(_state == Constantes.FIGHT_STATE_FINISHED)return;
		if(_ordreJeu.get(_curPlayer).getPDV()<=0)onFighterDie(_ordreJeu.get(_curPlayer), _ordreJeu.get(_curPlayer));
		
		//On actualise les sorts launch
		_ordreJeu.get(_curPlayer).ActualiseLaunchedSort();
		//reset des Max des Chatis
		_ordreJeu.get(_curPlayer).get_chatiValue().clear();
		//Gestion des glyphes
		ArrayList<Glyphe> glyphs = new ArrayList<>();//Copie du tableau
		glyphs.addAll(_glyphs);
		
		for(Glyphe g : glyphs)
		{
			if(_state >= Constantes.FIGHT_STATE_FINISHED)return;
			//Si c'est ce joueur qui l'a lanc�
			if(g.get_caster().getGUID() == _ordreJeu.get(_curPlayer).getGUID())
			{
				//on r�duit la dur�e restante, et si 0, on supprime
				if(g.decrementDuration() == 0)
				{
					_glyphs.remove(g);
					g.desapear();
					continue;//Continue pour pas que le joueur active le glyphe s'il �tait dessus
				}
			}
			//Si dans le glyphe
			int dist = Camino.getDistanceBetween(_map,_ordreJeu.get(_curPlayer).get_fightCell().getID() , g.get_cell().getID());
			if(dist <= g.get_size() && g._spell != 476)//476 a effet en fin de tour
			{
				//Alors le joueur est dans le glyphe
				g.onTraped(_ordreJeu.get(_curPlayer));
			}
		}
		if(_ordreJeu == null)return;
		if(_ordreJeu.size() < _curPlayer)return;
		if(_ordreJeu.get(_curPlayer) == null)return;
		if(_ordreJeu.get(_curPlayer).isDead())//Si joueur mort
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Fighter ID=  "+_ordreJeu.get(_curPlayer).getGUID()+" est mort");
			endTurn();
			return;
		}
		if(_ordreJeu.get(_curPlayer).getPersonnage() != null)
		{
			GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_ordreJeu.get(_curPlayer).getPersonnage());
		}
		if(_ordreJeu.get(_curPlayer).hasBuff(Constantes.EFFECT_PASS_TURN))//Si il doit passer son tour
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Fighter ID= "+_ordreJeu.get(_curPlayer).getGUID()+" passe son tour");
			endTurn();
			return;
		}
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+")Debut du tour de Fighter ID= "+_ordreJeu.get(_curPlayer).getGUID());
		GestorSalida.GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(this,7,_ordreJeu.get(_curPlayer).getGUID(), Constantes.TIME_BY_TURN);
		_turnTimer.restart();
		try {
			Thread.sleep(650);
		} catch (InterruptedException e1) {e1.printStackTrace();}
		_ordreJeu.get(_curPlayer).setCanPlay(true);
		
		if(_ordreJeu.get(_curPlayer).getPersonnage() == null 
				|| _ordreJeu.get(_curPlayer)._double != null 
				|| _ordreJeu.get(_curPlayer)._Perco != null)//Si ce n'est pas un joueur
		{
			new Inteligencia.IAThread(_ordreJeu.get(_curPlayer),this);
		}
		try
		{
			if ((this._type == 4) && (this._challenges.size() > 0) && !this._ordreJeu.get(this._curPlayer).isInvocation() && !this._ordreJeu.get(this._curPlayer).isDouble() && !this._ordreJeu.get(this._curPlayer).isPerco())
			{
				for (Entry<Integer, Retos> c : this._challenges.entrySet()) {
					if (c.getValue() == null) 
						continue;
					c.getValue().onPlayer_startTurn(this._ordreJeu.get(this._curPlayer));
				}
			}
		} catch (Exception e) {
			System.out.println("-----------------------Erreur challenge (startTurn())");
			e.printStackTrace(System.out);
		}
	}

	public void endTurn()
	{
		
		try
		{
			if(_curPlayer == -1)return;
			//_ticMyTimer_startTime = 0L;
		    _ticMyTimer_endTurn = false;
		    if(_ordreJeu == null || _ordreJeu.get(_curPlayer) == null)return;
			if(_state >= Constantes.FIGHT_STATE_FINISHED)return;
			if(_ordreJeu.get(_curPlayer).hasLeft() || _ordreJeu.get(_curPlayer).isDead())
			{
				startTurn();
				return;
			}
				
			_turnTimer.stop();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			
			if(!_curAction.equals("") && _ordreJeu.get(_curPlayer).getPersonnage() != null)
			{
				while(!_curAction.isEmpty()){}
			}
			
			GestorSalida.GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(this,7,_ordreJeu.get(_curPlayer).getGUID());
			
			_ordreJeu.get(_curPlayer).setCanPlay(false);
			_curAction = "";
			
			//Si empoisonn� (Cr�er une fonction applyEndTurnbuff si d'autres effets existent)
			for(EfectoHechizo SE : _ordreJeu.get(_curPlayer).getBuffsByEffectID(131))
			{
				int pas = SE.getValue();
				int val = -1;
				try
				{
					val = Integer.parseInt(SE.getArgs().split(";")[1]);
				}catch(Exception ignored){}
				if(val == -1)continue;
				
				int nbr = (int) Math.floor((double)_curFighterUsedPA/(double)pas);
				int dgt = val * nbr;
				//Si poison paralysant
				if(SE.getSpell() == 200)
				{
					int inte = SE.getCaster().getTotalStats().getEffect(Constantes.STATS_ADD_INTE);
					if(inte < 0)inte = 0;
					int pdom = SE.getCaster().getTotalStats().getEffect(Constantes.STATS_ADD_PERDOM);
					if(pdom < 0)pdom = 0;
					//on applique le boost
					dgt = ((100+inte+pdom)/100) * dgt;
				}
				if(_ordreJeu.get(_curPlayer).hasBuff(184))
				{
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 105, _ordreJeu.get(_curPlayer).getGUID()+"", _ordreJeu.get(_curPlayer).getGUID()+","+_ordreJeu.get(_curPlayer).getBuff(184).getValue());
					dgt = dgt-_ordreJeu.get(_curPlayer).getBuff(184).getValue();//R�duction physique
				}
				if(_ordreJeu.get(_curPlayer).hasBuff(105))
				{
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 105, _ordreJeu.get(_curPlayer).getGUID()+"", _ordreJeu.get(_curPlayer).getGUID()+","+_ordreJeu.get(_curPlayer).getBuff(105).getValue());
					dgt = dgt-_ordreJeu.get(_curPlayer).getBuff(105).getValue();//Immu
				}
				if(dgt <= 0)continue;
				
				if(dgt>_ordreJeu.get(_curPlayer).getPDV())dgt = _ordreJeu.get(_curPlayer).getPDV();//va mourrir
				_ordreJeu.get(_curPlayer).removePDV(dgt);
				dgt = -(dgt);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 100, SE.getCaster().getGUID()+"", _ordreJeu.get(_curPlayer).getGUID()+","+dgt);
				
			}
			ArrayList<Glyphe> glyphs = new ArrayList<>();//Copie du tableau
			glyphs.addAll(_glyphs);
			for(Glyphe g : glyphs)
			{
				if(_state >= Constantes.FIGHT_STATE_FINISHED)return;
				//Si dans le glyphe
				int dist = Camino.getDistanceBetween(_map,_ordreJeu.get(_curPlayer).get_fightCell().getID() , g.get_cell().getID());
				if(dist <= g.get_size() && g._spell == 476)//476 a effet en fin de tour
				{
					//Alors le joueur est dans le glyphe
					g.onTraped(_ordreJeu.get(_curPlayer));
				}
			}
			if(_ordreJeu.get(_curPlayer).getPDV() <= 0)onFighterDie(_ordreJeu.get(_curPlayer), _ordreJeu.get(_curPlayer));
			
			if ((this._type == 4) && (this._challenges.size() > 0) && !this._ordreJeu.get(this._curPlayer).isInvocation() && !this._ordreJeu.get(this._curPlayer).isDouble() && !this._ordreJeu.get(this._curPlayer).isPerco() && (this._ordreJeu.get(this._curPlayer).getTeam() == 0))
	        {
	        	for (Map.Entry<Integer, Retos> c : this._challenges.entrySet()) {
	        		if (c.getValue() == null) 
	        			continue; 
	        		c.getValue().onPlayer_endTurn(this._ordreJeu.get(this._curPlayer));
	         	}
	        }
			//reset des valeurs
			_curFighterUsedPA = 0;
			_curFighterUsedPM = 0;
			_curFighterPA = _ordreJeu.get(_curPlayer).getTotalStats().getEffect(Constantes.STATS_ADD_PA);
			_curFighterPM = _ordreJeu.get(_curPlayer).getTotalStats().getEffect(Constantes.STATS_ADD_PM);
			_ordreJeu.get(_curPlayer).refreshfightBuff();
			if(_ordreJeu.get(_curPlayer).getPersonnage() != null)
				if(_ordreJeu.get(_curPlayer).getPersonnage().isConectado())
					GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(_ordreJeu.get(_curPlayer).getPersonnage());
			
			GestorSalida.GAME_SEND_GTM_PACKET_TO_FIGHT(this, 7);
			GestorSalida.GAME_SEND_GTR_PACKET_TO_FIGHT(this, 7, _ordreJeu.get(_curPlayer==_ordreJeu.size()?0:_curPlayer).getGUID());
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+")Fin du tour de Fighter ID= "+_ordreJeu.get(_curPlayer).getGUID());
			startTurn();
		}catch(NullPointerException e)
		{
			e.printStackTrace();
			endTurn();
		}
	}

	private void InitOrdreJeu()
	{
		int curMaxIni = 0;
		Fighter curMax = null;
		boolean team1_ready = false;
		boolean team2_ready = false;
		byte actTeam = -1;
		do
		{
			if((actTeam == -1 || actTeam == 0 || team2_ready) && !team1_ready) 
			{
				team1_ready = true;
				for(Entry<Integer,Fighter> entry : _team0.entrySet())
				{
					if(_ordreJeu.contains(entry.getValue()))
						continue;
					team1_ready = false;
					if(entry.getValue().getInitiative() >= curMaxIni)
					{
						curMaxIni = entry.getValue().getInitiative();
						curMax = entry.getValue();
					}
				}
			}		
			if((actTeam == -1 || actTeam == 1 || team1_ready) && !team2_ready) 
			{
				team2_ready = true;
				for(Entry<Integer,Fighter> entry : _team1.entrySet())
				{
					if(_ordreJeu.contains(entry.getValue()))
						continue;
					team2_ready = false;
					if(entry.getValue().getInitiative() >= curMaxIni)
					{
						curMaxIni = entry.getValue().getInitiative();
						curMax = entry.getValue();
					}
				}
			}
				if(curMax == null)return;
				_ordreJeu.add(curMax);
				if(curMax.getTeam() == 0) 
					actTeam = 1; 
				else 
					actTeam = 0; 
				curMaxIni = 0;
				curMax = null;
		}while(_ordreJeu.size() != getFighters(3).size());
	}

	public void joinFight(Personaje perso, int guid)
	{	
		long timeRestant = MainServidor.CONFIG_MS_FOR_START_FIGHT - (System.currentTimeMillis() - _ticMyTimer_startTime);
		Fighter current_Join = null;
		if(_team0.containsKey(guid))
		{
			Case cell = getRandomCell(_start0);
			if(cell == null)return;
			
			if(onlyGroup0)
			{
				Grupo g = _init0.getPersonnage().getActualGrupo();
				if(g != null)
				{
					if(!g.getMiembrosGrupo().contains(perso))
					{
						GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
						return;
					}
				}
			}
			if(_type == Constantes.FIGHT_TYPE_AGRESSION)
			{
				if(perso.get_align() == Constantes.ALIGNEMENT_NEUTRE)
				{
					GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
					return;
				}
				if(_init0.getPersonnage().get_align() != perso.get_align())
				{
					GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
					return;
				}
			}
			if(_guildID > -1 && perso.get_guild() != null)
			{
				if(get_guildID() == perso.get_guild().get_id()) 
				{
					GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
					return;
				}
			}
			if(locked0)
			{
				GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
				return;
			}
			if(_type == Constantes.FIGHT_TYPE_CHALLENGE)
			{
				GestorSalida.GAME_SEND_GJK_PACKET(perso,2,1,1,0,timeRestant,_type);
			}else
			{
				GestorSalida.GAME_SEND_GJK_PACKET(perso,2,0,1,0,timeRestant,_type);
			}
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET(perso.getCuenta().getGameThread().get_out(), _map.getEsquemaPelea(), _st1);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_PORTE+",0");
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_PORTEUR+",0");
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getActualMapa(), perso.get_GUID());
			
			Fighter f = new Fighter(this, perso);
			current_Join = f;
			f.setTeam(0);
			_team0.put(perso.get_GUID(), f);
			perso.set_fight(this);
			f.set_fightCell(cell);
			f.get_fightCell().addFighter(f);
			//D�sactive le timer de regen
			GestorSalida.GAME_SEND_ILF_PACKET(perso, 0);
		}else if(_team1.containsKey(guid))
		{
			Case cell = getRandomCell(_start1);
			if(cell == null)return;
			
			if(onlyGroup1)
			{
				Grupo g = _init1.getPersonnage().getActualGrupo();
				if(g != null)
				{
					if(!g.getMiembrosGrupo().contains(perso))
					{
						GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
						return;
					}
				}
			}
			if(_type == Constantes.FIGHT_TYPE_AGRESSION)
			{
				if(perso.get_align() == Constantes.ALIGNEMENT_NEUTRE)
				{
					GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
					return;
				}
				if(_init1.getPersonnage().get_align() != perso.get_align())
				{
					GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
					return;
				}
			}
			if(_guildID > -1 && perso.get_guild() != null)
			{
				if(get_guildID() == perso.get_guild().get_id()) 
				{
					GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
					return;
				}
			}
			if(locked1)
			{
				GestorSalida.GAME_SEND_GA903_ERROR_PACKET(perso.getCuenta().getGameThread().get_out(),'f',guid);
				return;
			}
			if(_type == Constantes.FIGHT_TYPE_CHALLENGE)
			{
				GestorSalida.GAME_SEND_GJK_PACKET(perso,2,1,1,0,0,_type);
			}else
			{
				GestorSalida.GAME_SEND_GJK_PACKET(perso,2,0,1,0,0,_type);
			}
			GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET(perso.getCuenta().getGameThread().get_out(), _map.getEsquemaPelea(), _st2);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_PORTE+",0");
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_PORTEUR+",0");
			GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getActualMapa(), perso.get_GUID());
			Fighter f = new Fighter(this, perso);
			current_Join = f;
			f.setTeam(1);
			_team1.put(perso.get_GUID(), f);
			perso.set_fight(this);
			f.set_fightCell(cell);
			f.get_fightCell().addFighter(f);
		}
		perso.getActualCelda().removePlayer(perso.get_GUID());
		GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(perso.getActualMapa(),(current_Join.getTeam()==0?_init0:_init1).getGUID(), current_Join);
		GestorSalida.GAME_SEND_FIGHT_PLAYER_JOIN(this,7,current_Join);
		GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,_map,perso);
		if(_perco != null)
		{
			for(Personaje z : Mundo.getGuild(_guildID).getMembers())
			{
				if(z.isConectado())
				{
					Recaudador.parseAttaque(z, _guildID);
					Recaudador.parseDefense(z, _guildID);
				}
			}
		}
	}
	
	public void joinPercepteurFight(Personaje perso, int guid, int percoID)
	{	
		try {
			Thread.sleep(700);
		} catch (InterruptedException ignored) {}
		Fighter current_Join = null;
		Case cell = getRandomCell(_start1);
		if(cell == null)return;
		GestorSalida.GAME_SEND_GJK_PACKET(perso,2,0,1,0,0,_type);
		GestorSalida.GAME_SEND_FIGHT_PLACES_PACKET(perso.getCuenta().getGameThread().get_out(), _map.getEsquemaPelea(), _st2);
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_PORTE+",0");
		GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.get_GUID()+"", perso.get_GUID()+","+ Constantes.ETAT_PORTEUR+",0");
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getActualMapa(), perso.get_GUID());
		Fighter f = new Fighter(this, perso);
		current_Join = f;
		f.setTeam(1);
		_team1.put(perso.get_GUID(), f);
		perso.set_fight(this);
		f.set_fightCell(cell);
		f.get_fightCell().addFighter(f);
		GestorSalida.GAME_SEND_ILF_PACKET(perso, 0);
		
		perso.getActualCelda().removePlayer(perso.get_GUID());
		GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(perso.getActualMapa(), percoID, current_Join);
		GestorSalida.GAME_SEND_FIGHT_PLAYER_JOIN(this,7,current_Join);
		GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,_map,perso);
	}

	public void toggleLockTeam(int guid)
	{
		if(_init0 != null && _init0.getGUID() == guid)
		{
			locked0 = !locked0;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(locked0?"L'equipe 1 devient bloquee":"L'equipe 1 n'est plus bloquee");
			GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(), locked0?'+':'-', 'A', guid);
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(this,1,locked0?"095":"096");
		}else if(_init1 != null && _init1.getGUID() == guid)
		{
			locked1 = !locked1;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(locked1?"L'equipe 2 devient bloquee":"L'equipe 2 n'est plus bloquee");
			GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(_init1.getPersonnage().getActualMapa(), locked1?'+':'-', 'A', guid);
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(this,2,locked1?"095":"096");
		}
	}
	
	public void toggleOnlyGroup(int guid)
	{
		if(_init0 != null && _init0.getGUID() == guid)
		{
			onlyGroup0 = !onlyGroup0;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(locked0?"L'equipe 1 n'accepte que les membres du groupe":"L'equipe 1 n'est plus bloquee");
			GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(), onlyGroup0?'+':'-', 'P', guid);
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(this,1,onlyGroup0?"093":"094");
		}else if(_init1 != null && _init1.getGUID() == guid)
		{
			onlyGroup1 = !onlyGroup1;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(locked1?"L'equipe 2 n'accepte que les membres du groupe":"L'equipe 2 n'est plus bloquee");
			GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(_init1.getPersonnage().getActualMapa(), onlyGroup1?'+':'-', 'P', guid);
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(this,2,onlyGroup1?"095":"096");
		}
	}
	
	public void toggleLockSpec(int guid)
	{
		if((_init0 != null && _init0.getGUID() == guid) || (_init1 != null &&  _init1.getGUID() == guid))
		{
			specOk = !specOk;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(specOk?"Le combat accepte les spectateurs":"Le combat n'accepte plus les spectateurs");
			GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(), specOk?'+':'-', 'S', _init0.getGUID());
			GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(), specOk?'+':'-', 'S', _init1.getGUID());
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_AL_MAPA(_map,specOk?"039":"040");
		}
	}

	public void toggleHelp(int guid)
	{
		if(_init0 != null && _init0.getGUID() == guid)
		{
			help1 = !help1;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(help2?"L'equipe 1 demande de l'aide":"L'equipe 1s ne demande plus d'aide");
			GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(_init0.getPersonnage().getActualMapa(), locked0?'+':'-', 'H', guid);
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(this,1,help1?"0103":"0104");
		}else if(_init1 != null && _init1.getGUID() == guid)
		{
			help2 = !help2;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(help2?"L'equipe 2 demande de l'aide":"L'equipe 2 ne demande plus d'aide");
			GestorSalida.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(_init1.getPersonnage().getActualMapa(), locked1?'+':'-', 'H', guid);
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(this,2,help2?"0103":"0104");
		}
	}
	
	private void set_state(int _state) {
		this._state = _state;
	}
	
	private void set_guildID(int guildID) {
		this._guildID = guildID;
	}

	public int get_state() {
		return _state;
	}
	
	public int get_guildID() {
		return _guildID;
	}
	
	public int get_type() {
		return _type;
	}

	public List<Fighter> get_ordreJeu() {
		return _ordreJeu;
	}
	
	public Map<Integer, Case> get_raulebaque() {
		return _raulebaque;
	}
	
	public Map<Integer, Retos> get_challenges()
	  {
	    return this._challenges;
	  }

	public boolean fighterDeplace(Fighter f, GameAction GA)
	{
		String path = GA._args;
		if(path.equals(""))
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Echec du deplacement: chemin vide");
			return false;
		}
		if(_ordreJeu.size() <= _curPlayer)return false;
		if(_ordreJeu.get(_curPlayer) == null)return false;
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+")Tentative de deplacement de Fighter ID= "+f.getGUID()+" a partir de la case "+f.get_fightCell().getID());
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Path: "+path);
		if(!_curAction.equals("")|| _ordreJeu.get(_curPlayer).getGUID() != f.getGUID() || _state != Constantes.FIGHT_STATE_ACTIVE)
		{
			if(!_curAction.equals(""))
				if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Echec du deplacement: il y deja une action en cours");
			if(_ordreJeu.get(_curPlayer).getGUID() != f.getGUID())
				if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Echec du deplacement: ce n'est pas a ce joueur de jouer");
			if(_state != Constantes.FIGHT_STATE_ACTIVE)
				if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Echec du deplacement: le combat n'est pas en cours");
			return false;
		}
		
		ArrayList<Fighter> tacle = Camino.getEnemyFighterArround(f.get_fightCell().getID(), _map, this);
		if(tacle != null && !f.isState(6))//Tentative de Tacle : Si stabilisation alors pas de tacle possible
		{
			//Les stabilis�s ne taclent pas
			tacle.removeIf(T -> T.isState(6));
			if(!tacle.isEmpty())//Si tous les tacleur ne sont pas stabilis�s
			{
				if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Le personnage est a cote de ("+tacle.size()+") ennemi(s)");// ("+tacle.getPacketsName()+","+tacle.get_fightCell().getID()+") => Tentative de tacle:");
				int chance = Formulas.getTacleChance(f, tacle);
				int rand = Formulas.getRandomValue(0, 99);
				if(rand > chance)
				{
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7,GA._id, "104",_ordreJeu.get(_curPlayer).getGUID()+";", "");//Joueur tacl�
					int pertePA = _curFighterPA*chance/100;
					
					if(pertePA  < 0)pertePA = -pertePA;
					if(_curFighterPM < 0)_curFighterPM = 0; // -_curFighterPM :: 0 c'est plus simple :)
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7,GA._id,"129", f.getGUID()+"", f.getGUID()+",-"+_curFighterPM);
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7,GA._id,"102", f.getGUID()+"", f.getGUID()+",-"+pertePA);
					
					_curFighterPM = 0;
					_curFighterPA -= pertePA;
					if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Echec du deplacement: fighter tacle");
					return false;
				}
			}
		}
		
		//*
		AtomicReference<String> pathRef = new AtomicReference<>(path);
		int nStep = Camino.isValidPath(_map, f.get_fightCell().getID(), pathRef, this);
		String newPath = pathRef.get();
		if( nStep > _curFighterPM || nStep == -1000)
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Fighter ID= "+_ordreJeu.get(_curPlayer).getGUID()+" a demander un chemin inaccessible ou trop loin");
			return false;
		}
		
		_curFighterPM -= nStep;
		_curFighterUsedPM += nStep;

		String encriptarcelda = newPath.substring(newPath.length() - 2);

		int nextCellID = GestorEncriptador.cellCode_To_ID(encriptarcelda);
		//les monstres n'ont pas de GAS//GAF
		if(_ordreJeu.get(_curPlayer).getPersonnage() != null)
			GestorSalida.GAME_SEND_GAS_PACKET_TO_FIGHT(this,7,_ordreJeu.get(_curPlayer).getGUID());
        //Si le joueur n'est pas invisible
        if(!_ordreJeu.get(_curPlayer).isHide())
	        GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, GA._id, "1", _ordreJeu.get(_curPlayer).getGUID()+"", "a"+ GestorEncriptador.cellID_To_Code(f.get_fightCell().getID())+newPath);
        else//Si le joueur est planqu� x)
        {
        	if(_ordreJeu.get(_curPlayer).getPersonnage() != null)
        	{
        		//On envoie le path qu'au joueur qui se d�place
        		PrintWriter out = _ordreJeu.get(_curPlayer).getPersonnage().getCuenta().getGameThread().get_out();
        		GestorSalida.GAME_SEND_GA_PACKET(out,  GA._id+"", "1", _ordreJeu.get(_curPlayer).getGUID()+"", "a"+ GestorEncriptador.cellID_To_Code(f.get_fightCell().getID())+newPath);
        	}
        }
       
        //Si port�
        Fighter po = _ordreJeu.get(_curPlayer).get_holdedBy();
        if(po != null
        && _ordreJeu.get(_curPlayer).isState(Constantes.ETAT_PORTE)
        && po.isState(Constantes.ETAT_PORTEUR))
        {
        	System.out.println("Porteur: "+po.getPacketsName());
        	System.out.println("NextCellID "+nextCellID);
        	System.out.println("Cell du Porteur "+po.get_fightCell().getID());
        	
        	//si le joueur va bouger
       		if(nextCellID != po.get_fightCell().getID())
       		{
       			//on retire les �tats
       			po.setState(Constantes.ETAT_PORTEUR, 0);
       			_ordreJeu.get(_curPlayer).setState(Constantes.ETAT_PORTE,0);
       			//on retire d� lie les 2 fighters
       			po.set_isHolding(null);
       			_ordreJeu.get(_curPlayer).set_holdedBy(null);
       			//La nouvelle case sera d�finie plus tard dans le code
       			//On envoie les packets
       			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 950, po.getGUID()+"", po.getGUID()+","+ Constantes.ETAT_PORTEUR+",0");
    			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 950, _ordreJeu.get(_curPlayer).getGUID()+"", _ordreJeu.get(_curPlayer).getGUID()+","+ Constantes.ETAT_PORTE+",0");
       		}
      	}
        
		_ordreJeu.get(_curPlayer).get_fightCell().getFighters().clear();
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Fighter ID= "+f.getGUID()+" se deplace de la case "+_ordreJeu.get(_curPlayer).get_fightCell().getID()+" vers "+ GestorEncriptador.cellCode_To_ID(encriptarcelda));
        _ordreJeu.get(_curPlayer).set_fightCell(_map.getMapa(nextCellID));
        _ordreJeu.get(_curPlayer).get_fightCell().addFighter(_ordreJeu.get(_curPlayer));
        if(po != null) po.get_fightCell().addFighter(po);// m�me erreur que tant�t, bug ou plus de fighter sur la case
       if(nStep < 0) 
       {
    	   if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Fighter ID= "+f.getGUID()+" nStep negatives, reconversion");
    	   nStep = nStep*(-1);
       }
        _curAction = "GA;129;"+_ordreJeu.get(_curPlayer).getGUID()+";"+_ordreJeu.get(_curPlayer).getGUID()+",-"+nStep;
        
        //Si porteur
        po = _ordreJeu.get(_curPlayer).get_isHolding();
        if(po != null
        && _ordreJeu.get(_curPlayer).isState(Constantes.ETAT_PORTEUR)
        && po.isState(Constantes.ETAT_PORTE))
        {
       		//on d�place le port� sur la case
        	po.set_fightCell(_ordreJeu.get(_curPlayer).get_fightCell());
        	if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(po.getPacketsName()+" se deplace vers la case "+nextCellID);
      	}
        
        if(f.getPersonnage() == null)
        {
        	try {
    			Thread.sleep(900+100*nStep);//Estimation de la dur�e du d�placement
    		} catch (InterruptedException ignored) {}
			GestorSalida.GAME_SEND_GAMEACTION_TO_FIGHT(this,7,_curAction);
    		_curAction = "";
    		ArrayList<Piege> P = new ArrayList<>();
    		P.addAll(_traps);
    		for(Piege p : P)
    		{
    			Fighter F = _ordreJeu.get(_curPlayer);
    			int dist = Camino.getDistanceBetween(_map,p.get_cell().getID(),F.get_fightCell().getID());
    			//on active le piege
    			if(dist <= p.get_size())p.onTraped(F);
    		}
    		return true;
        }

        f.getPersonnage().getCuenta().getGameThread().addAction(GA);
		if ((this._type == 4) && (this._challenges.size() > 0) && !this._ordreJeu.get(this._curPlayer).isInvocation() && !this._ordreJeu.get(this._curPlayer).isDouble() && !this._ordreJeu.get(this._curPlayer).isPerco())
        {
        	for (Map.Entry<Integer, Retos> c : this._challenges.entrySet()) {
        		if (c.getValue() == null) 
        			continue; 
        		c.getValue().onPlayer_move(f);
         	}
        }
        
        return true;
    }

	public void onGK(Personaje perso)
	{
		if(_curAction.equals("")|| _ordreJeu.get(_curPlayer).getGUID() != perso.get_GUID() || _state!= Constantes.FIGHT_STATE_ACTIVE)return;
		if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+")Fin du deplacement de Fighter ID= "+perso.get_GUID());
		GestorSalida.GAME_SEND_GAMEACTION_TO_FIGHT(this,7,_curAction);
		GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this,7,2,_ordreJeu.get(_curPlayer).getGUID());
		//copie
		ArrayList<Piege> P = (new ArrayList<>());
		P.addAll(_traps);
		for(Piege p : P)
		{
			Fighter F = getFighterByPerso(perso);
			int dist = Camino.getDistanceBetween(_map,p.get_cell().getID(),F.get_fightCell().getID());
			//on active le piege
			if(dist <= p.get_size())
				p.onTraped(F);
			if(_state == Constantes.FIGHT_STATE_FINISHED)break;
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException ignored) {}

		_curAction = "";
	}
	
	public void playerPass(Personaje _perso)
	{
		Fighter f = getFighterByPerso(_perso);
		if(f == null)return;
		if(!f.canPlay())return;
		if(!_curAction.equals("")) return;//TODO
		endTurn();
	}
	
	public int tryCastSpell(Fighter fighter,SortStats Spell, int caseID)
	{
		if(!_curAction.equals(""))return 10;
		if(Spell == null)return 10;
		
		Case Cell = _map.getMapa(caseID);
		
		if(CanCastSpell(fighter,Spell,Cell, -1))
		{
			_curAction = "casting";
			if(fighter.getPersonnage() != null)
				GestorSalida.ENVIAR_PAQUETE_CARACTERISTICAS(fighter.getPersonnage()); // envoi des stats du lanceur
			
			if(MainServidor.MOSTRAR_ENVIADOS)
				JuegoServidor.addToLog(fighter.getPacketsName()+" tentative de lancer le sort "+Spell.getSpellID()+" sur la case "+caseID);
			_curFighterPA -= Spell.getPACost();
			_curFighterUsedPA += Spell.getPACost();
			GestorSalida.GAME_SEND_GAS_PACKET_TO_FIGHT(this, 7, fighter.getGUID()); // infos concernant la d�pense de PA ?
			boolean isEc = Spell.getTauxEC() != 0 && Formulas.getRandomValue(1, Spell.getTauxEC()) == Spell.getTauxEC();
			if(isEc)
			{
				if(MainServidor.MOSTRAR_ENVIADOS)
					JuegoServidor.addToLog(fighter.getPacketsName()+" Echec critique sur le sort "+Spell.getSpellID());
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 302, fighter.getGUID()+"", Spell.getSpellID()+""); // envoi de l'EC
			}else
			{
				try
				{
					if ((this._type == 4) && (this._challenges.size() > 0) 
							&& !this._ordreJeu.get(this._curPlayer).isInvocation() 
							&& !this._ordreJeu.get(this._curPlayer).isDouble() 
							&& !this._ordreJeu.get(this._curPlayer).isPerco())
					{
						for (Entry<Integer, Retos> c : this._challenges.entrySet()) {
							if (c.getValue() == null) 
								continue;
							c.getValue().onPlayer_action(this._ordreJeu.get(this._curPlayer), Spell.getSpellID());
							c.getValue().onPlayer_spell(this._ordreJeu.get(this._curPlayer));

						}
					}
				} catch (Exception e) {
					System.out.println("-----------------------Erreur challenge (tryCastSpell())");
					e.printStackTrace(System.out);
				}
				
				boolean isCC = fighter.testIfCC(Spell.getTauxCC());
				String sort = Spell.getSpellID()+","+caseID+","+Spell.getSpriteID()+","+Spell.getLevel()+","+Spell.getSpriteInfos();
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 300, fighter.getGUID()+"", sort); // xx lance le sort
				if(isCC)
				{
					if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(fighter.getPacketsName()+" Coup critique sur le sort "+Spell.getSpellID());
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 301, fighter.getGUID()+"", sort); // CC !
				}
				//Si le joueur est invi, on montre la case
				if(fighter.isHide())showCaseToAll(fighter.getGUID(), fighter.get_fightCell().getID());
				//on applique les effets de l'arme
				Spell.applySpellEffectToFight(this,fighter,Cell,isCC);
			}
			// le client ne peut continuer sans l'envoi de ce packet qui annonce le co�t en PA
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102,fighter.getGUID()+"",fighter.getGUID()+",-"+Spell.getPACost());
			GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, fighter.getGUID());
			//Refresh des Stats
			//refreshCurPlayerInfos();
			if(!isEc)
				fighter.addLaunchedSort(Cell.getFirstFighter(),Spell);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {}
			if((isEc && Spell.isEcEndTurn()))
			{
				_curAction = "";
				try {
					Thread.sleep(500);
				} catch (InterruptedException ignored) {}
				if(fighter.getMob() != null || fighter.isInvocation())//Mob, Invoque
				{
					return 5;
				}else
				{
					endTurn();
					return 5;
				}
			}
			verifIfTeamAllDead();
		}else if (fighter.getMob() != null || fighter.isInvocation())
		{
			return 10;
		}
		if(fighter.getPersonnage() != null)
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102,fighter.getGUID()+"",fighter.getGUID()+",-0"); // annonce le co�t en PA
		try {
			Thread.sleep(500);
		} catch (InterruptedException ignored) {}
		_curAction = "";
		return 0;
	}

	public boolean CanCastSpell(Fighter fighter, SortStats spell, Case cell, int launchCase)
	{
		int ValidlaunchCase;
		if(launchCase <= -1)
		{
			ValidlaunchCase = fighter.get_fightCell().getID();
		}else
		{
			ValidlaunchCase = launchCase;
		}
		
		Fighter f = _ordreJeu.get(_curPlayer);
		Personaje perso = fighter.getPersonnage();
		//Si le sort n'est pas existant
		if(spell == null)
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Sort non existant");
			if(perso != null)
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1169");
			}
			return false;
		}
		//Si ce n'est pas au joueur de jouer
		if (f == null || f.getGUID() != fighter.getGUID()) 
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Ce n'est pas au joueur. Doit jouer :("+f.getGUID()+"). Fautif :("+fighter.getGUID()+")");
			if(perso != null)
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1175");
			}
			return false;	
		}
		//Si le joueur n'a pas assez de PA
		if(_curFighterPA < spell.getPACost())
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Le joueur n'a pas assez de PA ("+_curFighterPA+"/"+spell.getPACost()+")");
			if(perso != null)
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1170;" + _curFighterPA + "~" + spell.getPACost());
			}
			return false;
		}
		//Si la cellule vis�e n'existe pas
		if(cell == null)
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") La cellule visee n'existe pas");
			if(perso != null)
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1172");
			}
			return false;
		}
		//Si la cellule vis�e n'est pas align�e avec le joueur alors que le sort le demande
		if(spell.isLineLaunch() && !Camino.casesAreInSameLine(_map, ValidlaunchCase, cell.getID(), 'z'))
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Le sort demande un lancer en ligne, or la case n'est pas alignee avec le joueur");
			if(perso != null)
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1173");
			}
			return false;
		}
		//Si le sort demande une ligne de vue et que la case demand�e n'en fait pas partie
		if(spell.hasLDV() && !Camino.checkLoS(_map, ValidlaunchCase, cell.getID(), fighter, false))
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Le sort demande une ligne de vue, mais la case visee n'est pas visible pour le joueur");
			if(perso != null)
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1174");
			}
			return false;
		}
		// Pour peur si la personne pouss�e a la ligne de vue vers la case
		char dir = Camino.getDirBetweenTwoCase(ValidlaunchCase, cell.getID(), _map, true);
		if(spell.getSpellID() == 67)
			if(!Camino.checkLoS(_map, Camino.GetCaseIDFromDirrection(ValidlaunchCase, dir, _map, true), cell.getID(), null, true)) {
				if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") Le sort demande une ligne de vue, mais la case visee n'est pas visible pour le joueur");
				if(perso != null)
					GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1174");
				return false;
			}
		
		int dist = Camino.getDistanceBetween(_map, ValidlaunchCase, cell.getID());
		int MaxPO = spell.getMaxPO();
		if(spell.isModifPO())
		{
			MaxPO += fighter.getTotalStats().getEffect(Constantes.STATS_ADD_PO);
		}
		//V�rification Port�e mini / maxi
		if(dist < spell.getMinPO() || dist > MaxPO)
		{
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("("+_curPlayer+") La case est trop proche ou trop eloignee Min: "+spell.getMinPO()+" Max: "+spell.getMaxPO()+" Dist: "+dist);
			if(perso != null)
			{
				GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(perso, "1171;" + spell.getMinPO() + "~" + spell.getMaxPO() + "~" + dist);
			}
			return false;
		}
		//v�rification cooldown
		if(!LaunchedSort.coolDownGood(fighter,spell.getSpellID()))
		{
			return false;
		}
		//v�rification nombre de lancer par tour
		int nbLancer = spell.getMaxLaunchbyTurn();
		if(nbLancer - LaunchedSort.getNbLaunch(fighter, spell.getSpellID()) <= 0 && nbLancer > 0)
		{
			return false;
		}
		//v�rification nombre de lancer par cible
		Fighter target = cell.getFirstFighter();
		int nbLancerT = spell.getMaxLaunchbyByTarget();
		if(nbLancerT - LaunchedSort.getNbLaunchTarget(fighter, target, spell.getSpellID()) <= 0 && nbLancerT > 0)
		{
			return false;
		}
		return true;
	}

	
	public String GetGE(int win)
    {
		long time = System.currentTimeMillis() - _startTime;
		int initGUID = _init0.getGUID();
		
		int type = Constantes.FIGHT_TYPE_CHALLENGE;// toujours 0
		if(_type == Constantes.FIGHT_TYPE_AGRESSION)//Sauf si gain d'honneur
			type = _type;
		
		StringBuilder Packet = new StringBuilder();
        Packet.append("GE").append(time);
		//String Packet = "GE"+time;
        // si c'est un combat PVM alors bonus potentiel en �toiles
		if(_type == Constantes.FIGHT_TYPE_PVM && _mobGroup != null)
			Packet.append(";").append(_mobGroup.get_bonusValue());
		Packet.append("|").append(initGUID).append("|").append(type).append("|");
	    ArrayList<Fighter> TEAM1 = new ArrayList<>();
        ArrayList<Fighter> TEAM2 = new ArrayList<>();
        if(win == 1)
        {
        	TEAM1.addAll(_team0.values());
        	TEAM2.addAll(_team1.values());
        }
        else
        {
        	TEAM1.addAll(_team1.values());
        	TEAM2.addAll(_team0.values());
        }
        //Calculs des niveaux de groupes
        //int TEAM1lvl = 0;
       // int TEAM2lvl = 0;
        //Traque
        Personaje curp = null;
        //Evaluation du level
        for(Fighter F : TEAM1)
        {
        	if(F.isInvocation())continue;
        	if(TEAM1.size() == 1) curp = F.getPersonnage();
        	//TEAM1lvl += F.get_lvl();
        }
        //Evaluation de la pr�sence de la traque
        for(Fighter F : TEAM2)
        {
        	if(F.isInvocation())continue;
        	if(curp != null && curp.get_traque() != null && curp.get_traque().get_traqued() == F.getPersonnage())
        	{ 
        		GestorSalida.GAME_SEND_MESSAGE(curp, "Thomas Sacre : Contrat fini, reviens me voir pour recuperer ta recompense.", "000000");
        		curp.get_traque().set_traqued(null); 
        		curp.get_traque().set_time(-2); 
        	} 
        	//TEAM2lvl += F.get_lvl();
        }
        //fin
        /* DEBUG
        System.out.println("TEAM1: lvl="+TEAM1lvl);
        System.out.println("TEAM2: lvl="+TEAM2lvl);
        //*/
        //DROP SYSTEM
        
      //Challenge augmente la PP totale (atteint plus facilement les seuils)
		double factChalDrop = 100;
		if ((this._type == 4) && (this._challenges.size() > 0)) {
  	       try {
  	    	   for (Entry<Integer, Retos> c : this._challenges.entrySet()) {
  	    		   if ((c.getValue() == null) || (!c.getValue().get_win()))
  	    			   continue; 
  	    		   factChalDrop += c.getValue().get_gainDrop();
  	    	   }
  	       } catch (Exception e) {
  	    	   System.out.println("-------------Erreur du facteur de drop de challenge. GetGE;");
  	       }
  	     factChalDrop += _mobGroup.get_bonusValue(); // on ajoute le bonus en �toiles
		}
 		factChalDrop /= 100;
        	//Calcul de la PP de groupe
	        int groupPP = 0,minkamas = 0,maxkamas = 0;
	        for(Fighter F : TEAM1) {
	        	if(!F.isInvocation() || (F.getMob() != null && F.getMob().getTemplate().getID() ==285))
	        		groupPP += F.getTotalStats().getEffect(Constantes.STATS_ADD_PROS);
	        }
	        if(groupPP <0)groupPP =0;
	        groupPP *= factChalDrop;
        	//Calcul des drops possibles
	        ArrayList<Drop> possibleDrops = new ArrayList<>();
	        for(Fighter F : TEAM2)
	        {
	        	//Evaluation de l'argent � gagner
	        	if(F.isInvocation() || F.getMob() == null)continue;
	        	minkamas += F.getMob().getTemplate().getMinKamas();
	        	maxkamas += F.getMob().getTemplate().getMaxKamas();
	        	//Evaluation de la liste des drops droppable
	        	for(Drop D : F.getMob().getDrops())
	        	{
	        		if(D.getMinProsp() <= groupPP)
	        		{
	        			int taux = (int)(D.get_taux()* MainServidor.CONFIG_DROP);
	        			possibleDrops.add(new Drop(D.get_itemID(),0,taux,D.get_max()));
	        		}
	        	}
	        }
	        if(_type == Constantes.FIGHT_TYPE_PVT) {
	        	minkamas = (int)_perco.getKamas() / TEAM1.size();
	        	maxkamas = minkamas;
	        	possibleDrops = _perco.getDrops();
	        }
	        //On R�ordonne la liste des combattants en fonction de la PP
	        ArrayList<Fighter> Temp = new ArrayList<>();
	        Fighter curMax = null;
	        while(Temp.size() < TEAM1.size())
	        {
	        	int curPP = -1;
		        for(Fighter F : TEAM1)
		        {
	        		//S'il a plus de PP et qu'il n'est pas list�
		        	if(F.getTotalStats().getEffect(Constantes.STATS_ADD_PROS) > curPP && !Temp.contains(F))
		        	{
		        		curMax = F;
		        		curPP = F.getTotalStats().getEffect(Constantes.STATS_ADD_PROS);
		        	}
		        }
	        	Temp.add(curMax);
	        }
	        //On enleve les invocs
	        TEAM1.clear();
	        TEAM1.addAll(Temp);
	        /* DEBUG
	        System.out.println("DROP: PP ="+groupPP);
	        System.out.println("DROP: nbr="+possibleDrops.size());
	        System.out.println("DROP: Kam="+totalkamas);
	        //*/
	    //FIN DROP SYSTEM
	    //XP SYSTEM
	        long totalXP = 0;
	        for(Fighter F : TEAM2)
	        {
	        	if(F.isInvocation() || F.getMob() == null)continue;
	        	totalXP += F.getMob().getBaseXp();
	        }
	        if ((this._type == 4) && (this._challenges.size() > 0)) {
     	       try
     	       {
     	    	   long totalGainXp = 0;
     	    	   for (Entry<Integer, Retos> c : this._challenges.entrySet()) {
     	    		   if ((c.getValue() == null) || (!c.getValue().get_win()))
     	    			   continue; 
     	    		   totalGainXp += c.getValue().get_gainXp();
     	           }
     	    	   totalGainXp += _mobGroup.get_bonusValue(); // on ajoute le bonus en �toiles
 	    		   totalXP *= 100L + totalGainXp; //on multiplie par la somme des boost chal
 	    		   totalXP /= 100L;

     	       } catch (Exception e) {
     	    	   System.out.println("-------------Erreur du total XP de challenge. GetGE;");
     	       }
     	 
     	     }
	        /* DEBUG
	        System.out.println("TEAM1: xpTotal="+totalXP);
	        //*/
	    //FIN XP SYSTEM
		//Capture d'�mes
	        boolean mobCapturable = true;
	        for(Fighter F : TEAM2)
	        {
	        	try
	        	{
	        		mobCapturable &= F.getMob().getTemplate().isCapturable();
	        	}catch (Exception e) {
					mobCapturable = false;
					break;
				}
	        }
	        isCapturable |= mobCapturable;
	        
	        if(isCapturable)
	        {
		        boolean isFirst = true;
		        int maxLvl = 0;
		        String pierreStats = "";

		        
		        for(Fighter F : TEAM2)	//Cr�ation de la pierre et verifie si le groupe peut �tre captur�
		        {
		        	if(!isFirst)
		        		pierreStats += "|";
		        	
		        	pierreStats += F.getMob().getTemplate().getID() + "," + F.get_lvl();//Converti l'ID du monstre en Hex et l'ajoute au stats de la futur pierre d'�me
		        	
		        	isFirst = false;
		        	
		        	if(F.get_lvl() > maxLvl)	//Trouve le monstre au plus haut lvl du groupe (pour la puissance de la pierre)
		        		maxLvl = F.get_lvl();
		        }
		        pierrePleine = new PiedraAlma(Mundo.getNewItemGuid(),1,7010, Constantes.ITEM_POS_NO_EQUIPED,pierreStats);	//Cr�e la pierre d'�me
		        
		        for(Fighter F : TEAM1)	//R�cup�re les captureur
		        {
		        	if(!F.isInvocation() && F.isState(Constantes.ETAT_CAPT_AME))
		        	{
		        		_captureur.add(F);
		        	}
		        }
		        if(_captureur.size() > 0 && !Mundo.isArenaMap(get_map().getID()))	//S'il y a des captureurs
	    		{
	    			for (int i = 0; i < _captureur.size(); i++)
	    			{
	    				try
	    				{
			        		Fighter f = _captureur.get(Formulas.getRandomValue(0, _captureur.size()-1));	//R�cup�re un captureur au hasard dans la liste
			        		if(!(f.getPersonnage().getObjetByPos(Constantes.ITEM_POS_ARME).getTemplate().getType() == Constantes.ITEM_TYPE_PIERRE_AME))
		    				{
			    				_captureur.remove(f);
		    					continue;
		    				}
			    			Couple<Integer,Integer> pierreJoueur = Formulas.decompPierreAme(f.getPersonnage().getObjetByPos(Constantes.ITEM_POS_ARME));//R�cup�re les stats de la pierre �quipp�
			    			
			    			if(pierreJoueur.second < maxLvl)	//Si la pierre est trop faible
			    			{
			    				_captureur.remove(f);
		    					continue;
		    				}
			    			
			    			int captChance = Formulas.totalCaptChance(pierreJoueur.first, f.getPersonnage());
			    			
			    			if(Formulas.getRandomValue(1, 100) <= captChance)	//Si le joueur obtiens la capture
			    			{
			    				//Retire la pierre vide au personnage et lui envoie ce changement
			    				int pierreVide = f.getPersonnage().getObjetByPos(Constantes.ITEM_POS_ARME).getGuid();
			    				f.getPersonnage().deleteItem(pierreVide);
			    				GestorSalida.GAME_SEND_REMOVE_ITEM_PACKET(f.getPersonnage(), pierreVide);
			    				
			    				captWinner = f._id;
			    				break;
			    			}
		    			}
	    				catch(NullPointerException e)
	    				{
	    					continue;
	    				}
	    			}
	    		}
	        }
	    //Fin Capture
	    for(Fighter i : TEAM1)
		{
	    	if(i.hasLeft()) continue;//Si il abandonne, il ne gagne pas d'xp
	    	if(i._double != null)continue;//Pas de double dans les gains
        	if(type == Constantes.FIGHT_TYPE_CHALLENGE)
        	{
        		if(i.isInvocation() && i.getMob() != null && i.getMob().getTemplate().getID() != 285)continue;
        		long winxp 	= Formulas.getXpWinPvm3(i, TEAM1, TEAM2, totalXP, _mobGroup != null ? _mobGroup.get_bonusValue() : 0);
        		AtomicReference<Long> XP = new AtomicReference<>();
        		XP.set(winxp);
        		long guildxp = Formulas.getGuildXpWin(i,XP);
        		long mountxp = 0;
        		
        		if(i.getPersonnage() != null && i.getPersonnage().isOnMount())
        		{
        			mountxp = Formulas.getMountXpWin(i,XP);
        			i.getPersonnage().getMount().addXp(mountxp);
        			GestorSalida.GAME_SEND_Re_PACKET(i.getPersonnage(),"+",i.getPersonnage().getMount());
        		}
        		
                if(MainServidor.CONFIG_XP_DEFI && _type == Constantes.FIGHT_TYPE_CHALLENGE)
                {
              	      long xp = Formulas.XPDefie(i, TEAM1, TEAM2);
                        XP.set(Long.valueOf(xp));
                        guildxp = Formulas.getGuildXpWin(i, XP);
                        int winKamas = Formulas.getKamasWinPVP(i, TEAM1, minkamas, maxkamas);
                        if ((i.getPersonnage() != null) && (i.getPersonnage().isOnMount()))
                        {
                          mountxp = Formulas.getMountXpWin(i,XP);
                          i.getPersonnage().getMount().addXp(mountxp);
                          GestorSalida.GAME_SEND_Re_PACKET(i.getPersonnage(), "+", i.getPersonnage().getMount());
                        }
                }
                
        		int winKamas	= Formulas.getKamasWin(i,TEAM1,minkamas,maxkamas);
        		String drops = "";
        		//Drop system
        		
        		ArrayList<Drop> temp = new ArrayList<>();
        		temp.addAll(possibleDrops);
        		Map<Integer,Integer> itemWon = new TreeMap<>();
    			int PP = i.getTotalStats().getEffect(Constantes.STATS_ADD_PROS);
        		boolean allIsDropped = false;
    			while(!allIsDropped) {
    				for(Drop D : temp)
    				{
    					int t = (int)(D.get_taux()*PP);//Permet de gerer des taux>0.01
    					t = (int)((double)t*factChalDrop);
    					if(_type == Constantes.FIGHT_TYPE_PVT)
    						t = 10000/TEAM1.size();
    					int jet = Formulas.getRandomValue(0, 100*100);
    					//	System.out.println("PP : "+PP+"    chance : "+t+"    jet : "+jet);
    					if(jet < t)
    					{
    						ObjTemplate OT = Mundo.getObjTemplate(D.get_itemID());
    						if(OT == null)continue;
        				//	on ajoute a la liste
    						itemWon.put(OT.getID(),(itemWon.get(OT.getID())==null?0:itemWon.get(OT.getID()))+1);
    						
    						D.setMax(D.get_max()-1);
    						if(D.get_max() == 0)possibleDrops.remove(D);
    					}
    				}
    				allIsDropped = _type != Constantes.FIGHT_TYPE_PVT;
    				if(possibleDrops.isEmpty())
						allIsDropped = true;
    			}
    	        if(i._id == captWinner && pierrePleine != null)	//S'il � captur� le groupe
        		{
        			if(drops.length() >0)drops += ",";
        			drops += pierrePleine.getTemplate().getID()+"~"+1;
        			if(i.getPersonnage().addObjet(pierrePleine, false))
        				Mundo.addObjet(pierrePleine, true);
        		}
        		for(Entry<Integer,Integer> entry : itemWon.entrySet())
        		{
        			ObjTemplate OT = Mundo.getObjTemplate(entry.getKey());
        			if(OT == null)continue;
        			if(drops.length() >0)drops += ",";
        			drops += entry.getKey()+"~"+entry.getValue();
        			Objeto obj = OT.createNewItem(entry.getValue(), false);
        			if(i.getPersonnage() != null && i.getPersonnage().addObjet(obj, true))
        				Mundo.addObjet(obj, true);
        			else if (i.isInvocation() && i.getMob().getTemplate().getID() == 285 && i.getInvocator().getPersonnage().addObjet(obj, true))
        				Mundo.addObjet(obj, true);
        		}
        		//fin drop system
        		winxp = XP.get();
        		if(winxp != 0 && i.getPersonnage() != null)
        			i.getPersonnage().addXp(winxp);
        		if(winKamas != 0 && i.getPersonnage() != null)
        			i.getPersonnage().addKamas(winKamas);
        		else if (winKamas != 0 && i.isInvocation() && i.getInvocator().getPersonnage() != null)
        			i.getInvocator().getPersonnage().addKamas(winKamas);
        		if(guildxp > 0 && i.getPersonnage().getMiembroGremio() != null)
        			i.getPersonnage().getMiembroGremio().giveXpToGuild(guildxp);

        		Packet.append("2;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.get_lvl()).append(";").append((i.isDead() ?  "1" : "0" )).append(";");
        		Packet.append(i.xpString(";")).append(";");
        		Packet.append((winxp == 0?"":winxp)).append(";");
        		Packet.append((guildxp == 0?"":guildxp)).append(";");
        		Packet.append((mountxp == 0?"":mountxp)).append(";");
        		Packet.append(drops).append(";");//Drop
        		Packet.append((winKamas == 0?"":winKamas)).append("|");
        	}else
        	{
        		// Si c'est un neutre, on ne gagne pas de points
        		int winH = 0;
        		int winD = 0;
        		if(type == Constantes.FIGHT_TYPE_AGRESSION)
        		{
	        		if(_init1.getPersonnage().get_align() != 0 && _init0.getPersonnage().get_align() != 0)
	    			{
	        			if(_init1.getPersonnage().getCuenta().getActualIP().compareTo(_init0.getPersonnage().getCuenta().getActualIP()) != 0 || MainServidor.ALLOW_MULE_PVP)
	        			{
	            			winH = Formulas.calculHonorWin(TEAM1,TEAM2,i);
	        			}
	        			if(i.getPersonnage().getDeshonor() > 0) winD = -1;
	    			}
        		}
        		Personaje P = i.getPersonnage();
        		if(P.get_honor()+winH<0)winH = -P.get_honor();
        		P.addHonor(winH);
        		P.setDeshonor(P.getDeshonor()+winD);
        		Packet.append("2;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.get_lvl()).append(";").append((i.isDead() ?  "1" : "0" )).append(";");
        		Packet.append((P.get_align()!= Constantes.ALIGNEMENT_NEUTRE? Mundo.getExpLevel(P.getGrade()).pvp:0)).append(";");
        		Packet.append(P.get_honor()).append(";");
        		int maxHonor = Mundo.getExpLevel(P.getGrade()+1).pvp;
        		if(maxHonor == -1)maxHonor = Mundo.getExpLevel(P.getGrade()).pvp;
        		Packet.append((P.get_align()!= Constantes.ALIGNEMENT_NEUTRE?maxHonor:0)).append(";");
        		Packet.append(winH).append(";");
        		Packet.append(P.getGrade()).append(";");
        		Packet.append(P.getDeshonor()).append(";");
        		Packet.append(winD);
        		Packet.append(";;0;0;0;0;0|");
        	}
		}
		for(Fighter i : TEAM2)
		{
			if(i._double != null)continue;//Pas de double dans les gains
			if(i.isInvocation() && i.getMob().getTemplate().getID() != 285)continue;//On affiche pas les invocs
			if(_type != Constantes.FIGHT_TYPE_AGRESSION)
			{
				if(i.getPDV() == 0 || i.hasLeft())
				{
					Packet.append("0;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.get_lvl()).append(";1").append(";").append(i.xpString(";")).append(";;;;|");
				}else
				{
					Packet.append("0;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.get_lvl()).append(";0").append(";").append(i.xpString(";")).append(";;;;|");
				}
			}else
        	{
        		// Si c'est un neutre, on ne gagne pas de points
        		int winH = 0;
        		int winD = 0;
        		if(_init1.getPersonnage().get_align() != 0 && _init0.getPersonnage().get_align() != 0)
    			{
        			if(_init1.getPersonnage().getCuenta().getActualIP().compareTo(_init0.getPersonnage().getCuenta().getActualIP()) != 0 || MainServidor.ALLOW_MULE_PVP)
            		{
            			winH = Formulas.calculHonorWin(TEAM1,TEAM2,i);
        			}
    			}
        		
        		Personaje P = i.getPersonnage();
        		if(P.get_honor()+winH<0)winH = -P.get_honor();
        		P.addHonor(winH);
        		if(P.getDeshonor()-winD<0) winD = 0;
        		P.setDeshonor(P.getDeshonor()-winD);
        		Packet.append("0;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.get_lvl()).append(";").append((i.isDead() ?  "1" : "0" )).append(";");
        		Packet.append((P.get_align()!= Constantes.ALIGNEMENT_NEUTRE? Mundo.getExpLevel(P.getGrade()).pvp:0)).append(";");
        		Packet.append(P.get_honor()).append(";");
        		int maxHonor = Mundo.getExpLevel(P.getGrade()+1).pvp;
        		if(maxHonor == -1)maxHonor = Mundo.getExpLevel(P.getGrade()).pvp;
        		Packet.append((P.get_align()!= Constantes.ALIGNEMENT_NEUTRE?maxHonor:0)).append(";");
        		Packet.append(winH).append(";");
        		Packet.append(P.getGrade()).append(";");
        		Packet.append(P.getDeshonor()).append(";");
        		Packet.append(winD);
        		Packet.append(";;0;0;0;0;0|");
        	}
		}
		if(Recaudador.GetPercoByMapID(_map.getID()) != null && _type == 4)//On a un percepteur ONLY PVM ?
		{
			Recaudador p = Recaudador.GetPercoByMapID(_map.getID());
			long winxp 	= (int)Math.floor(Formulas.getXpWinPerco(p,TEAM1,TEAM2,totalXP)/100);
			long winkamas 	= (int)Math.floor(Formulas.getKamasWinPerco(minkamas,maxkamas)/100);
			p.setXp(p.getXp()+winxp);
			p.setKamas(p.getKamas()+winkamas);
			Packet.append("5;").append(p.getGuid()).append(";").append(p.get_N1()).append(",").append(p.get_N2()).append(";").append(Mundo.getGuild(p.get_guildID()).get_lvl()).append(";0;");
			Gremio G = Mundo.getGuild(p.get_guildID());
			Packet.append(G.get_lvl()).append(";");
			Packet.append(G.get_xp()).append(";");
			Packet.append(Mundo.getGuildXpMax(G.get_lvl())).append(";");
			Packet.append(";");//XpGagner
			Packet.append(winxp).append(";");//XpGuilde
			Packet.append(";");//Monture
			
			String drops = "";
    		ArrayList<Drop> temp = new ArrayList<>();
    		temp.addAll(possibleDrops);
    		Map<Integer,Integer> itemWon = new TreeMap<>();
    		
    		for(Drop D : temp)
    		{
    			int t = (int)(D.get_taux()*100);//Permet de gerer des taux>0.01
    			int jet = Formulas.getRandomValue(0, 100*100);
    			if(jet < t)
    			{
    				ObjTemplate OT = Mundo.getObjTemplate(D.get_itemID());
    				if(OT == null)continue;
    				//on ajoute a la liste
    				itemWon.put(OT.getID(),(itemWon.get(OT.getID())==null?0:itemWon.get(OT.getID()))+1);
    				
    				D.setMax(D.get_max()-1);
    				if(D.get_max() == 0)possibleDrops.remove(D);
    			}
    		}
    		for(Entry<Integer,Integer> entry : itemWon.entrySet())
    		{
    			ObjTemplate OT = Mundo.getObjTemplate(entry.getKey());
    			if(OT == null)continue;
    			if(drops.length() >0)drops += ",";
    			drops += entry.getKey()+"~"+entry.getValue();
    			Objeto obj = OT.createNewItem(entry.getValue(), false);
    			p.addObjet(obj);
    			Mundo.addObjet(obj, true);
    		}
    		Packet.append(drops).append(";");//Drop
    		Packet.append(winkamas).append("|");
			
			GestorSQL.actualizar_recaudador(p);
		}
        return Packet.toString();
    }
	
	public boolean verifIfTeamIsDead() {
		boolean fini = true;
		for(Entry<Integer,Fighter> entry : _team1.entrySet()) {
			if(entry.getValue().isInvocation())continue;
			if(!entry.getValue().isDead())
			{
				fini = false;
				break;
			}
		}
		return fini;
	}
    
	public void verifIfTeamAllDead()
	{
		if(_state >= Constantes.FIGHT_STATE_FINISHED)return;
		boolean team0 = true;
		boolean team1 = true;
		for(Entry<Integer,Fighter> entry : _team0.entrySet())
		{
			if(entry.getValue().isInvocation())continue;
			if(!entry.getValue().isDead())
			{
				team0 = false;
				break;
			}
		}
		for(Entry<Integer,Fighter> entry : _team1.entrySet())
		{
			if(entry.getValue().isInvocation())continue;
			if(!entry.getValue().isDead())
			{
				team1 = false;
				break;
			}
		}
		if(team0 || team1 || !verifyStillInFight())
		{
			try
			{
				if ((this._type == 4) && (this._challenges.size() > 0))
				{
					for (Entry<Integer, Retos> c : this._challenges.entrySet()) {
						if (c.getValue() == null) 
							continue; 
						c.getValue().onFight_end();
					}
				}
			} catch (Exception e) {
				System.out.println("--------------Erreur challenge: onFight_end");
			}
			_ticMyTimer_startTime = 0L;
	        _ticMyTimer_endTurn = false;
	        _state = Constantes.FIGHT_STATE_FINISHED;
			int winner = team0?2:1;
			if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("L'equipe "+winner+" gagne !");

			_turnTimer.stop();
			//On despawn tous le monde
			_curPlayer = -1;
			for(Entry<Integer, Fighter> entry : _team0.entrySet())
			{
				GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_map, entry.getValue().getGUID());
			}
			for(Entry<Integer, Fighter> entry : _team1.entrySet())
			{
				GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_map, entry.getValue().getGUID());
			}
			this._init0.getPersonnage().getActualMapa().removeFight(this._id);
			GestorSalida.GAME_SEND_FIGHT_GE_PACKET_TO_FIGHT(this,7,winner);
			
			for(Entry<Integer, Fighter> entry : _team0.entrySet())//Team joueurs
			{
				Personaje perso = entry.getValue().getPersonnage();
				if(perso == null)continue;
				perso.set_duelID(-1);
				perso.set_ready(false);
				perso.set_fight(null);
			}
			switch(_type)//Team mobs sauf en d�fi/aggro
			{
				case Constantes.FIGHT_TYPE_AGRESSION://Aggro
					for(Entry<Integer, Fighter> entry : _team1.entrySet())
					{
						Personaje perso = entry.getValue().getPersonnage();
						if(perso == null)continue;
						perso.set_duelID(-1);
						perso.set_ready(false);
						perso.set_fight(null);
					}
				break;
				case Constantes.FIGHT_TYPE_PVM://PvM
					if(_team1.get(-1) == null)return;
				break;	
			}
			
			//on vire les spec du combat
			for(Personaje perso: _spec.values())
			{
				//on remet le perso sur la map
				perso.getActualMapa().addPlayer(perso);
				//SocketManager.GAME_SEND_GV_PACKET(perso);	//Mauvaise ligne apparemment
				perso.refreshMapAfterFight();
			}
			
			Mundo.getCarte(_map.getID()).removeFight(_id);
			GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(Mundo.getCarte(_map.getID()));
			_map = null;
			_ordreJeu = null;
			ArrayList<Fighter> winTeam = new ArrayList<>();
			ArrayList<Fighter> looseTeam = new ArrayList<>();
			if(team0)
			{
				looseTeam.addAll(_team0.values());
				winTeam.addAll(_team1.values());
			}
			else
			{
				winTeam.addAll(_team0.values());
				looseTeam.addAll(_team1.values());
			}
			try
			{
				Thread.sleep(1600);
			}catch(Exception ignored){}

			//Pour les gagnants, on active les endFight actions
			for(Fighter F : winTeam)
			{
				if(F._Perco != null)
				{
					//On actualise la guilde+Message d'attaque FIXME
					for(Personaje z : Mundo.getGuild(_guildID).getMembers())
					{
						if(z == null) continue;
						if(z.isConectado())
						{
							GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parsetoGuild(z.get_guild().get_id()));
							GestorSalida.GAME_SEND_MESSAGE(z, "Votre percepteur remporte la victoire.", MainServidor.CONFIG_MOTD_COLOR);
						}
					}
					F._Perco.set_inFight((byte)0);
					F._Perco.set_inFightID((byte)-1);
					for(Personaje z : Mundo.getCarte(F._Perco.get_mapID()).getPersos())
					{
						if(z == null) continue;
						GestorSalida.GAME_SEND_MAP_PERCO_GMS_PACKETS(z.getCuenta().getGameThread().get_out(), z.getActualMapa());
					}
				}
				if(F.hasLeft())continue;
				if(F.getPersonnage() == null)continue;
				if(F.isInvocation())continue;
				if(!F.getPersonnage().isConectado())continue;
				
				if(_type != Constantes.FIGHT_TYPE_CHALLENGE)
				{
					if(F.getPDV() <= 0)
					{
						F.getPersonnage().set_PDV(1);
					}else
					{
						F.getPersonnage().set_PDV(F.getPDV());	
					}
				}
				
				/*try
				{
					Thread.sleep(1000);
				}catch(Exception E){};*/
				if(_type != Constantes.FIGHT_TYPE_CHALLENGE)
					F.getPersonnage().getActualMapa().applyEndFightAction(_type, F.getPersonnage());

				try
				{
					Thread.sleep(200);
				}catch(Exception ignored){}
				F.getPersonnage().refreshMapAfterFight();
			}
			//Pour les perdant on TP au point de sauvegarde
			for(Fighter F : looseTeam)
			{
				if(F._Perco != null)
				{
					_mapOld.RemoveNPC(F._Perco.getGuid());
					GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_mapOld, F._Perco.getGuid());
					_perco.DelPerco(F._Perco.getGuid());
					GestorSQL.eliminar_recaudador(F._Perco.getGuid());
					//On actualise la guilde+Message d'attaque FIXME
					for(Personaje z : Mundo.getGuild(_guildID).getMembers())
					{
						if(z == null) continue;
						if(z.isConectado())
						{
							GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parsetoGuild(z.get_guild().get_id()));
							GestorSalida.GAME_SEND_MESSAGE(z, "Votre percepteur est mort.", MainServidor.CONFIG_MOTD_COLOR);
						}
					}
				}
				if(F.hasLeft())continue;
				if(F.getPersonnage() == null)continue;
				if(F.isInvocation())continue;
				if(!F.getPersonnage().isConectado())continue;
				
				if(_type != Constantes.FIGHT_TYPE_CHALLENGE)
				{
					try
					{
						Thread.sleep(1000);
					}catch(Exception ignored){}
					int EnergyLoos = Formulas.getLoosEnergy(F.get_lvl(), _type==1, _type==5);
					int Energy = F.getPersonnage().get_energy() - EnergyLoos;
					if(Energy < 0) Energy = 0;
					F.getPersonnage().set_energy(Energy);
					if(Energy == 0)
					{
						F.getPersonnage().set_Ghosts();
					}else
					{
						F.getPersonnage().warpToSavePos();
						F.getPersonnage().set_PDV(1);
					}
					if(F.getPersonnage().isConectado())
						GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(F.getPersonnage(), "034;"+EnergyLoos);
				}
				
				try
				{
					Thread.sleep(200);
				}catch(Exception ignored){}
				F.getPersonnage().refreshMapAfterFight();
			}
			
		}
	}

	public void onFighterDie(Fighter target, Fighter caster) 
	{
		target.setIsDead(true);
		if(!target.hasLeft()) deadList.put(target.getGUID(), target);//on ajoute le joueur � la liste des cadavres ;)
		GestorSalida.GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(this,7,target.getGUID());
		target.get_fightCell().getFighters().clear();// Supprime tout causait bug si port�/porteur
		
		if(target.isState(Constantes.ETAT_PORTEUR))
		{ 
			Fighter f = target.get_isHolding();
			f.set_fightCell(f.get_fightCell());
			f.get_fightCell().addFighter(f);//Le bug venait par manque de ceci, il ni avait plus de firstFighter
			f.setState(Constantes.ETAT_PORTE, 0);//J'ajoute ceci quand m�me pour signaler qu'ils ne sont plus en �tat port�/porteur
			target.setState(Constantes.ETAT_PORTEUR, 0);
			f.set_holdedBy(null);
			target.set_isHolding(null);
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 950, f.getGUID()+"", f.getGUID()+","+ Constantes.ETAT_PORTE+",0");
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 950, target.getGUID()+"", target.getGUID()+","+ Constantes.ETAT_PORTEUR+",0");
		}
		
		if(target.getTeam() == 0)
		{
			TreeMap<Integer,Fighter> team = new TreeMap<>();
			team.putAll(_team0);
			for(Entry<Integer,Fighter> entry : team.entrySet())
			{
				if(entry.getValue().getInvocator() == null)continue;
				if(entry.getValue().getPDV() == 0)continue;
				if(entry.getValue().isDead())continue;
				if(entry.getValue().getInvocator().getGUID() == target.getGUID())//si il a �t� invoqu� par le joueur mort
				{
					onFighterDie(entry.getValue(), caster);
					
					int index = _ordreJeu.indexOf(entry.getValue());
					if(index != -1)_ordreJeu.remove(index);
					
					if(_team0.containsKey(entry.getValue().getGUID()))_team0.remove(entry.getValue().getGUID());
					else _team1.remove(entry.getValue().getGUID());
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getGUID()+"", getGTL());
				}
			}
			if ((this._type == 4) && (this._challenges.size() > 0))
			{
				for (Map.Entry<Integer, Retos> c : this._challenges.entrySet()) {
					if (c.getValue() == null) 
						continue; 
					c.getValue().onFighter_die(target);
				}
			}
		}else if(target.getTeam() == 1)
		{
			TreeMap<Integer,Fighter> team = new TreeMap<>();
			team.putAll(_team1);
			for(Entry<Integer,Fighter> entry : team.entrySet())
			{
				if(entry.getValue().getInvocator() == null)continue;
				if(entry.getValue().getPDV() == 0)continue;
				if(entry.getValue().isDead())continue;
				if(entry.getValue().getInvocator().getGUID() == target.getGUID())//si il a �t� invoqu� par le joueur mort
				{
					onFighterDie(entry.getValue(), caster);
					
					int index = _ordreJeu.indexOf(entry.getValue());
					if(index != -1)_ordreJeu.remove(index);
					
					if(_team0.containsKey(entry.getValue().getGUID()))_team0.remove(entry.getValue().getGUID());
					else _team1.remove(entry.getValue().getGUID());
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getGUID()+"", getGTL());
              	}
			}
		}
		if(target.getMob() != null)
		{
			//Si c'est une invocation, on la retire de la liste
			try
			{
				boolean isStatic = false;
				for(int id : Constantes.STATIC_INVOCATIONS)if(id == target.getMob().getTemplate().getID())isStatic = true;
				if(target.isInvocation() && !isStatic)
				{
					//Il ne peut plus jouer, et est mort on revient au joueur pr�cedent pour que le startTurn passe au suivant
					if(!target.canPlay() && _ordreJeu.get(_curPlayer).getGUID() == target.getGUID())
					{
						_curPlayer--;
					}
					//Il peut jouer, et est mort alors on passe son tour pour que l'autre joue, puis on le supprime de l'index sans probl�mes
					if(target.canPlay() && _ordreJeu.get(_curPlayer).getGUID() == target.getGUID())
					{
	    				endTurn();
					}
					
					//On ne peut pas supprimer l'index tant que le tour du prochain joueur n'est pas lanc�
					int index = _ordreJeu.indexOf(target);
					
					//Si le joueur courant a un index plus �lev�, on le diminue pour �viter le outOfBound
					if(_curPlayer > index) _curPlayer--;
					
					if(index != -1)_ordreJeu.remove(index);
					
					
					if(_team0.containsKey(target.getGUID()))_team0.remove(target.getGUID());
					else _team1.remove(target.getGUID());
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getGUID()+"", getGTL());
				}
			}catch(Exception e){e.printStackTrace();}
		}
		if ((this._type == 4) && (this._challenges.size() > 0))
		{
			for (Map.Entry<Integer, Retos> c : this._challenges.entrySet()) {
				if (c.getValue() == null) 
					continue; 
				c.getValue().onMob_die(target, caster);
			}
		}
		
		//on supprime les glyphes du joueur
		ArrayList<Glyphe> glyphs = new ArrayList<>();//Copie du tableau
		glyphs.addAll(_glyphs);
		for(Glyphe g : glyphs)
		{
			//Si c'est ce joueur qui l'a lanc�
			if(g.get_caster().getGUID() == target.getGUID())
			{
				GestorSalida.GAME_SEND_GDZ_PACKET_TO_FIGHT(this, 7, "-", g.get_cell().getID(), g.get_size(), 4);
				GestorSalida.GAME_SEND_GDC_PACKET_TO_FIGHT(this, 7, g.get_cell().getID());
				_glyphs.remove(g);
			}
		}
		
		//on supprime les pieges du joueur
		ArrayList<Piege> Ps = new ArrayList<>();
		Ps.addAll(_traps);
		for(Piege p : Ps)
		{
			if(p.get_caster().getGUID() == target.getGUID())
			{
				p.desappear();
				_traps.remove(p);
			}
		}
		if(target.canPlay() && _ordreJeu.get(_curPlayer).getGUID() == target.getGUID())
		{
			endTurn();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException ignored) {}
	}

	public int getTeamID(int guid)
	{
		if(_team0.containsKey(guid))
			return 1;
		if(_team1.containsKey(guid))
			return 2;
		if(_spec.containsKey(guid))
			return 4;
		return -1;
	}
	
	public int getOtherTeamID(int guid)
	{
		if(_team0.containsKey(guid))
			return 2;
		if(_team1.containsKey(guid))
			return 1;
		return -1;
	}

	public void tryCaC(Personaje perso, int cellID)
	{
		Fighter caster = getFighterByPerso(perso);
		
		if(caster == null)return;
		
		if(_ordreJeu.get(_curPlayer).getGUID() != caster.getGUID())//Si ce n'est pas a lui de jouer
			return;
		 // Pour les challenges, v�rif sur CaC
		if ((this._type == 4) && (this._challenges.size() > 0) && !this._ordreJeu.get(this._curPlayer).isInvocation() && !this._ordreJeu.get(this._curPlayer).isDouble() && !this._ordreJeu.get(this._curPlayer).isPerco())
		{
			for (Entry<Integer, Retos> c : this._challenges.entrySet()) {
				if (c.getValue() == null) 
					continue;
				c.getValue().onPlayer_cac(this._ordreJeu.get(this._curPlayer));
			}
		}
		// Fin Challenges
		if(perso.getObjetByPos(Constantes.ITEM_POS_ARME) == null)//S'il n'a pas de CaC
		{
			if(_curFighterPA < 4)//S'il n'a pas assez de PA
				return;
			
			GestorSalida.GAME_SEND_GAS_PACKET_TO_FIGHT(this, 7, perso.get_GUID());
			
			//Si le joueur est invisible
			if(caster.isHide())
				caster.unHide(-1);
			
			Fighter target = _map.getMapa(cellID).getFirstFighter();
			
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 303, perso.get_GUID()+"", cellID+"");
			
			if(target != null)
			{
				int dmg = Formulas.getRandomJet("1d5+0");
				int finalDommage = Formulas.calculFinalDommage(this,caster, target, Constantes.ELEMENT_NEUTRE, dmg,false,true, -1);
				finalDommage = EfectoHechizo.applyOnHitBuffs(finalDommage,target,caster,this);//S'il y a des buffs sp�ciaux
				
				if(finalDommage>target.getPDV())
					finalDommage = target.getPDV();//Target va mourir
				target.removePDV(finalDommage);
				finalDommage = -(finalDommage);
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
			}
			_curFighterPA-= 4;
			GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102,perso.get_GUID()+"",perso.get_GUID()+",-4");
			GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, perso.get_GUID());
			
			if(target.getPDV() <=0)
				onFighterDie(target, caster);
			verifIfTeamAllDead();
		}else
		{
			Objeto arme = perso.getObjetByPos(Constantes.ITEM_POS_ARME);
			
			//Pierre d'�mes = EC
			if(arme.getTemplate().getType() == 83)
			{
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 305, perso.get_GUID()+"", "");//Echec Critique Cac
				GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, perso.get_GUID());//Fin de l'action
				try{
					Thread.sleep(500);
				}catch(Exception ignored){}
				endTurn();
			}
			
			int PACost = arme.getTemplate().getPACost();
			
			if(_curFighterPA < PACost)//S'il n'a pas assez de PA			
				return;
			
			GestorSalida.GAME_SEND_GAS_PACKET_TO_FIGHT(this, 7, perso.get_GUID());
			
			boolean isEc = arme.getTemplate().getTauxEC() != 0 && Formulas.getRandomValue(1, arme.getTemplate().getTauxEC()) == arme.getTemplate().getTauxEC();
			if(isEc)
			{
				if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(perso.getNombre()+" Echec critique sur le CaC ");
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 305, perso.get_GUID()+"", "");//Echec Critique Cac
				GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, perso.get_GUID());//Fin de l'action
				endTurn();
			}else
			{
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 303, perso.get_GUID()+"", cellID+"");
				boolean isCC = caster.testIfCC(arme.getTemplate().getTauxCC());
				if(isCC)
				{
					if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog(perso.getNombre()+" Coup critique sur le CaC");
					GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 301, perso.get_GUID()+"", "0");
				}
				
				//Si le joueur est invisible
				if(caster.isHide())caster.unHide(-1);
				
				ArrayList<EfectoHechizo> effets = arme.getEffects();
				if(isCC)
				{
					effets = arme.getCritEffects();
				}
				for(EfectoHechizo SE : effets)
				{
					if(_state != Constantes.FIGHT_STATE_ACTIVE)break;
					ArrayList<Fighter> cibles = Camino.getCiblesByZoneByWeapon(this,arme.getTemplate().getType(),_map.getMapa(cellID),caster.get_fightCell().getID());
					SE.setTurn(0);
					SE.applyToFight(this, caster, cibles, true);
				}
				_curFighterPA-= PACost;
				GestorSalida.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102,perso.get_GUID()+"",perso.get_GUID()+",-"+PACost);
				GestorSalida.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, perso.get_GUID());
				verifIfTeamAllDead();
			}
		}
	}
	
	public Fighter getFighterByPerso(Personaje perso)
	{
		Fighter fighter = null;
		if(_team0.get(perso.get_GUID()) != null)
			fighter = _team0.get(perso.get_GUID());
		if(_team1.get(perso.get_GUID()) != null)
			fighter = _team1.get(perso.get_GUID());
		return fighter;
	}

	public Fighter getCurFighter()
	{
		return _ordreJeu.get(_curPlayer);
	}

	public void refreshCurPlayerInfos()
	{
		_curFighterPA = _ordreJeu.get(_curPlayer).getTotalStats().getEffect(Constantes.STATS_ADD_PA) - _curFighterUsedPA;
		_curFighterPM = _ordreJeu.get(_curPlayer).getTotalStats().getEffect(Constantes.STATS_ADD_PM) - _curFighterUsedPM;
	}

	public void leftFight(Personaje perso, Personaje target)
	{
		if(perso == null)return;
		Fighter F = this.getFighterByPerso(perso);
		Fighter T = null;
		if(target != null) T = this.getFighterByPerso(target);
		
		if(MainServidor.MOSTRAR_ENVIADOS)
		{
			if(target != null && T != null) 
			{
				JuegoServidor.addToLog(perso.getNombre()+" expulse "+T.getPersonnage().getNombre());
			}else
			{
				JuegoServidor.addToLog(perso.getNombre()+" a quitter le combat");
			}
		}
		
		if(F != null)
		{
			
			switch(_type)
			{
				case Constantes.FIGHT_TYPE_CHALLENGE://D�fie
				case Constantes.FIGHT_TYPE_AGRESSION://PVP
				case Constantes.FIGHT_TYPE_PVM://PVM
				case Constantes.FIGHT_TYPE_PVT://Perco
					if(_state >= Constantes.FIGHT_STATE_ACTIVE)
					{
						onFighterDie(F, F);
						boolean StillInFight = false;
						if(_type == Constantes.FIGHT_TYPE_CHALLENGE || _type == Constantes.FIGHT_TYPE_AGRESSION || _type == Constantes.FIGHT_TYPE_PVT)
						{
							StillInFight = verifyStillInFightTeam(F.getGUID());
						}else
						{
							StillInFight = verifyStillInFight();
						}
						
						if(!StillInFight)//S'arr�te ici si il ne reste plus personne dans le combat et dans la team
						{
							//Met fin au combat
							verifIfTeamAllDead();
						}else
						{
							F.setLeft(true);
							GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(_map, F.getGUID());
								
							Personaje P = F.getPersonnage();
							P.set_duelID(-1);
							P.set_ready(false);
							P.fullPDV();
							P.set_fight(null);
							P.setSitted(false);
							P.set_away(false);
							
							if(_type == Constantes.FIGHT_TYPE_AGRESSION || _type == Constantes.FIGHT_TYPE_PVM || _type == Constantes.FIGHT_TYPE_PVT)
							{
								int EnergyLoos = Formulas.getLoosEnergy(P.get_lvl(), _type==1, _type==5);
								int Energy = P.get_energy() - EnergyLoos;
								if(Energy < 0) Energy = 0;
								P.set_energy(Energy);
								if(P.isConectado())
									GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(P, "034;"+EnergyLoos);
								
								if(_type == Constantes.FIGHT_TYPE_AGRESSION)
								{
									int honor = P.get_honor()-500;
									if(honor < 0) honor = 0;
									P.set_honor(honor);
									if(P.isConectado())
										GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(P, "076;"+honor);
								}
								
								
								try
								{
									Thread.sleep(1000);
								}catch(Exception ignored){}

								if(Energy == 0)
								{
									P.set_Ghosts();
								}else
								{
									P.warpToSavePos();
									P.set_PDV(1);
								}
							}
							
							if(P.isConectado())
							{
								try
								{
									Thread.sleep(200);
								}catch(Exception ignored){}
								GestorSalida.GAME_SEND_GV_PACKET(P);
								P.refreshMapAfterFight();
							}
							
							//si c'�tait a son tour de jouer
							if(_ordreJeu.get(_curPlayer) == null)return;
							if(_ordreJeu.get(_curPlayer).getGUID() == F.getGUID())
							{
								endTurn();
							}
						}
					}else if(_state == Constantes.FIGHT_STATE_PLACE)
					{
						boolean isValid1 = false;
						if(T != null)
						{
							if(_init0 != null &&_init0.getPersonnage() != null)
							{
								if(F.getPersonnage().get_GUID() == _init0.getPersonnage().get_GUID())
								{
									isValid1 = true;
								}
							}
							if(_init1 != null &&_init1.getPersonnage() != null)
							{
								if(F.getPersonnage().get_GUID() == _init1.getPersonnage().get_GUID())
								{
									isValid1 = true;
								}
							}
						}
						
						if(isValid1)//Celui qui fait l'action a lancer le combat et leave un autre personnage
						{
							if((T.getTeam() == F.getTeam()) && (T.getGUID() != F.getGUID()))
							{
								if(MainServidor.MOSTRAR_ENVIADOS) System.out.println("EXLUSION DE : "+T.getPersonnage().getNombre());
								GestorSalida.GAME_SEND_ON_FIGHTER_KICK(this, T.getPersonnage().get_GUID(), getTeamID(T.getGUID()));
								if(_type == Constantes.FIGHT_TYPE_AGRESSION || _type == Constantes.FIGHT_TYPE_CHALLENGE || _type == Constantes.FIGHT_TYPE_PVT) GestorSalida.GAME_SEND_ON_FIGHTER_KICK(this, T.getPersonnage().get_GUID(), getOtherTeamID(T.getGUID()));
								Personaje P = T.getPersonnage();
								P.set_duelID(-1);
								P.set_ready(false);
								P.fullPDV();
								P.set_fight(null);
								P.setSitted(false);
								P.set_away(false);
								
								if(P.isConectado())
								{
									try
									{
										Thread.sleep(200);
									}catch(Exception ignored){}
									GestorSalida.GAME_SEND_GV_PACKET(P);
									P.refreshMapAfterFight();
								}
								
								//On le supprime de la team
								if(_team0.containsKey(T.getGUID()))
								{
									T._cell.removeFighter(T);
									_team0.remove(T.getGUID());
								}
								else if(_team1.containsKey(T.getGUID()))
								{
									T._cell.removeFighter(T);
									_team1.remove(T.getGUID());
								}
								for(Personaje z : _mapOld.getPersos()) FightStateAddFlag(this._mapOld, z);
							}
						}else if(T == null)//Il leave de son plein gr� donc (T = null)
						{
							boolean isValid2 = false;
							if(_init0 != null &&_init0.getPersonnage() != null)
							{
								if(F.getPersonnage().get_GUID() == _init0.getPersonnage().get_GUID())
								{
									isValid2 = true;
								}
							}
							if(_init1 != null &&_init1.getPersonnage() != null)
							{
								if(F.getPersonnage().get_GUID() == _init1.getPersonnage().get_GUID())
								{
									isValid2 = true;
								}
							}
							
							if(isValid2)//Soit il a lancer le combat => annulation du combat
							{
								for(Fighter f : this.getFighters(F.getTeam2()))
								{
									Personaje P = f.getPersonnage();
									P.set_duelID(-1);
									P.set_ready(false);
									P.fullPDV();
									P.set_fight(null);
									P.setSitted(false);
									P.set_away(false);
									
									if(F.getPersonnage().get_GUID() != f.getPersonnage().get_GUID())//Celui qui a join le fight revient sur la map
									{
										if(P.isConectado())
										{
											try
											{
												Thread.sleep(200);
											}catch(Exception ignored){}
											GestorSalida.GAME_SEND_GV_PACKET(P);
											P.refreshMapAfterFight();
										}
									}else//Celui qui a fait le fight meurt + perte honor
									{
										if(_type == Constantes.FIGHT_TYPE_AGRESSION || _type == Constantes.FIGHT_TYPE_PVM || _type == Constantes.FIGHT_TYPE_PVT)
										{
											int EnergyLoos = Formulas.getLoosEnergy(P.get_lvl(), _type==1, _type==5);
											int Energy = P.get_energy() - EnergyLoos;
											if(Energy < 0) Energy = 0;
											P.set_energy(Energy);
											if(P.isConectado())
												GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(P, "034;"+EnergyLoos);
											
											if(_type == Constantes.FIGHT_TYPE_AGRESSION)
											{
												int honor = P.get_honor()-500;
												if(honor < 0) honor = 0;
												P.set_honor(honor);
												if(P.isConectado())
													GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(P, "076;"+honor);
											}
											
											
											try
											{
												Thread.sleep(1000);
											}catch(Exception ignored){}
											if(Energy == 0)
											{
												P.set_Ghosts();
											}else
											{
												P.warpToSavePos();
												P.set_PDV(1);
											}
										}
										
										if(P.isConectado())
										{	
											try
											{
												Thread.sleep(200);
											}catch(Exception ignored){}
											GestorSalida.GAME_SEND_GV_PACKET(P);
											P.refreshMapAfterFight();
										}
									}
								}
								_ticMyTimer_startTime = 0L;
		                        _ticMyTimer_endTurn = false;
								if(_type == Constantes.FIGHT_TYPE_AGRESSION || _type == Constantes.FIGHT_TYPE_CHALLENGE || _type == Constantes.FIGHT_TYPE_PVT)
								{
									for(Fighter f : this.getFighters(F.getOtherTeam()))
									{
										if(f.getPersonnage() == null) continue;
										Personaje P = f.getPersonnage();
										P.set_duelID(-1);
										P.set_ready(false);
										P.fullPDV();
										P.set_fight(null);
										P.setSitted(false);
										P.set_away(false);
										
										if(P.isConectado())
										{
											try
											{
												Thread.sleep(200);
											}catch(Exception ignored){}
											GestorSalida.GAME_SEND_GV_PACKET(P);
											P.refreshMapAfterFight();
										}
									}
								}
								_state = 4;//Nous assure de ne pas d�marrer le combat
								Mundo.getCarte(_map.getID()).removeFight(_id);
								GestorSalida.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(Mundo.getCarte(_map.getID()));
								GestorSalida.GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(this._mapOld,_init0.getGUID());
								if(_type == Constantes.FIGHT_TYPE_PVT)
								{
									//On actualise la guilde+Message d'attaque FIXME
									for(Personaje z : Mundo.getGuild(_guildID).getMembers())
									{
										if(z == null) continue;
										if(z.isConectado())
										{
											GestorSalida.GAME_SEND_gITM_PACKET(z, Recaudador.parsetoGuild(z.get_guild().get_id()));
											GestorSalida.GAME_SEND_MESSAGE(z, "Votre percepteur remporte la victioire.", MainServidor.CONFIG_MOTD_COLOR);
										}
									}
									_perco.set_inFight((byte)0);
									_perco.set_inFightID((byte)-1);
									for(Personaje z : Mundo.getCarte(_perco.get_mapID()).getPersos())
									{
										if(z == null) continue;
										GestorSalida.GAME_SEND_MAP_PERCO_GMS_PACKETS(z.getCuenta().getGameThread().get_out(), z.getActualMapa());
									}
								}
								if(_type == Constantes.FIGHT_TYPE_PVM)
								{			
									int align = -1;
									if(_team1.size() >0)
									{
										 _team1.get(_team1.keySet().toArray()[0]).getMob().getTemplate().getAlign();
									}
									//Si groupe non fixe
									if(!_mobGroup.isFix()) Mundo.getCarte(_map.getID()).spawnGroup(align, 1, true,_mobGroup.getCeldaID());//Respawn d'un groupe
								}
								_map = null;
								_ordreJeu = null;
							}else//Soit il a rejoin le combat => Left de lui seul
							{
								GestorSalida.GAME_SEND_ON_FIGHTER_KICK(this, F.getPersonnage().get_GUID(), getTeamID(F.getGUID()));
								if(_type == Constantes.FIGHT_TYPE_AGRESSION || _type == Constantes.FIGHT_TYPE_CHALLENGE || _type == Constantes.FIGHT_TYPE_PVT) GestorSalida.GAME_SEND_ON_FIGHTER_KICK(this, F.getPersonnage().get_GUID(), getOtherTeamID(F.getGUID()));
								Personaje P = F.getPersonnage();
								P.set_duelID(-1);
								P.set_ready(false);
								P.fullPDV();
								P.set_fight(null);
								P.setSitted(false);
								P.set_away(false);
								
								if(_type == Constantes.FIGHT_TYPE_AGRESSION || _type == Constantes.FIGHT_TYPE_PVM || _type == Constantes.FIGHT_TYPE_PVT)
								{
									int EnergyLoos = Formulas.getLoosEnergy(P.get_lvl(), _type==1, _type==5);
									int Energy = P.get_energy() - EnergyLoos;
									if(Energy < 0) Energy = 0;
									P.set_energy(Energy);
									if(P.isConectado())
										GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(P, "034;"+EnergyLoos);
									
									if(_type == Constantes.FIGHT_TYPE_AGRESSION)
									{
										int honor = P.get_honor()-500;
										if(honor < 0) honor = 0;
										P.set_honor(honor);
										if(P.isConectado())
											GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(P, "076;"+honor);
									}
									
									try
									{
										Thread.sleep(1000);
									}catch(Exception ignored){}
									if(Energy == 0)
									{
										P.set_Ghosts();
									}else
									{
										P.warpToSavePos();
										P.set_PDV(1);
									}
								}
								
								if(P.isConectado())
								{
									try
									{
										Thread.sleep(200);
									}catch(Exception ignored){}
									GestorSalida.GAME_SEND_GV_PACKET(P);
									P.refreshMapAfterFight();
								}
								
								//On le supprime de la team
								if(_team0.containsKey(F.getGUID()))
								{
									F._cell.removeFighter(F);
									_team0.remove(F.getGUID());
								}
								else if(_team1.containsKey(F.getGUID()))
								{
									F._cell.removeFighter(F);
									_team1.remove(F.getGUID());
								}
								for(Personaje z : _mapOld.getPersos()) FightStateAddFlag(this._mapOld, z);
							}
						}
					}else
					{
						if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Phase de combat non geree, type de combat:"+_type+" T:"+T+" F:"+F);
					}
				break;
				default:
					if(MainServidor.MOSTRAR_ENVIADOS) JuegoServidor.addToLog("Type de combat non geree, type de combat:"+_type+" T:"+T+" F:"+F);
				break;
			}
		}else//Si perso en spec
		{
			GestorSalida.GAME_SEND_GV_PACKET(perso);
			_spec.remove(perso.get_GUID());
			perso.setSitted(false);
			perso.set_fight(null);
			perso.set_away(false);
		}
	}
	
	public String getGTL()
	{
		String packet = "GTL";
		for(Fighter f: get_ordreJeu())
		{
			packet += "|"+f.getGUID();
		}
		return packet+(char)0x00;
	}

	public int getNextLowerFighterGuid()
	{
		int g = -1;
		for(Fighter f : getFighters(3))
		{
			if(f.getGUID() < g)
				g = f.getGUID();
		}
		g--;
		return g;
	}

	public void addFighterInTeam(Fighter f, int team)
	{
		if(team == 0)
			_team0.put(f.getGUID(), f);
		else if (team == 1)
			_team1.put(f.getGUID(), f);
	}

	public String parseFightInfos()
	{
		StringBuilder infos = new StringBuilder();
		infos.append(_id).append(";");
		long time = System.nanoTime()-_startTime;
		infos.append((_startTime  == 0?"-1":time)).append(";");
		//Team1
		infos.append("0,");//0 car toujours joueur :)
		//Team2
		//Team2
		//Team2
		//Team2
		switch (_type) {
			case Constantes.FIGHT_TYPE_CHALLENGE -> {
				infos.append("0,");
				infos.append(_team0.size()).append(";");
				infos.append("0,");
				infos.append("0,");
				infos.append(_team1.size()).append(";");
			}
			case Constantes.FIGHT_TYPE_AGRESSION -> {
				infos.append(_init0.getPersonnage().get_align()).append(",");
				infos.append(_team0.size()).append(";");
				infos.append("0,");
				infos.append(_init1.getPersonnage().get_align()).append(",");
				infos.append(_team1.size()).append(";");
			}
			case Constantes.FIGHT_TYPE_PVM -> {
				infos.append("0,");
				infos.append(_team0.size()).append(";");
				infos.append("1,");
				infos.append(_team1.get(_team1.keySet().toArray()[0]).getMob().getTemplate().getAlign()).append(",");
				infos.append(_team1.size()).append(";");
			}
			case Constantes.FIGHT_TYPE_PVT -> {
				infos.append("0,");
				infos.append(_team0.size()).append(";");
				infos.append("4,");
				infos.append("0,");
				infos.append(_team1.size()).append(";");
			}
		}
		return infos.toString();
	}

	public void showCaseToTeam(int guid, int cellID)
	{
		int teams = getTeamID(guid)-1;
		if(teams == 4)return;//Les spectateurs ne montrent pas
		ArrayList<PrintWriter> PWs = new ArrayList<>();
		if(teams == 0)
		{
			for(Entry<Integer,Fighter> e : _team0.entrySet())
			{
				if(e.getValue().getPersonnage() != null && e.getValue().getPersonnage().getCuenta().getGameThread() != null)
					PWs.add(e.getValue().getPersonnage().getCuenta().getGameThread().get_out());
			}
		}
		else if(teams == 1)
		{
			for(Entry<Integer,Fighter> e : _team1.entrySet())
			{
				if(e.getValue().getPersonnage() != null && e.getValue().getPersonnage().getCuenta().getGameThread() != null)
					PWs.add(e.getValue().getPersonnage().getCuenta().getGameThread().get_out());
			}
		}
		GestorSalida.GAME_SEND_FIGHT_SHOW_CASE(PWs, guid, cellID);
	}
	
	public void showCaseToAll(int guid, int cellID)
	{
		ArrayList<PrintWriter> PWs = new ArrayList<>();
		for(Entry<Integer,Fighter> e : _team0.entrySet())
		{
			if(e.getValue().getPersonnage() != null && e.getValue().getPersonnage().getCuenta().getGameThread() != null)
				PWs.add(e.getValue().getPersonnage().getCuenta().getGameThread().get_out());
		}
		for(Entry<Integer,Fighter> e : _team1.entrySet())
		{
			if(e.getValue().getPersonnage() != null && e.getValue().getPersonnage().getCuenta().getGameThread() != null)
				PWs.add(e.getValue().getPersonnage().getCuenta().getGameThread().get_out());
		}
		for(Entry<Integer, Personaje> e : _spec.entrySet())
		{
			PWs.add(e.getValue().getCuenta().getGameThread().get_out());
		}
		GestorSalida.GAME_SEND_FIGHT_SHOW_CASE(PWs, guid, cellID);
	}

	public void joinAsSpect(Personaje p)
	{
		if(!specOk  || _state != Constantes.FIGHT_STATE_ACTIVE)
		{
			GestorSalida.ENVIAR_MENSAJE_DESDE_LANG(p, "157");
			return;
		}
		long timeRestant = (MainServidor.CONFIG_MS_PER_TURN - 1) - (System.currentTimeMillis() - _ticMyTimer_startTime);
		p.getActualCelda().removePlayer(p.get_GUID());
		GestorSalida.GAME_SEND_GJK_PACKET(p, _state, 0, 0, 1, timeRestant, _type);
		GestorSalida.GAME_SEND_GS_PACKET(p);
		GestorSalida.GAME_SEND_GTL_PACKET(p,this);
		GestorSalida.GAME_SEND_ERASE_ON_MAP_TO_MAP(p.getActualMapa(), p.get_GUID());
		GestorSalida.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,_map,p);
		GestorSalida.GAME_SEND_GAMETURNSTART_PACKET(p,_ordreJeu.get(_curPlayer).getGUID(), Constantes.TIME_BY_TURN);
		_spec.put(p.get_GUID(), p);
		p.set_fight(this);
		GestorSalida.ENVIAR_MENSAJE_DESDE_LANG_EN_PELEA(this, 7, "036;"+p.getNombre());
		
		if ((this._type == 4) && (this._challenges.size() > 0))
		{
			for (Entry<Integer, Retos> c : this._challenges.entrySet()) {
				if (c.getValue() == null) 
					continue; 
				GestorSalida.send(p, c.getValue().parseToPacket());
			}
		}
		
	}

	public boolean verifyStillInFight()//Return true si au moins un joueur est encore dans le combat
	{
		for(Fighter f : _team0.values())
		{
			if(f.isPerco()) return true;
			if(f.isInvocation() 
			|| f.isDead()
			|| f.getPersonnage() == null
			|| f.getMob() != null
			|| f._double != null
			|| f.hasLeft())
			{
				continue;
			}
			if(f.getPersonnage() != null && f.getPersonnage().getPelea() != null
					&& f.getPersonnage().getPelea().get_id() == this.get_id()) //Si il n'est plus dans ce combat
			{
				return true;
			}
		}
		for(Fighter f : _team1.values())
		{
			if(f.isPerco()) return true;
			if(f.isInvocation() 
					|| f.isDead()
					|| f.getPersonnage() == null
					|| f.getMob() != null
					|| f._double != null
					|| f.hasLeft())
					{
						continue;
					}
			if(f.getPersonnage() != null && f.getPersonnage().getPelea() != null
					&& f.getPersonnage().getPelea().get_id() == this.get_id()) //Si il n'est plus dans ce combat
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean verifyStillInFightTeam(int guid)//Return true si au moins un joueur est encore dans la team
	{
		if(_team0.containsKey(guid))
		{
			for(Fighter f : _team0.values())
			{
				if(f.isPerco()) return true;
				if(f.isInvocation() 
						|| f.isDead()
						|| f.getPersonnage() == null
						|| f.getMob() != null
						|| f._double != null
						|| f.hasLeft())
						{
							continue;
						}
				if(f.getPersonnage() != null && f.getPersonnage().getPelea() != null
						&& f.getPersonnage().getPelea().get_id() == this.get_id()) //Si il n'est plus dans ce combat
				{
					return true;
				}
			}
		}else if(_team1.containsKey(guid))
		{
			for(Fighter f : _team1.values())
			{
				if(f.isPerco()) return true;
				if(!f.isInvocation() 
						|| f.isDead()
						|| f.getPersonnage() == null
						|| f.getMob() != null
						|| f._double != null
						|| f.hasLeft())
						{
							continue;
						}
				if(f.getPersonnage() != null && f.getPersonnage().getPelea() != null
						&& f.getPersonnage().getPelea().get_id() == this.get_id()) //Si il n'est plus dans ce combat
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static void FightStateAddFlag(Mapa _map, Personaje P)
	{
		for(Entry<Integer, Pelea> fight : _map.get_fights().entrySet())
		{
			if(fight.getValue()._state == Constantes.FIGHT_STATE_PLACE)
			{
				if(fight.getValue()._type == Constantes.FIGHT_TYPE_CHALLENGE)
				{
					GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(P, fight.getValue()._init0.getPersonnage().getActualMapa(),0,fight.getValue()._init0.getGUID(),fight.getValue()._init1.getGUID(),fight.getValue()._init0.getPersonnage().getActualCelda().getID(),"0;-1", fight.getValue()._init1.getPersonnage().getActualCelda().getID(), "0;-1");
					for(Entry<Integer, Fighter> F : fight.getValue()._team0.entrySet())
					{
						if(MainServidor.MOSTRAR_ENVIADOS) System.out.println(F.getValue().getPersonnage().getNombre());
						GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(P, fight.getValue()._init0.getPersonnage().getActualMapa(),fight.getValue()._init0.getGUID(), fight.getValue()._init0);
					}
					for(Entry<Integer, Fighter> F : fight.getValue()._team1.entrySet())
					{
						if(MainServidor.MOSTRAR_ENVIADOS) System.out.println(F.getValue().getPersonnage().getNombre());
						GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(P, fight.getValue()._init1.getPersonnage().getActualMapa(),fight.getValue()._init1.getGUID(), fight.getValue()._init1);
					}
				}else if(fight.getValue()._type == Constantes.FIGHT_TYPE_AGRESSION)
				{
					GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(P, fight.getValue()._init0.getPersonnage().getActualMapa(),0,fight.getValue()._init0.getGUID(),fight.getValue()._init1.getGUID(),fight.getValue()._init0.getPersonnage().getActualCelda().getID(),"0;"+fight.getValue()._init0.getPersonnage().get_align(), fight.getValue()._init1.getPersonnage().getActualCelda().getID(), "0;"+fight.getValue()._init1.getPersonnage().get_align());
					for(Entry<Integer, Fighter> F : fight.getValue()._team0.entrySet())
					{
						if(MainServidor.MOSTRAR_ENVIADOS) System.out.println(F.getValue().getPersonnage().getNombre());
						GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(P, fight.getValue()._init0.getPersonnage().getActualMapa(),fight.getValue()._init0.getGUID(), fight.getValue()._init0);
					}
					for(Entry<Integer, Fighter> F : fight.getValue()._team1.entrySet())
					{
						if(MainServidor.MOSTRAR_ENVIADOS) System.out.println(F.getValue().getPersonnage().getNombre());
						GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(P, fight.getValue()._init1.getPersonnage().getActualMapa(),fight.getValue()._init1.getGUID(), fight.getValue()._init1);
					}
				}else if(fight.getValue()._type == Constantes.FIGHT_TYPE_PVM)
				{
					GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(P, fight.getValue()._init0.getPersonnage().getActualMapa(),4,fight.getValue()._init0.getGUID(),fight.getValue()._mobGroup.getID(),(fight.getValue()._init0.getPersonnage().getActualCelda().getID()+1),"0;-1",fight.getValue()._mobGroup.getCeldaID(),"1;-1");
					for(Entry<Integer, Fighter> F : fight.getValue()._team0.entrySet())
					{
						if(MainServidor.MOSTRAR_ENVIADOS) System.out.println("PVM1: "+F.getValue().getPersonnage().getNombre());
						GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(P, fight.getValue()._init0.getPersonnage().getActualMapa(),fight.getValue()._init0.getGUID(), fight.getValue()._init0);
					}
					for(Entry<Integer, Fighter> F : fight.getValue()._team1.entrySet())
					{
						if(MainServidor.MOSTRAR_ENVIADOS) System.out.println("PVM2: "+F.getValue());
						GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(P, fight.getValue()._map,fight.getValue()._mobGroup.getID(), F.getValue());
					}
				}else if(fight.getValue()._type == Constantes.FIGHT_TYPE_PVT)
				{
					GestorSalida.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(P, fight.getValue()._init0.getPersonnage().getActualMapa(),5,fight.getValue()._init0.getGUID(),fight.getValue()._perco.getGuid(),(fight.getValue()._init0.getPersonnage().getActualCelda().getID()+1),"0;-1",fight.getValue()._perco.get_cellID(),"3;-1");
					for(Entry<Integer, Fighter> F : fight.getValue()._team0.entrySet())
					{
						if(MainServidor.MOSTRAR_ENVIADOS) System.out.println("PVT1: "+F.getValue().getPersonnage().getNombre());
						GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(P, fight.getValue()._init0.getPersonnage().getActualMapa(),fight.getValue()._init0.getGUID(), fight.getValue()._init0);
					}
					for(Entry<Integer, Fighter> F : fight.getValue()._team1.entrySet())
					{
						if(MainServidor.MOSTRAR_ENVIADOS) System.out.println("PVT2: "+F.getValue());
						GestorSalida.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(P, fight.getValue()._map,fight.getValue()._perco.getGuid(), F.getValue());
					}
				}
			}
		}
	}
	
	public static int getFightIDByFighter(Mapa _map, int guid)
	{
		for(Entry<Integer, Pelea> fight : _map.get_fights().entrySet())
		{
			for(Entry<Integer, Fighter> F : fight.getValue()._team0.entrySet())
			{
				if(F.getValue().getPersonnage() != null && F.getValue().getGUID() == guid)
				{
					return fight.getValue().get_id();
				}
			}
		}
		return 0;
	}
	
	public Map<Integer,Fighter> getDeadList()
	{
		return deadList;
	}	
		
	public void delOneDead(Fighter target)
	{
		deadList.remove(target.getGUID());
	}
}
