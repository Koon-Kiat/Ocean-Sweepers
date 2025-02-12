package project.game.abstractengine.entitymanager;

public abstract class NonMovableEntity extends Entity{
	
	public NonMovableEntity(float x, float y, float width, float height, boolean active) {
		super(x,y,width, height, active);
	}
	
	public abstract void update();
}
