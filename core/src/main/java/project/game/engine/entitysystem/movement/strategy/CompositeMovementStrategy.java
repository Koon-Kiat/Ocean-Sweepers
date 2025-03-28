package project.game.engine.entitysystem.movement.strategy;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.movement.api.ICompositeMovementStrategy;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IMovementStrategy;

/**
 * CompositeMovementStrategy combines multiple movement strategies and applies
 * them in sequence, blending their effects.
 */
public class CompositeMovementStrategy implements ICompositeMovementStrategy {

    private static final GameLogger LOGGER = new GameLogger(CompositeMovementStrategy.class);
    private final List<IMovementStrategy> strategies = new ArrayList<>();
    private float[] weights;

    /**
     * Create a composite strategy with a single base strategy
     * 
     * @param baseStrategy The primary movement strategy
     */
    public CompositeMovementStrategy(IMovementStrategy baseStrategy) {
        if (baseStrategy == null) {
            throw new MovementException("Base strategy cannot be null");
        }
        this.strategies.add(baseStrategy);
        this.weights = new float[1];
        this.weights[0] = 1.0f;
    }

    /**
     * Create a composite strategy with multiple strategies and corresponding
     * weights
     * 
     * @param baseStrategy         The primary movement strategy
     * @param additionalStrategies Additional strategies to apply
     * @param weights              Weights for each strategy (including base
     *                             strategy)
     */
    public CompositeMovementStrategy(IMovementStrategy baseStrategy, List<IMovementStrategy> additionalStrategies,
            float[] weights) {
        if (baseStrategy == null) {
            throw new MovementException("Base strategy cannot be null");
        }

        // Add base strategy first
        this.strategies.add(baseStrategy);

        // Add additional strategies
        if (additionalStrategies != null) {
            for (IMovementStrategy strategy : additionalStrategies) {
                if (strategy != null) {
                    this.strategies.add(strategy);
                }
            }
        }

        // Configure weights
        if (weights != null && weights.length == this.strategies.size()) {
            this.weights = weights;
            normalizeWeights(this.weights);
        } else {
            // Default to equal weights
            this.weights = new float[this.strategies.size()];
            for (int i = 0; i < this.weights.length; i++) {
                this.weights[i] = 1.0f / this.weights.length;
            }
        }
    }

    /**
     * Get all the strategies in this composition
     */
    @Override
    public List<IMovementStrategy> getAllStrategies() {
        return new ArrayList<>(strategies);
    }

    /**
     * Get the base strategy (first strategy in the list)
     */
    @Override
    public IMovementStrategy getBaseStrategy() {
        return strategies.isEmpty() ? null : strategies.get(0);
    }

    /**
     * Get the weight for a given strategy index
     */
    @Override
    public float getWeight(int index) {
        if (index >= 0 && index < weights.length) {
            return weights[index];
        }
        return 0;
    }

    /**
     * Set the weight for a given strategy index
     */
    @Override
    public void setWeight(int index, float weight) {
        if (index >= 0 && index < weights.length) {
            weights[index] = weight;
            normalizeWeights(weights);
        }
    }

    /**
     * Add a strategy to the composition with the specified weight
     * 
     * @param strategy The strategy to add
     * @param weight   The weight of this strategy in the composition
     * @return This composite strategy for method chaining
     */
    @Override
    public CompositeMovementStrategy addStrategy(IMovementStrategy strategy, float weight) {
        if (strategy != null) {
            strategies.add(strategy);

            // Create new weights array with one more element
            float[] newWeights = new float[weights.length + 1];
            System.arraycopy(weights, 0, newWeights, 0, weights.length);
            newWeights[weights.length] = weight;

            // Normalize weights
            normalizeWeights(newWeights);
            this.weights = newWeights;
        }
        return this;
    }

    /**
     * Remove a strategy from the composition by index
     * 
     * @param index The index of the strategy to remove
     * @return True if the strategy was removed, false otherwise
     */
    @Override
    public boolean removeStrategy(int index) {
        if (index < 0 || index >= strategies.size() || strategies.size() <= 1) {
            // Can't remove if invalid index or only one strategy remains
            return false;
        }

        strategies.remove(index);

        // Create new weights array with one less element
        float[] newWeights = new float[weights.length - 1];

        // Copy weights before the removed index
        if (index > 0) {
            System.arraycopy(weights, 0, newWeights, 0, index);
        }

        // Copy weights after the removed index
        if (index < weights.length - 1) {
            System.arraycopy(weights, index + 1, newWeights, index, weights.length - 1 - index);
        }

        // Normalize weights
        normalizeWeights(newWeights);
        this.weights = newWeights;

        return true;
    }

