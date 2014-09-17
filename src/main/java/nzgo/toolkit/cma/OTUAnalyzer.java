package nzgo.toolkit.cma;

import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.MixedOTUs;

import java.io.IOException;
import java.nio.file.Path;

/**
 * OTU Analyzer
 * @author Walter Xie
 */
public class OTUAnalyzer extends Module{

    public static final String UC_FILE = "clusters" + NameSpace.SUFFIX_UC;
    public static final String MIXED_OTUS_FILE = "mixed_otus" + NameSpace.SUFFIX_TSV;

    public OTUAnalyzer() {
        super("OTUAnalyzer", NZGOToolkit.TOOLKIT[1]);
    }

    public OTUAnalyzer(Path ucFile, Path outFile, String regex) {

        MixedOTUs mixedOTUs = new MixedOTUs(ucFile, regex);
        try {
            mixedOTUs.writeMixedOTUs(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<regular-expression>]");
        System.out.println("  <regular-expression> is used to identify whether " +
                "a sequence in a OTU is different to the head sequence of that OTU.");
        System.out.println();
        System.out.println("  Example: " + getName() + " .*NZAC.*");
        System.out.println("  Example: " + getName() + " -in clusters.uc .*NZAC.*");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    //Main method
    public static void main(final String[] args) throws IOException {
        Module module = new OTUAnalyzer();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("in", "input-file-name", "Input uc file name (*.uc), " +
                        "if this option is not selected, then look for the file " + UC_FILE + ""),
                new Arguments.StringOption("out", "output-file-name", "Output file name (*.tsv), " +
                        "if this option is not selected, then use " + MIXED_OTUS_FILE + ""),
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path working = module.init(arguments, args);
        // regex
        String regex = module.getFirstArg(arguments);

        // input
        String inputFileName = UC_FILE;
        if (arguments.hasOption("in")) {
            inputFileName = arguments.getStringOption("in");
        }
        Path inputFile = module.getInputFile(working, inputFileName, new String[]{NameSpace.SUFFIX_UC});

        // output
        String outFileName = MIXED_OTUS_FILE;
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }
        Path outFile = module.validateOutputFile(outFileName, new String[]{NameSpace.SUFFIX_TSV}, "output", arguments.hasOption("overwrite"));

        new OTUAnalyzer(inputFile, outFile, regex);
    }

}
