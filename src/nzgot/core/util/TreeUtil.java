package nzgot.core.util;

import beast.evolution.tree.Node;
import beast.util.TreeParser;
import nzgot.core.io.Importer;

import java.io.*;
import java.util.List;

/**
 * standard newick tree
 * depend on BEAST 2
 * @author Walter Xie
 */
public class TreeUtil {




    //Main method
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        System.out.println("\nWorking path = " + workPath);

        List<String> driftingOTUs = getDriftOTUs(workPath + "clusters.uc");

        File treeFile = new File(workPath + "tree.newick");

        BufferedReader reader = Importer.getReader(treeFile, "tree");
        TreeParser newickTree = new TreeParser(reader.readLine(), false, false, true, 1);
        reader.close();

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String label = simplifyLabel(leafNode.getID());
            String metaDataString = getMetaString(label, driftingOTUs);

            leafNode.setID(label);
            leafNode.metaDataString = metaDataString;
        }

        writeNexusTree(workPath+"new-tree.nex", newickTree.toString());
    }

    protected static List<String> getDriftOTUs(String ucFilePath) {
        File ucFile = new File(ucFilePath);
        UCParser ucParser = new UCParser(ucFile);

        List<String> driftingOTUs = ucParser.getDriftingOTUs();
        for (int i = 0; i < driftingOTUs.size(); i++) {
            driftingOTUs.set(i, simplifyLabel(driftingOTUs.get(i)));
        }

        System.out.println(driftingOTUs.size() + " Drifting OTUs = " + driftingOTUs);

        return driftingOTUs;
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

    protected static String getMetaString(String label, List<String> driftingOTUs) {
        if (driftingOTUs.contains(label)) {
            return "col=DRIFT";
        } else {
            char c = label.charAt(0);
            if (Character.isDigit(c)) {
                return "col=GO";
            } else {
                return "col=BOLD";
            }
        }
    }

    protected static void writeNexusTree(String nexusFilePath, String newickTree) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(nexusFilePath));
        out.write("#nexus\n" + "Begin trees;\n");
        out.write("tree = " + newickTree + "\n");
        out.write("End;\n");
        out.flush();
        out.close();
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

}
