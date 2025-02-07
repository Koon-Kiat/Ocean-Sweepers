package project.game.defaultmovements;

import com.badlogic.gdx.math.Vector2;

import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.MovementUtils;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;


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
     * Updates the position using MovementData to move at a constant speed.
     * 
     * @param data The MovementData containing the position, direction, and delta time.
     */
    @Override
    public void updatePosition(MovementData data) {
        float delta = data.getDeltaTime();
        Vector2 deltaMovement = new Vector2();

        switch (data.getDirection()) {
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

        // Update position
        data.setX(data.getX() + deltaMovement.x);
        data.setY(data.getY() + deltaMovement.y);
    }
}
