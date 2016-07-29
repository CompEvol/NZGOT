package nzgo.toolkit.edna;

import beast.evolution.tree.Tree;
import beast.util.TreeParser;
import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.io.TreeFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.*;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.tree.DirtyTree;
import nzgo.toolkit.core.tree.TreeAnnotation;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Tree Annotation
 * @author Walter Xie
 */
public class TreeRegex extends Module{

    public static NameParser nameParser = new SiteNameParser(); //"\\|", "-"

    public TreeRegex() {
        super("TreeRegex", NZGOToolkit.TOOLKIT[1]);
    }


    private TreeRegex(String newickTree, String dirtyInput, Path outFile, Path traitsMapTSV, int traitsIO) {
        super();

        // if dirtyInput is null, do nothing
        DirtyTree.cleanDirtyTreeOutput(newickTree, dirtyInput);

        Tree tree = null;
        try {
            tree = new TreeParser(newickTree, false, false, true, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TreeAnnotation treeAnnotation = new TreeAnnotation(tree, nameParser);

        try {
            if (traitsIO == 1 || traitsIO == 3)
                treeAnnotation.importPreTaxaTraits(traitsMapTSV);

            if (traitsIO >= 2)
                treeAnnotation.writeTaxaTraits(traitsMapTSV);

            // fire setTaxaTraits() same time
            treeAnnotation.writeNexusTree(outFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<*.newick> or <test-string>]");
        System.out.println("  <newick-tree> is a string or a file (*.newick).");
        System.out.println();
        System.out.println("  Example: " + getName() + " tree.newick");
        System.out.println("  Example: " + getName() + " -out tree.nex (((A:1.5,B:0.5):1.1,C:3.0);");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
        System.out.println("  <test-string> is a test string only applied to -test* option.");
        System.out.println("  Example: " + getName() + " -testSeparator IDME8NK01ETVXF|DirectSoil|LB1-A");
        System.out.println("  Example: " + getName() + " -levelSeparator -testSeparator IDME8NK01ETVXF|DirectSoil|LB1-A");
        System.out.println();
    }

    // main
    public static void main(String[] args) {
        Module module = new TreeRegex();

        Arguments.Option[] newOptions = new Arguments.Option[]{
//                        new Arguments.StringOption("in", "input-file-name", "Input file name (*.fasta) including a correct suffix."),
                new Arguments.StringOption("out", "output-file-name", "Output tree file name (*.nex) including a correct suffix."),
                new Arguments.StringOption("dirty_input", DirtyTree.valuesToString(), false,
                        "The dirty newick tree input from other tools, which contains invalid characters. " +
                        "This option is not required for a standard newick format."),

                new Arguments.Option("traitsMapIn", "use a customized " + NameSpace.TRAITS_MAPPING_FILE + " to load traits, " +
                        "where the 1st column is leaf nodes labels, the 2nd is the mapped trait. " +
                        "The mapped trait is used first, and then regex groups are applied to get traits. " +
                        "If the option \"-traitsMapOut\" is used together, then create/overwrite " +
                        NameSpace.TRAITS_MAPPING_FILE + " first and load traits from this file."),
                new Arguments.Option("traitsMapOut", "create/overwrite " + NameSpace.TRAITS_MAPPING_FILE +
                        " for mapping traits to tree nodes, where the 1st column is leaf nodes labels, " +
                        "the 2nd is the mapped trait."),

                new Arguments.StringOption("regex_type", RegexFactory.RegexType.getRegexTypes(), false, "Two types are available: " +
                        "separator and matcher.\nLevel separators parse names in different naming level. " +
                        "Separators are uploaded from " + NameSpace.SEPARATORS_FILE + ", where the 1st column is a regular expression, " +
                        "the 2nd is the index at the string array parsed by the regular expression. " +
                        "Use default separators if no " + NameSpace.SEPARATORS_FILE + " index = 0 if no 2nd column.\n" +
                        "Regex groups match names into groups. Matchers are uploaded from " + NameSpace.MATCHERS_FILE +
                        ", where the 1st column is a regular expression, the 2nd is a unique name for this group. " +
                        "If no 2nd column, name is the regular expression string without none word characters."),
                new Arguments.Option("printSeparators", "print defined separators in sequence, " +
                        "including the index at the array from splitting label"),

                // -test* args[0]
                new Arguments.Option("testSeparator", "print the trait parsed from given string, using defined primary (1st) separator."),
                new Arguments.Option("testLevelSeparators", "print the list of parsed items in different naming level in sequence. " +
                        "Return the original string/substring if it cannot be parsed."),
                new Arguments.Option("testGroupMatcher", "print the name(s) of the matches of the related regular expression(s). " +
                        "Return \"" + NameParser.OTHER + "\" for the regular expression not matched. " +
                        "This have to use with -groupMatcher together. Note: the valid regular expression groups " +
                        "can have only one match, correct 1st column in " + NameSpace.SEPARATORS_FILE + " if more than one names are printed."),

        };
        final Arguments arguments = module.getArguments(newOptions);
        String outFileName = "tree" + NameSpace.SUFFIX_NEX;
        String newickTree = null;

        Path working = module.init(arguments, args);

        // separators
        RegexFactory.RegexType regexType = null;
        if (arguments.hasOption("regex_type")) {
            regexType = RegexFactory.RegexType.valueOf(arguments.getStringOption("regex_type"));
            Path separatorsTSV = module.inputValidFile(working, NameSpace.SEPARATORS_FILE, "customized separators", NameSpace.SUFFIX_TSV);
            nameParser = new NameParser(separatorsTSV, regexType);
        }

        if (arguments.hasOption("printSeparators")) {
            nameParser.printSeparators();
            System.exit(0);
        } else if (arguments.hasOption("testSeparator")) {
            String firstArg = module.getFirstArg(arguments);
            nameParser.getSeparator(0).print(firstArg, true);
            System.exit(0);
        } else if (arguments.hasOption("testLevelSeparators")) {
            String firstArg = module.getFirstArg(arguments);
            for (Regex regex : nameParser.getRegexList()) {
                ((Separator) regex).print(firstArg, true);
            }
            System.exit(0);
        } else if (arguments.hasOption("testGroupMatcher")) {
            String firstArg = module.getFirstArg(arguments);
            for (Regex regex : nameParser.getRegexList()) {
                ((Matcher) regex).print(firstArg, true);
            }
            System.exit(0);
        }

        // input
        String firstArg = module.getFirstArg(arguments);
        if (firstArg.endsWith(NameSpace.SUFFIX_NEWICK)) {
            Path inputFile = module.getInputFile(working, firstArg, NameSpace.SUFFIX_NEWICK);
            try {
                newickTree = TreeFileIO.importNewickTree(inputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            outFileName = "tree" + NameSpace.SUFFIX_NEX;
        } else if (firstArg.endsWith(";")) {
            newickTree = firstArg;
        }

        // output
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }
        Path outFile = module.validateOutputFile(outFileName, "output", arguments.hasOption("overwrite"), NameSpace.SUFFIX_NEX);

        // program parameters
        String dirtyInput = arguments.getStringOption("dirty_input");

        if (newickTree == null) {
            MyLogger.error("Invalid newick tree string/file : " + firstArg);
            System.exit(0);
        }

        // traits Map
        Path traitsMapTSV = null;
        int traitsIO = 0; // input = 1, output = 2, both = 3
        if (arguments.hasOption("traitsMapOut")) {
            traitsMapTSV = module.validateOutputFile(NameSpace.TRAITS_MAPPING_FILE, "traits mapping", true, NameSpace.SUFFIX_TSV);
            traitsIO += 2;
        } else if (arguments.hasOption("traitsMapIn")) {
            traitsMapTSV = module.inputValidFile(working, NameSpace.TRAITS_MAPPING_FILE, "traits mapping", NameSpace.SUFFIX_TSV);
            traitsIO += 1;
        }

        new TreeRegex(newickTree, dirtyInput, outFile, traitsMapTSV, traitsIO);
    }
}
