package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.util.BioSortedSet;

import java.util.Collection;

/**
 * the set to keep all Taxon
 * it can be just a lineage
 * elementsSet contains Taxon or String or Element
 * @author Walter Xie
 */
public class Taxa<E> extends BioSortedSet<E> {

    public Taxa() { }

    public Taxa(Collection<? extends E> c) {
        super(c);
    }

    public void addTaxa(Taxa<E> newTaxa) {
        this.addAll(newTaxa);
    }
}
