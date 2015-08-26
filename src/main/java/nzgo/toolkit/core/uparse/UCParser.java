package nzgo.toolkit.core.uparse;

import nzgo.toolkit.core.r.DataFrame;

import java.util.List;

/**
 * USEARCH cluster format (UC) is a tab-separated text file
 * http://www.drive5.com/usearch/manual/ucout.html
 * @author Walter Xie
 */
public class UCParser extends Parser {

    public static final String HIT = "H";
    public static final String Centroid = "S";
    public static final String Cluster_Record = "C";
    public static final String NO_HIT = "N";

    public static final int Record_Type_COLUMN_ID = 0;
    public static final int Cluster_Number_COLUMN_ID = 1;
    public static final int H_Identity_COLUMN_ID = 3;
    public static final int Query_Sequence_COLUMN_ID = 8;
    public static final int Target_Sequence_COLUMN_ID = 9;

    public static List<String> getDuplicateSequences(String uniqueSequence, DataFrame<String> derep_uc) {
         return derep_uc.getColDataEqualTo(Query_Sequence_COLUMN_ID, uniqueSequence, Target_Sequence_COLUMN_ID);
    }
}
