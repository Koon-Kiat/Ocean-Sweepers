package project.game.common.util;

import java.io.File;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import project.game.common.logging.util.LogPaths;

/**
 * Utility class for finding project paths and resources.
 */
public final class ProjectPaths {

    private ProjectPaths() {
        // Utility class, no instantiation
    }

    /**
     * Gets the absolute path to the project's root directory.
     * Uses LogPaths for a consistent implementation.
     * 
     * @return the path to the project root
     */
    public static String getProjectRoot() {
        // Use LogPaths for consistent path handling
        return LogPaths.getProjectRoot();
    }

    /**
     * Gets the path to a resource file, handling both development and production
     * environments.
     * 
     * @param relativePath the relative path to the resource from the project root
     * @return the absolute path to the resource
     */
    public static String getResourcePath(String relativePath) {
        // First try to get it as an internal file (for production/packaged game)
        FileHandle internal = Gdx.files.internal(relativePath);
        if (internal.exists()) {
            return internal.path();
        }

        // If not found as internal, try project root (for development)
        String projectRoot = getProjectRoot();
        File resourceFile = new File(projectRoot, relativePath);

        // Check core/assets directory (common in libGDX projects)
        if (!resourceFile.exists()) {
            resourceFile = new File(projectRoot, "core/assets/" + relativePath);
        }

        // Check assets directory
        if (!resourceFile.exists()) {
            resourceFile = new File(projectRoot, "assets/" + relativePath);
        }

        // If still not found, try the absolute path
        if (!resourceFile.exists()) {
            resourceFile = new File(relativePath);
        }

        return resourceFile.getAbsolutePath();
    }

    /**
     * Gets the config directory path.
     * 
     * @return the path to the config directory
     */
    public static String getConfigPath() {
        String projectRoot = getProjectRoot();
        // Try multiple possible config locations
        String[] possiblePaths = {
                "core/src/main/resources/config",
                "core/assets/config",
                "assets/config",
                "config",
                "core/src/main/java/project/game/config"
        };

        for (String path : possiblePaths) {
            File configDir = new File(projectRoot, path);
            if (configDir.exists() && configDir.isDirectory()) {
                return configDir.getAbsolutePath();
            }
        }

        // If no config directory found, create one in core/assets/config
        File defaultConfigDir = new File(projectRoot, "core/assets/config");
        defaultConfigDir.mkdirs();
        return defaultConfigDir.getAbsolutePath();
    }

    /**
     * Gets the path to a config file, searching in multiple possible locations.
     * 
     * @param configFileName the name of the config file
     * @return the absolute path to the config file, or null if not found
     */
    public static String findConfigFile(String configFileName) {
        String configDir = getConfigPath();
        File configFile = new File(configDir, configFileName);

        if (configFile.exists()) {
            return configFile.getAbsolutePath();
        }

        // Try as a resource file
        String resourcePath = getResourcePath("config/" + configFileName);
        File resourceFile = new File(resourcePath);
        if (resourceFile.exists()) {
            return resourceFile.getAbsolutePath();
        }

        // Try in core/src/main/java/project/game/config
        String projectRoot = getProjectRoot();
        File sourceConfigFile = new File(projectRoot, "core/src/main/java/project/game/config/" + configFileName);
        if (sourceConfigFile.exists()) {
            return sourceConfigFile.getAbsolutePath();
        }

        return null;
    }

    /**
     * Gets the path to a config file, searching in multiple possible locations.
     * This overload allows specifying a specific project root.
     * 
     * @param configFileName the name of the config file
     * @param projectRoot    the project root directory to use for searching
     * @return the absolute path to the config file, or null if not found
     */
    public static String findConfigFile(String configFileName, String projectRoot) {
        // Try the standard config path locations with the specified root
        String[] possiblePaths = {
                "core/src/main/resources/config",
                "core/assets/config",
                "assets/config",
                "config",
                "core/src/main/java/project/game/config"
        };

        for (String path : possiblePaths) {
            File configFile = new File(new File(projectRoot, path), configFileName);
            if (configFile.exists()) {
                return configFile.getAbsolutePath();
            }
        }

        // If not found in expected locations, try as a resource file
        try {
            if (Gdx.files != null) {
                FileHandle internal = Gdx.files.internal("config/" + configFileName);
                if (internal.exists()) {
                    return internal.path();
                }
            }
        } catch (Exception e) {
            // Gdx might not be initialized, just continue
        }

        return null;
    }
}