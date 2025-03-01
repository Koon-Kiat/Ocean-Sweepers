package project.game.context.movement;

import java.util.logging.Level;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.ILogger;
import project.game.common.logging.LogManager;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.entity.MovableEntity;
import project.game.engine.entitysystem.movement.MovementManager;

/**
 * Provides follow movement for movable entities.
 * 
 * The entity moves towards the target entity at a constant speed.
 * The target entity is provided by an IPositionable interface.
 */
public class FollowMovementBehavior implements IMovementBehavior {

    private static final ILogger LOGGER = LogManager.getLogger(FollowMovementBehavior.class);
    private final IPositionable target;
    private final float speed;

    /**
     * Constructs a FollowMovementBehavior with the specified parameters.
     * Terminates the program if any provided parameter is negative or null.
     */
    public FollowMovementBehavior(IPositionable target, float speed) {
        if (target == null) {
            String errorMessage = "Target cannot be null in FollowMovementBehavior.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        } else {
            this.target = target;
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
            Vector2 targetPos = new Vector2(target.getX(), target.getY());
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
