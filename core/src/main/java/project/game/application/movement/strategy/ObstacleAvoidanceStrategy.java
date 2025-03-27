package project.game.application.movement.strategy;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import project.game.application.movement.api.StrategyType;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;

/**
 * A pure obstacle avoidance strategy that focuses only on avoiding obstacles.
 * This is designed to be combined with other strategies using the decorator
 * pattern.
 */
public class ObstacleAvoidanceStrategy extends AbstractMovementStrategy {

    private final float speed;
    private final Vector2 persistentDirection = new Vector2(1, 0);

    // Enhanced obstacle avoidance parameters
    private final float criticalRadius = 80f;
    private final float lookaheadDistance = 350f;
    private final Vector2 lastSafePosition = new Vector2();
    private final float maxAvoidanceTime = 2.0f;
    private final float directionWeight = 1.5f;
    private final Vector2 lastAvoidanceForce = new Vector2();
    private final float steeringStrength = 3.0f;
    private List<Entity> obstacles = new ArrayList<>();
    private float currentRotationFactor = 1.0f;
    private float directionChangeSmoothing = 0.2f;
    private float avoidanceRadius = 300f;
    private boolean isAvoiding = false;
    private float avoidanceTimer = 0;
    private float avoidanceWeight = 5.0f;

    /**
     * Constructor for pure obstacle avoidance
     * 
     * @param speed       The movement speed
     * @param lenientMode Whether to use lenient mode for error handling
     */
    public ObstacleAvoidanceStrategy(float speed, boolean lenientMode) {
        super(ObstacleAvoidanceStrategy.class, lenientMode);
        this.speed = validateSpeed(speed, 200f);
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

    /**
     * Set the radius at which to start avoiding obstacles
     */
    public void setAvoidanceRadius(float radius) {
        if (radius > 0) {
            this.avoidanceRadius = radius;
        }
    }

    /**
     * Set how strongly to prioritize obstacle avoidance
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

    /**
     * Gets the strategy type for this movement strategy.
     */
    @Override
    public StrategyType getStrategyType() {
        return StrategyType.OBSTACLE_AVOIDANCE;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Start with current direction or use a default
            Vector2 movementDir;
            if (persistentDirection.len2() < 0.001f) {
                persistentDirection.set(1, 0);
            }
            movementDir = new Vector2(persistentDirection);

            // Store current position
            Vector2 currentPos = new Vector2(movable.getX(), movable.getY());

            // Get current velocity for direction-aware obstacle detection
            Vector2 currentVelocity = getSafeVelocity(movable);
            if (currentVelocity.len2() < 0.001f) {
                currentVelocity.set(movementDir);
            }

            // Calculate obstacle avoidance forces with direction awareness
            ObstacleAvoidanceResult avoidanceResult = calculateObstacleAvoidance(movable, currentVelocity);
            Vector2 avoidanceForce = avoidanceResult.force;

            float currentSpeed = speed;

            // Reset avoidance state if no obstacles are nearby
            if (!hasNearbyObstacles(currentPos, currentVelocity)) {
                isAvoiding = false;
                avoidanceTimer = 0;
                lastAvoidanceForce.setZero();
                directionChangeSmoothing = 0.2f; // Reset to normal smoothing
            }

            // Check if we need to avoid obstacles
            if (avoidanceResult.shouldAvoid) {
                if (!isAvoiding) {
                    lastSafePosition.set(currentPos);
                    isAvoiding = true;
                    avoidanceTimer = 0;
                }

                avoidanceTimer += deltaTime;
                if (avoidanceTimer <= maxAvoidanceTime) {
                    // Calculate tangential avoidance direction with momentum
                    Vector2 avoidDir = calculateTangentialAvoidance(currentPos, avoidanceResult.nearestObstacle,
                            currentVelocity, deltaTime);

                    // Blend with previous avoidance force for smoother transitions
                    if (lastAvoidanceForce.len2() > 0) {
                        avoidDir.scl(0.7f).add(lastAvoidanceForce.scl(0.3f));
                        avoidDir.nor();
                    }

                    movementDir.set(avoidDir);
                    lastAvoidanceForce.set(avoidDir);
                } else {
                    isAvoiding = false;
                }
            } else {
                isAvoiding = false;

                // Gradual return to normal movement
                if (avoidanceForce.len2() > 0.001f) {
                    // Blend movement direction with avoidance force
                    movementDir.scl(1.5f).add(avoidanceForce.scl(avoidanceWeight));
                    movementDir.nor();
                    directionChangeSmoothing = 0.4f;
                    lastAvoidanceForce.set(avoidanceForce).nor();
                } else {
                    directionChangeSmoothing = 0.2f;
                    lastAvoidanceForce.setZero();
                }
            }

            // Blend direction changes more smoothly
            persistentDirection.lerp(movementDir, directionChangeSmoothing).nor();

            // Apply movement with consistent speed
            Vector2 moveVec = new Vector2(persistentDirection).scl(currentSpeed * deltaTime);

            // Apply movement
            applyMovement(movable, moveVec);

            // Update velocity for animations
            updateVelocity(movable, moveVec, deltaTime);

        } catch (Exception e) {
            handleMovementException(e, "Error in ObstacleAvoidanceStrategy: " + e.getMessage());
        }
    }

