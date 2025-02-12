package project.game.abstractengine.testentity;

import project.game.abstractengine.entitymanager.Entity;
import project.game.abstractengine.entitymanager.PlayableMovableEntity;
import project.game.abstractengine.entitymanager.interfaces.*;
import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.movementmanager.PlayerMovementManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BucketEntity extends PlayableMovableEntity implements Movable, Collidable, Renderable {
	
	private PlayerMovementManager movementManager;
	private String texturePath;
	
	public BucketEntity(Entity entity, float speed, PlayerMovementManager movementManager, String texturePath) {
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
        if (  isActive() && GameAsset.getInstance().isLoaded()) {
            Texture texture = GameAsset.getInstance().getAsset(texturePath, Texture.class);
            batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        }
    }
	
	public PlayerMovementManager getMovementManager() {
		return this.movementManager;
	}
	
}
