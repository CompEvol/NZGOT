package nzgo.toolkit.core.community;

import jebl.evolution.sequences.Sequence;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.util.ArrayUtil;
import nzgo.toolkit.core.util.BioSortedSet;

import java.util.Arrays;

/**
 * OTU, 1) elementsSet contains Reads, 2) or no elements but using readsPerSite
 * 1) E could be String or jebl.evolution.sequences.Sequence
 * Assuming 1 sequence only can be assigned to 1 OTU
 * 2) use readsPerSite to fast counts
 * @author Walter Xie
 */
public class OTU<E> extends BioSortedSet<E> {

    public boolean simple = false;
    public int[] readsPerSite;
    public Taxon taxonLCA;

    @Deprecated
    protected Reference reference;
    @Deprecated
    protected String alias; // special case to have 2 names

    public OTU(String name) {
        super(name);
    }



    public int size() {
        if (super.size() < 1 && readsPerSite != null) {
            int size = 0;
            for (int reads : readsPerSite) {
                size += reads;
            }
            return size;
        }
        return super.size();
    }

    public void countReadsPerSite(SiteNameParser siteNameParser, String[] sites) {
        readsPerSite = new int[sites.length];

        for (Object read: this) {
            String label;
            if (read instanceof Sequence) {
                label = ((Sequence) read).getTaxon().getName();
            } else {
                label = read.toString();
            }

            String sampleLocation = siteNameParser.getSite(label);
            int i = ArrayUtil.indexOf(sampleLocation, sites);
            if (i < 0) {
                throw new IllegalArgumentException("Error: incorrect site location : " + sampleLocation +
                        " from sites array : " + Arrays.asList(sites));
            } else {
                readsPerSite[i]++;
            }
        }

        if (simple) this.clear(); // be careful
    }

    public boolean hasTaxon() {
        return taxonLCA != null && taxonLCA.isClassified();
    }

//    public Taxon getTaxonLCA() {
//        return taxonLCA;
//    }
//
//    public void setTaxonLCA(Taxon taxonLCA) {
//        this.taxonLCA = taxonLCA;
//    }

    @Deprecated
    public String getAlias() {
        if (alias == null) return getName();
        return alias;
    }
    @Deprecated
    public void setAlias(String alias) {
        this.alias = alias;
    }
    @Deprecated
    public Reference getReference() {
        return reference;
    }
    @Deprecated
    public void setReference(Reference reference) {
        this.reference = reference;
    }
}
