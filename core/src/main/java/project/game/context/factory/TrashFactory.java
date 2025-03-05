package project.game.context.factory;

import project.game.context.entity.Trash;
import project.game.engine.entitysystem.entity.Entity;
import project.game.context.api.constant.IGameConstants;
import com.badlogic.gdx.physics.box2d.World;
import project.game.context.api.pool.ObjectPool;

import java.util.Random;

public class TrashFactory implements ObjectPool.ObjectFactory<Trash> {
    private final IGameConstants constants;
    private final World world;
    private final Random random;

    public TrashFactory(IGameConstants constants, World world) {
        this.constants = constants;
        this.world = world;
        this.random = new Random();
    }

    @Override
    public Trash createObject() {
        float x = random.nextFloat() * (constants.GAME_WIDTH() - constants.TRASH_WIDTH());
        float y = random.nextFloat() * (constants.GAME_HEIGHT() - constants.TRASH_HEIGHT());
        Entity trashEntity = new Entity(x, y, constants.TRASH_WIDTH(), constants.TRASH_HEIGHT(), true);
        Trash trash = new Trash(trashEntity, world, "droplet.png");
        trash.initBody(world); // Initialize the body with the correct position
        return trash;
    }
}