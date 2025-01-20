package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.input.InputManager;
import project.game.movement.Direction;
import project.game.movement.PlayerMovementManager;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture dropImage;
    private Rectangle drop;
    private PlayerMovementManager playerMovementManager;
    private InputManager inputManager;

    public static final float GAME_WIDTH = 640f;
    public static final float GAME_HEIGHT = 480f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        try {
            dropImage = new Texture(Gdx.files.internal("droplet.png"));
            System.out.println("[DEBUG] Loaded droplet.png successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load droplet.png: " + e.getMessage());
        }

        drop = new Rectangle();
        drop.x = 0;
        drop.y = 400;
        drop.width = dropImage.getWidth();
        drop.height = dropImage.getHeight();

        playerMovementManager = new PlayerMovementManager.Builder()
                .setX(drop.x)
                .setY(drop.y)
                .setSpeed(400)
                .setDirection(Direction.NONE)
                .build();
        inputManager = new InputManager(playerMovementManager);
        Gdx.input.setInputProcessor(inputManager);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        playerMovementManager.updatePosition();
        drop.x = (float) playerMovementManager.getX();
        drop.y = (float) playerMovementManager.getY();

        batch.begin();
        batch.draw(dropImage, drop.x, drop.y, drop.width, drop.height);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropImage.dispose();
    }
}
