package nzgo.toolkit.core.community;

import jebl.evolution.sequences.Sequence;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.util.ArrayUtil;
import nzgo.toolkit.core.util.BioSortedSet;
import nzgo.toolkit.core.util.Element;

import java.util.Arrays;

/**
 * OTU, 1) elementsSet contains Reads, 2) or no elements but using readsPerSite
 * 1) E could be String or jebl.evolution.sequences.Sequence
 * Assuming 1 sequence only can be assigned to 1 OTU
 * 2) use readsPerSite to fast counts
 *
 * @author Walter Xie
 */
public class OTU<E> extends BioSortedSet<E> {

    public Taxon taxonLCA;

    protected int[] readsPerSite;
    @Deprecated
    protected Reference reference;
    @Deprecated
    protected String alias; // special case to have 2 names

    public OTU(String name) {
        super(name);
    }

    //TODO: is it real faster to use readsPerSite to store number ? it may just save memory.
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

    /**
     * get total annotated size from Reads in this OTU
     * for normal size use this.size();
     * @return
     */
    public int getTotalAnnotatedSize() {
        int size = 0;
        for (E read : this) {
            if (!(read instanceof Element))
                throw new IllegalArgumentException("Read " + read + " needs to be countable, such as Element.");

            int sizeAnnotated = ((Element) read).getCounter().getCount();
            size += sizeAnnotated;
        }
        return size;
    }

    public int[] getReadsPerSite() {
        return readsPerSite;
    }

    public void setReadsPerSite(SiteNameParser siteNameParser, String[] sites, final boolean countSizeAnnotation, final boolean removeElements) {
        readsPerSite = new int[sites.length];

        for (E read : this) {
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
                if (countSizeAnnotation) {
                    if (!(read instanceof Element))
                        throw new IllegalArgumentException("Read " + read + " needs to be countable, such as Element.");

                    int sizeAnnotated = ((Element) read).getCounter().getCount();
                    readsPerSite[i]+= sizeAnnotated;
                } else {
                    readsPerSite[i]++;
                }
            }
        }

        if (removeElements) this.clear(); // be careful
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
