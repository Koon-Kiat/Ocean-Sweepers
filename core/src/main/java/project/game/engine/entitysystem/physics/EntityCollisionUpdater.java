package project.game.engine.entitysystem.physics;

import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.entitysystem.movement.core.MovementManager;

/**
 * EntityCollisionUpdater is a utility class that updates an entity's position
 * based on its MovementManager and collision state.
 */
public class EntityCollisionUpdater {

    /**
     * Updates an entity's position using its MovementManager when not in collision,
     * otherwise lets Box2D handle the physics.
     */
    public static void updateEntity(ICollidableVisitor entity, MovementManager movementManager,
            float gameWidth, float gameHeight, float pixelsToMeters,
            float collisionMovementStrength, float movementThreshold) {

        // If movement manager is null, just sync entity position
        if (movementManager == null) {
            syncEntity(entity, pixelsToMeters);
            return;
        }

        float halfWidth = entity.getEntity().getWidth() / 2;
        float halfHeight = entity.getEntity().getHeight() / 2;
        float currentX = entity.getEntity().getX();
        float currentY = entity.getEntity().getY();
        float inputX = movementManager.getX();
        float inputY = movementManager.getY();

        // Clamp input position within screen bounds
        inputX = Math.max(halfWidth, Math.min(inputX, gameWidth - halfWidth));
        inputY = Math.max(halfHeight, Math.min(inputY, gameHeight - halfHeight));

        // Check if we're at a boundary
        boolean atBoundary = isAtBoundary(currentX, currentY, halfWidth, halfHeight, gameWidth, gameHeight);

        if (atBoundary) {
            // Always clear any active collisions when at boundary
            if (entity.isInCollision()) {
                clearCollisionState(entity);
            }

            // Reset physics state at boundary
            entity.getBody().setLinearVelocity(0, 0);
            entity.getBody().setAngularVelocity(0);
            entity.getBody().setLinearDamping(0.1f);

            // Calculate which boundaries we're touching
            boolean touchingLeft = currentX <= halfWidth + 2;
            boolean touchingRight = currentX >= gameWidth - halfWidth - 2;
            boolean touchingBottom = currentY <= halfHeight + 2;
            boolean touchingTop = currentY >= gameHeight - halfHeight - 2;

            float dx = inputX - currentX;
            float dy = inputY - currentY;

            // Enhanced boundary movement logic
            if (touchingLeft || touchingRight) {
                // Allow vertical movement at full speed
                inputX = currentX;
                // Prevent "sticky" corners by allowing small movement away from boundary
                if ((touchingLeft && dx > 0) || (touchingRight && dx < 0)) {
                    inputX = currentX + dx * 0.5f;
                }
            }

            if (touchingTop || touchingBottom) {
                // Allow horizontal movement at full speed
                inputY = currentY;
                // Prevent "sticky" corners by allowing small movement away from boundary
                if ((touchingBottom && dy > 0) || (touchingTop && dy < 0)) {
                    inputY = currentY + dy * 0.5f;
                }
            }

            // Final position clamping
            inputX = Math.max(halfWidth, Math.min(inputX, gameWidth - halfWidth));
            inputY = Math.max(halfHeight, Math.min(inputY, gameHeight - halfHeight));

            // Update all positions smoothly
            entity.getEntity().setX(inputX);
            entity.getEntity().setY(inputY);
            entity.getBody().setTransform(inputX / pixelsToMeters, inputY / pixelsToMeters, 0);
            movementManager.setX(inputX);
            movementManager.setY(inputY);
            return;
        }

        // Handle non-boundary movement
        if (entity.isInCollision()) {
            // During collision, Box2D handles physics
            syncEntity(entity, pixelsToMeters);

            // Update movement manager position without applying movement
            // This ensures movement manager stays in sync with physics
            currentX = entity.getEntity().getX();
            currentY = entity.getEntity().getY();
            movementManager.setX(currentX);
            movementManager.setY(currentY);
            movementManager.clearVelocity();
            return;
        }

        // Normal movement when not in collision or at boundary
        entity.getEntity().setX(inputX);
        entity.getEntity().setY(inputY);
        entity.getBody().setTransform(inputX / pixelsToMeters, inputY / pixelsToMeters, 0);
        entity.getBody().setLinearVelocity(0, 0);
    }

    /**
     * Synchronizes the logical entity's position from the Box2D body.
     */
    public static void syncEntity(ICollidableVisitor entity, float pixelsToMeters) {
        if (entity == null || entity.getBody() == null)
            return;

        // Get position from physics body
        float x = entity.getBody().getPosition().x * pixelsToMeters;
        float y = entity.getBody().getPosition().y * pixelsToMeters;

        // Update entity position to match physics
        entity.getEntity().setX(x);
        entity.getEntity().setY(y);
    }

    /**
     * Checks if an entity is at or very close to the screen boundary
     */
    private static boolean isAtBoundary(float x, float y, float halfWidth, float halfHeight, float gameWidth,
            float gameHeight) {
        return x <= halfWidth || x >= gameWidth - halfWidth ||
                y <= halfHeight || y >= gameHeight - halfHeight;
    }

    /**
     * Attempts to clear an entity's collision state using reflection
     */
    private static void clearCollisionState(ICollidableVisitor entity) {
        try {
            java.lang.reflect.Method method = entity.getClass().getMethod("setCollisionActive", long.class);
            method.invoke(entity, 0L);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            // Method doesn't exist or can't be accessed - silently continue
        }
    }
}