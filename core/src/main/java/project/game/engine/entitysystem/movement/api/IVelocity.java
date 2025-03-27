package project.game.engine.entitysystem.movement.api;

import com.badlogic.gdx.math.Vector2;

public interface IVelocity {

    float getSpeed();

    void setSpeed(float speed);

    Vector2 getVelocity();

    void setVelocity(Vector2 velocity);

    void setVelocity(float x, float y);

    void normalizeVelocity();

    void clearVelocity();

}
