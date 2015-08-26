package nzgo.toolkit.core.r;

import nzgo.toolkit.core.math.Arithmetic;
import nzgo.toolkit.core.util.NumberUtil;

import java.util.Arrays;

/**
 * MatrixUtil
 * @author Walter Xie
 */
public class MatrixUtil {

    public static double[][] appendRow(double[][] main, double[]... row) {
        double[][] result = Arrays.copyOf(main, main.length+row.length);
        for (int i = 0; i < row.length; i++) {
            assert row[i].length == result[main.length+i].length;
            result[main.length+i] = Arrays.copyOf(row[i], result[main.length + i].length);
        }
        return result;
    }

    // sum by rows, 1st[] is row, 2nd[] is column
    public static double[] rowSums(double[][] matrix) {
        double[] rowSums = new double[matrix.length];
        for (int r = 0; r < matrix.length; r++) {
            rowSums[r] = Arithmetic.sum(matrix[r]);
        }
        return rowSums;
    }

    // sum by columns, 1st[] is row, 2nd[] is column
    public static double[] colSums(double[][] matrix) {
        double[] colSums = new double[matrix[0].length];
        for (int c = 0; c < matrix[0].length; c++) {
            colSums[c] = 0;
            for (int r = 0; r < matrix.length; r++)
                colSums[c] += matrix[r][c];
        }
        return colSums;
    }

    public static double sum(double[][] matrix) {
        return Arithmetic.sum(rowSums(matrix));
    }

    //++++++++++ generic ++++++++++

    public static <T> T[][] appendRow(T[][] main, T[]... row) {
        T[][] result = Arrays.copyOf(main, main.length+row.length);
        for (int i = 0; i < row.length; i++) {
            assert row[i].length == result[main.length+i].length;
            result[main.length+i] = Arrays.copyOf(row[i], result[main.length + i].length);
        }
        return result;
    }

    // sum by rows, 1st[] is row, 2nd[] is column
    public static Number[] rowSums(Number[][] matrix) {
        Number[] rowSums = new Number[matrix.length];
        for (int r = 0; r < matrix.length; r++) {
            rowSums[r] = NumberUtil.sum(matrix[r]);
        }
        return rowSums;
    }

    // sum by columns, 1st[] is row, 2nd[] is column
    public static Number[] colSums(Number[][] matrix) {
        Number[] colSums = new Number[matrix[0].length];
        for (int c = 0; c < matrix[0].length; c++) {
            colSums[c] = 0;
            for (int r = 0; r < matrix.length; r++)
                NumberUtil.sum(colSums[c], matrix[r][c]);
        }
        return colSums;
    }

    public static Number sum(Number[][] matrix) {
        return NumberUtil.sum(rowSums(matrix));
    }

}
