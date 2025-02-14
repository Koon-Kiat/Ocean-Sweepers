package project.game.abstractengine.entitysystem.interfaces;

import project.game.abstractengine.entitysystem.entitymanager.Entity;

public interface ICollidable {

	boolean checkCollision(Entity other);

	void onCollision(Entity other);
}
