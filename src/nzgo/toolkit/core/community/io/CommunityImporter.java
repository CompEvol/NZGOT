package nzgo.toolkit.core.community.io;

import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.util.NameSpace;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 * Community Matrix Importer
 * Not attempt to store reads as Sequence, but as String
 * both OTU and reference mapping file are uc format.
 * @author Walter Xie
 */
public class CommunityImporter extends OTUsImporter {

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
        community.setSampleType(NameSpace.BY_PLOT);
        MyLogger.info("\nSet sample type: " + community.getSampleType());

        // 2nd, parse label to get sample
        importOTUsAndMappingFromUCFile(otuMappingUCFile, community, canCreateOTU, samples);

        // 3rd, set diversities and samples
        community.setSamplesAndDiversities(samples);
    }

}
