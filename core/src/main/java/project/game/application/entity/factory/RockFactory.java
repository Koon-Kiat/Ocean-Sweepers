package project.game.application.entity.factory;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.entity.flyweight.TextureFlyweightFactory;
import project.game.application.entity.obstacle.Rock;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class RockFactory extends AbstractEntityFactory<Rock> {
    private final TextureRegion[] rockRegions;
    private final Random random;

    public RockFactory(
            IGameConstants constants,
            World world,
            List<Entity> existingEntities,
            CollisionManager collisionManager,
            TextureRegion[] rockRegions) {
        super(constants, world, existingEntities, collisionManager);
        this.rockRegions = rockRegions;
        this.random = new Random();
        // Initialize the Flyweight Factory with rock textures
        for (int i = 0; i < rockRegions.length; i++) {
            TextureFlyweightFactory.addTexture("rock_" + i, rockRegions[i]);
        }
    }

    @Override
    public Rock createEntity(float x, float y) {
        Entity rockEntity = new Entity(x, y, constants.ROCK_WIDTH(), constants.ROCK_HEIGHT(), true);
        // Select a random texture ID
        int randomTextureId = random.nextInt(rockRegions.length);
        // Retrieve the texture from the Flyweight Factory
        TextureRegion selectedRock = TextureFlyweightFactory.getTexture("rock_" + randomTextureId);
        Rock rock = new Rock(rockEntity, world, selectedRock);
        existingEntities.add(rockEntity);
        if (collisionManager != null) {
            collisionManager.addEntity(rock, null);
        }
        return rock;
    }
}