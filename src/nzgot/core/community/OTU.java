package nzgot.core.community;

import nzgot.core.util.BioSortedSet;

/**
 * OTU
 * elementsSet contains Reads
 * E could be String or jebl.evolution.sequences.Sequence
 * Assuming 1 sequence only can be assigned to 1 OTU
 * @author Walter Xie
 */
public class OTU<E> extends BioSortedSet<E> {

    protected Reference reference;
    protected BioDiversity bioDiversity;

    public OTU(String name) {
        super(name);
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public BioDiversity getBioDiversity() {
        return bioDiversity;
    }

    public void setBioDiversity(BioDiversity bioDiversity) {
        this.bioDiversity = bioDiversity;
    }
}
