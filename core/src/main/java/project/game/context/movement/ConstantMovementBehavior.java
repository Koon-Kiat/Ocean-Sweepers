package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.common.util.MovementUtils;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.entitysystem.entity.MovableEntity;
import project.game.engine.entitysystem.movement.MovementManager;

/**
 * Provides constant movement for movable entities.
 * 
 * The entity moves in a single direction at a constant speed.
 * The speed is provided in the constructor.
 */
public class ConstantMovementBehavior implements IMovementBehavior {

    private static final GameLogger LOGGER = new GameLogger(ConstantMovementBehavior.class);
    private final float speed;

    public ConstantMovementBehavior(float speed) {
        if (speed < 0) {
            if (MovementManager.LENIENT_MODE) {
                LOGGER.warn("Negative speed provided in ConstantMovementBehavior: {0}. Using absolute value.", speed);
                speed = Math.abs(speed);
            } else {
                String errorMessage = "Negative speed provided in ConstantMovementBehavior: " + speed;
                LOGGER.error(errorMessage);
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
                    LOGGER.error(errorMessage);
                    throw new MovementException(errorMessage);
            }
            entity.setX(entity.getX() + deltaMovement.x);
            entity.setY(entity.getY() + deltaMovement.y);
        } catch (MovementException e) {
            LOGGER.error("Illegal argument in ConstantMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.fatal("Unexpected exception in ConstantMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new MovementException("Error updating position in ConstantMovementBehavior", e);
        }
    }
}
