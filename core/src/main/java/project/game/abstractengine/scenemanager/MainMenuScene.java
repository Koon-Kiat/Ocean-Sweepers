package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;

import project.game.abstractengine.iomanager.SceneIOManager;

public class MainMenuScene extends Scene {
    private Skin skin;
    private TextButton playButton, exitButton, optionsButton;
    private GameScene gameScene;

    private OrthographicCamera camera;
    private FitViewport viewport;

    public MainMenuScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(inputManager);
        this.sceneManager = sceneManager;
        create();
    }

    // Init UI elements
    @Override
    public void create() {
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(stage.getHeight(), stage.getWidth(), camera);
        stage.setViewport(viewport);


        skin = new Skin(Gdx.files.internal("uiskin.json"));
        playButton = new TextButton("PLAY", skin); // Start moves to gamescene
        optionsButton = new TextButton("OPTIONS", skin); // Options moves to options menu scene
        
        Options options = new Options(sceneManager, gameScene, inputManager);
        options.create();
        options.setMainMenuButtonVisibility(false);
        exitButton = new TextButton("EXIT", skin); // Exit closes game

        // Instead of checking clicks manually in render, add click listeners here:
        inputManager.addClickListener(playButton, () -> {
            System.out.println("Start Game Clicked!");
            sceneManager.setScene("game");
        });

        inputManager.addClickListener(optionsButton, () -> {
            System.out.println("Options Clicked!"); // Debug log
            sceneManager.setScene("options"); // Switch to OptionsScene

        });

        inputManager.addClickListener(exitButton, () -> {
            Gdx.app.exit(); // Close game
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

    //
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
