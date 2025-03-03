package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * Provides follow movement for movable entities.
 * 
 * The entity moves towards the target entity at a constant speed.
 * The target entity is provided by an IPositionable interface.
 */
public class FollowMovementBehavior implements IMovementBehavior {

    private static final GameLogger LOGGER = new GameLogger(FollowMovementBehavior.class);
    private final IPositionable target;
    private final float speed;
    private final boolean lenientMode;  

    /**
     * Constructs a FollowMovementBehavior with the specified parameters.
     * Terminates the program if any provided parameter is negative or null.
     */
    public FollowMovementBehavior(IPositionable target, float speed, boolean lenientMode) {
        this.lenientMode = lenientMode;
        if (target == null) {
            String errorMessage = "Target cannot be null in FollowMovementBehavior.";
            LOGGER.error(errorMessage);
            throw new MovementException(errorMessage);
        } else {
            this.target = target;
        }
        if (speed < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative speed provided in FollowMovementBehavior: {0}. Using absolute value.", speed);
                this.speed = Math.abs(speed);
            } else {
                String errorMessage = "Negative speed provided in FollowMovementBehavior: " + speed;
                LOGGER.error(errorMessage);
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
            LOGGER.error("Illegal argument in FollowMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Invalid argument in FollowMovementBehavior", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error in FollowMovementBehavior: " + e.getMessage(), e);
            throw new MovementException("Error updating position in FollowMovementBehavior", e);
        }
    }
}
