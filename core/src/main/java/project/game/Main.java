package project.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;


import project.game.abstractengine.entitymanager.Entity;
import project.game.abstractengine.entitymanager.EntityManager;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.testentity.BucketEntity;
import project.game.abstractengine.testentity.DropEntity;
import project.game.builder.NPCMovementBuilder;
import project.game.builder.PlayerMovementBuilder;
import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entity.movementmanager.NPCMovementManager;
import project.game.abstractengine.entity.movementmanager.PlayerMovementManager;
import project.game.abstractengine.scenemanager.GameScene;
import project.game.abstractengine.scenemanager.MainMenuScene;
import project.game.abstractengine.scenemanager.SceneManager;
import project.game.logmanager.LogManager;


public class Main extends ApplicationAdapter {


    private SpriteBatch batch;
    private Texture dropImage;
    private Texture bucketImage;
    private DropEntity drop;
    private BucketEntity bucket;
    private PlayerMovementManager playerMovementManager;
    private NPCMovementManager npcMovementManager;
    private SceneIOManager inputManager;
    private EntityManager entityManager;

    static {
        LogManager.initialize();
    }

    public static final float GAME_WIDTH = 640;
    public static final float GAME_HEIGHT = 480;

    private SceneManager sceneManager;
    private MainMenuScene mainMenuScene;
    private GameScene gameScene;

    @Override
    public void create() {

    	
        batch = new SpriteBatch();
        //gameAsset = gameAsset.getInstance();
        entityManager = new EntityManager();
        try {
        	GameAsset.getInstance().loadTextureAssets("droplet.png");
        	GameAsset.getInstance().loadTextureAssets("bucket.png");
        	GameAsset.getInstance().update();
        	GameAsset.getInstance().getAssetManager().finishLoading();
        	if (GameAsset.getInstance().isLoaded()) {
        		dropImage = GameAsset.getInstance().getAsset("droplet.png", Texture.class);
        		bucketImage = GameAsset.getInstance().getAsset("bucket.png", Texture.class);
        	} else {
        		System.err.println("[ERROR] Asset 'droplet.png'/'bucket.png' not loaded yet");
        	}
        	
            //dropImage = new Texture("droplet.png");
            System.out.println("[DEBUG] Loaded droplet.png successfully.");
            System.out.println("[DEBUG] Loaded bucket.png successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load droplet.png: " + e.getMessage());
        }

        
        Entity genericDropEntity = new Entity(0,400, dropImage.getWidth(), dropImage.getHeight(), true);

        
        Entity genericBucketEntity = new Entity(5,40,bucketImage.getWidth(), bucketImage.getHeight(), true);

        playerMovementManager = new PlayerMovementBuilder()
                .setX(genericBucketEntity.getX())
                .setY(genericBucketEntity.getY())
                .setSpeed(1600f)
                .setDirection(Direction.NONE)
                .withConstantMovement()
                .build();
        
        
        
        npcMovementManager = new NPCMovementBuilder()
                .setX(genericDropEntity.getX())
                .setY(genericDropEntity.getY())
                .setSpeed(200f)
                .withZigZagMovement(50f, 1f)
                .setDirection(Direction.RIGHT)
                .build();

        inputManager = new SceneIOManager();
        
        bucket = new BucketEntity(genericBucketEntity, 1600f, playerMovementManager, "bucket.png");
        drop = new DropEntity(genericDropEntity, 200f, npcMovementManager, "droplet.png");
        
        entityManager.addEntity(bucket);
        entityManager.addEntity(drop);
        Gdx.input.setInputProcessor(inputManager);

        // Scene Manager setup
        sceneManager = new SceneManager();
        mainMenuScene = new MainMenuScene(sceneManager);
        gameScene = new GameScene(sceneManager);
        sceneManager.addScene("menu", mainMenuScene);
        sceneManager.addScene("game", gameScene);
        System.out.println("Available scenes: " + sceneManager.getSceneList());
        sceneManager.setScene("menu");

    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0f, 0);

        float deltaTime = Gdx.graphics.getDeltaTime();

        // Set deltaTime for movement managers
//        playerMovementManager.setDeltaTime(deltaTime);
//        npcMovementManager.setDeltaTime(deltaTime);

        // Update player's movement based on pressed keys
//        playerMovementManager.updateDirection(inputManager.getPressedKeys());

        // Update positions based on new directions
        playerMovementManager.updateMovement();
        npcMovementManager.updateMovement();

        // Update rectangle positions so the bucket follows the playerMovement position
        bucket.setX(playerMovementManager.getX());
        bucket.setY(playerMovementManager.getY());
        drop.setX(npcMovementManager.getX());
        drop.setY(npcMovementManager.getY());

        
        entityManager.checkCollision();
        
        batch.begin();
        
        entityManager.draw(batch);
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

        // Render current scene (Scene Manager)
        sceneManager.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        // sceneManager.resize(width, height);
    }
}

