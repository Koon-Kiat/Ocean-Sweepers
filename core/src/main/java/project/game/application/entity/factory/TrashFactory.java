package project.game.application.entity.factory;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.entity.item.Trash;
import project.game.application.movement.builder.NPCMovementBuilder;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class TrashFactory extends AbstractEntityFactory<Trash> {
    private static final GameLogger LOGGER = new GameLogger(TrashFactory.class);
    private static final float BASE_SPEED_MIN = 50f;
    private static final float BASE_SPEED_MAX = 100f;
    private static final float ZIG_SPEED_MIN = 30f;
    private static final float ZIG_SPEED_MAX = 50f;
    private static final float MIN_AMPLITUDE = 6f;
    private static final float MAX_AMPLITUDE = 15f;
    private static final float MIN_FREQUENCY = 0.2f;
    private static final float MAX_FREQUENCY = 0.5f;
    private static final float DEFAULT_CONSTANT_WEIGHT = 0.9f;
    private static final float DEFAULT_ZIGZAG_WEIGHT = 0.1f;
    private final TextureRegion[] trashTextures;
    private final java.util.Random random;
    private IEntityRemovalListener removalListener;

    public TrashFactory(
            IGameConstants constants,
            World world,
            List<Entity> existingEntities,
            CollisionManager collisionManager,
            TextureRegion[] trashTextures) {
        super(constants, world, existingEntities, collisionManager);
        this.trashTextures = trashTextures;
        this.random = new java.util.Random();
    }

    public void setRemovalListener(IEntityRemovalListener removalListener) {
        this.removalListener = removalListener;
    }

    /**
     * Creates a new Trash entity at the specified position.
     * 
     * @param x The x-coordinate for the trash.
     * @param y The y-coordinate for the trash.
     * @return A new Trash entity.
     */
    @Override
    public Trash createEntity(float x, float y) {
        Entity trashEntity = new Entity(x, y, constants.TRASH_WIDTH(), constants.TRASH_HEIGHT(), true);
        TextureRegion selectedTexture = trashTextures[random.nextInt(trashTextures.length)];

        // Create NPCMovementManager with ocean current simulation movement
        NPCMovementManager movementManager = createTrashMovement(trashEntity);

        Trash trash = new Trash(trashEntity, world, selectedTexture);

        // Set up the trash object with required components
        if (collisionManager != null) {
            trash.setCollisionManager(collisionManager);
        }

        if (removalListener != null) {
            trash.setRemovalListener(removalListener);
        }

        // Set the movement manager and register with collision manager
        trash.setMovementManager(movementManager);

        // Add to list of existing entities
        existingEntities.add(trashEntity);

        return trash;
    }

    /**
     * Creates a movement manager for trash that simulates ocean currents.
     */
    private NPCMovementManager createTrashMovement(Entity trashEntity) {
        try {
            // Generate dominant flow direction (mostly horizontal)
            float dominantDirection = MathUtils.randomBoolean() ? 1f : -1f; // Left or right flow
            float dirX = dominantDirection;
            float dirY = MathUtils.random(-0.3f, 0.3f); // Slight vertical drift

            return new NPCMovementBuilder()
                    .withEntity(trashEntity)
                    .setSpeed(MathUtils.random(BASE_SPEED_MIN, BASE_SPEED_MAX))
                    .setInitialVelocity(dirX, dirY)
                    .setLenientMode(true)
                    .withRandomizedOceanCurrentMovement(
                            BASE_SPEED_MIN, BASE_SPEED_MAX,
                            ZIG_SPEED_MIN, ZIG_SPEED_MAX,
                            MIN_AMPLITUDE, MAX_AMPLITUDE,
                            MIN_FREQUENCY, MAX_FREQUENCY,
                            DEFAULT_CONSTANT_WEIGHT, DEFAULT_ZIGZAG_WEIGHT)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error creating trash movement: {0}", e.getMessage());
            // Fallback to basic movement if creation fails
            return new NPCMovementBuilder()
                    .withEntity(trashEntity)
                    .setSpeed(BASE_SPEED_MIN)
                    .setInitialVelocity(1, 0)
                    .withConstantMovement()
                    .setLenientMode(true)
                    .build();
        }
    }
}