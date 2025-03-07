package project.game.engine.entitysystem.movement;

import project.game.application.builder.NPCMovementBuilder;
import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.engine.api.movement.IMovementStrategyFactory;

/**
 * NPCMovementManager is a concrete class that extends MovementManager and
 * provides movement functionality for non-player characters (NPCs) in the game.
 * 
 * It extends MovementManager and uses the NPCMovementBuilder to configure the
 * NPC's movement strategy.
 */
public class NPCMovementManager extends MovementManager {

    private static final GameLogger LOGGER = new GameLogger(NPCMovementManager.class);
    private final NPCMovementBuilder builder;

    /**
     * Constructor using the Builder pattern.
     */
    public NPCMovementManager(NPCMovementBuilder builder, IMovementStrategyFactory movementStrategyFactory) {
        super(checkBuilder(builder).getMovableEntity(),
                checkBuilder(builder).getSpeed(),
                checkBuilder(builder).getInitialVelocity(),
                checkBuilder(builder).getMovementStrategy(),
                checkBuilder(builder).isLenientMode(),
                movementStrategyFactory);
        this.builder = builder;
    }

    public NPCMovementBuilder getBuilder() {
        return this.builder;
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
