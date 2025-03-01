//// filepath: /c:/OOPProject/core/src/main/java/project/game/engine/entitysystem/collisionmanager/EntityCollisionUpdater.java
package project.game.engine.entitysystem.collision;

import project.game.context.factory.GameConstantsFactory;
import project.game.engine.api.collision.ICollidable;
import project.game.engine.entitysystem.movement.MovementManager;

/**
 * EntityCollisionUpdater is a utility class that updates an entity’s position
 * based on its MovementManager and collision state.
 * 
 * It also synchronizes the entity’s position with its Box2D body.
 */
public class EntityCollisionUpdater {

    /**
     * Updates an entity’s position using its MovementManager.
     * 
     * If the entity is not in collision, the target (input) position is clamped
     * and used to update the Box2D body.
     * If it is in collision, a blend is performed between the physics position and
     * the input.
     */
    public static void updateEntity(ICollidable entity, MovementManager movementManager, float gameWidth,
            float gameHeight) {
        // Calculate half dimensions from the entity’s logical dimensions
        float halfWidth = entity.getEntity().getWidth() / 2;
        float halfHeight = entity.getEntity().getHeight() / 2;

        // Retrieve desired input position from the manager and clamp it within screen
        // bounds
        float inputX = movementManager.getX();
        float inputY = movementManager.getY();
        inputX = Math.max(halfWidth, Math.min(inputX, gameWidth - halfWidth));
        inputY = Math.max(halfHeight, Math.min(inputY, gameHeight - halfHeight));

        if (!entity.isInCollision()) {
            // Normal update: override physics using clamped input.
            entity.getEntity().setX(inputX);
            entity.getEntity().setY(inputY);
            entity.getBody().setTransform(inputX / GameConstantsFactory.getConstants().PIXELS_TO_METERS(),
                    inputY / GameConstantsFactory.getConstants().PIXELS_TO_METERS(), 0);
        } else {
            // Collision mode: blend input with current physics position.
            float physicsX = entity.getBody().getPosition().x * GameConstantsFactory.getConstants().PIXELS_TO_METERS();
            float physicsY = entity.getBody().getPosition().y * GameConstantsFactory.getConstants().PIXELS_TO_METERS();
            float blendFactor = 0.1f; // adjust blending factor as needed
            float newX = physicsX + (inputX - physicsX) * blendFactor;
            float newY = physicsY + (inputY - physicsY) * blendFactor;
            entity.getEntity().setX(newX);
            entity.getEntity().setY(newY);
            // Also update the manager to avoid stale input.
            movementManager.setX(newX);
            movementManager.setY(newY);
        }
    }

    /**
     * Synchronizes the logical entity’s position from the Box2D body.
     */
    public static void syncEntity(ICollidable entity) {
        float x = entity.getBody().getPosition().x * GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float y = entity.getBody().getPosition().y * GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        entity.getEntity().setX(x);
        entity.getEntity().setY(y);
    }
}