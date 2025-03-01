package project.game.common.logging;

import java.text.SimpleDateFormat;
import java.util.logging.Level;

/**
 * Configuration settings for the logging system.
 * Following the Single Responsibility Principle, this class only handles
 * configuration settings without any logging logic.
 */
public class LoggerConfig {
    // Use default values that are more generic
    private String logDirectory = "core\\src\\main\\java\\project\\game\\log";
    private String logFilePrefix = "app";
    private String logFileExtension = ".log";
    private String dateTimeFormat = "yyyy-MM-dd_HH-mm-ss";
    private Level fileLogLevel = Level.INFO;
    private Level consoleLogLevel = Level.INFO;
    private int maxLogFiles = 10;
    private boolean useConsoleLogging = true;
    private boolean useFileLogging = true;
    private String loggerFormatter = null;

    /**
     * Creates a default LoggerConfig instance.
     */
    public LoggerConfig() {
        // Default constructor with default values
    }

    /**
     * Creates a LoggerConfig with specified directory and file settings.
     * 
     * @param logDirectory  directory where log files are stored
     * @param logFilePrefix prefix for log files
     * @param maxLogFiles   maximum number of log files to keep
     */
    public LoggerConfig(String logDirectory, String logFilePrefix, int maxLogFiles) {
        this.logDirectory = logDirectory;
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
        this.fileLogLevel = fileLogLevel;
        this.consoleLogLevel = consoleLogLevel;
    }

    // Getters and setters follow the Open/Closed principle by allowing
    // extension of behavior without modifying the class

    public String getLogDirectory() {
        return logDirectory;
    }

    public LoggerConfig setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
        return this;
    }

    public String getLogFilePrefix() {
        return logFilePrefix;
    }

    public LoggerConfig setLogFilePrefix(String logFilePrefix) {
        this.logFilePrefix = logFilePrefix;
        return this;
    }

    public String getLogFileExtension() {
        return logFileExtension;
    }

    public LoggerConfig setLogFileExtension(String logFileExtension) {
        this.logFileExtension = logFileExtension;
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

    public Level getConsoleLogLevel() {
        return consoleLogLevel;
    }

    public LoggerConfig setConsoleLogLevel(Level consoleLogLevel) {
        this.consoleLogLevel = consoleLogLevel;
        return this;
    }

    public int getMaxLogFiles() {
        return maxLogFiles;
    }

    public LoggerConfig setMaxLogFiles(int maxLogFiles) {
        this.maxLogFiles = maxLogFiles;
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

    public String getLoggerFormatter() {
        return loggerFormatter;
    }

    public LoggerConfig setLoggerFormatter(String loggerFormatter) {
        this.loggerFormatter = loggerFormatter;
        return this;
    }

    /**
     * Creates a new SimpleDateFormat with the configured date-time format.
     *
     * @return a new SimpleDateFormat instance
     */
    public SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat(dateTimeFormat);
    }

    /**
     * Creates a copy of this configuration.
     *
     * @return a new LoggerConfig with the same settings
     */
    public LoggerConfig copy() {
        LoggerConfig copy = new LoggerConfig();
        copy.logDirectory = this.logDirectory;
        copy.logFilePrefix = this.logFilePrefix;
        copy.logFileExtension = this.logFileExtension;
        copy.dateTimeFormat = this.dateTimeFormat;
        copy.fileLogLevel = this.fileLogLevel;
        copy.consoleLogLevel = this.consoleLogLevel;
        copy.maxLogFiles = this.maxLogFiles;
        copy.useConsoleLogging = this.useConsoleLogging;
        copy.useFileLogging = this.useFileLogging;
        copy.loggerFormatter = this.loggerFormatter;
        return copy;
    }
}