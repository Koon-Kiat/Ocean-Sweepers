package project.game.engine.entitysystem.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.engine.api.render.IRenderable;
import project.game.engine.asset.CustomAssetManager;

/**
 * RenderableEntity is an abstract class that extends Entity and provides the
 * necessary methods and fields for entities that are renderable.
 */
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
		CustomAssetManager.getInstance().loadTextureAssets(texturePath);
	}

	@Override
	public void render(SpriteBatch batch) {
		if (entity.isActive() && CustomAssetManager.getInstance().isLoaded()) {
			Texture texture = CustomAssetManager.getInstance().getAsset(texturePath, Texture.class);
			batch.draw(texture, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
		}
	}

	public abstract void update();
}
