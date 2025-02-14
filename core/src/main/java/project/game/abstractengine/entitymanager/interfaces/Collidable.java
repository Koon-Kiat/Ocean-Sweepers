package project.game.abstractengine.entitymanager.interfaces;

import project.game.abstractengine.entitymanager.Entity;

public interface Collidable {
	
	boolean checkCollision(Entity other);
	
	void onCollision(Entity other);
}
