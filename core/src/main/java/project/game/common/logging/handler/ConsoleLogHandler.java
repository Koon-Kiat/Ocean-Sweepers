package project.game.common.logging.handler;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import project.game.common.logging.core.LogLevel;
import project.game.engine.api.logging.ILogEvent;
import project.game.engine.logging.AbstractLogHandler;

/**
 * Log handler that writes log events to the console (stdout/stderr).
 */
public class ConsoleLogHandler extends AbstractLogHandler {
    private static final String DEFAULT_FORMAT = "[%1$tF %1$tT] [%2$-5s] [%3$s] %4$s";
    private final PrintStream infoStream;
    private final PrintStream errorStream;
    private final String format;
    private final SimpleDateFormat dateFormat;
    private final boolean useColors;

    /**
     * Creates a new ConsoleLogHandler with the default format and no colors.
     */
    public ConsoleLogHandler() {
        this(DEFAULT_FORMAT, System.out, System.err, false);
    }

    /**
     * Creates a new ConsoleLogHandler with the specified format.
     *
     * @param format      the format string for log messages (using String.format
     *                    syntax)
     * @param infoStream  the stream for info-level and below messages
     * @param errorStream the stream for error-level and above messages
     * @param useColors   whether to use ANSI color codes in the output
     */
    public ConsoleLogHandler(String format, PrintStream infoStream, PrintStream errorStream, boolean useColors) {
        this.format = format;
        this.infoStream = infoStream;
        this.errorStream = errorStream;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        this.useColors = useColors;
    }

    @Override
    protected boolean doHandle(ILogEvent event) {
        String formattedMessage = formatMessage(event);
        PrintStream stream = (event.getLevel().getSeverity() >= LogLevel.ERROR.getSeverity()) ? errorStream
                : infoStream;

        // Apply colors if enabled
        if (useColors) {
            formattedMessage = applyColor(event.getLevel(), formattedMessage);
        }

        stream.println(formattedMessage);

        // Print stack trace if throwable is present
        if (event.getThrowable() != null) {
            event.getThrowable().printStackTrace(stream);
        }

        return false; // Allow other handlers to process the event
    }

    @Override
    public void close() {
        // Don't close System.out or System.err
        flush();
    }

    @Override
    public void flush() {
        infoStream.flush();
        errorStream.flush();
    }

    /**
     * Formats a log message according to the format string.
     *
     * @param event the log event
     * @return the formatted message
     */
    private String formatMessage(ILogEvent event) {
        Date timestamp = new Date(event.getTimestamp());
        return String.format(format, timestamp, event.getLevel().getName(),
                shortenLoggerName(event.getLoggerName()), event.getMessage());
    }

    /**
     * Shortens a logger name for display.
     *
     * @param loggerName the full logger name
     * @return the shortened logger name (just the last segment)
     */
    private String shortenLoggerName(String loggerName) {
        if (loggerName == null) {
            return "root";
        }
        int lastDot = loggerName.lastIndexOf('.');
        return lastDot > 0 && lastDot < loggerName.length() - 1
                ? loggerName.substring(lastDot + 1)
                : loggerName;
    }

    /**
     * Applies ANSI color codes to a message based on the log level.
     *
     * @param level   the log level
     * @param message the message
     * @return the colored message
     */
    private String applyColor(LogLevel level, String message) {
        // ANSI color codes
        final String RESET = "\u001B[0m";
        final String RED = "\u001B[31m";
        final String YELLOW = "\u001B[33m";
        final String GREEN = "\u001B[32m";
        final String CYAN = "\u001B[36m";
        final String MAGENTA = "\u001B[35m";

        switch (level) {
            case FATAL:
                return RED + message + RESET;
            case ERROR:
                return RED + message + RESET;
            case WARN:
                return YELLOW + message + RESET;
            case INFO:
                return GREEN + message + RESET;
            case DEBUG:
                return CYAN + message + RESET;
            case TRACE:
                return MAGENTA + message + RESET;
            default:
                return message;
        }
    }
}