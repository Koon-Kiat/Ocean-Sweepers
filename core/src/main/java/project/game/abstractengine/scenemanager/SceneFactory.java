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

public class SceneFactory {

    private static final Logger LOGGER = Logger.getLogger(SceneFactory.class.getName());
    private final SceneManager sceneManager;
    private final SceneIOManager inputManager;
    private final Map<String, Supplier<Scene>> sceneCreators;

    public SceneFactory(SceneManager sceneManager, SceneIOManager inputManager) {
        this.sceneManager = sceneManager;
        this.inputManager = inputManager;

        /**
         * @brief A map of scene creators.
         * 
         *        The sceneCreators map is a map of scene creators. Each entry in the
         *        map is a pair of a scene name and a scene creator. The scene creator
         *        is a lambda expression that creates a new instance of the scene.
         */
        sceneCreators = Map.of(
                "menu", () -> new MainMenuScene(sceneManager, inputManager),
                "game", () -> new GameScene(sceneManager, inputManager),
                "options", () -> new OptionsScene(sceneManager, inputManager),
                "gameover", () -> new GameOverScene(sceneManager, inputManager));
    }

    public void createAndRegisterScenes() {

        /**
         * @brief Iterates over the sceneCreators map and registers each scene with the
         *        scene manager.
         *
         *        For each entry in the sceneCreators map, create a new instance of the
         *        scene using the corresponding scene creator and add it to the scene
         *        manager.
         */
        sceneCreators.forEach((name, creator) -> {
            Scene scene = creator.get();
            sceneManager.addScene(name, scene);
            LOGGER.log(Level.INFO, "Registered scene: {0}", name);
        });
    }
}