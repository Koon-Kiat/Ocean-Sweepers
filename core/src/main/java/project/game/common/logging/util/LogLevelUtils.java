package project.game.common.logging.util;

import java.util.logging.Level;

import project.game.common.logging.core.LogLevel;

/**
 * Utility class for LogLevel conversions and operations.
 */
public final class LogLevelUtils {

    private LogLevelUtils() {
        // Prevent instantiation
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts a Java logging level to a LogLevel.
     * 
     * @param level the Java logging level
     * @return the corresponding LogLevel, or INFO if level is null
     */
    public static LogLevel fromJavaLevel(Level level) {
        // Handle null level by defaulting to INFO
        if (level == null) {
            return LogLevel.INFO;
        }

        int levelValue = level.intValue();
        if (levelValue >= Level.OFF.intValue()) {
            return LogLevel.OFF;
        } else if (levelValue <= Level.FINEST.intValue()) {
            return LogLevel.TRACE;
        } else if (levelValue <= Level.FINE.intValue()) {
            return LogLevel.DEBUG;
        } else if (levelValue <= Level.INFO.intValue()) {
            return LogLevel.INFO;
        } else if (levelValue <= Level.WARNING.intValue()) {
            return LogLevel.WARN;
        } else if (levelValue <= Level.SEVERE.intValue()) {
            return LogLevel.ERROR;
        } else {
            return LogLevel.INFO; // Default
        }
    }

    /**
     * Converts a string level name to a LogLevel.
     * 
     * @param levelName the level name (case insensitive)
     * @return the corresponding LogLevel, or INFO if not found
     */
    public static LogLevel fromString(String levelName) {
        if (levelName == null || levelName.isEmpty()) {
            return LogLevel.INFO;
        }

        try {
            // First try to match our LogLevel names
            LogLevel result = LogLevel.fromName(levelName);
            if (result != null) {
                return result;
            }

            // Then try Java level names
            Level javaLevel = Level.parse(levelName);
            return fromJavaLevel(javaLevel);
        } catch (IllegalArgumentException e) {
            // If all fails, return INFO
            return LogLevel.INFO;
        }
    }

    /**
     * Checks if the source level is sufficient to log at the target level.
     * For example, if source is INFO and target is DEBUG, the result is false,
     * but if source is DEBUG and target is INFO, the result is true.
     * 
     * @param sourceLevel the source level
     * @param targetLevel the target level
     * @return true if the source level is sufficient
     */
    public static boolean isLoggable(LogLevel sourceLevel, LogLevel targetLevel) {
        return sourceLevel.getSeverity() <= targetLevel.getSeverity();
    }
}