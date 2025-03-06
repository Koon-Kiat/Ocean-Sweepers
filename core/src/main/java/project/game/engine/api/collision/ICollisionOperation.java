package project.game.engine.api.collision;

import java.util.List;

/**
 * Interface for handling collisions using the visitor pattern
 * 
 * This interface is used to handle collisions between entities in the game. The
 * visitor pattern is used to allow for flexible collision handling between
 * different types of entities.
 */
public interface ICollisionOperation {

    void handleCollisionWith(Object other, List<Runnable> collisionQueue);

    boolean handlesCollisionWith(Class<?> clazz);
}