package project.game.engine.entitysystem.movement.management;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementManager;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;

/**
 * MovementManager manages movement for entities implementing IMovable.
 * Uses aggregation to separate movement logic from entity state.
 */
public class MovementManager implements IMovementManager {

    private static final GameLogger LOGGER = new GameLogger(MovementManager.class);
    private final IMovable movable;
    private final boolean lenientMode;
    private IMovementStrategy movementStrategy;

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
            movable.setSpeed(correctedSpeed);
        }

        // Set movement strategy with validation
        if (movementStrategy == null) {
            if (lenientMode) {
                LOGGER.warn("Movement strategy is null, but required for MovementManager.");
                throw new MovementException("Movement strategy cannot be null");
            } else {
                throw new MovementException("Movement strategy cannot be null");
            }
        } else {
            this.movementStrategy = movementStrategy;
        }

        // Set initial velocity if provided in the entity
        if (initialVelocity != null && (initialVelocity.x != 0 || initialVelocity.y != 0)) {
            movable.setVelocity(initialVelocity);
        }
    }

    @Override
    public IMovable getMovableEntity() {
        return movable;
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

    public boolean isLenientMode() {
        return lenientMode;
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
                movable.clearVelocity();
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
            resultVelocity.nor().scl(movable.getSpeed());
        }

        movable.setVelocity(resultVelocity);
    }
}
