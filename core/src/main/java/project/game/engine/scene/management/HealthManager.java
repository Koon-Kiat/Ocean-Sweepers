package project.game.engine.scene.management;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * This class is responsible for managing the health of the player.
 */
public class HealthManager {

    private static HealthManager instance;
    private final Texture heartTexture;
    private final int maxLives = 3;
    private int lives;

    public HealthManager(Texture heartTexture) {
        this.lives = maxLives;
        this.heartTexture = heartTexture;
    }

    public static HealthManager getInstance(Texture heartTexture) {
        if (instance == null) {
            instance = new HealthManager(heartTexture);
        }
        return instance;
    }

    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    public void dispose() {

    }

    public void draw(SpriteBatch batch) {
        int heartSpacing = 50;
        int xOffset = 25;
        int yOffset = Gdx.graphics.getHeight() - 75;

        // Define heart dimensions
        int heartWidth = 60;
        int heartHeight = 60;

        for (int i = 0; i < lives; i++) {
            batch.draw(heartTexture,
                    xOffset + (i * heartSpacing),
                    yOffset,
                    heartWidth,
                    heartHeight);
        }
    }

    public void draw(SpriteBatch batch, float x, float y, int health) {
        int heartSpacing = 50;

        // Define heart dimensions
        int heartWidth = 60;
        int heartHeight = 60;

        for (int i = 0; i < health; i++) {
            batch.draw(heartTexture,
                    x + (i * heartSpacing),
                    y,
                    heartWidth,
                    heartHeight);
        }
    }

    public int getLives() {
        return lives;
    }

    public void resetHealth() {
        this.lives = maxLives;
    }
}
