package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.abstractengine.scenemanager.GameOverScene;
import project.game.abstractengine.scenemanager.GameScene;
import project.game.abstractengine.scenemanager.MainMenuScene;
import project.game.abstractengine.scenemanager.OptionsScene;
import project.game.abstractengine.scenemanager.SceneManager;
import project.game.logmanager.LogManager;

public class Main extends ApplicationAdapter {

    static {
        LogManager.initialize();
    }

    public static final float GAME_WIDTH = 640;
    public static final float GAME_HEIGHT = 480;

    private SceneManager sceneManager;
    private MainMenuScene mainMenuScene;
    private GameScene gameScene;
    private OptionsScene optionsScene;
    private GameOverScene gameOverScene;

    @Override
    public void create() {
        // Scene Manager setup
        sceneManager = new SceneManager();
        mainMenuScene = new MainMenuScene(sceneManager);
        gameScene = new GameScene(sceneManager);
        optionsScene = new OptionsScene(sceneManager);
        gameOverScene = new GameOverScene();
        sceneManager.addScene("menu", mainMenuScene);
        sceneManager.addScene("game", gameScene);
        sceneManager.addScene("options", optionsScene); 
        sceneManager.addScene("gameover", gameOverScene);
        System.out.println("Available scenes: " + sceneManager.getSceneList());
        //sceneManager.setScene("menu");
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
