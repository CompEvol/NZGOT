package nzgo.toolkit.cma;

import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.io.CommunityFileIO;
import nzgo.toolkit.core.io.OTUsFileIO;
import nzgo.toolkit.core.logger.MyLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Community Matrix Analysis
 * @author Walter Xie
 */
public class CommunityMatrixAnalysis {

    //Main method
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        File otusFile = null;
        File otuMappingFile = null;
        File referenceMappingFile = null;

        File folder = new File(workPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile()) {
                String fileName = file.getName();
                if (OTUsFileIO.isOTUsFile(fileName)) {
                    MyLogger.info("\nFind OTUs file: " + file);

                    otusFile = file;

                } else if (OTUsFileIO.isOTUMappingFile(fileName)) {
                    MyLogger.info("\nFind OTU mapping file: " + file);

                    otuMappingFile = file;

                } else if (OTUsFileIO.isReferenceMappingFile(fileName)) {
                    MyLogger.info("\nFind reference sequence mapping file: " + file);

                    referenceMappingFile = file;

                } else {
                    MyLogger.info("\nIgnore file: " + file);
                }
            }
        }

        if (otuMappingFile == null) throw new IllegalArgumentException("Error: cannot find OTU mapping file !");

        Community community;
        String outFileAndPath;

        if (otusFile != null) {
            MyLogger.info("\nCreate OTUs from otu file. " + otusFile);
        } else {
            MyLogger.info("\nCreate OTUs from mapping file. " + otuMappingFile);
        }

        community = new Community(otusFile, otuMappingFile, referenceMappingFile);

        if (referenceMappingFile != null) {
            outFileAndPath = workPath + File.separator + "report_ref_reads.txt";
            CommunityFileIO.writeRefReads(outFileAndPath, community);

        } else {
            MyLogger.info("\nWarning: create community analysis without providing reference sequence. ");
        }

        Path outCMFilePath = Paths.get(workPath, "community_matrix.csv");
        CommunityFileIO.writeCommunityMatrix(outCMFilePath, community);
    }

}
