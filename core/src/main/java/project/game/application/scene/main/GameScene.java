package project.game.application.scene.main;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.api.entity.ILifeLossCallback;
import project.game.application.entity.factory.EntityFactoryManager;
import project.game.application.entity.item.Trash;
import project.game.application.entity.obstacle.Rock;
import project.game.application.entity.player.Boat;
import project.game.application.movement.builder.PlayerMovementBuilder;
import project.game.application.scene.overlay.Options;
import project.game.application.scene.overlay.Scenetransition;
import project.game.application.scene.ui.AudioUI;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.asset.management.CustomAssetManager;
import project.game.engine.audio.config.AudioConfig;
import project.game.engine.audio.management.AudioManager;
import project.game.engine.audio.music.MusicManager;
import project.game.engine.audio.sound.SoundManager;
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
import project.game.engine.scene.management.TimeManager;

public class GameScene extends Scene implements IEntityRemovalListener {

    private static final GameLogger LOGGER = new GameLogger(GameScene.class);

    // Menu
    private final HealthManager healthManager;
    private final ScoreManager scoreManager;
    private boolean isVolumePopupOpen = false;
    private boolean isMenuOpen = false;
    private InputMultiplexer inputMultiplexer;
    private Options options;
    private Skin skin;
    private OrthographicCamera camera;

    // Transition
    private Scenetransition sceneTransition;

    // Audio
    private AudioManager audioManager;
    private AudioConfig config;
    private AudioUI audioUI;

    // Constants
    private IGameConstants constants;

    // Movement
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private List<NPCMovementManager> trashMovementManagers = new ArrayList<>();
    List<IMovementStrategy> strategyPool = new ArrayList<>();

    // Entities
    public static List<Entity> existingEntities;
    private EntityManager entityManager;
    private Boat boat;
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
    private TextureRegion[] boatTextureRegions;
    private TextureRegion[] boatDirectionalSprites;
    private TextureRegion[] rockRegions;
    private Texture[] trashTextures;
    private TextureRegion[] trashRegions;
    private Texture backgroundTexture;
    private final Texture heartTexture = new Texture("heart.png");
    protected TimeManager timer;
    private boolean showTimer = true; // Flag to control if the timer is shown
    private BitmapFont upheavalFont;

    private float remainingTime;

    public GameScene(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
        this.healthManager = HealthManager.getInstance(heartTexture);
        this.scoreManager = ScoreManager.getInstance();
        this.timer = new TimeManager(0, 30);
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public Skin getSkin() {
        return skin;
    }

    /**
     * Initializes the in-game popup menu for options and key rebinding.
     */
    public void initPopUpMenu() {
        options = new Options(sceneManager, this, inputManager);
        inputMultiplexer = new InputMultiplexer();

        sceneUIManager.getStage().addActor(options.getRebindMenu());
    }

    public void loseLife() {
        healthManager.loseLife();
    }

    protected void draw() {
        // Regular rendering code
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, constants.GAME_WIDTH(), constants.GAME_HEIGHT());
        batch.end();

        // Draw entities
        batch.begin();
        entityManager.draw(batch);

        // Adding a label for turtle health
        upheavalFont.draw(batch, "Player Health:", 50,
                sceneUIManager.getStage().getHeight() - 30);
        // Draw health and score
        healthManager.draw(batch, 300, sceneUIManager.getStage().getHeight() - 60, healthManager.getLives());

        // print score
        upheavalFont.draw(batch, "Score: " + scoreManager.getScore(), 500,
                sceneUIManager.getStage().getHeight() - 30);

        // Time left in logs
        // System.out.println("Time Left: " + timer.getMinutes() + ":" + timer.getSeconds());

        // print timer
        if (showTimer) {
            skin.getFont("default-font").setColor(1, 1, 1, 1); // Set color to white
            skin.getFont("default-font").draw(batch, String.format("Time: %02d:%02d",
                    timer.getMinutes(), timer.getSeconds()), 200, sceneUIManager.getStage().getHeight() - 60);
            skin.getFont("default-font").setColor(0, 0, 0, 1); // Reset color to black
        }

        batch.end();

        // Draw stage
        sceneUIManager.getStage().act(Gdx.graphics.getDeltaTime());
        sceneUIManager.getStage().draw();
    }

    public void setShowTimer(boolean show) {
        this.showTimer = show;
    }

