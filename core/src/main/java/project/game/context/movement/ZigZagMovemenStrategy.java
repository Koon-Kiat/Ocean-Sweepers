package project.game.context.movement;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.engine.api.movement.IMovable;
import project.game.engine.entitysystem.movement.AbstractMovementStrategy;

/**
 * Provides zig-zag movement for movable entities.
 * 
 * The entity moves in a zig-zag pattern at a constant speed. The amplitude and
 * frequency of the oscillation are provided in the constructor. The entity
 * moves in the primary direction and oscillates in the perpendicular direction.
 */
public class ZigZagMovemenStrategy extends AbstractMovementStrategy {

    private final float speed;
    private final float amplitude;
    private final float frequency;
    private float elapsedTime;

    public ZigZagMovemenStrategy(float speed, float amplitude, float frequency, boolean lenientMode) {
        super(ZigZagMovemenStrategy.class, lenientMode);

        // Validate speed
        this.speed = validateSpeed(speed, 200f);

        // amplitude can be negative, it just affects phase
        this.amplitude = amplitude;

        // Validate frequency
        this.frequency = validateNonNegative(frequency, "Frequency", 1.0f);

        this.elapsedTime = 0f;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            elapsedTime += deltaTime;

            // Get current velocity to determine primary direction
            Vector2 velocity = getSafeVelocity(movable);

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
            applyMovement(movable, movementDelta);

            // Update velocity for animations
            updateVelocity(movable, movementDelta, deltaTime);

        } catch (Exception e) {
            handleMovementException(e, "Error in ZigZagMovementStrategy: " + e.getMessage());
        }
    }
}
