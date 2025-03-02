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
}