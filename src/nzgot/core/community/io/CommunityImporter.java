package nzgot.core.community.io;

import nzgot.core.community.Community;
import nzgot.core.community.OTU;
import nzgot.core.community.OTUs;
import nzgot.core.community.Reference;
import nzgot.core.community.util.NameParser;
import nzgot.core.community.util.NameSpace;
import nzgot.core.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

/**
 * Community Matrix Importer
 * Not attempt to store reads as Sequence, but as String
 * @author Walter Xie
 */
public class CommunityImporter extends OTUsImporter {

    public static void importOTUs (File otusFile, OTUs otus) throws IOException, IllegalArgumentException {
        BufferedReader reader = new BufferedReader(new FileReader(otusFile));

        Logger.getLogger().info("\nImport OTUs from file: " + otusFile);

        OTU otu = null;
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
//                line.replaceAll("size=", "");
                // the current label only contains otu name
                String otuName = line.substring(1);
                otu = new OTU(otuName);

                otus.addUniqueElement(otu);

            } else {
                // TODO add ref sequence
            }

            line = reader.readLine();
        }

        reader.close();
    }

    public static void importOTUsAndMapping(File otuMappingFile, OTUs otus) throws IOException, IllegalArgumentException {

        BufferedReader reader = new BufferedReader(new FileReader(otuMappingFile));

        Logger.getLogger().info("\nImport OTUs and OTU mapping from file: " + otuMappingFile);

        OTU otu = null;
        String line = reader.readLine();
        while (line != null) {
            // 2 columns: 1st -> read id, 2nd -> otu name
            String[] fields = line.split(NameParser.SEPARATOR_COLUMN, -1);

            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            String otuName = fields[OTU_MAPPING_INDEX_OTU_NAME];
            if (otus.containsOTU(otuName)) {
                otu = (OTU) otus.getUniqueElement(otuName);
                otu.addUniqueElement(fields[OTU_MAPPING_INDEX_READ]);
            } else {
                otu = new OTU(otuName);
                otu.addUniqueElement(fields[OTU_MAPPING_INDEX_READ]);
                otus.addUniqueElement(otu);
            }

            line = reader.readLine();
        }

        reader.close();
    }

    /**
     * 1st set sampleType, default to BY_PLOT
     * 2nd load reads into each OTU, and parse label to get sample array
     * 3rd set sample array, and calculate Alpha diversity for each OTU
     * @param otuMappingFile
     * @param community
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    public static void importOTUMappingBySamples(File otuMappingFile, Community community) throws IOException, IllegalArgumentException {
        TreeSet<String> samples = new TreeSet<>();
        NameParser nameParser = NameParser.getInstance();

        BufferedReader reader = new BufferedReader(new FileReader(otuMappingFile));

        Logger.getLogger().info("\nImport OTU mapping (to reads) file: " + otuMappingFile);

        // 1st, set sampleType, default to BY_PLOT
        community.setSampleType(NameSpace.BY_PLOT);
        Logger.getLogger().info("\nSet sample type: " + community.getSampleType());

        // 2nd, parse label to get sample
        String line = reader.readLine();
        while (line != null) {
            // 2 columns: 1st -> read id, 2nd -> otu name
            String[] fields = line.split(NameParser.SEPARATOR_COLUMN, -1);

            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            OTU otu = (OTU) community.getUniqueElement(fields[OTU_MAPPING_INDEX_OTU_NAME]);
            if (otu == null) {
                throw new IllegalArgumentException("Error: find an invalid OTU " + fields[1] +
                        ", from the mapping file which does not exist in OTUs file !");
            } else {
                otu.addUniqueElement(fields[OTU_MAPPING_INDEX_READ]);

                // if by plot, then add plot to TreeSet, otherwise add subplot
                String sampleLocation = nameParser.getSampleBy(community.getSampleType(), fields[OTU_MAPPING_INDEX_READ]);
                samples.add(sampleLocation);
            }

            line = reader.readLine();
        }

        reader.close();

        // 3rd, set diversities and samples
        community.setSamplesAndDiversities(samples);
    }

    public static void importReferenceSequenceMapping(File referenceMappingFile, OTUs otus) throws IOException, IllegalArgumentException {

        BufferedReader reader = new BufferedReader(new FileReader(referenceMappingFile));

        Logger.getLogger().info("\nImport reference sequence mapping (to OTU) file: " + referenceMappingFile);

        String line = reader.readLine();
        while (line != null) {
            // 3 columns: 1st -> identity %, 2nd -> otu name, 3rd -> reference sequence id
            String[] fields = line.split(NameParser.SEPARATOR_COLUMN, -1);

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            OTU otu = (OTU) otus.getUniqueElement(fields[REF_SEQ_MAPPING_INDEX_OTU_NAME]);
            if (otu == null) {
                throw new IllegalArgumentException("Error: find an invalid OTU " + fields[1] +
                        ", from the mapping file which does not exist in OTUs file !");
            } else {
                Reference<OTU, String> refSeq = new Reference<>(otu, fields[REF_SEQ_MAPPING_INDEX_REF_SEQ]);
                otu.setReference(refSeq);
            }

            line = reader.readLine();
        }

        reader.close();
    }

    public static boolean isOTUsFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTUS_RELABELED) && fileName.endsWith(NameSpace.POSTFIX_OTUS);
    }

    public static boolean isOTUMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_MAPPING) && fileName.endsWith(NameSpace.POSTFIX_MAPPING);
    }

    public static boolean isReferenceMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_REFERENCE) && fileName.endsWith(NameSpace.POSTFIX_MAPPING);
    }
}
