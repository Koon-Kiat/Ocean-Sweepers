package project.game.application.movement.strategy;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IStoppableStrategy;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;

/**
 * Provides accelerated movement for movable entities.
 */
public class AcceleratedMovementStrategy extends AbstractMovementStrategy implements IStoppableStrategy {

    private final float acceleration;
    private final float deceleration;
    private final float maxSpeed;
    private float currentSpeed;

    /**
     * Constructs an AcceleratedMovementStrategy with specified parameters.
     */
    public AcceleratedMovementStrategy(float acceleration, float deceleration, float maxSpeed, boolean lenientMode) {
        super(AcceleratedMovementStrategy.class, lenientMode);

        // Validate parameters
        float[] accelParams = validateParameters(acceleration, deceleration, maxSpeed);
        this.acceleration = accelParams[0];
        this.deceleration = accelParams[1];
        this.maxSpeed = accelParams[2];

        this.currentSpeed = 0f;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Clamp delta to prevent excessively large updates.
            deltaTime = Math.min(deltaTime, 1 / 30f);

            // Get current velocity
            Vector2 velocity = getSafeVelocity(movable);
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
                applyMovement(movable, movement);
            }

        } catch (Exception e) {
            handleMovementException(e, "Error in AcceleratedMovementStrategy: " + e.getMessage());
            if (lenientMode) {
                movable.setVelocity(0, 0);
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
        // No special resume strategy needed - entity's velocity will be set elsewhere
    }

    /**
     * Validates all parameters at once and returns validated values
     */
    private float[] validateParameters(float acceleration, float deceleration, float maxSpeed) {
        float validAccel = acceleration;
        float validDecel = deceleration;
        float validMaxSpeed = maxSpeed;

        if (acceleration < 0 || deceleration < 0 || maxSpeed < 0) {
            String errorMessage = "Illegal negative values provided: acceleration=" + acceleration +
                    ", deceleration=" + deceleration + ", maxSpeed=" + maxSpeed;
            logger.error(errorMessage);
            if (lenientMode) {
                validAccel = Math.abs(acceleration);
                validDecel = Math.abs(deceleration);
                validMaxSpeed = Math.abs(maxSpeed);
                logger.warn("LENIENT_MODE enabled: Using absolute values for parameters.");
            } else {
                throw new MovementException(errorMessage);
            }
        }

        return new float[] { validAccel, validDecel, validMaxSpeed };
    }
}
