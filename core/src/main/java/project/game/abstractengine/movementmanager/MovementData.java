package project.game.abstractengine.movementmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;

/**
 * @class MovementData
 * @brief Holds minimal data needed for movement updates.
 *
 *        This class decouples movement behavior from MovementManager,
 *        preventing
 *        direct dependency on that concrete class.
 */
public class MovementData {

    private static final Logger LOGGER = Logger.getLogger(MovementData.class.getName());
    private float x;
    private float y;
    private float speed;
    private float deltaTime;
    private Direction direction;

    /**
     * Constructs a MovementData object with the specified parameters.
     *
     * @param x         Initial x-coordinate.
     * @param y         Initial y-coordinate.
     * @param speed     Movement speed.
     * @param deltaTime Time elapsed since last update.
     * @param direction Movement direction.
     */
    public MovementData(float x, float y, float speed, float deltaTime, Direction direction) {
        if (speed < 0) {
            String errorMessage = "Speed cannot be negative.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (deltaTime < 0) {
            String errorMessage = "DeltaTime cannot be negative.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (direction == null) {
            String errorMessage = "Direction cannot be null.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.deltaTime = deltaTime;
        this.direction = direction;

    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if (speed < 0) {
            String errorMessage = "Negative speed provided: " + speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException("Speed must be non-negative.");
        }
        this.speed = speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (direction == null) {
            String errorMessage = "Direction cannot be null.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.direction = direction;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(float deltaTime) {
        if (deltaTime < 0) {
            String errorMessage = "Negative deltaTime provided in updatePosition: " + deltaTime;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.deltaTime = deltaTime;
    }

}
