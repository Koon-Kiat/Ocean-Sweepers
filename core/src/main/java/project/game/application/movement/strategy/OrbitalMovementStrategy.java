package project.game.application.movement.strategy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.application.movement.api.StrategyType;
import project.game.common.exception.MovementException;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IPositionable;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;

/**
 * Makes an entity orbit around a target entity.
 * 
 * The orbit can be circular or elliptical, and the rotation speed is
 * configurable.
 */
public class OrbitalMovementStrategy extends AbstractMovementStrategy {

    private final IPositionable target;
    private final float orbitRadius;
    private final float rotationSpeed;
    private final float eccentricity;
    private float currentAngle = 0;
    private Vector2 lastValidPosition = null;
    private static final float MIN_SAFE_DISTANCE = 20f;

    public OrbitalMovementStrategy(IPositionable target, float orbitRadius, float rotationSpeed, float eccentricity,
            boolean lenientMode) {
        super(OrbitalMovementStrategy.class, lenientMode);

        // Validate target
        validateTarget(target, "Target");
        this.target = target;

        // Validate orbit radius
        if (orbitRadius <= MIN_SAFE_DISTANCE) {
            String errorMessage = "Orbit radius must be greater than " + MIN_SAFE_DISTANCE + ". Got: " + orbitRadius;
            if (lenientMode) {
                logger.warn(errorMessage + " Using minimum safe radius of " + (MIN_SAFE_DISTANCE * 2) + ".");
                this.orbitRadius = MIN_SAFE_DISTANCE * 2;
            } else {
                logger.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.orbitRadius = orbitRadius;
        }

        // Verify rotation speed is reasonable
        float safeRotationSpeed = rotationSpeed;
        if (Math.abs(rotationSpeed) > 10f) {
            String errorMessage = "Rotation speed is unusually high: " + rotationSpeed + " rad/s";
            if (lenientMode) {
                logger.warn(errorMessage + " Clamping to 3.0 rad/s");
                safeRotationSpeed = Math.signum(rotationSpeed) * Math.min(Math.abs(rotationSpeed), 3.0f);
            } else {
                logger.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        this.rotationSpeed = safeRotationSpeed;

        this.eccentricity = MathUtils.clamp(eccentricity, 0, 0.8f);
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.ORBITAL;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Make sure we have a valid last position reference
            if (lastValidPosition == null) {
                lastValidPosition = new Vector2(movable.getX(), movable.getY());
            }

            // Guard against extremely large delta time values
            float safeDeltaTime = Math.min(deltaTime, 0.1f);

            // Update the current angle - smooth the rotation step
            currentAngle += rotationSpeed * safeDeltaTime;

            // Keep angle between 0 and 2π
            currentAngle = currentAngle % (2 * MathUtils.PI);

            // Calculate the current radius based on the elliptical orbit formula
            // with guard against division by zero or negative values
            float denominator = 1 + eccentricity * MathUtils.cos(currentAngle);
            if (denominator < 0.1f)
                denominator = 0.1f; // Safety check

            float currentRadius = orbitRadius * (1 - eccentricity * eccentricity) / denominator;

            // Ensure minimum safe distance
            currentRadius = Math.max(currentRadius, MIN_SAFE_DISTANCE);

            // Calculate the orbital position relative to the target
            float orbitX = currentRadius * MathUtils.cos(currentAngle);
            float orbitY = currentRadius * MathUtils.sin(currentAngle);

            // Calculate new absolute position
            float newX = target.getX() + orbitX;
            float newY = target.getY() + orbitY;

            // Check for reasonable movement (prevent teleportation)
            Vector2 newPos = new Vector2(newX, newY);
            if (lastValidPosition.dst(newPos) > orbitRadius * 0.5f) {
                // Position change is too large - interpolate to create a smoother transition
                newPos.set(lastValidPosition).lerp(newPos, 0.1f);
                logger.warn("Detected large movement in OrbitalMovementStrategy, smoothing transition");
            }

            // Set position and update last valid position
            movable.setX(newPos.x);
            movable.setY(newPos.y);
            lastValidPosition.set(newPos);

            // Calculate velocity for proper facing direction
            // Use a small step ahead to calculate direction vector
            float nextAngle = currentAngle + rotationSpeed * 0.01f;
            float nextX = currentRadius * MathUtils.cos(nextAngle);
            float nextY = currentRadius * MathUtils.sin(nextAngle);

            Vector2 velocityDirection = new Vector2(nextX - orbitX, nextY - orbitY).nor();
            
            // Scale by rotation speed and radius to get appropriate magnitude
            float velocityMagnitude = Math.abs(rotationSpeed) * currentRadius;
            Vector2 velocity = velocityDirection.scl(velocityMagnitude);

            movable.setVelocity(velocity);

        } catch (Exception e) {
            handleMovementException(e, "Error in OrbitalMovementStrategy: " + e.getMessage());
        }
    }
}