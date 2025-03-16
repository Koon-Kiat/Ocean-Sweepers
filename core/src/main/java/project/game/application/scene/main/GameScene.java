package project.game.application.scene.main;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.entity.factory.EntityFactoryManager;
import project.game.application.entity.item.Trash;
import project.game.application.entity.npc.SeaTurtle;
import project.game.application.entity.obstacle.Rock;
import project.game.application.entity.player.Boat;
import project.game.application.movement.builder.NPCMovementBuilder;
import project.game.application.movement.builder.PlayerMovementBuilder;
import project.game.application.movement.strategy.ConstantMovementStrategy;
import project.game.application.scene.overlay.Options;
import project.game.application.scene.ui.AudioUI;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.asset.management.CustomAssetManager;
import project.game.engine.audio.config.AudioConfig;
import project.game.engine.audio.management.AudioManager;
import project.game.engine.audio.music.MusicManager;
import project.game.engine.audio.sound.SoundManager;
import project.game.engine.entitysystem.entity.api.IRenderable;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.management.EntityManager;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.movement.core.PlayerMovementManager;
import project.game.engine.entitysystem.physics.boundary.WorldBoundaryFactory;
import project.game.engine.entitysystem.physics.management.CollisionManager;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.HealthManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;
import project.game.engine.scene.management.ScoreManager;

public class GameScene extends Scene implements IEntityRemovalListener {

    private static final GameLogger LOGGER = new GameLogger(GameScene.class);

    // Menu
    private boolean isVolumePopupOpen = false;
    private boolean isMenuOpen = false;
    private InputMultiplexer inputMultiplexer;
    private Options options;
    private Window popupMenu;
    private Skin skin;
    private OrthographicCamera camera;
    private HealthManager healthManager;
    private ScoreManager scoreManager;

    // Audio
    private AudioManager audioManager;
    private AudioConfig config;
    private AudioUI audioUI;

    // Constants
    private IGameConstants constants;

    // Movement
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    List<IMovementStrategy> strategyPool = new ArrayList<>();

    // Entities
    public static List<Entity> existingEntities;
    private EntityManager entityManager;
    private Boat boat;
    // private SeaTurtle monster;
    private List<Rock> rocks;
    private List<Trash> trashes;

    // Factories
    private EntityFactoryManager entityFactoryManager;

    // Physics
    private World world;
    private Matrix4 debugMatrix;
    private Box2DDebugRenderer debugRenderer;
    private CollisionManager collisionManager;

    // Sprite sheet identifiers
    private static final String BOAT_SPRITESHEET = "boat_sprites";
    private static final String ROCK_SPRITESHEET = "rock_sprites";

    // Entity type identifiers for directional sprites
    private static final String BOAT_ENTITY = "boat";
    private SpriteBatch batch;
    private Texture rockImage;
    private Texture boatSpritesheet;
    private Texture trashImage;
    // private Texture monsterImage;
    private TextureRegion[] boatDirectionalSprites;
    private TextureRegion[] rockRegions;
    private Texture[] trashTextures;
    private TextureRegion[] trashRegions;
    // private TextureRegion monsterRegion;
    private Texture backgroundTexture;

