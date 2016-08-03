package nzgo.toolkit.core.r;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * resemble R data frame, only string type
 *
 * Note: use Java index to start from 0, not R index
 *
 * @author Walter Xie
 */
public class DataFrame<T> {

    // list of columns
    protected List<List<T>> data = new ArrayList<>();
    protected List<String> colNames = new ArrayList<>();
    protected List<String> rowNames = new ArrayList<>();


    public DataFrame() { }

    public DataFrame(final List<String> colNames) {
        this.colNames = colNames;
        for (int i=0; i<colNames.size(); i++)
            this.data.add(new ArrayList<T>());
    }

    public DataFrame(final List<String> colNames, final List<List<T>> data) {
        this(colNames);
        setData(data);
    }

    public int ncol() {
        return data.size();
    }

    public int nrow() {
        if (data.size() == 0) return 0;
        return data.get(0).size();
    }

    public List<String> getColNames() {
        return colNames;
    }

    // Note: use Java index to start from 0
    public String getColName(int col) {
        assert col >= 0 && col < ncol();
        return colNames.get(col);
    }
    public void setColNames(List<String> colNames) {
        this.colNames.clear();
        this.colNames.addAll(colNames);
    }

    public List<String> getRowNames() {
        return rowNames;
    }

    public void setRowNames(List<String> rowNames) {
        this.rowNames.clear();
        this.rowNames.addAll(rowNames);
    }

    public List<List<T>> getData() {
        return data;
    }

    // Note: use Java index to start from 0
    public List<T> getColData(int col) {
        assert col >= 0 && col < ncol();
        return data.get(col);
    }

    // get a list of values in col column in the rows,
    // whose values in colOfValue column equal to the given value
    public List<T> getColDataEqualTo(int col, T value, int colOfValue) {
        List<T> colDataEqualTo = new ArrayList<>();
        List<T> colData = getColData(col);
        List<T> colDataOfValue = getColData(colOfValue);
        for (int i = 0; i < colData.size(); i++)
            if (colDataOfValue.get(i).equals(value))
                colDataEqualTo.add(colData.get(i));

        return colDataEqualTo;
    }

    //TODO generalise to multi-column
    // get a list of values in column 'col' for the row
    // where column 'colOfValue1' and column 'colOfValue2' values
    // both match the 'value1' and 'value2'
    public List<T> getColDataEqualToAnd(int col, T value1, int colOfValue1, T value2, int colOfValue2) {
        List<T> colDataEqualTo = new ArrayList<>();
        List<T> colData = getColData(col);
        List<T> colDataOfValue1 = getColData(colOfValue1);
        List<T> colDataOfValue2 = getColData(colOfValue2);
        for (int i = 0; i < colData.size(); i++)
            if (colDataOfValue1.get(i).equals(value1) && colDataOfValue2.get(i).equals(value2))
                colDataEqualTo.add(colData.get(i));

        return colDataEqualTo;
    }

    // Note: use Java index to start from 0
    public void removeCol(int col) {
        assert col >= 0 && col < ncol() : "Incorrect column index " + col + " !";
        MyLogger.debug("Remove " + col + " column " + getColName(col));
        colNames.remove(col);
        data.remove(col);
    }

    // set data after set col names
    public void setData(List<List<T>> data) {
        assert data.size() == colNames.size();
        this.data.clear();
        this.data.addAll(data);
        // add row names if empty
        if (rowNames.size() < 1)
            setRowNames(StringUtil.getNames("", 0, nrow()));
    }

    public void appendRow(T[] row) {
        assert row.length == colNames.size() : "column names do not match row data length !";
        rowNames.add(Integer.toString(nrow() + 1));
        for (int i = 0; i < ncol(); i++) {
            List<T> colData = data.get(i);
            colData.add(row[i]);
            assert rowNames.size() == colData.size();
        }
    }

    // col == ncol() to append a new column
    public void appendCol(int col, T value) {
        assert col >= 0 && col <= ncol() : "Incorrect column index " + col + " !";
        String colName = "V" + Integer.toString(col);
        if (colNames.contains(colName))
            colName += "_1";
        colNames.add(colName);

        this.data.add(col, new ArrayList<T>());
        List<T> colData = data.get(col);
        for (int i = 0; i < nrow(); i++)
            colData.add(value); // fill default value in this column for all rows

        assert rowNames.size() == colData.size() : "row names do not match column data length !";
    }


    public void replaceCol(int col, List<T> colData) {
        assert col >= 0 && col < ncol();
        assert colData.size() == nrow();

        data.set(col, colData);
    }
}
