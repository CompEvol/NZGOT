package nzgo.toolkit.core.community;

import nzgo.toolkit.core.util.BioSortedSet;

/**
 * Sample Location = Plot
 * elementsSet contains subplot being sampled
 * @author Walter Xie
 */
public class SampleLocation<E> extends BioSortedSet<E> {

    protected double latitude;
    protected double longitude;
    protected double elevation;

    public SampleLocation(String name) {
        super(name);
    }
}
