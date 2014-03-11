package nzgo.toolkit.core.community;

import nzgo.toolkit.core.taxonomy.Taxa;
import nzgo.toolkit.core.taxonomy.TaxaUtil;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.util.BioSortedSet;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

/**
 * the set to keep all OTUs
 * elementsSet contains OTU
 * @author Walter Xie
 */
public class OTUs<E> extends BioSortedSet<E> {

    public OTUs(String name) {
        super(name);
    }

    /**
     * give a sequence name to get the OTU it belongs to
     * or give name to get the OTU
     * or give alias to get the OTU, if OTU has alias
     * TODO: only suit for hard clustering currently
     * @param name
     * @return
     */
    public E getOTUByName(String name) {
        Object sequence;
        for(E e : this){
            OTU otu = (OTU) e;
            if (name.contentEquals(otu.getName()) || name.contentEquals(otu.getAlias())) {
                return e;
            } else {
                sequence = otu.getUniqueElement(name);
                if (sequence != null)
                    return e;
            }
        }
        return null;
    }

    /**
     * key -> reference sequence id, value -> number of reads
     * sum up reads according to reference sequence
     * E has to be OTU
     */
    public Map<String, Integer> getRefSeqReadsCountMap() {
        Map<String, Integer> readsCountMap = new HashMap<>();

        for(E e : this){
            OTU otu = (OTU) e;
            Reference reference = otu.getReference();
            if (reference != null) {
                String refSeqId = reference.toString();
                int reads = otu.size();
                // if refseq has count in map, then add new count to it
                if (readsCountMap.containsKey(refSeqId)) {
                    reads += readsCountMap.get(refSeqId);
                    readsCountMap.put(refSeqId, reads);
                }
                readsCountMap.put(refSeqId, reads);
            }
        }

        return readsCountMap;
    }

    /**
     * set taxon classification from a map
     * if OTU has no classification from the map
     * then setTaxonLCA unclassified
     * @param otuTaxaMap
     */
    public void setTaxa(SortedMap<String, Taxon> otuTaxaMap) {
        for(E e : this){
            OTU otu = (OTU) e;
            Taxon taxon = otuTaxaMap.get(otu.getName());
            if (taxon != null)
                otu.setTaxonLCA(taxon);
        }

        Taxon unclassified = TaxaUtil.getUnclassified();
        for(E e : this){
            OTU otu = (OTU) e;
            if (!otu.hasTaxon())
                otu.setTaxonLCA(unclassified);
        }
    }

    /**
     * get Taxa from all Taxon of OTU in OTUs
     * @return
     */
    public Taxa getTaxa() {
        Taxa taxa = new Taxa();

        for(E e : this){
            OTU otu = (OTU) e;
            if (otu.hasTaxon()) {
                // may have multi-otus assigned to same taxon
                taxa.add(otu.getTaxonLCA());
            }
        }

        if (taxa.size() < 1) return null;
        return taxa;
    }

    /**
     * use to check if any OTU imported from otu fasta file
     * that does not exist in the mapping file
     * In this case, OTU set is empty
     * @return
     */
    public boolean isValid() {
        boolean isValid = true;
        for(E e : this){
            OTU otu = (OTU) e;
            if (otu.size() < 1) {
                isValid = false;
                System.err.println("Error: empty OTU : " + e.toString());
            }
        }
        return isValid;
    }
}
