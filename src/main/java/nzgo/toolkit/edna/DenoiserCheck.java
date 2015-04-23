package nzgo.toolkit.edna;

import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.State;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.util.StatUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * DenoiserCheck
 * @author Walter Xie
 */
public class DenoiserCheck extends Module {

    public DenoiserCheck() {
        super("DenoiserCheck", "Compare two fasta sequences");
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<input-file-name>]");
        System.out.println("  <input-file-name> is fasta.");
        System.out.println();
        System.out.println("  Example: " + getName() + " -comp 2.fasta");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    //Main method
    public static void main(final String[] args) {
        Module module = new DenoiserCheck();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("comp", "2nd-input-file-name", "The 2nd fasta file.")
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path working = module.init(arguments, args);
        // input
        String inputFileName = module.getFirstArg(arguments);
        Path inputFile = module.getInputFile(working, inputFileName, NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FASTQ);

        // output
        String inputFileName2 = arguments.getStringOption("comp");
        Path inputFile2 = module.getInputFile(working, inputFileName2, NameSpace.SUFFIX_FASTA);

        int totalCorrected = 0;
        try {
            List<Sequence> sequenceList1 = SequenceFileIO.importNucleotideSequences(inputFile);
            List<Sequence> sequenceList2 = SequenceFileIO.importNucleotideSequences(inputFile2);

            System.out.println("File 1 has " + sequenceList1.size() + " sequences, and file 2 has " + sequenceList2.size() + " sequences.");

            for (int i = 0; i < sequenceList2.size(); i++) {
                Sequence sequence2 = sequenceList2.get(i);

                for (int j = 0; j < sequenceList1.size(); j++) {
                    Sequence sequence1 = sequenceList1.get(j);

                    String name1 = sequence1.getTaxon().getName();
                    String name2 = sequence2.getTaxon().getName();

                    // e.g. SRR1720797.5807 5807 length=538
                    if (name1.indexOf(" ") > 0) name1 = name1.substring(0, name1.indexOf(" "));
                    if (name2.indexOf(" ") > 0) name2 = name2.substring(0, name2.indexOf(" "));

                    // JBEL FastaImporter trim label after space
                    if (name1.equalsIgnoreCase(name2)) {
                        int correctedSite = 0;
                        int minLength = StatUtil.min(sequence1.getLength(), sequence2.getLength());
                        for (int s = 0; s < minLength; s++) {
                            State state1 = sequence1.getState(s);
                            State state2 = sequence2.getState(s);

                            if (state1.compareTo(state2) != 0) {
                                correctedSite++;
                            }
                        }

                        if (correctedSite > 0) totalCorrected++;

                        MyLogger.debug(totalCorrected + " sites different between " +
                                sequence1.getTaxon().getName() + " (length=" + sequence1.getLength() + ") and " +
                                sequence2.getTaxon().getName() + " (length=" + sequence2.getLength() + ") in overlap.");
                    }
                }
            }

            MyLogger.info("There are total " + totalCorrected + " sequences corrected.");
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }


    }

}