    public GameScene(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
        this.healthManager = HealthManager.getInstance();
        this.scoreManager = ScoreManager.getInstance();

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

    public void loseLife() {
        healthManager.loseLife();
    }

    /**
     * Closes the popup menu and resumes game play.
     */
    public void closePopupMenu() {
        isMenuOpen = false;
        options.getPopupMenu().setVisible(false);
        inputMultiplexer.removeProcessor(sceneUIManager.getStage());
        inputMultiplexer.addProcessor(inputManager);
        LOGGER.debug("Popup menu closed");
    }

    @Override
    public void render(float deltaTime) {
        input();

        try {
            // Update movement for all entities
            playerMovementManager.updateMovement();
            // npcMovementManager.updateMovement();

            // Make sure collision handling catches up with new positions
            collisionManager.updateGame(constants.GAME_WIDTH(), constants.GAME_HEIGHT(), constants.PIXELS_TO_METERS());
        } catch (Exception e) {
            LOGGER.error("Exception during game update: {0}", e.getMessage());
        }

        // Regular rendering code
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, constants.GAME_WIDTH(), constants.GAME_HEIGHT());
        batch.end();

        // Draw entities
        batch.begin();
        entityManager.draw(batch);
        batch.end();

        // Draw health and score
        batch.begin();
        healthManager.draw(batch);
        skin.getFont("default-font").draw(batch, "Score: " + scoreManager.getScore(), 200,
                sceneUIManager.getStage().getHeight() - 30);
        batch.end();

        // Draw stage
        sceneUIManager.getStage().act(Gdx.graphics.getDeltaTime());
        sceneUIManager.getStage().draw();

        // Render debug matrix
        debugMatrix = camera.combined.cpy().scl(constants.PIXELS_TO_METERS());
        debugRenderer.render(world, debugMatrix);

        // Only step the physics if we have active bodies
        int activeBodyCount = 0;
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for (Body body : bodies) {
            if (body.isActive()) {
                activeBodyCount++;
            }
        }

        // Ensure we have enough bodies for physics to work
        if (activeBodyCount > 1) {
            // Step the world
            float timeStep = 1.0f / 60.0f;
            int velocityIterations = 6;
            int positionIterations = 2;
            world.step(timeStep, velocityIterations, positionIterations);

            // Now it's safe to remove bodies
            collisionManager.processRemovalQueue();
            collisionManager.processCollisions();
            collisionManager.syncEntityPositions(constants.PIXELS_TO_METERS());

            // Play sound effect on collision
            if (collisionManager.collision() && audioManager != null) {
                audioManager.playSoundEffect("drophit");
            }
        } else {
            // Process removal queue to prevent leaks
            LOGGER.warn("Not enough active bodies for physics simulation");
        }

        scoreManager.addScore(10);
        LOGGER.info("Score: {0}", scoreManager.getScore());
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
        Gdx.input.setCursorPosition(0, 0);

        MusicManager.getInstance().loadMusicTracks("BackgroundMusic.mp3");
        audioManager.playMusic("BackgroundMusic");
    }

    @Override
    public void dispose() {
        // Log world state before disposal
        LOGGER.info("Before GameScene disposal:");

        batch.dispose();
        boatSpritesheet.dispose();
        trashImage.dispose();
        rockImage.dispose();
        debugRenderer.dispose();
        if (audioManager != null) {
            audioManager.dispose();
        }

        LOGGER.info("GameScene disposed");
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        inputManager.enableMovementControls();
        constants = GameConstantsFactory.getConstants();
        config = new AudioConfig();
        LOGGER.info("GameScene inputManager instance: {0}", System.identityHashCode(inputManager));

        initPopUpMenu();
        displayMessage();

        try {
            // Initialize game assets
            initializeGameAssets();

            entityManager = new EntityManager();

            // Initialize entities and movement managers
            Entity boatEntity = new Entity(
                    constants.PLAYER_START_X(),
                    constants.PLAYER_START_Y(),
                    constants.PLAYER_WIDTH(),
                    constants.PLAYER_HEIGHT(),
                    true);

            // Entity monsterEntity = new Entity(
            //         constants.MONSTER_START_X(),
            //         constants.MONSTER_START_Y(),
            //         constants.MONSTER_WIDTH(),
            //         constants.MONSTER_HEIGHT(),
            //         true);

            playerMovementManager = new PlayerMovementBuilder()
                    .withEntity(boatEntity)
                    .setSpeed(constants.PLAYER_SPEED())
                    .setInitialVelocity(0, 0)
                    .setLenientMode(true)
                    .withConstantMovement()
                    .build();

            strategyPool = new ArrayList<>();
            strategyPool.add(new ConstantMovementStrategy(constants.NPC_SPEED(), true));

            List<Entity> rockEntities = new ArrayList<>();
            rocks = new ArrayList<>();
            trashes = new ArrayList<>();
            existingEntities = new ArrayList<>();

            camera = new OrthographicCamera(constants.GAME_WIDTH(), constants.GAME_HEIGHT());
            camera.position.set(constants.GAME_WIDTH() / 2, constants.GAME_HEIGHT() / 2, 0);
            camera.update();

            // Initialize CollisionManager
            collisionManager = new CollisionManager(world, inputManager);
            collisionManager.init();

            // Initialize EntityFactoryManager
            entityFactoryManager = new EntityFactoryManager(
                    constants,
                    world,
                    existingEntities,
                    collisionManager,
                    null,
                    rockRegions,
                    trashRegions);
            entityFactoryManager.setTrashRemovalListener(this);

            // Create entities using factory manager
            boat = new Boat(boatEntity, world, playerMovementManager, boatDirectionalSprites);
            // monster = new SeaTurtle(monsterEntity, world, npcMovementManager, monsterRegion);

            boat.setCollisionManager(collisionManager);

            // Create rocks and trash
            for (int i = 0; i < constants.NUM_ROCKS(); i++) {
                Rock rock = entityFactoryManager.createRock();
                rocks.add(rock);
                entityManager.addRenderableEntity(rock);
                rockEntities.add(rock.getEntity());
            }

            for (int i = 0; i < constants.NUM_TRASHES(); i++) {
                Trash trash = entityFactoryManager.createTrash();
                trashes.add(trash);
                entityManager.addRenderableEntity(trash);
            }

            // Set up NPC movement with obstacle avoidance
            float[] customWeights = { 0.30f, 0.70f };
            // npcMovementManager = new NPCMovementBuilder()
            //         .setSpeed(constants.NPC_SPEED())
            //         .setInitialVelocity(1, 1)
            //         .withInterceptorAndObstacleAvoidance(playerMovementManager, rockEntities, customWeights)
            //         .setLenientMode(true)
            //         .build();

            // Add entities to the entity manager
            entityManager.addRenderableEntity(boat);
            // entityManager.addRenderableEntity(monster);

            // Add entities to collision manager
            collisionManager.addEntity(boat, playerMovementManager);
            // collisionManager.addEntity(monster, npcMovementManager);

            for (Rock rock : rocks) {
                collisionManager.addEntity(rock, null);
            }

            // Create boundaries
            WorldBoundaryFactory.createScreenBoundaries(world, constants.GAME_WIDTH(), constants.GAME_HEIGHT(), 0.5f,
                    constants.PIXELS_TO_METERS());

            // Log world status after initialization
            LOGGER.info("Physics world initialization complete");

            // Initialize AudioManager and AudioUI
            audioManager = AudioManager.getInstance(MusicManager.getInstance(), SoundManager.getInstance(), config);
            audioUI = new AudioUI(audioManager, config, sceneUIManager.getStage(), skin);
            audioManager.setAudioUI(audioUI);

            // Load and play audio
            MusicManager.getInstance().loadMusicTracks("BackgroundMusic.mp3");
            SoundManager.getInstance().loadSoundEffects(
                    new String[] { "watercollision.mp3", "Boinkeffect.mp3", "selection.mp3" },
                    new String[] { "drophit", "keybuttons", "selection" });

            // Set audio configuration
            audioManager.setMusicVolume(config.getMusicVolume());
            audioManager.setSoundEnabled(config.isSoundEnabled());

        } catch (Exception e) {
            LOGGER.error("Exception during game creation: {0}", e.getMessage());
            LOGGER.error("Stack trace: {0}", (Object) e.getStackTrace());
        }

        // Log completion of initialization
        LOGGER.info("GameScene initialization complete");
    }

