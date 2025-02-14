package project.game.abstractengine.entitymanager;

import project.game.abstractengine.entitymanager.interfaces.Collidable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public abstract class CollidableEntity extends Entity implements Collidable {
	
	private final Entity entity;
	private final Body body;
	
	public CollidableEntity(Entity entity, World world) {
		this.entity = entity;
		this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
	}
	
	private Body createBody(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.5f;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }
	
	public Body createBody(World world, float x, float y, float width, float height, float density, float friction, float restitution) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }
	
	public void updatePosition() {
		entity.setX(body.getPosition().x);
		entity.setY(body.getPosition().y);
    }
	
	public boolean checkCollision(Entity other) {
		return entity.getX() < other.getX() + other.getWidth() &&
				entity.getX() + entity.getWidth() > other.getX() &&
				entity.getY() < other.getY() + other.getHeight() &&
				entity.getY() + entity.getHeight() > other.getY();
	}
	
	public Entity getEntity() {
		return this.entity;
	}
	
	public Body getBody() {
		return this.body;
	}
	
	public abstract void onCollision(Entity other);
}
