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
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.api.entity.ILifeLossCallback;
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
import project.game.engine.entitysystem.physics.collision.resolution.CollisionResponseHandler;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class SeaTurtle implements ISpriteRenderable, ICollidableVisitor {
    private static final GameLogger LOGGER = new GameLogger(Main.class);

    private final NPCMovementManager movementManager;
    private TextureRegion[] sprites;
    private int currentSpriteIndex;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;
    private long lastCollisionTime = 0;
    private IEntityRemovalListener removalListener;//additional code
    private ICollidableVisitor currentCollisionEntity;
    private CollisionManager collisionManager;
    private final Vector2 accumulatedImpulse = new Vector2();
    private final Vector2 accumulatedVelocity = new Vector2();

    private ILifeLossCallback healthCallback;

    private boolean healthLossCooldown = false;
    private long healthLossCooldownEndTime = 0;
    private static final long HEALTH_LOSS_COOLDOWN_DURATION = 500; 

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
        registerSeaTurtleCollisionHandler(Trash.class, SeaTurtle::handleTrashCollision);
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

        // Sync positions between physics body, entity, and movement manager
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        CollisionResponseHandler.syncEntity(this, pixelsToMeters);

        if (movementManager != null) {
            // Update movement manager position to match physics
            float physX = getBody().getPosition().x * pixelsToMeters;
            float physY = getBody().getPosition().y * pixelsToMeters;
            // Use setX/setY instead of setVelocity for position
            movementManager.getMovableEntity().setX(physX);
            movementManager.getMovableEntity().setY(physY);

            // Update velocity in movement manager to match physics
            Vector2 velocity = getBody().getLinearVelocity();
            movementManager.getMovableEntity().setVelocity(velocity.x, velocity.y);
        }

        // Set higher damping during collision
        getBody().setLinearDamping(5.0f);
    }

    /**
     * Checks if this entity should be considered permanent in the game world
     */
    public static boolean isEntityPermanent(String entityType) {
        return entityType.equals("SeaTurtle") ||
                entityType.equals("boundary");
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
        Vector2 velocity = movementManager.getMovableEntity().getVelocity();

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

            // LOGGER.debug("SeaTurtle moving at angle: {0}, direction: {1}",
            // angle, getCurrentDirectionName());
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
        entityManager.removeRenderableEntity(this);
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
        bodyDef.linearDamping = 0.8f; // Reduced from previous value
        bodyDef.angularDamping = 0.8f;

        Body newBody = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();

        // Make hitbox slightly smaller for better collision response
        float hitboxScale = 0.7f;
        float hitboxWidth = (width * hitboxScale) / pixelsToMeters;
        float hitboxHeight = (height * hitboxScale) / pixelsToMeters;
        shape.setAsBox(hitboxWidth / 2, hitboxHeight / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1000.0f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.1f;

        Filter filter = new Filter();
        filter.categoryBits = 0x0008;
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
        // Always use Box2D for collision detection
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

            // Reset accumulators at the beginning of collision handling
            accumulatedImpulse.set(0, 0);
            accumulatedVelocity.set(getBody().getLinearVelocity());

            // Dispatch to appropriate handler based on the other entity's type
            dispatchCollisionHandling(other);

            // Apply accumulated impulse
            getBody().applyLinearImpulse(
                    accumulatedImpulse.x,
                    accumulatedImpulse.y,
                    getBody().getWorldCenter().x,
                    getBody().getWorldCenter().y,
                    true);

            // Apply accumulated velocity changes
            if (accumulatedVelocity.len2() > 0) {
                getBody().setLinearVelocity(accumulatedVelocity);
            }
        }
    }

    @Override
    public boolean isInCollision() {
        if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
            collisionActive = false;
            // Reset damping when collision ends
            getBody().setLinearDamping(10f);
        }
        return collisionActive;
    }

    @Override
    public void collideWith(Object other) {
        onCollision((ICollidableVisitor) other);
    }

    @Override
    public void collideWithBoundary() {
        // Get current velocity
        Vector2 velocity = body.getLinearVelocity();

        // Get position to determine which boundary was hit
        float x = entity.getX();
        float y = entity.getY();
        float bounceMultiplier = 0.8f; // Maintain 80% of speed after bounce

        // Get game boundaries
        float gameWidth = GameConstantsFactory.getConstants().GAME_WIDTH();
        float gameHeight = GameConstantsFactory.getConstants().GAME_HEIGHT();

        // Determine which boundary was hit and reverse appropriate velocity component
        if (x <= 0 || x >= gameWidth) {
            velocity.x = -velocity.x * bounceMultiplier;
        }
        if (y <= 0 || y >= gameHeight) {
            velocity.y = -velocity.y * bounceMultiplier;
        }

        // Ensure minimum speed after bounce
        float minSpeed = 2.0f;
        if (velocity.len() < minSpeed) {
            velocity.nor().scl(minSpeed);
        }

        // Accumulate velocity changes
        accumulatedVelocity.set(velocity);

        // Apply the new velocity
        body.setLinearVelocity(velocity);

        // Keep damping low to maintain movement
        body.setLinearDamping(10f);
    }



    /**
     * Dispatches collision handling to the appropriate registered handler
     * 
     * @param other The other entity involved in the collision
     */
    private void dispatchCollisionHandling(ICollidableVisitor other) {
        // Get other entity's class and find a matching handler
        Class<?> otherClass = other.getClass();

        // Handle collision based on registered handlers - single execution only
        BiConsumer<SeaTurtle, ICollidableVisitor> handler = SEA_TURTLE_COLLISION_HANDLERS.get(otherClass);
        if (handler != null) {
            // Only set collision active for non-trash entities
            if (otherClass != Trash.class) {
                setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
            }
            handler.accept(this, other);
        }
    }

    /**
     * Handle collision with a boat
     */
    private void handleBoatCollision(ICollidableVisitor boat) {
        // Prevent double-handling if we're already in collision
        if (isInCollision()) {
            return;
        }

        float seaTurtleX = getBody().getPosition().x;
        float seaTurtleY = getBody().getPosition().y;
        float boatX = boat.getBody().getPosition().x;
        float boatY = boat.getBody().getPosition().y;

        // Get current velocities of both entities
        Vector2 turtleVelocity = getBody().getLinearVelocity();
        Vector2 boatVelocity = boat.getBody().getLinearVelocity();

        // Create a copy for calculations
        Vector2 turtleVel = new Vector2(turtleVelocity);
        Vector2 boatVel = new Vector2(boatVelocity);

        // Calculate approach vector (the direction they're approaching each other from)
        // This combines both position difference and velocity
        Vector2 approachDir = new Vector2();

        // First component: Position difference (where they are relative to each other)
        approachDir.x = seaTurtleX - boatX;
        approachDir.y = seaTurtleY - boatY;

        // If they're very close, ensure we have a valid direction
        if (approachDir.len() < 0.0001f) {
            // If positions are almost identical, use velocity directions
            approachDir.x = -boatVel.x;
            approachDir.y = -boatVel.y;

            // If boat velocity is very small, use turtle velocity
            if (approachDir.len() < 0.0001f) {
                approachDir.x = turtleVel.x;
                approachDir.y = turtleVel.y;
            }
        }

        // Second component: Consider their velocities to determine their approach
        // directions
        float dotProduct = boatVel.x * turtleVel.x + boatVel.y * turtleVel.y;
        float boatSpeed = boatVel.len();
        float turtleSpeed = turtleVel.len();

        // Only normalize if they have meaningful velocity
        if (boatSpeed > 0.1f)
            boatVel.nor();
        if (turtleSpeed > 0.1f)
            turtleVel.nor();

        // Normalize approach direction
        if (approachDir.len() > 0.0001f) {
            approachDir.nor();
        } else {
            // Fallback to a default direction if no clear approach
            approachDir.set(1, 0);
        }

        // Calculate impulse forces based on speeds and mass ratio
        float impactSpeed = Math.max(0.8f, (boatSpeed + turtleSpeed) * 0.5f);
        float turtleToBoatMassRatio = 1000.0f / 500.0f; // Turtle mass / Boat mass

        // Calculate push directions
        // Turtle should be pushed in the direction opposite to the boat's velocity
        Vector2 turtlePushDir = new Vector2(approachDir);

        // Boat should be pushed in the direction opposite to the turtle's velocity
        Vector2 boatPushDir = new Vector2(-approachDir.x, -approachDir.y);

        // Apply impulse to turtle - higher mass means less impulse
        float turtleForce = 0.04f * impactSpeed / turtleToBoatMassRatio;
        getBody().applyLinearImpulse(
                turtlePushDir.x * turtleForce,
                turtlePushDir.y * turtleForce,
                seaTurtleX,
                seaTurtleY,
                true);

        // Apply impulse to boat - lower mass means more impulse
        float boatForce = 0.06f * impactSpeed;
        boat.getBody().applyLinearImpulse(
                boatPushDir.x * boatForce,
                boatPushDir.y * boatForce,
                boatX,
                boatY,
                true);

        LOGGER.debug("Applied opposing collision forces - turtle: {0}, boat: {1}, approachDir: {2},{3}",
                turtleForce, boatForce, approachDir.x, approachDir.y);

        // Cap maximum velocities
        Vector2 newTurtleVel = getBody().getLinearVelocity();
        float maxTurtleSpeed = 1.2f;
        if (newTurtleVel.len() > maxTurtleSpeed) {
            newTurtleVel.nor().scl(maxTurtleSpeed);
            getBody().setLinearVelocity(newTurtleVel);
        }

        Vector2 newBoatVel = boat.getBody().getLinearVelocity();
        float maxBoatSpeed = 1.2f;
        if (newBoatVel.len() > maxBoatSpeed) {
            newBoatVel.nor().scl(maxBoatSpeed);
            boat.getBody().setLinearVelocity(newBoatVel);
        }

        // Set collision active
        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());
    }

    /**
     * Handle collision with a rock
     */
    private void handleRockCollision(ICollidableVisitor rock) {
        float seaTurtleX = getEntity().getX();
        float seaTurtleY = getEntity().getY();
        float rockX = rock.getEntity().getX();
        float rockY = rock.getEntity().getY();

        float dx = seaTurtleX - rockX;
        float dy = seaTurtleY - rockY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.0001f) {
            dx /= distance;
            dy /= distance;

            // Get current velocity and calculate impact speed
            Vector2 velocity = getBody().getLinearVelocity();
            float currentSpeed = velocity.len();

            // Scale down impulse based on current speed
            float speedFactor = Math.min(1.0f, 1.0f / (1 + currentSpeed * 0.2f));
            float rockImpulse = GameConstantsFactory.getConstants().ROCK_BASE_IMPULSE()
                    / GameConstantsFactory.getConstants().PIXELS_TO_METERS() * speedFactor;

            // Accumulate impulse
            accumulatedImpulse.x += dx * rockImpulse;
            accumulatedImpulse.y += dy * rockImpulse;

            // Apply scaled impulse
            getBody().applyLinearImpulse(
                    dx * rockImpulse,
                    dy * rockImpulse,
                    getBody().getWorldCenter().x,
                    getBody().getWorldCenter().y,
                    true);

            // Cap maximum velocity after collision
            velocity = getBody().getLinearVelocity();
            float maxSpeed = 5.0f;
            if (velocity.len() > maxSpeed) {
                velocity.nor().scl(maxSpeed);
                accumulatedVelocity.set(velocity);
            }
        }
    }

    public void setHealthCallback(ILifeLossCallback callback) {
        this.healthCallback = callback;
    }

    /**
     * Handle collision with trash
     */
    private void handleTrashCollision(ICollidableVisitor trash) {
        if (!(trash instanceof Trash))
            return;

        // Check if either entity is permenant before scheduling removal
        String entityType = trash.getClass().getSimpleName();
        if (!isEntityPermanent(entityType)) {
            if (collisionManager != null) {
                // Schedule the trash for removal
                collisionManager.scheduleBodyRemoval(trash.getBody(), trash.getEntity(), null);
                LOGGER.debug("Scheduled trash for removal", trash.getEntity().getClass().getSimpleName());

                if (removalListener != null) {
                    removalListener.onEntityRemove(trash.getEntity()); //additional code
                }


                // Check cooldown before triggering health loss
                long currentTime = System.currentTimeMillis();
                if (healthCallback != null && (!healthLossCooldown || currentTime > healthLossCooldownEndTime)) {
                    // Reset cooldown
                    healthLossCooldown = true;
                    healthLossCooldownEndTime = currentTime + HEALTH_LOSS_COOLDOWN_DURATION;
                    
                    // Call health loss callback
                    healthCallback.onLifeLost();
                    LOGGER.info("Turtle lost health from eating trash");
                }

                // Restore damping after removing trash
                getBody().setLinearDamping(10f);
            } else {
                LOGGER.warn("Collision manager not set for SeaTurtle, cannot remove trash");
            }
        }
    }
    public void setEntityRemovalListener(IEntityRemovalListener listener) {
        this.removalListener = listener;
    }

}