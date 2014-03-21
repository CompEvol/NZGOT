package nzgo.toolkit.core.community;

import nzgo.toolkit.core.io.CommunityFileIO;
import nzgo.toolkit.core.io.OTUsFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.naming.SampleNameParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;

/**
 * Community Matrix
 * elementsSet contains OTU
 * same as OTUs, but including IO inputs and sample parser
 * load all OTUs and mappings from 3 files
 * 2 compulsory files: otusFile, otuMappingFile
 * 1 optional file: refSeqMappingFile
 * @author Walter Xie
 */
public class Community<E> extends OTUs<E> {

    public final SampleNameParser sampleNameParser;
    // the final samples already parsed from label
    public String[] samples;

//    protected final File otusFile;
//    protected final File otuMappingFile;
//    protected final File refSeqMappingFile; // optional: Sanger sequence for reference

    public Community(Community community, String name) {
        super(name);
//        otusFile = null;
//        otuMappingFile = null;
//        refSeqMappingFile = null;
        this.sampleNameParser = community.sampleNameParser;
        this.samples = community.samples;
    }

    public Community(SampleNameParser sampleNameParser, File otuMappingFile) {
        this(sampleNameParser, null, otuMappingFile, null);
    }

    public Community(SampleNameParser sampleNameParser, File otuMappingFile, File refSeqMappingFile) {
        this(sampleNameParser, null, otuMappingFile, refSeqMappingFile);
    }

    /**
     * create communit matrix from either otusFile or otuMappingFile or both
     * refSeqMappingFile is optional
     * if otuMappingFile null, then no elements (sequences) in OTU
     * @param otusFile
     * @param otuMappingFile
     * @param refSeqMappingFile
     */
    public Community(SampleNameParser sampleNameParser, File otusFile, File otuMappingFile, File refSeqMappingFile) {
        super(otusFile != null ? NameUtil.getNameWithoutExtension(otusFile.getName()) : NameUtil.getNameWithoutExtension(otuMappingFile.getName()));
//        this.otusFile = otusFile;
//        this.otuMappingFile = otuMappingFile;
//        this.refSeqMappingFile = refSeqMappingFile;

        this.sampleNameParser = sampleNameParser;

        if (otusFile == null && otuMappingFile == null)
            throw new IllegalArgumentException("Community needs either OTUs or mapping file ! ");

        try {
            if (otusFile != null)
                OTUsFileIO.importOTUs(otusFile, this);

            TreeSet<String> samples = OTUsFileIO.importOTUsAndMappingFromUCFile(otuMappingFile, this, otusFile == null, sampleNameParser);

            // if samples is null, then no samples being parsed
            if (samples != null && samples.size() > 0) {
                this.samples = samples.toArray(new String[samples.size()]);
                setSamplesAndDiversities(sampleNameParser);
            }

            if (refSeqMappingFile != null)
                OTUsFileIO.importRefSeqMappingFromUCFile(refSeqMappingFile, this);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @return  Community only has taxonomy
     */
    public Community<E> getClassifiedCommunity() {
        Community<E> classifiedCommunity = new Community<>(this, this.getName() + "_classified");

        int reads = 0;
        int otus = 0;
        for(E e : this){
            OTU otu = (OTU) e;
            if (otu.hasTaxon()) {
                classifiedCommunity.add(e);
                reads += otu.size();
                otus++;
            }
        }

        MyLogger.debug("Get classified community: total reads = " + reads + ", total OTUs = " + otus);

        return classifiedCommunity;
    }


    /**
     * E has to be OTU
     * @param sampleNameParser
     */
    public void setSamplesAndDiversities(SampleNameParser sampleNameParser) {
        for (E e : this) {
            OTU otu = (OTU) e;
            AlphaDiversity alphaDiversity = new AlphaDiversity(sampleNameParser, samples, otu);
            otu.setAlphaDiversity(alphaDiversity);
        }
    }

    public String[] getSamples() {
        return (samples == null) ? new String[0] : samples;
    }

//    public File getRefSeqMappingFile() {
//        return refSeqMappingFile;
//    }

    //Main method
    public static void main(final String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);


        File otusFile = new File(workPath + "reads-Arthropoda.fasta");
        File otuMappingFile = new File(workPath + "map.uc");
        SampleNameParser sampleNameParser = new SampleNameParser();
        Community community = new Community(sampleNameParser, otusFile, otuMappingFile, null);

        Path outCMFilePath = Paths.get(workPath, community.getName() + "_" + CommunityFileIO.COMMUNITY_MATRIX + ".csv");
        try {
            CommunityFileIO.writeCommunityMatrix(outCMFilePath, community);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
