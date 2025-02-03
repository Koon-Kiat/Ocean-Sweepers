package project.game.scenemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import project.game.iomanager.SceneIOManager;
import project.game.movementmanager.Direction;
import project.game.movementmanager.EnemyMovement;
import project.game.movementmanager.PlayerMovement;
import project.game.movementmanager.interfaces.IMovementManager;


public class GameScene extends Scene {
    private SceneManager sceneManager;
    private PlayerMovement playerMovement;
    private EnemyMovement enemyMovement;
    private SceneIOManager inputManager;
    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private Rectangle drop;
    private Rectangle bucket;

    // public GameScene() {
    //     sceneManager = new SceneManager();
    //     sceneManager.addScene("menu", new MainMenuScene());
    //     sceneManager.setScene("menu");
    // }

    public GameScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        
        IMovementManager bucketMovementManager = new PlayerMovement.Builder()
                .setX(50)
                .setY(400)
                .setSpeed(200f)
                .setDirection(Direction.NONE)
                .build();

        inputManager = new SceneIOManager(bucketMovementManager);
        Gdx.input.setInputProcessor(inputManager);

        
        try {
            dropImage = new Texture(Gdx.files.internal("droplet.png"));
            System.out.println("[DEBUG] Loaded droplet.png successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load droplet.png: " + e.getMessage());
        }

        try {
            bucketImage = new Texture(Gdx.files.internal("bucket.png"));
            System.out.println("[DEBUG] Loaded bucket.png successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load bucket.png: " + e.getMessage());
        }

        drop = new Rectangle();
        drop.x = 0;
        drop.y = 400;
        drop.width = dropImage.getWidth();
        drop.height = dropImage.getHeight();

        bucket = new Rectangle();
        bucket.x = bucketMovementManager.getX();
        bucket.y = bucketMovementManager.getY();
        bucket.width = bucketImage.getWidth();
        bucket.height = bucketImage.getHeight();

        playerMovement = new PlayerMovement.Builder()
                .setX(drop.x)
                .setY(drop.y)
                .setSpeed(1600f)
                .withConstantMovement()
                .setDirection(Direction.NONE)
                .build();

        enemyMovement = new EnemyMovement.Builder()
                .setX(bucket.x)
                .setY(bucket.y)
                .setSpeed(400f)
                .setDirection(Direction.RIGHT)
                .withRandomisedMovement(playerMovement, 50f, 2f, 1f, 2f)
                .build();
    }

    public void render(float deltaTime) {
        //float deltaTime = Gdx.graphics.getDeltaTime();

        // Set deltaTime for movement managers
        playerMovement.setDeltaTime(deltaTime);
        enemyMovement.setDeltaTime(deltaTime);

        // Update positions
        playerMovement.updatePosition();
        enemyMovement.updatePosition();

        // Update bucket position based on input
        inputManager.getMovementManager().setDeltaTime(deltaTime);
        inputManager.getMovementManager().updateMovement();

        // Update rectangle positions
        drop.x = enemyMovement.getX();
        drop.y = enemyMovement.getY();

        bucket.x = inputManager.getMovementManager().getX();
        bucket.y = inputManager.getMovementManager().getY();

        batch.begin();
        batch.draw(dropImage, drop.x, drop.y, drop.width, drop.height);
        batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        batch.end();

        // Print pressed keys
        for (Integer key : inputManager.getPressedKeys()) {
            System.out.println("[DEBUG] Key pressed: " + Input.Keys.toString(key));
        }

        // Print mouse click status
        if (inputManager.isMouseClicked()) {
            System.out.println("[DEBUG] Mouse is clicked at position: " + inputManager.getMousePosition());
        } else {
            System.out.println("[DEBUG] Mouse is not clicked.");
        }
    }

}
