package project.game.application.movement.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.badlogic.gdx.math.Vector2;

import project.game.application.entity.item.Trash;
import project.game.application.movement.api.StrategyType;
import project.game.common.exception.MovementException;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;

/**
 * A movement strategy that targets the nearest trash entity in the game world.
 * 
 * This strategy continuously finds the closest trash object and directs
 * movement toward it.
 */
public class NearestTrashStrategy extends AbstractMovementStrategy {

    private final float speed;
    private final Vector2 direction;
    private final float minTargetSwitchDistance;
    private List<Trash> trashEntities;
    private Trash currentTarget;
    private final Entity targetingEntity;

    /**
     * Constructs a new NearestTrashStrategy.
     * 
     * @param speed         The movement speed
     * @param trashEntities The list of trash entities to target (can be updated)
     * @param lenientMode   Whether to use lenient mode for error handling
     */
    public NearestTrashStrategy(float speed, List<Trash> trashEntities, boolean lenientMode) {
        super(NearestTrashStrategy.class, lenientMode);
        this.speed = speed;
        this.trashEntities = trashEntities != null ? trashEntities : new ArrayList<>();
        this.direction = new Vector2(0, 0);
        this.minTargetSwitchDistance = 20.0f;
        this.targetingEntity = new Entity(); // Create a dedicated entity for position tracking

        logger.info("NearestTrashStrategy initialized with {0} trash entities", this.trashEntities.size());
    }

    /**
     * Updates the list of trash entities to target.
     * 
     * @param trashEntities The new list of trash entities
     */
    public void updateTrashEntities(List<Trash> trashEntities) {
        if (trashEntities != null) {
            this.trashEntities = new ArrayList<>(trashEntities);
            logger.debug("Updated trash entities list: now {0} entities", this.trashEntities.size());
        }
    }

    /**
     * Filters the trash entities based on a custom predicate.
     * 
     * @param filter The filter predicate to apply
     */
    public void filterTrashEntities(Predicate<Trash> filter) {
        if (filter != null && trashEntities != null) {
            trashEntities.removeIf(trash -> !filter.test(trash));
            logger.debug("Filtered trash entities: {0} entities remain", trashEntities.size());
        }
    }

