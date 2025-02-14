package project.game.builder;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;
import project.game.abstractengine.entity.movementmanager.NPCMovementManager;
import project.game.abstractengine.entity.movementmanager.interfaces.IMovementBehavior;
import project.game.abstractengine.entity.movementmanager.interfaces.IMovementManager;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.RandomisedMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;
import project.game.exceptions.MovementException;

/**
 * @class NPCMovementBuilder
 * @brief Builder for NPCMovement
 *
 *        This builder facilitates the creation of NPCMovement instances with
 *        customizable movement behaviors.
 */
public class NPCMovementBuilder {

    private static final Logger LOGGER = Logger.getLogger(NPCMovementBuilder.class.getName());
    private float x;
    private float y;
    private float speed;
    private Direction direction;
    private IMovementBehavior movementBehavior;

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
            throw new MovementException("Speed must be non-negative.");
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
        try {
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Failed to create ConstantMovementBehavior: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error creating ConstantMovementBehavior", e);
            throw new MovementException("Failed to create ConstantMovementBehavior", e);
        }
        return this;
    }

    public NPCMovementBuilder withZigZagMovement(float amplitude, float frequency) {
        validateZigZagParams(amplitude, frequency);
        try {
            this.movementBehavior = new ZigZagMovementBehavior(this.speed, amplitude, frequency);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating ZigZagMovementBehavior", e);
            throw new MovementException("Failed to create ZigZagMovementBehavior", e);
        }
        return this;
    }

    public NPCMovementBuilder withFollowMovement(IMovementManager targetManager) {
        if (targetManager == null) {
            String errorMsg = "Target manager is null in withFollowMovement.";
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new MovementException(errorMsg);
        }
        if (this.speed <= 0) {
            String errorMessage = "Invalid speed for FollowMovementBehavior: " + this.speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException("Speed must be positive for FollowMovementBehavior.");
        }
        try {
            this.movementBehavior = new FollowMovementBehavior(targetManager, this.speed);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating FollowMovementBehavior", e);
            throw new MovementException("Failed to create FollowMovementBehavior", e);
        }
        return this;
    }

    public NPCMovementBuilder withRandomisedMovement(List<IMovementBehavior> behaviorPool, float minDuration,
            float maxDuration) {
        if (behaviorPool == null || behaviorPool.isEmpty()) {
            String errorMessage = "Invalid behavior pool provided for RandomisedMovementBehavior.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        if (minDuration <= 0 || maxDuration <= 0 || minDuration > maxDuration) {
            String errorMessage = String.format(
                    "Invalid duration range for RandomisedMovementBehavior: minDuration=%.2f, maxDuration=%.2f",
                    minDuration, maxDuration);
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        try {
            this.movementBehavior = new RandomisedMovementBehavior(behaviorPool, minDuration, maxDuration);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception creating RandomisedMovementBehavior", e);
            throw new MovementException("Failed to create RandomisedMovementBehavior", e);
        }
        return this;
    }

    public NPCMovementManager build() {
        try {
            validateBuildRequirements();

            if (this.movementBehavior == null) {
                String warnMessage = "No movement behavior specified. Defaulting to ConstantMovementBehavior.";
                LOGGER.log(Level.WARNING, warnMessage);
                withConstantMovement(); // Use existing method instead of direct instantiation
            }

            if (this.direction == null) {
                this.direction = Direction.NONE;
                LOGGER.log(Level.WARNING, "No direction specified. Defaulting to Direction.NONE.");
            }

            if (!(this.movementBehavior instanceof FollowMovementBehavior) && this.direction == Direction.NONE) {
                String errorMessage = String.format(
                        "Invalid configuration: %s cannot be used with Direction.NONE",
                        this.movementBehavior.getClass().getSimpleName());
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new MovementException(errorMessage);
            }

            return new NPCMovementManager(this);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Failed to build NPCMovementManager: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            String msg = "Unexpected error building NPCMovementManager";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new MovementException(msg, e);
        }
    }

    // Private validations
    private void validateCoordinate(float coordinate, String coordinateName) {
        if (Float.isNaN(coordinate) || Float.isInfinite(coordinate)) {
            String errorMessage = "Invalid " + coordinateName + " coordinate: " + coordinate;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
    }

    private void validateZigZagParams(float amplitude, float frequency) {
        if (amplitude < 0 || frequency < 0) {
            String errorMsg = String.format(
                    "Negative amplitude or frequency for ZigZagMovement: amplitude=%.2f, frequency=%.2f",
                    amplitude, frequency);
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new MovementException(errorMsg);
        }
    }

    private void validateBuildRequirements() {
        if (speed < 0) {
            String errorMessage = "Speed cannot be negative. Current speed: " + speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
    }
}
