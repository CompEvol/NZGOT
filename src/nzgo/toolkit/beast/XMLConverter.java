package nzgo.toolkit.beast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * change models in beast 2 xml.
 *
 * @author Walter Xie
 */
public abstract class XMLConverter {
    public XMLConverter() { }

    protected void batchConvertXML(String sourcePath, String targetPath, String sFilePrefix, String tFilePrefix, int replicates){
        for (int r=0; r<replicates; r++) {
            // source
            Path inPath = Paths.get(sourcePath, sFilePrefix + r + ".xml");
            BufferedReader reader = null;
            try {
                reader = Files.newBufferedReader(inPath, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // target
            Path outPath = Paths.get(targetPath, tFilePrefix + r + ".xml");
            PrintStream out = null;
            try {
                out = new PrintStream(outPath.toFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            System.out.println("Convert " + inPath + " to " + outPath);
            // print head
            assert out != null;
            convertXML(reader, out);
        }
    }

    protected void convertXML(BufferedReader reader, PrintStream out){
        try {
            String replaceTo = replaceFrom(reader);
            while (replaceTo != null) {
                out.println(replaceTo);
                replaceTo = replaceFrom(reader);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.flush();
        out.close();
    }

    protected abstract String replaceFrom(final BufferedReader reader) throws IOException;
}
