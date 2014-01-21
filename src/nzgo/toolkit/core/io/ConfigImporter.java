package nzgo.toolkit.core.io;

import beast.evolution.tree.Tree;
import nzgo.toolkit.core.naming.Separator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Config Importer
 * @author Walter Xie
 */
public class ConfigImporter extends Importer {

    public static List<Separator> importSeparators (Path separatorsTSV) throws IOException {
        List<Separator> separators = new ArrayList<>();
        BufferedReader reader = getReader(separatorsTSV, "customized separators");

        Separator lineSeparator = new Separator("\t");
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("#")) { // comments
                String[] items = lineSeparator.parse(line);

                Separator separator = new Separator(items[0]);
                if (items.length > 1) {
                    int splitIndex = Integer.parseInt(items[1]);
                    separator.setSplitIndex(splitIndex);
                }
                separators.add(separator);
            }

            line = reader.readLine();
        }

        reader.close();

        return separators;
    }

    public static void insertTraitsToTree (Path traitsMapTSV, Tree newickTree) throws IOException {

        BufferedReader reader = getReader(traitsMapTSV, "traits mapping");

        Separator lineSeparator = new Separator("\t");
        String line = reader.readLine();
        while (line != null) {
            String[] items = lineSeparator.parse(line);

            if (line.startsWith("#")) { // comments

            }

            line = reader.readLine();
        }

        reader.close();




    }

}
