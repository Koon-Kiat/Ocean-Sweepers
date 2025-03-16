package project.game.engine.scene.management;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HealthManager {
    
    /*
     * This class is responsible for managing the health of the player.
     * ***Not under scene manager or a concrete scene class***
     */
    private static HealthManager instance;
    private final Texture heartTexture;
    private final int maxLives = 3;
    private int lives;

    public HealthManager() {
        this.lives = maxLives;
        this.heartTexture = new Texture("droplet.png");
    }

    public static HealthManager getInstance() {
        if (instance == null) {
            instance = new HealthManager();
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
        int xOffset = 20;
        int yOffset = 1000;
        for (int i = 0; i < lives; i++) {
            batch.draw(heartTexture, xOffset + (i * heartSpacing), yOffset);
        }
    }

    public int getLives() {
        return lives;
    }

    public void resetHealth() {
        this.lives = maxLives;
    }
}
