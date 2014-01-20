package nzgo.toolkit.tree;

import beast.app.util.Arguments;
import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.util.NameSpace;
import nzgo.toolkit.core.util.TreeUtil;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tree Annotation
 * @author Walter Xie
 */
public class TreeRegex extends Module{

    public TreeRegex() {
        super("TreeRegex", NZGOToolkit.TOOLKIT[1]);
    }

    /**
     */
    private TreeRegex(String newickTree, String dirtyInput, Path outFile) {
        super();

        String workPath = FileSystems.getDefault().toString();
        // print msg
        MyLogger.info("\nWorking path is " + workPath);

        // if dirtyInput is null, do nothing
        DirtyTree.cleanDirtyTreeOutput(newickTree, dirtyInput);



        try {
            TreeUtil.writeNexusTree(newickTree, outFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            TreeUtil.createTaxaBreakAndAnnotateTree(workPath, treeFileStem, newickTree);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<newick-tree>]");
        System.out.println("  <newick-tree> is a string or a file (*.newick).");
        System.out.println();
        System.out.println("  Example: " + getName() + " tree.newick");
        System.out.println("  Example: " + getName() + " -out tree.nex (((A:1.5,B:0.5):1.1,C:3.0);");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    // main
    public static void main(String[] args) {
        Module module = new TreeRegex();

        Arguments.Option[] newOptions = new Arguments.Option[]{
//                        new Arguments.StringOption("in", "input-file-name", "Input file name (*.fasta) including a correct postfix"),
                new Arguments.StringOption("out", "output-file-name", "Output tree file name (*.nex) including a correct postfix"),
                new Arguments.StringOption("dirty_input", DirtyTree.valuesToString(), false,
                        "The dirty newick tree input from other tools, which contains invalid characters. " +
                        "This option is not required for a standard newick format."),
        };
        final Arguments arguments = module.getArguments(newOptions);
        String outFileName = "tree" + NameSpace.POSTFIX_NEX;
        String newickTree = null;

        module.init(arguments, args);
        String firstArg = module.getFirstArg(arguments);
        if (firstArg.endsWith(NameSpace.POSTFIX_NEWICK)) {
            Path inputFile = module.getInputFile(arguments, firstArg, NameSpace.POSTFIX_NEWICK);
            try {
                newickTree = TreeUtil.getNewickTreeFromFile(inputFile.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }

            outFileName = "tree" + NameSpace.POSTFIX_NEX;
        } else if (firstArg.endsWith(";")) {
            newickTree = firstArg;
        }

        // output
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }

        Path outFile = module.validateOutputFile(outFileName, NameSpace.POSTFIX_NEX);
        if (!arguments.hasOption("overwrite") && Files.exists(outFile)) {
            MyLogger.error("Output file exists, please use \"-overwrite\" to allow overwrite output");
            System.exit(0);
        }

        // program parameters
        String dirtyInput = null;
        if (arguments.hasOption("dirty_input")) {
            dirtyInput = arguments.getStringOption("dirty_input");
        }

        if (newickTree == null) {
            MyLogger.error("Invalid newick tree string/file : " + firstArg);
            System.exit(0);
        }
        new TreeRegex(newickTree, dirtyInput, outFile);
    }
}
