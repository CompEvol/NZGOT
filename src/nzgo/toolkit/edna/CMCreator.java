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
        super("CMCreator", "Create community matrix from UPARSE output UP file and dereplication UC file.");
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<otus-file-name>]");
        System.out.println("  <otus-file-name> is OTUs from UPARSE, such as otus.fasta, " +
                "or final OTUs removed chimeras OTUs, such as 16s.fasta.\n" +
                "If give chimeras OTUs using option -chimeotus chimeras.fasta, " +
                "then remove chimera OTUs from input OTUs to generate final OTUs and community matrix, " +
                "otherwise skip removing chimera OTUs and use input as the final OTUs to create community matrix.\n" +
                "Note: qc-folder containing all dereplication results (derep.uc) has to be " +
                "the sibling folder containing all OTUs and chimeras results, " +
                "which is mostly the directory to run this program."
        );
        System.out.println();
        System.out.println("  Example: " + getName() + " 16s.fasta");
        System.out.println("  Example: " + getName() + " otus.fasta -chimeotus chimeras.fasta -prefix 18s");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    //Main method
    public static void main(final String[] args) {
        Module module = new CMCreator();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("chimeotus", "chimeras-otus",
                        "chimeras OTUs discovered by Uchime, such as chimeras.fasta."),
                new Arguments.StringOption("qc", "qc-folder",
                        "qc folder to contain all dereplication results, such as derep.uc."),
//                new Arguments.StringOption("otus", "otus-folder",
//                        "otus folder to contain all OTUs and chimeras results, " +
//                                "such as otus.fasta and chimeras.fasta"),
                new Arguments.StringOption("prefix", "output-prefix",
                        "prefix for output community matrix in csv file, such as 16s.csv, " +
                                "and final OTUs, such as 16s.fasta.")
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path workDir = module.init(arguments, args);
        MyLogger.info("\nWorking path = " + workDir);

        // input OTUs, if give chimeras OTUs,
        // then rm chimera OTUs from input to generate final OTUs and CM,
        // otherwise skip rm chimera OTUs and use input as the final OTUs
        String inputFileName = module.getFirstArg(arguments, true);
        Path otusPath = module.getInputFile(workDir, inputFileName, NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FNA);

        // output
        String outFilePrefix = "16s";
        if (arguments.hasOption("prefix")) {
            outFilePrefix = arguments.getStringOption("prefix");
        }
        String qcFolder = "qc";
        if (arguments.hasOption("qc")) {
            qcFolder = arguments.getStringOption("qc");
        }
//        String otusFolder = "otus97";
//        if (arguments.hasOption("otus")) {
//            otusFolder = arguments.getStringOption("otus");
//        }

        Path chimerasPath = null;
        if (arguments.hasOption("chimeotus")) {
            String chimeraOTUs = arguments.getStringOption("chimeotus");
            chimerasPath = module.getInputFile(workDir, chimeraOTUs, NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FNA);
        }

        Path finalOTUsPath = Paths.get(workDir.toString(), outFilePrefix+".fasta");
        if (chimerasPath != null) {
            MyLogger.info("Removing chimera OTUs from " + otusPath.toString());

            if (!arguments.hasOption("overwrite") && Files.exists(finalOTUsPath))
                throw new IllegalArgumentException("Find output file " + finalOTUsPath + ", use -overwrite option to allow overwrite.");

            try {
                CommunityMatrix.removeChimeras(otusPath, chimerasPath, finalOTUsPath);
            } catch (IOException | ImportException e) {
                e.printStackTrace();
            }
        } else {
            //skip rm chimera OTUs and use input as the final OTUs
            finalOTUsPath = otusPath;
            MyLogger.info("Use given final OTUs " + finalOTUsPath.toString() + " and skip removing chimera OTUs.");
        }

        Path derepUcPath = module.getInputFile(Paths.get(workDir.getParent().toString(), qcFolder), "derep.uc", NameSpace.SUFFIX_UC);
        Path outUpPath = module.getInputFile(workDir, "out.up", NameSpace.SUFFIX_UP);
        Path cmPath = Paths.get(workDir.toString(), outFilePrefix+".csv");

        if (!arguments.hasOption("overwrite") && Files.exists(cmPath))
            throw new IllegalArgumentException("Find output file " + cmPath + ", use -overwrite option to allow overwrite.");

        Matrix communityMatrix = null;
        try {
            communityMatrix = CommunityMatrix.createCommunityMatrix(finalOTUsPath, outUpPath, derepUcPath);

            CommunityMatrix.writeCommunityMatrix(cmPath, communityMatrix, ",");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
