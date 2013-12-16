package nzgo.toolkit.core.community;

import nzgo.toolkit.core.community.io.CommunityImporter;
import nzgo.toolkit.core.community.io.OTUsImporter;
import nzgo.toolkit.core.community.util.NameSpace;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 * Community Matrix
 * elementsSet contains OTU
 * load all OTUs and mappings from 3 files
 * 2 compulsory files: otusFile, otuMappingFile
 * 1 optional file: refSeqMappingFile
 * @author Walter Xie
 */
public class Community<E> extends OTUs<E> {

    // the sampling location determined by sampleType, default by plot
    // e.g. 454 soil data: by subplot is 2-C and 2-N, by plot is 2
    protected String sampleType = NameSpace.BY_PLOT;
    // the final samples already parsed from label
    protected String[] samples;

    protected final File otusFile;
    protected final File otuMappingFile;
    protected final File refSeqMappingFile; // optional: Sanger sequence for reference

    public Community(File otuMappingFile) {
        this(null, otuMappingFile, null);
    }

    public Community(File otuMappingFile, File refSeqMappingFile) {
        this(null, otuMappingFile, refSeqMappingFile);
    }

    public Community(File otusFile, File otuMappingFile, File refSeqMappingFile) {
        super(otusFile != null ? otusFile.getName() : otuMappingFile.getName());
        this.otusFile = otusFile;
        this.otuMappingFile = otuMappingFile;
        this.refSeqMappingFile = refSeqMappingFile;

        try {
            if (otusFile != null)
                OTUsImporter.importOTUs(otusFile, this);

            CommunityImporter.importOTUsAndMappingFromUCFile(otuMappingFile, this, otusFile == null);

            if (refSeqMappingFile != null)
                OTUsImporter.importRefSeqMappingFromUCFile(refSeqMappingFile, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void initSamplesBy(TreeSet<String> samples){
        if (samples == null || samples.size() < 1)
            throw new IllegalArgumentException("Error: cannot parse sample from read name : " + samples);

        this.samples = samples.toArray(new String[samples.size()]);
    }

    public void setSamplesAndDiversities(TreeSet<String> samples) {
        initSamplesBy(samples);

        for (E e : this) {
            OTU otu = (OTU) e;
            AlphaDiversity alphaDiversity = new AlphaDiversity(sampleType, this.samples, otu);
            otu.setAlphaDiversity(alphaDiversity);
        }
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
        if (samples != null) {
        //TODO update matrix and diversity
        }
    }

    public String[] getSamples() {
        return samples;
    }

    public File getRefSeqMappingFile() {
        return refSeqMappingFile;
    }

}
