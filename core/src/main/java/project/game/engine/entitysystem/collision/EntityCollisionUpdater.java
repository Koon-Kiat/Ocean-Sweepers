package project.game.engine.entitysystem.collision;

import project.game.engine.api.collision.ICollidable;
import project.game.engine.entitysystem.movement.MovementManager;

/**
 * EntityCollisionUpdater is a utility class that updates an entity's position
 * based on its MovementManager and collision state.
 * 
 * It also synchronizes the entity's position with its Box2D body.
 */
public class EntityCollisionUpdater {

    /**
     * Updates an entity's position using its MovementManager.
     * 
     * @param entity          The collidable entity to update
     * @param movementManager The movement manager controlling the entity
     * @param gameWidth       Width of the game area in pixels
     * @param gameHeight      Height of the game area in pixels
     * @param pixelsToMeters  Conversion factor from pixels to Box2D meters
     */
    public static void updateEntity(ICollidable entity, MovementManager movementManager, float gameWidth,
            float gameHeight, float pixelsToMeters) {
        // Calculate half dimensions from the entity's logical dimensions
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
            entity.getBody().setTransform(inputX / pixelsToMeters,
                    inputY / pixelsToMeters, 0);
        } else {
            // Collision mode: blend input with current physics position.
            float physicsX = entity.getBody().getPosition().x * pixelsToMeters;
            float physicsY = entity.getBody().getPosition().y * pixelsToMeters;
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
     * Synchronizes the logical entity's position from the Box2D body.
     * 
     * @param entity         The collidable entity to synchronize
     * @param pixelsToMeters Conversion factor from pixels to Box2D meters
     */
    public static void syncEntity(ICollidable entity, float pixelsToMeters) {
        float x = entity.getBody().getPosition().x * pixelsToMeters;
        float y = entity.getBody().getPosition().y * pixelsToMeters;
        entity.getEntity().setX(x);
        entity.getEntity().setY(y);
    }
}