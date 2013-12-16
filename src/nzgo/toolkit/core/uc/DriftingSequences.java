package nzgo.toolkit.core.uc;

import nzgo.toolkit.core.io.Importer;
import nzgo.toolkit.core.logger.MyLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Drifting Sequence is from different database
 * with the head sequence of its assigned OTU
 * @author Walter Xie
 */
public class DriftingSequences extends UCParser{

    public List<String[]> driftingSequences = new ArrayList<>();

    public DriftingSequences(File ucFile) {
        try {
            findDriftingSequences(ucFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * find the sequences assigned to a OTU are from the different
     * database of the head sequence.
     * (or the different category)
     * 2 categories are allowed at moment (e.g. BOLD, GOD)
     * @param ucFile
     * @throws java.io.IOException
     */
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
        MyLogger.info("\nFind " + driftingSequences.size() + " drifting sequences : ");
        for (String[] fields : driftingSequences) {
            MyLogger.info(fields[Cluster_Number_COLUMN_ID] + COLUMN_SEPARATOR +
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

    //Main method
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        File folder = new File(workPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile()) {
                String fileName = file.getName();
                if (isUCFile(fileName)) {
                    DriftingSequences driftingSequences = new DriftingSequences(file);
                    driftingSequences.reportDriftingSequences();
                } else {
                    MyLogger.info("\nIgnore file: " + file);
                }
            }
        }

    }

}
