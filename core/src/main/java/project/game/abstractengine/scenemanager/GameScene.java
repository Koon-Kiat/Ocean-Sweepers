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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value.Fixed;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import project.game.Direction;
import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.collisionmanager.CollisionManager;
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

    private final static float PIXELS_TO_METERS = 32f;
    private static final float DROP_START_X = 0f / PIXELS_TO_METERS;
    private static final float DROP_START_Y = 400f / PIXELS_TO_METERS;
    private static final float BUCKET_START_X = 5f / PIXELS_TO_METERS;
    private static final float BUCKET_START_Y = 40f / PIXELS_TO_METERS;

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
        world = new World(new Vector2(0, -9.8f), true);

        createScreenBoundaries();
        inputManager = new SceneIOManager();
        new CollisionManager(world);

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

        camera = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT); // Initialize the camera
        camera.position.set(GAME_WIDTH / 2, GAME_HEIGHT / 2, 0);
        camera.update();

        debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputManager); // Added first
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

        debugMatrix = camera.combined.cpy().scl(PIXELS_TO_METERS);

        debugRenderer.render(world, debugMatrix);

        // Fixed timestep for Box2D
        float timeStep = 1 / 60f; // 60 frames per second
        int velocityIterations = 6;
        int positionIterations = 2;
        world.step(timeStep, velocityIterations, positionIterations);
    }

    private void updateGame() {
        playerMovementManager.updateDirection(inputManager.getPressedKeys(), inputManager.getKeyBindings());

        // Update movement; exceptions here will be logged and thrown upward
        playerMovementManager.updateMovement();
        npcMovementManager.updateMovement();

        // Clamp sprite positions to screen boundaries
        float bucketX = Math.max(0, Math.min(playerMovementManager.getX(), GAME_WIDTH - bucket.getWidth()));
        float bucketY = Math.max(0, Math.min(playerMovementManager.getY(), GAME_HEIGHT - bucket.getHeight()));
        float dropX = Math.max(0, Math.min(npcMovementManager.getX(), GAME_WIDTH - drop.getWidth()));
        float dropY = Math.max(0, Math.min(npcMovementManager.getY(), GAME_HEIGHT - drop.getHeight()));

        // Synchronize rectangle positions with movement manager positions
        bucket.setX(bucketX);
        bucket.setY(bucketY);
        drop.setX(dropX);
        drop.setY(dropY);

        // Update Box2D body positions to match sprite positions
        bucket.getBody().setTransform(bucket.getX() / PIXELS_TO_METERS, bucket.getY() / PIXELS_TO_METERS, 0);
        drop.getBody().setTransform(drop.getX() / PIXELS_TO_METERS, drop.getY() / PIXELS_TO_METERS, 0);
    }

    private void createScreenBoundaries() {
        float screenWidth = GAME_WIDTH / PIXELS_TO_METERS;
        float screenHeight = GAME_HEIGHT / PIXELS_TO_METERS;
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