package project.game.engine.entitysystem.collision;

import project.game.engine.api.ICollidable;
import project.game.engine.api.ICollisionTarget;
import project.game.engine.entitysystem.entity.Entity;

/**
 * A collision target implementation for collidable entities.
 * This implementation eliminates the need for instanceof checks
 * by providing a visitable target for collision handling.
 */
public class CollidableEntityTarget implements ICollisionTarget {

    private final ICollidable collidable;

    public CollidableEntityTarget(ICollidable collidable) {
        this.collidable = collidable;
    }

    @Override
    public void acceptCollision(ICollidable visitor, Runnable collisionAction) {
        // First check if the collision is valid according to collision detection logic
        Entity visitorEntity = visitor.getEntity();
        Entity targetEntity = collidable.getEntity();

        // Only handle collision if detector confirms it
        if (collidable.checkCollision(visitorEntity)) {
            // The visitor collides with this target
            visitor.collideWith(collidable);

            // The target also needs to handle collision with the visitor
            // This ensures both entities respond to the collision
            collidable.collideWith(visitor);

            // Execute additional collision handling if provided
            if (collisionAction != null) {
                collisionAction.run();
            }
        }
    }

    /**
     * Get the collidable entity this target represents
     * 
     * @return The collidable entity
     */
    public ICollidable getCollidable() {
        return collidable;
    }
}