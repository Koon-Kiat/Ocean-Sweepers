package project.game.abstractengine.entity.movementmanager.interfaces;

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

    void setDeltaTime(float deltaTime);

    float getX();

    float getY();

    void updateMovement();

    void updateDirection(Set<Integer> pressedKey, Map<Integer, Direction> keyBindings);

}
