package project.game.context.constant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import project.game.common.logging.core.GameLogger;
import project.game.engine.constant.AbstractConfigurationLoader;

/**
 * LibGDX-specific implementation of configuration loading.
 * Handles JSON file loading using LibGDX's file handling and JSON parsing
 * capabilities.
 */
public class GameConfigurationLoader extends AbstractConfigurationLoader {
    private static final GameLogger LOGGER = new GameLogger(GameConfigurationLoader.class);
    private static final GameConfigurationLoader INSTANCE = new GameConfigurationLoader();

    private GameConfigurationLoader() {
        // Private constructor for singleton
    }

    public static GameConfigurationLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Load a configuration profile from a JSON file.
     */
    public boolean loadProfile(String configFile, String profileName) {
        ConfigurableGameConstants constants = ConfigurableGameConstants.getInstance();
        return loadConfiguration(configFile, profileName, constants);
    }

    /**
     * Save the current configuration to a JSON file.
     */
    public boolean saveProfile(String configFile) {
        ConfigurableGameConstants constants = ConfigurableGameConstants.getInstance();
        return saveConfiguration(configFile, constants);
    }

    @Override
    protected Map<String, Object> readConfigurationData(String source) throws IOException {
        FileHandle file = resolveFileHandle(source);
        if (file == null || !file.exists()) {
            LOGGER.error("Configuration file not found: {0}", source);
            return null;
        }

        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(file);
            Map<String, Object> configData = new HashMap<>();

            // Parse float constants
            JsonValue floats = root.get("floats");
            if (floats != null) {
                for (JsonValue entry = floats.child; entry != null; entry = entry.next) {
                    configData.put(entry.name, entry.asFloat());
                    LOGGER.debug("Loaded float constant: {0} = {1}", entry.name, entry.asFloat());
                }
            }

            // Parse long constants
            JsonValue longs = root.get("longs");
            if (longs != null) {
                for (JsonValue entry = longs.child; entry != null; entry = entry.next) {
                    configData.put(entry.name, entry.asLong());
                    LOGGER.debug("Loaded long constant: {0} = {1}", entry.name, entry.asLong());
                }
            }

            LOGGER.info("Successfully loaded {0} constants from {1}", configData.size(), source);
            return configData;
        } catch (GdxRuntimeException e) {
            LOGGER.error("Error parsing JSON file: {0}", source);
            throw new IOException("Error parsing JSON file: " + source, e);
        }
    }

    @Override
    protected boolean writeConfigurationData(String destination, Map<String, Object> data) throws IOException {
        try {
            Map<String, Object> configData = new HashMap<>();
            Map<String, Object> floats = new HashMap<>();
            Map<String, Object> longs = new HashMap<>();

            // Group by type
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Float) {
                    floats.put(entry.getKey(), value);
                } else if (value instanceof Long) {
                    longs.put(entry.getKey(), value);
                }
            }

            configData.put("floats", floats);
            configData.put("longs", longs);

            Json json = new Json();
            json.setUsePrototypes(false);
            String jsonString = json.prettyPrint(configData);

            FileHandle file = Gdx.files.local(destination);
            file.writeString(jsonString, false);
            return true;
        } catch (GdxRuntimeException e) {
            throw new IOException("Error writing JSON file: " + destination, e);
        }
    }

    @Override
    protected void handleWarning(String message) {
        LOGGER.warn(message);
    }

    @Override
    protected void handleError(String message, Exception e) {
        LOGGER.fatal(message + ": " + e.getMessage(), e);
    }

    private FileHandle resolveFileHandle(String path) {
        // For development environment, first try direct file access
        File javaFile = new File(path);
        if (!javaFile.exists()) {
            // Try as resource from classpath
            try {
                URL resource = GameConfigurationLoader.class.getClassLoader().getResource(path);
                if (resource != null) {
                    javaFile = new File(resource.toURI());
                }
            } catch (java.net.URISyntaxException e) {
                handleWarning("Failed to resolve resource URL: " + e.getMessage());
            }
        }

        FileHandle file;
        if (javaFile.exists()) {
            // Use Java file directly when in development
            file = new FileHandle(javaFile);
            LOGGER.info("Loading configuration from development path: {0}", javaFile.getAbsolutePath());
        } else if (Gdx.files != null) {
            // Try LibGDX file handling for runtime
            file = Gdx.files.internal(path);
            if (!file.exists()) {
                file = Gdx.files.absolute(path);
                if (!file.exists()) {
                    file = Gdx.files.local(path);
                    if (!file.exists()) {
                        return null;
                    }
                }
            }
            LOGGER.info("Loading configuration from runtime path: {0}", file.path());
        } else {
            return null;
        }

        return file;
    }
}