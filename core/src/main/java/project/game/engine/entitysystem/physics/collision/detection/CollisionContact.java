package project.game.engine.entitysystem.physics.collision.detection;

import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.api.ICollisionElement;

/**
 * A collision target implementation for collidable entities.
 */
public class CollisionContact implements ICollisionElement {

    private final ICollidableVisitor collidable;

    public CollisionContact(ICollidableVisitor collidable) {
        this.collidable = collidable;
    }

    /**
     * Get the collidable entity this target represents
     * 
     * @return The collidable entity
     */
    public ICollidableVisitor getCollidable() {
        return collidable;
    }

    @Override
    public void acceptCollision(ICollidableVisitor visitor, Runnable collisionAction) {
        // First check if the collision is valid according to collision detection logic
        Entity visitorEntity = visitor.getEntity();

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
}