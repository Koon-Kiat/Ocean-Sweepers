package project.game.IOManager;

import com.badlogic.gdx.Input;

import project.game.MovementManager.Direction;
import project.game.MovementManager.interfaces.IMovementManager;

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
        if (super.keyDown(keycode)) {
            updateDirection();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (super.keyUp(keycode)) {
            updateDirection();
            return true;
        }
        return false;
    }

    private void updateDirection() {
        boolean up = pressedKeys.contains(Input.Keys.W);
        boolean down = pressedKeys.contains(Input.Keys.S);
        boolean left = pressedKeys.contains(Input.Keys.A);
        boolean right = pressedKeys.contains(Input.Keys.D);

        // Handle vertical direction
        if (up && down) {
            up = false;
            down = false;
        }

        // Handle horizontal direction
        if (left && right) {
            left = false;
            right = false;
        }

        Direction newDirection = Direction.NONE;

        if (up && right) {
            newDirection = Direction.UP_RIGHT;
        } else if (up && left) {
            newDirection = Direction.UP_LEFT;
        } else if (down && right) {
            newDirection = Direction.DOWN_RIGHT;
        } else if (down && left) {
            newDirection = Direction.DOWN_LEFT;
        } else if (up) {
            newDirection = Direction.UP;
        } else if (down) {
            newDirection = Direction.DOWN;
        } else if (left) {
            newDirection = Direction.LEFT;
        } else if (right) {
            newDirection = Direction.RIGHT;
        }

        movementManager.setDirection(newDirection);
        System.out.println("[DEBUG] Direction set to " + newDirection);
    }

    public IMovementManager getMovementManager() {
        return movementManager;
    }
}
