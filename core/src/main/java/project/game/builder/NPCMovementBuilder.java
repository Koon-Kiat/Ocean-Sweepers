package project.game.builder;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;
import project.game.abstractengine.movementmanager.NPCMovementManager;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.abstractengine.movementmanager.interfaces.IMovementManager;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.RandomisedMovementBehavior;
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
            String errorMessage = "Negative speed provided: " + speed;
            LOGGER.log(Level.SEVERE, errorMessage);
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
            LOGGER.log(Level.WARNING, "Negative amplitude and/or frequency provided: amplitude={0}, frequency={1}",
                    new Object[] { amplitude, frequency });
        }
        try {
            this.movementBehavior = new ZigZagMovementBehavior(this.speed, amplitude, frequency);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating ZigZagMovementBehavior: " + e.getMessage(), e);
            throw e;
        }
        return this;
    }

    public NPCMovementBuilder withFollowMovement(IMovementManager targetManager) {
        if (targetManager == null) {
            String errorMessage = "Target manager is null in withFollowMovement.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (this.speed <= 0) {
            String errorMessage = "Invalid speed for FollowMovementBehavior: " + this.speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException("Speed must be positive for FollowMovementBehavior.");
        }
        try {
            this.movementBehavior = new FollowMovementBehavior(targetManager, this.speed);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating FollowMovementBehavior: " + e.getMessage(), e);
            throw new RuntimeException("Error creating FollowMovementBehavior", e);
        }
        return this;
    }

    public NPCMovementBuilder withRandomisedMovement(List<IMovementBehavior> behaviorPool, float minDuration,
            float maxDuration) {
        if (behaviorPool == null || behaviorPool.isEmpty()) {
            String errorMessage = "Invalid behavior pool provided for RandomisedMovementBehavior.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (minDuration <= 0 || maxDuration <= 0 || minDuration > maxDuration) {
            String errorMessage = "Invalid duration range for RandomisedMovementBehavior: minDuration=" + minDuration
                    + ", maxDuration=" + maxDuration;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            this.movementBehavior = new RandomisedMovementBehavior(behaviorPool, minDuration, maxDuration);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating RandomisedMovementBehavior: " + e.getMessage(), e);
            throw new RuntimeException("Error creating RandomisedMovementBehavior", e);
        }
        return this;
    }

    public NPCMovementManager build() {
        if (this.movementBehavior == null) {
            // Default to constant movement if no behavior is specified.
            String warnMessage = "No movement behavior specified. Defaulting to ConstantMovementBehavior.";
            LOGGER.log(Level.WARNING, warnMessage);
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
        }
        if (this.direction == null) {
            this.direction = Direction.NONE;
            LOGGER.log(Level.WARNING, "No direction specified. Defaulting to Direction.NONE.");
        }
        // For movement behaviors that rely on a specified direction (i.e. not
        // FollowMovementBehavior),
        // ensure that direction is not NONE.
        if (!(this.movementBehavior instanceof FollowMovementBehavior) && this.direction == Direction.NONE) {
            String errorMessage = "Invalid configuration: Movement behavior "
                    + this.movementBehavior.getClass().getSimpleName() + " cannot be used with Direction.NONE.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException("Movement behavior cannot be used with Direction.NONE.");
        }
        return new NPCMovementManager(this);
    }
}
