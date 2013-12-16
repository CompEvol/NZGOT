package nzgo.tookit.core.logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * Singleton to get logger
 * @author Walter Xie
 */
public class MyLogger {

    //create an object of SingleObject
    private static Logger LOGGER;

    //make the constructor private so that this class cannot be instantiated
    private MyLogger(){ }

    private static Logger getLogger() {
        if (null == LOGGER) {
            Level level = Level.FINER;
            LOGGER = Logger.getLogger(MyLogger.class.getName());
            LOGGER.setLevel(level);
            LOGGER.setUseParentHandlers(false);

            LogFormatter formatter = new LogFormatter();
            StreamHandler handler = new StreamHandler(System.out, formatter);
            handler.setLevel(level);
            LOGGER.addHandler(handler);
        }
        return LOGGER;
    }

    /**
     * message for debugging only
     * @param msg
     */
    public static void debug(String msg) {
        getLogger().fine(msg);
    }

    /**
     * message for user after release
     * @param msg
     */
    public static void info(String msg) {
        getLogger().info(msg);
    }

    /**
     * message to warn user after release
     * @param msg
     */
    public static void warn(String msg) {
        getLogger().warning(msg);
    }

    /**
     * system error
     * @param msg
     */
    public static void error(String msg) {
        getLogger().severe(msg);
    }

    public static void main(String[] args) {

        // Set up a simple configuration that logs on the console.

        getLogger().finer("test finer.");

        getLogger().fine("test fine !");

        getLogger().config("test config !");

        getLogger().info("test info !");

        getLogger().warning("test warning !");

        getLogger().severe("error !");
    }


}

class LogFormatter extends Formatter {
    //
    // Create a DateFormat to format the logger timestamp.
    //
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        return getHeader(record) + formatMessage(record) + "\n";
    }

    private String getHeader(LogRecord record) {
        String header;
        if (Level.FINE == record.getLevel()) {
            header = "[DEBUG] ";
        } else if (Level.INFO == record.getLevel()) {
            header = "";
        } else if (Level.WARNING == record.getLevel()) {
            header = "[WARNING] ";
        } else if (Level.SEVERE == record.getLevel()) {
            header = "\n" + df.format(new Date(record.getMillis())) + " ";
            header += "[" + record.getSourceClassName() + ".";
            header += record.getSourceMethodName() + "]\n";
            header += "[ERROR] ";
        } else {
            header = "[" + record.getLevel() + "] ";
        }
        return header;
    }
}
