package nzgo.toolkit;

import beast.app.util.Arguments;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.pipeline.Module;

import java.nio.file.FileSystems;

/**
 * Main class of NZGOToolkit
 * @author Walter Xie
 */
public class NZGOToolkit extends Module{

    public final static String[] TOOLKIT = new String[]{"NZGOToolkit", "New Zealand Genomic Observatory Toolkit"};

    public NZGOToolkit() {
        super(TOOLKIT[0],TOOLKIT[1]);
    }

    /**
     */
    private NZGOToolkit(String cleanedNewickTree, final String treeFileStem) {
        super();
        // print msg
        MyLogger.info("\nWorking path is " + FileSystems.getDefault());
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<xml>]");
//        System.out.println("  <xml> is a .");
//        System.out.println();
//        System.out.println("  Example: " + getName() + " tree.newick");
//        System.out.println("  Example: " + getName() + " (((A:1.5,B:0.5):1.1,C:3.0);");
//        System.out.println("  Example: " + getName() + " -help");
//        System.out.println();
    }

    // main
    public static void main(String[] args) {
        Module module = new NZGOToolkit();
        throw new UnsupportedOperationException("Not implemented yet !");
//        Arguments.Option[] newOptions = new Arguments.Option[]{
////                        new Arguments.StringOption("in", "input-file-name", "Input file name (*.fasta) including a correct suffix"),
//                new Arguments.StringOption("out", "output-file-name", "Output tree file name (*.nex) including a correct suffix"),
//                new Arguments.StringOption("dirty_input", new String[]{"FastTree", "Geneious"},
//                        false, "The dirty newick tree input from other tools, which contains invalid characters. " +
//                        "This option is not required for a standard newick format."),
//        };
//        final Arguments arguments = module.getArguments(newOptions);
//
//
//
//
//        Path inputFile = module.getInputFile(args, arguments, NameSpace.SUFFIX_NEWICK);
//
//        // output
//        String outFileName = inputFile.getFileName().toString().replace(".fasta", "_translate.fasta");
//        if (arguments.hasOption("out")) {
//            outFileName = arguments.getStringOption("out");
//        }
//
//        Path outFile = module.validateOutputFile(outFileName, NameSpace.SUFFIX_NEX);
//        if (!arguments.hasOption("overwrite") && Files.exists(outFile)) {
//            MyLogger.error("Output file exists, please use \"-overwrite\" to allow overwrite output");
//            System.exit(0);
//        }
//
//        // program parameters
//        GeneticCode geneticCode = GeneticCode.UNIVERSAL;
//        if (arguments.hasOption("genetic_code")) {
//            String code = arguments.getStringOption("genetic_code");
//            geneticCode = GeneticCode.valueOf(code);
//        }
//        final boolean stripSequencesInFrame1 = arguments.hasOption("strip");
//
//        new NZGOToolkit(inputFile.toFile(), outFile.toFile(), geneticCode, stripSequencesInFrame1);
    }
}
