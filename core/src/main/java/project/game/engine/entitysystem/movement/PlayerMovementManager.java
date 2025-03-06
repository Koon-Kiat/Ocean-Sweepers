package project.game.engine.entitysystem.movement;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.context.builder.PlayerMovementBuilder;

/**
 * PlayerMovementManager is a concrete class that extends MovementManager and
 * provides movement functionality for the player character in the game.
 * 
 * It extends MovementManager and uses the PlayerMovementBuilder to configure
 * the player's movement strategy.
 */
public class PlayerMovementManager extends MovementManager {

    private static final GameLogger LOGGER = new GameLogger(PlayerMovementManager.class);
    private final PlayerMovementBuilder builder;

    /**
     * Constructor using the Builder pattern.
     */
    public PlayerMovementManager(PlayerMovementBuilder builder) {
        super(checkBuilder(builder).getMovableEntity(),
                checkBuilder(builder).getSpeed(),
                checkBuilder(builder).getInitialVelocity(),
                checkBuilder(builder).getMovementStrategy(),
                checkBuilder(builder).isLenientMode());
        this.builder = builder;
    }

    public PlayerMovementBuilder getBuilder() {
        return this.builder;
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
