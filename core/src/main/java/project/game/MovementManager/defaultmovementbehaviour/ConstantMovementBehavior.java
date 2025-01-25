package project.game.MovementManager.defaultmovementbehaviour;

import com.badlogic.gdx.math.Vector2;
import project.game.MovementManager.interfaces.IMovementBehavior;
import project.game.MovementManager.MovementManager;
import project.game.MovementManager.MovementUtils;


/**
 * @class ConstantMovementBehavior
 * @brief Implements a movement behavior with constant speed.
 *
 * Entities using this behavior will move consistently in their set direction
 * without any acceleration or deceleration. Diagonal movements are handled by
 * scaling the movement on each axis to maintain consistent overall speed.
 */
public class ConstantMovementBehavior implements IMovementBehavior {

    private final float speed;

    public ConstantMovementBehavior(float speed) {
        this.speed = speed;
    }

    /**
     * Updates the position of the MovementManager based on the current
     * direction and speed.
     *
     * @param manager The MovementManager whose position is to be updated.
     */
    @Override
    public void updatePosition(MovementManager manager) {
        float delta = manager.getDeltaTime();
        Vector2 deltaMovement = new Vector2();

        switch (manager.getDirection()) {
            case UP:
                deltaMovement.y += speed * delta;
                break;
            case DOWN:
                deltaMovement.y -= speed * delta;
                break;
            case LEFT:
                deltaMovement.x -= speed * delta;
                break;
            case RIGHT:
                deltaMovement.x += speed * delta;
                break;
            case UP_LEFT:
                deltaMovement.x -= MovementUtils.calculateDiagonalSpeed(speed) * delta;
                deltaMovement.y += MovementUtils.calculateDiagonalSpeed(speed) * delta;
                break;
            case UP_RIGHT:
                deltaMovement.x += MovementUtils.calculateDiagonalSpeed(speed) * delta;
                deltaMovement.y += MovementUtils.calculateDiagonalSpeed(speed) * delta;
                break;
            case DOWN_LEFT:
                deltaMovement.x -= MovementUtils.calculateDiagonalSpeed(speed) * delta;
                deltaMovement.y -= MovementUtils.calculateDiagonalSpeed(speed) * delta;
                break;
            case DOWN_RIGHT:
                deltaMovement.x += MovementUtils.calculateDiagonalSpeed(speed) * delta;
                deltaMovement.y -= MovementUtils.calculateDiagonalSpeed(speed) * delta;
                break;
            case NONE:
                // No movement
                break;
        }

        // Update the position vector with the calculated delta movement
        manager.getPosition().add(deltaMovement);

        // Ensure the entity remains within game boundaries
        manager.clampPosition();
    }
}
