package nzgot.core.uc;

/**
 * USEARCH cluster format (UC) is a tab-separated text file
 * http://www.drive5.com/usearch/manual/ucout.html
 * @author Walter Xie
 */
public class UCParser {

    public static final String POSTFIX_UC = ".uc";

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

    public static final String COLUMN_SEPARATOR = "\t";

    public static boolean isUCFile(String fileName) {
        return fileName.endsWith(POSTFIX_UC);
    }

    public static boolean isNA(String field) {
        return field.trim().contentEquals(NA);
    }
}
