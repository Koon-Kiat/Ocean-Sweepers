package project.game.engine.entitysystem.collision;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.context.core.GameConstants;

/**
 * BoundaryFactory is a factory class that creates screen boundaries for a Box2D
 * world.
 */
public class BoundaryFactory {

    /**
     * A class representing a single boundary in the game world.
     */
    private static class Boundary {
        private final World world;
        private final float posX;
        private final float posY;
        private final float halfWidth;
        private final float halfHeight;
        private final BodyDef.BodyType bodyType;
        private final float density;
        private final float friction;
        private final float restitution;
        private final String userData;

        /**
         * Creates a new boundary with specified physical properties. The boundary is
         * defined as a rectangular Box2D body with the specified parameters.
         *
         * @param world       The Box2D world where the boundary will be created
         * @param posX        Center x-position of the boundary in Box2D coordinates
         * @param posY        Center y-position of the boundary in Box2D coordinates
         * @param halfWidth   Half of the total width for the boundary rectangle (in
         *                    Box2D meters)
         * @param halfHeight  Half of the total height for the boundary rectangle (in
         *                    Box2D meters)
         * @param bodyType    The body type (typically StaticBody for boundaries)
         * @param density     Density of the fixture (affects mass in dynamic bodies)
         * @param friction    Friction of the fixture (affects sliding behavior)
         * @param restitution Bounciness of the fixture (affects how objects bounce off)
         * @param userData    String identifier for the body used in collision detection
         */
        public Boundary(World world,
                float posX,
                float posY,
                float halfWidth,
                float halfHeight,
                BodyDef.BodyType bodyType,
                float density,
                float friction,
                float restitution,
                String userData) {
            this.world = world;
            this.posX = posX;
            this.posY = posY;
            this.halfWidth = halfWidth;
            this.halfHeight = halfHeight;
            this.bodyType = bodyType;
            this.density = density;
            this.friction = friction;
            this.restitution = restitution;
            this.userData = userData;
        }

        /**
         * Creates and adds this boundary to the Box2D world.
         */
        public void create() {
            // Create body definition
            BodyDef boundaryDef = new BodyDef();
            boundaryDef.type = bodyType;
            boundaryDef.position.set(posX, posY);

            // Create the body
            Body body = world.createBody(boundaryDef);

            // Create shape
            PolygonShape shape = new PolygonShape();
            // setAsBox expects half-width and half-height
            shape.setAsBox(halfWidth, halfHeight);

            // Create fixture
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = density;
            fixtureDef.friction = friction;
            fixtureDef.restitution = restitution;

            body.createFixture(fixtureDef);
            body.setUserData(userData);

            // Cleanup
            shape.dispose();
        }
    }

    /**
     * Creates screen boundaries using the specified scene width (pixels), height
     * (pixels) and boundary thickness (meters).
     *
     * @param world         the Box2D world
     * @param gameWidth     the width in pixels
     * @param gameHeight    the height in pixels
     * @param edgeThickness the boundary thickness in Box2D meters
     */
    public static void createScreenBoundaries(World world,
            float gameWidth,
            float gameHeight,
            float edgeThickness) {

        // Convert pixel dimensions to Box2D meters
        float screenWidth = gameWidth / GameConstants.PIXELS_TO_METERS;
        float screenHeight = gameHeight / GameConstants.PIXELS_TO_METERS;
        float edgeThicknessMeters = edgeThickness / GameConstants.PIXELS_TO_METERS;

        // Half-thickness of the boundary (Box2D uses half-extents)
        float halfThickness = edgeThicknessMeters / 2f;

        // Common physical properties
        BodyDef.BodyType bodyType = BodyDef.BodyType.StaticBody;
        float density = 1f;
        float friction = 0.4f;
        float restitution = 0.2f;
        String userData = "boundary";

        new Boundary(
                world,
                screenWidth / 2f, // centerX
                screenHeight - halfThickness, // centerY
                screenWidth / 2f, // halfWidth = half the total width
                halfThickness, // halfHeight = half the total thickness
                bodyType, density, friction, restitution, userData).create();

        new Boundary(
                world,
                screenWidth / 2f,
                halfThickness,
                screenWidth / 2f,
                halfThickness,
                bodyType, density, friction, restitution, userData).create();

        new Boundary(
                world,
                halfThickness,
                screenHeight / 2f,
                halfThickness,
                screenHeight / 2f,
                bodyType, density, friction, restitution, userData).create();

        new Boundary(
                world,
                screenWidth - halfThickness,
                screenHeight / 2f,
                halfThickness,
                screenHeight / 2f,
                bodyType, density, friction, restitution, userData).create();
    }

    private BoundaryFactory() {
        // Private constructor to prevent instantiation
    }
}
