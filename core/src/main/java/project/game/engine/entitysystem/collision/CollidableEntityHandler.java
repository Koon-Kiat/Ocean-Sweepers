package project.game.engine.entitysystem.collision;

import java.util.List;

import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.collision.ICollisionOperation;
import project.game.engine.entitysystem.entity.Entity;

/**
 * Default implementation of ICollisionHandler for ICollidable entities
 * This class adapts the ICollidable interface to the ICollisionHandler
 * interface
 */
public class CollidableEntityHandler implements ICollisionOperation {

    private final ICollidableVisitor collidable;

    public CollidableEntityHandler(ICollidableVisitor collidable) {
        this.collidable = collidable;
    }

    @Override
    public void handleCollisionWith(Object other, List<Runnable> collisionQueue) {
        if ("boundary".equals(other)) {
            collisionQueue.add(collidable::collideWithBoundary);
            return;
        }

        // Handle other collidables through double dispatch
        if (other instanceof ICollidableVisitor) {
            ICollidableVisitor otherCollidable = (ICollidableVisitor) other;
            Entity otherEntity = otherCollidable.getEntity();

            if (collidable.checkCollision(otherEntity)) {
                final ICollidableVisitor otherFinal = otherCollidable;
                collisionQueue.add(() -> collidable.collideWith(otherFinal));
            }
        }
    }

    @Override
    public boolean handlesCollisionWith(Class<?> clazz) {
        return ICollidableVisitor.class.isAssignableFrom(clazz) ||
                String.class.equals(clazz);
    }
}