package project.game.application.entity.flyweight;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

public class TextureFlyweightFactory {
    private static final Map<String, TextureRegion> textureCache = new HashMap<>();

    public static void addTexture(String key, TextureRegion texture) {
        textureCache.putIfAbsent(key, texture);
    }

    public static TextureRegion getTexture(String key) {
        return textureCache.get(key);
    }
}