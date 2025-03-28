package project.game.application.scene.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

import project.game.application.entity.item.Trash;
import project.game.application.entity.obstacle.Rock;
import project.game.application.entity.player.Boat;
import project.game.application.movement.builder.PlayerMovementBuilder;
import project.game.application.movement.factory.MovementStrategyFactory;
import project.game.engine.asset.management.CustomAssetManager;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.movement.core.PlayerMovementManager;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.SceneManager;

public class GameScene1 extends AbstractGameScene {

    // Sprite sheet identifiers
    private static final String BOAT_SPRITESHEET = "boat_sprites";

    // Entity type identifiers for directional sprites
    private static final String BOAT_ENTITY = "boat";

    // Boat-specific variables
    private Boat boat;
    private PlayerMovementManager playerMovementManager;
    private Texture boatSpritesheet;
    private TextureRegion[] boatTextureRegions;
    private TextureRegion[] boatDirectionalSprites;

    // Constant

    public GameScene1(SceneManager sceneManager, SceneInputManager inputManager) {
        super(sceneManager, inputManager, 50);
    }

    public void loseLife() {
        healthManager.loseLife();
    }

    @Override
    protected void initializeGameAssets() {
        CustomAssetManager assetManager = CustomAssetManager.getInstance();

        // Load all texture assets first
        assetManager.loadTextureAssets("trash1.png");
        assetManager.loadTextureAssets("trash2.png");
        assetManager.loadTextureAssets("trash3.png");
        assetManager.loadTextureAssets("steamboat.png");
        assetManager.loadTextureAssets("Rocks.png");
        assetManager.loadTextureAssets("ocean_background.jpg");
        assetManager.update();
        assetManager.loadAndFinish();

        // Get background texture directly
        backgroundTexture = assetManager.getAsset("ocean_background.jpg", Texture.class);

        // Create and store boat sprite sheet (7x7)
        boatSpritesheet = assetManager.getAsset("steamboat.png", Texture.class);
        boatTextureRegions = assetManager.createSpriteSheet(BOAT_SPRITESHEET, "steamboat.png", 7, 7);

        // Create boat directional sprites for all 8 directions
        TextureRegion[] eightDirectionalSprites = new TextureRegion[8];
        eightDirectionalSprites[Boat.DIRECTION_UP] = boatTextureRegions[0];
        eightDirectionalSprites[Boat.DIRECTION_RIGHT] = boatTextureRegions[11];
        eightDirectionalSprites[Boat.DIRECTION_DOWN] = boatTextureRegions[23];
        eightDirectionalSprites[Boat.DIRECTION_LEFT] = boatTextureRegions[35];
        eightDirectionalSprites[Boat.DIRECTION_UP_RIGHT] = boatTextureRegions[7];
        eightDirectionalSprites[Boat.DIRECTION_DOWN_RIGHT] = boatTextureRegions[14];
        eightDirectionalSprites[Boat.DIRECTION_DOWN_LEFT] = boatTextureRegions[28];
        eightDirectionalSprites[Boat.DIRECTION_UP_LEFT] = boatTextureRegions[42];

        // Register the directional sprites with the asset manager
        assetManager.registerDirectionalSprites(BOAT_ENTITY, eightDirectionalSprites);
        boatDirectionalSprites = eightDirectionalSprites;

        // Create rock sprite sheet (3x3)
        rockImage = assetManager.getAsset("Rocks.png", Texture.class);
        rockRegions = assetManager.createSpriteSheet(ROCK_SPRITESHEET, "Rocks.png", 3, 3);

        // Load trash textures and create TextureRegions
        trashTextures = new Texture[3];
        trashRegions = new TextureRegion[3];
        String[] trashPaths = { "trash1.png", "trash2.png", "trash3.png" };

        for (int i = 0; i < trashPaths.length; i++) {
            trashTextures[i] = assetManager.getAsset(trashPaths[i], Texture.class);
            trashRegions[i] = new TextureRegion(trashTextures[i]);
        }

        // Store first trash texture for reference
        trashImage = trashTextures[0];

        LOGGER.info("Game assets initialized successfully");
    }

