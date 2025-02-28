package project.game.common.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.common.exception.MovementException;
/**
 * Calculates the adjusted speed for an entity moving diagonally to maintain
 * the overall velocity consistency.
 */
public class MovementUtils {

    private static final Logger LOGGER = Logger.getLogger(MovementUtils.class.getName());

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
