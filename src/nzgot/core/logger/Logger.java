package nzgot.core.logger;

import org.apache.logging.log4j.LogManager;

/**
 * Singleton to get log4j logger
 * @author Walter Xie
 */
public class Logger {

    //create an object of SingleObject
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(Logger.class.getName());

    //make the constructor private so that this class cannot be instantiated
    private Logger(){}

    //Get the only object available
    public static org.apache.logging.log4j.Logger getLogger(){
        return logger;
    }
}
