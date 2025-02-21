package project.game.builder;

import java.util.List;
import java.util.logging.Level;

import project.game.Direction;
import project.game.abstractengine.entitysystem.movementmanager.MovementManager;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;
import project.game.abstractengine.interfaces.IMovementBehavior;
import project.game.abstractengine.interfaces.IMovementManager;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.RandomisedMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;
import project.game.exceptions.MovementException;

/**
 * Builder class for creating NPCMovementManager objects.
 */
public class NPCMovementBuilder extends AbstractMovementBuilder<NPCMovementBuilder> {

    @Override
    public NPCMovementBuilder setSpeed(float speed) {
        return super.setSpeed(speed);
    }

    public NPCMovementBuilder withConstantMovement() {
        try {
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error in ConstantMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in ConstantMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        try {
            this.movementBehavior = new ZigZagMovementBehavior(this.speed, amplitude, frequency);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error in ZigZagMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in ZigZagMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withFollowMovement(IMovementManager targetManager) {
        if (targetManager == null) {
            String errorMsg = "Target manager is null in withFollowMovement.";
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            this.movementBehavior = new FollowMovementBehavior(targetManager, this.speed);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error in FollowMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in FollowMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withRandomisedMovement(List<IMovementBehavior> behaviorPool, float minDuration,
            float maxDuration) {
        if (behaviorPool == null) {
            String errorMsg = "Behavior pool cannot be null in withRandomisedMovement.";
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new MovementException(errorMsg);
        }
        if (behaviorPool.isEmpty()) {
            if (MovementManager.LENIENT_MODE) {
                LOGGER.log(Level.WARNING,
                        "Behavior pool is empty in withRandomisedMovement. Delegating to RandomisedMovementBehavior fallback.");
            } else {
                String errorMsg = "Behavior pool cannot be empty in withRandomisedMovement.";
                LOGGER.log(Level.SEVERE, errorMsg);
                throw new MovementException(errorMsg);
            }
        }
        try {
            this.movementBehavior = new RandomisedMovementBehavior(behaviorPool, minDuration, maxDuration);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error in RandomisedMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in RandomisedMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementManager build() {
        try {
            validateBuildRequirements();
            if (this.entity == null) {
                String errorMsg = "Entity must not be null for NPCMovementBuilder.";
                LOGGER.log(Level.SEVERE, errorMsg);
                throw new MovementException(errorMsg);
            }
            if (this.movementBehavior == null) {
                LOGGER.log(Level.WARNING, "No movement behavior specified. Defaulting to ConstantMovementBehavior.");
                withConstantMovement();
            }
            if (this.direction == null) {
                this.direction = Direction.NONE;
                LOGGER.log(Level.WARNING, "No direction specified. Defaulting to Direction.NONE.");
            }
            if (!(this.movementBehavior instanceof FollowMovementBehavior) && this.direction == Direction.NONE) {
                if (MovementManager.LENIENT_MODE) {
                    LOGGER.log(Level.WARNING,
                            "{0} cannot be used with Direction.NONE. Defaulting direction to UP.",
                            this.movementBehavior.getClass().getSimpleName());
                    this.direction = Direction.UP;
                } else {
                    String errorMessage = "Invalid configuration: " + this.movementBehavior.getClass().getSimpleName()
                            + " cannot be used with Direction.NONE";
                    LOGGER.log(Level.SEVERE, errorMessage);
                    throw new MovementException(errorMessage);
                }
            }
            return new NPCMovementManager(this);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Failed to build NPCMovementManager: " + e.getMessage(), e);
            throw new MovementException("Failed to build NPCMovementManager: " + e.getMessage(), e);
        } catch (Exception e) {
            String msg = "Unexpected error building NPCMovementManager";
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
                        "Negative speed ({0}) found in NPCMovementBuilder. Using absolute value.",
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
