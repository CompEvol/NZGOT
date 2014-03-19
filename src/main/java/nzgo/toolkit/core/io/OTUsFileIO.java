package nzgo.toolkit.core.io;

import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.community.Reference;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.SampleNameParser;
import nzgo.toolkit.core.uc.UCParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;

/**
 * OTUs FileIO
 * attempt to store reads as Sequence
 * both OTU and reference mapping file are uc format.
 * @author Walter Xie
 */
public class OTUsFileIO extends FileIO {

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
    public static TreeSet<String> importOTUsAndMappingFromUCFile(File otuMappingUCFile, OTUs otus) throws IOException, IllegalArgumentException {
        return importOTUsAndMappingFromUCFile(otuMappingUCFile, otus, true);
    }

    /**
     * Ideally otuMappingUCFile should have all OTUs,
     * so that the validation assumed to be done before this method
     * @param otuMappingUCFile
     * @param otus
     * @param canCreateOTU
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static TreeSet<String> importOTUsAndMappingFromUCFile(File otuMappingUCFile, OTUs otus, boolean canCreateOTU) throws IOException, IllegalArgumentException {
        SampleNameParser sampleNameParser = new SampleNameParser();
        return importOTUsAndMappingFromUCFile(otuMappingUCFile, otus, canCreateOTU, sampleNameParser);
    }

    /**
     * 1st set sampleType in sampleNameParser, default to BY_PLOT
     * 2nd load reads into each OTU, and parse label to get sample array
     * 3rd set sample array, and calculate Alpha diversity for each OTU
     *
     * set hits into each OTU, if no OTU is uploaded, then create new OTUs from mapping file
     * e.g. S	17	300	*	*	*	*	*	HA5K40001BTFNL|IndirectSoil|3-H;size=177;	*
     * e.g. H	11	300	98.3	+	0	0	300M	G86XGG201B3O46|IndirectSoil|5-I;size=166;	G86XGG201B3YD9|IndirectSoil|5-N;size=248;
     * @param otuMappingUCFile
     * @param otus
     * @param canCreateOTU              if use otuMappingUCFile to create OTUs
     * @param sampleNameParser          used only for community, if null then ignore samples
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    public static TreeSet<String> importOTUsAndMappingFromUCFile(File otuMappingUCFile, OTUs otus, boolean canCreateOTU, SampleNameParser sampleNameParser) throws IOException, IllegalArgumentException {
        TreeSet<String> samples = null;

        BufferedReader reader = getReader(otuMappingUCFile, "OTUs and OTU mapping from");
        int total = 0;
        String line = reader.readLine();
        while (line != null) {
            // 2 columns: 1st -> read id, 2nd -> otu name
            String[] fields = lineParser.getSeparator(0).parse(line);

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
//                            DereplicatedSequence hit = new DereplicatedSequence(hitName, identity, size);
                            // TODO bug to use DereplicatedSequence, may move to a new input, check if affect ER
                            otu.add(hitName);

                            if (sampleNameParser != null) {
                                if (samples == null) {
                                    samples = new TreeSet<>();

                                    MyLogger.info("\nSample type: " + sampleNameParser.sampleType);
                                }

                                // if by plot, then add plot to TreeSet, otherwise add subplot
                                String sampleLocation = sampleNameParser.getSample(hitName);
                                samples.add(sampleLocation);
                            }
                        }
                    } else if (canCreateOTU) {
                        OTU otu = new OTU(otuName);
//                        DereplicatedSequence hit = new DereplicatedSequence(hitName, identity, size);
                        otu.addElement(hitName);
                        otus.addUniqueElement(otu);
                    }

                    total++;
                }
            }

            line = reader.readLine();
        }

        reader.close();

        MyLogger.debug("total = " + total);

        return samples;
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
            String[] fields = lineParser.getSeparator(0).parse(line);

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
            String[] fields = lineParser.getSeparator(0).parse(line);

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

    /**
     * this method only works for specific directory structure
     */
    public static void reportOTUs (Path workDir, String otuMappingFileName, String reportFileName, String cmFileName, String[] experiments, int[] thresholds) throws IOException {
        if (!UCParser.isUCFile(otuMappingFileName))
            throw new IllegalArgumentException("Invalid OTU mapping file name : " + otuMappingFileName);

        for (String experiment : experiments) {
            // go into each gene folder
            Path workPath = Paths.get(workDir.toString(), experiment);
            MyLogger.info("\nWorking path = " + workPath);

            Path outFile = Paths.get(workPath.toString(), experiment + reportFileName);
            BufferedWriter writer = FileIO.getWriter(outFile, "OTUs summary report at " + experiment);

            writer.write("Threshold\tOTUs\tReads\tOTUs1Read\tOTUs2Reads");

            for (int thre : thresholds) {
                Path otusPath = Paths.get(workPath.toString(), "otus" + thre);

                File otuMappingFile = Paths.get(otusPath.toString(), otuMappingFileName).toFile();
                SampleNameParser sampleNameParser = new SampleNameParser();
                Community community = new Community(sampleNameParser, otuMappingFile);

                Path outCMFilePath = Paths.get(otusPath.toString(), experiment + "_" + thre + cmFileName);
                int[] report = CommunityFileIO.writeCommunityMatrix(outCMFilePath, community);
                writer.write(thre + "\t" + report[0] + "\t" + report[1] + "\t" + report[2] + "\t" + report[3]);
            }

            writer.close();
        }
    }



    public static boolean isOTUsFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTUS_RELABELED) && fileName.endsWith(NameSpace.SUFFIX_OTUS);
    }

    public static boolean isOTUMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_MAPPING) && UCParser.isUCFile(fileName);
    }

    public static boolean isReferenceMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_REFERENCE) && UCParser.isUCFile(fileName);
    }


    //Main method
    public static void main(final String[] args) {

        String[] experiments = new String[]{"CO1-soilkit","CO1-indirect","ITS","trnL","16S"}; //"CO1-soilkit","CO1-indirect","ITS","trnL","16S"
        int[] thresholds = new int[]{90,91,92,93,94,95,96,97,98,99,100}; // 90,91,92,93,94,95,96,97,98,99,100
        Path workDir = Paths.get("/Users/dxie004/Documents/ModelEcoSystem/454/2010-pilot/WalterPipeline/");
        String otuMappingFileName = "map.uc";
        String reportFileName = "_otus_report.tsv";
        String cmFileName = "_cm.csv";

        try {
            reportOTUs (workDir, otuMappingFileName, reportFileName, cmFileName, experiments, thresholds);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
