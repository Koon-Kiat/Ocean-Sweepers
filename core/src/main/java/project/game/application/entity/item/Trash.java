package project.game.application.entity.item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.entity.npc.SeaTurtle;
import project.game.application.entity.obstacle.Rock;
import project.game.application.entity.player.Boat;
import project.game.common.config.factory.GameConstantsFactory;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.api.IRenderable;
import project.game.engine.entitysystem.entity.api.ISpriteRenderable;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.entity.management.EntityManager;
import project.game.engine.entitysystem.movement.core.NPCMovementManager;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class Trash implements ISpriteRenderable, ICollidableVisitor {

    private static final GameLogger LOGGER = new GameLogger(Trash.class);
    private final Entity entity;
    private final World world;
    private final Body body;
    private TextureRegion[] sprites;
    private int currentSpriteIndex;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;
    private IEntityRemovalListener removalListener;
    private CollisionManager collisionManager;
    private NPCMovementManager movementManager;
    private float lastMotionCheckTime = 0;
    private Vector2 lastPosition = new Vector2();
    private float minimumVelocity = 1.0f; // Minimum desired velocity
    private float lastTrashCollisionTime = 0;
    private float trashCollisionCooldown = 1.0f; // 1 second cooldown for trash-trash collisions

    // Type-based collision handler registry
    private static final Map<Class<?>, BiConsumer<Trash, ICollidableVisitor>> TRASH_COLLISION_HANDLERS = new ConcurrentHashMap<>();

    static {
        // Register collision handler for Boat
        registerTrashCollisionHandler(Boat.class, Trash::handleBoatCollision);
        // Handler for trash-trash collisions
        registerTrashCollisionHandler(Trash.class, Trash::handleTrashCollision);
        // Register collision handler for SeaTurtle
        registerTrashCollisionHandler(SeaTurtle.class, Trash::handleSeaTurtleCollision);
        // Register collision handler for Rock
        registerTrashCollisionHandler(Rock.class, Trash::handleRockCollision);
    }

    /**
     * Register a handler for a specific type of collidable entity for Trash
     * 
     * @param <T>     Type of collidable
     * @param clazz   Class of collidable
     * @param handler Function to handle collision with the collidable
     */
    public static <T extends ICollidableVisitor> void registerTrashCollisionHandler(
            Class<T> clazz, BiConsumer<Trash, ICollidableVisitor> handler) {
        TRASH_COLLISION_HANDLERS.put(clazz, handler);
    }

    public Trash(Entity entity, World world, TextureRegion sprite) {
        this.entity = entity;
        this.world = world;
        this.sprites = new TextureRegion[] { sprite };
        this.currentSpriteIndex = 0;
        this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
        this.lastPosition.set(entity.getX(), entity.getY());
    }

    public void setRemovalListener(IEntityRemovalListener removalListener) {
        this.removalListener = removalListener;
    }

    public void setCollisionActive(long durationMillis) {
        collisionActive = true;
        collisionEndTime = System.currentTimeMillis() + durationMillis;

        // When collision becomes active, sync positions but maintain velocity
        float pixelsToMeters = GameConstantsFactory.getConstants().PIXELS_TO_METERS();
        float physX = getBody().getPosition().x * pixelsToMeters;
        float physY = getBody().getPosition().y * pixelsToMeters;

        // Update entity position
        getEntity().setX(physX);
        getEntity().setY(physY);

        // If we have a movement manager, update its position too
        if (movementManager != null) {
            movementManager.setX(physX);
            movementManager.setY(physY);

            // Keep the current velocity in the movement manager
            Vector2 currentVel = getBody().getLinearVelocity();
            movementManager.setVelocity(currentVel.x, currentVel.y);
        }

        // Use very low damping during collision to maintain movement
        getBody().setLinearDamping(0.1f);
    }

    public void setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
    }

    /**
     * Set the movement manager for this trash object
     * 
     * @param movementManager The NPCMovementManager to use
     */
    public void setMovementManager(NPCMovementManager movementManager) {
        this.movementManager = movementManager;
    }

    /**
     * Get the movement manager for this trash object
     * 
     * @return The NPCMovementManager used by this trash
     */
    public NPCMovementManager getMovementManager() {
        return this.movementManager;
    }

    @Override
    public String getTexturePath() {
        return "trash1.png";
    }

    public boolean isActive() {
        return entity.isActive();
    }

    public IEntityRemovalListener getRemovalListener() {
        return this.removalListener;
    }

    @Override
    public boolean isRenderable() {
        return true;
    }

    public void removeFromManager(EntityManager entityManager) {
        entityManager.removeRenderableEntity(this);
    }

    @Override
    public void collideWith(Object other) {
        if (other instanceof ICollidableVisitor) {
            onCollision((ICollidableVisitor) other);
        }
    }

    @Override
    public void collideWithBoundary() {
        // Get current velocity
        com.badlogic.gdx.math.Vector2 velocity = body.getLinearVelocity();

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

        // Apply the new velocity
        body.setLinearVelocity(velocity);

        // Keep damping very low to maintain movement
        body.setLinearDamping(0.01f);
    }

    @Override
    public boolean checkCollision(Entity other) {
        // Always return true to ensure collision is checked using Box2D
        return true;
    }

    @Override
    public void onCollision(ICollidableVisitor other) {
        LOGGER.info("{0} collided with {1}",
                new Object[] { getEntity().getClass().getSimpleName(),
                        other == null ? "boundary" : other.getClass().getSimpleName() });

        if (other != null) {
            // Dispatch to appropriate collision handler based on entity type
            dispatchCollisionHandling(other);
        }
    }

    /**
     * Dispatches collision handling to the appropriate registered handler
     * 
     * @param other The other entity involved in the collision
     */
    private void dispatchCollisionHandling(ICollidableVisitor other) {
        // Get other entity's class and find a matching handler
        Class<?> otherClass = other.getClass();

        // Look for a handler for this specific class or its superclasses
        for (Map.Entry<Class<?>, BiConsumer<Trash, ICollidableVisitor>> entry : TRASH_COLLISION_HANDLERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(otherClass)) {
                entry.getValue().accept(this, other);
                return;
            }
        }
    }

    @Override
    public boolean isInCollision() {
        if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
            collisionActive = false;

            // When collision ends, ensure we maintain movement
            if (movementManager != null) {
                Vector2 currentVel = getBody().getLinearVelocity();
                movementManager.setVelocity(currentVel.x, currentVel.y);
            }

            // Keep damping low after collision ends
            getBody().setLinearDamping(0.1f);
            ensureMinimumMovement();
        }
        return collisionActive;
    }

    @Override
    public TextureRegion getCurrentSprite() {
        if (!hasSprites()) {
            return null;
        }
        return sprites[currentSpriteIndex];
    }

    @Override
    public void updateSpriteIndex() {
        // Trash currently uses a single sprite
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
        if (isActive() && getCurrentSprite() != null) {
            float renderX = entity.getX() - entity.getWidth() / 2;
            float renderY = entity.getY() - entity.getHeight() / 2;
            batch.draw(getCurrentSprite(), renderX, renderY, entity.getWidth(), entity.getHeight());
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
        bodyDef.bullet = true; // Enable continuous collision detection
        bodyDef.linearDamping = 0.1f; // Consistent base damping
        bodyDef.angularDamping = 0.1f;
        bodyDef.allowSleep = false; // Never let the body sleep

        Body newBody = world.createBody(bodyDef);
        CircleShape shape = new CircleShape();

        // Make the collision shape closer to visual size
        float collisionRadius = (width * 0.45f) / pixelsToMeters;
        shape.setRadius(collisionRadius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f; // More mass for better collision response
        fixtureDef.friction = 0.01f; // Keep very low friction
        fixtureDef.restitution = 0.6f; // Moderate bounce

        // Set up collision filtering
        Filter filter = new Filter();
        filter.categoryBits = 0x0004; // Trash category
        filter.maskBits = -1; // Collide with everything
        fixtureDef.filter.categoryBits = filter.categoryBits;
        fixtureDef.filter.maskBits = filter.maskBits;

        newBody.createFixture(fixtureDef);
        shape.dispose();
        newBody.setUserData(this);

        // Set initial random velocity to ensure movement
        float angle = (float) (Math.random() * Math.PI * 2);
        float speed = 2.5f; // Slightly higher initial speed
        newBody.setLinearVelocity(
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed);

        return newBody;
    }

    private void handleBoatCollision(ICollidableVisitor boat) {
        // Check if the boat covers up half of the trash
        float boatX = boat.getEntity().getX();
        float boatY = boat.getEntity().getY();
        float boatWidth = boat.getEntity().getWidth();
        float boatHeight = boat.getEntity().getHeight();

        float trashX = entity.getX();
        float trashY = entity.getY();
        float trashWidth = entity.getWidth();
        float trashHeight = entity.getHeight();

        boolean isCovered = (boatX < trashX + trashWidth / 2 && boatX + boatWidth > trashX + trashWidth / 2) &&
                (boatY < trashY + trashHeight / 2 && boatY + boatHeight > trashY + trashHeight / 2);

        if (isCovered) {
            // Always mark as inactive regardless of collisionManager availability
            // to prevent further collision processing
            entity.setActive(false);

            // Check if this entity should be removed
            String entityType = this.getClass().getSimpleName();
            if (!Boat.isEntityPermanent(entityType)) {
                if (collisionManager != null) {
                    collisionManager.scheduleBodyRemoval(getBody(), entity, removalListener);
                    if (removalListener != null) {
                        removalListener.onEntityRemove(entity);
                    }
                } else {
                    // If collisionManager isn't available, just log an error
                    // but don't try to destroy the body directly
                    LOGGER.error("CollisionManager not set in Trash object - cannot safely remove trash");
                    // Still notify the removal listener if available
                    if (removalListener != null) {
                        removalListener.onEntityRemove(entity);
                    }
                }
            }
        }
    }

    private void handleTrashCollision(ICollidableVisitor other) {
        if (other == null || !(other instanceof Trash)) {
            return;
        }

        // Add cooldown to prevent rapid multiple collisions
        float currentTime = System.currentTimeMillis() / 1000f;
        if (currentTime - lastTrashCollisionTime < trashCollisionCooldown) {
            return;
        }
        lastTrashCollisionTime = currentTime;

        // Calculate collision response
        float trash1X = getBody().getPosition().x;
        float trash1Y = getBody().getPosition().y;
        float trash2X = other.getBody().getPosition().x;
        float trash2Y = other.getBody().getPosition().y;

        float dx = trash1X - trash2X;
        float dy = trash1Y - trash2Y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.0001f) {
            dx /= distance;
            dy /= distance;

            // Apply a moderate bounce force
            float bounceForce = GameConstantsFactory.getConstants().BOAT_BASE_IMPULSE();

            // Get current velocities
            Vector2 vel1 = getBody().getLinearVelocity();
            Vector2 vel2 = other.getBody().getLinearVelocity();

            // Calculate new velocities using elastic collision
            float totalSpeed = vel1.len() + vel2.len();
            float speedShare = totalSpeed > 0 ? vel1.len() / totalSpeed : 0.5f;

            // Apply impulses
            getBody().applyLinearImpulse(
                    dx * bounceForce * (1 - speedShare),
                    dy * bounceForce * (1 - speedShare),
                    trash1X,
                    trash1Y,
                    true);

            other.getBody().applyLinearImpulse(
                    -dx * bounceForce * speedShare,
                    -dy * bounceForce * speedShare,
                    trash2X,
                    trash2Y,
                    true);

            // Ensure minimum speeds
            ensureMinimumSpeed(getBody(), 2.0f);
            ensureMinimumSpeed(other.getBody(), 2.0f);

            // Update movement managers if available
            if (movementManager != null) {
                Vector2 newVel = getBody().getLinearVelocity();
                movementManager.setVelocity(newVel.x, newVel.y);
            }

            // Update other trash's movement manager if available
            if (other instanceof Trash) {
                Trash otherTrash = (Trash) other;
                NPCMovementManager otherManager = otherTrash.getMovementManager();
                if (otherManager != null) {
                    Vector2 newVel = other.getBody().getLinearVelocity();
                    otherManager.setVelocity(newVel.x, newVel.y);
                }
            }
        }

        // Set very short collision duration
        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());

        // Keep damping very low
        getBody().setLinearDamping(0.1f);
        other.getBody().setLinearDamping(0.1f);
    }

    /**
     * Ensures a body maintains at least the minimum speed while capping maximum
     * speed
     */
    private void ensureMinimumSpeed(Body body, float minSpeed) {
        Vector2 velocity = body.getLinearVelocity();
        float speed = velocity.len();

        float maxSpeed = 5.0f; // Cap maximum speed

        if (speed < minSpeed) {
            velocity.nor().scl(minSpeed);
            body.setLinearVelocity(velocity);
        } else if (speed > maxSpeed) {
            velocity.nor().scl(maxSpeed);
            body.setLinearVelocity(velocity);
        }
    }

    private void handleSeaTurtleCollision(ICollidableVisitor seaTurtle) {
        // Check if the sea turtle covers up half of the trash
        float seaTurtleX = seaTurtle.getEntity().getX();
        float seaTurtleY = seaTurtle.getEntity().getY();
        float seaTurtleWidth = seaTurtle.getEntity().getWidth();
        float seaTurtleHeight = seaTurtle.getEntity().getHeight();

        float trashX = entity.getX();
        float trashY = entity.getY();
        float trashWidth = entity.getWidth();
        float trashHeight = entity.getHeight();

        boolean isCovered = (seaTurtleX < trashX + trashWidth / 2
                && seaTurtleX + seaTurtleWidth > trashX + trashWidth / 2) &&
                (seaTurtleY < trashY + trashHeight / 2 && seaTurtleY + seaTurtleHeight > trashY + trashHeight / 2);

        if (isCovered) {
            // Always mark as inactive regardless of collisionManager availability
            // to prevent further collision processing
            entity.setActive(false);

            // Check if this entity should be removed
            String entityType = this.getClass().getSimpleName();
            if (!SeaTurtle.isEntityPermanent(entityType)) {
                if (collisionManager != null) {
                    collisionManager.scheduleBodyRemoval(getBody(), entity, removalListener);
                } else {
                    // If collisionManager isn't available, just log an error
                    // but don't try to destroy the body directly
                    LOGGER.error("CollisionManager not set in Trash object - cannot safely remove trash");
                    // Still notify the removal listener if available
                    if (removalListener != null) {
                        removalListener.onEntityRemove(entity);
                    }
                }
            }
        }
    }

    /**
     * Handle collision with a rock
     */
    private void handleRockCollision(ICollidableVisitor rock) {
        if (rock == null || !(rock instanceof Rock)) {
            return;
        }

        // Set collision active but with VERY short duration
        setCollisionActive(GameConstantsFactory.getConstants().COLLISION_ACTIVE_DURATION());

        // Get positions
        float trashX = getBody().getPosition().x;
        float trashY = getBody().getPosition().y;
        float rockX = rock.getBody().getPosition().x;
        float rockY = rock.getBody().getPosition().y;

        // Calculate direction from rock to trash
        float dx = trashX - rockX;
        float dy = trashY - rockY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.0001f) {
            // Normalize direction
            dx /= distance;
            dy /= distance;

            // Apply a moderate bounce force
            float bounceForce = GameConstantsFactory.getConstants().BOAT_BASE_IMPULSE();

            // Apply the impulse
            getBody().applyLinearImpulse(
                    dx * bounceForce,
                    dy * bounceForce,
                    trashX,
                    trashY,
                    true);

            // Get current velocity after impulse and ensure proper range
            Vector2 velocity = getBody().getLinearVelocity();
            float speed = velocity.len();

            float minSpeed = 1.0f;
            float maxSpeed = 5.0f;

            if (speed < minSpeed) {
                velocity.nor().scl(minSpeed);
                getBody().setLinearVelocity(velocity);
            } else if (speed > maxSpeed) {
                velocity.nor().scl(maxSpeed);
                getBody().setLinearVelocity(velocity);
            }
        }

        // Very important: Set extremely low damping to ensure continued movement
        getBody().setLinearDamping(0.01f);

        // Ensure movement continues after rock collision
        if (distance < 1.5f) {
            // If very close to rock, add some perpendicular motion to prevent getting stuck
            float perpX = -dy; // Perpendicular to direction vector
            float perpY = dx;

            float sideForce = GameConstantsFactory.getConstants().BOAT_BASE_IMPULSE() * 0.2f;
            getBody().applyLinearImpulse(
                    perpX * sideForce,
                    perpY * sideForce,
                    trashX,
                    trashY,
                    true);
        }
    }

    /**
     * Updates the trash entity, ensuring it maintains movement
     * This should be called every frame
     */
    public void update(float deltaTime) {
        // Handle end of collision period
        if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
            collisionActive = false;
            // Reset damping to ensure continued movement
            body.setLinearDamping(0.01f);

            // Apply a small random impulse to ensure movement continues
            ensureMinimumMovement();
        }

        // Check if object is moving enough every 0.5 seconds
        float currentTime = System.currentTimeMillis() / 1000f;
        if (currentTime - lastMotionCheckTime > 0.5f) {
            ensureMinimumMovement();
            lastMotionCheckTime = currentTime;

            // Update last position
            lastPosition.set(entity.getX(), entity.getY());
        }
    }

    /**
     * Ensures the trash has minimum required movement
     */
    private void ensureMinimumMovement() {
        // Check if we've moved significantly
        float dx = entity.getX() - lastPosition.x;
        float dy = entity.getY() - lastPosition.y;
        float distanceMoved = (float) Math.sqrt(dx * dx + dy * dy);

        Vector2 currentVelocity = body.getLinearVelocity();
        float currentSpeed = currentVelocity.len();

        // If barely moving, apply a random impulse
        if (distanceMoved < 5.0f && currentSpeed < minimumVelocity) {
            // Generate random direction
            float angle = (float) (Math.random() * Math.PI * 2);
            float impulseX = (float) Math.cos(angle) * 0.5f;
            float impulseY = (float) Math.sin(angle) * 0.5f;

            // Apply small impulse
            body.applyLinearImpulse(
                    impulseX,
                    impulseY,
                    body.getWorldCenter().x,
                    body.getWorldCenter().y,
                    true);

            // Ensure very low damping
            body.setLinearDamping(0.01f);

            LOGGER.debug("Applied movement correction to trash: [{0}, {1}]",
                    impulseX, impulseY);
        }
    }

}