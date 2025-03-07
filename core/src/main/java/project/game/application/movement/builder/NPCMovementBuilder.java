package project.game.application.movement.builder;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import project.game.application.movement.factory.MovementStrategyFactory;
import project.game.application.movement.strategy.FollowMovementStrategy;
import project.game.common.exception.MovementException;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.api.movement.IMovementStrategyFactory;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.entity.MovableEntity;
import project.game.engine.entitysystem.movement.type.NPCMovementManager;

/**
 * Builder class for creating NPCMovementManager objects.
 * Updated to use Vector2 instead of Direction.
 */
public class NPCMovementBuilder extends AbstractMovementBuilder<NPCMovementBuilder> {

    private IMovementStrategyFactory movementStrategyFactory;

    public NPCMovementBuilder() {
        this.movementStrategyFactory = MovementStrategyFactory.getInstance();
    }

    /**
     * Sets the movement strategy factory to use for creating movement strategies.
     * 
     * @param factory The factory to use
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withMovementStrategyFactory(IMovementStrategyFactory factory) {
        if (factory != null) {
            this.movementStrategyFactory = factory;
        } else {
            LOGGER.warn("Null movement strategy factory provided. Using default factory.");
        }
        return this;
    }

    /**
     * Gets the movement strategy factory that will be used to create strategies.
     * 
     * @return The movement strategy factory
     */
    public IMovementStrategyFactory getMovementStrategyFactory() {
        return this.movementStrategyFactory;
    }

