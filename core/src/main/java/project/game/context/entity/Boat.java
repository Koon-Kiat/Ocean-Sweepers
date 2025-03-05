package project.game.context.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.common.logging.core.GameLogger;
import project.game.context.factory.GameConstantsFactory;
import project.game.engine.api.collision.ICollidable;
import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.CustomAssetManager;
import project.game.engine.entitysystem.entity.CollidableEntity;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.movement.PlayerMovementManager;

public class Boat extends CollidableEntity implements IRenderable {

	private static final GameLogger LOGGER = new GameLogger(Boat.class);
	private final PlayerMovementManager movementManager;
	private final String texturePath;
	private boolean collisionActive = false;
	private long collisionEndTime = 0;

	public Boat(Entity entity, World world, PlayerMovementManager movementManager, String texturePath) {
		super(entity, world);
		this.movementManager = movementManager;
		this.texturePath = texturePath;
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

	@Override
	public boolean isActive() {
		return super.getEntity().isActive();
	}

	@Override
	public Body getBody() {
		return super.getBody();
	}

	@Override
	public String getTexturePath() {
		return texturePath;
	}

	@Override
	public Entity getEntity() {
		return super.getEntity();
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

	/**
	 * Set the collision to be active for a certain duration.
	 */
	public void setCollisionActive(long durationMillis) {
		collisionActive = true;
		collisionEndTime = System.currentTimeMillis() + durationMillis;

		// When collision becomes active, sync positions to prevent desynchronization
		float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
		float physX = getBody().getPosition().x * pixelsToMeters;
		float physY = getBody().getPosition().y * pixelsToMeters;

		// Update entity position and movement manager
		getEntity().setX(physX);
		getEntity().setY(physY);
		if (movementManager != null) {
			movementManager.setX(physX);
			movementManager.setY(physY);
		}
	}

	@Override
	public boolean isInCollision() {
		if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
			collisionActive = false;
			// Important: Clear any lingering velocity when exiting collision state
			getBody().setLinearVelocity(0, 0);
		}
		return collisionActive;
	}

	@Override
	public boolean checkCollision(Entity other) {
		// Use Box2D for collision detection
		return true;
	}

	@Override
	public void onCollision(ICollidable other) {
		// Only handle collisions with actual entities, not boundaries
		if (other != null) {
			// Log normal entity collisions
			LOGGER.info("{0} collided with {1}",
					new Object[] { getEntity().getClass().getSimpleName(),
							other.getClass().getSimpleName() });

			

			if (other instanceof Rock) {
				setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
				float boatX = getBody().getPosition().x;
				float boatY = getBody().getPosition().y;
				float rockX = other.getBody().getPosition().x;
				float rockY = other.getBody().getPosition().y;

				float dx = boatX - rockX;
				float dy = boatY - rockY;
				float distance = (float) Math.sqrt(dx * dx + dy * dy);
				LOGGER.info("go distance: " +  distance);

				if (distance > 0.0001f) {
					dx /= distance;
					dy /= distance;
					float bounceForce = GameConstantsFactory.getConstants().BOAT_BOUNCE_FORCE();
					LOGGER.info("boat bounce force: " + bounceForce);
					LOGGER.info("dy dx: " + dx);
					getBody().applyLinearImpulse(dx * bounceForce, dy * bounceForce, boatX, boatY, true);
				}
			} else if (other instanceof Trash) {
				Trash trash = (Trash) other;
				trash.getEntity().setActive(false);
				getWorld().destroyBody(trash.getBody());
			}
		}
	}

	@Override
	public final Body createBody(World world, float x, float y, float width, float height) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
		float centerX = (x + width / 2) / pixelsToMeters;
		float centerY = (y + height / 2) / pixelsToMeters;
		bodyDef.position.set(centerX, centerY);
		bodyDef.fixedRotation = true;
		bodyDef.linearDamping = 0.5f;
		bodyDef.bullet = true;
		bodyDef.allowSleep = false;

		Body newBody = world.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(
				(width / 2) / pixelsToMeters,
				(height / 2) / pixelsToMeters);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 10.0f;
		fixtureDef.friction = 0.1f;
		fixtureDef.restitution = 0.0f;

		Filter filter = new Filter();
		filter.categoryBits = 0x0001;
		filter.maskBits = -1;
		fixtureDef.filter.categoryBits = filter.categoryBits;
		fixtureDef.filter.maskBits = filter.maskBits;

		newBody.createFixture(fixtureDef);
		shape.dispose();
		newBody.setUserData(this);
		return newBody;
	}

	public PlayerMovementManager getMovementManager() {
		return this.movementManager;
	}
}