package nzgo.toolkit.core.io;

import beast.evolution.tree.Tree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Tree FileIO
 * @author Walter Xie
 */
public class TreeFileIO extends FileIO {

    public static String importNewickTree(Path treeFile) throws IOException {
        BufferedReader reader = getReader(treeFile, "tree");
        String newickTree = reader.readLine();
        reader.close();

        return newickTree;
    }

    /**
     * write Newick tree into Nexus tree
     * @param treeFile
     * @param newickTree
     * @throws IOException
     */
    public static void writeNexusTree(Path treeFile, Tree newickTree) throws IOException {

        BufferedWriter writer = getWriter(treeFile, "Nexus tree");

        writer.write("#nexus\n" + "Begin trees;\n");
        writer.write("tree = " + newickTree.getRoot().toNewick() + ";\n");
        writer.write("End;\n");
        writer.flush();
        writer.close();
    }

}
