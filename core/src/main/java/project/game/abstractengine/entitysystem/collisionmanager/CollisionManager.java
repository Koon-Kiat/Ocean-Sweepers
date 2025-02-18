package project.game.abstractengine.entitysystem.collisionmanager;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.entitysystem.interfaces.ICollidable;

public class CollisionManager implements ContactListener {

    private World world;

    public CollisionManager(World world) {
        this.world = world;
        world.setContactListener(this);
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        // Debug: Print fixture collision details
        // System.out.println("Collision detected between: " + fixtureA + " and " + fixtureB);

        Object userDataA = fixtureA.getUserData(); // Get user data from Fixture A
        Object userDataB = fixtureB.getUserData(); // Get user data from Fixture B

        if (userDataA instanceof ICollidable && userDataB instanceof ICollidable) {
            ICollidable entityA = (ICollidable) userDataA;
            ICollidable entityB = (ICollidable) userDataB;

            if (entityA.checkCollision(entityB.getEntity())) {
                entityA.onCollision(entityB.getEntity());
            }
            if (entityB.checkCollision(entityA.getEntity())) {
                entityB.onCollision(entityA.getEntity());
            }
        } else if (userDataA instanceof ICollidable && userDataB instanceof String && userDataB.equals("boundary")) {
            ICollidable entityA = (ICollidable) userDataA;
            entityA.onCollision(null); // Pass null as the other entity
        } else if (userDataB instanceof ICollidable && userDataA instanceof String && userDataA.equals("boundary")) {
            ICollidable entityB = (ICollidable) userDataB;
            entityB.onCollision(null); // Pass null as the other entity
        }
    }

    @Override
    public void endContact(Contact contact) {
        // Called when two fixtures stop touching
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Modify the contact properties if needed
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Called after a collision is resolved
    }
}