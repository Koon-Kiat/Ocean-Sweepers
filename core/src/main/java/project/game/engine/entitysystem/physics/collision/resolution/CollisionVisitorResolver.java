package project.game.engine.entitysystem.physics.collision.resolution;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.api.ICollisionElement;
import project.game.engine.entitysystem.physics.boundary.BoundaryCollisionElement;
import project.game.engine.entitysystem.physics.collision.detection.CollisionContact;

/**
 * A resolver class that handles collisions using the visitor pattern.
 */
public class CollisionVisitorResolver {

    private static final GameLogger LOGGER = new GameLogger(CollisionVisitorResolver.class);

    // Registry of objects that can be collided with (target registry)
    private final Map<Object, ICollisionElement> collisionTargets = new ConcurrentHashMap<>();

    // Registry of collision visitors (maps objects to their visitor interfaces)
    private final Map<Object, Function<Object, Runnable>> collisionVisitors = new ConcurrentHashMap<>();

    /**
     * Register a boundary with the collision resolver
     */
    public void registerBoundary() {
        collisionTargets.put("boundary", BoundaryCollisionElement.getInstance());
    }

    /**
     * Register a collidable entity with the collision resolver
     * 
     * @param collidable The entity to register
     */
    public void registerCollidable(ICollidableVisitor collidable) {
        // Register as a target
        collisionTargets.put(collidable, new CollisionContact(collidable));

        // Register as a visitor
        collisionVisitors.put(collidable, target -> {
            // For boundary collisions
            if ("boundary".equals(target)) {
                return () -> collidable.collideWithBoundary();
            }

            // For entity-entity collisions
            // Get the collision target if registered
            ICollisionElement collisionTarget = collisionTargets.get(target);
            if (collisionTarget != null) {
                return () -> collisionTarget.acceptCollision(collidable, null);
            }
            return null;
        });
    }

    /**
     * Remove an object from the collision registry
     * 
     * @param object The object to unregister
     */
    public void unregister(Object object) {
        collisionTargets.remove(object);
        collisionVisitors.remove(object);
    }

    /**
     * Resolve collision between two objects
     * 
     * @param objectA        First object in the collision
     * @param objectB        Second object in the collision
     * @param collisionQueue Queue to add collision actions to
     */
    public void resolveCollision(Object objectA, Object objectB, List<Runnable> collisionQueue) {
        // First direction: objectA visiting objectB
        resolveCollisionOneWay(objectA, objectB, collisionQueue);

        // Second direction: objectB visiting objectA
        resolveCollisionOneWay(objectB, objectA, collisionQueue);
    }

    public void debugPrintRegistrations() {
        LOGGER.info("Registered targets: " + collisionTargets.size());
        LOGGER.info("Registered visitors: " + collisionVisitors.size());
    }

    /**
     * Resolve collision in one direction using pure polymorphism
     * 
     * @param visitor        The object visiting the target
     * @param target         The target being visited
     * @param collisionQueue Queue to add collision actions to
     */
    private void resolveCollisionOneWay(Object visitor, Object target, List<Runnable> collisionQueue) {
        // Get the visitor handler for this object (if it exists)
        Function<Object, Runnable> visitorFunction = collisionVisitors.get(visitor);

        if (visitorFunction != null) {
            // The visitor can visit targets - create collision handling action
            Runnable collisionAction = visitorFunction.apply(target);

            // If a valid collision action was created, add it to the queue
            if (collisionAction != null) {
                collisionQueue.add(collisionAction);
            }
        }
    }
}