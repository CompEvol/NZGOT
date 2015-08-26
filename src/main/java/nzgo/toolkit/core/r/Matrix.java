package nzgo.toolkit.core.r;

import nzgo.toolkit.core.util.MatrixUtil;
import nzgo.toolkit.core.util.StringUtil;

/**
 * resemble R data frame
 * 1st[] is row, 2nd[] is column, data structure is different to DataFrame.java
 * Note: use Java index to start from 0, not R index
 *
 * @author Walter Xie
 */
public class Matrix {

    protected Double[][] data;
    protected String[] colNames;
    protected String[] rowNames;

    public Matrix(int nrow, int ncol) {
        data = new Double[nrow][ncol];
        rowNames = new String[nrow];
        colNames = new String[ncol];
    }

    public Matrix(Double[][] data) {
        this.data = data;
        // set row/col names later
    }

    public int nrow() {
        return data.length;
    }

    public int ncol() {
        return data[0].length;
    }

    public String[] getColNames() {
        return colNames;
    }

    // Note: use Java index to start from 0
    public String getColName(int col) {
        assert col >= 0 && col < ncol();
        return colNames[col];
    }

    public void setColNames(String[] colNames) {
        this.colNames = colNames;
    }

    public String[] getRowNames() {
        return rowNames;
    }

    // Note: use Java index to start from 0
    public String getRowName(int row) {
        assert row >= 0 && row < nrow();
        return rowNames[row];
    }

    public void setRowNames(String[] rowNames) {
        this.rowNames = rowNames;
    }

    public Double[][] getData() {
        return data;
    }

    // add rowData with original data[row]
    public void addRowData(int row, Double[] rowData) {
        assert row >= 0 && row < nrow() && rowData.length == data[row].length;
        for (int i = 0; i < rowData.length; i++)
            data[row][i] += rowData[i];
    }

    public void appendRow(Double[]... rowData) {
        MatrixUtil.appendRow(data, rowData);
    }

    public String[] summary() {
        String[] summary = new String[6];

        summary[0] = "Matrix has " + ncol() + " columns and " + nrow() + " rows.";
        summary[1] = "Data sum = " + MatrixUtil.sum(data) + ".";

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        summary[2] = "Column names (col sums) :";
        Number[] colSums = MatrixUtil.colSums(data);
        for (int i = 0; i < colNames.length; i++) {
            summary[2] += (" " + colNames[i] + " (" + colSums[i] + "), ");
            min = Math.min(min, colSums[i].doubleValue());
            max = Math.min(max, colSums[i].doubleValue());
        }
        summary[2] = StringUtil.replaceLast(summary[2], ", ", ".");
        summary[3] = "Column sums : min = " + min + ", max = " + max + ".";

        int singleton = 0;
        int coupleton = 0;
        summary[4] = "Row names (row sums) : ";
        Number[] rowSums = MatrixUtil.rowSums(data);
        for (int i = 0; i < rowNames.length; i++) {
            summary[4] += (" " + rowNames[i] + " (" + rowSums[i] + "), ");
            if (rowSums[i].doubleValue() == 1)
                singleton++;
            if (rowSums[i].doubleValue() == 2)
                coupleton++;
        }
        summary[4] = StringUtil.replaceLast(summary[4], ", ", ".");
        summary[5] = "Matrix has " + singleton + " singletons, " + coupleton + " coupletons.";

        return summary;
    }
}
