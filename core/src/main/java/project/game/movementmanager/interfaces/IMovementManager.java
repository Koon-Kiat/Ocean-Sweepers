package project.game.movementmanager.interfaces;

import project.game.movementmanager.Direction;

public interface IMovementManager {
    void setDeltaTime(float deltaTime);
    void updateMovement();
    float getX();
    float getY();
    void setDirection(Direction direction);
}
