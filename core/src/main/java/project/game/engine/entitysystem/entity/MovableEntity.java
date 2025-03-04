package project.game.engine.entitysystem.entity;

import com.badlogic.gdx.math.Vector2;

import project.game.engine.api.movement.IPositionable;

/**
 * MovableEntity is an abstract class that extends Entity and provides the
 * necessary methods and fields for entities that can move.
 */
public abstract class MovableEntity extends Entity implements IPositionable {

	private final Entity entity;
	private float speed;
	private Vector2 velocity;

	public MovableEntity(Entity entity, float speed) {
		super(entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight(), entity.isActive());
		this.entity = entity;
		this.speed = speed;
		this.velocity = new Vector2(0, 0);
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

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2 velocity) {
		if (velocity == null) {
			this.velocity = new Vector2(0, 0);
			return;
		}
		this.velocity = velocity;
	}

	public void setVelocity(float x, float y) {
		this.velocity.x = x;
		this.velocity.y = y;
	}

	public void normalizeVelocity() {
		if (velocity.len() > 0) {
			velocity.nor().scl(speed);
		}
	}

	public void clearVelocity() {
		velocity.set(0, 0);
	}
}
