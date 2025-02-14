package project.game.abstractengine.entitymanager.interfaces;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import project.game.abstractengine.entitymanager.Entity;

public interface Collidable {
	
	Entity getEntity();
	
	Body getBody();
	
	boolean checkCollision(Entity other);
	
	void onCollision(Entity other);
	
	Body createBody(World world, float x, float y, float width, float height);
}
