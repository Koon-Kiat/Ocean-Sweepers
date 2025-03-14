package project.game.engine.entitysystem.entity.api;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Interface for entities that can render different sprites based on state.
 * Extends IRenderable to include basic rendering capabilities while adding
 * sprite-specific functionality.
 */
public interface ISpriteRenderable extends IRenderable {

    /**
     * Gets the current sprite to display
     * 
     * @return The current TextureRegion to render
     */
    TextureRegion getCurrentSprite();

    /**
     * Updates which sprite should be displayed based on the entity's current state
     */
    void updateSpriteIndex();

    /**
     * Sets the array of sprites available to the entity
     * 
     * @param sprites The new array of sprites
     */
    void setSprites(TextureRegion[] sprites);

    /**
     * Sets which sprite in the array is currently active
     * 
     * @param index The index to set
     */
    void setCurrentSpriteIndex(int index);

    /**
     * @return true if sprite array is available and not empty
     */
    boolean hasSprites();

    /**
     * Get the number of sprites in the sprite array
     * 
     * @return The number of sprites, or 0 if no sprites available
     */
    int getSpritesCount();
}