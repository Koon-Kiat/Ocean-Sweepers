package project.game.abstractengine.testentity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.entitymanager.NPCMovableEntity;
import project.game.abstractengine.entitysystem.interfaces.ICollidable;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;
import project.game.abstractengine.entitysystem.movementmanager.NPCMovementManager;

public class DropEntity extends NPCMovableEntity implements ICollidable, IRenderable {

	private NPCMovementManager movementManager;
	private String texturePath;

	public DropEntity(Entity entity, float speed, NPCMovementManager movementManager, String texturePath) {
		super(entity, speed, movementManager);
		this.texturePath = texturePath;
		GameAsset.getInstance().loadTextureAssets(texturePath);
	}

	@Override
	public boolean checkCollision(Entity other) {
		return super.getEntity().getX() < other.getX() + other.getWidth() &&
				getEntity().getX() + getEntity().getWidth() > other.getX() &&
				getEntity().getY() < other.getY() + other.getHeight() &&
				getEntity().getY() + getEntity().getHeight() > other.getY();
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

	public NPCMovementManager getMovementManager() {
		return this.movementManager;
	}
}
