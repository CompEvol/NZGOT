package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.util.BioSortedSet;

import java.util.Collection;

/**
 * the set to keep all Taxon
 * elementsSet contains Taxon or String or Element
 * @author Walter Xie
 */
public class Taxa<E> extends BioSortedSet<E> {

    public Taxa() { }

    public Taxa(Collection<? extends E> c) {
        super(c);
    }

}
