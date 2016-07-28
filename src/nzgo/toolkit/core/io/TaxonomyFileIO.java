package nzgo.toolkit.core.io;

import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.taxonomy.Rank;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.taxonomy.TaxonSet;
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
    public static final String TAXONOMY_ASSIGNMENT = "taxonomy_assignment";

    public static TaxonSet importTaxa (Path taxaTSV) throws IOException {
        TaxonSet taxonSet = new TaxonSet();
        BufferedReader reader = getReader(taxaTSV, "taxa");

        Separator lineSeparator = new Separator("\t");
        String line = reader.readLine();
        while (line != null) {
            if (hasContent(line)) { // not comments or empty
                String[] items = lineSeparator.parse(line);
//                if (items.length < 2)
//                    throw new IllegalArgumentException("Invalid file format for taxa traits mapping, line : " + line);

                taxonSet.addUniqueElement(items[0]);
            }

            line = reader.readLine();
        }
        reader.close();

        if (taxonSet.size() < 1)
            throw new IllegalArgumentException("It needs at least one taxon !");

        return taxonSet;
    }

    public static SortedMap<String, String> importPreTaxaTraits (Path traitsMapTSV) throws IOException {
        return ConfigFileIO.importTwoColumnTSV(traitsMapTSV, "pre-defined taxa traits mapping");
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
     * @param elementTaxonMap
     * @param ranks
     * @throws java.io.IOException
     */
    public static void writeElementTaxonomyMap(Path outFilePath, SortedMap<String, Taxon> elementTaxonMap, Rank... ranks) throws IOException {
        BufferedWriter writer = getWriter(outFilePath, "taxonomic mapping" + (ranks==null?"":" and assignment") );

        int total = 0;
        for (Map.Entry<String, Taxon> entry : elementTaxonMap.entrySet()) {
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

            total++;
        }

        writer.flush();
        writer.close();

        MyLogger.debug("Total entry in ElementTaxonomyMap = " + total);
    }

    /**
     * Taxonomy Assignment of Community at Rank
     * if rank == null, then get the assignment of overall
     *
     * @param outTAFilePath
     * @param taxonomySet
     * @param rank
     * @throws IOException
     */
    public static void writeTaxonomyAssignment(Path outTAFilePath, TaxonSet<Taxon> taxonomySet, Rank rank) throws IOException {

        BufferedWriter writer = getWriter(outTAFilePath, "taxonomy assignment");

        int total1 = 0;
        int total2 = 0;
        for(Taxon t : taxonomySet){
            String taxonName = t.getScientificName(); // rank == null
            if (rank != null) {
                // get parent taxon at the given rank
                Taxon ta = t.getParentTaxonOn(rank);
//                String str = ("no " + rank.toString()).toLowerCase();
                taxonName = ta.getScientificName();
            }
            writer.write(taxonName);
            int c1 = t.getCounter(OTUs.READS_COUNTER_ID).getCount();
            writer.write("\t" + c1);
            int c2 = t.getCounter(OTUs.OTUS_COUNTER_ID).getCount();
            writer.write("\t" + c2);
            writer.write("\n");

            total1 += c1;
            total2 += c2;
        }

        writer.flush();
        writer.close();

        MyLogger.debug("Total reads = " + total1 + ", total OTUs = " + total2);
    }

    public static void writeTaxonomyReport(Path outTAFilePath, TaxonSet<Taxon> taxonomySet, Rank... rank) {

    }

}
