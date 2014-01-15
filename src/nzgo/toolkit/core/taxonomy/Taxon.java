package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.util.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Taxon
 * @author Walter Xie
 */
public class Taxon extends Element {

    protected String taxId;
    protected Rank rank;
    protected String parentTaxId;

    public List<Taxon> lineage = new ArrayList<>();

    public Taxon() {
        super();
    }

    public Taxon(String scientificName) {
        super(scientificName);
    }

    public Taxon(String scientificName, String taxId) {
        super(scientificName);
        setTaxId(taxId);
    }

    /**
     * if this taxon belong to given biology classification
     * if give null return true
     * @param bioClassification
     * @return
     */
    public boolean belongsTo (Taxon bioClassification) {
        if (bioClassification == null)
            return true;
        if (getTaxId().equalsIgnoreCase(bioClassification.getTaxId()))
            return true;
        for (Taxon parentTaxon : lineage) {
            if (parentTaxon.getTaxId().equalsIgnoreCase(bioClassification.getTaxId()))
                return true;
        }
        return false;
    }

    /**
     * get taxon from lineage on the given rank
     * if failed, return this taxon
     * @param rank
     * @return
     */
    public Taxon getParentTaxonOn(Rank rank) {
        for (Taxon parentTaxon : lineage) {
            if (rank.equals(parentTaxon.getRank()))
                return parentTaxon;
        }
        if (this.rank.compareTo(rank) >= 0) return this;
        return null;
    }

    public String getScientificName() {
        return getName();
    }

    public void setScientificName(String scientificName) {
        setName(scientificName);
    }

    public String getTaxId() {
        if (taxId == null )
            throw new IllegalArgumentException("Taxon " + this + " requires a unique id ! ");
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getParentTaxId() {
        return parentTaxId;
    }

    public void setParentTaxId(String parentTaxId) {
        this.parentTaxId = parentTaxId;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

}
