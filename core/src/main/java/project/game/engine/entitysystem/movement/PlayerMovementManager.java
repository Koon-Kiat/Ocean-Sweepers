package project.game.engine.entitysystem.movement;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.context.builder.PlayerMovementBuilder;
import project.game.exceptions.MovementException;

/**
 * PlayerMovementManager is a concrete class that extends MovementManager and
 * provides movement functionality for the player character in the game.
 * 
 * It extends MovementManager and uses the PlayerMovementBuilder to configure
 * the player's movement behavior.
 */
public class PlayerMovementManager extends MovementManager {

    private static final Logger LOGGER = Logger.getLogger(PlayerMovementManager.class.getName());
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
