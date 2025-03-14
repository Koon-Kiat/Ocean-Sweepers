package project.game.engine.entitysystem.physics.collision.management;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.api.ICollisionOperation;

/**
 * Default implementation of ICollisionHandler for ICollidable entities
 * This class adapts the ICollidable interface to the ICollisionHandler
 * interface
 */
public class CollisionEntityManager implements ICollisionOperation {

    private static final Map<Class<?>, Function<Object, ICollidableVisitor>> CONVERTERS = new ConcurrentHashMap<>();
    private final ICollidableVisitor collidable;

    static {
        // Register default converter for ICollidableVisitor
        registerConverter(ICollidableVisitor.class, o -> (ICollidableVisitor) o);
    }

    public CollisionEntityManager(ICollidableVisitor collidable) {
        this.collidable = collidable;
    }

    /**
     * Register a converter for a specific type to enable polymorphic dispatch
     * 
     * @param <T>       Type of object to convert
     * @param clazz     Class of object to convert
     * @param converter Function to convert to ICollidableVisitor
     */
    public static <T> void registerConverter(Class<T> clazz, Function<T, ICollidableVisitor> converter) {
        @SuppressWarnings("unchecked")
        Function<Object, ICollidableVisitor> castedConverter = obj -> converter.apply((T) obj);
        CONVERTERS.put(clazz, castedConverter);
    }

    @Override
    public void handleCollisionWith(Object other, List<Runnable> collisionQueue) {
        if ("boundary".equals(other)) {
            collisionQueue.add(collidable::collideWithBoundary);
            return;
        }

        // Try to handle via double dispatch with another collidable
        tryHandleCollidable(other, collisionQueue);
    }

    @Override
    public boolean handlesCollisionWith(Class<?> clazz) {
        // Check if we have a converter for this class or any of its superclasses
        for (Class<?> registeredClass : CONVERTERS.keySet()) {
            if (registeredClass.isAssignableFrom(clazz)) {
                return true;
            }
        }

        // Also handle strings (for boundary collisions)
        return String.class.equals(clazz);
    }

    /**
     * Tries to handle a collision with another potentially collidable object
     * 
     * @param other          The other object involved in the collision
     * @param collisionQueue Queue to add collision actions to
     */
    private void tryHandleCollidable(Object other, List<Runnable> collisionQueue) {
        ICollidableVisitor otherCollidable = getCollidableVisitor(other);
        if (otherCollidable != null) {
            Entity otherEntity = otherCollidable.getEntity();
            if (collidable.checkCollision(otherEntity)) {
                final ICollidableVisitor otherFinal = otherCollidable;
                collisionQueue.add(() -> collidable.collideWith(otherFinal));
            }
        }
    }

    /**
     * Attempts to get an ICollidableVisitor from an object using registered
     * converters
     * 
     * @param object The object to convert
     * @return The ICollidableVisitor or null if not convertible
     */
    private ICollidableVisitor getCollidableVisitor(Object object) {
        if (object == null) {
            return null;
        }

        for (Map.Entry<Class<?>, Function<Object, ICollidableVisitor>> entry : CONVERTERS.entrySet()) {
            if (entry.getKey().isInstance(object)) {
                try {
                    return entry.getValue().apply(object);
                } catch (Exception e) {
                    // Failed to convert, try next converter
                }
            }
        }

        return null;
    }
}