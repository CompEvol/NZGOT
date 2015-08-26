package nzgo.toolkit.core.math;


/**
 * Arithmetic
 * @author Walter Xie
 */
public class Arithmetic {

    public static double sum(double[] array) {
        double sum = 0;
        for (double d : array)
            sum += d;
        return sum;
    }

    public static int sum(int[] array) {
        int sum = 0;
        for (int d : array)
            sum += d;
        return sum;
    }

    public static double mean(double[] array) {
        if (array == null || array.length < 1)
            return 0;
        return sum(array)/array.length;
    }

    public static int mean(int[] array) {
        if (array == null || array.length < 1)
            return 0;
        return sum(array)/array.length;
    }

    public static double max(double[] array) {
        double max = Double.MIN_VALUE;
        for (double d : array)
            if (max < d)
                max = d;
        return max;
    }

    public static double min(double[] array) {
        double min = Double.MAX_VALUE;
        for (double d : array)
            if (min > d)
                min = d;
        return min;
    }

}
