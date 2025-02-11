package project.game.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.abstractengine.movementmanager.interfaces.IMovementManager;
import project.game.exceptions.MovementException;

/**
 * @class FollowMovementBehavior
 * @brief Moves the entity towards a target using MovementData.
 * 
 *        This class implements a movement behavior that moves the entity
 *        towards a
 *        target. The target is specified by an IMovementManager, which provides
 *        the
 *        target's position. The speed of the movement can be set in the
 *        constructor.
 */
public class FollowMovementBehavior implements IMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(FollowMovementBehavior.class.getName());
    private final IMovementManager targetManager;
    private final float speed;

    /**
     * Constructs a FollowMovementBehavior with the specified parameters.
     * Terminates the program if any provided parameter is negative or null.
     * 
     * @param targetManager IMovementManager providing the target's position.
     * @param speed         Speed of the movement.
     */
    public FollowMovementBehavior(IMovementManager targetManager, float speed) {
        if (targetManager == null) {
            String errorMessage = "Target manager cannot be null in FollowMovementBehavior.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        if (speed < 0) {
            String errorMessage = "Negative speed provided in FollowMovementBehavior: " + speed;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        this.targetManager = targetManager;
        this.speed = speed;
    }

    @Override
    public void applyMovementBehavior(MovementData data, float deltaTime) {
        try {
            Vector2 targetPos = new Vector2(targetManager.getX(), targetManager.getY());
            Vector2 currentPos = new Vector2(data.getX(), data.getY());
            Vector2 direction = targetPos.sub(currentPos).nor();
            float newX = data.getX() + direction.x * speed * deltaTime;
            float newY = data.getY() + direction.y * speed * deltaTime;
            data.setX(newX);
            data.setY(newY);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Illegal argument in FollowMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Runtime exception in FollowMovementBehavior.updatePosition: " + e.getMessage(),
                    e);
            throw new MovementException("Error updating position in FollowMovementBehavior", e);
        }
    }
}
