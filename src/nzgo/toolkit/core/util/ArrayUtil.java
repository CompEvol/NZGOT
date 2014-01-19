package nzgo.toolkit.core.util;

import java.util.Arrays;

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

    public static <T> T[] combineArrays(T[] array1, T[] array2) {
        T[] combinedArray = Arrays.copyOfRange(array2, array1.length, array1.length + array2.length);
        for (int i=0; i < array1.length; i++) {
            combinedArray[i] = array1[i];
        }
        return combinedArray;
    }
}
