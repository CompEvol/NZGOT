package nzgo.toolkit.edna;

import jebl.evolution.io.ImportException;
import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameParser;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.r.Matrix;
import nzgo.toolkit.core.uparse.io.CommunityFileIO;
import nzgo.toolkit.uparse.CommunityMatrix;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Create Community Matrix from UPARSE output file
 * @author Walter Xie
 */
public class CMCreator extends Module {

    public CMCreator() {
        super("CMCreator", "Create community matrix from UPARSE output UP file");
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<input-file-name>]");
        System.out.println("  <input-file-name> is up mapping file from UPARSE.");
        System.out.println();
//        System.out.println("  Example: " + getName() + " clusters.uc");
        System.out.println("  Example: " + getName() + " out.up");
        System.out.println("  Example: " + getName() + " -working /mypath out.up");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    //Main method
    public static void main(final String[] args) {
        Module module = new CMCreator();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("qc", "qc-folder",
                        "qc folder to contain all dereplication results, such as derep.uc."),
                new Arguments.StringOption("otus", "otus-folder",
                        "otus folder to contain all OTUs and chimeras results, " +
                                "such as otus.fasta and chimeras.fasta"),
                new Arguments.StringOption("prefix", "output-prefix",
                        "prefix for output community matrix in csv file, such as 16s.csv, " +
                                "and final OTUs, such as 16s.fasta.")
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path workDir = module.init(arguments, args);

        // if give final OTUs in input, then skip rm chimera OTUs
//        String inputFileName = module.getFirstArg(arguments);
//        if (inputFileName != null) {
//            Path inputFile = module.getInputFile(workDir, inputFileName, NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FNA);
//        } else {
//
//        }

        MyLogger.info("\nWorking path = " + workDir);

        // output
        String outFilePrefix = "16s";
        if (arguments.hasOption("prefix")) {
            outFilePrefix = arguments.getStringOption("prefix");
        }

        String qcFolder = "qc";
        if (arguments.hasOption("qc")) {
            qcFolder = arguments.getStringOption("qc");
        }

        String otusFolder = "otus97";
        if (arguments.hasOption("otus")) {
            otusFolder = arguments.getStringOption("otus");
        }

        Path otusPath = Paths.get(workDir.toString(), otusFolder, "otus.fasta");
        Path chimerasPath = Paths.get(workDir.toString(), otusFolder, "chimeras.fasta");
        Path finalOTUsPath = Paths.get(workDir.toString(), otusFolder, outFilePrefix+".fasta");

        if (Files.exists(finalOTUsPath)) {
            MyLogger.info("Final OTUs exist" + finalOTUsPath.toString() + ", use it and skip removing chimera OTUs.");
        } else {
            MyLogger.info("Removing chimera OTUs from " + otusPath.toString());
            try {
                CommunityMatrix.removeChimeras(otusPath, chimerasPath, finalOTUsPath);
            } catch (IOException | ImportException e) {
                e.printStackTrace();
            }
        }

        Path derepUcPath = Paths.get(workDir.toString(), qcFolder, "derep.uc");
        Path outUpPath = Paths.get(workDir.toString(), otusFolder, "out.up");
        Path cmPath = Paths.get(workDir.toString(), otusFolder, outFilePrefix+".csv");

        Matrix communityMatrix = null;
        try {
            communityMatrix = CommunityMatrix.createCommunityMatrix(finalOTUsPath, outUpPath, derepUcPath);

            CommunityMatrix.writeCommunityMatrix(cmPath, communityMatrix, ",");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
