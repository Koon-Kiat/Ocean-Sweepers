package project.game.abstractengine.entitysystem.interfaces;

import java.util.Map;
import java.util.Set;

import project.game.Direction;

/**
 * @interface IMovementManager
 * @brief Defines the contract for movement managers.
 * 
 *        Classes implementing this interface must provide methods to update the
 *        direction and movement of an entity.
 */
public interface IMovementManager {

    void updateMovement();

    void updateDirection(Set<Integer> pressedKey, Map<Integer, Direction> keyBindings);

}