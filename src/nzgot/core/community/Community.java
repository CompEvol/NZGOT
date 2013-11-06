package nzgot.core.community;

import nzgot.core.community.io.CMImporter;
import nzgot.core.community.util.NameSpace;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 * Community Matrix
 * elementsSet contains OTU
 * load all OTUs and mappings from 3 files
 * 2 compulsory files: otusFile, otuMappingFile
 * 1 optional file: referenceMappingFile
 * @author Walter Xie
 */
public class Community<E> extends OTUs<E> {

    // the sampling location determined by sampleType, default by plot
    // e.g. 454 soil data: by subplot is 2-C and 2-N, by plot is 2
    protected int sampleType = NameSpace.BY_PLOT;
    // the final samples already parsed from label
    protected String[] samples;

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

    public void initSamplesBy(TreeSet<String> samples){
        if (samples == null || samples.size() < 1)
            throw new IllegalArgumentException("Error: cannot parse sample from read name : " + samples);

        this.samples = samples.toArray(new String[samples.size()]);
    }

    public void setDiversities () {
        for (E e : this) {
            OTU otu = (OTU) e;
            AlphaDiversity alphaDiversity = new AlphaDiversity(sampleType, samples, otu);
            otu.setAlphaDiversity(alphaDiversity);
        }
    }

    public int getSampleType() {
        return sampleType;
    }

    public void setSampleType(int sampleType) {
        this.sampleType = sampleType;
        if (samples != null) {
        //TODO update matrix and diversity
        }
    }

    public String[] getSamples() {
        return samples;
    }

    public File getReferenceMappingFile() {
        return referenceMappingFile;
    }

}
