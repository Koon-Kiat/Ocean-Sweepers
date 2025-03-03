package project.game.context.movement;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * Makes an entity approach its target in a spiral pattern.
 * The spiral tightens as the entity gets closer to the target.
 */
public class SpiralApproachBehavior implements IMovementBehavior {
    private static final GameLogger LOGGER = new GameLogger(SpiralApproachBehavior.class);

    private final IPositionable target;
    private final float speed;
    private final float spiralTightness; // Controls how tight the spiral is
    private final float approachSpeed; // Controls how quickly entity moves toward target
    private final boolean lenientMode;
    private float currentAngle = 0;

    public SpiralApproachBehavior(IPositionable target, float speed, float spiralTightness, float approachSpeed,
            boolean lenientMode) {
        this.lenientMode = lenientMode;

        if (target == null) {
            String errorMessage = "Target cannot be null in SpiralApproachBehavior.";
            LOGGER.error(errorMessage);
            throw new MovementException(errorMessage);
        }
        this.target = target;

        if (speed <= 0) {
            String errorMessage = "Speed must be positive. Got: " + speed;
            if (lenientMode) {
                LOGGER.warn(errorMessage + " Using default speed of 200.");
                this.speed = 200f;
            } else {
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.speed = speed;
        }

        this.spiralTightness = Math.max(0.1f, spiralTightness);
        this.approachSpeed = Math.max(0.1f, approachSpeed);
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            // Calculate vector to target
            Vector2 toTarget = new Vector2(target.getX() - entity.getX(), target.getY() - entity.getY());
            float distanceToTarget = toTarget.len();

            // Update angle based on distance (spiral gets tighter as we get closer)
            float angleSpeed = speed / (distanceToTarget * spiralTightness);
            currentAngle += angleSpeed * deltaTime;

            // Keep angle between 0 and 2π
            currentAngle = currentAngle % (2 * MathUtils.PI);

            // Calculate spiral offset
            float spiralRadius = distanceToTarget * 0.5f;
            float offsetX = spiralRadius * MathUtils.cos(currentAngle);
            float offsetY = spiralRadius * MathUtils.sin(currentAngle);

            // Calculate approach vector
            Vector2 approachVector = new Vector2(toTarget).nor().scl(approachSpeed * deltaTime);

            // Combine spiral and approach movements
            Vector2 newPosition = new Vector2(entity.getX(), entity.getY());
            newPosition.add(approachVector);
            newPosition.add(offsetX - entity.getX(), offsetY - entity.getY());

            // Calculate velocity for proper facing
            Vector2 velocity = new Vector2(newPosition).sub(entity.getX(), entity.getY());
            entity.setVelocity(velocity.scl(1f / deltaTime));

            // Update position
            entity.setX(newPosition.x);
            entity.setY(newPosition.y);

        } catch (Exception e) {
            String errorMessage = "Error in SpiralApproachBehavior: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            if (!lenientMode) {
                throw new MovementException(errorMessage, e);
            }
        }
    }
}