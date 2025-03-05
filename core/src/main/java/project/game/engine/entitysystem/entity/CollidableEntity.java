package project.game.engine.entitysystem.entity;

import java.util.List;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.engine.api.collision.ICollidable;
import project.game.engine.api.collision.ICollisionHandler;

/**
 * Abstract class for entities that can collide with other entities.
 */
public abstract class CollidableEntity extends Entity implements ICollidable, ICollisionHandler {

	private final Entity entity;
	private Body body;
	private final World world;
	private boolean inCollision;

	public CollidableEntity(Entity baseEntity, World world) {
		this.entity = baseEntity;
		this.inCollision = false;
		this.world = world;
	}

	public final void initBody(World world) {
		if (body == null) {
			body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
		}
	}

	@Override
	public Entity getEntity() {
		return entity;
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public Body createBody(World world, float x, float y, float width, float height) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x, y);

		Body createdBody = world.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(entity.getWidth() / 2, entity.getHeight() / 2);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.3f;
		fixtureDef.restitution = 0.0f;

		createdBody.createFixture(fixtureDef);
		shape.dispose();

		return createdBody;
	}

	public Body createBody(World world, float x, float y, float width, float height, float density,
			float friction,
			float restitution) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x, y);

		Body createdBody = world.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(entity.getWidth() / 2, entity.getHeight() / 2);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = density;
		fixtureDef.friction = friction;
		fixtureDef.restitution = restitution;

		createdBody.createFixture(fixtureDef);
		shape.dispose();

		createdBody.setUserData(this);

		return createdBody;
	}

	public void updatePosition() {
		entity.setX(body.getPosition().x);
		entity.setY(body.getPosition().y);
	}

	@Override
	public boolean checkCollision(Entity other) {
		return entity.getX() < other.getX() + other.getWidth() &&
				entity.getX() + entity.getWidth() > other.getX() &&
				entity.getY() < other.getY() + other.getHeight() &&
				entity.getY() + entity.getHeight() > other.getY();
	}

	@Override
	public abstract void onCollision(ICollidable other);

	@Override
	public void collideWith(Object other) {
		// Default implementation - delegates to onCollision if other is an ICollidable
		if (other instanceof ICollidable) {
			onCollision((ICollidable) other);
		}
	}

	@Override
	public void collideWithBoundary() {
		// Default implementation - calls onCollision with null to indicate boundary
		// collision
		onCollision(null);
	}

	@Override
	public boolean isInCollision() {
		return inCollision;
	}

	/**
	 * Sets the collision state of this entity
	 * 
	 * @param inCollision Whether this entity is in a collision
	 */
	public void setInCollision(boolean inCollision) {
		this.inCollision = inCollision;
	}

	/**
	 * Implementation of the CollisionHandler interface for polymorphic dispatch
	 */
	@Override
	public void handleCollisionWith(Object other, List<Runnable> collisionQueue) {
		// Handle boundary case
		if ("boundary".equals(other)) {
			collisionQueue.add(this::collideWithBoundary);
			return;
		}

		// Handle other collidables
		if (other instanceof ICollidable) {
			ICollidable otherCollidable = (ICollidable) other;

			// Check if collision should be processed
			if (checkCollision(otherCollidable.getEntity())) {
				final ICollidable otherFinal = otherCollidable;
				collisionQueue.add(() -> collideWith(otherFinal));
			}
		}
	}

	@Override
	public boolean handlesCollisionWith(Class<?> clazz) {
		// This entity can handle collisions with ICollidables and boundaries
		return ICollidable.class.isAssignableFrom(clazz) ||
				String.class.equals(clazz); // For boundary collisions
	}
}