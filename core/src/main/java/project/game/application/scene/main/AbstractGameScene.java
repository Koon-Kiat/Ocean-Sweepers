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

import project.game.application.entity.api.IEntityRemovalListener;
import project.game.application.entity.factory.EntityFactoryManager;
import project.game.application.entity.item.Trash;
import project.game.application.entity.obstacle.Rock;
import project.game.application.scene.overlay.Options;
import project.game.application.scene.overlay.Scenetransition;
import project.game.application.scene.ui.AudioUI;
import project.game.common.config.api.IGameConstants;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.audio.config.AudioConfig;
import project.game.engine.audio.management.AudioManager;
import project.game.engine.audio.music.MusicManager;
import project.game.engine.audio.sound.SoundManager;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.management.EntityManager;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.physics.boundary.WorldBoundaryFactory;
import project.game.engine.entitysystem.physics.management.CollisionManager;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.HealthManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;
import project.game.engine.scene.management.ScoreManager;
import project.game.engine.scene.management.TimeManager;

/**
 * Base game scene class that contains common functionality for game scenes.
 */
public abstract class AbstractGameScene extends Scene implements IEntityRemovalListener {

    protected static final GameLogger LOGGER = new GameLogger(AbstractGameScene.class);

    // Menu
    protected final HealthManager healthManager;
    protected final ScoreManager scoreManager;
    protected boolean isVolumePopupOpen = false;
    protected boolean isMenuOpen = false;
    protected InputMultiplexer inputMultiplexer;
    protected Options options;
    protected Skin skin;
    protected OrthographicCamera camera;

    // Transition
    protected Scenetransition sceneTransition;

    // Audio
    protected AudioManager audioManager;
    protected AudioConfig config;
    protected AudioUI audioUI;

    // Constants
    protected IGameConstants constants;

    // Movement
    protected NPCMovementManager npcMovementManager;
    protected List<NPCMovementManager> trashMovementManagers = new ArrayList<>();

    // Entities
    protected List<Entity> existingEntities;
    protected EntityManager entityManager;
    protected List<Rock> rocks;
    protected List<Trash> trashes;

    // Factories
    protected EntityFactoryManager entityFactoryManager;

    // Physics
    protected World world;
    protected Matrix4 debugMatrix;
    protected Box2DDebugRenderer debugRenderer;
    protected CollisionManager collisionManager;

    // Sprite sheet identifiers
    protected static final String ROCK_SPRITESHEET = "rock_sprites";

    // Rendering
    protected SpriteBatch batch;
    protected Texture rockImage;
    protected Texture trashImage;
    protected TextureRegion[] rockRegions;
    protected Texture[] trashTextures;
    protected TextureRegion[] trashRegions;
    protected Texture backgroundTexture;
    protected BitmapFont upheavalFont;

    // Timer
    protected TimeManager timer;
    protected boolean showTimer = true;

