package project.game.movement;

import com.badlogic.gdx.math.Vector2;

/**
 * @class MovementManager
 * @brief Abstract base class for managing entity movement.
 *
 * The MovementManager class provides foundational properties and methods for
 * managing the position, speed, and direction of an entity within the game
 * world. Subclasses must implement the
 * {@code updatePosition()}, {@code stop()}, and {@code resume()} methods to
 * define specific movement behaviors.
 */
public abstract class MovementManager {

    protected Vector2 position;
    protected float speed;
    protected Direction direction;

    /**
     * @brief Constructs a MovementManager with the specified parameters.
     *
     * @param x Initial x-coordinate.
     * @param y Initial y-coordinate.
     * @param speed Movement speed.
     * @param direction Initial movement direction.
     */
    public MovementManager(float x, float y, float speed, Direction direction) {
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.direction = direction;
    }

    // Getters and Setters
    /**
     * @brief Retrieves the current x-coordinate of the entity.
     *
     * @return The x-coordinate.
     */
    public float getX() {
        return position.x;
    }

    /**
     * @brief Sets the x-coordinate of the entity.
     *
     * @param x The new x-coordinate.
     */
    public void setX(float x) {
        this.position.x = x;
    }

    /**
     * @brief Retrieves the current y-coordinate of the entity.
     *
     * @return The y-coordinate.
     */
    public float getY() {
        return position.y;
    }

    /**
     * @brief Sets the y-coordinate of the entity.
     *
     * @param y The new y-coordinate.
     */
    public void setY(float y) {
        this.position.y = y;
    }

    /**
     * @brief Retrieves the movement speed of the entity.
     *
     * @return The speed in units per second.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * @brief Sets the movement speed of the entity.
     *
     * @param speed The new speed in units per second.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * @brief Retrieves the current movement direction of the entity.
     *
     * @return The movement direction.
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * @brief Sets the movement direction of the entity.
     *
     * @param direction The new movement direction.
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * @brief Updates the entity's position based on speed and direction.
     *
     * This method should be called each frame to ensure the entity moves
     * smoothly and consistently across devices with varying frame rates.
     */
    public abstract void updatePosition();

    /**
     * @brief Stops the entity's movement.
     *
     * Implementing classes should define how the movement is halted, typically
     * by setting the direction to {@code Direction.NONE}.
     */
    public abstract void stop();

    /**
     * @brief Resumes the entity's movement.
     *
     * Implementing classes should define how the movement is resumed,
     * potentially restoring the previous direction or state before stopping.
     */
    public abstract void resume();

}
