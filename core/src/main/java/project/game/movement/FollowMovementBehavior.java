package project.game.movement;

import com.badlogic.gdx.math.Vector2;

public class FollowMovementBehavior implements IMovementBehavior {

    private final IMovementManager targetManager;
    private final float speed;

    public FollowMovementBehavior(IMovementManager targetManager, float speed) {
        this.targetManager = targetManager;
        this.speed = speed;
    }

    @Override
    public void updatePosition(MovementManager manager) {
        float deltaTime = manager.getDeltaTime();
        Vector2 targetPos = new Vector2(targetManager.getX(), targetManager.getY());
        Vector2 currentPos = new Vector2(manager.getX(), manager.getY());

        // Calculate direction to follow
        Vector2 direction = targetPos.sub(currentPos).nor();

        // Move towards the target
        manager.setX(manager.getX() + direction.x * speed * deltaTime);
        manager.setY(manager.getY() + direction.y * speed * deltaTime);

        // Clamp within world boundaries
        manager.clampPosition();
    }
}
