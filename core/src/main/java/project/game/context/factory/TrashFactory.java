package project.game.context.factory;

import java.util.Random;

import com.badlogic.gdx.physics.box2d.World;

import project.game.context.api.constant.IGameConstants;
import project.game.context.api.pool.ObjectPool;
import project.game.context.entity.Trash;
import project.game.engine.entitysystem.entity.Entity;
import java.util.List;

public class TrashFactory implements ObjectPool.ObjectFactory<Trash> {
    private final IGameConstants constants;
    private final World world;
    private final Random random;
    private final List<Entity> existingEntities;

    public TrashFactory(IGameConstants constants, World world, List<Entity> existingEntities) {
        this.constants = constants;
        this.world = world;
        this.random = new Random();
        this.existingEntities = existingEntities;
    }

    @Override
    public Trash createObject() {
        float x, y;
        boolean overlap;
        do {
            x = random.nextFloat() * (constants.GAME_WIDTH() - constants.TRASH_WIDTH());
            y = random.nextFloat() * (constants.GAME_HEIGHT() - constants.TRASH_HEIGHT());
            overlap = false;
            for (Entity entity : existingEntities) {
                if (isOverlapping(x, y, constants.TRASH_WIDTH(), constants.TRASH_HEIGHT(), entity)) {
                    overlap = true;
                    break;
                }
            }
        } while (overlap);

        Entity trashEntity = new Entity(x, y, constants.TRASH_WIDTH(), constants.TRASH_HEIGHT(), true);
        Trash trash = new Trash(trashEntity, world, "droplet.png");
        trash.initBody(world);
        existingEntities.add(trashEntity);
        return trash;
    }

    private boolean isOverlapping(float x, float y, float width, float height, Entity entity) {
        return x < entity.getX() + entity.getWidth() &&
               x + width > entity.getX() &&
               y < entity.getY() + entity.getHeight() &&
               y + height > entity.getY();
    }
}