package project.game.application.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import project.game.engine.api.scene.IScene;
import project.game.engine.io.SceneIOManager;
import project.game.engine.scene.Scene;
import project.game.engine.scene.SceneManager;

public class GameOverScene extends Scene {

    private SpriteBatch batch;
    private BitmapFont font;

    public GameOverScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(sceneManager, inputManager);
    }

    /**
     * Renders the Game Over scene by clearing the screen, drawing the "Game Over"
     * text, and updating the stage.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render(delta);
        Gdx.input.setInputProcessor(sceneUIManager.getStage());

        batch.begin();
        font.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 100, 0, Align.center,
                false);
        batch.end();
        sceneUIManager.getStage().act(Gdx.graphics.getDeltaTime());
        sceneUIManager.getStage().draw();
    }

    @Override
    public void dispose() {
        sceneUIManager.getStage().dispose();
        batch.dispose();
        font.dispose();

    }

    /**
     * Updates the viewport when the window is resized.
     */
    @Override
    public void resize(int width, int height) {
        sceneUIManager.getStage().getViewport().update(width, height, true);
    }

    /**
     * Initializes the Game Over scene by creating UI elements such as buttons and
     * tables.
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
            // First get the game scene by name and reset it
            IScene gameScene = sceneManager.getScene("game");
            if (gameScene != null) {
                gameScene.resetScene();
            }
            // Then switch to the game scene
            sceneManager.setScene("game");
        });

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.setSize(200, 60);
        exitButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 80);
        inputManager.addButtonClickListener(exitButton, () -> {
            sceneManager.setScene("menu");
        });

        Table table = new Table();
        table.setFillParent(true);
        sceneUIManager.getStage().addActor(table);

        sceneUIManager.getStage().addActor(retryButton);
        sceneUIManager.getStage().addActor(exitButton);

    }
}
