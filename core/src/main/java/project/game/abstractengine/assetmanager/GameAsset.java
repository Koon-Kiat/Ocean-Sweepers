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

public class GameAsset implements Disposable {

    private static GameAsset instance;
    private final AssetManager assetManager;
    private static final Logger LOGGER = Logger.getLogger(GameAsset.class.getName());

    // Reference counting to track asset usage
    private final Map<String, Integer> assetReferenceCount = new HashMap<>();

    // Group-based asset management
    private final Map<String, Set<String>> assetGroups = new HashMap<>();

    private GameAsset() {
        assetManager = new AssetManager();
    }

    public static synchronized GameAsset getInstance() {
        if (instance == null) {
            instance = new GameAsset();
        }
        return instance;
    }

    /** Generalized asset loading method with reference counting */
    public synchronized <T> void loadAsset(String filePath, Class<T> type) {
        if (!assetManager.isLoaded(filePath, type)) {
            try {
                assetManager.load(filePath, type);
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
        assetManager.finishLoading();
        LOGGER.log(Level.INFO, "All assets finished loading.");
    }

    /** Gets asset if loaded */
    public <T> T getAsset(String filePath, Class<T> type) {
        if (assetManager.isLoaded(filePath, type)) {
            return assetManager.get(filePath, type);
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
                if (assetManager.isLoaded(filePath)) {
                    assetManager.unload(filePath);
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
        return assetManager.isLoaded(filePath);
    }

    /** Asynchronously updates asset loading, returns true when done */
    public boolean isLoaded() {
        return assetManager.update();
    }

    /** Gets asset loading progress */
    public float getLoadProgress() {
        return assetManager.getProgress();
    }

    /** Gets the asset manager */
    public AssetManager getAssetManager() {
        return this.assetManager;
    }

    /** Properly disposes of all assets */
    @Override
    public synchronized void dispose() {
        assetReferenceCount.clear();
        assetGroups.clear();
        assetManager.dispose();
        LOGGER.log(Level.INFO, "All assets disposed.");
    }

    /** Updates asset loading progress */
    public void update() {
        assetManager.update();
    }
}
