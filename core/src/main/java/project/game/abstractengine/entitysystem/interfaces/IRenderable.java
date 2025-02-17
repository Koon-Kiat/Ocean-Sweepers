package project.game.abstractengine.entitysystem.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IRenderable {

	String getTexturePath();

	void render(SpriteBatch batch);
}
