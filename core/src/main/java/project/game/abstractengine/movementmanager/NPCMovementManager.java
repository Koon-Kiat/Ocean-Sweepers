package project.game.abstractengine.movementmanager;

import project.game.Direction;
import project.game.builder.NPCMovementBuilder;

/**
 * @class EnemyMovement
 * @brief Manages the movement logic specific to enemy entities.
 *
 * Extends the abstract MovementManager to provide enemy-specific movement
 * behaviors, such as zig-zag movement. Utilizes a Builder pattern for flexible
 * configuration.
 */
public class NPCMovementManager extends MovementManager {

    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder The Builder instance containing configuration parameters.
     */
    public NPCMovementManager(NPCMovementBuilder builder) {
        super(builder.x, builder.y, builder.speed, builder.direction, builder.movementBehavior);
    }

    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
    }

    @Override
    public void setDeltaTime(float deltaTime) {
        super.setDeltaTime(deltaTime);
    }

    @Override
    public void updateMovement() {
        updatePosition();
    }
}
