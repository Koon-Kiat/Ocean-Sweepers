package project.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.abstractengine.entity.movementmanager.interfaces.IMovementBehavior;
import project.game.abstractengine.entity.movementmanager.interfaces.IMovementManager;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.builder.NPCMovementBuilder;
import project.game.builder.PlayerMovementBuilder;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;
import project.game.logmanager.LogManager;

public class Main extends ApplicationAdapter {

    static {
        LogManager.initialize();
    }

    public static final float GAME_WIDTH = 1920;
    public static final float GAME_HEIGHT = 1080;
    private static final float PLAYER_SPEED = 1600f;
    private static final float NPC_SPEED = 500f;
    private static final float DROP_START_X = 0f;
    private static final float DROP_START_Y = 400f;
    private static final float BUCKET_START_X = 5f;
    private static final float BUCKET_START_Y = 40f;

    List<IMovementBehavior> behaviorPool = new ArrayList<>();
    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private Rectangle drop;
    private Rectangle bucket;
    private IMovementManager playerMovementManager;
    private IMovementManager npcMovementManager;
    private SceneIOManager inputManager;

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

        drop = new Rectangle(DROP_START_X, DROP_START_Y, dropImage.getWidth(), dropImage.getHeight());
        bucket = new Rectangle(BUCKET_START_X, BUCKET_START_Y, bucketImage.getWidth(), bucketImage.getHeight());

        playerMovementManager = new PlayerMovementBuilder()
                .setX(bucket.x)
                .setY(bucket.y)
                .setSpeed(PLAYER_SPEED)
                .withAcceleratedMovement(1000f, 1500f)
                .build();

        behaviorPool.add(new ConstantMovementBehavior(NPC_SPEED));
        behaviorPool.add(new ZigZagMovementBehavior(NPC_SPEED, 100f, 5f));
        behaviorPool.add(new FollowMovementBehavior(playerMovementManager, NPC_SPEED));

        npcMovementManager = new NPCMovementBuilder()
                .setX(drop.x)
                .setY(drop.y)
                .setSpeed(NPC_SPEED)
                .withRandomisedMovement(behaviorPool, 1f, 2f)
                .setDirection(Direction.RIGHT)
                .build();

        inputManager = new SceneIOManager(playerMovementManager);
        Gdx.input.setInputProcessor(inputManager);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);
        try {
            updateGame();
        } catch (Exception e) {
            System.err.println("[ERROR] Exception during game update: " + e.getMessage());
            Gdx.app.error("Main", "Exception during game update", e);
        }

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
            System.out.println("[DEBUG] Mouse is clicked at position: " +
                    inputManager.getMousePosition());
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

    private void updateGame() {
        // Update player's movement based on pressed keys
        playerMovementManager.updateDirection(inputManager.getPressedKeys());

        // Update movement; exceptions here will be logged and thrown upward
        playerMovementManager.updateMovement();
        npcMovementManager.updateMovement();

        // Synchronize rectangle positions with movement manager positions
        bucket.x = playerMovementManager.getX();
        bucket.y = playerMovementManager.getY();
        drop.x = npcMovementManager.getX();
        drop.y = npcMovementManager.getY();
    }
}
