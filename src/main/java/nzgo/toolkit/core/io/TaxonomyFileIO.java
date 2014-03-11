package nzgo.toolkit.core.io;

import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.taxonomy.Rank;
import nzgo.toolkit.core.taxonomy.Taxa;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.taxonomy.TaxonomyPool;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.SortedMap;
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

        String line = reader.readLine();
        while (line != null) {
            if (hasContent(line)) { // not comments or empty
                String[] items = lineParser.getSeparator(0).parse(line);
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

    /**
     * 1st column is Element (Sequence/OTU/tree leaf), 2nd is taxid
     * @param inFilePath
     * @return
     * @throws IOException
     */
    public static SortedMap<String, Taxon> importElementTaxonomyMap(Path inFilePath) throws IOException, XMLStreamException {
        SortedMap<String, Taxon> otuTaxaMap = new TreeMap<>();
        BufferedReader reader = getReader(inFilePath, "Element taxonomy mapping");

        Separator lineSeparator = new Separator("\t");
        String line = reader.readLine();
        while (line != null) {
            if (hasContent(line)) { // not comments or empty
                String[] items = lineSeparator.parse(line);
                if (items.length < 2)
                    throw new IllegalArgumentException("Invalid file format for Element taxonomy mapping, line : " + line);
                if (otuTaxaMap.containsKey(items[0]))
                    throw new IllegalArgumentException("Find duplicate name for " + items[0]);

                Taxon taxon = TaxonomyPool.getAndAddTaxIdByMemory(items[1]);
                otuTaxaMap.put(items[0], taxon);
            }

            line = reader.readLine();
        }
        reader.close();

        if (otuTaxaMap.size() < 1)
            throw new IllegalArgumentException("OTU taxonomy map is empty !");

        return otuTaxaMap;
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

    /**
     * key is Element (Sequence/OTU/tree leaf), value is LCA, taxid, rank
     * if ranks = null, then no rank column
     * @param outFilePath
     * @param otuTaxaMap
     * @param ranks
     * @throws java.io.IOException
     */
    public static void writeElementTaxonomyMap(Path outFilePath, SortedMap<String, Taxon> otuTaxaMap, Rank... ranks) throws IOException {
        BufferedWriter writer = getWriter(outFilePath, "taxonomic mapping" + (ranks==null?"":" and assignment") );

        //        writer.write("# \n");
        for (Map.Entry<String, Taxon> entry : otuTaxaMap.entrySet()) {
            Taxon taxon = entry.getValue();
            writer.write(entry.getKey() + "\t" + (taxon==null?"":taxon.getScientificName()));
            writer.write("\t" + (taxon==null?"":taxon.getTaxId()) + "\t" + (taxon==null?"":taxon.getRank()));

            if (taxon != null && ranks != null) {
                for (Rank rank : ranks) {
                    Taxon t = taxon.getParentTaxonOn(rank);
                    String str = ("no " + rank.toString()).toLowerCase();
                    writer.write("\t" + (t==null?str:t.getScientificName()));
                    writer.write("\t" + (t==null?str:t.getTaxId()) + "\t" + (t==null?str:t.getRank()) );
                }
            }

            writer.write("\n");
        }

        writer.flush();
        writer.close();
    }
}
