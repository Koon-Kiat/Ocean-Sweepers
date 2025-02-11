package project.game.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import project.game.iomanager.SceneIOManager;

public class MainMenuScene extends Scene {
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private Skin skin;
    private TextButton playButton, exitButton;
    private SceneManager sceneManager;
    private GameScene gameScene;

    private SceneIOManager inputManager;

    public MainMenuScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    // Init UI elements
    @Override
    public void create() {
        // Initialize SceneIOManager and set it as the input processor
        inputManager = new SceneIOManager();

        // Use an InputMultiplexer so that SceneIOManager gets input events first,
        // then the stage


        skin = new Skin(Gdx.files.internal("uiskin.json"));
        playButton = new TextButton("PLAY", skin);
        exitButton = new TextButton("EXIT", skin);

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
        // Gdx.input.setInputProcessor(stage);
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(inputManager); // Added first
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // Check for a mouse click via SceneIOManager.
        if (inputManager.isMouseClicked()) {
            // Get the click position in screen coordinates.
            Vector2 clickPosition = inputManager.getMousePosition();
            System.out.println("[DEBUG] Mouse is clicked at position: " + inputManager.getMousePosition());
            // Convert Y coordinate to stage space (origin at bottom left)
            clickPosition.y = Gdx.graphics.getHeight() - clickPosition.y;

            if (playButton.getX() <= clickPosition.x &&
                    clickPosition.x <= playButton.getX() + playButton.getWidth() &&
                    playButton.getY() <= clickPosition.y &&
                    clickPosition.y <= playButton.getY() + playButton.getHeight()) {
                System.out.println("Start Game Clicked!");
                sceneManager.setScene("game");
            } else if (exitButton.getX() <= clickPosition.x &&
                    clickPosition.x <= exitButton.getX() + exitButton.getWidth() &&
                    exitButton.getY() <= clickPosition.y &&
                    clickPosition.y <= exitButton.getY() + exitButton.getHeight()) {

                Gdx.app.exit(); // Exit the game
            }
        }

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
