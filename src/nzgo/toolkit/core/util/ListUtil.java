package nzgo.toolkit.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * ListUtil
 * @author Walter Xie
 */
public class ListUtil {

    /**
     * use List<T[]> as 2 d matrix
     * @param list
     * @param columnIndex
     * @param <T>
     * @return
     */
    public static <T> List<T> getColumnOf(List<T[]> list, int columnIndex) {
        List<T> column = new ArrayList<>();
        for (T[] rows : list) {
            column.add(rows[columnIndex]);
        }
        return column;
    }

    public static <T> T[] getFirstRowMatch(List<T[]> list, int columnIndex, T value) {
        for (T[] rows : list) {
            if (rows[columnIndex].equals(value))
                return rows;
        }
        return null;
    }
}
