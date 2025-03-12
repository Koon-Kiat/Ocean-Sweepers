package project.game.application.entity.factory;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.api.pool.ObjectPool;
import project.game.application.entity.item.Trash;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class TrashFactory implements ObjectPool.ObjectFactory<Trash> {

    private final IGameConstants constants;
    private final World world;
    private final Random random;
    private final List<Entity> existingEntities;
    private final Texture[] trashTextures;
    private final  CollisionManager collisionManager;
    private IEntityRemovalListener removalListener;

    // Add texture array to constructor
    public TrashFactory(IGameConstants constants, World world, List<Entity> existingEntities,
            Texture[] trashTextures, CollisionManager collisionManager) {
        this.constants = constants;
        this.world = world;
        this.random = new Random();
        this.existingEntities = existingEntities;
        this.trashTextures = trashTextures;
        this.collisionManager = collisionManager;
    }

    public void setRemovalListener(IEntityRemovalListener removalListener) {
        this.removalListener = removalListener;
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

        // Select a random texture directly from the array
        String textureName;
        if (trashTextures != null && trashTextures.length > 0) {
            int randomIndex = random.nextInt(trashTextures.length);
            textureName = "trash" + (randomIndex + 1) + ".png";
        } else {
            textureName = "trash1.png";
        }

        Trash trash = new Trash(trashEntity, world, textureName);
        trash.initBody(world);

        // Set the collision manager and removal listener
        if (collisionManager != null) {
            trash.setCollisionManager(collisionManager);
        }

        if (removalListener != null) {
            trash.setRemovalListener(removalListener);
        }

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