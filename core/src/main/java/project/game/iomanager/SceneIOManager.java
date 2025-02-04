package project.game.iomanager;

import com.badlogic.gdx.Input;

import project.game.movementmanager.Direction;
import project.game.movementmanager.interfaces.IMovementManager;

import java.util.HashMap;
import java.util.Map;


public class SceneIOManager extends IOManager {

    private IMovementManager movementManager;
    private Map<Integer, Direction> keyBindings;

    public SceneIOManager(IMovementManager movementManager) {
        super();
        if (movementManager == null) {
            throw new IllegalArgumentException("MovementManager cannot be null.");
        }
        this.movementManager = movementManager;
        this.keyBindings = new HashMap<>();
        initializeKeyBindings();
    }

    private void initializeKeyBindings() {
        keyBindings.put(Input.Keys.W, Direction.UP);
        keyBindings.put(Input.Keys.S, Direction.DOWN);
        keyBindings.put(Input.Keys.A, Direction.LEFT);
        keyBindings.put(Input.Keys.D, Direction.RIGHT);
    }

    @Override
    public boolean keyDown(int keycode) {
        pressedKeys.add(keycode);
        // Update movement based on the new pressed keys
        movementManager.updateDirection(pressedKeys);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        pressedKeys.remove(keycode);
        // Update movement based on the updated pressed keys
        movementManager.updateDirection(pressedKeys);
        return true;
    }

}
