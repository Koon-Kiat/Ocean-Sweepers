package project.game.application.movement.factory;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import project.game.application.movement.strategy.AcceleratedMovementStrategy;
import project.game.application.movement.strategy.ConstantMovementStrategy;
import project.game.application.movement.strategy.FollowMovementStrategy;
import project.game.application.movement.strategy.InterceptorMovementStrategy;
import project.game.application.movement.strategy.ObstacleAvoidanceStrategy;
import project.game.application.movement.strategy.OrbitalMovementStrategy;
import project.game.application.movement.strategy.RandomisedMovementStrategy;
import project.game.application.movement.strategy.SpiralApproachStrategy;
import project.game.application.movement.strategy.SpringFollowStrategy;
import project.game.application.movement.strategy.ZigZagMovemenStrategy;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;
import project.game.engine.entitysystem.movement.api.IMovementStrategyFactory;
import project.game.engine.entitysystem.movement.api.IPositionable;
import project.game.engine.entitysystem.movement.strategy.CompositeMovementStrategy;

/**
 * Factory class for creating movement strategies.
 * This helps avoid direct instantiation of dependencies and follows the
 * Dependency Inversion Principle.
 */
public class MovementStrategyFactory implements IMovementStrategyFactory {

    private static final GameLogger LOGGER = new GameLogger(MovementStrategyFactory.class);
    private static final MovementStrategyFactory INSTANCE = new MovementStrategyFactory();

    /**
     * Gets the singleton instance of the factory.
     * 
     * @return The singleton instance
     */
    public static MovementStrategyFactory getInstance() {
        return INSTANCE;
    }

    // Constructor is now protected to allow subclassing but prevent arbitrary
    // instantiation
    protected MovementStrategyFactory() {
        // Protected constructor for singleton pattern and to allow subclassing
    }

