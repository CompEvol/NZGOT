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
public class TaxonSet<E> extends BioSortedSet<E> {

    public TaxonSet() { }

    public TaxonSet(Collection<? extends E> c) {
        super(c);
    }

    public boolean containsTaxon(Taxon taxon) {
        return containsTaxon(taxon.toString());
    }

    public E getTaxon(Taxon taxon) {
        return getTaxon(taxon.toString());
    }

    public void addTaxon(E taxon) {
        super.addElement(taxon);
    }

    /**
     * Taxon toString(): if taxId is null, then return name
     * if E has taxId, then taxIdOrName should be taxId,
     * otherwise taxIdOrName should be name if E has null taxId
     * @param taxIdOrName
     * @return
     */
    public boolean containsTaxon(String taxIdOrName) {
        for (E e : this) {
            if (e.toString().contentEquals(taxIdOrName))
                return true;
        }
        return false;
    }

    public E getTaxon(String taxIdOrName) {
        for (E e : this) {
            if (e.toString().contentEquals(taxIdOrName))
                return e;
        }
        return null;
    }

    public void addTaxonSet(TaxonSet<E> newTaxonSet) {
        this.addAll(newTaxonSet);
    }

    public void addTaxonAndLineage(Taxon taxon) {
        List<Taxon> lineage = taxon.getLineage();
        for(int i = lineage.size() - 1; i >= 0; i--){
            this.add((E) lineage.get(i)); //TODO E is String?
        }
    }

}
