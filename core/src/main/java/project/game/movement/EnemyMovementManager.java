package project.game.movement;

/**
 * @class EnemyMovementManager
 * @brief Manages the movement logic specific to enemy entities.
 *
 * Extends the abstract MovementManager to provide enemy-specific movement
 * behaviors, such as zig-zag movement. Utilizes a Builder pattern for flexible
 * configuration.
 */
public class EnemyMovementManager extends MovementManager implements IMovementManager {


    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
     */
    private EnemyMovementManager(Builder builder) {
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
     * @brief Builder for EnemyMovementManager, allowing step-by-step
     * configuration.
     *
     * This Builder pattern facilitates the creation of EnemyMovementManager
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

        public EnemyMovementManager build() {
            if (this.movementBehavior == null) {
                // Default to constant movement if no behavior is specified
                this.movementBehavior = new ConstantMovementBehavior(this.speed);
            }
            if (this.direction == null) {
                this.direction = Direction.NONE;
            }
            return new EnemyMovementManager(this);
        }
    }
}
