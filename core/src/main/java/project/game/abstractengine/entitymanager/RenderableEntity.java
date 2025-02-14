package project.game.abstractengine.entitymanager;

import project.game.abstractengine.entitymanager.interfaces.Renderable;
import project.game.abstractengine.assetmanager.GameAsset;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class RenderableEntity extends Entity implements Renderable {
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
		if(entity.isActive() && GameAsset.getInstance().isLoaded()) {
			Texture texture = GameAsset.getInstance().getAsset(texturePath,  Texture.class);
			batch.draw(texture, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
		}
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public String getTexturePath() {
		return this.texturePath;
	}

	public abstract void update();
}
