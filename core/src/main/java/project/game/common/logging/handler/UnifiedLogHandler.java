package project.game.common.logging.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import project.game.common.logging.core.LogLevel;
import project.game.engine.api.logging.ILoggerEvent;
import project.game.engine.logging.AbstractLogHandler;

/**
 * Unified log handler that can write log events to both console and file.
 * Supports file rotation based on size or date and console color output.
 */
public class UnifiedLogHandler extends AbstractLogHandler {
    // Console logging settings
    private static final String DEFAULT_CONSOLE_FORMAT = "[%1$tF %1$tT] [%2$-5s] [%3$s] %4$s";
    private final PrintStream infoStream;
    private final PrintStream errorStream;
    private final boolean useConsoleColors;
    private final boolean enableConsoleLogging;
    private final String consoleFormat;

    // File logging settings
    private static final String DEFAULT_FILE_FORMAT = "[%1$tF %1$tT] [%2$-5s] [%3$s] [%5$s] %4$s";
    private static final int DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private final String fileFormat;
    private final String baseFilePath;
    private final boolean appendToFile;
    private final int maxFileSize;
    private final boolean rotateOnSize;
    private final boolean rotateDaily;
    private final int maxBackupFiles;
    private final boolean enableFileLogging;

    // File handling state
    private File currentFile;
    private PrintWriter fileWriter;
    private AtomicLong currentFileSize = new AtomicLong(0);
    private long lastRotationTimestamp = 0;

    // Static tracking for handling duplicate initializations
    private static String currentActiveLogFile = null;

    /**
     * Creates a new UnifiedLogHandler with console logging only.
     */
    public UnifiedLogHandler() {
        this(true, false, DEFAULT_CONSOLE_FORMAT, System.out, System.err, false, null);
    }

    /**
     * Creates a new UnifiedLogHandler with file logging only.
     * 
     * @param filePath the path to the log file
     * @throws IOException if an I/O error occurs
     */
    public UnifiedLogHandler(String filePath) throws IOException {
        this(false, true, filePath, true, DEFAULT_FILE_FORMAT, DEFAULT_MAX_FILE_SIZE, true, false, 5);
    }

    /**
     * Creates a UnifiedLogHandler with both console and file logging.
     * 
     * @param filePath  the path to the log file
     * @param useColors whether to use console colors
     * @throws IOException if an I/O error occurs
     */
    public UnifiedLogHandler(String filePath, boolean useColors) throws IOException {
        this(true, true, DEFAULT_CONSOLE_FORMAT, System.out, System.err, useColors,
                filePath, true, DEFAULT_FILE_FORMAT, DEFAULT_MAX_FILE_SIZE, true, false, 5);
    }

    /**
     * Constructor for console-only logging.
     */
    private UnifiedLogHandler(boolean enableConsole, boolean enableFile, String consoleFormat,
            PrintStream infoStream, PrintStream errorStream, boolean useColors,
            String filePath) {
        this(enableConsole, enableFile, consoleFormat, infoStream, errorStream, useColors,
                filePath, true, DEFAULT_FILE_FORMAT, DEFAULT_MAX_FILE_SIZE, true, false, 5);
    }

    /**
     * Constructor for file-only logging.
     */
    private UnifiedLogHandler(boolean enableConsole, boolean enableFile, String filePath,
            boolean append, String fileFormat, int maxFileSize,
            boolean rotateOnSize, boolean rotateDaily, int maxBackupFiles) throws IOException {
        this(enableConsole, enableFile, DEFAULT_CONSOLE_FORMAT, System.out, System.err, false,
                filePath, append, fileFormat, maxFileSize, rotateOnSize, rotateDaily, maxBackupFiles);
    }

