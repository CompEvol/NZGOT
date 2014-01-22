package nzgo.toolkit.core.io;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * file FileIO
 * @author Walter Xie
 */
public class FileIO {

    public static NameParser nameParser = new NameParser(); // default tab and |

    public static boolean hasContent(String line) {
        return !line.startsWith("#") && !line.trim().isEmpty();
    }

    // java 1.6
    public static BufferedReader getReader(File file, String msg) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        if (msg != null)
            MyLogger.info("\nImport " + msg + " file: " + file);

        return reader;
    }

    // java 1.7
    public static BufferedReader getReader(Path path, String msg) throws IOException {

        BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());

        if (msg != null)
            MyLogger.info("\nImport " + msg + " file: " + path);

        return reader;
    }

    // java 1.7
    public static BufferedWriter getWriter(Path path, String msg) throws IOException {

        BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset());

        if (msg != null)
            MyLogger.info("\nOutput " + msg + " file: " + path);

        return writer;
    }
}
