package project.game.movementmanager.interfaces;

import project.game.movementmanager.MovementManager;

/**
 * @interface IMovementBehavior
 * @brief Defines the contract for movement behaviors.
 *
 * Classes implementing this interface must provide an implementation for
 * updating the position of a MovementManager. This allows for flexible movement
 * patterns such as constant movement, accelerated movement, and zig-zag
 * movement.
 */
public interface IMovementBehavior {

    /**
     * Updates the position of the given MovementManager based on the specific
     * behavior.
     *
     * @param manager The MovementManager whose position needs to be updated.
     */
    void updatePosition(MovementManager manager);
}
