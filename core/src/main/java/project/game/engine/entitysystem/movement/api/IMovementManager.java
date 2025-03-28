package project.game.engine.entitysystem.movement.api;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;

/**
 * Interface for movement managers.
 */
public interface IMovementManager {

    IMovable getMovableEntity();

    void updateMovement();

    void updateVelocity(Set<Integer> pressedKeys, Map<Integer, Vector2> keyBindings);

}