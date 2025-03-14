package project.game.engine.io.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

/**
 * Manages keyboard bindings for directional movement controls.
 * This class is separate from the main input manager to allow scenes
 * to opt-in to movement controls only when needed.
 */
public class KeyBindingsManager {

    private static final Logger LOGGER = Logger.getLogger(KeyBindingsManager.class.getName());
    private final Map<Integer, Vector2> keyBindings;

    public KeyBindingsManager() {
        this.keyBindings = new HashMap<>();
        initializeDefaultKeyBindings();
    }

    // Set up initial key bindings using WASD
    public final void initializeDefaultKeyBindings() {
        keyBindings.clear();
        // Cardinal directions (WASD only)
        keyBindings.put(Input.Keys.W, new Vector2(0, 1)); // Up
        keyBindings.put(Input.Keys.S, new Vector2(0, -1)); // Down
        keyBindings.put(Input.Keys.A, new Vector2(-1, 0)); // Left
        keyBindings.put(Input.Keys.D, new Vector2(1, 0)); // Right
    }

    // Getter for keyBindings map (for use by other systems)
    public Map<Integer, Vector2> getKeyBindings() {
        return keyBindings;
    }

    // Update keyBindings based on new strings provided (e.g., during key rebind)
    public void updateKeyBindings(String upKeyString, String downKeyString, String leftKeyString,
            String rightKeyString) {
        keyBindings.clear();

        // Convert the key strings to key codes
        int upKey = getKeycodeFromString(upKeyString);
        int downKey = getKeycodeFromString(downKeyString);
        int leftKey = getKeycodeFromString(leftKeyString);
        int rightKey = getKeycodeFromString(rightKeyString);

        keyBindings.put(upKey, new Vector2(0, 1)); // Up
        keyBindings.put(downKey, new Vector2(0, -1)); // Down
        keyBindings.put(leftKey, new Vector2(-1, 0)); // Left
        keyBindings.put(rightKey, new Vector2(1, 0)); // Right
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
                    LOGGER.log(Level.WARNING, "Invalid key string: {0}", keyString);
                    return Input.Keys.UNKNOWN;
                }
        }
    }
}