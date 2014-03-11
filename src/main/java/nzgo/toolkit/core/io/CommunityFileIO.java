package nzgo.toolkit.core.io;

import nzgo.toolkit.core.community.AlphaDiversity;
import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeSet;

/**
 * Community Matrix FileIO
 * Not attempt to store reads as Sequence, but as String
 * both OTU and reference mapping file are uc format.
 * @author Walter Xie
 */
public class CommunityFileIO extends OTUsFileIO {

    /**
     * Ideally otuMappingUCFile should have all OTUs,
     * so that the validation assumed to be done before this method
     * 1st set sampleType, default to BY_PLOT
     * 2nd load reads into each OTU, and parse label to get sample array
     * 3rd set sample array, and calculate Alpha diversity for each OTU
     * @param otuMappingUCFile
     * @param community
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    public static void importOTUsAndMappingFromUCFile(File otuMappingUCFile, Community community, boolean canCreateOTU) throws IOException, IllegalArgumentException {
        TreeSet<String> samples = new TreeSet<>();

        // 1st, set sampleType, default to BY_PLOT
        community.setSampleType(NameSpace.BY_SUBPLOT);
        MyLogger.info("\nSet sample type: " + community.getSampleType());

        // 2nd, parse label to get sample
        importOTUsAndMappingFromUCFile(otuMappingUCFile, community, canCreateOTU, samples);

        // 3rd, set diversities and samples
        community.setSamplesAndDiversities(samples);
    }


    /**
     * write community matrix
     * A COLUMN PER SAMPLE AND A ROW FOR EACH SPECIES/OTU
     *
     * @param outCMFilePath
     * @param community
     * @param filterNoTaxonomy      not print the OTU that has no taxonomy
     //* @param ranks
     * @throws IOException
     * @throws IllegalArgumentException
     */
    //TODO tidy up my strange code
    public static void writeCommunityMatrix(Path outCMFilePath, Community community, boolean filterNoTaxonomy/*, Rank... ranks*/) throws IOException, IllegalArgumentException {

        BufferedWriter writer = getWriter(outCMFilePath, "community matrix");

        for (String sample : community.getSamples()) {
            writer.write("," + sample);
        }
        writer.write("\n");

        for(Object o : community){
            OTU otu = (OTU) o;

            if (!filterNoTaxonomy || (filterNoTaxonomy && otu.hasTaxon()) ) {
                writer.write(otu.getName());

                // print AlphaDiversity column
                AlphaDiversity alphaDiversity = otu.getAlphaDiversity();
                if (alphaDiversity != null) {
//                throw new IllegalArgumentException("Error: cannot AlphaDiversity report for OTU : " + otu);

                    for (int a : alphaDiversity.getAlphaDiversity()) {
                        writer.write("," + a);
                    }
                }
            }

            // print Taxon column
//            if (otu.hasTaxon()) {
//                Taxon taxonLCA = otu.getTaxonLCA();
//                writer.write("," + taxonLCA.getScientificName());
//                if (taxonLCA != null && ranks != null) {
//                    for (Rank rank : ranks) {
//                        Taxon t = taxonLCA.getParentTaxonOn(rank);
//                        String str = ("no " + rank.toString()).toLowerCase();
//                        writer.write("," + (t==null?str:t.getScientificName()));
//                    }
//                }
//            }

            if (!filterNoTaxonomy || (filterNoTaxonomy && otu.hasTaxon()) )
                writer.write("\n");

        }

        writer.flush();
        writer.close();
    }

    public static void writeCommunityMatrix(Path outCMFilePath, Community community) throws IOException, IllegalArgumentException {
        writeCommunityMatrix(outCMFilePath, community, false);
    }

    /**
     * 2 columns: 1st -> reference sequence id, 2nd -> number of reads
     * last row is total reads
     * at least one OTU should have reference
     * @param outFileAndPath
     * @param otus
     * @throws IOException
     * @throws IllegalArgumentException
     */

    public static void writeRefReads(String outFileAndPath, OTUs otus) throws IOException, IllegalArgumentException {

        PrintStream out = new PrintStream(new FileOutputStream(outFileAndPath));

        MyLogger.info("\nGenerate report of how many reads map to reference sequence in the file: " + outFileAndPath);

        int total = 0;
        Map<String, Integer> readsCountMap = otus.getRefSeqReadsCountMap();
        for(Map.Entry<String, Integer> entry : readsCountMap.entrySet()){
            out.println(entry.getKey() + "\t" + entry.getValue());
            total += entry.getValue();
        }
        out.println("total\t" + total);
        out.flush();
        out.close();
    }


}
