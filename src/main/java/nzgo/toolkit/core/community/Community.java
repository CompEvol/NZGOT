package nzgo.toolkit.core.community;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.DereplicatedSequence;
import nzgo.toolkit.core.uparse.io.CommunityFileIO;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;

import javax.activation.UnsupportedDataTypeException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeSet;

/**
 * Community Matrix: elementsSet contains OTU
 * same as OTUs, but including IO inputs and site parser
 * load all OTUs and mappings from 3 files
 * 2 compulsory files: otusFile, otuMappingFile
 * 1 optional file: refSeqMappingFile
 *
 * Note: countReadsPerSite() count annotated size if isCountSizeAnnotation() is true,
 * CommunityFileIO.importCommunityFrom*File cannot count annotated size,
 * use countReadsPerSite() after it to count annotated size
 *
 * @author Walter Xie
 */
public class Community<E> extends OTUs<E> {

    public final SiteNameParser siteNameParser;
    // the final sites already parsed from label
    protected final String[] sites;

    public Community(Community community, String name) {
        super(name);
        this.siteNameParser = community.siteNameParser;
        this.sites = community.sites;
    }

    public Community(File otuMappingFile, SiteNameParser siteNameParser) {
        this(otuMappingFile, siteNameParser, false, false);
    }

    /**
     * create community matrix from ucMappingFile only
     * @param ucMappingFile
     * @param siteNameParser
     * @param countSizeAnnotation
     * @param removeElements                 if true, then clear elementsSet
     */
    public Community(File ucMappingFile, SiteNameParser siteNameParser, boolean countSizeAnnotation, final boolean removeElements) {
        super(NameUtil.getNameNoExtension(ucMappingFile.getName()));
        this.siteNameParser = siteNameParser;

        if (ucMappingFile == null)
            throw new IllegalArgumentException("Community needs mapping file ! ");

        TreeSet<String> sitesTS = null;
        try {
            this.setCountSizeAnnotation(countSizeAnnotation);
            sitesTS = CommunityFileIO.importCommunityFromUCFile(this, ucMappingFile, siteNameParser);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sitesTS != null) {
            this.sites = sitesTS.toArray(new String[sitesTS.size()]);
            countReadsPerSite();
        } else {
            this.sites = null;
        }
    }