    public NPCMovementBuilder withConstantMovement() {
        try {
            this.movementStrategy = MovementStrategyFactory.createConstantMovement(this.speed, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating ConstantMovementStrategy in lenient mode: " + e.getMessage()
                        + ". Using default movement.");
                this.movementStrategy = this.movementStrategyFactory.createDefaultMovement();
                return this;
            }
            LOGGER.fatal("Error in ConstantMovementStrategy: " + e.getMessage(), e);
            throw new MovementException("Error in ConstantMovementStrategy: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        try {
            this.movementStrategy = MovementStrategyFactory.createZigZagMovement(this.speed, amplitude, frequency,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating ZigZagMovementStrategy in lenient mode: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in ZigZagMovementStrategy: " + e.getMessage(), e);
            throw new MovementException("Error in ZigZagMovementStrategy: " + e.getMessage(), e);
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
            this.movementStrategy = MovementStrategyFactory.createFollowMovement(target, this.speed, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error in FollowMovementStrategy in lenient mode: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in FollowMovementStrategy: " + e.getMessage(), e);
            throw new MovementException("Error in FollowMovementStrategy: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withRandomisedMovement(List<IMovementStrategy> strategyPool, float minDuration,
            float maxDuration) {
        if (strategyPool == null || strategyPool.isEmpty()) {
            String errorMsg = strategyPool == null ? "Strategy pool cannot be null in withRandomisedMovement."
                    : "Strategy pool cannot be empty in withRandomisedMovement.";
            if (this.lenientMode) {
                LOGGER.warn(errorMsg + " Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            this.movementStrategy = MovementStrategyFactory.createRandomisedMovement(strategyPool, minDuration,
                    maxDuration, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error in RandomisedMovementStrategy in lenient mode: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in RandomisedMovementStrategy: " + e.getMessage(), e);
            throw new MovementException("Error in RandomisedMovementStrategy: " + e.getMessage(), e);
        }
        return this;
    }

    public NPCMovementBuilder withOrbitalMovement(IPositionable target, float orbitRadius, float rotationSpeed,
            float eccentricity) {
        try {
            this.movementStrategy = MovementStrategyFactory.createOrbitalMovement(target, orbitRadius, rotationSpeed,
                    eccentricity,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating OrbitalMovementStrategy: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    public NPCMovementBuilder withSpringFollow(IPositionable target, float springConstant, float damping) {
        try {
            this.movementStrategy = MovementStrategyFactory.createSpringFollowMovement(target, springConstant, damping,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating SpringFollowStrategy: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    public NPCMovementBuilder withInterceptorMovement(IMovable movable) {
        try {
            this.movementStrategy = MovementStrategyFactory.createInterceptorMovement(movable, this.speed,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating InterceptorMovementStrategy: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    public NPCMovementBuilder withSpiralApproach(IPositionable target, float spiralTightness, float approachSpeed) {
        try {
            this.movementStrategy = MovementStrategyFactory.createSpiralApproachMovement(target, this.speed,
                    spiralTightness, approachSpeed,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating SpiralApproachStrategy: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    public NPCMovementBuilder withObstacleAvoidanceTargeting(IMovable target) {
        try {
            this.movementStrategy = MovementStrategyFactory.createObstacleAvoidanceStrategy(target, this.speed,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating ObstacleAvoidanceStrategy: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    /**
     * Creates an obstacle avoidance Strategy for this movement builder with a list
     * of obstacles to avoid.
     * 
     * @param obstacles The list of entities to avoid
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withObstacleAvoidance(List<Entity> obstacles) {
        try {
            this.movementStrategy = MovementStrategyFactory.createObstacleAvoidanceStrategy(this.speed, obstacles,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating ObstacleAvoidanceStrategy with obstacles: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    /**
     * Creates a default obstacle avoidance Strategy without specific obstacles.
     * Obstacles can be added later through the movementStrategy if needed.
     * 
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withObstacleAvoidance() {
        try {
            this.movementStrategy = MovementStrategyFactory.createObstacleAvoidanceStrategy(this.speed, null,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating ObstacleAvoidanceStrategy: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    /**
     * Creates a combined interceptor with obstacle avoidance movement strategy.
     * This makes the NPC follow/intercept a target while avoiding obstacles.
     * 
     * @param target    The target to follow/intercept
     * @param obstacles The obstacles to avoid
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withInterceptorAndObstacleAvoidance(IMovable target, List<Entity> obstacles) {
        try {
            this.movementStrategy = MovementStrategyFactory.createInterceptorWithObstacleAvoidance(
                    target, obstacles, this.speed, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating combined InterceptorWithObstacleAvoidance: " + e.getMessage()
                        + ". Using interceptor movement fallback.");
                return withInterceptorMovement(target);
            }
            throw e;
        }
        return this;
    }

    /**
     * Creates a combined interceptor with obstacle avoidance movement strategy
     * using custom weights.
     * 
     * @param target    The target to follow/intercept
     * @param obstacles The obstacles to avoid
     * @param weights   Custom weights for interceptor and avoidance (first for
     *                  interceptor, second for avoidance)
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withInterceptorAndObstacleAvoidance(IMovable target, List<Entity> obstacles,
            float[] weights) {
        try {
            this.movementStrategy = MovementStrategyFactory.createInterceptorWithObstacleAvoidance(
                    target, obstacles, this.speed, weights, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating combined InterceptorWithObstacleAvoidance with custom weights: "
                        + e.getMessage()
                        + ". Using interceptor movement fallback.");
                return withInterceptorMovement(target);
            }
            throw e;
        }
        return this;
    }

    /**
     * Creates a composite movement strategy combining multiple strategies.
     * 
     * @param baseStrategy         The primary strategy
     * @param additionalStrategies Additional strategies to apply
     * @param weights              The relative weights for each strategy
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withCompositeMovement(
            IMovementStrategy baseStrategy,
            List<IMovementStrategy> additionalStrategies,
            float[] weights) {

        try {
            this.movementStrategy = MovementStrategyFactory.createCompositeMovementStrategy(
                    baseStrategy, additionalStrategies, weights);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating CompositeMovementStrategy: " + e.getMessage()
                        + ". Using base strategy as fallback.");
                this.movementStrategy = baseStrategy;
            } else {
                throw e;
            }
        }
        return this;
    }

    public NPCMovementManager build() {
        try {
            validateBuildRequirements();
            if (this.entity == null && this.movableEntity == null) {
                String errorMsg = "Entity must not be null for NPCMovementBuilder.";
                LOGGER.fatal(errorMsg);
                throw new MovementException(errorMsg);
            }
            if (this.movementStrategy == null) {
                LOGGER.warn("No movement Strategy specified. Defaulting to ConstantMovementStrategy.");
                withConstantMovement();
            }

            // Use initialVelocity instead of direction
            if (initialVelocity.len2() < 0.0001f
                    && !(this.movementStrategy instanceof FollowMovementStrategy)) {
                if (lenientMode) {
                    LOGGER.warn("{0} needs an initial velocity. Setting default up direction.",
                            this.movementStrategy.getClass().getSimpleName());
                    this.initialVelocity = new Vector2(0, 1); // Default to moving up
                } else {
                    String errorMessage = "Invalid configuration: " + this.movementStrategy.getClass().getSimpleName()
                            + " needs a non-zero initial velocity";
                    LOGGER.fatal(errorMessage);
                    throw new MovementException(errorMessage);
                }
            }

            if (this.movementStrategyFactory == null) {
                LOGGER.warn("Movement strategy factory is null. Using default factory.");
                this.movementStrategyFactory = MovementStrategyFactory.getInstance();
            }

            return new NPCMovementManager(this, this.movementStrategyFactory);
        } catch (MovementException e) {
            LOGGER.fatal("Failed to build NPCMovementManager: " + e.getMessage(), e);
            throw new MovementException("Failed to build NPCMovementManager: " + e.getMessage(), e);
        } catch (Exception e) {
            String msg = "Unexpected error building NPCMovementManager";
            LOGGER.fatal(msg, e);
            throw new MovementException(msg, e);
        }
    }

    @Override
    public NPCMovementBuilder setSpeed(float speed) {
        return super.setSpeed(speed);
    }

    @Override
    protected MovableEntity createMovableEntityFromEntity(Entity entity, float speed) {
        if (entity == null) {
            String errorMsg = "Cannot create MovableEntity: Entity is null";
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }

        return new NPCMovableEntity(entity, speed);
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

    /**
     * Concrete implementation of MovableEntity for NPC entities
     */
    private static class NPCMovableEntity extends MovableEntity {
        public NPCMovableEntity(Entity entity, float speed) {
            super(entity, speed);
        }
    }
}
