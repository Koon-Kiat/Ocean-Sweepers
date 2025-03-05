package project.game.context.movement;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.entitysystem.entity.Entity;

/**
 * Advanced movement strategy that predicts where a moving target will be and
 * attempts to intercept it. Uses vector math to calculate interception points
 * and avoids obstacles along the way.
 */
public class ObstacleAvoidanceStrategy implements IMovementStrategy {

    private static final GameLogger LOGGER = new GameLogger(ObstacleAvoidanceStrategy.class);
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

    // Obstacle avoidance parameters - INCREASED FOR BETTER AVOIDANCE
    private List<Entity> obstacles = new ArrayList<>();
    private float avoidanceRadius = 300f; // Increased for better avoidance distance
    private float avoidanceWeight = 5.0f; // Significantly increased to prioritize obstacle avoidance
    private final int lookaheadRays = 5; // Number of rays to cast for obstacle detection
    private final float lookaheadDistance = 350f; // Increased from 300f
    private final float rayAngle = 30f; // Angle between rays in degrees

    /**
     * Constructor for obstacle avoidance with a movable target
     * 
     * @param target      The target to avoid or intercept
     * @param speed       The movement speed
     * @param lenientMode Whether to use lenient mode for error handling
     */
    public ObstacleAvoidanceStrategy(IMovable target, float speed, boolean lenientMode) {
        this.lenientMode = lenientMode;

        if (target == null) {
            // If target is null, we'll just do obstacle avoidance without tracking a target
            LOGGER.info("Target is null in ObstacleAvoidanceStrategy, will focus on obstacle avoidance only");
            this.target = null;
            this.lastTargetPos = new Vector2(0, 0);
            this.targetVelocity = new Vector2(0, 0);
        } else {
            this.target = target;
            // Initialize with position data, not velocity
            this.lastTargetPos = new Vector2(target.getX(), target.getY());
            this.targetVelocity = new Vector2(0, 0);
        }

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
    }

    /**
     * Set the list of obstacles to avoid
     */
    public void setObstacles(List<Entity> obstacles) {
        this.obstacles = obstacles != null ? obstacles : new ArrayList<>();
    }

    /**
     * Add an obstacle to the avoidance list
     */
    public void addObstacle(Entity obstacle) {
        if (obstacle != null && !obstacles.contains(obstacle)) {
            obstacles.add(obstacle);
        }
    }

