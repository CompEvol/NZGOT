package java.nzgot.cma;

import java.io.File;
import java.io.IOException;
import java.nzgot.cma.bio.CommunityMatrix;

/**
 * Community Matrix Analysis
 * @author Walter Xie
 */
public class CommunityMatrixAnalysis {

    //Main method
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        System.out.println("\nWorking path = " + workPath);

        File otusFile = null;
        File otuMappingFile = null;
        File referenceMappingFile = null;

        File folder = new File(workPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile()) {
                String fileName = file.getName();
                if (CommunityMatrix.isOTUsFile(fileName)) {
                    System.out.println("\nFind OTUs file: " + file);

                    otusFile = file;

                } else if (CommunityMatrix.isOTUMappingFile(fileName)) {
                    System.out.println("\nFind OTU mapping file: " + file);

                    otuMappingFile = file;

                } else if (CommunityMatrix.isReferenceMappingFile(fileName)) {
                    System.out.println("\nFind reference sequence mapping file: " + file);

                    referenceMappingFile = file;

                } else {
                    System.out.println("\nIgnore file: " + file);
                }
            }
        }

        if (otusFile == null) throw new IllegalArgumentException("Error: cannot find OTUs file !");
        if (otuMappingFile == null) throw new IllegalArgumentException("Error: cannot find OTU mapping file !");

        CommunityMatrix communityMatrix = new CommunityMatrix(otusFile, otuMappingFile);

        if (referenceMappingFile == null) throw new IllegalArgumentException("Error: cannot find reference mapping file !");
        communityMatrix.importReferenceMappingFile(referenceMappingFile);

        String outFile = workPath + File.separator + "report_ref_reads.txt";
        communityMatrix.writeRefReads(outFile);
    }

}
