package project.game.context.movement;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import project.game.common.exception.MovementException;
import project.game.common.factory.GameConstantsFactory;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;
import project.game.engine.entitysystem.movement.AbstractMovementStrategy;

/**
 * Provides randomised movement for movable entities.
 * 
 * The entity moves in a random direction at a random speed for a random
 * duration.
 * The strategy pool is provided in the constructor.
 */
public class RandomisedMovementStrategy extends AbstractMovementStrategy {

    private final List<IMovementStrategy> strategyPool;
    private final float minDuration;
    private final float maxDuration;
    private IMovementStrategy currentStrategy;
    private float remainingTime;

    /**
     * Constructs a RandomisedMovementStrategy with the specified parameters.
     */
    public RandomisedMovementStrategy(List<IMovementStrategy> strategyPool, float minDuration, float maxDuration,
            boolean lenientMode) {
        super(RandomisedMovementStrategy.class, lenientMode);

        // Validate strategy pool
        this.strategyPool = validateStrategyPool(strategyPool);

        // Validate durations
        float[] durations = validateRange(minDuration, maxDuration, "minDuration", "maxDuration", 1.0f, 2.0f);
        this.minDuration = durations[0];
        this.maxDuration = durations[1];

        // Initialize state
        this.remainingTime = MathUtils.random(this.minDuration, this.maxDuration);
        pickRandomStrategy();
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            remainingTime -= deltaTime;
            if (remainingTime <= 0) {
                pickRandomStrategy();
                remainingTime = MathUtils.random(minDuration, maxDuration);
            }
            if (currentStrategy != null) {
                currentStrategy.move(movable, deltaTime);
            }
        } catch (Exception e) {
            handleMovementException(e, "Error in RandomisedMovementStrategy: " + e.getMessage());
        }
    }

    private void pickRandomStrategy() {
        currentStrategy = strategyPool.get(MathUtils.random(strategyPool.size() - 1));
    }

    /**
     * Validates the strategy pool and provides a fallback if needed
     */
    private List<IMovementStrategy> validateStrategyPool(List<IMovementStrategy> pool) {
        if (pool == null || pool.isEmpty()) {
            String errorMessage = "Invalid strategy pool provided for RandomisedMovementStrategy.";
            logger.error(errorMessage);
            if (lenientMode) {
                logger.warn("{0} Using fallback pool with ConstantMovementStrategy.", errorMessage);
                List<IMovementStrategy> fallbackPool = new ArrayList<>();
                fallbackPool.add(new ConstantMovementStrategy(
                        GameConstantsFactory.getConstants().DEFAULT_SPEED(),
                        lenientMode));
                return fallbackPool;
            } else {
                throw new MovementException(errorMessage);
            }
        }
        return pool;
    }
}
