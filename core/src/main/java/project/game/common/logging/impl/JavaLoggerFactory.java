package project.game.common.logging.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import project.game.common.logging.api.AbstractLoggerFactory;
import project.game.common.logging.api.ILogger;
import project.game.common.logging.api.ProjectPathStrategy;
import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.formatter.GameLogFormatter;

/**
 * Implementation of LoggerFactory that uses Java's built-in logging.
 * This class handles the actual configuration of loggers and handlers.
 */
public class JavaLoggerFactory extends AbstractLoggerFactory {
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
        this.pathStrategy = new DefaultProjectPathStrategy();
        try {
            configureLogging();
        } catch (Exception e) {
            handleConfigurationError(e);
        }
    }

    /**
     * Creates a new JavaLoggerFactory with the specified configuration and path
     * strategy.
     * 
     * @param config       the logger configuration
     * @param pathStrategy the project path strategy
     */
    public JavaLoggerFactory(LoggerConfig config, ProjectPathStrategy pathStrategy) {
        super(config, pathStrategy);
        try {
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
        for (java.util.logging.Handler h : ROOT_LOGGER.getHandlers()) {
            ROOT_LOGGER.removeHandler(h);
        }

        // Setup root logger adapter
        rootLoggerAdapter = JavaLoggerAdapter.getLoggerInstance("");

        // Get project root directory
        String projectPath = pathStrategy.getProjectRootPath();

        if (config.isFileLoggingEnabled()) {
            setupFileLogging(projectPath);
        }

        if (config.isConsoleLoggingEnabled()) {
            setupConsoleLogging();
        }

        // Set the root logger level (using the more restrictive of the console and file
        // levels)
        Level rootLevel = getLowerLevel(config.getConsoleLogLevel(), config.getFileLogLevel());
        ROOT_LOGGER.setLevel(rootLevel);

        // Centralize logging
        Logger.getLogger("project.game").setLevel(rootLevel);
    }

    private void setupFileLogging(String projectPath) throws IOException {
        // Create logs directory
        File logDir = new File(projectPath, config.getLogDirectory());
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        // Rotate log files if necessary
        rotateLogFiles(logDir);

        // Create the log file with safe file name
        String logFileName = config.generateLogFileName();
        String logFilePath = new File(logDir, logFileName).getAbsolutePath();

        // Create file handler
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

    private void rotateLogFiles(File logDir) {
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(config.getLogFileExtension()));
        if (logFiles != null && logFiles.length > config.getMaxLogFiles()) {
            Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));
            int filesToDelete = logFiles.length - config.getMaxLogFiles();
            for (int i = 0; i < filesToDelete; i++) {
                logFiles[i].delete();
            }
        }
    }

    private Formatter createFormatter() {
        if (config.getLoggerFormatter() != null) {
            try {
                Class<?> formatterClass = Class.forName(config.getLoggerFormatter());
                return (Formatter) formatterClass.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException e) {
                System.err.println("[WARN] Formatter class not found: " + e.getMessage());
            } catch (InstantiationException | IllegalAccessException e) {
                System.err.println("[WARN] Could not instantiate formatter: " + e.getMessage());
            } catch (ReflectiveOperationException e) {
                System.err.println("[WARN] Reflection error while instantiating formatter: " + e.getMessage());
            }
        }
        // Default to our GameLogFormatter instead of Java's SimpleFormatter
        return new GameLogFormatter();
    }

    private Level getLowerLevel(Level a, Level b) {
        return a.intValue() < b.intValue() ? a : b;
    }
}