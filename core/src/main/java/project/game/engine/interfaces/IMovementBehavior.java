package project.game.engine.interfaces;

import project.game.engine.entitysystem.entitymanager.MovableEntity;

/**
 * Interface for movement behaviors.
 * 
 * Classes implementing this interface must provide methods to apply movement
 * behaviors to entities.
 */
public interface IMovementBehavior {

    void applyMovementBehavior(MovableEntity entity, float deltaTime);
}
