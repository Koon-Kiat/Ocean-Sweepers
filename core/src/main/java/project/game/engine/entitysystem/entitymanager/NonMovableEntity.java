package project.game.engine.entitysystem.entitymanager;

/**
 * NonMovableEntity is an abstract class that extends Entity and provides the
 * necessary methods and fields for entities that are non-movable.
 */
public abstract class NonMovableEntity extends Entity {

	public NonMovableEntity(float x, float y, float width, float height, boolean active) {
		super(x, y, width, height, active);
	}

	public abstract void update();
}