    private boolean hasNearbyObstacles(Vector2 position, Vector2 direction) {
        if (obstacles == null || obstacles.isEmpty())
            return false;

        // Look in a cone in front of the entity
        Vector2 forward = new Vector2(direction).nor();
        float checkDistance = avoidanceRadius * 1.5f; // Increased check distance

        for (Entity obstacle : obstacles) {
            if (obstacle == null || !obstacle.isActive())
                continue;

            Vector2 toObstacle = new Vector2(obstacle.getX(), obstacle.getY()).sub(position);
            float distance = toObstacle.len();

            if (distance < criticalRadius)
                return true; // Always consider very close obstacles

            if (distance < checkDistance) {
                // Check if obstacle is in front of us (within a 150-degree cone)
                float dot = forward.dot(toObstacle.nor());
                if (dot > -0.866f) { // cos(150°) ≈ -0.866
                    return true;
                }
            }
        }
        return false;
    }

    private Vector2 calculateTangentialAvoidance(Vector2 position, Entity obstacle, Vector2 desiredDir,
            float deltaTime) {
        if (obstacle == null)
            return desiredDir;

        Vector2 toObstacle = new Vector2(obstacle.getX(), obstacle.getY()).sub(position);
        float distance = toObstacle.len();

        // Calculate perpendicular direction
        Vector2 perpendicular = new Vector2(-toObstacle.y, toObstacle.x).nor();

        // Determine optimal avoidance direction
        if (perpendicular.dot(desiredDir) < 0) {
            perpendicular.scl(-1);
        }

        // Enhanced path finding with dynamic steering
        float obstacleRadius = Math.max(obstacle.getWidth(), obstacle.getHeight()) / 2;
        float clearanceNeeded = obstacleRadius + criticalRadius;

        // Calculate a wider berth when close to obstacles
        float proximityFactor = Math.max(0, 1 - (distance / avoidanceRadius));
        float steeringFactor = steeringStrength * (1 + proximityFactor * 2);

        // Apply stronger avoidance when very close
        if (distance < clearanceNeeded * 1.5f) {
            perpendicular.scl(steeringFactor);
            currentRotationFactor = Math.min(currentRotationFactor + deltaTime * 2, 2.0f);
            return perpendicular.nor();
        }

        // Gradually blend between avoidance and desired direction
        float blendFactor = Math.min(1.0f,
                Math.max(0.0f, (distance - clearanceNeeded) / (avoidanceRadius - clearanceNeeded)));
        currentRotationFactor = Math.max(1.0f, currentRotationFactor - deltaTime);

        return perpendicular.scl(1 - blendFactor).add(desiredDir.scl(blendFactor)).nor();
    }

    private class ObstacleAvoidanceResult {
        Vector2 force;
        boolean shouldAvoid;
        Entity nearestObstacle;

        ObstacleAvoidanceResult(Vector2 force, boolean shouldAvoid, Entity nearestObstacle) {
            this.force = force;
            this.shouldAvoid = shouldAvoid;
            this.nearestObstacle = nearestObstacle;
        }
    }

