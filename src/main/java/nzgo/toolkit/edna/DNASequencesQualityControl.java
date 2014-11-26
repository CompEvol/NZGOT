package nzgo.toolkit.edna;

import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.GeneticCode;
import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.sequences.AminoAcidUtil;
import nzgo.toolkit.core.sequences.GeneticCodeUtil;

import java.io.IOException;
import java.nio.file.Path;

/**
 * DNA Sequences Quality Control
 * @author Walter Xie
 */
public class DNASequencesQualityControl extends Module{

    public DNASequencesQualityControl() {
        super("DNASequencesQualityControl", NZGOToolkit.TOOLKIT[1]);
    }

    /**
     * Model constructor
     * @param inputFile
     * @param outFile
     * @param geneticCode
     * @param stripSequencesInFrame1
     */
    private DNASequencesQualityControl(Path inputFile, Path outFile, GeneticCode geneticCode, boolean stripSequencesInFrame1) {
        super();
        // print msg
//        MyLogger.info("\nWorking path is " + FileSystems.getDefault());
        MyLogger.info("Input file is " + inputFile);
        MyLogger.info("Output file is " + outFile);
        MyLogger.info("Genetic code set to " + geneticCode.getName() + ", " + geneticCode.getDescription());
        if (stripSequencesInFrame1)
            MyLogger.info("Strip sequences to fit in Frame 1 ");

        int[] result = new int[2];
        try {
            result = AminoAcidUtil.writeTranslatableSequences(inputFile.toFile(), outFile.toFile(), geneticCode, stripSequencesInFrame1);
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }

        MyLogger.info("\nInput has " + result[0] + " sequences in total, where " + result[1] + " are translatable.");
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<*.fasta>]");
        System.out.println();
        System.out.println("  Example: " + getName() + " co1.fasta");
        System.out.println("  Example: " + getName() + " -out co1_translate.fasta -genetic_code invertebrateMitochondrial co1.fasta");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    // main
    public static void main(String[] args) {
        Module module = new DNASequencesQualityControl();

        Arguments.Option[] newOptions = new Arguments.Option[]{
//                        new Arguments.StringOption("in", "input-file-name", "Input file name (*.fasta) including a correct suffix."),
                new Arguments.StringOption("out", "output-file-name", "Output file name (*.fasta) including a correct suffix."),
                new Arguments.StringOption("genetic_code", GeneticCodeUtil.getGeneticCodeNames(),
                        false, "A set of standard genetic codes, default to universal standard code."),
                new Arguments.Option("strip", "strip sequences to fit in Frame 1."),
//                        new Arguments.Option("allow_reverse", "Allow reverse sequences into the program."), //TODO

                new Arguments.Option("print_genetic_code", "Print available genetic codes."),
        };
        final Arguments arguments = module.getArguments(newOptions);

        if (arguments.hasOption("print_genetic_code")) {
            GeneticCodeUtil.printGeneticCodes();
            System.exit(0);
        }

        Path working = module.init(arguments, args);
        // input
        String inputFileName = module.getFirstArg(arguments);
        Path inputFile = module.getInputFile(working, inputFileName, NameSpace.SUFFIX_FASTA);

        // output
        String outFileName = inputFile.getFileName().toString().replace(NameSpace.SUFFIX_FASTA, "_translate" + NameSpace.SUFFIX_FASTA);
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }
        Path outFile = module.validateOutputFile(outFileName, "output", arguments.hasOption("overwrite"), NameSpace.SUFFIX_FASTA);

        // program parameters
        GeneticCode geneticCode = GeneticCode.UNIVERSAL;
        if (arguments.hasOption("genetic_code")) {
            String code = arguments.getStringOption("genetic_code");
            geneticCode = GeneticCode.valueOf(code);
        }
        final boolean stripSequencesInFrame1 = arguments.hasOption("strip");

        new DNASequencesQualityControl(inputFile, outFile, geneticCode, stripSequencesInFrame1);
    }
}
