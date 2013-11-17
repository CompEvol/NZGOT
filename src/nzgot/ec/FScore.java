package nzgot.ec;

import jebl.evolution.align.scores.AminoAcidScores;
import jebl.evolution.align.scores.Blosum45;
import jebl.evolution.sequences.AminoAcidState;
import jebl.evolution.sequences.NucleotideState;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.Sequence;


/**
 * Provides scoring for alignment + correction
 *
 * @author Alexei Drummond
 */
public class FScore {

    myGeneticCode geneticCode;

    double indelPenalty;
    double stopCodonPenalty;
    double correctionPenalty;

    AminoAcidScores scores = new Blosum45();

    Sequence read;
    NucleotideState[] readStates;
    
    
    Sequence ref;

    public FScore(myGeneticCode geneticCode,
                  double indelPenalty,
                  double correctionPenalty,
                  double stopCodonPenalty,
                  AminoAcidScores scores,
                  Sequence read,
                  Sequence ref) {

        this.geneticCode = geneticCode;
        this.indelPenalty = indelPenalty;
        this.correctionPenalty = correctionPenalty;
        this.stopCodonPenalty = stopCodonPenalty;
        this.scores = scores;

        this.read = read;
                
        this.ref = ref;
    }

    public FScore(myGeneticCode geneticCode,
                  double indelPenalty,
                  double correctionPenalty,
                  double stopCodonPenalty,
                  AminoAcidScores scores) {

        this(geneticCode, indelPenalty, correctionPenalty, stopCodonPenalty, scores, null, null);
    }


    /**
     * @param i1
     * @param i2
     * @param i3
     * @param states an array of size one of preallocated memory for the standard case
     * @return
     */
    public AminoAcidState[] getAminoAcidStates(int i1, int i2, int i3, AminoAcidState[] states) {

        if (i1 >= 0) {
        	states[0] =geneticCode.getTranslation(
                    (NucleotideState) read.getState(i1),
                    (NucleotideState) read.getState(i2),
                    (NucleotideState) read.getState(i3));
            return states;
        } else if (i2 >= 0) {
            AminoAcidState[] aa = new AminoAcidState[4];
            for (int i = 0; i < 4; i++) {
                aa[i] = geneticCode.getTranslation(
                        Nucleotides.getState(i),
                        (NucleotideState) read.getState(i2),
                        (NucleotideState) read.getState(i3));
            }
            return aa;
        } else if (i3 >= 0) {
            AminoAcidState[] aa = new AminoAcidState[16];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    aa[i * 4 + j] = geneticCode.getTranslation(
                            Nucleotides.getState(i),
                            Nucleotides.getState(j),
                            (NucleotideState) read.getState(i3));
                }
            }
            return aa;
        } else {
            throw new IllegalArgumentException("Must have at least one index of codon >= 0");
        }

    }

    /**
     * the match score for matching the codon represented by nucleotide indices with the j'th amino acid.
     * Indices into each sequence start from one!
     *
     * @param j
     * @return
     */
    public double matchScore(int[] codon, int j) {

        // all indices are origin 1
        int i1 = codon[0] - 1;
        int i2 = codon[1] - 1;
        int i3 = codon[2] - 1;
        j -= 1;

        double score = 0;

        AminoAcidState[] aaState = getAminoAcidStates(i1,i2,i3,aa);

        boolean isStop = true;
        for (AminoAcidState anAaState : aaState) {
            if (!anAaState.isStop()) {
                isStop = false;
            }
        }
        if (isStop) score += stopCodonPenalty;

        String refAA = (ref.getState(j)).getCode();

        double matchScore = 0.0;
        for (AminoAcidState aa : aaState) {
            matchScore += scores.getScore(aa.getCode().charAt(0), refAA.charAt(0));
        }
        matchScore /= (double) aaState.length;

        score += matchScore;

        return score;
    }

    public double matchDelete(int[] codon, int j) {

        return matchScore(codon, j) + correctionPenalty;
    }

    public double matchDuplicate(int[] codon, int j) {
                return matchScore(codon, j) + correctionPenalty;
    }

    private static AminoAcidState[] aa = new AminoAcidState[1];
    // NOT THREAD SAFE -- uses above static memory
    public double insertCodon(int[] codon) {

        double score = 0;

        AminoAcidState[] aaState = getTranslation(codon, aa);

        boolean isStop = true;
        for (AminoAcidState anAaState : aaState) {
            if (!anAaState.isStop()) {
                isStop = false;
            }
        }
        if (isStop) score += stopCodonPenalty;

        score += indelPenalty;

        return score;
    }

    public double insertCodonDuplicate(int[] codon) {
        return insertCodon(codon) + correctionPenalty;
    }

    public double insertCodonDelete(int[] codon) {
        return insertCodon(codon) + correctionPenalty;
    }

    public StringBuilder getCodonString(int[] codon) {

        StringBuilder c = new StringBuilder();

        for (int i : codon) {
            if (i >= 1) {
                c.append(read.getState(i-1));
            } else {
                c.append('-');
            }
        }
        return c;
    }

    public AminoAcidState[] getTranslation(int[] codon, AminoAcidState[] states) {
        return getAminoAcidStates(codon[0]-1, codon[1]-1, codon[2]-1, states);
    }

//    /**
//     * @param i         the length up to the end of this codon DNA in the read
//     * @param delOffset the offset from last nucleotide in the codon to the deleted nucleotide (valid values are -3, -2, -1, 0)
//     * @return
//     */
//    private int[] getCodonDelete(int i, int delOffset) {
//        if (delOffset < -3 || delOffset > 0)
//            throw new IllegalArgumentException("delOffSet was outside valid range (-3 to 0):" + delOffset);
//        int count = 0;
//        int[] codon = new int[3];
//        for (int offset = -3; offset <= 0; offset++) {
//            if (offset != delOffset) {
//                codon[count] = i + offset;
//                count += 1;
//            }
//        }
//        return codon;
//    }
}
