package nzgo.toolkit.metabarcoding;

import beast.app.util.Arguments;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.GeneticCode;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.sequences.GeneticCodeUtil;
import nzgo.toolkit.core.util.AminoAcidUtil;
import nzgo.toolkit.core.util.NameSpace;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sequences Quality Control
 * @author Walter Xie
 */
public class SequencesQualityControl extends Module{

    public SequencesQualityControl() {
        super("SequencesQualityControl");
    }

    /**
     * Model constructor
     * @param inputFile
     * @param outFile
     * @param geneticCode
     * @param stripSequencesInFrame1
     */
    private SequencesQualityControl(File inputFile, File outFile, GeneticCode geneticCode, boolean stripSequencesInFrame1) {
        super();
        // print msg
        MyLogger.info("\nWorking path is " + FileSystems.getDefault());
        MyLogger.info("Input file is " + inputFile);
        MyLogger.info("Output file is " + outFile);
        MyLogger.info("Genetic code set to " + geneticCode.getName() + ", " + geneticCode.getDescription());
        if (stripSequencesInFrame1)
            MyLogger.info("Strip sequences to fit in Frame 1 ");

        int[] result = new int[2];
        try {
            result = AminoAcidUtil.writeTranslatableSequences(inputFile, outFile, geneticCode, stripSequencesInFrame1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImportException e) {
            e.printStackTrace();
        }

        MyLogger.info("\nTotal " + result[0] + " sequences, " + result[1] + " are translatable.");
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<input-file-name>]");
        System.out.println();
        System.out.println("  Example: " + getName() + " co1.fasta");
        System.out.println("  Example: " + getName() + " -out co1_translate.fasta -genetic_code invertebrateMitochondrial co1.fasta");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    // main
    public static void main(String[] args) {
        Module module = new SequencesQualityControl();

        Arguments.Option[] newOptions = new Arguments.Option[]{
//                        new Arguments.StringOption("in", "input-file-name", "Input file (*.fasta) name including a correct postfix"),
                new Arguments.StringOption("out", "output-file-name", "Output file (*.fasta) name including a correct postfix"),
                new Arguments.StringOption("genetic_code", GeneticCodeUtil.getGeneticCodeNames(),
                        false, "A set of standard genetic codes, default to universal standard code"),
                new Arguments.Option("strip", "strip sequences to fit in Frame 1"),
//                        new Arguments.Option("allow_reverse", "Allow reverse sequences into the program"), //TODO

                new Arguments.Option("print_genetic_code", "Print available genetic codes"),
        };
        final Arguments arguments = module.getArguments(newOptions);

        if (arguments.hasOption("print_genetic_code")) {
            GeneticCodeUtil.printGeneticCodes();
            System.exit(0);
        }

        Path inputFile = module.getInputFile(args, arguments, NameSpace.POSTFIX_SEQUENCES);

        // output
        String outFileName = inputFile.getFileName().toString().replace(".fasta", "_translate.fasta");
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }

        Path outFile = module.validateOutputFile(outFileName, NameSpace.POSTFIX_SEQUENCES);
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

        new SequencesQualityControl(inputFile.toFile(), outFile.toFile(), geneticCode, stripSequencesInFrame1);
    }
}
