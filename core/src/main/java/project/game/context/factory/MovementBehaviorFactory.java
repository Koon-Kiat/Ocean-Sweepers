package project.game.context.factory;

import java.util.List;

import project.game.common.exception.MovementException;
import project.game.common.logging.core.GameLogger;
import project.game.context.movement.AcceleratedMovementBehavior;
import project.game.context.movement.ConstantMovementBehavior;
import project.game.context.movement.FollowMovementBehavior;
import project.game.context.movement.InterceptorMovementBehavior;
import project.game.context.movement.OrbitalMovementBehavior;
import project.game.context.movement.RandomisedMovementBehavior;
import project.game.context.movement.SpiralApproachBehavior;
import project.game.context.movement.SpringFollowBehavior;
import project.game.context.movement.ZigZagMovementBehavior;
import project.game.engine.api.movement.IMovementBehavior;
import project.game.engine.api.movement.IPositionable;
import project.game.engine.entitysystem.entity.MovableEntity;

/**
 * Factory class for creating movement behaviors.
 * This helps avoid direct instantiation of dependencies and follows the
 * Dependency Inversion Principle.
 */
public class MovementBehaviorFactory {
    private static final GameLogger LOGGER = new GameLogger(MovementBehaviorFactory.class);

    /**
     * Creates a constant movement behavior.
     * 
     * @param speed The speed of the movement.
     * @return A new ConstantMovementBehavior instance.
     */
    public static IMovementBehavior createConstantMovement(float speed, boolean lenientMode) {
        try {
            return new ConstantMovementBehavior(speed, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating ConstantMovementBehavior: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates an accelerated movement behavior.
     * 
     * @param acceleration The acceleration of the movement.
     * @param deceleration The deceleration of the movement.
     * @param speed        The speed of the movement.
     * @param lenientMode  Whether to enable lenient mode.
     * @return A new AcceleratedMovementBehavior instance.
     */
    public static IMovementBehavior createAcceleratedMovement(float acceleration, float deceleration, float speed,
            boolean lenientMode) {
        try {
            return new AcceleratedMovementBehavior(acceleration, deceleration, speed, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating AcceleratedMovementBehavior: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a zig-zag movement behavior.
     * 
     * @param speed       The speed of the movement.
     * @param amplitude   The amplitude of the zigzag pattern.
     * @param frequency   The frequency of the zigzag pattern.
     * @param lenientMode Whether to enable lenient mode.
     * @return A new ZigZagMovementBehavior instance.
     */
    public static IMovementBehavior createZigZagMovement(float speed, float amplitude, float frequency,
            boolean lenientMode) {
        try {
            return new ZigZagMovementBehavior(speed, amplitude, frequency, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating ZigZagMovementBehavior: " + e.getMessage(), e);
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
    public static IMovementBehavior createFollowMovement(IPositionable target, float speed, boolean lenientMode) {
        if (target == null) {
            String errorMsg = "Target is null in createFollowMovement.";
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            return new FollowMovementBehavior(target, speed, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating FollowMovementBehavior: " + e.getMessage(), e);
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
            float maxDuration, boolean lenientMode) {
        if (behaviorPool == null) {
            String errorMsg = "Behavior pool cannot be null in createRandomisedMovement.";
            LOGGER.fatal(errorMsg);
            throw new MovementException(errorMsg);
        }
        try {
            return new RandomisedMovementBehavior(behaviorPool, minDuration, maxDuration, lenientMode);
        } catch (MovementException e) {
            LOGGER.fatal("Error creating RandomisedMovementBehavior: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a default movement behavior when none is specified.
     * 
     * @return A new ConstantMovementBehavior with default speed.
     */
    public static IMovementBehavior createDefaultMovement() {
        return createConstantMovement(GameConstantsFactory.getConstants().DEFAULT_SPEED(), false);
    }

    /**
     * Creates an OrbitalMovementBehavior.
     */
    public static IMovementBehavior createOrbitalMovement(IPositionable target, float orbitRadius, float rotationSpeed,
            float eccentricity, boolean lenientMode) {
        try {
            return new OrbitalMovementBehavior(target, orbitRadius, rotationSpeed, eccentricity, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create OrbitalMovementBehavior: " + e.getMessage());
            throw new MovementException("Failed to create OrbitalMovementBehavior", e);
        }
    }

    /**
     * Creates a SpringFollowBehavior.
     */
    public static IMovementBehavior createSpringFollowMovement(IPositionable target, float springConstant,
            float damping,
            boolean lenientMode) {
        try {
            return new SpringFollowBehavior(target, springConstant, damping, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create SpringFollowBehavior: " + e.getMessage());
            throw new MovementException("Failed to create SpringFollowBehavior", e);
        }
    }

    /**
     * Creates an InterceptorMovementBehavior.
     */
    public static IMovementBehavior createInterceptorMovement(MovableEntity target, float speed, boolean lenientMode) {
        try {
            return new InterceptorMovementBehavior(target, speed, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create InterceptorMovementBehavior: " + e.getMessage());
            throw new MovementException("Failed to create InterceptorMovementBehavior", e);
        }
    }

    /**
     * Creates a SpiralApproachBehavior.
     */
    public static IMovementBehavior createSpiralApproachMovement(IPositionable target, float speed,
            float spiralTightness,
            float approachSpeed, boolean lenientMode) {
        try {
            return new SpiralApproachBehavior(target, speed, spiralTightness, approachSpeed, lenientMode);
        } catch (Exception e) {
            LOGGER.error("Failed to create SpiralApproachBehavior: " + e.getMessage());
            throw new MovementException("Failed to create SpiralApproachBehavior", e);
        }
    }

    // Private constructor to prevent instantiation
    private MovementBehaviorFactory() {
        throw new UnsupportedOperationException(
                "MovementBehaviorFactory is a utility class and cannot be instantiated.");
    }
}