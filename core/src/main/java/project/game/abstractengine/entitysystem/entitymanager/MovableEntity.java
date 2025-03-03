package project.game.abstractengine.entitysystem.entitymanager;

import project.game.Direction;
import project.game.abstractengine.interfaces.IPositionable;

/**
 * MovableEntity is an abstract class that extends Entity and provides the
 * necessary methods and fields for entities that can move.
 */
public abstract class MovableEntity extends Entity implements IPositionable {

	private final Entity entity;
	private float speed;
	private Direction direction;

	public MovableEntity(Entity entity, float speed) {
		super(entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight(), entity.isActive());
		this.entity = entity;
		this.speed = speed;
	}

	public Entity getEntity() {
		return entity;
	}

	public float getSpeed() {
		return this.speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		if (direction == null) {
			// LOGGER.log(Level.WARNING, "Null direction provided. Defaulting to
			// Direction.NONE.");
			this.direction = Direction.NONE;
		}
		this.direction = direction;
	}

	// These methods are inherited from Entity and already satisfy the IPositionable
	// interface
}
