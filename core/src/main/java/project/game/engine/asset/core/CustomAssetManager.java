package project.game.engine.asset.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import project.game.common.logging.core.GameLogger;

public class CustomAssetManager implements Disposable {

    private static final GameLogger LOGGER = new GameLogger(CustomAssetManager.class);
    private static CustomAssetManager instance;
    private final AssetManager asset_Manager;

    // Reference counting to track asset usage
    private final Map<String, Integer> assetReferenceCount = new HashMap<>();

    // Group-based asset management
    private final Map<String, Set<String>> assetGroups = new HashMap<>();

    // Sprite management - maps sprite sheet identifiers to arrays of TextureRegions
    private final Map<String, TextureRegion[]> spriteSheets = new HashMap<>();

    // Directional sprite sets - maps entity types to directional sprite arrays
    private final Map<String, TextureRegion[]> directionalSpriteSets = new HashMap<>();

    private CustomAssetManager() {
        asset_Manager = new AssetManager();
    }

    public static synchronized CustomAssetManager getInstance() {
        if (instance == null) {
            instance = new CustomAssetManager();
        }
        return instance;
    }

    /**
     * Generalized asset loading method with reference counting
     */
    public synchronized <T> void loadAsset(String filePath, Class<T> type) {
        if (!asset_Manager.isLoaded(filePath, type)) {
            try {
                asset_Manager.load(filePath, type);
                LOGGER.info("Loading asset: {0}", filePath);
            } catch (GdxRuntimeException e) {
                LOGGER.fatal("Failed to load asset: {0} | Error: {1}",
                        new Object[] { filePath, e.getMessage() });
                return;
            }
        }
        assetReferenceCount.put(filePath, assetReferenceCount.getOrDefault(filePath, 0) + 1);
    }

    public void loadTextureAssets(String filePath) {
        loadAsset(filePath, Texture.class);
    }

    public void loadSoundAsset(String filePath) {
        loadAsset(filePath, Sound.class);
    }

    public void loadMusicAsset(String filePath) {
        loadAsset(filePath, Music.class);
    }

    public void loadFontAsset(String filePath) {
        loadAsset(filePath, BitmapFont.class);
    }

    /** Loads a group of assets */
    public void loadAssetGroup(String groupName, Map<String, Class<?>> assets) {
        assetGroups.putIfAbsent(groupName, new HashSet<>());
        for (Map.Entry<String, Class<?>> entry : assets.entrySet()) {
            loadAsset(entry.getKey(), entry.getValue());
            assetGroups.get(groupName).add(entry.getKey());
        }
    }

    /**
     * Creates and stores a sprite sheet from a texture
     * 
     * @param spriteSheetId Identifier for the sprite sheet
     * @param texturePath   Path to the texture file
     * @param cols          Number of columns in the sprite sheet
     * @param rows          Number of rows in the sprite sheet
     * @return Array of TextureRegions representing the sprite sheet
     */
    public TextureRegion[] createSpriteSheet(String spriteSheetId, String texturePath, int cols, int rows) {
        // Ensure texture is loaded
        loadTextureAssets(texturePath);
        loadAndFinish();

        Texture texture = getAsset(texturePath, Texture.class);
        int frameWidth = texture.getWidth() / cols;
        int frameHeight = texture.getHeight() / rows;

        // Split the texture into regions
        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);

        // Flatten 2D array into 1D
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        // Store the sprite sheet
        spriteSheets.put(spriteSheetId, frames);
        LOGGER.info("Created sprite sheet '{0}' with {1} frames", spriteSheetId, frames.length);

