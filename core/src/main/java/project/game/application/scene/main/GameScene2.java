package project.game.application.scene.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.List;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.api.entity.ILifeLossCallback;
import project.game.application.entity.item.Trash;
import project.game.application.entity.npc.SeaTurtle;
import project.game.application.movement.builder.NPCMovementBuilder;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.asset.management.CustomAssetManager;
import project.game.engine.audio.management.AudioManager;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.management.EntityManager;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.physics.management.CollisionManager;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.HealthManager;
import project.game.engine.scene.management.Scene;
import project.game.engine.scene.management.SceneManager;
import project.game.engine.scene.management.ScoreManager;
import project.game.engine.scene.management.TimeManager;

public class GameScene2 extends Scene implements IEntityRemovalListener {

    private static final GameLogger LOGGER = new GameLogger(GameScene2.class);

    private IGameConstants constants;

    private HealthManager healthManager;
    private final AudioManager audioManager;
    private final ScoreManager scoreManager;
    private NPCMovementManager npcMovementManager;
    private CollisionManager collisionManager;
    private EntityManager entityManager;
    private CustomAssetManager assetManager;
    private final TimeManager timer;

    private final GameScene gameScene;
    private Texture heartTexture;
    private SpriteBatch batch;
    private Skin skin;
    private BitmapFont upheavalFont;
    private float remainingTime;

    private SeaTurtle seaTurtle;
    private Texture seaTurtleImage;
    private TextureRegion[] seaTurtleRegion;
    private static final String SEA_TURTLE_SPRITESHEET = "sea_turtle_sprites";
    private static final String SEA_TURTLE_ENTITY = "sea_turtle";

    private int turtleHealth = 3;
    private Texture turtleHeartTexture;
    private static final int MAX_TURTLE_HEALTH = 3;

    public GameScene2(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager);
        this.gameScene = new GameScene(sceneManager, inputManager);
        this.scoreManager = ScoreManager.getInstance();
        this.timer = new TimeManager(0, 50);
        // 
        this.audioManager = gameScene.getAudioManager(); 
        // Remove the healthManager initialization here, we'll do it in create()
        LOGGER.info("GameScene2 created with composition of GameScene");
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        upheavalFont = new BitmapFont(Gdx.files.internal("upheaval.fnt"));

        // Load heart texture once and use it for both boat and turtle
        heartTexture = new Texture("heart.png");
        // Use the same texture for turtle hearts
        turtleHeartTexture = heartTexture;

        // Initialize HealthManager with the loaded texture
        this.healthManager = HealthManager.getInstance(heartTexture);

