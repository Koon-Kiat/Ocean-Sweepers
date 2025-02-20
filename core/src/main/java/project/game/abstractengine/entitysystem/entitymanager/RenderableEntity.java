package project.game.abstractengine.entitysystem.entitymanager;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.abstractengine.assetmanager.assetManager;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;

public abstract class RenderableEntity extends Entity implements IRenderable {
	private final Entity entity;
	private final String texturePath;

	public RenderableEntity(Entity entity, String texturePath) {
		this.entity = entity;
		this.texturePath = texturePath;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public String getTexturePath() {
		return this.texturePath;
	}

	public void loadTexture() {
		assetManager.getInstance().loadTextureAssets(texturePath);
	}

	@Override
	public void render(SpriteBatch batch) {
		if (entity.isActive() && assetManager.getInstance().isLoaded()) {
			Texture texture = assetManager.getInstance().getAsset(texturePath, Texture.class);
			batch.draw(texture, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
		}
	}

	public abstract void update();
}
