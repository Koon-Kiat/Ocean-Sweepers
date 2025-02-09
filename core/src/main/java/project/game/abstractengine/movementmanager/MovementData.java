package project.game.abstractengine.movementmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;

/**
 * @class MovementData
 * @brief Holds minimal data needed for movement updates.
 *
 * This class decouples movement behavior from MovementManager, preventing
 * direct dependency on that concrete class.
 */
public class MovementData {

    private static final Logger LOGGER = Logger.getLogger(MovementData.class.getName());

    private float x;
    private float y;
    private float speed;
    private float deltaTime;
    private Direction direction;

    public MovementData(float x, float y, float speed, float deltaTime, Direction direction) {
        try {
            if (speed < 0) {
                throw new IllegalArgumentException("Speed cannot be negative.");
            }
            if (deltaTime < 0) {
                throw new IllegalArgumentException("DeltaTime cannot be negative.");
            }
            if (direction == null) {
                throw new IllegalArgumentException("Direction cannot be null.");
            }
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.deltaTime = deltaTime;
            this.direction = direction;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Illegal argument in MovementData constructor: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error constructing MovementData: " + e.getMessage(), e);
            throw e;
        }
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        try {
            this.x = x;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting x coordinate in MovementData: " + e.getMessage(), e);
        }
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        try {
            this.y = y;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting y coordinate in MovementData: " + e.getMessage(), e);
        }
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if (speed < 0) {
            IllegalArgumentException ex = new IllegalArgumentException("Speed cannot be negative.");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw ex;
        }
        this.speed = speed;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(float deltaTime) {
        if (deltaTime < 0) {
            IllegalArgumentException ex = new IllegalArgumentException("DeltaTime cannot be negative.");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw ex;
        }
        this.deltaTime = deltaTime;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (direction == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Direction cannot be null.");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw ex;
        }
        this.direction = direction;
    }
}
