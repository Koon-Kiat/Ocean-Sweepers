package project.game.common.logging;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import project.game.Main;

/**
 * Manages the logging system for the game. Provides a configurable, centralized
 * logging facility.
 * 
 * This class follows the Single Responsibility Principle by focusing only on
 * managing loggers. It follows the Open/Closed Principle by allowing extension
 * without modification through the Builder pattern.
 */
public class LogManager {
    private static final Logger ROOT_LOGGER = Logger.getLogger("");
    private static LogManager instance;
    private LoggerConfig config;
    private FileHandler fileHandler;
    private JavaLoggerAdapter rootLoggerAdapter;

    // Strategy for obtaining project root path - can be overridden for testing
    private ProjectPathStrategy pathStrategy = new DefaultProjectPathStrategy();

    /**
     * Private constructor to enforce singleton pattern
     */
    private LogManager(LoggerConfig config) {
        this.config = config;
        try {
            configureLogging();
        } catch (Exception e) {
            handleConfigurationError(e);
        }
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
     * Gets the singleton instance of LogManager, initializing it if necessary.
     * 
     * @return the LogManager instance
     */
    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager(new LoggerConfig());
        }
        return instance;
    }

    /**
     * Initializes the logging system with default configuration.
     */
    public static void initialize() {
        getInstance();
    }

    /**
     * Initializes the logging system with custom configuration.
     * 
     * @param config the logger configuration
     */
    public static void initialize(LoggerConfig config) {
        if (instance == null) {
            instance = new LogManager(config);
        } else {
            instance.reconfigure(config);
        }
    }

    /**
     * Gets an ILogger for the specified name.
     * 
     * @param name the logger name
     * @return the ILogger instance
     */
    public static ILogger getLogger(String name) {
        getInstance();
        return JavaLoggerAdapter.getLoggerInstance(name);
    }

    /**
     * Gets an ILogger for the specified class.
     * 
     * @param clazz the class
     * @return the ILogger instance
     */
    public static ILogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Gets the root logger adapter.
     * 
     * @return the root logger adapter
     */
    public ILogger getRootLogger() {
        return rootLoggerAdapter;
    }

    /**
     * Sets the strategy for determining project root path.
     * Useful for testing or when deploying to different environments.
     * 
     * @param pathStrategy the path strategy to use
     */
    public void setPathStrategy(ProjectPathStrategy pathStrategy) {
        this.pathStrategy = pathStrategy;
    }

    /**
     * Reconfigures the logging system with new settings.
     * 
     * @param newConfig the new logger configuration
     */
    public void reconfigure(LoggerConfig newConfig) {
        closeExistingHandlers();
        this.config = newConfig;
        try {
            configureLogging();
        } catch (Exception e) {
            handleConfigurationError(e);
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

        // Create the log file with timestamp
        String timestamp = config.createDateFormat().format(new Date());
        String logFileName = config.getLogFilePrefix() + "_" + timestamp + config.getLogFileExtension();
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
        return new SimpleFormatter();
    }

    private Level getLowerLevel(Level a, Level b) {
        return a.intValue() < b.intValue() ? a : b;
    }

    /**
     * Gets the current configuration.
     * 
     * @return the current logger configuration
     */
    public LoggerConfig getConfig() {
        return config;
    }

    /**
     * Strategy interface for determining project root path.
     * Follows the Strategy pattern to allow different implementations.
     */
    public interface ProjectPathStrategy {
        String getProjectRootPath() throws Exception;
    }

    /**
     * Default implementation of ProjectPathStrategy that uses the Main class
     * location.
     */
    private static class DefaultProjectPathStrategy implements ProjectPathStrategy {
        @Override
        public String getProjectRootPath() throws URISyntaxException {
            return new File(Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getAbsolutePath();
        }
    }

    /**
     * Builder for LogManager configuration.
     * Implements the Builder pattern to provide a fluent API for configuration.
     */
    public static class Builder {
        private final LoggerConfig config = new LoggerConfig();

        /**
         * Sets the log directory.
         * 
         * @param directory the log directory path
         * @return this builder for chaining
         */
        public Builder logDirectory(String directory) {
            config.setLogDirectory(directory);
            return this;
        }

        /**
         * Sets the log file prefix.
         * 
         * @param prefix the log file prefix
         * @return this builder for chaining
         */
        public Builder logFilePrefix(String prefix) {
            config.setLogFilePrefix(prefix);
            return this;
        }

        /**
         * Sets the log file extension.
         * 
         * @param extension the log file extension
         * @return this builder for chaining
         */
        public Builder logFileExtension(String extension) {
            config.setLogFileExtension(extension);
            return this;
        }

        /**
         * Sets the date-time format for log filenames.
         * 
         * @param format the date-time format
         * @return this builder for chaining
         */
        public Builder dateTimeFormat(String format) {
            config.setDateTimeFormat(format);
            return this;
        }

        /**
         * Sets the file logging level.
         * 
         * @param level the file logging level
         * @return this builder for chaining
         */
        public Builder fileLogLevel(Level level) {
            config.setFileLogLevel(level);
            return this;
        }

        /**
         * Sets the console logging level.
         * 
         * @param level the console logging level
         * @return this builder for chaining
         */
        public Builder consoleLogLevel(Level level) {
            config.setConsoleLogLevel(level);
            return this;
        }

        /**
         * Sets the maximum number of log files to keep.
         * 
         * @param maxFiles the maximum number of log files
         * @return this builder for chaining
         */
        public Builder maxLogFiles(int maxFiles) {
            config.setMaxLogFiles(maxFiles);
            return this;
        }

        /**
         * Enables or disables console logging.
         * 
         * @param enabled true to enable console logging
         * @return this builder for chaining
         */
        public Builder consoleLogging(boolean enabled) {
            config.setUseConsoleLogging(enabled);
            return this;
        }

        /**
         * Enables or disables file logging.
         * 
         * @param enabled true to enable file logging
         * @return this builder for chaining
         */
        public Builder fileLogging(boolean enabled) {
            config.setUseFileLogging(enabled);
            return this;
        }

        /**
         * Sets the formatter class name for logs.
         * 
         * @param formatterClassName the formatter class name
         * @return this builder for chaining
         */
        public Builder formatter(String formatterClassName) {
            config.setLoggerFormatter(formatterClassName);
            return this;
        }

        /**
         * Builds the LoggerConfig.
         * 
         * @return the configured LoggerConfig
         */
        public LoggerConfig build() {
            return config;
        }

        /**
         * Initializes the LogManager with this configuration.
         */
        public void initialize() {
            LogManager.initialize(config);
        }
    }
}
