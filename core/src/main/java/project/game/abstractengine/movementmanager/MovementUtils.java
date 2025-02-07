package project.game.abstractengine.movementmanager;

public class MovementUtils {
    /**
     * Calculates the diagonal speed to maintain consistent overall speed.
     *
     * @param speed The current speed.
     * @return The diagonal speed.
     */
    public static float calculateDiagonalSpeed(float speed) {
        return speed / (float) Math.sqrt(2);
    }
}
