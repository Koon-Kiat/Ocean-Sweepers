package project.game.engine.entitysystem.collision;

import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.collision.ICollisionElement;

/**
 * Represents a boundary in the game world that can be collided with.
 * This implementation uses the visitor pattern to handle collisions with
 * boundaries
 * without instanceof checks.
 */
public class BoundaryCollisionElement implements ICollisionElement {

    private static final BoundaryCollisionElement INSTANCE = new BoundaryCollisionElement();

    /**
     * Get the singleton instance of the boundary collision target
     * 
     * @return The boundary collision target instance
     */
    public static BoundaryCollisionElement getInstance() {
        return INSTANCE;
    }

    // Private constructor to enforce singleton pattern
    private BoundaryCollisionElement() {
    }

    @Override
    public void acceptCollision(ICollidableVisitor visitor, Runnable collisionAction) {
        // When a collidable visits a boundary, tell it to handle a boundary collision
        visitor.collideWithBoundary();
        if (collisionAction != null) {
            collisionAction.run();
        }
    }
}
