package project.game.abstractengine.entitysystem.collisionmanager;

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

import project.game.abstractengine.entitysystem.movementmanager.MovementManager;
import project.game.abstractengine.interfaces.ICollidable;
import project.game.abstractengine.iomanager.SceneIOManager;

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

        if (userDataA instanceof ICollidable && userDataB instanceof ICollidable) {
            ICollidable collidableA = (ICollidable) userDataA;
            ICollidable collidableB = (ICollidable) userDataB;

            // Invoke collision responses if collision is confirmed.
            if (collidableA.checkCollision(collidableB.getEntity())) {
                collisionQueue.add(() -> collidableA.onCollision(collidableB));
            }
            if (collidableB.checkCollision(collidableA.getEntity())) {
                collisionQueue.add(() -> collidableB.onCollision(collidableA));
            }
            collided = true;
        } else if (userDataA instanceof ICollidable && userDataB instanceof String
                && userDataB.equals("boundary")) {
            ICollidable collidableA = (ICollidable) userDataA;
            collisionQueue.add(() -> collidableA.onCollision(null));
            collided = true;
        } else if (userDataB instanceof ICollidable && userDataA instanceof String
                && userDataA.equals("boundary")) {
            ICollidable collidableB = (ICollidable) userDataB;
            collisionQueue.add(() -> collidableB.onCollision(null));
            collided = true;
        }
    }

    @Override
    public void endContact(Contact contact) {
        // Called when two fixtures stop touching.
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        // Reset collided to false when entities are no longer in contact
        if ((userDataA instanceof ICollidable && userDataB instanceof ICollidable) ||
                (userDataA instanceof ICollidable && userDataB instanceof String && userDataB.equals("boundary")) ||
                (userDataB instanceof ICollidable && userDataA instanceof String && userDataA.equals("boundary"))) {
            collided = false;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    public void processCollisions() {
        for (Runnable r : collisionQueue) {
            r.run();
        }
        collisionQueue.clear();
    }

    public void updateGame(float gameWidth, float gameHeight) {
        // Update movement managers for all entities (input processing).
        for (Map.Entry<ICollidable, MovementManager> entry : entityMap.entrySet()) {
            // Assume that each movement manager independently updates its state.
            entry.getValue().updateDirection(inputManager.getPressedKeys(), inputManager.getKeyBindings());
            entry.getValue().updateMovement();
        }

        // Update each entity using our generic updater.
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