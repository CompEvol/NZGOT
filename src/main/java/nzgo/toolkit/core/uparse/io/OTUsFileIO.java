package nzgo.toolkit.core.uparse.io;

import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.community.Reference;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.DereplicatedSequence;
import nzgo.toolkit.core.uparse.Parser;
import nzgo.toolkit.core.uparse.UCParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

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
        CommunityFileIO.importCommunityFromUCFile(otus, otuMappingUCFile, null);
    }

    /**
     * the fast way to create reference instance between OTUs and reference sequences
     * from reference sequence mapping file
     * e.g. H	24428	300	82.7	+	0	0	D155MI144M357I	HCHCI1P01B1RDE|IndirectSoil|LBI-E;size=742;	1194713|Arthropoda|Insecta|Lepidoptera|Lepidoptera|BOLD:AAH9129
     * @param otus
     * @param refSeqMappingUCFile
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void importRefSeqFromMapUCAndMapToOTUs(OTUs otus, File refSeqMappingUCFile) throws IOException, IllegalArgumentException {
        NameUtil.validateFileExtension(refSeqMappingUCFile.getName(), NameSpace.SUFFIX_UC);

        BufferedReader reader = getReader(refSeqMappingUCFile, "reference sequence mapping (to OTU) from");

        String line = reader.readLine();
        while (line != null) {
            // 3 columns: 1st -> identity %, 2nd -> otu name, 3rd -> reference sequence id
            String[] fields = lineParser.getSeparator(0).parse(line);

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            // important: only Hit or N in this mapping file
            if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.HIT)) {
                String otuName = Parser.getLabelNoSizeAnnotation(fields[UCParser.Query_Sequence_COLUMN_ID]);
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

    // only use to validate UPARSE pipeline OTUs map
    public static void importOTUsFromFasta (OTUs otus, File otusFile, boolean importSequence, boolean countSizeAnnotation) throws IOException, IllegalArgumentException {
        Module.validateFileName(otusFile.getName(), "OTUs", NameSpace.SUFFIX_FASTA);

        BufferedReader reader = getReader(otusFile, "OTUs from");

        otus.setCountSizeAnnotation(countSizeAnnotation);

        int totalAnnotatedSize = 0;
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                String label = line.substring(1);
                int annotatedSize = Parser.getAnnotatedSize(label);

                String otuName = Parser.getLabelNoSizeAnnotation(label);
                DereplicatedSequence representative = new DereplicatedSequence(otuName);
                if (countSizeAnnotation)
                    representative.setAnnotatedSize(annotatedSize);
                otus.addElement(representative);

                totalAnnotatedSize+=annotatedSize;

            } else if (importSequence) {
                // TODO add sequence
            }

            line = reader.readLine();
        }

        reader.close();

        MyLogger.info("\nImport " + otus.size() + " OTUs, ");
        if (totalAnnotatedSize > 0) MyLogger.info("Total annotated size = " + totalAnnotatedSize);
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
                String otuName = Parser.getLabelNoSizeAnnotation(fields[UCParser.Query_Sequence_COLUMN_ID]);
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

    @Deprecated
    public static boolean isOTUsFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTUS_RELABELED) && fileName.endsWith(NameSpace.SUFFIX_OTUS);
    }
    @Deprecated
    public static boolean isOTUMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_MAPPING) && NameUtil.hasFileExtension(fileName, NameSpace.SUFFIX_UC);
    }
    @Deprecated
    public static boolean isReferenceMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_REFERENCE) && NameUtil.hasFileExtension(fileName, NameSpace.SUFFIX_UC);
    }

    //Main method
    public static void main(final String[] args) {

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
