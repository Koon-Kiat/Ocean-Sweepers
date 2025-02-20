package project.game.abstractengine.testentity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.abstractengine.assetmanager.GameAsset;
import project.game.abstractengine.entitysystem.entitymanager.Entity;
import project.game.abstractengine.entitysystem.interfaces.IRenderable;

public class NonMovableDroplet implements IRenderable {

    private final Entity entity;
    private final String texturePath;

    public NonMovableDroplet(Entity entity, String texturePath) {
        this.entity = entity;
        this.texturePath = texturePath;
    }

    @Override
    public String getTexturePath() {
        return texturePath;
    }

    @Override
    public void render(SpriteBatch batch) {
        // Ensure that the asset manager has loaded assets and the entity is not null.
        if (entity != null && GameAsset.getInstance().isLoaded()) {
            // Use the entity's position and dimensions for rendering.
            float renderX = entity.getX() - entity.getWidth() / 2;
            float renderY = entity.getY() - entity.getHeight() / 2;
            Texture texture = GameAsset.getInstance().getAsset(texturePath, Texture.class);
            if (texture != null) {
                batch.draw(texture, renderX, renderY, entity.getWidth(), entity.getHeight());
            }
        }
    }
}
