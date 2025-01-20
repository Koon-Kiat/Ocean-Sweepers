package project.game.movement;

/**
 * @enum Direction
 * @brief Enumerates the possible movement directions.
 *
 * The Direction enum defines the various directions in which an entity can move
 * within the game world. It includes both cardinal and diagonal directions, as
 * well as a NONE option indicating no movement.
 */
public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    UP_LEFT,
    UP_RIGHT,
    DOWN_LEFT,
    DOWN_RIGHT,
    NONE;

    /**
     * @brief Determines if the direction involves vertical movement.
     *
     * @return True if moving up or down, including diagonal directions
     * involving vertical movement.
     */
    public boolean isVertical() {
        return this == UP || this == DOWN || this == UP_LEFT || this == UP_RIGHT || this == DOWN_LEFT || this == DOWN_RIGHT;
    }

    /**
     * @brief Determines if the direction involves horizontal movement.
     *
     * @return True if moving left or right, including diagonal directions
     * involving horizontal movement.
     */
    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT || this == UP_LEFT || this == UP_RIGHT || this == DOWN_LEFT || this == DOWN_RIGHT;
    }
}
