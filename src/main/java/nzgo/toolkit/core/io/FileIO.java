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

    public static NameParser lineParser = new NameParser(); // default tab and |

    public static boolean hasContent(String line) {
        return !line.startsWith("#") && !line.trim().isEmpty();
    }

    // java 1.6
    public static BufferedReader getReader(File file, String desc) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        if (desc != null)
            MyLogger.info("\nImport " + desc + " file: " + file);

        return reader;
    }

    // java 1.7
    public static BufferedReader getReader(Path path, String desc) throws IOException {

        BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());

        if (desc != null)
            MyLogger.info("\nImport " + desc + " file: " + path);

        return reader;
    }

    // java 1.7
    public static BufferedWriter getWriter(Path path, String desc) throws IOException {

        BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset());

        if (desc != null)
            MyLogger.info("\nOutput " + desc + " file: " + path);

        return writer;
    }

    public static PrintStream getPrintStream(Path outFile, String desc) throws IOException {
        if (outFile == null) return null;

        PrintStream out = new PrintStream(new FileOutputStream(outFile.toString()));

        if (desc != null)
            MyLogger.info("\nOutput " + desc + " file: " + outFile);

        return out;
    }

    public static PrintStream getPrintStream(String outFile, String desc) throws IOException {
        if (outFile == null) return null;

        PrintStream out = new PrintStream(new FileOutputStream(outFile));

        if (desc != null)
            MyLogger.info("\nOutput " + desc + " file: " + outFile);

        return out;
    }



}
