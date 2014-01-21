package nzgo.toolkit.labelregex;

import beast.app.util.Arguments;
import beast.evolution.tree.Tree;
import beast.util.TreeParser;
import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.community.util.SampleNameParser;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameParser;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.tree.DirtyTree;
import nzgo.toolkit.core.tree.TreeUtil;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tree Annotation
 * @author Walter Xie
 */
public class TreeRegex extends Module{

    public static final String SEPARATORS_FILE = "separators.tsv";
    public static final String TRAITS_MAPPING_FILE = "traitsMap.tsv";

    public static NameParser nameParser = new SampleNameParser(); //"\\|", "-"

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

        Tree tree = null;
        try {
            tree = new TreeParser(newickTree, false, false, true, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> traits = new TreeMap<>();
        TreeUtil.annotateTree(tree, traits);

        try {
            TreeUtil.writeNexusTree(tree, outFile.toFile());
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
        arguments.printUsage(getName(), "[<newick-tree> or <test-string>]");
        System.out.println("  <newick-tree> is a string or a file (*.newick).");
        System.out.println();
        System.out.println("  Example: " + getName() + " tree.newick");
        System.out.println("  Example: " + getName() + " -out tree.nex (((A:1.5,B:0.5):1.1,C:3.0);");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
        System.out.println("  <test-string> is a test string only applied to -test option.");
        System.out.println("  Example: " + getName() + " -test IDME8NK01ETVXF|DirectSoil|LB1-A");
        System.out.println("  Example: " + getName() + " -customSeparators -test IDME8NK01ETVXF|DirectSoil|LB1-A");
        System.out.println();
    }

    // main
    public static void main(String[] args) {
        Module module = new TreeRegex();

        Arguments.Option[] newOptions = new Arguments.Option[]{
//                        new Arguments.StringOption("in", "input-file-name", "Input file name (*.fasta) including a correct postfix."),
                new Arguments.StringOption("out", "output-file-name", "Output tree file name (*.nex) including a correct postfix."),
                new Arguments.StringOption("dirty_input", DirtyTree.valuesToString(), false,
                        "The dirty newick tree input from other tools, which contains invalid characters. " +
                        "This option is not required for a standard newick format."),

                new Arguments.Option("inputTraitsMap", "use a customized " + TRAITS_MAPPING_FILE + " to load traits, " +
                        "where the 1st column is leaf nodes labels, the 2nd is the mapped trait. " +
                        "If the option \"-outputTraitsMap\" is used together, then create/overwrite " +
                        TRAITS_MAPPING_FILE + " first and load traits from this file."),
                new Arguments.Option("outputTraitsMap", "create/overwrite " + TRAITS_MAPPING_FILE +
                        " for mapping traits to tree nodes, where the 1st column is leaf nodes labels, " +
                        "the 2nd is the mapped trait."),

                new Arguments.Option("customSeparators", "use a customized " + SEPARATORS_FILE + " to load separators, " +
                        "where the 1st column is a regular expression, the 2nd is the index at the string " +
                        "array parsed by the regular expression. Use default separators if no " + SEPARATORS_FILE +
                        ". Use default index 0 if no 2nd column."),
                new Arguments.Option("printSeparators", "print defined separators in sequence, " +
                        "including the index at the array from splitting label"),

                // -test args[0]
                new Arguments.Option("test", "print the trait parsed from given string, using defined primary (1st) separator."),
                new Arguments.Option("testByAll", "print the list of traits in sequence, " +
                        "which parsed from given string by each separator. Return original string if cannot parse."),
        };
        final Arguments arguments = module.getArguments(newOptions);
        String outFileName = "tree" + NameSpace.POSTFIX_NEX;
        String newickTree = null;

        module.init(arguments, args);

        // separators
        if (arguments.hasOption("customSeparators")) {
            Path separatorsTSV = module.validateInputFile(SEPARATORS_FILE, NameSpace.POSTFIX_TSV, "customized separators");
            nameParser = new NameParser(separatorsTSV);
        }

        if (arguments.hasOption("printSeparators")) {
            nameParser.printSeparators();
            System.exit(0);
        } else if (arguments.hasOption("test")) {
            String firstArg = module.getFirstArg(arguments);
            nameParser.getSeparator(0).printItem(firstArg, true);
            System.exit(0);
        } else if (arguments.hasOption("testByAll")) {
            String firstArg = module.getFirstArg(arguments);
            for (Separator separator : nameParser.getSeparators()) {
                separator.printItem(firstArg, true);
            }
            System.exit(0);
        }

        // traits Map
        if (arguments.hasOption("outputTraitsMap")) {
            Path traitsMapTSV = module.validateOutputFile(TRAITS_MAPPING_FILE, NameSpace.POSTFIX_TSV, "traits mapping", true);
            // write(separatorsTSV);
        } else if (arguments.hasOption("inputTraitsMap")) {
            Path traitsMapTSV = module.validateInputFile(TRAITS_MAPPING_FILE, NameSpace.POSTFIX_TSV, "traits mapping");

        }


        // input
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
        Path outFile = module.validateOutputFile(outFileName, NameSpace.POSTFIX_NEX, "output", arguments.hasOption("overwrite"));

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