    @Override
    public void onEntityRemove(Entity entity) {
        existingEntities.remove(entity);
        if (entity instanceof IRenderable) {
            entityManager.removeRenderableEntity((IRenderable) entity);
        }
        for (Trash trash : new ArrayList<>(trashes)) {
            if (trash.getEntity().equals(entity)) {
                trashes.remove(trash);
                break;
            }
        }
    }

    /**
     * Initializes game assets including sprites and textures
     */
    private void initializeGameAssets() {
        CustomAssetManager assetManager = CustomAssetManager.getInstance();

        // Load all texture assets first
        assetManager.loadTextureAssets("trash1.png");
        assetManager.loadTextureAssets("trash2.png");
        assetManager.loadTextureAssets("trash3.png");
        assetManager.loadTextureAssets("steamboat.png");
        assetManager.loadTextureAssets("Rocks.png");
        // assetManager.loadTextureAssets("monster.png");
        assetManager.loadTextureAssets("ocean_background.jpg");
        assetManager.update();
        assetManager.loadAndFinish();

        // Get background texture directly
        backgroundTexture = assetManager.getAsset("ocean_background.jpg", Texture.class);

        // Create and store boat sprite sheet (7x7)
        boatSpritesheet = assetManager.getAsset("steamboat.png", Texture.class);
        TextureRegion[] boatSheet = assetManager.createSpriteSheet(BOAT_SPRITESHEET, "steamboat.png", 7, 7);

        // Create boat directional sprites for all 8 directions
        TextureRegion[] eightDirectionalSprites = new TextureRegion[8];
        eightDirectionalSprites[Boat.DIRECTION_UP] = boatSheet[0]; // UP
        eightDirectionalSprites[Boat.DIRECTION_RIGHT] = boatSheet[11]; // RIGHT
        eightDirectionalSprites[Boat.DIRECTION_DOWN] = boatSheet[23]; // DOWN
        eightDirectionalSprites[Boat.DIRECTION_LEFT] = boatSheet[35]; // LEFT
        eightDirectionalSprites[Boat.DIRECTION_UP_RIGHT] = boatSheet[7]; // UP-RIGHT
        eightDirectionalSprites[Boat.DIRECTION_DOWN_RIGHT] = boatSheet[14]; // DOWN-RIGHT
        eightDirectionalSprites[Boat.DIRECTION_DOWN_LEFT] = boatSheet[28]; // DOWN-LEFT
        eightDirectionalSprites[Boat.DIRECTION_UP_LEFT] = boatSheet[42]; // UP-LEFT

        // Register the directional sprites with the asset manager
        assetManager.registerDirectionalSprites(BOAT_ENTITY, eightDirectionalSprites);
        boatDirectionalSprites = eightDirectionalSprites;

        // Create rock sprite sheet (3x3)
        rockImage = assetManager.getAsset("Rocks.png", Texture.class);
        rockRegions = assetManager.createSpriteSheet(ROCK_SPRITESHEET, "Rocks.png", 3, 3);

        // Load monster texture and create TextureRegion
        // monsterImage = assetManager.getAsset("monster.png", Texture.class);
        // monsterRegion = new TextureRegion(monsterImage);

        // Load trash textures and create TextureRegions
        trashTextures = new Texture[3];
        trashRegions = new TextureRegion[3];
        String[] trashPaths = { "trash1.png", "trash2.png", "trash3.png" };

        for (int i = 0; i < trashPaths.length; i++) {
            trashTextures[i] = assetManager.getAsset(trashPaths[i], Texture.class);
            trashRegions[i] = new TextureRegion(trashTextures[i]);
        }

        // Store first trash texture for reference
        trashImage = trashTextures[0];

        LOGGER.info("Game assets initialized successfully");
    }

