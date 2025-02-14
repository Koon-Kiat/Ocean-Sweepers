package project.game.abstractengine.entitymanager;

import project.game.abstractengine.entitymanager.interfaces.Collidable;

public abstract class CollidableEntity extends Entity implements Collidable {
	
	private final Entity entity;
	
	public CollidableEntity(Entity entity) {
		this.entity = entity;
	}
	
	public boolean checkCollision(Entity other) {
		return entity.getX() < other.getX() + other.getWidth() &&
				entity.getX() + entity.getWidth() > other.getX() &&
				entity.getY() < other.getY() + other.getHeight() &&
				entity.getY() + entity.getHeight() > other.getY();
	}
	
	public abstract void onCollision(Entity other);
}
