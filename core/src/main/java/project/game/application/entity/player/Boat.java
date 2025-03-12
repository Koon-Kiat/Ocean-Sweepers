package project.game.application.entity.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.entity.item.Trash;
import project.game.application.entity.obstacle.Rock;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.entity.SpriteEntity;
import project.game.engine.entitysystem.entity.api.IRenderable;
import project.game.engine.entitysystem.movement.type.PlayerMovementManager;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;

public class Boat extends SpriteEntity implements IRenderable {

    private static final GameLogger LOGGER = new GameLogger(Boat.class);
    private final PlayerMovementManager movementManager;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;

    // Direction constants - used as indices in directional sprite arrays
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    public static final int DIRECTION_UP_RIGHT = 4;
    public static final int DIRECTION_DOWN_RIGHT = 5;
    public static final int DIRECTION_DOWN_LEFT = 6;
    public static final int DIRECTION_UP_LEFT = 7;

    // Current direction index - default to DOWN
    private int currentDirectionIndex = DIRECTION_DOWN;

    // Threshold to determine if we should consider movement on an axis
    private static final float MOVEMENT_THRESHOLD = 0.01f;

    // Type handler registry for collision handling
    private static final Map<Class<?>, Consumer<Boat>> COLLISION_HANDLERS = new ConcurrentHashMap<>();

    static {
        // Register collision handlers for different entity types using lambda
        // expressions
        registerCollisionHandler(Rock.class, boat -> boat.handleRockCollision());
        registerCollisionHandler(Trash.class, boat -> boat.handleTrashCollision());
    }

    /**
     * Register a handler for a specific type of collidable entity
     * 
     * @param <T>     Type of collidable
     * @param clazz   Class of collidable
     * @param handler Function to handle collision with the collidable
     */
    public static <T extends ICollidableVisitor> void registerCollisionHandler(
            Class<T> clazz, Consumer<Boat> handler) {
        COLLISION_HANDLERS.put(clazz, handler);
    }

    // Track the current collision entity
    private ICollidableVisitor currentCollisionEntity;

    /**
     * Constructor for single texture boat
     */
    public Boat(Entity entity, World world, PlayerMovementManager movementManager, String texturePath) {
        super(entity, world, texturePath, null);
        this.movementManager = movementManager;
    }

    /**
     * Constructor for directional sprites boat
     */
    public Boat(Entity entity, World world, PlayerMovementManager movementManager, TextureRegion[] directionalSprites) {
        super(entity, world, null, directionalSprites);
        this.movementManager = movementManager;
    }

    public PlayerMovementManager getMovementManager() {
        return this.movementManager;
    }

    /**
     * Set the collision to be active for a certain duration.
     */
    public void setCollisionActive(long durationMillis) {
        collisionActive = true;
        collisionEndTime = System.currentTimeMillis() + durationMillis;

        // When collision becomes active, sync positions to prevent desynchronization
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float physX = getBody().getPosition().x * pixelsToMeters;
        float physY = getBody().getPosition().y * pixelsToMeters;

        // Update entity position and movement manager
        getEntity().setX(physX);
        getEntity().setY(physY);
        if (movementManager != null) {
            movementManager.setX(physX);
            movementManager.setY(physY);
        }
    }

    /**
     * Updates the current sprite index based on movement direction.
     * Uses vector angles to determine the most appropriate sprite direction.
     */
    @Override
    public void updateSpriteIndex() {
        // Skip updating if no movement manager or sprites
        if (movementManager == null || !hasSprites()) {
            return;
        }

        // Get velocity from movement manager
        Vector2 velocity = movementManager.getVelocity();

        // Only update direction if actually moving
        if (Math.abs(velocity.x) > MOVEMENT_THRESHOLD || Math.abs(velocity.y) > MOVEMENT_THRESHOLD) {
            // Calculate angle in degrees (0° is right, 90° is up)
            float angle = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));
            if (angle < 0)
                angle += 360; // Convert to 0-360 range

            // Determine direction based on angle
            if (angle >= 337.5 || angle < 22.5) {
                currentDirectionIndex = DIRECTION_RIGHT;
            } else if (angle >= 22.5 && angle < 67.5) {
                currentDirectionIndex = DIRECTION_UP_RIGHT;
            } else if (angle >= 67.5 && angle < 112.5) {
                currentDirectionIndex = DIRECTION_UP;
            } else if (angle >= 112.5 && angle < 157.5) {
                currentDirectionIndex = DIRECTION_UP_LEFT;
            } else if (angle >= 157.5 && angle < 202.5) {
                currentDirectionIndex = DIRECTION_LEFT;
            } else if (angle >= 202.5 && angle < 247.5) {
                currentDirectionIndex = DIRECTION_DOWN_LEFT;
            } else if (angle >= 247.5 && angle < 292.5) {
                currentDirectionIndex = DIRECTION_DOWN;
            } else if (angle >= 292.5 && angle < 337.5) {
                currentDirectionIndex = DIRECTION_DOWN_RIGHT;
            }

