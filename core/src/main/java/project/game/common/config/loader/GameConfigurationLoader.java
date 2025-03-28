package project.game.common.config.loader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import project.game.common.logging.core.GameLogger;
import project.game.engine.constant.io.AbstractConfigurationLoader;

/**
 * LibGDX-specific implementation of configuration loading.
 */
public class GameConfigurationLoader extends AbstractConfigurationLoader {

    private static final GameLogger LOGGER = new GameLogger(GameConfigurationLoader.class);
    private static final GameConfigurationLoader INSTANCE = new GameConfigurationLoader();
    private final JsonReader jsonReader;

    // Type categorizers
    private final Map<String, Predicate<Object>> typeCategorizers = new HashMap<>();

    private GameConfigurationLoader() {
        this.jsonReader = new JsonReader();
        initTypeCategorizers();
    }

    private void initTypeCategorizers() {
        // Check if value is a floating-point number
        typeCategorizers.put("floatTypes", value -> value != null && (value.getClass() == Float.class ||
                value.getClass() == Double.class));

        // Check if value is a long
        typeCategorizers.put("longTypes", value -> value != null &&
                value.getClass() == Long.class);
    }

    public static GameConfigurationLoader getInstance() {
        return INSTANCE;
    }

    @Override
    public Map<String, Object> readConfigurationData(String source) throws IOException {
        try {
            FileHandle fileHandle = resolveFileHandle(source);
            if (!fileHandle.exists()) {
                handleWarning("Configuration file does not exist: " + source);
                return new HashMap<>();
            }

            String jsonContent = fileHandle.readString();
            JsonValue root = jsonReader.parse(jsonContent);
            Map<String, Object> configData = new HashMap<>();

            // Handle floats
            JsonValue floats = root.get("floats");
            if (floats != null) {
                JsonValue entry = floats.child;
                while (entry != null) {
                    configData.put(entry.name, entry.asFloat());
                    entry = entry.next;
                }
            }

            // Handle longs
            JsonValue longs = root.get("longs");
            if (longs != null) {
                JsonValue entry = longs.child;
                while (entry != null) {
                    configData.put(entry.name, entry.asLong());
                    entry = entry.next;
                }
            }

            return configData;
        } catch (Exception e) {
            handleError("Failed to read configuration data", e);
            throw new IOException("Failed to read configuration", e);
        }
    }

    @Override
    public boolean writeConfigurationData(String destination, Map<String, Object> data) throws IOException {
        try {
            FileHandle fileHandle = resolveFileHandle(destination);
            Map<String, Object> categorizedData = new HashMap<>();
            Map<String, Object> floats = new HashMap<>();
            Map<String, Object> longs = new HashMap<>();

            // Categorize values by type using predicates
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object value = entry.getValue();

                if (typeCategorizers.get("floatTypes").test(value)) {
                    floats.put(entry.getKey(), value);
                } else if (typeCategorizers.get("longTypes").test(value)) {
                    longs.put(entry.getKey(), value);
                }
            }

            categorizedData.put("floats", floats);
            categorizedData.put("longs", longs);

            String jsonContent = new Json().prettyPrint(categorizedData);
            fileHandle.writeString(jsonContent, false);
            return true;
        } catch (Exception e) {
            handleError("Failed to write configuration data", e);
            return false;
        }
    }

    @Override
    protected void handleWarning(String message) {
        LOGGER.warn(message);
    }

    @Override
    protected void handleError(String message, Exception e) {
        LOGGER.error(message, e);
    }

    private FileHandle resolveFileHandle(String path) {
        if (Gdx.files == null) {
            // Running outside of LibGDX context (e.g. tests)
            return new FileHandle(path);
        }
        if (path.startsWith("classpath:")) {
            return Gdx.files.internal(path.substring("classpath:".length()));
        }
        return Gdx.files.absolute(path);
    }
}