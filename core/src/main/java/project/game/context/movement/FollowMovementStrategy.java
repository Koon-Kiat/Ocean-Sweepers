package project.game.context.movement;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.api.movement.IPositionable;

/**
 * Provides follow movement for movable entities with smooth path following.
 * 
 * The entity moves towards the target entity along a smooth curved path.
 * The path is recalculated when the target moves significantly.
 */
public class FollowMovementStrategy implements IMovementStrategy {

    private static final GameLogger LOGGER = new GameLogger(FollowMovementStrategy.class);
    private final IPositionable target;
    private final float speed;
    private final boolean lenientMode;

    // Path following parameters
    private final List<Vector2> pathPoints;
    private final Vector2 lastTargetPosition;
    private float pathProgress = 0f;
    private static final float PATH_RECALC_THRESHOLD = 100f; // Distance the target must move to recalculate path
    private static final float ARRIVAL_THRESHOLD = 10f; // Distance at which to consider "arrived" at target
    private static final int PATH_SEGMENTS = 20; // Number of segments in the smooth path
    private static final float MIN_CONTROL_POINT_DISTANCE = 80f; // Minimum distance for control points
    private static final float MAX_CONTROL_POINT_DISTANCE = 200f; // Maximum distance for control points
    private static final float PATH_PROGRESS_SPEED = 1.0f; // Speed of movement along the path (1.0 = 100% per second)

    /**
     * Constructs a FollowMovementBehavior with the specified parameters.
     * Terminates the program if any provided parameter is negative or null.
     */
    public FollowMovementStrategy(IPositionable target, float speed, boolean lenientMode) {
        this.lenientMode = lenientMode;
        if (target == null) {
            String errorMessage = "Target cannot be null in FollowMovementBehavior.";
            LOGGER.error(errorMessage);
            throw new MovementException(errorMessage);
        } else {
            this.target = target;
        }
        if (speed < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative speed provided in FollowMovementBehavior: {0}. Using absolute value.", speed);
                this.speed = Math.abs(speed);
            } else {
                String errorMessage = "Negative speed provided in FollowMovementBehavior: " + speed;
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.speed = speed;
        }

        // Initialize path data
        this.pathPoints = new ArrayList<>();
        this.lastTargetPosition = new Vector2(target.getX(), target.getY());
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
                movable.setX(movable.getX() + direction.x * speed * deltaTime);
                movable.setY(movable.getY() + direction.y * speed * deltaTime);

                // Make sure to always update the velocity for animations
                movable.setVelocity(direction.x * speed, direction.y * speed);
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
                // Fixed: Increased speed multiplier to ensure entity doesn't stop
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
                        movable.setX(movable.getX() + direction.x * moveSpeed);
                        movable.setY(movable.getY() + direction.y * moveSpeed);

                        // Update entity's velocity direction (for animations)
                        movable.setVelocity(direction.x * speed, direction.y * speed);
                    }
                }
            } else {
                // Fallback direct movement if path calculation failed
                directMovement(movable, targetPosition, deltaTime);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal argument in FollowMovementBehavior: " + e.getMessage(), e);
            if (!lenientMode) {
                throw new MovementException("Invalid argument in FollowMovementBehavior", e);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error in FollowMovementBehavior: " + e.getMessage(), e);
            if (!lenientMode) {
                throw new MovementException("Error updating position in FollowMovementBehavior", e);
            }
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
            float randomOffset = MathUtils.random(-0.3f, 0.3f); // Randomize the curve a bit
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
            LOGGER.error("Error calculating path in FollowMovementBehavior: " + e.getMessage(), e);
            pathPoints.clear(); // Clear path to fall back to direct movement
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
        direction.nor().scl(speed * deltaTime);

        // Apply movement
        movable.setX(movable.getX() + direction.x);
        movable.setY(movable.getY() + direction.y);

        // Update entity's velocity for animations
        movable.setVelocity(direction.x / deltaTime, direction.y / deltaTime);
    }
}
