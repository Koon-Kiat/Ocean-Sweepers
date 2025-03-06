package project.game.engine.api.movement;

import java.util.List;

/**
 * Interface for movement strategies that combine multiple other strategies.
 */
public interface ICompositeMovementStrategy extends IMovementStrategy {

    /**
     * Get all the strategies in this composition
     */
    List<IMovementStrategy> getAllStrategies();

    /**
     * Get the base strategy (first strategy in the list)
     */
    IMovementStrategy getBaseStrategy();

    /**
     * Get the weight for a given strategy index
     */
    float getWeight(int index);

    /**
     * Set the weight for a given strategy index
     */
    void setWeight(int index, float weight);

    /**
     * Add a strategy to the composition with the specified weight
     */
    ICompositeMovementStrategy addStrategy(IMovementStrategy strategy, float weight);

    /**
     * Remove a strategy from the composition by index
     */
    boolean removeStrategy(int index);

    /**
     * Remove a specific strategy from the composition
     */
    boolean removeStrategy(IMovementStrategy strategy);
}