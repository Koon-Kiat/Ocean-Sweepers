package project.game.abstractengine.iomanager;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import project.game.Direction;

public class SceneIOManager extends IOManager {

    

    private Map<Integer, Direction> keyBindings;

    

    public SceneIOManager() {
        super();
        this.keyBindings = new HashMap<>();
        initializedefaultKeyBindings();
    }

    private void initializedefaultKeyBindings() {
        keyBindings.put(Input.Keys.W, Direction.UP);
        keyBindings.put(Input.Keys.S, Direction.DOWN);
        keyBindings.put(Input.Keys.A, Direction.LEFT);
        keyBindings.put(Input.Keys.D, Direction.RIGHT);
    }

    // Corrected promptForKeyBindings method
    public void promptForKeyBindings(String upKeyString, String downKeyString, String leftKeyString, String rightKeyString) {
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

    private int getKeycodeFromString(String keyString) {
        try {
            return Input.Keys.valueOf(keyString);
        } catch (IllegalArgumentException e) {
            // Handle arrow keys
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
                    System.err.println("[ERROR] Invalid key string: " + keyString);
                    return Input.Keys.UNKNOWN; // Or a default key
            }
        }
    }

    public void clearPressedKeys() {
        pressedKeys.clear();
    }

    @Override
    public boolean keyDown(int keycode) {
        pressedKeys.add(keycode);
        // Update movement based on the new pressed keys
        // movementManager.updateDirection(pressedKeys, keyBindings);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        pressedKeys.remove(keycode);
        // Update movement based on the updated pressed keys
        // movementManager.updateDirection(pressedKeys, keyBindings);
        return true;
    }

    public Map<Integer, Direction> getKeyBindings() {
        return keyBindings;
    }

    public void addClickListener(TextButton button, Runnable callback) {
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.run();
            }
        });
    }

}
