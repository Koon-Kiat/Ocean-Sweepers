package project.game.engine.api.logging;

import project.game.common.logging.core.LogLevel;

public interface ILoggerConfig {

    String getLogDirectory();

    String getLogFilePrefix();

    String getLogFileExtension();

    String getDateTimeFormat();

    LogLevel getFileLogLevel();

    LogLevel getConsoleLogLevel();

    LogLevel getLogLevelForLogger(String loggerName);

    int getMaxLogFiles();

    boolean isConsoleLoggingEnabled();

    boolean isFileLoggingEnabled();

    boolean isAsyncLoggingEnabled();

    int getAsyncQueueSize();

    int getMaxFileSizeInKb();

    boolean isColoredConsoleEnabled();

    boolean isIncludeThreadName();

    boolean isDailyRolloverEnabled();

    String getLogFormat();

    String generateLogFileName();

    ILoggerConfig validate();

    ILoggerConfig copy();
}