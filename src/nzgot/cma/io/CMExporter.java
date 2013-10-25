package nzgot.cma.io;

import nzgot.cma.Community;
import nzgot.cma.OTU;
import nzgot.cma.util.NameParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * Community Matrix Exporter
 * @author Walter Xie
 */
public class CMExporter {

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

        for (String sample : community.samples) {
            out.print(NameParser.SEPARATOR_CSV_COLUMN + sample);
        }
        out.print("\n");

        for(Object o : community){
            OTU otu = (OTU) o;
            out.print(otu.getName());
            for (int a : otu.getAlphaDiversity()) {
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
     * depend on importOTUs, importOTUMapping, importReferenceMappingFile
     * @param outFileAndPath
     * @param community
     * @throws IOException
     * @throws IllegalArgumentException
     */

    public static void writeRefReads(String outFileAndPath, Community community) throws IOException, IllegalArgumentException {
        if (community.getReferenceMappingFile() == null)
            throw new IllegalArgumentException("Error: need to import reference sequence mapping file to generate this report !");

        PrintStream out = new PrintStream(new FileOutputStream(outFileAndPath));

        System.out.println("\nGenerate report of how many reads map to reference sequence in the file: " + outFileAndPath);

        int total = 0;
        Map<String, Integer> readsCountMap = community.getRefSeqReadsCountMap();
        for(Map.Entry<String, Integer> entry : readsCountMap.entrySet()){
            out.println(entry.getKey() + "\t" + entry.getValue());
            total += entry.getValue();
        }
        out.println("total\t" + total);
        out.flush();
        out.close();
    }

}
