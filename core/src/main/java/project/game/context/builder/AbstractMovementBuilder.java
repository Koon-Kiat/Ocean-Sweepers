package project.game.context.builder;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.context.core.Direction;
import project.game.context.factory.GameConstantsFactory;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.entitysystem.entity.Entity;

/**
 * Base builder class for movement builders.
 * 
 * It provides fluent method chaining for setting an entity, speed, and
 * direction, and ensures that parameters are validated.
 */
public abstract class AbstractMovementBuilder<T extends AbstractMovementBuilder<T>> {

    protected static final GameLogger LOGGER = new GameLogger(AbstractMovementBuilder.class);
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
            LOGGER.fatal(errorMessage);
            if (project.game.engine.entitysystem.movement.MovementManager.LENIENT_MODE) {
                this.speed = GameConstantsFactory.getConstants().DEFAULT_SPEED();
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
            LOGGER.warn("Null direction provided. Defaulting to Direction.NONE.");
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