package nzgo.toolkit.core.util;

import beast.app.util.Utils;
import nzgo.toolkit.core.logger.MyLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
     * TODO AccessDeniedException
     */
    public static Path getAppDir(String dir) {
        // "/usr/local/share/"
        Path appDir = Paths.get(File.separator + "usr", "local", "share", dir);
        if (Utils.isWindows()) {
            // "\\Program Files\\"
            appDir = Paths.get(File.separator + "Program Files", dir);
        } else if (Utils.isMac()) {
            // "/Library/Application Support/"
            appDir = Paths.get(File.separator + "Library", "Application Support", dir);
        }

        if (!Files.exists(appDir))
            try {
                Files.createDirectory(appDir);
            } catch (IOException e) {
                MyLogger.error("Cannot create app dir : " + appDir);
                e.printStackTrace();
            }

        return appDir;
    }

    public static Path getUserDir(String dir) {
        // "/usr/local/share/"
        Path appDir = Paths.get(System.getProperty("user.home"), dir);
        if (Utils.isWindows()) {
            // "\\Program Files\\"
            appDir = Paths.get(System.getProperty("user.home"), dir);
        } else if (Utils.isMac()) {
            // "/Library/Application Support/"
            appDir = Paths.get(System.getProperty("user.home"), "Library", "Application Support", dir);
        }

        if (!Files.exists(appDir))
            try {
                Files.createDirectory(appDir);
            } catch (IOException e) {
                MyLogger.error("Cannot create app dir : " + appDir);
                e.printStackTrace();
            }

        return appDir;
    }

}
