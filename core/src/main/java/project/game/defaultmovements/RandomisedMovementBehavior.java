package project.game.defaultmovements;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.MathUtils;

import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.exceptions.MovementException;

/**
 * @class RandomisedMovementBehavior
 * @brief Randomly selects a behavior from a pool and uses it for a random
 *        duration.
 * 
 *        This class implements a movement behavior that randomly selects a
 *        behavior from
 *        a pool of behaviors and uses it for a random duration. The duration is
 *        set in
 *        the constructor and the behavior is randomly selected from the pool.
 */
public class RandomisedMovementBehavior implements IMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(RandomisedMovementBehavior.class.getName());
    private final List<IMovementBehavior> behaviorPool;
    private final float minDuration;
    private final float maxDuration;
    private IMovementBehavior currentBehavior;
    private float remainingTime;

    /**
     * Constructs a RandomisedMovementBehavior with the specified parameters.
     * 
     * @param behaviorPool List of movement behaviors to choose from.
     * @param minDuration  Minimum duration for each behavior.
     * @param maxDuration  Maximum duration for each behavior.
     */
    public RandomisedMovementBehavior(List<IMovementBehavior> behaviorPool, float minDuration, float maxDuration) {
        if (behaviorPool == null || behaviorPool.isEmpty()) {
            String errorMessage = "Behavior pool cannot be null or empty.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        if (minDuration <= 0 || maxDuration <= 0 || minDuration > maxDuration) {
            String errorMessage = "Invalid duration range: minDuration=" + minDuration + ", maxDuration=" + maxDuration;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        this.behaviorPool = behaviorPool;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        pickRandomBehavior();
    }

    @Override
    public void applyMovementBehavior(MovementData data, float deltaTime) {
        try {
            remainingTime -= deltaTime;
            if (remainingTime <= 0) {
                pickRandomBehavior();
            }
            if (currentBehavior != null) {
                currentBehavior.applyMovementBehavior(data, deltaTime);
            }
        } catch (IllegalArgumentException e) {
            throw new MovementException("Invalid argument in RandomisedMovementBehavior", e);
        } catch (NullPointerException e) {
            throw new MovementException("Null reference in RandomisedMovementBehavior", e);
        } catch (MovementException e) {
            throw e;
        } catch (Exception e) {
            throw new MovementException("Unexpected error in RandomisedMovementBehavior", e);
        }
    }

    private void pickRandomBehavior() {
        currentBehavior = behaviorPool.get(MathUtils.random(behaviorPool.size() - 1));
        remainingTime = MathUtils.random(minDuration, maxDuration);
    }
}
