package project.game.application.movement.strategy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.application.movement.api.StrategyType;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.api.IPositionable;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;

/**
 * Makes an entity approach its target in a spiral pattern.
 * The spiral tightens as the entity gets closer to the target.
 */
public class SpiralApproachStrategy extends AbstractMovementStrategy {

    private final IPositionable target;
    private final float speed;
    private final float spiralTightness;
    private final float approachSpeed;
    private float currentAngle = 0;

    public SpiralApproachStrategy(IPositionable target, float speed, float spiralTightness, float approachSpeed,
            boolean lenientMode) {
        super(SpiralApproachStrategy.class, lenientMode);

        // Validate target
        validateTarget(target, "Target");
        this.target = target;

        // Validate speed
        this.speed = validateSpeed(speed, 200f);

        // Validate and set spiral parameters
        this.spiralTightness = Math.max(0.1f, spiralTightness);
        
        this.approachSpeed = Math.max(0.1f, approachSpeed);
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.SPIRAL_APPROACH;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Calculate vector to target
            Vector2 toTarget = new Vector2(target.getX() - movable.getX(), target.getY() - movable.getY());
            float distanceToTarget = toTarget.len();

            // Update angle based on distance (spiral gets tighter as we get closer)
            float angleSpeed = speed / (distanceToTarget * spiralTightness);
            currentAngle += angleSpeed * deltaTime;

            // Keep angle between 0 and 2π
            currentAngle = currentAngle % (2 * MathUtils.PI);

            // Calculate spiral offset
            float spiralRadius = distanceToTarget * 0.5f;
            float offsetX = spiralRadius * MathUtils.cos(currentAngle);
            float offsetY = spiralRadius * MathUtils.sin(currentAngle);

            // Calculate approach vector
            Vector2 approachVector = new Vector2(toTarget).nor().scl(approachSpeed * deltaTime);

            // Combine spiral and approach movements
            Vector2 newPosition = new Vector2(movable.getX(), movable.getY());
            newPosition.add(approachVector);
            newPosition.add(offsetX - movable.getX(), offsetY - movable.getY());

            // Calculate velocity for proper facing
            Vector2 velocity = new Vector2(newPosition).sub(movable.getX(), movable.getY());
            movable.setVelocity(velocity.scl(1f / deltaTime));

            // Update position
            movable.setX(newPosition.x);
            movable.setY(newPosition.y);

        } catch (Exception e) {
            handleMovementException(e, "Error in SpiralApproachStrategy: " + e.getMessage());
        }
    }
}