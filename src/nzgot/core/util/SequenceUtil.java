package nzgot.core.util;

import jebl.evolution.sequences.Sequence;

import java.util.List;

/**
 * JEBL Sequence Util
 * @author Thomas Hummel
 * @author Walter Xie
 */
public class SequenceUtil {

    /**
     * get sequence string from a list of sequences given the sequence label
     * @param sequenceLabel Label of query sequence
     * @param sequenceList Sequence list
     * @return amino acid sequence found from the list or null
     */
    public static String getSequenceStringFrom(String sequenceLabel, List<Sequence> sequenceList) {

        String seqName;

        for (Sequence sequence : sequenceList) {
            seqName = sequence.getTaxon().toString();
            if (seqName.contentEquals(sequenceLabel))
                return sequence.getString();
        }
        return null;

    }

}
