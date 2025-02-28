package project.game;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.engine.iomanager.SceneIOManager;
import project.game.engine.scenemanager.SceneFactory;
import project.game.engine.scenemanager.SceneManager;
import project.game.logmanager.LogManager;

public class Main extends ApplicationAdapter {

    static {
        LogManager.initialize();
    }
    
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
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