package project.game.application.scene.main;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.entity.factory.EntityFactoryManager;
import project.game.application.entity.item.Trash;
import project.game.application.entity.npc.SeaTurtle;
import project.game.application.entity.obstacle.Rock;
import project.game.application.entity.player.Boat;
import project.game.application.scene.overlay.Options;
import project.game.application.scene.ui.AudioUI;
import project.game.common.logging.core.GameLogger;
import project.game.engine.audio.config.AudioConfig;
import project.game.engine.audio.management.AudioManager;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.management.EntityManager;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.movement.core.PlayerMovementManager;
import project.game.engine.entitysystem.physics.management.CollisionManager;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.HealthManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;
import project.game.engine.scene.management.ScoreManager;

public class GameScene2 extends Scene implements IEntityRemovalListener {
    
    private static final GameLogger LOGGER = new GameLogger(GameScene.class);

    // Menu
    private boolean isVolumePopupOpen = false;
    private boolean isMenuOpen = false;
    private InputMultiplexer inputMultiplexer;
    private Options options;
    private Window popupMenu;
    private Skin skin;
    private OrthographicCamera camera;

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
    private SeaTurtle seaTurtle;
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
    private Texture monsterImage;
    private TextureRegion[] boatDirectionalSprites;
    private TextureRegion[] rockRegions;
    private Texture[] trashTextures;
    private TextureRegion[] trashRegions;
    private TextureRegion monsterRegion;
    private Texture backgroundTexture;
    private HealthManager healthManager;
    private ScoreManager scoreManager;

    private GameScene gameScene;
    private Texture heartTexture = new Texture("droplet.png");

    public GameScene2(SceneManager sceneManager, SceneInputManager sceneInputManager) {

        super(sceneManager, sceneInputManager);
        gameScene = new GameScene(sceneManager, sceneInputManager);
        this.healthManager = HealthManager.getInstance(heartTexture);
        this.scoreManager = ScoreManager.getInstance();
    }

    @Override
    public void create() {
        gameScene.initPopUpMenu();
        gameScene.displayMessage();

        gameScene.create();

    }

    @Override
    public void render(float deltaTime) {
        gameScene.input();

        try {
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
        healthManager.draw(batch); // Draws health (now droplet asset)
        // SCORE DRAWN HERE
        skin.getFont("default-font").draw(batch, "Score: " + scoreManager.getScore(), 200, sceneUIManager.getStage().getHeight() - 30);
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
                // SCORE SYSTEM IMPLEMENTED HERE
                // scoreManager.addScore(10);
                // LOGGER.log(Level.INFO, "Score: {0}", scoreManager.getScore());

            }
        } else {
            // Process removal queue to prevent leaks
            LOGGER.warn("Not enough active bodies for physics simulation");
        }
    }

    @Override
    public void onEntityRemove(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
