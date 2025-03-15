package project.game.engine.entitysystem.physics.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.entity.IEntityRemovalListener;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.core.PlayerMovementManager;
import project.game.engine.entitysystem.movement.management.MovementManager;
import project.game.engine.entitysystem.physics.api.ICollidableVisitor;
import project.game.engine.entitysystem.physics.api.ICollisionPairHandler;
import project.game.engine.entitysystem.physics.collision.detection.CollisionPairTracker;
import project.game.engine.entitysystem.physics.collision.resolution.CollisionResponseHandler;
import project.game.engine.entitysystem.physics.collision.resolution.CollisionVisitorResolver;
import project.game.engine.entitysystem.physics.lifecycle.PhysicsBodyRemovalRequest;
import project.game.engine.io.management.SceneInputManager;

/**
 * CollisionManager is a class that manages the collision detection and
 * resolution of entities in the game using a pure polymorphic approach.
 */
public class CollisionManager implements ContactListener {

    private static final GameLogger LOGGER = new GameLogger(CollisionManager.class);
    private final World world;
    private final List<Runnable> collisionQueue;
    private final SceneInputManager inputManager;
    private final CollisionVisitorResolver collisionResolver;
    private final ICollisionPairHandler collisionPairTracker;
    private final Map<ICollidableVisitor, MovementManager> entityMap;
    private final Map<MovementManager, Boolean> playerControlledMap;
    private boolean collided = false;
    private float collisionMovementStrength;
    private float movementThreshold;
    private long defaultCollisionDuration;
    private Queue<PhysicsBodyRemovalRequest> removalQueue = new LinkedList<>();
    private Set<Entity> entitiesScheduledForRemoval = new HashSet<>();

    public CollisionManager(World world, SceneInputManager inputManager) {
        this.world = world;
        this.inputManager = inputManager;
        this.collisionQueue = new ArrayList<>();
        this.entityMap = new HashMap<>();
        this.playerControlledMap = new HashMap<>();
        this.collisionResolver = new CollisionVisitorResolver();
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

    /**
     * Add an entity to the collision manager with its associated movement manager.
     * If the movement manager is a PlayerMovementManager, it will be marked as
     * player-controlled for input handling.
     */
    public void addEntity(ICollidableVisitor entity, MovementManager movementManager) {
        entityMap.put(entity, movementManager);

        // Register entity with the collision resolver
        collisionResolver.registerCollidable(entity);

        // Track whether this is a player-controlled movement manager
        if (movementManager != null) {
            boolean isPlayerControlled = movementManager.getClass().equals(PlayerMovementManager.class);
            playerControlledMap.put(movementManager, isPlayerControlled);
        }
    }

    public void processCollisions() {
        for (Runnable r : collisionQueue) {
            r.run();
        }
        collisionQueue.clear();
    }

    public void scheduleBodyRemoval(Body body, Entity entity, IEntityRemovalListener removalListener) {
        if (!entitiesScheduledForRemoval.contains(entity)) {
            removalQueue.add(new PhysicsBodyRemovalRequest(body, entity, removalListener));
            entitiesScheduledForRemoval.add(entity);
            System.out.println("Scheduled removal for entity: " + entity);
        } else {
            System.out.println("Entity already scheduled for removal: " + entity);
        }
    }

    public void processRemovalQueue() {
        while (!removalQueue.isEmpty()) {
            PhysicsBodyRemovalRequest request = removalQueue.poll();
            entitiesScheduledForRemoval.remove(request.getEntity());

            // Set entity as inactive
            request.getEntity().setActive(false);

            // Destroy the body
            world.destroyBody(request.getBody());

            // Notify listener if provided
            if (request.getRemovalListener() != null) {
                request.getRemovalListener().onEntityRemove(request.getEntity());
            }
        }
    }

    /**
     * Check if a movement manager is player-controlled
     */
    private boolean isPlayerControlled(MovementManager manager) {
        Boolean isPlayerControlled = playerControlledMap.get(manager);
        return isPlayerControlled != null && isPlayerControlled;
    }

    public void updateGame(float gameWidth, float gameHeight, float pixelsToMeters) {
        for (Map.Entry<ICollidableVisitor, MovementManager> entry : entityMap.entrySet()) {
            MovementManager manager = entry.getValue();
            if (manager != null) {
                // Only apply keyboard input to player-controlled movement managers
                if (isPlayerControlled(manager)) {
                    manager.updateVelocity(inputManager.getPressedKeys(), inputManager.getKeyBindings());
                }

                // Update all movement managers, regardless of type
                manager.updateMovement();
            }
            processRemovalQueue();
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
                CollisionResponseHandler.updateEntity(entity, manager, gameWidth, gameHeight,
                        pixelsToMeters, collisionMovementStrength,
                        movementThreshold);
            } else {
                CollisionResponseHandler.syncEntity(entity, pixelsToMeters);
            }
        }
    }

    public void syncEntityPositions(float pixelsToMeters) {
        for (ICollidableVisitor entity : entityMap.keySet()) {
            CollisionResponseHandler.syncEntity(entity, pixelsToMeters);
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

    // Use reflection to refresh collision state.
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