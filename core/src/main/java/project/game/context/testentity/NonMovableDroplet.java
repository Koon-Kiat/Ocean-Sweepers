package project.game.context.testentity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import project.game.engine.api.IRenderable;
import project.game.engine.asset.CustomAssetManager;
import project.game.engine.entitysystem.entitymanager.Entity;

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
        if (entity != null && CustomAssetManager.getInstance().isLoaded()) {

            // Render the entity using offset for BOX2D body
            float renderX = entity.getX() - entity.getWidth() / 2;
            float renderY = entity.getY() - entity.getHeight() / 2;
            Texture texture = CustomAssetManager.getInstance().getAsset(texturePath, Texture.class);
            if (texture != null) {
                batch.draw(texture, renderX, renderY, entity.getWidth(), entity.getHeight());
            }
        }
    }
}