        return frames;
    }

    /**
     * Registers a set of directional sprites for an entity type
     * 
     * @param entityType The type of entity (e.g., "player", "monster")
     * @param sprites    Array of directional sprites (typically UP, RIGHT, DOWN,
     *                   LEFT)
     */
    public void registerDirectionalSprites(String entityType, TextureRegion[] sprites) {
        directionalSpriteSets.put(entityType, sprites);
        LOGGER.info("Registered directional sprites for entity type: {0}", entityType);
    }

    /**
     * Creates a directional sprite set from specific frames in a sprite sheet
     * 
     * @param entityType    The type of entity to create sprites for
     * @param spriteSheetId ID of the sprite sheet to use
     * @param upIndex       Index of UP direction sprite
     * @param rightIndex    Index of RIGHT direction sprite
     * @param downIndex     Index of DOWN direction sprite
     * @param leftIndex     Index of LEFT direction sprite
     * @return Array of directional sprites in order: UP, RIGHT, DOWN, LEFT
     */
    public TextureRegion[] createDirectionalSprites(String entityType, String spriteSheetId,
            int upIndex, int rightIndex, int downIndex, int leftIndex) {
        if (!spriteSheets.containsKey(spriteSheetId)) {
            LOGGER.error("Sprite sheet '{0}' not found", spriteSheetId);
            return null;
        }

        TextureRegion[] sheet = spriteSheets.get(spriteSheetId);
        TextureRegion[] directionalSprites = new TextureRegion[4];

        // Check indices are within bounds
        if (upIndex >= 0 && upIndex < sheet.length &&
                rightIndex >= 0 && rightIndex < sheet.length &&
                downIndex >= 0 && downIndex < sheet.length &&
                leftIndex >= 0 && leftIndex < sheet.length) {

            directionalSprites[0] = sheet[upIndex]; // UP
            directionalSprites[1] = sheet[rightIndex]; // RIGHT
            directionalSprites[2] = sheet[downIndex]; // DOWN
            directionalSprites[3] = sheet[leftIndex]; // LEFT

            // Register the directional sprite set
            registerDirectionalSprites(entityType, directionalSprites);
            return directionalSprites;
        } else {
            LOGGER.error("Invalid sprite indices for directional sprites");
            return null;
        }
    }

    /**
     * Gets a registered directional sprite set for an entity type
     * 
     * @param entityType The type of entity
     * @return Array of directional sprites or null if not found
     */
    public TextureRegion[] getDirectionalSprites(String entityType) {
        return directionalSpriteSets.get(entityType);
    }

    /**
     * Gets a sprite sheet by ID
     * 
     * @param spriteSheetId ID of the sprite sheet
     * @return Array of TextureRegions or null if not found
     */
    public TextureRegion[] getSpriteSheet(String spriteSheetId) {
        return spriteSheets.get(spriteSheetId);
    }

    /**
     * Unloads all assets in a group
     */
    public void unloadAssetGroup(String groupName) {
        if (assetGroups.containsKey(groupName)) {
            for (String filePath : assetGroups.get(groupName)) {
                unloadAsset(filePath);
            }
            assetGroups.remove(groupName);
        }
    }

    /**
     * Ensures all assets finish loading
     */
    public void loadAndFinish() {
        asset_Manager.finishLoading();
        LOGGER.info("All assets finished loading.");
    }

    /**
     * Gets asset if loaded
     */
    public <T> T getAsset(String filePath, Class<T> type) {
        if (asset_Manager.isLoaded(filePath, type)) {
            return asset_Manager.get(filePath, type);
        } else {
            throw new GdxRuntimeException("Asset not loaded: " + filePath);
        }
    }

    /**
     * Unloads asset only when reference count reaches zero
     */
    public synchronized void unloadAsset(String filePath) {
        if (assetReferenceCount.containsKey(filePath)) {
            int count = assetReferenceCount.get(filePath);
            if (count > 1) {
                assetReferenceCount.put(filePath, count - 1);
            } else {
                if (asset_Manager.isLoaded(filePath)) {
                    asset_Manager.unload(filePath);
                    LOGGER.info("Unloaded asset: {0}", filePath);
                }
                assetReferenceCount.remove(filePath);
            }
        } else {
            LOGGER.info("Attempted to unload non-existent asset: {0}", filePath);
        }
    }

    /**
     * Checks if an asset is loaded
     */
    public boolean isAssetLoaded(String filePath) {
        return asset_Manager.isLoaded(filePath);
    }

    /**
     * Asynchronously updates asset loading, returns true when done
     */
    public boolean isLoaded() {
        return asset_Manager.update();
    }

    /**
     * Gets asset loading progress
     */
    public float getLoadProgress() {
        return asset_Manager.getProgress();
    }

    /**
     * Gets the asset manager
     */
    public AssetManager getasset_Manager() {
        return this.asset_Manager;
    }

    /**
     * Properly disposes of all assets
     */
    @Override
    public synchronized void dispose() {
        assetReferenceCount.clear();
        assetGroups.clear();
        spriteSheets.clear();
        directionalSpriteSets.clear();
        asset_Manager.dispose();
        LOGGER.info("All assets disposed.");
    }

    /**
     * Updates asset loading progress
     */
    public void update() {
        asset_Manager.update();
    }
}
