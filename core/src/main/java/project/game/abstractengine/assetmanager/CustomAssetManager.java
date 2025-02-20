package project.game.abstractengine.assetmanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class CustomAssetManager implements Disposable {

    private static final Logger LOGGER = Logger.getLogger(CustomAssetManager.class.getName());
    private static CustomAssetManager instance;
    private final AssetManager asset_Manager;

    // Reference counting to track asset usage
    private final Map<String, Integer> assetReferenceCount = new HashMap<>();

    // Group-based asset management
    private final Map<String, Set<String>> assetGroups = new HashMap<>();

    private CustomAssetManager() {
        asset_Manager = new AssetManager();
    }

    public static synchronized CustomAssetManager getInstance() {
        if (instance == null) {
            instance = new CustomAssetManager();
        }
        return instance;
    }

    /** Generalized asset loading method with reference counting */
    public synchronized <T> void loadAsset(String filePath, Class<T> type) {
        if (!asset_Manager.isLoaded(filePath, type)) {
            try {
                asset_Manager.load(filePath, type);
                LOGGER.log(Level.INFO, "Loading asset: {0}", filePath);
            } catch (GdxRuntimeException e) {
                LOGGER.log(Level.SEVERE, "Failed to load asset: {0} | Error: {1}",
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

    /** Unloads all assets in a group */
    public void unloadAssetGroup(String groupName) {
        if (assetGroups.containsKey(groupName)) {
            for (String filePath : assetGroups.get(groupName)) {
                unloadAsset(filePath);
            }
            assetGroups.remove(groupName);
        }
    }

    /** Ensures all assets finish loading */
    public void loadAndFinish() {
        asset_Manager.finishLoading();
        LOGGER.log(Level.INFO, "All assets finished loading.");
    }

    /** Gets asset if loaded */
    public <T> T getAsset(String filePath, Class<T> type) {
        if (asset_Manager.isLoaded(filePath, type)) {
            return asset_Manager.get(filePath, type);
        } else {
            throw new GdxRuntimeException("Asset not loaded: " + filePath);
        }
    }

    /** Unloads asset only when reference count reaches zero */
    public synchronized void unloadAsset(String filePath) {
        if (assetReferenceCount.containsKey(filePath)) {
            int count = assetReferenceCount.get(filePath);
            if (count > 1) {
                assetReferenceCount.put(filePath, count - 1);
            } else {
                if (asset_Manager.isLoaded(filePath)) {
                    asset_Manager.unload(filePath);
                    LOGGER.log(Level.INFO, "Unloaded asset: {0}", filePath);
                }
                assetReferenceCount.remove(filePath);
            }
        } else {
            LOGGER.log(Level.SEVERE, "Attempted to unload non-existent asset: {0}", filePath);
        }
    }

    /** Checks if an asset is loaded */
    public boolean isAssetLoaded(String filePath) {
        return asset_Manager.isLoaded(filePath);
    }

    /** Asynchronously updates asset loading, returns true when done */
    public boolean isLoaded() {
        return asset_Manager.update();
    }

    /** Gets asset loading progress */
    public float getLoadProgress() {
        return asset_Manager.getProgress();
    }

    /** Gets the asset manager */
    public AssetManager getasset_Manager() {
        return this.asset_Manager;
    }

    /** Properly disposes of all assets */
    @Override
    public synchronized void dispose() {
        assetReferenceCount.clear();
        assetGroups.clear();
        asset_Manager.dispose();
        LOGGER.log(Level.INFO, "All assets disposed.");
    }

    /** Updates asset loading progress */
    public void update() {
        asset_Manager.update();
    }
}
