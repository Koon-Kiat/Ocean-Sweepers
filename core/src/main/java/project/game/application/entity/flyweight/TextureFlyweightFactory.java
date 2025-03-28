package project.game.application.entity.flyweight;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureFlyweightFactory {
    
    private static final Map<String, TextureRegion> textureCache = new HashMap<>();
    private static final Map<String, Integer> referenceCounts = new HashMap<>();

    public static void addTexture(String key, TextureRegion texture) {
        textureCache.putIfAbsent(key, texture);
        referenceCounts.put(key, referenceCounts.getOrDefault(key, 0) + 1);
    }

    public static TextureRegion getTexture(String key) {
        return textureCache.get(key);
    }

    public static void releaseTexture(String key) {
        if (!referenceCounts.containsKey(key)) {
            return;
        }

        int count = referenceCounts.get(key) - 1;
        if (count <= 0) {
            // Remove texture if no references remain
            textureCache.remove(key);
            referenceCounts.remove(key);
        } else {
            referenceCounts.put(key, count);
        }
    }

    public static void clearAll() {
        textureCache.clear();
        referenceCounts.clear();
    }
}