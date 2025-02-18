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
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;

public class BucketEntity extends PlayerMovementManager implements ICollidable, IRenderable {

	private static final float PIXELS_TO_METERS = 32f; // Define the conversion factor

	private final PlayerMovementManager movementManager;
	private final String texturePath;
	private final Body body;

	public BucketEntity(Entity entity, World world, PlayerMovementManager movementManager, String texturePath) {
		super(movementManager.getBuilder());
		this.movementManager = movementManager;
		this.texturePath = texturePath;
		this.setWidth(entity.getWidth());
		this.setHeight(entity.getHeight());
		this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
		GameAsset.getInstance().loadTextureAssets(texturePath);
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
		if (other == null) {
			// Collision with screen boundary
			return true;
		} else if (other instanceof ICollidable) {
			Body otherBody = ((ICollidable) other).getBody();
			return body.getPosition().x < otherBody.getPosition().x + other.getWidth() &&
					body.getPosition().x + getWidth() > otherBody.getPosition().x &&
					body.getPosition().y < otherBody.getPosition().y + other.getHeight() &&
					body.getPosition().y + getHeight() > otherBody.getPosition().y;
		}
		return false; // Or handle the case where 'other' is not ICollidable appropriately
	}

	@Override
	public void onCollision(Entity other) {
		System.out.println(getEntity().getClass().getSimpleName() + " collided with "
				+ (other == null ? "boundary" : other.getClass().getSimpleName()));

		// Do nothing - the bucket should not move upon collision
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
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(x / PIXELS_TO_METERS, y / PIXELS_TO_METERS); // Scale position

		Body createBody = world.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2, height / 2);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.2f;

		Fixture fixture = createBody.createFixture(fixtureDef); // Get the Fixture
		fixture.setUserData(this); // Set user data on the fixture
		shape.dispose();

		return createBody;
	}

	public PlayerMovementManager getMovementManager() {
		return this.movementManager;
	}
}