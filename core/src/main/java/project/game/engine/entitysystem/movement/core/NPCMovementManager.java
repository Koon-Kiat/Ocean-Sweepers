package project.game.engine.entitysystem.movement.core;

import project.game.application.movement.builder.NPCMovementBuilder;
import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.entitysystem.movement.management.MovementManager;

/**
 * NPCMovementManager is a class that extends MovementManager and
 * provides movement functionality for non-player characters (NPCs) in the game.
 */
public class NPCMovementManager extends MovementManager {

    private static final GameLogger LOGGER = new GameLogger(NPCMovementManager.class);

    public NPCMovementManager(NPCMovementBuilder builder) {
        super(checkBuilder(builder).getMovable(),
                checkBuilder(builder).getSpeed(),
                checkBuilder(builder).getInitialVelocity(),
                checkBuilder(builder).getMovementStrategy(),
                checkBuilder(builder).isLenientMode());
    }

    private static NPCMovementBuilder checkBuilder(NPCMovementBuilder builder) {
        if (builder == null) {
            String errorMessage = "NPCMovementBuilder cannot be null.";
            LOGGER.fatal(errorMessage);
            throw new MovementException(errorMessage);
        }
        return builder;
    }
}
