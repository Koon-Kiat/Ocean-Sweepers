package project.game.application.scene.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.entity.factory.RockFactory;
import project.game.application.entity.factory.TrashFactory;
import project.game.application.entity.item.Trash;
import project.game.application.entity.npc.Monster;
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
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;

@SuppressWarnings("unused")
public class GameScene extends Scene implements IEntityRemovalListener {

    public static List<Entity> existingEntities;
    private static final GameLogger LOGGER = new GameLogger(GameScene.class);
    private EntityManager entityManager;
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private SpriteBatch batch;
    private Texture rockImage;
    private Texture boatSpritesheet;
    private Texture rockSpritesheet;
    private Texture trashImage;
    private Texture monsterImage;
    private RockFactory rockFactory;
    private TrashFactory trashFactory;
    private List<Rock> rocks;
    private List<Trash> trashes;
    private Boat boat;
    private TextureRegion[] boatDirectionalSprites;
    private Texture[] trashTextures;
    private Texture backgroundTexture;
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
    private AudioConfig config;
    private AudioUI audioUI;
    private IGameConstants constants;
    List<IMovementStrategy> strategyPool = new ArrayList<>();

    // Sprite sheet identifiers
    private static final String BOAT_SPRITESHEET = "boat_sprites";
    private static final String ROCK_SPRITESHEET = "rock_sprites";
    private static final String MONSTER_SPRITESHEET = "monster_sprites";

    // Entity type identifiers for directional sprites
    private static final String BOAT_ENTITY = "boat";
    private static final String MONSTER_ENTITY = "monster";

    public GameScene(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
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

    @Override
    public void render(float deltaTime) {
        input();

        try {
            collisionManager.updateGame(constants.GAME_WIDTH(), constants.GAME_HEIGHT(), constants.PIXELS_TO_METERS());
        } catch (Exception e) {
            LOGGER.error("Exception during game update: {0}", e.getMessage());
        }

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, constants.GAME_WIDTH(), constants.GAME_HEIGHT());
        batch.end();

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

        float timeStep = 1 / 300f;
        int velocityIterations = 8;
        int positionIterations = 3;
        world.step(timeStep, velocityIterations, positionIterations);

        // Process collisions
        collisionManager.processCollisions();
        collisionManager.syncEntityPositions(constants.PIXELS_TO_METERS());

        // Play sound effect on collision
        if (collisionManager.collision() && audioManager != null) {
            audioManager.playSoundEffect("drophit");
        }
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
        assetManager.loadTextureAssets("rock.png");
        assetManager.loadTextureAssets("Rocks.png");
        assetManager.loadTextureAssets("monster.png");
        assetManager.loadTextureAssets("ocean_background.jpg");
        assetManager.update();
        assetManager.loadAndFinish();

        // Get background texture directly
        backgroundTexture = assetManager.getAsset("ocean_background.jpg", Texture.class);

        // Create and store boat sprite sheet (7x7)
        boatSpritesheet = assetManager.getAsset("steamboat.png", Texture.class);
        TextureRegion[] boatSheet = assetManager.createSpriteSheet(BOAT_SPRITESHEET, "steamboat.png", 7, 7);

        // Create boat directional sprites for all 8 directions
        // Note: These indices are specific to your steamboat.png layout
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
        TextureRegion[] rockRegions = assetManager.createSpriteSheet(ROCK_SPRITESHEET, "Rocks.png", 3, 3);

        // Load monster texture
        monsterImage = assetManager.getAsset("monster.png", Texture.class);

        // Load trash textures
        trashTextures = new Texture[3];
        trashTextures[0] = assetManager.getAsset("trash1.png", Texture.class);
        trashTextures[1] = assetManager.getAsset("trash2.png", Texture.class);
        trashTextures[2] = assetManager.getAsset("trash3.png", Texture.class);

        // Keep this for backward compatibility
        trashImage = trashTextures[0];

        LOGGER.info("Game assets initialized successfully");
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

            TextureRegion[] rockRegions = CustomAssetManager.getInstance().getSpriteSheet(ROCK_SPRITESHEET);

            entityManager = new EntityManager();

            // Initialize entities and movement managers
            Entity boatEntity = new Entity(
                    constants.PLAYER_START_X(),
                    constants.PLAYER_START_Y(),
                    constants.PLAYER_WIDTH(),
                    constants.PLAYER_HEIGHT(),
                    true);

            Entity monsterEntity = new Entity(
                    constants.MONSTER_START_X(),
                    constants.MONSTER_START_Y(),
                    constants.MONSTER_WIDTH(),
                    constants.MONSTER_HEIGHT(),
                    true);

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

            rockFactory = new RockFactory(constants, world, existingEntities, rockRegions);
            trashFactory = new TrashFactory(constants, world, existingEntities, trashTextures, collisionManager);
            trashFactory.setRemovalListener(this);

            // Create entities
            for (int i = 0; i < constants.NUM_ROCKS(); i++) {
                Rock rock = rockFactory.createObject();
                rocks.add(rock);
                entityManager.addRenderableEntity(rock);
                existingEntities.add(rock.getEntity());
            }

            for (int i = 0; i < constants.NUM_TRASHES(); i++) {
                Trash trash = trashFactory.createObject();
                trash.setRemovalListener(this);
                trashes.add(trash);
                entityManager.addRenderableEntity(trash);
                existingEntities.add(trash.getEntity());
            }

            // Convert rocks to Entity objects for obstacle avoidance
            for (Rock rock : rocks) {
                rockEntities.add(rock.getEntity());
            }

            float[] customWeights = { 0.30f, 0.70f };
            npcMovementManager = new NPCMovementBuilder()
                    .withEntity(monsterEntity)
                    .setSpeed(constants.NPC_SPEED())
                    .setInitialVelocity(1, 1)
                    .withInterceptorAndObstacleAvoidance(playerMovementManager, rockEntities, customWeights)
                    .setLenientMode(true)
                    .build();

            // Use the directional sprites from the asset manager
            boat = new Boat(boatEntity, world, playerMovementManager, boatDirectionalSprites);
            monster = new Monster(monsterEntity, world, npcMovementManager, "monster.png");

            // Initialize bodies
            boat.initBody(world);
            monster.initBody(world);

            // Add entities to the entity manager
            entityManager.addRenderableEntity(boat);
            entityManager.addRenderableEntity(monster);

            LOGGER.info("Monster is using composite strategy to follow boat while avoiding {0} rocks",
                    rockEntities.size());

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
            WorldBoundaryFactory.createScreenBoundaries(world, constants.GAME_WIDTH(), constants.GAME_HEIGHT(), 1f,
                    constants.PIXELS_TO_METERS());

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
            e.printStackTrace();
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
    }

    private void handleAudioInput() {
        if (inputManager.isKeyJustPressed(Input.Keys.V)) {
            LOGGER.info("Key V detected!");

            // If pause menu is open, close it before opening volume settings
            if (isMenuOpen) {
                isMenuOpen = false;
                isPaused = false;
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
                isPaused = true;
                inputMultiplexer.clear();
                inputMultiplexer.addProcessor(sceneUIManager.getStage());
                inputMultiplexer.addProcessor(inputManager);
                Gdx.input.setInputProcessor(inputMultiplexer);
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
}