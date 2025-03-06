package project.game.engine.api.collision;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import project.game.engine.entitysystem.entity.Entity;

/**
 * Interface for collidable entities.
 * 
 * Provides methods for creating and checking collisions between entities.
 */
public interface ICollidableVisitor {

	Entity getEntity();

	Body getBody();

	World getWorld();

	Body createBody(World world, float x, float y, float width, float height);

	boolean checkCollision(Entity other);

	void onCollision(ICollidableVisitor other);

	boolean isInCollision();

	void collideWith(Object other);

	void collideWithBoundary();

}