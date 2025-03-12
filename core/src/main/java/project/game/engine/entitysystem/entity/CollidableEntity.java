package project.game.engine.entitysystem.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.collision.ICollisionOperation;

/**
 * Abstract class for entities that can collide with other entities.
 */
public abstract class CollidableEntity extends Entity implements ICollidableVisitor, ICollisionOperation {

	// Collision visitor dispatcher map
	private static final Map<Class<?>, BiConsumer<CollidableEntity, Object>> COLLISION_VISITORS = new ConcurrentHashMap<>();

	// Entity extractors for different object types
	private static final Map<Class<?>, Function<Object, Entity>> ENTITY_EXTRACTORS = new ConcurrentHashMap<>();
	
	private final Entity entity;
	private final World world;
	private boolean inCollision;
	private Body body;

	static {
		// Register handler for ICollidableVisitor
		registerCollisionHandler(ICollidableVisitor.class,
				(entity, other) -> entity.onCollision((ICollidableVisitor) other));

		// Register entity extractor for ICollidableVisitor
		registerEntityExtractor(ICollidableVisitor.class,
				other -> ((ICollidableVisitor) other).getEntity());
	}

	/**
	 * Register a handler for a specific type of object to enable polymorphic
	 * dispatch
	 * 
	 * @param <T>     Type of object to handle
	 * @param clazz   Class of object to handle
	 * @param handler Function to handle collision with the object
	 */
	public static <T> void registerCollisionHandler(Class<T> clazz, BiConsumer<CollidableEntity, T> handler) {
		@SuppressWarnings("unchecked")
		BiConsumer<CollidableEntity, Object> typedHandler = (entity, obj) -> handler.accept(entity, (T) obj);
		COLLISION_VISITORS.put(clazz, typedHandler);
	}

	/**
	 * Register an entity extractor for a specific type to enable polymorphic entity
	 * access
	 * 
	 * @param <T>       Type of object to extract from
	 * @param clazz     Class of object
	 * @param extractor Function to extract the Entity from the object
	 */
	public static <T> void registerEntityExtractor(Class<T> clazz, Function<T, Entity> extractor) {
		@SuppressWarnings("unchecked")
		Function<Object, Entity> castedExtractor = obj -> extractor.apply((T) obj);
		ENTITY_EXTRACTORS.put(clazz, castedExtractor);
	}

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

	public void updatePosition() {
		entity.setX(body.getPosition().x);
		entity.setY(body.getPosition().y);
	}

	public void setInCollision(boolean inCollision) {
		this.inCollision = inCollision;
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

	@Override
	public boolean checkCollision(Entity other) {
		return entity.getX() < other.getX() + other.getWidth() &&
				entity.getX() + entity.getWidth() > other.getX() &&
				entity.getY() < other.getY() + other.getHeight() &&
				entity.getY() + entity.getHeight() > other.getY();
	}

	@Override
	public boolean isInCollision() {
		return inCollision;
	}

	@Override
	public void collideWith(Object other) {
		dispatchCollision(other);
	}

	/**
	 * Dispatch collision to appropriate handler based on object type
	 * 
	 * @param other The object to collide with
	 */
	private void dispatchCollision(Object other) {
		if (other == null) {
			onCollision(null);
			return;
		}

		Class<?> otherClass = other.getClass();
		// Find the most specific handler for this class
		BiConsumer<CollidableEntity, Object> handler = null;

		// Look for exact class match or interface match
		for (Map.Entry<Class<?>, BiConsumer<CollidableEntity, Object>> entry : COLLISION_VISITORS.entrySet()) {
			if (entry.getKey().isAssignableFrom(otherClass)) {
				if (handler == null || handler.getClass().isAssignableFrom(entry.getKey().getClass())) {
					handler = entry.getValue();
				}
			}
		}

		// If we found a handler, use it
		if (handler != null) {
			handler.accept(this, other);
		}
	}

	@Override
	public void collideWithBoundary() {
		onCollision(null);
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

		// Get the appropriate visitor for the other object
		for (Class<?> clazz : COLLISION_VISITORS.keySet()) {
			if (clazz.isInstance(other)) {
				// Type is supported
				Entity otherEntity = extractEntityFrom(other);
				if (otherEntity != null && checkCollision(otherEntity)) {
					final Object otherFinal = other;
					collisionQueue.add(() -> collideWith(otherFinal));
				}
				return;
			}
		}
	}

	/**
	 * Extracts an Entity from an object using registered extractors
	 * 
	 * @param object The object to extract the Entity from
	 * @return The extracted Entity or null if not extractable
	 */
	private Entity extractEntityFrom(Object object) {
		if (object == null) {
			return null;
		}

		for (Map.Entry<Class<?>, Function<Object, Entity>> entry : ENTITY_EXTRACTORS.entrySet()) {
			if (entry.getKey().isInstance(object)) {
				try {
					return entry.getValue().apply(object);
				} catch (Exception e) {
					// Failed to extract, try next extractor
				}
			}
		}

		return null;
	}

	@Override
	public boolean handlesCollisionWith(Class<?> clazz) {
		// Check if we have a handler for this class or any of its
		// superclasses/interfaces
		for (Class<?> registeredClass : COLLISION_VISITORS.keySet()) {
			if (registeredClass.isAssignableFrom(clazz)) {
				return true;
			}
		}

		// Also handle strings (for boundary collisions)
		return String.class.equals(clazz);
	}

	@Override
	public abstract void onCollision(ICollidableVisitor other);
}