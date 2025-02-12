package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.abstractengine.entitymanager.Entity;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.movementmanager.NPCMovementManager;
import project.game.abstractengine.movementmanager.PlayerMovementManager;
import project.game.abstractengine.movementmanager.interfaces.IMovementManager;
import project.game.abstractengine.testentity.BucketEntity;
import project.game.builder.NPCMovementBuilder;
import project.game.builder.PlayerMovementBuilder;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private Rectangle drop;
    private BucketEntity bucket;
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private SceneIOManager inputManager;

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

//        bucket = new Rectangle();
//        bucket.x = 5;
//        bucket.y = 40;
//        bucket.width = bucketImage.getWidth();
//        bucket.height = bucketImage.getHeight();
        
        Entity genericBucketEntity = new Entity(5,40,bucketImage.getWidth(), bucketImage.getHeight(), true);

        playerMovementManager = new PlayerMovementBuilder()
                .setX(genericBucketEntity.getX())
                .setY(genericBucketEntity.getY())
                .setSpeed(1600f)
                .setDirection(Direction.NONE)
                .withConstantMovement()
                .build();
        
        bucket = new BucketEntity(genericBucketEntity, 1600f, playerMovementManager, "bucket.png");
        
        npcMovementManager = new NPCMovementBuilder()
                .setX(drop.x)
                .setY(drop.y)
                .setSpeed(200f)
                .withZigZagMovement(50f, 1f)
                .setDirection(Direction.RIGHT)
                .build();

        inputManager = new SceneIOManager(playerMovementManager);
        Gdx.input.setInputProcessor(inputManager);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();
        // Set deltaTime for movement managers
        playerMovementManager.setDeltaTime(deltaTime);
        npcMovementManager.setDeltaTime(deltaTime);

        // Update player's movement based on pressed keys
        playerMovementManager.updateDirection(inputManager.getPressedKeys());

        // Update positions based on new directions
        playerMovementManager.updateMovement();
        npcMovementManager.updateMovement();

        // Update rectangle positions so the bucket follows the playerMovement position
        bucket.setX(playerMovementManager.getX());
        bucket.setY(playerMovementManager.getY());
        drop.x = npcMovementManager.getX();
        drop.y = npcMovementManager.getY();

        batch.begin();
        
        batch.draw(dropImage, drop.x, drop.y, drop.width, drop.height);
        // batch.draw(bucketImage, bucket.getX(), bucket.getY(), bucket.getWidth(), bucket.getHeight());
        bucket.render(batch);
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

