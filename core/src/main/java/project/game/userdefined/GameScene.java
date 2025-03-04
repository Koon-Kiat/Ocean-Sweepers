package project.game.userdefined;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import project.game.abstractengine.assetmanager.CustomAssetManager;
import project.game.abstractengine.audiomanager.AudioManager;
import project.game.abstractengine.entitysystem.collisionmanager.BoundaryFactory;
import project.game.abstractengine.entitysystem.collisionmanager.CollisionManager;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.entitymanager.EntityManager;
import project.game.abstractengine.entitysystem.movementmanager.MovementManager;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;
import project.game.abstractengine.interfaces.IMovementBehavior;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.scenemanager.Scene;
import project.game.abstractengine.scenemanager.SceneManager;
import project.game.builder.NPCMovementBuilder;
import project.game.builder.PlayerMovementBuilder;
import project.game.constants.GameConstants;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;
import project.game.testentity.BucketEntity;
import project.game.testentity.DropEntity;
import project.game.testentity.NonMovableDroplet;

@SuppressWarnings({ "unused", "FieldMayBeFinal" })
public class GameScene extends Scene {
    private static final Logger LOGGER = Logger.getLogger(GameScene.class.getName());
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
    private boolean isPaused = false;
    private boolean isVolumePopupOpen = false;
    private boolean isMenuOpen = false;
    private InputMultiplexer inputMultiplexer;
    private Options options;
    private AudioManager audioManager;
    private NonMovableDroplet nonMovableDroplet;
    List<IMovementBehavior> behaviorPool = new ArrayList<>();

