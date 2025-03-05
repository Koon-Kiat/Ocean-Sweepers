package project.game.common.logging.util;

import java.io.File;

/**
 * Utility class for resolving log paths consistently across different runtime
 * environments.
 * This ensures logs are always created in the same location regardless of the
 * current working directory.
 */
public final class LogPaths {

    // The one, fixed location for all logs
    private static final String GLOBAL_LOG_DIRECTORY = "D:\\SIT\\year1\\Year_1_tri_2\\INF1009_oop\\vsoopproj\\OOPProject\\logs";

    // The project root directory
    private static final String PROJECT_ROOT = "D:\\SIT\\year1\\Year_1_tri_2\\INF1009_oop\\vsoopproj\\OOPProject";

    private LogPaths() {
        // Utility class, no instantiation
    }

    /**
     * Returns the project root directory.
     * 
     * @return the absolute path to the project root
     */
    public static String getProjectRoot() {
        return PROJECT_ROOT;
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
        File dir = new File(GLOBAL_LOG_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return GLOBAL_LOG_DIRECTORY;
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
        return new File(PROJECT_ROOT, relativePath).getAbsolutePath();
    }

    /**
     * Cleans up any logs that might have been mistakenly created in
     * invalid locations. This helps with migration.
     */
    public static void cleanupInvalidLogs() {
        // Common locations where logs might be erroneously created
        String[] potentialBadPaths = {
                PROJECT_ROOT + "\\assets\\logs",
                PROJECT_ROOT + "\\lwjgl3\\bin\\main\\logs"
        };

        // Get the valid global log directory
        File globalDir = new File(GLOBAL_LOG_DIRECTORY);
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