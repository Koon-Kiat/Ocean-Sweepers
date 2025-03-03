package project.game.common.logging.core;

import java.util.logging.Level;

/**
 * Abstract representation of log levels that can be mapped to various logging
 * backends.
 * This enum provides a platform-independent approach to log levels.
 */
public enum LogLevel {
    /**
     * Detailed tracing information, typically used during development.
     */
    TRACE(Level.FINEST, "TRACE", 100),

    /**
     * Debugging information useful during development.
     */
    DEBUG(Level.FINE, "DEBUG", 200),

    /**
     * General information about system operation.
     */
    INFO(Level.INFO, "INFO", 300),

    /**
     * Warnings that don't prevent the system from working but indicate potential
     * issues.
     */
    WARN(Level.WARNING, "WARN", 400),

    /**
     * Errors that may impact functionality but don't crash the system.
     */
    ERROR(Level.SEVERE, "ERROR", 500),

    /**
     * Critical errors that may crash the system or prevent it from functioning.
     */
    FATAL(Level.SEVERE, "FATAL", 600),

    /**
     * Level indicating that no messages should be logged.
     */
    OFF(Level.OFF, "OFF", Integer.MAX_VALUE);

    private final Level javaLevel;
    private final String name;
    private final int severity;

    /**
     * Creates a LogLevel with the specified Java level, name, and severity.
     * 
     * @param javaLevel the corresponding Java logging level
     * @param name      the name of this level
     * @param severity  the severity value (higher values are more severe)
     */
    LogLevel(Level javaLevel, String name, int severity) {
        this.javaLevel = javaLevel;
        this.name = name;
        this.severity = severity;
    }

    /**
     * Gets the Java logging level equivalent.
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
     * Gets the severity value of this level.
     * Higher values indicate more severe levels.
     * 
     * @return the severity value
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Looks up a LogLevel by its name (case-insensitive).
     * 
     * @param name the level name
     * @return the matching LogLevel, or null if not found
     */
    public static LogLevel fromName(String name) {
        if (name == null) {
            return null;
        }

        for (LogLevel level : values()) {
            if (level.name.equalsIgnoreCase(name)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Converts a Java logging level to a LogLevel.
     * 
     * @param level the Java logging level
     * @return the corresponding LogLevel
     */
    public static LogLevel fromJavaLevel(Level level) {
        if (level == null) {
            return INFO; // Default to INFO if null
        }

        int value = level.intValue();

        if (value >= Level.OFF.intValue()) {
            return OFF;
        } else if (value >= Level.SEVERE.intValue()) {
            return ERROR; // Default to ERROR for SEVERE
        } else if (value >= Level.WARNING.intValue()) {
            return WARN;
        } else if (value >= Level.INFO.intValue()) {
            return INFO;
        } else if (value >= Level.FINE.intValue()) {
            return DEBUG;
        } else {
            return TRACE;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}