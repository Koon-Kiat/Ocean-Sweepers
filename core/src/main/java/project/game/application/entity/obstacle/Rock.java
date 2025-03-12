package project.game.application.entity.obstacle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import project.game.Main;
import project.game.application.entity.npc.Monster;
import project.game.application.entity.player.Boat;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.core.CustomAssetManager;
import project.game.engine.entitysystem.entity.CollidableEntity;
import project.game.engine.entitysystem.entity.Entity;

public class Rock extends CollidableEntity implements IRenderable {

	private static final GameLogger LOGGER = new GameLogger(Main.class);
	private String texturePath;
	private TextureRegion rockRegion;
	private boolean collisionActive = false;
	private long collisionEndTime = 0;

	// Type-based collision handler registry
	private static final Map<Class<?>, BiConsumer<Rock, ICollidableVisitor>> ROCK_COLLISION_HANDLERS = new ConcurrentHashMap<>();

	static {
		// Register collision handlers for different entity types
		registerRockCollisionHandler(Boat.class, Rock::handleBoatCollision);
		registerRockCollisionHandler(Monster.class, Rock::handleMonsterCollision);
	}

	/**
	 * Register a handler for a specific type of collidable entity
	 * 
	 * @param <T>     Type of collidable
	 * @param clazz   Class of collidable
	 * @param handler Function to handle collision with the collidable
	 */
	public static <T extends ICollidableVisitor> void registerRockCollisionHandler(
			Class<T> clazz, BiConsumer<Rock, ICollidableVisitor> handler) {
		ROCK_COLLISION_HANDLERS.put(clazz, handler);
	}

	public Rock(Entity entity, World world, String texturePath) {
		super(entity, world);
		this.texturePath = texturePath;
	}

	// New constructor for a rock with a given region
	public Rock(Entity entity, World world, TextureRegion rockRegion) {
		super(entity, world);
		this.rockRegion = rockRegion;
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
			float renderX = getEntity().getX() - getEntity().getWidth() / 2;
			float renderY = getEntity().getY() - getEntity().getHeight() / 2;
			batch.draw(rockRegion, renderX, renderY, getEntity().getWidth(), getEntity().getHeight());
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
			// Dispatch to appropriate collision handler based on entity type
			dispatchCollisionHandling(other);
		}
	}

	/**
	 * Dispatches collision handling to the appropriate registered handler
	 * 
	 * @param other The other entity involved in the collision
	 */
	private void dispatchCollisionHandling(ICollidableVisitor other) {
		// Get other entity's class and find a matching handler
		Class<?> otherClass = other.getClass();

		// Look for a handler for this specific class or its superclasses
		for (Map.Entry<Class<?>, BiConsumer<Rock, ICollidableVisitor>> entry : ROCK_COLLISION_HANDLERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(otherClass)) {
				entry.getValue().accept(this, other);
				return;
			}
		}

		// Default handler for any entity (apply general repulsion)
		handleDefaultCollision(other);
	}

	/**
	 * Handle collision with a boat
	 */
	private void handleBoatCollision(ICollidableVisitor other) {
		// Apply strong repulsion force to boat
		applyRepulsionForce(other, 1.5f);
	}

	/**
	 * Handle collision with a monster
	 */
	private void handleMonsterCollision(ICollidableVisitor other) {
		// Apply very strong repulsion force to monster
		applyRepulsionForce(other, 15.0f);
	}

	/**
	 * Handle collision with any other entity
	 */
	private void handleDefaultCollision(ICollidableVisitor other) {
		// Apply standard repulsion force
		applyRepulsionForce(other, 1.0f);
	}

	/**
	 * Apply a repulsion force to the other entity
	 */
	private void applyRepulsionForce(ICollidableVisitor other, float multiplier) {
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
			float repulsionForce = GameConstantsFactory.getConstants().ROCK_BASE_IMPULSE() * multiplier;

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

	@Override
	public boolean isInCollision() {
		if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
			collisionActive = false;
		}
		return collisionActive;
	}
}