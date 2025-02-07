package project.game.abstractengine.movementmanager;

import project.game.Direction;
import project.game.builder.PlayerMovementBuilder;

/**
 * @class PlayerMovement
 * @brief Manages the movement logic specific to the player entity.
 *
 * Extends the abstract MovementManager to provide player-specific movement
 * behaviors. Utilizes a Builder pattern to allow flexible configuration of
 * movement properties.
 */
public class PlayerMovementManager extends MovementManager {

    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
     */
    public PlayerMovementManager(PlayerMovementBuilder builder) {
        super(builder.x, builder.y, builder.speed, builder.direction, builder.movementBehavior);
    }

    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
    }

    @Override
    public void setDeltaTime(float deltaTime) {
        super.setDeltaTime(deltaTime);
    }

    @Override
    public void updateMovement() {
        updatePosition();
    }
}



