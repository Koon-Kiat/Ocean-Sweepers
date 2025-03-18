package project.game.engine.entitysystem.entity.api;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Interface for renderable objects.
 * 
 * Classes implementing this interface must provide methods to render objects.
 */
public interface IRenderable {

	String getTexturePath();

	public boolean isRenderable();

	void render(SpriteBatch batch);
}
