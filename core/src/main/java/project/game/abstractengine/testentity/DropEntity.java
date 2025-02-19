package project.game.abstractengine.testentity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;
import project.game.constants.GameConstants;

public class DropEntity implements ICollidable, IRenderable {

	private final float IMPUSLE_STRENGTH = 5f;
	private final Entity entity;
	private final NPCMovementManager movementManager;
	private final String texturePath;
	private final Body body;
	private boolean collisionActive = false;
	private long collisionEndTime = 0;

	public DropEntity(Entity entity, World world, NPCMovementManager movementManager, String texturePath) {
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
		return true;
	}

	@Override
	public void onCollision(ICollidable other) {
		System.out.println(getEntity().getClass().getSimpleName() + " collided with " +
				(other == null ? "boundary" : other.getClass().getSimpleName()));
		setCollisionActive(GameConstants.COLLISION_ACTIVE_DURATION);
		if (other != null && (other instanceof BucketEntity)) {
			float impulseStrength = IMPUSLE_STRENGTH;
			Vector2 myPos = body.getPosition();
			Vector2 otherPos = other.getBody().getPosition();
			Vector2 normal = new Vector2(myPos.x - otherPos.x, myPos.y - otherPos.y).nor();
			Vector2 impulse = normal.scl(impulseStrength);
			body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
		} else {
			Vector2 impulse = new Vector2(-5f, 5f);
			body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
		}
	}

	@Override
	public final Body createBody(World world, float x, float y, float width, float height) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		float centerX = (x + width / 2) / GameConstants.PIXELS_TO_METERS;
		float centerY = (y + height / 2) / GameConstants.PIXELS_TO_METERS;
		bodyDef.position.set(centerX, centerY);
		bodyDef.fixedRotation = true;
		bodyDef.linearDamping = 0.2f;

		Body newBody = world.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox((width / 2) / GameConstants.PIXELS_TO_METERS, (height / 2) / GameConstants.PIXELS_TO_METERS);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 5.0f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.0f;
		newBody.createFixture(fixtureDef);
		shape.dispose();
		newBody.setUserData(this);
		return newBody;
	}

	public NPCMovementManager getMovementManager() {
		return this.movementManager;
	}
}