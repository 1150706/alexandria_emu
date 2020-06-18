package objetos;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import comunes.GestorSQL;
import comunes.GestorSalida;
import comunes.Mundo;
import comunes.Mundo.Couple;

public class Mercadillo {
	/**
	 * Contient les liens associant les templatID au Map de template.
	 * C'est une mani�re plus compr�hensible d'�crire : <categID,Map<LigneID,Ligne>>.
	 * @author Mathieu
	 *
	 */
	private class Categorie
	{
		Map<Integer,Template> _templates = new HashMap<>();//Dans le format <templateID,Template>
		
		@SuppressWarnings("unused")
		int categID;
		
		public Categorie(int categID)
		{
			this.categID = categID;
		}
		
		public void addEntry(HdvEntry toAdd)
		{
			int tempID = toAdd.getObjet().getTemplate().getID();
			if(_templates.get(tempID) == null)
				addTemplate(tempID,toAdd);
			else
				_templates.get(tempID).addEntry(toAdd);
		}
		public void addTemplate(int templateID, HdvEntry toAdd)
		{
			_templates.put(templateID, new Template(templateID, toAdd));
		}
		
		public boolean delEntry(HdvEntry toDel)
		{
			boolean toReturn = false;
			_templates.get(toDel.getObjet().getTemplate().getID()).delEntry(toDel);
			
			if((toReturn = _templates.get(toDel.getObjet().getTemplate().getID()).isEmpty()))
				delTemplate(toDel.getObjet().getTemplate().getID());
			
			return toReturn;
		}
		
		public Template getTemplate(int templateID)
		{
			return _templates.get(templateID);
		}
		
		public ArrayList<HdvEntry> getAllEntry()
		{
			ArrayList<HdvEntry> toReturn = new ArrayList<>();
			
			for(Template curTemp : _templates.values())
			{
				toReturn.addAll(curTemp.getAllEntry());
			}
			return toReturn;
		}
		
		public String parseTemplate()
		{
			boolean isFirst = true;
			String strTemplate = "";
			
			for(int curTemp : _templates.keySet())
			{
				if(!isFirst)
					strTemplate += ";";
				
				strTemplate += curTemp;
				
				isFirst = false;
			}
			
			return strTemplate;
		}
		
		public void delTemplate(int templateID)
		{
			_templates.remove(templateID);
		}
	}
	/**
	 * Contient les liens associant les ID des Lignes � des objets "Ligne".
	 * C'est une mani�re plus compr�hensible d'�crire : <LigneID,Ligne>.
	 * @author Mathieu
	 *
	 */
	private class Template
	{
		int templateID;
		Map<Integer, Ligne> _lignes = new HashMap<>();
		
		public Template(int templateID, HdvEntry toAdd)
		{
			this.templateID = templateID;
			
			addEntry(toAdd);
		}
		
		public void addEntry(HdvEntry toAdd)
		{
			//TODO : Peut-�tre catch� un nullPointerException � cause du for
			for(Ligne curLine : _lignes.values())//Boucle dans toutes les lignes pour essayer de trouver des objets de m�mes stats
			{
				if(curLine.addEntry(toAdd))//Si une ligne l'accepte, arr�te la m�thode.
					return;
			}

			//Si aucune ligne ne l'a accept�, cr�e une nouvelle ligne.
			int ligneID = Mundo.getNextLigneID();
			_lignes.put(ligneID, new Ligne(ligneID, toAdd));
		}
		public Ligne getLigne(int ligneID)
		{
			return _lignes.get(ligneID);
		}
		
		public boolean delEntry(HdvEntry toDel)
		{
			boolean toReturn =  _lignes.get(toDel.getLigneID()).delEntry(toDel);
			if(_lignes.get(toDel.getLigneID()).isEmpty())//Si la ligne est devenue vide
			{
				_lignes.remove(toDel.getLigneID());
			}
			
			return toReturn;
		}
		
		public ArrayList<HdvEntry> getAllEntry()
		{
			ArrayList<HdvEntry> toReturn = new ArrayList<>();
			
			for(Ligne curLine : _lignes.values())
			{
				toReturn.addAll(curLine.getAll());
			}
			return toReturn;
		}
		
		public String parseToEHl()
		{
			String toReturn = templateID + "|";
			
			boolean isFirst = true;
			for (Ligne curLine : _lignes.values())
			{
				if(!isFirst)
					toReturn += "|";
					
				toReturn += curLine.parseToEHl();
				
				isFirst = false;
			}
			return toReturn;
		}
		
