package project.game.application.movement.strategy;

import com.badlogic.gdx.math.Vector2;

import project.game.application.movement.api.StrategyType;
import project.game.engine.entitysystem.movement.api.IMovable;
import project.game.engine.entitysystem.movement.strategy.AbstractMovementStrategy;

/**
 * Provides constant movement for movable entities.
 * 
 * The entity moves based on its velocity vector at a constant speed.
 * The speed is provided in the constructor.
 */
public class ConstantMovementStrategy extends AbstractMovementStrategy {

    private final float speed;

    public ConstantMovementStrategy(float speed, boolean lenientMode) {
        super(ConstantMovementStrategy.class, lenientMode);
        this.speed = validateSpeed(speed, 200f);
    }

    @Override

    public StrategyType getStrategyType() {
        return StrategyType.CONSTANT;
    }

    @Override
    public void move(IMovable movable, float deltaTime) {
        try {
            // Get current velocity
            Vector2 velocity = getSafeVelocity(movable);

            // If velocity is zero, nothing to do
            if (velocity.len2() < 0.0001f) {
                return;
            }

            // Normalize and scale by speed and deltaTime
            Vector2 movement = new Vector2(velocity).nor().scl(speed * deltaTime);

            // Apply movement
            applyMovement(movable, movement);

        } catch (Exception e) {
            handleMovementException(e, "Error in ConstantMovementStrategy: " + e.getMessage());
        }
    }
}
