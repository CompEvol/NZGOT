package nzgot.cma.io;

import nzgot.cma.Community;

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
     * 2 columns: 1st -> reference sequence id, 2nd -> number of reads
     * last row is total reads
     * depend on importOTUs, importOTUMapping, importReferenceMappingFile
     * @param outFileAndPath
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    public static void writeRefReads(String outFileAndPath, Community community) throws IOException, IllegalArgumentException {
        if (community.getReferenceMappingFile() == null)
            throw new IllegalArgumentException("Error: need to import reference sequence mapping file to generate this report !");

        PrintStream out = new PrintStream(new FileOutputStream(outFileAndPath));

        System.out.println("\nGenerate report of how many reads map to reference sequence in the file: " + outFileAndPath);

        int total = 0;
        for(Map.Entry<String, Integer> entry : community.getRefSeqReadsCount().entrySet()){
            out.println(entry.getKey() + "\t" + entry.getValue());
            total += entry.getValue();
        }
        out.println("total\t" + total);
        out.flush();
        out.close();
    }

}
