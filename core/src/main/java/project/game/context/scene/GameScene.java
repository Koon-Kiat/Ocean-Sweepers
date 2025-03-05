package project.game.context.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

import project.game.common.logging.core.GameLogger;
import project.game.context.api.constant.IGameConstants;
import project.game.context.builder.NPCMovementBuilder;
import project.game.context.builder.PlayerMovementBuilder;
import project.game.context.entity.Boat;
import project.game.context.entity.Monster;
import project.game.context.entity.Rock;
import project.game.context.entity.Trash;
import project.game.context.factory.GameConstantsFactory;
import project.game.context.factory.RockFactory;
import project.game.context.factory.TrashFactory;
import project.game.context.movement.ConstantMovementStrategy;
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.asset.CustomAssetManager;
import project.game.engine.audio.AudioManager;
import project.game.engine.entitysystem.collision.BoundaryFactory;
import project.game.engine.entitysystem.collision.CollisionManager;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.entity.EntityManager;
import project.game.engine.entitysystem.movement.NPCMovementManager;
import project.game.engine.entitysystem.movement.PlayerMovementManager;
import project.game.engine.io.SceneIOManager;
import project.game.engine.scene.Scene;
import project.game.engine.scene.SceneManager;

@SuppressWarnings("unused")
public class GameScene extends Scene {

    private static final GameLogger LOGGER = new GameLogger(GameScene.class);
    private EntityManager entityManager;
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private SpriteBatch batch;
    private Texture rockImage;
    private Texture boatImage;
    private Texture trashImage;
    private Texture monsterImage;
    private RockFactory rockFactory;
    private TrashFactory trashFactory;
    private List<Rock> rocks;
    private List<Trash> trashes;
    private Boat boat;
    private Monster monster;
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
    // private Trash nonMovableTrash;
    private IGameConstants constants;
    List<IMovementStrategy> strategyPool = new ArrayList<>();

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
        constants = GameConstantsFactory.getConstants();
        LOGGER.info("GameScene inputManager instance: {0}", System.identityHashCode(inputManager));

        initPopUpMenu();
        displayMessage();

        try {
            CustomAssetManager.getInstance().loadTextureAssets("droplet.png");
            CustomAssetManager.getInstance().loadTextureAssets("bucket.png");
            CustomAssetManager.getInstance().loadTextureAssets("rock.png");
            CustomAssetManager.getInstance().loadTextureAssets("monster.png");
            CustomAssetManager.getInstance().update();
            CustomAssetManager.getInstance().getasset_Manager().finishLoading();
            if (CustomAssetManager.getInstance().isLoaded()) {
                boatImage = CustomAssetManager.getInstance().getAsset("bucket.png", Texture.class);
                trashImage = CustomAssetManager.getInstance().getAsset("droplet.png", Texture.class);
                rockImage = CustomAssetManager.getInstance().getAsset("rock.png", Texture.class);
                monsterImage = CustomAssetManager.getInstance().getAsset("monster.png", Texture.class);
            } else {
                LOGGER.warn("Some assets not loaded yet!");
            }
            LOGGER.info("Loaded droplet.png successfully.");
            LOGGER.info("Loaded bucket.png successfully.");
            LOGGER.info("Loaded rock.png successfully.");
            LOGGER.info("Loaded monster.png successfully.");
            if (boatImage == null) {
                LOGGER.error("boatImage is null after loading!");
            }
            if (trashImage == null) {
                LOGGER.error("trashImage is null after loading!");
            }
            if (rockImage == null) {
                LOGGER.error("rockImage is null after loading!");
            }
            if (monsterImage == null) {
                LOGGER.error("monsterImage is null after loading!");
            }

        } catch (Exception e) {
            LOGGER.error("Exception loading assets: {0}", e.getMessage());
        }

        entityManager = new EntityManager();

        Entity boatEntity = new Entity(
                constants.BUCKET_START_X(),
                constants.BUCKET_START_Y(),
                constants.BUCKET_WIDTH(),
                constants.BUCKET_HEIGHT(),
                true);

        Entity monsterEntity = new Entity(100, 100, 70f, 70f, true);

        playerMovementManager = new PlayerMovementBuilder()
                .withEntity(boatEntity)
                .setSpeed(constants.PLAYER_SPEED())
                .setInitialVelocity(0, 0)
                .setLenientMode(true)
                .withConstantMovement()
                .build();

        // Add strategy to the pool for Random Movement
        strategyPool = new ArrayList<>();
        strategyPool.add(new ConstantMovementStrategy(constants.NPC_SPEED(), true));
        LOGGER.info("Configured NPC movement strategys: {0}", strategyPool.size());

        // Create rock entities first so we can pass them to the builder
        rockFactory = new RockFactory(constants, world);
        rocks = new ArrayList<>();

        for (int i = 0; i < constants.NUM_ROCKS(); i++) {
            Rock rock = rockFactory.createObject();
            rocks.add(rock);
        }

        // Convert rocks to Entity objects for obstacle avoidance
        List<Entity> rockEntities = new ArrayList<>();
        for (Rock rock : rocks) {
            rockEntities.add(rock.getEntity());
        }

