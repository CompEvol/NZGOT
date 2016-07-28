package nzgo.toolkit.core.io;

import beast.evolution.tree.Tree;
import beast.util.TreeParser;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.tree.DirtyTree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
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

        if (!newickTree.endsWith(");"))
            throw new IllegalArgumentException("Invalid Newick tree : " + newickTree);

        return newickTree;
    }

    public static Tree importNewickTree(Path treeFile, String dirtyInput) throws Exception {
        String newickTree = importNewickTree(treeFile);

        // if dirtyInput is null, do nothing
        DirtyTree.cleanDirtyTreeOutput(newickTree, dirtyInput);

        return new TreeParser(newickTree, false, false, true, 1);
    }

    public static void writeNewickTree(Path treeFile, Tree newickTree) throws IOException {
        MyLogger.info("\nCreating Newick tree ..." + treeFile);

        BufferedWriter writer = getWriter(treeFile, "Newick tree");

        writer.write(newickTree.getRoot().toNewick() + ";\n");
        writer.flush();
        writer.close();
    }

    /**
     * write Newick tree into Nexus tree
     * @param treeFile
     * @param newickTree
     * @throws IOException
     */
    public static void writeNexusTree(Path treeFile, Tree newickTree) throws IOException {
        MyLogger.info("\nCreating Nexus tree ..." + treeFile);

        BufferedWriter writer = getWriter(treeFile, "Nexus tree");

        writer.write("#nexus\n" + "Begin trees;\n");
        writer.write("tree = " + newickTree.getRoot().toNewick() + ";\n");
        writer.write("End;\n");
        writer.flush();
        writer.close();
    }

    /**
     * change newick tree into nexus tree
     *
     * @param newickTree
     * @param nexusFilePath
     * @throws java.io.IOException
     */
    public static void writeNexusTree(String newickTree, String nexusFilePath) throws IOException {
        MyLogger.info("\nCreating Nexus tree ...");

        BufferedWriter out = new BufferedWriter(new FileWriter(nexusFilePath));
        out.write("#nexus\n" + "Begin trees;\n");
        out.write("tree = " + newickTree + "\n");
        out.write("End;\n");
        out.flush();
        out.close();
    }
}
