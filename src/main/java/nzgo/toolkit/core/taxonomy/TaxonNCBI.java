package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.naming.Separator;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

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

    /**
     * get agreed taxon between the lineages of this and taxon2 including themselves
     *
     * @param taxon2
     * @return
     */
    public TaxonNCBI getTaxonLCA(TaxonNCBI taxon2) {
        return (TaxonNCBI) super.getTaxonLCA(taxon2);
    }
//    public TaxonNCBI getTaxonLCA(TaxonNCBI taxon2) {
//        String[] lineage = taxonSeparator.parse(this.getLineageString());
//        String[] lineage2 = taxonSeparator.parse(taxon2.getLineageString());
//
//        int minLen = (lineage.length > lineage2.length) ? lineage2.length : lineage.length;
//
//        // assume lineage[0] == lineage2[0]
//        for(int i = 1; i < minLen; i++){
//            if (!lineage[i].equalsIgnoreCase(lineage2[i]))
//                return lineage[i-1];
//        }
//
//        return null; // exception
//    }

    public Taxon getParentTaxon() {
        try {
            return (parentTaxId != null) ? TaxonomyPool.getAndAddTaxIdByMemory(parentTaxId) : null;
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }
}
