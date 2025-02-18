package project.game.abstractengine.scenemanager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.Direction;
import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.entitymanager.EntityManager;
import project.game.abstractengine.entitysystem.interfaces.IMovementBehavior;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.testentity.BucketEntity;
import project.game.abstractengine.testentity.DropEntity;
import project.game.builder.NPCMovementBuilder;
import project.game.builder.PlayerMovementBuilder;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;
import project.game.logmanager.LogManager;

public class GameScene extends Scene {

    static {
        LogManager.initialize();
    }

    public static final float GAME_WIDTH = 640;
    public static final float GAME_HEIGHT = 480;

    private static final float PLAYER_SPEED = 1600f;
    private static final float NPC_SPEED = 400f;

    private static final float DROP_START_X = 0f;
    private static final float DROP_START_Y = 400f;
    private static final float BUCKET_START_X = 5f;
    private static final float BUCKET_START_Y = 40f;

    List<IMovementBehavior> behaviorPool = new ArrayList<>();

    private SceneManager sceneManager;
    private EntityManager entityManager;
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private DropEntity drop;
    private BucketEntity bucket;
    private Window popupMenu;
    private Skin skin;
    private Table table;
    private Options options;
    private boolean isMenuOpen = false, isRebindMenuOpen = false;
    private boolean isPaused = false;
    private InputMultiplexer inputMultiplexer;
    private MainMenuScene mainMenuScene;

    public GameScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        World world = new World(new Vector2(0, -9.8f), true);

        stage = new Stage();
        sceneManager = new SceneManager();
        System.out.println("Available scenes after init: " + sceneManager.getSceneList());

        options = new Options(sceneManager, this);

        options.setMainMenuButtonVisibility(true);
        options.getPopupMenu().setTouchable(Touchable.enabled);

        popupMenu = options.getPopupMenu();

        inputMultiplexer = new InputMultiplexer();
        // inputMultiplexer.addProcessor(inputManager);
        // inputMultiplexer.addProcessor(stage);

        // Add popup menu to the stage
        if (popupMenu != null) {
            float centerX = stage.getWidth() / 2f - popupMenu.getWidth() / 2f;
            float centerY = stage.getHeight() / 2f - popupMenu.getHeight() / 2f;
            popupMenu.setPosition(centerX, centerY);
        } else {
            Gdx.app.log("GameScene", "popupMenu is null");
        }

        stage.addActor(options.getPopupMenu());
        stage.addActor(options.getRebindMenu());

