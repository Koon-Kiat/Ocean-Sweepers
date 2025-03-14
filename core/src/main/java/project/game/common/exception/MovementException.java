package project.game.common.exception;

public class MovementException extends GameException {
    
    public MovementException(String message) {
        super(message);
    }

    public MovementException(String message, Throwable cause) {
        super(message, cause);
    }
}
