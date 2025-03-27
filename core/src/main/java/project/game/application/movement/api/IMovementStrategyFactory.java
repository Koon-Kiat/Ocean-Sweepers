package project.game.application.movement.api;

import java.util.List;

import project.game.application.entity.item.Trash;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;
import project.game.engine.entitysystem.movement.api.IPositionable;

/**
 * Interface defining a factory for creating movement strategies.
 */
public interface IMovementStrategyFactory {

        /**
         * Creates an accelerated movement strategy.
         */
        IMovementStrategy createAcceleratedMovement(float acceleration, float deceleration, float speed,
                        boolean lenientMode);

        /**
         * Creates a constant movement strategy.
         */
        IMovementStrategy createConstantMovement(float speed, boolean lenientMode);

        /**
         * Creates a follow movement strategy.
         */
        IMovementStrategy createFollowMovement(IPositionable target, float speed, boolean lenientMode);

        /**
         * Creates an InterceptorMovementStrategy.
         */
        IMovementStrategy createInterceptorMovement(IMovable movable, float speed, boolean lenientMode);

        /**
         * Creates an ObstacleAvoidanceStrategy for a specific target.
         */
        IMovementStrategy createObstacleAvoidanceStrategy(IMovable movable, float speed, boolean lenientMode);

        /**
         * Creates an ObstacleAvoidanceStrategy with a list of obstacles to avoid.
         */
        IMovementStrategy createObstacleAvoidanceStrategy(float speed, List<Entity> obstacles, boolean lenientMode);

        /**
         * Creates an OrbitalMovementStrategy.
         */
        IMovementStrategy createOrbitalMovement(IPositionable target, float orbitRadius, float rotationSpeed,
                        float eccentricity, boolean lenientMode);

        /**
         * Creates a randomised movement strategy.
         */
        IMovementStrategy createRandomisedMovement(List<IMovementStrategy> strategyPool, float minDuration,
                        float maxDuration, boolean lenientMode);

        /**
         * Creates a SpiralApproachStrategy.
         */
        IMovementStrategy createSpiralApproachMovement(IPositionable target, float speed, float spiralTightness,
                        float approachSpeed, boolean lenientMode);

        /**
         * Creates a SpringFollowStrategy.
         */
        IMovementStrategy createSpringFollowMovement(IPositionable target, float springConstant, float damping,
                        boolean lenientMode);

        /**
         * Creates a zig-zag movement strategy.
         */
        IMovementStrategy createZigZagMovement(float speed, float amplitude, float frequency, boolean lenientMode);

        /**
         * Creates a composite movement strategy combining multiple movement strategies.
         */
        IMovementStrategy createCompositeMovementStrategy(IMovementStrategy baseStrategy,
                        List<IMovementStrategy> additionalStrategies, float[] weights);

        /**
         * Creates a composite movement strategy combining interceptor movement with
         * obstacle avoidance.
         */
        IMovementStrategy createInterceptorWithObstacleAvoidance(IMovable target, List<Entity> obstacles,
                        float speed, float[] weights, boolean lenientMode);

        /**
         * Creates a composite movement strategy combining interceptor movement with
         * obstacle avoidance using default weights.
         */
        IMovementStrategy createInterceptorWithObstacleAvoidance(IMovable target, List<Entity> obstacles,
                        float speed, boolean lenientMode);

        /**
         * Creates an ocean current movement strategy combining constant and zigzag
         * movements.
         */
        IMovementStrategy createOceanCurrentMovement(float baseSpeed, float zigSpeed, float amplitude, float frequency,
                        float constantWeight, float zigzagWeight, boolean lenientMode);

        /**
         * Creates a randomized ocean current movement with parameters within specified
         * ranges.
         */
        IMovementStrategy createRandomizedOceanCurrentMovement(float minBaseSpeed, float maxBaseSpeed,
                        float minZigSpeed, float maxZigSpeed, float minAmplitude, float maxAmplitude,
                        float minFrequency, float maxFrequency, float constantWeight, float zigzagWeight,
                        boolean lenientMode);

        /**
         * Creates a movement strategy for targeting the nearest trash entity.
         */
        IMovementStrategy createNearestTrashStrategy(float speed, List<Trash> trashEntities, boolean lenientMode);

        /**
         * Creates a trash collector strategy that combines nearest trash targeting with
         * obstacle avoidance.
         */
        IMovementStrategy createTrashCollectorStrategy(float speed, List<Trash> trashEntities,
                        List<Entity> obstacles, float[] weights, boolean lenientMode);

        /**
         * Creates a trash collector strategy with default weights.
         */
        IMovementStrategy createTrashCollectorStrategy(float speed, List<Trash> trashEntities,
                        List<Entity> obstacles, boolean lenientMode);

        /**
         * Creates a default movement strategy when none is specified.
         * 
         * @return A default implementation of IMovementStrategy
         */
        IMovementStrategy createDefaultMovement();
}