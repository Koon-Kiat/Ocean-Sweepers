package project.game.movement;

public interface IMovementManager {

    void setDirection(Direction direction);

    void setDeltaTime(float deltaTime);

    void updateMovement();
}
