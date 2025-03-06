package project.game.context.movement;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;

/**
 * Provides zig-zag movement for movable entities.
 * 
 * The entity moves in a zig-zag pattern at a constant speed. The amplitude and
 * frequency of the oscillation are provided in the constructor. The entity
 * moves in the primary direction and oscillates in the perpendicular direction.
 */
public class ZigZagMovemenStrategy implements IMovementStrategy {

    private static final GameLogger LOGGER = new GameLogger(ZigZagMovemenStrategy.class);
    private final float speed;
    private final float amplitude;
    private final float frequency;
    private final boolean lenientMode;
    private float elapsedTime;

    public ZigZagMovemenStrategy(float speed, float amplitude, float frequency, boolean lenientMode) {
        this.lenientMode = lenientMode;
        if (speed < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative speed provided in ZigZagMovementBehavior: {0}. Using absolute value.", speed);
                speed = Math.abs(speed);
            } else {
                String errorMessage = "Illegal negative parameter in ZigZagMovementBehavior constructor: speed="
                        + speed;
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        if (frequency < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative frequency provided in ZigZagMovementBehavior: {0}. Using absolute value.",
                        frequency);
                frequency = Math.abs(frequency);
            } else {
                String errorMessage = "Illegal negative parameter in ZigZagMovementBehavior constructor: frequency="
                        + frequency;
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        this.speed = speed;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.elapsedTime = 0f;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            elapsedTime += deltaTime;

            // Get current velocity to determine primary direction
            Vector2 velocity = movable.getVelocity();

            // If velocity is too small, we can't determine a direction for zigzag
            if (velocity.len2() < 0.0001f) {
                return;
            }

            // Create a normalized copy of the primary direction
            Vector2 primaryDirection = new Vector2(velocity).nor();

            // Calculate perpendicular vector (rotate 90 degrees)
            Vector2 perpVector = new Vector2(-primaryDirection.y, primaryDirection.x);

            // Forward movement
            Vector2 movementDelta = new Vector2(primaryDirection).scl(speed * deltaTime);

            // Add zigzag oscillation
            float oscillation = amplitude * MathUtils.sin(frequency * elapsedTime);
            movementDelta.add(new Vector2(perpVector).scl(oscillation * deltaTime));

            // Apply movement
            movable.setX(movable.getX() + movementDelta.x);
            movable.setY(movable.getY() + movementDelta.y);

        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.error("Exception in ZigZagMovementBehavior.applyMovementBehavior: " + e.getMessage(), e);
            if (!lenientMode) {
                throw new MovementException("Error updating position in ZigZagMovementBehavior", e);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected exception in ZigZagMovementBehavior.applyMovementBehavior: " + e.getMessage(), e);
            if (!lenientMode) {
                throw new MovementException("Error updating position in ZigZagMovementBehavior", e);
            }
        }
    }
}
