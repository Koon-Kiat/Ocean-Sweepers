package project.game.common.logging.core;

import java.util.logging.Level;

/**
 * Game-specific log levels that map to Java's standard logging levels.
 * This enum provides a more game-oriented and type-safe approach to log levels.
 */
public enum LogLevel {
    /**
     * Detailed tracing information, typically used during development.
     */
    TRACE(Level.FINEST, "TRACE"),

    /**
     * Debugging information useful during development.
     */
    DEBUG(Level.FINE, "DEBUG"),

    /**
     * General information about game operation.
     */
    INFO(Level.INFO, "INFO"),

    /**
     * Warnings that don't prevent the game from working but indicate potential
     * issues.
     */
    WARN(Level.WARNING, "WARN"),

    /**
     * Errors that may impact gameplay but don't crash the game.
     */
    ERROR(Level.SEVERE, "ERROR"),

    /**
     * Critical errors that may crash the game or prevent it from functioning.
     */
    FATAL(Level.SEVERE, "FATAL");

    private final Level javaLevel;
    private final String name;

    /**
     * Creates a new LogLevel.
     * 
     * @param javaLevel the corresponding Java logging level
     * @param name      the name of this level
     */
    private LogLevel(Level javaLevel, String name) {
        this.javaLevel = javaLevel;
        this.name = name;
    }

    /**
     * Gets the corresponding Java logging level.
     * 
     * @return the Java logging level
     */
    public Level getJavaLevel() {
        return javaLevel;
    }

    /**
     * Gets the name of this level.
     * 
     * @return the level name
     */
    public String getName() {
        return name;
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
            return INFO;
        }

        int levelValue = level.intValue();
        if (levelValue <= Level.FINEST.intValue()) {
            return TRACE;
        } else if (levelValue <= Level.FINE.intValue()) {
            return DEBUG;
        } else if (levelValue <= Level.INFO.intValue()) {
            return INFO;
        } else if (levelValue <= Level.WARNING.intValue()) {
            return WARN;
        } else if (levelValue <= Level.SEVERE.intValue()) {
            return ERROR;
        } else {
            return FATAL;
        }
    }

    /**
     * Gets a LogLevel by its name (case-insensitive).
     * 
     * @param name the level name
     * @return the corresponding LogLevel or null if not found
     */
    public static LogLevel getByName(String name) {
        for (LogLevel level : values()) {
            if (level.name.equalsIgnoreCase(name)) {
                return level;
            }
        }
        return null;
    }
}