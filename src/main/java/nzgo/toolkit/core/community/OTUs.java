package nzgo.toolkit.core.community;

import nzgo.toolkit.core.taxonomy.Taxa;
import nzgo.toolkit.core.util.BioSortedSet;

import java.util.HashMap;
import java.util.Map;

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
     * give a sequence to get the OTU it belongs to
     * TODO: only suit for hard clustering currently
     * @param sequenceName
     * @return
     */
    public E getOTUOfSeq(String sequenceName) {
        Object sequence;
        for(E e : this){
            OTU otu = (OTU) e;
            if (sequenceName.contentEquals(otu.getName())) {
                return e;
            } else {
                sequence = otu.getUniqueElement(sequenceName);
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
     * get Taxa from all Taxon of OTU in OTUs
     * @return
     */
    public Taxa getTaxa() {
        Taxa taxa = new Taxa();

        for(E e : this){
            OTU otu = (OTU) e;
            if (otu.hasTaxon()) {
                taxa.addUniqueElement(otu.getTaxonAgreed());
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
