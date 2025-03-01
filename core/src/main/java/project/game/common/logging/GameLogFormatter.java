package project.game.common.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A custom formatter for log messages that provides a more structured format.
 * The format is: [timestamp] [level] [logger] message (exception if present)
 */
public class GameLogFormatter extends Formatter {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final SimpleDateFormat dateFormat;
    private boolean showThreadName = false;

    /**
     * Creates a GameLogFormatter with default date format.
     */
    public GameLogFormatter() {
        this("yyyy-MM-dd HH:mm:ss.SSS");
    }

    /**
     * Creates a GameLogFormatter with specified date format.
     * 
     * @param dateFormatPattern the pattern for formatting dates
     */
    public GameLogFormatter(String dateFormatPattern) {
        dateFormat = new SimpleDateFormat(dateFormatPattern);
    }

    /**
     * Configures whether to include thread name in log messages.
     * 
     * @param showThreadName true to show thread names
     */
    public void setShowThreadName(boolean showThreadName) {
        this.showThreadName = showThreadName;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        // Format timestamp
        sb.append('[');
        sb.append(dateFormat.format(new Date(record.getMillis())));
        sb.append("] ");

        // Format log level
        sb.append('[');
        sb.append(record.getLevel().getName());
        sb.append("] ");

        // Format logger name (shortened)
        sb.append('[');
        String loggerName = record.getLoggerName();
        if (loggerName != null) {
            int lastDot = loggerName.lastIndexOf('.');
            if (lastDot > 0 && lastDot < loggerName.length() - 1) {
                loggerName = loggerName.substring(lastDot + 1);
            }
        } else {
            loggerName = "root";
        }
        sb.append(loggerName);
        sb.append("] ");

        // Add thread name if enabled
        if (showThreadName) {
            sb.append('[');
            sb.append(Thread.currentThread().getName());
            sb.append("] ");
        }

        // Add message
        sb.append(formatMessage(record));

        // Add exception if present
        if (record.getThrown() != null) {
            sb.append(LINE_SEPARATOR);
            try (StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw)) {
                record.getThrown().printStackTrace(pw);
                sb.append(sw.toString());
            } catch (Exception ex) {
                sb.append("Failed to print stack trace: ").append(ex.getMessage());
            }
        }

        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }
}