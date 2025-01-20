package project.game.movement;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.Main;

/**
 * @abstractclass MovementManager
 * @brief Manages the movement logic for game entities.
 *
 * MovementManager serves as the base class for different types of movement managers,
 * handling common properties such as position, speed, direction, and the associated
 * movement behavior. It provides methods to update positions, clamp positions within
 * game boundaries, and control movement states.
 */
public abstract class MovementManager {

    private Vector2 position;
    private float speed;
    private Direction direction;
    private IMovementBehavior movementBehavior;
    private float deltaTime;
    
    /**
     * Constructs a MovementManager with the specified parameters.
     *
     * @param x Initial x-coordinate.
     * @param y Initial y-coordinate.
     * @param speed Movement speed.
     * @param direction Initial movement direction.
     * @param behavior Movement behavior strategy.
     */
    public MovementManager(float x, float y, float speed, Direction direction, IMovementBehavior behavior) {
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.direction = direction;
        this.movementBehavior = behavior;
    }

    // Getters and Setters
   
    /**
     * Gets the current x-coordinate.
     *
     * @return Current x-coordinate.
     */
    public float getX() {
        return position.x;
    }

    /**
     * Sets the x-coordinate.
     *
     * @param x New x-coordinate.
     */
    public void setX(float x) {
        this.position.x = x;
    }

    /**
     * Gets the current y-coordinate.
     *
     * @return Current y-coordinate.
     */
    public float getY() {
        return position.y;
    }

    /**
     * Sets the y-coordinate.
     *
     * @param y New y-coordinate.
     */
    public void setY(float y) {
        this.position.y = y;
    }

    /**
     * Gets the movement speed.
     *
     * @return Movement speed.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets the movement speed.
     *
     * @param speed New movement speed.
     * @throws IllegalArgumentException if speed is negative.
     */
    public void setSpeed(float speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed cannot be negative.");
        }
        this.speed = speed;
    }

    /**
     * Gets the current movement direction.
     *
     * @return Current direction.
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the movement direction.
     *
     * @param direction New movement direction.
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Sets the movement behavior strategy.
     *
     * @param movementBehavior New movement behavior.
     */
    public void setMovementBehavior(IMovementBehavior movementBehavior) {
        this.movementBehavior = movementBehavior;
    }

    /**
     * Gets the elapsed delta time.
     *
     * @return Delta time since the last frame.
     */
    public float getDeltaTime() {
        return deltaTime;
    }

    /**
     * Sets the delta time.
     *
     * @param deltaTime Elapsed delta time since the last frame.
     */
    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }

    /**
     * Gets the current position vector.
     *
     * @return Current position.
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Updates the position by delegating to the movement behavior.
     */
    public void updatePosition() {
        if (movementBehavior != null) {
            movementBehavior.updatePosition(this);
        }
    }

    /**
     * Stops movement by setting direction to NONE and resetting speed if using accelerated movement.
     */
    public void stop() {
        setDirection(Direction.NONE);
        if (movementBehavior instanceof AcceleratedMovementBehavior) {
            ((AcceleratedMovementBehavior) movementBehavior).stopMovement(this);
        }
    }

    /**
     * Resumes movement by restoring the last direction if using accelerated movement.
     */
    public void resume() {
        if (movementBehavior instanceof AcceleratedMovementBehavior) {
            ((AcceleratedMovementBehavior) movementBehavior).resumeMovement(this);
        }
    }

    /**
     * Clamps the position within the game boundaries.
     */
    public void clampPosition() {
        MovementUtils.clampPosition(this.position);
    }
}