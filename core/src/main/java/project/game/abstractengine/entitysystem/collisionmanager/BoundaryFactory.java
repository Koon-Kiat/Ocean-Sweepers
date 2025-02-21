package project.game.abstractengine.entitysystem.collisionmanager;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.constants.GameConstants;

public class BoundaryFactory {

    /**
     * Creates screen boundaries using the specified scene width, height, and
     * boundary thickness.
     * 
     * @param world         the Box2D world
     * @param gameWidth     width of the game scene in pixels
     * @param gameHeight    height of the game scene in pixels
     * @param edgeThickness thickness of the boundary in meters
     */
    public static void createScreenBoundaries(World world, float gameWidth, float gameHeight, float edgeThickness) {
        // Convert pixel dimensions to Box2D meters.
        float screenWidth = gameWidth / GameConstants.PIXELS_TO_METERS;
        float screenHeight = gameHeight / GameConstants.PIXELS_TO_METERS;

        // Create a BodyDef for static boundaries.
        BodyDef boundaryDef = new BodyDef();
        boundaryDef.type = BodyDef.BodyType.StaticBody;

        // Create a FixtureDef for boundaries.
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.2f;

        // Top boundary.
        boundaryDef.position.set(0, screenHeight);
        Body topBoundary = world.createBody(boundaryDef);
        PolygonShape topShape = new PolygonShape();
        topShape.setAsBox(screenWidth, edgeThickness);
        fixtureDef.shape = topShape;
        topBoundary.createFixture(fixtureDef);
        topBoundary.setUserData("boundary");
        topShape.dispose();

        // Bottom boundary.
        boundaryDef.position.set(0, 0);
        Body bottomBoundary = world.createBody(boundaryDef);
        PolygonShape bottomShape = new PolygonShape();
        bottomShape.setAsBox(screenWidth, edgeThickness);
        fixtureDef.shape = bottomShape;
        bottomBoundary.createFixture(fixtureDef);
        bottomBoundary.setUserData("boundary");
        bottomShape.dispose();

        // Left boundary.
        boundaryDef.position.set(0, 0);
        Body leftBoundary = world.createBody(boundaryDef);
        PolygonShape leftShape = new PolygonShape();
        leftShape.setAsBox(edgeThickness, screenHeight);
        fixtureDef.shape = leftShape;
        leftBoundary.createFixture(fixtureDef);
        leftBoundary.setUserData("boundary");
        leftShape.dispose();

        // Right boundary.
        boundaryDef.position.set(screenWidth, 0);
        Body rightBoundary = world.createBody(boundaryDef);
        PolygonShape rightShape = new PolygonShape();
        rightShape.setAsBox(edgeThickness, screenHeight);
        fixtureDef.shape = rightShape;
        rightBoundary.createFixture(fixtureDef);
        rightBoundary.setUserData("boundary");
        rightShape.dispose();
    }

    private BoundaryFactory() {
    }
}