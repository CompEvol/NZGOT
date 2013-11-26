package nzgot.core.util;

import nzgot.core.io.Importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

/**
 * manipulate newick tree exported from Geneious,
 * which wrap node label by '
 * @author Walter Xie
 */
public class GeneiousTreeUtil extends TreeUtil{




    //Main method
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        System.out.println("\nWorking path = " + workPath);

        List<String> driftingOTUs = getDriftOTUs(workPath + "clusters.uc");

        File treeFile = new File(workPath + "tree-geneious.newick");

        BufferedReader reader = Importer.getReader(treeFile, "tree");
        StringTokenizer st = new StringTokenizer(reader.readLine(), "'");
        reader.close();

        StringBuffer newTree = new StringBuffer();

        int i = 0;
        String label = null;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (i % 2 != 0) {
//                label = complementTaxon(token, workPath);
                label = simplifyLabel(token);
            }

            if (i > 0) {
                newTree.append("'");

                if (i % 2 == 0) {
                    String metaDataString = getMetaString(label, driftingOTUs);
                    newTree.append("[&").append(metaDataString).append("]");
                }
            }

            if (i % 2 != 0) {
                newTree.append(label);
            } else {
                newTree.append(token);
            }

            i++;
        }

        writeNexusTree(workPath + "new-tree-geneious.nex", newTree.toString());
    }

}
