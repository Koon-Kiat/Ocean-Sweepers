package project.game.context.builder;

import project.game.common.exception.MovementException;
import project.game.context.factory.MovementStrategyFactory;
import project.game.engine.entitysystem.entity.Entity;
import project.game.engine.entitysystem.entity.MovableEntity;
import project.game.engine.entitysystem.movement.PlayerMovementManager;

/**
 * Builder class for creating PlayerMovementManager objects.
 * Updated to use Vector2 for movement instead of Direction.
 */
public class PlayerMovementBuilder extends AbstractMovementBuilder<PlayerMovementBuilder> {

    private static final float DEFAULT_SPEED = 200f;
    private static final float DEFAULT_ACCELERATION = 500f;
    private static final float DEFAULT_DECELERATION = 250f;

    // Static factory methods
    public static PlayerMovementBuilder createDefaultPlayer() {
        return new PlayerMovementBuilder()
                .setSpeed(DEFAULT_SPEED)
                .setInitialVelocity(0, 0)
                .withConstantMovement();
    }

    public static PlayerMovementBuilder createAcceleratingPlayer() {
        return new PlayerMovementBuilder()
                .setSpeed(DEFAULT_SPEED)
                .setInitialVelocity(0, 0)
                .withAcceleratedMovement(DEFAULT_ACCELERATION, DEFAULT_DECELERATION);
    }

    public PlayerMovementBuilder withConstantMovement() {
        try {
            this.movementStrategy = MovementStrategyFactory.createConstantMovement(this.speed, this.lenientMode);
        } catch (Exception e) {
            if (this.lenientMode) {
                LOGGER.warn("Failed to create ConstantMovementStrategy in lenient mode: " + e.getMessage()
                        + ". Using default movement.");
                this.movementStrategy = MovementStrategyFactory.createDefaultMovement();
                return this;
            }
            LOGGER.fatal("Failed to create ConstantMovementStrategy", e);
            throw new MovementException("Failed to create ConstantMovementStrategy", e);
        }
        return this;
    }

    public PlayerMovementBuilder withAcceleratedMovement(float acceleration, float deceleration) {
        try {
            this.movementStrategy = MovementStrategyFactory.createAcceleratedMovement(acceleration, deceleration,
                    this.speed, this.lenientMode);
        } catch (MovementException e) {
            if (this.lenientMode) {
                LOGGER.warn("Error creating AcceleratedMovementStrategy in lenient mode: " + e.getMessage()
                        + ". Using ConstantMovement fallback.");
                return withConstantMovement();
            }
            LOGGER.fatal("Error in movement strategy creation: " + e.getMessage(), e);
            throw new MovementException("Error in movement strategy creation: " + e.getMessage(), e);
        }
        return this;
    }

    @Override
    protected MovableEntity createMovableEntityFromEntity(Entity entity, float speed) {
        if (entity == null) {
            String errorMsg = "Cannot create MovableEntity: Entity is null";
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }

        return new PlayerMovableEntity(entity, speed);
    }

    /**
     * Concrete implementation of MovableEntity for player entities
     */
    private static class PlayerMovableEntity extends MovableEntity {
        public PlayerMovableEntity(Entity entity, float speed) {
            super(entity, speed);
        }
    }

    public PlayerMovementManager build() {
        validateBuildRequirements();
        try {
            if (this.entity == null && this.movableEntity == null) {
                String errorMsg = "Entity must not be null for PlayerMovementBuilder.";
                LOGGER.fatal(errorMsg);
                throw new MovementException(errorMsg);
            }
            if (this.movementStrategy == null) {
                LOGGER.info("No movement strategy specified. Using default ConstantMovementStrategy.");
                withConstantMovement();
            }
            return new PlayerMovementManager(this);
        } catch (MovementException e) {
            LOGGER.fatal("Failed to build PlayerMovementManager: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            String msg = "Unexpected error building PlayerMovementManager";
            LOGGER.fatal(msg, e);
            throw new MovementException(msg, e);
        }
    }

    // Private validation method
    @Override
    protected void validateBuildRequirements() {
        if (speed < 0) {
            if (lenientMode) {
                LOGGER.warn("Negative speed found in PlayerMovementBuilder ({0}). Using absolute value.",
                        new Object[] { speed });
                speed = Math.abs(speed);
            } else {
                String errorMessage = "Speed cannot be negative. Current speed: " + speed;
                LOGGER.fatal(errorMessage);
                throw new MovementException(errorMessage);
            }
        }
    }

}