		public boolean isEmpty()
		{
			if(_lignes.size() == 0)
				return true;
			
			return false;
		}
	}
	/**
	 * Contient des HdvEntry de m�me template et de m�me statistiques.
	 * Les lignes sont des : ArrayList<HdvEntry>
	 * @author Mathieu
	 *
	 */
	public static class Ligne
	{
		private int ligneID;
		private ArrayList<ArrayList<HdvEntry>> _entries = new ArrayList<>(3);//La premi�re ArrayList est un tableau de 3 (0=1 1=10 2=100 de quantit�)
		private String _strStats;
		private int templateID;
		
		public Ligne(int ligneID, HdvEntry toAdd)
		{
			this.ligneID = ligneID;
			this._strStats = toAdd.getObjet().parseStatsString();
			this.templateID = toAdd.getObjet().getTemplate().getID();
			
			for (int i = 0; i < 3; i++)
			{
				_entries.add(new ArrayList<>());//Boucle 3 fois pour ajouter 3 List vide dans la SuperList
			}
			addEntry(toAdd);
		}
		
		public String getStrStats()
		{
			return this._strStats;
		}
		
		/**
		 * M�thode pour ajouter un HdvEntry � la ligne.
		 * @param toAdd L'objet HdvEntry � ajouter � la ligne
		 * @return Cette fonction retourne false dans le cas o� l'objet � ajouter n'a pas les m�mes stats que la ligne. Dans tout les autres cas, elle retourne true.
		 */
		public boolean addEntry(HdvEntry toAdd)
		{
			if(!haveSameStats(toAdd) && !isEmpty())
				return false;
			
			toAdd.setLigneID(this.ligneID);
			byte index = (byte) (toAdd.getAmount(false) - 1);
			
			_entries.get(index).add(toAdd);
			trier(index);
			
			return true;//Anonce que l'objet � �t� accept�
		}
		public boolean haveSameStats(HdvEntry toAdd)
		{
			return _strStats.equalsIgnoreCase(toAdd.getObjet().parseToSave())
					&& toAdd.getObjet().getTemplate().getType() != 85;//R�cup�re les stats de l'objet et compare avec ceux de la ligne
		}
		
		public HdvEntry doYouHave(int amount, int price)
		{
			int index = amount-1;
			for (int i = 0; i < _entries.get(index).size(); i++) 
			{
				if(_entries.get(index).get(i).getPrice() == price)
					return _entries.get(index).get(i);
			}
			
			
			return null;
		}
		
		public int[] getFirsts()
		{
			int[] toReturn = new int[3];
			
			for (int i = 0; i < _entries.size(); i++) 
			{
				try{
					toReturn[i] = _entries.get(i).get(0).getPrice();//R�cup�re le premier objet de chaque liste
				}catch(IndexOutOfBoundsException e){toReturn[i] = 0;}
			}
			
			return toReturn;
		}
		public ArrayList<HdvEntry> getAll()
		{
			//Additionne le nombre d'objet de chaque quantit�
			int totalSize = _entries.get(0).size() + _entries.get(1).size() + _entries.get(2).size();
			ArrayList<HdvEntry> toReturn = new ArrayList<>(totalSize);

			//Boucler dans les quantit�
			for (ArrayList<HdvEntry> entry : _entries) {
				toReturn.addAll(entry);
			}
			
			return toReturn;
		}
		public boolean delEntry(HdvEntry toDel)
		{
			byte index = (byte) (toDel.getAmount(false) - 1);
			
			boolean toReturn = _entries.get(index).remove(toDel);
			
			trier(index);
			
			return toReturn;
		}
		public HdvEntry delEntry(byte amount)
		{
			byte index = (byte) (amount -1);
			HdvEntry toReturn = _entries.get(index).remove(0);
			trier(index);
			return toReturn;
		}
		
		public String parseToEHl()
		{
			StringBuilder toReturn = new StringBuilder();

			int[] price = getFirsts();
			toReturn.append(ligneID).append(";").append(_strStats).append(";").append((price[0]==0?"":price[0])).append(";").append((price[1]==0?"":price[1])).append(";").append((price[2]==0?"":price[2]));
			
			return toReturn.toString();
		}		
		public String parseToEHm()
		{
			StringBuilder toReturn = new StringBuilder();
			
			int[] prix = getFirsts();
			toReturn.append(ligneID).append("|").append(templateID).append("|").append(_strStats).append("|").append((prix[0]==0?"":prix[0])).append("|").append((prix[1]==0?"":prix[1])).append("|").append((prix[2]==0?"":prix[2]));
			
			return toReturn.toString();
		}
		
