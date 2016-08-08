package nzgo.toolkit.core.r;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    protected List<String> colNames = new ArrayList<>(); //TODO unique?
    protected List<String> rowNames = new ArrayList<>();


    public DataFrame() { }

    public DataFrame(final List<String> colNames) {
        this.colNames = colNames;
        for (int i=0; i<colNames.size(); i++)
            this.data.add(new ArrayList<T>());
    }

    public DataFrame(final Set<String> colNames) {
        this.colNames.addAll(colNames);
        for (int i=0; i<colNames.size(); i++)
            this.data.add(new ArrayList<T>());
    }

    public DataFrame(final List<String> colNames, final List<List<T>> data) {
        this(colNames);
        setData(data);
    }

    public DataFrame(final Set<String> colNames, final List<List<T>> data) {
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

    public void setColName(int col, String newName) {
        assert col >= 0 && col < ncol();
        colNames.set(col, newName);
    }

    public int getColId(String colName) {
        return colNames.indexOf(colName);
    }

    public List<String> getRowNames() {
        return rowNames;
    }

    public void setRowNames(List<String> rowNames) {
        this.rowNames.clear();
        this.rowNames.addAll(rowNames);
    }

    public void setRowName(int row, String newName) {
        assert row >= 0 && row < ncol();
        rowNames.set(row, newName);
    }

    public int getRowId(String rowName) {
        return rowNames.indexOf(rowName);
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

    public void setData(int row, int col, T value) {
        assert col >= 0 && col < ncol() : "Incorrect column index " + col + " !";
        assert row >= 0 && row < nrow() : "Incorrect row index " + row + " !";
        List<T> colData = data.get(col);
        colData.set(row, value);
    }

    public T getData(int row, int col) {
        assert col >= 0 && col < ncol() : "Incorrect column index " + col + " !";
        assert row >= 0 && row < nrow() : "Incorrect row index " + row + " !";
        List<T> colData = data.get(col);
        return colData.get(row);
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

    public void appendRow(String newName, T value) {
        if (rowNames.contains(newName))
            throw new IllegalArgumentException("Row name exist ! " + newName);
        rowNames.add(newName);

        for (int i = 0; i < ncol(); i++) {
            List<T> colData = data.get(i);
            colData.add(value); // fill default value in this row for all columns
            assert rowNames.size() == colData.size() : "row names do not match column data length !";
        }
    }

    // col == ncol() to append a new column
    public void appendCol(String newName, T value) {
        if (colNames.contains(newName))
            newName += "_1";
        colNames.add(newName);

        this.data.add(new ArrayList<T>());
        List<T> colData = data.get(ncol());
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
