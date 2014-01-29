package nzgo.toolkit.core.tree;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.TreeParser;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.TreeFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.Assembler;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.naming.SampleNameParser;
import nzgo.toolkit.core.taxonomy.Rank;
import nzgo.toolkit.core.taxonomy.Taxa;
import nzgo.toolkit.core.taxonomy.TaxaSort;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.uc.MixedOTUs;
import nzgo.toolkit.core.util.BioSortedSet;
import nzgo.toolkit.core.util.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * standard newick tree
 * depend on BEAST 2
 * @author Walter Xie
 */
public class TreeUtil {

    public static SampleNameParser sampleNameParser = new SampleNameParser(); //TODO


    public static void assembleTreeTaxa(Tree newickTree, Assembler assembler) {
        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String taxon = assembler.getAssembledLabel(leafNode.getID());

            if (!NameUtil.isEmptyNull(taxon))
                leafNode.setID(taxon);
        }
    }

    /**
     * get traits from the tree leave nodes' labels
     * TODO generalize to select any type of trait
     * @param newickTree
     * @return
     */
    public static BioSortedSet<Element> getTaxaTraitsFromTree(Tree newickTree) {
        BioSortedSet<Element> traits = new BioSortedSet<>("taxa");

        Element notIdentified = new Element(TaxaSort.Error.UNIDENTIFIED.toString());

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            // only work for taxon at moment
            String taxon = getTaxon(leafNode.getID());

            if (taxon == null || "null".equalsIgnoreCase(taxon)) {
//                MyLogger.warn("Find invalid taxon " + taxon + " from tip " + leafNode.getID());
                notIdentified.incrementCount(1);
            } else {
                if (!traits.containsUniqueElement(taxon)) {
                    Element countableTaxon = new Element(taxon);
                    countableTaxon.incrementCount(1); // default count 0
                    traits.add(countableTaxon);
                } else {
                    Element countableTaxon = traits.getUniqueElement(taxon);
                    countableTaxon.incrementCount(1);
                }
            }
        }
        if (notIdentified.getCount() > 0)
            traits.add(notIdentified);

        return traits;
    }

    /**
     * method to specify how to retrieve taxon from tip label
     * and add into traits list
     * overwrite for new traits
     * @param label
     * @return
     */
    protected static String getTaxon(String label) {
        char c = label.charAt(0);
//        String[] fields = sampleNameParser.parse(label);
        if (Character.isDigit(c)) {
//            if (fields.length < 10) return fields[8];
//            if (fields[9] == null) return fields[8];
//            if (fields[9].contentEquals("null")) return fields[8];
//            return fields[9];
            sampleNameParser.getSeparator(0).setSplitIndex(3); //TODO
        } else {
            sampleNameParser.getSeparator(0).setSplitIndex(1);
        }

        return sampleNameParser.getSeparator(0).getItem(label);
    }

    public static List<Element> getTaxaTraits(Tree newickTree) {
        // annotations to insert in nodes
        List<Element> taxaTraits = new ArrayList<>();

        Element notIdentified = new Element(TaxaSort.Error.UNIDENTIFIED.toString());

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            // only work for taxon at moment
            String taxon = getTaxon(leafNode.getID());

            if (taxon == null || "null".equalsIgnoreCase(taxon)) {
//                MyLogger.warn("Find invalid taxon " + taxon + " from tip " + leafNode.getID());
                notIdentified.incrementCount(1);
                taxaTraits.set(i, notIdentified);
            } else {
                Element countableTaxon;
                if (taxaTraits.contains(taxon)) {
                    countableTaxon = taxaTraits.get(i);
                } else {
                    countableTaxon = new Element(taxon);
                }
                countableTaxon.incrementCount(1);

//                for (Element trait : taxaTraits) {
//                    if (trait.compareTo(taxon) == 0) {
//                        countableTaxon = taxaTraits.get(i);
//                        countableTaxon.incrementCount(1);
//                        break;
//                    }
//                }
//                if (countableTaxon == null) {
//                    countableTaxon = new Element(taxon);
//                    countableTaxon.incrementCount(1);
//                }

                taxaTraits.set(i, countableTaxon);
            }
        }

        return taxaTraits;
    }

    protected static void printTraits(List traits) {
        MyLogger.info("\n" + traits.size() + " Taxa extracted from tree tips labels : ");
        for (Object taxon : traits) {
            MyLogger.info(taxon.toString());
        }
    }

    /**
     * TODO: generalize hard code to simplify label
     * @param label
     * @return
     */
    protected static String simplifyLabel(String label) {
        char c = label.charAt(0);
        if (Character.isDigit(c)) {
            String[] fields = sampleNameParser.getSeparator(0).parse(label);
            String taxon = fields[8];
            if (fields.length > 9 && fields[9] != null && !fields[9].contentEquals("null"))
                taxon = fields[9];
            return fields[0]+"|"+fields[1]+"|"+fields[7]+"|"+taxon;
        } else {
            return label;
        }
    }

    protected static void simplifyLabelsOfTree(Tree newickTree) {
        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String label = simplifyLabel(leafNode.getID());
            leafNode.setID(label);
        }
    }

    protected static String getMetaStringFrom(String label, List traits) {
        for (Object tr : traits) {
            if (label.contains(tr.toString()))
                return "trait=" + tr.toString();
        }
        return "trait=" + TaxaSort.Error.UNIDENTIFIED.toString();
    }

    protected static <T, E> String getMetaStringFrom(String label, Map<T, E> traits) {
        for (Map.Entry<T, E> entry : traits.entrySet()) {
            if (label.contains(entry.getKey().toString())) {
                return "trait=" + entry.getValue().toString();
            }
        }
        return "trait=" + TaxaSort.Error.UNIDENTIFIED.toString();
    }

    protected static List<String> getMixedOTUs(String ucFilePath) {
        File ucFile = new File(ucFilePath);
        MixedOTUs mixedOTUs = new MixedOTUs(ucFile.toPath(), ".*NZAC.*");
        mixedOTUs.reportMixedOTUs();

        List<String> mixedOTUsList = mixedOTUs.getMixedOTUs();
        for (int i = 0; i < mixedOTUsList.size(); i++) {
            mixedOTUsList.set(i, simplifyLabel(mixedOTUsList.get(i)));
        }

//        MyLogger.info(mixedOTUsList.size() + " Mixed OTUs = " + mixedOTUsList);

        return mixedOTUsList;
    }

    protected static String getMetaStringByDB(String label, List<String> mixedOTUs) {
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

        BufferedReader reader = FileIO.getReader(sequences, null);

        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                line = line.substring(1);
                String[] fields = sampleNameParser.getSeparator(0).parse(line);

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

    public static String getNewickTreeFromFile(File treeFile) throws IOException {
        BufferedReader reader = FileIO.getReader(treeFile, "tree");
        String newickTree = reader.readLine();
        reader.close();

        return newickTree;
    }

    public static void annotateTree(Tree newickTree, List traits) {

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String metaDataString = getMetaStringFrom(leafNode.getID(), traits);
            if (leafNode.metaDataString != null && leafNode.metaDataString.length() > 1)
                metaDataString = ", " + metaDataString;

            leafNode.metaDataString = metaDataString;
        }

    }

    public static void annotateTree(Tree newickTree, Map traits) {

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String metaDataString = getMetaStringFrom(leafNode.getID(), traits);
            if (leafNode.metaDataString != null && leafNode.metaDataString.length() > 1)
                metaDataString = leafNode.metaDataString + ", " + metaDataString;

            leafNode.metaDataString = metaDataString;
        }

    }

    protected static void annotateTreeByOTUs(Tree newickTree, List<String> mixedOTUs) {

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String metaDataString = getMetaStringByDB(leafNode.getID(), mixedOTUs);
            leafNode.metaDataString = metaDataString;
        }

    }

    public static void createTaxaBreakAndAnnotateTree(String workPath, String stem, String cleanedNewickTree) throws Exception {
        Tree newickTree = new TreeParser(cleanedNewickTree, false, false, true, 1);
        simplifyLabelsOfTree(newickTree);

        // annotate tree by database
        List<String> mixedOTUs = getMixedOTUs(workPath + "clusters.uc");
        annotateTreeByOTUs(newickTree, mixedOTUs);
        TreeFileIO.writeNexusTree(newickTree.getRoot().toNewick() + ";", workPath + "new-" + stem + NameSpace.SUFFIX_NEX);

        // taxa break
        Taxon bioClassification = new Taxon("Insecta", "50557");
        BioSortedSet<Element> taxaFromTree = getTaxaTraitsFromTree(newickTree);
        Taxa taxa = new Taxa(taxaFromTree);
        TaxaSort taxaSort = new TaxaSort(taxa, Rank.ORDER, bioClassification); // default to check prefix
        taxaSort.writeTaxaSortTable(workPath);

        // annotate tree by traits (taxa)
        annotateTree(newickTree, taxaSort.taxaSortMap);
        TreeFileIO.writeNexusTree(newickTree.getRoot().toNewick() + ";", workPath + "taxa-" + stem + NameSpace.SUFFIX_NEX);
    }


    //Main method
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        final String stem = "tree-pairwise-ga-clustering";

        File treeFile = new File(workPath + stem + NameSpace.SUFFIX_NEWICK);
        String newickTree = getNewickTreeFromFile(treeFile);

        DirtyTree.cleanDirtyTreeOutput(newickTree, DirtyTree.GENEIOUS.toString());

        createTaxaBreakAndAnnotateTree(workPath, stem, newickTree);
    }

}
