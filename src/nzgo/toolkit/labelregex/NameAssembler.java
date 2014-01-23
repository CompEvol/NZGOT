package nzgo.toolkit.labelregex;

import beast.app.util.Arguments;
import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;

import java.nio.file.Path;

/**
 * Name Magic
 * @author Walter Xie
 */
public class NameAssembler extends Module{

    public NameAssembler() {
        super("NameAssembler", NZGOToolkit.TOOLKIT[1]);
    }


    private NameAssembler(Path inputFile, Path outFile) {
        super();


    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<*.fasta> or <*.newick>]");
        System.out.println();
        System.out.println("  Example: " + getName() + " co1.fasta");
        System.out.println("  Example: " + getName() + " tree.newick");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    // main
    public static void main(String[] args) {
        Module module = new NameAssembler();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("out", "output-file-name", "Output file name and its suffix is same as input file."),

                new Arguments.StringOption("separator", "regular-expression", "The regular expression to separate a name into items to be assembled."),
                new Arguments.StringOption("regex", "regular-expression", "The regular expression to find matched names to be proceeded."),
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path working = module.init(arguments, args);
        // input
        String inputFileName = module.getFirstArg(arguments);
        Path inputFile = module.getInputFile(working, inputFileName, new String[]{NameSpace.SUFFIX_SEQUENCES, NameSpace.SUFFIX_NEWICK});

        // output
        String outFileName = "new-" + inputFile.getFileName().toString();
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }
        Path outFile = module.validateOutputFile(outFileName, null, "output", arguments.hasOption("overwrite"));

        // program parameters

        new NameAssembler(inputFile, outFile);
    }
}
