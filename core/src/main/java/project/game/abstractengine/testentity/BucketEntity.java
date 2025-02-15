package project.game.abstractengine.testentity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;

public class BucketEntity extends PlayerMovementManager implements ICollidable, IRenderable {

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
		return getX() < other.getX() + other.getWidth() &&
				getX() + getWidth() > other.getX() &&
				getY() < other.getY() + other.getHeight() &&
				getY() + getHeight() > other.getY();
	}

	@Override
	public void onCollision(Entity other) {
		System.out.println(getID() + " collided with " + other.getID());
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
		bodyDef.position.set(x, y);

		Body createBody = world.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2, height / 2);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.3f;
		fixtureDef.restitution = 0.5f;

		createBody.createFixture(fixtureDef);
		shape.dispose();

		return createBody;
	}

	public PlayerMovementManager getMovementManager() {
		return this.movementManager;
	}
}