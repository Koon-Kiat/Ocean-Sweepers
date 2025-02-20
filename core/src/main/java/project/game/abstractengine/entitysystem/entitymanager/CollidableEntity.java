package project.game.abstractengine.entitysystem.entitymanager;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.interfaces.ICollidable;

public abstract class CollidableEntity extends Entity implements ICollidable {

	private final Entity entity;
	private Body body;

	public CollidableEntity(Entity baseEntity, World world) {
		this.entity = baseEntity;
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
		fixtureDef.restitution = 0.5f;

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
}