    /**
     * Remove an obstacle from the avoidance list
     */
    public void removeObstacle(Entity obstacle) {
        obstacles.remove(obstacle);
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Log obstacle count occasionally for debugging
            if (Math.random() < 0.01) { // Only log 1% of the time to avoid spamming
                LOGGER.debug("ObstacleAvoidanceStrategy tracking {0} obstacles", obstacles.size());
            }

            Vector2 movementDir;

            // Target-based movement (if target exists)
            if (target != null) {
                // Get the target's current position
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

                // Detect if target is approaching
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
                if (isApproaching) {
                    // Target is moving toward us - move directly toward target
                    movementDir = directDir;
                    directionChangeSmoothing = 0.4f;
                } else {
                    // Target is not approaching - use interception prediction
                    movementDir = interceptDir.nor();
                    directionChangeSmoothing = 0.2f;
                }
            } else {
                // No target - use current direction or initialize a default direction
                if (persistentDirection.len2() < 0.001f) {
                    // Initialize with a default direction if not set
                    persistentDirection.set(1, 0);
                }
                movementDir = new Vector2(persistentDirection);
            }

            // Calculate obstacle avoidance forces
            Vector2 avoidanceForce = calculateObstacleAvoidance(movable, movementDir);

            // Check if we're seeing any avoidance forces at all
            if (avoidanceForce.len2() > 0.001f) {
                // If force was generated, log it for debugging
                LOGGER.debug("Generated avoidance force: ({0}, {1}) with magnitude {2}",
                        avoidanceForce.x, avoidanceForce.y, avoidanceForce.len());

                // If avoiding obstacles, blend with higher weight to obstacle avoidance
                // Use higher priority for obstacle avoidance vs. desired direction
                movementDir = new Vector2(movementDir).scl(1f).add(avoidanceForce.scl(avoidanceWeight)).nor();

                // Use higher smoothing for more responsive obstacle avoidance
                directionChangeSmoothing = 0.8f; // Increased for more responsive avoidance
            }

            // Gradually blend new direction with persistent direction for smoothness
            persistentDirection.lerp(movementDir, directionChangeSmoothing).nor();

            // If we have a target, force movement toward target if we're heading away and
            // not avoiding obstacles
            if (target != null) {
                Vector2 toTarget = new Vector2(target.getX(), target.getY())
                        .sub(movable.getX(), movable.getY()).nor();
                float directionDot = persistentDirection.dot(toTarget);

                // Only correct direction if we're not actively avoiding obstacles
                if (directionDot < 0.3f && avoidanceForce.len2() < 0.5f) {
                    persistentDirection.lerp(toTarget, 0.5f).nor();
                }
            }

            // Calculate actual movement vector
            Vector2 moveVec = new Vector2(persistentDirection).scl(speed * deltaTime);

            // Apply movement
            movable.setX(movable.getX() + moveVec.x);
            movable.setY(movable.getY() + moveVec.y);

            // Update velocity for animations
            movable.setVelocity(moveVec.x / deltaTime, moveVec.y / deltaTime);

        } catch (Exception e) {
            String errorMessage = "Error in ObstacleAvoidanceStrategy: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            if (!lenientMode) {
                throw new MovementException(errorMessage, e);
            }
        }
    }

    /**
     * Calculate obstacle avoidance steering forces
     * 
     * @return A normalized vector representing the avoidance direction
     */
    private Vector2 calculateObstacleAvoidance(IMovable movable, Vector2 desiredDirection) {
        if (obstacles == null || obstacles.isEmpty()) {
            return new Vector2(0, 0);
        }

        Vector2 avoidanceForce = new Vector2(0, 0);
        Vector2 movablePos = new Vector2(movable.getX(), movable.getY());
        Vector2 forwardDir = new Vector2(desiredDirection).nor();

        // Make the forward direction non-zero if it's zero
        if (forwardDir.len2() < 0.0001f) {
            forwardDir.set(1, 0); // Default direction if none provided
        }

        // Cast rays to detect obstacles
        boolean obstacleDetected = false;
        float nearestObstacleDistance = Float.MAX_VALUE;
        Entity nearestObstacle = null;

        // Check each obstacle for proximity and collision potential
        for (Entity obstacle : obstacles) {
            // Skip null or inactive obstacles
            if (obstacle == null || !obstacle.isActive()) {
                continue;
            }

            // Calculate vector to obstacle
            float obstacleX = obstacle.getX();
            float obstacleY = obstacle.getY();

            // Check if this is the same entity
            if (movable instanceof Entity) {
                Entity movableEntity = (Entity) movable;
                if (movableEntity == obstacle) {
                    continue; // Don't avoid yourself
                }
            }

            // Create a vector from movable to obstacle
            Vector2 toObstacle = new Vector2(obstacleX, obstacleY).sub(movablePos);
            float distanceToObstacle = toObstacle.len();

            // Account for obstacle size
            float obstacleRadius = Math.max(obstacle.getWidth(), obstacle.getHeight()) / 2;
            float effectiveDistance = distanceToObstacle - obstacleRadius;

            // Check if obstacle is within avoidance radius
            if (effectiveDistance < avoidanceRadius) {
                // Get normalized vector to obstacle
                Vector2 toObstacleNorm = new Vector2(toObstacle).nor();

                // Check if obstacle is in front or very close
                float dotProduct = forwardDir.dot(toObstacleNorm);

                // If obstacle is ahead of us or very close (within 70% of the radius)
                if (dotProduct > -0.5f || effectiveDistance < avoidanceRadius * 0.7f) {
                    obstacleDetected = true;

                    // More aggressive avoidance for very close obstacles
                    float priority = 1.0f;
                    if (effectiveDistance < avoidanceRadius * 0.3f) {
                        priority = 2.0f; // Higher priority for close obstacles
                    }

                    // Find the closest obstacle with priority consideration
                    float weightedDistance = effectiveDistance / priority;
                    if (weightedDistance < nearestObstacleDistance) {
                        nearestObstacleDistance = weightedDistance;
                        nearestObstacle = obstacle;
                    }
                }
            }
        }

        // If we detected an obstacle ahead, calculate avoidance force
        if (obstacleDetected && nearestObstacle != null) {
            // Get the nearest obstacle position
            float obstacleX = nearestObstacle.getX();
            float obstacleY = nearestObstacle.getY();
            float obstacleRadius = Math.max(nearestObstacle.getWidth(), nearestObstacle.getHeight()) / 2;

            // Vector from movable to obstacle center
            Vector2 toObstacle = new Vector2(obstacleX, obstacleY).sub(movablePos);
            float distanceToObstacle = toObstacle.len() - obstacleRadius;

            // Normalize the toObstacle vector
            Vector2 toObstacleNorm = new Vector2(toObstacle).nor();

            // Determine which way to steer by taking the perpendicular vector
            // We want to find the "best" perpendicular direction to avoid the obstacle
            Vector2 perpendicular = new Vector2(-forwardDir.y, forwardDir.x);

            // Check if the perpendicular is pointing toward or away from the obstacle
            // by calculating the dot product
            if (perpendicular.dot(toObstacleNorm) > 0) {
                // If the perpendicular is pointing toward the obstacle,
                // we need the opposite perpendicular
                perpendicular.scl(-1);
            }

            // Obstacle avoidance force with strength inversely proportional to distance
            float avoidanceStrength = 1.0f - (distanceToObstacle / avoidanceRadius);
            avoidanceStrength = Math.max(0.1f, Math.min(1.0f, avoidanceStrength)); // Clamp between 0.1 and 1.0

            // Apply stronger avoidance for very close obstacles
            float scaleFactor = 3.0f;
            if (distanceToObstacle < avoidanceRadius * 0.3f) {
                scaleFactor = 5.0f; // Even stronger for very close obstacles
            }

            // Set the avoidance force
            avoidanceForce.set(perpendicular).scl(avoidanceStrength * scaleFactor);

            // Add a direct repulsion force away from the obstacle
            Vector2 directRepulsion = new Vector2(toObstacleNorm).scl(-avoidanceStrength * 2.0f);
            avoidanceForce.add(directRepulsion);

            // Log obstacle avoidance for debugging
            LOGGER.debug("Avoiding obstacle at ({0},{1}), distance: {2}, force: {3}, strength: {4}",
                    obstacleX, obstacleY, distanceToObstacle, avoidanceForce.len(), avoidanceStrength);
        }

        return avoidanceForce;
    }

    /**
     * Set the radius at which to start avoiding obstacles
     */
    public void setAvoidanceRadius(float radius) {
        if (radius > 0) {
            this.avoidanceRadius = radius;
        }
    }

    /**
     * Set how strongly to prioritize obstacle avoidance vs. target interception
     */
    public void setAvoidanceWeight(float weight) {
        if (weight > 0) {
            this.avoidanceWeight = weight;
        }
    }

    /**
     * Get the current list of obstacles being avoided
     * 
     * @return List of obstacle entities
     */
    public List<Entity> getObstacles() {
        return obstacles;
    }
}