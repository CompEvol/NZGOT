package nzgot.core.util;

import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AminoAcidUtil
 * @author Walter Xie
 */
public class AminoAcidUtil {

    public static final int Translation_Frames = 3; // only forward

    public static Boolean[][] getTranslationFrameTable(List<Sequence> forwardSequences, GeneticCode geneticCode) {
        if (forwardSequences == null)
            throw new IllegalArgumentException("Sequences list cannot be null !");

        // only forward sequences at moment
        // last row is summary to the frame suits for most sequences
        Boolean[][] translationFrameTable = new Boolean[forwardSequences.size() + 1][Translation_Frames];
        int[] total = new int[Translation_Frames];

        for (int i=0; i<forwardSequences.size(); i++) {
            for (int j=0; j<Translation_Frames; j++) {
                // Translation Frame = j + 1
                boolean isTranslatable = isTranslatable(forwardSequences.get(i).getStates(), geneticCode, j+1);
                if (isTranslatable) {
                    translationFrameTable[i][j] = true;
                    total[j]++;
                }
            }
        }

        int maxID = ArrayUtil.indexOfMax(total);
        translationFrameTable[forwardSequences.size()][maxID] = true;

        return translationFrameTable;
    }

    public static List<Sequence> stripSequencesIntoSameFrame(List<Sequence> forwardSequences, Boolean[][] translationFrameTable) {
        // last row is summary, and Translation Frame = index + 1
        int defaultFrame = ArrayUtil.indexOf(true, translationFrameTable[translationFrameTable.length-1]) + 1;

        List<Sequence> badSequences = new ArrayList<>();

        int i = 0;
        for (Sequence forwardSeq : forwardSequences) {
            if (!translationFrameTable[i][defaultFrame-1]) {
                int frame = ArrayUtil.indexOf(true, translationFrameTable[i]) + 1;

                if (frame < 1 || frame > 3) {
                    badSequences.add(forwardSeq);
                    forwardSequences.remove(i);

                    System.out.println("Warning: remove bad sequence " + forwardSeq.getTaxon());
                } else {

                    stripSequences(forwardSequences.get(i), frame);

                }
            }
            i++;
        }

        return badSequences;
    }

    public static void stripSequences(Sequence sequence, int frame) {


    }

    public void outputSequencesInSameFrame(File inFastaFile, File outFastaFile, GeneticCode geneticCode) throws IOException, ImportException {
        FastaImporter sequenceImport = new FastaImporter(inFastaFile , SequenceType.NUCLEOTIDE);

        List<Sequence> forwardSequences = sequenceImport.importSequences();

        Boolean[][] translationFrameTable = getTranslationFrameTable(forwardSequences, geneticCode);




        //Corrected sequences
        Writer write = new OutputStreamWriter(new FileOutputStream(outFastaFile));
        FastaExporter fe = new FastaExporter(write);
        fe.exportSequences(forwardSequences);
        write.flush();
        write.close();
    }

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
        System.out.println("\nWorking path = " + workPath);

        File inFastaFile = new File(workPath + "all97.fasta");
        File outFastaFile = new File(workPath + "all97_same_frame.fasta");

        FastaImporter sequenceImport = new FastaImporter(inFastaFile , SequenceType.NUCLEOTIDE);

        List<Sequence> forwardSequences = sequenceImport.importSequences();

        GeneticCode geneticCode = GeneticCode.INVERTEBRATE_MT;
        System.out.println("Genetic Code = " + geneticCode.getName() + ", " + geneticCode.getDescription());

        Boolean[][] translationFrameTable = getTranslationFrameTable(forwardSequences, geneticCode);




        //Corrected sequences
        Writer write = new OutputStreamWriter(new FileOutputStream(outFastaFile));
        FastaExporter fe = new FastaExporter(write);
        fe.exportSequences(forwardSequences);
        write.flush();
        write.close();
    }
}
