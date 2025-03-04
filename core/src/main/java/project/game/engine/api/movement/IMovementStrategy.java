package project.game.engine.api.movement;

/**
 * Interface for movement behaviors.
 * 
 * Classes implementing this interface must provide methods to apply movement
 * behaviors to entities.
 */
public interface IMovementStrategy {

    void move(IMovable movable, float deltaTime);
}
