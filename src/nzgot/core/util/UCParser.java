package nzgot.core.util;

import nzgot.core.io.Importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * USEARCH cluster format (UC) is a tab-separated text file
 * http://www.drive5.com/usearch/manual/ucout.html
 * @author Walter Xie
 */
public class UCParser {

    public static final String POSTFIX_UC = ".uc";

    public static final String HIT = "H";
    public static final String Centroid = "S";
    public static final String Cluster_Record = "C";
    public static final String NO_HIT = "N";

    public static final int Record_Type_COLUMN_ID = 0;
    public static final int Cluster_Number_COLUMN_ID = 1;
    public static final int H_Identity_COLUMN_ID = 3;
    public static final int Query_Sequence_COLUMN_ID = 8;
    public static final int Target_Sequence_COLUMN_ID = 9;

    public static final String COLUMN_SEPARATOR = "\t";

    public List<String[]> driftingSequences = new ArrayList<>();

    public UCParser(File ucFile) {
        try {
            findDriftingSequences(ucFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findDriftingSequences(File ucFile) throws IOException {

        BufferedReader reader = Importer.getReader(ucFile, "uc");

        String line = reader.readLine();
        while (line != null) {
            String[] fields = line.split(COLUMN_SEPARATOR, -1);

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid uc file in the line: " + line);

            if (fields[Record_Type_COLUMN_ID].contentEquals(HIT)) {
                if (!isInSameDatabase(fields[Query_Sequence_COLUMN_ID], fields[Target_Sequence_COLUMN_ID]))
                    driftingSequences.add(fields);
            }

            line = reader.readLine();
        }

        reader.close();
    }

    public List<String> getDriftingOTUs() {
        List<String> driftingOTUs = new ArrayList<>();
        for (String[] fields : driftingSequences) {
            if (!driftingOTUs.contains(fields[Target_Sequence_COLUMN_ID]))
                driftingOTUs.add(fields[Target_Sequence_COLUMN_ID]);
        }
        return driftingOTUs;
    }

    public void reportDriftingSequences() {
        System.out.println("\nFind " + driftingSequences.size() + " drifting sequences : ");
        for (String[] fields : driftingSequences) {
            System.out.println(fields[Cluster_Number_COLUMN_ID] + COLUMN_SEPARATOR +
                    fields[Query_Sequence_COLUMN_ID] + COLUMN_SEPARATOR + fields[Target_Sequence_COLUMN_ID]);
        }
    }

    /**
     * hard code to identify whether 2 sequences are from the same database (BOLD, GOD)
     * check if 1st char of sequence label is integer or string
     * BOLD sequence label starts string
     * GOD sequence label starts integer
     * @param label1
     * @param label2
     * @return
     */
    public static boolean isInSameDatabase(String label1, String label2) {
        char c1 = label1.charAt(0);
        char c2 = label2.charAt(0);

        return Character.isDigit(c1) == Character.isDigit(c2);
    }

    public static boolean isUCFile(String fileName) {
        return fileName.endsWith(POSTFIX_UC);
    }


    //Main method
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        System.out.println("\nWorking path = " + workPath);

        File folder = new File(workPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile()) {
                String fileName = file.getName();
                if (isUCFile(fileName)) {
                    UCParser ucParser = new UCParser(file);
                    ucParser.reportDriftingSequences();
                } else {
                    System.out.println("\nIgnore file: " + file);
                }
            }
        }

    }

}
