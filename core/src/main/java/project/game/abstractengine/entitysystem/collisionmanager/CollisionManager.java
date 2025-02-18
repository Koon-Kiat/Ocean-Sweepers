package project.game.abstractengine.entitysystem.collisionmanager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.entitysystem.interfaces.ICollidable;

public class CollisionManager implements ContactListener {

    private final World world;
    private final List<Runnable> collisionQueue;

    public CollisionManager(World world) {
        this.world = world;
        this.collisionQueue = new ArrayList<>();
    }

    public void init() {
        world.setContactListener(this);
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

            // Enqueue collision responses instead of calling onCollision directly.
            if (collidableA.checkCollision(collidableB.getEntity())) {
                collisionQueue.add(() -> collidableA.onCollision(collidableB));
            }
            if (collidableB.checkCollision(collidableA.getEntity())) {
                collisionQueue.add(() -> collidableB.onCollision(collidableA));
            }
        } else if (userDataA instanceof ICollidable && userDataB instanceof String && userDataB.equals("boundary")) {
            ICollidable collidableA = (ICollidable) userDataA;
            collisionQueue.add(() -> collidableA.onCollision(null));
        } else if (userDataB instanceof ICollidable && userDataA instanceof String && userDataA.equals("boundary")) {
            ICollidable collidableB = (ICollidable) userDataB;
            collisionQueue.add(() -> collidableB.onCollision(null));
        }
    }

    @Override
    public void endContact(Contact contact) {
        // Called when two fixtures stop touching.
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Modify the contact properties if needed.
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Called after a collision is resolved.
    }


    public void processCollisions() {
        for (Runnable r : collisionQueue) {
            r.run();
        }
        collisionQueue.clear();
    }
}