package project.game.engine.entitysystem.movement;

import java.util.logging.Level;

import project.game.common.api.ILogger;
import project.game.common.exception.MovementException;
import project.game.common.logging.LogManager;
import project.game.context.builder.PlayerMovementBuilder;

/**
 * PlayerMovementManager is a concrete class that extends MovementManager and
 * provides movement functionality for the player character in the game.
 * 
 * It extends MovementManager and uses the PlayerMovementBuilder to configure
 * the player's movement behavior.
 */
public class PlayerMovementManager extends MovementManager {

    private static final ILogger LOGGER = LogManager.getLogger(PlayerMovementManager.class);
    private final PlayerMovementBuilder builder;

    /**
     * Private constructor to enforce the use of the Builder.
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
