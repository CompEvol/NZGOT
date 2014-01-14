package nzgo.toolkit.core.util;

import beast.evolution.tree.Node;
import beast.util.TreeParser;
import nzgo.toolkit.core.community.util.NameSpace;
import nzgo.toolkit.core.io.Importer;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.uc.MixedOTUs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * standard newick tree
 * depend on BEAST 2
 * @author Walter Xie
 */
public class TreeUtil {

    /**
     * clean not standard format generated from FastTree
     * @param newickTree    FastTree output
     * @return              standard newick tree
     * @throws IOException
     */
    public static String cleanNewickTree(String newickTree) throws IOException {
        // e.g. Coleoptera:0.13579)0.909:0.03425)0.963:0.04190,
        // replace )0.909: to ):
        return newickTree.replaceAll("\\)[0..1]\\.\\d+:", "):");
    }

    /**
     * change newick tree into nexus tree
     * @param nexusFilePath
     * @param newickTree
     * @throws IOException
     */
    public static void writeNexusTree(String nexusFilePath, String newickTree) throws IOException {
        MyLogger.info("\nCreating Nexus tree ...");

        BufferedWriter out = new BufferedWriter(new FileWriter(nexusFilePath));
        out.write("#nexus\n" + "Begin trees;\n");
        out.write("tree = " + newickTree + "\n");
        out.write("End;\n");
        out.flush();
        out.close();
    }

    protected static List<String> getMixedOTUs(String ucFilePath) {
        File ucFile = new File(ucFilePath);
        MixedOTUs mixedOTUs = new MixedOTUs(ucFile);

        List<String> mixedOTUsList = mixedOTUs.getMixedOTUs();
        for (int i = 0; i < mixedOTUsList.size(); i++) {
            mixedOTUsList.set(i, simplifyLabel(mixedOTUsList.get(i)));
        }

//        MyLogger.info(mixedOTUsList.size() + " Mixed OTUs = " + mixedOTUsList);

        return mixedOTUsList;
    }

    protected static String simplifyLabel(String label) {
        char c = label.charAt(0);
        if (Character.isDigit(c)) {
            String[] fields = label.split("\\|", -1);
            return fields[0]+"|"+fields[1]+"|"+fields[7]+(fields.length > 9 ? "|"+fields[9] : "");
        } else {
            return label;
        }
    }

    protected static String getTaxon(String label) {
        char c = label.charAt(0);
        String[] fields = label.split("\\|", -1);
        if (Character.isDigit(c)) {
            if (fields.length < 10) return fields[8];
            if (fields[9] == null) return fields[8];
            if (fields[9].contentEquals("null")) return fields[8];
            return fields[9];
        } else {
            return fields[1];
        }
    }

    protected static String getMetaString(String label, List<String> mixedOTUs) {
        if (mixedOTUs.contains(label)) {
            return "col=MIXED";
        } else {
            char c = label.charAt(0);
            if (Character.isDigit(c)) {
                return "col=GO";
            } else {
                return "col=BOLD";
            }
        }
    }

    protected static String complementTaxon(String label, String workPath) throws IOException {

        File sequences = new File(workPath + "all.fasta");

        BufferedReader reader = Importer.getReader(sequences, null);

        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                line = line.substring(1);
                String[] fields = line.split("\\|", -1);

                if (fields.length < 2)
                    throw new IllegalArgumentException("Error: invalid sequence label in the line: " + line);

                if (line.startsWith(label)) {
                    char c = label.charAt(0);
                    if (Character.isDigit(c)) {
                        return fields[0]+"|"+fields[1]+"|"+fields[7]+(fields.length > 9 ? "|"+fields[9] : "");
                    } else {
                        return line;
                    }
                }

            }

            line = reader.readLine();
        }

        reader.close();

        return label;
    }

    protected static List<String> getTraits(TreeParser newickTree) {
        List<String> traits = new ArrayList<>();

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String taxon = getTaxon(leafNode.getID());

            if (!traits.contains(taxon)) traits.add(taxon);
        }

        return traits;
    }


    protected static void printTraits(String tree) throws Exception {

        TreeParser newickTree = new TreeParser(tree.replaceAll("'", ""), false, false, true, 1);
        List<String> traits = getTraits(newickTree);

        MyLogger.info("\n" + traits.size() + " Taxa extracted from tree tips labels : ");
        for (String taxon : traits) {
            MyLogger.info(taxon);
        }
    }

    //Main method
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        final String stem = "tree";

        List<String> mixedOTUs = getMixedOTUs(workPath + "clusters.uc");

        File treeFile = new File(workPath + stem + NameSpace.POSTFIX_NEWICK);

        BufferedReader reader = Importer.getReader(treeFile, "tree");
        String cleanedNewickTree = cleanNewickTree(reader.readLine());
        reader.close();
//        writeNexusTree(workPath+"tree-cleaned.nex", cleanedNewickTree);

        TreeParser newickTree = new TreeParser(cleanedNewickTree, false, false, true, 1);

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String label = simplifyLabel(leafNode.getID());
            String metaDataString = getMetaString(label, mixedOTUs);

            leafNode.setID(label);
            leafNode.metaDataString = metaDataString;
        }

        writeNexusTree(workPath + "new-" + stem + NameSpace.POSTFIX_NEX, newickTree.getRoot().toNewick() + ";");
    }


}
