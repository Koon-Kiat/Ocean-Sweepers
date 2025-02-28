package project.game.context.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.util.MovementUtils;
import project.game.context.core.Direction;
import project.game.engine.api.IStoppableMovementBehavior;
import project.game.engine.entitysystem.entity.MovableEntity;
import project.game.engine.entitysystem.movement.MovementManager;

/**
 * Provides accelerated movement for movable entities.
 */
public class AcceleratedMovementBehavior implements IStoppableMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(AcceleratedMovementBehavior.class.getName());
    private final float acceleration;
    private final float deceleration;
    private final float maxSpeed;
    private float currentSpeed;

    /**
     * Constructs an AcceleratedMovementBehavior with specified parameters.
     * Terminates the program if any provided parameter is negative.
     */
    public AcceleratedMovementBehavior(float acceleration, float deceleration, float maxSpeed) {
        if (acceleration < 0 || deceleration < 0 || maxSpeed < 0) {
            String errorMessage = "Illegal negative values provided: acceleration=" + acceleration +
                    ", deceleration=" + deceleration + ", maxSpeed=" + maxSpeed;
            LOGGER.log(Level.SEVERE, errorMessage);
            if (MovementManager.LENIENT_MODE) {
                this.acceleration = Math.abs(acceleration);
                this.deceleration = Math.abs(deceleration);
                this.maxSpeed = Math.abs(maxSpeed);
                LOGGER.log(Level.WARNING, "LENIENT_MODE enabled: Using absolute values for parameters.");
            } else {
                throw new MovementException(errorMessage);
            }
        } else {
            this.acceleration = acceleration;
            this.deceleration = deceleration;
            this.maxSpeed = maxSpeed;
        }
        this.currentSpeed = 0f;
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            // Clamp delta to prevent excessively large updates.
            deltaTime = Math.min(deltaTime, 1 / 30f);

            if (entity.getDirection() != Direction.NONE) {
                currentSpeed += acceleration * deltaTime;
                if (currentSpeed > maxSpeed) {
                    currentSpeed = maxSpeed;
                }
            } else {
                currentSpeed -= deceleration * deltaTime;
                if (currentSpeed < 0) {
                    currentSpeed = 0;
                }
            }

            Vector2 deltaMovement = new Vector2();
            float diagonalSpeed;

            switch (entity.getDirection()) {
                case UP:
                    deltaMovement.y += currentSpeed * deltaTime;
                    break;
                case DOWN:
                    deltaMovement.y -= currentSpeed * deltaTime;
                    break;
                case LEFT:
                    deltaMovement.x -= currentSpeed * deltaTime;
                    break;
                case RIGHT:
                    deltaMovement.x += currentSpeed * deltaTime;
                    break;
                case UP_LEFT:
                    diagonalSpeed = MovementUtils.calculateDiagonalSpeed(currentSpeed);
                    deltaMovement.x -= diagonalSpeed * deltaTime;
                    deltaMovement.y += diagonalSpeed * deltaTime;
                    break;
                case UP_RIGHT:
                    diagonalSpeed = MovementUtils.calculateDiagonalSpeed(currentSpeed);
                    deltaMovement.x += diagonalSpeed * deltaTime;
                    deltaMovement.y += diagonalSpeed * deltaTime;
                    break;
                case DOWN_LEFT:
                    diagonalSpeed = MovementUtils.calculateDiagonalSpeed(currentSpeed);
                    deltaMovement.x -= diagonalSpeed * deltaTime;
                    deltaMovement.y -= diagonalSpeed * deltaTime;
                    break;
                case DOWN_RIGHT:
                    diagonalSpeed = MovementUtils.calculateDiagonalSpeed(currentSpeed);
                    deltaMovement.x += diagonalSpeed * deltaTime;
                    deltaMovement.y -= diagonalSpeed * deltaTime;
                    break;
                case NONE:
                    break;
                default:
                    String errorMessage = "Unknown movement direction: " + entity.getDirection();
                    LOGGER.log(Level.SEVERE, errorMessage);
                    throw new MovementException(errorMessage);
            }

            float newX = entity.getX() + deltaMovement.x;
            float newY = entity.getY() + deltaMovement.y;
            entity.setX(newX);
            entity.setY(newY);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Exception in AcceleratedMovementBehavior.updatePosition: " + e.getMessage(), e);
            if (MovementManager.LENIENT_MODE) {
                entity.setDirection(Direction.NONE);
            } else {
                throw e;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in AcceleratedMovementBehavior: " + e.getMessage(), e);
            if (MovementManager.LENIENT_MODE) {
                entity.setDirection(Direction.NONE);
            } else {
                throw new MovementException("Error updating position in AcceleratedMovementBehavior", e);
            }
        }
    }

    @Override
    public void stopMovement(MovableEntity entity, float deltaTime) {
        currentSpeed = 0;
        entity.setDirection(Direction.NONE);
    }

    @Override
    public void resumeMovement(MovableEntity entity, float deltaTime) {
    }
}