    private void handleAudioInput() {
        if (inputManager.isKeyJustPressed(Input.Keys.V)) {
            LOGGER.info("Key V detected!");

            // If pause menu is open, close it before opening volume settings
            if (isMenuOpen) {
                isMenuOpen = false;
                options.getRebindMenu().setVisible(false);
                inputMultiplexer.clear();
                inputMultiplexer.addProcessor(inputManager); // Restore game input
                Gdx.input.setInputProcessor(inputMultiplexer);
                LOGGER.info("Closed rebind menu because V was pressed.");
            }

            isVolumePopupOpen = !isVolumePopupOpen;
            if (isVolumePopupOpen) {
                audioManager.showVolumeControls();

                // Ensure UI elements are interactive
                if (audioUI != null) {
                    audioUI.restoreUIInteractivity();
                } else {
                    LOGGER.error("Error: audioUIManager is null!");
                }

                // Always ensure both stage & game input are handled
                inputMultiplexer.clear();
                inputMultiplexer.addProcessor(sceneUIManager.getStage());
                inputMultiplexer.addProcessor(inputManager);
                Gdx.input.setInputProcessor(inputMultiplexer);

                LOGGER.info("Opened volume settings.");
            } else {
                audioManager.hideVolumeControls();
                inputMultiplexer.clear();
                inputMultiplexer.addProcessor(inputManager);
                Gdx.input.setInputProcessor(inputMultiplexer);

                LOGGER.info("Closed volume settings.");
            }
        }
    }

    /**
     * Handles key inputs for game control:
     */
    private void input() {
        handleAudioInput();
        for (Integer key : inputManager.getKeyBindings().keySet()) {
            if (inputManager.isKeyJustPressed(key)) {
                LOGGER.info("Direction Key pressed: {0}", Input.Keys.toString(key));
                if (audioManager != null) {
                    audioManager.playSoundEffect("keybuttons");
                } else {
                    LOGGER.warn("AudioManager is null");
                }
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
                inputMultiplexer.clear();
                inputMultiplexer.addProcessor(sceneUIManager.getStage());
                inputMultiplexer.addProcessor(inputManager);
                Gdx.input.setInputProcessor(inputMultiplexer);
                LOGGER.info("InputProcessor set to stage");
            } else {
                inputMultiplexer.removeProcessor(sceneUIManager.getStage());
                inputMultiplexer.addProcessor(inputManager);
                sceneUIManager.getStage().setKeyboardFocus(null);
                LOGGER.info("InputProcessor set to inputManager");
            }
        }

        if (inputManager.isKeyJustPressed(Input.Keys.NUM_0)) {
            loseLife();
            if (healthManager.getLives() == 0) {
                sceneManager.setScene("gameover");
                audioManager.stopMusic();
                audioManager.hideVolumeControls();
                options.getRebindMenu().setVisible(false);
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
}