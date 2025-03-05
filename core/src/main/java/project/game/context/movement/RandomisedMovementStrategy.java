package project.game.context.movement;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.context.factory.GameConstantsFactory;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;

/**
 * Provides randomised movement for movable entities.
 * 
 * The entity moves in a random direction at a random speed for a random
 * duration.
 * The strategy pool is provided in the constructor.
 */
public class RandomisedMovementStrategy implements IMovementStrategy {

    private static final GameLogger LOGGER = new GameLogger(RandomisedMovementStrategy.class);
    private final List<IMovementStrategy> strategyPool;
    private final float minDuration;
    private final float maxDuration;
    private IMovementStrategy currentStrategy;
    private float remainingTime;
    private final boolean lenientMode;

    /**
     * Constructs a RandomisedMovementStrategy with the specified parameters.
     */
    public RandomisedMovementStrategy(List<IMovementStrategy> strategyPool, float minDuration, float maxDuration,
            boolean lenientMode) {
        this.lenientMode = lenientMode;
        if (strategyPool == null || strategyPool.isEmpty()) {
            String errorMessage = "Invalid strategy pool provided for RandomisedMovementStrategy.";
            LOGGER.error(errorMessage);
            if (lenientMode) {
                LOGGER.warn("{0} Using fallback pool with ConstantMovementStrategy.", errorMessage);
                this.strategyPool = new ArrayList<>();
                this.strategyPool
                        .add(new ConstantMovementStrategy(GameConstantsFactory.getConstants().DEFAULT_SPEED(),
                                lenientMode));
            } else {
                throw new MovementException(errorMessage);
            }
        } else {
            this.strategyPool = strategyPool;
        }
        // Validate durations in a single block.
        if (minDuration <= 0 || maxDuration <= 0) {
            String errorMessage = "Invalid duration range: minDuration=" + minDuration + ", maxDuration=" + maxDuration;
            if (lenientMode) {
                LOGGER.warn("{0} Using fallback values: minDuration=1.0f, maxDuration=2.0f.",
                        errorMessage);
                this.minDuration = 1.0f;
                this.maxDuration = 2.0f;
            } else {
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else if (minDuration > maxDuration) {
            if (lenientMode) {
                LOGGER.warn(
                        "Invalid duration range: minDuration ({0}) is greater than maxDuration ({1}). Swapping values.",
                        new Object[] { minDuration, maxDuration });
                this.minDuration = maxDuration;
                this.maxDuration = minDuration;
            } else {
                String errorMessage = "Invalid duration range: minDuration (" + minDuration
                        + ") is greater than maxDuration (" + maxDuration + ")";
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }
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
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid argument in RandomisedMovementStrategy", e);
            if (!lenientMode) {
                throw new MovementException("Invalid argument in RandomisedMovementStrategy", e);
            }
        } catch (NullPointerException e) {
            LOGGER.error("Null reference in RandomisedMovementStrategy", e);
            if (!lenientMode) {
                throw new MovementException("Null reference in RandomisedMovementStrategy", e);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error in RandomisedMovementStrategy: " + e.getMessage(), e);
            if (!lenientMode) {
                throw new MovementException("Unexpected error in RandomisedMovementStrategy", e);
            }
        }
    }

    private void pickRandomStrategy() {
        currentStrategy = strategyPool.get(MathUtils.random(strategyPool.size() - 1));
    }
}
