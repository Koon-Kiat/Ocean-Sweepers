package project.game.builder;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;
import project.game.abstractengine.movementmanager.PlayerMovementManager;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.defaultmovements.AcceleratedMovementBehavior;
import project.game.defaultmovements.ConstantMovementBehavior;

/**
 * @class PlayerMovementBuilder
 * @brief Builder for PlayerMovement
 *
 *        This builder facilitates the creation of PlayerMovement instances with
 *        customizable movement behaviors.
 */
public class PlayerMovementBuilder {

    private static final Logger LOGGER = Logger.getLogger(PlayerMovementBuilder.class.getName());
    private static final float DEFAULT_SPEED = 200f;
    private static final float DEFAULT_ACCELERATION = 500f;
    private static final float DEFAULT_DECELERATION = 250f;
    private float x;
    private float y;
    private float speed = DEFAULT_SPEED;
    private Direction direction = Direction.NONE;
    private IMovementBehavior movementBehavior;

    // Static factory methods
    public static PlayerMovementBuilder createDefaultPlayer(float x, float y) {
        return new PlayerMovementBuilder()
                .setX(x)
                .setY(y)
                .setSpeed(DEFAULT_SPEED)
                .withConstantMovement();
    }

    public static PlayerMovementBuilder createAcceleratingPlayer(float x, float y) {
        return new PlayerMovementBuilder()
                .setX(x)
                .setY(y)
                .setSpeed(DEFAULT_SPEED)
                .withAcceleratedMovement(DEFAULT_ACCELERATION, DEFAULT_DECELERATION);
    }

    public float getX() {
        return x;
    }

    public PlayerMovementBuilder setX(float x) {
        validateCoordinate(x, "X");
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public PlayerMovementBuilder setY(float y) {
        validateCoordinate(y, "Y");
        this.y = y;
        return this;
    }

    public float getSpeed() {
        return speed;
    }

    public PlayerMovementBuilder setSpeed(float speed) {
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

    public PlayerMovementBuilder setDirection(Direction direction) {
        if (direction == null) {
            LOGGER.log(Level.WARNING, "No direction provided. Defaulting to Direction.NONE.");
            this.direction = Direction.NONE;
        } else {
            this.direction = direction;
        }
        return this;
    }

    public IMovementBehavior getMovementBehavior() {
        return movementBehavior;
    }

    public PlayerMovementBuilder withAcceleratedMovement(float acceleration, float deceleration) {
        validateAccelerationParameters(acceleration, deceleration);
        try {
            this.movementBehavior = new AcceleratedMovementBehavior(acceleration, deceleration, this.speed);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create AcceleratedMovementBehavior", e);
            throw new IllegalStateException("Failed to create AcceleratedMovementBehavior", e);
        }
        return this;
    }

    public PlayerMovementBuilder withConstantMovement() {
        try {
            this.movementBehavior = new ConstantMovementBehavior(this.speed);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create ConstantMovementBehavior", e);
            throw new IllegalStateException("Failed to create ConstantMovementBehavior", e);
        }
        return this;
    }

    public PlayerMovementManager build() {
        validateBuildRequirements();
        try {
            if (movementBehavior == null) {
                LOGGER.log(Level.INFO, "No movement behavior specified. Using default ConstantMovementBehavior.");
                withConstantMovement();
            }
            return new PlayerMovementManager(this);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to build PlayerMovementManager", e);
            throw new IllegalStateException("Failed to build PlayerMovementManager", e);
        }
    }

    // Private validation methods
    private void validateCoordinate(float coordinate, String coordinateName) {
        if (Float.isNaN(coordinate) || Float.isInfinite(coordinate)) {
            String errorMessage = "Invalid " + coordinateName + " coordinate: " + coordinate;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateAccelerationParameters(float acceleration, float deceleration) {
        if (acceleration < 0 || deceleration < 0) {
            String errorMessage = String.format(
                    "Invalid acceleration parameters: acceleration=%f, deceleration=%f. Both must be non-negative.",
                    acceleration, deceleration);
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
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
