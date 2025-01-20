package project.game.movement;

public interface IMovementManager {

    float getX();

    float getY();

    void setDirection(Direction direction);

    void setDeltaTime(float deltaTime);

    void updateMovement();
}
