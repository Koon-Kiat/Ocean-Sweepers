package project.game.application.entity.factory;

import java.util.List;

import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.factory.IEntityFactory;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.management.CollisionManager;

/**
 * Abstract factory implementing common functionality for entity creation
 * 
 * @param <T> The type of object to be created by this factory
 */
public abstract class AbstractEntityFactory<T> implements IEntityFactory<T> {
    protected final IGameConstants constants;
    protected final World world;
    protected final List<Entity> existingEntities;
    protected final CollisionManager collisionManager;

    protected AbstractEntityFactory(IGameConstants constants, World world, List<Entity> existingEntities,
            CollisionManager collisionManager) {
        this.constants = constants;
        this.world = world;
        this.existingEntities = existingEntities;
        this.collisionManager = collisionManager;
    }

    protected boolean isOverlapping(float x, float y, float width, float height, Entity entity) {
        return x < entity.getX() + entity.getWidth() &&
                x + width > entity.getX() &&
                y < entity.getY() + entity.getHeight() &&
                y + height > entity.getY();
    }

    protected boolean checkCollisionWithExisting(float x, float y, float width, float height) {
        for (Entity entity : existingEntities) {
            if (isOverlapping(x, y, width, height, entity)) {
                return true;
            }
        }
        return false;
    }
}