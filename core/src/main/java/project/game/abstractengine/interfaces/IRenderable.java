package project.game.abstractengine.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IRenderable {

	String getTexturePath();

	void render(SpriteBatch batch);
}
