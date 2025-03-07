package project.game.engine.scene.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import project.game.engine.api.scene.IScene;
import project.game.engine.io.scene.SceneIOManager;

/**
 * Manages the scenes in the game.
 */
public class SceneManager {

    private final Map<String, IScene> scenes;
    private final Stack<IScene> sceneHistory;
    private final SceneIOManager baseInputManager;
    private IScene currentScene;

    public SceneManager() {
        baseInputManager = new SceneIOManager();
        this.scenes = new HashMap<>();
        this.sceneHistory = new Stack<>();
    }

    public IScene getScene(String name) {
        return currentScene;
    }

    public Set<String> getSceneList() {
        return scenes.keySet();
    }

    public IScene getCurrentScene() {
        return currentScene;
    }

    public SceneIOManager getInputManager() {
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
        }

        currentScene = scenes.get(name);
        currentScene.show();
    }

    public void returnToPreviousScene() {
        if (!sceneHistory.isEmpty()) {
            currentScene.hide();
            currentScene = sceneHistory.pop();
            currentScene.show();
        }
    }

    public void removeScene(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("Scene '" + name + "' does not exist!");
        }

        IScene scene = scenes.remove(name);
        scene.dispose();
    }

    public void render(float delta) {
        if (currentScene != null) {
            currentScene.render(delta);
        }
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

}
