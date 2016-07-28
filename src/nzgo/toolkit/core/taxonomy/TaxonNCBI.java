package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.naming.Separator;

/**
 * Taxon specially for NCBI eFecth
 *
 * @author Walter Xie
 */
public class TaxonNCBI extends Taxon {
    final static protected Separator taxonSeparator = new Separator(";");

    protected int gi;
    protected String lineageString;

    public TaxonNCBI() {
        super();
    }

    public TaxonNCBI(String taxId) {
        super();
        setTaxId(taxId);
    }

    public TaxonNCBI(String scientificName, String taxId) {
        super(scientificName, taxId);
    }

    public TaxonNCBI(String scientificName, String taxId, String parentTaxId) {
        super(scientificName, taxId, parentTaxId);
    }

    public TaxonNCBI(int gi, String taxId) {
        super();
        setGi(gi);
        setTaxId(taxId);
    }

    public int getGi() {
        return gi;
    }

    public void setGi(int gi) {
        this.gi = gi;
    }


    //+++++++++ only work with lineageString ++++++++++

    /**
     * if not include this taxon, then add it in the end
     * @return    the string of taxonomy lineage including this taxon separated by ;
     */
    public String getLineageString() {
        if (lineageString == null)
            throw new IllegalArgumentException("lineageString is null !");
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
        return getLineageString().substring(0, lineageString.lastIndexOf(";"));
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
