package nzgot.core.community.io;

import nzgot.core.community.AlphaDiversity;
import nzgot.core.community.Community;
import nzgot.core.community.OTU;
import nzgot.core.community.OTUs;
import nzgot.core.community.util.NameParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * Community Matrix Exporter
 * @author Walter Xie
 */
public class CommunityExporter {

    /**
     * write community matrix
     * A COLUMN PER SAMPLE AND A ROW FOR EACH SPECIES/OTU
     * @param outFileAndPath
     * @param community
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void writeCommunityMatrix(String outFileAndPath, Community community) throws IOException, IllegalArgumentException {

        PrintStream out = new PrintStream(new FileOutputStream(outFileAndPath));

        System.out.println("\nReport community matrix " + community.getName() + " in the file : " + outFileAndPath);

        for (String sample : community.getSamples()) {
            out.print(NameParser.SEPARATOR_CSV_COLUMN + sample);
        }
        out.print("\n");

        for(Object o : community){
            OTU otu = (OTU) o;
            out.print(otu.getName());

            AlphaDiversity alphaDiversity = otu.getAlphaDiversity();

            if (alphaDiversity == null)
                throw new IllegalArgumentException("Error: cannot AlphaDiversity report for OTU : " + otu);

            for (int a : alphaDiversity.getAlphaDiversity()) {
                out.print(NameParser.SEPARATOR_CSV_COLUMN + a);
            }
            out.print("\n");
        }

        out.flush();
        out.close();
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

        System.out.println("\nGenerate report of how many reads map to reference sequence in the file: " + outFileAndPath);

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
