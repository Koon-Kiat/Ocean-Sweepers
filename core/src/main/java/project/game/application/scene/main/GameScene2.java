package project.game.application.scene.main;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

import project.game.application.entity.item.Trash;
import project.game.application.entity.npc.SeaTurtle;
import project.game.application.entity.obstacle.Rock;
import project.game.application.entity.player.Boat;
import project.game.application.movement.builder.NPCMovementBuilder;
import project.game.application.movement.builder.PlayerMovementBuilder;
import project.game.application.movement.factory.MovementStrategyFactory;
import project.game.engine.asset.management.CustomAssetManager;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.movement.core.PlayerMovementManager;
import project.game.engine.io.management.SceneInputManager;
import project.game.engine.scene.management.SceneManager;

public class GameScene2 extends AbstractGameScene {

    // Sprite sheet identifiers
    private static final String BOAT_SPRITESHEET = "boat_sprites";

    // Entity type identifiers for directional sprites
    private static final String BOAT_ENTITY = "boat";

    // Sprite sheet identifiers
    private static final String SEA_TURTLE_SPRITESHEET = "sea_turtle_sprites";

    // Entity type identifiers for directional sprites
    private static final String SEA_TURTLE_ENTITY = "sea_turtle";

    // Boat-specific variables
    private Boat boat;
    private PlayerMovementManager playerMovementManager;
    private Texture boatSpritesheet;
    private TextureRegion[] boatTextureRegions;
    private TextureRegion[] boatDirectionalSprites;

    // Turtle-specific variables
    private SeaTurtle seaTurtle;
    private TextureRegion[] seaTurtleRegion;

    // Health
    private int turtleHealth = 3;
    private static final int MAX_TURTLE_HEALTH = 3;

    public GameScene2(SceneManager sceneManager, SceneInputManager inputManager) {
        // Call base class constructor with a timer duration of 50 seconds
        super(sceneManager, inputManager, 50);
    }

    /**
     * Check if the turtle is still alive
     */
    public boolean isTurtleAlive() {
        return turtleHealth > 0;
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
            sceneManager.setScene("gameover");
            audioManager.playSoundEffect("loss");
            audioManager.stopMusic();
        }
    }

    public void loseLife() {
        healthManager.loseLife();
    }

    @Override
    public void show() {
        turtleHealth = MAX_TURTLE_HEALTH;
        super.show();
        LOGGER.info("GameScene2 shown");
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
        // Create sea turtle entity
        CustomAssetManager assetManager = CustomAssetManager.getInstance();

        assetManager.loadTextureAssets("seaturtle.png");
        assetManager.update();
        assetManager.loadAndFinish();

        seaTurtleRegion = assetManager.createSpriteSheet(SEA_TURTLE_SPRITESHEET, "seaturtle.png", 4, 2);
        TextureRegion[] turtleDirectionalSprites = new TextureRegion[8];

        turtleDirectionalSprites[SeaTurtle.DIRECTION_UP] = seaTurtleRegion[7];
        turtleDirectionalSprites[SeaTurtle.DIRECTION_RIGHT] = seaTurtleRegion[2];
        turtleDirectionalSprites[SeaTurtle.DIRECTION_DOWN] = seaTurtleRegion[0];
        turtleDirectionalSprites[SeaTurtle.DIRECTION_LEFT] = seaTurtleRegion[1];
        turtleDirectionalSprites[SeaTurtle.DIRECTION_UP_RIGHT] = seaTurtleRegion[5];
        turtleDirectionalSprites[SeaTurtle.DIRECTION_DOWN_RIGHT] = seaTurtleRegion[3];
        turtleDirectionalSprites[SeaTurtle.DIRECTION_DOWN_LEFT] = seaTurtleRegion[4];
        turtleDirectionalSprites[SeaTurtle.DIRECTION_UP_LEFT] = seaTurtleRegion[6];

        // Register the directional sprites with the asset manager
        assetManager.registerDirectionalSprites(SEA_TURTLE_ENTITY, turtleDirectionalSprites);
        seaTurtleRegion = turtleDirectionalSprites;

        Entity seaTurtleEntity = new Entity(
                constants.SEA_TURTLE_START_X(),
                constants.SEA_TURTLE_START_Y(),
                constants.SEA_TURTLE_WIDTH(),
                constants.SEA_TURTLE_HEIGHT(),
                true);

        float[] customWeights = { 0.40f, 0.60f };

        List<Entity> rockEntities = new ArrayList<>();
        for (Rock rock : rocks) {
            rockEntities.add(rock.getEntity());
        }
        List<Trash> trashEntities = new ArrayList<>();
        for (Trash trash : trashes) {
            trashEntities.add(trash);
        }
        npcMovementManager = new NPCMovementBuilder(MovementStrategyFactory.getInstance())
                .withEntity(seaTurtleEntity)
                .setSpeed(constants.NPC_SPEED())
                .setInitialVelocity(1, 0)
                .withTrashCollector(
                        trashEntities,
                        rockEntities,
                        customWeights)
                .setLenientMode(true)
                .build();

        seaTurtle = new SeaTurtle(seaTurtleEntity, world, npcMovementManager, seaTurtleRegion);
        seaTurtle.setEntityRemovalListener(this);
        seaTurtle.setCollisionManager(collisionManager);

        // Set health callback for the turtle
        seaTurtle.setHealthCallback(() -> {
            reduceTurtleHealth();
        });

        entityManager.addSpriteEntity(seaTurtle);
        collisionManager.addEntity(seaTurtle, npcMovementManager);
        existingEntities.add(seaTurtleEntity);
    }

    @Override
    protected void createRocks() {
        int numRocks = constants.NUM_ROCKS();
        for (int i = 0; i < numRocks; i++) {
            Rock rock = entityFactoryManager.createRock();
            rocks.add(rock);
            entityManager.addSpriteEntity(rock);
            collisionManager.addEntity(rock, null);
            existingEntities.add(rock.getEntity());
        }
        LOGGER.info("Created " + numRocks + " rocks for GameScene2");
    }

    @Override
    protected void createTrash() {
        // Create more trash for the turtle to collect in this scene
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
                LOGGER.info("Created and registered trash entity " + i + " with movement manager");
            }
        }
        LOGGER.info("Created " + numTrash + " trash objects for GameScene2");
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
        upheavalFont.draw(batch, String.format("Time: %02d:%02d",
                timer.getMinutes(), timer.getSeconds()), 500, sceneUIManager.getStage().getHeight() - 60);

        // Adding a label for turtle health
        upheavalFont.draw(batch, "Turtle Health:", 50,
                sceneUIManager.getStage().getHeight() - 60);
        // Draw turtle health
        healthManager.draw(batch, 300, sceneUIManager.getStage().getHeight() - 100, turtleHealth);

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
            LOGGER.error("Exception during game update: {0}", e.getMessage());
        }

        draw();

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

        if (seaTurtle != null) {
            if (seaTurtle.getBody() != null) {
                world.destroyBody(seaTurtle.getBody());
            }
            entityManager.removeSpriteEntity(seaTurtle);
            seaTurtle = null;
        }
    }
}