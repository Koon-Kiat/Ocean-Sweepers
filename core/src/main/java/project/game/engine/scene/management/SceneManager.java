package project.game.engine.scene.management;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.api.IScene;

/**
 * Manages the scenes in the game.
 */
public class SceneManager {

    private final Map<String, IScene> scenes;
    private final Stack<IScene> sceneHistory;
    private final SceneInputManager baseInputManager;
    private IScene currentScene;
    private String previousScene = "";

    public SceneManager() {
        baseInputManager = new SceneInputManager();
        this.scenes = new HashMap<>();
        this.sceneHistory = new Stack<>();
    }

    // public IScene getScene(String name) {
    //     return currentScene;
    // }

    public IScene getScene(String name) {
        if (!scenes.containsKey(name)) {
            System.err.println("Scene not found: " + name);
            return null;
        }
        return scenes.get(name);
    }

    public SceneInputManager getInputManager() {
        return baseInputManager;
    }

    public void addScene(String name, IScene scene) {
        scenes.put(name, scene);
    }

    public void setScene(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("Scene '" + name + "' not found!");
        }

        if (currentScene != null) {
            currentScene.hide();
            sceneHistory.push(currentScene);
            previousScene = currentScene.getClass().getSimpleName();
        }

        currentScene = scenes.get(name);
        currentScene.show();
    }

    public String getPreviousScene() {
        return previousScene;
    }

    public void removeScene(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("Scene '" + name + "' does not exist!");
        }

        IScene scene = scenes.remove(name);
        scene.dispose();
    }

    public void resize(int width, int height) {
        if (currentScene != null) {
            currentScene.resize(width, height);
        }
    }

    public void dispose() {
        for (IScene scene : scenes.values()) {
            if (scene != null) {
                scene.dispose();
            }
        }
        scenes.clear();
    }

    public IScene getCurrentScene() {
        return currentScene;
    }
}
