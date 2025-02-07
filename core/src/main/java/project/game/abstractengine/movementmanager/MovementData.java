package project.game.abstractengine.movementmanager;

import project.game.Direction;

/**
 * @class MovementData
 * @brief Holds minimal data needed for movement updates.
 *
 * This class decouples movement behavior from MovementManager, preventing
 * direct dependency on that concrete class.
 */
public class MovementData {

    private float x;
    private float y;
    private float speed;
    private float deltaTime;
    private Direction direction;

    public MovementData(float x, float y, float speed, float deltaTime, Direction direction) {
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
        this.speed = speed;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
