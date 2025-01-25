package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.IOManager.SceneIOManager;
import project.game.MovementManager.Direction;
import project.game.MovementManager.EnemyMovement;
import project.game.MovementManager.interfaces.IMovementManager;
import project.game.MovementManager.PlayerMovement;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private Rectangle drop;
    private Rectangle bucket;
    private PlayerMovement playerMovement;
    private EnemyMovement enemyMovement;
    private SceneIOManager inputManager;

    public static final float GAME_WIDTH = 640f;
    public static final float GAME_HEIGHT = 480f;

    @Override
    public void create() {
        IMovementManager bucketMovementManager = new PlayerMovement.Builder()
                .setX(50)
                .setY(400)
                .setSpeed(200f)
                .setDirection(Direction.NONE)
                .build();

        inputManager = new SceneIOManager(bucketMovementManager);
        Gdx.input.setInputProcessor(inputManager);

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
        bucket.x = bucketMovementManager.getX();
        bucket.y = bucketMovementManager.getY();
        bucket.width = bucketImage.getWidth();
        bucket.height = bucketImage.getHeight();

        playerMovement = new PlayerMovement.Builder()
                .setX(drop.x)
                .setY(drop.y)
                .setSpeed(1600f)
                .withConstantMovement()
                .setDirection(Direction.NONE)
                .build();

        enemyMovement = new EnemyMovement.Builder()
                .setX(bucket.x)
                .setY(bucket.y)
                .setSpeed(400f)
                .setDirection(Direction.RIGHT)
                .withRandomisedMovement(playerMovement, 50f, 2f, 1f, 2f)
                .build();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();

        // Set deltaTime for movement managers
        playerMovement.setDeltaTime(deltaTime);
        enemyMovement.setDeltaTime(deltaTime);

        // Update positions
        playerMovement.updatePosition();
        enemyMovement.updatePosition();

        // Update bucket position based on input
        inputManager.getMovementManager().setDeltaTime(deltaTime);
        inputManager.getMovementManager().updateMovement();

        // Update rectangle positions
        drop.x = enemyMovement.getX();
        drop.y = enemyMovement.getY();

        bucket.x = inputManager.getMovementManager().getX();
        bucket.y = inputManager.getMovementManager().getY();

        batch.begin();
        batch.draw(dropImage, drop.x, drop.y, drop.width, drop.height);
        batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        batch.end();

        // Print pressed keys
        for (Integer key : inputManager.getPressedKeys()) {
            System.out.println("[DEBUG] Key pressed: " + Input.Keys.toString(key));
        }

        // Print mouse click status
        if (inputManager.isMouseClicked()) {
            System.out.println("[DEBUG] Mouse is clicked at position: " + inputManager.getMousePosition());
        } else {
            System.out.println("[DEBUG] Mouse is not clicked.");
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropImage.dispose();
        bucketImage.dispose();
    }
}
