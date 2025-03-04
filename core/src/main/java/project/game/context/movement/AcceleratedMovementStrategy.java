package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IStoppableStrategy;

/**
 * Provides accelerated movement for movable entities.
 */
public class AcceleratedMovementStrategy implements IStoppableStrategy {

    private static final GameLogger LOGGER = new GameLogger(AcceleratedMovementStrategy.class);
    private final float acceleration;
    private final float deceleration;
    private final float maxSpeed;
    private final boolean lenientMode;
    private float currentSpeed;

    /**
     * Constructs an AcceleratedMovementBehavior with specified parameters.
     * Terminates the program if any provided parameter is negative.
     */
    public AcceleratedMovementStrategy(float acceleration, float deceleration, float maxSpeed, boolean lenientMode) {
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
    public void move(IMovable movable, float deltaTime) {
        try {
            // Clamp delta to prevent excessively large updates.
            deltaTime = Math.min(deltaTime, 1 / 30f);

            // Get current velocity
            Vector2 velocity = movable.getVelocity();
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
                movable.setX(movable.getX() + movement.x);
                movable.setY(movable.getY() + movement.y);
            }

        } catch (MovementException e) {
            LOGGER.fatal("Exception in AcceleratedMovementBehavior.updatePosition: " + e.getMessage(), e);
            if (lenientMode) {
                movable.setVelocity(0, 0);
            } else {
                throw e;
            }
        } catch (Exception e) {
            LOGGER.fatal("Unexpected error in AcceleratedMovementBehavior: " + e.getMessage(), e);
            if (lenientMode) {
                movable.setVelocity(0, 0);
            } else {
                throw new MovementException("Error updating position in AcceleratedMovementBehavior", e);
            }
        }
    }

    @Override
    public void stopMovement(IMovable movable, float deltaTime) {
        currentSpeed = 0;
        movable.setVelocity(0, 0);
    }

    @Override
    public void resumeMovement(IMovable movable, float deltaTime) {
        // No special resume behavior needed - entity's velocity will be set elsewhere
    }
}
