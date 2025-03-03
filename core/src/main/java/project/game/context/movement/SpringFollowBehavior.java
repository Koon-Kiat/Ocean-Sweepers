package project.game.context.movement;

import com.badlogic.gdx.math.Vector2;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * Implements spring-like following behavior between entities.
 * The follower is connected to the target by an imaginary spring,
 * creating elastic movement with oscillation and damping.
 */
public class SpringFollowBehavior implements IMovementBehavior {
    private static final GameLogger LOGGER = new GameLogger(SpringFollowBehavior.class);

    private final IPositionable target;
    private final float springConstant; // Higher = stiffer spring
    private final float damping; // Higher = more damping (less oscillation)
    private final boolean lenientMode;
    private final Vector2 velocity;

    public SpringFollowBehavior(IPositionable target, float springConstant, float damping, boolean lenientMode) {
        this.lenientMode = lenientMode;

        if (target == null) {
            String errorMessage = "Target cannot be null in SpringFollowBehavior.";
            LOGGER.error(errorMessage);
            throw new MovementException(errorMessage);
        }
        this.target = target;

        // Validate spring constant
        if (springConstant <= 0) {
            String errorMessage = "Spring constant must be positive. Got: " + springConstant;
            if (lenientMode) {
                LOGGER.warn(errorMessage + " Using default value of 5.0.");
                this.springConstant = 5.0f;
            } else {
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.springConstant = springConstant;
        }

        // Validate damping
        if (damping < 0) {
            String errorMessage = "Damping must be non-negative. Got: " + damping;
            if (lenientMode) {
                LOGGER.warn(errorMessage + " Using default value of 0.5.");
                this.damping = 0.5f;
            } else {
                LOGGER.error(errorMessage);
                throw new MovementException(errorMessage);
            }
        } else {
            this.damping = damping;
        }

        this.velocity = new Vector2(0, 0);
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            // Calculate spring force based on distance to target (Hooke's Law)
            Vector2 toTarget = new Vector2(target.getX() - entity.getX(), target.getY() - entity.getY());
            Vector2 springForce = new Vector2(toTarget).scl(springConstant);

            // Apply damping force based on current velocity
            Vector2 dampingForce = new Vector2(velocity).scl(-damping);

            // Calculate total force
            Vector2 totalForce = springForce.add(dampingForce);

            // Update velocity (F = ma, assuming mass = 1)
            velocity.add(totalForce.x * deltaTime, totalForce.y * deltaTime);

            // Update position
            entity.setX(entity.getX() + velocity.x * deltaTime);
            entity.setY(entity.getY() + velocity.y * deltaTime);

            // Update entity's velocity for animations
            entity.setVelocity(velocity);

        } catch (Exception e) {
            String errorMessage = "Error in SpringFollowBehavior: " + e.getMessage();
            LOGGER.error(errorMessage, e);
            if (!lenientMode) {
                throw new MovementException(errorMessage, e);
            }
        }
    }
}