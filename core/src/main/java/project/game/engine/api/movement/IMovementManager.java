package project.game.engine.api.movement;

import java.util.Map;
import java.util.Set;

import project.game.context.core.Direction;

/**
 * Interface for movement managers.
 * Extends IPositionable to ensure all movement managers have position
 * information.
 */
public interface IMovementManager extends IPositionable {

    void updateMovement();

    void updateDirection(Set<Integer> pressedKey, Map<Integer, Direction> keyBindings);

}