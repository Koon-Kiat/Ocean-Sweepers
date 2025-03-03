package project.game.common.logging.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Custom formatter for Java logger that formats log messages
 * according to the game's requirements.
 */
public class GameLogFormatter extends Formatter {
    private static final String DEFAULT_FORMAT = "[%1$tF %1$tT] [%2$-5s] [%3$s] %4$s";
    private final SimpleDateFormat dateFormat;

    /**
     * Creates a new GameLogFormatter with the default date format.
     */
    public GameLogFormatter() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    @Override
    public String format(LogRecord record) {
        Date timestamp = new Date(record.getMillis());
        String level = record.getLevel().getName();
        String loggerName = shortenLoggerName(record.getLoggerName());
        String message = record.getMessage();

        // Format the message
        String formattedMessage = String.format(DEFAULT_FORMAT,
                timestamp, level, loggerName, message);

        // Add stack trace if there's a thrown exception
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            StringBuilder sb = new StringBuilder(formattedMessage);
            sb.append('\n');
            appendStackTraceAsString(sb, thrown);
            return sb.toString();
        }

        return formattedMessage + "\n";
    }

    /**
     * Shortens a logger name for display, keeping only the class name.
     *
     * @param loggerName the full logger name
     * @return the shortened logger name
     */
    private String shortenLoggerName(String loggerName) {
        if (loggerName == null || loggerName.isEmpty()) {
            return "root";
        }

        int lastDot = loggerName.lastIndexOf('.');
        return lastDot > 0 && lastDot < loggerName.length() - 1
                ? loggerName.substring(lastDot + 1)
                : loggerName;
    }

    /**
     * Appends a stack trace to a StringBuilder.
     * 
     * @param sb     the StringBuilder to append to
     * @param thrown the Throwable to get the stack trace from
     */
    private void appendStackTraceAsString(StringBuilder sb, Throwable thrown) {
        sb.append(thrown.getClass().getName());
        sb.append(": ");
        sb.append(thrown.getMessage());
        sb.append('\n');

        for (StackTraceElement element : thrown.getStackTrace()) {
            sb.append("\tat ");
            sb.append(element.toString());
            sb.append('\n');
        }

        // Handle cause if present
        Throwable cause = thrown.getCause();
        if (cause != null) {
            sb.append("Caused by: ");
            appendStackTraceAsString(sb, cause);
        }
    }
}