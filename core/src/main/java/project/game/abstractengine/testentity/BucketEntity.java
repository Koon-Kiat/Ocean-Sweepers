package project.game.abstractengine.testentity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.constants.GameConstants;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;

public class BucketEntity implements ICollidable, IRenderable {

	private final Entity entity;
	private final PlayerMovementManager movementManager;
	private final String texturePath;
	private final Body body;
	private boolean collisionActive = false;
	private long collisionEndTime = 0;

	public BucketEntity(Entity entity, World world, PlayerMovementManager movementManager, String texturePath) {
		this.entity = entity;
		this.movementManager = movementManager;
		this.texturePath = texturePath;
		this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
	}

	private float entityX() {
		return entity.getX();
	}

	private float entityY() {
		return entity.getY();
	}

	private float entityWidth() {
		return entity.getWidth();
	}

	private float entityHeight() {
		return entity.getHeight();
	}

	public boolean isActive() {
		return movementManager.isActive();
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public String getTexturePath() {
		return texturePath;
	}

	@Override
	public Entity getEntity() {
		return entity;
	}

	@Override
	public void render(SpriteBatch batch) {
		if (isActive() && GameAsset.getInstance().isLoaded()) {
			float renderX = entityX() - entityWidth() / 2;
			float renderY = entityY() - entityHeight() / 2;
			Texture texture = GameAsset.getInstance().getAsset(texturePath, Texture.class);
			batch.draw(texture, renderX, renderY, entityWidth(), entityHeight());
		}
	}

	public void setCollisionActive(long durationMillis) {
		collisionActive = true;
		collisionEndTime = System.currentTimeMillis() + durationMillis;
	}

	public boolean isInCollision() {
		if (collisionActive && System.currentTimeMillis() > collisionEndTime) {
			collisionActive = false;
		}
		return collisionActive;
	}

	@Override
	public boolean checkCollision(Entity other) {
		if (other == null) {
			return true;
		} else if (other instanceof ICollidable) {
			Body otherBody = ((ICollidable) other).getBody();
			return body.getPosition().x < otherBody.getPosition().x + other.getWidth() &&
					body.getPosition().x + entityWidth() > otherBody.getPosition().x &&
					body.getPosition().y < otherBody.getPosition().y + other.getHeight() &&
					body.getPosition().y + entityHeight() > otherBody.getPosition().y;
		}
		return false;
	}

	@Override
	public void onCollision(ICollidable other) {
		System.out.println(getEntity().getClass().getSimpleName() + " collided with " +
				(other == null ? "boundary" : other.getClass().getSimpleName()));
	}

	@Override
	public final Body createBody(World world, float x, float y, float width, float height) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		float centerX = (x + width / 2) / GameConstants.PIXELS_TO_METERS;
		float centerY = (y + height / 2) / GameConstants.PIXELS_TO_METERS;
		bodyDef.position.set(centerX, centerY);
		bodyDef.fixedRotation = true;
		bodyDef.linearDamping = 0.1f;

		Body newBody = world.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox((width / 2) / GameConstants.PIXELS_TO_METERS, (height / 2) / GameConstants.PIXELS_TO_METERS);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1000.0f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.1f;
		newBody.createFixture(fixtureDef);
		shape.dispose();
		newBody.setUserData(this);
		return newBody;
	}

	public PlayerMovementManager getMovementManager() {
		return this.movementManager;
	}
}