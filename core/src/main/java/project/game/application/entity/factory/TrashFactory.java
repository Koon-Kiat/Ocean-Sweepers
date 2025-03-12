package project.game.application.entity.factory;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.pool.ObjectPool;
import project.game.application.entity.item.Trash;
import project.game.engine.entitysystem.entity.Entity;

public class TrashFactory implements ObjectPool.ObjectFactory<Trash> {

    private final IGameConstants constants;
    private final World world;
    private final Random random;
    private final List<Entity> existingEntities;
    private final Texture[] trashTextures;

    // Add texture array to constructor
    public TrashFactory(IGameConstants constants, World world, List<Entity> existingEntities, Texture[] trashTextures) {
        this.constants = constants;
        this.world = world;
        this.random = new Random();
        this.existingEntities = existingEntities;
        this.trashTextures = trashTextures;
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
        // Get random texture directly from array
        String textureName;
        if (trashTextures != null && trashTextures.length > 0) {
            // Use random texture from array
            textureName = "trash" + (random.nextInt(trashTextures.length) + 1) + ".png";
        } else {
            // Fallback to default if textures not provided
            textureName = "trash1.png";
        }

        
        Trash trash = new Trash(trashEntity, world, textureName);
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