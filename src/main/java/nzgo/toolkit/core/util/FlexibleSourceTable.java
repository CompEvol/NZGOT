package nzgo.toolkit.core.util;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * each column is list, at least 1 column
 *
 * @author Walter Xie
 */
public class FlexibleSourceTable {

    protected String[] heads;
    protected List[] columns;

    public FlexibleSourceTable(String... heads) {
        int numCol = heads.length;
        assert numCol > 0;

        this.heads = heads;
        columns = new List[numCol]; // make for loop valid
        for (int l = 0; l < getTotalColumns(); l++) {
            columns[l] = new ArrayList<String>();
        }
    }

    public void addValue(String... values) {
        assert values.length == getTotalColumns();
        for (int l = 0; l < getTotalColumns(); l++) {
            columns[l].add(values[l]);
        }
    }

    public void addValueToColumn(String value, String head) {
        int col = getColumn(head);
        addValueToColumn(value, col);
    }

    public void addValueToColumn(String value, int col) {
        if (col < 0 || col >= getTotalColumns())
            throw  new IllegalArgumentException("Invalid column index " + col + " not in range [0, " + getTotalColumns() + ")");
        columns[col].add(value);
    }

    public void replaceValue(String value, String head, int row) {
        int col = getColumn(head);
        replaceValue(value, col, row);
    }

    public void replaceValue(String value, int col, int row) {
        if (col < 0 || col >= getTotalColumns())
            throw  new IllegalArgumentException("Invalid column index " + col + " not in range [0, " + getTotalColumns() + ")");
        if (row < 0 || row >= getTotalRows())
            throw  new IllegalArgumentException("Invalid row index " + row + " not in range [0, " + getTotalRows() + ")");
        columns[col].set(row, value);
    }

    public int getTotalColumns() {
        return columns.length;
    }

    public int getTotalRows() {
        for (int l = 1; l < getTotalColumns(); l++) {
            assert columns[0].size() == columns[l].size();
        }
        return columns[0].size();
    }

    public int getFirstRow(String value, String head) {
        int col = getColumn(head);
        return getFirstRow(value, col);
    }

    public int getFirstRow(String value, int col) {
        if (col < 0 || col >= getTotalColumns())
            throw new IllegalArgumentException("Invalid column index " + col + " not in range [0, " + getTotalColumns() + ")");
        return columns[col].indexOf(value);
    }

    protected int getColumn(String head) {
        return ArrayUtil.indexOf(head, heads);
    }

    /**
     * heads are in the 1st row
     * @param outPath
     * @throws IOException
     */
    public void outputTable(Path outPath) throws IOException {
        PrintStream out = FileIO.getPrintStream(outPath, "Source Table");

        //head
        out.println(ArrayUtil.toString("\t", heads));

        for (int i = 0; i < getTotalRows(); i++) {
            for (int l = 0; l < getTotalColumns(); l++) {
                if (l > 0)
                    out.print("\t");
                out.print(columns[l].get(i));
            }
            out.print("\n");
        }

        out.flush();
        out.close();

        MyLogger.info("\nTable has total " + getTotalColumns() + " columns, and " + getTotalRows() + " rows.");
    }

    /**
     * heads are in the 1st column
     * @param outPath
     * @throws IOException
     */
    public void outputTableTranspose(Path outPath) throws IOException {
        PrintStream out = FileIO.getPrintStream(outPath, "Transposed Source Table");

        for (int l = 0; l < getTotalColumns(); l++) {
            //head
            out.print(heads[l]);
            for (int i = 0; i < getTotalRows(); i++) {
                out.print("\t");
                out.print(columns[l].get(i));
            }
            out.print("\n");
        }

        out.flush();
        out.close();

        MyLogger.info("\nTable has total " + getTotalColumns() + " columns, and " + getTotalRows() + " rows.");
    }


    // default "\t"
    public static void createFlexibleSourceTable(Path inPath) {
        createFlexibleSourceTable(inPath, "\t");
    }


    /**
     * create FlexibleSourceTable from a file
     * @param inPath
     */
    public static void createFlexibleSourceTable(Path inPath, String delimiter) {
        final Separator separator = new Separator(delimiter);
        try {
            BufferedReader reader = OTUsFileIO.getReader(inPath, "");

            String line = reader.readLine();
            while (line != null) {
                String[] items = separator.parse(line);

                throw new UnsupportedOperationException("TODO");

//                line = reader.readLine();
            }


            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
