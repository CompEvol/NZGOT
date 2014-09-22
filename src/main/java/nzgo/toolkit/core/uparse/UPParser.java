package nzgo.toolkit.core.uparse;

/**
 * Output file, UPARSE tabbed format. Supported by cluster_otus and uparse_ref.
 * http://drive5.com/usearch/manual8/opt_uparseout.html
 * @author Walter Xie
 */
public class UPParser extends Parser {
    // Classification
    public static final String OTU = "otu";
    public static final String OTU_MEMBER = "match";
    public static final String CHIMERA = "chimera";

    public static final int QUERY_COLUMN_ID = 0;
    public static final int Classification_COLUMN_ID = 1;
    public static final int Identity_COLUMN_ID = 2;
    public static final int OTU_COLUMN_ID = 4;

}
