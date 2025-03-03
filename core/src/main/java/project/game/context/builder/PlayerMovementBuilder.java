package project.game.context.builder;

import project.game.common.exception.MovementException;
import project.game.context.factory.MovementBehaviorFactory;
import project.game.engine.entitysystem.movement.PlayerMovementManager;

/**
 * Builder class for creating PlayerMovementManager objects.
 * Updated to use Vector2 for movement instead of Direction.
 */
public class PlayerMovementBuilder extends AbstractMovementBuilder<PlayerMovementBuilder> {

    private static final float DEFAULT_SPEED = 200f;
    private static final float DEFAULT_ACCELERATION = 500f;
    private static final float DEFAULT_DECELERATION = 250f;

    // Static factory methods
    public static PlayerMovementBuilder createDefaultPlayer() {
        return new PlayerMovementBuilder()
                .setSpeed(DEFAULT_SPEED)
                .setInitialVelocity(0, 0)
                .withConstantMovement();
    }

    public static PlayerMovementBuilder createAcceleratingPlayer() {
        return new PlayerMovementBuilder()
                .setSpeed(DEFAULT_SPEED)
                .setInitialVelocity(0, 0)
                .withAcceleratedMovement(DEFAULT_ACCELERATION, DEFAULT_DECELERATION);
    }

    public PlayerMovementBuilder withConstantMovement() {
        try {
            this.movementBehavior = MovementBehaviorFactory.createConstantMovement(this.speed, this.lenientMode);
        } catch (Exception e) {
            if (this.lenientMode) {
                LOGGER.warn("Failed to create ConstantMovementBehavior in lenient mode: " + e.getMessage()
                        + ". Using default movement.");
                this.movementBehavior = MovementBehaviorFactory.createDefaultMovement();
                return this;
            }
            LOGGER.fatal("Failed to create ConstantMovementBehavior", e);
            throw new MovementException("Failed to create ConstantMovementBehavior", e);
        }
        return this;
    }

    public PlayerMovementBuilder withAcceleratedMovement(float acceleration, float deceleration) {
        try {
            this.movementBehavior = MovementBehaviorFactory.createAcceleratedMovement(acceleration, deceleration,
                    this.speed, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating AcceleratedMovementBehavior in lenient mode: " + e.getMessage()
                        + ". Using ConstantMovement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in movement behavior creation: " + e.getMessage(), e);
            throw new MovementException("Error in movement behavior creation: " + e.getMessage(), e);
        }
        return this;
    }

    public PlayerMovementManager build() {
        validateBuildRequirements();
        try {
            if (this.entity == null) {
                String errorMsg = "Entity must not be null for PlayerMovementBuilder.";
                LOGGER.fatal(errorMsg);
                throw new MovementException(errorMsg);
            }
            if (this.movementBehavior == null) {
                LOGGER.info("No movement behavior specified. Using default ConstantMovementBehavior.");
                withConstantMovement();
            }
            return new PlayerMovementManager(this);
        } catch (MovementException e) {
            LOGGER.fatal("Failed to build PlayerMovementManager: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            String msg = "Unexpected error building PlayerMovementManager";
            LOGGER.fatal(msg, e);
            throw new MovementException(msg, e);
        }
    }

    // Private validation method
    @Override
    protected void validateBuildRequirements() {
        if (speed < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative speed found in PlayerMovementBuilder ({0}). Using absolute value.",
                        new Object[] { speed });
                speed = Math.abs(speed);
            } else {
                String errorMessage = "Speed cannot be negative. Current speed: " + speed;
                LOGGER.fatal(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
    }

}
