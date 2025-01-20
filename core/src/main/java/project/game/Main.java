package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.input.InputManager;
import project.game.movement.Direction;
import project.game.movement.EnemyMovementManager;
import project.game.movement.PlayerMovementManager;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private Rectangle drop;
    private Rectangle bucket;
    private PlayerMovementManager playerMovementManager;
    private EnemyMovementManager enemyMovementManager;
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

        try {
            bucketImage = new Texture(Gdx.files.internal("bucket.png"));
            System.out.println("[DEBUG] Loaded bucket.png successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load bucket.png: " + e.getMessage());
        }

        drop = new Rectangle();
        drop.x = 0;
        drop.y = 400;
        drop.width = dropImage.getWidth();
        drop.height = dropImage.getHeight();

        bucket = new Rectangle();
        bucket.x = 50;
        bucket.y = 400;
        bucket.width = bucketImage.getWidth();
        bucket.height = bucketImage.getHeight();

        playerMovementManager = new PlayerMovementManager.Builder()
                .setX(drop.x)
                .setY(drop.y)
                .setSpeed(1600f)
                .withAcceleratedMovement(300f, 1200f)
                .setDirection(Direction.NONE)
                .build();

        enemyMovementManager = new EnemyMovementManager.Builder()
                .setX(bucket.x)
                .setY(bucket.y)
                .setSpeed(100f)
                .setDirection(Direction.RIGHT)
                .withZigZagMovement(50f, 2f)
                .build();

        inputManager = new InputManager(playerMovementManager);
        Gdx.input.setInputProcessor(inputManager);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();

        // Set deltaTime for movement managers
        playerMovementManager.setDeltaTime(deltaTime);
        enemyMovementManager.setDeltaTime(deltaTime);

        // Update positions
        playerMovementManager.updatePosition();
        enemyMovementManager.updatePosition();

        // Update rectangle positions
        drop.x = playerMovementManager.getX();
        drop.y = playerMovementManager.getY();

        bucket.x = enemyMovementManager.getX();
        bucket.y = enemyMovementManager.getY();

        batch.begin();
        batch.draw(dropImage, drop.x, drop.y, drop.width, drop.height);
        batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropImage.dispose();
        bucketImage.dispose();
    }
}
