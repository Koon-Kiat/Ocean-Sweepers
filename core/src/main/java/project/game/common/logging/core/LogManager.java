package project.game.common.logging.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

import project.game.engine.api.logging.ILogManager;

/**
 * Base implementation of ILogManager.
 * This class handles log file management and rotation.
 */
public class LogManager implements ILogManager {

    private static volatile LogManager instance;
    private final String logDirectory;
    private int maxLogFiles;
    private File currentLogFile;

    protected LogManager(String logDirectory, int maxLogFiles) {
        this.logDirectory = logDirectory;
        this.maxLogFiles = maxLogFiles;
        initializeLogManager();
    }

    public static synchronized LogManager createInstance(String logDirectory, int maxLogFiles) {
        if (instance == null) {
            instance = new LogManager(logDirectory, maxLogFiles);
        }
        return instance;
    }

    @Override
    public synchronized File getCurrentLogFile() {
        return currentLogFile;
    }

    @Override
    public synchronized void setCurrentLogFile(File logFile) {
        this.currentLogFile = logFile;
    }

    @Override
    public final void cleanupOldLogs() {
        File dir = new File(logDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] logFiles = dir.listFiles(this::isValidLogFile);
        if (logFiles == null || logFiles.length <= maxLogFiles) {
            return;
        }

        // Sort by last modified time
        Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));

        // Delete oldest files until we're at the limit
        for (int i = 0; i < logFiles.length - maxLogFiles; i++) {
            try {
                Files.delete(logFiles[i].toPath());
            } catch (IOException e) {
                // Log error through standard error since logger might not be initialized
                System.err.println("Failed to delete old log file: " + e.getMessage());
            }
        }
    }

    @Override
    public String getLogDirectory() {
        return logDirectory;
    }

    @Override
    public int getMaxLogFiles() {
        return maxLogFiles;
    }

    @Override
    public void setMaxLogFiles(int maxFiles) {
        if (maxFiles > 0) {
            this.maxLogFiles = maxFiles;
            cleanupOldLogs();
        }
    }

    /**
     * Template method that can be overridden by subclasses to customize log file
     * filtering.
     * 
     * @param file the file to check
     * @return true if this is a valid log file that should be included in rotation
     */
    protected boolean isValidLogFile(File file) {
        return file.isFile() && file.getName().endsWith(".log");
    }

    private void initializeLogManager() {
        cleanupOldLogs();
    }
}