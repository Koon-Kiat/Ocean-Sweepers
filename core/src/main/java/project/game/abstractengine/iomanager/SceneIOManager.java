package project.game.abstractengine.iomanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.badlogic.gdx.Input;

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

    public void promptForKeyBindings() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter key for UP direction:");
        int upKey = Input.Keys.valueOf(scanner.nextLine().toUpperCase());
        System.out.println("Enter key for DOWN direction:");
        int downKey = Input.Keys.valueOf(scanner.nextLine().toUpperCase());
        System.out.println("Enter key for LEFT direction:");
        int leftKey = Input.Keys.valueOf(scanner.nextLine().toUpperCase());
        System.out.println("Enter key for RIGHT direction:");
        int rightKey = Input.Keys.valueOf(scanner.nextLine().toUpperCase());

        keyBindings.put(upKey, Direction.UP);
        keyBindings.put(downKey, Direction.DOWN);
        keyBindings.put(leftKey, Direction.LEFT);
        keyBindings.put(rightKey, Direction.RIGHT);
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

}
