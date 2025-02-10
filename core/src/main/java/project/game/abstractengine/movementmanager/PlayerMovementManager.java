package project.game.abstractengine.movementmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;
import project.game.builder.PlayerMovementBuilder;

/**
 * @class PlayerMovement
 * @brief Manages the movement logic specific to the player entity.
 *
 *        Extends the abstract MovementManager to provide player-specific
 *        movement
 *        behaviors. Utilizes a Builder pattern to allow flexible configuration
 *        of
 *        movement properties.
 */
public class PlayerMovementManager extends MovementManager {

    private static final Logger LOGGER = Logger.getLogger(PlayerMovementManager.class.getName());

    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
     */
    public PlayerMovementManager(PlayerMovementBuilder builder) {
        super(checkBuilder(builder).x, builder.y, builder.speed, builder.direction, builder.movementBehavior);
    }

    private static PlayerMovementBuilder checkBuilder(PlayerMovementBuilder builder) {
        if (builder == null) {
            String errorMessage = "PlayerMovementBuilder cannot be null.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        return builder;
    }

    @Override
    public void setDirection(Direction direction) {
        try {
            super.setDirection(direction);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error setting direction in PlayerMovementManager: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void setDeltaTime(float deltaTime) {
        try {
            super.setDeltaTime(deltaTime);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error setting delta time in PlayerMovementManager: " + e.getMessage(), e);
            throw e;

        }
    }

    @Override
    public void updateMovement() {
            updatePosition();
    }
}
