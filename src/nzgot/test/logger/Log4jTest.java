package nzgot.test.logger;

import nzgot.core.logger.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * test log4j2 logger
 * @author Walter Xie
 */
public class Log4jTest {

    @Before
    public void init() throws Exception
    {
        // Log4J junit configuration.
//        DOMConfigurator.configure();
    }

    @Test
    public void testOne() throws Exception {
        Logger.getLogger().info("INFO TEST");
        Logger.getLogger().debug("DEBUG TEST");
        Logger.getLogger().error("ERROR TEST");

        assertTrue(true);
    }

}
