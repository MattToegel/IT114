package Project.Common;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Utility class for logging messages to a log file.
 * This class provides methods to log messages at various levels and ensures
 * thread-safe logging to an appropriate log file.
 */
public enum LoggerUtil {
    INSTANCE;

    private Logger logger;
    private LoggerConfig config;
    private boolean isConfigured = false;

    LoggerUtil() {
    }

    /**
     * Sets the configuration for the logger.
     * 
     * @param config the LoggerConfig object containing all the settings
     */
    public void setConfig(LoggerConfig config) {
        this.config = config;
        setupLogger();
    }

    /**
     * CustomFormatter class for formatting the log messages.
     * This class formats the log messages to include the date, log level, source,
     * and message.
     */
    private static class CustomFormatter extends Formatter {
        private static final String PATTERN = "MM/dd/yyyy HH:mm:ss";
        private static final String RESET = "\u001B[0m";
        private static final String RED = "\u001B[31m";
        private static final String GREEN = "\u001B[32m";
        private static final String YELLOW = "\u001B[33m";
        private static final String BLUE = "\u001B[34m";
        private static final String PURPLE = "\u001B[35m";
        private static final String CYAN = "\u001B[36m";
        private static final String WHITE = "\u001B[37m";

        @Override
        public String format(LogRecord record) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN);
            String date = dateFormat.format(new Date(record.getMillis()));
            String callingClass = getCallingClassName();
            String source = callingClass != null ? callingClass
                    : record.getSourceClassName() != null ? record.getSourceClassName() : "unknown";
            String message = formatMessage(record);
            String level = getColoredLevel(record.getLevel());
            String throwable = "";
            if (record.getThrown() != null) {
                // Use stackTraceLimit from LoggerConfig to truncate stack trace
                throwable = "\n"
                        + getFormattedStackTrace(record.getThrown(), LoggerUtil.INSTANCE.config.getStackTraceLimit());
            }
            return String.format("%s [%s] (%s):\n> %s%s\n", date, source, level, message, throwable);
        }

        /**
         * Determines the name of the class that called the logging method.
         * 
         * @return the name of the calling class
         */
        private static String getCallingClassName() {
            String loggerUtilPackage = LoggerUtil.class.getPackage().getName();
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                // Skip all classes in the logging framework and the package of LoggerUtil
                if (!className.startsWith("java.util.logging") &&
                        !className.startsWith(loggerUtilPackage) &&
                        !className.equals(Thread.class.getName())) {
                    return className;
                }
            }
            return null;
        }

        /**
         * Returns a colored log level string based on the severity.
         * 
         * @param level the log level
         * @return the colored log level string
         */
        private static String getColoredLevel(Level level) {
            switch (level.getName()) {
                case "SEVERE":
                    return RED + level.getName() + RESET;
                case "WARNING":
                    return YELLOW + level.getName() + RESET;
                case "INFO":
                    return GREEN + level.getName() + RESET;
                case "CONFIG":
                    return CYAN + level.getName() + RESET;
                case "FINE":
                    return BLUE + level.getName() + RESET;
                case "FINER":
                    return PURPLE + level.getName() + RESET;
                case "FINEST":
                    return WHITE + level.getName() + RESET;
                default:
                    return level.getName();
            }
        }

        /**
         * Generates the stack trace string from the given Throwable.
         * The format includes the exception class name, message (if any),
         * and the stack trace elements up to the specified maxElements.
         * 
         * @param throwable   the throwable to extract the stack trace from
         * @param maxElements the maximum number of stack trace elements to show
         * @return the formatted stack trace as a string
         */
        private static String getFormattedStackTrace(Throwable throwable, int maxElements) {
            StringBuilder sb = new StringBuilder();

            // Add the exception class name and message
            sb.append(throwable.getClass().getName());
            if (throwable.getMessage() != null) {
                sb.append(": ").append(throwable.getMessage());
            }
            sb.append("\n");

            // Stack trace elements
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            int length = stackTrace.length;
            // Limit the output to the top maxElements items
            int displayLimit = Math.min(maxElements, length);

            // Add the stack trace up to the limit
            for (int i = 0; i < displayLimit; i++) {
                sb.append("\tat ").append(stackTrace[i]).append("\n");
            }

            // If stack trace was truncated, append a message to indicate it
            if (length > maxElements) {
                sb.append("\t... ").append(length - maxElements).append(" more elements truncated ...\n");
            }

            return sb.toString();
        }
    }

    /**
     * Ensures the logger is configured only once.
     */
    private synchronized void setupLogger() {
        if (isConfigured)
            return;

        try {
            logger = Logger.getLogger("ApplicationLogger");

            // Remove default console handlers
            Logger rootLogger = Logger.getLogger("");
            for (var handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Customize the file naming pattern
            String logPattern = config.getLogLocation().replace(".log", "-%g.log");
            // FileHandler writes log messages to a specified file, with support for
            // rotating log files
            FileHandler fileHandler = new FileHandler(
                    logPattern,
                    config.getFileSizeLimit(),
                    config.getFileCount(),
                    true);
            fileHandler.setFormatter(new CustomFormatter());
            fileHandler.setLevel(config.getFileLogLevel());
            logger.addHandler(fileHandler);

            // ConsoleHandler prints log messages to the console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new CustomFormatter());
            consoleHandler.setLevel(config.getConsoleLogLevel());
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.ALL);
            isConfigured = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs a message at the specified level.
     * 
     * @param level   the level of the log message
     * @param message the log message
     */
    public void log(Level level, String message) {
        if (!isConfigured)
            setupLogger();
        logger.log(level, message);
    }

    /**
     * Logs a message at the specified level, overloaded to accept an Object.
     * If the Object is a String, logs it as a message.
     * If the Object is a Throwable (Exception), logs its message and stack trace.
     * Otherwise, logs the Object's toString().
     * 
     * @param level   the level of the log message
     * @param message the Object to log
     */
    public void log(Level level, Object message) {
        if (!isConfigured)
            setupLogger();
        if (message instanceof String) {
            logger.log(level, (String) message);
        } else if (message instanceof Throwable) {
            logger.log(level, ((Throwable) message).getMessage(), (Throwable) message);
        } else {
            logger.log(level, message.toString());
        }
    }

    /**
     * Logs an exception at the specified level.
     *
     * @param level     the level of the log message
     * @param message   the log message
     * @param throwable the exception to log
     */
    public void log(Level level, String message, Throwable throwable) {
        if (!isConfigured)
            setupLogger();
        logger.log(level, message, throwable);
    }

    /**
     * Logs an informational message.
     * 
     * @param message the log message
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs an informational message, overloaded to accept an Object.
     * 
     * @param message the Object to log
     */
    public void info(Object message) {
        log(Level.INFO, message);
    }

    /**
     * Logs an exception with an INFO level.
     *
     * @param message   the log message
     * @param throwable the exception to log
     */
    public void info(String message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }

    /**
     * Logs a warning message.
     * 
     * @param message the log message
     */
    public void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs a warning message, overloaded to accept an Object.
     * 
     * @param message the Object to log
     */
    public void warning(Object message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs an exception with a WARNING level.
     *
     * @param message   the log message
     * @param throwable the exception to log
     */
    public void warning(String message, Throwable throwable) {
        log(Level.WARNING, message, throwable);
    }

    /**
     * Logs a severe error message.
     * 
     * @param message the log message
     */
    public void severe(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Logs a severe error message, overloaded to accept an Object.
     * 
     * @param message the Object to log
     */
    public void severe(Object message) {
        log(Level.SEVERE, message);
    }

    /**
     * Logs an exception with a SEVERE level.
     *
     * @param message   the log message
     * @param throwable the exception to log
     */
    public void severe(String message, Throwable throwable) {
        log(Level.SEVERE, message, throwable);
    }

    /**
     * Logs a fine-grained informational message.
     * 
     * @param message the log message
     */
    public void fine(String message) {
        log(Level.FINE, message);
    }

    /**
     * Logs a fine-grained informational message, overloaded to accept an Object.
     * 
     * @param message the Object to log
     */
    public void fine(Object message) {
        log(Level.FINE, message);
    }

    /**
     * Logs a finer-grained informational message.
     * 
     * @param message the log message
     */
    public void finer(String message) {
        log(Level.FINER, message);
    }

    /**
     * Logs a finer-grained informational message, overloaded to accept an Object.
     * 
     * @param message the Object to log
     */
    public void finer(Object message) {
        log(Level.FINER, message);
    }

    /**
     * Logs the finest-grained informational message.
     * 
     * @param message the log message
     */
    public void finest(String message) {
        log(Level.FINEST, message);
    }

    /**
     * Logs the finest-grained informational message, overloaded to accept an
     * Object.
     * 
     * @param message the Object to log
     */
    public void finest(Object message) {
        log(Level.FINEST, message);
    }

    /**
     * Configuration class for the LoggerUtil.
     * This class encapsulates all the properties for configuring the logger.
     */
    public static class LoggerConfig {
        private int fileSizeLimit = 1024 * 1024; // 1MB default file size
        private int fileCount = 5; // default number of rotating log files
        private String logLocation = "application.log";
        private Level fileLogLevel = Level.ALL; // default log level for file
        private Level consoleLogLevel = Level.ALL; // default log level for console
        private int stackTraceLimit = 10; // default maximum number of stack trace elements

        // Getters and Setters for each property

        /**
         * Gets the file limit for the log files.
         * 
         * @return the maximum size of each log file in bytes
         */
        public int getFileSizeLimit() {
            return fileSizeLimit;
        }

        /**
         * Sets the file limit for the log files.
         * 
         * @param fileLimit the maximum size of each log file in bytes
         */
        public void setFileSizeLimit(int fileLimit) {
            this.fileSizeLimit = fileLimit;
        }

        /**
         * Gets the number of rotating log files.
         * 
         * @return the number of log files
         */
        public int getFileCount() {
            return fileCount;
        }

        /**
         * Sets the number of rotating log files.
         * 
         * @param fileCount the number of log files
         */
        public void setFileCount(int fileCount) {
            this.fileCount = fileCount;
        }

        /**
         * Gets the file location for log files.
         * 
         * @return the file location for logs
         */
        public String getLogLocation() {
            return logLocation;
        }

        /**
         * Sets the file location for log files.
         * 
         * @param logLocation the file location for logs
         */
        public void setLogLocation(String logLocation) {
            this.logLocation = logLocation;
        }

        /**
         * Gets the log level for file logging.
         * 
         * @return the log level for file logging
         */
        public Level getFileLogLevel() {
            return fileLogLevel;
        }

        /**
         * Sets the log level for file logging.
         * 
         * @param fileLogLevel the log level for file logging
         */
        public void setFileLogLevel(Level fileLogLevel) {
            this.fileLogLevel = fileLogLevel;
        }

        /**
         * Gets the log level for console logging.
         * 
         * @return the log level for console logging
         */
        public Level getConsoleLogLevel() {
            return consoleLogLevel;
        }

        /**
         * Sets the log level for console logging.
         * 
         * @param consoleLogLevel the log level for console logging
         */
        public void setConsoleLogLevel(Level consoleLogLevel) {
            this.consoleLogLevel = consoleLogLevel;
        }

        /**
         * Gets the stack trace limit for logging.
         * 
         * @return the maximum number of stack trace elements to show
         */
        public int getStackTraceLimit() {
            return stackTraceLimit;
        }

        /**
         * Sets the stack trace limit for logging.
         * 
         * @param stackTraceLimit the maximum number of stack trace elements to show
         */
        public void setStackTraceLimit(int stackTraceLimit) {
            this.stackTraceLimit = stackTraceLimit;
        }
    }

    /**
     * Example usage
     * 
     * @param args
     */
    public static void main(String[] args) {
        // Create a LoggerConfig instance and set the desired configurations
        LoggerUtil.LoggerConfig config = new LoggerUtil.LoggerConfig();
        config.setFileSizeLimit(2048 * 1024); // 2MB file size limit
        config.setFileCount(10); // 10 rotating log files
        config.setLogLocation("example.log"); // Log file location
        config.setFileLogLevel(Level.ALL); // Log level for file
        config.setConsoleLogLevel(Level.ALL); // Log level for console

        // Set the logger configuration
        LoggerUtil.INSTANCE.setConfig(config);

        // Original examples
        LoggerUtil.INSTANCE.info("This is an info message.");
        LoggerUtil.INSTANCE.warning("This is a warning message.");
        LoggerUtil.INSTANCE.severe("This is a severe error message.");
        LoggerUtil.INSTANCE.fine("This is a fine-grained informational message.");
        LoggerUtil.INSTANCE.finer("This is a finer-grained informational message.");
        LoggerUtil.INSTANCE.finest("This is the finest-grained informational message.");

        // Simulate logging from a thread
        new Thread(() -> {
            LoggerUtil.INSTANCE.info("This is a message from a separate thread.");
        }).start();

        // Simulate an exception
        LoggerUtil.INSTANCE.warning("This is a simulated warning exception.", new IOException("Simulated IOException"));
        LoggerUtil.INSTANCE.severe("This is a simulated severe error", new Exception("Simulated Exception"));

        // New examples to test Object overloads
        LoggerUtil.INSTANCE.info(new Object() {
            @Override
            public String toString() {
                return "Logging a custom object using info";
            }
        });

        LoggerUtil.INSTANCE.warning(new Exception("Logging a Throwable object using warning"));

        LoggerUtil.INSTANCE.severe(new Object() {
            @Override
            public String toString() {
                return "Logging a custom object using severe";
            }
        });

        LoggerUtil.INSTANCE.fine(new Object() {
            @Override
            public String toString() {
                return "Logging a custom object using fine";
            }
        });

        // Logging a null value to test edge cases
        try {
            LoggerUtil.INSTANCE.info((Object) null);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("A NullPointerException occurred!", e);
        }
        // New example to trigger a larger stack trace (StackOverflowError)
        try {
            recursiveMethod(0);
        } catch (StackOverflowError e) {
            LoggerUtil.INSTANCE.severe("A StackOverflowError occurred!", e);
        }
    }

    private static void recursiveMethod(int depth) {
        // Keep calling itself to cause a StackOverflowError
        recursiveMethod(depth + 1);
    }
}
