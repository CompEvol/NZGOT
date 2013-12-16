package nzgo.tookit.core.util;

import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.*;
import nzgo.tookit.core.logger.MyLogger;
import nzgo.tookit.core.sequences.Translation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AminoAcidUtil
 * @author Walter Xie
 */
public class AminoAcidUtil {

    public static final int Translation_Frames = 3; // only forward

    /**
     * get Translation relationship class, such as translation errors
     * @param forwardSequence
     * @param geneticCode
     * @return
     */
    public static Translation getTranslation(Sequence forwardSequence, GeneticCode geneticCode) {
        List<Integer> frames = new ArrayList<>();
        for (int i=0; i<Translation_Frames; i++) {
            // Translation Frame = i + 1
            boolean isTranslatable = isTranslatable(forwardSequence.getStates(), geneticCode, i+1);
            if (isTranslatable) {
                frames.add(i+1);
            }
        }

        Translation translation;
        if (frames.size() == 1) {
            // translatable
            translation = new Translation(forwardSequence, frames.get(0));
        } else if (frames.size() > 1) {
            // multi-frame
            translation = new Translation(forwardSequence, frames);
        } else { // frames.size() < 1
            // no frame
            translation = new Translation(forwardSequence, Translation.Error.NO_FRAME);
        }

        return translation;
    }

    public static List<Translation> getTranslations(List<Sequence> forwardSequences, GeneticCode geneticCode) {
        if (forwardSequences == null)
            throw new IllegalArgumentException("Sequences list cannot be null !");

        List<Translation> translationList = new ArrayList<>();

        // only forward sequences at moment
        for (Sequence forwardSequence : forwardSequences) {
            Translation translation = getTranslation(forwardSequence, geneticCode);
            translationList.add(translation);
        }

        return translationList;
    }

    /**
     * strip List<Sequence> sequences to fit in Frame 1
     * @param forwardSequences
     * @param translationList
     * @param geneticCode
     * @return
     */
    public static List<Sequence> stripSequencesInFrame1(List<Sequence> forwardSequences, List<Translation> translationList, GeneticCode geneticCode) {
        assert forwardSequences.size() == translationList.size();

        List<Sequence> sequencesInFrame1 = new ArrayList<>();

        for (int i=0; i<forwardSequences.size(); i++) {
            Translation translation = translationList.get(i);
            if (translation.isTranslatable()) {
                Sequence sequence = forwardSequences.get(i);

                if (translation.getFrame() == 1) {
                    sequencesInFrame1.add(sequence);
                } else {
                    int offset = translation.getFrame() - 1;
                    State[] states = sequence.getStates();
                    BasicSequence newSeq = new BasicSequence(sequence.getSequenceType(), sequence.getTaxon(), Arrays.copyOfRange(states, offset, states.length));

                    boolean isTranslatable = isTranslatable(newSeq.getStates(), geneticCode, 1);
                    if (!isTranslatable) throw new IllegalArgumentException("Strip sequence from frame " + translation.getFrame() + " to frame 1 becomes not translated !");

                    sequencesInFrame1.add(newSeq);
                }
            }
        }

        return sequencesInFrame1;
    }

    /**
     * strip sequences given in a fasta import file to fit in Frame 1
     * and output to another fasta file
     * @param inFastaFile
     * @param outFastaFile
     * @param geneticCode
     * @return
     * @throws IOException
     * @throws ImportException
     */
    public static int[] stripSequencesInFrame1(File inFastaFile, File outFastaFile, GeneticCode geneticCode) throws IOException, ImportException {
        int[] result = new int[2];

        FastaImporter sequenceImport = new FastaImporter(inFastaFile , SequenceType.NUCLEOTIDE);

        List<Sequence> forwardSequences = sequenceImport.importSequences();
        result[0] = forwardSequences.size();
        List<Translation> translationList = getTranslations(forwardSequences, geneticCode);
        List<Sequence> sequencesInFrame1 = stripSequencesInFrame1(forwardSequences, translationList, geneticCode);
        result[1] = sequencesInFrame1.size();

        //Corrected sequences
        Writer write = new OutputStreamWriter(new FileOutputStream(outFastaFile));
        FastaExporter fe = new FastaExporter(write);
        fe.exportSequences(sequencesInFrame1);
        write.flush();
        write.close();

        return result;
    }

    /**
     * no stop codon in AminoAcidState[]
     * @param nucStates
     * @param geneticCode
     * @param readingFrame
     * @return
     */
    public static boolean isTranslatable(State[] nucStates, GeneticCode geneticCode, int readingFrame) {
        AminoAcidState[] aaStates = Utils.translate(nucStates, geneticCode, readingFrame);

        return !hasStopState(aaStates);
    }

    public static boolean hasStopState(AminoAcidState[] aaStates) {
        for (AminoAcidState aaState : aaStates) {
            if (aaState.isStop())
                return true;
        }
        return false;
    }

    // main
    public static void main(String[] args) throws ImportException, IOException{
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        File inFastaFile = new File(workPath + "NZ-insects-BOLD-2013-11-21-co1-5P.fasta");
        File outFastaFile = new File(workPath + "BOLD-co1-frame1.fasta");

        GeneticCode geneticCode = GeneticCode.INVERTEBRATE_MT;
        MyLogger.info("Genetic Code = " + geneticCode.getName() + ", " + geneticCode.getDescription());

        int[] result = stripSequencesInFrame1(inFastaFile, outFastaFile, geneticCode);

        MyLogger.info("\nTotal " + result[0] + " sequences, " + result[1] + " are translatable.");
    }
}
