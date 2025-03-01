package project.game.context.movement;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.badlogic.gdx.math.MathUtils;

import project.game.common.api.ILogger;
import project.game.common.exception.MovementException;
import project.game.common.logging.LogManager;
import project.game.context.core.GameConstants;
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

    private static final ILogger LOGGER = LogManager.getLogger(RandomisedMovementBehavior.class);
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
            LOGGER.log(Level.SEVERE, errorMessage);
            if (MovementManager.LENIENT_MODE) {
                LOGGER.log(Level.WARNING, "{0} Using fallback pool with ConstantMovementBehavior.", errorMessage);
                this.behaviorPool = new ArrayList<>();
                this.behaviorPool.add(new ConstantMovementBehavior(GameConstants.DEFAULT_SPEED));
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
                LOGGER.log(Level.WARNING, "{0} Using fallback values: minDuration=1.0f, maxDuration=2.0f.",
                        errorMessage);
                this.minDuration = 1.0f;
                this.maxDuration = 2.0f;
            } else {
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new MovementException(errorMessage);
            }
        } else if (minDuration > maxDuration) {
            if (MovementManager.LENIENT_MODE) {
                LOGGER.log(Level.WARNING,
                        "Invalid duration range: minDuration ({0}) is greater than maxDuration ({1}). Swapping values.",
                        new Object[] { minDuration, maxDuration });
                this.minDuration = maxDuration;
                this.maxDuration = minDuration;
            } else {
                String errorMessage = "Invalid duration range: minDuration (" + minDuration
                        + ") is greater than maxDuration (" + maxDuration + ")";
                LOGGER.log(Level.SEVERE, errorMessage);
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
            LOGGER.log(Level.SEVERE, "Invalid argument in RandomisedMovementBehavior", e);
            throw new MovementException("Invalid argument in RandomisedMovementBehavior", e);
        } catch (NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Null reference in RandomisedMovementBehavior", e);
            throw new MovementException("Null reference in RandomisedMovementBehavior", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in RandomisedMovementBehavior: " + e.getMessage(), e);
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
