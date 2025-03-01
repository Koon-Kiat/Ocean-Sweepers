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

import project.game.engine.api.ICollidable;
import project.game.engine.api.ICollisionHandler;
import project.game.engine.entitysystem.movement.MovementManager;
import project.game.engine.io.SceneIOManager;

/**
 * CollisionManager is a class that manages the collision detection and
 * resolution of entities in the game.
 */
public class CollisionManager implements ContactListener {

    private final World world;
    private final List<Runnable> collisionQueue;
    private final SceneIOManager inputManager;

    // Maintain a map of collidable entities and their associated MovementManager.
    private final Map<ICollidable, MovementManager> entityMap;
    private boolean collided = false;

    public CollisionManager(World world, SceneIOManager inputManager) {
        this.world = world;
        this.inputManager = inputManager;
        this.collisionQueue = new ArrayList<>();
        this.entityMap = new HashMap<>();
    }

    public void init() {
        world.setContactListener(this);
    }

    public void addEntity(ICollidable entity, MovementManager movementManager) {
        entityMap.put(entity, movementManager);
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        handleCollisionEvent(userDataA, userDataB);
    }

    /**
     * Handle collision events between two objects using visitor pattern
     */
    private void handleCollisionEvent(Object objectA, Object objectB) {
        handleCollisionSide(objectA, objectB);
        handleCollisionSide(objectB, objectA);

        // Mark collision status
        collided = true;
    }

    /**
     * Handle one side of a collision
     */
    private void handleCollisionSide(Object object, Object otherObject) {
        if (object instanceof ICollisionHandler) {
            // Polymorphically delegate collision handling
            ICollisionHandler handler = (ICollisionHandler) object;
            handler.handleCollisionWith(otherObject, collisionQueue);
        }
    }

    @Override
    public void endContact(Contact contact) {
        // Reset collision status when objects separate
        collided = false;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // No implementation required
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // No implementation required
    }

    public void processCollisions() {
        for (Runnable r : collisionQueue) {
            r.run();
        }
        collisionQueue.clear();
    }

    public void updateGame(float gameWidth, float gameHeight) {
        for (Map.Entry<ICollidable, MovementManager> entry : entityMap.entrySet()) {
            entry.getValue().updateDirection(inputManager.getPressedKeys(), inputManager.getKeyBindings());
            entry.getValue().updateMovement();
        }
        for (Map.Entry<ICollidable, MovementManager> entry : entityMap.entrySet()) {
            EntityCollisionUpdater.updateEntity(entry.getKey(), entry.getValue(), gameWidth, gameHeight);
        }
    }

    public void syncEntityPositions() {
        for (ICollidable entity : entityMap.keySet()) {
            EntityCollisionUpdater.syncEntity(entity);
        }
    }

    public boolean collision() {
        return collided;
    }
}