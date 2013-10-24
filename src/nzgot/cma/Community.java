package nzgot.cma;

import nzgot.cma.io.CMImporter;
import nzgot.core.util.BioObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Community Matrix
 * @author Walter Xie
 */
public class Community extends BioObject {

    protected final File otusFile;
    protected final File otuMappingFile;
    protected final File referenceMappingFile; // optional: Sanger sequence for reference

    public Community(File otusFile, File otuMappingFile) {
        this(otusFile, otuMappingFile, null);
    }

    public Community(File otusFile, File otuMappingFile, File referenceMappingFile) {
        super(otusFile.getName());
        this.otusFile = otusFile;
        this.otuMappingFile = otuMappingFile;
        this.referenceMappingFile = referenceMappingFile;

        try {
            CMImporter.importOTUs(otusFile, this);
            CMImporter.importOTUMapping(otuMappingFile, this);
            if (referenceMappingFile != null)
                CMImporter.importReferenceMappingFile(referenceMappingFile, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<OTU, int[]> getCommunityMatrix(String[] sample) {
        //TODO
        return null;
    }

    /**
     * key -> reference sequence id, value -> number of reads
     * sum up reads according to reference sequence
     * depend on data uploaded from importOTUs, importOTUMapping, importReferenceMappingFile
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public Map<String, Integer> getRefSeqReadsCount() throws IOException, IllegalArgumentException {
        Map<String, Integer> readsCountMap = new HashMap<String, Integer>();

        for(Object e : elementsSet){
            OTU otu = (OTU) e;
            String refSeqId = otu.getRefSeqId();
            if (refSeqId != null) {
                int reads = otu.elementsSet.size();
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

    public File getReferenceMappingFile() {
        return referenceMappingFile;
    }

}
