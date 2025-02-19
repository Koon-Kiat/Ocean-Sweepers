package project.game.abstractengine.entitysystem.collisionmanager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.constants.GameConstants;
import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;
import project.game.abstractengine.iomanager.SceneIOManager;
import project.game.abstractengine.testentity.BucketEntity;
import project.game.abstractengine.testentity.DropEntity;

public class CollisionManager implements ContactListener {

    private final World world;
    private final List<Runnable> collisionQueue;
    private final PlayerMovementManager playerMovementManager;
    private final NPCMovementManager npcMovementManager;
    private final BucketEntity bucket;
    private final DropEntity drop;
    private SceneIOManager inputManager;

    public CollisionManager(World world, PlayerMovementManager playerMovementManager,
            NPCMovementManager npcMovementManager, BucketEntity bucket, DropEntity drop, SceneIOManager inputManager) {
        this.world = world;
        this.collisionQueue = new ArrayList<>();
        this.playerMovementManager = playerMovementManager;
        this.npcMovementManager = npcMovementManager;
        this.bucket = bucket;
        this.drop = drop;
        this.inputManager = inputManager;
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

    public void createScreenBoundaries(float gameWidth, float gameHeight) {
        float screenWidth = gameWidth / GameConstants.PIXELS_TO_METERS;
        float screenHeight = gameHeight / GameConstants.PIXELS_TO_METERS;
        float edgeThickness = 0.1f;

        // Create BodyDef for static boundaries
        BodyDef boundaryDef = new BodyDef();
        boundaryDef.type = BodyDef.BodyType.StaticBody;

        // Create FixtureDef for boundaries
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.2f;

        // Create top boundary
        boundaryDef.position.set(0, screenHeight);
        Body topBoundary = world.createBody(boundaryDef);
        PolygonShape topShape = new PolygonShape();
        topShape.setAsBox(screenWidth, edgeThickness);
        fixtureDef.shape = topShape;
        topBoundary.createFixture(fixtureDef);
        topBoundary.setUserData("boundary");
        topShape.dispose();

        // Create bottom boundary
        boundaryDef.position.set(0, 0);
        Body bottomBoundary = world.createBody(boundaryDef);
        PolygonShape bottomShape = new PolygonShape();
        bottomShape.setAsBox(screenWidth, edgeThickness);
        fixtureDef.shape = bottomShape;
        bottomBoundary.createFixture(fixtureDef);
        bottomBoundary.setUserData("boundary");
        bottomShape.dispose();

        // Create left boundary
        boundaryDef.position.set(0, 0);
        Body leftBoundary = world.createBody(boundaryDef);
        PolygonShape leftShape = new PolygonShape();
        leftShape.setAsBox(edgeThickness, screenHeight);
        fixtureDef.shape = leftShape;
        leftBoundary.createFixture(fixtureDef);
        leftBoundary.setUserData("boundary");  
        leftShape.dispose();

        // Create right boundary
        boundaryDef.position.set(screenWidth, 0);
        Body rightBoundary = world.createBody(boundaryDef);
        PolygonShape rightShape = new PolygonShape();
        rightShape.setAsBox(edgeThickness, screenHeight);
        fixtureDef.shape = rightShape;
        rightBoundary.createFixture(fixtureDef);
        rightBoundary.setUserData("boundary");
        rightShape.dispose();
    }

    public void updateGame(float gameWidth, float gameHeight) {
        // Update movement managers (input processing, etc.)
        playerMovementManager.updateDirection(inputManager.getPressedKeys(), inputManager.getKeyBindings());
        playerMovementManager.updateMovement();
        npcMovementManager.updateMovement();
    
        // Always update player (bucket) from input, regardless of collision state
        float bucketX = playerMovementManager.getX();
        float bucketY = playerMovementManager.getY();
    
        // Calculate the bucket's half-width and half-height
        float bucketHalfWidth = bucket.getEntity().getWidth() / 2;
        float bucketHalfHeight = bucket.getEntity().getHeight() / 2;
    
        // Clamp player positions so the player remains within screen bounds
        bucketX = Math.max(bucketHalfWidth, Math.min(bucketX, gameWidth - bucketHalfWidth));
        bucketY = Math.max(bucketHalfHeight, Math.min(bucketY, gameHeight - bucketHalfHeight));
    
        // Update player's entity and Box2D body (convert pixels → meters)
        bucket.getEntity().setX(bucketX);
        bucket.getEntity().setY(bucketY);
        bucket.getBody().setTransform(bucketX / GameConstants.PIXELS_TO_METERS,
                bucketY / GameConstants.PIXELS_TO_METERS, 0);
    
        // For the NPC (drop), check if it's in collision and blend if needed
        if (!drop.isInCollision()) {
            // Normal update when no collision is active for the NPC
            float dropX = npcMovementManager.getX();
            float dropY = npcMovementManager.getY();
    
            // Calculate the drop's half-width and half-height
            float dropHalfWidth = drop.getEntity().getWidth() / 2;
            float dropHalfHeight = drop.getEntity().getHeight() / 2;
    
            // Clamp NPC positions
            dropX = Math.max(dropHalfWidth, Math.min(dropX, gameWidth - dropHalfWidth));
            dropY = Math.max(dropHalfHeight, Math.min(dropY, gameHeight - dropHalfHeight));
    
            // Update drop's entity and Box2D body (convert pixels → meters)
            drop.getEntity().setX(dropX);
            drop.getEntity().setY(dropY);
            drop.getBody().setTransform(dropX / GameConstants.PIXELS_TO_METERS,
                    dropY / GameConstants.PIXELS_TO_METERS, 0);
        } else {
            // COLLISION MODE for NPC:
            // Get current physics position (in pixels)
            float physicsDropX = drop.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS;
            float physicsDropY = drop.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS;
    
            // Retrieve desired input position from the movement manager
            float inputDropX = npcMovementManager.getX();
            float inputDropY = npcMovementManager.getY();
    
            // Blend physics with input using a blending factor
            float blendFactor = 0.1f; // adjust as needed
            float newDropX = physicsDropX + (inputDropX - physicsDropX) * blendFactor;
            float newDropY = physicsDropY + (inputDropY - physicsDropY) * blendFactor;
    
            // Update NPC's entity to the blended value and synchronize the movement manager
            // so stale input does not accumulate
            drop.getEntity().setX(newDropX);
            drop.getEntity().setY(newDropY);
            npcMovementManager.setX(newDropX);
            npcMovementManager.setY(newDropY);
        }
    }

    public void syncEntityPositions() {
        bucket.getEntity().setX(bucket.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS);
        bucket.getEntity().setY(bucket.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS);
        drop.getEntity().setX(drop.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS);
        drop.getEntity().setY(drop.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS);
    }
}