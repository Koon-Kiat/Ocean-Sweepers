package project.game.application.entity.factory;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

import project.game.application.api.constant.IGameConstants;
import project.game.application.api.entity.IEntityRemovalListener;
import project.game.application.entity.item.Trash;
import project.game.engine.entitysystem.entity.base.Entity;
import project.game.engine.entitysystem.physics.management.CollisionManager;

public class TrashFactory extends AbstractEntityFactory<Trash> {
    private final TextureRegion[] trashTextures;
    private final java.util.Random random;
    private IEntityRemovalListener removalListener;

    public TrashFactory(
            IGameConstants constants,
            World world,
            List<Entity> existingEntities,
            CollisionManager collisionManager,
            TextureRegion[] trashTextures) {
        super(constants, world, existingEntities, collisionManager);
        this.trashTextures = trashTextures;
        this.random = new java.util.Random();
    }

    public void setRemovalListener(IEntityRemovalListener removalListener) {
        this.removalListener = removalListener;
    }

    @Override
    public Trash createEntity(float x, float y) {
        Entity trashEntity = new Entity(x, y, constants.TRASH_WIDTH(), constants.TRASH_HEIGHT(), true);
        TextureRegion selectedTexture = trashTextures[random.nextInt(trashTextures.length)];

        Trash trash = new Trash(trashEntity, world, selectedTexture);

        if (collisionManager != null) {
            trash.setCollisionManager(collisionManager);
            // Add entity to collision manager's tracking
            collisionManager.addEntity(trash, null);
        }

        if (removalListener != null) {
            trash.setRemovalListener(removalListener);
        }

        existingEntities.add(trashEntity);
        return trash;
    }
}