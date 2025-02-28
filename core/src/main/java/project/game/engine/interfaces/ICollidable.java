package project.game.engine.interfaces;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import project.game.engine.entitysystem.entitymanager.Entity;

/**
 * Interface for collidable entities.
 * 
 * Provides methods for creating and checking collisions between entities.
 */
public interface ICollidable {

	Entity getEntity();

	Body getBody();

	Body createBody(World world, float x, float y, float width, float height);

	boolean checkCollision(Entity other);

	void onCollision(ICollidable other);

	boolean isInCollision();

}