		public void trier(byte index)
		{
			Collections.sort(_entries.get(index));
		}
		
		public boolean isEmpty()
		{
			for (ArrayList<HdvEntry> entry : _entries) {
				try {
					if (entry.get(0) != null)//V�rifie s'il existe un objet dans chacune des 3 quantit�
						return false;
				} catch (IndexOutOfBoundsException e) {
				}
			}
			
			return true;
		}
	}
	/**
	 * Contient toutes les informations necessaire sur la vente d'un objet.
	 * -Son prix
	 * -Sa quantit�
	 * -Le nombres d'heures depuis la mise en vente
	 * -Le propri�taire
	 * -Une r�f�rence vers l'objet � vendre.
	 * @author Mathieu
	 *
	 */
	public static class HdvEntry implements Comparable<HdvEntry>
	{
		private int _hdvID;
		private int _price;
		private byte _amount;//Dans le format : 1=1 2=10 3=100
		private Objeto _obj;
		private int _ligneID;
		private int _owner;
		
		public HdvEntry(int price, byte amount, int owner, Objeto obj)
		{
			this._price = price;
			this._amount = amount;
			this._obj = obj;
			this._owner = owner;
			//TODO : Ajouter le nouvel objet dans la bonne categorie et le bon template
		}

		public void setHdvID(int id)
		{
			this._hdvID = id;
		}
		public int getHdvID()
		{
			return this._hdvID;
		}
		public int getPrice()
		{
			return this._price;
		}
		public byte getAmount(boolean parseToRealNumber)
		{
			if(parseToRealNumber)
				return (byte)(Math.pow(10,(double)_amount) / 10);
			else
				return this._amount;
		}
		public Objeto getObjet()
		{
			return this._obj;
		}
		public int getLigneID()
		{
			return this._ligneID;
		}
		public void setLigneID(int ID)
		{
			this._ligneID = ID;
		}
		public int getOwner()
		{
			return this._owner;
		}
		public String parseToEL()
		{
			StringBuilder toReturn = new StringBuilder();
			
			int count = getAmount(true);//Transf�re dans le format (1,10,100) le montant qui etait dans le format (1,2,3)
			toReturn.append(_ligneID).append(";").append(count).append(";").append(_obj.getTemplate().getID()).append(";").append(_obj.parseStatsString()).append(";").append(_price).append(";350");//350 = temps restant
			
			return toReturn.toString();
		}
		public String parseToEmK()
		{
			StringBuilder toReturn = new StringBuilder();
			
			int count = getAmount(true);//Transf�re dans le format (1,10,100) le montant qui etait dans le format (1,2,3)
			toReturn.append(_obj.getGuid()).append("|").append(count).append("|").append(_obj.getTemplate().getID()).append("|").append(_obj.parseStatsString()).append("|").append(_price).append("|350");//350 = temps restant
			
			return toReturn.toString();
		}
		/*
		public String parseItem(char divider)
		{
			int count = getAmount(true);//Transf�re dans le format (1,10,100) le montant qui etait dans le format (1,2,3)
			return _ligneID+divider+count+divider+_obj.getTemplate().getID()+divider+_obj.parseStatsString()+divider+_price+divider+"350";//350 = temps restant
		}
		*/
		public int compareTo(HdvEntry o)
		{
			HdvEntry e = (HdvEntry)o;
			int celuiCi = this.getPrice();
			int autre = e.getPrice();
			if(autre > celuiCi)
				return -1;
			if(autre == celuiCi)
				return 0;
			if(autre < celuiCi )
				return 1;
			return 0;
		}
	}
	
	private int _hdvID;
	private float _taxe;
	private short _sellTime;
	private short _maxCompteItem;
	private String _strCategories;
	private short _lvlMax;
	
	private Map<Integer,Categorie> _categories = new HashMap<>();
	private Map<Integer,Couple<Integer, Integer>> _path = new HashMap<>();	//<LigneID,<CategID,TemplateID>>
	
	private DecimalFormat pattern = new DecimalFormat("0.0"); 
	
	public Mercadillo(int hdvID, float taxe, short sellTime, short maxItemCompte, short lvlMax, String categories)
	{
		this._hdvID = hdvID;
		this._taxe = taxe;
		this._maxCompteItem = maxItemCompte;
		this._strCategories = categories;
		this._lvlMax = lvlMax;
		int categID;
		for(String strCategID : categories.split(","))
		{
			categID = Integer.parseInt(strCategID);
			_categories.put(categID, new Categorie(categID));
		}
	}
	
