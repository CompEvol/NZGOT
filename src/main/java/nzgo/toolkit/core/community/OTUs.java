package nzgo.toolkit.core.community;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.AssemblerUtil;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.taxonomy.TaxonSet;
import nzgo.toolkit.core.taxonomy.TaxonomyUtil;
import nzgo.toolkit.core.uparse.io.CommunityFileIO;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;
import nzgo.toolkit.core.util.BioSortedSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

/**
 * the set to keep all OTUs
 * elementsSet contains OTU
 * @author Walter Xie
 */
public class OTUs<E> extends BioSortedSet<E> {

    public static final int READS_COUNTER_ID = 0;
    public static final int OTUS_COUNTER_ID = 1;

    public boolean removeSizeAnnotation = true;

    protected boolean countSizeAnnotation = false;

    public OTUs(String name) {
        super(name);
    }

    public OTUs(String name, boolean countSizeAnnotation) {
        this(name);
        setCountSizeAnnotation(countSizeAnnotation);
    }

    public boolean isCountSizeAnnotation() {
        return countSizeAnnotation;
    }

    public void setCountSizeAnnotation(boolean countSizeAnnotation) {
        this.countSizeAnnotation = countSizeAnnotation;
    }

    public OTU getOTU(String name) {
        E e = this.getUniqueElement(name);
        if (e != null)
            return (OTU) e;
        return null;
    }

    /**
     * append |size to the end of OTU name
     * remove size annotation, if removeSizeAnnotation = true
     * need to update otus.fasta afterwords
     */
    public void appendSizeToLabel(OTUs<OTU> otus){
        for(OTU otu : otus){
            String label = AssemblerUtil.appendSizeToLabel(otu, removeSizeAnnotation);
            otu.setName(label);
        }
    }

    /**
     * sizes[0] is number of OTUs, sizes[1] in number of reads
     * if countSizeAnnotation = true, sizes[1] is the total of annotated size
     * @return
     */
    public int[] getSizes() {
        int[] sizes = new int[2];
        sizes[0] = this.size();
        for(E e : this){
            OTU otu = (OTU) e;
            sizes[1] += otu.size();
        }
        return sizes;
    }

    /**
     * set taxon classification from a map
     * if OTU has no classification from the map
     * then setTaxonLCA unclassified
     * @param otuTaxaMap
     */
    public void setTaxonomy(SortedMap<String, Taxon> otuTaxaMap) {
        Taxon unclassified = TaxonomyUtil.getUnclassified();
        int cla = 0;
        int uncla = 0;
        for(E e : this){
            OTU otu = (OTU) e;
            Taxon taxon = otuTaxaMap.get(otu.getName());
            if (taxon != null) {
                otu.taxonLCA = taxon;
                cla ++;
            } else {
                otu.taxonLCA = unclassified;
                uncla ++;
            }
        }
        MyLogger.debug("Classified OTUs = " + cla + ", unclassified OTUs = " + uncla);
    }

    /**
     * return taxonomy assignment of OTUs
     * E has to be OTU
     * @return
     */
    public TaxonSet<Taxon> getTaxonomy() {
        TaxonSet<Taxon> taxonomySet = new TaxonSet<>();

        for(E e : this){
            OTU otu = (OTU) e;
            Taxon taxonLCA = otu.taxonLCA;

            if (taxonLCA == null)
                throw new IllegalArgumentException("OTU " + otu + " does not have taxonomic identification !");
            if (otu.size() < 1)
                throw new IllegalArgumentException("OTU " + otu + " does not have any elements, size = " + otu.size() + " !");

            if (taxonomySet.containsTaxon(taxonLCA.toString())) {
                Taxon taxonAssigned = taxonomySet.getTaxon(taxonLCA.toString());
                taxonAssigned.getCounter(READS_COUNTER_ID).incrementCount(otu.size());
                taxonAssigned.getCounter(OTUS_COUNTER_ID).incrementCount(1);
            } else {
                if (taxonLCA.getCountersSize() < 2)
                    taxonLCA.addCounter(); // add 2nd counter for number of otu
                taxonLCA.getCounter(READS_COUNTER_ID).setCount(otu.size());
                taxonLCA.getCounter(OTUS_COUNTER_ID).setCount(1);
                taxonomySet.addTaxon(taxonLCA);
            }
        }
        return taxonomySet;
    }

    /**
     * give a sequence name to get the OTU it belongs to
     * or give name to get the OTU
     * or give alias to get the OTU, if OTU has alias
     * TODO: only suit for hard clustering currently
     * @param name
     * @return
     */
    @Deprecated
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


    public static void validateOTUsMapping(File otusFile, File otuMappingUCFile) throws IOException {
        OTUs otus = new OTUs(otusFile.getName());
        OTUsFileIO.importOTUsFromFasta(otus, otusFile, false, true);

        OTUs otusMap = new OTUs(otuMappingUCFile.getName());
        OTUsFileIO.importOTUsFromMapUC(otusMap, otuMappingUCFile);

        BioSortedSet diff = otus.symmetricDiff(otusMap);

        MyLogger.info("\nOTUs size = " + otus.size() + ", mapping file OTUs size = " + otusMap.size() + ", symmetric diff = " + diff.elementsToString());
    }


    //Main method
    public static void main(final String[] args) {
        String[] experiments = new String[]{"18S-test"}; //"COI","COI-spun","ITS","trnL","16S","18S"
        int[] thresholds = new int[]{97}; // 90,91,92,93,94,95,96,97,98,99,100
        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/PipelineUPARSE/");
        String otuMappingFileName = "map2.uc";
        String reportFileName = "_otus_report.tsv";
        String cmFileName = "_cm2.csv";
//        String otuMappingFileName = "map_size2.uc";
//        String reportFileName = "_otus_size2_report.tsv";
//        String cmFileName = "_cm_size2.csv";

        try {
//            CommunityFileIO.reportCommunityByOTUThreshold(workDir, otuMappingFileName, reportFileName, cmFileName, experiments, thresholds, 97);
            for (String experiment : experiments) {
                // go into each gene folder
                Path workPath = Paths.get(workDir.toString(), experiment);
                MyLogger.info("\nWorking path = " + workPath);
                for (int thre : thresholds) {
                    Path otusPath = Paths.get(workPath.toString(), "otus" + thre);

                    File otuMappingFile = Paths.get(otusPath.toString(), otuMappingFileName).toFile();
                    SiteNameParser siteNameParser = new SiteNameParser();
                    Community community = new Community(otuMappingFile, siteNameParser);

                    Path outCMFilePath = Paths.get(otusPath.toString(), experiment + "_" + thre + cmFileName);
                    int[] report = CommunityFileIO.writeCommunityMatrix(outCMFilePath, community);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
