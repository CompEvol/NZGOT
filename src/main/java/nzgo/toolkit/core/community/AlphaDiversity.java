package nzgo.toolkit.core.community;

import jebl.evolution.sequences.Sequence;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.util.ArrayUtil;

import java.util.Arrays;

/**
 * Alpha Diversity for samples given a OTU
 * TODO need review
 * @author Walter Xie
 */
public class AlphaDiversity {

    protected int[] alphaDiversity; // only valid for given sample array

    public AlphaDiversity(SiteNameParser siteNameParser, String[] samples, OTU otu) {
        setAlphaDiversity(siteNameParser, samples, otu);
    }

    public void setAlphaDiversity(SiteNameParser siteNameParser, String[] samples, OTU otu) {
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

            String sampleLocation = siteNameParser.getSite(label);
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
