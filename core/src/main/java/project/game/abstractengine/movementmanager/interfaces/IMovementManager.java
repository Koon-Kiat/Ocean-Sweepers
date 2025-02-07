package project.game.abstractengine.movementmanager.interfaces;

import java.util.Set;

public interface IMovementManager {

    void setDeltaTime(float deltaTime);

    void updateMovement();

    float getX();

    float getY();
    
    void updateDirection(Set<Integer> pressedKeys);
}
