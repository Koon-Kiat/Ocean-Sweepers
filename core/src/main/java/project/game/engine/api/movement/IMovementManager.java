package project.game.engine.api.movement;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;

/**
 * Interface for movement managers.
 * Extends IPositionable to ensure all movement managers have position
 * information.
 */
public interface IMovementManager extends IPositionable {

    void updateMovement();

    void updateVelocity(Set<Integer> pressedKeys, Map<Integer, Vector2> keyBindings);

}