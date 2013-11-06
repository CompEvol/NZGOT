package nzgot.core.community;

import jebl.evolution.sequences.Sequence;
import nzgot.core.community.util.NameParser;
import nzgot.core.util.ArrayUtil;
import nzgot.core.util.BioSortedSet;

import java.util.Arrays;

/**
 * OTU
 * elementsSet contains Reads
 * E could be String or jebl.evolution.sequences.Sequence
 * @author Walter Xie
 */
public class OTU<E> extends BioSortedSet<E> {

    private int[] alphaDiversity;
    protected Reference reference;

    public OTU(String name) {
        super(name);
    }

    public void setAlphaDiversity(int samplesBy, String[] samples) {
        initAlphaDiversity(samples);

        for (E read: this) {
            String label = "";
            if (read instanceof Sequence) {
               label = ((Sequence) read).getTaxon().getName();
            } else {
                label = read.toString();
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

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    private void initAlphaDiversity(String[] samples) {
        if (samples == null || samples.length < 1)
            throw new IllegalArgumentException("Error: sample array was not initialized: " + samples);

        alphaDiversity = new int[samples.length];
    }

}
