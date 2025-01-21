package project.game.movement;

import com.badlogic.gdx.math.Vector2;


/**
 * @class AcceleratedMovementBehavior
 * @brief Implements a movement behavior with acceleration and deceleration.
 *
 * Entities using this behavior will accelerate when moving and decelerate when
 * stopping. This creates a more natural and responsive movement pattern
 * compared to constant speed. Diagonal movements are handled by scaling the
 * movement on each axis to maintain consistent overall speed.
 */
public class AcceleratedMovementBehavior implements IMovementBehavior {

    private final float acceleration;
    private final float deceleration;
    private final float maxSpeed;
    private float currentSpeed;

    /**
     * Constructs an AcceleratedMovementBehavior with specified parameters.
     *
     * @param acceleration Rate of acceleration.
     * @param deceleration Rate of deceleration.
     * @param maxSpeed Maximum achievable speed.
     */
    public AcceleratedMovementBehavior(float acceleration, float deceleration, float maxSpeed) {
        this.acceleration = acceleration;
        this.deceleration = deceleration;
        this.maxSpeed = maxSpeed;
        this.currentSpeed = 0f;
    }


    /**
     * Updates the position of the MovementManager based on
     * acceleration/deceleration logic.
     *
     * @param manager The MovementManager whose position is to be updated.
     */
    @Override
    public void updatePosition(MovementManager manager) {
        float delta = manager.getDeltaTime();
        delta = Math.min(delta, 1 / 30f);

        if (manager.getDirection() != Direction.NONE) {
            currentSpeed += acceleration * delta;
            if (currentSpeed > maxSpeed) {
                currentSpeed = maxSpeed;
            }
        } else {
            currentSpeed -= deceleration * delta;
            if (currentSpeed < 0) {
                currentSpeed = 0;
            }
        }

        Vector2 deltaMovement = new Vector2();

        switch (manager.getDirection()) {
            case UP:
                deltaMovement.y += currentSpeed * delta;
                break;
            case DOWN:
                deltaMovement.y -= currentSpeed * delta;
                break;
            case LEFT:
                deltaMovement.x -= currentSpeed * delta;
                break;
            case RIGHT:
                deltaMovement.x += currentSpeed * delta;
                break;
            case UP_LEFT:
                float diagonalSpeed = MovementUtils.calculateDiagonalSpeed(currentSpeed);
                deltaMovement.x -= diagonalSpeed * delta;
                deltaMovement.y += diagonalSpeed * delta;
                break;
            case UP_RIGHT:
                diagonalSpeed = MovementUtils.calculateDiagonalSpeed(currentSpeed);
                deltaMovement.x += diagonalSpeed * delta;
                deltaMovement.y += diagonalSpeed * delta;
                break;
            case DOWN_LEFT:
                diagonalSpeed = MovementUtils.calculateDiagonalSpeed(currentSpeed);
                deltaMovement.x -= diagonalSpeed * delta;
                deltaMovement.y -= diagonalSpeed * delta;
                break;
            case DOWN_RIGHT:
                diagonalSpeed = MovementUtils.calculateDiagonalSpeed(currentSpeed);
                deltaMovement.x += diagonalSpeed * delta;
                deltaMovement.y -= diagonalSpeed * delta;
                break;
            case NONE:
                break;
        }

        // Update the position vector with the calculated delta movement
        manager.getPosition().add(deltaMovement);

        // Ensure the entity remains within game boundaries
        manager.clampPosition();
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
