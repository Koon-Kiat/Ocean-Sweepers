package project.game.abstractengine.entitysystem.interfaces;

import project.game.abstractengine.entitysystem.entitymanager.MovableEntity;

/**
 * @interface IStoppableMovementBehavior
 * @brief Defines the contract for movement behaviors that can be stopped and
 *        resumed.
 *
 *        Classes implementing this interface must provide methods to stop and
 *        resume
 *        the movement of an entity.
 */
public interface IStoppableMovementBehavior extends IMovementBehavior {

    void stopMovement(MovableEntity entity, float deltaTime);

    void resumeMovement(MovableEntity entity, float deltaTime);
}
