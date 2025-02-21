package project.game.builder;

import java.util.logging.Level;

import project.game.abstractengine.entitysystem.movementmanager.MovementManager;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.exceptions.MovementException;

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
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error in ConstantMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in ConstantMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public PlayerMovementBuilder withConstantMovement() {
        try {
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
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
