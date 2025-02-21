package project.game.abstractengine.interfaces;

import project.game.abstractengine.entitysystem.entitymanager.MovableEntity;

/**
 * Interface for movement behaviors.
 * 
 * Classes implementing this interface must provide methods to apply movement
 * behaviors to entities.
 */
public interface IMovementBehavior {

    void applyMovementBehavior(MovableEntity entity, float deltaTime);
}