    /**
     * Full constructor with all options.
     */
    private UnifiedLogHandler(boolean enableConsole, boolean enableFile,
            String consoleFormat, PrintStream infoStream, PrintStream errorStream,
            boolean useColors, String filePath, boolean append, String fileFormat,
            int maxFileSize, boolean rotateOnSize, boolean rotateDaily,
            int maxBackupFiles) {
        this.enableConsoleLogging = enableConsole;
        this.enableFileLogging = enableFile;

        // Console settings
        this.consoleFormat = consoleFormat;
        this.infoStream = infoStream;
        this.errorStream = errorStream;
        this.useConsoleColors = useColors;

        // File settings
        this.baseFilePath = filePath;
        this.appendToFile = append;
        this.fileFormat = fileFormat;
        this.maxFileSize = maxFileSize;
        this.rotateOnSize = rotateOnSize;
        this.rotateDaily = rotateDaily;
        this.maxBackupFiles = maxBackupFiles;

        // Initialize file logging if enabled
        if (enableFileLogging) {
            try {
                openFile();
            } catch (IOException e) {
                System.err.println("Failed to initialize file logging: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected boolean doHandle(ILoggerEvent event) {
        if (enableConsoleLogging) {
            handleConsoleLogging(event);
        }

        if (enableFileLogging) {
            handleFileLogging(event);
        }

        return false; // Allow other handlers to process the event
    }

    private void handleConsoleLogging(ILoggerEvent event) {
        String formattedMessage = formatConsoleMessage(event);
        PrintStream stream = (event.getLevel().getSeverity() >= LogLevel.ERROR.getSeverity())
                ? errorStream
                : infoStream;

        if (useConsoleColors) {
            formattedMessage = applyColor(event.getLevel(), formattedMessage);
        }

        stream.println(formattedMessage);
        if (event.getThrowable() != null) {
            event.getThrowable().printStackTrace(stream);
        }
    }

    private void handleFileLogging(ILoggerEvent event) {
        try {
            // Format message first to ensure accurate size calculation
            String formattedMessage = formatFileMessage(event);
            // Calculate potential stacktrace size
            int throwableSize = 0;
            if (event.getThrowable() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                event.getThrowable().printStackTrace(pw);
                throwableSize = sw.toString().length();
            }

            // Calculate total message size (including line terminators)
            int totalSize = formattedMessage.length() + 2 + throwableSize; // +2 for line terminator

            synchronized (this) {
                if (fileWriter != null) {
                    long now = System.currentTimeMillis();
                    boolean shouldRotate = false;

                    // Only check rotation if enough time has passed AND we have content
                    if ((now - lastRotationTimestamp) >= 5000 && currentFileSize.get() > 0) {
                        // Check size-based rotation
                        if (rotateOnSize && maxFileSize > 0 &&
                                (currentFileSize.get() + totalSize) >= maxFileSize) {
                            shouldRotate = true;
                        }

                        // Check time-based rotation
                        if (!shouldRotate && rotateDaily) {
                            Calendar lastCal = Calendar.getInstance();
                            lastCal.setTimeInMillis(lastRotationTimestamp);
                            Calendar nowCal = Calendar.getInstance();
                            nowCal.setTimeInMillis(now);

                            if (lastCal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR) ||
                                    lastCal.get(Calendar.MONTH) != nowCal.get(Calendar.MONTH) ||
                                    lastCal.get(Calendar.DAY_OF_MONTH) != nowCal.get(Calendar.DAY_OF_MONTH)) {
                                shouldRotate = true;
                            }
                        }

                        if (shouldRotate) {
                            // Ensure file is flushed before rotation
                            fileWriter.flush();
                            rotate();
                        }
                    }

                    // Write the message
                    fileWriter.println(formattedMessage);
                    if (event.getThrowable() != null) {
                        event.getThrowable().printStackTrace(fileWriter);
                    }
                    // Update size after writing
                    currentFileSize.addAndGet(totalSize);

                    // Ensure content is written to disk
                    fileWriter.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    private String formatConsoleMessage(ILoggerEvent event) {
        Date timestamp = new Date(event.getTimestamp());
        return String.format(consoleFormat, timestamp, event.getLevel().getName(),
                shortenLoggerName(event.getLoggerName()), event.getMessage());
    }

    private String formatFileMessage(ILoggerEvent event) {
        Date timestamp = new Date(event.getTimestamp());
        return String.format(fileFormat, timestamp, event.getLevel().getName(),
                event.getLoggerName(), event.getMessage(), event.getThreadName());
    }

    private String shortenLoggerName(String loggerName) {
        if (loggerName == null) {
            return "root";
        }
        int lastDot = loggerName.lastIndexOf('.');
        return lastDot > 0 && lastDot < loggerName.length() - 1
                ? loggerName.substring(lastDot + 1)
                : loggerName;
    }

    private String applyColor(LogLevel level, String message) {
        final String RESET = "\u001B[0m";
        final String RED = "\u001B[31m";
        final String YELLOW = "\u001B[33m";
        final String GREEN = "\u001B[32m";
        final String CYAN = "\u001B[36m";
        final String MAGENTA = "\u001B[35m";

        String coloredMessage;
        switch (level) {
            case FATAL:
            case ERROR:
                coloredMessage = RED + message + RESET;
                break;
            case WARN:
                coloredMessage = YELLOW + message + RESET;
                break;
            case INFO:
                coloredMessage = GREEN + message + RESET;
                break;
            case DEBUG:
                coloredMessage = CYAN + message + RESET;
                break;
            case TRACE:
                coloredMessage = MAGENTA + message + RESET;
                break;
            default:
                coloredMessage = message;
        }
        return coloredMessage;
    }

    private synchronized void openFile() throws IOException {
        if (!enableFileLogging)
            return;

        // Check if this log file is already actively being written to
        // This prevents duplicate log files during JVM restarts or multiple
        // initializations
        if (baseFilePath != null && baseFilePath.equals(currentActiveLogFile)) {
            // This exact log file is already being written to, so we'll just append to it
            appendToExistingLogFile();
            return;
        }

        // Create a new log file with timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String timestamp = dateFormat.format(new Date());

        // Generate unique filename if log format includes timestamp
        File logFile;
        String logPath;

        if (baseFilePath != null && baseFilePath.contains("GameLog")) {
            // Use directory from baseFilePath but create a timestamped filename
            File baseFile = new File(baseFilePath);
            File parentDir = baseFile.getParentFile();
            String parentPath = (parentDir != null) ? parentDir.getPath() : ".";
            logPath = parentPath + File.separator + "GameLog_" + timestamp + ".log";
            logFile = new File(logPath);
        } else {
            logFile = new File(baseFilePath);
            logPath = baseFilePath;
        }

        currentFile = logFile;
        currentActiveLogFile = logPath; // Remember the active log file globally

        File parentDir = currentFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir);
            }
        }

        // Close existing writer if any
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }

        // Set the initial size correctly
        currentFileSize.set(currentFile.exists() ? currentFile.length() : 0);
        fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(currentFile, appendToFile)), true);
        // Only update timestamp if this is a fresh file
        if (!currentFile.exists() || !appendToFile) {
            lastRotationTimestamp = System.currentTimeMillis();
        }

        // Log that we're starting a new session
        if (fileWriter != null) {
            fileWriter.println("[" + timestamp + "] [INFO ] [LogHandler] === Starting new logging session ===");
            fileWriter.flush();
        }
    }

