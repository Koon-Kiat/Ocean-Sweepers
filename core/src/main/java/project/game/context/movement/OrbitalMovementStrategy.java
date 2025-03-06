package project.game.context.movement;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.api.movement.IPositionable;

/**
 * Makes an entity orbit around a target entity.
 * The orbit can be circular or elliptical, and the rotation speed is
 * configurable.
 */
public class OrbitalMovementStrategy implements IMovementStrategy {
    
    private static final GameLogger LOGGER = new GameLogger(OrbitalMovementStrategy.class);
    private final IPositionable target;
    private final float orbitRadius;
    private final float rotationSpeed; // Radians per second
    private final boolean lenientMode;
    private float currentAngle = 0;
    private final float eccentricity; // 0 = circle, >0 = ellipse

    public OrbitalMovementStrategy(IPositionable target, float orbitRadius, float rotationSpeed, float eccentricity,
            boolean lenientMode) {
        this.lenientMode = lenientMode;

        if (target == null) {
            String errorMessage = "Target cannot be null in OrbitalMovementBehavior.";
            LOGGER.error(errorMessage);
            throw new MovementException(errorMessage);
        }
        this.target = target;

        if (orbitRadius <= 0) {
            String errorMessage = "Orbit radius must be positive. Got: " + orbitRadius;
            if (lenientMode) {
                LOGGER.warn(errorMessage + " Using default radius of 100.");
                this.orbitRadius = 100;
            } else {
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.orbitRadius = orbitRadius;
        }

        // Negative rotation speed is allowed (counterclockwise rotation)
        this.rotationSpeed = rotationSpeed;

        // Clamp eccentricity between 0 and 0.99 (1 would make it a parabola)
        this.eccentricity = MathUtils.clamp(eccentricity, 0, 0.99f);
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Update the current angle
            currentAngle += rotationSpeed * deltaTime;

            // Keep angle between 0 and 2π
            currentAngle = currentAngle % (2 * MathUtils.PI);

            // Calculate the current radius based on the elliptical orbit formula
            float currentRadius = orbitRadius * (1 - eccentricity * eccentricity) /
                    (1 + eccentricity * MathUtils.cos(currentAngle));

            // Calculate the orbital position relative to the target
            float orbitX = currentRadius * MathUtils.cos(currentAngle);
            float orbitY = currentRadius * MathUtils.sin(currentAngle);

            // Set the entity's position relative to the target
            movable.setX(target.getX() + orbitX);
            movable.setY(target.getY() + orbitY);

            // Calculate and set velocity for proper facing direction
            float nextAngle = currentAngle + rotationSpeed * deltaTime;
            float nextX = currentRadius * MathUtils.cos(nextAngle);
            float nextY = currentRadius * MathUtils.sin(nextAngle);

            Vector2 velocity = new Vector2(nextX - orbitX, nextY - orbitY);
            velocity.nor().scl(Math.abs(rotationSpeed) * currentRadius);
            movable.setVelocity(velocity);

        } catch (Exception e) {
            String errorMessage = "Error in OrbitalMovementBehavior: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            if (!lenientMode) {
                throw new MovementException(errorMessage, e);
            }
        }
    }
}