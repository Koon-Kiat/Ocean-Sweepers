package project.game.common.logging.factory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.common.logging.adapter.JavaLoggerAdapter;
import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.util.GameLogFormatter;
import project.game.common.logging.util.LogPaths;
import project.game.engine.api.logging.ILogger;
import project.game.engine.api.logging.ILoggerFactory;
import project.game.engine.logging.AbstractLoggerFactory;

/**
 * Implementation of ILoggerFactory that uses Java's built-in logging.
 * This class handles the actual configuration of loggers and handlers.
 */
public class JavaLoggerFactory extends AbstractLoggerFactory implements ILoggerFactory {
    private static final Logger ROOT_LOGGER = Logger.getLogger("");
    private FileHandler fileHandler;
    private JavaLoggerAdapter rootLoggerAdapter;

    /**
     * Creates a new JavaLoggerFactory with the specified configuration.
     * 
     * @param config the logger configuration
     */
    public JavaLoggerFactory(LoggerConfig config) {
        super(config);
        try {
            // First clean up any logs in invalid locations
            LogPaths.cleanupInvalidLogs();
            configureLogging();
        } catch (Exception e) {
            handleConfigurationError(e);
        }
    }

    @Override
    public ILogger getLogger(String name) {
        return JavaLoggerAdapter.getLoggerInstance(name);
    }

    @Override
    public ILogger getRootLogger() {
        return rootLoggerAdapter;
    }

    @Override
    protected void doReconfigure() {
        closeExistingHandlers();
        try {
            configureLogging();
        } catch (Exception e) {
            handleConfigurationError(e);
        }
    }

    @Override
    public void shutdown() {
        closeExistingHandlers();
    }

    /**
     * Handles errors during logger configuration.
     */
    private void handleConfigurationError(Exception e) {
        System.err.println("[ERROR] Failed to configure logging: " + e.getMessage());
        System.err.println("[ERROR] Exception type: " + e.getClass().getName());
        if (e.getCause() != null) {
            System.err.println("[ERROR] Caused by: " + e.getCause().getMessage());
        }
    }

    /**
     * Closes all existing handlers on the root logger.
     */
    private void closeExistingHandlers() {
        for (java.util.logging.Handler h : ROOT_LOGGER.getHandlers()) {
            ROOT_LOGGER.removeHandler(h);
            h.close();
        }
    }

    private void configureLogging() throws Exception {
        // Clean up existing handlers
        closeExistingHandlers();

        // Setup root logger adapter
        rootLoggerAdapter = JavaLoggerAdapter.getLoggerInstance("");

        if (config.isFileLoggingEnabled()) {
            setupFileLogging();
        }

        if (config.isConsoleLoggingEnabled()) {
            setupConsoleLogging();
        }

        // Set the root logger level (using the more restrictive of the console and file
        // levels)
        Level rootLevel = getLowerLevel(config.getConsoleLogLevel(), config.getFileLogLevel());
        ROOT_LOGGER.setLevel(rootLevel);

        // Configure package-specific logging levels
        configurePackageLevels();

        // Centralize logging for game package
        Logger.getLogger("project.game").setLevel(rootLevel);
    }

    /**
     * Configures logging levels for specific packages to control verbosity
     */
    private void configurePackageLevels() {
        // Raise level for window/UI related logging
        Logger.getLogger("com.badlogic.gdx").setLevel(Level.WARNING);

        // Suppress built-in factory debug messages
        Logger.getLogger("serialization").setLevel(Level.INFO);

        // Set level for window resize messages
        Logger.getLogger("project.game.Main").setLevel(Level.INFO);
    }

    private void setupFileLogging() throws IOException {
        // Always use the global log directory path
        String logDirPath = LogPaths.getGlobalLogDirectory();
        File logDir = new File(logDirPath).getAbsoluteFile();

        // Double check we're getting proper absolute path
        if (!logDir.isAbsolute()) {
            throw new IOException("Log directory must be absolute: " + logDirPath);
        }

        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                throw new IOException("Failed to create log directory: " + logDir.getAbsolutePath());
            }
        }

        // Rotate log files if necessary before creating a new one
        rotateLogFiles(logDir);

        // Create the log file with safe file name
        String logFileName = config.generateLogFileName();
        String logFilePath = new File(logDir, logFileName).getAbsolutePath();

        // Create file handler with absolute path
        fileHandler = new FileHandler(logFilePath);
        fileHandler.setLevel(config.getFileLogLevel());

        // Set formatter
        Formatter formatter = createFormatter();
        fileHandler.setFormatter(formatter);

        // Add to root logger
        ROOT_LOGGER.addHandler(fileHandler);

        // Add a shutdown hook to flush/close the file handler
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (fileHandler != null) {
                fileHandler.flush();
                fileHandler.close();
            }
        }));
    }

    private void setupConsoleLogging() {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(config.getConsoleLogLevel());
        consoleHandler.setFormatter(createFormatter());
        ROOT_LOGGER.addHandler(consoleHandler);
    }

    /**
     * Fixed: Properly enforces the maxLogFiles setting by maintaining exactly
     * the specified number of files, including the new one to be created.
     */
    private void rotateLogFiles(File logDir) {
        // Get all existing log files
        File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith(config.getLogFilePrefix())
                && name.endsWith(config.getLogFileExtension()));

        // If we have no configuration for max files or no files exist, return
        int maxFiles = config.getMaxLogFiles();
        if (maxFiles <= 0 || logFiles == null) {
            return;
        }

        // Sort files by last modified time (oldest first)
        Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));

        // Calculate how many files to delete
        // To keep exactly maxFiles total after creating a new one:
        // If we have 5 files now and maxFiles is 5, we need to delete 1 old file
        // to make room for 1 new file (4 old + 1 new = 5 total)
        int filesToDelete = Math.max(0, logFiles.length - (maxFiles - 1));

        if (filesToDelete > 0) {
            System.out.println("[INFO] Current log count: " + logFiles.length +
                    ", max allowed: " + maxFiles + ", deleting " + filesToDelete +
                    " oldest log(s) before creating new one");

            // Delete the oldest files
            for (int i = 0; i < filesToDelete; i++) {
                if (!logFiles[i].delete()) {
                    System.err.println("[WARN] Failed to delete old log file: " + logFiles[i].getAbsolutePath());
                } else {
                    System.out.println("[INFO] Deleted old log file: " + logFiles[i].getName());
                }
            }
        }
    }

    private Formatter createFormatter() {
        return new GameLogFormatter();
    }

    private Level getLowerLevel(Level a, Level b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return a.intValue() < b.intValue() ? a : b;
    }
}