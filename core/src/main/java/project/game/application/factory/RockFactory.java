package project.game.application.factory;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.pool.ObjectPool;
import project.game.application.entity.Rock;
import project.game.engine.entitysystem.entity.Entity;

public class RockFactory implements ObjectPool.ObjectFactory<Rock> {
    
    private final IGameConstants constants;
    private final World world;
    private final Random random;
    private final List<Entity> existingEntities;

    public RockFactory(IGameConstants constants, World world, List<Entity> existingEntities) {
        this.constants = constants;
        this.world = world;
        this.random = new Random();
        this.existingEntities = existingEntities;
    }

    @Override
    public Rock createObject() {
        float x, y;
        boolean overlap;
        do {
            x = random.nextFloat() * (constants.GAME_WIDTH() - constants.ROCK_WIDTH());
            y = random.nextFloat() * (constants.GAME_HEIGHT() - constants.ROCK_HEIGHT());
            overlap = false;
            for (Entity entity : existingEntities) {
                if (isOverlapping(x, y, constants.ROCK_WIDTH(), constants.ROCK_HEIGHT(), entity)) {
                    overlap = true;
                    break;
                }
            }
        } while (overlap);

        Entity rockEntity = new Entity(x, y, constants.ROCK_WIDTH(), constants.ROCK_HEIGHT(), true);
        Rock rock = new Rock(rockEntity, world, "rock.png");
        rock.initBody(world);
        existingEntities.add(rockEntity);
        return rock;
    }

    private boolean isOverlapping(float x, float y, float width, float height, Entity entity) {
        return x < entity.getX() + entity.getWidth() &&
               x + width > entity.getX() &&
               y < entity.getY() + entity.getHeight() &&
               y + height > entity.getY();
    }
}