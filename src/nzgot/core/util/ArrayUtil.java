package nzgot.core.util;

/**
 * ArrayUtil
 * @author Walter Xie
 */
public class ArrayUtil {

    public static int indexOf(Object o, Object[] array) {
        for (int i = 0; i < array.length; i++) {
            if (o.equals(array[i])) return i;
        }
        return -1;
    }
}
