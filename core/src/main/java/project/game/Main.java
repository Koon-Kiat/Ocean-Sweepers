package project.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.abstractengine.entity.movementmanager.interfaces.IMovementBehavior;
import project.game.abstractengine.entity.movementmanager.interfaces.IMovementManager;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.scenemanager.GameScene;
import project.game.abstractengine.scenemanager.MainMenuScene;
import project.game.abstractengine.scenemanager.SceneManager;
import project.game.logmanager.LogManager;


public class Main extends ApplicationAdapter {

    static {
        LogManager.initialize();
    }

    public static final float GAME_WIDTH = 640;
    public static final float GAME_HEIGHT = 480;
    private static final float PLAYER_SPEED = 1600f;
    private static final float NPC_SPEED = 500f;
    private static final float DROP_START_X = 0f;
    private static final float DROP_START_Y = 400f;
    private static final float BUCKET_START_X = 5f;
    private static final float BUCKET_START_Y = 40f;

    List<IMovementBehavior> behaviorPool = new ArrayList<>();
    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private BitmapFont font;
    private Rectangle drop;
    private Rectangle bucket;
    private Rectangle rebindRectangle;
    private ShapeRenderer shapeRenderer;
    private IMovementManager playerMovementManager;
    private Stage stage;
    private IMovementManager npcMovementManager;
    private SceneIOManager inputManager;
    private SceneManager sceneManager;
    private MainMenuScene mainMenuScene;
    private GameScene gameScene;

    @Override
    public void create() {
        // Scene Manager setup
        sceneManager = new SceneManager();
        mainMenuScene = new MainMenuScene(sceneManager);
        gameScene = new GameScene(sceneManager);
        sceneManager.addScene("menu", mainMenuScene);
        sceneManager.addScene("game", gameScene);
        System.out.println("Available scenes: " + sceneManager.getSceneList());
        sceneManager.setScene("menu");

        // IMovementManager bucketMovementManager = new PlayerMovement.Builder()
        //         .setX(50)
        //         .setY(400)
        //         .setSpeed(200f)
        //         .setDirection(Direction.NONE)
        //         .build();

        // inputManager = new SceneIOManager(bucketMovementManager);
        // Gdx.input.setInputProcessor(inputManager);

        // batch = new SpriteBatch();
        // try {
        //     dropImage = new Texture(Gdx.files.internal("droplet.png"));
        //     System.out.println("[DEBUG] Loaded droplet.png successfully.");
        // } catch (Exception e) {
        //     System.err.println("[ERROR] Failed to load droplet.png: " + e.getMessage());
        // }

        // try {
        //     bucketImage = new Texture(Gdx.files.internal("bucket.png"));
        //     System.out.println("[DEBUG] Loaded bucket.png successfully.");
        // } catch (Exception e) {
        //     System.err.println("[ERROR] Failed to load bucket.png: " + e.getMessage());
        // }

        // drop = new Rectangle();
        // drop.x = 0;
        // drop.y = 400;
        // drop.width = dropImage.getWidth();
        // drop.height = dropImage.getHeight();

        // bucket = new Rectangle();
        // bucket.x = bucketMovementManager.getX();
        // bucket.y = bucketMovementManager.getY();
        // bucket.width = bucketImage.getWidth();
        // bucket.height = bucketImage.getHeight();

        // playerMovement = new PlayerMovement.Builder()
        //         .setX(drop.x)
        //         .setY(drop.y)
        //         .setSpeed(1600f)
        //         .withConstantMovement()
        //         .setDirection(Direction.NONE)
        //         .build();

        // enemyMovement = new EnemyMovement.Builder()
        //         .setX(bucket.x)
        //         .setY(bucket.y)
        //         .setSpeed(400f)
        //         .setDirection(Direction.RIGHT)
        //         .withRandomisedMovement(playerMovement, 50f, 2f, 1f, 2f)
        //         .build();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);
        // try {
        //     updateGame();
        // } catch (Exception e) {
        //     System.err.println("[ERROR] Exception during game update: " + e.getMessage());
        //     Gdx.app.error("Main", "Exception during game update", e);
        // }

        float deltaTime = Gdx.graphics.getDeltaTime();
        // Render current scene (Scene Manager)
        sceneManager.render(deltaTime);


        // Set deltaTime for movement managers
        // playerMovement.setDeltaTime(deltaTime);
        // enemyMovement.setDeltaTime(deltaTime);

        // // Update positions
        // playerMovement.updatePosition();
        // enemyMovement.updatePosition();

        // // Update bucket position based on input
        // inputManager.getMovementManager().setDeltaTime(deltaTime);
        // inputManager.getMovementManager().updateMovement();

        // // Update rectangle positions
        // drop.x = enemyMovement.getX();
        // drop.y = enemyMovement.getY();

        // bucket.x = inputManager.getMovementManager().getX();
        // bucket.y = inputManager.getMovementManager().getY();

        // batch.begin();
        // batch.draw(dropImage, drop.x, drop.y, drop.width, drop.height);
        // batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        // batch.end();

        // Print pressed keys

        // for (Integer key : inputManager.getPressedKeys()) {
        //     System.out.println("[DEBUG] Key pressed: " + Input.Keys.toString(key));
        // }
        // // Print pressed keys
        // for (Integer key : inputManager.getPressedKeys()) {
        //     System.out.println("[DEBUG] Key pressed: " + Input.Keys.toString(key));
        // }

        // // Print mouse click status
        // if (inputManager.isMouseClicked()) {
        //     System.out.println("[DEBUG] Mouse is clicked at position: " + inputManager.getMousePosition());
        // } else {
        //     System.out.println("[DEBUG] Mouse is not clicked.");
        // }
    }

    // @Override
    // public void dispose() {
    //     batch.dispose();
    //     dropImage.dispose();
    //     bucketImage.dispose();
    // }

    // private void updateGame() {
    //     // Update player's movement based on pressed keys
    //     playerMovementManager.updateDirection(inputManager.getPressedKeys());

    //     // Update movement; exceptions here will be logged and thrown upward
    //     playerMovementManager.updateMovement();
    //     npcMovementManager.updateMovement();

    //     // Synchronize rectangle positions with movement manager positions
    //     bucket.x = playerMovementManager.getX();
    //     bucket.y = playerMovementManager.getY();
    //     drop.x = npcMovementManager.getX();
    //     drop.y = npcMovementManager.getY();
    // }

    @Override
    public void resize(int width, int height) {
        //sceneManager.resize(width, height);
    }
}
