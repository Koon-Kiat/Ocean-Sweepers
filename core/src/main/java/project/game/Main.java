package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.scenemanager.SceneFactory;
import project.game.abstractengine.scenemanager.SceneManager;
import project.game.logmanager.LogManager;

public class Main extends ApplicationAdapter {

    static {
        LogManager.initialize();
    }

    public static final float GAME_WIDTH = 640;
    public static final float GAME_HEIGHT = 480;

    private SceneManager sceneManager;

    @Override
    public void create() {
        // Scene Manager setup
        sceneManager = new SceneManager();
        SceneIOManager sharedInputManager = sceneManager.getInputManager();

        // Initializing and registering scenes now done in Scene Factory
        SceneFactory sceneFactory = new SceneFactory(sceneManager,sharedInputManager);
        sceneFactory.createAndRegisterScenes();

        System.out.println("Available scenes: " + sceneManager.getSceneList());
        sceneManager.setScene("menu");
        System.out.println("[DEBUG] sceneManager in main: " + sceneManager);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();
        // Render current scene (Scene Manager)
        sceneManager.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.resize(width, height);
    }
}