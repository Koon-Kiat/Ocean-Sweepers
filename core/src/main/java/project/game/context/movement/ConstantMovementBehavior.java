package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * Provides constant movement for movable entities.
 * 
 * The entity moves based on its velocity vector at a constant speed.
 * The speed is provided in the constructor.
 */
public class ConstantMovementBehavior implements IMovementBehavior {

    private static final GameLogger LOGGER = new GameLogger(ConstantMovementBehavior.class);
    private final float speed;
    private final boolean lenientMode;

    public ConstantMovementBehavior(float speed, boolean lenientMode) {
        this.lenientMode = lenientMode;
        if (speed < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative speed provided in ConstantMovementBehavior: {0}. Using absolute value.", speed);
                speed = Math.abs(speed);
            } else {
                String errorMessage = "Negative speed provided in ConstantMovementBehavior: " + speed;
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        this.speed = speed;
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            // Get current velocity
            Vector2 velocity = entity.getVelocity();

            // If velocity is zero, nothing to do
            if (velocity.len2() < 0.0001f) {
                return;
            }

            // Normalize and scale by speed and deltaTime
            Vector2 movement = new Vector2(velocity).nor().scl(speed * deltaTime);

            // Apply movement
            entity.setX(entity.getX() + movement.x);
            entity.setY(entity.getY() + movement.y);

        } catch (MovementException e) {
            LOGGER.error("Illegal argument in ConstantMovementBehavior.updatePosition: " + e.getMessage(), e);
            if (!lenientMode) {
                throw e;
            }
        } catch (Exception e) {
            String errorMessage = "Unexpected exception in ConstantMovementBehavior.updatePosition: " + e.getMessage();
            LOGGER.fatal(errorMessage, e);
            if (!lenientMode) {
                throw new MovementException("Error updating position in ConstantMovementBehavior", e);
            }
        }
    }
}
