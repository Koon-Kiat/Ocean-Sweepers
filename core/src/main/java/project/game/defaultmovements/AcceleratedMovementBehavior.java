package project.game.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.Direction;
import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.MovementManager;
import project.game.abstractengine.movementmanager.MovementUtils;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;

public class AcceleratedMovementBehavior implements IMovementBehavior {

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
            throw new IllegalArgumentException(errorMessage);
        }
        this.acceleration = acceleration;
        this.deceleration = deceleration;
        this.maxSpeed = maxSpeed;
        this.currentSpeed = 0f;
    }

    /**
     * Updates the position using MovementData to move with acceleration and
     * deceleration. If any error occurs (e.g. negative delta time), the error
     * is logged and the program terminates.
     *
     * @param data The MovementData containing the position, direction, and
     *             delta time.
     */
    @Override
    public void updatePosition(MovementData data) {
        try {
            float deltaTime = data.getDeltaTime();
            if (deltaTime < 0) {
                String errorMessage = "Negative deltaTime provided in updatePosition: " + deltaTime;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

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
                    throw new IllegalArgumentException(errorMessage);
            }

            // Update the position in MovementData.
            float newX = data.getX() + deltaMovement.x;
            float newY = data.getY() + deltaMovement.y;

            data.setX(newX);
            data.setY(newY);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Exception in AcceleratedMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new RuntimeException("Error updating position in AcceleratedMovementBehavior", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Unexpected exception in AcceleratedMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new RuntimeException("Error updating position in AcceleratedMovementBehavior", e);
        }
    }

    /**
     * Stops movement by resetting speed and direction.
     *
     * @param manager The MovementManager instance.
     */
    public void stopMovement(MovementManager manager) {
        currentSpeed = 0;
        manager.setDirection(Direction.NONE);
    }

    /**
     * Resumes movement by restoring the last direction before stopping.
     *
     * @param manager The MovementManager instance.
     */
    public void resumeMovement(MovementManager manager) {
    }
}