    public Community(File otuMappingFile, File refSeqMappingFile, SiteNameParser siteNameParser) {
        this(otuMappingFile, siteNameParser, false, false);

        try {
            if (refSeqMappingFile != null)
                OTUsFileIO.importRefSeqFromMapUCAndMapToOTUs(this, refSeqMappingFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * create community matrix from upMappingFile generated in OTU clustering,
     * and remove chimeras discovered in denovo chimeras filtering.
     *  @param upMappingFile
     * @param chimerasFile      if null, no chimeras filtering
     * @param siteNameParser
     * @param countSizeAnnotation
     * @param removeElements
     */
    public Community(Path upMappingFile, Path chimerasFile, SiteNameParser siteNameParser, boolean countSizeAnnotation, final boolean removeElements) {
        super(NameUtil.getNameNoExtension(upMappingFile.getFileName().toString()));
        this.siteNameParser = siteNameParser;

        if (upMappingFile == null)
            throw new IllegalArgumentException("Community needs mapping file ! ");

        TreeSet<String> sitesTS = null;
        try {
            this.setCountSizeAnnotation(countSizeAnnotation);
            sitesTS = CommunityFileIO.importCommunityFromUPFile(this, upMappingFile, chimerasFile, siteNameParser);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sitesTS != null) {
            this.sites = sitesTS.toArray(new String[sitesTS.size()]);
            countReadsPerSite();
        } else {
            this.sites = null;
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

        MyLogger.debug("Get classified community: total OTUs = " + otus + ", total reads = " + reads);

        return classifiedCommunity;
    }

    /**
     * get subset of this community whose OTU exists in otusFastaFile
     * @param otusFastaFile
     * @return
     * @throws IOException
     */
    public Community<E> getSubCommunity(Path otusFastaFile) throws IOException {
        Module.validateFileName(otusFastaFile.getFileName().toString(), "OTUs", NameSpace.SUFFIX_FASTA);

        Community<E> subCommunity = new Community<>(this, this.getName() + "_sub");

        int reads = 0;
        int otus = 0;
        BufferedReader reader = FileIO.getReader(otusFastaFile, "OTUs head sequences");
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                String label = line.substring(1);
                E e = this.getUniqueElement(label);
                if (e != null) {
                    subCommunity.add(e);
                    OTU otu = (OTU) e;
                    reads += otu.size();
                    otus++;
                }
            }
            line = reader.readLine();
        }
        reader.close();

        MyLogger.debug("Get sub community: total OTUs = " + otus + ", total reads = " + reads);

        return subCommunity;
    }

    public void countReadsPerSite() {
        if (siteNameParser == null || sites == null || sites.length < 1)
            throw new IllegalArgumentException("Error: sample array was not initialized ! ");

        for (E e : this) {
            OTU otu = (OTU) e;
            otu.setReadsPerSite(siteNameParser, sites, countSizeAnnotation, removeElements);
//            AlphaDiversity alphaDiversity = new AlphaDiversity(siteNameParser, sites, otu);
//            otu.setAlphaDiversity(alphaDiversity);
        }
    }

    public String[] getSites() {
        return (sites == null) ? new String[0] : sites;
    }

    public int filterChimeras(List<E> chimeras) throws UnsupportedDataTypeException {
        int sizeRemoved = 0;
        for (E chimera : chimeras) {
            if (chimera instanceof DereplicatedSequence) {
                if (this.contains(chimera)) {
                    int annotatedSize = ((DereplicatedSequence) chimera).getAnnotatedSize();
                    sizeRemoved += annotatedSize;
                    this.remove(chimera);
//                    MyLogger.debug("remove chimera " + chimera + ", annotated size = " + annotatedSize);
                }
            } else {
                throw new UnsupportedDataTypeException("Unsupported Sequence Type : " + chimera);
            }
        }
        return sizeRemoved;
    }

//    public File getRefSeqMappingFile() {
//        return refSeqMappingFile;
//    }

    //Main method
    public static void main(final String[] args) {
        String[] experiments = new String[]{"18S-test"}; //"COI","COI-spun","ITS","trnL","16S","18S"
        int[] thresholds = new int[]{97}; // 90,91,92,93,94,95,96,97,98,99,100
        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/PipelineUPARSE/");
        String otuMappingFileName = "out.up";
        String chimerasFileName = "chimeras.fasta";
        String reportFileName = "_otus_report.tsv";
        String cmFileName = "_cm.csv";

        boolean countSizeAnnotation = true;
        boolean removeElements = false;

        try {
//            CommunityFileIO.reportCommunityByOTUThreshold(workDir, otuMappingFileName, reportFileName, cmFileName, experiments, thresholds, 97);
            for (String experiment : experiments) {
                // go into each gene folder
                Path workPath = Paths.get(workDir.toString(), experiment);
                MyLogger.info("\nWorking path = " + workPath);
                for (int thre : thresholds) {
                    Path otusPath = Paths.get(workPath.toString(), "otus" + thre);

                    Path otuMappingFile = Paths.get(otusPath.toString(), otuMappingFileName);
                    Path chimerasFile = Paths.get(otusPath.toString(), chimerasFileName);
                    SiteNameParser siteNameParser = new SiteNameParser();
                    Community community = new Community(otuMappingFile, chimerasFile, siteNameParser, countSizeAnnotation, removeElements);

                    Path outCMFilePath = Paths.get(otusPath.toString(), experiment + "_" + thre + cmFileName);
                    int[] report = CommunityFileIO.writeCommunityMatrix(outCMFilePath, community);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
