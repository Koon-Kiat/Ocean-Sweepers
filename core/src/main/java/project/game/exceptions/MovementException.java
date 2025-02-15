package project.game.exceptions;

public class MovementException extends GameException {
    public MovementException(String message) {
        super(message);
    }

    public MovementException(String message, Throwable cause) {
        super(message, cause);
    }
}
