package nzgo.toolkit.core.ncbi.submission;

import nzgo.toolkit.core.io.ConfigFileIO;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;

/**
 * MD5 Checksum Table
 *
 * @author Walter Xie
 */
public class MD5ChecksumTable {

    final static String[] genes = new String[]{"16S", "18S", "COI", "ITS", "trnL"};
    final static String[] location = new String[]{"1-B","1-L","2-C","2-M","3-H","3-N","4-A","4-N","5-I","5-N","6-I","6-N","7-M","7-N","8-I","8-O","CM30C30-L","CM30C30-N","LB1-A","LB1-E"};
    final static String[] biosamples = new String[]{"SAMN03200223", "SAMN03200224", "SAMN03200225", "SAMN03200226", "SAMN03200227", "SAMN03200228", "SAMN03200229", "SAMN03200230", "SAMN03200231", "SAMN03200232", "SAMN03200233", "SAMN03200234", "SAMN03200235", "SAMN03200236", "SAMN03200237", "SAMN03200238", "SAMN03200239", "SAMN03200240", "SAMN03200241", "SAMN03200242"};

    public MD5ChecksumTable(Path inPath, Path outPath) {
        assert genes.length == 5;
        assert location.length == 20;
        assert biosamples.length == 20;

        SortedMap<String, String> md5Map = null;
        try {
            md5Map = ConfigFileIO.importTwoColumnTSV(inPath, "md5 mapping");

            outputTable(outPath, md5Map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outputTable(Path outPath, SortedMap<String, String> md5Map) throws IOException {
        PrintStream out = FileIO.getPrintStream(outPath, "md5 table");

        for (int i = 0; i < location.length; i++) {
            for (String gene : genes) {
                String fileName = gene + "-" + location[i] + NameSpace.SUFFIX_FASTQ;
                String checksum = md5Map.get(fileName);
                out.println(biosamples[i] + "\t" + gene + "-" + location[i] + "\t" + fileName + "\t" + checksum +
                        "\t" + "Hauturu " + location[i] + " soil Dec 2010");
            }
        }

        out.flush();
        out.close();
    }



    //Main method
    public static void main(final String[] args) {
        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/GigaDB-NZGO/");
        MyLogger.info("\nWorking path = " + workDir);

        Path inPath = Module.validateInputFile(workDir, "md5.txt", "input");

        Path outPath = Paths.get(workDir.toString(), "md5Table.txt");

        MD5ChecksumTable sourceModifiersTable = new MD5ChecksumTable(inPath, outPath);
    }

}
