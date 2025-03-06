package project.game.context.factory;

import java.util.ArrayList;
import java.util.List;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.context.movement.AcceleratedMovementStrategy;
import project.game.context.movement.ConstantMovementStrategy;
import project.game.context.movement.FollowMovementStrategy;
import project.game.context.movement.InterceptorMovementStrategy;
import project.game.context.movement.ObstacleAvoidanceStrategy;
import project.game.context.movement.OrbitalMovementStrategy;
import project.game.context.movement.RandomisedMovementStrategy;
import project.game.context.movement.SpiralApproachStrategy;
import project.game.context.movement.SpringFollowStrategy;
import project.game.context.movement.ZigZagMovemenStrategy;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.movement.CompositeMovementStrategy;

/**
 * Factory class for creating movement strategys.
 * This helps avoid direct instantiation of dependencies and follows the
 * Dependency Inversion Principle.
 */
public class MovementStrategyFactory {

    private static final GameLogger LOGGER = new GameLogger(MovementStrategyFactory.class);

    // Private constructor to prevent instantiation
    private MovementStrategyFactory() {
        throw new UnsupportedOperationException(
                "MovementStrategyFactory is a utility class and cannot be instantiated.");
    }

    /**
     * Creates a default movement strategy when none is specified.
     * 
     * @return A new ConstantMovementStrategy with default speed.
     */
    public static IMovementStrategy createDefaultMovement() {
        return createConstantMovement(GameConstantsFactory.getConstants().DEFAULT_SPEED(), false);
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
}