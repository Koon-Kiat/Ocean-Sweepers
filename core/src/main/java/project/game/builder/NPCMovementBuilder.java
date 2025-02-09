package project.game.builder;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;
import project.game.abstractengine.movementmanager.NPCMovementManager;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;

public class NPCMovementBuilder {

    private static final Logger LOGGER = Logger.getLogger(NPCMovementBuilder.class.getName());

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
            LOGGER.log(Level.SEVERE, "Negative speed provided: {0}.", speed);
            throw new IllegalArgumentException("Speed must be non-negative.");
        }
        this.speed = speed;
        return this;
    }

    public NPCMovementBuilder setDirection(Direction direction) {
        if (direction == null) {
            LOGGER.log(Level.WARNING, "Null direction provided. Defaulting to Direction.NONE.");
            this.direction = Direction.NONE;
        } else {
            this.direction = direction;
        }
        return this;
    }

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        if (amplitude < 0 || frequency < 0) {
            LOGGER.log(Level.WARNING, "Negative amplitude and/or frequency provided: amplitude={0}, frequency={1}", new Object[]{amplitude, frequency});
        }
        this.movementBehavior = new ZigZagMovementBehavior(this.speed, amplitude, frequency);
        return this;
    }

    // Other movement behavior methods...
public NPCMovementManager build() {
        if (this.movementBehavior == null) {
            // Default to constant movement if no behavior is specified
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
        }
        if (this.direction == null) {
            this.direction = Direction.NONE;
        }
        // Check if a movement behavior is used while direction is NONE.
        if (this.direction == Direction.NONE) {
            LOGGER.log(Level.SEVERE, "Invalid configuration: Movement behavior {0} cannot be used with Direction.NONE.", this.movementBehavior.getClass().getSimpleName());
            throw new IllegalArgumentException("Movement behavior cannot be used with Direction.NONE.");
        }
        return new NPCMovementManager(this);
    }
}
