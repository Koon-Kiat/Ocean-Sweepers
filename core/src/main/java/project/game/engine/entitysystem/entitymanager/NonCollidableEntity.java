package project.game.engine.entitysystem.entitymanager;

/**
 * NonCollidableEntity is an abstract class that extends Entity and provides the
 * necessary methods and fields for entities that are non-collidable.
 */
public abstract class NonCollidableEntity extends Entity {

	public NonCollidableEntity(float x, float y, float width, float height, boolean active) {
		super(x, y, width, height, active);
	}

	public abstract void update();
}
