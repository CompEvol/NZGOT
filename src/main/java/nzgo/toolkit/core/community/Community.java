package nzgo.toolkit.core.community;

import nzgo.toolkit.core.io.CommunityFileIO;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.OTUsFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.pipeline.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;

/**
 * Community Matrix: elementsSet contains OTU
 * same as OTUs, but including IO inputs and site parser
 * load all OTUs and mappings from 3 files
 * 2 compulsory files: otusFile, otuMappingFile
 * 1 optional file: refSeqMappingFile
 * @author Walter Xie
 */
public class Community<E> extends OTUs<E> {

    public final SiteNameParser siteNameParser;
    // the final sites already parsed from label
    public final String[] sites;

    public Community(Community community, String name) {
        super(name);
        this.siteNameParser = community.siteNameParser;
        this.sites = community.sites;
    }

    public Community(File otuMappingFile, SiteNameParser siteNameParser) {
        this(otuMappingFile, siteNameParser, false);
    }

    /**
     * create community matrix from otuMappingFile only
     * @param otuMappingFile
     * @param siteNameParser
     * @param simple                 if true, then clear elementsSet
     */
    public Community(File otuMappingFile, SiteNameParser siteNameParser, boolean simple) {
        super(NameUtil.getNameWithoutExtension(otuMappingFile.getName()));
        this.siteNameParser = siteNameParser;

        if (otuMappingFile == null)
            throw new IllegalArgumentException("Community needs mapping file ! ");

        TreeSet<String> sitesTS = null;
        try {
            sitesTS = CommunityFileIO.importCommunityFromMapUC(this, otuMappingFile, siteNameParser);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sitesTS != null) {
            this.sites = sitesTS.toArray(new String[sitesTS.size()]);
            countReads(siteNameParser, simple);
        } else {
            this.sites = null;
        }
    }

    public Community(File otuMappingFile, File refSeqMappingFile, SiteNameParser siteNameParser) {
        this(otuMappingFile, siteNameParser, false);

        try {
            if (refSeqMappingFile != null)
                OTUsFileIO.importRefSeqFromMapUCAndMapToOTUs(this, refSeqMappingFile);
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
        Module.validateFileName(otusFastaFile.getFileName().toString(), new String[]{NameSpace.SUFFIX_FASTA}, "OTUs");

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

    /**
     * E has to be OTU
     * @param siteNameParser
     */
    public void countReads(SiteNameParser siteNameParser, boolean simple) {
        if (sites == null || sites.length < 1)
            throw new IllegalArgumentException("Error: sample array was not initialized: " + sites);

        for (E e : this) {
            OTU otu = (OTU) e;
            otu.simple = simple;
            otu.countReadsPerSite(siteNameParser, sites);
//            AlphaDiversity alphaDiversity = new AlphaDiversity(siteNameParser, sites, otu);
//            otu.setAlphaDiversity(alphaDiversity);
        }
    }

    public String[] getSites() {
        return (sites == null) ? new String[0] : sites;
    }

//    public File getRefSeqMappingFile() {
//        return refSeqMappingFile;
//    }

    //Main method
    public static void main(final String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        File otuMappingFile = new File(workPath + "map.uc");
        SiteNameParser siteNameParser = new SiteNameParser();
        Community community = new Community(otuMappingFile, siteNameParser, true);

        Path outCMFilePath = Paths.get(workPath, community.getName() + "_" + CommunityFileIO.COMMUNITY_MATRIX + ".csv");
        try {
            CommunityFileIO.writeCommunityMatrix(outCMFilePath, community);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
