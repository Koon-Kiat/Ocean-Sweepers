package project.game.engine.api.collision;

/**
 * An interface representing a target that can be collided with.
 */
public interface ICollisionElement {
    
    /**
     * Accept method for the visitor pattern - called when something collides with
     * this target
     * 
     * @param visitor         The ICollidable that collided with this target
     * @param collisionAction A callback to run when the collision is processed
     */
    void acceptCollision(ICollidableVisitor visitor, Runnable collisionAction);
}