package project.game.engine.io.management;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import project.game.common.logging.core.GameLogger;
import project.game.engine.io.binding.KeyBindingsManager;

/**
 * SceneIOManager handles input events (keyboard and mouse) for the game.
 * It extends IOManager to provide additional functionality for scene-based
 * input handling.
 */
public class SceneInputManager extends InputManager {

    private static final GameLogger LOGGER = new GameLogger(SceneInputManager.class);

    // Map holding key codes mapped to their movement vectors
    private KeyBindingsManager keyBindingsManager;

    public SceneInputManager() {
        super();
    }

    public SceneInputManager(boolean withMovementControls) {
        super();
        if (withMovementControls) {
            enableMovementControls();
        }
    }

    // Enable movement controls for scenes that need them
    public final void enableMovementControls() {
        if (keyBindingsManager == null) {
            keyBindingsManager = new KeyBindingsManager();
        }
    }

    // Disable movement controls for scenes that don't need them
    public void disableMovementControls() {
        keyBindingsManager = null;
    }

    // Update key bindings (forwards to KeyBindingsManager if available)
    public void promptForKeyBindings(String upKeyString, String downKeyString, String leftKeyString,
            String rightKeyString) {
        if (keyBindingsManager != null) {
            keyBindingsManager.updateKeyBindings(upKeyString, downKeyString, leftKeyString, rightKeyString);
        } else {
            LOGGER.warn("Attempted to update key bindings but movement controls are disabled");
        }
    }

    // Get key bindings map (may return empty map if disabled)
    public Map<Integer, Vector2> getKeyBindings() {
        return keyBindingsManager != null ? keyBindingsManager.getKeyBindings() : new HashMap<>();
    }

    // Reset key bindings to defaults
    public void resetKeyBindingsToDefault() {
        if (keyBindingsManager != null) {
            keyBindingsManager.initializeDefaultKeyBindings();
        }
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
