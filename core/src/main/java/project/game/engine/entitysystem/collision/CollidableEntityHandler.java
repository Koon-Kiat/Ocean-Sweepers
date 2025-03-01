package project.game.engine.entitysystem.collision;

import java.util.List;

import project.game.engine.api.collision.ICollidable;
import project.game.engine.api.collision.ICollisionHandler;
import project.game.engine.entitysystem.entity.Entity;

/**
 * Default implementation of ICollisionHandler for ICollidable entities
 * This class adapts the ICollidable interface to the ICollisionHandler
 * interface
 */
public class CollidableEntityHandler implements ICollisionHandler {

    private final ICollidable collidable;

    public CollidableEntityHandler(ICollidable collidable) {
        this.collidable = collidable;
    }

    @Override
    public void handleCollisionWith(Object other, List<Runnable> collisionQueue) {
        if ("boundary".equals(other)) {
            collisionQueue.add(collidable::collideWithBoundary);
            return;
        }

        // Handle other collidables through double dispatch
        if (other instanceof ICollidable) {
            ICollidable otherCollidable = (ICollidable) other;
            Entity otherEntity = otherCollidable.getEntity();

            if (collidable.checkCollision(otherEntity)) {
                final ICollidable otherFinal = otherCollidable;
                collisionQueue.add(() -> collidable.collideWith(otherFinal));
            }
        }
    }

    @Override
    public boolean handlesCollisionWith(Class<?> clazz) {
        return ICollidable.class.isAssignableFrom(clazz) ||
                String.class.equals(clazz);
    }
}