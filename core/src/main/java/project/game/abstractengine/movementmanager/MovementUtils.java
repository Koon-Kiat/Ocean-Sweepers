package project.game.abstractengine.movementmanager;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.Main;

public class MovementUtils {

    /**
     * Clamps the position within the game boundaries.
     *
     * @param position The position vector to clamp.
     */
    public static void clampPosition(Vector2 position) {
        position.x = MathUtils.clamp(position.x, 0, Main.GAME_WIDTH);
        position.y = MathUtils.clamp(position.y, 0, Main.GAME_HEIGHT);
    }

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