    @Override
    public void render(float deltaTime) {
        input();
        timer.update(deltaTime);

        if (timer.isTimeUp()) {
            timer.stop();
            sceneManager.setScene("gameover");
            if (sceneManager.hasWon() == false) {
                audioManager.playSoundEffect("loss");
            } else {
                audioManager.playSoundEffect("success");
            }
            audioManager.stopMusic();
        }

        try {
            // Update movement for all entities - simple null checks only
            if (playerMovementManager != null) {
                playerMovementManager.updateMovement();
            }

            if (npcMovementManager != null) {
                npcMovementManager.updateMovement();
            }

            // Update all trash movement managers
            if (trashMovementManagers != null) {
                for (NPCMovementManager trashManager : trashMovementManagers) {
                    if (trashManager != null) {
                        trashManager.updateMovement();
                    }
                }
            }

            // Make sure collision handling catches up with new positions
            if (collisionManager != null) {
                collisionManager.updateGame(constants.GAME_WIDTH(), constants.GAME_HEIGHT(),
                        constants.PIXELS_TO_METERS());
            }
        } catch (Exception e) {
            LOGGER.error("Exception during game update: {0}", e.getMessage());
        }

        draw();

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
            // Step the world with fixed time step for stable physics (1/60 second)
            float timeStep = 1.0f / 60.0f; // Fixed time step instead of variable deltaTime
            int velocityIterations = 6;
            int positionIterations = 2;
            world.step(timeStep, velocityIterations, positionIterations);

            // Now it's safe to remove bodies
            collisionManager.processRemovalQueue();
            collisionManager.processCollisions();
            collisionManager.syncEntityPositions(constants.PIXELS_TO_METERS());
        } else {
            // Process removal queue to prevent leaks
            LOGGER.warn("Not enough active bodies for physics simulation");
        }

        remainingTime -= deltaTime;

        if (trashes.isEmpty()) {
            scoreManager.multiplyScore((float) (remainingTime / 100));
            // Indicate that the player has won
            sceneManager.setWinState(true);
            sceneManager.setScene("gameover");
            audioManager.playSoundEffect("success");
            audioManager.stopMusic();
        }

