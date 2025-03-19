package project.game.engine.entitysystem.physics.api;

/**
 * Interface for handling collision pair tracking without explicit type
 * checking.
 * This interface allows us to track active collisions between objects
 * in a polymorphic way.
 */
public interface ICollisionPairHandler {

    /**
     * Add a collision pair to active collisions if both objects are compatible.
     * 
     * @param objectA The first object in the collision
     * @param objectB The second object in the collision
     * @return true if a collision pair was added, false otherwise
     */
    boolean addCollisionPair(Object objectA, Object objectB);

    /**
     * Remove a collision pair from active collisions if both objects are
     * compatible.
     * 
     * @param objectA The first object in the collision
     * @param objectB The second object in the collision
     * @return true if a collision pair was removed, false otherwise
     */
    boolean removeCollisionPair(Object objectA, Object objectB);

    /**
     * Check if an entity is involved in any active collisions.
     * 
     * @param entity The entity to check
     * @return true if the entity is in an active collision, false otherwise
     */
    boolean isEntityInCollision(ICollidableVisitor entity);

    /**
     * Check if there are any active collisions.
     * 
     * @return true if there are no active collisions
     */
    boolean isEmpty();
}