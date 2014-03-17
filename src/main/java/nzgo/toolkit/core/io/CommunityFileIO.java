package nzgo.toolkit.core.io;

import nzgo.toolkit.core.community.AlphaDiversity;
import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.Rank;
import nzgo.toolkit.core.taxonomy.Taxon;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Map;

/**
 * Community Matrix FileIO
 * Not attempt to store reads as Sequence, but as String
 * both OTU and reference mapping file are uc format.
 * @author Walter Xie
 */
public class CommunityFileIO extends OTUsFileIO {

    public static final String COMMUNITY_MATRIX = "community_matrix";

    /**
     * write community matrix
     * A COLUMN PER SAMPLE AND A ROW FOR EACH SPECIES/OTU
     *
     * @param outCMFilePath
     * @param community
     * @param printTaxonomy      print the OTU's taxonomy
     * @param ranks              has to set printTaxonomy = true
     * @throws IOException
     * @throws IllegalArgumentException
     */
    //TODO tidy up my strange code
    public static int[] writeCommunityMatrix(Path outCMFilePath, Community community, boolean printTaxonomy, Rank... ranks) throws IOException, IllegalArgumentException {

        BufferedWriter writer = getWriter(outCMFilePath, "community matrix");

        for (String sample : community.getSamples()) {
            writer.write("," + sample);
        }
        writer.write("\n");

        int total = 0;
        int otu1Read = 0;
        int otu2Reads = 0;
        for(Object o : community){
            OTU otu = (OTU) o;

            writer.write(otu.getName());

            // print AlphaDiversity column
            AlphaDiversity alphaDiversity = otu.getAlphaDiversity();
            if (alphaDiversity != null) {
//                throw new IllegalArgumentException("Error: cannot AlphaDiversity report for OTU : " + otu);

                for (int a : alphaDiversity.getAlphaDiversity()) {
                    writer.write("," + a);
                }
            }

//            print Taxon column
            if (printTaxonomy && otu.hasTaxon()) {
                Taxon taxonLCA = otu.getTaxonLCA();
                writer.write("," + taxonLCA.getScientificName());
                if (taxonLCA != null && ranks != null) {
                    for (Rank rank : ranks) {
                        Taxon t = taxonLCA.getParentTaxonOn(rank);
                        String str = ("no " + rank.toString()).toLowerCase();
                        writer.write("," + (t==null?str:t.getScientificName()));
                    }
                }
            }

            writer.write("\n");

            int size = otu.size();
            total += size;
            if (size == 1) {
                otu1Read ++;
            } else if (size == 2) {
                otu2Reads ++;
            }
        }

        writer.flush();
        writer.close();

        MyLogger.info("\nCommunity Matrix " + community.getName() + ": " + community.size() + " OTUs, " + total + " sequences, " +
                otu1Read + " OTUs represented by 1 reads, " + otu2Reads + " OTUs represented by 2 reads, " +
                community.getSamples().length + " samples = " + community.getSamples());

        return new int[]{community.size(), total, otu1Read, otu2Reads, community.getSamples().length};
    }

    public static int[] writeCommunityMatrix(Path outCMFilePath, Community community) throws IOException, IllegalArgumentException {
        return writeCommunityMatrix(outCMFilePath, community, false);
    }

    /**
     * 2 columns: 1st -> reference sequence id, 2nd -> number of reads
     * last row is total reads
     * at least one OTU should have reference
     * @param outFileAndPath
     * @param otus
     * @throws IOException
     * @throws IllegalArgumentException
     */

    public static void writeRefReads(String outFileAndPath, OTUs otus) throws IOException, IllegalArgumentException {

        PrintStream out = new PrintStream(new FileOutputStream(outFileAndPath));

        MyLogger.info("\nGenerate report of how many reads map to reference sequence in the file: " + outFileAndPath);

        int total = 0;
        Map<String, Integer> readsCountMap = otus.getRefSeqReadsCountMap();
        for(Map.Entry<String, Integer> entry : readsCountMap.entrySet()){
            out.println(entry.getKey() + "\t" + entry.getValue());
            total += entry.getValue();
        }
        out.println("total\t" + total);
        out.flush();
        out.close();
    }


}
