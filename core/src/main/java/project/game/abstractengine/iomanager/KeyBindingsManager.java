package project.game.abstractengine.iomanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Input;

import project.game.Direction;

/**
 * Manages keyboard bindings for directional movement controls.
 * This class is separate from the main input manager to allow scenes
 * to opt-in to movement controls only when needed.
 */
public class KeyBindingsManager {
    private static final Logger LOGGER = Logger.getLogger(KeyBindingsManager.class.getName());
    
    // Map holding key codes mapped to their in-game Direction
    private Map<Integer, Direction> keyBindings;
    
    public KeyBindingsManager() {
        this.keyBindings = new HashMap<>();
        initializeDefaultKeyBindings();
    }
    
    // Set up initial key bindings using WASD
    public void initializeDefaultKeyBindings() {
        keyBindings.clear();
        keyBindings.put(Input.Keys.W, Direction.UP);
        keyBindings.put(Input.Keys.S, Direction.DOWN);
        keyBindings.put(Input.Keys.A, Direction.LEFT);
        keyBindings.put(Input.Keys.D, Direction.RIGHT);
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
                    LOGGER.log(Level.WARNING, "Invalid key string: {0}", keyString);
                    return Input.Keys.UNKNOWN;
                }
        }
    }
    
    // Getter for keyBindings map (for use by other systems)
    public Map<Integer, Direction> getKeyBindings() {
        return keyBindings;
    }
}