        // LOGGER.info("Score: {0}", scoreManager.getScore());
    }

    @Override
    public void show() {
        timer.resetTime();
        inputManager.resetInputState();
        timer.start();
        if (inputMultiplexer == null) {
            inputMultiplexer = new InputMultiplexer();
        } else {
            inputMultiplexer.clear();
        }
        inputMultiplexer.addProcessor(sceneUIManager.getStage());
        inputMultiplexer.addProcessor(inputManager);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCursorPosition(0, 0);

        // Make sure AudioManager is initialized before using it
        if (audioManager == null) {
            // Initialize AudioManager if it's null
            config = config != null ? config : new AudioConfig();
            audioManager = AudioManager.getInstance(MusicManager.getInstance(), SoundManager.getInstance(), config);
            LOGGER.info("Initializing AudioManager in show() method");
        }

        // Make sure MusicManager is initialized and play music safely
        MusicManager.getInstance().loadMusicTracks("BackgroundMusic.mp3");
        if (audioManager != null) {
            audioManager.playMusic("BackgroundMusic");
            LOGGER.info("Playing background music");
        } else {
            LOGGER.error("AudioManager is still null after initialization attempt");
        }
    }

    @Override
    public void hide() {
        timer.stop();

    }

    @Override
    public void dispose() {

        scoreManager.multiplyScore((float) (remainingTime / 100));
        LOGGER.info("Final Score: " + scoreManager.getScore());
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
        sceneTransition = new Scenetransition(sceneManager); // Initialize transition
        batch = new SpriteBatch();
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        upheavalFont = new BitmapFont(Gdx.files.internal("upheaval.fnt"));
        inputManager.enableMovementControls();
        constants = GameConstantsFactory.getConstants();
        config = new AudioConfig();
        LOGGER.info("GameScene inputManager instance: {0}", System.identityHashCode(inputManager));
        initPopUpMenu();
        displayMessage();

        remainingTime = 300.0f;

        // Init assets
        try {
            // Initialize game assets first
            initializeGameAssets();

            // Create entity manager
            entityManager = new EntityManager();

            // Initialize lists
            rocks = new ArrayList<>();
            trashes = new ArrayList<>();
            existingEntities = new ArrayList<>();
            trashMovementManagers = new ArrayList<>();

            // Initialize camera
            camera = new OrthographicCamera(constants.GAME_WIDTH(), constants.GAME_HEIGHT());
            camera.position.set(constants.GAME_WIDTH() / 2, constants.GAME_HEIGHT() / 2, 0);
            camera.update();

            // Initialize CollisionManager before creating any entities
            collisionManager = new CollisionManager(world, inputManager);
            collisionManager.init();

            // Create boat (player) entity
            Entity boatEntity = new Entity(
                    constants.PLAYER_START_X(),
                    constants.PLAYER_START_Y(),
                    constants.PLAYER_WIDTH(),
                    constants.PLAYER_HEIGHT(),
                    true);

            playerMovementManager = new PlayerMovementBuilder()
                    .withEntity(boatEntity)
                    .setSpeed(constants.PLAYER_SPEED())
                    .setInitialVelocity(0, 0)
                    .setLenientMode(true)
                    .withConstantMovement()
                    .build();

            boat = new Boat(boatEntity, world, playerMovementManager, boatDirectionalSprites);
            boat.setCollisionManager(collisionManager);

            // Add boat to managers
            entityManager.addRenderableEntity(boat);
            collisionManager.addEntity(boat, playerMovementManager);
            existingEntities.add(boatEntity);

            // Initialize EntityFactoryManager
            entityFactoryManager = new EntityFactoryManager(
                    constants,
                    world,
                    existingEntities,
                    collisionManager,
                    rockRegions,
                    trashRegions);
            entityFactoryManager.setTrashRemovalListener(this);

            // Set life loss callback for boat
            boat.setLifeLossCallback(new ILifeLossCallback() {
                @Override
                public void onLifeLost() {
                    loseLife();
                    audioManager.playSoundEffect("collision");
                    if (healthManager.getLives() == 0) {
                        sceneManager.setScene("gameover");
                        audioManager.playSoundEffect("loss");
                        audioManager.stopMusic();
                        audioManager.hideVolumeControls();
                        options.getRebindMenu().setVisible(false);
                    }
                }
            });

            // Create rocks and trash
            for (int i = 0; i < constants.NUM_ROCKS(); i++) {
                Rock rock = entityFactoryManager.createRock();
                rocks.add(rock);
                entityManager.addRenderableEntity(rock);
                collisionManager.addEntity(rock, null);
                existingEntities.add(rock.getEntity());
            }

            // Create all trash entities
            for (int i = 0; i < constants.NUM_TRASHES(); i++) {
                Trash trash = entityFactoryManager.createTrash();
                if (trash != null) {
                    trashes.add(trash);
                    entityManager.addRenderableEntity(trash);

                    // Get and store the movement manager
                    NPCMovementManager trashMovementManager = trash.getMovementManager();
                    if (trashMovementManager != null) {
                        trashMovementManagers.add(trashMovementManager);
                        collisionManager.addEntity(trash, trashMovementManager);
                    }

                    existingEntities.add(trash.getEntity());
                    LOGGER.info("Created and registered trash entity {0} with movement manager", i);
                }
            }

            // Create world boundaries last
            WorldBoundaryFactory.createScreenBoundaries(world, constants.GAME_WIDTH(), constants.GAME_HEIGHT(), 0.5f,
                    constants.PIXELS_TO_METERS());

            // Initialize audio
            audioManager = AudioManager.getInstance(MusicManager.getInstance(), SoundManager.getInstance(), config);
            audioUI = new AudioUI(audioManager, config, sceneUIManager.getStage(), skin);
            audioManager.setAudioUI(audioUI);

            MusicManager.getInstance().loadMusicTracks("BackgroundMusic.mp3");
            SoundManager.getInstance().loadSoundEffects(
                    new String[] { "Boinkeffect.mp3", "selection.mp3", "rubble.mp3", "explosion.mp3", "loss.mp3",
                            "success.mp3", "points.mp3" },
                    new String[] { "keybuttons", "selection", "collision", "explosion", "loss", "success", "points" });

            audioManager.setMusicVolume(config.getMusicVolume());
            audioManager.setSoundEnabled(config.isSoundEnabled());

            LOGGER.info("GameScene initialization complete");

        } catch (Exception e) {
            LOGGER.error("Exception during game creation: {0}", e.getMessage());
            LOGGER.error("Stack trace: {0}", (Object) e.getStackTrace());
        }

        // Log completion of initialization
        LOGGER.info("GameScene initialization complete");

        // Init complete
    }

    @Override
    public void onEntityRemove(Entity entity) {
        if (entity == null || entityManager == null) {
            LOGGER.error("Entity or EntityManager is null");
            return;
        }

        LOGGER.info("Removing entity: {0}", entity.getID());
        existingEntities.remove(entity);
        entity.removeFromManager(entityManager);
        LOGGER.info("Entity removed from manager: {0}", entity.getID());

        for (Trash trash : new ArrayList<>(trashes)) {
            if (trash.getEntity().equals(entity)) {
                // Also remove the trash's movement manager from our list
                NPCMovementManager trashManager = trash.getMovementManager();
                if (trashManager != null) {
                    trashMovementManagers.remove(trashManager);
                    LOGGER.info("Trash movement manager removed for entity: {0}", entity.getID());
                }

                trashes.remove(trash);
                audioManager.playSoundEffect("points");
                LOGGER.info("Trash removed: {0}", trash.getEntity().getID());
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
        assetManager.loadTextureAssets("ocean_background.jpg");
        assetManager.update();
        assetManager.loadAndFinish();

        // Get background texture directly
        backgroundTexture = assetManager.getAsset("ocean_background.jpg", Texture.class);

        // Create and store boat sprite sheet (7x7)
        boatSpritesheet = assetManager.getAsset("steamboat.png", Texture.class);
        boatTextureRegions = assetManager.createSpriteSheet(BOAT_SPRITESHEET, "steamboat.png", 7, 7);

        // Create boat directional sprites for all 8 directions
        TextureRegion[] eightDirectionalSprites = new TextureRegion[8];
        eightDirectionalSprites[Boat.DIRECTION_UP] = boatTextureRegions[0]; // UP
        eightDirectionalSprites[Boat.DIRECTION_RIGHT] = boatTextureRegions[11]; // RIGHT
        eightDirectionalSprites[Boat.DIRECTION_DOWN] = boatTextureRegions[23]; // DOWN
        eightDirectionalSprites[Boat.DIRECTION_LEFT] = boatTextureRegions[35]; // LEFT
        eightDirectionalSprites[Boat.DIRECTION_UP_RIGHT] = boatTextureRegions[7]; // UP-RIGHT
        eightDirectionalSprites[Boat.DIRECTION_DOWN_RIGHT] = boatTextureRegions[14]; // DOWN-RIGHT
        eightDirectionalSprites[Boat.DIRECTION_DOWN_LEFT] = boatTextureRegions[28]; // DOWN-LEFT
        eightDirectionalSprites[Boat.DIRECTION_UP_LEFT] = boatTextureRegions[42]; // UP-LEFT

        // Register the directional sprites with the asset manager
        assetManager.registerDirectionalSprites(BOAT_ENTITY, eightDirectionalSprites);
        boatDirectionalSprites = eightDirectionalSprites;

        // Create rock sprite sheet (3x3)
        rockImage = assetManager.getAsset("Rocks.png", Texture.class);
        rockRegions = assetManager.createSpriteSheet(ROCK_SPRITESHEET, "Rocks.png", 3, 3);

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

    /**
     * Handles key inputs for game control:
     */
    private void input() {
        // handleAudioInput();
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

        // end game (debugging purposes)
        if (inputManager.isKeyJustPressed(Input.Keys.E)) {
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
        // Switch to game2 scene (debugging purposes)
        if (inputManager.isKeyJustPressed(Input.Keys.N)) {
            audioManager.stopMusic();
            sceneManager.setScene("game2");
            
        }
        // Switch to game2 scene (just for testing)
        // if (inputManager.isKeyJustPressed(Input.Keys.N)) {
        // sceneManager.setScene("game2");
        // audioManager.stopMusic();
        // }
    }
    public AudioManager getAudioManager() {
        return audioManager;
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
                "Debugging: \nPress P to pause and rebind keys\nPress E to end the game\nPress N to switch to GameScene2");
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
        sceneUIManager.getStage().getActors()
                .select(a -> a.getClass() == TextField.class) // Filter only TextField instances
                .forEach(Actor::remove); // Remove them from the stage
    }

    /*
     * protected getters so only game2 accesses them
     */

    protected World getWorld() {
        return world;
    }

    protected CollisionManager getCollisionManager() {
        return collisionManager;
    }

    protected List<Trash> getTrashes() {
        return trashes;
    }

    protected List<Entity> getRockEntities() {
        return existingEntities;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }
    
    public EntityFactoryManager getEntityFactoryManager() {
        return entityFactoryManager;
    }

}