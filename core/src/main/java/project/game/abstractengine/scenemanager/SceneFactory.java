package project.game.abstractengine.scenemanager;

import java.util.Map;
import java.util.function.Supplier;

import project.game.abstractengine.iomanager.SceneIOManager;

public class SceneFactory {
    private final SceneManager sceneManager;
    private final SceneIOManager inputManager;
    private final Map<String, Supplier<Scene>> sceneCreators;

    public SceneFactory(SceneManager sceneManager, SceneIOManager inputManager) {
        this.sceneManager = sceneManager;
        this.inputManager = inputManager;

        // Initializing scenes according to their names
        sceneCreators = Map.of(
            "menu", () -> new MainMenuScene(sceneManager, inputManager),
            "game", () -> new GameScene(sceneManager, inputManager),
            "options", () -> new OptionsScene(sceneManager, inputManager),
            "gameover", () -> new GameOverScene(sceneManager, inputManager)
        );
    }

    public void createAndRegisterScenes() {
        
        // Registering scenes to scene manager more efficiently
        sceneCreators.forEach((name, creator) -> {
            Scene scene = creator.get();
            sceneManager.addScene(name, scene);
            System.out.println("Registered scene: " + name);
        });
    }
}
