package project.game.abstractengine.movementmanager;

import java.util.logging.Logger;

import project.game.Direction;
import project.game.exceptions.MovementException;

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
    private Direction direction;

    /**
     * Constructs a MovementData object with the specified parameters.
     *
     * @param x         Initial x-coordinate.
     * @param y         Initial y-coordinate.
     * @param speed     Movement speed.
     * @param direction Movement direction.
     */
    public MovementData(float x, float y, float speed, Direction direction) {
        if (speed < 0) {
            throw new MovementException("Speed cannot be negative: " + speed);
        }
        if (direction == null) {
            throw new MovementException("Direction cannot be null");
        }
        this.x = x;
        this.y = y;
        this.speed = speed;
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
        this.speed = speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
