package project.game.engine.entitysystem.entity;

import project.game.context.api.Direction;
import project.game.engine.api.movement.IPositionable;

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
			this.direction = Direction.NONE;
		}
		this.direction = direction;
	}
}
