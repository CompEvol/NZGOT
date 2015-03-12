package nzgo.toolkit.beast.one;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * remove taxa from beast 1 xml.
 *
 * @author Walter Xie
 */
public class TaxaEditor {
    private static String[] taxaToKeep = new String[]{
//    "CB070117.03_1","CB070121.08_1","CB070121.13_1","CB070121.16_1","CB121212.12_1","CB111229.15_1","CB130106.26_1" //25gene7t716
//   "CB070117.03_1","CB070121.08_1","CB070121.13_1","CB070121.16_1","CB121212.12_1","CA1.T401_1","CA3.T415_1" //25gene7t0
          "CA1.T401_1","CA3.T415_1","CB070117.03_1","CB070121.08_1","CB070121.16_1","CB111229.15_1",
          "CB130106.05_1","CB130106.24_1" //8t
    };

    public TaxaEditor() { }

    public static void main(String[] args) {

        Path workPath = Paths.get("C:\\Beast1\\Adelie");

        Path inputXML = Paths.get(workPath.toString(), "113g46t.xml");

        try {
            BufferedReader reader = FileIO.getReader(inputXML, "xml file");

            Path outputXML = Paths.get(workPath.toString(), "113g8t.xml");
            PrintStream out = FileIO.getPrintStream(outputXML, "XML");

            String line = reader.readLine();
            while (line != null) {
                if (line.contains("ntax=46")) {
                    out.println(line.replace("46", Integer.toString(taxaToKeep.length)));
                } else if (line.contains("fileName=")) {
                    out.println(line.replaceAll("46t", Integer.toString(taxaToKeep.length) + "t"));
                } else if (line.contains("<taxon id=")) {
                    if (StringUtil.contains(line, taxaToKeep)) {
                        out.println(line); // <taxon id=
                        line = reader.readLine();
                        out.println(line); // <date value="
                        line = reader.readLine();
                        out.println(line); // </taxon>
                    } else {
                        line = reader.readLine();
                        line = reader.readLine();
                    }
                } else if (line.contains("<sequence>")) {
                    String nextLine = reader.readLine();
                    if (StringUtil.contains(nextLine, taxaToKeep)) {
                        out.println(line); // <sequence>
                        out.println(nextLine); // <taxon idref=
                        line = reader.readLine();
                        out.println(line); // TGGAAT...
                        line = reader.readLine();
                        out.println(line); // </sequence>
                    } else {
                        line = reader.readLine();
                        line = reader.readLine();
                    }
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
