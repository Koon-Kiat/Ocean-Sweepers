package project.game.abstractengine.entitysystem.movementmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.builder.NPCMovementBuilder;
import project.game.exceptions.MovementException;

/**
 * @class EnemyMovement
 * @brief Manages the movement logic specific to enemy entities.
 *
 *        Extends the abstract MovementManager to provide enemy-specific
 *        movement
 *        behaviors, such as zig-zag movement. Utilizes a Builder pattern for
 *        flexible
 *        configuration.
 */
public class NPCMovementManager extends MovementManager {

    private static final Logger LOGGER = Logger.getLogger(NPCMovementManager.class.getName());
    private final NPCMovementBuilder builder;

    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
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
