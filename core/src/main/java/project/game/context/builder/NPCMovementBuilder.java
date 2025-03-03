package project.game.context.builder;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.context.factory.MovementBehaviorFactory;
import project.game.context.movement.FollowMovementBehavior;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.entity.MovableEntity;
import project.game.engine.entitysystem.movement.NPCMovementManager;

/**
 * Builder class for creating NPCMovementManager objects.
 * Updated to use Vector2 instead of Direction.
 */
public class NPCMovementBuilder extends AbstractMovementBuilder<NPCMovementBuilder> {

    @Override
    public NPCMovementBuilder setSpeed(float speed) {
        return super.setSpeed(speed);
    }

    public NPCMovementBuilder withConstantMovement() {
        try {
            this.movementBehavior = MovementBehaviorFactory.createConstantMovement(this.speed, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating ConstantMovementBehavior in lenient mode: " + e.getMessage()
                        + ". Using default movement.");
                this.movementBehavior = MovementBehaviorFactory.createDefaultMovement();
                return this;
            }
            LOGGER.fatal("Error in ConstantMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in ConstantMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        try {
            this.movementBehavior = MovementBehaviorFactory.createZigZagMovement(this.speed, amplitude, frequency,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating ZigZagMovementBehavior in lenient mode: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in ZigZagMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in ZigZagMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withFollowMovement(IPositionable target) {
        if (target == null) {
            String errorMsg = "Target is null in withFollowMovement.";
            if (this.lenientMode) {
                LOGGER.warn(errorMsg + " Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            this.movementBehavior = MovementBehaviorFactory.createFollowMovement(target, this.speed, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error in FollowMovementBehavior in lenient mode: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in FollowMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in FollowMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withRandomisedMovement(List<IMovementBehavior> behaviorPool, float minDuration,
            float maxDuration) {
        if (behaviorPool == null || behaviorPool.isEmpty()) {
            String errorMsg = behaviorPool == null ? "Behavior pool cannot be null in withRandomisedMovement."
                    : "Behavior pool cannot be empty in withRandomisedMovement.";
            if (this.lenientMode) {
                LOGGER.warn(errorMsg + " Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            this.movementBehavior = MovementBehaviorFactory.createRandomisedMovement(behaviorPool, minDuration,
                    maxDuration, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error in RandomisedMovementBehavior in lenient mode: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in RandomisedMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error in RandomisedMovementBehavior: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withOrbitalMovement(IPositionable target, float orbitRadius, float rotationSpeed,
            float eccentricity) {
        try {
            this.movementBehavior = MovementBehaviorFactory.createOrbitalMovement(target, orbitRadius, rotationSpeed,
                    eccentricity,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating OrbitalMovementBehavior: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    public NPCMovementBuilder withSpringFollow(IPositionable target, float springConstant, float damping) {
        try {
            this.movementBehavior = MovementBehaviorFactory.createSpringFollowMovement(target, springConstant, damping,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating SpringFollowBehavior: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    public NPCMovementBuilder withInterceptorMovement(MovableEntity target) {
        try {
            this.movementBehavior = MovementBehaviorFactory.createInterceptorMovement(target, this.speed,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating InterceptorMovementBehavior: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    public NPCMovementBuilder withSpiralApproach(IPositionable target, float spiralTightness, float approachSpeed) {
        try {
            this.movementBehavior = MovementBehaviorFactory.createSpiralApproachMovement(target, this.speed,
                    spiralTightness, approachSpeed,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating SpiralApproachBehavior: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
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

            // Use initialVelocity instead of direction
            if (initialVelocity.len2() < 0.0001f
                    && !(this.movementBehavior instanceof FollowMovementBehavior)) {
                if (lenientMode) {
                    LOGGER.warn("{0} needs an initial velocity. Setting default up direction.",
                            this.movementBehavior.getClass().getSimpleName());
                    this.initialVelocity = new Vector2(0, 1); // Default to moving up
                } else {
                    String errorMessage = "Invalid configuration: " + this.movementBehavior.getClass().getSimpleName()
                            + " needs a non-zero initial velocity";
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
            if (lenientMode) {
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
