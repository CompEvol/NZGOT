package nzgot.cma;

import nzgot.core.util.BioObject;

/**
 * Sample Location = Plot
 * elementsSet contains subplot being sampled
 * @author Walter Xie
 */
public class SampleLocation extends BioObject {

    protected double latitude;
    protected double longitude;
    protected double elevation;

    public SampleLocation(String name) {
        super(name);
    }
}
