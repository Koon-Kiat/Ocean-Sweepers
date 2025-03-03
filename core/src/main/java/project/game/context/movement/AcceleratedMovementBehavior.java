package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IStoppableMovementBehavior;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * Provides accelerated movement for movable entities.
 */
public class AcceleratedMovementBehavior implements IStoppableMovementBehavior {

    private static final GameLogger LOGGER = new GameLogger(AcceleratedMovementBehavior.class);
    private final float acceleration;
    private final float deceleration;
    private final float maxSpeed;
    private final boolean lenientMode;
    private float currentSpeed;

    /**
     * Constructs an AcceleratedMovementBehavior with specified parameters.
     * Terminates the program if any provided parameter is negative.
     */
    public AcceleratedMovementBehavior(float acceleration, float deceleration, float maxSpeed, boolean lenientMode) {
        this.lenientMode = lenientMode;
        if (acceleration < 0 || deceleration < 0 || maxSpeed < 0) {
            String errorMessage = "Illegal negative values provided: acceleration=" + acceleration +
                    ", deceleration=" + deceleration + ", maxSpeed=" + maxSpeed;
            LOGGER.fatal(errorMessage);
            if (lenientMode) {
                this.acceleration = Math.abs(acceleration);
                this.deceleration = Math.abs(deceleration);
                this.maxSpeed = Math.abs(maxSpeed);
                LOGGER.warn("LENIENT_MODE enabled: Using absolute values for parameters.");
            } else {
                throw new MovementException(errorMessage);
            }
        } else {
            this.acceleration = acceleration;
            this.deceleration = deceleration;
            this.maxSpeed = maxSpeed;
        }
        this.currentSpeed = 0f;
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            // Clamp delta to prevent excessively large updates.
            deltaTime = Math.min(deltaTime, 1 / 30f);

            // Get current velocity
            Vector2 velocity = entity.getVelocity();
            boolean isMoving = velocity.len2() > 0.0001f;

            // Update current speed based on acceleration/deceleration
            if (isMoving) {
                currentSpeed += acceleration * deltaTime;
                if (currentSpeed > maxSpeed) {
                    currentSpeed = maxSpeed;
                }
            } else {
                currentSpeed -= deceleration * deltaTime;
                if (currentSpeed < 0) {
                    currentSpeed = 0;
                }
            }

            // If we're moving, apply movement
            if (isMoving && currentSpeed > 0) {
                // Create a normalized copy of the velocity vector
                Vector2 normalizedVelocity = new Vector2(velocity).nor();

                // Scale by current speed and deltaTime
                Vector2 movement = normalizedVelocity.scl(currentSpeed * deltaTime);

                // Apply movement
                entity.setX(entity.getX() + movement.x);
                entity.setY(entity.getY() + movement.y);
            }

        } catch (MovementException e) {
            LOGGER.fatal("Exception in AcceleratedMovementBehavior.updatePosition: " + e.getMessage(), e);
            if (lenientMode) {
                entity.setVelocity(0, 0);
            } else {
                throw e;
            }
        } catch (Exception e) {
            LOGGER.fatal("Unexpected error in AcceleratedMovementBehavior: " + e.getMessage(), e);
            if (lenientMode) {
                entity.setVelocity(0, 0);
            } else {
                throw new MovementException("Error updating position in AcceleratedMovementBehavior", e);
            }
        }
    }

    @Override
    public void stopMovement(MovableEntity entity, float deltaTime) {
        currentSpeed = 0;
        entity.setVelocity(0, 0);
    }

    @Override
    public void resumeMovement(MovableEntity entity, float deltaTime) {
        // No special resume behavior needed - entity's velocity will be set elsewhere
    }
}
