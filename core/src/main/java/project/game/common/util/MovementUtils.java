package project.game.common.util;

import project.game.common.exception.MovementException;
import project.game.common.logging.context.ContextualLoggerFactory;
import project.game.common.logging.context.LogMessageContext;
import project.game.common.logging.core.GameLogger;
import project.game.common.logging.core.LogLevel;

/**
 * Calculates the adjusted speed for an entity moving diagonally to maintain
 * the overall velocity consistency.
 */
public class MovementUtils {

    // Using the new GameLogger instead of ILogger
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

            // Using the new error method from GameLogger
            LOGGER.error(errorMessage);

            // Create contextual error details
            LogMessageContext errorContext = ContextualLoggerFactory.createContext("MovementCalculation")
                    .with("invalidSpeed", speed)
                    .with("calculationType", "diagonal")
                    .build();

            // Log with additional context
            LOGGER.getContextualLogger().log(
                    LogLevel.ERROR.getJavaLevel(),
                    errorContext,
                    "Invalid movement parameter detected");

            throw new MovementException(errorMessage);
        }

        try {
            float adjustedSpeed = speed / (float) Math.sqrt(2);

            // Move detailed calculation logging to TRACE level
            LOGGER.trace("Calculated diagonal speed: {0} (from base speed: {1})", adjustedSpeed, speed);

            return adjustedSpeed;
        } catch (Exception e) {
            String errorMessage = "Error calculating diagonal speed: " + e.getMessage();

            // Using error method with exception
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
