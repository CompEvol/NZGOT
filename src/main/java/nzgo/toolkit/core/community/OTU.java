package nzgo.toolkit.core.community;

import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.util.BioSortedSet;

/**
 * OTU
 * elementsSet contains Reads
 * E could be String or jebl.evolution.sequences.Sequence
 * Assuming 1 sequence only can be assigned to 1 OTU
 * @author Walter Xie
 */
public class OTU<E> extends BioSortedSet<E> {

    protected Reference reference;
    protected AlphaDiversity alphaDiversity; //
    protected Taxon taxonAgreed;

    protected String alias; // special case to have 2 names

    public OTU(String name) {
        super(name);
    }

    public String getAlias() {
        if (alias == null) return getName();
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public AlphaDiversity getAlphaDiversity() {
        return alphaDiversity;
    }

    public void setAlphaDiversity(AlphaDiversity alphaDiversity) {
        this.alphaDiversity = alphaDiversity;
    }

    public boolean hasTaxon() {
        return taxonAgreed != null;
    }

    public Taxon getTaxonAgreed() {
        return taxonAgreed;
    }

    public void setTaxonAgreed(Taxon taxonAgreed) {
        this.taxonAgreed = taxonAgreed;
    }
}
