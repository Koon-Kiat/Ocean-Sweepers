package project.game.abstractengine.interfaces;

import java.util.Map;
import java.util.Set;

import project.game.Direction;

/**
 * Interface for movement managers.
 */
public interface IMovementManager {

    void updateMovement();

    void updateDirection(Set<Integer> pressedKey, Map<Integer, Direction> keyBindings);

}