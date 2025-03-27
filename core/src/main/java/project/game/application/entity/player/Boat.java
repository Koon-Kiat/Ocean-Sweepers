package project.game.application.entity.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.entity.ILifeLossCallback;
import project.game.application.entity.item.Trash;
import project.game.application.entity.npc.SeaTurtle;
import project.game.application.entity.obstacle.Rock;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.api.ISpriteRenderable;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.management.EntityManager;
import project.game.engine.entitysystem.movement.core.PlayerMovementManager;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.management.CollisionManager;
import project.game.engine.scene.management.ScoreManager;

public class Boat implements ISpriteRenderable, ICollidableVisitor {

    private static final GameLogger LOGGER = new GameLogger(Boat.class);

    // Threshold to determine if we should consider movement on an axis
    private static final float MOVEMENT_THRESHOLD = 0.01f;

    // Type handler registry for collision handling
    private static final Map<Class<?>, Consumer<Boat>> COLLISION_HANDLERS = new ConcurrentHashMap<>();

    // Player movement manager
    private final PlayerMovementManager movementManager;

    // Collision
    private final World world;
    private final Body body;
    private final Entity entity;
    private final boolean rockCollisionActive = false;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;
    private ICollidableVisitor currentCollisionEntity;
    private CollisionManager collisionManager;
    private ILifeLossCallback lifeLossCallback;
    private boolean boundaryCollisionActive = false;
    private int currentDirectionIndex;
    private TextureRegion[] sprites;
    private String texturePath;

    // Add a cooldown for life loss
    private boolean lifeLossCooldown = false;
    private long lifeLossCooldownEndTime = 0;
    private static final long LIFE_LOSS_COOLDOWN_DURATION = 1000;

    // Direction constants - used as indices in directional sprite arrays
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    public static final int DIRECTION_UP_RIGHT = 4;
    public static final int DIRECTION_DOWN_RIGHT = 5;
    public static final int DIRECTION_DOWN_LEFT = 6;
    public static final int DIRECTION_UP_LEFT = 7;

    static {
        registerCollisionHandler(Rock.class, Boat::handleRockCollision);
        registerCollisionHandler(Trash.class, Boat::handleTrashCollision);
        registerCollisionHandler(SeaTurtle.class, Boat::handleSeaTurtleCollision);
    }

    /**
     * Constructor for single texture boat
     */
    public Boat(Entity entity, World world, PlayerMovementManager movementManager, String texturePath) {
        this.entity = entity;
        this.world = world;
        this.movementManager = movementManager;
        this.texturePath = texturePath;
        this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
    }

    /**
     * Constructor for directional sprites boat
     */
    public Boat(Entity entity, World world, PlayerMovementManager movementManager, TextureRegion[] directionalSprites) {
        this.entity = entity;
        this.world = world;
        this.movementManager = movementManager;
        this.sprites = directionalSprites;
        this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
    }

    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    public PlayerMovementManager getMovementManager() {
        return this.movementManager;
    }

    public static boolean isEntityPermanent(String entityType) {
        return entityType.equals("Boat") ||
                entityType.equals("SeaTurtle") ||
                entityType.equals("boundary");
    }

    public void removeFromManager(EntityManager entityManager) {
        entityManager.removeRenderableEntity(this);
    }

    public void setLifeLossCallback(ILifeLossCallback callback) {
        this.lifeLossCallback = callback;
    }

    public void setCollisionActive(long durationMillis) {
        collisionActive = true;
        collisionEndTime = System.currentTimeMillis() + durationMillis;

        // Only sync positions if we're not in a boundary collision
        if (!boundaryCollisionActive) {
            syncPositions();
        }
    }

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

    @Override
    public TextureRegion getCurrentSprite() {
        if (!hasSprites()) {
            return null;
        }
        return sprites[currentDirectionIndex];
    }

