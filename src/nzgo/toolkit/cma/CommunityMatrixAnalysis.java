package nzgo.toolkit.cma;

import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.io.CommunityExporter;
import nzgo.toolkit.core.io.OTUsImporter;
import nzgo.toolkit.core.logger.MyLogger;

import java.io.File;
import java.io.IOException;

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
                if (OTUsImporter.isOTUsFile(fileName)) {
                    MyLogger.info("\nFind OTUs file: " + file);

                    otusFile = file;

                } else if (OTUsImporter.isOTUMappingFile(fileName)) {
                    MyLogger.info("\nFind OTU mapping file: " + file);

                    otuMappingFile = file;

                } else if (OTUsImporter.isReferenceMappingFile(fileName)) {
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
            CommunityExporter.writeRefReads(outFileAndPath, community);

        } else {
            MyLogger.info("\nWarning: create community analysis without providing reference sequence. ");
        }

        outFileAndPath = workPath + File.separator + "community_matrix.csv";
        CommunityExporter.writeCommunityMatrix(outFileAndPath, community);
    }

}
