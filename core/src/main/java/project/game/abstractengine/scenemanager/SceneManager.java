package project.game.abstractengine.scenemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import project.game.abstractengine.iomanager.SceneIOManager;

/**
 * @class SceneManager
 * @brief Manages the scenes of the game.
 * 
 *        The SceneManager class is responsible for managing different scenes in
 *        the game. It allows adding, removing, setting, and retrieving scenes,
 *        as well as rendering and resizing the current scene.
 */
public class SceneManager {
    private final Map<String, Scene> scenes;
    private final SceneIOManager baseInputManager;
    private Scene currentScene;

    public SceneManager() {
        baseInputManager = new SceneIOManager();
        this.scenes = new HashMap<>();
    }

    public void addScene(String name, Scene scene) {
        scenes.put(name, scene);
    }

    public void setScene(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("Scene '" + name + "' not found!");
        }

        if (currentScene != null) {
            currentScene.hide();
        }

        currentScene = scenes.get(name);
        currentScene.show();
    }

    public void removeScene(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("Scene '" + name + "' does not exist!");
        }

        Scene scene = scenes.remove(name);
        scene.dispose();
    }

    public Scene getScene(String name) {
        return currentScene;
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
        for (Scene scene : scenes.values()) {
            scene.dispose();
        }
        scenes.clear();
    }

    public Set<String> getSceneList() {
        return scenes.keySet();
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public SceneIOManager getInputManager() {
        return baseInputManager;
    }
}
