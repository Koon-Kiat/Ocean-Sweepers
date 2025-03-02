package project.game.engine.io;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import project.game.common.logging.GameLogger;
import project.game.context.core.Direction;

/**
 * SceneIOManager handles input events (keyboard and mouse) for the game.
 * It extends IOManager to provide additional functionality for scene-based
 * input handling.
 */
public class SceneIOManager extends IOManager {

    private static final GameLogger LOGGER = new GameLogger(SceneIOManager.class);

    // Map holding key codes mapped to their in-game Direction
    private final Map<Integer, Direction> keyBindings;

    // Constructor: initialize default key bindings
    public SceneIOManager() {
        super();
        this.keyBindings = new HashMap<>();
        initializedefaultKeyBindings();
    }

    // Set up initial key bindings using WASD
    private void initializedefaultKeyBindings() {
        keyBindings.put(Input.Keys.W, Direction.UP);
        keyBindings.put(Input.Keys.S, Direction.DOWN);
        keyBindings.put(Input.Keys.A, Direction.LEFT);
        keyBindings.put(Input.Keys.D, Direction.RIGHT);
    }

    // Update keyBindings based on new strings provided (e.g., during key rebind)
    public void promptForKeyBindings(String upKeyString, String downKeyString, String leftKeyString,
            String rightKeyString) {
        keyBindings.clear();

        // Convert the key strings to key codes
        int upKey = getKeycodeFromString(upKeyString);
        int downKey = getKeycodeFromString(downKeyString);
        int leftKey = getKeycodeFromString(leftKeyString);
        int rightKey = getKeycodeFromString(rightKeyString);

        keyBindings.put(upKey, Direction.UP);
        keyBindings.put(downKey, Direction.DOWN);
        keyBindings.put(leftKey, Direction.LEFT);
        keyBindings.put(rightKey, Direction.RIGHT);
    }

    // Convert a key string (e.g., "W", "UP") to a LibGDX input key code
    private int getKeycodeFromString(String keyString) {
        // Ensure key string is uppercase
        keyString = keyString.toUpperCase();

        // Handle arrow keys explicitly
        switch (keyString) {
            case "UP":
                return Input.Keys.UP;
            case "DOWN":
                return Input.Keys.DOWN;
            case "LEFT":
                return Input.Keys.LEFT;
            case "RIGHT":
                return Input.Keys.RIGHT;
            default:
                try {
                    return Input.Keys.valueOf(keyString);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Invalid key string: {0}", keyString);
                    return Input.Keys.UNKNOWN;
                }
        }
    }

    // Getter for keyBindings map (for use by other systems)
    public Map<Integer, Direction> getKeyBindings() {
        return keyBindings;
    }

    // Attach a click listener to an Actor that calls the provided callback on
    // click.
    public void addButtonClickListener(Actor actor, Runnable callback) {
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.run();
            }
        });
    }

    // Attach a touchDown listener to an Actor that calls a TouchDownCallback.
    public void addWindowTouchDownListener(Actor actor, TouchDownCallback callback) {
        actor.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Optionally do something before calling the callback
                callback.onTouchDown(event, x, y, pointer, button);
                event.stop();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    // Functional interface for handling touchDown events via lambda expressions.
    @FunctionalInterface
    public interface TouchDownCallback {
        void onTouchDown(InputEvent event, float x, float y, int pointer, int button);
    }

}
