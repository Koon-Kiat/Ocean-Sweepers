package project.game.context.builder;

import java.util.List;

import project.game.common.exception.MovementException;
import project.game.context.core.Direction;
import project.game.context.factory.MovementBehaviorFactory;
import project.game.context.movement.FollowMovementBehavior;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.movement.MovementManager;
import project.game.engine.entitysystem.movement.NPCMovementManager;

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
            this.movementBehavior = MovementBehaviorFactory.createConstantMovement(this.speed);
        } catch (MovementException e) {
            LOGGER.fatal("Error in ConstantMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in ConstantMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        try {
            this.movementBehavior = MovementBehaviorFactory.createZigZagMovement(this.speed, amplitude, frequency);
        } catch (MovementException e) {
            LOGGER.fatal("Error in ZigZagMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in ZigZagMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withFollowMovement(IPositionable target) {
        if (target == null) {
            String errorMsg = "Target is null in withFollowMovement.";
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            this.movementBehavior = MovementBehaviorFactory.createFollowMovement(target, this.speed);
        } catch (MovementException e) {
            LOGGER.fatal("Error in FollowMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in FollowMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withRandomisedMovement(List<IMovementBehavior> behaviorPool, float minDuration,
            float maxDuration) {
        if (behaviorPool == null) {
            String errorMsg = "Behavior pool cannot be null in withRandomisedMovement.";
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        if (behaviorPool.isEmpty()) {
            if (MovementManager.LENIENT_MODE) {
                LOGGER.warn(
                        "Behavior pool is empty in withRandomisedMovement. Delegating to RandomisedMovementBehavior fallback.");
            } else {
                String errorMsg = "Behavior pool cannot be empty in withRandomisedMovement.";
                LOGGER.fatal(errorMsg);
                throw new MovementException(errorMsg);
            }
        }
        try {
            this.movementBehavior = MovementBehaviorFactory.createRandomisedMovement(behaviorPool, minDuration,
                    maxDuration);
        } catch (MovementException e) {
            LOGGER.fatal("Error in RandomisedMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in RandomisedMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementManager build() {
        try {
            validateBuildRequirements();
            if (this.entity == null) {
                String errorMsg = "Entity must not be null for NPCMovementBuilder.";
                LOGGER.fatal(errorMsg);
                throw new MovementException(errorMsg);
            }
            if (this.movementBehavior == null) {
                LOGGER.warn("No movement behavior specified. Defaulting to ConstantMovementBehavior.");
                withConstantMovement();
            }
            if (this.direction == null) {
                this.direction = Direction.NONE;
                LOGGER.warn("No direction specified. Defaulting to Direction.NONE.");
            }
            if (!(this.movementBehavior instanceof FollowMovementBehavior) && this.direction == Direction.NONE) {
                if (MovementManager.LENIENT_MODE) {
                    LOGGER.warn("{0} cannot be used with Direction.NONE. Defaulting direction to UP.",
                            this.movementBehavior.getClass().getSimpleName());
                    this.direction = Direction.UP;
                } else {
                    String errorMessage = "Invalid configuration: " + this.movementBehavior.getClass().getSimpleName()
                            + " cannot be used with Direction.NONE";
                    LOGGER.fatal(errorMessage);
                    throw new MovementException(errorMessage);
                }
            }
            return new NPCMovementManager(this);
        } catch (MovementException e) {
            LOGGER.fatal("Failed to build NPCMovementManager: " + e.getMessage(), e);
            throw new MovementException("Failed to build NPCMovementManager: " + e.getMessage(), e);
        } catch (Exception e) {
            String msg = "Unexpected error building NPCMovementManager";
            LOGGER.fatal(msg, e);
            throw new MovementException(msg, e);
        }
    }

    // Private validation method
    @Override
    protected void validateBuildRequirements() {
        if (speed < 0) {
            if (MovementManager.LENIENT_MODE) {
                LOGGER.warn("Negative speed ({0}) found in NPCMovementBuilder. Using absolute value.",
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
