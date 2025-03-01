package project.game.engine.api;

/**
 * An interface representing a target that can be collided with.
 * This is part of a visitor pattern implementation to eliminate instanceof
 * checks.
 */
public interface ICollisionTarget {
    /**
     * Accept method for the visitor pattern - called when something collides with
     * this target
     * 
     * @param visitor         The ICollidable that collided with this target
     * @param collisionAction A callback to run when the collision is processed
     */
    void acceptCollision(ICollidable visitor, Runnable collisionAction);
}