package nzgo.toolkit.edna;

import jebl.evolution.io.ImportException;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.r.Matrix;
import nzgo.toolkit.uparse.CommunityMatrix;
import nzgo.toolkit.uparse.FinalOTUs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * Create Community Matrix from UPARSE output file
 * @author Walter Xie
 */
public class CMCreator extends Module {

    public CMCreator() {
        super("CMCreator", "Create community matrix from UPARSE OTU clustering UP mapping file and dereplication UC file.");
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<otus-file-name>]");
        System.out.println("  <otus-file-name> is compulsory input always in the last argument. " +
                "It can be either OTUs from UPARSE, such as otus.fasta, " +
                "or final OTUs after chimera OTUs are removed, such as 16s.fasta.\n" +
                "\n************************* Description ****************************\n" +
                "If give chimeras OTUs using option -chimeotus chimeras.fasta, " +
                "then remove chimera OTUs from input OTUs to generate final OTUs and community matrix, " +
                "otherwise skip removing chimera OTUs and use input as the final OTUs to create community matrix.\n" +
                "Note: qc-folder containing all dereplication results (derep.uc) has to be " +
                "the sibling folder containing all OTUs and chimeras results, " +
                "which is mostly the directory to run this program."
        );
        System.out.println();
        System.out.println("  Example: " + getName() + " 16s.fasta");
        System.out.println("  Example: " + getName() + " -chimeotus chimeras.fasta -prefix 18s otus.fasta");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    //Main method
    public static void main(final String[] args) {
        Module module = new CMCreator();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("chimeotus", "chimera-otus",
                        "chimeras OTUs discovered by Uchime, such as chimeras.fasta."),
                new Arguments.StringOption("qc", "qc-folder",
                        "qc folder to contain all dereplication results, such as derep.uc."),
//                new Arguments.StringOption("otus", "otus-folder",
//                        "otus folder to contain all OTUs and chimeras results, " +
//                                "such as otus.fasta and chimeras.fasta"),
                new Arguments.StringOption("prefix", "output-prefix",
                        "prefix for output community matrix in csv file, such as 16s.csv, " +
                                "and final OTUs, such as 16s.fasta."),
                new Arguments.Option("nocm", "Only used to remove chimera OTUs, " +
                        "no community matrix created."),
                new Arguments.Option("debug", "Give more messages in debug level.")
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path workDir = module.init(arguments, args);
        MyLogger.info("\nWorking path = " + workDir);

        // input OTUs, if give chimeras OTUs,
        // then rm chimera OTUs from input to generate final OTUs and CM,
        // otherwise skip rm chimera OTUs and use input as the final OTUs
        String inputFileName = module.getFirstArg(arguments, true);
        Path otusPath = module.getInputFile(workDir, inputFileName, NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FNA);

        if (arguments.hasOption("debug")) {
            MyLogger.setLevel(Level.FINE);
        } else {
            MyLogger.setLevel(Level.INFO);
        }

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
            chimerasPath = module.inputValidFile(workDir, chimeraOTUs, "chimeras OTUs",
                    NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FNA);
        }

        Path finalOTUsPath = Paths.get(workDir.toString(), outFilePrefix+".fasta");
        if (chimerasPath != null) {
            MyLogger.info("Removing chimera OTUs from " + otusPath.toString());

            if (!arguments.hasOption("overwrite") && Files.exists(finalOTUsPath))
                throw new IllegalArgumentException("Find output file " + finalOTUsPath +
                        ", use -overwrite option to allow overwrite.");

            FinalOTUs finalOTUs = new FinalOTUs(otusPath, chimerasPath);
            try {
                finalOTUs.rmChimeraOTUs(finalOTUsPath);
            } catch (IOException | ImportException e) {
                e.printStackTrace();
            }

            if (arguments.hasOption("nocm")) {
                MyLogger.info("Only remove chimera OTUs, no community matrix created.");
                System.exit(0);
            }

        } else {
            //skip rm chimera OTUs and use input as the final OTUs
            finalOTUsPath = otusPath;
            MyLogger.info("Use given final OTUs " + finalOTUsPath.toString() + " and skip removing chimera OTUs.");
        }

        Path derepUcPath = module.inputValidFile(Paths.get(workDir.getParent().toString(), qcFolder),
                "derep.uc", "dereplication UC file", NameSpace.SUFFIX_UC);
        Path outUpPath = module.inputValidFile(workDir, "out.up", "OTU clustering UP mapping file", NameSpace.SUFFIX_UP);
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
