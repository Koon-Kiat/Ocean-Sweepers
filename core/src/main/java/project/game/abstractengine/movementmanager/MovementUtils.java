package project.game.abstractengine.movementmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.exceptions.MovementException;

/**
 * @class MovementUtils
 * @brief Provides utility methods for movement calculations.
 *
 *        This class provides a static method to calculate the diagonal speed
 *        based on the current speed. This is useful for maintaining consistent
 *        overall speed when moving diagonally.
 */
public class MovementUtils {

    private static final Logger LOGGER = Logger.getLogger(MovementUtils.class.getName());

    /**
     * Calculates the diagonal speed to maintain consistent overall speed.
     *
     * @param speed The current speed.
     * @return The diagonal speed.
     */
    public static float calculateDiagonalSpeed(float speed) {
        if (speed < 0) {
            String errorMessage = "Speed cannot be negative in calculateDiagonalSpeed.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        try {
            return speed / (float) Math.sqrt(2);
        } catch (Exception e) {
            String errorMessage = "Error calculating diagonal speed: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new MovementException("Error calculating diagonal speed", e);
        }
    }
}
