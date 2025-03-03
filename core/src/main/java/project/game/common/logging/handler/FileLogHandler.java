package project.game.common.logging.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import project.game.engine.api.logging.ILogEvent;
import project.game.engine.logging.AbstractLogHandler;

/**
 * Log handler that writes log events to a file.
 * Supports file rotation based on size or date.
 */
public class FileLogHandler extends AbstractLogHandler {
    private static final String DEFAULT_FORMAT = "[%1$tF %1$tT] [%2$-5s] [%3$s] [%5$s] %4$s";
    private static final int DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final String format;
    private final String baseFilePath;
    private final boolean appendToFile;
    private final SimpleDateFormat dateFormat;
    private final int maxFileSize;
    private final boolean rotateOnSize;
    private final boolean rotateDaily;
    private final int maxBackupFiles;

    private File currentFile;
    private PrintWriter writer;
    private AtomicLong currentFileSize = new AtomicLong(0);
    private long lastRotationTimestamp = 0;

    /**
     * Creates a new FileLogHandler with default settings.
     * 
     * @param filePath the path to the log file
     * @throws IOException if an I/O error occurs
     */
    public FileLogHandler(String filePath) throws IOException {
        this(filePath, true, DEFAULT_FORMAT, DEFAULT_MAX_FILE_SIZE, true, false, 5);
    }

    /**
     * Creates a new FileLogHandler with the specified settings.
     * 
     * @param filePath       the path to the log file
     * @param append         whether to append to an existing file
     * @param format         the format string for log messages
     * @param maxFileSize    the maximum file size before rotation (0 for no limit)
     * @param rotateOnSize   whether to rotate files when they reach maxFileSize
     * @param rotateDaily    whether to rotate files daily
     * @param maxBackupFiles the maximum number of backup files to keep
     * @throws IOException if an I/O error occurs
     */
    public FileLogHandler(String filePath, boolean append, String format, int maxFileSize,
            boolean rotateOnSize, boolean rotateDaily, int maxBackupFiles) throws IOException {
        this.baseFilePath = filePath;
        this.appendToFile = append;
        this.format = format;
        this.maxFileSize = maxFileSize;
        this.rotateOnSize = rotateOnSize;
        this.rotateDaily = rotateDaily;
        this.maxBackupFiles = maxBackupFiles;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        openFile();
    }

    /**
     * Opens the log file for writing.
     * 
     * @throws IOException if an I/O error occurs
     */
    private synchronized void openFile() throws IOException {
        currentFile = new File(baseFilePath);

        // Create parent directories if they don't exist
        File parentDir = currentFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir);
            }
        }

        // Update the current file size if the file already exists
        if (currentFile.exists()) {
            currentFileSize.set(currentFile.length());
        } else {
            currentFileSize.set(0);
        }

        // Create the writer
        writer = new PrintWriter(new BufferedWriter(new FileWriter(currentFile, appendToFile)), true);
        lastRotationTimestamp = System.currentTimeMillis();
    }

    @Override
    protected boolean doHandle(ILogEvent event) {
        try {
            checkRotation();

            String formattedMessage = formatMessage(event);

            synchronized (this) {
                writer.println(formattedMessage);

                // Print stack trace if throwable is present
                if (event.getThrowable() != null) {
                    event.getThrowable().printStackTrace(writer);
                }

                currentFileSize.addAndGet(formattedMessage.length() + 1); // +1 for the newline
            }

            return false; // Allow other handlers to process the event
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
            setEnabled(false); // Disable this handler if it fails
            return false;
        }
    }

    /**
     * Checks if the log file should be rotated and performs rotation if needed.
     * 
     * @throws IOException if an I/O error occurs
     */
    private synchronized void checkRotation() throws IOException {
        boolean shouldRotate = false;
        long now = System.currentTimeMillis();

        // Check if we should rotate based on size
        if (rotateOnSize && maxFileSize > 0 && currentFileSize.get() >= maxFileSize) {
            shouldRotate = true;
        }

        // Check if we should rotate based on date (once per day)
        if (rotateDaily) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
            String today = dayFormat.format(new Date(now));
            String lastRotationDay = dayFormat.format(new Date(lastRotationTimestamp));
            if (!today.equals(lastRotationDay)) {
                shouldRotate = true;
            }
        }

        if (shouldRotate) {
            rotate();
        }
    }

    /**
     * Rotates the log file by renaming the current file and creating a new one.
     * 
     * @throws IOException if an I/O error occurs
     */
    private synchronized void rotate() throws IOException {
        // Close the current writer
        if (writer != null) {
            writer.close();
        }

        // Delete old backup files if we're over the limit
        if (maxBackupFiles > 0) {
            purgeOldBackupFiles();
        }

        // Determine the backup file name
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String backupFilePath = baseFilePath + "." + timestamp;
        File backupFile = new File(backupFilePath);

        // Rename the current file to the backup file
        if (currentFile.exists() && !currentFile.renameTo(backupFile)) {
            throw new IOException("Failed to rename log file for rotation");
        }

        // Open a new file
        openFile();
    }

    /**
     * Deletes old backup files if we're over the maximum limit.
     */
    private void purgeOldBackupFiles() {
        File dir = new File(currentFile.getParent() == null ? "." : currentFile.getParent());
        String baseName = currentFile.getName();

        File[] backupFiles = dir.listFiles((d, name) -> name.startsWith(baseName + "."));
        if (backupFiles != null && backupFiles.length >= maxBackupFiles) {
            // Sort files by last modified time (oldest first)
            java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

            // Delete the oldest files to get down to the limit
            int numToDelete = backupFiles.length - maxBackupFiles + 1; // +1 for the new backup
            for (int i = 0; i < numToDelete; i++) {
                if (!backupFiles[i].delete()) {
                    System.err.println("Failed to delete old backup file: " + backupFiles[i]);
                }
            }
        }
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
                event.getLoggerName(), event.getMessage(), event.getThreadName());
    }

    @Override
    public void close() {
        synchronized (this) {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }
    }

    @Override
    public void flush() {
        synchronized (this) {
            if (writer != null) {
                writer.flush();
            }
        }
    }
}