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
 * The behavior pool is provided in the constructor.
 */
public class RandomisedMovementStrategy implements IMovementStrategy {

    private static final GameLogger LOGGER = new GameLogger(RandomisedMovementStrategy.class);
    private final List<IMovementStrategy> behaviorPool;
    private final float minDuration;
    private final float maxDuration;
    private IMovementStrategy currentBehavior;
    private float remainingTime;
    private final boolean lenientMode;

    /**
     * Constructs a RandomisedMovementBehavior with the specified parameters.
     */
    public RandomisedMovementStrategy(List<IMovementStrategy> behaviorPool, float minDuration, float maxDuration,
            boolean lenientMode) {
        this.lenientMode = lenientMode;
        if (behaviorPool == null || behaviorPool.isEmpty()) {
            String errorMessage = "Invalid behavior pool provided for RandomisedMovementBehavior.";
            LOGGER.error(errorMessage);
            if (lenientMode) {
                LOGGER.warn("{0} Using fallback pool with ConstantMovementBehavior.", errorMessage);
                this.behaviorPool = new ArrayList<>();
                this.behaviorPool
                        .add(new ConstantMovementStrategy(GameConstantsFactory.getConstants().DEFAULT_SPEED(),
                                lenientMode));
            } else {
                throw new MovementException(errorMessage);
            }
        } else {
            this.behaviorPool = behaviorPool;
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
        pickRandomBehavior();
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            remainingTime -= deltaTime;
            if (remainingTime <= 0) {
                pickRandomBehavior();
                remainingTime = MathUtils.random(minDuration, maxDuration);
            }
            if (currentBehavior != null) {
                currentBehavior.move(movable, deltaTime);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid argument in RandomisedMovementBehavior", e);
            if (!lenientMode) {
                throw new MovementException("Invalid argument in RandomisedMovementBehavior", e);
            }
        } catch (NullPointerException e) {
            LOGGER.error("Null reference in RandomisedMovementBehavior", e);
            if (!lenientMode) {
                throw new MovementException("Null reference in RandomisedMovementBehavior", e);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error in RandomisedMovementBehavior: " + e.getMessage(), e);
            if (!lenientMode) {
                throw new MovementException("Unexpected error in RandomisedMovementBehavior", e);
            }
        }
    }

    private void pickRandomBehavior() {
        currentBehavior = behaviorPool.get(MathUtils.random(behaviorPool.size() - 1));
    }
}
