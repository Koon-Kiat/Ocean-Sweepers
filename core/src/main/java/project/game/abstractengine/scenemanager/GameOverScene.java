package project.game.abstractengine.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import project.game.abstractengine.iomanager.SceneIOManager;

public class GameOverScene extends Scene {
    private SpriteBatch batch;
    private BitmapFont font;

    // Provide an explicit constructor that calls the super constructor
    public GameOverScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(inputManager);
        this.sceneManager = sceneManager;
    }

    /*
     * Initializes and draws the Game Over Scene when player ends the game
     * 
     * "Game Over" text displayed, to generalize end of game state
     * 
     * Game over scene transitions to:
     * Game scene (Retry), Main menu scene (Exit)
     */
    @Override
    public void create() {
        batch = new SpriteBatch();

        // Font for Game Over text
        font = new BitmapFont();
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        TextButton retryButton = new TextButton("Play again", skin);
        retryButton.setSize(200, 60);
        retryButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f);
        inputManager.addButtonClickListener(retryButton, () -> {
            sceneManager.setScene("game");
            sceneManager.getScene(null).create(); // To restart game at its initial state
        });

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.setSize(200, 60);
        exitButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 80);
        inputManager.addButtonClickListener(exitButton, () -> {
            sceneManager.setScene("menu");
        });

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        stage.addActor(retryButton);
        stage.addActor(exitButton);

    }

    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render(delta);
        Gdx.input.setInputProcessor(stage);

        batch.begin();
        font.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 100, 0, Align.center,
                false);
        batch.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        font.dispose();
    }
}
