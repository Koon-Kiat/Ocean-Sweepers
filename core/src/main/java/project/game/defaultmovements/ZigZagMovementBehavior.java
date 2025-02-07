package project.game.defaultmovements;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.Direction;
import project.game.abstractengine.movementmanager.MovementData;
import project.game.abstractengine.movementmanager.interfaces.IMovementBehavior;


/**
 * @class ZigZagMovementBehavior
 * @brief Implements a zig-zag movement pattern aligned with the primary
 * direction.
 *
 * Entities using this behavior will move consistently in their primary
 * direction while oscillating perpendicularly to create a zig-zag motion.
 */
public class ZigZagMovementBehavior implements IMovementBehavior {

    private final float speed;
    private final float amplitude;
    private final float frequency;
    private float elapsedTime;

    /**
     * Constructs a ZigZagMovementBehavior with specified parameters.
     *
     * @param speed Forward movement speed.
     * @param amplitude Amplitude of horizontal oscillation.
     * @param frequency Frequency of the oscillation.
     */
    public ZigZagMovementBehavior(float speed, float amplitude, float frequency) {
        this.speed = speed;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.elapsedTime = 0f;
    }

    /**
     * Updates the position using MovementData to move in a zig-zag pattern.
     *
     * @param data The MovementData containing the position, direction, and delta
     * time.
     */
    @Override
    public void updatePosition(MovementData data) {
        float delta = data.getDeltaTime();
        elapsedTime += delta;

        Vector2 deltaMovement = new Vector2();

        Direction primaryDirection = data.getDirection();
        Vector2 primaryVector = getPrimaryVector(primaryDirection);
        Vector2 perpVector = getPerpendicularVector(primaryDirection);

        // Move forward
        deltaMovement.add(primaryVector.scl(speed * delta));

        // Zig-zag
        float oscillation = amplitude * MathUtils.sin(frequency * elapsedTime) * delta;
        deltaMovement.add(perpVector.scl(oscillation));

        data.setX(data.getX() + deltaMovement.x);
        data.setY(data.getY() + deltaMovement.y);
    }

    /**
     * Returns the primary movement vector based on the direction.
     *
     * @param direction The primary movement direction.
     * @return A normalized Vector2 representing the primary movement direction.
     */
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

    /**
     * Returns the perpendicular movement vector based on the primary direction.
     *
     * The perpendicular direction is rotated 90 degrees to the right.
     *
     * @param direction The primary movement direction.
     * @return A normalized Vector2 representing the perpendicular movement
     * direction.
     */
    private Vector2 getPerpendicularVector(Direction direction) {
        Vector2 primary = getPrimaryVector(direction);
        return new Vector2(-primary.y, primary.x).nor(); // Rotate 90 degrees to the right
    }
}
