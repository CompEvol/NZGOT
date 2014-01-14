package nzgo.toolkit.core.taxonomy;

import java.util.ArrayList;
import java.util.List;

/**
 * Taxon
 * @author Walter Xie
 */
public class Taxon {

    protected String scientificName;
    protected String taxId;
    protected String rank;
    protected List<Taxon> Lineage = new ArrayList<>();

    public Taxon(String scientificName) {
        setScientificName(scientificName);
    }

    public String getScientificName() {
        if (scientificName == null ) setScientificName("Unknown");
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}
