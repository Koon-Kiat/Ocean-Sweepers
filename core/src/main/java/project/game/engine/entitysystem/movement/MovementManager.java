package project.game.engine.entitysystem.movement;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IMovementManager;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * MovementManager is an abstract class that provides basic movement
 * functionality for entities in the game.
 * 
 * It extends MovableEntity and implements IMovementManager.
 */
public abstract class MovementManager extends MovableEntity implements IMovementManager {

    private static final GameLogger LOGGER = new GameLogger(MovementManager.class);
    private IMovementBehavior movementBehavior;
    private final boolean lenientMode;

    /**
     * Constructs a MovementManager with the specified parameters.
     */
    public MovementManager(Entity entity, float speed, Vector2 initialVelocity, IMovementBehavior behavior,
            boolean lenientMode) {
        super(entity, speed);
        this.lenientMode = lenientMode;

        float correctedSpeed = speed;
        if (speed < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative speed provided ({0}). Using absolute value.", speed);
                correctedSpeed = Math.abs(speed);
            } else {
                throw new MovementException("Speed cannot be negative: " + speed);
            }
        }
        super.setSpeed(correctedSpeed);

        if (behavior == null) {
            if (lenientMode) {
                LOGGER.warn("Movement behavior is null. Defaulting to VectorMovementBehavior.");
                behavior = project.game.context.factory.MovementBehaviorFactory.createDefaultMovement();
            } else {
                throw new MovementException("Movement behavior cannot be null");
            }
        }

        if (initialVelocity != null) {
            setVelocity(initialVelocity);
        }
        this.movementBehavior = behavior;
    }

    public IMovementBehavior getMovementBehavior() {
        return movementBehavior;
    }

    public void setMovementBehavior(IMovementBehavior movementBehavior) {
        if (movementBehavior == null) {
            String msg = "Movement behavior cannot be null.";
            LOGGER.fatal(msg);
            throw new MovementException(msg);
        }
        this.movementBehavior = movementBehavior;
    }

    public void applyMovementUpdate(float dt) {
        if (movementBehavior == null) {
            LOGGER.fatal("Cannot update position: movement behavior is not set.");
            return;
        }

        try {
            movementBehavior.applyMovementBehavior(this, dt);
        } catch (Exception e) {
            String errorMessage = "Error during movement behavior update: " + e.getMessage();
            LOGGER.fatal(errorMessage, e);
            setVelocity(0, 0);
        }
    }

    @Override
    public void updateMovement() {
        float dt = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        try {
            applyMovementUpdate(dt);
        } catch (Exception e) {
            LOGGER.fatal("Error updating movement: " + e.getMessage(), e);
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