    @Override
    protected void createMainCharacter() {
        // Create boat (player) entity
        Entity boatEntity = new Entity(
                constants.PLAYER_START_X(),
                constants.PLAYER_START_Y(),
                constants.PLAYER_WIDTH(),
                constants.PLAYER_HEIGHT(),
                true);

        playerMovementManager = new PlayerMovementBuilder(MovementStrategyFactory.getInstance())
                .withEntity(boatEntity)
                .setSpeed(constants.PLAYER_SPEED())
                .setInitialVelocity(0, 0)
                .setLenientMode(true)
                .withConstantMovement()
                .build();

        boat = new Boat(boatEntity, world, playerMovementManager, boatDirectionalSprites);
        boat.setCollisionManager(collisionManager);

        // Add boat to managers
        entityManager.addSpriteEntity(boat);
        collisionManager.addEntity(boat, playerMovementManager);
        existingEntities.add(boatEntity);

        // Set life loss callback for boat
        boat.setLifeLossCallback(() -> {
            loseLife();
            audioManager.playSoundEffect("collision");
            if (healthManager.getLives() == 0) {
                sceneManager.setScene("gameover");
                audioManager.playSoundEffect("loss");
                audioManager.stopMusic();
                audioManager.hideVolumeControls();
                options.getRebindMenu().setVisible(false);
            }
        });
    }

    @Override
    protected void createSeaTurtle() {

    }

    @Override
    protected void createRocks() {
        // Create a standard number of rocks for the first scene
        int numRocks = constants.NUM_ROCKS();
        for (int i = 0; i < numRocks; i++) {
            Rock rock = entityFactoryManager.createRock();
            rocks.add(rock);
            entityManager.addSpriteEntity(rock);
            collisionManager.addEntity(rock, null);
            existingEntities.add(rock.getEntity());
        }
        LOGGER.info("Created " + numRocks + " rocks for GameScene1");
    }

    @Override
    protected void createTrash() {
        // Create a standard number of trash objects for the first scene
        int numTrash = constants.NUM_TRASHES();
        for (int i = 0; i < numTrash; i++) {
            Trash trash = entityFactoryManager.createTrash();
            if (trash != null) {
                trashes.add(trash);
                entityManager.addSpriteEntity(trash);

                // Get and store the movement manager
                NPCMovementManager trashMovementManager = trash.getMovementManager();
                if (trashMovementManager != null) {
                    trashMovementManagers.add(trashMovementManager);
                    collisionManager.addEntity(trash, trashMovementManager);
                }

                existingEntities.add(trash.getEntity());
            }
        }
        LOGGER.info("Created " + numTrash + " trash objects for GameScene1");
    }

    @Override
    protected void draw() {
        // Regular rendering code
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, constants.GAME_WIDTH(), constants.GAME_HEIGHT());
        batch.end();

        // Draw entities
        batch.begin();
        entityManager.draw(batch);

        // Adding a label for player health
        upheavalFont.draw(batch, "Player Health:", 50,
                sceneUIManager.getStage().getHeight() - 30);
        // Draw health and score
        healthManager.draw(batch, 300, sceneUIManager.getStage().getHeight() - 60, healthManager.getLives());

        // print score
        upheavalFont.draw(batch, "Score: " + scoreManager.getScore(), 500,
                sceneUIManager.getStage().getHeight() - 30);

        // Print timer
        if (showTimer) {
            upheavalFont.draw(batch, String.format("Time: %02d:%02d",
                    timer.getMinutes(), timer.getSeconds()), 500, sceneUIManager.getStage().getHeight() - 60);
        }

        batch.end();

        // Draw stage
        sceneUIManager.getStage().act(Gdx.graphics.getDeltaTime());
        sceneUIManager.getStage().draw();
    }

    @Override
    protected void input() {
        // First call the base class input method to handle common inputs
        super.input();

        // Switch to game2 scene (turtle scene)
        if (inputManager.isKeyJustPressed(Input.Keys.N)) {
            if (!"GameScene".equals(sceneManager.getPreviousScene())) {
                audioManager.stopMusic();
                sceneManager.setScene("game2");
            } else {
                LOGGER.info("Already in GameScene2, ignoring key press.");
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
            if (scoreManager.hasWon() == false) {
                audioManager.playSoundEffect("loss");
            } else {
                audioManager.playSoundEffect("success");
            }
            audioManager.stopMusic();
            return;
        }

        try {
            if (playerMovementManager != null) {
                playerMovementManager.updateMovement();
            }

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
            LOGGER.error("Exception during game update: " + e.getMessage());
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

        if (trashes.isEmpty()) {
            float remainingTime = timer.getRemainingTime();
            scoreManager.multiplyScore((float) (remainingTime / 100));
            scoreManager.setWinState(true);
            sceneManager.setScene("gameover");
            audioManager.playSoundEffect("success");
            audioManager.stopMusic();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if (boatSpritesheet != null) {
            boatSpritesheet.dispose();
        }

        // Dispose of the Boat if it wasn't already handled by disposeEntities
        if (boat != null) {
            if (boat.getBody() != null) {
                world.destroyBody(boat.getBody());
            }
            entityManager.removeSpriteEntity(boat);
            boat = null;
        }
    }
}