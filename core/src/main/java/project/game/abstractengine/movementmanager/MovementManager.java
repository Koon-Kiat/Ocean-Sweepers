package project.game.abstractengine.movementmanager;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

import project.game.Direction;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.abstractengine.movementmanager.interfaces.IMovementManager;
import project.game.defaultmovements.AcceleratedMovementBehavior;

/**
 * @abstractclass MovementManager
 * @brief Manages the movement logic for game entities.
 *
 *        MovementManager serves as the base class for different types of
 *        movement
 *        managers, handling common properties such as position, speed,
 *        direction, and
 *        the associated movement behavior. It provides methods to update
 *        positions,
 *        and control movement states.
 */
public abstract class MovementManager implements IMovementManager {

    private static final Logger LOGGER = Logger.getLogger(MovementManager.class.getName());
    private final Vector2 position;
    private float speed;
    private Direction direction;
    private IMovementBehavior movementBehavior;
    private float deltaTime;

    /**
     * Constructs a MovementManager with the specified parameters.
     *
     * @param x         Initial x-coordinate.
     * @param y         Initial y-coordinate.
     * @param speed     Movement speed.
     * @param direction Initial movement direction.
     * @param behavior  Movement behavior strategy.
     */
    public MovementManager(float x, float y, float speed, Direction direction, IMovementBehavior behavior) {
        if (speed < 0) {
            String msg = "Speed cannot be negative.";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }
        if (behavior == null) {
            String msg = "Movement behavior cannot be null.";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }

        this.position = new Vector2(x, y);
        this.speed = speed;
        this.direction = (direction != null) ? direction : Direction.NONE;
        this.movementBehavior = behavior;
    }

    @Override
    public float getX() {
        return position.x;
    }

    public void setX(float x) {
        this.position.x = x;
    }

    @Override
    public float getY() {
        return position.y;
    }

    public void setY(float y) {
        this.position.y = y;
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
            LOGGER.log(Level.WARNING, "Null direction provided. Defaulting to Direction.NONE.");
            this.direction = Direction.NONE;
        }
        this.direction = direction;

    }

    public void setMovementBehavior(IMovementBehavior movementBehavior) {
        if (movementBehavior == null) {
            String msg = "Movement behavior cannot be null.";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }
        this.movementBehavior = movementBehavior;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    @Override
    public void setDeltaTime(float deltaTime) {
        if (deltaTime < 0) {
            String errorMessage = "Negative deltaTime provided in updatePosition: " + deltaTime;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.deltaTime = deltaTime;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void applyMovementUpdate() {
        if (movementBehavior == null) {
            LOGGER.log(Level.SEVERE, "Cannot update position: movement behavior is not set.");
            return;
        }

        // Build a MovementData object from current fields
        MovementData data;
        try {
            data = new MovementData(getX(), getY(), getSpeed(), getDeltaTime(), getDirection());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create MovementData instance: " + e.getMessage(), e);
            return;
        }

        try {
            movementBehavior.applyMovementBehavior(data);
        } catch (Exception e) {
            String errorMessage = "Error during movement behavior update: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            setDirection(Direction.NONE);
            return;
        }

        // Update our MovementManager with the possibly updated data
        try {
            setX(data.getX());
            setY(data.getY());
            setSpeed(data.getSpeed());
            setDirection(data.getDirection());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating MovementManager fields from MovementData: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void updateMovement() {
        try {
            applyMovementUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating movement: " + e.getMessage(), e);
        }
    }

    public void stop() {
        try {
            setDirection(Direction.NONE);
            if (movementBehavior instanceof AcceleratedMovementBehavior) {
                ((AcceleratedMovementBehavior) movementBehavior).stopMovement(this);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during stop(): " + e.getMessage(), e);
            throw e;
        }
    }

    public void resume() {
        try {
            if (movementBehavior instanceof AcceleratedMovementBehavior) {
                ((AcceleratedMovementBehavior) movementBehavior).resumeMovement(this);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during resume(): " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void updateDirection(Set<Integer> pressedKeys) {
        if (pressedKeys == null) {
            LOGGER.log(Level.WARNING, "Pressed keys set is null. No direction update performed.");
            return;
        }

        try {
            boolean up = pressedKeys.contains(Input.Keys.W);
            boolean down = pressedKeys.contains(Input.Keys.S);
            boolean left = pressedKeys.contains(Input.Keys.A);
            boolean right = pressedKeys.contains(Input.Keys.D);

            // Resolve conflicts if opposite directions are pressed.
            if (up && down) {
                up = down = false;
            }
            if (left && right) {
                left = right = false;
            }

            Direction newDirection = Direction.NONE;
            if (up && right) {
                newDirection = Direction.UP_RIGHT;
            } else if (up && left) {
                newDirection = Direction.UP_LEFT;
            } else if (down && right) {
                newDirection = Direction.DOWN_RIGHT;
            } else if (down && left) {
                newDirection = Direction.DOWN_LEFT;
            } else if (up) {
                newDirection = Direction.UP;
            } else if (down) {
                newDirection = Direction.DOWN;
            } else if (left) {
                newDirection = Direction.LEFT;
            } else if (right) {
                newDirection = Direction.RIGHT;
            }

            setDirection(newDirection);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing input keys for direction update: " + e.getMessage(), e);
            throw e;
        }
    }



}
