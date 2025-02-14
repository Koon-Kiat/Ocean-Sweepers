package project.game.abstractengine.entitysystem.movementmanager;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.Direction;
import project.game.abstractengine.entitysystem.interfaces.IMovementBehavior;
import project.game.abstractengine.entitysystem.interfaces.IMovementManager;
import project.game.exceptions.MovementException;

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
        validateConstructorParameters(speed, behavior);
        this.position = new Vector2(x, y);
        this.speed = speed;
        this.direction = (direction != null) ? direction : Direction.NONE;
        this.movementBehavior = behavior;

    }

    private void validateConstructorParameters(float speed, IMovementBehavior behavior) {
        if (speed < 0) {
            throw new MovementException("Speed cannot be negative: " + speed);
        }
        if (behavior == null) {
            throw new MovementException("Movement behavior cannot be null");
        }
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
            throw new MovementException("Speed must be non-negative.");
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
            throw new MovementException(msg);
        }
        this.movementBehavior = movementBehavior;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void applyMovementUpdate(float dt) {
        if (movementBehavior == null) {
            LOGGER.log(Level.SEVERE, "Cannot update position: movement behavior is not set.");
            return;
        }

        // Build a MovementData object from current fields
        MovementData data;
        try {
            data = new MovementData(getX(), getY(), getSpeed(), getDirection());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create MovementData instance: " + e.getMessage(), e);
            return;
        }

        try {
            movementBehavior.applyMovementBehavior(data, dt);
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
        float dt = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        try {
            applyMovementUpdate(dt);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating movement: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateDirection(Set<Integer> pressedKeys, Map<Integer, Direction> keyBindings) {
        boolean up = false;
        boolean down = false;
        boolean left = false;
        boolean right = false;

        // Iterate over pressed keys and set flags based on the provided bindings
        for (Integer key : pressedKeys) {
            Direction mapped = keyBindings.get(key);
            if (mapped != null) {
                switch (mapped) {
                    case UP:
                        up = true;
                        break;
                    case DOWN:
                        down = true;
                        break;
                    case LEFT:
                        left = true;
                        break;
                    case RIGHT:
                        right = true;
                        break;
                    default:
                        break;
                }
            }
        }

        // Handle vertical direction
        if (up && down) {
            up = false;
            down = false;
        }

        // Handle horizontal direction
        if (left && right) {
            left = false;
            right = false;
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
    }

}