            LOGGER.debug("Boat moving at angle: {0}, direction: {1}",
                    angle, getCurrentDirectionName());
        }

        // Check if we need to map our 8-directional index to a 4-directional sprite
        // array
        if (getCurrentSprite() != null) {
            // If we only have 4 directional sprites, map diagonal directions to cardinal
            // directions
            if (hasSprites() && getSpritesCount() <= 4) {
                int mappedIndex = mapTo4DirectionalIndex(currentDirectionIndex);
                setCurrentSpriteIndex(mappedIndex);
            } else {
                // We have 8-directional sprites
                setCurrentSpriteIndex(currentDirectionIndex);
            }
        }
    }

    /**
     * Maps an 8-directional index to a 4-directional index for sprite display
     * 
     * @param eightDirIndex The 8-directional index (0-7)
     * @return The 4-directional index (0-3)
     */
    private int mapTo4DirectionalIndex(int eightDirIndex) {
        switch (eightDirIndex) {
            case DIRECTION_UP:
                return DIRECTION_UP; // UP
            case DIRECTION_RIGHT:
                return DIRECTION_RIGHT; // RIGHT
            case DIRECTION_DOWN:
                return DIRECTION_DOWN; // DOWN
            case DIRECTION_LEFT:
                return DIRECTION_LEFT; // LEFT
            case DIRECTION_UP_RIGHT:
                return DIRECTION_RIGHT; // UP_RIGHT maps to RIGHT
            case DIRECTION_DOWN_RIGHT:
                return DIRECTION_RIGHT; // DOWN_RIGHT maps to RIGHT
            case DIRECTION_DOWN_LEFT:
                return DIRECTION_LEFT; // DOWN_LEFT maps to LEFT
            case DIRECTION_UP_LEFT:
                return DIRECTION_LEFT; // UP_LEFT maps to LEFT
            default:
                return DIRECTION_DOWN; // Default to DOWN
        }
    }

    /**
     * Get the current direction as a descriptive string (for debugging)
     */
    public String getCurrentDirectionName() {
        switch (currentDirectionIndex) {
            case DIRECTION_UP:
                return "UP";
            case DIRECTION_RIGHT:
                return "RIGHT";
            case DIRECTION_DOWN:
                return "DOWN";
            case DIRECTION_LEFT:
                return "LEFT";
            case DIRECTION_UP_RIGHT:
                return "UP_RIGHT";
            case DIRECTION_DOWN_RIGHT:
                return "DOWN_RIGHT";
            case DIRECTION_DOWN_LEFT:
                return "DOWN_LEFT";
            case DIRECTION_UP_LEFT:
                return "UP_LEFT";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public Body createBody(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float centerX = (x + width / 2) / pixelsToMeters;
        float centerY = (y + height / 2) / pixelsToMeters;
        bodyDef.position.set(centerX, centerY);
        bodyDef.fixedRotation = true;
        bodyDef.linearDamping = 0.5f;
        bodyDef.bullet = true;
        bodyDef.allowSleep = false;

        Body newBody = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();

        // Reduce the hitbox size to be smaller than the visual sprite
        // Using 60% of the original width and height for the hitbox
        float hitboxScale = 0.6f;
        float hitboxWidth = (width * hitboxScale) / pixelsToMeters;
        float hitboxHeight = (height * hitboxScale) / pixelsToMeters;
        shape.setAsBox(hitboxWidth / 2, hitboxHeight / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 10.0f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.0f;

        Filter filter = new Filter();
        filter.categoryBits = 0x0001;
        filter.maskBits = -1;
        fixtureDef.filter.categoryBits = filter.categoryBits;
        fixtureDef.filter.maskBits = filter.maskBits;

        newBody.createFixture(fixtureDef);
        shape.dispose();
        newBody.setUserData(this);
        return newBody;
    }

    @Override
    public boolean checkCollision(Entity other) {
        // Use Box2D for collision detection
        return true;
    }

    @Override
    public void onCollision(ICollidableVisitor other) {
        // Only handle collisions with actual entities, not boundaries
        if (other != null) {
            // Store the current collision entity
            this.currentCollisionEntity = other;

            // Log normal entity collisions
            LOGGER.info("{0} collided with {1}",
                    new Object[] { getEntity().getClass().getSimpleName(),
                            other.getClass().getSimpleName() });

            // Dispatch to appropriate handler based on the other entity's type
            dispatchCollisionHandling(other);
        }
    }

    /**
     * Dispatches collision handling to the appropriate registered handler
     * 
     * @param other The other entity involved in the collision
     */
    private void dispatchCollisionHandling(ICollidableVisitor other) {
        Class<?> otherClass = other.getClass();
        // Look for a handler for this specific class
        for (Map.Entry<Class<?>, Consumer<Boat>> entry : COLLISION_HANDLERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(otherClass)) {
                entry.getValue().accept(this);
                return;
            }
        }
    }

    /**
     * Handle collision with a rock
     */
    private void handleRockCollision() {
        if (currentCollisionEntity == null)
            return;

        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
        float boatX = getBody().getPosition().x;
        float boatY = getBody().getPosition().y;
        float rockX = currentCollisionEntity.getBody().getPosition().x;
        float rockY = currentCollisionEntity.getBody().getPosition().y;

        float dx = boatX - rockX;
        float dy = boatY - rockY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        LOGGER.info("go distance: " + distance);

        if (distance > 0.0001f) {
            dx /= distance;
            dy /= distance;
            float bounceForce = GameConstantsFactory.getConstants().BOAT_BOUNCE_FORCE();
            LOGGER.info("boat bounce force: " + bounceForce);
            LOGGER.info("dy dx: " + dx);
            getBody().applyLinearImpulse(dx * bounceForce, dy * bounceForce, boatX, boatY, true);
        }
    }

    /**
     * Handle collision with trash
     */
    private void handleTrashCollision() {
        if (currentCollisionEntity == null || !(currentCollisionEntity instanceof Trash))
            return;

        Trash trash = (Trash) currentCollisionEntity;
        trash.getEntity().setActive(false);
        getWorld().destroyBody(trash.getBody());
    }

    @Override
    public boolean isInCollision() {
        if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
            collisionActive = false;
            getBody().setLinearVelocity(0, 0);
        }
        return collisionActive;
    }
}