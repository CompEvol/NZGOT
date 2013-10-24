package nzgot.cma.io;

import nzgot.cma.Community;
import nzgot.cma.OTU;
import nzgot.cma.util.NameSpace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Community Matrix Importer
 * @author Walter Xie
 */
public class CMImporter {

    public static void importOTUs (File otusFile, Community community) throws IOException, IllegalArgumentException {
        BufferedReader reader = new BufferedReader(new FileReader(otusFile));

        System.out.println("\nImport OTUs from file: " + otusFile);

        OTU otu = null;
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
//                line.replaceAll("size=", "");
                // the current label only contains otu name
                String otuName = line.substring(1);
                otu = new OTU(otuName);

                community.addUniqueElement(otu);

            } else {
                // TODO add ref sequence
            }

            line = reader.readLine();
        }

        reader.close();
    }

    public static void importOTUMapping (File otuMappingFile, Community community) throws IOException, IllegalArgumentException {
        int indexRead = 0;
        int indexOTUName = 1;

        BufferedReader reader = new BufferedReader(new FileReader(otuMappingFile));

        System.out.println("\nImport OTU mapping (to reads) file: " + otuMappingFile);

        String line = reader.readLine();
        while (line != null) {
            // 2 columns: 1st -> read id, 2nd -> otu name
            String[] fields = line.split("\t", -1);

            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            OTU otu = (OTU) community.getUniqueElement(fields[indexOTUName]);
            if (otu == null) {
                throw new IllegalArgumentException("Error: find an invalid OTU " + fields[1] +
                        ", from the mapping file which does not exist in OTUs file !");
            } else {
                otu.addUniqueElement(fields[indexRead]);
            }

            line = reader.readLine();
        }

        reader.close();
    }

    public static void importReferenceMappingFile (File referenceMappingFile, Community community) throws IOException, IllegalArgumentException {
        int indexIdentity = 0;
        int indexOTUName = 1;
        int indexRefSeq = 2;

        BufferedReader reader = new BufferedReader(new FileReader(referenceMappingFile));

        System.out.println("\nImport reference sequence mapping (to OTU) file: " + referenceMappingFile);

        String line = reader.readLine();
        while (line != null) {
            // 3 columns: 1st -> identity %, 2nd -> otu name, 3rd -> reference sequence id
            String[] fields = line.split("\t", -1);

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            OTU otu = (OTU) community.getUniqueElement(fields[indexOTUName]);
            if (otu == null) {
                throw new IllegalArgumentException("Error: find an invalid OTU " + fields[1] +
                        ", from the mapping file which does not exist in OTUs file !");
            } else {
                otu.setRefSeqId(fields[indexRefSeq]);
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
