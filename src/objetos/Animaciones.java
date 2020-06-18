package objetos;

public class Animaciones {
	
	private final int ID;
	private final int AnimationId;
	private final String AnimationName;
	private final int AnimationArea;
	private final int AnimationAction;
	private final int AnimationSize;
	
	public Animaciones(int Id, int AnimId, String Name, int Area, int Action, int Size)
	{
		this.ID = Id;
		this.AnimationId = AnimId;
		this.AnimationName = Name;
		this.AnimationArea = Area;
		this.AnimationAction = Action;
		this.AnimationSize = Size;
	}
	
	public int getId() 
	{
		return ID;
	}
	
	public String getName() 
	{
		return AnimationName;
	}
	
	public int getArea() 
	{
		return AnimationArea;
	}
	
	public int getAction() 
	{
		return AnimationAction;
	}
	
	public int getSize() 
	{
		return AnimationSize;
	}
	
	public int getAnimationId() 
	{
		return AnimationId;
	}
	
	public static String PrepareToGA(Animaciones animation)
	{
		return animation.getAnimationId() + "," + animation.getArea() + "," + animation.getAction() + "," + animation.getSize();
	}
	
}