        if (gameScene != null) {
            gameScene.create();
        }
    }

    @Override
    public void render(float deltaTime) {
        gameScene.timer.stop();
        timer.update(deltaTime);

        if (timer.isTimeUp()) {
            timer.stop();
            sceneManager.setScene("gameover");
            if (scoreManager.getScore() < 500) {
                audioManager.playSoundEffect("loss");
            } else {
                audioManager.playSoundEffect("success");
                audioManager.stopMusic();
            }
            audioManager.stopMusic();
            return;
        }
        List<Trash> trashes = gameScene.getTrashes();
        if (gameScene.getTrashes().isEmpty()) {
            float remainingTime = timer.getRemainingTime(); // use your TimeManager's method
            timer.stop();
            scoreManager.multiplyScore(remainingTime / 100f);
            scoreManager.setWinState(true);
            audioManager.stopMusic();
            audioManager.playSoundEffect("success");
            sceneManager.setScene("gameover");
            return;
        }

        gameScene.render(deltaTime);
        batch.begin();
        upheavalFont.draw(batch, String.format("Time: %02d:%02d",
                timer.getMinutes(), timer.getSeconds()), 500, sceneUIManager.getStage().getHeight() - 60);

        // Adding a label for turtle health
        upheavalFont.draw(batch, "Turtle Health:", 50,
                sceneUIManager.getStage().getHeight() - 60);

        healthManager.draw(batch, 300, sceneUIManager.getStage().getHeight() - 100, turtleHealth);

        batch.end();

    }

    @Override
    public void hide() {
        if (gameScene != null) {
            gameScene.hide();
        }
    }

    @Override
    public void pause() {
        if (gameScene != null) {
            gameScene.pause();
        }
    }

    @Override
    public void resume() {
        if (gameScene != null) {
            gameScene.resume();
        }
    }

    @Override
    public void dispose() {
        if (gameScene != null) {
            gameScene.dispose();
        }

        if (heartTexture != null) {
            heartTexture.dispose();
        }

        // Don't dispose turtleHeartTexture since it's the same as heartTexture
        // This would cause a double-free error

        LOGGER.info("GameScene2 disposed");
    }

    @Override
    public void show() {

        super.show();
        turtleHealth = MAX_TURTLE_HEALTH;
        timer.resetTime();
        timer.start();
        gameScene.setShowTimer(false);

        if (gameScene != null) {
            gameScene.show();
            gameScene.getEntityFactoryManager().setTrashRemovalListener(GameScene2.this); //addition

            // Reset input state to prevent constant movement
            inputManager.resetInputState();

            entityManager = gameScene.getEntityManager();
            collisionManager = gameScene.getCollisionManager();
            assetManager = CustomAssetManager.getInstance();

            // Load the sea turtle texture and explicitly finish loading before using it
            assetManager.loadTextureAssets("seaturtle.png");
            assetManager.update();
            assetManager.loadAndFinish(); // Ensure all assets are fully loaded before continuing

            // Only proceed if the asset is actually loaded
            seaTurtleImage = assetManager.getAsset("seaturtle.png", Texture.class);
            seaTurtleRegion = assetManager.createSpriteSheet(SEA_TURTLE_SPRITESHEET, "seaturtle.png", 4, 2);
            TextureRegion[] turtleDirectionalSprites = new TextureRegion[8];

            turtleDirectionalSprites[SeaTurtle.DIRECTION_UP] = seaTurtleRegion[7]; // UP
            turtleDirectionalSprites[SeaTurtle.DIRECTION_RIGHT] = seaTurtleRegion[2]; // RIGHT
            turtleDirectionalSprites[SeaTurtle.DIRECTION_DOWN] = seaTurtleRegion[0]; // DOWN
            turtleDirectionalSprites[SeaTurtle.DIRECTION_LEFT] = seaTurtleRegion[1]; // LEFT

            turtleDirectionalSprites[SeaTurtle.DIRECTION_UP_RIGHT] = seaTurtleRegion[5]; // UP
            turtleDirectionalSprites[SeaTurtle.DIRECTION_DOWN_RIGHT] = seaTurtleRegion[3]; // RIGHT
            turtleDirectionalSprites[SeaTurtle.DIRECTION_DOWN_LEFT] = seaTurtleRegion[4]; // DOWN
            turtleDirectionalSprites[SeaTurtle.DIRECTION_UP_LEFT] = seaTurtleRegion[6]; // LEFT

            // Register the directional sprites with the asset manager
            assetManager.registerDirectionalSprites(SEA_TURTLE_ENTITY, turtleDirectionalSprites);
            seaTurtleRegion = turtleDirectionalSprites;

            constants = GameConstantsFactory.getConstants();

            Entity seaTurtleEntity = new Entity(
                    constants.SEA_TURTLE_START_X(),
                    constants.SEA_TURTLE_START_Y(),
                    constants.SEA_TURTLE_WIDTH(),
                    constants.SEA_TURTLE_HEIGHT(),
                    true);

            float[] customWeights = { 0.40f, 0.60f };
            npcMovementManager = new NPCMovementBuilder()
                    .withEntity(seaTurtleEntity)
                    .setSpeed(constants.NPC_SPEED())
                    .setInitialVelocity(1, 0)
                    .withTrashCollector(gameScene.getTrashes(), gameScene.getRockEntities(), customWeights)
                    .setLenientMode(true)
                    .build();

            seaTurtle = new SeaTurtle(seaTurtleEntity, gameScene.getWorld(), npcMovementManager, seaTurtleRegion);
            seaTurtle.setCollisionManager(gameScene.getCollisionManager());

            // Set health callback for the turtle
            seaTurtle.setHealthCallback(new ILifeLossCallback() {
                @Override
                public void onLifeLost() {
                    reduceTurtleHealth();
                }
            });

            entityManager.addRenderableEntity(seaTurtle);
            collisionManager.addEntity(seaTurtle, npcMovementManager);

            LOGGER.info("GameScene2 shown (delegated to GameScene)");
        } else {
            LOGGER.error("Failed to load seaturtle.png asset");

        }
    }

    @Override
    public void onEntityRemove(Entity entity) {
        if (gameScene != null) {
            gameScene.onEntityRemove(entity);
        }
    }

    public void reduceTurtleHealth() {
        turtleHealth--;
        LOGGER.info("Turtle health reduced to " + turtleHealth);

        // Play sound effect for health loss
        audioManager.playSoundEffect("collision");

        if (turtleHealth <= 0) {
            // Turtle has died
            LOGGER.info("Turtle died!");
            timer.stop();
            audioManager.stopMusic();
            audioManager.playSoundEffect("loss");
            sceneManager.setScene("gameover");
            
            
        }
    }

    

    /**
     * Check if the turtle is still alive
     */
    public boolean isTurtleAlive() {
        return turtleHealth > 0;
    }
}
