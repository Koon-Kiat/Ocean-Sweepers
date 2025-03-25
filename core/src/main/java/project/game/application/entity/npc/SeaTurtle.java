package project.game.application.entity.npc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.Main;
import project.game.application.entity.item.Trash;
import project.game.application.entity.obstacle.Rock;
import project.game.application.entity.player.Boat;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.api.ISpriteRenderable;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.management.EntityManager;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class SeaTurtle implements ISpriteRenderable, ICollidableVisitor {
    private static final GameLogger LOGGER = new GameLogger(Main.class);

    private final NPCMovementManager movementManager;
    private TextureRegion[] sprites;
    private int currentSpriteIndex;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;
    private long lastCollisionTime = 0;
    private CollisionManager collisionManager;

    // Threshold to determine if we should consider movement on an axis
    private static final float MOVEMENT_THRESHOLD = 0.01f;

    // Direction constants - used as indices in directional sprite arrays
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    public static final int DIRECTION_UP_RIGHT = 4;
    public static final int DIRECTION_DOWN_RIGHT = 5;
    public static final int DIRECTION_DOWN_LEFT = 6;
    public static final int DIRECTION_UP_LEFT = 7;

    // Type-based collision handler registry
    private final Entity entity;
    private final World world;
    private final Body body;

    private String texturePath;
    private int currentDirectionIndex;

    // Type-based collision handler registry
    private static final Map<Class<?>, BiConsumer<SeaTurtle, ICollidableVisitor>> SEA_TURTLE_COLLISION_HANDLERS = new ConcurrentHashMap<>();

    static {
        // Register collision handlers for specific entity types
        registerSeaTurtleCollisionHandler(Boat.class, SeaTurtle::handleBoatCollision);
        registerSeaTurtleCollisionHandler(Rock.class, SeaTurtle::handleRockCollision);
        registerSeaTurtleCollisionHandler(Trash.class, (seaTurtle, trash) -> {
            /* Ignore trash collisions */});
    }

    /**
     * Register a handler for a specific type of collidable entity
     * 
     * @param <T>     Type of collidable
     * @param clazz   Class of collidable
     * @param handler Function to handle collision with the collidable
     */
    public static <T extends ICollidableVisitor> void registerSeaTurtleCollisionHandler(
            Class<T> clazz, BiConsumer<SeaTurtle, ICollidableVisitor> handler) {
        SEA_TURTLE_COLLISION_HANDLERS.put(clazz, handler);
    }

    public SeaTurtle(Entity entity, World world, NPCMovementManager movementManager, String texturePath) {
        this.entity = entity;
        this.world = world;
        this.movementManager = movementManager;
        this.texturePath = texturePath;
        this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
    }

    public SeaTurtle(Entity entity, World world, NPCMovementManager movementManager,
            TextureRegion[] directionalSprites) {
        this.entity = entity;
        this.world = world;
        this.movementManager = movementManager;
        this.sprites = directionalSprites;
        this.currentSpriteIndex = 0;
        this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
    }

    public NPCMovementManager getMovementManager() {
        return this.movementManager;
    }

    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    /**
     * Set the collision to be active for a certain duration.
     */
    public void setCollisionActive(long durationMillis) {
        collisionActive = true;
        collisionEndTime = System.currentTimeMillis() + durationMillis;
        lastCollisionTime = System.currentTimeMillis();

        // When collision becomes active, sync position between physics body and entity
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float physX = getBody().getPosition().x * pixelsToMeters;
        float physY = getBody().getPosition().y * pixelsToMeters;

        // Update entity position
        getEntity().setX(physX);
        getEntity().setY(physY);

        // Temporarily increase damping during collision to prevent bouncing too much
        getBody().setLinearDamping(3.0f);
    }

    public boolean isActive() {
        return entity.isActive();
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

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Body getBody() {
        return body;
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
    public TextureRegion getCurrentSprite() {
        if (!hasSprites()) {
            return null;
        }
        return sprites[currentSpriteIndex];
    }

    @Override
    public void setSprites(TextureRegion[] sprites) {
        this.sprites = sprites;
    }

    @Override
    public void setCurrentSpriteIndex(int index) {
        if (hasSprites() && index >= 0 && index < sprites.length) {
            this.currentSpriteIndex = index;
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
    public void render(SpriteBatch batch) {
        updateSpriteIndex();

        if (getCurrentSprite() != null) {
            float renderX = getEntity().getX() - getEntity().getWidth() / 2;
            float renderY = getEntity().getY() - getEntity().getHeight() / 2;
            float width = entity.getWidth();
            float height = entity.getHeight();
            batch.draw(getCurrentSprite(), renderX, renderY, width, height);
        }
    }

    /**
     * Updates the current sprite index based on entity state
     * This method determines which sprite to display based on movement direction
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

            // Determine direction based on angle - using 8 directions for sea turtle
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

            LOGGER.debug("SeaTurtle moving at angle: {0}, direction: {1}",
                    angle, getCurrentDirectionName());
        }

        // Check if we need to map our 8-directional index to a 4-directional sprite
        // array
        if (hasSprites()) {
            // If we have fewer than 8 directional sprites, map to available sprites
            if (getSpritesCount() <= 4) {
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

    public void removeFromManager(EntityManager entityManager) {
        entityManager.removeEntityUsingRegistry(this.entity);
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
        bodyDef.bullet = true;
        bodyDef.linearDamping = 0.8f;
        bodyDef.angularDamping = 0.8f;

        Body newBody = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                (width / 2) / pixelsToMeters,
                (height / 2) / pixelsToMeters);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 500.0f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.1f;

        Filter filter = new Filter();
        filter.categoryBits = 0x0008;
        filter.maskBits = -1 & ~0x0004;
        fixtureDef.filter.categoryBits = filter.categoryBits;
        fixtureDef.filter.maskBits = filter.maskBits;

        newBody.createFixture(fixtureDef);
        shape.dispose();
        newBody.setUserData(this);
        return newBody;
    }

    @Override
    public boolean checkCollision(Entity other) {
        // Always use Box2D for collision detection
        return true;
    }

    /**
     * Handle sea turtle collisions with other entities
     */
    @Override
    public void onCollision(ICollidableVisitor other) {
        long currentTime = System.currentTimeMillis();
        long cooldownTime = shouldApplyCooldown(other) ? 300 : 100;

        if (currentTime - lastCollisionTime < cooldownTime) {
            return;
        }

        if (other != null) {
            LOGGER.info("{0} collided with {1}",
                    new Object[] { getEntity().getClass().getSimpleName(),
                            other.getClass().getSimpleName() });

            dispatchCollisionHandling(other);
        }
    }

    @Override
    public boolean isInCollision() {
        if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
            collisionActive = false;
            // Reset damping when collision ends
            getBody().setLinearDamping(0.2f);
            // Clear any lingering velocity when exiting collision state
            getBody().setLinearVelocity(0, 0);
        }
        return collisionActive;
    }

    @Override
    public void collideWith(Object other) {
        onCollision((ICollidableVisitor) other);
    }

    @Override
    public void collideWithBoundary() {
        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
    }

    /**
     * Determine if a cooldown should be applied based on entity type
     */
    private boolean shouldApplyCooldown(ICollidableVisitor other) {
        return other != null && other.getClass() == Boat.class;
    }

    /**
     * Dispatches collision handling to the appropriate registered handler
     * 
     * @param other The other entity involved in the collision
     */
    private void dispatchCollisionHandling(ICollidableVisitor other) {
        // Skip trash collisions
        if (other.getClass() == Trash.class) {
            return;
        }

        // Set collision active by default for non-trash entities
        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());

        // Get other entity's class and find a matching handler
        Class<?> otherClass = other.getClass();

        // Look for a handler for this specific class or its superclasses
        for (Map.Entry<Class<?>, BiConsumer<SeaTurtle, ICollidableVisitor>> entry : SEA_TURTLE_COLLISION_HANDLERS
                .entrySet()) {
            if (entry.getKey().isAssignableFrom(otherClass)) {
                entry.getValue().accept(this, other);
                return;
            }
        }
    }

    /**
     * Handle collision with a boat
     */
    private void handleBoatCollision(ICollidableVisitor boat) {
        float seaTurtleX = getEntity().getX();
        float seaTurtleY = getEntity().getY();
        float boatX = boat.getEntity().getX();
        float boatY = boat.getEntity().getY();

        float dx = seaTurtleX - boatX;
        float dy = seaTurtleY - boatY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.0001f) {
            // Normalize direction
            dx /= distance;
            dy /= distance;

            // Reverse the direction to push the boat AWAY from sea turtle
            dx = -dx;
            dy = -dy;

            // Apply scaled impulse force with improved physics
            float baseImpulse = GameConstantsFactory.getConstants().SEA_TURTLE_BASE_IMPULSE();
            Vector2 seaTurtleVel = getBody().getLinearVelocity();

            // Add velocity component to the impulse
            float velMagnitude = (float) Math.sqrt(seaTurtleVel.x * seaTurtleVel.x + seaTurtleVel.y * seaTurtleVel.y);
            float impactMultiplier = Math.min(0.5f + velMagnitude * 0.01f, 2.0f);

            // Apply impulse to push the boat away
            float pushForce = baseImpulse * (1.0f + impactMultiplier);
            boat.getBody().applyLinearImpulse(
                    dx * pushForce,
                    dy * pushForce,
                    boat.getBody().getWorldCenter().x,
                    boat.getBody().getWorldCenter().y,
                    true);

            // High damping for better control after impact
            boat.getBody().setLinearDamping(3.0f);

            // Access the boat's setCollisionActive method through reflection
            try {
                java.lang.reflect.Method method = boat.getClass().getMethod("setCollisionActive", long.class);
                method.invoke(boat, GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                LOGGER.warn("Could not set collision active state on boat: {0}", e.getMessage());
            }
        }
    }

    /**
     * Handle collision with a rock
     */
    private void handleRockCollision(ICollidableVisitor rock) {
        float seaTurtleX = getEntity().getX();
        float seaTurtleY = getEntity().getY();
        float rockX = rock.getEntity().getX();
        float rockY = rock.getEntity().getY();

        float dx = seaTurtleX - rockX; // Reversed direction (rock pushing sea turtle)
        float dy = seaTurtleY - rockY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.0001f) {
            // Normalize direction
            dx /= distance;
            dy /= distance;

            // Apply impulse to sea turtle (being pushed by rock)
            float rockImpulse = GameConstantsFactory.getConstants().ROCK_BASE_IMPULSE();
            getBody().applyLinearImpulse(
                    dx * rockImpulse,
                    dy * rockImpulse,
                    getBody().getWorldCenter().x,
                    getBody().getWorldCenter().y,
                    true);

            // Reduce damping to allow movement
            getBody().setLinearDamping(0.5f);
        }
    }
}