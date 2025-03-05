package project.game.context.factory;

import java.util.Random;

import com.badlogic.gdx.physics.box2d.World;

import project.game.context.api.constant.IGameConstants;
import project.game.context.api.pool.ObjectPool;
import project.game.context.entity.Rock;
import project.game.engine.entitysystem.entity.Entity;

public class RockFactory implements ObjectPool.ObjectFactory<Rock> {
    private final IGameConstants constants;
    private final World world;
    private final Random random;

    public RockFactory(IGameConstants constants, World world) {
        this.constants = constants;
        this.world = world;
        this.random = new Random();
    }

    @Override
    public Rock createObject() {
        float x = random.nextFloat() * (constants.GAME_WIDTH() - constants.ROCK_WIDTH());
        float y = random.nextFloat() * (constants.GAME_HEIGHT() - constants.ROCK_HEIGHT());
        Entity rockEntity = new Entity(x, y, constants.ROCK_WIDTH(), constants.ROCK_HEIGHT(), true);
        Rock rock = new Rock(rockEntity, world, "rock.png");
        rock.initBody(world);
        return rock;
    }
}