package project.game.MovementManager;

import project.game.MovementManager.defaultmovementbehaviour.ConstantMovementBehavior;
import project.game.MovementManager.defaultmovementbehaviour.FollowMovementBehavior;
import project.game.MovementManager.defaultmovementbehaviour.RandomisedMovementBehavior;
import project.game.MovementManager.defaultmovementbehaviour.ZigZagMovementBehavior;
import project.game.MovementManager.interfaces.IMovementBehavior;
import project.game.MovementManager.interfaces.IMovementManager;

import java.util.Arrays;

/**
 * @class EnemyMovement
 * @brief Manages the movement logic specific to enemy entities.
 *
 * Extends the abstract MovementManager to provide enemy-specific movement
 * behaviors, such as zig-zag movement. Utilizes a Builder pattern for flexible
 * configuration.
 */
public class EnemyMovement extends MovementManager implements IMovementManager {


    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
     */
    private EnemyMovement(Builder builder) {
        super(builder.x, builder.y, builder.speed, builder.direction, builder.movementBehavior);
    }

    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
    }

    @Override
    public void setDeltaTime(float deltaTime) {
        super.setDeltaTime(deltaTime);
    }

    @Override
    public void updateMovement() {
        updatePosition();
    }

    /**
     * @class Builder
     * @brief Builder for EnemyMovement, allowing step-by-step
     * configuration.
     *
     * This Builder pattern facilitates the creation of EnemyMovement
     * instances with customizable movement behaviors such as zig-zag movement.
     */
    public static class Builder {

        private float x;
        private float y;
        private float speed;
        private Direction direction;
        private IMovementBehavior movementBehavior;

        public Builder setX(float x) {
            this.x = x;
            return this;
        }

        public Builder setY(float y) {
            this.y = y;
            return this;
        }

        public Builder setSpeed(float speed) {
            if (speed < 0) {
                throw new IllegalArgumentException("Speed cannot be negative.");
            }
            this.speed = speed;
            return this;
        }

        public Builder setDirection(Direction direction) {
            if (direction == null) {
                throw new IllegalArgumentException("Direction cannot be null.");
            }
            this.direction = direction;
            return this;
        }

        public Builder withZigZagMovement(float amplitude, float frequency) {
            if (amplitude < 0 || frequency < 0) {
                throw new IllegalArgumentException("Amplitude and frequency cannot be negative.");
            }
            this.movementBehavior = new ZigZagMovementBehavior(this.speed, amplitude, frequency);
            return this;
        }

        public Builder withFollowMovement(IMovementManager targetManager) {
            this.movementBehavior = new FollowMovementBehavior(targetManager, this.speed);
            return this;
        }

        public Builder withRandomisedMovement(IMovementManager followTarget, float amplitude, float frequency, float minDuration, float maxDuration) {
            this.movementBehavior = new RandomisedMovementBehavior(
                Arrays.asList(
                    new ConstantMovementBehavior(this.speed),
                    new ZigZagMovementBehavior(this.speed, amplitude, frequency),
                    new FollowMovementBehavior(followTarget, this.speed)
                ),
                minDuration,
                maxDuration
            );
            return this;
        }

        public EnemyMovement build() {
            if (this.movementBehavior == null) {
                // Default to constant movement if no behavior is specified
                this.movementBehavior = new ConstantMovementBehavior(this.speed);
            }
            if (this.direction == null) {
                this.direction = Direction.NONE;
            }
            return new EnemyMovement(this);
        }
    }
}
