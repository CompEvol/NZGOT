package nzgo.toolkit.core.io;

import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.taxonomy.Taxa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Taxonomy FileIO
 * @author Walter Xie
 */
public class TaxonomyFileIO extends FileIO {

    public static Taxa importTaxa (Path taxaTSV) throws IOException {
        Taxa taxa = new Taxa();
        BufferedReader reader = getReader(taxaTSV, "taxa");

        Separator lineSeparator = new Separator("\t");
        String line = reader.readLine();
        while (line != null) {
            if (hasContent(line)) { // not comments or empty
                String[] items = lineSeparator.parse(line);
//                if (items.length < 2)
//                    throw new IllegalArgumentException("Invalid file format for taxa traits mapping, line : " + line);

                taxa.addUniqueElement(items[0]);
            }

            line = reader.readLine();
        }
        reader.close();

        if (taxa.size() < 1)
            throw new IllegalArgumentException("It needs at least one taxon !");

        return taxa;
    }

    public static Map<String, String> importPreTaxaTraits (Path traitsMapTSV) throws IOException {
        Map<String, String> preTaxaTraits = new TreeMap<>();
        BufferedReader reader = getReader(traitsMapTSV, "pre-defined taxa traits mapping");

        Separator lineSeparator = new Separator("\t");
        String line = reader.readLine();
        while (line != null) {
            if (hasContent(line)) { // not comments or empty
                String[] items = lineSeparator.parse(line);
                if (items.length < 2)
                    throw new IllegalArgumentException("Invalid file format for taxa traits mapping, line : " + line);
                if (preTaxaTraits.containsKey(items[0]))
                    throw new IllegalArgumentException("Find duplicate name for leaf node : " + items[0]);

                preTaxaTraits.put(items[0], items[1]);
            }

            line = reader.readLine();
        }
        reader.close();

        if (preTaxaTraits.size() < 1)
            throw new IllegalArgumentException("It needs at least one separator !");

        return preTaxaTraits;
    }

    public static void writeTaxaTraits (Path traitsMapTSV, String[][] taxaTraits) throws IOException {
        BufferedWriter writer = getWriter(traitsMapTSV, "taxa traits map");

//        writer.write("# \n");
        for (int i = 0; i < taxaTraits.length; i++) {
            writer.write(taxaTraits[i][0] + "\t" + taxaTraits[i][1] + "\n");
        }

        writer.flush();
        writer.close();
    }

    public static void writeTaxaMap (Path traitsMapTSV, Map<String, String> taxaTraits) throws IOException {
        ConfigFileIO.writeTSVFileFromMap(traitsMapTSV, taxaTraits, "taxa traits map");
    }

}
