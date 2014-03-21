package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.util.Element;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Taxon
 * toString() return taxId, if it is null, return name
 * @author Walter Xie
 */
public class Taxon extends Element {

    protected String taxId;
    protected Rank rank;
    protected String parentTaxId;
//    protected Taxon parentTaxon; //TODO Taxon or String?

    public Taxon() {
        super();
    }

    // name
    public Taxon(String scientificName) {
        super(scientificName);
    }

    public Taxon(String scientificName, String taxId) {
        this(scientificName);
        setTaxId(taxId);
    }

    public Taxon(String scientificName, String taxId, String parentTaxId) {
        this(scientificName, taxId);
        setParentTaxId(parentTaxId);
    }

    /**
     * lineage NOT including this Taxon
     * start from "cellular organisms", whose parent is root
     * @return
     */
    public List<Taxon> getLineage () {
        List<Taxon> lineage = new ArrayList<>();
        try {
            getParentLineage(lineage, this);
        } catch (IOException | XMLStreamException e) {
            MyLogger.error("Cannot get taxonomy lineage of " + this);
            e.printStackTrace();
        }
        return lineage;
    }

    protected void getParentLineage(List<Taxon> lineage, Taxon taxon) throws IOException, XMLStreamException {
        if (!TaxonomyUtil.isRoot(taxon.getTaxId()) && taxon.getParentTaxon() != null) {
            getParentLineage(lineage, taxon.getParentTaxon());
            // add "cellular organisms" first, which is the first in xml
            lineage.add(taxon.getParentTaxon());
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
        if (isSameAs(bioClassification))
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
        if (this.rank != null && this.rank.compareTo(rank) >= 0)
            return this;
        return TaxonomyUtil.getNoRankOn(rank);
    }

    /**
     * get agreed taxon between the lineages of this and taxon2 including themselves
     *
     * @param taxon2
     * @return
     */
    public Taxon getTaxonLCA(Taxon taxon2) {
        if (taxon2 == null || taxon2.belongsTo(this))
            return this;

        List<Taxon> lineage = getLineage();
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
        return !(isSameAs(TaxonomyUtil.getUnclassified()) || getScientificName().equalsIgnoreCase(DEFAULT_NAME));
    }

    public String getScientificName() {
        return getName();
    }

    public void setScientificName(String scientificName) {
        setName(scientificName);
    }

    public String getTaxId() {
        if (taxId == null)
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

    public Taxon getParentTaxon() throws IOException, XMLStreamException {
        return (parentTaxId != null) ? TaxonomyPool.getAndAddTaxIdByMemory(parentTaxId) : null;
    }

//    public void setParentTaxon(Taxon parentTaxon) {
//        this.parentTaxon = parentTaxon;
//    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public boolean isSameAs(Taxon taxon) {
        if (taxId != null) {
            return this.getTaxId().contentEquals(taxon.getTaxId());
        } else {
            // TODO
            return this.getScientificName().equalsIgnoreCase(taxon.getScientificName());
        }
    }

    public String toString() {
        if (taxId == null)
            return getName();
        return getTaxId();
    }

}
