package project.game.application.scene.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import project.game.application.scene.ui.AudioUI;
import project.game.common.logging.core.GameLogger;
import project.game.engine.audio.config.AudioConfig;
import project.game.engine.audio.management.AudioManager;
import project.game.engine.audio.music.MusicManager;
import project.game.engine.audio.sound.SoundManager;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;

public class MainMenuScene extends Scene {

    private static final GameLogger LOGGER = new GameLogger(MainMenuScene.class);
    private Skin skin;
    private TextButton playButton, exitButton;
    private GameScene gameScene;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AudioManager audioManager;
    private Stage stage;
    private AudioUI audioUI;
    private AudioConfig audioConfig;
    private boolean disposed = false;

    public MainMenuScene(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.input.setInputProcessor(sceneUIManager.getStage());
    }

    @Override
    public void show() {
    }

    @Override
    public void dispose() {
        if (!disposed) {
            sceneUIManager.getStage().dispose();
            skin.dispose();
            disposed = true;
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        viewport.setWorldSize(width, height);
        sceneUIManager.getStage().getViewport().update(width, height, true);
    }

    /**
     * Initializes the main menu scene with Play, and Exit buttons.
     * Sets up the viewport, UI skin, audio manager, and button click listeners.
     */
    @Override
    public void create() {
        stage = new Stage();
        this.camera = new OrthographicCamera();

        this.viewport = new FitViewport(sceneUIManager.getStage().getHeight(), sceneUIManager.getStage().getWidth(),
                camera);
        sceneUIManager.getStage().setViewport(viewport);

        // Load background texture
        Texture backgroundTexture = new Texture(Gdx.files.internal("mainmenu.jpg"));
        Image backgroundImage = new Image(backgroundTexture);

        // Make background fill the entire screen
        backgroundImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundImage.setPosition(0, 0);

        // Add background as the first actor (bottom layer)
        sceneUIManager.getStage().addActor(backgroundImage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        BitmapFont customFont = new BitmapFont(Gdx.files.internal("upheaval.fnt"));

        Drawable transparentDrawable = createColorDrawable(new Color(0, 0, 0, 0));
        
        // Create button style with custom font
        TextButtonStyle textButtonStyle = new TextButtonStyle(
                transparentDrawable,
                transparentDrawable,
                transparentDrawable,
                customFont);

        // Set normal text color
        textButtonStyle.fontColor = Color.WHITE;

        // Set different colors for different button states
        textButtonStyle.overFontColor = Color.YELLOW; // Color when hovering
        textButtonStyle.downFontColor = Color.GOLD; // Color when pressed

        Texture titleTexture = new Texture(Gdx.files.internal("gametitle.png"));
        Image titleImage = new Image(titleTexture);

        // Create buttons with the custom style
        playButton = new TextButton("PLAY", textButtonStyle);

        // Add padding to the buttons themselves
        playButton.pad(15); // Sets uniform padding on all sides
        exitButton = new TextButton("EXIT", textButtonStyle);

        exitButton.pad(15);

        audioConfig = new AudioConfig();
        audioManager = AudioManager.getInstance(MusicManager.getInstance(), SoundManager.getInstance(), audioConfig);
        audioUI = new AudioUI(audioManager, audioConfig, sceneUIManager.getStage(), skin);

        // Instead of checking clicks manually in render, add click listeners here:
        inputManager.addButtonClickListener(playButton, () -> {
            audioManager.playSoundEffect("selection");
            LOGGER.info("Start Game Clicked!");
            sceneManager.setScene("game");
            audioManager.playMusic("background");
        });

        inputManager.addButtonClickListener(exitButton, () -> {
            audioManager.playSoundEffect("selection");
            LOGGER.info("Exit Clicked!");
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

        table.add(titleImage).padBottom(20).padTop(50);
        table.row();

        table.add(playButton).padBottom(10);
        table.row();
        table.add(exitButton);

        sceneUIManager.getStage().addActor(table);

        LOGGER.info("Main Menu Scene sceneManager instance: {0}", System.identityHashCode(sceneManager));

    }

    // Add this helper method at the bottom of your MainMenuScene class
    private Drawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

}