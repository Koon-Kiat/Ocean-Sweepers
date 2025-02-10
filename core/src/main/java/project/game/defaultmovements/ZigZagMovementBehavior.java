package project.game.defaultmovements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.Direction;
import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;

/**
 * Moves the entity in a zig-zag pattern. The entity moves forward in the
 * primary direction while oscillating sideways. A negative amplitude inverts
 * the oscillation.
 */
public class ZigZagMovementBehavior implements IMovementBehavior {

    private static final Logger LOGGER = Logger.getLogger(ZigZagMovementBehavior.class.getName());
    private final float speed;
    private final float amplitude;
    private final float frequency;
    private float elapsedTime;

    public ZigZagMovementBehavior(float speed, float amplitude, float frequency) {
        if (speed < 0 || frequency < 0) {
            String errorMessage = "Illegal negative parameter in ZigZagMovementBehavior constructor: "
                    + "speed=" + speed + ", frequency=" + frequency;
            LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.speed = speed;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.elapsedTime = 0f;
    }

    @Override
    public void updatePosition(MovementData data) {
        try {
            float delta = data.getDeltaTime();
            if (delta < 0) {
                String errorMessage = "Negative deltaTime provided in ZigZagMovementBehavior.updatePosition: " + delta;
                LOGGER.log(Level.SEVERE, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            elapsedTime += delta;

            Vector2 deltaMovement = new Vector2();

            Direction primaryDirection = data.getDirection();
            Vector2 primaryVector = getPrimaryVector(primaryDirection);
            Vector2 perpVector = getPerpendicularVector(primaryDirection);

            // Move forward in the primary direction.
            deltaMovement.add(primaryVector.scl(speed * delta));

            // Apply zig-zag oscillation; amplitude may be negative for a phase inversion.
            float oscillation = amplitude * MathUtils.sin(frequency * elapsedTime) * delta;
            deltaMovement.add(perpVector.scl(oscillation));

            data.setX(data.getX() + deltaMovement.x);
            data.setY(data.getY() + deltaMovement.y);

        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Exception in ZigZagMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new RuntimeException("Error updating position in ZigZagMovementBehavior", e);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected exception in ZigZagMovementBehavior.updatePosition: " + e.getMessage(), e);
            throw new RuntimeException("Error updating position in ZigZagMovementBehavior", e);
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