    @Override
    public void updateSpriteIndex() {
        // Skip updating if no movement manager or sprites
        if (movementManager == null || !hasSprites()) {
            return;
        }

        // Get velocity from movement manager
        Vector2 velocity = movementManager.getMovableEntity().getVelocity();

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

    @Override
    public void setSprites(TextureRegion[] sprites) {
        this.sprites = sprites;
    }

    @Override
    public void setCurrentSpriteIndex(int index) {
        if (hasSprites() && index >= 0 && index < sprites.length) {
            currentDirectionIndex = index;
        }
    }

    @Override
    public boolean hasSprites() {
        return sprites != null && sprites.length > 0;
    }

    @Override
    public int getSpritesCount() {
        return hasSprites() ? sprites.length : 0;
    }

    @Override
    public String getTexturePath() {
        return texturePath;
    }

    @Override
    public boolean isRenderable() {
        return true;
    }

    @Override
    public void render(SpriteBatch batch) {
        // Update sprite direction before rendering
        updateSpriteIndex();

        if (getCurrentSprite() != null) {
            float renderX = getEntity().getX() - getEntity().getWidth() / 2;
            float renderY = getEntity().getY() - getEntity().getHeight() / 2;
            float width = entity.getWidth();
            float height = entity.getHeight();
            batch.draw(getCurrentSprite(), renderX, renderY, width, height);
        }
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public final Body createBody(World world, float x, float y, float width, float height) {
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
        fixtureDef.density = 500.0f;
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

    @Override
    public boolean isInCollision() {
        long currentTime = System.currentTimeMillis();

        // Update collision state
        if (collisionActive && currentTime > collisionEndTime) {
            collisionActive = false;
            getBody().setLinearVelocity(0, 0);
        }

        // Update life loss cooldown
        if (lifeLossCooldown && currentTime > lifeLossCooldownEndTime) {
            lifeLossCooldown = false;
        }

        return collisionActive;
    }

    @Override
    public void collideWith(Object other) {
        // Direct visitor pattern implementation
        onCollision((ICollidableVisitor) other);

    }

    @Override
    public void collideWithBoundary() {
        // Handle collision with world boundaries
        boundaryCollisionActive = true;

        // Only sync positions if no other collision is active
        if (!rockCollisionActive) {
            syncPositions();
        }
    }

    /**
     * Handle collision with sea turtle
     */
    private void handleSeaTurtleCollision() {
        if (currentCollisionEntity == null || isInCollision()) {
            return;
        }
        // Only set the collision state active, but don't apply forces
        // The sea turtle's handleBoatCollision is responsible for applying forces to
        // both entities
        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());

        // Apply higher damping to quickly stop motion and let the physics handle things
        getBody().setLinearDamping(5.0f);

        LOGGER.debug("Boat collided with sea turtle - letting sea turtle handle physics");
    }

    /**
     * Handle collision with trash
     */
    private void handleTrashCollision() {
        if (currentCollisionEntity == null || !(currentCollisionEntity instanceof Trash))
            return;

        Trash trash = (Trash) currentCollisionEntity;

        // Check if either entity is permanent before scheduling removal
        String entityType = trash.getClass().getSimpleName();
        if (!isEntityPermanent(entityType)) {
            if (collisionManager != null) {
                collisionManager.scheduleBodyRemoval(trash.getBody(), trash.getEntity(), trash.getRemovalListener());
            } else {
                LOGGER.warn("CollisionManager not set in Boat object - cannot safely remove trash");
            }
        }

        ScoreManager.getInstance().addScore(50);
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

        if (distance > 0.0001f) {
            // Normalize direction
            dx /= distance;
            dy /= distance;

            // Calculate bounce force based on approach velocity
            // Higher speeds result in lower bounce multiplier to prevent excessive bouncing
            float bounceForce = GameConstantsFactory.getConstants().BOAT_BASE_IMPULSE()
                    / GameConstantsFactory.getConstants().PIXELS_TO_METERS();
            if (boundaryCollisionActive) {
                bounceForce *= 1.5f; // Stronger impulse when both collisions are active
            }

            // Apply impulse in the direction away from rock
            getBody().applyLinearImpulse(dx * bounceForce, dy * bounceForce, boatX, boatY, true);

            // Set higher damping during collision to reduce bouncing
            getBody().setLinearDamping(1.0f);

            // Reset velocity if it's too high after collision
            Vector2 velocity = getBody().getLinearVelocity();
            float newSpeed = velocity.len();
            float maxSpeed = 5.0f;
            if (newSpeed > maxSpeed) {
                velocity.nor().scl(maxSpeed);
                getBody().setLinearVelocity(velocity);
            }
        }

        // Call the life loss callback if set and not in cooldown
        if (lifeLossCallback != null && !lifeLossCooldown) {
            LOGGER.info("Boat collided with rock - losing life");
            lifeLossCallback.onLifeLost();

            // Set the cooldown
            lifeLossCooldown = true;
            lifeLossCooldownEndTime = System.currentTimeMillis() + LIFE_LOSS_COOLDOWN_DURATION;
        }

        ScoreManager.getInstance().subtractScore(25);
    }

    private void syncPositions() {
        // When collision becomes active, sync positions to prevent desynchronization
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float physX = getBody().getPosition().x * pixelsToMeters;
        float physY = getBody().getPosition().y * pixelsToMeters;

        // Update entity position and movement manager
        getEntity().setX(physX);
        getEntity().setY(physY);
        if (movementManager != null) {
            movementManager.getMovableEntity().setX(physX);
            movementManager.getMovableEntity().setY(physY);
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
     * Maps an 8-directional index to a 4-directional index for sprite display
     * 
     * @param eightDirIndex The 8-directional index (0-7)
     * @return The 4-directional index (0-3)
     */
    private int mapTo4DirectionalIndex(int eightDirIndex) {
        switch (eightDirIndex) {
            case DIRECTION_UP:
                return DIRECTION_UP;
            case DIRECTION_RIGHT:
                return DIRECTION_RIGHT;
            case DIRECTION_DOWN:
                return DIRECTION_DOWN;
            case DIRECTION_LEFT:
                return DIRECTION_LEFT;
            case DIRECTION_UP_RIGHT:
                return DIRECTION_RIGHT;
            case DIRECTION_DOWN_RIGHT:
                return DIRECTION_RIGHT;
            case DIRECTION_DOWN_LEFT:
                return DIRECTION_LEFT;
            case DIRECTION_UP_LEFT:
                return DIRECTION_LEFT;
            default:
                return DIRECTION_DOWN;
        }
    }
}