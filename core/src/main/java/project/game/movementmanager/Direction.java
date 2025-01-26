package project.game.movementmanager;

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


    public boolean is(Direction direction) {
        return this == direction;
    }
}
