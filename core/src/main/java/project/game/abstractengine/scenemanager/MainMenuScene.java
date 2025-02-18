package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class MainMenuScene extends Scene {
    private Skin skin;
    private TextButton playButton, exitButton, optionsButton;

    public MainMenuScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(inputManager);
        this.sceneManager = sceneManager;
        create();
    }

    // Init UI elements
    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        playButton = new TextButton("PLAY", skin); // Start moves to gamescene
        optionsButton = new TextButton("OPTIONS", skin); // Options moves to options menu scene
        
        options = new Options(sceneManager, gameScene, inputManager);
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

    // @Override
    // public void render(float delta) {
    //     //Gdx.input.setInputProcessor(stage);
    //     if (options.getPopupMenu().isVisible()) {
    //         Gdx.input.setInputProcessor(options.getStage());
    //     } else if (options.getRebindMenu().isVisible()) {
    //         Gdx.input.setInputProcessor(options.getStage());
    //     } else {
    //         InputMultiplexer multiplexer = new InputMultiplexer();
    //         multiplexer.addProcessor(stage);
    //         multiplexer.addProcessor(inputManager); // Added first
    //         Gdx.input.setInputProcessor(multiplexer);
    //     }
    // }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.input.setInputProcessor(stage);
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
