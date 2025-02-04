package project.game.movementmanager.interfaces;

import java.util.Set;

import project.game.movementmanager.Direction;

public interface IMovementManager {

    void setDeltaTime(float deltaTime);

    void updateMovement();

    float getX();

    float getY();
    void updateDirection(Set<Integer> pressedKeys);
}
