package project.game.engine.entitysystem.entity;

import com.badlogic.gdx.math.Vector2;

import project.game.engine.entitysystem.movement.api.IMovable;

/**
 * MovableEntity is an abstract class that extends Entity and provides the
 * necessary methods and fields for entities that can move.
 */
public abstract class MovableEntity extends Entity implements IMovable {

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

	@Override
	public float getSpeed() {
		return this.speed;
	}

	@Override
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	@Override
	public Vector2 getVelocity() {
		return new Vector2(velocity);
	}

	@Override
	public void setVelocity(Vector2 velocity) {
		if (velocity == null) {
			this.velocity = new Vector2(0, 0);
			return;
		}
		this.velocity = velocity;
	}

	@Override
	public void setVelocity(float x, float y) {
		this.velocity.x = x;
		this.velocity.y = y;
	}

	@Override
	public void normalizeVelocity() {
		if (velocity.len() > 0) {
			velocity.nor().scl(speed);
		}
	}

	@Override
	public void clearVelocity() {
		velocity.set(0, 0);
	}
}
