package project.game.abstractengine.entitysystem.entitymanager;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;

public abstract class RenderableEntity extends Entity implements IRenderable {
	private final Entity entity;
	private final String texturePath;

	public RenderableEntity(Entity entity, String texturePath) {
		this.entity = entity;
		this.texturePath = texturePath;
	}

	public void loadTexture() {
		GameAsset.getInstance().loadTextureAssets(texturePath);
	}

	@Override
	public void render(SpriteBatch batch) {
		if (entity.isActive() && GameAsset.getInstance().isLoaded()) {
			Texture texture = GameAsset.getInstance().getAsset(texturePath, Texture.class);
			batch.draw(texture, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
		}
	}

	public Entity getEntity() {
		return entity;
	}

	public abstract void update();
}