    /**
     * Remove a specific strategy from the composition
     * 
     * @param strategy The strategy to remove
     * @return True if the strategy was removed, false otherwise
     */
    @Override
    public boolean removeStrategy(IMovementStrategy strategy) {
        int index = strategies.indexOf(strategy);
        if (index != -1) {
            return removeStrategy(index);
        }
        return false;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Create temporary state for capturing movement from each strategy
            TempMovableState tempState = new TempMovableState(movable);
            Vector2 resultVelocity = new Vector2(0, 0);

            // Apply each strategy with its weight
            for (int i = 0; i < strategies.size(); i++) {
                IMovementStrategy strategy = strategies.get(i);
                if (strategy != null) {
                    // Reset temp state position for each strategy so they all start from the
                    // original position
                    if (i > 0)
                        tempState.resetPosition(movable);

                    float weight = weights[i];
                    applyStrategyWithWeight(strategy, tempState, deltaTime, weight, resultVelocity);
                }
            }

            // Apply the final weighted position and velocity to the actual movable
            movable.setX(tempState.finalX);
            movable.setY(tempState.finalY);

            // Make sure velocity is set consistently
            movable.setVelocity(resultVelocity);

        } catch (Exception e) {
            LOGGER.error("Error in CompositeMovementStrategy: " + e.getMessage(), e);
            throw new MovementException("Failed to apply composite movement", e);
        }
    }

    /**
     * Normalize weights so they sum to 1.0
     */
    private void normalizeWeights(float[] weights) {
        float sum = 0;
        for (float weight : weights) {
            sum += Math.abs(weight); // Use absolute values for weights
        }

        if (sum > 0.0001f) {
            for (int i = 0; i < weights.length; i++) {
                weights[i] = Math.abs(weights[i]) / sum;
            }
        } else {
            // If all weights are zero, use equal weights
            for (int i = 0; i < weights.length; i++) {
                weights[i] = 1.0f / weights.length;
            }
        }
    }

    /**
     * Apply a strategy with the given weight and accumulate its effect
     */
    private void applyStrategyWithWeight(IMovementStrategy strategy, TempMovableState tempState,
            float deltaTime, float weight, Vector2 resultVelocity) {
        // Apply strategy to temp state
        strategy.move(tempState, deltaTime);

        // Accumulate weighted position delta
        float dx = (tempState.getX() - tempState.originalX) * weight;
        float dy = (tempState.getY() - tempState.originalY) * weight;

        tempState.finalX += dx;
        tempState.finalY += dy;

        // Accumulate weighted velocity
        Vector2 strategyVelocity = tempState.getVelocity();
        resultVelocity.add(strategyVelocity.x * weight, strategyVelocity.y * weight);
    }

    /**
     * Temporary state used to capture movement from each strategy
     */
    private static class TempMovableState implements IMovable {
        private float x, y;
        private float finalX, finalY;
        private float originalX, originalY;
        private float speed;
        private final Vector2 velocity = new Vector2();

        public TempMovableState(IMovable source) {
            this.x = source.getX();
            this.y = source.getY();
            this.originalX = x;
            this.originalY = y;
            this.finalX = x;
            this.finalY = y;
            this.speed = source.getSpeed();
            this.velocity.set(source.getVelocity());
        }

        public void resetPosition(IMovable source) {
            this.x = source.getX();
            this.y = source.getY();
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public void setX(float x) {
            this.x = x;
        }

        @Override
        public void setY(float y) {
            this.y = y;
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        @Override
        public void setSpeed(float speed) {
            this.speed = speed;
        }

        @Override
        public Vector2 getVelocity() {
            return velocity;
        }

        @Override
        public void setVelocity(Vector2 velocity) {
            this.velocity.set(velocity);
        }

        @Override
        public void setVelocity(float x, float y) {
            this.velocity.set(x, y);
        }

        @Override
        public void normalizeVelocity() {
            if (velocity.len2() > 0) {
                velocity.nor();
            }
        }

        @Override
        public void clearVelocity() {
            velocity.set(0, 0);
        }
    }
}
