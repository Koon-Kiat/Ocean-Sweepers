package project.game.application.movement.composite;

import java.util.ArrayList;
import java.util.List;

import project.game.application.movement.strategy.InterceptorMovementStrategy;
import project.game.application.movement.strategy.ObstacleAvoidanceStrategy;
import project.game.common.exception.MovementException;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.api.ICompositeMovementStrategy;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;
import project.game.engine.entitysystem.movement.strategy.CompositeMovementStrategy;

/**
 * A specialized composite strategy that combines interceptor movement with
 * obstacle avoidance. This is useful for NPCs that need to chase targets
 * while avoiding obstacles in their path.
 */
public class InterceptorAvoidanceStrategy extends AbstractMovementStrategy {

    private final ICompositeMovementStrategy compositeStrategy;
    private final InterceptorMovementStrategy interceptorStrategy;
    private final ObstacleAvoidanceStrategy avoidanceStrategy;
    private final float interceptorWeight;
    private final float avoidanceWeight;

    /**
     * Creates a strategy that combines interceptor movement with obstacle
     * avoidance.
     * 
     * @param target      The target to intercept/follow
     * @param obstacles   The obstacles to avoid
     * @param speed       The movement speed
     * @param weights     Optional weights for interceptor and avoidance strategies
     * @param lenientMode Whether to use lenient mode
     */
    public InterceptorAvoidanceStrategy(
            IMovable target,
            List<Entity> obstacles,
            float speed,
            float[] weights,
            boolean lenientMode) {

        super(InterceptorAvoidanceStrategy.class, lenientMode);

        try {
            // Create the base interceptor strategy
            this.interceptorStrategy = new InterceptorMovementStrategy(target, speed, lenientMode);

            // Create obstacle avoidance strategy
            this.avoidanceStrategy = new ObstacleAvoidanceStrategy(speed, lenientMode);
            if (obstacles != null && !obstacles.isEmpty()) {
                this.avoidanceStrategy.setObstacles(obstacles);
            }

            // Set weights with defaults if not provided
            if (weights != null && weights.length >= 2) {
                this.interceptorWeight = weights[0];
                this.avoidanceWeight = weights[1];
            } else {
                // Default weights: 60% interception, 40% avoidance
                this.interceptorWeight = 0.6f;
                this.avoidanceWeight = 0.4f;
                logger.debug("Using default weights for interceptor with obstacle avoidance: {0}, {1}",
                        this.interceptorWeight, this.avoidanceWeight);
            }

            // Create composite strategy
            List<IMovementStrategy> additionalStrategies = new ArrayList<>();
            additionalStrategies.add(avoidanceStrategy);
            float[] strategyWeights = new float[] { interceptorWeight, avoidanceWeight };

            this.compositeStrategy = new CompositeMovementStrategy(
                    interceptorStrategy, additionalStrategies, strategyWeights);

            logger.info("InterceptorAvoidanceStrategy created with weights: interceptor={0}, avoidance={1}",
                    interceptorWeight, avoidanceWeight);

        } catch (Exception e) {
            String msg = "Failed to create InterceptorAvoidanceStrategy: " + e.getMessage();
            logger.error(msg);
            throw new MovementException(msg, e);
        }
    }

    /**
     * Updates the list of obstacles to avoid.
     * 
     * @param obstacles The new list of obstacle entities
     */
    public void updateObstacles(List<Entity> obstacles) {
        avoidanceStrategy.setObstacles(obstacles);
    }

    /**
     * Gets the interceptor strategy.
     * 
     * @return The interceptor strategy
     */
    public InterceptorMovementStrategy getInterceptorStrategy() {
        return interceptorStrategy;
    }

    /**
     * Gets the obstacle avoidance strategy.
     * 
     * @return The obstacle avoidance strategy
     */
    public ObstacleAvoidanceStrategy getAvoidanceStrategy() {
        return avoidanceStrategy;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            if (movable == null) {
                if (lenientMode) {
                    logger.warn("Entity is null in InterceptorAvoidanceStrategy.move; skipping movement");
                    return;
                } else {
                    throw new MovementException("Entity cannot be null in InterceptorAvoidanceStrategy");
                }
            }

            // Delegate to the composite strategy
            compositeStrategy.move(movable, deltaTime);

        } catch (MovementException e) {
            handleMovementException(e, "Error in InterceptorAvoidanceStrategy.move");
        } catch (Exception e) {
            handleMovementException(e, "Unexpected error in InterceptorAvoidanceStrategy.move");
        }
    }
}