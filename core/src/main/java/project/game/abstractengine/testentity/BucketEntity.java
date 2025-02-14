package project.game.abstractengine.testentity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;
import project.game.abstractengine.entitysystem.movementmanager.PlayerMovementManager;
import project.game.builder.PlayerMovementBuilder;

public class BucketEntity extends PlayerMovementBuilder implements ICollidable, IRenderable {

	private final Entity entity;
	private final PlayerMovementManager movementManager;
	private final String texturePath;

	public BucketEntity(Entity entity, PlayerMovementManager movementManager, String texturePath) {
		super();
		this.entity = entity;
		this.movementManager = movementManager;
		this.texturePath = texturePath;
		GameAsset.getInstance().loadTextureAssets(texturePath);
	}

	public Entity getEntity() {
		return this.entity;
	}

	public float getWidth() {
		return entity.getWidth();
	}

	public float getHeight() {
		return entity.getHeight();
	}

	public String getID() {
		return entity.getID();
	}

	public boolean isActive() {
		return entity.isActive();
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

	public PlayerMovementManager getMovementManager() {
		return this.movementManager;
	}
}