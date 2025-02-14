package project.game.abstractengine.scenemanager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.Direction;
import project.game.abstractengine.entitysystem.interfaces.IMovementBehavior;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.builder.NPCMovementBuilder;
import project.game.builder.PlayerMovementBuilder;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;
import project.game.logmanager.LogManager;

public class GameScene extends Scene {

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

    private SceneManager sceneManager;
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private SceneIOManager inputManager;
    private Rectangle rebindRectangle;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private Rectangle drop;
    private Rectangle bucket;
    private Window popupMenu;
    private Stage stage;
    private Skin skin;
    private Table table;

    // public GameScene() {
    // sceneManager = new SceneManager();
    // sceneManager.addScene("menu", new MainMenuScene());
    // sceneManager.setScene("menu");
    // }

    public GameScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        inputManager = new SceneIOManager();
        rebindRectangle = new Rectangle(50, 50, 150, 50);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        stage = new Stage();
        table = new Table();

        popupMenu = new Window("Pop up", skin);
        popupMenu.setSize(200, 150);
        popupMenu.setPosition(300, 300);
        popupMenu.setVisible(false);

        TextButton button1 = new TextButton("Rebind Keys", skin);
        TextButton button2 = new TextButton("Return to main menu", skin);
        TextButton button3 = new TextButton("Close", skin);

        // Button listeners (Debug for now)
        button1.addListener(event -> {
            System.out.println("'Rebind keys' selected");
            return true;
        });

        button2.addListener(event -> {
            System.out.println("'Return to main menu' selected");
            return true;
        });

        button3.addListener(event -> {
            popupMenu.setVisible(false);
            return true;
        });

        Table table = new Table();
        table.add(button1).fillX().pad(5);
        table.row();
        table.add(button2).fillX().pad(5);
        table.row();
        table.add(button3).fillX().pad(5);

        popupMenu.add(table);
        stage.addActor(popupMenu);

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

        drop = new Rectangle(DROP_START_X, DROP_START_Y, dropImage.getWidth(), dropImage.getHeight());
        bucket = new Rectangle(BUCKET_START_X, BUCKET_START_Y, bucketImage.getWidth(), bucketImage.getHeight());

        playerMovementManager = new PlayerMovementBuilder()
                .setX(bucket.x)
                .setY(bucket.y)
                .setSpeed(PLAYER_SPEED)
                .withAcceleratedMovement(1000f, 1500f)
                .build();

        behaviorPool = new ArrayList<>();
        behaviorPool.add(new ConstantMovementBehavior(NPC_SPEED));
        behaviorPool.add(new ZigZagMovementBehavior(NPC_SPEED, 100f, 5f));
        behaviorPool.add(new FollowMovementBehavior(playerMovementManager, NPC_SPEED));

        npcMovementManager = new NPCMovementBuilder()
                .setX(drop.x)
                .setY(drop.y)
                .setSpeed(NPC_SPEED)
                .withRandomisedMovement(behaviorPool, 1f, 2f)
                .setDirection(Direction.RIGHT)
                .build();

        inputManager = new SceneIOManager();
        Gdx.input.setInputProcessor(inputManager);
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(0, 0, 0f, 0);

        Gdx.input.setInputProcessor(inputManager);

        try {
            updateGame();
        } catch (Exception e) {
            System.err.println("[ERROR] Exception during game update: " + e.getMessage());
            Gdx.app.error("Main", "Exception during game update", e);
        }

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
            System.out.println("[DEBUG] Mouse is clicked at position: " +
                    inputManager.getMousePosition());
        } else {
            System.out.println("[DEBUG] Mouse is not clicked.");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            popupMenu.setVisible(!popupMenu.isVisible());
        }
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void updateGame() {
        // Update player's movement based on pressed keys
        // playerMovementManager.updateDirection(inputManager.getPressedKeys());

        playerMovementManager.updateDirection(inputManager.getPressedKeys(), inputManager.getKeyBindings());

        // Update movement; exceptions here will be logged and thrown upward
        playerMovementManager.updateMovement();
        npcMovementManager.updateMovement();

        // Synchronize rectangle positions with movement manager positions
        bucket.x = playerMovementManager.getX();
        bucket.y = playerMovementManager.getY();
        drop.x = npcMovementManager.getX();
        drop.y = npcMovementManager.getY();
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropImage.dispose();
        bucketImage.dispose();
    }

}