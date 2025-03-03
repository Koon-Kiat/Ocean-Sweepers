package project.game.common.logging.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import project.game.common.logging.core.LogLevel;
import project.game.common.logging.util.LogPaths;
import project.game.engine.api.logging.ILoggerConfig;

/**
 * Configuration settings for the logging system.
 * Following the Single Responsibility Principle, this class only handles
 * configuration settings without any logging logic.
 */
public class LoggerConfig implements ILoggerConfig {
    private String logDirectory = "logs";
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
    private int maxFileSizeInKb = 10240;
    private boolean useColoredConsole = true;
    private boolean includeThreadName = true;
    private boolean dailyRollover = true;
    private String logFormat = "[%1$tF %1$tT] [%2$-5s] [%3$s] %4$s";

    public LoggerConfig() {
    }

    public LoggerConfig(String logDirectory, String logFilePrefix, int maxLogFiles) {
        // Always use global log directory regardless of the input parameter
        this.logDirectory = LogPaths.getGlobalLogDirectory();
        this.logFilePrefix = logFilePrefix;
        this.maxLogFiles = maxLogFiles;
    }

    public LoggerConfig(Level fileLogLevel, Level consoleLogLevel) {
        // Use global log directory and set levels
        this.logDirectory = LogPaths.getGlobalLogDirectory();
        this.fileLogLevel = fileLogLevel;
        this.consoleLogLevel = consoleLogLevel;
    }

    public LoggerConfig(LogLevel fileLogLevel, LogLevel consoleLogLevel) {
        // Use global log directory and set levels
        this.logDirectory = LogPaths.getGlobalLogDirectory();
        this.fileLogLevel = fileLogLevel.getJavaLevel();
        this.consoleLogLevel = consoleLogLevel.getJavaLevel();
    }

    @Override
    public String getLogDirectory() {
        // Always return global log directory
        return LogPaths.getGlobalLogDirectory();
    }

    public LoggerConfig setLogDirectory(String logDirectory) {
        // Always use global log directory regardless of input
        this.logDirectory = LogPaths.getGlobalLogDirectory();
        return this;
    }

    @Override
    public String getLogFilePrefix() {
        return logFilePrefix;
    }

    public LoggerConfig setLogFilePrefix(String logFilePrefix) {
        if (logFilePrefix != null) {
            this.logFilePrefix = logFilePrefix;
        }
        return this;
    }

    @Override
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

    @Override
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public LoggerConfig setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
        return this;
    }

    @Override
    public LogLevel getFileLogLevel() {
        return LogLevel.fromJavaLevel(fileLogLevel);
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

    @Override
    public LogLevel getConsoleLogLevel() {
        return LogLevel.fromJavaLevel(consoleLogLevel);
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

    @Override
    public LogLevel getLogLevelForLogger(String loggerName) {
        return null;
    }

    @Override
    public int getMaxLogFiles() {
        return maxLogFiles;
    }

    public LoggerConfig setMaxLogFiles(int maxLogFiles) {
        if (maxLogFiles >= 0) {
            this.maxLogFiles = maxLogFiles;
        }
        return this;
    }

    @Override
    public boolean isConsoleLoggingEnabled() {
        return useConsoleLogging;
    }

    public LoggerConfig setUseConsoleLogging(boolean useConsoleLogging) {
        this.useConsoleLogging = useConsoleLogging;
        return this;
    }

    @Override
    public boolean isFileLoggingEnabled() {
        return useFileLogging;
    }

    public LoggerConfig setUseFileLogging(boolean useFileLogging) {
        this.useFileLogging = useFileLogging;
        return this;
    }

    @Override
    public boolean isAsyncLoggingEnabled() {
        return useAsyncLogging;
    }

    public LoggerConfig setUseAsyncLogging(boolean useAsyncLogging) {
        this.useAsyncLogging = useAsyncLogging;
        return this;
    }

    @Override
    public int getAsyncQueueSize() {
        return asyncQueueSize;
    }

    public LoggerConfig setAsyncQueueSize(int asyncQueueSize) {
        if (asyncQueueSize > 0) {
            this.asyncQueueSize = asyncQueueSize;
        }
        return this;
    }

    @Override
    public int getMaxFileSizeInKb() {
        return maxFileSizeInKb;
    }

    public LoggerConfig setMaxFileSizeInKb(int maxFileSizeInKb) {
        if (maxFileSizeInKb > 0) {
            this.maxFileSizeInKb = maxFileSizeInKb;
        }
        return this;
    }

    @Override
    public boolean isColoredConsoleEnabled() {
        return useColoredConsole;
    }

    public LoggerConfig setUseColoredConsole(boolean useColoredConsole) {
        this.useColoredConsole = useColoredConsole;
        return this;
    }

    @Override
    public boolean isIncludeThreadName() {
        return includeThreadName;
    }

    public LoggerConfig setIncludeThreadName(boolean includeThreadName) {
        this.includeThreadName = includeThreadName;
        return this;
    }

    @Override
    public boolean isDailyRolloverEnabled() {
        return dailyRollover;
    }

    public LoggerConfig setDailyRollover(boolean dailyRollover) {
        this.dailyRollover = dailyRollover;
        return this;
    }

    @Override
    public String getLogFormat() {
        return logFormat;
    }

    public LoggerConfig setLogFormat(String logFormat) {
        if (logFormat != null && !logFormat.isEmpty()) {
            this.logFormat = logFormat;
        }
        return this;
    }

    @Override
    public String generateLogFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormat.format(new Date());
        return logFilePrefix + "_" + timestamp + logFileExtension;
    }

    @Override
    public LoggerConfig validate() {
        // Force log directory to global path
        this.logDirectory = LogPaths.getGlobalLogDirectory();

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

    @Override
    public LoggerConfig copy() {
        LoggerConfig copy = new LoggerConfig();
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

    public static LoggerConfig forDevelopment() {
        return new LoggerConfig()
                .setConsoleLogLevel(Level.FINE)
                .setFileLogLevel(Level.FINEST)
                .setUseConsoleLogging(true)
                .setUseFileLogging(true)
                .setUseColoredConsole(true)
                .setIncludeThreadName(true)
                .setMaxLogFiles(10);
    }

    public static LoggerConfig forProduction() {
        return new LoggerConfig()
                .setConsoleLogLevel(Level.WARNING)
                .setFileLogLevel(Level.INFO)
                .setUseConsoleLogging(true)
                .setUseFileLogging(true)
                .setUseColoredConsole(false)
                .setIncludeThreadName(false)
                .setMaxLogFiles(5)
                .setUseAsyncLogging(true);
    }
}