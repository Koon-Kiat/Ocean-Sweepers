package project.game.engine.api;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Interface for renderable objects.
 * 
 * Classes implementing this interface must provide methods to render objects.
 */
public interface IRenderable {

	String getTexturePath();

	void render(SpriteBatch batch);
}
