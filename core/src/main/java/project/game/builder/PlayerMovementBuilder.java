// filepath: /c:/OOPProject/OOPProject/core/src/main/java/project/game/movementmanager/PlayerMovementPlayerMovementBuilder.java
package project.game.builder;

import project.game.Direction;
import project.game.abstractengine.movementmanager.PlayerMovementManager;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.defaultmovements.AcceleratedMovementBehavior;
import project.game.defaultmovements.ConstantMovementBehavior;

/**
 * @class PlayerMovementBuilder
 * @brief Builder for PlayerMovement, now moved to its own file.
 *
 * This builder facilitates the creation of PlayerMovement instances with
 * customizable movement behaviors.
 */
public class PlayerMovementBuilder {

    public float x;
    public float y;
    public float speed;
    public Direction direction;
    public IMovementBehavior movementBehavior;

    public PlayerMovementBuilder setX(float x) {
        this.x = x;
        return this;
    }

    public PlayerMovementBuilder setY(float y) {
        this.y = y;
        return this;
    }

    public PlayerMovementBuilder setSpeed(float speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed cannot be negative.");
        }
        this.speed = speed;
        return this;
    }

    public PlayerMovementBuilder setDirection(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null.");
        }
        this.direction = direction;
        return this;
    }

    public PlayerMovementBuilder withAcceleratedMovement(float acceleration, float deceleration) {
        if (acceleration < 0 || deceleration < 0) {
            throw new IllegalArgumentException("Acceleration and deceleration cannot be negative.");
        }
        this.movementBehavior = new AcceleratedMovementBehavior(acceleration, deceleration, this.speed);
        return this;
    }

    public PlayerMovementBuilder withConstantMovement() {
        this.movementBehavior = new ConstantMovementBehavior(this.speed);
        return this;
    }

    public PlayerMovementManager build() {
        if (this.movementBehavior == null) {
            // Default to constant movement if no behavior is specified
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
        }
        if (this.direction == null) {
            this.direction = Direction.NONE;
        }
        return new PlayerMovementManager(this);
    }

}
