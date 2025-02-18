package project.game.abstractengine.testentity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.assetmanager.GameAsset;
import project.game.Direction;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;

public class DropEntity extends NPCMovementManager implements ICollidable, IRenderable {

	private static final float PIXELS_TO_METERS = 32f; // Define the conversion factor

	private final NPCMovementManager movementManager;
	private final String texturePath;
	private final Body body;

	public DropEntity(Entity entity, World world, NPCMovementManager movementManager, String texturePath) {
		super(movementManager.getBuilder());
		this.movementManager = movementManager;
		this.texturePath = texturePath;
		this.setWidth(entity.getWidth());
		this.setHeight(entity.getHeight());
		this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
		GameAsset.getInstance().loadTextureAssets(texturePath);

		// Set initial velocity
		body.setLinearVelocity(1, 1); // Adjust the values as needed
	}

	@Override
	public Body getBody() {
		return this.body;
	}

	@Override
	public String getTexturePath() {
		return this.texturePath;
	}

	@Override
	public Entity getEntity() {
		return this;
	}

	@Override
	public boolean checkCollision(Entity other) {
		System.out.println("checkCollision called. Other: " + other); // Debug print
		if (other == null) {
			// Collision with screen boundary
			return true;
		} else if (other instanceof ICollidable) {
			return true;
		}
		return false; // Or handle the case where 'other' is not ICollidable appropriately
	}

	@Override
public void onCollision(Entity other) {
    System.out.println(getEntity().getClass().getSimpleName() + " collided with "
            + (other == null ? "boundary" : other.getClass().getSimpleName()));

    if (other instanceof BucketEntity) {
		System.out.println("Colliding with BucketEntity"); // Debug print
        // Calculate a bounce direction (you might need to adjust this)
        float bounceAngle = (float) Math.toRadians(75); // Adjust the angle as needed
        float bounceX = (float) Math.cos(bounceAngle);
        float bounceY = (float) Math.sin(bounceAngle);
		System.out.println("Bounce Angle: " + bounceAngle + ", X: " + bounceX + ", Y: " + bounceY); // Debug print

        // Apply an impulse to the droplet's body
        float impulseStrength = 10f; // Adjust the strength as needed
		System.out.println("Impulse Strength: " + impulseStrength); // Debug print
        body.applyLinearImpulse(bounceX * impulseStrength, bounceY * impulseStrength, body.getPosition().x,
                body.getPosition().y, true);
    } else {
		System.out.println("Colliding with Boundary"); // Debug print
        Vector2 currentVelocity = body.getLinearVelocity();
		System.out.println("Current Velocity: " + currentVelocity); // Debug print

        // Reflect the velocity based on the collision normal (simplified)
        Vector2 reflectedVelocity = new Vector2(-currentVelocity.x, -currentVelocity.y);
		System.out.println("Reflected Velocity: " + reflectedVelocity); // Debug print

        // Apply a small impulse to ensure continuous movement
        float impulseStrength = 5f; // Adjust as needed
		System.out.println("Impulse Strength: " + impulseStrength); // Debug print
        body.applyLinearImpulse(reflectedVelocity.x * impulseStrength, reflectedVelocity.y * impulseStrength,
                body.getPosition().x, body.getPosition().y, true);

        // Update the NPCMovementManager's direction
        movementManager.setDirection(reflectedVelocity);
    }
}

	@Override
	public void render(SpriteBatch batch) {
		if (isActive() && GameAsset.getInstance().isLoaded()) {
			Texture texture = GameAsset.getInstance().getAsset(texturePath, Texture.class);
			batch.draw(texture, getX(), getY(), getWidth(), getHeight());
		}
	}

	@Override
	public final Body createBody(World world, float x, float y, float width, float height) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x / PIXELS_TO_METERS, y / PIXELS_TO_METERS); // Scale position

		Body createdBody = world.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2, height / 2);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.3f;
		fixtureDef.restitution = 0.5f;

		Fixture fixture = createdBody.createFixture(fixtureDef); // Get the Fixture
		fixture.setUserData(this); // Set user data on the fixture
		shape.dispose();

		return createdBody;
	}

	public NPCMovementManager getMovementManager() {
		return this.movementManager;
	}
}