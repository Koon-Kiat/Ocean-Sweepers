package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.application.scene.factory.SceneFactory;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.common.logging.util.LogPaths;
import project.game.common.util.file.ProjectPaths;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.SceneManager;
import project.game.engine.scene.management.SceneRenderer;

public class Main extends ApplicationAdapter {

    private static final GameLogger LOGGER = new GameLogger(Main.class);
    private SceneManager sceneManager;
    private SceneRenderer sceneRenderer;

    @Override
    public void create() {
        LOGGER.info("Application starting up");
        LOGGER.debug("Java version: {0}", System.getProperty("java.version"));
        LOGGER.debug("Using log directory: {0}", LogPaths.getGlobalLogDirectory());

        String projectRoot = LogPaths.getProjectRoot();
        String configFile = ProjectPaths.findConfigFile("default-config.json", projectRoot);

        if (configFile == null) {
            LOGGER.error("Could not find default-config.json in any config locations");
            throw new RuntimeException("Missing required configuration file");
        }

        LOGGER.info("Loading game constants from: {0}", configFile);

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
        sceneRenderer = new SceneRenderer(sceneManager);
        SceneInputManager sharedInputManager = sceneManager.getInputManager();

        // Initializing and registering scenes now done in Scene Factory
        LOGGER.info("Initializing scene factory");
        SceneFactory sceneFactory = new SceneFactory(sceneManager, sharedInputManager);
        sceneFactory.createAndRegisterScenes();

        sceneManager.setScene("menu");
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();
        sceneRenderer.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        LOGGER.debug("Resizing window to {0}x{1}", width, height);
        sceneManager.resize(width, height);
    }

    @Override
    public void dispose() {
        LOGGER.info("Application shutting down");
        if (sceneManager != null) {
            sceneManager.dispose();
            sceneManager = null;
        }

    }
}