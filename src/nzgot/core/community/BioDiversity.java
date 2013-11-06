package nzgot.core.community;

import jebl.evolution.sequences.Sequence;
import nzgot.core.community.util.NameParser;
import nzgot.core.util.ArrayUtil;

import java.util.Arrays;

/**
 * BioDiversity
 * default to calculate alpha
 * @author Walter Xie
 */
public class BioDiversity {

    protected int[] alphaDiversity;

    public BioDiversity(int samplesBy, String[] samples, OTU otu) {
        setAlphaDiversity(samplesBy, samples, otu);
    }

    public void setAlphaDiversity(int samplesBy, String[] samples, OTU otu) {
        if (samples == null || samples.length < 1)
            throw new IllegalArgumentException("Error: sample array was not initialized: " + samples);

        alphaDiversity = new int[samples.length];

        for (Object sequence: otu) {
            String label = "";
            if (sequence instanceof Sequence) {
               label = ((Sequence) sequence).getTaxon().getName();
            } else {
               label = sequence.toString();
            }

            String sampleLocation = NameParser.getInstance().getSampleBy(samplesBy, label);
            int i = ArrayUtil.indexOf(sampleLocation, samples);
            if (i < 0) {
                throw new IllegalArgumentException("Error: missing sample location : " + sampleLocation +
                        " from samples array : " + Arrays.asList(samples));
            } else {
                alphaDiversity[i]++;
            }
        }
    }

    public int[] getAlphaDiversity() {
        return alphaDiversity;
    }
}
