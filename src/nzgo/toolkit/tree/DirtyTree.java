package nzgo.toolkit.tree;

import java.util.Arrays;

/**
 * The dirty newick tree input from other tools,
 * which contains invalid characters
 * @author Walter Xie
 */
public enum DirtyTree {

    GENEIOUS("Geneious"),
    FASTTREE("FastTree");

    private String output;

    private DirtyTree(String output) {
        this.output = output;
    }

    public static void cleanDirtyTreeOutput(String newickTree, String dirtyInput) {
        if (dirtyInput != null) {
            String dirtyTree = newickTree;

            if (GENEIOUS.toString().equalsIgnoreCase(dirtyInput)) {
                newickTree = cleanGeneiousTreeOutput(dirtyTree);
            } else {
                newickTree = cleanFastTreeOutput(dirtyTree);
            }
        }
    }

    public static String cleanGeneiousTreeOutput(String newickTree) {
        return newickTree.replaceAll("'", "");
    }

    public static String cleanFastTreeOutput(String newickTree) {
        // e.g. Coleoptera:0.13579)0.909:0.03425)0.963:0.04190,
        // replace )0.909: to ):
        return newickTree.replaceAll("\\)[0..1]\\.\\d+:", "):");
    }

    @Override
    public String toString() {
        return output;
    }

    public static String[] valuesToString() {
        return Arrays.copyOf(DirtyTree.values(), DirtyTree.values().length, String[].class);
    }
}
