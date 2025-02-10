package project.game.defaultmovements;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.MathUtils;

import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;

/**
 * @class RandomisedMovementBehavior
 * @brief Randomly selects a behavior from a pool and uses it for a random duration.
 * 
 * This class implements a movement behavior that randomly selects a behavior from
 * a pool of behaviors and uses it for a random duration. The duration is set in
 * the constructor and the behavior is randomly selected from the pool.
 */
public class RandomisedMovementBehavior implements IMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(RandomisedMovementBehavior.class.getName());
    private final List<IMovementBehavior> behaviorPool;
    private final float minDuration;
    private final float maxDuration;

    private IMovementBehavior currentBehavior;
    private float remainingTime;

    public RandomisedMovementBehavior(List<IMovementBehavior> behaviorPool, float minDuration, float maxDuration) {
        if (behaviorPool == null || behaviorPool.isEmpty()) {
            String errorMessage = "Behavior pool cannot be null or empty.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (minDuration <= 0 || maxDuration <= 0 || minDuration > maxDuration) {
            String errorMessage = "Invalid duration range: minDuration=" + minDuration + ", maxDuration=" + maxDuration;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.behaviorPool = behaviorPool;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        pickRandomBehavior();
    }

    @Override
    public void updatePosition(MovementData data) {
        try {
            float deltaTime = data.getDeltaTime();
            if (deltaTime < 0) {
                String errorMessage = "Negative deltaTime provided in RandomisedMovementBehavior.updatePosition: " + deltaTime;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            remainingTime -= deltaTime;
            if (remainingTime <= 0) {
                pickRandomBehavior();
            }
            if (currentBehavior != null) {
                currentBehavior.updatePosition(data);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Invalid input error in RandomisedMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new RuntimeException("Error updating position in RandomisedMovementBehavior", e);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Runtime error in RandomisedMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw e;
        }
    }

    private void pickRandomBehavior() {
        currentBehavior = behaviorPool.get(MathUtils.random(behaviorPool.size() - 1));
        remainingTime = MathUtils.random(minDuration, maxDuration);
    }
}
