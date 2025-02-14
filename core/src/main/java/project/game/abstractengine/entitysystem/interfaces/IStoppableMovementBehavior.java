package project.game.abstractengine.entitysystem.interfaces;

import project.game.abstractengine.entitysystem.movementmanager.MovementData;

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

    void stopMovement(MovementData movementData, float deltaTime);

    void resumeMovement(MovementData movementData, float deltaTime);
}
