package nzgot.core.community.io;

import jebl.evolution.sequences.Sequence;
import nzgot.core.community.OTU;
import nzgot.core.community.OTUs;
import nzgot.core.community.util.NameParser;
import nzgot.core.community.util.NameSpace;
import nzgot.core.io.Importer;
import nzgot.core.uc.UCParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * OTUs Importer
 * attempt to store reads as Sequence
 * both OTU and reference mapping file are uc format.
 * @author Walter Xie
 */
public class OTUsImporter extends Importer {

//    // column index in OTU mapping file
//    public static int OTU_MAPPING_INDEX_READ = 0;
//    public static int OTU_MAPPING_INDEX_OTU_NAME = 1;
//    // column index in reference sequence mapping file
//    public static int REF_SEQ_MAPPING_INDEX_IDENTITY = 0;
//    public static int REF_SEQ_MAPPING_INDEX_OTU_NAME = 1;
//    public static int REF_SEQ_MAPPING_INDEX_REF_SEQ = 2;

    // TODO is this efficient?
    public static void importOTUsAndMapping(File otuMappingFile, OTUs otus, List<Sequence> sequences) throws IOException, IllegalArgumentException {

        BufferedReader reader = getReader(otuMappingFile, "OTUs and OTU mapping from");

        OTU otu = null;
        String line = reader.readLine();
        while (line != null) {
            // 2 columns: 1st -> read id, 2nd -> otu name
            String[] fields = line.split(NameParser.COLUMN_SEPARATOR, -1);

            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.HIT)) {
                String otuName = fields[UCParser.Target_Sequence_COLUMN_ID];
                if (otus.containsOTU(otuName)) {
                    otu = (OTU) otus.getUniqueElement(otuName);
                    otu.addUniqueElement(fields[UCParser.Query_Sequence_COLUMN_ID]);
                } else {
                    otu = new OTU(otuName);
                    otu.addUniqueElement(fields[UCParser.Query_Sequence_COLUMN_ID]);
                    otus.addUniqueElement(otu);
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
}
