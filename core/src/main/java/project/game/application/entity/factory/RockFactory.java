package project.game.application.entity.factory;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.pool.ObjectPool;
import project.game.application.entity.obstacle.Rock;
import project.game.engine.entitysystem.entity.Entity;

public class RockFactory implements ObjectPool.ObjectFactory<Rock> {

    private final IGameConstants constants;
    private final World world;
    private final Random random;
    private final List<Entity> existingEntities;

    private TextureRegion[] rockRegions; // Array of rock sprites

    // Constructor with rockRegions parameter
    public RockFactory(IGameConstants constants, World world, List<Entity> existingEntities,
            TextureRegion[] rockRegions) {
        this.constants = constants;
        this.world = world;
        this.random = new Random();
        this.existingEntities = existingEntities;
        this.rockRegions = rockRegions;
    }

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
                if (x < entity.getX() + entity.getWidth() &&
                        x + constants.ROCK_WIDTH() > entity.getX() &&
                        y < entity.getY() + entity.getHeight() &&
                        y + constants.ROCK_HEIGHT() > entity.getY()) {
                    overlap = true;
                    break;
                }
            }
        } while (overlap);

        Entity rockEntity = new Entity(x, y, constants.ROCK_WIDTH(), constants.ROCK_HEIGHT(), true);

        // Randomly select one rock sprite from the 3x3 spritesheet
        TextureRegion selectedRock = rockRegions[random.nextInt(rockRegions.length)];

        // Create a Rock instance using the selected TextureRegion.
        // (Ensure your Rock class has a constructor accepting a TextureRegion.)
        Rock rock = new Rock(rockEntity, world, selectedRock);
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