package project.game.userdefined;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import project.game.abstractengine.audiomanager.AudioManager;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.scenemanager.Scene;
import project.game.abstractengine.scenemanager.SceneManager;

public class MainMenuScene extends Scene {

    private static final Logger LOGGER = Logger.getLogger(MainMenuScene.class.getName());
    private Skin skin;
    private TextButton playButton, exitButton, optionsButton;
    private GameScene gameScene;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private AudioManager audioManager;

    public MainMenuScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(inputManager);
        this.sceneManager = sceneManager;
    }

    /**
     * @brief Creates the main menu scene.
     * 
     *        Initializes and draws the Main Menu Scene Play, Options, Exit buttons
     *        drawn here:
     *        - "PLAY" button moves to the game scene
     *        - "OPTIONS" button moves to the options menu scene
     *        - "EXIT" button closes
     * 
     *        First scene upon start up and can be returned from:
     *        Game Scene, Options Scene, Game Over Scene
     */
    @Override
    public void create() {
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(stage.getHeight(), stage.getWidth(), camera);
        stage.setViewport(viewport);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        playButton = new TextButton("PLAY", skin);
        optionsButton = new TextButton("OPTIONS", skin);

        Options options = new Options(sceneManager, gameScene, inputManager);
        options.create();
        options.setMainMenuButtonVisibility(false);
        exitButton = new TextButton("EXIT", skin);

        // AudioManager for sound effects and music
        audioManager = new AudioManager(stage);

        // Instead of checking clicks manually in render, add click listeners here:
        inputManager.addButtonClickListener(playButton, () -> {
            audioManager.playSoundEffect("selection");
            LOGGER.log(Level.INFO, "Start Game Clicked!");
            sceneManager.setScene("game");
        });

        inputManager.addButtonClickListener(optionsButton, () -> {
            audioManager.playSoundEffect("selection");
            LOGGER.log(Level.INFO, "Options Clicked!");
            sceneManager.setScene("options");

        });

        inputManager.addButtonClickListener(exitButton, () -> {
            audioManager.playSoundEffect("selection");
            System.out.println("Exit Clicked!"); // Debug log
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    Gdx.app.exit();
                    dispose();
                }
            }, 0.5f);
        });

        Table table = new Table();
        table.setFillParent(true);
        table.add(playButton).padBottom(10);
        table.row();
        table.add(optionsButton).padBottom(10);
        table.row();
        table.add(exitButton);

        stage.addActor(table);

    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        viewport.setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
