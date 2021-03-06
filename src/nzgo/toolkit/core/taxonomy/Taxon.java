package nzgo.toolkit.core.taxonomy;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Taxon
 * toString() return taxId, if it is null, return name
 * @author Walter Xie
 */
public class Taxon extends TaxonNoID {

    protected String taxId;
    protected String parentTaxId;
//    protected Taxon parentTaxon; //TODO Taxon or String?
    protected String lineageString;

    public Taxon() {
        super();
    }

    public Taxon(String scientificName, String taxId) {
        super(scientificName);
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
    public List<Taxon> getLineage() {
        List<Taxon> lineage = new ArrayList<>();
        getParentLineage(lineage, this);
        return lineage;
    }

    protected void getParentLineage(List<Taxon> lineage, Taxon taxon) {
        if (!TaxonomyUtil.isRoot(taxon.getTaxId()) && taxon.getParentTaxon() != null) {
            getParentLineage(lineage, taxon.getParentTaxon());
            // add "cellular organisms" first, which is the first in xml
            lineage.add(taxon.getParentTaxon());
        }
    }

    /**
     * if this taxon belong to given taxonomic category
     * if give null return true
     * @param taxonCategory
     * @return
     */
    public boolean belongsTo (Taxon... taxonCategory) {
        if (taxonCategory == null)
            return true;
        if (isSameIn(taxonCategory))
            return true;

        List<Taxon> lineage = getLineage();
        for(int i = lineage.size() - 1; i >= 0; i--){
            if (lineage.get(i).isSameIn(taxonCategory))
                return true;
        }
        return false;
    }

    public boolean isSameIn(Taxon... taxon) {
        for (Taxon t: taxon) {
            if (this.getTaxId().contentEquals(t.getTaxId()))
                return true;
        }
        return false;
    }

    public boolean isSameId(String taxId) {
        return this.getTaxId().contentEquals(taxId);
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
     * get agreed taxon between the lineages of this and taxon2 including themselves,
     * always take the lowest common ancestor
     * @param taxon2
     * @return
     */
    public Taxon getTaxonLCA(Taxon taxon2) {
        if (taxon2 == null)
            throw new IllegalArgumentException("Cannot take null taxon as input to take LCA !");
        if (taxon2.belongsTo(this)) // taxon2 is lower than this
            return this;

        List<Taxon> lineage = getLineage();
        for(int i = lineage.size() - 1; i >= 0; i--){
            if (taxon2.belongsTo(lineage.get(i)))
                return lineage.get(i);
        }

        return null; // exception
    }

    /**
     * take the lowest taxon if in the same lineage, otherwise take LCA if contradict
     * @param taxon2
     * @return
     */
    public Taxon getTaxonLowLinLCA(Taxon taxon2) {
        if (taxon2 == null)
            throw new IllegalArgumentException("Cannot take null taxon as input to take LCA !");
        if (taxon2.belongsTo(this))
            return taxon2;
        if (this.belongsTo(taxon2))
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

    public Taxon getParentTaxon() {
        try {
            return (parentTaxId != null) ? TaxonomyPool.getAndAddTaxIdByMemory(parentTaxId) : null;
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public void setParentTaxon(Taxon parentTaxon) {
//        this.parentTaxon = parentTaxon;
//    }

    public String toString() {
        return getTaxId();
    }


    //+++++++++ only work with lineageString ++++++++++

    /**
     * if not include this taxon, then add it in the end
     * @return    the string of taxonomy lineage including this taxon separated by ;
     */
    public String getLineageString() {
        if (!lineageString.contains(getScientificName())) {
            String includeThisTaxon = lineageString + ";" + getScientificName();
            setLineageString(includeThisTaxon);
        }
        return lineageString;
    }

    /**
     *
     * @return   the string of parent taxonomy lineage excluding this taxon separated by ;
     */
    public String getParentLineageString() {
        if (lineageString.contains(getScientificName())) {
            return lineageString.substring(0, lineageString.lastIndexOf(";"));
        }
        return lineageString;
    }

    /**
     * set the string of taxonomy lineage separated by ;
     * @param lineageString
     */
    public void setLineageString(String lineageString) {
        this.lineageString = lineageString.replaceAll("; ", ";");
    }

    /**
     * if this taxon belong to given taxonomic category
     * @param taxonCategory
     * @return
     */
    public boolean belongsTo(String... taxonCategory) {
        for (String taxCate: taxonCategory) {
            if (lineageString.contains(taxCate))
                return true;
        }
        return false;
    }

}
