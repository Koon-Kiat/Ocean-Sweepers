package project.game.context.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.abstractengine.entitysystem.entitymanager.MovableEntity;
import project.game.abstractengine.entitysystem.movementmanager.MovementManager;
import project.game.abstractengine.interfaces.IMovementBehavior;
import project.game.exceptions.MovementException;
import project.game.utils.MovementUtils;

/**
 * Provides constant movement for movable entities.
 * 
 * The entity moves in a single direction at a constant speed.
 * The speed is provided in the constructor.
 */
public class ConstantMovementBehavior implements IMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(ConstantMovementBehavior.class.getName());
    private final float speed;

    public ConstantMovementBehavior(float speed) {
        if (speed < 0) {
            if (MovementManager.LENIENT_MODE) {
                LOGGER.log(Level.WARNING,
                        "Negative speed provided in ConstantMovementBehavior: {0}. Using absolute value.", speed);
                speed = Math.abs(speed);
            } else {
                String errorMessage = "Negative speed provided in ConstantMovementBehavior: " + speed;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        this.speed = speed;
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            Vector2 deltaMovement = new Vector2();
            switch (entity.getDirection()) {
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
                            + entity.getDirection();
                    LOGGER.log(Level.SEVERE, errorMessage);
                    throw new MovementException(errorMessage);
            }
            entity.setX(entity.getX() + deltaMovement.x);
            entity.setY(entity.getY() + deltaMovement.y);
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