    private synchronized void appendToExistingLogFile() throws IOException {
        // Open the existing active log file for appending
        currentFile = new File(currentActiveLogFile);

        // Check if file exists, create if it doesn't
        if (!currentFile.exists()) {
            File parentDir = currentFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            currentFile.createNewFile();
        }

        // Close existing writer if any
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }

        // Set the initial size correctly and open the writer
        currentFileSize.set(currentFile.length());
        fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(currentFile, true)), true);

        // Update timestamp
        lastRotationTimestamp = System.currentTimeMillis();

        // Log that we're appending to an existing session
        if (fileWriter != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());
            fileWriter.println("[" + timestamp + "] [INFO ] [LogHandler] === Continuing existing logging session ===");
            fileWriter.flush();
        }
    }

    private synchronized void checkRotation() throws IOException {
        if (!enableFileLogging)
            return;

        boolean shouldRotate = false;
        long now = System.currentTimeMillis();

        // Add buffer time to prevent immediate rotation after creation
        if ((now - lastRotationTimestamp) < 5000) {
            return; // Skip rotation checks if file was created less than 5 seconds ago
        }

        if (rotateOnSize && maxFileSize > 0 && currentFileSize.get() >= maxFileSize) {
            shouldRotate = true;
        }

        if (rotateDaily) {
            // More accurate day comparison to avoid timezone/DST issues
            Calendar lastCal = Calendar.getInstance();
            lastCal.setTimeInMillis(lastRotationTimestamp);
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(now);

            // Compare year, month and day components
            if (lastCal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR) ||
                    lastCal.get(Calendar.MONTH) != nowCal.get(Calendar.MONTH) ||
                    lastCal.get(Calendar.DAY_OF_MONTH) != nowCal.get(Calendar.DAY_OF_MONTH)) {
                shouldRotate = true;
            }
        }

        if (shouldRotate) {
            rotate();
        }
    }

    private synchronized void rotate() throws IOException {
        if (!enableFileLogging || fileWriter == null)
            return;

        // Only rotate if we have content
        if (currentFile.exists() && currentFile.length() > 0) {
            // Close current writer
            fileWriter.close();
            fileWriter = null;

            // Remove old backup files before creating new one
            if (maxBackupFiles > 0) {
                purgeOldBackupFiles();
            }

            // Create backup with unique timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
            String timestamp = dateFormat.format(new Date());
            String backupFilePath = baseFilePath + "." + timestamp;
            File backupFile = new File(backupFilePath);

            if (!currentFile.renameTo(backupFile)) {
                throw new IOException("Failed to rename log file for rotation");
            }

            // Open new file
            openFile();
            // Update timestamp AFTER successful rotation
            lastRotationTimestamp = System.currentTimeMillis();
        }
    }

    private void purgeOldBackupFiles() {
        File dir = new File(currentFile.getParent() == null ? "." : currentFile.getParent());
        String baseName = currentFile.getName();

        // Get all backup files for this log
        File[] backupFiles = dir.listFiles((d, name) -> name.startsWith(baseName + "."));

        // Keep exactly maxBackupFiles files (not maxBackupFiles - 1)
        if (backupFiles != null && backupFiles.length >= maxBackupFiles) {
            // Sort by modification time
            java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

            // Delete oldest files to maintain exactly maxBackupFiles
            int numToDelete = backupFiles.length - maxBackupFiles + 1; // +1 because we're about to create a new one
            for (int i = 0; i < numToDelete; i++) {
                if (!backupFiles[i].delete()) {
                    System.err.println("Failed to delete old backup file: " + backupFiles[i]);
                }
            }
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (fileWriter != null) {
                fileWriter.close();
                fileWriter = null;
            }
        }
        if (enableConsoleLogging) {
            infoStream.flush();
            errorStream.flush();
        }
    }

    @Override
    public void flush() {
        synchronized (this) {
            if (fileWriter != null) {
                fileWriter.flush();
            }
        }
        if (enableConsoleLogging) {
            infoStream.flush();
            errorStream.flush();
        }
    }
}