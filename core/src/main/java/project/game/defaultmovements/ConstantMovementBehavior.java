package project.game.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.MovementUtils;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;


/**
 * @class ConstantMovementBehavior
 * @brief Moves the entity in a constant direction using MovementData.
 * 
 * This class implements a movement behavior that moves the entity in a constant
 * direction. The speed of the movement can be set in the constructor.
 */
public class ConstantMovementBehavior implements IMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(ConstantMovementBehavior.class.getName());
    private final float speed;

    public ConstantMovementBehavior(float speed) {
        if (speed < 0) {
            String errorMessage = "Negative speed provided in ConstantMovementBehavior: " + speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            System.exit(1);
        }
        this.speed = speed;
    }

    @Override
    public void updatePosition(MovementData data) {
        try {
            float delta = data.getDeltaTime();
            if (delta < 0) {
                String errorMessage = "Negative deltaTime provided in ConstantMovementBehavior.updatePosition: " + delta;
                LOGGER.log(Level.SEVERE, errorMessage);
                System.exit(1);
            }
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
                    // No movement for NONE.
                    break;
                default:
                    LOGGER.log(Level.SEVERE, "Unknown direction in ConstantMovementBehavior.updatePosition: {0}", data.getDirection());
                    System.exit(1);
            }
            data.setX(data.getX() + deltaMovement.x);
            data.setY(data.getY() + deltaMovement.y);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception in ConstantMovementBehavior.updatePosition: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}
