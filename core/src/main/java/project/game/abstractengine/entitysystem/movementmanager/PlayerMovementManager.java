package project.game.abstractengine.entitysystem.movementmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.builder.PlayerMovementBuilder;
import project.game.exceptions.MovementException;

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
    private final PlayerMovementBuilder builder;

    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
     */
    public PlayerMovementManager(PlayerMovementBuilder builder) {
        super(checkBuilder(builder).getEntity(),
                checkBuilder(builder).getSpeed(),
                checkBuilder(builder).getDirection(),
                checkBuilder(builder).getMovementBehavior());
        this.builder = builder;
    }

    private static PlayerMovementBuilder checkBuilder(PlayerMovementBuilder builder) {
        if (builder == null) {
            String errorMessage = "PlayerMovementBuilder cannot be null.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        return builder;
    }

    public PlayerMovementBuilder getBuilder() {
        return this.builder;
    }
}
