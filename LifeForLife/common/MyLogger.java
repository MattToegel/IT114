package LifeForLife.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Built from a combination of answers from
 * https://stackoverflow.com/questions/15758685/how-to-write-logs-in-text-file-when-using-java-util-logging-logger
 * Goal is to have an easier way to log to file with classnames attached
 */
public class MyLogger {
    static int SIZE_BYTES = 1_000_000;// max size of file
    static int ROTATIONCOUNT = 1;// how many versions until overwrite
    static String logsDirectoryFolder = "logs";
    private String className;
    private static Logger logger = Logger.getLogger(MyLogger.class.getCanonicalName());
    static FileHandler fh = null;

    public static MyLogger getLogger(String className) {
        synchronized (className) {
            if (fh == null) {
                try {
                    // This block configure the logger with handler and formatter
                    Files.createDirectories(Paths.get(logsDirectoryFolder));
                    fh = new FileHandler(
                            logsDirectoryFolder + File.separator + className + "_" + getCurrentTimeString() + ".log",
                            SIZE_BYTES,
                            ROTATIONCOUNT, true);
                    logger.addHandler(fh);
                    SimpleFormatter formatter = new SimpleFormatter();
                    fh.setFormatter(formatter);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addCloseHandlersShutdownHook();
            }
        }
        return new MyLogger(className);

    }

    private static void addCloseHandlersShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Close all handlers to get rid of empty .LCK files
            for (Handler handler : logger.getHandlers()) {
                handler.close();
            }
        }));
    }

    private static String getCurrentTimeString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return dateFormat.format(new Date());
    }

    private MyLogger(String className) {
        this.className = className;
    }

    public void info(String message) {
        logger.info(String.format("%s: %s", className, message));
    }

    public void warning(String message) {
        logger.warning(String.format("%s: %s", className, message));
    }

    public void severe(String message) {
        logger.severe(String.format("%s: %s", className, message));
    }

    public void fine(String message) {
        logger.fine(String.format("%s: %s", className, message));
    }
}
