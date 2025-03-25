package project.game.engine.entitysystem.entity.management;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.api.IRenderable;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;

/**
 * EntityManager class that manages entities and renderables
 */
public class EntityManager {

	private static final GameLogger LOGGER = new GameLogger(EntityManager.class);
	private final List<IRenderable> renderables;
	private final List<Entity> entityList;
	private final Set<String> entityIDs;

	// Type conversion registry
	private static final Map<Class<?>, Function<Object, Entity>> ENTITY_EXTRACTORS = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Function<Object, ICollidableVisitor>> COLLIDABLE_EXTRACTORS = new ConcurrentHashMap<>();

	// Registry for removal methods
    private static final Map<Class<?>, Consumer<Entity>> REMOVAL_METHODS = new ConcurrentHashMap<>();

	static {
		// Register default converters
		registerEntityExtractor(Entity.class, obj -> (Entity) obj);
		registerCollidableExtractor(ICollidableVisitor.class, obj -> (ICollidableVisitor) obj);
	}

	/**
	 * Register an entity extractor for a specific type
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

	// Register a removal method for a specific type
    public static <T> void registerRemovalMethod(Class<T> clazz, Consumer<Entity> removalMethod) {
        REMOVAL_METHODS.put(clazz, removalMethod);
    }

	/**
	 * Register a collidable extractor for a specific type
	 * 
	 * @param <T>       Type of object to extract from
	 * @param clazz     Class of object
	 * @param extractor Function to extract the ICollidableVisitor from the object
	 */
	public static <T> void registerCollidableExtractor(Class<T> clazz, Function<T, ICollidableVisitor> extractor) {
		@SuppressWarnings("unchecked")
		Function<Object, ICollidableVisitor> castedExtractor = obj -> extractor.apply((T) obj);
		COLLIDABLE_EXTRACTORS.put(clazz, castedExtractor);
	}

	public EntityManager() {
		this.renderables = new ArrayList<>();
		this.entityList = new ArrayList<>();
		this.entityIDs = new HashSet<>();

		// Register default removal methods
		registerRemovalMethod(IRenderable.class, entity -> removeRenderableEntity((IRenderable) entity));
		registerRemovalMethod(Entity.class, this::removeEntity);
	}

	public boolean addRenderableEntity(IRenderable renderable) {
		renderables.add(renderable);

		Entity entity = extractEntity(renderable);
		if (entity != null) {
			if (entityIDs.contains(entity.getID())) {
				LOGGER.warn("Duplicate ID: {0}", entity.getID());
				return false;
			}
			entityIDs.add(entity.getID());
			entityList.add(entity);
		}
		return true;
	}

	private void printRenderableList() {
		LOGGER.info("Current renderable entities:");
		for (IRenderable renderable : renderables) {
			Entity entity = extractEntity(renderable);
			if (entity != null) {
				LOGGER.info("Renderable entity ID: {0}", entity.getID());
			}
		}
	}

	private void printEntityList() {
		LOGGER.info("Current entities:");
		for (Entity entity : entityList) {
			if (entity != null) {
				LOGGER.info("Entity ID: {0}", entity.getID());
			}
		}
	}

	public void removeRenderableEntity(IRenderable renderable) {
		if (renderable == null) {
			LOGGER.error("Renderable is null");
			return;
		}
		renderables.remove(renderable);
		Entity entity = extractEntity(renderable);
		if (entity != null) {
			entityIDs.remove(entity.getID());
			entityList.remove(entity);
			LOGGER.info("Renderable entity removed: {0", entity.getID());
		}
		printRenderableList();
	}

	public boolean addEntity(Entity entity) {
		if (entityIDs.contains(entity.getID())) {
			LOGGER.warn("Duplicate ID: {0}", entity.getID());
			return false;
		}
		entityIDs.add(entity.getID());
		entityList.add(entity);
		return true;
	}

	public void removeEntity(Entity entity) {
		if (entity == null) {
			LOGGER.error("Entity is null");
			return;
		}
		LOGGER.info("Removing entity: {0}", entity.getID());
		entityList.remove(entity);
		entityIDs.remove(entity.getID());
		LOGGER.info("Entity removed: {0}", entity.getID());
		printEntityList();
	}

	public void draw(SpriteBatch batch) {
		// Iterate over renderables instead of entityList
		for (IRenderable renderable : renderables) {
			renderable.render(batch);
		}
	}

	public void checkCollision() {
		for (int i = 0; i < entityList.size(); i++) {
			Entity entityA = entityList.get(i);
			ICollidableVisitor collidableA = extractCollidable(entityA);

			if (collidableA != null) {
				for (int j = i + 1; j < entityList.size(); j++) {
					Entity entityB = entityList.get(j);
					ICollidableVisitor collidableB = extractCollidable(entityB);

					if (collidableB != null && collidableA.checkCollision(entityB)) {
						collidableA.onCollision(collidableB);
						collidableB.onCollision(collidableA);
					}
				}
			}
		}
	}

	/**
	 * Extract an Entity object from any object using registered extractors
	 * 
	 * @param object The object to extract from
	 * @return The extracted Entity or null if not extractable
	 */
	private Entity extractEntity(Object object) {
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

	/**
	 * Extract an ICollidableVisitor object from any object using registered
	 * extractors
	 * 
	 * @param object The object to extract from
	 * @return The extracted ICollidableVisitor or null if not extractable
	 */
	private ICollidableVisitor extractCollidable(Object object) {
		if (object == null) {
			return null;
		}

		for (Map.Entry<Class<?>, Function<Object, ICollidableVisitor>> entry : COLLIDABLE_EXTRACTORS.entrySet()) {
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

	// New method to remove an entity using the registry
    public void removeEntityUsingRegistry(Entity entity) {
        if (entity == null) {
            LOGGER.error("Entity is null");
            return;
        }

        // Use the registry to find and invoke the appropriate removal method
        for (Map.Entry<Class<?>, Consumer<Entity>> entry : REMOVAL_METHODS.entrySet()) {
            if (entry.getKey().isInstance(entity)) {
                entry.getValue().accept(entity);
                return;
            }
        }

        LOGGER.warn("No removal method registered for entity type: {0}", entity.getClass().getSimpleName());
    }
}
