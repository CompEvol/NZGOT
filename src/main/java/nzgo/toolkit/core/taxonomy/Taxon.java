package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.util.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Taxon
 * TODO move BLAST specific code to another class for generalization?
 * @author Walter Xie
 */
public class Taxon extends Element {

    protected String taxId;
    protected Rank rank;
    protected String parentTaxId;

    protected int gi;

    public List<Taxon> lineage = new ArrayList<>();

    public Taxon() {
        super();
    }

    public Taxon(String scientificName) {
        super(scientificName);
    }

    public Taxon(String scientificName, String taxId) {
        this(scientificName);
        setTaxId(taxId);
    }

    public Taxon(int gi, String taxId) {
        this();
        setGi(gi);
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

    /**
     * get agreed taxon between the lineages of this and taxon2 including themselves
     *
     * @param taxon2
     * @return
     */
    public Taxon getTaxonAgreed(Taxon taxon2) {
        if (taxon2 == null || taxon2.belongsTo(this))
            return this;

        for (Taxon parentTaxon1 : lineage) {
            if (taxon2.belongsTo(parentTaxon1))
                return parentTaxon1;
        }

        return null; // exception
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

    public boolean taxIdEquals(Taxon taxon) {
        return this.getTaxId().equals(taxon.getTaxId());
    }

    public int getGi() {
        return gi;
    }

    public void setGi(int gi) {
        this.gi = gi;
    }

}
