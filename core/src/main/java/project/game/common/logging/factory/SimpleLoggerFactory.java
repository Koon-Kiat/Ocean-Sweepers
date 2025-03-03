package project.game.common.logging.factory;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

import project.game.common.logging.config.LoggerConfig;
import project.game.common.logging.core.LogLevel;
import project.game.common.logging.core.SimpleLogger;
import project.game.common.logging.handler.UnifiedLogHandler;
import project.game.common.logging.util.LogPaths;
import project.game.engine.api.logging.ILogger;
import project.game.engine.logging.AbstractLoggerFactory;

/**
 * Factory for creating and managing SimpleLogger instances.
 * Implements a singleton pattern to maintain logger instances.
 */
public class SimpleLoggerFactory extends AbstractLoggerFactory {
    private static final ConcurrentHashMap<String, SimpleLogger> loggers = new ConcurrentHashMap<>();
    private SimpleLogger rootLogger;
    private UnifiedLogHandler unifiedHandler;

    public SimpleLoggerFactory(LoggerConfig config) {
        super(config);
        try {
            LogPaths.cleanupInvalidLogs();
            initialize();
        } catch (Exception e) {
            handleConfigurationError(e);
        }
    }

    private void initialize() {
        try {
            // Create root logger
            rootLogger = new SimpleLogger("root", this);
            loggers.put("root", rootLogger);

            // Create and configure the unified handler
            setupUnifiedHandler();

            // Configure package-specific logging levels
            configurePackageLevels();

        } catch (Exception e) {
            handleConfigurationError(e);
            // Fallback to console-only logging
            unifiedHandler = new UnifiedLogHandler();
            rootLogger.addHandler(unifiedHandler);
        }
    }

    private void setupUnifiedHandler() throws Exception {
        String logPath = null;
        if (config.isFileLoggingEnabled()) {
            logPath = new File(LogPaths.getGlobalLogDirectory(), config.generateLogFileName()).getAbsolutePath();
            rotateLogFiles(new File(LogPaths.getGlobalLogDirectory()));
        }

        if (config.isFileLoggingEnabled() && config.isConsoleLoggingEnabled()) {
            unifiedHandler = new UnifiedLogHandler(logPath, config.isColoredConsoleEnabled());
        } else if (config.isFileLoggingEnabled()) {
            unifiedHandler = new UnifiedLogHandler(logPath);
        } else {
            unifiedHandler = new UnifiedLogHandler();
        }

        rootLogger.addHandler(unifiedHandler);
        rootLogger.setLevel(LogLevel.INFO);
    }

    private void configurePackageLevels() {
        // Set default levels for specific packages
        LogLevel gdxLevel = LogLevel.WARN;
        LogLevel serializationLevel = LogLevel.INFO;
        LogLevel mainLevel = LogLevel.INFO;

        setLoggerLevel("com.badlogic.gdx", gdxLevel);
        setLoggerLevel("serialization", serializationLevel);
        setLoggerLevel("project.game.Main", mainLevel);
    }

    private void setLoggerLevel(String name, LogLevel level) {
        SimpleLogger logger = loggers.computeIfAbsent(name, k -> new SimpleLogger(k, this));
        logger.setLevel(level);
    }

    private void rotateLogFiles(File logDir) {
        File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith(config.getLogFilePrefix()) &&
                name.endsWith(config.getLogFileExtension()));

        int maxFiles = config.getMaxLogFiles();
        if (maxFiles <= 0 || logFiles == null) {
            return;
        }

        Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));

        int filesToDelete = Math.max(0, logFiles.length - (maxFiles - 1));
        if (filesToDelete > 0) {
            for (int i = 0; i < filesToDelete; i++) {
                if (!logFiles[i].delete()) {
                    System.err.println("Failed to delete old log file: " + logFiles[i].getAbsolutePath());
                }
            }
        }
    }

    private void handleConfigurationError(Exception e) {
        System.err.println("Failed to configure logging: " + e.getMessage());
        if (e.getCause() != null) {
            System.err.println("Caused by: " + e.getCause().getMessage());
        }
    }

    @Override
    public ILogger getLogger(String name) {
        SimpleLogger logger = loggers.computeIfAbsent(name, k -> {
            SimpleLogger newLogger = new SimpleLogger(k, this);
            newLogger.addHandler(unifiedHandler);

            LogLevel configuredLevel = config.getLogLevelForLogger(k);
            if (configuredLevel != null) {
                newLogger.setLevel(configuredLevel);
            } else {
                newLogger.setLevel(findParentLevel(k));
            }
            return newLogger;
        });
        return (ILogger) logger;
    }

    private LogLevel findParentLevel(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            String parentName = name.substring(0, lastDot);
            SimpleLogger parent = loggers.get(parentName);
            if (parent != null) {
                return parent.getLevel();
            }
            return findParentLevel(parentName);
        }
        return rootLogger.getLevel();
    }

    @Override
    public ILogger getRootLogger() {
        return (ILogger) rootLogger;
    }

    @Override
    protected void doReconfigure() {
        if (unifiedHandler != null) {
            unifiedHandler.close();
        }
        initialize();

        loggers.values().forEach(logger -> {
            LogLevel configuredLevel = config.getLogLevelForLogger(logger.getName());
            if (configuredLevel != null) {
                logger.setLevel(configuredLevel);
            }
        });
    }

    @Override
    public void shutdown() {
        if (unifiedHandler != null) {
            unifiedHandler.close();
        }
        loggers.clear();
    }
}