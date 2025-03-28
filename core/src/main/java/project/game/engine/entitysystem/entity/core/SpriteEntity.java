package project.game.engine.entitysystem.entity.core;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import project.game.engine.asset.management.CustomAssetManager;
import project.game.engine.entitysystem.entity.api.ISpriteRenderable;
import project.game.engine.entitysystem.entity.base.Entity;

/**
 * SpriteEntity provides support for sprite-based rendering instead of single
 * textures. This allows for directional sprites and animations while
 * maintaining collision capabilities.
 */
public abstract class SpriteEntity extends Entity implements ISpriteRenderable {

    private final Entity entity;
    private final String texturePath;
    private TextureRegion[] sprites;
    private int currentSpriteIndex;

    /**
     * Constructor for SpriteEntity
     * 
     * @param entity      The underlying entity
     * @param texturePath Fallback texture path in case sprites aren't available
     * @param sprites     Array of TextureRegion sprites
     */
    public SpriteEntity(Entity entity, String texturePath, TextureRegion[] sprites) {
        this.entity = entity;
        this.texturePath = texturePath;
        this.sprites = sprites;
        this.currentSpriteIndex = 0;
    }

    public Entity getEntity() {
        return entity;
    }

    public void loadTexture() {
        CustomAssetManager.getInstance().loadTextureAssets(texturePath);
    }

    /**
     * Gets the current sprite to display
     * 
     * @return The current TextureRegion to render
     */
    @Override
    public TextureRegion getCurrentSprite() {
        if (sprites == null || sprites.length == 0) {
            return null;
        }
        return sprites[currentSpriteIndex];
    }

    /**
     * Updates the current sprite index based on entity state
     * This method should be implemented by subclasses to determine
     * which sprite to display based on movement direction, etc.
     */
    @Override
    public abstract void updateSpriteIndex();

    /**
     * Sets the sprite array
     * 
     * @param sprites The new array of sprites
     */
    @Override
    public void setSprites(TextureRegion[] sprites) {
        this.sprites = sprites;
    }

    /**
     * Sets the current sprite index
     * 
     * @param index The index to set
     */
    @Override
    public void setCurrentSpriteIndex(int index) {
        if (sprites != null && index >= 0 && index < sprites.length) {
            this.currentSpriteIndex = index;
        }
    }

    /**
     * @return true if sprite array is available and not empty
     */
    @Override
    public boolean hasSprites() {
        return sprites != null && sprites.length > 0;
    }

    /**
     * Get the number of sprites in the sprite array
     * 
     * @return The number of sprites, or 0 if no sprites available
     */
    @Override
    public int getSpritesCount() {
        return (sprites != null) ? sprites.length : 0;
    }

    /**
     * Get the fallback texture path
     */
    @Override
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Implementation of the render method for sprite-based rendering
     */
    @Override
    public void render(SpriteBatch batch) {
        if (!this.isActive()) {
            return;
        }

        // Calculate render coordinates (centered on Box2D body)
        float renderX = this.getX() - this.getWidth() / 2;
        float renderY = this.getY() - this.getHeight() / 2;

        // Update the sprite index based on current state
        updateSpriteIndex();

        if (hasSprites()) {
            // Use sprite-based rendering
            TextureRegion currentSprite = getCurrentSprite();
            if (currentSprite != null) {
                batch.draw(currentSprite, renderX, renderY,
                        this.getWidth(), this.getHeight());
            }
        } else if (texturePath != null && CustomAssetManager.getInstance().isLoaded()) {
            // Fall back to texture-based rendering using CustomAssetManager
            batch.draw(
                    CustomAssetManager.getInstance().getAsset(texturePath, Texture.class),
                    renderX, renderY,
                    this.getWidth(), this.getHeight());
        }
    }

    public abstract void update();
}