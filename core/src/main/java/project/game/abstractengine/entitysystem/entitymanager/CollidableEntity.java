package project.game.abstractengine.entitysystem.entitymanager;

import project.game.abstractengine.entitysystem.interfaces.ICollidable;

public abstract class CollidableEntity extends Entity implements ICollidable {

	private final Entity entity;

	public CollidableEntity(Entity entity) {
		this.entity = entity;
	}

	@Override
	public boolean checkCollision(Entity other) {
		return entity.getX() < other.getX() + other.getWidth() &&
				entity.getX() + entity.getWidth() > other.getX() &&
				entity.getY() < other.getY() + other.getHeight() &&
				entity.getY() + entity.getHeight() > other.getY();
	}

	@Override
	public abstract void onCollision(Entity other);
}
