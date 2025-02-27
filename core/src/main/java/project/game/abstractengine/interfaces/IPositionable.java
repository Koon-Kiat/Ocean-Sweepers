package project.game.abstractengine.interfaces;

/**
 * Interface for objects that have a position in 2D space.
 * This interface breaks the circular dependency between MovementManager and
 * FollowMovementBehavior.
 */
public interface IPositionable {
    /**
     * Gets the X-coordinate of the object.
     * 
     * @return the X-coordinate
     */
    float getX();

    /**
     * Gets the Y-coordinate of the object.
     * 
     * @return the Y-coordinate
     */
    float getY();
}