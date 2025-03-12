package project.game.engine.entitysystem.physics.boundary;

import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.api.ICollisionElement;

/**
 * Represents a boundary in the game world that can be collided with.
 * This implementation uses the visitor pattern to handle collisions with
 * boundaries.
 */
public class BoundaryCollisionElement implements ICollisionElement {

    private static final BoundaryCollisionElement INSTANCE = new BoundaryCollisionElement();

    // Private constructor to enforce singleton pattern
    private BoundaryCollisionElement() {
    }

    /**
     * Get the singleton instance of the boundary collision target
     * 
     * @return The boundary collision target instance
     */
    public static BoundaryCollisionElement getInstance() {
        return INSTANCE;
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
