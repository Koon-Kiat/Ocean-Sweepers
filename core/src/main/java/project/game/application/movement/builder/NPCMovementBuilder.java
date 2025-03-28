package project.game.application.movement.builder;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import project.game.application.entity.item.Trash;
import project.game.application.movement.api.IMovementStrategyFactory;
import project.game.application.movement.api.StrategyType;
import project.game.common.exception.MovementException;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.core.MovableEntity;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;
import project.game.engine.entitysystem.movement.api.IPositionable;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;

/**
 * Builder class for creating NPCMovementManager objects.
 * Updated to use Vector2 instead of Direction.
 */
public class NPCMovementBuilder extends AbstractMovementBuilder<NPCMovementBuilder> {

    private IMovementStrategyFactory movementStrategyFactory;

    public NPCMovementBuilder(IMovementStrategyFactory factory) {
        this.movementStrategyFactory = factory;
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
            this.movementStrategy = this.movementStrategyFactory.createConstantMovement(this.speed, this.lenientMode);
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
            this.movementStrategy = this.movementStrategyFactory.createFollowMovement(target, this.speed,
                    this.lenientMode);
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

    public NPCMovementBuilder withInterceptorMovement(IMovable movable) {
        try {
            this.movementStrategy = this.movementStrategyFactory.createInterceptorMovement(movable, this.speed,
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

    public NPCMovementBuilder withObstacleAvoidance() {
        try {
            this.movementStrategy = this.movementStrategyFactory.createObstacleAvoidanceStrategy(this.speed, null,
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

    public NPCMovementBuilder withObstacleAvoidanceTargeting(IMovable target) {
        try {
            this.movementStrategy = this.movementStrategyFactory.createObstacleAvoidanceStrategy(target, this.speed,
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
            this.movementStrategy = this.movementStrategyFactory.createObstacleAvoidanceStrategy(this.speed, obstacles,
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

    public NPCMovementBuilder withOrbitalMovement(IPositionable target, float orbitRadius, float rotationSpeed,
            float eccentricity) {
        try {
            this.movementStrategy = this.movementStrategyFactory.createOrbitalMovement(target, orbitRadius,
                    rotationSpeed,
                    eccentricity, this.lenientMode);
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
            this.movementStrategy = this.movementStrategyFactory.createRandomisedMovement(strategyPool, minDuration,
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

    public NPCMovementBuilder withSpiralApproach(IPositionable target, float spiralTightness, float approachSpeed) {
        try {
            this.movementStrategy = this.movementStrategyFactory.createSpiralApproachMovement(target, this.speed,
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

    public NPCMovementBuilder withSpringFollow(IPositionable target, float springConstant, float damping) {
        try {
            this.movementStrategy = this.movementStrategyFactory.createSpringFollowMovement(target, springConstant,
                    damping,
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

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        try {
            this.movementStrategy = this.movementStrategyFactory.createZigZagMovement(this.speed, amplitude, frequency,
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
            this.movementStrategy = this.movementStrategyFactory.createCompositeMovementStrategy(
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
            this.movementStrategy = this.movementStrategyFactory.createInterceptorWithObstacleAvoidance(
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

    public NPCMovementBuilder withInterceptorAndObstacleAvoidance(IMovable target, List<Entity> obstacles,
            float[] weights) {
        try {
            this.movementStrategy = this.movementStrategyFactory.createInterceptorWithObstacleAvoidance(
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
     * Creates an ocean current movement strategy for simulating realistic floating
     * debris motion.
     * The movement combines constant directional flow with zigzag oscillation.
     * 
     * @param baseSpeed      Speed for constant directional movement
     * @param zigSpeed       Speed for zigzag oscillation component
     * @param amplitude      Amplitude of zigzag oscillation
     * @param frequency      Frequency of zigzag oscillation
     * @param constantWeight Weight for constant movement (0.0-1.0)
     * @param zigzagWeight   Weight for zigzag movement (0.0-1.0)
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withOceanCurrentMovement(
            float baseSpeed, float zigSpeed, float amplitude, float frequency,
            float constantWeight, float zigzagWeight) {

        try {
            this.movementStrategy = this.movementStrategyFactory.createOceanCurrentMovement(
                    baseSpeed, zigSpeed, amplitude, frequency,
                    constantWeight, zigzagWeight, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating OceanCurrentMovement in lenient mode: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in OceanCurrentMovement: " + e.getMessage(), e);
            throw new MovementException("Error in OceanCurrentMovement: " + e.getMessage(), e);
        }
        return this;
    }

    /**
     * Creates a randomized ocean current movement with parameters within specified
     * ranges.
     * 
     * @param minBaseSpeed   Minimum constant speed
     * @param maxBaseSpeed   Maximum constant speed
     * @param minZigSpeed    Minimum zigzag speed
     * @param maxZigSpeed    Maximum zigzag speed
     * @param minAmplitude   Minimum zigzag amplitude
     * @param maxAmplitude   Maximum zigzag amplitude
     * @param minFrequency   Minimum zigzag frequency
     * @param maxFrequency   Maximum zigzag frequency
     * @param constantWeight Weight for constant movement
     * @param zigzagWeight   Weight for zigzag movement
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withRandomizedOceanCurrentMovement(
            float minBaseSpeed, float maxBaseSpeed,
            float minZigSpeed, float maxZigSpeed,
            float minAmplitude, float maxAmplitude,
            float minFrequency, float maxFrequency,
            float constantWeight, float zigzagWeight) {

        try {
            this.movementStrategy = this.movementStrategyFactory.createRandomizedOceanCurrentMovement(
                    minBaseSpeed, maxBaseSpeed,
                    minZigSpeed, maxZigSpeed,
                    minAmplitude, maxAmplitude,
                    minFrequency, maxFrequency,
                    constantWeight, zigzagWeight,
                    this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating RandomizedOceanCurrentMovement in lenient mode: " + e.getMessage()
                        + ". Using constant movement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in RandomizedOceanCurrentMovement: " + e.getMessage(), e);
            throw new MovementException("Error in RandomizedOceanCurrentMovement: " + e.getMessage(), e);
        }
        return this;
    }

    /**
     * Creates a trash collector movement strategy for the entity.
     * This combines trash targeting with obstacle avoidance for intelligent trash
     * collection behavior.
     * 
     * @param trashEntities The list of trash entities to target
     * @param obstacles     The list of obstacles to avoid
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withTrashCollector(List<Trash> trashEntities, List<Entity> obstacles) {
        return withTrashCollector(trashEntities, obstacles, null);
    }

    /**
     * Creates a trash collector movement strategy for the entity with custom
     * weights.
     * This combines trash targeting with obstacle avoidance for intelligent trash
     * collection behavior.
     * 
     * @param trashEntities The list of trash entities to target
     * @param obstacles     The list of obstacles to avoid
     * @param weights       The weights for trash targeting and obstacle avoidance
     *                      [targetWeight, avoidanceWeight]
     * @return This builder for method chaining
     */
    public NPCMovementBuilder withTrashCollector(List<Trash> trashEntities, List<Entity> obstacles, float[] weights) {
        try {
            this.movementStrategy = this.movementStrategyFactory.createTrashCollectorStrategy(
                    this.speed, trashEntities, obstacles, weights, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating TrashCollectorStrategy: " + e.getMessage() +
                        ". Using constant movement fallback.");
                return withConstantMovement();
            }
            throw e;
        }
        return this;
    }

    public NPCMovementManager build() {
        try {
            validateBuildRequirements();
            if (this.entity == null && this.movable == null) {
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
                    && !this.movementStrategy.isStrategyType(StrategyType.FOLLOW)) {
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
                String errorMsg = "MovementStrategyFactory cannot be null";
                LOGGER.fatal(errorMsg);
                throw new MovementException(errorMsg);
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

    @Override
    protected IMovable createMovableFromEntity(Entity entity, float speed) {
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
