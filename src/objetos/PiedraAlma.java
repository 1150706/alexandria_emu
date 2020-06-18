package objetos;
import java.util.ArrayList;

import comunes.Constantes;
import comunes.Mundo;
import comunes.Mundo.Couple;

public class PiedraAlma extends Objeto {
	private final ArrayList<Couple<Integer, Integer>> _monsters;
	
	public PiedraAlma(int Guid, int qua, int template, int pos, String strStats)
	{
		this.guid = Guid;
		this.template = Mundo.getObjTemplate(template);	//7010 = Pierre d'ame pleine
		this.quantity = 1;
		this.position = Constantes.ITEM_POS_NO_EQUIPED;
		
		_monsters = new ArrayList<>();	//Couple<MonstreID,Level>
		parseStringToStats(strStats);
	}
	
	public void parseStringToStats(String monsters) //Dans le format "monstreID,lvl|monstreID,lvl..."
	{
		String[] split = monsters.split("\\|");
		for(String s : split)
		{	
			try
			{
				int monstre = Integer.parseInt(s.split(",")[0]);
				int level = Integer.parseInt(s.split(",")[1]);
				
				_monsters.add(new Couple<>(monstre, level));
				
			}catch(Exception e){continue;}
		}
	}
	
	public String parseStatsString()
	{
		StringBuilder stats = new StringBuilder();
		boolean isFirst = true;
		for(Couple<Integer, Integer> coupl : _monsters)
		{
			if(!isFirst)
				stats.append(",");
			
			try
			{
				stats.append("26f#0#0#").append(Integer.toHexString(coupl.first));
			}catch(Exception e)
			{
				e.printStackTrace();
				continue;
			}

			isFirst = false;
		}
		return stats.toString();
	}
	
	public String parseGroupData()//Format : id,lvlMin,lvlMax;id,lvlMin,lvlMax...
	{
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true;
		for(Couple<Integer, Integer> curMob : _monsters)
		{
			if(!isFirst)
				toReturn.append(";");
			
			toReturn.append(curMob.first).append(",").append(curMob.second).append(",").append(curMob.second);
			
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public String parseToSave()
	{
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true;
		for(Couple<Integer, Integer> curMob : _monsters)
		{
			if(!isFirst)
				toReturn.append("|");
			toReturn.append(curMob.first).append(",").append(curMob.second);
			isFirst = false;
		}
		return toReturn.toString();
	}
}
