package nzgo.toolkit.core.uparse;

import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.r.DataFrame;

import java.util.*;

/**
 * Parse USEARCH output
 * @author Walter Xie
 */
public class Parser {
    public static final String NA = "*";
    public static final String REGEX_SIZE_ANNOTATION = ";?size=\\d+;?";
    public static final String REGEX_SIZE = ".*size=(\\d*).*";

    protected final Separator lineSeparator = new Separator("\t");

    public static boolean isNA(String field) {
        return field.trim().contentEquals(NA);
    }

    public static String getLabelNoSizeAnnotation(String label) {
        return label.replaceAll(REGEX_SIZE_ANNOTATION, "");
    }

    public static String getLabel(String label, boolean removeSizeAnnotation) {
        return removeSizeAnnotation ? getLabelNoSizeAnnotation(label) : label;
    }

    public static void rmSizeAnnotation(DataFrame<String> out, int col) {
        List<String> colData = out.getColData(col);
        for (int i = 0; i < colData.size(); i++) {
            String val = colData.get(i);
            colData.set(i, getLabelNoSizeAnnotation(val));
        }
        out.replaceCol(col, colData);
    }

    // assume sample name is the 1st element, chop the end
    public static String getSample(String label, String regex) {
        return label.replaceAll(regex, "");
    }

    public static String getSample(String label, Separator separator, int i) {
        String[] items = separator.parse(label);
        return items[i];
    }

    public static Set<String> getSamples(Collection<String> labels, String regex, boolean sort) {
        Set<String> samples;
        if (sort)
            samples = new TreeSet<>();
        else
            samples = new HashSet<>();

        for (String label :labels) {
            String sample = getSample(label, regex);
            samples.add(sample);
        }
        return samples;
    }

    public static long getAnnotatedSize(String label) {
        String size = label.replaceFirst(REGEX_SIZE, "$1");

        if (size != null && size.length() != label.length())
            return Long.parseLong(size);
        else
            return 0;
    }

    // need to remove size annotation to determine if they are same label
    public static boolean isSameSequence(String label1, String label2) {
        return getLabelNoSizeAnnotation(label1).equalsIgnoreCase(getLabelNoSizeAnnotation(label2));
    }

    public static double getIdentity(String identity) {
        if (identity.length() > 0 && !Parser.isNA(identity))
            return Double.parseDouble(identity);
        else
            return 0;
    }

    /**
     * deal with size annotation from UC file
     * @param name
     * @return
     */
    public static int getAnnotatedSizeInt(String name) {
        String size = name.replaceFirst(REGEX_SIZE, "$1");

        if (size != null && size.length() != name.length())
            return Integer.parseInt(size);
        else
            return 0;
    }
}
