package project.game.context.builder;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.interfaces.IMovementBehavior;
import project.game.constants.GameConstants;
import project.game.exceptions.MovementException;

/**
 * Base builder class for movement builders.
 * 
 * It provides fluent method chaining for setting an entity, speed, and
 * direction, and ensures that parameters are validated.
 */
public abstract class AbstractMovementBuilder<T extends AbstractMovementBuilder<T>> {

    protected static final Logger LOGGER = Logger.getLogger(AbstractMovementBuilder.class.getName());
    protected Entity entity;
    protected float speed;
    protected Direction direction = Direction.NONE;
    protected IMovementBehavior movementBehavior;

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public Entity getEntity() {
        return entity;
    }

    public T withEntity(Entity entity) {
        this.entity = entity;
        return self();
    }

    public float getSpeed() {
        return speed;
    }

    public T setSpeed(float speed) {
        if (speed < 0) {
            String errorMessage = "Negative speed provided: " + speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            if (project.game.abstractengine.entitysystem.movementmanager.MovementManager.LENIENT_MODE) {
                this.speed = GameConstants.DEFAULT_SPEED;
            } else {
                throw new MovementException("Speed must be non-negative.");
            }
        } else {
            this.speed = speed;
        }
        return self();
    }

    public Direction getDirection() {
        return direction;
    }

    public T setDirection(Direction direction) {
        if (direction == null) {
            LOGGER.log(Level.WARNING, "Null direction provided. Defaulting to Direction.NONE.");
            this.direction = Direction.NONE;
        } else {
            this.direction = direction;
        }
        return self();
    }

    public IMovementBehavior getMovementBehavior() {
        return movementBehavior;
    }

    protected abstract void validateBuildRequirements();
}