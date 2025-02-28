package project.game.common.logging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import project.game.Main;

/**
 * Configures the logging system for the game.
 */
public class LogManager {

    private static final Logger ROOT_LOGGER = Logger.getLogger("");

    public static void initialize() {
        try {
            // Remove any existing handlers
            for (java.util.logging.Handler h : ROOT_LOGGER.getHandlers()) {
                ROOT_LOGGER.removeHandler(h);
            }

            // Get project root directory
            String projectPath = new File(Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getAbsolutePath();

            // Create logs directory in project root
            File logDir = new File(projectPath, "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // Check if there are more than 5 log files and delete the oldest ones
            File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
            if (logFiles != null && logFiles.length > 5) {
                Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));
                int filesToDelete = logFiles.length - 5;
                for (int i = 0; i < filesToDelete; i++) {
                    logFiles[i].delete();
                }
            }

            // Create a timestamped log file so a new one is created each run
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = sdf.format(new Date());
            String logFile = new File(logDir, "Main_" + timestamp + ".log").getAbsolutePath();
            FileHandler fileHandler = new FileHandler(logFile);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            ROOT_LOGGER.addHandler(fileHandler);

            // Add a shutdown hook to flush/close the file handler
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                fileHandler.flush();
                fileHandler.close();
            }));

            // Create and add a ConsoleHandler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());
            ROOT_LOGGER.addHandler(consoleHandler);

            // Set the root logger level
            ROOT_LOGGER.setLevel(Level.ALL);

            // Centralize logging
            Logger.getLogger("project.game").setLevel(Level.ALL);
        } catch (SecurityException | java.io.IOException | java.net.URISyntaxException e) {
            System.err.println("[ERROR] Failed to configure log file or permissions: " + e.getMessage());
            ROOT_LOGGER.log(Level.SEVERE, "Failed to configure log file or permissions", e);
        }
    }
}
