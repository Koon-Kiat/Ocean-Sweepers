package project.game.engine.entitysystem.movement.strategy;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IMovementStrategy;

/**
 * Abstract base class for movement strategies. Provides common functionality
 * for validation, error handling, and movement operations.
 */
public abstract class AbstractMovementStrategy implements IMovementStrategy {

    protected final GameLogger logger;
    protected final boolean lenientMode;

    /**
     * Constructor for AbstractMovementStrategy
     * 
     * @param clazz       The class of the concrete strategy (for logging)
     * @param lenientMode Whether to use lenient mode for error handling
     */
    protected AbstractMovementStrategy(Class<?> clazz, boolean lenientMode) {
        this.logger = new GameLogger(clazz);
        this.lenientMode = lenientMode;
    }

    /**
     * Main movement method required by IMovementStrategy
     * 
     * @param movable   The movable entity
     * @param deltaTime The time delta
     */
    @Override
    public abstract void move(IMovable movable, float deltaTime);

    /**
     * Validates that a target is not null
     * 
     * @param target    The target to validate
     * @param paramName The parameter name for error messages
     * @throws MovementException if target is null and not in lenient mode
     * @return true if the target is valid
     */
    protected boolean validateTarget(Object target, String paramName) {
        if (target == null) {
            String errorMessage = paramName + " cannot be null in " + getClass().getSimpleName() + ".";
            logger.error(errorMessage);
            if (!lenientMode) {
                throw new MovementException(errorMessage);
            }
            return false;
        }
        return true;
    }

    /**
     * Validates that a speed value is positive
     * 
     * @param speed        The speed to validate
     * @param defaultValue Default value to use in lenient mode
     * @return The validated speed (original or default)
     * @throws MovementException if speed is invalid and not in lenient mode
     */
    protected float validateSpeed(float speed, float defaultValue) {
        if (speed <= 0) {
            String errorMessage = "Speed must be positive. Got: " + speed;
            if (lenientMode) {
                logger.warn(errorMessage + " Using default value of " + defaultValue + ".");
                return defaultValue;
            } else {
                logger.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        return speed;
    }

    /**
     * Validates that a value is non-negative
     * 
     * @param value        The value to validate
     * @param paramName    The parameter name for error messages
     * @param defaultValue Default value to use in lenient mode
     * @return The validated value (original or default)
     * @throws MovementException if value is invalid and not in lenient mode
     */
    protected float validateNonNegative(float value, String paramName, float defaultValue) {
        if (value < 0) {
            String errorMessage = paramName + " must be non-negative. Got: " + value;
            if (lenientMode) {
                logger.warn(errorMessage + " Using default value of " + defaultValue + ".");
                return defaultValue;
            } else {
                logger.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        return value;
    }

    /**
     * Handles exceptions during movement updates
     * 
     * @param e            The exception to handle
     * @param errorMessage The error message to log
     * @throws MovementException if not in lenient mode
     */
    protected void handleMovementException(Exception e, String errorMessage) {
        logger.error(errorMessage, e);
        if (!lenientMode) {
            if (e instanceof MovementException) {
                throw (MovementException) e;
            } else {
                throw new MovementException(errorMessage, e);
            }
        }
    }

    /**
     * Safe method to get velocity, ensuring a non-null vector is returned
     * 
     * @param movable The movable entity
     * @return The velocity vector (never null)
     */
    protected Vector2 getSafeVelocity(IMovable movable) {
        Vector2 velocity = movable.getVelocity();
        return (velocity != null) ? velocity : new Vector2(0, 0);
    }

    /**
     * Updates the entity's position based on a movement vector
     * 
     * @param movable    The entity to move
     * @param moveVector The movement vector (scaled by deltaTime)
     */
    protected void applyMovement(IMovable movable, Vector2 moveVector) {
        movable.setX(movable.getX() + moveVector.x);
        movable.setY(movable.getY() + moveVector.y);
    }

    /**
     * Updates the entity's velocity for animations
     * 
     * @param movable    The entity to update
     * @param moveVector The movement vector
     * @param deltaTime  The time delta
     */
    protected void updateVelocity(IMovable movable, Vector2 moveVector, float deltaTime) {
        if (deltaTime > 0) {
            movable.setVelocity(moveVector.x / deltaTime, moveVector.y / deltaTime);
        } else {
            movable.setVelocity(moveVector);
        }
    }

    /**
     * Validates min/max range values
     * 
     * @param min          The minimum value
     * @param max          The maximum value
     * @param minParamName The min parameter name for error messages
     * @param maxParamName The max parameter name for error messages
     * @param defaultMin   Default min value to use in lenient mode
     * @param defaultMax   Default max value to use in lenient mode
     * @return A float array with [validMin, validMax]
     * @throws MovementException if range is invalid and not in lenient mode
     */
    protected float[] validateRange(float min, float max, String minParamName, String maxParamName,
            float defaultMin, float defaultMax) {
        float validMin = min;
        float validMax = max;

        if (min <= 0 || max <= 0) {
            String errorMessage = "Invalid " + minParamName + "/" + maxParamName + " range: " +
                    minParamName + "=" + min + ", " + maxParamName + "=" + max;
            if (lenientMode) {
                logger.warn(errorMessage + " Using fallback values: " + minParamName + "=" +
                        defaultMin + ", " + maxParamName + "=" + defaultMax + ".");
                validMin = defaultMin;
                validMax = defaultMax;
            } else {
                logger.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else if (min > max) {
            if (lenientMode) {
                logger.warn(
                        "Invalid range: " + minParamName + " ({0}) is greater than " + maxParamName
                                + " ({1}). Swapping values.",
                        new Object[] { min, max });
                validMin = max;
                validMax = min;
            } else {
                String errorMessage = "Invalid range: " + minParamName + " (" + min
                        + ") is greater than " + maxParamName + " (" + max + ")";
                logger.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        return new float[] { validMin, validMax };
    }
}