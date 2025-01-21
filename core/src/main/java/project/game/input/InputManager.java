package project.game.input;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import project.game.movement.Direction;
import project.game.movement.IMovementManager;

/**
 * @class InputManager
 * @brief Handles user input for player movement.
 *
 * The InputManager class extends LibGDX's InputAdapter to capture and process
 * keyboard inputs. It manages the set of currently pressed movement keys and
 * updates the player's movement direction accordingly by interacting with the
 * IMovementManager.
 */
public class InputManager extends InputAdapter {

    private final IMovementManager movementManager;
    private final Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());

    /**
     * @brief Constructs an InputManager with the specified IMovementManager.
     *
     * @param movementManager Reference to the IMovementManager to update
     * directions.
     */
    public InputManager(IMovementManager movementManager) {
        if (movementManager == null) {
            throw new IllegalArgumentException("MovementManager cannot be null.");
        }
        this.movementManager = movementManager;
    }

    /**
     * @brief Called when a key is pressed.
     *
     * @param keycode The code of the key that was pressed.
     * @return True if the key event was handled, false otherwise.
     *
     * Adds the pressed key to the set of active keys and updates the movement
     * direction.
     */
    @Override
    public boolean keyDown(int keycode) {
        if (isMovementKey(keycode)) {
            if (pressedKeys.add(keycode)) {
                updateDirection();
            }
            return true;
        }
        return false;
    }

    /**
     * @brief Called when a key is released.
     *
     * @param keycode The code of the key that was released.
     * @return True if the key event was handled, false otherwise.
     *
     * Removes the released key from the set of active keys and updates the
     * movement direction.
     */
    @Override
    public boolean keyUp(int keycode) {
        if (isMovementKey(keycode)) {
            pressedKeys.remove(keycode);
            updateDirection();
            return true;
        }
        return false;
    }

    /**
     * @brief Determines if a keycode corresponds to a movement key.
     *
     * @param keycode The keycode to check.
     * @return True if the key is W, A, S, or D; false otherwise.
     */
    private boolean isMovementKey(int keycode) {
        return keycode == Input.Keys.W || keycode == Input.Keys.A
                || keycode == Input.Keys.S || keycode == Input.Keys.D;
    }

    /**
     * @brief Updates the player's movement direction based on the currently
     * pressed keys.
     *
     * Handles the cancellation of opposite key presses (W & S, A & D) to
     * determine the resulting movement direction.
     */
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
}
