package nzgo.toolkit.core.tree;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import nzgo.toolkit.core.io.ConfigFileIO;
import nzgo.toolkit.core.io.TreeFileIO;
import nzgo.toolkit.core.naming.NameParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Tree Annotation
 * tips: create another TreeAnnotation by loading annotated newickTree and new lineParser
 * for multi-annotation
 * @author Walter Xie
 */
public class TreeAnnotation {

    public static final String TRAIT_EQUAL = "trait=";

    public final Tree newickTree;
    public final NameParser nameParser;

    // for input, and affect getMetaStringFrom(leafNode)
    protected Map<String, String> preTaxaTraits;
    // for output
    protected String[][] taxaTraits;

    public TreeAnnotation(Tree newickTree, NameParser nameParser) {
        this.newickTree = newickTree;
        this.nameParser = nameParser;
    }

    public void setTaxaTraits() {
        taxaTraits = new String[newickTree.getLeafNodeCount()][2];

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String metaDataString = getMetaStringFrom(leafNode.getID());
            taxaTraits[i][0] = leafNode.getID();
            taxaTraits[i][1] = metaDataString;
//            taxaTraits[i][2] = size

            if (leafNode.metaDataString != null && leafNode.metaDataString.length() > 1)
                metaDataString = ", " + metaDataString;

            leafNode.metaDataString = metaDataString;
        }
    }

    // fire setTaxaTraits()
    public String[][] getTaxaTraits() {
        if (taxaTraits == null)
            setTaxaTraits();
        return taxaTraits;
    }

    /**
     * if preTaxaTraits not null, use its mapping first
     * @param leafNode
     * @return
     */
    public String getMetaStringFrom(String leafNode) {
        String trait = null;
        if (preTaxaTraits != null)
            trait = preTaxaTraits.get(leafNode);

        if (trait == null)
            trait = nameParser.getFinalItem(leafNode);

        return TRAIT_EQUAL + trait;
    }

    /**
     * import pre-defined traits map
     * @param traitsMapTSV
     * @throws IOException
     */
    public void importPreTaxaTraits(Path traitsMapTSV) throws IOException {
        preTaxaTraits = ConfigFileIO.importPreTaxaTraits(traitsMapTSV);
    }

    /**
     * write traits map to file
     * @param traitsMapTSV
     * @throws IOException
     */
    public void writeTaxaTraits(Path traitsMapTSV) throws IOException {
        ConfigFileIO.writeTaxaTraits(traitsMapTSV, getTaxaTraits());
    }

    /**
     * output annotated tree into .nex
     * @param treeFile
     * @throws IOException
     */
    public void writeNexusTree(Path treeFile) throws IOException {
        TreeFileIO.writeNexusTree(treeFile, newickTree);
    }


}
