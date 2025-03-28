package project.game.application.movement.strategy;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.application.movement.api.StrategyType;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IPositionable;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;

/**
 * Provides follow movement for movable entities with smooth path following.
 * 
 * The entity moves towards the target entity along a smooth curved path.
 * The path is recalculated when the target moves significantly.
 */
public class FollowMovementStrategy extends AbstractMovementStrategy {

    private static final float PATH_RECALC_THRESHOLD = 100f;
    private static final float ARRIVAL_THRESHOLD = 10f;
    private static final int PATH_SEGMENTS = 20;
    private static final float MIN_CONTROL_POINT_DISTANCE = 80f;
    private static final float MAX_CONTROL_POINT_DISTANCE = 200f;
    private static final float PATH_PROGRESS_SPEED = 1.0f;
    private final IPositionable target;
    private final float speed;
    private final List<Vector2> pathPoints;
    private final Vector2 lastTargetPosition;
    private float pathProgress = 0f;

    /**
     * Constructs a FollowMovementStrategy with the specified parameters.
     */
    public FollowMovementStrategy(IPositionable target, float speed, boolean lenientMode) {
        super(FollowMovementStrategy.class, lenientMode);

        // Validate target
        validateTarget(target, "Target");
        this.target = target;

        // Validate speed
        this.speed = validateNonNegative(speed, "Speed", 200f);

        // Initialize path data
        this.pathPoints = new ArrayList<>();
        this.lastTargetPosition = new Vector2(target.getX(), target.getY());
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.FOLLOW;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            Vector2 currentPosition = new Vector2(movable.getX(), movable.getY());
            Vector2 targetPosition = new Vector2(target.getX(), target.getY());

            // Check if target has moved enough to recalculate path
            boolean shouldRecalculatePath = pathPoints.isEmpty() ||
                    new Vector2(lastTargetPosition).sub(targetPosition).len2() > PATH_RECALC_THRESHOLD
                            * PATH_RECALC_THRESHOLD;

            // Calculate distance to target
            float distanceToTarget = currentPosition.dst(targetPosition);

            // If close enough to target, just move directly to it
            if (distanceToTarget < ARRIVAL_THRESHOLD) {
                // Instead of slowing down, maintain speed when close to target
                // This ensures continuous movement when reaching the target
                Vector2 direction = new Vector2(targetPosition).sub(currentPosition).nor();
                Vector2 moveVec = new Vector2(direction).scl(speed * deltaTime);

                applyMovement(movable, moveVec);
                updateVelocity(movable, moveVec, deltaTime);
                return;
            }

            // If we need a new path, calculate it
            if (shouldRecalculatePath) {
                calculateSmoothPath(currentPosition, targetPosition);
                // Reset progress to 0 to start a new path
                pathProgress = 0f;
                lastTargetPosition.set(targetPosition);
            }

            // Follow the path
            if (!pathPoints.isEmpty()) {
                // Advance along the path based on speed and delta time
                pathProgress += PATH_PROGRESS_SPEED * deltaTime * (speed / 50f);

                // Clamp progress to [0,1]
                pathProgress = MathUtils.clamp(pathProgress, 0f, 1f);

                // If we've reached the end of the path, force recalculation on next update
                if (pathProgress >= 0.95f) {
                    lastTargetPosition.set(-999999, -999999); // Force recalculation
                }

                // Interpolate along the path
                int index = (int) (pathProgress * (pathPoints.size() - 1));
                if (index < pathPoints.size() - 1) {
                    float segmentProgress = (pathProgress * (pathPoints.size() - 1)) - index;
                    Vector2 currentPoint = pathPoints.get(index);
                    Vector2 nextPoint = pathPoints.get(index + 1);

                    // Interpolate between path points
                    float moveX = currentPoint.x + (nextPoint.x - currentPoint.x) * segmentProgress;
                    float moveY = currentPoint.y + (nextPoint.y - currentPoint.y) * segmentProgress;

                    // Calculate direction and move
                    Vector2 direction = new Vector2(moveX - movable.getX(), moveY - movable.getY());
                    float length = direction.len();
                    if (length > 0.0001f) {
                        direction.nor();
                        // Fixed: Always use full speed rather than slowing down when close
                        float moveSpeed = speed * deltaTime;
                        Vector2 moveVec = new Vector2(direction).scl(moveSpeed);

                        applyMovement(movable, moveVec);
                        updateVelocity(movable, direction.scl(speed), 1.0f);
                    }
                }
            } else {
                // Fallback direct movement if path calculation failed
                directMovement(movable, targetPosition, deltaTime);
            }
        } catch (Exception e) {
            handleMovementException(e, "Error in FollowMovementStrategy: " + e.getMessage());
        }
    }

    /**
     * Calculates a smooth path from current position to target using Bezier curves
     */
    private void calculateSmoothPath(Vector2 start, Vector2 end) {
        try {
            pathPoints.clear();

            // Create control points for the Bezier curve
            Vector2 direction = new Vector2(end).sub(start);
            float distance = direction.len();
            direction.nor();

            // Generate perpendicular vector for control points
            Vector2 perpendicular = new Vector2(-direction.y, direction.x);

            // Scale control point distance based on total distance, but within bounds
            float controlPointDistance = MathUtils.clamp(
                    distance * 0.5f,
                    MIN_CONTROL_POINT_DISTANCE,
                    MAX_CONTROL_POINT_DISTANCE);

            // Create a slight curve by offsetting control points
            float randomOffset = MathUtils.random(-0.3f, 0.3f);
            Vector2 controlPoint1 = new Vector2(start)
                    .add(new Vector2(direction).scl(distance * 0.3f))
                    .add(new Vector2(perpendicular).scl(controlPointDistance * randomOffset));

            Vector2 controlPoint2 = new Vector2(end)
                    .sub(new Vector2(direction).scl(distance * 0.3f))
                    .add(new Vector2(perpendicular).scl(controlPointDistance * randomOffset));

            // Create the Bezier curve
            Vector2[] bezierPoints = new Vector2[] {
                    new Vector2(start),
                    controlPoint1,
                    controlPoint2,
                    new Vector2(end)
            };

            // Sample points along the curve
            Bezier<Vector2> bezier = new Bezier<>(bezierPoints);
            for (int i = 0; i <= PATH_SEGMENTS; i++) {
                float t = i / (float) PATH_SEGMENTS;
                Vector2 point = new Vector2();
                bezier.valueAt(point, t);
                pathPoints.add(point);
            }
        } catch (Exception e) {
            logger.error("Error calculating path in FollowMovementStrategy: " + e.getMessage(), e);
            pathPoints.clear();
        }
    }

    /**
     * Direct movement toward target (fallback if path calculation fails)
     */
    private void directMovement(IMovable movable, Vector2 targetPos, float deltaTime) {
        Vector2 currentPos = new Vector2(movable.getX(), movable.getY());
        Vector2 direction = new Vector2(targetPos).sub(currentPos);

        // If we're already very close to the target, don't move
        if (direction.len2() < 0.0001f) {
            return;
        }

        // Normalize and scale by speed and deltaTime
        Vector2 moveVec = direction.nor().scl(speed * deltaTime);

        // Apply movement
        applyMovement(movable, moveVec);
        updateVelocity(movable, moveVec, deltaTime);
    }
}
