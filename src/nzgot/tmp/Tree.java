package nzgot.tmp;

import nzgot.core.io.Importer;
import nzgot.core.util.UCParser;

import java.io.*;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Walter Xie
 */
public class Tree {




    //Main method
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        System.out.println("\nWorking path = " + workPath);

        File ucFile = new File(workPath + "clusters.uc");
        UCParser ucParser = new UCParser(ucFile);
        List<String> driftingOTUs = ucParser.getDriftingOTUs();

        File treeFile = new File(workPath + "Tree.newick");

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
                    if (driftingOTUs.contains(label)) {
                        newTree.append("[&col=blue]");
                    } else {
                        char c = label.charAt(0);
                        if (Character.isDigit(c)) {
                            newTree.append("[&col=red]");
                        } else {
                            newTree.append("[&col=green]");
                        }
                    }
                }
            }

            if (i % 2 != 0) {
                newTree.append(label);
            } else {
                newTree.append(token);
            }

            i++;
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(workPath + "newTree.nex"));
        out.write("Begin trees;\n");
        out.write(newTree.toString());
        out.write("End;\n");
        out.flush();
        out.close();
    }

    private static String simplifyLabel(String label) {
        char c = label.charAt(0);
        if (Character.isDigit(c)) {
            String[] fields = label.split("\\|", -1);
            return fields[0]+"|"+fields[1]+"|"+fields[7]+(fields.length > 9 ? "|"+fields[9] : "");
        } else {
            return label;
        }
    }

    private static String complementTaxon(String label, String workPath) throws IOException {

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
