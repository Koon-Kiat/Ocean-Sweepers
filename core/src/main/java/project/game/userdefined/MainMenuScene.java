package project.game.userdefined;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import project.game.abstractengine.audiomanager.AudioConfig;
import project.game.abstractengine.audiomanager.AudioManager;
import project.game.abstractengine.audiomanager.AudioUIManager;
import project.game.abstractengine.audiomanager.MusicManager;
import project.game.abstractengine.audiomanager.SoundManager;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.scenemanager.Scene;
import project.game.abstractengine.scenemanager.SceneManager;

public class MainMenuScene extends Scene {

    private static final Logger LOGGER = Logger.getLogger(MainMenuScene.class.getName());
    private Skin skin;
    private TextButton playButton, exitButton, optionsButton;
    private GameScene gameScene;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private AudioManager audioManager;

    public MainMenuScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(inputManager);
        this.sceneManager = sceneManager;
    }

    /**
     * Initializes the main menu scene with Play, Options, and Exit buttons.
     * Sets up the viewport, UI skin, audio manager, and button click listeners.
     */
    @Override
    public void create() {
        stage = new Stage();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        stage.setViewport(viewport);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        playButton = new TextButton("PLAY", skin);
        optionsButton = new TextButton("OPTIONS", skin);

        Options options = new Options(sceneManager, gameScene, inputManager);
        options.create();
        options.setMainMenuButtonVisibility(false);
        exitButton = new TextButton("EXIT", skin);

        // Initialize AudioManager for sound effects and music.
        AudioConfig audioConfig = new AudioConfig();
        AudioUIManager audioUIManager = new AudioUIManager(null, audioConfig, stage);
        audioManager = AudioManager.getInstance(MusicManager.getInstance(), SoundManager.getInstance(), new AudioConfig(), new AudioUIManager(audioManager, new AudioConfig(), stage));
        // audioManager.playMusic("BackgroundMusic");

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
            LOGGER.log(Level.INFO, "Exit Clicked!");
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
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}