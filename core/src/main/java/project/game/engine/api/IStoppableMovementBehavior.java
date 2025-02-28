package project.game.engine.api;

import project.game.engine.entitysystem.entitymanager.MovableEntity;

/**
 * Interface for stoppable movement behaviors.
 * 
 * Classes implementing this interface must provide methods to stop and resume
 * movement behaviors.
 */
public interface IStoppableMovementBehavior extends IMovementBehavior {

    void stopMovement(MovableEntity entity, float deltaTime);

    void resumeMovement(MovableEntity entity, float deltaTime);
}
