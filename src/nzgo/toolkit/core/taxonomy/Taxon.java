package nzgo.toolkit.core.taxonomy;

import java.util.ArrayList;
import java.util.List;

/**
 * Taxon
 * @author Walter Xie
 */
public class Taxon implements Comparable {

    protected String scientificName;
    protected String taxId;
    protected String rank;
    protected String parentTaxId;

    public List<Taxon> lineage = new ArrayList<>();

    public Taxon() {  }

    public Taxon(String scientificName) {
        setScientificName(scientificName);
    }

    public Taxon getParentTaxonOn(String rank) {
        for (Taxon parentTaxon : lineage) {
            if (rank.equalsIgnoreCase(parentTaxon.getRank()))
                return parentTaxon;
        }
        return null;
    }

    public String getScientificName() {
        if (scientificName == null ) setScientificName("Unknown");
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getTaxId() {
        if (taxId == null )
            throw new IllegalArgumentException("Taxon require a unique id ! ");
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

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return getScientificName(); // getTaxId();
    }

    @Override
    public int compareTo(Object o) {
        return scientificName.compareTo(o.toString());
    }
}
