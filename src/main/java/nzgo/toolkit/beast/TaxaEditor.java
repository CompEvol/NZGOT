package nzgo.toolkit.beast;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * remove taxa from beast 2 xml.
 *
 * @author Walter Xie
 */
public class TaxaEditor {
    private static String[] taxaToKeep = new String[]{
            "CB070121.08_1", "CB070121.16_1", "CB111229.15_1", "CB130106.26_1" // 100gene4t329
//        "CB070121.08_1", "CB070121.16_1", "CA1.T401_1", "CA3.T415_1"     // 100gene4t0
    };

    public TaxaEditor() {

    }

    public static void main(String[] args) {

        Path workPath = Paths.get(System.getProperty("user.home") + "/Documents/BEAST2/Adelie/Adelie/");

        Path inputXML = Paths.get(workPath.toString(), "403gene96t.xml");

        try {
            BufferedReader reader = FileIO.getReader(inputXML, "xml file");

            Path outputXML = Paths.get(workPath.toString(), "403gene4t329.xml");
            PrintStream out = FileIO.getPrintStream(outputXML, "XML");

            String line = reader.readLine();
            while (line != null) {
                if (line.contains("<sequence id=")) {
                    if (StringUtil.contains(line, taxaToKeep)) {
                        out.println(line);
                    }
                } else if (line.contains("<trait id=\"dateTrait.t:")) {
                    out.println(line);
                    //traits in xml
                    String traits = "";
                    while (!line.contains("<taxa id=\"TaxonSet")) {
                        if (StringUtil.contains(line, taxaToKeep)) {
                            traits += "\n" + line;
                        }
                        line = reader.readLine();
                    }
                    // last line of traits in xml
                    if (traits.endsWith(",")) {
                        traits = traits.substring(1, traits.lastIndexOf(",")); // 1st char is \n
                    }
                    traits += "\n" + line;
                    out.println(traits);
                } else {
                    out.println(line);
                }

                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    } // main
}
