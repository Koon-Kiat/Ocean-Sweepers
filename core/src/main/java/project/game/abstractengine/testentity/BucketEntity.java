package project.game.abstractengine.testentity;

import project.game.abstractengine.entitymanager.Entity;
import project.game.abstractengine.entitymanager.PlayableMovableEntity;
import project.game.abstractengine.entitymanager.interfaces.*;
import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entity.movementmanager.PlayerMovementManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class BucketEntity extends PlayableMovableEntity implements Movable, Collidable, Renderable {
	
	private PlayerMovementManager movementManager;
	private String texturePath;
	private final Body body;
	
	public BucketEntity(Entity entity, World world,  float speed, PlayerMovementManager movementManager, String texturePath) {
		super(entity, speed, movementManager);
		this.body = createBody(world, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
		this.texturePath = texturePath;
		GameAsset.getInstance().loadTextureAssets(texturePath);
		
	}
	
	public Body createBody(World world, float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.5f;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
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
        if (isActive() && GameAsset.getInstance().isLoaded()) {
            Texture texture = GameAsset.getInstance().getAsset(texturePath, Texture.class);
            batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        }
    }
	
	public PlayerMovementManager getMovementManager() {
		return this.movementManager;
	}
	
	public String getTexturePath() {
		return this.texturePath;
	}


	public Body getBody() {
		return this.body;
	}
	
}
