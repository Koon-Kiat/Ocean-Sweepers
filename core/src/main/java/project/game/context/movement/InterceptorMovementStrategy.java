package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;

/**
 * Advanced movement behavior that predicts where a moving target will be and
 * attempts to intercept it. Uses vector math to calculate interception points.
 */
public class InterceptorMovementStrategy implements IMovementStrategy {

    private static final GameLogger LOGGER = new GameLogger(InterceptorMovementStrategy.class);
    private final IMovable target;
    private final float speed;
    private final boolean lenientMode;
    private final Vector2 lastTargetPos;
    private final Vector2 targetVelocity;
    private static final float PREDICTION_TIME = 0.5f;
    private static final float MIN_DISTANCE = 10f;
    private final Vector2 persistentDirection = new Vector2(1, 0);
    private float directionChangeSmoothing = 0.2f;
    private boolean isApproaching = false;

    public InterceptorMovementStrategy(IMovable target, float speed, boolean lenientMode) {
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

        // Correctly initialize with position data, not velocity
        this.lastTargetPos = new Vector2(target.getX(), target.getY());
        this.targetVelocity = new Vector2(0, 0);
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Get the target's current position (not velocity)
            Vector2 currentTargetPos = new Vector2(target.getX(), target.getY());

            // Calculate target's velocity based on position change
            Vector2 targetDelta = new Vector2(currentTargetPos).sub(lastTargetPos);
            targetVelocity.set(targetDelta.scl(1f / deltaTime));

            // Update last known position
            lastTargetPos.set(currentTargetPos);

            // Calculate vector from movable object to target
            Vector2 toTarget = new Vector2(currentTargetPos).sub(movable.getX(), movable.getY());
            float distance = toTarget.len();

            // If we're very close to target, maintain minimum distance
            if (distance < MIN_DISTANCE) {
                Vector2 avoidance = new Vector2(toTarget).nor().scl(-MIN_DISTANCE);
                movable.setX(currentTargetPos.x + avoidance.x);
                movable.setY(currentTargetPos.y + avoidance.y);
                return;
            }

            // Direct vector to target (without prediction)
            Vector2 directDir = new Vector2(toTarget).nor();

            // Calculate predicted target position based on target's velocity
            Vector2 predictedPos = new Vector2(currentTargetPos).add(
                    new Vector2(targetVelocity).scl(PREDICTION_TIME));

            // Calculate direction to predicted position
            Vector2 interceptDir = new Vector2(predictedPos).sub(movable.getX(), movable.getY());

            // IMPROVED LOGIC: Detect if target is approaching (moving toward the
            // interceptor)
            Vector2 normalizedToTarget = new Vector2(toTarget).nor();
            Vector2 normalizedTargetVelocity = new Vector2(targetVelocity);

            // Only normalize if not close to zero (avoid NaN)
            if (normalizedTargetVelocity.len2() > 0.001f) {
                normalizedTargetVelocity.nor();
            } else {
                normalizedTargetVelocity.set(0, 0);
            }

            float dotProduct = normalizedToTarget.dot(normalizedTargetVelocity);

            // Detect if target is approaching
            isApproaching = dotProduct < -0.1f;

            // Movement strategy depends on whether target is approaching
            Vector2 movementDir;

            if (isApproaching) {
                // Target is moving toward us - move directly toward target
                // This ensures we don't back away when the target approaches
                movementDir = directDir;
                // Use less smoothing when target approaches for quicker response
                directionChangeSmoothing = 0.4f;
            } else {
                // Target is not approaching - use interception prediction
                movementDir = interceptDir.nor();
                directionChangeSmoothing = 0.2f;
            }

            // Gradually blend new direction with persistent direction for smoothness
            // But guarantee forward movement by ensuring dot product with toTarget is
            // positive
            persistentDirection.lerp(movementDir, directionChangeSmoothing).nor();

            // Force movement toward target if we're heading away
            float directionDot = persistentDirection.dot(normalizedToTarget);
            if (directionDot < 0.3f) {
                // Blend more toward direct direction if we're not heading toward target
                persistentDirection.lerp(directDir, 0.5f).nor();
            }

            // Calculate actual movement vector
            Vector2 moveVec = new Vector2(persistentDirection).scl(speed * deltaTime);

            // Apply movement
            movable.setX(movable.getX() + moveVec.x);
            movable.setY(movable.getY() + moveVec.y);

            // Update velocity for animations
            movable.setVelocity(moveVec.x / deltaTime, moveVec.y / deltaTime);

        } catch (Exception e) {
            String errorMessage = "Error in InterceptorMovementBehavior: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            if (!lenientMode) {
                throw new MovementException(errorMessage, e);
            }
        }
    }
}