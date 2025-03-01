package project.game.engine.entitysystem.collision;

import project.game.engine.api.collision.ICollidable;
import project.game.engine.api.collision.ICollisionTarget;

/**
 * Represents a boundary in the game world that can be collided with.
 * This implementation uses the visitor pattern to handle collisions with
 * boundaries
 * without instanceof checks.
 */
public class BoundaryCollisionTarget implements ICollisionTarget {

    private static final BoundaryCollisionTarget INSTANCE = new BoundaryCollisionTarget();

    /**
     * Get the singleton instance of the boundary collision target
     * 
     * @return The boundary collision target instance
     */
    public static BoundaryCollisionTarget getInstance() {
        return INSTANCE;
    }

    // Private constructor to enforce singleton pattern
    private BoundaryCollisionTarget() {
    }

    @Override
    public void acceptCollision(ICollidable visitor, Runnable collisionAction) {
        // When a collidable visits a boundary, tell it to handle a boundary collision
        visitor.collideWithBoundary();
        if (collisionAction != null) {
            collisionAction.run();
        }
    }
}
