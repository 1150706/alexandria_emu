package objetos;
import java.util.ArrayList;

import comunes.Constantes;
import comunes.Mundo;
import comunes.Mundo.Doble;

public class PiedraAlma extends Objeto {
	private final ArrayList<Doble<Integer, Integer>> _monsters;
	
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
				
				_monsters.add(new Doble<>(monstre, level));
				
			}catch(Exception e){continue;}
		}
	}
	
	public String parseStatsString()
	{
		StringBuilder stats = new StringBuilder();
		boolean isFirst = true;
		for(Doble<Integer, Integer> coupl : _monsters)
		{
			if(!isFirst)
				stats.append(",");
			
			try
			{
				stats.append("26f#0#0#").append(Integer.toHexString(coupl.primero));
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
		for(Doble<Integer, Integer> curMob : _monsters)
		{
			if(!isFirst)
				toReturn.append(";");
			
			toReturn.append(curMob.primero).append(",").append(curMob.segundo).append(",").append(curMob.segundo);
			
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public String parseToSave()
	{
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true;
		for(Doble<Integer, Integer> curMob : _monsters)
		{
			if(!isFirst)
				toReturn.append("|");
			toReturn.append(curMob.primero).append(",").append(curMob.segundo);
			isFirst = false;
		}
		return toReturn.toString();
	}
}
