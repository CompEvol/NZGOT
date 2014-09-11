package nzgo.toolkit.labelregex;

import beast.evolution.tree.Tree;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.io.TaxonomyFileIO;
import nzgo.toolkit.core.io.TreeFileIO;
import nzgo.toolkit.core.naming.Assembler;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.sequences.SequenceUtil;
import nzgo.toolkit.core.tree.DirtyTree;
import nzgo.toolkit.core.tree.TreeUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Name Assembler
 * @author Walter Xie
 */
public class NameAssembler extends Module{

    public NameAssembler() {
        super("NameAssembler", NZGOToolkit.TOOLKIT[1]);
    }


    private NameAssembler(Path inputFile, Path outFile, Assembler assembler, String dirtyInput) {
        super();

        if (inputFile.endsWith(NameSpace.SUFFIX_NEWICK)) {

            Tree tree = null;
            try {
                tree = TreeFileIO.importNewickTree(inputFile, dirtyInput);
            } catch (Exception e) {
                e.printStackTrace();
            }

            TreeUtil.assembleTreeTaxa(tree, assembler);

            try {
                TreeFileIO.writeNewickTree(outFile, tree);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (inputFile.endsWith(NameSpace.SUFFIX_FASTA)) {

            List<Sequence> sequences = null;
            try {
                sequences = SequenceFileIO.importNucleotideSequences(inputFile);
            } catch (IOException | ImportException e) {
                e.printStackTrace();
            }

            SequenceUtil.assembleSequenceLabels(sequences, assembler);

            try {
                SequenceFileIO.writeToFasta(outFile, sequences);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            throw new UnsupportedOperationException("Do not support input not *.fasta or *.newick yet !");

        }

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
                new Arguments.StringOption("dirty_input", DirtyTree.valuesToString(), false,
                        "The dirty newick tree input from other tools, which contains invalid characters. " +
                                "This option is not required for a standard newick format."),

                new Arguments.StringOption("separator", "regular-expression", "The regular expression to separate a name into items to be assembled."),
                new Arguments.StringOption("matcher", "regular-expression", "The regular expression to select matched names to be proceeded. " +
                        "If no, then proceed all names."),
                new Arguments.StringOption("commands", "commands-string", "The string to define commands to assemble items parsed by separator. " +
                        "Multi-commands can be separated by |, e.g. " + Assembler.CommandType.getExample() +
                        "Note: the item's index may change after each command, so that every commands use the input items indexes."),
                new Arguments.Option("traitsMap", "load traits from file, when the command " + Assembler.CommandType.ADD_ITEM_MAPPED +
                        " is used, where the 1st column is the item separated from the name, the 2nd is the mapped trait. " +
                        "If no this option, then look for the file " + NameSpace.TRAITS_MAPPING_FILE + ""),
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path working = module.init(arguments, args);
        // input
        String inputFileName = module.getFirstArg(arguments);
        Path inputFile = module.getInputFile(working, inputFileName, new String[]{NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_NEWICK});

        // output
        String outFileName = "new-" + inputFile.getFileName().toString();
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }
        Path outFile = module.validateOutputFile(outFileName, null, "output", arguments.hasOption("overwrite"));

        // program parameters
        String dirtyInput = arguments.getStringOption("dirty_input");
        String separatorArg = arguments.getStringOption("separator");
        String matcherArg = arguments.getStringOption("matcher");
        String commandsArg = arguments.getStringOption("commands");

        // traits Map
        Map<String, String> traitsMap = null;
        if (commandsArg.contains(Assembler.CommandType.ADD_ITEM_MAPPED.toString())) {
            String traitsMapTSVName = NameSpace.TRAITS_MAPPING_FILE;
            if (arguments.hasOption("traitsMap")) {
                traitsMapTSVName = arguments.getStringOption("traitsMap");
            }
            Path traitsMapTSV = module.validateInputFile(working, traitsMapTSVName, new String[]{NameSpace.SUFFIX_TSV}, "traits mapping");
            try {
                traitsMap = TaxonomyFileIO.importPreTaxaTraits(traitsMapTSV);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Assembler assembler = new Assembler(separatorArg, matcherArg, commandsArg, traitsMap);

        new NameAssembler(inputFile, outFile, assembler, dirtyInput);
    }
}
