package project.game.abstractengine.movementmanager.interfaces;

import java.util.Set;

public interface IMovementManager {

    float getX();

    float getY();

    void setDeltaTime(float deltaTime);

    void updateDirection(Set<Integer> pressedKeys);

    void updateMovement();
}
