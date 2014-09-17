package nzgo.toolkit.cma;

import nzgo.toolkit.core.uparse.io.CommunityFileIO;

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
        String[] experiments = new String[]{"18S-test"}; //"CO1-soilkit","CO1-indirect","ITS","trnL","16S","18S"
        int[] thresholds = new int[]{97}; // 90,91,92,93,94,95,96,97,98,99,100
        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/WalterPipeline/");
        String otuMappingFileName = "mapchimeras.uc";
        String reportFileName = "_otus_report.tsv";
        String cmFileName = "_cm.csv";
//        String otuMappingFileName = "map_size2.uc";
//        String reportFileName = "_otus_size2_report.tsv";
//        String cmFileName = "_cm_size2.csv";

        try {
            // -1 not add size to otus.fasta
            CommunityFileIO.reportCommunityByOTUThreshold(workDir, otuMappingFileName, reportFileName, cmFileName, experiments, thresholds, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");
//
//        String workPath = args[0];
//        MyLogger.info("\nWorking path = " + workPath);
//
//        File otusFile = null;
//        File otuMappingFile = null;
//        File referenceMappingFile = null;
//
//        File folder = new File(workPath);
//        File[] listOfFiles = folder.listFiles();
//
//        for (int i = 0; i < listOfFiles.length; i++) {
//            File file = listOfFiles[i];
//            if (file.isFile()) {
//                String fileName = file.getName();
//                if (OTUsFileIO.isOTUsFile(fileName)) {
//                    MyLogger.info("\nFind OTUs file: " + file);
//
//                    otusFile = file;
//
//                } else if (OTUsFileIO.isOTUMappingFile(fileName)) {
//                    MyLogger.info("\nFind OTU mapping file: " + file);
//
//                    otuMappingFile = file;
//
//                } else if (OTUsFileIO.isReferenceMappingFile(fileName)) {
//                    MyLogger.info("\nFind reference sequence mapping file: " + file);
//
//                    referenceMappingFile = file;
//
//                } else {
//                    MyLogger.info("\nIgnore file: " + file);
//                }
//            }
//        }
//
//        if (otuMappingFile == null) throw new IllegalArgumentException("Error: cannot find OTU mapping file !");
//
//        Community community;
//        String outFileAndPath;
//        SiteNameParser siteNameParser = new SiteNameParser();
//
//        MyLogger.info("\nCreate OTUs from mapping file. " + otuMappingFile);
//
//        community = new Community(otuMappingFile, referenceMappingFile, siteNameParser);
//
//        if (referenceMappingFile != null) {
//            outFileAndPath = workPath + File.separator + "report_ref_reads.txt";
//            CommunityFileIO.writeRefReads(outFileAndPath, community);
//
//        } else {
//            MyLogger.info("\nWarning: create community analysis without providing reference sequence. ");
//        }
//
//        Path outCMFilePath = Paths.get(workPath, CommunityFileIO.COMMUNITY_MATRIX +  ".csv");
//        CommunityFileIO.writeCommunityMatrix(outCMFilePath, community);
    }

}
