package nzgo.toolkit.core.uparse;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.Separator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nzgo.toolkit.core.io.FileIO.getReader;

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

    private static UPParser upParser = new UPParser( );

    //prevents any other class from instantiating
    private UPParser(){ }

    // Static 'instance' method
    public static UPParser getInstance( ) {
        return upParser;
    }

    // read out.up into HashMap, key is unique sequence as the member of OTU, value is OTU
    public HashMap<String, String> createOTUsMap(List<String> otus, Path outUpPath) {
        if (!outUpPath.endsWith(NameSpace.SUFFIX_UP))
            throw new IllegalArgumentException("The UP mapping file is required ! " + outUpPath.getFileName());

        HashMap<String, String> uniq_seq_to_otu = new HashMap<>();
        Separator separator = new Separator("\t");
        int nrow = 0;
        try {
            BufferedReader reader = getReader(outUpPath, "OTUs UP mapping file");

            String line = reader.readLine();
            while (line != null) {
                String[] items = separator.parse(line);
                nrow++;

                // validation
                if (items.length < OTU_COLUMN_ID + 1)
                    throw new IllegalArgumentException("Invalid file format or separator to get " +
                            items.length + " columns, but expecting at least " +
                            (OTU_COLUMN_ID + 1) + " columns in row " + nrow);

                // rm size annotation
                String memberID = getLabelNoSizeAnnotation(items[QUERY_COLUMN_ID]);
                String otuID = getLabelNoSizeAnnotation(items[OTU_COLUMN_ID]);

                if (items[Classification_COLUMN_ID].equalsIgnoreCase(OTU_MEMBER) &&
                        otus.contains(otuID))
                    uniq_seq_to_otu.put(memberID, otuID);

                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyLogger.info("Match " + uniq_seq_to_otu.size() + " unique sequences to " +
                otus.size() + " OTUs from " + nrow + " lines in mapping file.");

        Set<String> uniq_v = new HashSet<>(uniq_seq_to_otu.values());
        if (otus.size() != uniq_v.size())
            throw new RuntimeException("OTUs (" + otus.size() + ") in UP mapping file" +
                    " does not match given OTUs (" + uniq_v.size() + ") !");

        return uniq_seq_to_otu;
    }

//    @Deprecated
//    public static List<String> getMembers(String otu, DataFrame<String> out_up) {
//        return out_up.getColDataEqualToAnd(QUERY_COLUMN_ID, OTU_MEMBER, Classification_COLUMN_ID, otu, OTU_COLUMN_ID);
//    }

}
