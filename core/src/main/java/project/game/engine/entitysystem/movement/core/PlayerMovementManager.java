package project.game.engine.entitysystem.movement.core;

import project.game.application.movement.builder.PlayerMovementBuilder;
import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.movement.management.MovementManager;

/**
 * PlayerMovementManager is a class that extends MovementManager and
 * provides movement functionality for the player character in the game.
 */
public class PlayerMovementManager extends MovementManager {

    private static final GameLogger LOGGER = new GameLogger(PlayerMovementManager.class);

    public PlayerMovementManager(PlayerMovementBuilder builder) {
        super(checkBuilder(builder).getMovable(),
                checkBuilder(builder).getSpeed(),
                checkBuilder(builder).getInitialVelocity(),
                checkBuilder(builder).getMovementStrategy(),
                checkBuilder(builder).isLenientMode());
    }

    private static PlayerMovementBuilder checkBuilder(PlayerMovementBuilder builder) {
        if (builder == null) {
            String errorMessage = "PlayerMovementBuilder cannot be null.";
            LOGGER.fatal(errorMessage);
            throw new MovementException(errorMessage);
        }
        return builder;
    }
}
