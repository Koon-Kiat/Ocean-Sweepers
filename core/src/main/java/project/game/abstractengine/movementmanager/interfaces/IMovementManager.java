package project.game.abstractengine.movementmanager.interfaces;

import java.util.Set;

/**
 * @interface IMovementManager
 * @brief Defines the contract for movement managers.
 * 
 *        Classes implementing this interface must provide methods to update the
 *        direction and movement of an entity.
 */
public interface IMovementManager {

    float getX();

    float getY();

    void updateDirection(Set<Integer> pressedKeys);

    void updateMovement();
}
