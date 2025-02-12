package project.game.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.Direction;
import project.game.abstractengine.entity.movementmanager.MovementData;
import project.game.abstractengine.entity.movementmanager.MovementUtils;
import project.game.abstractengine.entity.movementmanager.interfaces.IStoppableMovementBehavior;
import project.game.exceptions.MovementException;

/**
 * @class AcceleratedMovementBehavior
 * 
 * @brief Moves the entity with acceleration and deceleration using
 *        MovementData.
 * 
 *        Moves the entity with acceleration and deceleration using
 *        MovementData.
 * 
 *        This class implements a movement behavior that moves the entity with
 *        acceleration and deceleration. The acceleration, deceleration, and
 *        maximum
 *        speed can be set in the constructor. The entity accelerates when
 *        moving in a
 *        direction and decelerates when stopping or changing direction.
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
     *
     * @param acceleration Rate of acceleration.
     * @param deceleration Rate of deceleration.
     * @param maxSpeed     Maximum achievable speed.
     */
    public AcceleratedMovementBehavior(float acceleration, float deceleration, float maxSpeed) {
        if (acceleration < 0 || deceleration < 0 || maxSpeed < 0) {
            String errorMessage = "Illegal negative values provided: acceleration="
                    + acceleration + ", deceleration=" + deceleration + ", maxSpeed=" + maxSpeed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        this.acceleration = acceleration;
        this.deceleration = deceleration;
        this.maxSpeed = maxSpeed;
        this.currentSpeed = 0f;
    }

    @Override
    public void applyMovementBehavior(MovementData data, float deltaTime) {
        try {
            // Clamp delta to prevent excessively large updates.
            deltaTime = Math.min(deltaTime, 1 / 30f);

            if (data.getDirection() != Direction.NONE) {
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

            switch (data.getDirection()) {
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
                    String errorMessage = "Unknown movement direction: " + data.getDirection();
                    LOGGER.log(Level.SEVERE, errorMessage);
                    throw new MovementException(errorMessage);
            }

            // Update the position in MovementData.
            float newX = data.getX() + deltaMovement.x;
            float newY = data.getY() + deltaMovement.y;

            data.setX(newX);
            data.setY(newY);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Exception in AcceleratedMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new MovementException("Error updating position in AcceleratedMovementBehavior", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Unexpected exception in AcceleratedMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new MovementException("Error updating position in AcceleratedMovementBehavior", e);
        }
    }

    @Override
    public void stopMovement(MovementData data, float deltaTime) {
        currentSpeed = 0;
        data.setDirection(Direction.NONE);
    }

    @Override
    public void resumeMovement(MovementData data, float deltaTime) {
    }
}
