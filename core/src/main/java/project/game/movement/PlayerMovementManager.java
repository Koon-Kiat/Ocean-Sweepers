package project.game.movement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import project.game.Main;

/**
 * @class PlayerMovementManager
 * @brief Manages the player's movement based on direction and speed.
 *
 * The PlayerMovementManager class extends MovementManager to provide specific
 * movement behaviors for the player entity. It updates the player's position
 * according to the current direction and speed, ensuring smooth and consistent
 * movement within the game world.
 */
public class PlayerMovementManager extends MovementManager {

    private static final float SQRT_TWO = 1.4142f;
    private final float diagonalSpeed;

    /**
     * Constructs a PlayerMovementManager using the Builder pattern.
     *
     * @param builder The builder instance containing initialization parameters.
     */
    public PlayerMovementManager(Builder builder) {
        super(builder.x, builder.y, builder.speed, builder.direction);
        this.diagonalSpeed = this.speed / SQRT_TWO;
    }

    public static class Builder {

        private float x;
        private float y;
        private float speed;
        private Direction direction;

        public Builder() {
            this.x = 0;
            this.y = 0;
            this.speed = 0;
            this.direction = Direction.NONE;
        }

        public Builder setX(float x) {
            this.x = x;
            return this;
        }

        public Builder setY(float y) {
            this.y = y;
            return this;
        }

        public Builder setSpeed(float speed) {
            if (speed < 0) {
                throw new IllegalArgumentException("Speed cannot be negative.");
            }
            this.speed = speed;
            return this;
        }

        public Builder setDirection(Direction direction) {
            this.direction = direction;
            return this;
        }

        public PlayerMovementManager build() {
            return new PlayerMovementManager(this);
        }
    }

    /**
     * @brief Updates the player's position based on speed, direction, and frame
     * delta time.
     *
     * This method ensures that movement is consistent across devices with
     * varying frame rates by scaling movement calculations with delta time. It
     * also clamps the player's position within the game boundaries to prevent
     * moving out of bounds.
     */
    @Override
    public void updatePosition() {
        float delta = Gdx.graphics.getDeltaTime();

        // Clamp delta to a maximum value to prevent large movements
        delta = Math.min(delta, 1 / 30f);

        Vector2 deltaMovement = new Vector2(0, 0);

        switch (direction) {
            case UP:
                deltaMovement.y += speed * delta;
                break;
            case DOWN:
                deltaMovement.y -= speed * delta;
                break;
            case LEFT:
                deltaMovement.x -= speed * delta;
                break;
            case RIGHT:
                deltaMovement.x += speed * delta;
                break;
            case UP_LEFT:
                deltaMovement.x -= diagonalSpeed * delta;
                deltaMovement.y += diagonalSpeed * delta;
                break;
            case UP_RIGHT:
                deltaMovement.x += diagonalSpeed * delta;
                deltaMovement.y += diagonalSpeed * delta;
                break;
            case DOWN_LEFT:
                deltaMovement.x -= diagonalSpeed * delta;
                deltaMovement.y -= diagonalSpeed * delta;
                break;
            case DOWN_RIGHT:
                deltaMovement.x += diagonalSpeed * delta;
                deltaMovement.y -= diagonalSpeed * delta;
                break;
            case NONE:
                // No movement
                break;
        }

        position.add(deltaMovement);

        // Clamp the position to game boundaries
        position.x = MathUtils.clamp(position.x, 0, Main.GAME_WIDTH);
        position.y = MathUtils.clamp(position.y, 0, Main.GAME_HEIGHT);
    }

    /**
     * @brief Stops the player's movement by setting the direction to NONE.
     */
    @Override
    public void stop() {
        setDirection(Direction.NONE);
    }

    @Override
    public void resume() {
        // Implement resume logic if needed
    }
}
