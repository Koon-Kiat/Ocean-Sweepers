package project.game.application.movement.strategy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.application.movement.api.StrategyType;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;

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

    // Enhanced parameters for realistic water movement
    private final float driftFactor = 1.5f;
    private float wavePhase = 0f;
    private float waveAmplitude = 0f;

    public ZigZagMovemenStrategy(float speed, float amplitude, float frequency, boolean lenientMode) {
        super(ZigZagMovemenStrategy.class, lenientMode);
        this.speed = validateSpeed(speed, 200f);
        this.amplitude = amplitude;
        this.frequency = validateNonNegative(frequency, "Frequency", 1.0f);
        this.elapsedTime = 0f;
        this.wavePhase = MathUtils.random(0f, MathUtils.PI2);
        this.waveAmplitude = amplitude * 0.3f;
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.ZIGZAG;
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

            // Forward movement with drift factor applied
            Vector2 movementDelta = new Vector2(primaryDirection).scl(speed * deltaTime * driftFactor);

            // Primary zigzag oscillation
            float primaryOscillation = amplitude * MathUtils.sin(frequency * elapsedTime);

            // Secondary zigzag oscillation with different phase for more natural motion
            float secondaryOscillation = waveAmplitude * MathUtils.sin(frequency * 1.7f * elapsedTime + wavePhase);

            // Combined oscillation
            float totalOscillation = (primaryOscillation + secondaryOscillation) * deltaTime;

            // Add zigzag oscillation to movement
            movementDelta.add(new Vector2(perpVector).scl(totalOscillation));

            // Apply movement
            applyMovement(movable, movementDelta);

            // Update velocity for animations
            updateVelocity(movable, movementDelta, deltaTime);

        } catch (Exception e) {
            handleMovementException(e, "Error in ZigZagMovementStrategy: " + e.getMessage());
        }
    }
}
