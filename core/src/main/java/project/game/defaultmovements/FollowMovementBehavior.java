package project.game.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Vector2;

import project.game.abstractengine.entitysystem.entitymanager.MovableEntity;
import project.game.abstractengine.entitysystem.movementmanager.MovementManager;
import project.game.abstractengine.interfaces.IMovementBehavior;
import project.game.abstractengine.interfaces.IMovementManager;
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
        } else {
            this.targetManager = targetManager;
        }
        if (speed < 0) {
            if (MovementManager.LENIENT_MODE) {
                LOGGER.log(Level.WARNING,
                        "Negative speed provided in FollowMovementBehavior: {0}. Using absolute value.", speed);
                this.speed = Math.abs(speed);
            } else {
                String errorMessage = "Negative speed provided in FollowMovementBehavior: " + speed;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.speed = speed;
        }
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            Vector2 targetPos = new Vector2(((MovableEntity) targetManager).getX(),
                    ((MovableEntity) targetManager).getY());
            Vector2 currentPos = new Vector2(entity.getX(), entity.getY());
            Vector2 direction = targetPos.sub(currentPos).nor();
            float newX = entity.getX() + direction.x * speed * deltaTime;
            float newY = entity.getY() + direction.y * speed * deltaTime;
            entity.setX(newX);
            entity.setY(newY);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Illegal argument in FollowMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Invalid argument in FollowMovementBehavior", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in FollowMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error updating position in FollowMovementBehavior", e);
        }
    }
}
