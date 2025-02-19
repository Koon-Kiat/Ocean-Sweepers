package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;

import project.game.abstractengine.audiomanager.AudioManager;
import project.game.abstractengine.iomanager.SceneIOManager;

public class MainMenuScene extends Scene {
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private Skin skin;
    private TextButton playButton, exitButton;
    private SceneManager sceneManager;
    private SceneIOManager inputManager;
    private GameScene gameScene;
    private AudioManager audioManager;

    public MainMenuScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    // Init UI elements
    @Override
    public void create() {
        inputManager = new SceneIOManager();
        audioManager = new AudioManager(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        playButton = new TextButton("PLAY", skin);
        // optionsButton = new TextButton("OPTIONS", skin); to add input
        exitButton = new TextButton("EXIT", skin);

        // Instead of checking clicks manually in render, add click listeners here:
        inputManager.addClickListener(playButton, () -> {
            audioManager.playSoundEffect("selection");
            System.out.println("Start Game Clicked!");
            sceneManager.setScene("game");
        });
        
        inputManager.addClickListener(exitButton, () -> {
            audioManager.playSoundEffect("selection");
            System.out.println("Exit Clicked!");
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    Gdx.app.exit();
                }
            }, 0.5f); // Delay before exit
        });

        Table table = new Table();
        table.setFillParent(true);

        table.add(playButton).padBottom(10);
        table.row();
        table.add(exitButton);

        stage.addActor(table);

    }

    //
    @Override
    public void show() {
        // backgroundTexture = new Texture("main_menu_background.png");
        // batch = new SpriteBatch();
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputManager); // Added first
        Gdx.input.setInputProcessor(multiplexer);

    }

    @Override
    public void render(float delta) {
        super.render(delta);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
