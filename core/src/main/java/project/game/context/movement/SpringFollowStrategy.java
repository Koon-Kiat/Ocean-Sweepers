package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.engine.api.movement.IMovable;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.movement.AbstractMovementStrategy;

/**
 * Implements spring-like following strategy between entities.
 * The follower is connected to the target by an imaginary spring,
 * creating elastic movement with oscillation and damping.
 */
public class SpringFollowStrategy extends AbstractMovementStrategy {

    private final IPositionable target;
    private final float springConstant; // Higher = stiffer spring
    private final float damping; // Higher = more damping (less oscillation)
    private final Vector2 velocity;

    public SpringFollowStrategy(IPositionable target, float springConstant, float damping, boolean lenientMode) {
        super(SpringFollowStrategy.class, lenientMode);

        // Validate target
        validateTarget(target, "Target");
        this.target = target;

        // Validate spring constant
        this.springConstant = validateSpeed(springConstant, 5.0f);

        // Validate damping
        this.damping = validateNonNegative(damping, "Damping", 0.5f);

        this.velocity = new Vector2(0, 0);
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Calculate spring force based on distance to target (Hooke's Law)
            Vector2 toTarget = new Vector2(target.getX() - movable.getX(), target.getY() - movable.getY());
            Vector2 springForce = new Vector2(toTarget).scl(springConstant);

            // Apply damping force based on current velocity
            Vector2 dampingForce = new Vector2(velocity).scl(-damping);

            // Calculate total force
            Vector2 totalForce = springForce.add(dampingForce);

            // Update velocity (F = ma, assuming mass = 1)
            velocity.add(totalForce.x * deltaTime, totalForce.y * deltaTime);

            // Update position
            movable.setX(movable.getX() + velocity.x * deltaTime);
            movable.setY(movable.getY() + velocity.y * deltaTime);

            // Update movable's velocity for animations
            movable.setVelocity(velocity);

        } catch (Exception e) {
            handleMovementException(e, "Error in SpringFollowStrategy: " + e.getMessage());
        }
    }
}