    /**
     * Creates an accelerated movement strategy.
     * 
     * @param acceleration The acceleration of the movement.
     * @param deceleration The deceleration of the movement.
     * @param speed        The speed of the movement.
     * @param lenientMode  Whether to enable lenient mode.
     * @return A new AcceleratedMovementStrategy instance.
     */
    public static IMovementStrategy createAcceleratedMovement(float acceleration, float deceleration, float speed,
            boolean lenientMode) {
        try {
            return new AcceleratedMovementStrategy(acceleration, deceleration, speed, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating AcceleratedMovementStrategy: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a constant movement strategy.
     * 
     * @param speed The speed of the movement.
     * @return A new ConstantMovementStrategy instance.
     */
    public static IMovementStrategy createConstantMovement(float speed, boolean lenientMode) {
        try {
            return new ConstantMovementStrategy(speed, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating ConstantMovementStrategy: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a follow movement strategy.
     * 
     * @param target The target to follow.
     * @param speed  The speed of the movement.
     * @return A new FollowMovementStrategy instance.
     */
    public static IMovementStrategy createFollowMovement(IPositionable target, float speed, boolean lenientMode) {
        if (target == null) {
            String errorMsg = "Target is null in createFollowMovement.";
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            return new FollowMovementStrategy(target, speed, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating FollowMovementStrategy: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates an InterceptorMovementStrategy.
     */
    public static IMovementStrategy createInterceptorMovement(IMovable movable, float speed, boolean lenientMode) {
        try {
            return new InterceptorMovementStrategy(movable, speed, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create InterceptorMovementStrategy: " + e.getMessage());
            throw new MovementException("Failed to create InterceptorMovementStrategy", e);
        }
    }

    /**
     * Creates an ObstacleAvoidanceStrategy.
     */
    public static IMovementStrategy createObstacleAvoidanceStrategy(IMovable movable, float speed,
            boolean lenientMode) {
        try {
            ObstacleAvoidanceStrategy strategy = new ObstacleAvoidanceStrategy(speed, lenientMode);
            return strategy;
        } catch (Exception e) {
            LOGGER.error("Failed to create ObstacleAvoidanceStrategy: " + e.getMessage());
            throw new MovementException("Failed to create ObstacleAvoidanceStrategy", e);
        }
    }

    /**
     * Creates an ObstacleAvoidanceStrategy with a list of obstacles to avoid.
     * 
     * @param speed       The movement speed
     * @param obstacles   List of obstacle entities to avoid
     * @param lenientMode Whether to use lenient mode for error handling
     * @return A new ObstacleAvoidanceStrategy instance
     */
    public static IMovementStrategy createObstacleAvoidanceStrategy(float speed, List<Entity> obstacles,
            boolean lenientMode) {
        try {
            ObstacleAvoidanceStrategy strategy = new ObstacleAvoidanceStrategy(speed, lenientMode);
            if (obstacles != null && !obstacles.isEmpty()) {
                strategy.setObstacles(obstacles);
            }
            return strategy;
        } catch (Exception e) {
            LOGGER.error("Failed to create ObstacleAvoidanceStrategy with obstacles: " + e.getMessage());
            throw new MovementException("Failed to create ObstacleAvoidanceStrategy with obstacles", e);
        }
    }

    /**
     * Creates an OrbitalMovementStrategy.
     */
    public static IMovementStrategy createOrbitalMovement(IPositionable target, float orbitRadius, float rotationSpeed,
            float eccentricity, boolean lenientMode) {
        try {
            return new OrbitalMovementStrategy(target, orbitRadius, rotationSpeed, eccentricity, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create OrbitalMovementStrategy: " + e.getMessage());
            throw new MovementException("Failed to create OrbitalMovementStrategy", e);
        }
    }

    /**
     * Creates a randomised movement strategy.
     * 
     * @param strategyPool The pool of strategys to randomly select from.
     * @param minDuration  The minimum duration for each strategy.
     * @param maxDuration  The maximum duration for each strategy.
     * @return A new RandomisedMovementStrategy instance.
     */
    public static IMovementStrategy createRandomisedMovement(List<IMovementStrategy> strategyPool,
            float minDuration,
            float maxDuration, boolean lenientMode) {
        if (strategyPool == null) {
            String errorMsg = "Strategy pool cannot be null in createRandomisedMovement.";
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            return new RandomisedMovementStrategy(strategyPool, minDuration, maxDuration, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating RandomisedMovementStrategy: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a SpiralApproachStrategy.
     */
    public static IMovementStrategy createSpiralApproachMovement(IPositionable target, float speed,
            float spiralTightness,
            float approachSpeed, boolean lenientMode) {
        try {
            return new SpiralApproachStrategy(target, speed, spiralTightness, approachSpeed, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create SpiralApproachStrategy: " + e.getMessage());
            throw new MovementException("Failed to create SpiralApproachStrategy", e);
        }
    }

    /**
     * Creates a SpringFollowStrategy.
     */
    public static IMovementStrategy createSpringFollowMovement(IPositionable target, float springConstant,
            float damping,
            boolean lenientMode) {
        try {
            return new SpringFollowStrategy(target, springConstant, damping, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create SpringFollowStrategy: " + e.getMessage());
            throw new MovementException("Failed to create SpringFollowStrategy", e);
        }
    }

    /**
     * Creates a zig-zag movement strategy.
     * 
     * @param speed       The speed of the movement.
     * @param amplitude   The amplitude of the zigzag pattern.
     * @param frequency   The frequency of the zigzag pattern.
     * @param lenientMode Whether to enable lenient mode.
     * @return A new ZigZagMovementStrategy instance.
     */
    public static IMovementStrategy createZigZagMovement(float speed, float amplitude, float frequency,
            boolean lenientMode) {
        try {
            return new ZigZagMovemenStrategy(speed, amplitude, frequency, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating ZigZagMovementStrategy: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a composite movement strategy combining multiple movement strategies.
     * 
     * @param baseStrategy         The primary movement strategy
     * @param additionalStrategies Additional strategies to apply
     * @param weights              Weights for each strategy (including base
     *                             strategy)
     * @return A new CompositeMovementStrategy instance
     */
    public static IMovementStrategy createCompositeMovementStrategy(
            IMovementStrategy baseStrategy,
            List<IMovementStrategy> additionalStrategies,
            float[] weights) {

        if (baseStrategy == null) {
            LOGGER.error("Base strategy cannot be null for composite movement");
            throw new MovementException("Base strategy cannot be null for composite movement");
        }

        try {
            return new CompositeMovementStrategy(baseStrategy, additionalStrategies, weights);
        } catch (Exception e) {
            LOGGER.error("Failed to create CompositeMovementStrategy: " + e.getMessage());
            throw new MovementException("Failed to create CompositeMovementStrategy", e);
        }
    }

    /**
     * Creates a composite movement strategy combining interceptor movement with
     * obstacle avoidance.
     * This is a common use case where an entity follows a target while avoiding
     * obstacles.
     * 
     * @param target      The target to follow/intercept
     * @param obstacles   The obstacles to avoid
     * @param speed       The movement speed
     * @param weights     Optional custom weights for interceptor and avoidance
     *                    (first for interceptor, second for avoidance)
     * @param lenientMode Whether to use lenient mode
     * @return A CompositeMovementStrategy that combines interception and obstacle
     *         avoidance
     */
    public static IMovementStrategy createInterceptorWithObstacleAvoidance(
            IMovable target,
            List<Entity> obstacles,
            float speed,
            float[] weights,
            boolean lenientMode) {

        try {
            // Create the base interceptor strategy
            IMovementStrategy interceptor = createInterceptorMovement(target, speed, lenientMode);

            // Create obstacle avoidance strategy
            IMovementStrategy avoidance = createObstacleAvoidanceStrategy(speed, obstacles, lenientMode);

            // Use provided weights or default if not provided
            float[] finalWeights = weights;
            if (weights == null || weights.length != 2) {
                // Default weights: 60% interception, 40% avoidance
                finalWeights = new float[] { 0.6f, 0.4f };
                LOGGER.debug("Using default weights for interceptor with obstacle avoidance: {0}, {1}",
                        finalWeights[0], finalWeights[1]);
            }

            List<IMovementStrategy> additionalStrategies = new ArrayList<>();
            additionalStrategies.add(avoidance);

            return createCompositeMovementStrategy(interceptor, additionalStrategies, finalWeights);
        } catch (Exception e) {
            LOGGER.error("Failed to create interceptor with obstacle avoidance: " + e.getMessage());
            throw new MovementException("Failed to create interceptor with obstacle avoidance", e);
        }
    }

    /**
     * Creates a composite movement strategy combining interceptor movement with
     * obstacle avoidance using default weights.
     * 
     * @param target      The target to follow/intercept
     * @param obstacles   The obstacles to avoid
     * @param speed       The movement speed
     * @param lenientMode Whether to use lenient mode
     * @return A CompositeMovementStrategy that combines interception and obstacle
     *         avoidance
     */
    public static IMovementStrategy createInterceptorWithObstacleAvoidance(
            IMovable target,
            List<Entity> obstacles,
            float speed,
            boolean lenientMode) {
        return createInterceptorWithObstacleAvoidance(target, obstacles, speed, null, lenientMode);
    }

    /**
     * Creates an ocean current movement strategy combining constant and zigzag
     * movements
     * for realistic floating debris simulation.
     * 
     * @param baseSpeed      Speed for constant component
     * @param zigSpeed       Speed for zigzag component
     * @param amplitude      Zigzag amplitude
     * @param frequency      Zigzag frequency
     * @param constantWeight Weight for constant movement (0.0-1.0)
     * @param zigzagWeight   Weight for zigzag movement (0.0-1.0)
     * @param lenientMode    Whether to use lenient mode
     * @return A composite strategy that simulates ocean current effects
     */
    public static IMovementStrategy createOceanCurrentMovement(
            float baseSpeed, float zigSpeed, float amplitude, float frequency,
            float constantWeight, float zigzagWeight, boolean lenientMode) {

        try {
            // Create constant movement for directional flow
            IMovementStrategy constantMovement = createConstantMovement(baseSpeed, lenientMode);

            // Create zigzag movement for wave oscillation
            IMovementStrategy zigzagMovement = createZigZagMovement(zigSpeed, amplitude, frequency, lenientMode);

            // Combine them with specified weights
            List<IMovementStrategy> additionalStrategies = new ArrayList<>();
            additionalStrategies.add(zigzagMovement);
            float[] weights = new float[] { constantWeight, zigzagWeight };

            return createCompositeMovementStrategy(constantMovement, additionalStrategies, weights);
        } catch (Exception e) {
            LOGGER.error("Failed to create OceanCurrentMovement: " + e.getMessage());
            throw new MovementException("Failed to create OceanCurrentMovement", e);
        }
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
     * @param lenientMode    Whether to use lenient mode
     * @return A randomized ocean current movement strategy
     */
    public static IMovementStrategy createRandomizedOceanCurrentMovement(
            float minBaseSpeed, float maxBaseSpeed,
            float minZigSpeed, float maxZigSpeed,
            float minAmplitude, float maxAmplitude,
            float minFrequency, float maxFrequency,
            float constantWeight, float zigzagWeight,
            boolean lenientMode) {

        try {
            // Generate random parameters within specified ranges
            float baseSpeed = MathUtils.random(minBaseSpeed, maxBaseSpeed);
            float zigSpeed = MathUtils.random(minZigSpeed, maxZigSpeed);
            float amplitude = MathUtils.random(minAmplitude, maxAmplitude);
            float frequency = MathUtils.random(minFrequency, maxFrequency);

            // Create the ocean current movement with randomized parameters
            return createOceanCurrentMovement(
                    baseSpeed, zigSpeed, amplitude, frequency,
                    constantWeight, zigzagWeight, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create RandomizedOceanCurrentMovement: " + e.getMessage());
            throw new MovementException("Failed to create RandomizedOceanCurrentMovement", e);
        }
    }

    /**
     * Creates a default movement strategy when none is specified.
     * 
     * @return A new ConstantMovementStrategy with default speed.
     */
    @Override
    public IMovementStrategy createDefaultMovement() {
        return createConstantMovement(GameConstantsFactory.getConstants().DEFAULT_SPEED(), false);
    }
}