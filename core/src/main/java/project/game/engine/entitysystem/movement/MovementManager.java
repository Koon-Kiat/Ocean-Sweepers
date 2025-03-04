package project.game.engine.entitysystem.movement;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementManager;
import project.game.engine.api.movement.IMovementStrategy;

/**
 * MovementManager manages movement for entities implementing IMovable.
 * Uses aggregation to separate movement logic from entity state.
 */
public class MovementManager implements IMovementManager {

    private static final GameLogger LOGGER = new GameLogger(MovementManager.class);
    private final IMovable movable;
    private IMovementStrategy movementStrategy;
    private final boolean lenientMode;

    /**
     * Constructs a MovementManager with the specified parameters.
     */
    public MovementManager(IMovable movable, float speed, Vector2 initialVelocity,
            IMovementStrategy movementStrategy, boolean lenientMode) {
        if (movable == null) {
            throw new MovementException("MovableEntity cannot be null");
        }
        this.movable = movable;
        this.lenientMode = lenientMode;

        // Handle initial speed validation
        float correctedSpeed = speed;
        if (speed < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative speed provided ({0}). Using absolute value.", speed);
                correctedSpeed = Math.abs(speed);
            } else {
                throw new MovementException("Speed cannot be negative: " + speed);
            }
            setSpeed(correctedSpeed);
        }

        // Set movement strategy with validation
        if (movementStrategy == null) {
            if (lenientMode) {
                LOGGER.warn("Movement strategy is null. Defaulting to VectorMovementBehavior.");
                this.movementStrategy = project.game.context.factory.MovementBehaviorFactory.createDefaultMovement();
            } else {
                throw new MovementException("Movement strategy cannot be null");
            }
        } else {
            this.movementStrategy = movementStrategy;
        }

        // Set initial velocity if provided in the entity
        if (initialVelocity != null && (initialVelocity.x != 0 || initialVelocity.y != 0)) {
            setVelocity(initialVelocity);
        }
    }

    public IMovementStrategy getMovementStrategy() {
        return movementStrategy;
    }

    public void setMovementStrategy(IMovementStrategy movementStrategy) {
        if (movementStrategy == null) {
            String msg = "Movement strategy cannot be null.";
            LOGGER.fatal(msg);
            throw new MovementException(msg);
        }
        this.movementStrategy = movementStrategy;
    }

    public IMovable getMovableEntity() {
        return movable;
    }

    @Override
    public float getX() {
        return movable.getX();
    }

    @Override
    public void setX(float x) {
        try {
            movable.setX(x);
        } catch (Exception e) {
            LOGGER.error("Error setting X position to " + x, e);
            if (!lenientMode) {
                throw new MovementException("Failed to set X position", e);
            }
        }
    }

    @Override
    public float getY() {
        return movable.getY();
    }

    @Override
    public void setY(float y) {
        try {
            movable.setY(y);
        } catch (Exception e) {
            LOGGER.error("Error setting Y position to " + y, e);
            if (!lenientMode) {
                throw new MovementException("Failed to set Y position", e);
            }
        }
    }

    @Override
    public float getSpeed() {
        return movable.getSpeed();
    }

    @Override
    public final void setSpeed(float speed) {
        if (speed < 0 && !lenientMode) {
            throw new MovementException("Speed cannot be negative: " + speed);
        }
        movable.setSpeed(speed < 0 ? Math.abs(speed) : speed);
    }

    @Override
    public Vector2 getVelocity() {
        return movable.getVelocity();
    }

    @Override
    public final void setVelocity(Vector2 velocity) {
        if (velocity == null) {
            if (lenientMode) {
                LOGGER.warn("Null velocity provided. Using zero vector instead.");
                movable.setVelocity(0, 0);
            } else {
                throw new MovementException("Velocity vector cannot be null");
            }
        } else {
            movable.setVelocity(velocity);
        }
    }

    @Override
    public void setVelocity(float x, float y) {
        movable.setVelocity(x, y);
    }

    @Override
    public void normalizeVelocity() {
        Vector2 velocity = getVelocity();
        if (velocity != null && velocity.len2() > 0) {
            velocity.nor();
            setVelocity(velocity);
        }
    }

    @Override
    public void clearVelocity() {
        setVelocity(0, 0);
    }

    public void applyMovementUpdate(float dt) {
        if (movementStrategy == null) {
            LOGGER.fatal("Cannot update position: movement strategy is not set.");
            return;
        }

        try {
            movementStrategy.move(movable, dt);
        } catch (Exception e) {
            String errorMessage = "Error during movement strategy update: " + e.getMessage();
            LOGGER.fatal(errorMessage, e);
            if (lenientMode) {
                clearVelocity();
            } else {
                throw new MovementException(errorMessage, e);
            }
        }
    }

    @Override
    public void updateMovement() {
        float dt = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        try {
            applyMovementUpdate(dt);
        } catch (Exception e) {
            LOGGER.fatal("Error updating movement: " + e.getMessage(), e);
            if (!lenientMode) {
                throw new MovementException("Failed to update movement", e);
            }
        }
    }

    @Override
    public void updateVelocity(Set<Integer> pressedKeys, Map<Integer, Vector2> keyBindings) {
        Vector2 resultVelocity = new Vector2(0, 0);

        // Accumulate velocity from all pressed keys
        for (Integer key : pressedKeys) {
            Vector2 vec = keyBindings.get(key);
            if (vec != null) {
                resultVelocity.add(vec);
            }
        }

        // Normalize and apply speed if we have movement
        if (resultVelocity.len2() > 0.0001f) {
            // This ensures diagonal movement doesn't go faster than cardinal movement
            resultVelocity.nor().scl(getSpeed());
        }

        setVelocity(resultVelocity);
    }

    public boolean isLenientMode() {
        return lenientMode;
    }
}
