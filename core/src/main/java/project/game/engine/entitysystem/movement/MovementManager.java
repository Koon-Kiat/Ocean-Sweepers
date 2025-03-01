package project.game.engine.entitysystem.movement;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import project.game.common.api.ILogger;
import project.game.common.exception.MovementException;
import project.game.common.logging.LogManager;
import project.game.context.core.Direction;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IMovementManager;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * MovementManager is an abstract class that provides basic movement
 * functionality for entities in the game.
 * 
 * It extends MovableEntity and implements IMovementManager.
 */
public abstract class MovementManager extends MovableEntity implements IMovementManager {

    private static final ILogger LOGGER = LogManager.getLogger(MovementManager.class);
    private IMovementBehavior movementBehavior;
    public static boolean LENIENT_MODE = false;

    /**
     * Constructs a MovementManager with the specified parameters.
     */
    public MovementManager(Entity entity, float speed, Direction direction, IMovementBehavior behavior) {
        super(entity, speed);
        // Use lenient mode to set defaults instead of throwing an exception.
        float correctedSpeed = speed;
        if (speed < 0) {
            if (LENIENT_MODE) {
                LOGGER.log(Level.WARNING, "Negative speed provided ({0}). Using absolute value.", speed);
                correctedSpeed = Math.abs(speed);
            } else {
                throw new MovementException("Speed cannot be negative: " + speed);
            }
        }
        super.setSpeed(correctedSpeed);
        if (behavior == null) {
            if (LENIENT_MODE) {
                LOGGER.log(Level.WARNING,
                        "Movement behavior is null. Defaulting to ConstantMovementBehavior with speed 1.0.");
                behavior = project.game.context.factory.MovementBehaviorFactory.createDefaultMovement();
            } else {
                throw new MovementException("Movement behavior cannot be null");
            }
        }
        setDirection((direction != null) ? direction : Direction.NONE);
        this.movementBehavior = behavior;
    }

    public static void setLenientMode(boolean mode) {
        LENIENT_MODE = mode;
        LOGGER.log(Level.INFO, "Lenient mode set to: {0}", mode);
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
