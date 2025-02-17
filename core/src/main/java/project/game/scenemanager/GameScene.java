package project.game.scenemanager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

import project.game.Direction;
import project.game.abstractengine.entity.movementmanager.NPCMovementManager;
import project.game.abstractengine.entity.movementmanager.PlayerMovementManager;
import project.game.abstractengine.entity.movementmanager.interfaces.IMovementBehavior;
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
    private Stage stage;
    private Skin skin;
    private Table table;
    private Options options;
    private boolean isMenuOpen = false, isRebindMenuOpen = false;
    private boolean isPaused = false;
    private InputMultiplexer inputMultiplexer;

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

        stage = new Stage();

        options = new Options(sceneManager, this);
        options.create();  
        options.setMainMenuButtonVisibility(true);
        options.getPopupMenu().setTouchable(Touchable.enabled);
        inputMultiplexer = new InputMultiplexer();
        //inputMultiplexer.addProcessor(inputManager);
        //inputMultiplexer.addProcessor(stage);

        // Add popup menu to the stage
        options.getPopupMenu().setVisible(false);
        options.getPopupMenu().setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 100);
        stage.addActor(options.getPopupMenu());
        stage.addActor(options.getRebindMenu());
        
        // Add listener for interaction
        options.getPopupMenu().addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                System.out.println("[DEBUG] Popup Menu Key Pressed: " + Input.Keys.toString(keycode));
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("[DEBUG] Popup Menu Touched at: (" + x + ", " + y + ")");
                return true;
            }
        });
        
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
        //Gdx.input.setInputProcessor(inputManager);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void render(float deltaTime) {
        ScreenUtils.clear(0, 0, 0f, 0);

        //Gdx.input.setInputProcessor(inputManager);

        // Set the initial input processor to the inputManager
        // Update: using inputMultiplexer since we need to switch between inputManager and stage
        inputMultiplexer.addProcessor(inputManager);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Toggle options menu with 'P'
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            isMenuOpen = !isMenuOpen;
            //isPaused = !isPaused;
            options.getPopupMenu().setVisible(isMenuOpen);
            if (isMenuOpen) {
                isPaused = true;
                // Set input processor to stage when the menu is open
                stage.setKeyboardFocus(options.getPopupMenu()); // Force focus on the popup
                //Gdx.input.setInputProcessor(stage);
                inputMultiplexer.removeProcessor(inputManager);
                inputMultiplexer.addProcessor(0, stage);
                System.out.println("[DEBUG] InputProcessor set to stage");
                
            } else if (isRebindMenuOpen) {
                stage.getRoot().findActor("rebindMenu").setVisible(true);  
            }
            else {
                // Set input processor back to inputManager when the menu is closed
                //Gdx.input.setInputProcessor(inputManager);
                isPaused = false;
                inputMultiplexer.removeProcessor(stage);
                inputMultiplexer.addProcessor(inputManager);
                System.out.println("[DEBUG] InputProcessor set to inputManager");
            }
        }

        // Small issue here where when user hits close button instead of "P" again, the game will not unpause itself

        if (isMenuOpen) {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        } 
        else if (!isPaused) {
            try {
                updateGame();
            } catch (Exception e) {
                System.err.println("[ERROR] Exception during game update: " + e.getMessage());
                Gdx.app.error("Main", "Exception during game update", e);
            }
        }

        // Original code for updateGame

        // try {
        //     updateGame();
        // } catch (Exception e) {
        //     System.err.println("[ERROR] Exception during game update: " + e.getMessage());
        //     Gdx.app.error("Main", "Exception during game update", e);
        // }
            
        batch.begin();
        batch.draw(dropImage, drop.x, drop.y, drop.width, drop.height);
        batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        batch.end();

        // Draw the rebind rectangle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1); // Green color
        shapeRenderer.rect(rebindRectangle.x, rebindRectangle.y, rebindRectangle.width, rebindRectangle.height);
        shapeRenderer.end();

        // Check for mouse click only if not paused
        if (!isMenuOpen && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2 clickPosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            if (rebindRectangle.contains(clickPosition.x, Gdx.graphics.getHeight() - clickPosition.y)) {
                inputManager.promptForKeyBindings();
            }
        }

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
        
    }

    private void updateGame() {
        // Update player's movement based on pressed keys
        //playerMovementManager.updateDirection(inputManager.getPressedKeys());

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

    public void closePopupMenu() {
        isMenuOpen = false;
        isPaused = false;
        options.getPopupMenu().setVisible(false);
        inputMultiplexer.removeProcessor(stage);
        inputMultiplexer.addProcessor(inputManager);
        System.out.println("[DEBUG] Popup closed and game unpaused");
    }
}