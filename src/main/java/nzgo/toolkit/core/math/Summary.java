package nzgo.toolkit.core.math;


/**
 * Summary
 * @author Walter Xie
 */
public class Summary {

    public static int frequency(double value, double[] array) {
        int frequency = 0;
        for (double d : array) {
            if (d == value)
                frequency++;
        }
        return frequency;
    }

    public static int frequency(int value, int[] array) {
        int frequency = 0;
        for (int d : array) {
            if (d == value)
                frequency++;
        }
        return frequency;
    }

    public static int singleton(double[] array) {
        return frequency(1.0, array);
    }

    public static int coupleton(double[] array) {
        return frequency(2.0, array);
    }
}
