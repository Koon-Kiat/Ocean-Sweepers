package project.game.defaultmovements;

import com.badlogic.gdx.math.Vector2;

import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;
import project.game.abstractengine.movementmanager.interfaces.IMovementManager;

/**
 * @class FollowMovementBehavior
 * @brief Moves the entity toward the target's position using MovementData.
 */
public class FollowMovementBehavior implements IMovementBehavior {

    private final IMovementManager targetManager;
    private final float speed;

    public FollowMovementBehavior(IMovementManager targetManager, float speed) {
        this.targetManager = targetManager;
        this.speed = speed;
    }

    /**
     * Uses MovementData for the follower, plus targetManager for the position
     * of whatever is being followed.
     */
    @Override
    public void updatePosition(MovementData data) {
        float deltaTime = data.getDeltaTime();

        // To follow the target, we need the target's x/y.
        Vector2 targetPos = new Vector2(targetManager.getX(), targetManager.getY());
        Vector2 currentPos = new Vector2(data.getX(), data.getY());

        // Compute direction
        Vector2 direction = targetPos.sub(currentPos).nor();

        // Update positions
        float newX = data.getX() + direction.x * speed * deltaTime;
        float newY = data.getY() + direction.y * speed * deltaTime;

        data.setX(newX);
        data.setY(newY);
    }
}
