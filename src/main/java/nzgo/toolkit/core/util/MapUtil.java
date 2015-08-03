package nzgo.toolkit.core.util;


import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * MapUtil
 *
 * @author Walter Xie
 */
public class MapUtil {

    // a generic method that returns a SortedSet of Map.Entry, given a Map whose values are Comparable
    // refer to http://stackoverflow.com/questions/2864840/treemap-sort-by-value, modified by Walter
    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>>
    entriesSortedByValues(Map<K, V> map, final boolean ascending) {
        final int a = ascending ? 1 : -1;
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue());
                        return res != 0 ? a * res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
