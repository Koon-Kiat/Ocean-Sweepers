package project.game.common.util;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;

/**
 * Calculates the adjusted speed for an entity moving diagonally to maintain
 * the overall velocity consistency.
 */
public class MovementUtils {

    private static final GameLogger LOGGER = new GameLogger(MovementUtils.class);

    /**
     * Calculates the adjusted speed for diagonal movement.
     * 
     * @param speed the base speed
     * @return the adjusted speed for diagonal movement
     * @throws MovementException if there's an error in calculation
     */
    public static float calculateDiagonalSpeed(float speed) {
        if (speed < 0) {
            String errorMessage = "Speed cannot be negative in calculateDiagonalSpeed.";
            LOGGER.error(errorMessage);
            throw new MovementException(errorMessage);
        }

        try {
            float adjustedSpeed = speed / (float) Math.sqrt(2);
            LOGGER.trace("Calculated diagonal speed: {0} (from base speed: {1})", adjustedSpeed, speed);
            return adjustedSpeed;
        } catch (Exception e) {
            String errorMessage = "Error calculating diagonal speed: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            throw new MovementException("Error calculating diagonal speed", e);
        }
    }

    /**
     * Validates if a movement speed is within acceptable bounds.
     * 
     * @param speed the speed to validate
     * @return true if the speed is valid
     */
    public static boolean isValidSpeed(float speed) {
        boolean isValid = speed >= 0 && speed <= 1000;

        if (!isValid) {
            LOGGER.warn("Speed value outside of recommended range: {0}", speed);
        }

        return isValid;
    }
}
