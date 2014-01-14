package nzgo.toolkit.core.util;

import beast.util.TreeParser;
import nzgo.toolkit.core.logger.MyLogger;

import java.util.List;

/**
 * manipulate newick tree exported from Geneious,
 * which wrap node label by '
 * @author Walter Xie
 */
public class GeneiousTreeUtil extends TreeUtil{

    public static String cleanGeneiousTreeOutput(String newickTree) {
        return newickTree.replaceAll("'", "");
    }

    //Main method
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        final String stem = "tree-pairwise-ga-clustering";

        String rawNewickTree = getRawNewickTree(workPath, stem);
        String cleanedNewickTree = cleanGeneiousTreeOutput(rawNewickTree);
        TreeParser newickTree = getTreeFromOTUs(workPath, cleanedNewickTree);

//        writeNexusTree(newickTree.getRoot().toNewick() + ";", workPath + "new-" + stem + NameSpace.POSTFIX_NEX);

        List<String> traits = getTraits(newickTree);
        writeTaxaTable(traits, "order", workPath + "taxaTable.txt");
    }

    //Main method: Uncleaned Geneious Tree
//    public static void main(final String[] args) throws Exception {
//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");
//
//        String workPath = args[0];
//        MyLogger.info("\nWorking path = " + workPath);
//
//        final String stem = "tree-pairwise-ga-clustering";
//
//        List<String> driftingOTUs = getMixedOTUs(workPath + "clusters.uc");
//
//        File treeFile = new File(workPath + stem + NameSpace.POSTFIX_NEWICK);
//
//        BufferedReader reader = Importer.getReader(treeFile, "tree");
//        String tree = reader.readLine();
//        TreeParser newickTree = cleanGeneiousTreeOutput(tree);
//        List<String> traits = getTraits(newickTree);
//        printTraits(traits);
//
//        StringTokenizer st = new StringTokenizer(tree, "'");
//        reader.close();
//
//        StringBuffer newTree = new StringBuffer();
//
//        int i = 0;
//        String label = null;
//        while (st.hasMoreTokens()) {
//            String token = st.nextToken();
//            if (i % 2 != 0) {
////                label = complementTaxon(token, workPath);
//                label = simplifyLabel(token);
//            }
//
//            if (i > 0) {
//                newTree.append("'");
//
//                if (i % 2 == 0) {
//                    String metaDataString = getMetaString(label, driftingOTUs);
//                    newTree.append("[&").append(metaDataString).append("]");
//                }
//            }
//
//            if (i % 2 != 0) {
//                newTree.append(label);
//            } else {
//                newTree.append(token);
//            }
//
//            i++;
//        }
//
////        writeNexusTree(workPath + "new-" + stem + NameSpace.POSTFIX_NEX, newTree.toString());
//
//    }

}
