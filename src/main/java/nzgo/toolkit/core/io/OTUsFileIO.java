package nzgo.toolkit.core.io;

import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.community.Reference;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.AssemblerUtil;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.uc.UCParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * OTUs FileIO: OTUs are fasta file, and both OTU mapping and reference mapping files are uc format
 * attempt to store reads as Sequence
 *
 * Be careful to use size annotation as OTU size,
 * because it is less than the actual size get from OTU mapping file.
 * More reads may be mapped to OTUs during usearch_global, which are dereplicate sequences.
 *
 * @author Walter Xie
 */
public class OTUsFileIO extends FileIO {


    /**
     * no site information
     * @param otus
     * @param otuMappingUCFile
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void importOTUsFromMapUC(OTUs otus, File otuMappingUCFile) throws IOException, IllegalArgumentException {
        CommunityFileIO.importCommunityFromMapUC(otus, otuMappingUCFile, null);
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
    public static void importRefSeqFromMapUCAndMapToOTUs(File refSeqMappingUCFile, OTUs otus) throws IOException, IllegalArgumentException {
        UCParser.validateUCFile(refSeqMappingUCFile.getName());

        BufferedReader reader = getReader(refSeqMappingUCFile, "reference sequence mapping (to OTU) from");

        String line = reader.readLine();
        while (line != null) {
            // 3 columns: 1st -> identity %, 2nd -> otu name, 3rd -> reference sequence id
            String[] fields = lineParser.getSeparator(0).parse(line);

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            // important: only Hit or N in this mapping file
            if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.HIT)) {
                String otuName = UCParser.getLabelNoSizeAnnotation(fields[UCParser.Query_Sequence_COLUMN_ID]);
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
                String otuName = UCParser.getLabelNoSizeAnnotation(fields[UCParser.Query_Sequence_COLUMN_ID]);
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
     * this method only works for specific directory structure described in WalterPipeline.txt
     * @param workDir
     * @param otuMappingFileName
     * @param reportFileName
     * @param cmFileName
     * @param experiments
     * @param thresholds
     * @param thresholdWhoseOTUsToAppendSize
     * @throws IOException
     */
    public static void reportOTUs(Path workDir, String otuMappingFileName, String reportFileName, String cmFileName,
                                  String[] experiments, int[] thresholds, int thresholdWhoseOTUsToAppendSize) throws IOException {

        UCParser.validateUCFile(otuMappingFileName);

        for (String experiment : experiments) {
            // go into each gene folder
            Path workPath = Paths.get(workDir.toString(), experiment);
            MyLogger.info("\nWorking path = " + workPath);

            Path outFile = Paths.get(workPath.toString(), experiment + reportFileName);
            BufferedWriter writer = FileIO.getWriter(outFile, "OTUs summary report at " + experiment);

            writer.write("Threshold\tOTUs\tReads\tOTUs1Read\tOTUs2Reads\n");

            for (int thre : thresholds) {
                Path otusPath = Paths.get(workPath.toString(), "otus" + thre);

                File otuMappingFile = Paths.get(otusPath.toString(), otuMappingFileName).toFile();
                SiteNameParser siteNameParser = new SiteNameParser();
                Community community = new Community(otuMappingFile, siteNameParser);

                Path outCMFilePath = Paths.get(otusPath.toString(), experiment + "_" + thre + cmFileName);
                int[] report = CommunityFileIO.writeCommunityMatrix(outCMFilePath, community);
                writer.write(thre + "\t" + report[0] + "\t" + report[1] + "\t" + report[2] + "\t" + report[3] + "\n");

                if (thresholdWhoseOTUsToAppendSize == thre) { // 97
                    Path otusFile = Paths.get(otusPath.toString(), "otus.fasta");
                    AssemblerUtil.removeAnnotationAppendSizeToLabel(otusFile, community);
                }
            }

            writer.close();
        }
    }

    @Deprecated
    public static boolean isOTUsFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTUS_RELABELED) && fileName.endsWith(NameSpace.SUFFIX_OTUS);
    }
    @Deprecated
    public static boolean isOTUMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_MAPPING) && UCParser.isUCFile(fileName);
    }
    @Deprecated
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
//            String[] fields = line.split(SiteNameParser.columnSeparator, -1);
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
