package project.game.abstractengine.entitysystem.entitymanager;

import project.game.abstractengine.entitysystem.interfaces.IMovable;

public abstract class MovableEntity extends Entity implements IMovable {

	private final Entity entity;
	private float speed;

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

}
