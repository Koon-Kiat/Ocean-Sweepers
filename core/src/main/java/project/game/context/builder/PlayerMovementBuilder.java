package project.game.context.builder;

import java.util.logging.Level;

import project.game.common.exception.MovementException;
import project.game.context.factory.MovementBehaviorFactory;
import project.game.engine.entitysystem.movement.MovementManager;
import project.game.engine.entitysystem.movement.PlayerMovementManager;

/**
 * Builder class for creating PlayerMovementManager objects.
 */
public class PlayerMovementBuilder extends AbstractMovementBuilder<PlayerMovementBuilder> {

    private static final float DEFAULT_SPEED = 200f;
    private static final float DEFAULT_ACCELERATION = 500f;
    private static final float DEFAULT_DECELERATION = 250f;

    // Static factory methods
    public static PlayerMovementBuilder createDefaultPlayer() {
        return new PlayerMovementBuilder()
                .setSpeed(DEFAULT_SPEED)
                .withConstantMovement();
    }

    public static PlayerMovementBuilder createAcceleratingPlayer() {
        return new PlayerMovementBuilder()
                .setSpeed(DEFAULT_SPEED)
                .withAcceleratedMovement(DEFAULT_ACCELERATION, DEFAULT_DECELERATION);
    }

    public PlayerMovementBuilder withAcceleratedMovement(float acceleration, float deceleration) {
        try {
            this.movementBehavior = MovementBehaviorFactory.createConstantMovement(this.speed);
            // Note: This seems to be using a ConstantMovementBehavior but should probably
            // use
            // an accelerated movement behavior - you might want to add that to your factory
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error in movement behavior creation: " + e.getMessage(), e);
            throw new MovementException("Error in movement behavior creation: " + e.getMessage(), e);
        }
        return this;
    }

    public PlayerMovementBuilder withConstantMovement() {
        try {
            this.movementBehavior = MovementBehaviorFactory.createConstantMovement(this.speed);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create ConstantMovementBehavior", e);
            throw new MovementException("Failed to create ConstantMovementBehavior", e);
        }
        return this;
    }

    public PlayerMovementManager build() {
        validateBuildRequirements();
        try {
            if (this.entity == null) {
                String errorMsg = "Entity must not be null for PlayerMovementBuilder.";
                LOGGER.log(Level.SEVERE, errorMsg);
                throw new MovementException(errorMsg);
            }
            if (this.movementBehavior == null) {
                LOGGER.log(Level.INFO, "No movement behavior specified. Using default ConstantMovementBehavior.");
                withConstantMovement();
            }
            return new PlayerMovementManager(this);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Failed to build PlayerMovementManager: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            String msg = "Unexpected error building PlayerMovementManager";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new MovementException(msg, e);
        }
    }

    // Private validation method
    @Override
    protected void validateBuildRequirements() {
        if (speed < 0) {
            if (MovementManager.LENIENT_MODE) {
                LOGGER.log(Level.WARNING,
                        "Negative speed found in PlayerMovementBuilder ({0}). Using absolute value.",
                        new Object[] { speed });
                speed = Math.abs(speed);
            } else {
                String errorMessage = "Speed cannot be negative. Current speed: " + speed;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new MovementException(errorMessage);
            }
        }
    }

}