        // Add listener for interaction
        options.getPopupMenu().addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                System.out.println("[DEBUG] Popup Menu Key Pressed: " + Input.Keys.toString(keycode));
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("[DEBUG] Popup Menu Touched at: (" + x + ", " + y + ")");
                return true;
            }
        });

        // // Instead of checking clicks manually in render, add click listeners here:
        // inputManager.addClickListener(button1, () -> {
        // System.out.println("Rebind Keys Clicked!");
        // inputManager.promptForKeyBindings();
        // });

        // inputManager.addClickListener(button2, () -> {
        // System.out.println("Return to main menu Clicked!");
        // });

        // inputManager.addClickListener(button3, () -> {
        // System.out.println("Game Closed!");
        // Gdx.app.exit();
        // });

        // gameAsset = gameAsset.getInstance();
        entityManager = new EntityManager();
        try {
            GameAsset.getInstance().loadTextureAssets("droplet.png");
            GameAsset.getInstance().loadTextureAssets("bucket.png");
            GameAsset.getInstance().update(); // Update the asset manager
            GameAsset.getInstance().getAssetManager().finishLoading(); // Force loading to complete

            if (GameAsset.getInstance().isLoaded()) {
                dropImage = GameAsset.getInstance().getAsset("droplet.png", Texture.class);
                bucketImage = GameAsset.getInstance().getAsset("bucket.png", Texture.class);
            } else {
                System.err.println("[ERROR] Some assets not loaded yet!"); // More general message
            }

            System.out.println("[DEBUG] Loaded droplet.png successfully.");
            System.out.println("[DEBUG] Loaded bucket.png successfully.");

            // Check if textures are null after loading
            if (dropImage == null) {
                System.err.println("[ERROR] dropImage is null after loading!");
            }
            if (bucketImage == null) {
                System.err.println("[ERROR] bucketImage is null after loading!");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load assets: " + e.getMessage());
        }

        // Create entities
        Entity genericDropEntity = new Entity(DROP_START_X, DROP_START_Y, 50, 50, true);

        Entity genericBucketEntity = new Entity(BUCKET_START_X, BUCKET_START_Y, 50, 50, true);

        playerMovementManager = new PlayerMovementBuilder()
                .withEntity(genericBucketEntity)
                .setSpeed(PLAYER_SPEED)
                .setDirection(Direction.NONE)
                .withConstantMovement()
                .build();

        behaviorPool = new ArrayList<>();
        behaviorPool.add(new ConstantMovementBehavior(NPC_SPEED));
        behaviorPool.add(new ZigZagMovementBehavior(NPC_SPEED, 100f, 5f));
        behaviorPool.add(new FollowMovementBehavior(playerMovementManager, NPC_SPEED));

        npcMovementManager = new NPCMovementBuilder()
                .withEntity(genericDropEntity)
                .setSpeed(NPC_SPEED)
                .withRandomisedMovement(behaviorPool, 3, 4)
                .setDirection(Direction.RIGHT)
                .build();

        bucket = new BucketEntity(genericBucketEntity, world, playerMovementManager, "bucket.png");
        drop = new DropEntity(genericDropEntity, world, npcMovementManager, "droplet.png");

        entityManager.addEntity(bucket);
        entityManager.addEntity(drop);

    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage); // Stage first
        multiplexer.addProcessor(inputManager); // Then inputManager
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(0, 0, 0f, 0);

        input();

        // Print pressed keys for debugging
        for (int keycode : inputManager.getPressedKeys()) {
            System.out.println("[DEBUG] Pressed Key: " + Input.Keys.toString(keycode));
        }

        if (!isPaused) {
            try {
                updateGame();
                // Print pressed keys and their associated directions for debugging
                for (int keycode : inputManager.getPressedKeys()) {
                    Direction direction = inputManager.getKeyBindings().get(keycode);
                    if (direction != null) {
                        System.out.println(
                                "[DEBUG] Pressed Key: " + Input.Keys.toString(keycode) + ", Direction: " + direction);
                    } else {
                        System.out.println("[DEBUG] Pressed Key: " + Input.Keys.toString(keycode)
                                + ", Direction: No Direction Assigned");
                    }
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Exception during game update: " + e.getMessage());
                Gdx.app.error("Main", "Exception during game update", e);
            }
        }

        // Original code for update game ONLY
        // try {
        // updateGame();
        // } catch (Exception e) {
        // System.err.println("[ERROR] Exception during game update: " +
        // e.getMessage());
        // Gdx.app.error("Main", "Exception during game update", e);
        // }

        batch.begin();
        entityManager.draw(batch);
        batch.end();

    }

    private void input() {
        // Set the initial input processor to the inputManager
        // Update: using inputMultiplexer since we need to switch between inputManager and stage
        System.out.println("Available scenes: " + sceneManager.getSceneList());

        inputMultiplexer.addProcessor(inputManager);
        Gdx.input.setInputProcessor(inputMultiplexer);

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            sceneManager.setScene("menu");
        }
        
    }

    private void updateGame() {
        
        playerMovementManager.updateDirection(inputManager.getPressedKeys(), inputManager.getKeyBindings());

        // Update movement; exceptions here will be logged and thrown upward
        playerMovementManager.updateMovement();
        npcMovementManager.updateMovement();

        // Synchronize rectangle positions with movement manager positions
        bucket.setX(playerMovementManager.getX());
        bucket.setY(playerMovementManager.getY());
        drop.setX(npcMovementManager.getX());
        drop.setY(npcMovementManager.getY());
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropImage.dispose();
        bucketImage.dispose();
    }

    public void closePopupMenu() {
        isMenuOpen = false;
        isPaused = false;
        options.getPopupMenu().setVisible(false);
        inputMultiplexer.removeProcessor(stage);
        inputMultiplexer.addProcessor(inputManager);
        inputManager.clearPressedKeys(); // Clear the pressedKeys set
        System.out.println("[DEBUG] Popup closed and game unpaused");
    }

}