package project.game.abstractengine.entitysystem.movementmanager;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.entitymanager.MovableEntity;
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
public abstract class MovementManager extends MovableEntity implements IMovementManager {

    private static final Logger LOGGER = Logger.getLogger(MovementManager.class.getName());
    private IMovementBehavior movementBehavior;

    /**
     * Constructs a MovementManager with the specified parameters.
     *
     * @param speed     Movement speed.
     * @param direction Initial movement direction.
     * @param behavior  Movement behavior strategy.
     */
    public MovementManager(Entity entity, float speed, Direction direction, IMovementBehavior behavior) {
        super(entity, speed);
        validateConstructorParameters(speed, behavior);
        setDirection((direction != null) ? direction : Direction.NONE);
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

    public IMovementBehavior getMovementBehavior() {
        return movementBehavior;
    }

    public void setMovementBehavior(IMovementBehavior movementBehavior) {
        if (movementBehavior == null) {
            String msg = "Movement behavior cannot be null.";
            LOGGER.log(Level.SEVERE, msg);
            throw new MovementException(msg);
        }
        this.movementBehavior = movementBehavior;
    }

    public void applyMovementUpdate(float dt) {
        if (movementBehavior == null) {
            LOGGER.log(Level.SEVERE, "Cannot update position: movement behavior is not set.");
            return;
        }

        try {
            movementBehavior.applyMovementBehavior(this, dt);
        } catch (Exception e) {
            String errorMessage = "Error during movement behavior update: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            setDirection(Direction.NONE);
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