    /**
     * Constructor for the base game scene.
     * 
     * @param sceneManager  The scene manager
     * @param inputManager  The input manager
     * @param timerDuration The duration of the timer in seconds
     */
    public AbstractGameScene(SceneManager sceneManager, SceneInputManager inputManager, int timerDuration) {
        super(sceneManager, inputManager);
        this.scoreManager = ScoreManager.getInstance();
        this.timer = new TimeManager(0, timerDuration);
        this.healthManager = HealthManager.getInstance(new Texture("heart.png"));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public Skin getSkin() {
        return skin;
    }

    public void initPopUpMenu() {
        options = new Options(sceneManager, null, inputManager);
        inputMultiplexer = new InputMultiplexer();

        sceneUIManager.getStage().addActor(options.getRebindMenu());
    }

    public void setShowTimer(boolean show) {
        this.showTimer = show;
    }

    public EntityFactoryManager getEntityFactoryManager() {
        return entityFactoryManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
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
                scoreManager.addScore(100);
                break;
            }
        }
    }

    @Override
    public void render(float deltaTime) {
        input();
        timer.update(deltaTime);

        if (timer.isTimeUp()) {
            timer.stop();
            sceneManager.setScene("gameover");
            if (scoreManager.getScore() < 100000) {
                audioManager.playSoundEffect("loss");
            } else {
                audioManager.playSoundEffect("success");
                audioManager.stopMusic();
            }
            audioManager.stopMusic();
            return;
        }

        if (trashes.isEmpty()) {
            float remainingTime = timer.getRemainingTime();
            timer.stop();
            scoreManager.multiplyScore((float) (remainingTime / 100));
            scoreManager.setWinState(true);
            audioManager.stopMusic();
            audioManager.playSoundEffect("success");
            sceneManager.setScene("gameover");
            return;
        }

        try {
            if (npcMovementManager != null) {
                npcMovementManager.updateMovement();
            }

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
            float timeStep = 1.0f / 60.0f;
            int velocityIterations = 6;
            int positionIterations = 2;
            world.step(timeStep, velocityIterations, positionIterations);

            collisionManager.processRemovalQueue();
            collisionManager.processCollisions();
            collisionManager.syncEntityPositions(constants.PIXELS_TO_METERS());
        } else {
            LOGGER.warn("Not enough active bodies for physics simulation");
        }
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

        if (audioManager == null) {
            config = config != null ? config : new AudioConfig();
            audioManager = AudioManager.getInstance(MusicManager.getInstance(), SoundManager.getInstance(), config);
            LOGGER.info("Initializing AudioManager in show() method");
        }

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
        disposeEntities();
        batch.dispose();
        debugRenderer.dispose();

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }

        if (trashImage != null) {
            trashImage.dispose();
        }

        if (rockImage != null) {
            rockImage.dispose();
        }

        if (audioManager != null) {
            audioManager.dispose();
        }

        LOGGER.info("BaseGameScene disposed");
    }

    @Override
    public void create() {
        sceneTransition = new Scenetransition(sceneManager);
        batch = new SpriteBatch();
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        upheavalFont = new BitmapFont(Gdx.files.internal("upheaval.fnt"));
        inputManager.enableMovementControls();
        constants = GameConstantsFactory.getConstants();
        config = new AudioConfig();

        LOGGER.info("BaseGameScene inputManager instance: {0}", System.identityHashCode(inputManager));
        initPopUpMenu();

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

            collisionManager = new CollisionManager(world, inputManager);
            collisionManager.init();

            // Initialize EntityFactoryManager
            entityFactoryManager = new EntityFactoryManager(
                    constants,
                    world,
                    existingEntities,
                    collisionManager,
                    rockRegions,
                    trashRegions);
            entityFactoryManager.setTrashRemovalListener(this);

            // Create scene-specific entities
            createRocks();
            createTrash();
            createMainCharacter();
            createSeaTurtle();

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

            LOGGER.info("BaseGameScene initialization complete");

        } catch (Exception e) {
            LOGGER.error("Exception during game creation: {0}", e.getMessage());
            LOGGER.error("Stack trace: {0}", (Object) e.getStackTrace());
        }
    }

    protected abstract void createMainCharacter();

    protected abstract void createSeaTurtle();

    protected abstract void createRocks();

    protected abstract void createTrash();

    protected abstract void initializeGameAssets();

    protected World getWorld() {
        return world;
    }

    protected CollisionManager getCollisionManager() {
        return collisionManager;
    }

    protected List<Trash> getTrashes() {
        return trashes;
    }

    protected List<Rock> getRockEntities() {
        return rocks;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Handles key inputs for game control.
     */
    protected void input() {
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

        // End game (debugging purposes)
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
    }

    /**
     * Removes the on-screen key binding message.
     */
    protected void hideDisplayMessage() {
        sceneUIManager.getStage().getActors()
                .select(a -> a.getClass() == TextField.class)
                .forEach(Actor::remove);
    }

    /**
     * Dispose of all entities in the scene.
     */
    protected void disposeEntities() {
        LOGGER.info("Disposing all entities...");

        // Dispose of all Trash entities
        for (Trash trash : trashes) {
            if (trash.getBody() != null) {
                world.destroyBody(trash.getBody());
            }
            entityManager.removeSpriteEntity(trash);
        }
        trashes.clear();

        // Dispose of all Rock entities
        for (Rock rock : rocks) {
            if (rock.getBody() != null) {
                world.destroyBody(rock.getBody());
            }
            entityManager.removeSpriteEntity(rock);
        }
        rocks.clear();

        // Clear the existingEntities list
        if (existingEntities != null) {
            existingEntities.clear();
        }

        LOGGER.info("All entities disposed.");
    }

    /**
     * Method to be implemented by child classes to draw scene-specific UI.
     */
    protected abstract void draw();
}