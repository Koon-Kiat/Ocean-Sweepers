package project.game.engine.api.logging;

import java.io.File;

/**
 * Interface for managing log files and rotation.
 * Implementations should handle log file creation, cleanup, and rotation
 * policies.
 */
public interface ILogManager {
    /**
     * Gets the current active log file.
     * 
     * @return the current log file
     */
    File getCurrentLogFile();

    /**
     * Sets the current active log file.
     * 
     * @param logFile the log file to set as current
     */
    void setCurrentLogFile(File logFile);

    /**
     * Cleans up old log files based on the implementation's rotation policy.
     */
    void cleanupOldLogs();

    /**
     * Gets the directory where logs are stored.
     * 
     * @return the log directory path
     */
    String getLogDirectory();

    /**
     * Gets the maximum number of log files to keep.
     * 
     * @return maximum number of log files
     */
    int getMaxLogFiles();

    /**
     * Sets the maximum number of log files to keep.
     * 
     * @param maxFiles maximum number of files
     */
    void setMaxLogFiles(int maxFiles);
}