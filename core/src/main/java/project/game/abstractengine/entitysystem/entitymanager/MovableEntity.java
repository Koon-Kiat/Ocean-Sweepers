package project.game.abstractengine.entitysystem.entitymanager;

import project.game.Direction;

public abstract class MovableEntity extends Entity {

	private final Entity entity;
	private float speed;
	private Direction direction;

	public MovableEntity(Entity entity, float speed) {
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

}
