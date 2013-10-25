package nzgot.cma;

import nzgot.cma.io.CMImporter;
import nzgot.cma.util.NameSpace;
import nzgot.core.util.BioSortedSet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Community Matrix
 * elementsSet contains OTU
 * @author Walter Xie
 */
public class Community<E> extends BioSortedSet<E> {

    // the sampling location determined by samplesBy, default by plot
    // e.g. 454 soil data: by subplot is 2-C and 2-N, by plot is 2
    public String[] samples;
    protected int samplesBy = NameSpace.BY_PLOT;

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

    public void initSamples(TreeSet<String> samples){
        if (samples == null || samples.size() < 1)
            throw new IllegalArgumentException("Error: cannot parse sample from read name : " + samples);

        this.samples = samples.toArray(new String[samples.size()]);
    }

    public void setAlphaDiversity () {
        for (E e : this) {
            OTU otu = (OTU) e;
            otu.setAlphaDiversity(samplesBy, samples);
        }
    }

    /**
     * key -> reference sequence id, value -> number of reads
     * sum up reads according to reference sequence
     * depend on data uploaded from importOTUs, importOTUMapping, importReferenceMappingFile
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public Map<String, Integer> getRefSeqReadsCountMap() throws IOException, IllegalArgumentException {
        Map<String, Integer> readsCountMap = new HashMap<>();

        for(E e : this){
            OTU otu = (OTU) e;
            String refSeqId = otu.getRefSeqId();
            if (refSeqId != null) {
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


    public int getSamplesBy() {
        return samplesBy;
    }

    public void setSamplesBy(int samplesBy) {
        this.samplesBy = samplesBy;
        //TODO update matrix
    }

    public File getReferenceMappingFile() {
        return referenceMappingFile;
    }

}
