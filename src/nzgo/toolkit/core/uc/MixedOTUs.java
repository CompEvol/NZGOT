package nzgo.toolkit.core.uc;

import nzgo.toolkit.core.io.Importer;
import nzgo.toolkit.core.logger.MyLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mixed Sequence is from different database
 * with the head sequence of its assigned OTU
 * @author Walter Xie
 */
public class MixedOTUs extends UCParser{

    // rows from uc file containing mixed sequences from diff database
    public List<String[]> rowsOfMixedSequences = new ArrayList<>();

    public MixedOTUs(File ucFile) {
        try {
            findMixedOTUs(ucFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        reportMixedSequences();
    }

    /**
     * find rows of uc file having mixed sequences, which contains sequences
     * from the different database of the head sequence.
     * (or the different category)
     * 2 categories are allowed at moment (e.g. BOLD, GOD)
     * @param ucFile
     * @throws java.io.IOException
     */
    public void findMixedOTUs(File ucFile) throws IOException {

        BufferedReader reader = Importer.getReader(ucFile, "uc");

        String line = reader.readLine();
        while (line != null) {
            String[] fields = Importer.nameParser.parse(line); // use same separator in Importer.getReader

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid uc file in the line: " + line);

            if (fields[Record_Type_COLUMN_ID].contentEquals(HIT)) {
                if (!isInSameDatabase(fields[Query_Sequence_COLUMN_ID], fields[Target_Sequence_COLUMN_ID]))
                    rowsOfMixedSequences.add(fields);
            }

            line = reader.readLine();
        }

        reader.close();
    }

    /**
     * get only OTUs
     * @return
     */
    public List<String> getMixedOTUs() {
        List<String> mixedOTUs = new ArrayList<>();
        for (String[] fields : this.rowsOfMixedSequences) {
            if (!mixedOTUs.contains(fields[Target_Sequence_COLUMN_ID]))
                mixedOTUs.add(fields[Target_Sequence_COLUMN_ID]);
        }
        return mixedOTUs;
    }

    public void reportMixedSequences() {
        MyLogger.info("\nFind " + rowsOfMixedSequences.size() + " mixed sequences in " + getMixedOTUs().size() + " OTUs : ");
        for (String[] fields : rowsOfMixedSequences) {
            MyLogger.info(fields[Cluster_Number_COLUMN_ID] + COLUMN_SEPARATOR + fields[Target_Sequence_COLUMN_ID] +
                    COLUMN_SEPARATOR + fields[Query_Sequence_COLUMN_ID] );
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
                    MixedOTUs mixedOTUs = new MixedOTUs(file);
                } else {
                    MyLogger.info("\nIgnore file: " + file);
                }
            }
        }

    }

}
