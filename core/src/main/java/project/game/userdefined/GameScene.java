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
        LOGGER.log(Level.INFO, "GameScene inputManager instance: {0}", System.identityHashCode(inputManager));

        inputManager = new SceneIOManager();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
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

        bucket = new BucketEntity(genericBucketEntity, world, playerMovementManager, "bucket.png");
        drop = new DropEntity(genericDropEntity, world, npcMovementManager, "droplet.png");
        nonMovableDroplet = new NonMovableDroplet(genericNonMovableDroplet, "droplet.png");

        bucket.initBody(world);
        drop.initBody(world);

        // Add entities to the entity manager
        entityManager.addRenderableEntity(bucket);
        entityManager.addRenderableEntity(drop);
        entityManager.addRenderableEntity(nonMovableDroplet);

        camera = new OrthographicCamera(GameConstants.GAME_WIDTH, GameConstants.GAME_HEIGHT);
        camera.position.set(GameConstants.GAME_WIDTH / 2, GameConstants.GAME_HEIGHT / 2, 0);
        camera.update();

        debugRenderer = new Box2DDebugRenderer();

        // Initialize CollisionManager and create screen boundaries
        collisionManager = new CollisionManager(world, inputManager);
        collisionManager.init();
        collisionManager.addEntity(drop, npcMovementManager);
        collisionManager.addEntity(bucket, playerMovementManager);

        BoundaryFactory.createScreenBoundaries(world, GameConstants.GAME_WIDTH, GameConstants.GAME_HEIGHT, 0.1f);

        // Initialize AudioManager and play background music
        audioManager = new AudioManager(stage);
        audioManager.playMusic("BackgroundMusic");

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

        try {
            collisionManager.updateGame(GameConstants.GAME_WIDTH, GameConstants.GAME_HEIGHT);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during game update: {0}", e.getMessage());
        }

        batch.begin();
        entityManager.draw(batch);
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        debugMatrix = camera.combined.cpy().scl(GameConstants.PIXELS_TO_METERS);
        debugRenderer.render(world, debugMatrix);

        float timeStep = 1 / 60f;
        world.step(timeStep, 6, 2);
        collisionManager.processCollisions();
        collisionManager.syncEntityPositions();

        if (collisionManager.collision()) {
            if (audioManager != null) {
                audioManager.playSoundEffect("drophit");
            } else {
                System.err.println("[ERROR] AudioManager is null!");
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
     * @brief Initializes the popup menu for the game scene
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

    /**
     * @brief Handles input for the game scene
     * 
     *        Game Scene will transition to:
     *        - Main Menu Scene on 'M' key press
     *        - Game Over Scene on 'E' key press
     *        - Rebind Pop-up window on 'P' key press
     */

    private void input() {
        if (inputManager.isKeyJustPressed(Input.Keys.V)) {
            isVolumePopupOpen = !isVolumePopupOpen;
            if (isVolumePopupOpen) {
                audioManager.togglePause();
                audioManager.showVolumeControls();
                inputMultiplexer.setProcessors(stage, inputManager);
            } else {
                audioManager.hideVolumeControls();
                audioManager.togglePause();
                inputMultiplexer.setProcessors(inputManager, stage);
            }
        }

        if (inputManager.isKeyJustPressed(Input.Keys.M)) {
            sceneManager.setScene("menu");
        } else if (inputManager.isKeyJustPressed(Input.Keys.E)) {
            sceneManager.setScene("gameover");
            audioManager.stopMusic();
        }

        if (inputManager.isKeyJustPressed(Input.Keys.P)) {
            isMenuOpen = !isMenuOpen;
            hideDisplayMessage();
            options.getRebindMenu().setVisible(isMenuOpen);
            if (isMenuOpen) {
                isPaused = true;
                inputMultiplexer.setProcessors(stage, inputManager);
                LOGGER.log(Level.INFO, "InputProcessor set to stage");
            } else {
                isPaused = false;
                inputMultiplexer.removeProcessor(stage);
                inputMultiplexer.addProcessor(inputManager);
                stage.setKeyboardFocus(null);
                LOGGER.log(Level.INFO, "InputProcessor set to inputManager");
            }
        }
    }

    /**
     * @brief Displays a message on the screen for key bindings
     */
    private void displayMessage() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        final TextField.TextFieldStyle style = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        style.background = null;

        final TextField textField = new TextField("", style);
        textField.setWidth(300);
        textField.setHeight(40);
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

    /**
     * @brief Closes the popup menu and unpauses the game
     */
    public void closePopupMenu() {
        isMenuOpen = false;
        isPaused = false;
        options.getPopupMenu().setVisible(false);
        inputMultiplexer.removeProcessor(stage);
        inputMultiplexer.addProcessor(inputManager);
        LOGGER.log(Level.INFO, "Popup closed and game unpaused");
    }

}