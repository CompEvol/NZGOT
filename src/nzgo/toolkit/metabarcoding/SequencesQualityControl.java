package nzgo.toolkit.metabarcoding;

import beast.app.util.Arguments;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.GeneticCode;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.sequences.GeneticCodeUtil;
import nzgo.toolkit.core.util.AminoAcidUtil;
import nzgo.toolkit.core.util.NameSpace;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sequences Quality Control
 * @author Walter Xie
 */
public class SequencesQualityControl {

    public static void printTitle() {


    }

    public static void printUsage(final Arguments arguments) {
        String program = "SequencesQualityControl";

        arguments.printUsage(program, "[<input-file-name>]");
        System.out.println();
        System.out.println("  Example: " + program + " co1.fasta");
        System.out.println("  Example: " + program + " -out co1_translate.fasta -genetic_code invertebrateMitochondrial co1.fasta");
        System.out.println("  Example: " + program + " -help");
        System.out.println();
    }

    // main
    public static void main(String[] args) throws ImportException, IOException{

        final Arguments arguments = new Arguments(
                new Arguments.Option[]{
                        new Arguments.Option("working", "Change working directory (user.dir) to input file's directory"),
                        new Arguments.Option("overwrite", "Allow overwriting of output files"),
//                        new Arguments.Option("options", "Display an options dialog"),
//                        new Arguments.Option("window", "Provide a console window"),
//                        new Arguments.Option("verbose", "Give verbose parsing messages"),

//                        new Arguments.StringOption("in", "input-file-name", "Input file (*.fasta) name including a correct postfix"),
                        new Arguments.StringOption("out", "output-file-name", "Output file (*.fasta) name including a correct postfix"),
                        new Arguments.StringOption("genetic_code", GeneticCodeUtil.getGeneticCodeNames(),
                                false, "A set of standard genetic codes, default to universal standard code"),
                        new Arguments.Option("strip", "strip sequences to fit in Frame 1"),
//                        new Arguments.Option("allow_reverse", "Allow reverse sequences into the program"), //TODO

                        new Arguments.Option("print_genetic_code", "Print available genetic codes"),
                        new Arguments.Option("help", "Print this information and stop"),
                });

        try {
            arguments.parseArguments(args);
        } catch (Arguments.ArgumentException ae) {
            System.out.println();
            System.out.println(ae.getMessage());
            System.out.println();
            printUsage(arguments);
            System.exit(1);
        }

        if (arguments.hasOption("help")) {
            printUsage(arguments);
            System.exit(0);
        } else if (arguments.hasOption("print_genetic_code")) {
            GeneticCodeUtil.printGeneticCodes();
            System.exit(0);
        }

        printTitle();

        String inputFileName = null;

        // check args[]
        final String[] args2 = arguments.getLeftoverArguments();
        if (args2.length > 1) {
            MyLogger.error("Unknown option: " + args2[1]);
            printUsage(arguments);
            System.exit(0);
        } else if (args2.length > 0) {
            inputFileName = args2[0];
        }

        if (inputFileName == null || !inputFileName.endsWith(NameSpace.POSTFIX_SEQUENCES)) {
            MyLogger.error("Invalid input file name : " + inputFileName + ", which *.fasta is required");
            System.exit(0);
        }

        // input
        Path inputFile = Paths.get(inputFileName);
        if (inputFile == null || Files.notExists(inputFile)) {
            MyLogger.error("Cannot find input file : " + inputFileName);
            System.exit(0);
        }

        // set working directory
        if (inputFile.toFile().getParent() != null && arguments.hasOption("working")) {
            System.setProperty("user.dir", inputFile.toFile().getParentFile().getAbsolutePath());
        }

        // output
        String outFileName = inputFileName.replace(".fasta", "_translate.fasta");
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }
        if (outFileName == null || !outFileName.endsWith(NameSpace.POSTFIX_SEQUENCES)) {
            MyLogger.error("Invalid output file name : " + outFileName + ", which *.fasta is required");
            System.exit(0);
        }

        Path outFile = FileSystems.getDefault().getPath(".", outFileName);
        if (!arguments.hasOption("overwrite") && Files.exists(outFile)) {
            MyLogger.error("Output file exists, please use \"-overwrite\" to allow overwrite output");
            System.exit(0);
        }

        // program parameters
        GeneticCode geneticCode = GeneticCode.UNIVERSAL;
        if (arguments.hasOption("genetic_code")) {
            String code = arguments.getStringOption("genetic_code");
            geneticCode = GeneticCode.valueOf(code);
        }
        final boolean stripSequencesInFrame1 = arguments.hasOption("strip");

        // print msg
        MyLogger.info("\nWorking path is " + FileSystems.getDefault());
        MyLogger.info("Input file is " + inputFile.getFileName());
        MyLogger.info("Output file is " + outFile.getFileName());
        MyLogger.info("Genetic code set to " + geneticCode.getName() + ", " + geneticCode.getDescription());
        if (stripSequencesInFrame1)
            MyLogger.info("Strip sequences to fit in Frame 1 ");

        int[] result = AminoAcidUtil.writeTranslatableSequences(inputFile.toFile(), outFile.toFile(), geneticCode, stripSequencesInFrame1);

        MyLogger.info("\nTotal " + result[0] + " sequences, " + result[1] + " are translatable.");

    }
}
