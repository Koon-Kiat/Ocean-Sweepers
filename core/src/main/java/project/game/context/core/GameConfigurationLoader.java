package project.game.context.core;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import project.game.common.api.ILogger;
import project.game.common.logging.LogManager;

/**
 * Utility class for loading game configurations from JSON files.
 * Uses LibGDX's JSON parsing capabilities to load configuration profiles.
 */
public class GameConfigurationLoader {
    private static final ILogger LOGGER = LogManager.getLogger(GameConfigurationLoader.class);

    /**
     * Load a configuration profile from a JSON file.
     * 
     * @param configFile  The path to the configuration file
     * @param profileName The name to give this configuration profile
     * @return true if loading was successful, false otherwise
     */
    public static boolean loadConfiguration(String configFile, String profileName) {
        try {
            // For development environment, first try direct file access
            File javaFile = new File(configFile);
            if (!javaFile.exists()) {
                // Try as resource from classpath
                URL resource = GameConfigurationLoader.class.getClassLoader().getResource(configFile);
                if (resource != null) {
                    javaFile = new File(resource.toURI());
                }
            }

            FileHandle file;
            if (javaFile.exists()) {
                // Use Java file directly when in development
                file = new FileHandle(javaFile);
                LOGGER.log(Level.INFO, "Loading configuration from development path: {0}", javaFile.getAbsolutePath());
            } else if (Gdx.files != null) {
                // Try LibGDX file handling for runtime
                file = Gdx.files.internal(configFile);
                if (!file.exists()) {
                    file = Gdx.files.absolute(configFile);
                    if (!file.exists()) {
                        file = Gdx.files.local(configFile);
                        if (!file.exists()) {
                            LOGGER.log(Level.SEVERE, "Configuration file not found at any path: {0}", configFile);
                            return false;
                        }
                    }
                }
                LOGGER.log(Level.INFO, "Loading configuration from runtime path: {0}", file.path());
            } else {
                LOGGER.log(Level.SEVERE, "Unable to load configuration file: {0}", configFile);
                return false;
            }

            LOGGER.log(Level.INFO, "Loading configuration from: {0}", file.path());

            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(file);

            ConfigurableGameConstants constants = ConfigurableGameConstants.getInstance();
            constants.createProfile(profileName);

            // Validate against registry before loading
            Map<String, ConstantDefinition> validationMap = new HashMap<>();
            for (String key : ConstantsRegistry.getAllKeys()) {
                validationMap.put(key, ConstantsRegistry.get(key));
            }

            // Load float constants
            JsonValue floats = root.get("floats");
            if (floats != null) {
                for (JsonValue entry = floats.child; entry != null; entry = entry.next) {
                    String key = entry.name;
                    ConstantDefinition def = validationMap.get(key);

                    if (def == null) {
                        LOGGER.log(Level.WARNING, "Unknown constant in config file: {0}", key);
                        continue;
                    }

                    if (def.getType() != Float.class) {
                        LOGGER.log(Level.WARNING, "Type mismatch for constant {0}. Expected Float", key);
                        continue;
                    }

                    constants.setValue(key, entry.asFloat());
                }
            }

            // Load long constants
            JsonValue longs = root.get("longs");
            if (longs != null) {
                for (JsonValue entry = longs.child; entry != null; entry = entry.next) {
                    String key = entry.name;
                    ConstantDefinition def = validationMap.get(key);

                    if (def == null) {
                        LOGGER.log(Level.WARNING, "Unknown constant in config file: {0}", key);
                        continue;
                    }

                    if (def.getType() != Long.class) {
                        LOGGER.log(Level.WARNING, "Type mismatch for constant {0}. Expected Long", key);
                        continue;
                    }

                    constants.setValue(key, entry.asLong());
                }
            }

            // Verify all required constants are present
            for (String key : ConstantsRegistry.getAllKeys()) {
                if (!constants.hasValue(profileName, key)) {
                    ConstantDefinition def = ConstantsRegistry.get(key);
                    LOGGER.log(Level.WARNING, "Missing constant {0} in profile {1}. Using default value: {2}",
                            new Object[] { key, profileName, def.getDefaultValue() });
                    constants.setValue(key, def.getDefaultValue());
                }
            }

            LOGGER.log(Level.INFO, "Successfully loaded configuration profile: {0}", profileName);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading configuration: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Save the current configuration to a JSON file.
     * 
     * @param configFile The path where to save the configuration
     * @return true if saving was successful, false otherwise
     */
    public static boolean saveConfiguration(String configFile) {
        try {
            ConfigurableGameConstants constants = ConfigurableGameConstants.getInstance();
            Map<String, Object> configData = new HashMap<>();
            Map<String, Object> floats = new HashMap<>();
            Map<String, Object> longs = new HashMap<>();

            // Group constants by type
            for (String key : ConstantsRegistry.getAllKeys()) {
                ConstantDefinition def = ConstantsRegistry.get(key);
                Object value = constants.getRawValue(key);

                if (def.getType() == Float.class) {
                    floats.put(key, value);
                } else if (def.getType() == Long.class) {
                    longs.put(key, value);
                }
            }

            configData.put("floats", floats);
            configData.put("longs", longs);

            Json json = new Json();
            json.setUsePrototypes(false);
            String jsonString = json.prettyPrint(configData);

            FileHandle file = Gdx.files.local(configFile);
            file.writeString(jsonString, false);

            LOGGER.log(Level.INFO, "Successfully saved configuration to: {0}", configFile);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving configuration: " + e.getMessage(), e);
            return false;
        }
    }
}