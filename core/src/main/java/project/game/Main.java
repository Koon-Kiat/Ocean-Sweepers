package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.common.logging.GameLogger;
import project.game.common.logging.LogInitializer;
import project.game.common.logging.LogLevel;
import project.game.common.util.ProjectPaths;
import project.game.context.factory.GameConstantsFactory;
import project.game.engine.io.SceneIOManager;
import project.game.engine.scene.SceneFactory;
import project.game.engine.scene.SceneManager;

public class Main extends ApplicationAdapter {

    private static final GameLogger LOGGER = new GameLogger(Main.class);
    private SceneManager sceneManager;

    @Override
    public void create() {

        LogInitializer.builder()
                .withDevMode()
                .withLogPrefix("MyGame")
                .withLogDirectory("logs")
                .withMaxLogFiles(5)
                .initialize();

        LOGGER.info("Application starting up");
        LOGGER.debug("Java version: %s", System.getProperty("java.version"));

        // Find the config file
        String configFile = ProjectPaths.findConfigFile("default-config.json");
        if (configFile == null) {
            LOGGER.error("Could not find default-config.json in any config locations");
            throw new RuntimeException("Missing required configuration file");
        }

        LOGGER.info("Loading game constants from: %s", configFile);

        try {
            GameConstantsFactory.initialize(configFile);
            LOGGER.info("Game constants loaded successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to load game constants", e);
            throw new RuntimeException("Failed to initialize game constants", e);
        }

        // Scene Manager setup
        LOGGER.debug("Creating scene manager");
        sceneManager = new SceneManager();
        SceneIOManager sharedInputManager = sceneManager.getInputManager();

        // Initializing and registering scenes now done in Scene Factory
        LOGGER.info("Initializing scene factory");
        SceneFactory sceneFactory = new SceneFactory(sceneManager, sharedInputManager);
        sceneFactory.createAndRegisterScenes();

        LOGGER.info("Available scenes: %s", sceneManager.getSceneList());
        sceneManager.setScene("menu");
        LOGGER.info("Current scene: %s", sceneManager.getCurrentScene());
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();
        sceneManager.render(deltaTime);

        // Only log this at TRACE level to avoid performance impact
        if (LOGGER.getLevel() == LogLevel.TRACE) {
            LOGGER.trace("Frame rendered, delta: %f", deltaTime);
        }
    }

    @Override
    public void resize(int width, int height) {
        LOGGER.debug("Resizing window to %d×%d", width, height);
        sceneManager.resize(width, height);
    }

    @Override
    public void dispose() {
        LOGGER.info("Application shutting down");
        sceneManager.dispose();

        // Ensure logging system is properly shut down
        LogInitializer.shutdown();
    }
}