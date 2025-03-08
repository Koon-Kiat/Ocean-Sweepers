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
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.core.CustomAssetManager;
import project.game.engine.audio.config.AudioConfig;
import project.game.engine.audio.core.AudioManager;
import project.game.engine.audio.music.MusicManager;
import project.game.engine.audio.sound.SoundManager;
import project.game.engine.entitysystem.collision.BoundaryFactory;
import project.game.engine.entitysystem.collision.CollisionManager;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.entity.EntityManager;
import project.game.engine.entitysystem.movement.type.NPCMovementManager;
import project.game.engine.entitysystem.movement.type.PlayerMovementManager;
import project.game.engine.io.scene.SceneIOManager;
import project.game.engine.scene.core.Scene;
import project.game.engine.scene.core.SceneManager;

@SuppressWarnings("unused")
public class GameScene extends Scene implements IEntityRemovalListener {

    public static List<Entity> existingEntities;
    private static final GameLogger LOGGER = new GameLogger(GameScene.class);
    private EntityManager entityManager;
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private SpriteBatch batch;
    private Texture rockImage;
    private Texture boatImage;
    private Texture boatSpritesheet;
    private Texture trashImage;
    private Texture monsterImage;
    private RockFactory rockFactory;
    private TrashFactory trashFactory;
    private List<Rock> rocks;
    private List<Trash> trashes;
    private Boat boat;
    private TextureRegion[] boatDirectionalSprites;
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

    public GameScene(SceneManager sceneManager, SceneIOManager inputManager) {
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
        boatImage.dispose();
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
            CustomAssetManager.getInstance().loadTextureAssets("droplet.png");
            // CustomAssetManager.getInstance().loadTextureAssets("bucket.png");
            CustomAssetManager.getInstance().loadTextureAssets("steamboat_black_0001-sheet.png");
            CustomAssetManager.getInstance().loadTextureAssets("rock.png");
            CustomAssetManager.getInstance().loadTextureAssets("monster.png");
            CustomAssetManager.getInstance().loadTextureAssets("ocean_background.jpg");
            CustomAssetManager.getInstance().update();
            CustomAssetManager.getInstance().getasset_Manager().finishLoading();
            if (CustomAssetManager.getInstance().isLoaded()) {
                // boatImage = CustomAssetManager.getInstance().getAsset("bucket.png",
                // Texture.class);
                boatSpritesheet = CustomAssetManager.getInstance().getAsset("steamboat_black_0001-sheet.png", Texture.class);
                trashImage = CustomAssetManager.getInstance().getAsset("droplet.png", Texture.class);
                rockImage = CustomAssetManager.getInstance().getAsset("rock.png", Texture.class);
                monsterImage = CustomAssetManager.getInstance().getAsset("monster.png", Texture.class);
                backgroundTexture = CustomAssetManager.getInstance().getAsset("ocean_background.jpg", Texture.class);

                int frameWidth = boatSpritesheet.getWidth() / 7; // 7 columns
                int frameHeight = boatSpritesheet.getHeight() / 7; // 7 rows
                TextureRegion[][] tmpFrames = TextureRegion.split(boatSpritesheet, frameWidth, frameHeight);

                // Create array for the 4 directional sprites
                boatDirectionalSprites = new TextureRegion[4];

                // Assign specific frames for each direction
                boatDirectionalSprites[0] = tmpFrames[0][0]; // UP - row 0, col 0
                boatDirectionalSprites[1] = tmpFrames[1][4]; // RIGHT - row 1, col 4
                boatDirectionalSprites[2] = tmpFrames[3][2]; // DOWN - row 3, col 2
                boatDirectionalSprites[3] = tmpFrames[5][0]; // LEFT - row 5, col 0

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
        LOGGER.info("Configured NPC movement strategys: {0}", strategyPool.size());

        List<Entity> rockEntities = new ArrayList<>();
        rocks = new ArrayList<>();
        trashes = new ArrayList<>();
        existingEntities = new ArrayList<>();
        rockFactory = new RockFactory(constants, world, existingEntities);
        trashFactory = new TrashFactory(constants, world, existingEntities);
        Random random = new Random();

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

        // Initialize entities
        // boat = new Boat(boatEntity, world, playerMovementManager, "bucket.png");
        boat = new Boat(boatEntity, world, playerMovementManager, boatDirectionalSprites);
        monster = new Monster(monsterEntity, world, npcMovementManager, "monster.png");

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