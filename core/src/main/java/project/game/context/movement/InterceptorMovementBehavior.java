package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * Advanced movement behavior that predicts where a moving target will be and
 * attempts to intercept it. Uses vector math to calculate interception points.
 */
public class InterceptorMovementBehavior implements IMovementBehavior {
    private static final GameLogger LOGGER = new GameLogger(InterceptorMovementBehavior.class);

    private final MovableEntity target; // Need MovableEntity to access velocity
    private final float speed;
    private final boolean lenientMode;
    private final Vector2 lastTargetPos;
    private final Vector2 targetVelocity;
    private static final float PREDICTION_TIME = 0.5f; // How far ahead to predict
    private static final float MIN_DISTANCE = 10f; // Minimum distance to target

    public InterceptorMovementBehavior(MovableEntity target, float speed, boolean lenientMode) {
        this.lenientMode = lenientMode;

        if (target == null) {
            String errorMessage = "Target cannot be null in InterceptorMovementBehavior.";
            LOGGER.error(errorMessage);
            throw new MovementException(errorMessage);
        }
        this.target = target;

        if (speed <= 0) {
            String errorMessage = "Speed must be positive. Got: " + speed;
            if (lenientMode) {
                LOGGER.warn(errorMessage + " Using default value of 200.");
                this.speed = 200f;
            } else {
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.speed = speed;
        }

        this.lastTargetPos = new Vector2(target.getX(), target.getY());
        this.targetVelocity = new Vector2(0, 0);
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            // Update target velocity estimate
            Vector2 currentTargetPos = new Vector2(target.getX(), target.getY());
            Vector2 targetDelta = new Vector2(currentTargetPos).sub(lastTargetPos);
            targetVelocity.set(targetDelta.scl(1f / deltaTime));
            lastTargetPos.set(currentTargetPos);

            // Calculate predicted target position
            Vector2 predictedPos = new Vector2(currentTargetPos).add(
                    new Vector2(targetVelocity).scl(PREDICTION_TIME));

            // Calculate direction to predicted position
            Vector2 interceptDir = new Vector2(predictedPos).sub(
                    new Vector2(entity.getX(), entity.getY()));

            float distance = interceptDir.len();

            // If we're very close to target, maintain minimum distance
            if (distance < MIN_DISTANCE) {
                Vector2 avoidance = new Vector2(interceptDir).nor().scl(-MIN_DISTANCE);
                entity.setX(currentTargetPos.x + avoidance.x);
                entity.setY(currentTargetPos.y + avoidance.y);
                return;
            }

            // Calculate movement
            interceptDir.nor().scl(speed * deltaTime);

            // Apply movement
            entity.setX(entity.getX() + interceptDir.x);
            entity.setY(entity.getY() + interceptDir.y);

            // Update velocity for animations
            entity.setVelocity(interceptDir.x / deltaTime, interceptDir.y / deltaTime);

        } catch (Exception e) {
            String errorMessage = "Error in InterceptorMovementBehavior: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            if (!lenientMode) {
                throw new MovementException(errorMessage, e);
            }
        }
    }
}