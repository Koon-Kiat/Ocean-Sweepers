package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.engine.api.movement.IMovable;

/**
 * Advanced movement strategy that predicts where a moving target will be and
 * attempts to intercept it. Uses vector math to calculate interception points.
 */
public class InterceptorMovementStrategy extends AbstractMovementStrategy {

    private static final float PREDICTION_TIME = 0.5f;
    private static final float MIN_DISTANCE = 10f;
    private final IMovable target;
    private final float speed;
    private final Vector2 lastTargetPos;
    private final Vector2 targetVelocity;
    private final Vector2 persistentDirection = new Vector2(1, 0);
    private float directionChangeSmoothing = 0.2f;
    private boolean isApproaching = false;

    public InterceptorMovementStrategy(IMovable target, float speed, boolean lenientMode) {
        super(InterceptorMovementStrategy.class, lenientMode);

        // Validate target
        validateTarget(target, "Target");
        this.target = target;

        // Validate speed
        this.speed = validateSpeed(speed, 200f);

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

            // Detect if target is approaching (moving toward the interceptor)
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
            applyMovement(movable, moveVec);

            // Update velocity for animations
            updateVelocity(movable, moveVec, deltaTime);

        } catch (Exception e) {
            handleMovementException(e, "Error in InterceptorMovementStrategy: " + e.getMessage());
        }
    }
}