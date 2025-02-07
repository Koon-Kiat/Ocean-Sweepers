package project.game.builder;

import java.util.Arrays;

import project.game.Direction;
import project.game.abstractengine.movementmanager.NPCMovementManager;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.abstractengine.movementmanager.interfaces.IMovementManager;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.RandomisedMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;

public class NPCMovementBuilder {

    public float x;
    public float y;
    public float speed;
    public Direction direction;
    public IMovementBehavior movementBehavior;

    public NPCMovementBuilder setX(float x) {
        this.x = x;
        return this;
    }

    public NPCMovementBuilder setY(float y) {
        this.y = y;
        return this;
    }

    public NPCMovementBuilder setSpeed(float speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed cannot be negative.");
        }
        this.speed = speed;
        return this;
    }

    public NPCMovementBuilder setDirection(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null.");
        }
        this.direction = direction;
        return this;
    }

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        if (amplitude < 0 || frequency < 0) {
            throw new IllegalArgumentException("Amplitude and frequency cannot be negative.");
        }
        this.movementBehavior = new ZigZagMovementBehavior(this.speed, amplitude, frequency);
        return this;
    }

    public NPCMovementBuilder withFollowMovement(IMovementManager targetManager) {
        this.movementBehavior = new FollowMovementBehavior(targetManager, this.speed);
        return this;
    }

    public NPCMovementBuilder withRandomisedMovement(IMovementManager followTarget, float amplitude, float frequency, float minDuration, float maxDuration) {
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

    public NPCMovementManager build() {
        if (this.movementBehavior == null) {
            // Default to constant movement if no behavior is specified
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
        }
        if (this.direction == null) {
            this.direction = Direction.NONE;
        }
        return new NPCMovementManager(this);
    }
}
