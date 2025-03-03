package project.game.common.logging.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import project.game.common.logging.core.LogLevel;
import project.game.common.logging.util.LogPaths;

/**
 * Configuration settings for the logging system.
 * Following the Single Responsibility Principle, this class only handles
 * configuration settings without any logging logic.
 */
public class LoggerConfig {
    private String logDirectory = "logs"; // Default, will be overridden by LogPaths
    private String logFilePrefix = "GameLog";
    private String logFileExtension = ".log";
    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    private Level fileLogLevel = Level.INFO;
    private Level consoleLogLevel = Level.INFO;
    private int maxLogFiles = 5;
    private boolean useConsoleLogging = true;
    private boolean useFileLogging = true;
    private boolean useAsyncLogging = false;
    private int asyncQueueSize = 1000;
    private int maxFileSizeInKb = 10240; // 10MB
    private boolean useColoredConsole = true;
    private boolean includeThreadName = true;
    private boolean dailyRollover = true;
    private String logFormat = "[%1$tF %1$tT] [%2$-5s] [%3$s] %4$s";

    /**
     * Creates a LoggerConfig with default settings.
     */
    public LoggerConfig() {
        // Override default log directory with our global path
        this.logDirectory = LogPaths.getGlobalLogDirectory();
    }

    /**
     * Creates a LoggerConfig with specified directory and file settings.
     * 
     * @param logDirectory  the directory for log files
     * @param logFilePrefix the prefix for log file names
     * @param maxLogFiles   the maximum number of log files to keep
     */
    public LoggerConfig(String logDirectory, String logFilePrefix, int maxLogFiles) {
        // Always use global log directory regardless of the input parameter
        this.logDirectory = LogPaths.getGlobalLogDirectory();
        this.logFilePrefix = logFilePrefix;
        this.maxLogFiles = maxLogFiles;
    }

    /**
     * Creates a LoggerConfig with specified logging levels.
     * 
     * @param fileLogLevel    logging level for file output
     * @param consoleLogLevel logging level for console output
     */
    public LoggerConfig(Level fileLogLevel, Level consoleLogLevel) {
        // Use global log directory and set levels
        this.logDirectory = LogPaths.getGlobalLogDirectory();
        this.fileLogLevel = fileLogLevel;
        this.consoleLogLevel = consoleLogLevel;
    }

    /**
     * Creates a LoggerConfig with specified logging levels.
     * 
     * @param fileLogLevel    logging level for file output
     * @param consoleLogLevel logging level for console output
     */
    public LoggerConfig(LogLevel fileLogLevel, LogLevel consoleLogLevel) {
        // Use global log directory and set levels
        this.logDirectory = LogPaths.getGlobalLogDirectory();
        this.fileLogLevel = fileLogLevel.getJavaLevel();
        this.consoleLogLevel = consoleLogLevel.getJavaLevel();
    }

    // Getters and setters follow the Open/Closed principle by allowing
    // extension of behavior without modifying the class

    public String getLogDirectory() {
        // Always return global log directory
        return LogPaths.getGlobalLogDirectory();
    }

    public LoggerConfig setLogDirectory(String logDirectory) {
        // Always use global log directory regardless of input
        this.logDirectory = LogPaths.getGlobalLogDirectory();
        return this;
    }

    public String getLogFilePrefix() {
        return logFilePrefix;
    }

    public LoggerConfig setLogFilePrefix(String logFilePrefix) {
        if (logFilePrefix != null) {
            this.logFilePrefix = logFilePrefix;
        }
        return this;
    }

    public String getLogFileExtension() {
        return logFileExtension;
    }

    public LoggerConfig setLogFileExtension(String logFileExtension) {
        if (logFileExtension == null || logFileExtension.isEmpty()) {
            this.logFileExtension = ".log";
        } else {
            if (!logFileExtension.startsWith(".")) {
                this.logFileExtension = "." + logFileExtension;
            } else {
                this.logFileExtension = logFileExtension;
            }
        }
        return this;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public LoggerConfig setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
        return this;
    }

    public Level getFileLogLevel() {
        return fileLogLevel;
    }

    public LoggerConfig setFileLogLevel(Level fileLogLevel) {
        this.fileLogLevel = fileLogLevel;
        return this;
    }

    public LoggerConfig setFileLogLevel(LogLevel fileLogLevel) {
        if (fileLogLevel != null) {
            this.fileLogLevel = fileLogLevel.getJavaLevel();
        }
        return this;
    }

    public Level getConsoleLogLevel() {
        return consoleLogLevel;
    }

    public LoggerConfig setConsoleLogLevel(Level consoleLogLevel) {
        this.consoleLogLevel = consoleLogLevel;
        return this;
    }

    public LoggerConfig setConsoleLogLevel(LogLevel consoleLogLevel) {
        if (consoleLogLevel != null) {
            this.consoleLogLevel = consoleLogLevel.getJavaLevel();
        }
        return this;
    }

    public int getMaxLogFiles() {
        return maxLogFiles;
    }

    public LoggerConfig setMaxLogFiles(int maxLogFiles) {
        if (maxLogFiles >= 0) {
            this.maxLogFiles = maxLogFiles;
        }
        return this;
    }

    public boolean isConsoleLoggingEnabled() {
        return useConsoleLogging;
    }

    public LoggerConfig setUseConsoleLogging(boolean useConsoleLogging) {
        this.useConsoleLogging = useConsoleLogging;
        return this;
    }

