package project.game.application.scene.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import project.game.application.scene.main.GameScene;
import project.game.application.scene.main.MainMenuScene;
import project.game.application.scene.overlay.GameOverScene;
import project.game.application.scene.overlay.OptionsScene;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.scene.IScene;
import project.game.engine.io.scene.SceneIOManager;
import project.game.engine.scene.core.SceneManager;

/**
 * Factory class for creating and registering scenes.
 */
@SuppressWarnings("unused")
public class SceneFactory {

    private static final GameLogger LOGGER = new GameLogger(SceneFactory.class);
    private final SceneManager sceneManager;
    private final SceneIOManager inputManager;
    private final Map<String, Supplier<IScene>> sceneCreators;

    public SceneFactory(SceneManager sceneManager, SceneIOManager inputManager) {
        this.sceneManager = sceneManager;
        this.inputManager = inputManager;

        /**
         * Map of scene creators for each scene in the game.
         * 
         * The key is the name of the scene and the value is a lambda expression that
         * creates a new instance of the scene.
         */
        sceneCreators = new HashMap<>();
        sceneCreators.put("menu", () -> new MainMenuScene(sceneManager, inputManager));
        sceneCreators.put("game", () -> new GameScene(sceneManager, inputManager));
        sceneCreators.put("options", () -> new OptionsScene(sceneManager, inputManager));
        sceneCreators.put("gameover", () -> new GameOverScene(sceneManager, inputManager));
    }

    public void createAndRegisterScenes() {

        /**
         * Create and register each scene in the game.
         */
        sceneCreators.forEach((name, creator) -> {
            IScene scene = creator.get();
            sceneManager.addScene(name, scene);
            LOGGER.info("Registered scene: " + name);
        });
    }
}