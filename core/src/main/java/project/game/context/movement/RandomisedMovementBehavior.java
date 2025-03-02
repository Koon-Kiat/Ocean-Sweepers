package project.game.context.movement;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import project.game.common.exception.MovementException;
import project.game.common.logging.GameLogger;
import project.game.context.factory.GameConstantsFactory;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.entitysystem.entity.MovableEntity;
import project.game.engine.entitysystem.movement.MovementManager;

/**
 * Provides randomised movement for movable entities.
 * 
 * The entity moves in a random direction at a random speed for a random
 * duration.
 * The behavior pool is provided in the constructor.
 */
public class RandomisedMovementBehavior implements IMovementBehavior {

    private static final GameLogger LOGGER = new GameLogger(RandomisedMovementBehavior.class);
    private final List<IMovementBehavior> behaviorPool;
    private final float minDuration;
    private final float maxDuration;
    private IMovementBehavior currentBehavior;
    private float remainingTime;

    /**
     * Constructs a RandomisedMovementBehavior with the specified parameters.
     */
    public RandomisedMovementBehavior(List<IMovementBehavior> behaviorPool, float minDuration, float maxDuration) {
        if (behaviorPool == null || behaviorPool.isEmpty()) {
            String errorMessage = "Invalid behavior pool provided for RandomisedMovementBehavior.";
            LOGGER.error(errorMessage);
            if (MovementManager.LENIENT_MODE) {
                LOGGER.warn("{0} Using fallback pool with ConstantMovementBehavior.", errorMessage);
                this.behaviorPool = new ArrayList<>();
                this.behaviorPool
                        .add(new ConstantMovementBehavior(GameConstantsFactory.getConstants().DEFAULT_SPEED()));
            } else {
                throw new MovementException(errorMessage);
            }
        } else {
            this.behaviorPool = behaviorPool;
        }
        // Validate durations in a single block.
        if (minDuration <= 0 || maxDuration <= 0) {
            String errorMessage = "Invalid duration range: minDuration=" + minDuration + ", maxDuration=" + maxDuration;
            if (MovementManager.LENIENT_MODE) {
                LOGGER.warn("{0} Using fallback values: minDuration=1.0f, maxDuration=2.0f.",
                        errorMessage);
                this.minDuration = 1.0f;
                this.maxDuration = 2.0f;
            } else {
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else if (minDuration > maxDuration) {
            if (MovementManager.LENIENT_MODE) {
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
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            remainingTime -= deltaTime;
            if (remainingTime <= 0) {
                pickRandomBehavior();
            }
            if (currentBehavior != null) {
                currentBehavior.applyMovementBehavior(entity, deltaTime);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid argument in RandomisedMovementBehavior", e);
            throw new MovementException("Invalid argument in RandomisedMovementBehavior", e);
        } catch (NullPointerException e) {
            LOGGER.error("Null reference in RandomisedMovementBehavior", e);
            throw new MovementException("Null reference in RandomisedMovementBehavior", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error in RandomisedMovementBehavior: " + e.getMessage(), e);
            if (!MovementManager.LENIENT_MODE) {
                throw new MovementException("Unexpected error in RandomisedMovementBehavior", e);
            }
        }
    }

    private void pickRandomBehavior() {
        currentBehavior = behaviorPool.get(MathUtils.random(behaviorPool.size() - 1));
        remainingTime = MathUtils.random(minDuration, maxDuration);
    }
}