    public GameScene(SceneManager sceneManager, SceneIOManager inputManager) {
        super(sceneManager, inputManager);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        inputManager.enableMovementControls();
        LOGGER.log(Level.INFO, "GameScene inputManager instance: {0}", System.identityHashCode(inputManager));

        initPopUpMenu();
        displayMessage();

        try {
            CustomAssetManager.getInstance().loadTextureAssets("droplet.png");
            CustomAssetManager.getInstance().loadTextureAssets("bucket.png");
            CustomAssetManager.getInstance().update();
            CustomAssetManager.getInstance().getasset_Manager().finishLoading();
            if (CustomAssetManager.getInstance().isLoaded()) {
                dropImage = CustomAssetManager.getInstance().getAsset("droplet.png", Texture.class);
                bucketImage = CustomAssetManager.getInstance().getAsset("bucket.png", Texture.class);
            } else {
                LOGGER.log(Level.WARNING, "Some assets not loaded yet!");
            }
            LOGGER.log(Level.INFO, "Loaded droplet.png successfully.");
            LOGGER.log(Level.INFO, "Loaded bucket.png successfully.");
            if (dropImage == null) {
                LOGGER.log(Level.SEVERE, "dropImage is null after loading!");
            }
            if (bucketImage == null) {
                LOGGER.log(Level.SEVERE, "bucketImage is null after loading!");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load assets: {0}", e.getMessage());
        }

        entityManager = new EntityManager();

        // Create entities
        Entity genericDropEntity = new Entity(
                GameConstants.DROP_START_X,
                GameConstants.DROP_START_Y,
                GameConstants.DROP_WIDTH,
                GameConstants.DROP_HEIGHT,
                true);
        Entity genericBucketEntity = new Entity(
                GameConstants.BUCKET_START_X,
                GameConstants.BUCKET_START_Y,
                GameConstants.BUCKET_WIDTH,
                GameConstants.BUCKET_HEIGHT,
                true);
        Entity genericNonMovableDroplet = new Entity(100f, 200f, 50f, 50f, false);

        // Set lenient mode for movement manager
        MovementManager.setLenientMode(true);

        playerMovementManager = new PlayerMovementBuilder()
                .withEntity(genericBucketEntity)
                .setSpeed(GameConstants.PLAYER_SPEED)
                .setDirection(Direction.NONE)
                .withConstantMovement()
                .build();

        // Add behavior to the pool for Random Movement
        behaviorPool = new ArrayList<>();
        behaviorPool.add(new ConstantMovementBehavior(GameConstants.NPC_SPEED));
        behaviorPool.add(
                new ZigZagMovementBehavior(GameConstants.NPC_SPEED, GameConstants.AMPLITUDE, GameConstants.FREQUENCY));
        behaviorPool.add(new FollowMovementBehavior(playerMovementManager, GameConstants.NPC_SPEED));

        npcMovementManager = new NPCMovementBuilder()
                .withEntity(genericDropEntity)
                .setSpeed(GameConstants.NPC_SPEED)
                .withFollowMovement(playerMovementManager)
                .setDirection(Direction.NONE)
                .build();

        // Initialize entities
        bucket = new BucketEntity(genericBucketEntity, world, playerMovementManager, "bucket.png");
        drop = new DropEntity(genericDropEntity, world, npcMovementManager, "droplet.png");
        nonMovableDroplet = new NonMovableDroplet(genericNonMovableDroplet, "droplet.png");

        // Initialize bodies
        bucket.initBody(world);
        drop.initBody(world);

        // Add entities to the entity manager
        entityManager.addRenderableEntity(bucket);
        entityManager.addRenderableEntity(drop);
        entityManager.addRenderableEntity(nonMovableDroplet);

        camera = new OrthographicCamera(GameConstants.GAME_WIDTH, GameConstants.GAME_HEIGHT);
        camera.position.set(GameConstants.GAME_WIDTH / 2, GameConstants.GAME_HEIGHT / 2, 0);
        camera.update();

        // Initialize CollisionManager
        collisionManager = new CollisionManager(world, inputManager);
        collisionManager.init();

        // Add entities to the collision manager
        collisionManager.addEntity(drop, npcMovementManager);
        collisionManager.addEntity(bucket, playerMovementManager);

        // Create screen boundaries
        BoundaryFactory.createScreenBoundaries(world, GameConstants.GAME_WIDTH, GameConstants.GAME_HEIGHT, 0.1f);

        // Initialize AudioManager and play background music
        audioManager = new AudioManager(sceneUIManager.getStage());
        audioManager.playMusic("BackgroundMusic");

    }

    @Override
    public void show() {
        if (inputMultiplexer == null) {
            inputMultiplexer = new InputMultiplexer();
        } else {
            inputMultiplexer.clear();
        }
        inputMultiplexer.addProcessor(sceneUIManager.getStage());
        inputMultiplexer.addProcessor(inputManager);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float deltaTime) {
        input();

        try {
            collisionManager.updateGame(GameConstants.GAME_WIDTH, GameConstants.GAME_HEIGHT);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during game update: {0}", e.getMessage());
        }

        // Draw entities
        batch.begin();
        entityManager.draw(batch);
        batch.end();

        // Draw stage
        sceneUIManager.getStage().act(Gdx.graphics.getDeltaTime());
        sceneUIManager.getStage().draw();

        // Render debug matrix
        debugMatrix = camera.combined.cpy().scl(GameConstants.PIXELS_TO_METERS);
        debugRenderer.render(world, debugMatrix);

        // Step the physics simulation forward at a rate of 60hz
        float timeStep = 1 / 60f;
        world.step(timeStep, 6, 2);

        // Process collisions
        collisionManager.processCollisions();
        collisionManager.syncEntityPositions();

        // Play sound effect on collision
        if (collisionManager.collision()) {
            if (audioManager != null) {
                audioManager.playSoundEffect("drophit");
            } else {
                LOGGER.log(Level.SEVERE, "AudioManager is null!");
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

    /**
     * Initializes the in-game popup menu for options and key rebinding.
     */
    public void initPopUpMenu() {
        options = new Options(sceneManager, this, inputManager);
        options.setMainMenuButtonVisibility(true);
        options.getPopupMenu().setTouchable(Touchable.enabled);

        popupMenu = options.getPopupMenu();
        inputMultiplexer = new InputMultiplexer();

        // Add popup menu to the stage
        if (popupMenu != null) {
            float centerX = sceneUIManager.getStage().getWidth() / 2f - popupMenu.getWidth() / 2f;
            float centerY = sceneUIManager.getStage().getHeight() / 2f - popupMenu.getHeight() / 2f;
            popupMenu.setPosition(centerX, centerY);
        } else {
            Gdx.app.log("GameScene", "popupMenu is null");
        }

        sceneUIManager.getStage().addActor(options.getPopupMenu());
        sceneUIManager.getStage().addActor(options.getRebindMenu());
    }

    /**
     * Handles key inputs for game control:
     */
    private void input() {
        for (Integer key : inputManager.getKeyBindings().keySet()) {
            if (inputManager.isKeyJustPressed(key)) {
                LOGGER.log(Level.INFO, "Direction Key pressed: {0}", Input.Keys.toString(key));
                audioManager.playSoundEffect("keybuttons");
            }
        }
        // Toggle volume controls
        if (inputManager.isKeyJustPressed(Input.Keys.V)) {
            if (isMenuOpen) {
                isMenuOpen = false;
                options.getRebindMenu().setVisible(false);
            }
            isVolumePopupOpen = !isVolumePopupOpen;
            if (isVolumePopupOpen) {
                audioManager.showVolumeControls();
            } else {
                audioManager.hideVolumeControls();
            }
        }
        // Toggle game menu
        if (inputManager.isKeyJustPressed(Input.Keys.M)) {
            sceneManager.setScene("menu");
        } else if (inputManager.isKeyJustPressed(Input.Keys.E)) {
            sceneManager.setScene("gameover");
            audioManager.stopMusic();
            audioManager.hideVolumeControls();
            options.getRebindMenu().setVisible(false);
        }
        // Toggle pause menu
        if (inputManager.isKeyJustPressed(Input.Keys.P)) {
            if (isVolumePopupOpen) {
                audioManager.hideVolumeControls();
                isVolumePopupOpen = false;
            }
            isMenuOpen = !isMenuOpen;
            hideDisplayMessage();
            options.getRebindMenu().setVisible(isMenuOpen);
            if (isMenuOpen) {
                isPaused = true;
                inputMultiplexer.setProcessors(sceneUIManager.getStage(), inputManager);
                LOGGER.log(Level.INFO, "InputProcessor set to stage");
            } else {
                isPaused = false;
                inputMultiplexer.removeProcessor(sceneUIManager.getStage());
                inputMultiplexer.addProcessor(inputManager);
                sceneUIManager.getStage().setKeyboardFocus(null);
                LOGGER.log(Level.INFO, "InputProcessor set to inputManager");
            }
        }
    }

    /**
     * Displays an on-screen message with key binding instructions.
     */
    private void displayMessage() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        final TextField.TextFieldStyle style = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        style.background = null;

        final TextField textField = new TextField("", style);
        textField.setWidth(300);
        textField.setHeight(40);
        textField.setPosition(sceneUIManager.getStage().getWidth() / 2f - textField.getWidth() / 2f,
        sceneUIManager.getStage().getHeight() - textField.getHeight());
        textField.setMessageText(
                "Press M to return to main menu...\nPress P to pause and rebind keys\nPress E to end the game");
        textField.setDisabled(true);
        sceneUIManager.getStage().addActor(textField);

        // Overlay debug message
        batch.begin();
        skin.getFont("default-font").draw(batch, "Debug Mode Active", 10, sceneUIManager.getStage().getHeight() - 10);
        batch.end();
    }

    /**
     * Removes the on-screen key binding message.
     */
    private void hideDisplayMessage() {
        for (Actor actor : sceneUIManager.getStage().getActors()) {
            if (actor instanceof TextField) {
                actor.remove();
            }
        }
    }

    /**
     * Closes the popup menu and resumes game play.
     */
    public void closePopupMenu() {
        isMenuOpen = false;
        isPaused = false;
        options.getPopupMenu().setVisible(false);
        inputMultiplexer.removeProcessor(sceneUIManager.getStage());
        inputMultiplexer.addProcessor(inputManager);
        LOGGER.log(Level.INFO, "Popup closed and game unpaused");
    }

}