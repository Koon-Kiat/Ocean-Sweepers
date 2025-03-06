package project.game.engine.io;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

/**
 * IOManager handles input events (keyboard and mouse) for the game.
 * It extends LibGDX's InputAdapter to provide basic behavior.
 */
public class IOManager extends InputAdapter {

    private final Vector2 mousePosition;
    private final Set<Integer> pressedKeys;
    private boolean isMouseClicked;

    public IOManager() {
        this.mousePosition = new Vector2();
        this.isMouseClicked = false;
        this.pressedKeys = new HashSet<>();
    }

    public Vector2 getMousePosition() {
        return mousePosition;
    }

    public boolean isMouseClicked() {
        return isMouseClicked;
    }

    public Set<Integer> getPressedKeys() {
        return pressedKeys;
    }

    public boolean isKeyJustPressed(int keycode) {
        return Gdx.input.isKeyJustPressed(keycode);
    }

    public void clearPressedKeys() {
        getPressedKeys().clear();
    }

    @Override
    public boolean keyDown(int keycode) {
        return pressedKeys.add(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return pressedKeys.remove(keycode);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        isMouseClicked = true;
        mousePosition.set(screenX, screenY);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        isMouseClicked = false;
        return true;
    }
}
