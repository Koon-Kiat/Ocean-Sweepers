package project.game.defaultmovements;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;

/**
 * @class RandomisedMovementBehavior
 * @brief Chooses a random sub-behavior from a pool and applies it via
 * MovementData.
 */
public class RandomisedMovementBehavior implements IMovementBehavior {

    private final List<IMovementBehavior> behaviorPool;

    // Minimum and maximum duration (in seconds) this behavior will remain active
    private final float minDuration;
    private final float maxDuration;

    private IMovementBehavior currentBehavior;
    private float remainingTime;

    /**
     * @param behaviorPool A list of movement behaviors to randomly choose from.
     * @param minDuration The minimum time (in seconds) to use a chosen
     * behavior.
     * @param maxDuration The maximum time (in seconds) to use a chosen
     * behavior.
     *
     * Example usage: RandomisedMovementBehavior randomBehavior = new
     * RandomisedMovementBehavior( Arrays.asList(new
     * ConstantMovementBehavior(200f), new AcceleratedMovementBehavior(50f, 20f,
     * 300f), new FollowMovementBehavior(playerManager, 100f)), 2f, 5f );
     */
    public RandomisedMovementBehavior(List<IMovementBehavior> behaviorPool, float minDuration, float maxDuration) {
        if (behaviorPool == null || behaviorPool.isEmpty()) {
            throw new IllegalArgumentException("Behavior pool cannot be null or empty.");
        }
        if (minDuration <= 0 || maxDuration <= 0 || minDuration > maxDuration) {
            throw new IllegalArgumentException("Invalid duration range.");
        }

        this.behaviorPool = behaviorPool;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;

        // Pick an initial behavior
        pickRandomBehavior();
    }


    /**
     * Updates the position of the MovementManager based on the current behavior.
     *
     * @param data The MovementData containing the position, direction, and delta
     * time.
     */
    @Override
    public void updatePosition(MovementData data) {
        float deltaTime = data.getDeltaTime();
        remainingTime -= deltaTime;

        // If the current behavior expired, pick a new one
        if (remainingTime <= 0) {
            pickRandomBehavior();
        }

        // Delegate to the currently active behavior
        if (currentBehavior != null) {
            currentBehavior.updatePosition(data);
        }
    }

    /**
     * Randomly selects a new behavior from the pool and sets a random duration.
     */
    private void pickRandomBehavior() {
        currentBehavior = behaviorPool.get(MathUtils.random(behaviorPool.size() - 1));
        remainingTime = MathUtils.random(minDuration, maxDuration);
    }
}
