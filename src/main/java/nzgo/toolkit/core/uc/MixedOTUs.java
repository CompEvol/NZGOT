package nzgo.toolkit.core.uc;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.Matcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Mixed OTUs
 * contain any sequence belongs to a different type to the head sequence
 * such as different databases (BOLD, GOD)
 * @author Walter Xie
 */
public class MixedOTUs extends UCParser{
    // TODO: use a list of matchers for more than 2 types
    protected final Matcher matcher;

    // rows from uc file containing mixed sequences from diff database
    protected List<String[]> sequencesInMixedOTUs = new ArrayList<>();

    public MixedOTUs(Path ucFile, String regex) {
        matcher = new Matcher(regex);

        try {
            setMixedOTUs(ucFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * find rows of uc file where sequences belong to mixed OTUs,
     * from the different database of the head sequence.
     * (or the different category)
     * 2 categories are allowed at moment (e.g. BOLD, GOD)
     * @param ucFile
     * @throws IOException
     */
    public void setMixedOTUs(Path ucFile) throws IOException {
        BufferedReader reader = FileIO.getReader(ucFile, "uc");

        String line = reader.readLine();
        while (line != null) {
            String[] fields = FileIO.lineParser.getSeparator(0).parse(line); // use same separator in FileIO.getReader

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid uc file in the line: " + line);

            if (fields[Record_Type_COLUMN_ID].contentEquals(HIT)) {
                if (!isSameType(fields[Query_Sequence_COLUMN_ID], fields[Target_Sequence_COLUMN_ID]))
                    sequencesInMixedOTUs.add(fields);
            }

            line = reader.readLine();
        }

        reader.close();
    }

    /**
     * get OTUs (head sequences) only
     * @return
     */
    public List<String> getMixedOTUs() {
        List<String> mixedOTUs = new ArrayList<>();
        for (String[] fields : this.sequencesInMixedOTUs) {
            if (!mixedOTUs.contains(fields[Target_Sequence_COLUMN_ID]))
                mixedOTUs.add(fields[Target_Sequence_COLUMN_ID]);
        }
        return mixedOTUs;
    }

    public void writeMixedOTUs(Path outFile) throws IOException {
        BufferedWriter writer = FileIO.getWriter(outFile, "mixed OTUs");

//        writer.write("# \n");
        for (String mixedOTU : getMixedOTUs()) {
            writer.write(mixedOTU + "\tmixed\n");
        }

        writer.flush();
        writer.close();
    }

    public void reportMixedOTUs() {
        MyLogger.info("\nFind " + sequencesInMixedOTUs.size() + " mixed sequences in " + getMixedOTUs().size() + " OTUs : ");
        for (String[] fields : sequencesInMixedOTUs) {
            MyLogger.info( lineSeparator.getLabel(fields[Cluster_Number_COLUMN_ID],
                    fields[Target_Sequence_COLUMN_ID], fields[Query_Sequence_COLUMN_ID]) );
        }
    }

    /**
     * identify whether 2 sequences are in the same type,
     * given the regular expression to match one of two types
     * such as from same database (BOLD, GOD)
     * @param label1
     * @param label2
     * @return
     */
    public boolean isSameType(String label1, String label2) {
        return matcher.isMatched(label1) == matcher.isMatched(label2);
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
                    MixedOTUs mixedOTUs = new MixedOTUs(file.toPath(), ".*NZAC.*");
                    mixedOTUs.reportMixedOTUs();
                } else {
                    MyLogger.info("\nIgnore file: " + file);
                }
            }
        }

    }

}
