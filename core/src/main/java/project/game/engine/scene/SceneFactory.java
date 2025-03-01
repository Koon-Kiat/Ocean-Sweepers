package project.game.engine.scene;

import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

import project.game.common.api.ILogger;
import project.game.common.logging.LogManager;
import project.game.context.scene.GameOverScene;
import project.game.context.scene.GameScene;
import project.game.context.scene.MainMenuScene;
import project.game.context.scene.OptionsScene;
import project.game.engine.io.SceneIOManager;

/**
 * Factory class for creating and registering scenes.
 */
@SuppressWarnings("unused")
public class SceneFactory {

    private static final ILogger LOGGER = LogManager.getLogger(SceneFactory.class);
    private final SceneManager sceneManager;
    private final SceneIOManager inputManager;
    private final Map<String, Supplier<Scene>> sceneCreators;

    public SceneFactory(SceneManager sceneManager, SceneIOManager inputManager) {
        this.sceneManager = sceneManager;
        this.inputManager = inputManager;

        /**
         * Map of scene creators for each scene in the game.
         * 
         * The key is the name of the scene and the value is a lambda expression that
         * creates a new instance of the scene.
         */
        sceneCreators = Map.of(
                "menu", () -> new MainMenuScene(sceneManager, inputManager),
                "game", () -> new GameScene(sceneManager, inputManager),
                "options", () -> new OptionsScene(sceneManager, inputManager),
                "gameover", () -> new GameOverScene(sceneManager, inputManager));
    }

    public void createAndRegisterScenes() {

        /**
         * Create and register each scene in the game.
         */
        sceneCreators.forEach((name, creator) -> {
            Scene scene = creator.get();
            sceneManager.addScene(name, scene);
            LOGGER.log(Level.INFO, "Registered scene: {0}", name);
        });
    }
}