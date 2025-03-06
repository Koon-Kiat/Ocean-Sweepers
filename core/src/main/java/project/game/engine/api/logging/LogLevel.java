package project.game.engine.api.logging;

/**
 * Defines the available logging levels in order of increasing severity.
 */
public enum LogLevel {

    TRACE(100),
    DEBUG(200),
    INFO(300),
    WARN(400),
    ERROR(500),
    FATAL(600);

    LogLevel(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }

    public int compareSeverity(LogLevel other) {
        return Integer.compare(this.severity, other.severity);
    }

    private final int severity;

}