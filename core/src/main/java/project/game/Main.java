package project.game;

import java.util.logging.Level;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.common.api.ILogger;
import project.game.common.logging.GameLogFormatter;
import project.game.common.logging.LogManager;
import project.game.engine.io.SceneIOManager;
import project.game.engine.scene.SceneFactory;
import project.game.engine.scene.SceneManager;

public class Main extends ApplicationAdapter {

    static {
        new LogManager.Builder()
                .logFilePrefix("GameLog")
                .dateTimeFormat("yyyy-MM-dd_HH-mm-ss")
                .fileLogLevel(Level.ALL)
                .consoleLogLevel(Level.INFO)
                .maxLogFiles(5)
                .formatter(GameLogFormatter.class.getName())
                .initialize();
    }

    private static final ILogger LOGGER = LogManager.getLogger(Main.class);
    private SceneManager sceneManager;

    @Override
    public void create() {
        // Scene Manager setup
        sceneManager = new SceneManager();
        SceneIOManager sharedInputManager = sceneManager.getInputManager();

        // Initializing and registering scenes now done in Scene Factory
        SceneFactory sceneFactory = new SceneFactory(sceneManager, sharedInputManager);
        sceneFactory.createAndRegisterScenes();

        LOGGER.log(Level.INFO, "Available scenes: {0}", sceneManager.getSceneList());
        sceneManager.setScene("menu");
        LOGGER.log(Level.INFO, "Current scene: {0}", sceneManager.getCurrentScene());
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();
        sceneManager.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.resize(width, height);
    }
}