    private ObstacleAvoidanceResult calculateObstacleAvoidance(IMovable movable, Vector2 currentDirection) {
        if (obstacles == null || obstacles.isEmpty()) {
            return new ObstacleAvoidanceResult(new Vector2(0, 0), false, null);
        }

        Vector2 avoidanceForce = new Vector2(0, 0);
        Vector2 movablePos = new Vector2(movable.getX(), movable.getY());
        Vector2 forwardDir = new Vector2(currentDirection).nor();
        boolean shouldAvoid = false;
        Entity nearestObstacle = null;
        float nearestDistance = Float.MAX_VALUE;

        // Look ahead for obstacles in the movement direction
        Vector2 lookaheadPoint = new Vector2(forwardDir).scl(lookaheadDistance).add(movablePos);

        int obstacleCount = 0;
        for (Entity obstacle : obstacles) {
            if (obstacle == null || !obstacle.isActive())
                continue;
            if (movable instanceof Entity && ((Entity) movable) == obstacle)
                continue;

            float obstacleX = obstacle.getX();
            float obstacleY = obstacle.getY();
            Vector2 toObstacle = new Vector2(obstacleX, obstacleY).sub(movablePos);
            float obstacleRadius = Math.max(obstacle.getWidth(), obstacle.getHeight()) / 2;
            float effectiveDistance = toObstacle.len() - obstacleRadius;

            // Update nearest obstacle
            if (effectiveDistance < nearestDistance) {
                nearestDistance = effectiveDistance;
                nearestObstacle = obstacle;
            }

            // Only consider obstacles that are in our path or very close
            Vector2 toLookahead = lookaheadPoint.cpy().sub(movablePos);
            float projectedDist = toObstacle.dot(toLookahead.nor());
            float perpDist = toObstacle.len2() - projectedDist * projectedDist;

            // Consider direction when calculating avoidance
            float directionFactor = (forwardDir.dot(toObstacle.nor()) + 1) * 0.5f; // 0 to 1
            float effectiveRadius = obstacleRadius * (1 + directionFactor * directionWeight);

            // Enhanced obstacle detection
            if ((projectedDist > 0
                    && perpDist < (effectiveRadius + criticalRadius * 2) * (effectiveRadius + criticalRadius * 2))
                    || effectiveDistance < criticalRadius * 1.5f) {
                shouldAvoid = true;
                obstacleCount++;
            }

            // Only consider obstacles within avoidance radius and in front of us
            if (effectiveDistance < avoidanceRadius * 1.2f && projectedDist > -obstacleRadius) {
                Vector2 avoidDir = calculateAvoidanceDirection(movablePos, obstacle, forwardDir);
                float strength = calculateAvoidanceStrength(effectiveDistance)
                        * (1 + directionFactor * directionWeight);

                // Increase avoidance strength when obstacle is directly in path
                if (perpDist < (effectiveRadius + criticalRadius) * (effectiveRadius + criticalRadius)) {
                    strength *= 2.0f;
                }

                avoidanceForce.add(avoidDir.scl(strength));
            }
        }

        // If we have multiple close obstacles, increase avoidance force
        if (obstacleCount > 1) {
            avoidanceForce.scl(1.5f + (obstacleCount - 1) * 0.2f);
        }

        return new ObstacleAvoidanceResult(avoidanceForce, shouldAvoid, nearestObstacle);
    }

    private Vector2 calculateAvoidanceDirection(Vector2 position, Entity obstacle, Vector2 forward) {
        Vector2 toObstacle = new Vector2(obstacle.getX(), obstacle.getY()).sub(position);
        Vector2 perpendicular = new Vector2(-forward.y, forward.x);

        // Choose the perpendicular direction that points away from the obstacle
        if (perpendicular.dot(toObstacle) > 0) {
            perpendicular.scl(-1);
        }

        return perpendicular.nor();
    }

    private float calculateAvoidanceStrength(float distance) {
        float normalizedDistance = distance / avoidanceRadius;
        return (1 - normalizedDistance) * (1 - normalizedDistance); // Quadratic falloff
    }
}