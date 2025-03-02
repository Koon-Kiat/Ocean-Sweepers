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
        if (levelValue <= Level.FINEST.intValue()) {
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
            return LogLevel.FATAL;
        }
    }

    /**
     * Gets a LogLevel by its name (case-insensitive).
     * 
     * @param name the level name
     * @return the corresponding LogLevel or null if not found
     */
    public static LogLevel getByName(String name) {
        for (LogLevel level : LogLevel.values()) {
            if (level.getName().equalsIgnoreCase(name)) {
                return level;
            }
        }
        return null;
    }
}