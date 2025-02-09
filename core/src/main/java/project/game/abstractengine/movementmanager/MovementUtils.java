package project.game.abstractengine.movementmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

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
            IllegalArgumentException ex = new IllegalArgumentException("Speed cannot be negative in calculateDiagonalSpeed.");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw ex;
        }
        try {
            return speed / (float) Math.sqrt(2);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating diagonal speed: " + e.getMessage(), e);
            throw e;
        }
    }
}
