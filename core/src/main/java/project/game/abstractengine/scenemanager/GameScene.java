package project.game.abstractengine.scenemanager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.Direction;
import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.constants.GameConstants;
import project.game.abstractengine.entitysystem.collisionmanager.CollisionManager;
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
    private static final float PLAYER_SPEED = 600f;
    private static final float NPC_SPEED = 400f;
    private static final float DROP_START_X = 0f;
    private static final float DROP_START_Y = 0f;
    private static final float DROP_WIDTH = 50f;
    private static final float DROP_HEIGHT = 50f;
    private static final float BUCKET_START_X = 400f;
    private static final float BUCKET_START_Y = 400f;
    private static final float BUCKET_WIDTH = 50f;
    private static final float BUCKET_HEIGHT = 50f;
    List<IMovementBehavior> behaviorPool = new ArrayList<>();
    private SceneManager sceneManager;
    private EntityManager entityManager;
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private SceneIOManager inputManager;
    private TextButton button1, button2, button3;
    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private DropEntity drop;
    private BucketEntity bucket;
    private Window popupMenu;
    private Stage stage;
    private Skin skin;
    private Table table;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private Matrix4 debugMatrix;
    private CollisionManager collisionManager;

    // public GameScene() {
    // sceneManager = new SceneManager();
    // sceneManager.addScene("menu", new MainMenuScene());
    // sceneManager.setScene("menu");
    // }

    public GameScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        world = new World(new Vector2(0, 0), true);

        createScreenBoundaries();
        inputManager = new SceneIOManager();
        collisionManager = new CollisionManager(world);
        collisionManager.init();
        entityManager = new EntityManager();

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();
        table = new Table();

        popupMenu = new Window("Pop up", skin);
        popupMenu.setSize(200, 150);
        popupMenu.setPosition(300, 300);
        popupMenu.setVisible(false);

        button1 = new TextButton("Rebind Keys", skin);
        button2 = new TextButton("Return to main menu", skin);
        button3 = new TextButton("Close", skin);

        table = new Table();
        table.add(button1).fillX().pad(5);
        table.row();
        table.add(button2).fillX().pad(5);
        table.row();
        table.add(button3).fillX().pad(5);

        popupMenu.add(table);
        stage.addActor(popupMenu);

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
        Entity genericDropEntity = new Entity(DROP_START_X, DROP_START_Y, DROP_WIDTH, DROP_HEIGHT, true);
        Entity genericBucketEntity = new Entity(BUCKET_START_X, BUCKET_START_Y, BUCKET_WIDTH, BUCKET_HEIGHT, true);

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
                .withFollowMovement(playerMovementManager)
                .setDirection(Direction.RIGHT)
                .build();

        bucket = new BucketEntity(genericBucketEntity, world, playerMovementManager, "bucket.png");
        drop = new DropEntity(genericDropEntity, world, npcMovementManager, "droplet.png");

        entityManager.addRenderableEntity(bucket);
        entityManager.addRenderableEntity(drop);

        // Instead of checking clicks manually in render, add click listeners here:
        inputManager.addClickListener(button1, () -> {
            System.out.println("Rebind Keys Clicked!");
            inputManager.promptForKeyBindings();
        });

        inputManager.addClickListener(button2, () -> {
            System.out.println("Return to main menu Clicked!");
        });

        inputManager.addClickListener(button3, () -> {
            System.out.println("Game Closed!");
            Gdx.app.exit();
        });

        camera = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT);
        camera.position.set(GAME_WIDTH / 2, GAME_HEIGHT / 2, 0);
        camera.update();

        debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputManager);
        Gdx.input.setInputProcessor(multiplexer);

        float centerX = stage.getWidth() / 2f - popupMenu.getWidth() / 2f;
        float centerY = stage.getHeight() / 2f - popupMenu.getHeight() / 2f;
        popupMenu.setPosition(centerX, centerY);
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(0, 0, 0f, 0);

        try {
            updateGame();
        } catch (Exception e) {
            System.err.println("[ERROR] Exception during game update: " + e.getMessage());
            Gdx.app.error("Main", "Exception during game update", e);
        }

        batch.begin();
        entityManager.draw(batch);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            popupMenu.setVisible(!popupMenu.isVisible());
        }

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        debugMatrix = camera.combined.cpy().scl(GameConstants.PIXELS_TO_METERS);
        debugRenderer.render(world, debugMatrix);

        // Fixed timestep for Box2D
        float timeStep = 1 / 60f;
        world.step(timeStep, 6, 2);
        collisionManager.processCollisions();
        syncEntityPositions();
    }

    private void updateGame() {
        // Update movement managers (input processing, etc.)
        playerMovementManager.updateDirection(inputManager.getPressedKeys(), inputManager.getKeyBindings());
        playerMovementManager.updateMovement();
        npcMovementManager.updateMovement();

        // Always update player (bucket) from input, regardless of collision state
        float bucketX = playerMovementManager.getX();
        float bucketY = playerMovementManager.getY();

        // Clamp player positions so the player remains within screen bounds
        bucketX = Math.max(0, Math.min(bucketX, GAME_WIDTH - bucket.getEntity().getWidth()));
        bucketY = Math.max(0, Math.min(bucketY, GAME_HEIGHT - bucket.getEntity().getHeight()));

        // Update player's entity and Box2D body (convert pixels → meters)
        bucket.getEntity().setX(bucketX);
        bucket.getEntity().setY(bucketY);
        bucket.getBody().setTransform(bucketX / GameConstants.PIXELS_TO_METERS,
                bucketY / GameConstants.PIXELS_TO_METERS, 0);

        // For the NPC (drop), check if it's in collision and blend if needed
        if (!drop.isInCollision()) {
            // Normal update when no collision is active for the NPC
            float dropX = npcMovementManager.getX();
            float dropY = npcMovementManager.getY();

            // Clamp NPC positions
            dropX = Math.max(0, Math.min(dropX, GAME_WIDTH - drop.getEntity().getWidth()));
            dropY = Math.max(0, Math.min(dropY, GAME_HEIGHT - drop.getEntity().getHeight()));

            drop.getEntity().setX(dropX);
            drop.getEntity().setY(dropY);
            drop.getBody().setTransform(dropX / GameConstants.PIXELS_TO_METERS,
                    dropY / GameConstants.PIXELS_TO_METERS, 0);
        } else {
            // COLLISION MODE for NPC:
            // Get current physics position (in pixels)
            float physicsDropX = drop.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS;
            float physicsDropY = drop.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS;

            // Retrieve desired input position from the movement manager
            float inputDropX = npcMovementManager.getX();
            float inputDropY = npcMovementManager.getY();

            // Blend physics with input using a blending factor
            float blendFactor = 0.1f; // adjust as needed
            float newDropX = physicsDropX + (inputDropX - physicsDropX) * blendFactor;
            float newDropY = physicsDropY + (inputDropY - physicsDropY) * blendFactor;

            // Update NPC's entity to the blended value and synchronize the movement manager
            // so stale input does not accumulate
            drop.getEntity().setX(newDropX);
            drop.getEntity().setY(newDropY);
            npcMovementManager.setX(newDropX);
            npcMovementManager.setY(newDropY);
        }
    }

    /**
     * Syncs visual entity positions from Box2D bodies (convert meters → pixels)
     */
    private void syncEntityPositions() {
        bucket.getEntity().setX(bucket.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS);
        bucket.getEntity().setY(bucket.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS);
        drop.getEntity().setX(drop.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS);
        drop.getEntity().setY(drop.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS);
    }

    private void createScreenBoundaries() {
        float screenWidth = GAME_WIDTH / GameConstants.PIXELS_TO_METERS;
        float screenHeight = GAME_HEIGHT / GameConstants.PIXELS_TO_METERS;
        float edgeThickness = 0.1f; // Adjust as needed

        // Create BodyDef for static boundaries
        BodyDef boundaryDef = new BodyDef();
        boundaryDef.type = BodyDef.BodyType.StaticBody;

        // Create FixtureDef for boundaries
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.2f;

        // Create top boundary
        boundaryDef.position.set(0, screenHeight);
        Body topBoundary = world.createBody(boundaryDef);
        PolygonShape topShape = new PolygonShape();
        topShape.setAsBox(screenWidth, edgeThickness);
        fixtureDef.shape = topShape;
        topBoundary.createFixture(fixtureDef);
        topShape.dispose();

        // Create bottom boundary
        boundaryDef.position.set(0, 0);
        Body bottomBoundary = world.createBody(boundaryDef);
        PolygonShape bottomShape = new PolygonShape();
        bottomShape.setAsBox(screenWidth, edgeThickness);
        fixtureDef.shape = bottomShape;
        bottomBoundary.createFixture(fixtureDef);
        bottomShape.dispose();

        // Create left boundary
        boundaryDef.position.set(0, 0);
        Body leftBoundary = world.createBody(boundaryDef);
        PolygonShape leftShape = new PolygonShape();
        leftShape.setAsBox(edgeThickness, screenHeight);
        fixtureDef.shape = leftShape;
        leftBoundary.createFixture(fixtureDef);
        leftShape.dispose();

        // Create right boundary
        boundaryDef.position.set(screenWidth, 0);
        Body rightBoundary = world.createBody(boundaryDef);
        PolygonShape rightShape = new PolygonShape();
        rightShape.setAsBox(edgeThickness, screenHeight);
        fixtureDef.shape = rightShape;
        rightBoundary.createFixture(fixtureDef);
        rightShape.dispose();
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropImage.dispose();
        bucketImage.dispose();
        debugRenderer.dispose();
    }

}