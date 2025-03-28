package project.game.engine.entitysystem.movement.api;

import project.game.application.movement.api.StrategyType;

/**
 * Interface for movement behaviors.
 */
public interface IMovementStrategy {

    /**
     * Gets the strategy type for this movement strategy.
     * 
     * @return The strategy type enum value
     */
    default StrategyType getStrategyType() {
        return StrategyType.UNKNOWN;
    }

    /**
     * Checks if the strategy is of a specified type.
     * 
     * @param type The strategy type to check for
     * @return true if this strategy is of the specified type
     */
    default boolean isStrategyType(StrategyType type) {
        return getStrategyType() == type;
    }

    /**
     * Apply movement to a movable entity.
     * 
     * @param movable   The entity to move
     * @param deltaTime The time elapsed since the last update
     */
    void move(IMovable movable, float deltaTime);
}
