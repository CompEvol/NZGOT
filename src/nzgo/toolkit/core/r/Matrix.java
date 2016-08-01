package nzgo.toolkit.core.r;

import nzgo.toolkit.core.math.Arithmetic;
import nzgo.toolkit.core.math.Summary;
import nzgo.toolkit.core.util.StringUtil;

import java.util.IllegalFormatException;

/**
 * resemble R data frame
 * 1st[] is row, 2nd[] is column, data structure is different to DataFrame.java
 * Note: use Java index to start from 0, not R index
 *
 * @author Walter Xie
 */
public class Matrix {

    protected double[][] data;
    protected String[] colNames;
    protected String[] rowNames;

    public Matrix(int nrow, int ncol) {
        data = new double[nrow][ncol];
        rowNames = new String[nrow];
        colNames = new String[ncol];
    }

    public Matrix(double[][] data) {
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
        synchronized (rowNames) {
            return colNames[col];
        }
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
        synchronized (rowNames) {
            return rowNames[row];
        }
    }

    public void setRowNames(String[] rowNames) {
        this.rowNames = rowNames;
    }

    public double[][] getData() {
        return data;
    }

    // add rowData with original data[row]
    public void addRowData(int row, double[] rowData) {
        synchronized (data) {
            assert row >= 0 && row < nrow() && rowData.length == data[row].length;
            for (int i = 0; i < rowData.length; i++)
                data[row][i] += rowData[i];
        }
    }

    public void appendRow(double[]... rowData) {
        synchronized (data) {
            MatrixUtil.appendRow(data, rowData);
        }
    }

    public String[] summary() {
        String[] summary = new String[7];

        summary[0] = "Matrix has " + ncol() + " columns and " + nrow() + " rows.";
        summary[1] = "Data sum = " + MatrixUtil.sum(data) + ".";

        double[] colSums = MatrixUtil.colSums(data);
        summary[2] = "Column names (col sums) :";
        for (int i = 0; i < colNames.length; i++) {
            summary[2] += (" " + colNames[i] + " (" + colSums[i] + "), ");
        }
        summary[2] = StringUtil.replaceLast(summary[2], ", ", ".");
        summary[3] = "Column sums : min = " + Arithmetic.min(colSums) + ", max = " + Arithmetic.max(colSums) + ".";

        double[] rowSums = MatrixUtil.rowSums(data);
        summary[4] = "Row names (row sums) : ";
        for (int i = 0; i < rowNames.length; i++) {
            summary[4] += (" " + rowNames[i] + " (" + rowSums[i] + "), ");
        }
        summary[4] = StringUtil.replaceLast(summary[4], ", ", ".");
        summary[5] = "Column sums : min = " + Arithmetic.min(rowSums) + ", max = " + Arithmetic.max(rowSums) + ".";

        summary[6] = "Matrix has " + Summary.singleton(rowSums) + " singletons, " + Summary.coupleton(rowSums) + " coupletons.";

        return summary;
    }

    public void validate() {
        if (colNames.length != ncol())
            throw new IllegalArgumentException("Column names do not match the number of columns !");
        if (rowNames.length != nrow())
            throw new IllegalArgumentException("Row names do not match the number of rows !");

        double[] rowSums = MatrixUtil.rowSums(data);
        for (int i = 0; i < rowSums.length; i++) {
            if (rowSums[i] == 0)
                throw new IllegalArgumentException("Row " + i + " " + rowNames[i] + " is empty !");
        }

        double[] colSums = MatrixUtil.colSums(data);
        for (int i = 0; i < colSums.length; i++) {
            if (colSums[i] == 0)
                throw new IllegalArgumentException("Column " + i + " " + colNames[i] + " is empty !");
        }
    }
}
