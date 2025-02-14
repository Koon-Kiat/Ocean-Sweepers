package project.game.abstractengine.entitymanager.interfaces;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Renderable {
	
	String getTexturePath();
	
	void render(SpriteBatch batch);
}
