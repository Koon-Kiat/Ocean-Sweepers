package project.game.abstractengine.movementmanager.interfaces;

import project.game.abstractengine.movementmanager.MovementData;

/**
 * @interface IMovementBehavior
 * @brief Defines the contract for movement behaviors.
 *
 *        Classes implementing this interface must provide an implementation for
 *        updating the position of a MovementManager. This allows for flexible
 *        movement patterns such as constant movement, accelerated movement, and
 *        zig-zag movement.
 */
public interface IMovementBehavior {

    void applyMovementBehavior(MovementData movementData, float deltaTime);
}
