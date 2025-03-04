package project.game.abstractengine.scenemanager;

import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.userdefined.GameOverScene;
import project.game.userdefined.GameScene;
import project.game.userdefined.MainMenuScene;
import project.game.userdefined.OptionsScene;

/**
 * Factory class for creating and registering scenes.
 */
@SuppressWarnings("unused")
public class SceneFactory {

    private static final Logger LOGGER = Logger.getLogger(SceneFactory.class.getName());
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
            IScene scene = creator.get();
            sceneManager.addScene(name, scene);
            LOGGER.log(Level.INFO, "Registered scene: {0}", name);
        });
    }
}