	public int getHdvID()
	{
		return this._hdvID;
	}
	public float getTaxe()
	{
		return this._taxe;
	}
	public short getSellTime()
	{
		return this._sellTime;
	}
	public short getMaxItemCompte()
	{
		return this._maxCompteItem;
	}
	public String getStrCategories()
	{
		return this._strCategories;
	}
	public short getLvlMax()
	{
		return this._lvlMax;
	}
	
	public String parseToEHl(int templateID)
	{
		int type = Mundo.getObjTemplate(templateID).getType();
		
		return _categories.get(type).getTemplate(templateID).parseToEHl();
	}
	public String parseTemplate(int categID)
	{
		return _categories.get(categID).parseTemplate();
	}
	public String parseTaxe()
	{
		return pattern.format(_taxe).replace(",", ".");
	}
	
	public Ligne getLigne(int ligneID)
	{
		try
		{
			int categ = _path.get(ligneID).first;
			int template = _path.get(ligneID).second;
			
			return _categories.get(categ).getTemplate(template).getLigne(ligneID);
		}
		catch(NullPointerException e)
		{
			return null;
		}
	}
	
	public ArrayList<HdvEntry> getAllEntry()
	{
		ArrayList<HdvEntry> toReturn = new ArrayList<>();
		for(Categorie curCat : _categories.values())
		{
			toReturn.addAll(curCat.getAllEntry());
		}
		
		return toReturn;
	}
	
	public void addEntry(HdvEntry toAdd)
	{
		toAdd.setHdvID(this._hdvID);
		int categ = toAdd.getObjet().getTemplate().getType();
		int template = toAdd.getObjet().getTemplate().getID();
		_categories.get(categ).addEntry(toAdd);
		_path.put(toAdd.getLigneID(), new Couple<>(categ, template));
		
		Mundo.addHdvItem(toAdd.getOwner(), _hdvID, toAdd);
	}
	public boolean delEntry(HdvEntry toDel)
	{
		boolean toReturn =  _categories.get(toDel.getObjet().getTemplate().getType()).delEntry(toDel);
		if(toReturn)
		{
			_path.remove(toDel.getLigneID());
			Mundo.removeHdvItem(toDel.getOwner(), toDel.getHdvID(), toDel);
		}
		
		return toReturn;
	}
	
	public synchronized boolean buyItem(int ligneID,byte amount, int price, Jugador newOwner)
	{
		boolean toReturn = true;
		
		try
		{
			if(newOwner.get_kamas() < price)
				return false;
			
			Ligne ligne = getLigne(ligneID);
			
			HdvEntry toBuy = ligne.doYouHave(amount, price);
			
			newOwner.addKamas(price * -1);//Retire l'argent � l'acheteur (prix et taxe de vente)
			
			if(toBuy.getOwner() != -1)
			{
				Cuenta C = Mundo.getCompte(toBuy.getOwner());
				if(C != null)
				{
					C.setBankKamas(C.getBankKamas()+toBuy.getPrice());//Ajoute l'argent au vendeur
				}
			}
			GestorSalida.GAME_SEND_STATS_PACKET(newOwner);//Met a jour les kamas de l'acheteur
			
			newOwner.addObjet(toBuy.getObjet(), true);//Ajoute l'objet au nouveau propri�taire
			toBuy.getObjet().getTemplate().newSold(toBuy.getAmount(true),price);//Ajoute la ventes au statistiques
			
			delEntry(toBuy);//Retire l'item de l'HDV ainsi que de la liste du vendeur
			
			if(Mundo.getCompte(toBuy.getOwner()) != null && Mundo.getCompte(toBuy.getOwner()).get_curPerso() != null)
			{
				GestorSalida.GAME_SEND_Im_PACKET(Mundo.getCompte(toBuy.getOwner()).get_curPerso(),"065;"+price+"~"+toBuy.getObjet().getTemplate().getID()+"~"+toBuy.getObjet().getTemplate().getID()+"~1");
				//Si le vendeur est connecter, envoie du packet qui lui annonce la vente de son objet
			}
			if(toBuy.getOwner() == -1)
			{
				GestorSQL.guardar_objeto(toBuy.getObjet());
			}
			toBuy = null;
		}
		catch(NullPointerException e)
		{
			toReturn = false;
		}
		
		return toReturn;
	}
}
