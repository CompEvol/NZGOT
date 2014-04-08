package nzgo.toolkit.core.taxonomy;

/**
 * Taxon specially for NCBI
 *
 * @author Walter Xie
 */
public class TaxonNCBI extends Taxon {

    protected int gi;

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

}
