package nzgo.toolkit.core.community.io;

import nzgo.toolkit.core.community.*;
import nzgo.toolkit.core.community.util.SampleNameParser;
import nzgo.toolkit.core.io.Importer;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.uc.UCParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 * OTUs Importer
 * attempt to store reads as Sequence
 * both OTU and reference mapping file are uc format.
 * @author Walter Xie
 */
public class OTUsImporter extends Importer {

    public static void importOTUs (File otusFile, OTUs otus) throws IOException, IllegalArgumentException {

        BufferedReader reader = getReader(otusFile, "OTUs from");

        OTU otu = null;
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
//                line.replaceAll("size=", "");
                // the current label only contains otu name
                String otuName = UCParser.getLabelNoAnnotation(line.substring(1));
                otu = new OTU(otuName);

                otus.addUniqueElement(otu);

            } else {
                // TODO add ref sequence
            }

            line = reader.readLine();
        }

        reader.close();
    }

    // default to create OTUs from mapping file
    public static void importOTUsAndMappingFromUCFile(File otuMappingUCFile, OTUs otus) throws IOException, IllegalArgumentException {
        importOTUsAndMappingFromUCFile(otuMappingUCFile, otus, true);
    }

    public static void importOTUsAndMappingFromUCFile(File otuMappingUCFile, OTUs otus, boolean canCreateOTU) throws IOException, IllegalArgumentException {
        importOTUsAndMappingFromUCFile(otuMappingUCFile, otus, canCreateOTU, null);
    }

    /**
     * set hits into each OTU, if no OTU is uploaded, then create new OTUs from mapping file
     * e.g. S	17	300	*	*	*	*	*	HA5K40001BTFNL|IndirectSoil|3-H;size=177;	*
     * e.g. H	11	300	98.3	+	0	0	300M	G86XGG201B3O46|IndirectSoil|5-I;size=166;	G86XGG201B3YD9|IndirectSoil|5-N;size=248;
     * @param otuMappingUCFile
     * @param otus
     * @param canCreateOTU     if use otuMappingUCFile to create OTUs
     * @param samples          used only for community to store samples
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    public static void importOTUsAndMappingFromUCFile(File otuMappingUCFile, OTUs otus, boolean canCreateOTU, TreeSet<String> samples) throws IOException, IllegalArgumentException {
        SampleNameParser sampleNameParser = new SampleNameParser();

        BufferedReader reader = getReader(otuMappingUCFile, "OTUs and OTU mapping from");

        String line = reader.readLine();
        while (line != null) {
            // 2 columns: 1st -> read id, 2nd -> otu name
            String[] fields = nameParser.parse(line);

            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            // important: Centroid is excluded from Hit list
            if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.HIT) || fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.Centroid)) {
                String otuName = UCParser.getLabelNoAnnotation(fields[UCParser.Target_Sequence_COLUMN_ID]);
                if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.Centroid))
                    otuName = UCParser.getLabelNoAnnotation(fields[UCParser.Query_Sequence_COLUMN_ID]);

                if (!UCParser.isNA(otuName)) {

                    String hitName = UCParser.getLabelNoAnnotation(fields[UCParser.Query_Sequence_COLUMN_ID]);
                    double identity = UCParser.getIdentity(fields[UCParser.H_Identity_COLUMN_ID]);
                    int size = UCParser.getSize(fields[UCParser.Query_Sequence_COLUMN_ID]);

                    if (otus.containsUniqueElement(otuName)) {
                        OTU otu = (OTU) otus.getUniqueElement(otuName);

                        if (otu == null) {
                            throw new IllegalArgumentException("Error: find an invalid OTU " + otuName +
                                    ", from the mapping file which does not exist in OTUs file !");
                        } else {
                            DereplicatedSequence hit = new DereplicatedSequence(hitName, identity, size);

                            otu.addElement(hit);

                            if (samples != null) {
                                // if by plot, then add plot to TreeSet, otherwise add subplot
                                String sampleType = ((Community) otus).getSampleType();
                                String sampleLocation = sampleNameParser.getSampleBy(sampleType, hitName);
                                samples.add(sampleLocation);
                            }
                        }
                    } else if (canCreateOTU) {
                        OTU otu = new OTU(otuName);
                        DereplicatedSequence hit = new DereplicatedSequence(hitName, identity, size);
                        otu.addElement(hit);
                        otus.addUniqueElement(otu);
                    }
                }
            }

            line = reader.readLine();
        }

        reader.close();

    }

    /**
     * the fast way to create reference instance between OTUs and reference sequences
     * from reference sequence mapping file
     * e.g. H	24428	300	82.7	+	0	0	D155MI144M357I	HCHCI1P01B1RDE|IndirectSoil|LBI-E;size=742;	1194713|Arthropoda|Insecta|Lepidoptera|Lepidoptera|BOLD:AAH9129
     * @param refSeqMappingUCFile
     * @param otus
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void importRefSeqMappingFromUCFile(File refSeqMappingUCFile, OTUs otus) throws IOException, IllegalArgumentException {

        BufferedReader reader = getReader(refSeqMappingUCFile, "reference sequence mapping (to OTU) from");

        String line = reader.readLine();
        while (line != null) {
            // 3 columns: 1st -> identity %, 2nd -> otu name, 3rd -> reference sequence id
            String[] fields = nameParser.parse(line);

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            // important: only Hit or N in this mapping file
            if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.HIT)) {
                String otuName = UCParser.getLabelNoAnnotation(fields[UCParser.Query_Sequence_COLUMN_ID]);
                OTU otu = (OTU) otus.getUniqueElement(otuName);
                if (otu == null) {
                    throw new IllegalArgumentException("Error: find an invalid OTU " + otuName +
                            ", from the mapping file which does not exist in OTUs file !");
                } else {
                    Reference<OTU, String> refSeq = new Reference<>(otu, fields[UCParser.Target_Sequence_COLUMN_ID]);
                    otu.setReference(refSeq);
                }
            }

            line = reader.readLine();
        }

        reader.close();
    }

    //TODO developing: replace OTU to Target
    public static void importDBSearchFromUCFile(File databaseSearchUCFile, OTUs otus) throws IOException, IllegalArgumentException {

        BufferedReader reader = getReader(databaseSearchUCFile, "reference sequence mapping (to OTU) from");

        String line = reader.readLine();
        while (line != null) {
            // 3 columns: 1st -> identity %, 2nd -> otu name, 3rd -> reference sequence id
            String[] fields = nameParser.parse(line);

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            // important: only Hit or N in this mapping file
            if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.HIT)) {
                String otuName = UCParser.getLabelNoAnnotation(fields[UCParser.Query_Sequence_COLUMN_ID]);
                OTU otu = (OTU) otus.getUniqueElement(otuName);
                if (otu == null) {
                    throw new IllegalArgumentException("Error: find an invalid OTU " + otuName +
                            ", from the mapping file which does not exist in OTUs file !");
                } else {
                    Reference<OTU, String> refSeq = new Reference<>(otu, fields[UCParser.Target_Sequence_COLUMN_ID]);
                    otu.setReference(refSeq);
                }
            }

            line = reader.readLine();
        }

        reader.close();
    }

    public static boolean isOTUsFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTUS_RELABELED) && fileName.endsWith(NameSpace.POSTFIX_OTUS);
    }

    public static boolean isOTUMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_MAPPING) && UCParser.isUCFile(fileName);
    }

    public static boolean isReferenceMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_REFERENCE) && UCParser.isUCFile(fileName);
    }

    // TODO is this efficient?
//    public static void importOTUsAndMapping(File otuMappingFile, OTUs otus, List<Sequence> sequences) throws IOException, IllegalArgumentException {
//
//        BufferedReader reader = getReader(otuMappingFile, "OTUs and OTU mapping from");
//
//        OTU otu = null;
//        String line = reader.readLine();
//        while (line != null) {
//            // 2 columns: 1st -> read id, 2nd -> otu name
//            String[] fields = line.split(SampleNameParser.columnSeparator, -1);
//
//            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);
//
//            if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.HIT)) {
//                String otuName = fields[UCParser.Target_Sequence_COLUMN_ID];
//                if (otus.containsOTU(otuName)) {
//                    otu = (OTU) otus.getUniqueElement(otuName);
//                    otu.addUniqueElement(fields[UCParser.Query_Sequence_COLUMN_ID]);
//                } else {
//                    otu = new OTU(otuName);
//                    otu.addUniqueElement(fields[UCParser.Query_Sequence_COLUMN_ID]);
//                    otus.addUniqueElement(otu);
//                }
//            }
//
//            line = reader.readLine();
//        }
//
//        reader.close();
//    }
}
