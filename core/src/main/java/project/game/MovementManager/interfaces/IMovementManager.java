package project.game.MovementManager.interfaces;

import project.game.MovementManager.Direction;

public interface IMovementManager {
    void setDeltaTime(float deltaTime);
    void updateMovement();
    float getX();
    float getY();
    void setDirection(Direction direction);
}
