package project.game.common.logging.util;

import java.io.File;

/**
 * Utility class for resolving log paths consistently across different runtime
 * environments.
 * This ensures logs are always created in the same location regardless of the
 * current working directory.
 */
public final class LogPaths {
    // The name of the log directory relative to the project root
    private static final String LOG_DIRECTORY_NAME = "logs";

    // Directory detection markers that identify the project root
    private static final String[] PROJECT_MARKERS = {
            "build.gradle", "settings.gradle", "pom.xml", "core", "assets"
    };

    private static String projectRoot;
    private static String logDirectory;

    static {
        initializePaths();
    }

    private LogPaths() {
        // Utility class, no instantiation
    }

    /**
     * Initialize paths by finding the project root directory
     */
    private static void initializePaths() {
        // Start with the working directory
        File currentDir = new File(System.getProperty("user.dir"));
        projectRoot = findProjectRoot(currentDir);
        logDirectory = new File(projectRoot, LOG_DIRECTORY_NAME).getAbsolutePath();
    }

    /**
     * Find the project root by looking for project markers
     */
    private static String findProjectRoot(File startDir) {
        File dir = startDir;

        // Try to find a marker file/directory that indicates the project root
        while (dir != null) {
            int markersFound = 0;
            for (String marker : PROJECT_MARKERS) {
                if (new File(dir, marker).exists()) {
                    markersFound++;
                }
            }

            // If we found multiple markers, this is likely the project root
            if (markersFound >= 2) {
                return dir.getAbsolutePath();
            }

            // Go up one directory
            dir = dir.getParentFile();
        }

        // Fallback to user.dir if we couldn't find the project root
        System.err.println("WARNING: Could not detect project root. Using working directory.");
        return startDir.getAbsolutePath();
    }

    /**
     * Returns the project root directory.
     * 
     * @return the absolute path to the project root
     */
    public static String getProjectRoot() {
        return projectRoot;
    }

    /**
     * Returns the globally fixed absolute path for log files.
     * This is the only place logs should be written to, regardless of
     * how the application is launched.
     * 
     * @return the absolute path to the logs directory
     */
    public static String getGlobalLogDirectory() {
        // Create directory if it doesn't exist
        File dir = new File(logDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return logDirectory;
    }

    /**
     * Creates a full path to a log file with the given name
     * in the global log directory.
     * 
     * @param fileName the log file name
     * @return the absolute path to the log file
     */
    public static String getGlobalLogFilePath(String fileName) {
        return new File(getGlobalLogDirectory(), fileName).getAbsolutePath();
    }

    /**
     * Resolves a path relative to the project root.
     * 
     * @param relativePath the path relative to project root
     * @return the absolute path
     */
    public static String resolveProjectPath(String relativePath) {
        return new File(projectRoot, relativePath).getAbsolutePath();
    }

    /**
     * Cleans up any logs that might have been mistakenly created in
     * invalid locations. This helps with migration.
     */
    public static void cleanupInvalidLogs() {
        // Common locations where logs might be erroneously created
        String[] potentialBadPaths = {
                projectRoot + "\\assets\\logs",
                projectRoot + "\\lwjgl3\\bin\\main\\logs"
        };

        // Get the valid global log directory
        File globalDir = new File(logDirectory);
        if (!globalDir.exists()) {
            globalDir.mkdirs();
        }

        // Move any logs from invalid locations to the global directory
        for (String path : potentialBadPaths) {
            File badDir = new File(path);
            if (badDir.exists()) {
                File[] logFiles = badDir.listFiles((dir, name) -> name.endsWith(".log"));
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        File destFile = new File(globalDir, logFile.getName());
                        if (logFile.renameTo(destFile)) {
                            System.out
                                    .println("[LOG CLEANUP] Moved log file to global directory: " + logFile.getName());
                        }
                    }
                }
                deleteDirectory(badDir);
                System.out.println("[LOG CLEANUP] Removed invalid log directory: " + path);
            }
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     */
    private static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        return dir.delete();
    }
}