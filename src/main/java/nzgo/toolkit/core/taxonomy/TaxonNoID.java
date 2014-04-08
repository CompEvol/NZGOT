package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.util.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * TaxonNoID, not recommend to use
 * @author Walter Xie
 */
public class TaxonNoID extends Element {

    protected Rank rank;
    // if root, then parentTaxon is null
    protected TaxonNoID parentTaxon; //TODO Taxon or String?

    public TaxonNoID() {
        super();
    }

    // name
    public TaxonNoID(String scientificName) {
        super(scientificName);
    }

    public TaxonNoID(String scientificName, TaxonNoID parentTaxon) {
        this(scientificName);
        setParentTaxon(parentTaxon);
    }

    /**
     * lineage NOT including this Taxon
     * start from root, whose parent is null
     * @return
     */
    public List<TaxonNoID> getLineageNoID () {
        List<TaxonNoID> lineage = new ArrayList<>();
        getParentLineage(lineage, this);
        return lineage;
    }

    protected void getParentLineage(List<TaxonNoID> lineage, TaxonNoID taxon) {
        if (taxon.getParentTaxon() != null) {
            getParentLineage(lineage, taxon.getParentTaxon());
            lineage.add(taxon.getParentTaxon());
        }
    }

    /**
     * if this taxon belong to given biology classification
     * if give null return true
     * @param bioClassification
     * @return
     */
    public boolean belongsTo (TaxonNoID bioClassification) {
        if (bioClassification == null)
            return true;
        if (isSameAs(bioClassification))
            return true;

        List<TaxonNoID> lineage = getLineageNoID();
        for(int i = lineage.size() - 1; i >= 0; i--){
            if (lineage.get(i).isSameAs(bioClassification))
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
    public TaxonNoID getParentTaxonOn(Rank rank) {
        List<TaxonNoID> lineage = getLineageNoID();
        for(int i = lineage.size() - 1; i >= 0; i--){
            if (rank.equals(lineage.get(i).getRank()))
                return lineage.get(i);
        }
        if (this.rank != null && this.rank.compareTo(rank) >= 0)
            return this;
        return new TaxonNoID("no " + rank.toString());
    }

    /**
     * get agreed taxon between the lineages of this and taxon2 including themselves
     *
     * @param taxon2
     * @return
     */
    public TaxonNoID getTaxonLCA(TaxonNoID taxon2) {
        if (taxon2 == null || taxon2.belongsTo(this))
            return this;

        List<TaxonNoID> lineage = getLineageNoID();
        for(int i = lineage.size() - 1; i >= 0; i--){
            if (taxon2.belongsTo(lineage.get(i)))
                return lineage.get(i);
        }

        return null; // exception
    }

    /**
     * taxonomy is classified
     * @return
     */
    public boolean isClassified() {
        return !getScientificName().equalsIgnoreCase(DEFAULT_NAME);
    }

    public String getScientificName() {
        return getName();
    }

    public void setScientificName(String scientificName) {
        setName(scientificName);
    }

    public TaxonNoID getParentTaxon() {
        return this.parentTaxon;
    }

    public void setParentTaxon(TaxonNoID parentTaxon) {
        this.parentTaxon = parentTaxon;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public boolean isSameAs(TaxonNoID taxon) {
        return this.getScientificName().equalsIgnoreCase(taxon.getScientificName());
    }

    public String toString() {
        return getName();
    }

}
