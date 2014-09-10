package nzgo.toolkit.core.uc;

import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.Separator;

/**
 * USEARCH cluster format (UC) is a tab-separated text file
 * http://www.drive5.com/usearch/manual/ucout.html
 * @author Walter Xie
 */
public class UCParser {

    public static final String HIT = "H";
    public static final String Centroid = "S";
    public static final String Cluster_Record = "C";
    public static final String NO_HIT = "N";
    public static final String NA = "*";

    public static final int Record_Type_COLUMN_ID = 0;
    public static final int Cluster_Number_COLUMN_ID = 1;
    public static final int H_Identity_COLUMN_ID = 3;
    public static final int Query_Sequence_COLUMN_ID = 8;
    public static final int Target_Sequence_COLUMN_ID = 9;

    public static final String REGEX_SIZE_ANNOTATION = ";?size=\\d+;?";
    public static final String REGEX_SIZE = ".*size=(\\d*).*";

    protected final Separator lineSeparator = new Separator("\t");

    public static boolean isUCFile(String fileName) {
        return fileName.endsWith(NameSpace.SUFFIX_UC);
    }

    public static void validateUCFile(String fileName) {
        if (!isUCFile(fileName))
            throw new IllegalArgumentException("Invalid uc mapping file name : " + fileName);
    }

    public static boolean isNA(String field) {
        return field.trim().contentEquals(NA);
    }

    public static String getLabelNoSizeAnnotation(String label) {
        return label.replaceAll(REGEX_SIZE_ANNOTATION, "");
    }

    public static String getLabel(String label, boolean removeSizeAnnotation) {
        return removeSizeAnnotation ? getLabelNoSizeAnnotation(label) : label;
    }

    public static double getIdentity(String identity) {
        if (identity.length() > 0 && !UCParser.isNA(identity))
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
