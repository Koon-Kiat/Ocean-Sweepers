package project.game.common.util;

import java.io.File;
import java.util.logging.Level;

import project.game.Main;
import project.game.common.api.ILogger;
import project.game.common.logging.LogManager;

/**
 * Utility class for resolving project paths.
 */
public class ProjectPaths {
    private static final ILogger LOGGER = LogManager.getLogger(ProjectPaths.class);
    private static String projectRoot;

    static {
        try {
            projectRoot = new File(Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getAbsolutePath();
            LOGGER.log(Level.INFO, "Project root path: {0}", projectRoot);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to resolve project root path: " + e.getMessage(), e);
            // Fallback to working directory
            projectRoot = new File(".").getAbsolutePath();
        }
    }

    /**
     * Get the absolute path to a resource in the project.
     * 
     * @param relativePath The path relative to the project root
     * @return The absolute path to the resource
     */
    public static String getResourcePath(String relativePath) {
        return new File(projectRoot, relativePath).getAbsolutePath();
    }

    /**
     * Get the project root path
     */
    public static String getProjectRoot() {
        return projectRoot;
    }
}