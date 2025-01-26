package project.game.movementmanager;

import project.game.movementmanager.defaultmovementbehaviour.AcceleratedMovementBehavior;
import project.game.movementmanager.defaultmovementbehaviour.ConstantMovementBehavior;
import project.game.movementmanager.interfaces.IMovementBehavior;

/**
 * @class PlayerMovement
 * @brief Manages the movement logic specific to the player entity.
 *
 * Extends the abstract MovementManager to provide player-specific movement
 * behaviors. Utilizes a Builder pattern to allow flexible configuration of
 * movement properties.
 */
public class PlayerMovement extends MovementManager {

    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
     */
    private PlayerMovement(Builder builder) {
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
     * @brief Builder for PlayerMovement, allowing step-by-step configuration.
     *
     * This Builder pattern facilitates the creation of PlayerMovement instances
     * with customizable movement behaviors such as accelerated or constant
     * movement.
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

        public Builder withAcceleratedMovement(float acceleration, float deceleration) {
            if (acceleration < 0 || deceleration < 0) {
                throw new IllegalArgumentException("Acceleration and deceleration cannot be negative.");
            }
            this.movementBehavior = new AcceleratedMovementBehavior(acceleration, deceleration, this.speed);
            return this;
        }

        public Builder withConstantMovement() {
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
            return this;
        }

        public PlayerMovement build() {
            if (this.movementBehavior == null) {
                // Default to constant movement if no behavior is specified
                this.movementBehavior = new ConstantMovementBehavior(this.speed);
            }
            if (this.direction == null) {
                this.direction = Direction.NONE;
            }
            return new PlayerMovement(this);
        }

    }
}
