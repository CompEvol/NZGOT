package nzgo.toolkit.core.uparse;

import nzgo.toolkit.core.naming.Separator;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

    public static Set<String> getSamples(List<String> labels, String regex) {
        Set<String> samples = new TreeSet<>();

        for (String label :labels) {
            String sample = label.replaceAll(regex, "");
            samples.add(sample);
        }

        return samples;
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
    public static int getAnnotatedSize(String name) {
        String size = name.replaceFirst(REGEX_SIZE, "$1");

        if (size != null && size.length() != name.length())
            return Integer.parseInt(size);
        else
            return 0;
    }
}