        // Create NPC movement with composite strategy that follows boat while avoiding
        // rocks
        float[] customWeights = { 0.30f, 0.70f }; // 30% interception, 70% avoidance for better obstacle navigation
        npcMovementManager = new NPCMovementBuilder()
                .withEntity(monsterEntity)
                .setSpeed(constants.NPC_SPEED())
                .setInitialVelocity(1, 1) // Set a clear initial direction
                .withInterceptorAndObstacleAvoidance(playerMovementManager, rockEntities, customWeights)
                .setLenientMode(true)
                .build();

        LOGGER.info(
                "Created monster with composite movement strategy (30% intercept, 70% avoid) to follow boat while avoiding {0} rocks",
                rockEntities.size());

        // Initialize entities
        boat = new Boat(boatEntity, world, playerMovementManager, "bucket.png");
        monster = new Monster(monsterEntity, world, npcMovementManager, "monster.png");

        trashFactory = new TrashFactory(constants, world);
        trashes = new ArrayList<>();

        Random random = new Random();

        // Add rocks to entity manager
        for (Rock rock : rocks) {
            entityManager.addRenderableEntity(rock);
        }

        for (int i = 0; i < constants.NUM_TRASHES(); i++) {
            Trash trash = trashFactory.createObject();
            trashes.add(trash);
            entityManager.addRenderableEntity(trash);
        }

        // Initialize bodies
        boat.initBody(world);
        monster.initBody(world);

        // Add entities to the entity manager
        entityManager.addRenderableEntity(boat);
        entityManager.addRenderableEntity(monster);

        LOGGER.info("Monster is using composite strategy to follow boat while avoiding {0} rocks", rockEntities.size());

        camera = new OrthographicCamera(constants.GAME_WIDTH(), constants.GAME_HEIGHT());
        camera.position.set(constants.GAME_WIDTH() / 2, constants.GAME_HEIGHT() / 2, 0);
        camera.update();

        // Initialize CollisionManager
        collisionManager = new CollisionManager(world, inputManager);
        collisionManager.init();

        // Add entities to the collision manager
        collisionManager.addEntity(boat, playerMovementManager);
        collisionManager.addEntity(monster, npcMovementManager);
        for (Rock rock : rocks) {
            collisionManager.addEntity(rock, null);
        }
        for (Trash trash : trashes) {
            collisionManager.addEntity(trash, null);
        }

        // Create boundaries
        BoundaryFactory.createScreenBoundaries(world, constants.GAME_WIDTH(), constants.GAME_HEIGHT(), 1f,
                constants.PIXELS_TO_METERS());

        // Initialize AudioManager and play background music
        audioManager = new AudioManager(sceneUIManager.getStage());
        audioManager.playMusic("BackgroundMusic");

        // Log completion of initialization
        LOGGER.info("GameScene initialization complete");

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
        LOGGER.debug("GameScene shown");
    }

    @Override
    public void render(float deltaTime) {
        input();

        try {
            collisionManager.updateGame(constants.GAME_WIDTH(), constants.GAME_HEIGHT(), constants.PIXELS_TO_METERS());
        } catch (Exception e) {
            LOGGER.error("Exception during game update: {0}", e.getMessage());
        }

        // Draw entities
        batch.begin();
        entityManager.draw(batch);
        batch.end();

        // Draw stage
        sceneUIManager.getStage().act(Gdx.graphics.getDeltaTime());
        sceneUIManager.getStage().draw();

        // Render debug matrix
        debugMatrix = camera.combined.cpy().scl(constants.PIXELS_TO_METERS());
        debugRenderer.render(world, debugMatrix);

        // Step the physics simulation forward with fixed timestep and more iterations
        float timeStep = 1 / 300f; // Increased physics update rate
        int velocityIterations = 8; // Increased from 6
        int positionIterations = 3;
        world.step(timeStep, velocityIterations, positionIterations);

        // Process collisions
        collisionManager.processCollisions();
        collisionManager.syncEntityPositions(constants.PIXELS_TO_METERS());

        // Play sound effect on collision
        if (collisionManager.collision()) {
            if (audioManager != null) {
                audioManager.playSoundEffect("drophit");
            } else {
                LOGGER.error("AudioManager is null!");
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        boatImage.dispose();
        trashImage.dispose();
        rockImage.dispose();
        debugRenderer.dispose();
        LOGGER.info("GameScene disposed");
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
                LOGGER.info("Direction Key pressed: {0}", Input.Keys.toString(key));
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
                LOGGER.info("InputProcessor set to stage");
            } else {
                isPaused = false;
                inputMultiplexer.removeProcessor(sceneUIManager.getStage());
                inputMultiplexer.addProcessor(inputManager);
                sceneUIManager.getStage().setKeyboardFocus(null);
                LOGGER.info("InputProcessor set to inputManager");
            }
        }
    }

    /**
     * Displays an on-screen message with key binding instructions.
     */
    private void displayMessage() {
        LOGGER.info("Displaying welcome message");
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
        LOGGER.debug("Popup menu closed");
    }

}