package project.game.factory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.abstractengine.interfaces.IMovementBehavior;
import project.game.abstractengine.interfaces.IPositionable;
import project.game.constants.GameConstants;
import project.game.defaultmovements.ConstantMovementBehavior;
import project.game.defaultmovements.FollowMovementBehavior;
import project.game.defaultmovements.RandomisedMovementBehavior;
import project.game.defaultmovements.ZigZagMovementBehavior;
import project.game.exceptions.MovementException;

/**
 * Factory class for creating movement behaviors.
 * This helps avoid direct instantiation of dependencies and follows the
 * Dependency Inversion Principle.
 */
public class MovementBehaviorFactory {
    private static final Logger LOGGER = Logger.getLogger(MovementBehaviorFactory.class.getName());

    /**
     * Creates a constant movement behavior.
     * 
     * @param speed The speed of the movement.
     * @return A new ConstantMovementBehavior instance.
     */
    public static IMovementBehavior createConstantMovement(float speed) {
        try {
            return new ConstantMovementBehavior(speed);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error creating ConstantMovementBehavior: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a zig-zag movement behavior.
     * 
     * @param speed     The speed of the movement.
     * @param amplitude The amplitude of the zigzag pattern.
     * @param frequency The frequency of the zigzag pattern.
     * @return A new ZigZagMovementBehavior instance.
     */
    public static IMovementBehavior createZigZagMovement(float speed, float amplitude, float frequency) {
        try {
            return new ZigZagMovementBehavior(speed, amplitude, frequency);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error creating ZigZagMovementBehavior: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a follow movement behavior.
     * 
     * @param target The target to follow.
     * @param speed  The speed of the movement.
     * @return A new FollowMovementBehavior instance.
     */
    public static IMovementBehavior createFollowMovement(IPositionable target, float speed) {
        if (target == null) {
            String errorMsg = "Target is null in createFollowMovement.";
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            return new FollowMovementBehavior(target, speed);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error creating FollowMovementBehavior: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a randomised movement behavior.
     * 
     * @param behaviorPool The pool of behaviors to randomly select from.
     * @param minDuration  The minimum duration for each behavior.
     * @param maxDuration  The maximum duration for each behavior.
     * @return A new RandomisedMovementBehavior instance.
     */
    public static IMovementBehavior createRandomisedMovement(List<IMovementBehavior> behaviorPool,
            float minDuration,
            float maxDuration) {
        if (behaviorPool == null) {
            String errorMsg = "Behavior pool cannot be null in createRandomisedMovement.";
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            return new RandomisedMovementBehavior(behaviorPool, minDuration, maxDuration);
        } catch (MovementException e) {
            LOGGER.log(Level.SEVERE, "Error creating RandomisedMovementBehavior: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a default movement behavior when none is specified.
     * 
     * @return A new ConstantMovementBehavior with default speed.
     */
    public static IMovementBehavior createDefaultMovement() {
        return createConstantMovement(GameConstants.DEFAULT_SPEED);
    }
}