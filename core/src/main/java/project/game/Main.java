package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.iomanager.SceneIOManager;
import project.game.movementmanager.Direction;
import project.game.movementmanager.EnemyMovement;
import project.game.movementmanager.interfaces.IMovementManager;
import project.game.movementmanager.PlayerMovement;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private BitmapFont font;
    private Rectangle drop;
    private Rectangle bucket;
    private Rectangle rebindRectangle;
    private ShapeRenderer shapeRenderer;
    private PlayerMovement playerMovement;
    private EnemyMovement enemyMovement;
    private SceneIOManager inputManager;

    public static final float GAME_WIDTH = 640f;
    public static final float GAME_HEIGHT = 480f;

    @Override
    public void create() {

        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        inputManager = new SceneIOManager();
        Gdx.input.setInputProcessor(inputManager);
        // inputManager.promptForKeyBindings();

        batch = new SpriteBatch();
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
        // bucket.x = bucketMovementManager.getX();
        // bucket.y = bucketMovementManager.getY();
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
        
        // Create a rectangle in the middle of the screen for key rebind prompt
        rebindRectangle = new Rectangle();
        rebindRectangle.width = 200;
        rebindRectangle.height = 50;
        rebindRectangle.x = (Gdx.graphics.getWidth() - rebindRectangle.width) / 2;
        rebindRectangle.y = (Gdx.graphics.getHeight() - rebindRectangle.height) / 2;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();
        // Set deltaTime for movement managers
        playerMovement.setDeltaTime(deltaTime);
        enemyMovement.setDeltaTime(deltaTime);

        // Update player's movement based on pressed keys
        playerMovement.updateDirection(inputManager.getPressedKeys(), inputManager.getKeyBindings());
        // Update positions based on new directions
        playerMovement.updatePosition();
        enemyMovement.updatePosition();

        // Update rectangle positions so the bucket follows the playerMovement position
        bucket.x = playerMovement.getX();
        bucket.y = playerMovement.getY();
        drop.x = enemyMovement.getX();
        drop.y = enemyMovement.getY();

        batch.begin();
        batch.draw(dropImage, drop.x, drop.y, drop.width, drop.height);
        batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        batch.end();

        // Draw the rebind rectangle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1); // Green color
        shapeRenderer.rect(rebindRectangle.x, rebindRectangle.y, rebindRectangle.width, rebindRectangle.height);
        shapeRenderer.end();

        // Draw the text on the rebind rectangle
        batch.begin();
        font.draw(batch, "Rebind Keys", rebindRectangle.x + 20, rebindRectangle.y + 30);
        batch.end();

        
        // Check for mouse click within the rebind rectangle
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2 clickPosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            if (rebindRectangle.contains(clickPosition.x, Gdx.graphics.getHeight() - clickPosition.y)) {
                inputManager.promptForKeyBindings();
            }
        }
        
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

    @Override
    public void dispose() {
        batch.dispose();
        dropImage.dispose();
        bucketImage.dispose();
    }
}
