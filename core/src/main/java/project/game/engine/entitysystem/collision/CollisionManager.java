package project.game.engine.entitysystem.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import project.game.common.logging.core.GameLogger;
import project.game.engine.api.collision.ICollidableVisitor;
import project.game.engine.api.collision.ICollisionPairHandler;
import project.game.engine.entitysystem.movement.MovementManager;
import project.game.engine.io.SceneIOManager;

/**
 * CollisionManager is a class that manages the collision detection and
 * resolution of entities in the game using a pure polymorphic approach.
 */
public class CollisionManager implements ContactListener {

    private static final GameLogger LOGGER = new GameLogger(CollisionManager.class);
    private final World world;
    private final List<Runnable> collisionQueue;
    private final SceneIOManager inputManager;
    private final CollisionVisitorHandler collisionResolver;
    private final ICollisionPairHandler collisionPairTracker;
    private final Map<ICollidableVisitor, MovementManager> entityMap;
    private boolean collided = false;
    private float collisionMovementStrength;
    private float movementThreshold;
    private long defaultCollisionDuration;

    public CollisionManager(World world, SceneIOManager inputManager) {
        this.world = world;
        this.inputManager = inputManager;
        this.collisionQueue = new ArrayList<>();
        this.entityMap = new HashMap<>();
        this.collisionResolver = new CollisionVisitorHandler();
        this.collisionPairTracker = new CollisionPairTracker();
        collisionResolver.registerBoundary();
    }

    /**
     * Configure collision-related parameters
     * 
     * @param collisionMovementStrength Strength multiplier for movement during
     *                                  collision
     * @param movementThreshold         Minimum threshold for movement vector to
     *                                  apply velocity
     * @param defaultCollisionDuration  Default duration for collision state in
     *                                  milliseconds
     */
    public void configure(float collisionMovementStrength, float movementThreshold, long defaultCollisionDuration) {
        this.collisionMovementStrength = collisionMovementStrength;
        this.movementThreshold = movementThreshold;
        this.defaultCollisionDuration = defaultCollisionDuration;
    }

    public void init() {
        world.setContactListener(this);
    }

    public void addEntity(ICollidableVisitor entity, MovementManager movementManager) {
        entityMap.put(entity, movementManager);

        // Register entity with the collision resolver
        collisionResolver.registerCollidable(entity);
    }

    public void processCollisions() {
        for (Runnable r : collisionQueue) {
            r.run();
        }
        collisionQueue.clear();
    }

    public void updateGame(float gameWidth, float gameHeight, float pixelsToMeters) {
        for (Map.Entry<ICollidableVisitor, MovementManager> entry : entityMap.entrySet()) {
            MovementManager manager = entry.getValue();
            if (manager != null) {
                entry.getValue().updateVelocity(inputManager.getPressedKeys(), inputManager.getKeyBindings());
                entry.getValue().updateMovement();
            }
        }

        // Handle entity updates with collision awareness
        for (Map.Entry<ICollidableVisitor, MovementManager> entry : entityMap.entrySet()) {
            ICollidableVisitor entity = entry.getKey();
            MovementManager manager = entry.getValue();

            // Check if this entity is involved in any active collisions
            boolean entityInCollision = collisionPairTracker.isEntityInCollision(entity);

            // Make sure the entity's collision state matches our tracked state
            if (entityInCollision && !entity.isInCollision()) {
                // If we're tracking this as in collision but the entity doesn't know,
                // make sure it's aware that a collision is active
                refreshEntityCollisionState(entity);
            }

            if (manager != null) {
                EntityCollisionUpdater.updateEntity(entity, manager, gameWidth, gameHeight,
                        pixelsToMeters, collisionMovementStrength,
                        movementThreshold);
            } else {
                EntityCollisionUpdater.syncEntity(entity, pixelsToMeters);
            }
        }
    }

    public void syncEntityPositions(float pixelsToMeters) {
        for (ICollidableVisitor entity : entityMap.keySet()) {
            EntityCollisionUpdater.syncEntity(entity, pixelsToMeters);
        }
    }

    public boolean collision() {
        return collided;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        LOGGER.debug("Collision detected between: " +
                (userDataA != null ? userDataA.getClass().getSimpleName() : "null") +
                " and " +
                (userDataB != null ? userDataB.getClass().getSimpleName() : "null"));

        // Add to active collisions using our visitor pattern handler
        collisionPairTracker.addCollisionPair(userDataA, userDataB);

        // Delegate to collision resolver which uses pure polymorphism
        collisionResolver.resolveCollision(userDataA, userDataB, collisionQueue);

        // Mark collision status
        collided = true;
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        // Remove from active collisions
        collisionPairTracker.removeCollisionPair(userDataA, userDataB);

        // Only set collided = false if there are no more active collisions
        if (collisionPairTracker.isEmpty()) {
            collided = false;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // No implementation required
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // No implementation required
    }

    // Use reflection to refresh collision state, avoiding the need for instanceof
    private void refreshEntityCollisionState(ICollidableVisitor entity) {
        try {
            java.lang.reflect.Method method = entity.getClass().getMethod("setCollisionActive", long.class);
            method.invoke(entity, defaultCollisionDuration);
        } catch (Exception e) {
            // If the entity doesn't have this method, just leave it as is
            LOGGER.warn("Could not refresh collision state for entity: {0}",
                    entity.getClass().getSimpleName());
        }
    }
}