package project.game.engine.entitysystem.physics.collision.resolution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.badlogic.gdx.math.Vector2;

import project.game.engine.entitysystem.movement.management.MovementManager;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;

/**
 * EntityCollisionUpdater is a utility class that updates an entity's position
 * based on its MovementManager and collision state.
 */
public class CollisionResponseHandler {

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
        float inputX = movementManager.getMovableEntity().getX();
        float inputY = movementManager.getMovableEntity().getY();

        // Clamp input position within screen bounds
        inputX = Math.max(halfWidth, Math.min(inputX, gameWidth - halfWidth));
        inputY = Math.max(halfHeight, Math.min(inputY, gameHeight - halfHeight));
        // Check if we're at a boundary
        boolean atBoundary = isAtBoundary(currentX, currentY, halfWidth, halfHeight, gameWidth, gameHeight);

        if (atBoundary) {
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
            movementManager.getMovableEntity().setX(inputX);
            movementManager.getMovableEntity().setY(inputY);
            return;
        }

        // Handle non-boundary movement
        if (entity.isInCollision()) {
            // During collision, sync positions from Box2D physics
            float physX = entity.getBody().getPosition().x * pixelsToMeters;
            float physY = entity.getBody().getPosition().y * pixelsToMeters;

            // Update both entity and movement manager positions
            entity.getEntity().setX(physX);
            entity.getEntity().setY(physY);
            movementManager.getMovableEntity().setX(physX);
            movementManager.getMovableEntity().setY(physY);

            // Don't clear velocity completely during collision
            Vector2 velocity = entity.getBody().getLinearVelocity();
            float speed = velocity.len();

            // Keep some momentum during collision
            if (speed > 0) {
                velocity.scl(1f); // Gradual slowdown
                entity.getBody().setLinearVelocity(velocity);
            }

            entity.getBody().setLinearDamping(1f);
            return;
        }

        // Normal movement when not in collision or at boundary
        entity.getEntity().setX(inputX);
        entity.getEntity().setY(inputY);
        entity.getBody().setTransform(inputX / pixelsToMeters, inputY / pixelsToMeters, 0);

        // Get movement manager velocity and apply it to the body
        Vector2 velocity = movementManager.getMovableEntity().getVelocity();
        if (velocity.len2() > 0) {
            entity.getBody().setLinearVelocity(velocity);
            entity.getBody().setLinearDamping(1f); // Keep consistent low damping
        }
    }

    /**
     * Synchronizes the logical entity's position from the Box2D body.
     * Also adds the ability to limit maximum displacement during collisions.
     */
    public static void syncEntity(ICollidableVisitor entity, float pixelsToMeters) {
        if (entity == null || entity.getBody() == null)
            return;

        // Get position from physics body
        float x = entity.getBody().getPosition().x * pixelsToMeters;
        float y = entity.getBody().getPosition().y * pixelsToMeters;

        // Get current entity position
        float currentX = entity.getEntity().getX();
        float currentY = entity.getEntity().getY();

        // If in collision state, limit the maximum displacement per frame
        // This prevents entities from being pushed too far in a single step
        if (entity.isInCollision()) {
            // Calculate displacement from current position
            float dx = x - currentX;
            float dy = y - currentY;
            float displacement = (float) Math.sqrt(dx * dx + dy * dy);

            // Set a maximum displacement per frame during collision
            float maxDisplacement = 10.0f;

            // If displacement exceeds limit, scale it down
            if (displacement > maxDisplacement) {
                float scale = maxDisplacement / displacement;
                x = currentX + dx * scale;
                y = currentY + dy * scale;

                // Limit the velocity to prevent excessive speed
                Vector2 vel = entity.getBody().getLinearVelocity();
                if (vel.len() > 10.0f) {
                    vel.nor().scl(10.0f);
                    entity.getBody().setLinearVelocity(vel);
                }
            }
        }

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
            Method method = entity.getClass().getMethod("setCollisionActive", long.class);
            method.invoke(entity, 0L);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Method doesn't exist or can't be accessed - silently continue
        }
    }
}