    /**
     * Get velocity vector for external callers.
     */
    public Vector2 getVelocityVector(IMovable movable) {
        try {
            if (movable == null) {
                if (lenientMode) {
                    logger.warn("Entity is null in NearestTrashStrategy.getVelocityVector; returning zero velocity");
                    return new Vector2(0, 0);
                } else {
                    throw new MovementException("Entity cannot be null in NearestTrashStrategy");
                }
            }

            // Update our targeting entity to match the movable's position
            targetingEntity.setX(movable.getX());
            targetingEntity.setY(movable.getY());

            // Find the nearest trash
            Trash nearestTrash = findNearestTrash(targetingEntity);

            // Return movement vector toward the nearest trash
            return calculateMovementVectorToTarget(nearestTrash, targetingEntity);

        } catch (MovementException e) {
            if (lenientMode) {
                logger.error("Error in NearestTrashStrategy.getVelocityVector: {0}", e.getMessage());
                return new Vector2(0, 0);
            } else {
                throw e;
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            if (lenientMode) {
                logger.error("Parameter error in NearestTrashStrategy.getVelocityVector: {0}", e.getMessage());
                return new Vector2(0, 0);
            } else {
                throw new MovementException("Parameter error in NearestTrashStrategy", e);
            }
        } catch (Exception e) {
            if (lenientMode) {
                logger.error("Unexpected error in NearestTrashStrategy.getVelocityVector: {0}", e.getMessage());
                return new Vector2(0, 0);
            } else {
                throw new MovementException("Error calculating velocity in NearestTrashStrategy", e);
            }
        }
    }

    /**
     * Gets the strategy type for this movement strategy.
     */
    @Override
    public StrategyType getStrategyType() {
        return StrategyType.NEAREST_TRASH;
    }

    /**
     * Find the nearest trash entity and move towards it.
     */
    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            if (movable == null) {
                if (lenientMode) {
                    logger.warn("Entity is null in NearestTrashStrategy.move; skipping movement");
                    return;
                } else {
                    throw new MovementException("Entity cannot be null in NearestTrashStrategy");
                }
            }

            // Update our targeting entity with the movable's position
            targetingEntity.setX(movable.getX());
            targetingEntity.setY(movable.getY());

            // Look for a new nearest trash
            Trash nearestTrash = findNearestTrash(targetingEntity);

            // Decide whether to update the current target
            if (shouldSwitchTarget(currentTarget, nearestTrash, targetingEntity)) {
                currentTarget = nearestTrash;
                if (currentTarget != null) {
                    logger.debug("Switching to new trash target at ({0}, {1})",
                            currentTarget.getEntity().getX(), currentTarget.getEntity().getY());
                }
            }

            // Calculate movement vector and velocity
            Vector2 velocity = calculateMovementVectorToTarget(currentTarget, targetingEntity);

            // Apply movement with deltaTime
            velocity.scl(deltaTime);

            // Update movable position
            applyMovement(movable, velocity);

            // Update velocity for animations
            updateVelocity(movable, velocity, deltaTime);

        } catch (MovementException e) {
            handleMovementException(e, "Error in NearestTrashStrategy.move");
        } catch (NullPointerException | IllegalArgumentException e) {
            handleMovementException(e, "Parameter error in NearestTrashStrategy.move");
        } catch (Exception e) {
            handleMovementException(e, "Unexpected error in NearestTrashStrategy.move");
        }
    }

    /**
     * Finds the nearest active trash entity to the specified entity.
     * 
     * @param entity The entity to measure from
     * @return The nearest trash entity, or null if none are available
     */
    private Trash findNearestTrash(Entity entity) {
        if (trashEntities == null || trashEntities.isEmpty() || entity == null) {
            return null;
        }

        Trash nearest = null;
        float nearestDistance = Float.MAX_VALUE;

        for (Trash trash : trashEntities) {
            if (trash == null || !trash.getEntity().isActive()) {
                continue;
            }

            float distance = calculateDistance(entity, trash.getEntity());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = trash;
            }
        }

        return nearest;
    }

    /**
     * Calculates the distance between two entities.
     * 
     * @param entity1 The first entity
     * @param entity2 The second entity
     * @return The distance between the entities
     */
    private float calculateDistance(Entity entity1, Entity entity2) {
        float dx = entity1.getX() - entity2.getX();
        float dy = entity1.getY() - entity2.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Determines whether to switch to a new target.
     * 
     * @param currentTarget The current target
     * @param newTarget     The potential new target
     * @param entity        The entity that is moving
     * @return True if the target should be switched, false otherwise
     */
    private boolean shouldSwitchTarget(Trash currentTarget, Trash newTarget, Entity entity) {
        if (currentTarget == null || !currentTarget.getEntity().isActive()) {
            return newTarget != null;
        }

        if (newTarget == null) {
            return false;
        }

        float currentDistance = calculateDistance(entity, currentTarget.getEntity());
        float newDistance = calculateDistance(entity, newTarget.getEntity());

        // Switch if new target is significantly closer
        return newDistance < currentDistance - minTargetSwitchDistance;
    }

    /**
     * Calculate the movement vector toward a target trash entity.
     * 
     * @param trash      The trash entity to target
     * @param fromEntity The entity to calculate movement from
     * @return A movement vector scaled by speed
     */
    private Vector2 calculateMovementVectorToTarget(Trash trash, Entity fromEntity) {
        // If no target or target is inactive, maintain current direction
        if (trash == null || !trash.getEntity().isActive()) {
            if (direction.len() < 0.001f) {
                direction.set(1, 0); // Default direction if none set
            }
            return new Vector2(direction).nor().scl(speed);
        }

        // Calculate direction vector toward the trash
        direction.x = trash.getEntity().getX() - fromEntity.getX();
        direction.y = trash.getEntity().getY() - fromEntity.getY();

        // Normalize and scale by speed
        if (direction.len() > 0.001f) {
            direction.nor();
        } else {
            // We're very close to the target, maintain direction but reduce speed
            return new Vector2(direction).scl(speed * 0.5f);
        }

        return new Vector2(direction).scl(speed);
    }
}