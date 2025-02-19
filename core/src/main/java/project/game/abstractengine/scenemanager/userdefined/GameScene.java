package project.game.abstractengine.scenemanager.userdefined;

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
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

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
import project.game.abstractengine.scenemanager.Scene;
import project.game.abstractengine.scenemanager.SceneManager;
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
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private Matrix4 debugMatrix;
    private CollisionManager collisionManager;
    private boolean isPaused = false, isMenuOpen = false;
    private InputMultiplexer inputMultiplexer;
    private Options options;

    public GameScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(inputManager);
        this.sceneManager = sceneManager;
    }

    /**
     * @class GameScene
     * @brief Initializes and draws the Game Scene when player starts the game
     * 
     *        This method initializes the Game Scene by creating a new SpriteBatch
     *        and two Textures for the player and NPC entities. It also creates a
     *        new Skin object and a Stage object. The method also creates a new
     *        EntityManager object and adds the player and NPC entities to the
     *        EntityManager. The method also creates a new OrthographicCamera object
     *        and a Box2DDebugRenderer object.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();
        world = new World(new Vector2(0, 0), true);
        System.out.println("[DEBUG] GameScene inputManager instance: " + System.identityHashCode(inputManager));

        inputManager = new SceneIOManager();
        entityManager = new EntityManager();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        initPopUpMenu();
        displayMessage();

        try {
            GameAsset.getInstance().loadTextureAssets("droplet.png");
            GameAsset.getInstance().loadTextureAssets("bucket.png");
            GameAsset.getInstance().update();
            GameAsset.getInstance().getAssetManager().finishLoading();
            if (GameAsset.getInstance().isLoaded()) {
                dropImage = GameAsset.getInstance().getAsset("droplet.png", Texture.class);
                bucketImage = GameAsset.getInstance().getAsset("bucket.png", Texture.class);
            } else {
                System.err.println("[ERROR] Some assets not loaded yet!");
            }
            System.out.println("[DEBUG] Loaded droplet.png successfully.");
            System.out.println("[DEBUG] Loaded bucket.png successfully.");
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

        camera = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT);
        camera.position.set(GAME_WIDTH / 2, GAME_HEIGHT / 2, 0);
        camera.update();

        debugRenderer = new Box2DDebugRenderer();

        // Initialize CollisionManager and create screen boundaries
        collisionManager = new CollisionManager(world, playerMovementManager, npcMovementManager, bucket, drop,
                inputManager);
        collisionManager.init();
        collisionManager.createScreenBoundaries(GAME_WIDTH, GAME_HEIGHT);
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputManager);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float deltaTime) {
        input();
        show();

        try {
            collisionManager.updateGame(GAME_WIDTH, GAME_HEIGHT);
        } catch (Exception e) {
            System.err.println("[ERROR] Exception during game update: " + e.getMessage());
            Gdx.app.error("Main", "Exception during game update", e);
        }

        batch.begin();
        entityManager.draw(batch);
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        debugMatrix = camera.combined.cpy().scl(GameConstants.PIXELS_TO_METERS);
        debugRenderer.render(world, debugMatrix);

        // Fixed timestep for Box2D
        float timeStep = 1 / 60f;
        world.step(timeStep, 6, 2);
        collisionManager.processCollisions();
        collisionManager.syncEntityPositions();
    }

    /*
     * Initializes the pop-up menu for the game scene from Options class
     * Adds the pop-up menu to the stage
     */
    public void initPopUpMenu() {
        options = new Options(sceneManager, this, inputManager);

        options.setMainMenuButtonVisibility(true);
        options.getPopupMenu().setTouchable(Touchable.enabled);

        popupMenu = options.getPopupMenu();
        inputMultiplexer = new InputMultiplexer();

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
    }

    /*
     * Handles input for scene transitions and toggling the options menu
     * 
     * Game Scene will transition to:
     * - Main Menu Scene on 'M' key press
     * - Game Over Scene on 'E' key press
     * - Rebind Pop-up window on 'P' key press
     */
    private void input() {

        Gdx.input.setInputProcessor(inputManager);

        // Keyboard inputs to change scenes: "M" to go to main menu, "E" to go to game
        // over scene
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            sceneManager.setScene("menu");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            sceneManager.setScene("gameover");
        }

        // Toggle options menu with 'P'
        // Will open the rebind menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            isMenuOpen = !isMenuOpen;
            hideDisplayMessage();
            options.getRebindMenu().setVisible(isMenuOpen);
            if (isMenuOpen) {
                isPaused = true;
                inputMultiplexer.setProcessors(stage, inputManager); // Set stage first
                System.out.println("[DEBUG] InputProcessor set to stage");

            } else {
                isPaused = false;
                inputMultiplexer.removeProcessor(stage);
                inputMultiplexer.addProcessor(inputManager);
                stage.setKeyboardFocus(null);
                System.out.println("[DEBUG] InputProcessor set to inputManager");
            }
        }
    }

    /*
     * Displays a message on the screen for key bindings
     */
    private void displayMessage() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        final TextField.TextFieldStyle style = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        style.background = null; // Disable the background

        final TextField textField = new TextField("", style);
        textField.setWidth(300); // Adjust the width as needed
        textField.setHeight(40); // Adjust the height as needed
        textField.setPosition(stage.getWidth() / 2f - textField.getWidth() / 2f,
                stage.getHeight() - textField.getHeight());
        textField.setMessageText(
                "Press M to return to main menu...\nPress P to pause and rebind keys\nPress E to end the game");
        textField.setDisabled(true);
        stage.addActor(textField);

        // Overlay text over the debug matrix
        batch.begin();
        skin.getFont("default-font").draw(batch, "Debug Mode Active", 10, stage.getHeight() - 10);
        batch.end();
    }

    private void hideDisplayMessage() {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof TextField) {
                actor.remove();
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropImage.dispose();
        bucketImage.dispose();
        debugRenderer.dispose();
    }

    /*
     * Used in Options class to close the popup menu and unpause the game
     */
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