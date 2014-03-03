package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.util.BioSortedSet;

import java.util.Collection;
import java.util.List;

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

    public void addTaxonAndLineage(Taxon taxon) {
        List<Taxon> lineage = taxon.getLineage();
        for(int i = lineage.size() - 1; i >= 0; i--){
            this.add((E) lineage.get(i)); //TODO E is String?
        }
    }

}
