package project.game.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.abstractengine.entity.movementmanager.MovementData;
import project.game.abstractengine.entity.movementmanager.MovementUtils;
import project.game.abstractengine.entity.movementmanager.interfaces.IMovementBehavior;
import project.game.exceptions.MovementException;

/**
 * @class ConstantMovementBehavior
 * @brief Moves the entity in a constant direction using MovementData.
 * 
 *        This class implements a movement behavior that moves the entity in a
 *        constant
 *        direction. The speed of the movement can be set in the constructor.
 */
public class ConstantMovementBehavior implements IMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(ConstantMovementBehavior.class.getName());
    private final float speed;

    /**
     * Constructs a ConstantMovementBehavior with the specified speed. Terminates
     * the program if a negative speed is provided.
     * 
     * @param speed Speed of the movement.
     */
    public ConstantMovementBehavior(float speed) {
        if (speed < 0) {
            String errorMessage = "Negative speed provided in ConstantMovementBehavior: " + speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        this.speed = speed;
    }

    @Override
    public void applyMovementBehavior(MovementData data, float deltaTime) {
        try {
            Vector2 deltaMovement = new Vector2();
            switch (data.getDirection()) {
                case UP:
                    deltaMovement.y += speed * deltaTime;
                    break;
                case DOWN:
                    deltaMovement.y -= speed * deltaTime;
                    break;
                case LEFT:
                    deltaMovement.x -= speed * deltaTime;
                    break;
                case RIGHT:
                    deltaMovement.x += speed * deltaTime;
                    break;
                case UP_LEFT:
                    deltaMovement.x -= MovementUtils.calculateDiagonalSpeed(speed) * deltaTime;
                    deltaMovement.y += MovementUtils.calculateDiagonalSpeed(speed) * deltaTime;
                    break;
                case UP_RIGHT:
                    deltaMovement.x += MovementUtils.calculateDiagonalSpeed(speed) * deltaTime;
                    deltaMovement.y += MovementUtils.calculateDiagonalSpeed(speed) * deltaTime;
                    break;
                case DOWN_LEFT:
                    deltaMovement.x -= MovementUtils.calculateDiagonalSpeed(speed) * deltaTime;
                    deltaMovement.y -= MovementUtils.calculateDiagonalSpeed(speed) * deltaTime;
                    break;
                case DOWN_RIGHT:
                    deltaMovement.x += MovementUtils.calculateDiagonalSpeed(speed) * deltaTime;
                    deltaMovement.y -= MovementUtils.calculateDiagonalSpeed(speed) * deltaTime;
                    break;
                case NONE:
                    break;
                default:
                    String errorMessage = "Unknown direction in ConstantMovementBehavior.updatePosition: "
                            + data.getDirection();
                    LOGGER.log(Level.SEVERE, errorMessage);
                    throw new MovementException(errorMessage);
            }
            data.setX(data.getX() + deltaMovement.x);
            data.setY(data.getY() + deltaMovement.y);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Illegal argument in ConstantMovementBehavior.updatePosition: " + e.getMessage(),
                    e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Unexpected exception in ConstantMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new MovementException("Error updating position in ConstantMovementBehavior", e);
        }
    }
}
