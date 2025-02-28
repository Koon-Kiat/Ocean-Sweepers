package project.game.context.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.Direction;
import project.game.engine.entitysystem.entitymanager.MovableEntity;
import project.game.engine.interfaces.IMovementBehavior;
import project.game.exceptions.MovementException;

/**
 * Provides zig-zag movement for movable entities.
 * 
 * The entity moves in a zig-zag pattern at a constant speed. The amplitude and
 * frequency of the oscillation are provided in the constructor. The entity
 * moves in the primary direction and oscillates in the perpendicular direction.
 * 
 */
public class ZigZagMovementBehavior implements IMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(ZigZagMovementBehavior.class.getName());
    private final float speed;
    private final float amplitude;
    private final float frequency;
    private float elapsedTime;

    public ZigZagMovementBehavior(float speed, float amplitude, float frequency) {
        if (speed < 0) {
            if (project.game.engine.entitysystem.movementmanager.MovementManager.LENIENT_MODE) {
                LOGGER.log(Level.WARNING,
                        "Negative speed provided in ZigZagMovementBehavior: {0}. Using absolute value.", speed);
                speed = Math.abs(speed);
            } else {
                String errorMessage = "Illegal negative parameter in ZigZagMovementBehavior constructor: speed="
                        + speed;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        if (frequency < 0) {
            if (project.game.engine.entitysystem.movementmanager.MovementManager.LENIENT_MODE) {
                LOGGER.log(Level.WARNING,
                        "Negative frequency provided in ZigZagMovementBehavior: {0}. Using absolute value.", frequency);
                frequency = Math.abs(frequency);
            } else {
                String errorMessage = "Illegal negative parameter in ZigZagMovementBehavior constructor: frequency="
                        + frequency;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new MovementException(errorMessage);
            }
        }
        this.speed = speed;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.elapsedTime = 0f;
    }

    @Override
    public void applyMovementBehavior(MovableEntity entity, float deltaTime) {
        try {
            elapsedTime += deltaTime;
            Vector2 deltaMovement = new Vector2();
            Direction primaryDirection = entity.getDirection();
            Vector2 primaryVector = getPrimaryVector(primaryDirection);
            Vector2 perpVector = getPerpendicularVector(primaryDirection);

            // Forward movement.
            deltaMovement.add(primaryVector.scl(speed * deltaTime));

            // Zig-zag oscillation.
            float oscillation = amplitude * MathUtils.sin(frequency * elapsedTime) * deltaTime;

            deltaMovement.add(perpVector.scl(oscillation));
            entity.setX(entity.getX() + deltaMovement.x);
            entity.setY(entity.getY() + deltaMovement.y);
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Exception in ZigZagMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new MovementException("Error updating position in ZigZagMovementBehavior", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected exception in ZigZagMovementBehavior.updatePosition: " + e.getMessage(),
                    e);
            throw new MovementException("Error updating position in ZigZagMovementBehavior", e);
        }
    }

    private Vector2 getPrimaryVector(Direction direction) {
        switch (direction) {
            case UP:
                return new Vector2(0, 1);
            case DOWN:
                return new Vector2(0, -1);
            case LEFT:
                return new Vector2(-1, 0);
            case RIGHT:
                return new Vector2(1, 0);
            case UP_LEFT:
                return new Vector2(-MathUtils.sinDeg(45), MathUtils.cosDeg(45)).nor();
            case UP_RIGHT:
                return new Vector2(MathUtils.sinDeg(45), MathUtils.cosDeg(45)).nor();
            case DOWN_LEFT:
                return new Vector2(-MathUtils.sinDeg(45), -MathUtils.cosDeg(45)).nor();
            case DOWN_RIGHT:
                return new Vector2(MathUtils.sinDeg(45), -MathUtils.cosDeg(45)).nor();
            case NONE:
            default:
                return new Vector2(0, 0);
        }
    }

    private Vector2 getPerpendicularVector(Direction direction) {
        Vector2 primary = getPrimaryVector(direction);
        return new Vector2(-primary.y, primary.x).nor();
    }
}
