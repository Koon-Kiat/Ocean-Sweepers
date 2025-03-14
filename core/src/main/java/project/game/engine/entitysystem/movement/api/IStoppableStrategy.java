package project.game.engine.entitysystem.movement.api;

/**
 * Interface for stoppable movement behaviors.
 * 
 * Classes implementing this interface must provide methods to stop and resume
 * movement behaviors.
 */
public interface IStoppableStrategy extends IMovementStrategy {

    void stopMovement(IMovable movable, float deltaTime);

    void resumeMovement(IMovable movable, float deltaTime);
}
