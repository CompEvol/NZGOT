package nzgot.core.util;

/**
 * ArrayUtil
 * @author Walter Xie
 */
public class ArrayUtil {

    public static <T> int indexOf(T o, T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (o.equals(array[i])) return i;
        }
        return -1;
    }

    public static int indexOfMax(int[] array) {
        int max = array[0];
        int maxId = 0;
        for (int i = 1; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
                maxId = i;
            }
        }
        return maxId;
    }

}
