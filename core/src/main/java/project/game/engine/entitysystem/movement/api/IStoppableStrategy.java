package project.game.engine.entitysystem.movement.api;

/**
 * Interface for stoppable movement behaviors.
 */
public interface IStoppableStrategy extends IMovementStrategy {

    void stopMovement(IMovable movable, float deltaTime);

    void resumeMovement(IMovable movable, float deltaTime);
}
