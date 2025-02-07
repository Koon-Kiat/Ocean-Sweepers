package project.game.abstractengine.movementmanager.defaultmovementbehaviour;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.abstractengine.movementmanager.Direction;
import project.game.abstractengine.movementmanager.MovementManager;
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
     * Updates the position of the MovementManager to follow a zig-zag pattern.
     *
     * The zig-zag oscillation is applied perpendicular to the primary
     * direction.
     *
     * @param manager The MovementManager whose position is to be updated.
     */
    @Override
    public void updatePosition(MovementManager manager) {
        float delta = manager.getDeltaTime();
        elapsedTime += delta;

        Vector2 deltaMovement = new Vector2();

        // Determine the primary and perpendicular directions based on the current direction
        Direction primaryDirection = manager.getDirection();
        Vector2 primaryVector = getPrimaryVector(primaryDirection);
        Vector2 perpendicularVector = getPerpendicularVector(primaryDirection);

        // Apply primary movement
        deltaMovement.add(primaryVector.scl(speed * delta));

        // Apply zig-zag oscillation perpendicular to the primary direction
        float oscillation = amplitude * MathUtils.sin(frequency * elapsedTime) * delta;
        deltaMovement.add(perpendicularVector.scl(oscillation));

        // Update the position vector with the calculated delta movement
        manager.getPosition().add(deltaMovement);

        // Ensure the entity remains within game boundaries
        manager.clampPosition();
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
