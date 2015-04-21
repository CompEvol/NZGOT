package nzgo.toolkit.edna;

import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.State;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;

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
//            assert sequenceList1.size() == sequenceList2.size();

            for (int i = 0; i < sequenceList2.size(); i++) {
                Sequence sequence2 = sequenceList2.get(i);

                for (int j = 0; j < sequenceList1.size(); j++) {
                    Sequence sequence1 = sequenceList1.get(i);

                    if (sequence1.getTaxon().getName().equalsIgnoreCase(sequence2.getTaxon().getName())) {
                        assert sequence1.getLength() == sequence2.getLength();

                        int correctedSite = 0;
                        for (int s = 0; s < sequence1.getLength(); s++) {
                            State state1 = sequence1.getState(s);
                            State state2 = sequence2.getState(s);

                            if (state1.compareTo(state2) != 0) {
                                correctedSite++;
                            }
                        }

                        if (correctedSite > 0) totalCorrected++;
                    }
                }
//                System.out.println(sequence1.getTaxon().getName() + " has " + corrected + " sites corrected.");
            }

            System.out.println("There are total " + totalCorrected + " sequences corrected.");
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }


    }

}
