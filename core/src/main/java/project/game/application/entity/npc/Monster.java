package project.game.application.entity.npc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.core.CustomAssetManager;
import project.game.engine.entitysystem.entity.CollidableEntity;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.movement.type.NPCMovementManager;

public class Monster extends CollidableEntity implements IRenderable {

    private static final GameLogger LOGGER = new GameLogger(Main.class);
    private final NPCMovementManager movementManager;
    private final String texturePath;
    private boolean collisionActive = false;
    private long collisionEndTime = 0;
    private long lastCollisionTime = 0;

    // Type-based collision handler registry
    private static final Map<Class<?>, BiConsumer<Monster, ICollidableVisitor>> MONSTER_COLLISION_HANDLERS = 
        new ConcurrentHashMap<>();
    
    static {
        // Register collision handlers for specific entity types
        registerMonsterCollisionHandler(Boat.class, Monster::handleBoatCollision);
        registerMonsterCollisionHandler(Rock.class, Monster::handleRockCollision);
        registerMonsterCollisionHandler(Trash.class, (monster, trash) -> {/* Ignore trash collisions */});
    }
    
    /**
     * Register a handler for a specific type of collidable entity
     * @param <T> Type of collidable
     * @param clazz Class of collidable
     * @param handler Function to handle collision with the collidable
     */
    public static <T extends ICollidableVisitor> void registerMonsterCollisionHandler(
            Class<T> clazz, BiConsumer<Monster, ICollidableVisitor> handler) {
        MONSTER_COLLISION_HANDLERS.put(clazz, handler);
    }

    public Monster(Entity entity, World world, NPCMovementManager movementManager, String texturePath) {
        super(entity, world);
        this.movementManager = movementManager;
        this.texturePath = texturePath;
    }

    public NPCMovementManager getMovementManager() {
        return this.movementManager;
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

    @Override
    public boolean isActive() {
        return super.getEntity().isActive();
    }

    @Override
    public Entity getEntity() {
        return super.getEntity();
    }

    @Override
    public Body getBody() {
        return super.getBody();
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
    public String getTexturePath() {
        return texturePath;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (isActive() && CustomAssetManager.getInstance().isLoaded()) {
            // Render the entity using offset for BOX2D body
            float renderX = entityX() - entityWidth() / 2;
            float renderY = entityY() - entityHeight() / 2;
            Texture texture = CustomAssetManager.getInstance().getAsset(texturePath, Texture.class);
            batch.draw(texture, renderX, renderY, entityWidth(), entityHeight());
        }
    }

    @Override
    public boolean checkCollision(Entity other) {
        // Use Box2D for collision detection
        return true;
    }

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

    /**
     * Determine if a cooldown should be applied based on entity type
     */
    private boolean shouldApplyCooldown(ICollidableVisitor other) {
        return other != null && other.getClass() == Boat.class;
    }

    /**
     * Dispatches collision handling to the appropriate registered handler
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
        for (Map.Entry<Class<?>, BiConsumer<Monster, ICollidableVisitor>> entry : MONSTER_COLLISION_HANDLERS.entrySet()) {
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
        float monsterX = getEntity().getX();
        float monsterY = getEntity().getY();
        float boatX = boat.getEntity().getX();
        float boatY = boat.getEntity().getY();

        float dx = monsterX - boatX;
        float dy = monsterY - boatY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.0001f) {
            // Normalize direction
            dx /= distance;
            dy /= distance;

            // Reverse the direction to push the boat AWAY from monster
            dx = -dx;
            dy = -dy;

            // Apply scaled impulse force with improved physics
            float baseImpulse = GameConstantsFactory.getConstants().MONSTER_BASE_IMPULSE();
            Vector2 monsterVel = getBody().getLinearVelocity();

            // Add velocity component to the impulse
            float velMagnitude = (float) Math.sqrt(monsterVel.x * monsterVel.x + monsterVel.y * monsterVel.y);
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
            } catch (Exception e) {
                // If we can't invoke the method, just log it
                LOGGER.warn("Could not set collision active state on boat: {0}", e.getMessage());
            }
        }
    }

    /**
     * Handle collision with a rock
     */
    private void handleRockCollision(ICollidableVisitor rock) {
        float monsterX = getEntity().getX();
        float monsterY = getEntity().getY();
        float rockX = rock.getEntity().getX();
        float rockY = rock.getEntity().getY();

        float dx = monsterX - rockX; // Reversed direction (rock pushing monster)
        float dy = monsterY - rockY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.0001f) {
            // Normalize direction
            dx /= distance;
            dy /= distance;

            // Apply impulse to monster (being pushed by rock)
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

    private float entityX() {
        return super.getEntity().getX();
    }

    private float entityY() {
        return super.getEntity().getY();
    }

    private float entityWidth() {
        return super.getEntity().getWidth();
    }

    private float entityHeight() {
        return super.getEntity().getHeight();
    }
}