package project.game.common.logging.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import project.game.common.logging.util.LogPaths;
import project.game.engine.logging.api.ILogger;
import project.game.engine.logging.api.ILoggerEvent;
import project.game.engine.logging.api.LogLevel;
import project.game.engine.logging.base.AbstractLogger;

public class GameLogger extends AbstractLogger {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Object LOCK = new Object();
    private static volatile PrintWriter sharedFileWriter;
    private static volatile boolean isInitialized = false;
    private static LoggerConfig config;

    static {
        // Default configuration, can be overridden
        config = new LoggerConfig.Builder()
                .withLogDirectory(LogPaths.getGlobalLogDirectory())
                .withMaxLogFiles(10)
                .withDefaultLogLevel(LogLevel.INFO)
                .build();
    }

    public static void configure(LoggerConfig newConfig) {
        synchronized (LOCK) {
            config = newConfig;
            if (isInitialized) {
                // If already initialized, apply new configuration
                LogManager.createInstance(config.getLogDirectory(), config.getMaxLogFiles());
            }
        }
    }

    public GameLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    public GameLogger(String name) {
        super(name, config.getDefaultLogLevel());
        initializeLoggerIfNeeded();
    }

    public void dispose() {
        synchronized (LOCK) {
            if (sharedFileWriter != null) {
                flush();
                sharedFileWriter.close();
                sharedFileWriter = null;
                isInitialized = false;
            }
        }
    }

    @Override
    public void log(ILoggerEvent event) {
        if (isEnabled((LogLevel) event.getLevel())) {
            doLog((LogLevel) event.getLevel(), event.getMessage(), event.getThrowable());
        }
    }

    @Override
    public ILogger getLogger(String name) {
        GameLogger logger = new GameLogger(this.getName() + "." + name);
        return logger;
    }

    @Override
    public void flush() {
        synchronized (LOCK) {
            if (sharedFileWriter != null) {
                sharedFileWriter.flush();
            }
        }
    }

    @Override
    protected void doLog(LogLevel level, String message, Throwable thrown) {
        String timestamp = TIME_FORMATTER.format(LocalDateTime.now());
        String logEntry = MessageFormat.format("{0} [{1}] {2}: {3}",
                timestamp, level, getName(), message);

        // Write to console
        System.out.println(logEntry);

        // Write to file
        synchronized (LOCK) {
            if (sharedFileWriter != null) {
                sharedFileWriter.println(logEntry);
                if (thrown != null) {
                    thrown.printStackTrace(sharedFileWriter);
                    thrown.printStackTrace(System.out);
                }
            }
        }
    }

    private void initializeLoggerIfNeeded() {
        if (!isInitialized) {
            synchronized (LOCK) {
                if (!isInitialized) {
                    LogManager logManager = LogManager.createInstance(
                            config.getLogDirectory(),
                            config.getMaxLogFiles());

                    if (logManager.getCurrentLogFile() == null) {
                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                        File logFile = new File(logManager.getLogDirectory(), timestamp + ".log");
                        try {
                            logFile.getParentFile().mkdirs();
                            sharedFileWriter = new PrintWriter(new FileWriter(logFile, true), true);
                            logManager.setCurrentLogFile(logFile);
                        } catch (IOException e) {
                            System.err.println("Failed to create log file: " + e.getMessage());
                            throw new RuntimeException("Failed to initialize logger", e);
                        }
                    }
                    isInitialized = true;
                }
            }
        }
    }
}