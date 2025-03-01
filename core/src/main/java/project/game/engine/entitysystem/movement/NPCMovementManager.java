package project.game.engine.entitysystem.movement;

import java.util.logging.Level;

import project.game.common.api.ILogger;
import project.game.common.exception.MovementException;
import project.game.common.logging.LogManager;
import project.game.context.builder.NPCMovementBuilder;

/**
 * NPCMovementManager is a concrete class that extends MovementManager and
 * provides movement functionality for non-player characters (NPCs) in the game.
 * 
 * It extends MovementManager and uses the NPCMovementBuilder to configure the
 * NPC's movement behavior.
 */
public class NPCMovementManager extends MovementManager {

    private static final ILogger LOGGER = LogManager.getLogger(NPCMovementManager.class);
    private final NPCMovementBuilder builder;

    /**
     * Private constructor to enforce the use of the Builder.
     */
    public NPCMovementManager(NPCMovementBuilder builder) {
        super(checkBuilder(builder).getEntity(),
                checkBuilder(builder).getSpeed(),
                checkBuilder(builder).getDirection(),
                checkBuilder(builder).getMovementBehavior());
        this.builder = builder;
    }

    private static NPCMovementBuilder checkBuilder(NPCMovementBuilder builder) {
        if (builder == null) {
            String errorMessage = "NPCMovementBuilder cannot be null.";
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new MovementException(errorMessage);
        }
        return builder;
    }

    public NPCMovementBuilder getBuilder() {
        return this.builder;
    }
}
