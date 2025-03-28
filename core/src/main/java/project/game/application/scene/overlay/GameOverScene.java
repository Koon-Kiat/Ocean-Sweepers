package project.game.application.scene.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.api.IScene;
import project.game.engine.scene.management.HealthManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;
import project.game.engine.scene.management.ScoreManager;

public class GameOverScene extends Scene {

    private final HealthManager healthManager;
    private final ScoreManager scoreManager;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture heartTexture; 
    private Texture backgroundTexture, winBackgroundTexture;
    private boolean nextLevelButtonAdded = false;
    private TextButton nextLevelButton;
    private TextButtonStyle textButtonStyle;

    public GameOverScene(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
        this.healthManager = HealthManager.getInstance(heartTexture);
        this.scoreManager = ScoreManager.getInstance();
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
        // batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        /*
         * Win condition logic
         */
        if (scoreManager.hasWon()) {
        
        //if (scoreManager.getScore() >= 500) {
            batch.draw(winBackgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            // Draw "You Win!" text with outline
            font.setColor(Color.BLACK);
            font.draw(batch, "You Win!", Gdx.graphics.getWidth() / 2f + 2, Gdx.graphics.getHeight() - 100 - 2, 0, Align.center, false);
            font.draw(batch, "You Win!", Gdx.graphics.getWidth() / 2f - 2, Gdx.graphics.getHeight() - 100 + 2, 0, Align.center, false);
            font.setColor(Color.WHITE);
            font.draw(batch, "You Win!", Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 100, 0, Align.center, false);
            
            //if (!nextLevelButtonAdded) {
            if (!nextLevelButtonAdded && "GameScene".equals(sceneManager.getPreviousScene())) {

                nextLevelButtonAdded = true; // Prevent multiple additions
    
                nextLevelButton = new TextButton("Next Level", textButtonStyle);
                nextLevelButton.setSize(200, 60);
                nextLevelButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 80);
    
                inputManager.addButtonClickListener(nextLevelButton, () -> {
                    resetGame();
                    sceneManager.setScene("game2");
                });
    
                sceneUIManager.getStage().addActor(nextLevelButton);

            } else if ("GameScene2".equals(sceneManager.getPreviousScene())) {
                nextLevelButtonAdded = false; // Reset for next level
                if (nextLevelButton != null) {
                    nextLevelButton.remove();
                    nextLevelButton = null;
                }
            }
        } else {
            batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            // Draw "Game Over" text with outline
            font.setColor(Color.BLACK);
            font.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2f + 2, Gdx.graphics.getHeight() - 100 - 2, 0, Align.center, false);
            font.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2f - 2, Gdx.graphics.getHeight() - 100 + 2, 0, Align.center, false);
            font.setColor(Color.WHITE);
            font.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 100, 0, Align.center, false);
        }
        
        // Draw score text with outline
        font.setColor(Color.BLACK);
        font.draw(batch, "Score: " + scoreManager.getScore(), Gdx.graphics.getWidth() / 2f + 2, Gdx.graphics.getHeight() - 130 - 2, 0, Align.center, false);
        font.draw(batch, "Score: " + scoreManager.getScore(), Gdx.graphics.getWidth() / 2f - 2, Gdx.graphics.getHeight() - 130 + 2, 0, Align.center, false);
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + scoreManager.getScore(), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 130, 0, Align.center, false);
        // font.draw(batch, String.format("Time: %02d:%02d", 
        //         timer.getMinutes(), timer.getSeconds()), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 160, 0, Align.center, false);
        batch.end();
        sceneUIManager.getStage().act(Gdx.graphics.getDeltaTime());
        sceneUIManager.getStage().draw();
    }

    @Override
    public void dispose() {
        sceneUIManager.getStage().dispose();
        batch.dispose();
        font.dispose();
        backgroundTexture.dispose(); // Don't forget to dispose the background texture
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
        backgroundTexture = new Texture(Gdx.files.internal("gameoverbackground.jpg"));
        winBackgroundTexture = new Texture(Gdx.files.internal("win.jpg"));
        // Load custom font
        font = new BitmapFont(Gdx.files.internal("upheaval.fnt"));
        // Make game over text larger
        font.getData().setScale(1.5f);

        // Create transparent drawable for button backgrounds
        Drawable transparentDrawable = createColorDrawable(new Color(0, 0, 0, 0));

        // Create button style with custom font
        textButtonStyle = new TextButtonStyle(
                transparentDrawable,
                transparentDrawable,
                transparentDrawable,
                font);

        // Set normal text color
        textButtonStyle.fontColor = Color.WHITE;

        // Set different colors for different button states
        textButtonStyle.overFontColor = Color.YELLOW;
        textButtonStyle.downFontColor = Color.GOLD;

        TextButton retryButton = new TextButton("Play again", textButtonStyle);
        retryButton.setSize(200, 60);
        retryButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f);

        inputManager.addButtonClickListener(retryButton, () -> {
            // First get the game scene by name and reset it
            resetGame();
            scoreManager.setWinState(false); // Reset win state
            // Then set the scene back to the game
            sceneManager.setScene("game");
        });

        TextButton exitButton = new TextButton("Exit", textButtonStyle);
        exitButton.setSize(200, 60);
        exitButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 80);
        inputManager.addButtonClickListener(exitButton, () -> {
            resetGame(); // Ensure game is still reset
            scoreManager.setWinState(false); // Reset win state
            sceneManager.setScene("menu");
        });

        Table table = new Table();
        table.setFillParent(true);
        sceneUIManager.getStage().addActor(table);

        sceneUIManager.getStage().addActor(retryButton);
        sceneUIManager.getStage().addActor(exitButton);

    }

    private void resetGame() {
        IScene gameScene = sceneManager.getScene("game");
        IScene gameScene2 = sceneManager.getScene("game2");
        if (gameScene != null && gameScene2 != null) {
            gameScene.resetScene();
            gameScene2.resetScene();
        }
        // Upon retry, reset the score and health
        scoreManager.resetScore();
        healthManager.resetHealth();
    }

    // Add this helper method for creating the transparent drawable
    private Drawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