    public boolean isFileLoggingEnabled() {
        return useFileLogging;
    }

    public LoggerConfig setUseFileLogging(boolean useFileLogging) {
        this.useFileLogging = useFileLogging;
        return this;
    }

    public boolean isAsyncLoggingEnabled() {
        return useAsyncLogging;
    }

    public LoggerConfig setUseAsyncLogging(boolean useAsyncLogging) {
        this.useAsyncLogging = useAsyncLogging;
        return this;
    }

    public int getAsyncQueueSize() {
        return asyncQueueSize;
    }

    public LoggerConfig setAsyncQueueSize(int asyncQueueSize) {
        if (asyncQueueSize > 0) {
            this.asyncQueueSize = asyncQueueSize;
        }
        return this;
    }

    public int getMaxFileSizeInKb() {
        return maxFileSizeInKb;
    }

    public LoggerConfig setMaxFileSizeInKb(int maxFileSizeInKb) {
        if (maxFileSizeInKb > 0) {
            this.maxFileSizeInKb = maxFileSizeInKb;
        }
        return this;
    }

    public boolean isColoredConsoleEnabled() {
        return useColoredConsole;
    }

    public LoggerConfig setUseColoredConsole(boolean useColoredConsole) {
        this.useColoredConsole = useColoredConsole;
        return this;
    }

    public boolean isIncludeThreadName() {
        return includeThreadName;
    }

    public LoggerConfig setIncludeThreadName(boolean includeThreadName) {
        this.includeThreadName = includeThreadName;
        return this;
    }

    public boolean isDailyRolloverEnabled() {
        return dailyRollover;
    }

    public LoggerConfig setDailyRollover(boolean dailyRollover) {
        this.dailyRollover = dailyRollover;
        return this;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public LoggerConfig setLogFormat(String logFormat) {
        if (logFormat != null && !logFormat.isEmpty()) {
            this.logFormat = logFormat;
        }
        return this;
    }

    /**
     * Generates a log file name based on the current configuration.
     * The format is logFilePrefix_timestamp.logFileExtension
     * 
     * @return the full log file name
     */
    public String generateLogFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormat.format(new Date());
        return logFilePrefix + "_" + timestamp + logFileExtension;
    }

    /**
     * Validates and normalizes the configuration settings.
     * This ensures that all settings are valid and compatible.
     * 
     * @return this config instance with validated settings
     */
    public LoggerConfig validate() {
        // Force log directory to global path
        this.logDirectory = LogPaths.getGlobalLogDirectory();

        // Ensure log directory exists
        File dir = new File(logDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Ensure file extension starts with a dot
        if (logFileExtension != null && !logFileExtension.startsWith(".")) {
            logFileExtension = "." + logFileExtension;
        }

        // Ensure max log files is valid
        if (maxLogFiles < 0) {
            maxLogFiles = 0; // Disable log rotation
        }

        return this;
    }

    /**
     * Creates a copy of this configuration.
     *
     * @return a new LoggerConfig with the same settings
     */
    public LoggerConfig copy() {
        LoggerConfig copy = new LoggerConfig();
        // Force log directory to global path
        copy.logDirectory = LogPaths.getGlobalLogDirectory();
        copy.logFilePrefix = this.logFilePrefix;
        copy.logFileExtension = this.logFileExtension;
        copy.dateTimeFormat = this.dateTimeFormat;
        copy.fileLogLevel = this.fileLogLevel;
        copy.consoleLogLevel = this.consoleLogLevel;
        copy.maxLogFiles = this.maxLogFiles;
        copy.useConsoleLogging = this.useConsoleLogging;
        copy.useFileLogging = this.useFileLogging;
        copy.useAsyncLogging = this.useAsyncLogging;
        copy.asyncQueueSize = this.asyncQueueSize;
        copy.maxFileSizeInKb = this.maxFileSizeInKb;
        copy.useColoredConsole = this.useColoredConsole;
        copy.includeThreadName = this.includeThreadName;
        copy.dailyRollover = this.dailyRollover;
        copy.logFormat = this.logFormat;
        return copy;
    }

    /**
     * Creates a LoggerConfig with development-oriented settings.
     * This includes more verbose logging levels and colored console output.
     * 
     * @return a new pre-configured LoggerConfig for development
     */
    public static LoggerConfig forDevelopment() {
        return new LoggerConfig()
                .setConsoleLogLevel(Level.FINE) // DEBUG
                .setFileLogLevel(Level.FINEST) // TRACE
                .setUseConsoleLogging(true)
                .setUseFileLogging(true)
                .setUseColoredConsole(true)
                .setIncludeThreadName(true)
                .setMaxLogFiles(10);
    }

    /**
     * Creates a LoggerConfig with production-oriented settings.
     * This includes less verbose logging and optimized performance.
     * 
     * @return a new pre-configured LoggerConfig for production
     */
    public static LoggerConfig forProduction() {
        return new LoggerConfig()
                .setConsoleLogLevel(Level.WARNING) // WARN
                .setFileLogLevel(Level.INFO) // INFO
                .setUseConsoleLogging(true)
                .setUseFileLogging(true)
                .setUseColoredConsole(false)
                .setIncludeThreadName(false)
                .setMaxLogFiles(5)
                .setUseAsyncLogging(true); // Use async for better performance
    }
}