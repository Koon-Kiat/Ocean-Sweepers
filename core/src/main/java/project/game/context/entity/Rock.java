package project.game.context.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import project.game.Main;
import project.game.common.logging.core.GameLogger;
import project.game.context.factory.GameConstantsFactory;
import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.CustomAssetManager;
import project.game.engine.entitysystem.entity.CollidableEntity;
import project.game.engine.entitysystem.entity.Entity;

public class Rock extends CollidableEntity implements IRenderable {

	private static final GameLogger LOGGER = new GameLogger(Main.class);
	private final String texturePath;
	private boolean collisionActive = false;
	private long collisionEndTime = 0;

	public Rock(Entity entity, World world, String texturePath) {
		super(entity, world);
		this.texturePath = texturePath;
	}

	public void setCollisionActive(long durationMillis) {
		collisionActive = true;
		collisionEndTime = System.currentTimeMillis() + durationMillis;
	}

	@Override
	public boolean isActive() {
		return super.getEntity().isActive();
	}

	@Override
	public Entity getEntity() {
		return super.getEntity();
	}

	@Override
	public Body getBody() {
		return super.getBody();
	}

	@Override
	public final Body createBody(World world, float x, float y, float width, float height) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		float centerX = (x + width / 2) / GameConstantsFactory.getConstants().PIXELS_TO_METERS();
		float centerY = (y + height / 2) / GameConstantsFactory.getConstants().PIXELS_TO_METERS();
		bodyDef.position.set(centerX, centerY);
		bodyDef.fixedRotation = true;
		bodyDef.allowSleep = false;

		Body newBody = world.createBody(bodyDef);

		CircleShape shape = new CircleShape();
		float radius = Math.min(width, height) / 1.8f / GameConstantsFactory.getConstants().PIXELS_TO_METERS();
		shape.setRadius(radius);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.0f;

		// Set up collision filtering
		Filter filter = new Filter();
		filter.categoryBits = 0x0002;
		filter.maskBits = -1;
		fixtureDef.filter.categoryBits = filter.categoryBits;
		fixtureDef.filter.maskBits = filter.maskBits;

		newBody.createFixture(fixtureDef);
		shape.dispose();
		newBody.setUserData(this);
		return newBody;
	}

	@Override
	public String getTexturePath() {
		return texturePath;
	}

	@Override
	public void render(SpriteBatch batch) {
		if (isActive() && CustomAssetManager.getInstance().isLoaded()) {

			// Render the entity using offset for BOX2D body
			float renderX = entityX() - entityWidth() / 2;
			float renderY = entityY() - entityHeight() / 2;
			Texture texture = CustomAssetManager.getInstance().getAsset(texturePath, Texture.class);
			batch.draw(texture, renderX, renderY, entityWidth(), entityHeight());
		}
	}

	@Override
	public boolean checkCollision(Entity other) {
		// Always return true to ensure collision is checked using Box2D
		return true;
	}

	@Override
	public void onCollision(ICollidableVisitor other) {
		LOGGER.info("{0} collided with {1}",
				new Object[] { getEntity().getClass().getSimpleName(),
						other == null ? "boundary" : other.getClass().getSimpleName() });

		if (other != null) {
			// Apply strong repulsion force to any colliding entity
			float rockX = getEntity().getX();
			float rockY = getEntity().getY();
			float otherX = other.getEntity().getX();
			float otherY = other.getEntity().getY();

			// Calculate direction vector from rock to other entity
			float dx = otherX - rockX;
			float dy = otherY - rockY;
			float distance = (float) Math.sqrt(dx * dx + dy * dy);

			if (distance > 0.0001f) {
				// Normalize direction
				dx /= distance;
				dy /= distance;

				// Calculate repulsion force based on entity type
				float repulsionForce = GameConstantsFactory.getConstants().ROCK_BASE_IMPULSE();
				if (other instanceof Boat) {
					repulsionForce *= 1.5f;
				} else if (other instanceof Monster) {
					repulsionForce *= 15.0f;
				}

				// Apply impulse to push the other entity away
				other.getBody().applyLinearImpulse(
						dx * repulsionForce,
						dy * repulsionForce,
						other.getBody().getWorldCenter().x,
						other.getBody().getWorldCenter().y,
						true);
			}

			// Set collision states
			setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
			if (other.getBody() != null) {
				other.getBody().setLinearDamping(3.0f);
			}
		}
	}

	@Override
	public boolean isInCollision() {
		if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
			collisionActive = false;
		}
		return collisionActive;
	}

	private float entityX() {
		return super.getEntity().getX();
	}

	private float entityY() {
		return super.getEntity().getY();
	}

	private float entityWidth() {
		return super.getEntity().getWidth();
	}

	private float entityHeight() {
		return super.getEntity().getHeight();
	}
}