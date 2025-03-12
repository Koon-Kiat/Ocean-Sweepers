package project.game.engine.entitysystem.physics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.api.ICollisionPairHandler;

/**
 * A class that tracks active collision pairs using the visitor pattern.
 * This eliminates the need for type checks when tracking collisions.
 */
public class CollisionPairTracker implements ICollisionPairHandler {

    // Registry of objects that can be tracked in collisions
    private final Map<Class<?>, Function<Object, ICollidableVisitor>> converters = new ConcurrentHashMap<>();

    // Set of active collision pairs
    private final Set<CollisionPair> activeCollisions = new HashSet<>();

    public CollisionPairTracker() {
        // Register the ICollidable converter by default
        registerConverter(ICollidableVisitor.class, obj -> (ICollidableVisitor) obj);
    }

    /**
     * Register a converter for a specific class type.
     * This allows us to convert objects to ICollidable if they implement or wrap
     * ICollidable.
     * 
     * @param <T>       The type of class being registered
     * @param clazz     The class to register the converter for
     * @param converter Function to convert from the class to ICollidable
     */
    public final <T> void registerConverter(Class<T> clazz, Function<T, ICollidableVisitor> converter) {
        @SuppressWarnings("unchecked")
        Function<Object, ICollidableVisitor> castedConverter = obj -> converter.apply((T) obj);
        converters.put(clazz, castedConverter);
    }

    @Override
    public boolean addCollisionPair(Object objectA, Object objectB) {
        ICollidableVisitor collidableA = toCollidable(objectA);
        ICollidableVisitor collidableB = toCollidable(objectB);

        if (collidableA != null && collidableB != null) {
            return activeCollisions.add(new CollisionPair(collidableA, collidableB));
        }
        return false;
    }

    @Override
    public boolean removeCollisionPair(Object objectA, Object objectB) {
        ICollidableVisitor collidableA = toCollidable(objectA);
        ICollidableVisitor collidableB = toCollidable(objectB);

        if (collidableA != null && collidableB != null) {
            return activeCollisions.remove(new CollisionPair(collidableA, collidableB));
        }
        return false;
    }

    @Override
    public boolean isEntityInCollision(ICollidableVisitor entity) {
        for (CollisionPair pair : activeCollisions) {
            if (pair.involves(entity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there are any active collisions
     * 
     * @return true if there are no active collisions
     */
    @Override
    public boolean isEmpty() {
        return activeCollisions.isEmpty();
    }

    /**
     * Helper class to track pairs of colliding objects
     */
    private static class CollisionPair {
        private final ICollidableVisitor entityA;
        private final ICollidableVisitor entityB;

        public CollisionPair(ICollidableVisitor a, ICollidableVisitor b) {
            // Store in consistent order to ensure hashCode/equals work correctly
            // regardless of the order entities are passed in
            if (System.identityHashCode(a) <= System.identityHashCode(b)) {
                this.entityA = a;
                this.entityB = b;
            } else {
                this.entityA = b;
                this.entityB = a;
            }
        }

        public boolean involves(ICollidableVisitor entity) {
            return entityA == entity || entityB == entity;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != CollisionPair.class) {
                return false;
            }
            CollisionPair other = (CollisionPair) obj;
            return (entityA == other.entityA && entityB == other.entityB);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(entityA) * 31 + System.identityHashCode(entityB);
        }
    }

    /**
     * Try to convert an object to ICollidable using registered converters
     * 
     * @param object The object to convert
     * @return The converted ICollidable or null if no converter is available
     */
    private ICollidableVisitor toCollidable(Object object) {
        if (object == null) {
            return null;
        }

        // Try to convert the object to ICollidable using registered converters
        for (Map.Entry<Class<?>, Function<Object, ICollidableVisitor>> entry : converters.entrySet()) {
            Class<?> clazz = entry.getKey();
            if (clazz.isInstance(object)) {
                return entry.getValue().apply(object);
            }
        }

        return null;
    }
}