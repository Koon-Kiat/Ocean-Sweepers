package project.game.engine.scene.management;

import project.game.engine.scene.api.IScene;

/**
 * Handles rendering of the current scene.
 */
public class SceneRenderer {
    private final SceneManager sceneManager;

    public SceneRenderer(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void render(float delta) {
        IScene currentScene = sceneManager.getCurrentScene();
        if (currentScene != null) {
            currentScene.render(delta);
        }
    }
}