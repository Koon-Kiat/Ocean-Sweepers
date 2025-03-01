package project.game.context.entity;

import java.util.logging.Level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.common.logging.ILogger;
import project.game.common.logging.LogManager;
import project.game.context.core.GameConstants;
import project.game.engine.api.collision.ICollidable;
import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.CustomAssetManager;
import project.game.engine.entitysystem.entity.CollidableEntity;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.movement.PlayerMovementManager;

public class BucketEntity extends CollidableEntity implements IRenderable {

	private static final ILogger LOGGER = LogManager.getLogger(BucketEntity.class);
	private final PlayerMovementManager movementManager;
	private final String texturePath;
	private boolean collisionActive = false;
	private long collisionEndTime = 0;

	public BucketEntity(Entity entity, World world, PlayerMovementManager movementManager, String texturePath) {
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
		return movementManager.isActive();
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
	}

	@Override
	public boolean isInCollision() {
		if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
			collisionActive = false;
		}
		return collisionActive;
	}

	@Override
	public boolean checkCollision(Entity other) {
		if (other == null) {
			return true;
		} else if (other instanceof ICollidable) {
			Body otherBody = ((ICollidable) other).getBody();
			return super.getBody().getPosition().x < otherBody.getPosition().x + other.getWidth() &&
					super.getBody().getPosition().x + entityWidth() > otherBody.getPosition().x &&
					super.getBody().getPosition().y < otherBody.getPosition().y + other.getHeight() &&
					super.getBody().getPosition().y + entityHeight() > otherBody.getPosition().y;
		}
		return false;
	}

	@Override
	public void onCollision(ICollidable other) {
		LOGGER.log(Level.INFO, "{0} collided with {1}",
				new Object[] { getEntity().getClass().getSimpleName(),
						other == null ? "boundary" : other.getClass().getSimpleName() });
	}

	@Override
	public final Body createBody(World world, float x, float y, float width, float height) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		float centerX = (x + width / 2) / GameConstants.PIXELS_TO_METERS;
		float centerY = (y + height / 2) / GameConstants.PIXELS_TO_METERS;
		bodyDef.position.set(centerX, centerY);
		bodyDef.fixedRotation = true;
		bodyDef.linearDamping = 0.1f;

		Body newBody = world.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox((width / 2) / GameConstants.PIXELS_TO_METERS, (height / 2) / GameConstants.PIXELS_TO_METERS);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1000.0f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.1f;
		newBody.createFixture(fixtureDef);
		shape.dispose();
		newBody.setUserData(this);
		return newBody;
	}

	public PlayerMovementManager getMovementManager() {
		return this.movementManager;
	}
}