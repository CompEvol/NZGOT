package nzgo.toolkit.beast;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 */
public class CopyNex {
    private final static String srcDir = "C:\\Beast1\\Adelie\\16X_113_genome";//"C:\\Beast1\\Adelie\\16X_25_intron";//
    private final static String tagDir = "C:\\Beast1\\Adelie\\113genome";//"C:\\Beast1\\Adelie\\25intron";//

    public CopyNex() {
    }

    public static void main(String[] args) throws Exception {
        Path srcPath = Paths.get(srcDir);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(srcPath)) {
            for(Path nexFile : stream) {
                if (Files.exists(nexFile)) {
                    BufferedReader reader = Files.newBufferedReader(nexFile, Charset.defaultCharset());

                    String fileName = nexFile.getFileName().toString();
                    Path tagPath = Paths.get(tagDir, fileName);
                    PrintStream out = new PrintStream(tagPath.toFile());

                    System.out.println("Copy " + nexFile + " to " + tagPath);

                    String line = reader.readLine();
                    while (line != null) {
                        if (line.contains("NTAX=92")) {
                            line = line.replace("92", "46");
                        }

                        if (line.contains("28434=28434:CB070121.13_1")) {
                            line = line.replace(",", "");
                        }

                        if (!line.contains("_2") || line.contains("CLA_20_1")) {
                            out.println(line);
                        }

                        line = reader.readLine();
                    }
                    reader.close();

                    out.flush();
                    out.close();
                }
            }
        }

    }
    
}
