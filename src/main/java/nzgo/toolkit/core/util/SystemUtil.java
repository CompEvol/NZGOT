package nzgo.toolkit.core.util;

import beast.app.util.Utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * SystemUtil
 * @author Walter Xie
 */
public class SystemUtil {

    public static final String APP_DIR = "NZGOT";

    /**
     * return directory for system shared to users
     */
    public static Path getAppDir() {
        if (Utils.isWindows()) {
            // "\\Program Files\\"
            return Paths.get(File.separator + "Program Files", APP_DIR);
        }
        if (Utils.isMac()) {
            // "/Library/Application Support/"
            return Paths.get(File.separator + "Library", "Application Support", APP_DIR);
        }
        // "/usr/local/share/"
        return Paths.get(File.separator + "usr", "local", "share", APP_DIR);
    }

}
