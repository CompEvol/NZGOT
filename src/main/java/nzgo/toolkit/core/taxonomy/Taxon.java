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
    protected Taxon parentTaxon;

    protected int gi;

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
     * lineage NOT including this Taxon
     * start from "cellular organisms", whose parent is root
     * @return
     */
    public List<Taxon> getLineage () {
        List<Taxon> lineage = new ArrayList<>();
        getParentLineage(lineage, this);
        return lineage;
    }

    protected void getParentLineage(List<Taxon> lineage, Taxon taxon) {
        if (!TaxaUtil.getCellularOrganisms().taxIdEquals(taxon) && taxon.getParentTaxon() != null) {
            lineage.add(taxon.getParentTaxon());
            getParentLineage(lineage, taxon.getParentTaxon());
        }
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
        if (taxIdEquals(bioClassification))
            return true;

        List<Taxon> lineage = getLineage();
        for(int i = lineage.size() - 1; i >= 0; i--){
            if (lineage.get(i).getTaxId().equalsIgnoreCase(bioClassification.getTaxId()))
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
        List<Taxon> lineage = getLineage();
        for(int i = lineage.size() - 1; i >= 0; i--){
            if (rank.equals(lineage.get(i).getRank()))
                return lineage.get(i);
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

        List<Taxon> lineage = getLineage();
        for(int i = lineage.size() - 1; i >= 0; i--){
            if (taxon2.belongsTo(lineage.get(i)))
                return lineage.get(i);
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

    public Taxon getParentTaxon() {
        return parentTaxon;
    }

    public void setParentTaxon(Taxon parentTaxon) {
        this.parentTaxon = parentTaxon;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public boolean taxIdEquals(Taxon taxon) {
        return this.getTaxId().contentEquals(taxon.getTaxId());
    }

    public int getGi() {
        return gi;
    }

    public void setGi(int gi) {
        this.gi = gi;
    }

}
