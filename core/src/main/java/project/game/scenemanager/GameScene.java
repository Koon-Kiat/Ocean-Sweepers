package project.game.scenemanager;

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

import project.game.iomanager.SceneIOManager;
import project.game.movementmanager.Direction;
import project.game.movementmanager.EnemyMovement;
import project.game.movementmanager.PlayerMovement;


public class GameScene extends Scene {
    private SceneManager sceneManager;
    private PlayerMovement playerMovement;
    private EnemyMovement enemyMovement;
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
        ScreenUtils.clear(0, 0, 0f, 0);
        // Set deltaTime for movement managers
        playerMovement.setDeltaTime(deltaTime);
        enemyMovement.setDeltaTime(deltaTime);

        Gdx.input.setInputProcessor(inputManager);

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
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            popupMenu.setVisible(!popupMenu.isVisible());
        }
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

}