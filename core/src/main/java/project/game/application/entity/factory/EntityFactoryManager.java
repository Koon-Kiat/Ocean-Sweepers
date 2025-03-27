package project.game.application.entity.factory;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.entity.item.Trash;
import project.game.application.entity.obstacle.Rock;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class EntityFactoryManager {

    private final IGameConstants constants;
    private final RockFactory rockFactory;
    private final TrashFactory trashFactory;
    private final Random random;

    public EntityFactoryManager(
            IGameConstants constants,
            World world,
            List<Entity> existingEntities,
            CollisionManager collisionManager,
            TextureRegion[] rockRegions,
            TextureRegion[] trashRegions) {
        this.constants = constants;
        this.random = new Random();
        this.rockFactory = new RockFactory(constants, world, existingEntities, collisionManager, rockRegions);
        this.trashFactory = new TrashFactory(constants, world, existingEntities, collisionManager, trashRegions);
    }

    public Rock createRock() {
        float x, y;
        do {
            x = random.nextFloat() * (constants.GAME_WIDTH() - constants.ROCK_WIDTH());
            y = random.nextFloat() * (constants.GAME_HEIGHT() - constants.ROCK_HEIGHT());
        } while (rockFactory.checkCollisionWithExisting(x, y, constants.ROCK_WIDTH(), constants.ROCK_HEIGHT()));

        Rock rock = rockFactory.createEntity(x, y);
        System.out.println("Created Rock at: x=" + x + ", y=" + y);
        return rock;
    }

    public Trash createTrash() {
        float x, y;
        int maxRetries = 50;
        int retries = 0;
        Trash trash = null;

        do {
            x = random.nextFloat() * (constants.GAME_WIDTH() - constants.TRASH_WIDTH());
            y = random.nextFloat() * (constants.GAME_HEIGHT() - constants.TRASH_HEIGHT());
            if (!trashFactory.checkCollisionWithExisting(x, y, constants.TRASH_WIDTH(), constants.TRASH_HEIGHT())
                    || retries >= maxRetries) {
                trash = trashFactory.createEntity(x, y);
                System.out.println("Created Trash at: x=" + x + ", y=" + y);
                break;
            }
            retries++;
        } while (retries < maxRetries);

        // If we couldn't find a non-colliding position after max retries, just place it
        // somewhere
        if (trash == null) {
            x = random.nextFloat() * (constants.GAME_WIDTH() - constants.TRASH_WIDTH());
            y = random.nextFloat() * (constants.GAME_HEIGHT() - constants.TRASH_HEIGHT());
            trash = trashFactory.createEntity(x, y);
            System.out.println("Created Trash (after retries) at: x=" + x + ", y=" + y);
        }

        return trash;
    }

    public void setTrashRemovalListener(IEntityRemovalListener listener) {
        trashFactory.setRemovalListener(listener);
    }
}