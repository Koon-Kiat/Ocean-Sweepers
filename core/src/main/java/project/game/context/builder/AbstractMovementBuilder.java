package project.game.context.builder;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.context.factory.GameConstantsFactory;
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * Base builder class for movement builders.
 * 
 * It provides fluent method chaining for setting an entity, speed, and
 * initial velocity, and ensures that parameters are validated.
 */
public abstract class AbstractMovementBuilder<T extends AbstractMovementBuilder<T>> {

    protected static final GameLogger LOGGER = new GameLogger(AbstractMovementBuilder.class);
    protected MovableEntity movableEntity;
    protected Entity entity;
    protected float speed;
    protected Vector2 initialVelocity = new Vector2(0, 0);
    protected IMovementStrategy movementStrategy;
    protected boolean lenientMode = false;

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

    public MovableEntity getMovableEntity() {
        if (movableEntity != null) {
            return movableEntity;
        } else if (entity != null) {
            // You need to implement this method in concrete builder classes
            return createMovableEntityFromEntity(entity, speed);
        } else {
            String errorMessage = "No entity provided.";
            LOGGER.fatal(errorMessage);
            if (lenientMode) {
                LOGGER.warn("No entity provided in lenient mode. This will likely cause issues later.");
                return null;
            } else {
                throw new MovementException(errorMessage);
            }
        }
    }

    public T withMovableEntity(MovableEntity movableEntity) {
        this.movableEntity = movableEntity;
        return self();
    }

    public float getSpeed() {
        return speed;
    }

    public T setSpeed(float speed) {
        if (speed < 0) {
            String errorMessage = "Negative speed provided: " + speed;
            LOGGER.fatal(errorMessage);
            if (lenientMode) {
                this.speed = GameConstantsFactory.getConstants().DEFAULT_SPEED();
            } else {
                throw new MovementException("Speed must be non-negative.");
            }
        } else {
            this.speed = speed;
        }
        return self();
    }

    public Vector2 getInitialVelocity() {
        return initialVelocity;
    }

    public T setInitialVelocity(Vector2 initialVelocity) {
        if (initialVelocity == null) {
            LOGGER.warn("Null velocity provided. Defaulting to zero velocity.");
            this.initialVelocity = new Vector2(0, 0);
        } else {
            this.initialVelocity = initialVelocity;
        }
        return self();
    }

    public T setInitialVelocity(float x, float y) {
        this.initialVelocity = new Vector2(x, y);
        return self();
    }

    public IMovementStrategy getMovementStrategy() {
        return movementStrategy;
    }

    public boolean isLenientMode() {
        return lenientMode;
    }

    public T setLenientMode(boolean lenientMode) {
        this.lenientMode = lenientMode;
        return self();
    }

    protected abstract void validateBuildRequirements();

    protected abstract MovableEntity createMovableEntityFromEntity(Entity entity, float speed);

}