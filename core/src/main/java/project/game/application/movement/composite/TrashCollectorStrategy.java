package project.game.application.movement.composite;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import project.game.application.entity.item.Trash;
import project.game.application.movement.api.StrategyType;
import project.game.application.movement.strategy.NearestTrashStrategy;
import project.game.application.movement.strategy.ObstacleAvoidanceStrategy;
import project.game.common.exception.MovementException;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.api.ICompositeMovementStrategy;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;
import project.game.engine.entitysystem.movement.strategy.CompositeMovementStrategy;

/**
 * A specialized movement strategy for entities that collect trash.
 * Combines trash targeting with obstacle avoidance for intelligent
 * trash collection behavior.
 */
public class TrashCollectorStrategy extends AbstractMovementStrategy {

    private final ICompositeMovementStrategy compositeStrategy;
    private final NearestTrashStrategy trashTargetingStrategy;
    private final ObstacleAvoidanceStrategy obstacleAvoidanceStrategy;
    private final float trashTargetWeight;
    private final float obstacleAvoidanceWeight;

    /**
     * Creates a new TrashCollectorStrategy.
     *
     * @param speed         The movement speed
     * @param trashEntities The list of trash entities to target
     * @param obstacles     The list of obstacles to avoid
     * @param weights       The weights for trash targeting and obstacle avoidance
     *                      [targetWeight, avoidanceWeight]
     * @param lenientMode   Whether to use lenient mode for error handling
     */
    public TrashCollectorStrategy(float speed, List<Trash> trashEntities, List<Entity> obstacles,
            float[] weights, boolean lenientMode) {
        super(TrashCollectorStrategy.class, lenientMode);

        // Create the trash targeting strategy
        this.trashTargetingStrategy = new NearestTrashStrategy(speed, trashEntities, lenientMode);

        // Create the obstacle avoidance strategy
        this.obstacleAvoidanceStrategy = new ObstacleAvoidanceStrategy(speed, lenientMode);
        if (obstacles != null) {
            this.obstacleAvoidanceStrategy.setObstacles(obstacles);
        }

        // Set the weights
        if (weights != null && weights.length >= 2) {
            this.trashTargetWeight = weights[0];
            this.obstacleAvoidanceWeight = weights[1];
        } else {
            // Default weights: 70% trash targeting, 30% obstacle avoidance
            this.trashTargetWeight = 0.3f;
            this.obstacleAvoidanceWeight = 0.7f;
        }

        // Create the composite strategy
        List<IMovementStrategy> additionalStrategies = new ArrayList<>();
        additionalStrategies.add(obstacleAvoidanceStrategy);
        float[] strategyWeights = new float[] { trashTargetWeight, obstacleAvoidanceWeight };

        this.compositeStrategy = new CompositeMovementStrategy(
                trashTargetingStrategy, additionalStrategies, strategyWeights);

        logger.info("TrashCollectorStrategy created with weights: target={0}, avoidance={1}",
                trashTargetWeight, obstacleAvoidanceWeight);
    }

    /**
     * Updates the list of trash entities to target.
     * 
     * @param trashEntities The new list of trash entities
     */
    public void updateTrashEntities(List<Trash> trashEntities) {
        trashTargetingStrategy.updateTrashEntities(trashEntities);
    }

    /**
     * Updates the list of obstacles to avoid.
     * 
     * @param obstacles The new list of obstacle entities
     */
    public void updateObstacles(List<Entity> obstacles) {
        obstacleAvoidanceStrategy.setObstacles(obstacles);
    }

    /**
     * Gets the current nearest trash targeting strategy.
     * 
     * @return The trash targeting strategy
     */
    public NearestTrashStrategy getTrashTargetingStrategy() {
        return trashTargetingStrategy;
    }

    /**
     * Gets the obstacle avoidance strategy.
     * 
     * @return The obstacle avoidance strategy
     */
    public ObstacleAvoidanceStrategy getObstacleAvoidanceStrategy() {
        return obstacleAvoidanceStrategy;
    }

    /**
     * Gets the strategy type for this movement strategy.
     * 
     * @return The strategy type enum value
     */
    @Override
    public StrategyType getStrategyType() {
        return StrategyType.TRASH_COLLECTOR;
    }

    /**
     * Move the entity using the composite strategy.
     */
    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            if (movable == null) {
                if (lenientMode) {
                    logger.warn("Entity is null in TrashCollectorStrategy.move; skipping movement");
                    return;
                } else {
                    throw new MovementException("Entity cannot be null in TrashCollectorStrategy");
                }
            }

            // Delegate to the composite strategy
            compositeStrategy.move(movable, deltaTime);

        } catch (MovementException e) {
            handleMovementException(e, "Error in TrashCollectorStrategy.move");
        } catch (Exception e) {
            handleMovementException(e, "Unexpected error in TrashCollectorStrategy.move");
        }
    }

    /**
     * Calculate a velocity vector based on the composite strategy components.
     * This is a utility method for getting the combined movement direction.
     * 
     * @param movable The movable entity
     * @return The calculated velocity vector
     */
    public Vector2 calculateVelocityVector(IMovable movable) {
        try {
            Vector2 velocity = new Vector2(0, 0);

            // Get weighted velocities from component strategies
            List<IMovementStrategy> strategies = compositeStrategy.getAllStrategies();

            // Base strategy is at index 0
            if (!strategies.isEmpty()) {
                // Handle the trash targeting strategy
                IMovementStrategy baseStrategy = compositeStrategy.getBaseStrategy();
                if (baseStrategy.isStrategyType(StrategyType.NEAREST_TRASH)) {
                    NearestTrashStrategy trashStrategy = (NearestTrashStrategy) baseStrategy;
                    Vector2 trashVelocity = trashStrategy.getVelocityVector(movable);
                    velocity.add(
                            trashVelocity.x * compositeStrategy.getWeight(0),
                            trashVelocity.y * compositeStrategy.getWeight(0));
                }

                // For additional strategies
                for (int i = 1; i < strategies.size(); i++) {
                    // We can't directly get avoidance vectors from ObstacleAvoidanceStrategy
                    // So we'll just contribute a small vector in the current movement direction
                    // to avoid compiler errors for now
                    float weight = compositeStrategy.getWeight(i);

                    if (movable.getVelocity().len2() > 0.001f) {
                        Vector2 currentDir = movable.getVelocity().cpy().nor();
                        velocity.add(
                                currentDir.x * weight * 0.3f,
                                currentDir.y * weight * 0.3f);
                    }
                }
            }

            return velocity;

        } catch (MovementException e) {
            if (lenientMode) {
                logger.error("Error in TrashCollectorStrategy.calculateVelocityVector: {0}", e.getMessage());
                return new Vector2(0, 0);
            } else {
                throw new MovementException("Error calculating velocity in TrashCollectorStrategy", e);
            }
        } catch (Exception e) {
            if (lenientMode) {
                logger.error("Unexpected error in TrashCollectorStrategy.calculateVelocityVector: {0}", e.getMessage());
                return new Vector2(0, 0);
            } else {
                throw new MovementException("Unexpected error calculating velocity in TrashCollectorStrategy", e);
            }
        }
    }
}