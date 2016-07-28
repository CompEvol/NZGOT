package nzgo.toolkit.core.util;

import java.lang.reflect.Array;
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

    @Deprecated
    public static <T> T[] combineArrays(T[] array1, T[] array2, Class<? extends T> elementType) {
        final T[] combinedArray = (T[]) Array.newInstance(elementType, array1.length + array2.length);
        for (int i=0; i < array1.length; i++) {
            combinedArray[i] = array1[i];
        }
        for (int i=0; i < array2.length; i++) {
            combinedArray[i+array1.length] = array2[i];
        }
        return combinedArray;
    }

    // http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java
    public static <T> T[] concatenate(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static <T> String toString(String delimiter, T[] array) {
        if (array == null) return null;

        String aToS = "";
        for (T el : array) {
            aToS += el.toString() + delimiter;
        }
        if (aToS.length() > 0)
            aToS = aToS.substring(0, aToS.lastIndexOf(delimiter));
        return aToS;
    }

    public static <T> String toString(T... array) {
        if (array == null) return null;

        String aToS = "";
        if (array.length > 1)  aToS += "{";
        for (T el : array) {
            aToS += el.toString() + ", ";
        }
        if (aToS.lastIndexOf(", ") > 0)
            aToS = aToS.substring(0, aToS.lastIndexOf(", "));
        if (array.length > 1) aToS += "}";
        return aToS;
    }

}
