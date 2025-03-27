package project.game.application.entity.factory;

import java.util.List;

import com.badlogic.gdx.physics.box2d.World;

import project.game.application.entity.api.IEntityFactory;
import project.game.common.config.api.IGameConstants;
import project.game.common.config.factory.GameConstantsFactory;
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
        // Calculate overlap percentages
        float overlapX = Math.min(x + width, entity.getX() + entity.getWidth()) - Math.max(x, entity.getX());
        float overlapY = Math.min(y + height, entity.getY() + entity.getHeight()) - Math.max(y, entity.getY());

        // Only consider it an overlap if both dimensions overlap
        if (overlapX <= 0 || overlapY <= 0) {
            return false;
        }

        // Calculate overlap area as a percentage of the smaller entity
        float overlapArea = overlapX * overlapY;
        float entityArea = width * height;
        float otherArea = entity.getWidth() * entity.getHeight();
        float minArea = Math.min(entityArea, otherArea);

        // Only consider it a collision if overlap is more than 75% of the smaller
        // entity
        return overlapArea > (minArea * 0.75f);
    }

    protected boolean checkCollisionWithExisting(float x, float y, float width, float height) {
        // For rocks, maintain stricter collision checking
        if (existingEntities.isEmpty()) {
            return false;
        }

        Entity firstEntity = existingEntities.get(0);
        boolean isRock = firstEntity.getWidth() == GameConstantsFactory.getConstants().ROCK_WIDTH();

        // Use a more lenient check for non-rock entities
        float minDistanceSquared = isRock ? (width + height) * (width + height) : // Stricter for rocks
                (width + height) * (width + height) / 4; // More lenient for other entities

        for (Entity entity : existingEntities) {
            float dx = (x + width / 2) - (entity.getX() + entity.getWidth() / 2);
            float dy = (y + height / 2) - (entity.getY() + entity.getHeight() / 2);
            float distanceSquared = dx * dx + dy * dy;

            if (distanceSquared < minDistanceSquared) {
                return true;
            }
        }
        return false;
    }
}