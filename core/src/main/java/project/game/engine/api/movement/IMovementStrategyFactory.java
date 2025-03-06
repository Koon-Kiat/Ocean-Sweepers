package project.game.engine.api.movement;

/**
 * Interface defining a factory for creating movement strategies.
 * This follows the Dependency Inversion Principle by allowing MovementManager
 * to depend on an abstraction rather than a concrete implementation.
 */
public interface IMovementStrategyFactory {

    /**
     * Creates a default movement strategy when none is specified.
     * 
     * @return A default implementation of IMovementStrategy
     */
    IMovementStrategy createDefaultMovement();
}