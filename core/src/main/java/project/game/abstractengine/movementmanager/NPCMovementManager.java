package project.game.abstractengine.movementmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.Direction;
import project.game.builder.NPCMovementBuilder;

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

    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
     */
    public NPCMovementManager(NPCMovementBuilder builder) {
        super(checkBuilder(builder).x, builder.y, builder.speed, builder.direction, builder.movementBehavior);
    }

    private static NPCMovementBuilder checkBuilder(NPCMovementBuilder builder) {
        if (builder == null) {
            String errorMessage = "NPCMovementBuilder cannot be null.";
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
            LOGGER.log(Level.WARNING, "Error setting direction in NPCMovementManager: " + e.getMessage(), e);
        }
    }

    @Override
    public void setDeltaTime(float deltaTime) {
        try {
            super.setDeltaTime(deltaTime);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error setting delta time in NPCMovementManager: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void updateMovement() {
        updatePosition();
    }
}
