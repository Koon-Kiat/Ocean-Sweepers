package project.game.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class MainMenuScene extends Scene {
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private Skin skin;
    private TextButton playButton, exitButton, optionsButton;
    private SceneManager sceneManager;
    private GameScene gameScene;
    private Options options;
    
    public MainMenuScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    // Init UI elements
    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        playButton = new TextButton("PLAY", skin); // Start moves to gamescene
        optionsButton = new TextButton("OPTIONS", skin); // Options moves to options menu scene
        options = new Options(sceneManager, gameScene);
        options.create();
        options.setMainMenuButtonVisibility(false);
        exitButton = new TextButton("EXIT", skin); // Exit closes game

        // Add button functionality
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Start Game Clicked!"); // Debug log
                //sceneManager.addScene("game", new GameScene());
                sceneManager.setScene("game"); // Switch to GameScene
            }
        });
        
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Options Clicked!"); // Debug log
                System.out.println("Popup Menu Visible: " + options.getPopupMenu().isVisible());
                options.getPopupMenu().setVisible(true);
                //sceneManager.addScene("options", new OptionsMenu());
                //options.getPopupMenu().setVisible(true);
                //sceneManager.setScene("options"); // Switch to OptionsMenu
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit(); // Close game
            }
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
        //backgroundTexture = new Texture("main_menu_background.png");
        //batch = new SpriteBatch();
        // Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        //Gdx.input.setInputProcessor(stage);
        if (options.getPopupMenu().isVisible()) {
            Gdx.input.setInputProcessor(options.getStage());
        } else if (options.getRebindMenu().isVisible()) {
            Gdx.input.setInputProcessor(options.getStage());
        }
        else {
            Gdx.input.setInputProcessor(stage);
        }

        super.render(delta);
        options.render();

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
