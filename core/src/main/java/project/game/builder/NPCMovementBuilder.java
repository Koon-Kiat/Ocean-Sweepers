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

/**
 * @class NPCMovementBuilder
 * @brief Builder for NPCMovement
 *
 *        This builder facilitates the creation of NPCMovement instances with
 *        customizable movement behaviors.
 */
public class NPCMovementBuilder {

    private static final Logger LOGGER = Logger.getLogger(NPCMovementBuilder.class.getName());
    public float x;
    public float y;
    public float speed;
    public Direction direction;
    public IMovementBehavior movementBehavior;

    public float getX() {
        return x;
    }

    public NPCMovementBuilder setX(float x) {
        validateCoordinate(x, "X");
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public NPCMovementBuilder setY(float y) {
        validateCoordinate(y, "Y");
        this.y = y;
        return this;
    }

    public float getSpeed() {
        return speed;
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

    public Direction getDirection() {
        return direction;
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

    public IMovementBehavior getMovementBehavior() {
        return movementBehavior;
    }

    public NPCMovementBuilder withConstantMovement() {
        this.movementBehavior = new ConstantMovementBehavior(this.speed);
        return this;
    }

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        validateZigZagParams(amplitude, frequency);
        try {
            this.movementBehavior = new ZigZagMovementBehavior(this.speed, amplitude, frequency);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating ZigZagMovementBehavior", e);
            throw new IllegalStateException("Failed to create ZigZagMovementBehavior", e);
        }
        return this;
    }

    public NPCMovementBuilder withFollowMovement(IMovementManager targetManager) {
        if (targetManager == null) {
            String errorMsg = "Target manager is null in withFollowMovement.";
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (this.speed <= 0) {
            String errorMessage = "Invalid speed for FollowMovementBehavior: " + this.speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException("Speed must be positive for FollowMovementBehavior.");
        }
        try {
            this.movementBehavior = new FollowMovementBehavior(targetManager, this.speed);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating FollowMovementBehavior", e);
            throw new IllegalStateException("Failed to create FollowMovementBehavior", e);
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
            String errorMessage = String.format(
                    "Invalid duration range for RandomisedMovementBehavior: minDuration=%.2f, maxDuration=%.2f",
                    minDuration, maxDuration);
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            this.movementBehavior = new RandomisedMovementBehavior(behaviorPool, minDuration, maxDuration);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating RandomisedMovementBehavior", e);
            throw new IllegalStateException("Failed to create RandomisedMovementBehavior", e);
        }
        return this;
    }

    public NPCMovementManager build() {
        validateBuildRequirements();

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

    // Private validations
    private void validateCoordinate(float coordinate, String coordinateName) {
        if (Float.isNaN(coordinate) || Float.isInfinite(coordinate)) {
            String errorMessage = "Invalid " + coordinateName + " coordinate: " + coordinate;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateZigZagParams(float amplitude, float frequency) {
        if (amplitude < 0 || frequency < 0) {
            String errorMsg = String.format(
                    "Negative amplitude or frequency for ZigZagMovement: amplitude=%.2f, frequency=%.2f",
                    amplitude, frequency);
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    private void validateBuildRequirements() {
        if (speed < 0) {
            String errorMessage = "Speed cannot be negative. Current speed: " + speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }
}
