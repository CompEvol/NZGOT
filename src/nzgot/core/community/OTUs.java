package nzgot.core.community;

import nzgot.core.util.BioSortedSet;

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

    public boolean containsOTU(String otuName) {
        for(E e : this){
            if (e.toString().contentEquals(otuName))
                return true;
        }
        return false;
    }

    public E getOTU(String otuName) {
        for(E e : this){
            if (e.toString().contentEquals(otuName))
                return e;
        }
        return null;
    }

    /**
     * give a sequence to get the OTU it belongs to
     * @param sequenceName
     * @return
     */
    public E getOTUOfSeq(String sequenceName) {
        Object sequence;
        for(E e : this){
            OTU otu = (OTU) e;
            sequence = otu.getUniqueElement(sequenceName);
            if (sequence != null)
                return e;
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

}
