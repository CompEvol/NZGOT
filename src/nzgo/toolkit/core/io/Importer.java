package nzgo.toolkit.core.io;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.util.NameParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * file Importer
 * @author Walter Xie
 */
public class Importer {

    public static NameParser nameParser = new NameParser(); // default tab

    public static BufferedReader getReader(File file, String msg) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        if (msg != null)
            MyLogger.info("\nImport " + msg + " file: " + file);

        return reader;
    }

}
