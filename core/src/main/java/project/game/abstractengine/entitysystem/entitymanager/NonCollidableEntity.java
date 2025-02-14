package project.game.abstractengine.entitysystem.entitymanager;

public abstract class NonCollidableEntity extends Entity {
	
	public NonCollidableEntity(float x, float y, float width, float height, boolean active) {
		super(x,y,width, height, active);
	}
	
	public abstract void update();
}
